package uk.co.mpcontracting.rpmjukebox.jetty;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import lombok.SneakyThrows;
import org.eclipse.jetty.io.EofException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.service.CacheService;
import uk.co.mpcontracting.rpmjukebox.util.CacheType;
import uk.co.mpcontracting.rpmjukebox.util.ContextHelper;

@ExtendWith(MockitoExtension.class)
class CachingDataStreamTest {

  @Mock
  private AsyncContext asyncContext;

  @Mock
  private ServletOutputStream servletOutputStream;

  @Test
  @SneakyThrows
  void shouldWriteToOutputStreamAlreadyCached() {
    byte[] array = new byte[20000];
    Arrays.fill(array, (byte) 255);

    ByteArrayInputStream byteArrayInputStream = spy(new ByteArrayInputStream(array));
    CachingDataStream underTest = new CachingDataStream(CacheType.TRACK, "123", true, byteArrayInputStream,
        asyncContext, servletOutputStream);
    when(servletOutputStream.isReady()).thenReturn(true);

    underTest.onWritePossible();

    verify(asyncContext).complete();
    verify(servletOutputStream, times(5)).write(any(), anyInt(), anyInt());
    verify(byteArrayInputStream).close();
  }

  @Test
  @SneakyThrows
  void shouldWriteToOutputStreamNotCached() {
    byte[] array = new byte[20000];
    Arrays.fill(array, (byte) 255);

    ApplicationContext originalContext = (ApplicationContext) getField(ContextHelper.class, "applicationContext");
    ApplicationContext applicationContext = mock(ApplicationContext.class);
    setField(ContextHelper.class, "applicationContext", applicationContext);

    CacheService cacheService = mock(CacheService.class);
    when(applicationContext.getBean(CacheService.class)).thenReturn(cacheService);

    try {
      ByteArrayInputStream byteArrayInputStream = spy(new ByteArrayInputStream(array));

      CachingDataStream underTest = new CachingDataStream(CacheType.TRACK, "123", false, byteArrayInputStream,
          asyncContext, servletOutputStream);
      when(servletOutputStream.isReady()).thenReturn(true);

      underTest.onWritePossible();

      verify(asyncContext).complete();
      verify(servletOutputStream, times(5)).write(any(), anyInt(), anyInt());
      verify(cacheService).writeCache(CacheType.TRACK, "123", array);
      verify(byteArrayInputStream).close();
    } finally {
      setField(ContextHelper.class, "applicationContext", originalContext);
    }
  }

  @Test
  @SneakyThrows
  void shouldNotWriteToOutputStreamWhenNotReady() {
    byte[] array = new byte[20000];
    Arrays.fill(array, (byte) 255);

    ByteArrayInputStream byteArrayInputStream = spy(new ByteArrayInputStream(array));
    CachingDataStream underTest = new CachingDataStream(CacheType.TRACK, "123", true, byteArrayInputStream,
        asyncContext, servletOutputStream);
    when(servletOutputStream.isReady()).thenReturn(false);

    underTest.onWritePossible();

    verify(asyncContext, never()).complete();
    verify(servletOutputStream, never()).write(any(), anyInt(), anyInt());
    verify(byteArrayInputStream, never()).close();
  }

  @Test
  @SneakyThrows
  void shouldDealWithAnError() {
    byte[] array = new byte[20000];
    Arrays.fill(array, (byte) 255);

    ByteArrayInputStream byteArrayInputStream = spy(new ByteArrayInputStream(array));
    CachingDataStream underTest = new CachingDataStream(CacheType.TRACK, "123", true, byteArrayInputStream,
        asyncContext, servletOutputStream);

    underTest.onError(new Exception("CachingDataStreamTest.shouldDealWithAnError()"));

    verify(asyncContext).complete();
    verify(servletOutputStream, never()).write(any(), anyInt(), anyInt());
    verify(byteArrayInputStream).close();
  }

  @Test
  @SneakyThrows
  void shouldDealWithAnErrorWhenAsyncContextThrowsAnException() {
    byte[] array = new byte[20000];
    Arrays.fill(array, (byte) 255);

    ByteArrayInputStream byteArrayInputStream = spy(new ByteArrayInputStream(array));
    CachingDataStream underTest = new CachingDataStream(CacheType.TRACK, "123", true, byteArrayInputStream,
        asyncContext, servletOutputStream);
    doThrow(new RuntimeException("CachingDataStreamTest.shouldDealWithAnErrorWhenAsyncContextThrowsAnException()"))
        .when(asyncContext).complete();

    underTest.onError(new EofException("CachingDataStreamTest.shouldDealWithAnErrorWhenAsyncContextThrowsAnException()"));

    verify(asyncContext).complete();
    verify(servletOutputStream, never()).write(any(), anyInt(), anyInt());
    verify(byteArrayInputStream).close();
  }
}