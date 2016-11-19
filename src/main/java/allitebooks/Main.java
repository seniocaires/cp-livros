package allitebooks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

import util.HtmlUnitUtil;

public class Main {

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {

    WebClient webClientPaginaInicial = HtmlUnitUtil.novoWebClient();
    WebClient webClientPaginaListagem = HtmlUnitUtil.novoWebClient();
    WebClient webClientPaginaLivro = HtmlUnitUtil.novoWebClient();
    HtmlPage paginaInicial = null;
    HtmlPage paginaListagem = null;
    HtmlPage paginaLivro;
    HtmlImage image;
    HtmlElement linkPDF;
    String nomeLivro = "";
    HtmlUnorderedList listaCategorias;
    String categoria;
    String linkCategoria;
    int indicePagina;

    try {
      paginaInicial = webClientPaginaInicial.getPage(Util.getConfiguracao().getLinkSite());
      listaCategorias = (HtmlUnorderedList) paginaInicial.getElementById("menu-categories");
      for (HtmlElement itemCategoria : listaCategorias.getElementsByTagName("li")) {

        indicePagina = 1;
        linkCategoria = itemCategoria.getElementsByTagName("a").get(0).getAttribute("href");
        categoria = itemCategoria.getElementsByTagName("a").get(0).getTextContent();

        inicializarDiretorios(categoria);

        do {

          paginaListagem = webClientPaginaListagem.getPage(linkCategoria + "page/" + indicePagina);
          Logger.getLogger(Main.class.getName()).log(Level.INFO, "Acessando " + linkCategoria + "page/" + indicePagina);

          for (HtmlElement itemListaLivro : (List<HtmlElement>) paginaListagem.getByXPath("//div[@class='entry-thumbnail hover-thumb']")) {
            try {
              paginaLivro = webClientPaginaLivro.getPage(itemListaLivro.getElementsByTagName("a").get(0).getAttribute("href"));

              nomeLivro = paginaLivro.getElementsByTagName("h1").get(0).getTextContent().replaceAll(":", "").replaceAll("\\?", "").replaceAll("/", "").replaceAll("\\\\", "").replaceAll("#", "").replaceAll("$", "").replaceAll("\\*", "").replaceAll("|", "");

              image = (HtmlImage) ((HtmlDivision) paginaLivro.getByXPath("//div[@class='entry-body-thumbnail hover-thumb']").get(0)).getElementsByTagName("img").get(0);

              linkPDF = ((HtmlSpan) paginaLivro.getByXPath("//span[@class='download-links']").get(0)).getElementsByTagName("a").get(0);

              if (Util.getConfiguracao().getDownloadCapas()) {
                downloadImage(image, nomeLivro, categoria);
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

  private static void downloadImage(HtmlImage image, String nomeLivro, String categoria) throws IOException {
    String extensao = image.getSrcAttribute().substring(image.getSrcAttribute().lastIndexOf(".") + 1);

    File arquivo = new File(Util.getConfiguracao().getDiretorioDownload() + File.separator + categoria + File.separator + Util.getConfiguracao().getSubDiretorioCapas() + File.separator + nomeLivro + "." + extensao);

    if (!arquivo.exists()) {
      Logger.getLogger(Main.class.getName()).log(Level.INFO, "Salvando capa " + nomeLivro);

      image.saveAs(arquivo);

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

  private static boolean existeProximaPagina(HtmlPage paginaListagem, int indicePagina) {
    return !(indicePagina + " / " + indicePagina + " Pages").equals(((HtmlElement) paginaListagem.getFirstByXPath("//span[@class='pages']")).getTextContent());
  }

  private static void inicializarDiretorios(String categoria) {

    new File(Util.getConfiguracao().getDiretorioDownload(), categoria).mkdirs();

    if (Util.getConfiguracao().getDownloadCapas()) {
      new File(Util.getConfiguracao().getDiretorioDownload() + File.separator + categoria, Util.getConfiguracao().getSubDiretorioCapas()).mkdirs();
    }
    if (Util.getConfiguracao().getDownloadPDF()) {
      new File(Util.getConfiguracao().getDiretorioDownload() + File.separator + categoria, Util.getConfiguracao().getSubDiretorioPDF()).mkdirs();
    }
  }
}
