package util;

public class Proxy {

  private Boolean ativo;

  private String host;

  private String porta;

  private String login;

  private String senha;

  public Boolean getAtivo() {
    return ativo;
  }

  public String getHost() {
    return host;
  }

  public String getPorta() {
    return porta;
  }

  public String getLogin() {
    return login;
  }

  public final String getSenha() {
    return senha;
  }
}
