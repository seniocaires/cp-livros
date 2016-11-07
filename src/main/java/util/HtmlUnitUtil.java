package util;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;

public class HtmlUnitUtil {

  public static WebClient novoWebClient() {

    WebClient webClient;
    DefaultCredentialsProvider credentialsProvider;

    if (ConfiguracaoUtil.getProxy().getAtivo()) {
      webClient = new WebClient(BrowserVersion.CHROME, ConfiguracaoUtil.getProxy().getHost(), Integer.valueOf(ConfiguracaoUtil.getProxy().getPorta()));
      credentialsProvider = (DefaultCredentialsProvider) webClient.getCredentialsProvider();
      credentialsProvider.addCredentials(ConfiguracaoUtil.getProxy().getLogin(), ConfiguracaoUtil.getProxy().getSenha());
    } else {
      webClient = new WebClient(BrowserVersion.CHROME);
    }

    webClient.getOptions().setThrowExceptionOnScriptError(false);
    webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    webClient.getOptions().setCssEnabled(false);
    webClient.getOptions().setDoNotTrackEnabled(true);
    webClient.getOptions().setGeolocationEnabled(false);
    webClient.getOptions().setJavaScriptEnabled(false);
    webClient.getOptions().setPopupBlockerEnabled(true);
    webClient.getOptions().setPrintContentOnFailingStatusCode(false);

    return webClient;
  }
}
