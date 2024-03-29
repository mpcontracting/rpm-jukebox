package uk.co.mpcontracting.rpmjukebox.jetty;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.io.EofException;
import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class CachingDataStream implements WriteListener {

    private final CacheType cacheType;
    private final String id;
    private final boolean isCached;
    private final InputStream inputStream;
    private final AsyncContext asyncContext;
    private final ServletOutputStream outputStream;
    private ByteArrayOutputStream byteStream;

    CachingDataStream(CacheType cacheType, String id, boolean isCached, InputStream inputStream,
                      AsyncContext asyncContext, ServletOutputStream outputStream) {
        this.cacheType = cacheType;
        this.id = id;
        this.isCached = isCached;
        this.inputStream = inputStream;
        this.asyncContext = asyncContext;
        this.outputStream = outputStream;

        byteStream = new ByteArrayOutputStream();
    }

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
                    ContextHelper.getBean(CacheManager.class).writeCache(cacheType, id, byteStream.toByteArray());
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
