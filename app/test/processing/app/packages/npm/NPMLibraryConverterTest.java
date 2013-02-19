package processing.app.packages.npm;

import org.junit.Test;
import processing.app.packages.Library;

import java.io.File;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class NPMLibraryConverterTest {

  @Test
  public void shouldConvertToPackageJSON() throws Exception {
    File libFolder = new File(NPMLibraryConverterTest.class.getResource("/processing/app/packages/test_lib/library.properties").getFile()).getParentFile();
    Library library = Library.create(libFolder);

    NPMLibraryConverter converter = new NPMLibraryConverter();
    StringWriter writer = new StringWriter();
    converter.convert(library, writer);

    assertEquals("{\"dependencies\":{\"ethernet\":\">=1.0\",\"by-mocha\":\"*\",\"spi\":\"*\",\"servo\":\"=3.0\",\"grunt-mocha-cli\":\"*\"},\"name\":\"WebServer\",\"version\":\"1.0.0\"}", writer.toString());
  }
}
