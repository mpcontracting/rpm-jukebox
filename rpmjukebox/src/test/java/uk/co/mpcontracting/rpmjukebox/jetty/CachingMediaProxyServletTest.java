package uk.co.mpcontracting.rpmjukebox.jetty;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class CachingMediaProxyServletTest extends AbstractTest {

    private HttpServletRequest mockServletRequest;
    private HttpServletResponse mockServletResponse;
    private CacheManager mockCacheManager;
    private AsyncContext mockAsyncContext;
    private ServletOutputStream mockServletOutputStream;
    
    private ApplicationContext originalContext;
    private CachingMediaProxyServlet spyServlet;
    
    @Before
    public void setup() throws Exception {
        mockServletRequest = mock(HttpServletRequest.class);
        mockServletResponse = mock(HttpServletResponse.class);
        mockCacheManager = mock(CacheManager.class);
        mockAsyncContext = mock(AsyncContext.class);
        mockServletOutputStream = mock(ServletOutputStream.class);
        
        when(mockServletRequest.getAsyncContext()).thenReturn(mockAsyncContext);
        when(mockServletResponse.getOutputStream()).thenReturn(mockServletOutputStream);
        
        originalContext = (ApplicationContext)ReflectionTestUtils.getField(FxmlContext.class, "applicationContext");
        ApplicationContext mockContext = mock(ApplicationContext.class);
        when(mockContext.getBean(CacheManager.class)).thenReturn(mockCacheManager);
        
        ReflectionTestUtils.setField(FxmlContext.class, "applicationContext", mockContext);
        
        spyServlet = spy(new CachingMediaProxyServlet());
    }
    
    @Test
    public void shouldGetCachedFile() throws Exception {
        when(mockServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(mockServletRequest.getParameter("id")).thenReturn("123");
        when(mockServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(mockServletRequest.getMethod()).thenReturn("GET");
        
        File mockCachedFile = mock(File.class);
        when(mockCachedFile.length()).thenReturn(1000l);
        
        when(mockCacheManager.readCache(any(), anyString())).thenReturn(mockCachedFile);
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
        when(mockCachedFile.length()).thenReturn(1000l);
        
        when(mockCacheManager.readCache(any(), anyString())).thenReturn(mockCachedFile);
        doReturn(mock(FileInputStream.class)).when(spyServlet).getFileInputStream(any());
        
        spyServlet.doGet(mockServletRequest, mockServletResponse);
        
        verify(mockServletResponse, times(1)).setContentLengthLong(1000l);
        verify(mockServletOutputStream, never()).setWriteListener(any());
    }
    
    @Test
    public void shouldGetFileFromUrl() throws Exception {
        when(mockServletRequest.getParameter("cacheType")).thenReturn("TRACK");
        when(mockServletRequest.getParameter("id")).thenReturn("123");
        when(mockServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
        when(mockServletRequest.getMethod()).thenReturn("GET");
        when(mockCacheManager.readCache(any(), anyString())).thenReturn(null);
        
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getContentLength()).thenReturn(1000);
        
        URL mockURL = mock(URL.class);
        doReturn(mockURL).when(spyServlet).getURL(anyString());
        when(mockURL.openConnection()).thenReturn(mockConnection);
        
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
        when(mockCacheManager.readCache(any(), anyString())).thenReturn(null);
        
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getContentLength()).thenReturn(1000);
        
        URL mockURL = mock(URL.class);
        doReturn(mockURL).when(spyServlet).getURL(anyString());
        when(mockURL.openConnection()).thenReturn(mockConnection);
        
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
        when(mockCacheManager.readCache(any(), anyString())).thenReturn(null);
        
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockConnection.getResponseCode()).thenReturn(404);
        when(mockConnection.getContentLength()).thenReturn(1000);
        
        URL mockURL = mock(URL.class);
        doReturn(mockURL).when(spyServlet).getURL(anyString());
        when(mockURL.openConnection()).thenReturn(mockConnection);
        
        spyServlet.doGet(mockServletRequest, mockServletResponse);
        
        verify(mockServletResponse, never()).setContentLength(anyInt());
        verify(mockServletResponse, times(1)).setStatus(404);
        verify(mockServletOutputStream, never()).setWriteListener(any());
    }
    
    @After
    public void cleanup() {
        ReflectionTestUtils.setField(FxmlContext.class, "applicationContext", originalContext);
    }
}
