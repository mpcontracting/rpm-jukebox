package uk.co.mpcontracting.rpmjukebox.jetty;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.service.CacheService;
import uk.co.mpcontracting.rpmjukebox.service.InternetService;
import uk.co.mpcontracting.rpmjukebox.util.CacheType;
import uk.co.mpcontracting.rpmjukebox.util.ContextHelper;

@Slf4j
public class CachingMediaProxyServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    CacheType cacheType = CacheType.valueOf(request.getParameter("cacheType"));
    String id = request.getParameter("id");
    String url = request.getParameter("url");

    try {
      log.debug("Getting file : Cache type - {}, ID - {}, HTTP - {}, URL - {}", cacheType, id,
          request.getMethod(), url);

      Optional<File> cachedFile = ContextHelper.getBean(CacheService.class).readCache(cacheType, id);

      if (cachedFile.isPresent()) {
        response.setContentLengthLong(cachedFile.get().length());

        if (!request.getMethod().equals("HEAD")) {
          log.debug("Opening data stream to cached file - {}", id);
          openDataStream(request, response, cacheType, id, true, getFileInputStream(cachedFile.get()));
        }
      } else {
        URL location = new URI(url).toURL();
        HttpURLConnection connection = (HttpURLConnection) ContextHelper.getBean(InternetService.class)
            .openConnection(location);

        if (connection.getResponseCode() == 200) {
          response.setContentLength(connection.getContentLength());

          if (!request.getMethod().equals("HEAD")) {
            log.debug("Opening data stream to URL - {}", id);
            openDataStream(request, response, cacheType, id, false, connection.getInputStream());
          }
        } else {
          response.setStatus(connection.getResponseCode());
        }
      }
    } catch (Exception e) {
      log.error("Error getting file : Cache type - {}, ID - {}, HTTP - {}, URL - {}", cacheType, id,
          request.getMethod(), url);
    }
  }

  private void openDataStream(HttpServletRequest request, HttpServletResponse response, CacheType cacheType,
      String trackId, boolean isCached, InputStream inputStream) throws IOException {
    AsyncContext asyncContext = request.startAsync();
    ServletOutputStream outputStream = response.getOutputStream();
    outputStream.setWriteListener(new CachingDataStream(cacheType, trackId, isCached, inputStream,
        asyncContext, outputStream));
  }

  protected FileInputStream getFileInputStream(File file) throws FileNotFoundException {
    return new FileInputStream(file);
  }
}