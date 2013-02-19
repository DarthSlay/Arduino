package processing.app.packages;

import java.io.IOException;
import java.io.Writer;

public interface LibraryConverter {

  void convert(Library library, Writer writer) throws IOException;

}
