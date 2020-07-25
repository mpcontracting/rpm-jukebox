package uk.co.mpcontracting.rpmjukebox.manager;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getTestResourceFile;

@RunWith(MockitoJUnitRunner.class)
public class InternetManagerTest {

    @Mock
    private EventManager eventManager;

    @Mock
    private SettingsManager settingsManager;

    private InternetManager underTest;

    @Before
    public void setup() {
        underTest = new InternetManager();
        underTest.wireSettingsManager(settingsManager);

        setField(underTest, "eventManager", eventManager);
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionToFile() {
        URL url = spy(new URL("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath()));

        underTest.openConnection(url);

        verify(settingsManager, never()).getSystemSettings();
        verify(url, times(1)).openConnection();
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionNoProxy() {
        when(settingsManager.getSystemSettings()).thenReturn(SystemSettings.builder().build());

        URL url = mock(URL.class);
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(url.openConnection()).thenReturn(httpURLConnection);

        underTest.openConnection(url);

        verify(url, times(1)).openConnection();
        verify(url, never()).openConnection(any());
        verify(httpURLConnection, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionNoProxyMissingPort() {
        SystemSettings systemSettings = SystemSettings.builder()
                .proxyHost("localhost")
                .build();

        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        URL url = mock(URL.class);
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(url.openConnection()).thenReturn(httpURLConnection);

        underTest.openConnection(url);

        verify(url, times(1)).openConnection();
        verify(url, never()).openConnection(any());
        verify(httpURLConnection, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionNoProxyMissingHost() {
        SystemSettings systemSettings = SystemSettings.builder()
                .proxyPort(8080)
                .build();

        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        URL url = mock(URL.class);
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(url.openConnection()).thenReturn(httpURLConnection);

        underTest.openConnection(url);

        verify(url, times(1)).openConnection();
        verify(url, never()).openConnection(any());
        verify(httpURLConnection, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionUnauthenticatedProxy() {
        SystemSettings systemSettings = SystemSettings.builder()
                .proxyHost("localhost")
                .proxyPort(8080)
                .build();

        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        URL url = mock(URL.class);
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(url.openConnection(any())).thenReturn(httpURLConnection);

        underTest.openConnection(url);

        verify(url, never()).openConnection();
        verify(url, times(1)).openConnection(any());
        verify(httpURLConnection, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionUnauthenticatedProxyAuthenticatedFalse() {
        SystemSettings systemSettings = SystemSettings.builder()
                .proxyHost("localhost")
                .proxyPort(8080)
                .proxyRequiresAuthentication(false)
                .build();

        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        URL url = mock(URL.class);
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(url.openConnection(any())).thenReturn(httpURLConnection);

        underTest.openConnection(url);

        verify(url, never()).openConnection();
        verify(url, times(1)).openConnection(any());
        verify(httpURLConnection, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionAuthenticatedProxy() {
        SystemSettings systemSettings = SystemSettings.builder()
                .proxyHost("localhost")
                .proxyPort(8080)
                .proxyRequiresAuthentication(true)
                .proxyUsername("username")
                .proxyPassword("password")
                .build();

        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);

        URL url = mock(URL.class);
        HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
        when(url.openConnection(any())).thenReturn(httpURLConnection);

        underTest.openConnection(url);

        verify(url, never()).openConnection();
        verify(url, times(1)).openConnection(any());
        verify(httpURLConnection, times(1)).setRequestProperty(anyString(), anyString());
    }
}
