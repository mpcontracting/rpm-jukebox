package uk.co.mpcontracting.rpmjukebox.jetty;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.io.EofException;
import uk.co.mpcontracting.rpmjukebox.service.CacheService;
import uk.co.mpcontracting.rpmjukebox.util.CacheType;
import uk.co.mpcontracting.rpmjukebox.util.ContextHelper;

@Slf4j
@RequiredArgsConstructor
public class CachingDataStream implements WriteListener {

  private final CacheType cacheType;
  private final String id;
  private final boolean isCached;
  private final InputStream inputStream;
  private final AsyncContext asyncContext;
  private final ServletOutputStream outputStream;
  private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

  @Override
  public void onWritePossible() throws IOException {
    byte[] buffer = new byte[4096];

    // While we are able to write without blocking
    while (outputStream.isReady()) {
      int length = inputStream.read(buffer);

      // If we have reached EOF, then complete and write to the cache
      if (length < 0) {
        asyncContext.complete();

        if (!isCached) {
          ContextHelper.getBean(CacheService.class).writeCache(cacheType, id, byteStream.toByteArray());
        }

        cleanUpResources();

        return;
      }

      outputStream.write(buffer, 0, length);
      byteStream.write(buffer, 0, length);
    }
  }

  @Override
  public void onError(Throwable t) {
    if (!(t instanceof EofException)) {
      log.error("Error streaming data", t);
    }

    try {
      asyncContext.complete();
    } catch (Exception e) {
      // Ignore any errors here
    }

    cleanUpResources();
  }

  private void cleanUpResources() {
    log.debug("Cleaning up resources : Type - {}, ID - {}", cacheType, id);

    // Make sure the byte array is disposed of
    if (byteStream != null) {
      byteStream.reset();
      byteStream = null;
    }

    // Clean up the input stream
    try {
      if (inputStream != null) {
        inputStream.close();
      }
    } catch (Exception e) {
      // Ignore any errors here
    }
  }
}
