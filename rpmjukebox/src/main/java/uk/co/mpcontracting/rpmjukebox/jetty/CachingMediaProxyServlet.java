package uk.co.mpcontracting.rpmjukebox.jetty;

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

@Slf4j
public class CachingMediaProxyServlet extends HttpServlet {
	private static final long serialVersionUID = 5460471107965724660L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		String trackId = request.getParameter("trackId");
		String url = request.getParameter("url");

		try {
			log.info("Getting file : ID - " + trackId + ", URL " + url);
			
			URL location = new URL(url);
			HttpURLConnection connection = null;

			connection = (HttpURLConnection)location.openConnection();
			
			if (connection.getResponseCode() == 200) {
				response.setContentLength(connection.getContentLength());

				if (!request.getMethod().equals("HEAD")) {
					openDataStream(request, response, trackId, connection.getInputStream());
				}
			} else {
				response.setStatus(connection.getResponseCode());
			}
		} catch (Exception e) {
			log.error("Error getting file : ID - " + trackId + ", URL " + url);
		}
	}
	
	private void openDataStream(HttpServletRequest request, HttpServletResponse response, String trackId, InputStream inputStream) throws IOException {
		AsyncContext asyncContext = request.startAsync();
		ServletOutputStream outputStream = response.getOutputStream();
		outputStream.setWriteListener(new CachingDataStream(trackId, inputStream, asyncContext, outputStream));
	}
}
