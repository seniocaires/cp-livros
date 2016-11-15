package allitebooks;

public class Configuracao {

  private Boolean downloadCapas;

  private Boolean downloadPDF;

  private String diretorioDownload;

  private String subDiretorioCapas;

  private String subDiretorioPDF;

  private String linkSite;

  public Boolean getDownloadCapas() {
    return downloadCapas;
  }

  public Boolean getDownloadPDF() {
    return downloadPDF;
  }

  public String getDiretorioDownload() {
    return diretorioDownload;
  }

  public String getSubDiretorioCapas() {
    return subDiretorioCapas;
  }

  public String getSubDiretorioPDF() {
    return subDiretorioPDF;
  }

  public String getLinkSite() {
    return linkSite;
  }
}
