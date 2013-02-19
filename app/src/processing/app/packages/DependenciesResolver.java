package processing.app.packages;

import java.io.File;
import java.io.IOException;

public interface DependenciesResolver {

  void resolveAndDownload(File libFolder, File targetFolder) throws IOException;

}
