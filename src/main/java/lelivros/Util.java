package lelivros;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import util.ConfiguracaoUtil;

public class Util extends ConfiguracaoUtil {

  private static final Type CONFIGURACAO_TYPE = new TypeToken<Configuracao>() {
  }.getType();

  private static Configuracao configuracao;

  public static final Configuracao getConfiguracao() {

    if (configuracao == null) {

      Gson gson = new Gson();
      JsonReader jsonReader;
      try {
        jsonReader = new JsonReader(new FileReader("configuracao" + File.separator + "lelivros.json"));
        configuracao = gson.fromJson(jsonReader, CONFIGURACAO_TYPE);
      } catch (FileNotFoundException e) {
        Logger.getLogger(Util.class.getName()).log(Level.SEVERE, e.getMessage(), e);
      }
    }

    return configuracao;
  }
}
