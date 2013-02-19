package processing.app.packages.npm;

import org.apache.commons.exec.CommandLine;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import processing.app.helpers.FileUtils;
import processing.app.packages.DependenciesResolver;
import processing.app.packages.Library;
import processing.app.packages.LibraryConverter;
import processing.app.tools.ExternalProcessExecutor;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;

public class NPMDependenciesResolver implements DependenciesResolver {

  private final String pathToNpmExec;
  private final LibraryConverter libraryConverter;
  private final ObjectMapper objectMapper;

  public NPMDependenciesResolver(String pathToNpmExec, LibraryConverter libraryConverter) {
    this.pathToNpmExec = pathToNpmExec;
    this.libraryConverter = libraryConverter;
    this.objectMapper = new ObjectMapper();
  }

  @Override
  public void resolveAndDownload(File libFolder, File targetFolder) throws IOException {
    Library library = Library.create(libFolder);
    File packageJson = new File(targetFolder, "package.json");

    FileWriter fw = null;
    try {
      fw = new FileWriter(packageJson);
      libraryConverter.convert(library, fw);
    } finally {
      if (fw != null) {
        fw.close();
      }
    }

    ExternalProcessExecutor executor = new ExternalProcessExecutor();
    executor.setWorkingDirectory(targetFolder);
    CommandLine parse = CommandLine.parse(pathToNpmExec + " install -d");
    executor.execute(parse);

    recursivelyMoveNodeModulesToUpperFolder(targetFolder, targetFolder);
  }

  private void recursivelyMoveNodeModulesToUpperFolder(File currentFolder, File targetFolder) throws IOException {
    File subNodeModules = new File(currentFolder, "node_modules");
    if (subNodeModules.exists() && subNodeModules.isDirectory()) {
      recursivelyMoveNodeModulesToUpperFolder(subNodeModules, targetFolder);
      FileUtils.recursiveDelete(subNodeModules);
      return;
    }

    File[] nodeModules = currentFolder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.isDirectory() && !file.isHidden();
      }
    });

    for (File nodeModule : nodeModules) {
      subNodeModules = new File(nodeModule, "node_modules");
      if (subNodeModules.exists() && subNodeModules.isDirectory()) {
        recursivelyMoveNodeModulesToUpperFolder(subNodeModules, targetFolder);
        FileUtils.recursiveDelete(subNodeModules);
      }
      JsonNode packageJson = objectMapper.readTree(new File(nodeModule, "package.json"));
      String version = packageJson.get("version").getTextValue();
      File dest = new File(targetFolder, nodeModule.getName() + "-" + version);
      if (!dest.exists()) {
        if (!nodeModule.renameTo(dest)) {
          throw new IOException("Unable to move " + nodeModule + " to " + dest);
        }
      } else {
        FileUtils.recursiveDelete(nodeModule);
      }
    }
  }
}
