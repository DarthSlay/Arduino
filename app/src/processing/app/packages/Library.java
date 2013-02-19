package processing.app.packages;

import processing.app.helpers.PreferencesMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static processing.app.helpers.StringMatchers.wildcardMatch;

public class Library {

  private String name;
  private String version;
  private File folder, srcFolder;
  private List<String> architectures;
  private boolean pre15Lib;
  private List<String> dependencies;

  /**
   * Scans inside a folder and create a Library object out of it. Automatically
   * detects pre-1.5 libraries. Automatically fills metadata from
   * library.properties file if found.
   *
   * @param libFolder
   * @return
   */
  static public Library create(File libFolder) throws IOException {
    // A library is considered "new" if it contains a file called
    // "library.properties"
    File check = new File(libFolder, "library.properties");
    if (!check.exists() || !check.isFile())
      return createPre15Library(libFolder);
    else
      return createLibrary(libFolder);
  }

  private static Library createLibrary(File libFolder) throws IOException {
    // Parse metadata
    File propertiesFile = new File(libFolder, "library.properties");
    PreferencesMap properties = new PreferencesMap();
    properties.load(propertiesFile);

    // Library sanity checks
    // ---------------------

    // 1. Check mandatory properties
    if (!properties.containsKey("name"))
      throw new IOException("Missing 'name' from library");
    if (!properties.containsKey("version"))
      throw new IOException("Missing 'version' from library");
    if (!properties.containsKey("architectures"))
      throw new IOException("Missing 'architectures' from library");

    // 2. Check mandatory folders
    File srcFolder = new File(libFolder, "src");
    if (!srcFolder.exists() || !srcFolder.isDirectory())
      throw new IOException("Missing 'src' folder");

    // TODO: 3. check if root folder contains prohibited stuff

    // Extract metadata info
    // TODO: do for all metadata
    List<String> archs = new ArrayList<String>();
    for (String arch : properties.get("architectures").split(","))
      archs.add(arch.trim());

    List<String> dependencies = new ArrayList<String>();
    for (String dependency : properties.get("dependencies").split(",")) {
      dependency = dependency.trim();
      if (!dependency.equals("")) {
        dependencies.add(dependency);
      }
    }

    Library res = new Library();
    res.folder = libFolder;
    res.srcFolder = srcFolder;
    res.name = properties.get("name").trim();
    res.architectures = archs;
    res.dependencies = dependencies;
    res.version = properties.get("version").trim();
    res.pre15Lib = false;
    return res;
  }

  private static Library createPre15Library(File libFolder) {
    // construct an old style library
    Library res = new Library();
    res.folder = libFolder;
    res.srcFolder = libFolder;
    res.name = libFolder.getName();
    res.architectures = Arrays.asList(new String[]{"*"});
    res.pre15Lib = true;
    return res;
  }

  public List<File> getSrcFolders(String reqArch) {
    if (!supportsArchitecture(reqArch))
      return null;
    List<File> res = new ArrayList<File>();
    res.add(srcFolder);
    File archSpecificFolder = new File(srcFolder, reqArch);
    if (archSpecificFolder.exists() && archSpecificFolder.isDirectory())
      res.add(archSpecificFolder);
    return res;
  }

  public boolean supportsArchitecture(String reqArch) {
    for (String arch : architectures)
      if (wildcardMatch(reqArch, arch))
        return true;
    return false;
  }

  public static final Comparator<Library> CASE_INSENSITIVE_ORDER = new Comparator<Library>() {
    @Override
    public int compare(Library o1, Library o2) {
      return o1.getName().compareToIgnoreCase(o2.getName());
    }
  };

  public File getSrcFolder() {
    return srcFolder;
  }

  public String getName() {
    return name;
  }

  public boolean isPre15Lib() {
    return pre15Lib;
  }

  public File getFolder() {
    return folder;
  }

  public List<String> getDependencies() {
    return dependencies;
  }
}
