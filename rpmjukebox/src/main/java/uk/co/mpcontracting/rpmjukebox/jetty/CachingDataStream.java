package uk.co.mpcontracting.rpmjukebox.jetty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import org.eclipse.jetty.io.EofException;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;

@Slf4j
public class CachingDataStream implements WriteListener {

	private String trackId;
	private InputStream inputStream;
	private AsyncContext asyncContext;
	private ServletOutputStream outputStream;
	private ByteArrayOutputStream byteStream;

	public CachingDataStream(String trackId, InputStream inputStream, AsyncContext asyncContext, ServletOutputStream outputStream) {
		this.trackId = trackId;
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

				ApplicationContext.getBean(CacheManager.class).writeCache(trackId, byteStream.toByteArray());
				
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

	    asyncContext.complete();
	}
}
