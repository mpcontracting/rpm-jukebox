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
    private EventManager mockEventManager;

    @Mock
    private SettingsManager mockSettingsManager;

    private InternetManager internetManager;

    @Before
    public void setup() {
        internetManager = new InternetManager();
        internetManager.wireSettingsManager(mockSettingsManager);

        setField(internetManager, "eventManager", mockEventManager);
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionToFile() {
        URL spyDataFile = spy(new URL("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath()));

        internetManager.openConnection(spyDataFile);

        verify(mockSettingsManager, never()).getSystemSettings();
        verify(spyDataFile, times(1)).openConnection();
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionNoProxy() {
        when(mockSettingsManager.getSystemSettings()).thenReturn(SystemSettings.builder().build());

        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockUrl.openConnection()).thenReturn(mockConnection);

        internetManager.openConnection(mockUrl);

        verify(mockUrl, times(1)).openConnection();
        verify(mockUrl, never()).openConnection(any());
        verify(mockConnection, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionNoProxyMissingPort() {
        SystemSettings systemSettings = SystemSettings.builder()
                .proxyHost("localhost")
                .build();

        when(mockSettingsManager.getSystemSettings()).thenReturn(systemSettings);

        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockUrl.openConnection()).thenReturn(mockConnection);

        internetManager.openConnection(mockUrl);

        verify(mockUrl, times(1)).openConnection();
        verify(mockUrl, never()).openConnection(any());
        verify(mockConnection, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionNoProxyMissingHost() {
        SystemSettings systemSettings = SystemSettings.builder()
                .proxyPort(8080)
                .build();

        when(mockSettingsManager.getSystemSettings()).thenReturn(systemSettings);

        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockUrl.openConnection()).thenReturn(mockConnection);

        internetManager.openConnection(mockUrl);

        verify(mockUrl, times(1)).openConnection();
        verify(mockUrl, never()).openConnection(any());
        verify(mockConnection, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionUnauthenticatedProxy() {
        SystemSettings systemSettings = SystemSettings.builder()
                .proxyHost("localhost")
                .proxyPort(8080)
                .build();

        when(mockSettingsManager.getSystemSettings()).thenReturn(systemSettings);

        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockUrl.openConnection(any())).thenReturn(mockConnection);

        internetManager.openConnection(mockUrl);

        verify(mockUrl, never()).openConnection();
        verify(mockUrl, times(1)).openConnection(any());
        verify(mockConnection, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    @SneakyThrows
    public void shouldOpenConnectionUnauthenticatedProxyAuthenticatedFalse() {
        SystemSettings systemSettings = SystemSettings.builder()
                .proxyHost("localhost")
                .proxyPort(8080)
                .proxyRequiresAuthentication(false)
                .build();

        when(mockSettingsManager.getSystemSettings()).thenReturn(systemSettings);

        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockUrl.openConnection(any())).thenReturn(mockConnection);

        internetManager.openConnection(mockUrl);

        verify(mockUrl, never()).openConnection();
        verify(mockUrl, times(1)).openConnection(any());
        verify(mockConnection, never()).setRequestProperty(anyString(), anyString());
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

        when(mockSettingsManager.getSystemSettings()).thenReturn(systemSettings);

        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockUrl.openConnection(any())).thenReturn(mockConnection);

        internetManager.openConnection(mockUrl);

        verify(mockUrl, never()).openConnection();
        verify(mockUrl, times(1)).openConnection(any());
        verify(mockConnection, times(1)).setRequestProperty(anyString(), anyString());
    }
}
