package uk.co.mpcontracting.rpmjukebox.jetty;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;

import org.eclipse.jetty.io.EofException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.manager.CacheManager;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.ContextHelper;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class CachingDataStreamTest extends AbstractTest {

    private AsyncContext mockAsyncContext;
    private ServletOutputStream mockServletOutputStream;

    @Before
    public void setup() {
        mockAsyncContext = mock(AsyncContext.class);
        mockServletOutputStream = mock(ServletOutputStream.class);
    }

    @Test
    public void shouldWriteToOutputStreamAlreadyCached() throws Exception {
        byte[] array = new byte[20000];
        Arrays.fill(array, (byte)255);

        ByteArrayInputStream spyInputStream = spy(new ByteArrayInputStream(array));
        CachingDataStream dataStream = new CachingDataStream(CacheType.TRACK, "123", true, spyInputStream,
            mockAsyncContext, mockServletOutputStream);
        when(mockServletOutputStream.isReady()).thenReturn(true);

        dataStream.onWritePossible();

        verify(mockAsyncContext, times(1)).complete();
        verify(mockServletOutputStream, times(5)).write(any(), anyInt(), anyInt());
        verify(spyInputStream, times(1)).close();
    }

    @Test
    public void shouldWriteToOutputStreamNotCached() throws Exception {
        byte[] array = new byte[20000];
        Arrays.fill(array, (byte)255);

        ApplicationContext originalContext = (ApplicationContext)ReflectionTestUtils.getField(ContextHelper.class,
            "applicationContext");
        ApplicationContext mockContext = mock(ApplicationContext.class);
        ReflectionTestUtils.setField(ContextHelper.class, "applicationContext", mockContext);

        CacheManager mockCacheManager = mock(CacheManager.class);
        when(mockContext.getBean(CacheManager.class)).thenReturn(mockCacheManager);

        try {
            ByteArrayInputStream spyInputStream = spy(new ByteArrayInputStream(array));

            CachingDataStream dataStream = new CachingDataStream(CacheType.TRACK, "123", false, spyInputStream,
                mockAsyncContext, mockServletOutputStream);
            when(mockServletOutputStream.isReady()).thenReturn(true);

            dataStream.onWritePossible();

            verify(mockAsyncContext, times(1)).complete();
            verify(mockServletOutputStream, times(5)).write(any(), anyInt(), anyInt());
            verify(mockCacheManager, times(1)).writeCache(CacheType.TRACK, "123", array);
            verify(spyInputStream, times(1)).close();
        } finally {
            ReflectionTestUtils.setField(ContextHelper.class, "applicationContext", originalContext);
        }
    }

    @Test
    public void shouldNotWriteToOutputStreamWhenNotReady() throws Exception {
        byte[] array = new byte[20000];
        Arrays.fill(array, (byte)255);

        ByteArrayInputStream spyInputStream = spy(new ByteArrayInputStream(array));
        CachingDataStream dataStream = new CachingDataStream(CacheType.TRACK, "123", true, spyInputStream,
            mockAsyncContext, mockServletOutputStream);
        when(mockServletOutputStream.isReady()).thenReturn(false);

        dataStream.onWritePossible();

        verify(mockAsyncContext, never()).complete();
        verify(mockServletOutputStream, never()).write(any(), anyInt(), anyInt());
        verify(spyInputStream, never()).close();
    }

    @Test
    public void shouldDealWithAnError() throws Exception {
        byte[] array = new byte[20000];
        Arrays.fill(array, (byte)255);

        ByteArrayInputStream spyInputStream = spy(new ByteArrayInputStream(array));
        CachingDataStream dataStream = new CachingDataStream(CacheType.TRACK, "123", true, spyInputStream,
            mockAsyncContext, mockServletOutputStream);
        when(mockServletOutputStream.isReady()).thenReturn(true);

        dataStream.onError(new Exception("CachingDataStreamTest.shouldDealWithAnError()"));

        verify(mockAsyncContext, times(1)).complete();
        verify(mockServletOutputStream, never()).write(any(), anyInt(), anyInt());
        verify(spyInputStream, times(1)).close();
    }

    @Test
    public void shouldDealWithAnErrorWhenAsyncContextThrowsAnException() throws Exception {
        byte[] array = new byte[20000];
        Arrays.fill(array, (byte)255);

        ByteArrayInputStream spyInputStream = spy(new ByteArrayInputStream(array));
        CachingDataStream dataStream = new CachingDataStream(CacheType.TRACK, "123", true, spyInputStream,
            mockAsyncContext, mockServletOutputStream);
        when(mockServletOutputStream.isReady()).thenReturn(true);
        doThrow(new RuntimeException("CachingDataStreamTest.shouldDealWithAnErrorWhenAsyncContextThrowsAnException()"))
            .when(mockAsyncContext).complete();

        dataStream.onError(
            new EofException("CachingDataStreamTest.shouldDealWithAnErrorWhenAsyncContextThrowsAnException()"));

        verify(mockAsyncContext, times(1)).complete();
        verify(mockServletOutputStream, never()).write(any(), anyInt(), anyInt());
        verify(spyInputStream, times(1)).close();
    }
}
