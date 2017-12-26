package uk.co.mpcontracting.rpmjukebox.jetty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;
import uk.co.mpcontracting.rpmjukebox.manager.InternetManager;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;

@Slf4j
public class CachingMediaProxyServlet extends HttpServlet {
    private static final long serialVersionUID = 5460471107965724660L;

    @Override
    public void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        CacheType cacheType = CacheType.valueOf(httpServletRequest.getParameter("cacheType"));
        String id = httpServletRequest.getParameter("id");
        String url = httpServletRequest.getParameter("url");

        try {
            log.debug("Getting file : Cache type - " + cacheType + ", ID - " + id + ", URL " + url);

            File cachedFile = ContextHelper.getBean(CacheManager.class).readCache(cacheType, id);

            if (cachedFile != null) {
                httpServletResponse.setContentLengthLong(cachedFile.length());

                if (!httpServletRequest.getMethod().equals("HEAD")) {
                    openDataStream(httpServletRequest, httpServletResponse, cacheType, id, true,
                        getFileInputStream(cachedFile));
                }
            } else {
                URL location = new URL(url);
                Response response = null;

                try {
                    response = ContextHelper.getBean(InternetManager.class).openConnection(location);

                    if (response.isSuccessful()) {
                        httpServletResponse.setContentLengthLong(response.body().contentLength());

                        if (!httpServletRequest.getMethod().equals("HEAD")) {
                            openDataStream(httpServletRequest, httpServletResponse, cacheType, id, false,
                                response.body().byteStream());
                        }
                    } else {
                        httpServletResponse.setStatus(response.code());
                    }
                } finally {
                    if (response != null && response.body() != null) {
                        response.body().close();
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error getting file : ID - " + id + ", URL " + url);
        }
    }

    private void openDataStream(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
        CacheType cacheType, String trackId, boolean isCached, InputStream inputStream) throws IOException {
        AsyncContext asyncContext = httpServletRequest.startAsync();
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        outputStream.setWriteListener(
            new CachingDataStream(cacheType, trackId, isCached, inputStream, asyncContext, outputStream));
    }

    // Package level for testing purposes
    FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }
}
