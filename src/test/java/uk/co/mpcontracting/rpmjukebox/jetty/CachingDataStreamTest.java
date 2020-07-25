package uk.co.mpcontracting.rpmjukebox.jetty;

import lombok.SneakyThrows;
import org.eclipse.jetty.io.EofException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@RunWith(MockitoJUnitRunner.class)
public class CachingDataStreamTest {

    private AsyncContext asyncContext;
    private ServletOutputStream servletOutputStream;

    @Before
    public void setup() {
        asyncContext = mock(AsyncContext.class);
        servletOutputStream = mock(ServletOutputStream.class);
    }

    @Test
    @SneakyThrows
    public void shouldWriteToOutputStreamAlreadyCached() {
        byte[] array = new byte[20000];
        Arrays.fill(array, (byte) 255);

        ByteArrayInputStream byteArrayInputStream = spy(new ByteArrayInputStream(array));
        CachingDataStream underTest = new CachingDataStream(CacheType.TRACK, "123", true, byteArrayInputStream,
                asyncContext, servletOutputStream);
        when(servletOutputStream.isReady()).thenReturn(true);

        underTest.onWritePossible();

        verify(asyncContext, times(1)).complete();
        verify(servletOutputStream, times(5)).write(any(), anyInt(), anyInt());
        verify(byteArrayInputStream, times(1)).close();
    }

    @Test
    @SneakyThrows
    public void shouldWriteToOutputStreamNotCached() {
        byte[] array = new byte[20000];
        Arrays.fill(array, (byte) 255);

        ApplicationContext originalContext = (ApplicationContext) getField(ContextHelper.class, "applicationContext");
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        setField(ContextHelper.class, "applicationContext", applicationContext);

        CacheManager cacheManager = mock(CacheManager.class);
        when(applicationContext.getBean(CacheManager.class)).thenReturn(cacheManager);

        try {
            ByteArrayInputStream byteArrayInputStream = spy(new ByteArrayInputStream(array));

            CachingDataStream underTest = new CachingDataStream(CacheType.TRACK, "123", false, byteArrayInputStream,
                    asyncContext, servletOutputStream);
            when(servletOutputStream.isReady()).thenReturn(true);

            underTest.onWritePossible();

            verify(asyncContext, times(1)).complete();
            verify(servletOutputStream, times(5)).write(any(), anyInt(), anyInt());
            verify(cacheManager, times(1)).writeCache(CacheType.TRACK, "123", array);
            verify(byteArrayInputStream, times(1)).close();
        } finally {
            setField(ContextHelper.class, "applicationContext", originalContext);
        }
    }

    @Test
    @SneakyThrows
    public void shouldNotWriteToOutputStreamWhenNotReady() {
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
    public void shouldDealWithAnError() {
        byte[] array = new byte[20000];
        Arrays.fill(array, (byte) 255);

        ByteArrayInputStream byteArrayInputStream = spy(new ByteArrayInputStream(array));
        CachingDataStream underTest = new CachingDataStream(CacheType.TRACK, "123", true, byteArrayInputStream,
                asyncContext, servletOutputStream);

        underTest.onError(new Exception("CachingDataStreamTest.shouldDealWithAnError()"));

        verify(asyncContext, times(1)).complete();
        verify(servletOutputStream, never()).write(any(), anyInt(), anyInt());
        verify(byteArrayInputStream, times(1)).close();
    }

    @Test
    @SneakyThrows
    public void shouldDealWithAnErrorWhenAsyncContextThrowsAnException() {
        byte[] array = new byte[20000];
        Arrays.fill(array, (byte) 255);

        ByteArrayInputStream byteArrayInputStream = spy(new ByteArrayInputStream(array));
        CachingDataStream underTest = new CachingDataStream(CacheType.TRACK, "123", true, byteArrayInputStream,
                asyncContext, servletOutputStream);
        doThrow(new RuntimeException("CachingDataStreamTest.shouldDealWithAnErrorWhenAsyncContextThrowsAnException()"))
                .when(asyncContext).complete();

        underTest.onError(
                new EofException("CachingDataStreamTest.shouldDealWithAnErrorWhenAsyncContextThrowsAnException()"));

        verify(asyncContext, times(1)).complete();
        verify(servletOutputStream, never()).write(any(), anyInt(), anyInt());
        verify(byteArrayInputStream, times(1)).close();
    }
}
