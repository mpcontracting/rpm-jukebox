package uk.co.mpcontracting.rpmjukebox.jetty;

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

    private HttpServletRequest mockServletRequest;
    private HttpServletResponse mockServletResponse;
    private CacheManager mockCacheManager;
    private InternetManager mockInternetManager;
    private ServletOutputStream mockServletOutputStream;

    private ApplicationContext originalContext;
    private CachingMediaProxyServlet spyServlet;

    @Before
    public void setup() throws Exception {
        mockServletRequest = mock(HttpServletRequest.class);
        mockServletResponse = mock(HttpServletResponse.class);
        mockCacheManager = mock(CacheManager.class);
        mockInternetManager = mock(InternetManager.class);
        mockServletOutputStream = mock(ServletOutputStream.class);

        when(mockServletResponse.getOutputStream()).thenReturn(mockServletOutputStream);

        originalContext = (ApplicationContext) getField(ContextHelper.class, "applicationContext");
        ApplicationContext mockContext = mock(ApplicationContext.class);
        when(mockContext.getBean(CacheManager.class)).thenReturn(mockCacheManager);
        when(mockContext.getBean(InternetManager.class)).thenReturn(mockInternetManager);

        setField(ContextHelper.class, "applicationContext", mockContext);

        spyServlet = spy(new CachingMediaProxyServlet());
    }

    @Test
    public void shouldGetCachedFile() throws Exception {
        when(mockServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(mockServletRequest.getParameter("id")).thenReturn("123");
        when(mockServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(mockServletRequest.getMethod()).thenReturn("GET");

        File mockCachedFile = mock(File.class);
        when(mockCachedFile.length()).thenReturn(1000L);

        when(mockCacheManager.readCache(any(), anyString())).thenReturn(of(mockCachedFile));
        doReturn(mock(FileInputStream.class)).when(spyServlet).getFileInputStream(any());

        spyServlet.doGet(mockServletRequest, mockServletResponse);

        verify(mockServletResponse, times(1)).setContentLengthLong(1000l);
        verify(mockServletOutputStream, times(1)).setWriteListener(any());
    }

    @Test
    public void shouldNotGetCachedFileWhenRequestMethodIsHead() throws Exception {
        when(mockServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(mockServletRequest.getParameter("id")).thenReturn("123");
        when(mockServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(mockServletRequest.getMethod()).thenReturn("HEAD");

        File mockCachedFile = mock(File.class);
        when(mockCachedFile.length()).thenReturn(1000L);

        when(mockCacheManager.readCache(any(), anyString())).thenReturn(of(mockCachedFile));

        spyServlet.doGet(mockServletRequest, mockServletResponse);

        verify(mockServletResponse, times(1)).setContentLengthLong(1000L);
        verify(mockServletOutputStream, never()).setWriteListener(any());
    }

    @Test
    public void shouldGetFileFromUrl() throws Exception {
        when(mockServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(mockServletRequest.getParameter("id")).thenReturn("123");
        when(mockServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(mockServletRequest.getMethod()).thenReturn("GET");
        when(mockCacheManager.readCache(any(), anyString())).thenReturn(empty());

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getContentLength()).thenReturn(1000);

        when(mockInternetManager.openConnection(any())).thenReturn(mockConnection);

        spyServlet.doGet(mockServletRequest, mockServletResponse);

        verify(mockServletResponse, times(1)).setContentLength(1000);
        verify(mockServletResponse, never()).setStatus(anyInt());
        verify(mockServletOutputStream, times(1)).setWriteListener(any());
    }

    @Test
    public void shouldNotGetFileFromUrlWhenRequestMethodIsHead() throws Exception {
        when(mockServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(mockServletRequest.getParameter("id")).thenReturn("123");
        when(mockServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(mockServletRequest.getMethod()).thenReturn("HEAD");
        when(mockCacheManager.readCache(any(), anyString())).thenReturn(empty());

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getContentLength()).thenReturn(1000);

        when(mockInternetManager.openConnection(any())).thenReturn(mockConnection);

        spyServlet.doGet(mockServletRequest, mockServletResponse);

        verify(mockServletResponse, times(1)).setContentLength(1000);
        verify(mockServletResponse, never()).setStatus(anyInt());
        verify(mockServletOutputStream, never()).setWriteListener(any());
    }

    @Test
    public void shouldNotGetFileFromUrlWhenHttpError() throws Exception {
        when(mockServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(mockServletRequest.getParameter("id")).thenReturn("123");
        when(mockServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(mockServletRequest.getMethod()).thenReturn("GET");
        when(mockCacheManager.readCache(any(), anyString())).thenReturn(empty());

        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(404);

        when(mockInternetManager.openConnection(any())).thenReturn(mockConnection);

        spyServlet.doGet(mockServletRequest, mockServletResponse);

        verify(mockServletResponse, never()).setContentLength(anyInt());
        verify(mockServletResponse, times(1)).setStatus(404);
        verify(mockServletOutputStream, never()).setWriteListener(any());
    }

    @After
    public void cleanup() {
        setField(ContextHelper.class, "applicationContext", originalContext);
    }
}
