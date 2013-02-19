package processing.app.packages.npm;

import org.codehaus.jackson.map.ObjectMapper;
import processing.app.packages.Library;
import processing.app.packages.LibraryConverter;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NPMLibraryConverter implements LibraryConverter {

  @Override
  public void convert(Library library, Writer writer) throws IOException {
    Map<String, Object> packageJson = new HashMap<String, Object>();
    packageJson.put("name", library.getName());
    packageJson.put("version", "1.0.0");

    Map<String, Object> dependencies = new HashMap<String, Object>();
    for (String dependency : library.getDependencies()) {
      String name;
      String version = "*";
      if (dependency.contains("(")) {
        name = dependency.substring(0, dependency.indexOf("(")).trim().toLowerCase(Locale.US);
        version = dependency.substring(dependency.indexOf("(") + 1, dependency.indexOf(")"));
      } else {
        name = dependency.trim().toLowerCase(Locale.US);
      }
      dependencies.put(name, version);
    }

    packageJson.put("dependencies", dependencies);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.writeValue(writer, packageJson);
  }
}
