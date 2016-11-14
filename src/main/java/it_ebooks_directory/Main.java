package it_ebooks_directory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import util.HtmlUnitUtil;

public class Main {

  private static int pacoteAtual = 1;
  private static String diretorioPacoteAtual = Util.getConfiguracao().getDiretorioDownload() + File.separator + "pacote" + pacoteAtual;

  @SuppressWarnings("unchecked")
  public static void main(String[] args) {

    WebClient webClientPaginaListagem = HtmlUnitUtil.novoWebClient();
    WebClient webClientPaginaLivro = HtmlUnitUtil.novoWebClient();
    HtmlPage paginaListagem = null;
    HtmlPage paginaLivro;
    HtmlImage image;
    String nomeLivro = "";
    int indicePagina = 1;
    String linkPaginaListagem = Util.getConfiguracao().getLinkSite();

    try {

      indicePagina = 1;

      do {

        paginaListagem = webClientPaginaListagem.getPage(linkPaginaListagem);
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "Acessando " + linkPaginaListagem);

        for (HtmlElement itemListaLivro : (List<HtmlElement>) paginaListagem.getByXPath("//div[@class='media']")) {
          try {
            paginaLivro = webClientPaginaLivro.getPage(Util.getConfiguracao().getLinkSite() + itemListaLivro.getElementsByTagName("a").get(0).getAttribute("href"));

            nomeLivro = paginaLivro.getElementsByTagName("h1").get(0).getTextContent().replaceAll(":", "").replaceAll("\\?", "").replaceAll("/", "").replaceAll("\\\\", "").replaceAll("#", "").replaceAll("$", "").replaceAll("\\*", "");

            image = (HtmlImage) paginaLivro.getFirstByXPath("//img[@class='img-polaroid']");

            inicializarDiretorios();

            downloadCapa(image, nomeLivro);
            downloadLivro((HtmlElement) paginaLivro.getElementById("download"), nomeLivro);
          } catch (IndexOutOfBoundsException e) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Erro na página do livro " + itemListaLivro.getElementsByTagName("a").get(0).getAttribute("href") + e.getMessage(), e);
          }
        }

        indicePagina++;
        linkPaginaListagem = Util.getConfiguracao().getLinkSite() + "page-" + indicePagina + ".html";
      } while (existeProximaPagina(paginaListagem));

    } catch (FailingHttpStatusCodeException e) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    } catch (MalformedURLException e) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    } catch (IOException e) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private static void downloadLivro(HtmlElement botaoDownload, String nomeLivro) {
    File arquivo = new File(diretorioPacoteAtual + File.separator + nomeLivro + ".pdf");
    InputStream inputStream;
    OutputStream outputStream;
    try {
      if (!arquivoExiste(nomeLivro + ".pdf")) {
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "Salvando livro " + nomeLivro);

        inputStream = botaoDownload.click().getWebResponse().getContentAsStream();

        outputStream = new FileOutputStream(arquivo);
        byte[] bytes = new byte[1024];
        int read;
        boolean primeiroStream = true;
        while ((read = inputStream.read(bytes)) != -1) {
          if (primeiroStream && (new String(bytes)).contains("DOCTYPE html")) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Limite de download diário do site foi atingido! ");
            System.exit(0);
          }
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

  private static void downloadCapa(HtmlImage imagem, String nomeLivro) throws IOException {
    String extensao = imagem.getSrcAttribute().substring(imagem.getSrcAttribute().length() - 3);

    File arquivo = new File(diretorioPacoteAtual + File.separator + nomeLivro + "." + extensao);

    if (!arquivoExiste(nomeLivro + "." + extensao)) {
      Logger.getLogger(Main.class.getName()).log(Level.INFO, "Salvando capa " + nomeLivro);

      URL url = new URL(Util.getConfiguracao().getLinkSite() + imagem.getAttribute("src"));
      InputStream inputStream = url.openStream();
      OutputStream outputStream = new FileOutputStream(arquivo);

      byte[] b = new byte[2048];
      int length;

      while ((length = inputStream.read(b)) != -1) {
        outputStream.write(b, 0, length);
      }

      inputStream.close();
      outputStream.close();

      Logger.getLogger(Main.class.getName()).log(Level.INFO, "Capa salva.");
    }
  }

  private static boolean arquivoExiste(String nomeArquivo) {

    int quantidadePacotes = 0;

    File diretorioRaiz = new File(Util.getConfiguracao().getDiretorioDownload());
    File listaDiretorios[] = diretorioRaiz.listFiles();
    for (int i = 0; i < listaDiretorios.length; i++) {
      if (listaDiretorios[i].isDirectory()) {
        quantidadePacotes++;
      }
    }

    for (int indice = quantidadePacotes; indice >= 1; indice--) {
      if (new File(Util.getConfiguracao().getDiretorioDownload() + File.separator + "pacote" + indice + File.separator + nomeArquivo).exists()) {
        return true;
      }
    }

    return false;
  }

  private static boolean existeProximaPagina(HtmlPage paginaListagem) {
    return paginaListagem.getFirstByXPath("//a[@class='btn btn-inverse pull-right']") != null;
  }

  private static void inicializarDiretorios() {

    new File(diretorioPacoteAtual).mkdirs();
    boolean limitePacote = false;

    try {
      if (Files.list(Paths.get(diretorioPacoteAtual)).count() >= (Util.getConfiguracao().getQuantidadeLivrosPorPacote() * 2)) {
        limitePacote = true;
      }
    } catch (IOException e) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    }

    if (limitePacote) {
      pacoteAtual++;
      diretorioPacoteAtual = Util.getConfiguracao().getDiretorioDownload() + File.separator + "pacote" + pacoteAtual;
      inicializarDiretorios();
    }
  }
}
