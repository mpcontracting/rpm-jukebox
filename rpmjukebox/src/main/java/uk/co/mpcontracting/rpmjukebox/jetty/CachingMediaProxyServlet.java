package uk.co.mpcontracting.rpmjukebox.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;

@Slf4j
public class CachingMediaProxyServlet extends HttpServlet {
	private static final long serialVersionUID = 5460471107965724660L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		CacheType cacheType = CacheType.valueOf(request.getParameter("cacheType"));
		String trackId = request.getParameter("trackId");
		String url = request.getParameter("url");

		try {
			log.info("Getting file : Cache type - " + cacheType + ", Track ID - " + trackId + ", URL " + url);
			
			File cachedFile = ApplicationContext.getBean(CacheManager.class).readCache(cacheType, trackId);
			
			if (cachedFile != null) {
				response.setContentLengthLong(cachedFile.length());
				
				if (!request.getMethod().equals("HEAD")) {
					openDataStream(request, response, cacheType, trackId, true, new FileInputStream(cachedFile));
				}
			} else {
				URL location = new URL(url);
				HttpURLConnection connection = null;
	
				connection = (HttpURLConnection)location.openConnection();
				
				if (connection.getResponseCode() == 200) {
					response.setContentLength(connection.getContentLength());
	
					if (!request.getMethod().equals("HEAD")) {
						openDataStream(request, response, cacheType, trackId, false, connection.getInputStream());
					}
				} else {
					response.setStatus(connection.getResponseCode());
				}
			}
		} catch (Exception e) {
			log.error("Error getting file : ID - " + trackId + ", URL " + url);
		}
	}
	
	private void openDataStream(HttpServletRequest request, HttpServletResponse response, CacheType cacheType, String trackId, 
		boolean isCached, InputStream inputStream) throws IOException {
		AsyncContext asyncContext = request.startAsync();
		ServletOutputStream outputStream = response.getOutputStream();
		outputStream.setWriteListener(new CachingDataStream(cacheType, trackId, isCached, inputStream, asyncContext, outputStream));
	}
}
