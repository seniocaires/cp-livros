package lelivros;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

import util.HtmlUnitUtil;

public class Main {

  public static void main(String[] args) {

    WebClient webClientPaginaInicial = HtmlUnitUtil.novoWebClient();
    WebClient webClientPaginaListagem = HtmlUnitUtil.novoWebClient();
    WebClient webClientPaginaLivro = HtmlUnitUtil.novoWebClient();
    HtmlPage paginaInicial = null;
    HtmlPage paginaListagem = null;
    HtmlPage paginaLivro;
    HtmlDivision links;
    HtmlImage image;
    RenderedImage buf;
    HtmlElement linkEPUB;
    HtmlElement linkMOBI;
    HtmlElement linkPDF;
    String nomeLivro = "";
    HtmlUnorderedList listaLivros;
    HtmlUnorderedList listaCategorias;
    String categoria;
    String linkCategoria;
    int indicePagina;

    try {
      paginaInicial = webClientPaginaInicial.getPage(Util.getConfiguracao().getLinkSite());
      listaCategorias = (HtmlUnorderedList) paginaInicial.getFirstByXPath("//ul[@class='product-categories']");
      for (HtmlElement itemCategoria : listaCategorias.getElementsByTagName("li")) {

        indicePagina = 1;
        linkCategoria = itemCategoria.getElementsByTagName("a").get(0).getAttribute("href");
        categoria = itemCategoria.getElementsByTagName("a").get(0).getTextContent().replaceAll("\\.", "");

        inicializarDiretorios(categoria);

        do {

          paginaListagem = webClientPaginaListagem.getPage(Util.getConfiguracao().getLinkSite() + linkCategoria + "page/" + indicePagina);
          Logger.getLogger(Main.class.getName()).log(Level.INFO, "Acessando " + Util.getConfiguracao().getLinkSite() + linkCategoria + "page/" + indicePagina);

          listaLivros = (HtmlUnorderedList) paginaListagem.getFirstByXPath("//ul[@class='products']");
          for (HtmlElement itemListaLivro : listaLivros.getElementsByTagName("li")) {
            try {
              paginaLivro = webClientPaginaLivro.getPage(itemListaLivro.getElementsByTagName("a").get(0).getAttribute("href"));

              nomeLivro = paginaLivro.getElementsByTagName("h1").get(0).getTextContent().replaceAll(":", "").replaceAll("\\?", "").replaceAll("/", "").replaceAll("\\\\", "");

              links = (HtmlDivision) paginaLivro.getByXPath("//div[@class='images']").get(0);
              image = (HtmlImage) links.getElementsByTagName("img").get(0);
              buf = getImage(image);

              links = (HtmlDivision) paginaLivro.getByXPath("//div[@class='links-download']").get(0);
              linkEPUB = links.getElementsByTagName("a").get(0);
              linkMOBI = links.getElementsByTagName("a").get(1);
              linkPDF = links.getElementsByTagName("a").get(2);

              if (Util.getConfiguracao().getDownloadCapas()) {
                downloadImage(image, nomeLivro, buf, categoria);
              }
              if (Util.getConfiguracao().getDownloadEPUB()) {
                downloadLivro(nomeLivro + ".epub", linkEPUB, Util.getConfiguracao().getSubDiretorioEPUB(), categoria);
              }
              if (Util.getConfiguracao().getDownloadMOBI()) {
                downloadLivro(nomeLivro + ".mobi", linkMOBI, Util.getConfiguracao().getSubDiretorioMOBI(), categoria);
              }
              if (Util.getConfiguracao().getDownloadPDF()) {
                downloadLivro(nomeLivro + ".pdf", linkPDF, Util.getConfiguracao().getSubDiretorioPDF(), categoria);
              }
            } catch (IndexOutOfBoundsException e) {
              Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Erro na página do livro " + itemListaLivro.getElementsByTagName("a").get(0).getAttribute("href") + e.getMessage(), e);
            }
          }

          indicePagina++;
        } while (existeProximaPagina(paginaListagem, indicePagina - 1));

      }
    } catch (FailingHttpStatusCodeException e) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    } catch (MalformedURLException e) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    } catch (IOException e) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private static void downloadImage(HtmlImage image, String nomeLivro, RenderedImage buf, String categoria) throws IOException {
    String extensao = image.getSrcAttribute().substring(image.getSrcAttribute().length() - 3);

    File arquivo = new File(Util.getConfiguracao().getDiretorioDownload() + File.separator + categoria + File.separator + Util.getConfiguracao().getSubDiretorioCapas() + File.separator + nomeLivro + "." + extensao);

    if (!arquivo.exists()) {
      Logger.getLogger(Main.class.getName()).log(Level.INFO, "Salvando capa " + nomeLivro);

      ImageIO.write(buf, extensao, arquivo);

      Logger.getLogger(Main.class.getName()).log(Level.INFO, "Capa salva.");
    }
  }

  private static void downloadLivro(String nomeLivro, HtmlElement link, String subDiretorio, String categoria) {
    File arquivo = new File(Util.getConfiguracao().getDiretorioDownload() + File.separator + categoria + File.separator + subDiretorio + File.separator + nomeLivro);
    InputStream inputStream;
    OutputStream outputStream;
    try {
      if (!arquivo.exists()) {
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "Salvando livro " + nomeLivro);

        inputStream = link.click().getWebResponse().getContentAsStream();

        outputStream = new FileOutputStream(arquivo);
        byte[] bytes = new byte[1024];
        int read;
        while ((read = inputStream.read(bytes)) != -1) {
          outputStream.write(bytes, 0, read);
        }
        outputStream.close();
        inputStream.close();
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "Livro salvo.");
      }
    } catch (IOException e) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private static RenderedImage getImage(HtmlImage image) throws IOException {
    ImageReader reader = image.getImageReader();
    int minIndex = reader.getMinIndex();
    return reader.read(minIndex);
  }

  private static boolean existeProximaPagina(HtmlPage paginaListagem, int numeroUltimaPaginaAcessada) {
    return paginaListagem.getFirstByXPath("//a[@class='nextpostslink']") != null;
  }

  private static void inicializarDiretorios(String categoria) {

    new File(Util.getConfiguracao().getDiretorioDownload(), categoria).mkdirs();

    if (Util.getConfiguracao().getDownloadCapas()) {
      new File(Util.getConfiguracao().getDiretorioDownload() + File.separator + categoria, Util.getConfiguracao().getSubDiretorioCapas()).mkdirs();
    }
    if (Util.getConfiguracao().getDownloadEPUB()) {
      new File(Util.getConfiguracao().getDiretorioDownload() + File.separator + categoria, Util.getConfiguracao().getSubDiretorioEPUB()).mkdirs();
    }
    if (Util.getConfiguracao().getDownloadPDF()) {
      new File(Util.getConfiguracao().getDiretorioDownload() + File.separator + categoria, Util.getConfiguracao().getSubDiretorioPDF()).mkdirs();
    }
    if (Util.getConfiguracao().getDownloadMOBI()) {
      new File(Util.getConfiguracao().getDiretorioDownload() + File.separator + categoria, Util.getConfiguracao().getSubDiretorioMOBI()).mkdirs();
    }
  }
}
