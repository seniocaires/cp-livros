package lelivros;

public class Configuracao {

  private Boolean downloadCapas;

  private Boolean downloadPDF;

  private Boolean downloadMOBI;

  private Boolean downloadEPUB;

  private String diretorioDownload;

  private String subDiretorioCapas;

  private String subDiretorioPDF;

  private String subDiretorioMOBI;

  private String subDiretorioEPUB;

  private String linkSite;

  public Boolean getDownloadCapas() {
    return downloadCapas;
  }

  public Boolean getDownloadPDF() {
    return downloadPDF;
  }

  public Boolean getDownloadMOBI() {
    return downloadMOBI;
  }

  public Boolean getDownloadEPUB() {
    return downloadEPUB;
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

  public String getSubDiretorioMOBI() {
    return subDiretorioMOBI;
  }

  public String getSubDiretorioEPUB() {
    return subDiretorioEPUB;
  }

  public String getLinkSite() {
    return linkSite;
  }
}
