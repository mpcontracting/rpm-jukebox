package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.mpcontracting.rpmjukebox.event.Event;
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
    public void shouldReceiveSettingsUpdatedNoProxy() {
        when(mockSettingsManager.getSystemSettings()).thenReturn(new SystemSettings());

        internetManager.eventReceived(Event.SETTINGS_UPDATED);

        OkHttpClient httpClient = (OkHttpClient)ReflectionTestUtils.getField(internetManager, "httpClient");

        assertThat("Proxy should be null", httpClient.proxy(), nullValue());
        assertThat("Proxy authenticator should be NONE", httpClient.proxyAuthenticator(), equalTo(Authenticator.NONE));
    }

    @Test
    public void shouldReceiveSettingsUpdatedUnauthenticatedProxy() {
        SystemSettings systemSettings = new SystemSettings();
        systemSettings.setProxyHost("localhost");
        systemSettings.setProxyPort(8080);

        when(mockSettingsManager.getSystemSettings()).thenReturn(systemSettings);

        internetManager.eventReceived(Event.SETTINGS_UPDATED);

        OkHttpClient httpClient = (OkHttpClient)ReflectionTestUtils.getField(internetManager, "httpClient");
        InetSocketAddress address = (InetSocketAddress)httpClient.proxy().address();

        assertThat("Proxy host should be 'localhost'", address.getHostName(), equalTo("localhost"));
        assertThat("Proxy port should be 8080", address.getPort(), equalTo(8080));
        assertThat("Proxy authenticator should be NONE", httpClient.proxyAuthenticator(), equalTo(Authenticator.NONE));
    }

    @Test
    public void shouldReceiveSettingsUpdatedAuthenticatedProxy() throws Exception {
        SystemSettings systemSettings = new SystemSettings();
        systemSettings.setProxyHost("localhost");
        systemSettings.setProxyPort(8080);
        systemSettings.setProxyRequiresAuthentication(true);
        systemSettings.setProxyUsername("username");
        systemSettings.setProxyPassword("password");

        when(mockSettingsManager.getSystemSettings()).thenReturn(systemSettings);

        internetManager.eventReceived(Event.SETTINGS_UPDATED);

        OkHttpClient httpClient = (OkHttpClient)ReflectionTestUtils.getField(internetManager, "httpClient");
        InetSocketAddress address = (InetSocketAddress)httpClient.proxy().address();
        Authenticator authenticator = httpClient.proxyAuthenticator();
        Response response = new Response.Builder().request(new Request.Builder().url("http://www.example.com").build())
            .protocol(Protocol.HTTP_1_1).code(200).build();
        Request request = authenticator.authenticate(null, response);

        assertThat("Proxy host should be 'localhost'", address.getHostName(), equalTo("localhost"));
        assertThat("Proxy port should be 8080", address.getPort(), equalTo(8080));
        assertThat("Proxy authenticator should not be NONE", httpClient.proxyAuthenticator(),
            not(equalTo(Authenticator.NONE)));
        assertThat("Response should have a Proxy-Authorization header", request.header("Proxy-Authorization"),
            notNullValue());
    }
    
    @Test
    public void shouldGet404FromInternalJettyServer() throws Exception {
        when(mockSettingsManager.getSystemSettings()).thenReturn(new SystemSettings());
        
        URL url = new URL("http://localhost:" + internalJettyPort + "/invalid");
        Response response = null;
        
        try {
            response = internetManager.openConnection(url);
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
        
        assertThat("Response code should be 404", response.code(), equalTo(404));
    }
}
