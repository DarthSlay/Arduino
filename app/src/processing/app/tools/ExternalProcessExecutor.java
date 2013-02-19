package processing.app.tools;

import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handy process executor, collecting stdout into a given OutputStream
 */
public class ExternalProcessExecutor extends DefaultExecutor {

  public ExternalProcessExecutor() {
    this(false);
  }

  public ExternalProcessExecutor(boolean debug) {
    this(null, null, debug);
  }

  public ExternalProcessExecutor(OutputStream stdout) {
    this(stdout, null, false);
  }

  public ExternalProcessExecutor(final OutputStream stdout, final OutputStream stderr, final boolean debug) {
    this.setStreamHandler(new ExecuteStreamHandler() {
      @Override
      public void setProcessInputStream(OutputStream outputStream) throws IOException {
      }

      @Override
      public void setProcessErrorStream(InputStream inputStream) throws IOException {
        pipe(inputStream, stderr);
      }

      @Override
      public void setProcessOutputStream(InputStream inputStream) throws IOException {
        pipe(inputStream, stdout);
      }

      private void pipe(InputStream inputStream, OutputStream output) throws IOException {
        byte[] buf = new byte[4096];
        int bytes;
        while ((bytes = inputStream.read(buf)) != -1) {
          if (debug) {
            System.out.println(new String(buf, 0, bytes));
          }
          if (output != null) {
            output.write(buf, 0, bytes);
          }
        }
      }

      @Override
      public void start() throws IOException {
      }

      @Override
      public void stop() {
      }
    });

  }
}
