package uk.co.mpcontracting.rpmjukebox.jetty;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;
import uk.co.mpcontracting.rpmjukebox.manager.InternetManager;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

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

            Optional<File> cachedFile = ContextHelper.getBean(CacheManager.class).readCache(cacheType, id);

            if (cachedFile.isPresent()) {
                response.setContentLengthLong(cachedFile.get().length());

                if (!request.getMethod().equals("HEAD")) {
                    openDataStream(request, response, cacheType, id, true, getFileInputStream(cachedFile.get()));
                }
            } else {
                URL location = new URL(url);
                HttpURLConnection connection = (HttpURLConnection)ContextHelper.getBean(InternetManager.class)
                        .openConnection(location);

                if (connection.getResponseCode() == 200) {
                    response.setContentLength(connection.getContentLength());

                    if (!request.getMethod().equals("HEAD")) {
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

    // Package level for testing purposes
    FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }
}
