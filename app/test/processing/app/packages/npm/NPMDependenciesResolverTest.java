package processing.app.packages.npm;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import processing.app.helpers.FileUtils;
import processing.app.packages.DependenciesResolver;

import java.io.File;
import java.io.FileFilter;

import static org.junit.Assert.assertTrue;

public class NPMDependenciesResolverTest {

  private DependenciesResolver resolver;
  private File targetFolder;

  @Before
  public void setUp() throws Exception {
    File parent = new File(NPMDependenciesResolverTest.class.getResource("/").getFile());
    String pathToNpmExec = new File(parent, "../../build/linux/work/nodejs/bin/npm").getAbsolutePath();

    resolver = new NPMDependenciesResolver(pathToNpmExec, new NPMLibraryConverter());

    targetFolder = new File(System.getProperty("java.io.tmpdir"), "arduino_" + Math.round(Math.random() * 100000));
    targetFolder.mkdirs();
  }

  @After
  public void tearDown() throws Exception {
    FileUtils.recursiveDelete(targetFolder);
  }

  @Test
  public void shouldDownloadAndOrganizeDependencies() throws Exception {
    File libFolder = new File(NPMLibraryConverterTest.class.getResource("/processing/app/packages/test_lib_node_modules/library.properties").getFile()).getParentFile();

    resolver.resolveAndDownload(libFolder, targetFolder);

    assertTrue(listFoldersStartingWith("async").length >= 1);
    assertTrue(listFoldersStartingWith("orientdb").length >= 1);
    assertTrue(listFoldersStartingWith("by-mocha").length >= 1);
    assertTrue(listFoldersStartingWith("grunt-mocha-cli").length >= 1);

    File[] folders = targetFolder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.isDirectory();
      }
    });

    // modules we depend to have sub dependencies
    assertTrue(folders.length > 4);
  }

  private File[] listFoldersStartingWith(final String prefix) {
    return targetFolder.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.isDirectory() && file.getName().startsWith(prefix);
      }
    });
  }
}
