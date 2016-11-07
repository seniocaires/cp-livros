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

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

public class LeLivros {

	private static final int NUMERO_PAGINA_INICIAL = 1;
	private static final int NUMERO_PAGINA_FINAL = 6;
	private static final String DIRETORIO_SALVAR_LIVROS = "/tmp/teste";

	public static void main(String[] args) {

		WebClient webClientPaginaListagem = novoWebClient();
		WebClient webClientPaginaLivro = novoWebClient();
		HtmlPage paginaListagem;
		HtmlPage paginaLivro;
		HtmlDivision links;
		HtmlElement linkEPUB;
		HtmlElement linkMOBI;
		HtmlElement linkPDF;
		String nomeLivro = "";
		
		//criar os diretórios
		new java.io.File(DIRETORIO_SALVAR_LIVROS, "images").mkdirs();
		new java.io.File(DIRETORIO_SALVAR_LIVROS, "epub").mkdirs();
		new java.io.File(DIRETORIO_SALVAR_LIVROS, "pdf").mkdirs();
		new java.io.File(DIRETORIO_SALVAR_LIVROS, "mobi").mkdirs();

		for (int indicePagina = NUMERO_PAGINA_INICIAL; indicePagina <= NUMERO_PAGINA_FINAL; indicePagina++) {
			try {

				paginaListagem = webClientPaginaListagem
						.getPage("http://lelivros.me/categoria/administracao/page/" + indicePagina);
				Logger.getLogger(LeLivros.class.getName()).log(Level.INFO,
						"Acessando lelivros.me/categoria/administracao/page/" + indicePagina);

				HtmlUnorderedList lista = (HtmlUnorderedList) paginaListagem.getFirstByXPath("//ul[@class='products']");
				for (HtmlElement itemLista : lista.getElementsByTagName("li")) {
					try {
						paginaLivro = webClientPaginaLivro
								.getPage(itemLista.getElementsByTagName("a")
										.get(0)
										.getAttribute("href"));
						
						nomeLivro = paginaLivro.getElementsByTagName("h1")
								.get(0)
								.getTextContent()
								.replaceAll(":", "")
								.replaceAll("\\?", "")
								.replaceAll("/", "")
								.replaceAll("\\\\", "");

						// images
						links = (HtmlDivision) paginaLivro.getByXPath("//div[@class='images']").get(0);
						HtmlImage image = (HtmlImage) links.getElementsByTagName("img").get(0);
						RenderedImage buf = getImage(image);

						links = (HtmlDivision) paginaLivro.getByXPath("//div[@class='links-download']").get(0);
						linkEPUB = links.getElementsByTagName("a").get(0);
						linkMOBI = links.getElementsByTagName("a").get(1);
						linkPDF = links.getElementsByTagName("a").get(2);
						
						downloadImage(image, nomeLivro, buf);
						download(nomeLivro + ".epub", linkEPUB, "epub");
						download(nomeLivro + ".mobi", linkMOBI, "mobi");
						download(nomeLivro + ".pdf", linkPDF, "pdf");
					} catch (IndexOutOfBoundsException e) {
						Logger.getLogger(LeLivros.class.getName()).log(Level.SEVERE, "Erro na página do livro "
								+ itemLista.getElementsByTagName("a").get(0).getAttribute("href") + e.getMessage(), e);
					}
				}

			} catch (FailingHttpStatusCodeException e) {
				Logger.getLogger(LeLivros.class.getName()).log(Level.SEVERE, e.getMessage(), e);
			} catch (MalformedURLException e) {
				Logger.getLogger(LeLivros.class.getName()).log(Level.SEVERE, e.getMessage(), e);
			} catch (IOException e) {
				Logger.getLogger(LeLivros.class.getName()).log(Level.SEVERE, e.getMessage(), e);
			}
		}
	}
	
	private static void downloadImage(HtmlImage image, String nomeLivro, RenderedImage buf) throws IOException {
		String extensao = image.getSrcAttribute().substring(image.getSrcAttribute().length()-3);
		
		File arquivo = new File(DIRETORIO_SALVAR_LIVROS + "/images/" + nomeLivro + "." + extensao);
		
		if (!arquivo.exists()) {
			Logger.getLogger(LeLivros.class.getName()).log(Level.INFO, "Baixando Imagem " + nomeLivro);
			
			ImageIO.write(buf, extensao, arquivo);
			
			Logger.getLogger(LeLivros.class.getName()).log(Level.INFO, "Imagem Salva " + nomeLivro);
		}
	}

	private static void download(String nomeArquivo, HtmlElement link, String subdir) {
		File arquivo = new File(DIRETORIO_SALVAR_LIVROS + "/" + subdir + "/" + nomeArquivo);
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

	private static RenderedImage getImage(HtmlImage image) throws IOException {
		ImageReader reader = image.getImageReader();
		int minIndex = reader.getMinIndex();
		return reader.read(minIndex);
	}

	private static WebClient novoWebClient() {
		WebClient retorno = new WebClient(BrowserVersion.CHROME);

		//Usar proxy
//		WebClient retorno = new WebClient(BrowserVersion.CHROME, "url", porta);		
//		final DefaultCredentialsProvider credentialsProvider = (DefaultCredentialsProvider) retorno
//				.getCredentialsProvider();
//		credentialsProvider.addCredentials("", "");

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

