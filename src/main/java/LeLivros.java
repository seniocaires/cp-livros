import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

public class LeLivros {

  private static final int NUMERO_PAGINA_INICIAL = 1;
  private static final int NUMERO_PAGINA_FINAL = 449;
  private static final String DIRETORIO_SALVAR_LIVROS = "C:\\Users\\UsrAdm\\Downloads\\lelivros\\";

  public static void main(String[] args) {

    WebClient webClientPaginaListagem = novoWebClient();
    WebClient webClientPaginaLivro = novoWebClient();
    HtmlPage paginaListagem;
    HtmlPage paginaLivro;
    HtmlDivision links;
    HtmlElement linkEPUB;
    HtmlElement linkMOBI;
    HtmlElement linkPDF;
    String nomeLivro;

    for (int indicePagina = NUMERO_PAGINA_INICIAL; indicePagina <= NUMERO_PAGINA_FINAL; indicePagina++) {

      try {

        paginaListagem = webClientPaginaListagem.getPage("http://lelivros.me/book/page/" + indicePagina);

        HtmlUnorderedList lista = (HtmlUnorderedList) paginaListagem.getFirstByXPath("//ul[@class='products']");
        for (HtmlElement itemLista : lista.getElementsByTagName("li")) {
          paginaLivro = webClientPaginaLivro.getPage(itemLista.getElementsByTagName("a").get(0).getAttribute("href"));
          links = (HtmlDivision) paginaLivro.getByXPath("//div[@class='links-download']").get(0);
          linkEPUB = links.getElementsByTagName("a").get(0);
          linkMOBI = links.getElementsByTagName("a").get(1);
          linkPDF = links.getElementsByTagName("a").get(2);
          nomeLivro = paginaLivro.getElementsByTagName("h1").get(0).getTextContent().replaceAll(":", "").replaceAll("\\?", "");

          download(nomeLivro + ".pdf", linkPDF);
        }

      } catch (FailingHttpStatusCodeException e) {
        Logger.getLogger(LeLivros.class.getName()).log(Level.SEVERE, e.getMessage(), e);
      } catch (MalformedURLException e) {
        Logger.getLogger(LeLivros.class.getName()).log(Level.SEVERE, e.getMessage(), e);
      } catch (IOException e) {
        Logger.getLogger(LeLivros.class.getName()).log(Level.SEVERE, e.getMessage(), e);
      } catch (IndexOutOfBoundsException e) {
        Logger.getLogger(LeLivros.class.getName()).log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }

  private static void download(String nomeArquivo, HtmlElement link) {
    File arquivo = new File(DIRETORIO_SALVAR_LIVROS + nomeArquivo);
    InputStream inputStream;
    OutputStream outputStream;
    try {
      if (!arquivo.exists()) {
        Logger.getLogger(LeLivros.class.getName()).log(Level.INFO, "Salvando livro " + nomeArquivo);

        inputStream = link.click().getWebResponse().getContentAsStream();

        outputStream = new FileOutputStream(arquivo);
        byte[] bytes = new byte[1024];
        int read;
        while ((read = inputStream.read(bytes)) != -1) {
          outputStream.write(bytes, 0, read);
        }
        outputStream.close();
        inputStream.close();
        Logger.getLogger(LeLivros.class.getName()).log(Level.INFO, "Livro salvo.");
      }
    } catch (IOException e) {
      Logger.getLogger(LeLivros.class.getName()).log(Level.SEVERE, e.getMessage(), e);
    }
  }

  private static WebClient novoWebClient() {
    WebClient retorno = new WebClient(BrowserVersion.CHROME);
    retorno.getOptions().setThrowExceptionOnScriptError(false);
    retorno.getOptions().setThrowExceptionOnFailingStatusCode(false);
    retorno.getOptions().setCssEnabled(false);
    retorno.getOptions().setDoNotTrackEnabled(true);
    retorno.getOptions().setGeolocationEnabled(false);
    retorno.getOptions().setJavaScriptEnabled(false);
    retorno.getOptions().setPopupBlockerEnabled(true);
    retorno.getOptions().setPrintContentOnFailingStatusCode(false);
    return retorno;
  }
}
