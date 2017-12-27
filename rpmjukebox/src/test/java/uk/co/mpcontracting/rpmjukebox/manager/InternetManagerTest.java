package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class InternetManagerTest extends AbstractTest {

    @Autowired
    private InternetManager internetManager;

    @Value("${internal.jetty.port}")
    private int internalJettyPort;

    @Mock
    private SettingsManager mockSettingsManager;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(internetManager, "eventManager", getMockEventManager());
        ReflectionTestUtils.setField(internetManager, "settingsManager", mockSettingsManager);
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

    @Test
    public void shouldGet404FromInternalJettyServer() throws Exception {
        when(mockSettingsManager.getSystemSettings()).thenReturn(new SystemSettings());

        URL url = new URL("http://localhost:" + internalJettyPort + "/invalid");
        HttpURLConnection connection = null;
        
        try {
            connection = internetManager.openConnection(url);
            
            assertThat("Response code should be 404", connection.getResponseCode(), equalTo(404));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
