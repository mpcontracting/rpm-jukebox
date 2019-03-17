package uk.co.mpcontracting.rpmjukebox.manager;

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
    public void shouldOpenConnectionToFile() throws Exception {
        URL spyDataFile = spy(new URL("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath()));

        internetManager.openConnection(spyDataFile);

        verify(mockSettingsManager, never()).getSystemSettings();
        verify(spyDataFile, times(1)).openConnection();
    }

    @Test
    public void shouldOpenConnectionNoProxy() throws Exception {
        when(mockSettingsManager.getSystemSettings()).thenReturn(new SystemSettings());

        URL mockUrl = mock(URL.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        when(mockUrl.openConnection()).thenReturn(mockConnection);

        internetManager.openConnection(mockUrl);

        verify(mockUrl, times(1)).openConnection();
        verify(mockUrl, never()).openConnection(any());
        verify(mockConnection, never()).setRequestProperty(anyString(), anyString());
    }

    @Test
    public void shouldOpenConnectionNoProxyMissingPort() throws Exception {
        SystemSettings systemSettings = new SystemSettings();
        systemSettings.setProxyHost("localhost");

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
    public void shouldOpenConnectionNoProxyMissingHost() throws Exception {
        SystemSettings systemSettings = new SystemSettings();
        systemSettings.setProxyPort(8080);

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
    public void shouldOpenConnectionUnauthenticatedProxy() throws Exception {
        SystemSettings systemSettings = new SystemSettings();
        systemSettings.setProxyHost("localhost");
        systemSettings.setProxyPort(8080);

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
    public void shouldOpenConnectionUnauthenticatedProxyAuthenticatedFalse() throws Exception {
        SystemSettings systemSettings = new SystemSettings();
        systemSettings.setProxyHost("localhost");
        systemSettings.setProxyPort(8080);
        systemSettings.setProxyRequiresAuthentication(false);

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
    public void shouldOpenConnectionAuthenticatedProxy() throws Exception {
        SystemSettings systemSettings = new SystemSettings();
        systemSettings.setProxyHost("localhost");
        systemSettings.setProxyPort(8080);
        systemSettings.setProxyRequiresAuthentication(true);
        systemSettings.setProxyUsername("username");
        systemSettings.setProxyPassword("password");

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
