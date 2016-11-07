package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import lelivros.Main;

public class ConfiguracaoUtil {

  private static final Type PROXY_TYPE = new TypeToken<Proxy>() {
  }.getType();

  private static Proxy proxy;

  public static final Proxy getProxy() {

    if (proxy == null) {

      Gson gson = new Gson();
      JsonReader jsonReader;
      try {
        jsonReader = new JsonReader(new FileReader("configuracao" + File.separator + "proxy.json"));
        proxy = gson.fromJson(jsonReader, PROXY_TYPE);
      } catch (FileNotFoundException e) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, e.getMessage(), e);
      }
    }

    return proxy;
  }
}
