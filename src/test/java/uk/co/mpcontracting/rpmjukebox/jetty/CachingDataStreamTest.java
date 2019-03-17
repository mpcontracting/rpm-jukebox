package uk.co.mpcontracting.rpmjukebox.jetty;

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

        ApplicationContext originalContext = (ApplicationContext) getField(ContextHelper.class, "applicationContext");
        ApplicationContext mockContext = mock(ApplicationContext.class);
        setField(ContextHelper.class, "applicationContext", mockContext);

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
            setField(ContextHelper.class, "applicationContext", originalContext);
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
        doThrow(new RuntimeException("CachingDataStreamTest.shouldDealWithAnErrorWhenAsyncContextThrowsAnException()"))
            .when(mockAsyncContext).complete();

        dataStream.onError(
            new EofException("CachingDataStreamTest.shouldDealWithAnErrorWhenAsyncContextThrowsAnException()"));

        verify(mockAsyncContext, times(1)).complete();
        verify(mockServletOutputStream, never()).write(any(), anyInt(), anyInt());
        verify(spyInputStream, times(1)).close();
    }
}
