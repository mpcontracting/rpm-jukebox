package uk.co.mpcontracting.rpmjukebox.jetty;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.service.CacheService;
import uk.co.mpcontracting.rpmjukebox.service.InternetService;
import uk.co.mpcontracting.rpmjukebox.util.ContextHelper;

@ExtendWith(MockitoExtension.class)
class CachingMediaProxyServletTest {

  @Mock
  private HttpServletRequest httpServletRequest;

  @Mock
  private HttpServletResponse httpServletResponse;

  @Mock
  private ServletOutputStream servletOutputStream;

  @Mock
  private CacheService cacheService;

  @Mock
  private InternetService internetService;

  private ApplicationContext originalContext;
  private ApplicationContext mockContext;
  private CachingMediaProxyServlet underTest;

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    originalContext = (ApplicationContext) getField(ContextHelper.class, "applicationContext");
    mockContext = mock(ApplicationContext.class);
    when(mockContext.getBean(CacheService.class)).thenReturn(cacheService);

    setField(ContextHelper.class, "applicationContext", mockContext);

    underTest = spy(new CachingMediaProxyServlet());
  }

  @AfterEach
  void cleanup() {
    setField(ContextHelper.class, "applicationContext", originalContext);
  }

  @Test
  @SneakyThrows
  void shouldGetCachedFile() {
    when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);
    when(httpServletRequest.getParameter("cacheType")).thenReturn("TRACK");
    when(httpServletRequest.getParameter("id")).thenReturn("123");
    when(httpServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
    when(httpServletRequest.getMethod()).thenReturn("GET");

    File file = mock(File.class);
    when(file.length()).thenReturn(1000L);

    when(cacheService.readCache(any(), anyString())).thenReturn(of(file));
    doReturn(mock(FileInputStream.class)).when(underTest).getFileInputStream(any());

    underTest.doGet(httpServletRequest, httpServletResponse);

    verify(httpServletResponse).setContentLengthLong(1000L);
    verify(servletOutputStream).setWriteListener(any());
  }

  @Test
  void shouldNotGetCachedFileWhenRequestMethodIsHead() {
    when(httpServletRequest.getParameter("cacheType")).thenReturn("TRACK");
    when(httpServletRequest.getParameter("id")).thenReturn("123");
    when(httpServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
    when(httpServletRequest.getMethod()).thenReturn("HEAD");

    File file = mock(File.class);
    when(file.length()).thenReturn(1000L);

    when(cacheService.readCache(any(), anyString())).thenReturn(of(file));

    underTest.doGet(httpServletRequest, httpServletResponse);

    verify(httpServletResponse).setContentLengthLong(1000L);
    verify(servletOutputStream, never()).setWriteListener(any());
  }

  @Test
  @SneakyThrows
  void shouldGetFileFromUrl() {
    when(httpServletResponse.getOutputStream()).thenReturn(servletOutputStream);
    when(mockContext.getBean(InternetService.class)).thenReturn(internetService);
    when(httpServletRequest.getParameter("cacheType")).thenReturn("TRACK");
    when(httpServletRequest.getParameter("id")).thenReturn("123");
    when(httpServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
    when(httpServletRequest.getMethod()).thenReturn("GET");
    when(cacheService.readCache(any(), anyString())).thenReturn(empty());

    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(httpURLConnection.getResponseCode()).thenReturn(200);
    when(httpURLConnection.getContentLength()).thenReturn(1000);

    when(internetService.openConnection(any())).thenReturn(httpURLConnection);

    underTest.doGet(httpServletRequest, httpServletResponse);

    verify(httpServletResponse).setContentLength(1000);
    verify(httpServletResponse, never()).setStatus(anyInt());
    verify(servletOutputStream).setWriteListener(any());
  }

  @Test
  @SneakyThrows
  public void shouldNotGetFileFromUrlWhenRequestMethodIsHead() {
    when(mockContext.getBean(InternetService.class)).thenReturn(internetService);
    when(httpServletRequest.getParameter("cacheType")).thenReturn("TRACK");
    when(httpServletRequest.getParameter("id")).thenReturn("123");
    when(httpServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
    when(httpServletRequest.getMethod()).thenReturn("HEAD");
    when(cacheService.readCache(any(), anyString())).thenReturn(empty());

    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(httpURLConnection.getResponseCode()).thenReturn(200);
    when(httpURLConnection.getContentLength()).thenReturn(1000);

    when(internetService.openConnection(any())).thenReturn(httpURLConnection);

    underTest.doGet(httpServletRequest, httpServletResponse);

    verify(httpServletResponse).setContentLength(1000);
    verify(httpServletResponse, never()).setStatus(anyInt());
    verify(servletOutputStream, never()).setWriteListener(any());
  }

  @Test
  @SneakyThrows
  public void shouldNotGetFileFromUrlWhenHttpError() {
    when(mockContext.getBean(InternetService.class)).thenReturn(internetService);
    when(httpServletRequest.getParameter("cacheType")).thenReturn("TRACK");
    when(httpServletRequest.getParameter("id")).thenReturn("123");
    when(httpServletRequest.getParameter("url")).thenReturn("http://www.example.com/example.mp3");
    when(httpServletRequest.getMethod()).thenReturn("GET");
    when(cacheService.readCache(any(), anyString())).thenReturn(empty());

    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(httpURLConnection.getResponseCode()).thenReturn(404);

    when(internetService.openConnection(any())).thenReturn(httpURLConnection);

    underTest.doGet(httpServletRequest, httpServletResponse);

    verify(httpServletResponse, never()).setContentLength(anyInt());
    verify(httpServletResponse).setStatus(404);
    verify(servletOutputStream, never()).setWriteListener(any());
  }
}