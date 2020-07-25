package uk.co.mpcontracting.rpmjukebox.jetty;

import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;
import uk.co.mpcontracting.rpmjukebox.manager.InternetManager;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class CachingMediaProxyServletTest {

    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private CacheManager cacheManager;
    private InternetManager internetManager;
    private ServletOutputStream servletOutputStream;

    private ApplicationContext originalContext;
    private CachingMediaProxyServlet underTest;

    @Before
    @SneakyThrows
    public void setup() {
        httpServletRequest = mock(HttpServletRequest.class);
        httpServletResponse = mock(HttpServletResponse.class);
        cacheManager = mock(CacheManager.class);
        internetManager = mock(InternetManager.class);
        servletOutputStream = mock(ServletOutputStream.class);

        when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);

        originalContext = (ApplicationContext) getField(ContextHelper.class, "applicationContext");
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(CacheManager.class)).thenReturn(cacheManager);
        when(applicationContext.getBean(InternetManager.class)).thenReturn(internetManager);

        setField(ContextHelper.class, "applicationContext", applicationContext);

        underTest = spy(new CachingMediaProxyServlet());
    }

    @Test
    @SneakyThrows
    public void shouldGetCachedFile() {
        when(httpServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(httpServletRequest.getParameter("id")).thenReturn("123");
        when(httpServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(httpServletRequest.getMethod()).thenReturn("GET");

        File file = mock(File.class);
        when(file.length()).thenReturn(1000L);

        when(cacheManager.readCache(any(), anyString())).thenReturn(of(file));
        doReturn(mock(FileInputStream.class)).when(underTest).getFileInputStream(any());

        underTest.doGet(httpServletRequest, httpServletResponse);

        verify(httpServletResponse, times(1)).setContentLengthLong(1000L);
        verify(servletOutputStream, times(1)).setWriteListener(any());
    }

    @Test
    public void shouldNotGetCachedFileWhenRequestMethodIsHead() {
        when(httpServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(httpServletRequest.getParameter("id")).thenReturn("123");
        when(httpServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(httpServletRequest.getMethod()).thenReturn("HEAD");

        File file = mock(File.class);
        when(file.length()).thenReturn(1000L);

        when(cacheManager.readCache(any(), anyString())).thenReturn(of(file));

        underTest.doGet(httpServletRequest, httpServletResponse);

        verify(httpServletResponse, times(1)).setContentLengthLong(1000L);
        verify(servletOutputStream, never()).setWriteListener(any());
    }

    @Test
    @SneakyThrows
    public void shouldGetFileFromUrl() {
        when(httpServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(httpServletRequest.getParameter("id")).thenReturn("123");
        when(httpServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(cacheManager.readCache(any(), anyString())).thenReturn(empty());

        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(httpURLConnection.getResponseCode()).thenReturn(200);
        when(httpURLConnection.getContentLength()).thenReturn(1000);

        when(internetManager.openConnection(any())).thenReturn(httpURLConnection);

        underTest.doGet(httpServletRequest, httpServletResponse);

        verify(httpServletResponse, times(1)).setContentLength(1000);
        verify(httpServletResponse, never()).setStatus(anyInt());
        verify(servletOutputStream, times(1)).setWriteListener(any());
    }

    @Test
    @SneakyThrows
    public void shouldNotGetFileFromUrlWhenRequestMethodIsHead() {
        when(httpServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(httpServletRequest.getParameter("id")).thenReturn("123");
        when(httpServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(httpServletRequest.getMethod()).thenReturn("HEAD");
        when(cacheManager.readCache(any(), anyString())).thenReturn(empty());

        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(httpURLConnection.getResponseCode()).thenReturn(200);
        when(httpURLConnection.getContentLength()).thenReturn(1000);

        when(internetManager.openConnection(any())).thenReturn(httpURLConnection);

        underTest.doGet(httpServletRequest, httpServletResponse);

        verify(httpServletResponse, times(1)).setContentLength(1000);
        verify(httpServletResponse, never()).setStatus(anyInt());
        verify(servletOutputStream, never()).setWriteListener(any());
    }

    @Test
    @SneakyThrows
    public void shouldNotGetFileFromUrlWhenHttpError() {
        when(httpServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(httpServletRequest.getParameter("id")).thenReturn("123");
        when(httpServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(cacheManager.readCache(any(), anyString())).thenReturn(empty());

        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(httpURLConnection.getResponseCode()).thenReturn(404);

        when(internetManager.openConnection(any())).thenReturn(httpURLConnection);

        underTest.doGet(httpServletRequest, httpServletResponse);

        verify(httpServletResponse, never()).setContentLength(anyInt());
        verify(httpServletResponse, times(1)).setStatus(404);
        verify(servletOutputStream, never()).setWriteListener(any());
    }

    @After
    public void cleanup() {
        setField(ContextHelper.class, "applicationContext", originalContext);
    }
}
