package uk.co.mpcontracting.rpmjukebox.manager;

import com.igormaznitsa.commons.version.Version;
import de.felixroske.jfxsupport.GUIState;
import javafx.application.HostServices;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateManagerTest {

    @Mock
    private AppProperties mockAppProperties;

    @Mock
    private EventManager mockEventManager;

    @Mock
    private SettingsManager mockSettingsManager;

    @Mock
    private InternetManager mockInternetManager;

    @Mock
    private HttpURLConnection mockHttpURLConnection;

    @Mock
    private HostServices mockHostServices;

    private URL versionUrl;
    private UpdateManager updateManager;

    @Before
    public void setup() throws Exception {
        versionUrl = new URL("http://www.example.com");
        updateManager = new UpdateManager(mockAppProperties, mockSettingsManager, mockInternetManager);

        setField(updateManager, "eventManager", mockEventManager);
        setField(GUIState.class, "hostServices", mockHostServices);

        when(mockAppProperties.getVersionUrl()).thenReturn(versionUrl.toString());
        when(mockSettingsManager.getVersion()).thenReturn(new Version("1.0.0"));
        when(mockInternetManager.openConnection(versionUrl)).thenReturn(mockHttpURLConnection);
    }

    @Test
    public void shouldFindUpdatesAvailable() throws Exception {
        when(mockHttpURLConnection.getResponseCode()).thenReturn(200);
        when(mockHttpURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream("99.99.99".getBytes()));

        invokeMethod(updateManager, "checkForUpdates");

        assertThat(getField(updateManager, "newVersion")).isNotNull();
        verify(mockEventManager, times(1)).fireEvent(Event.NEW_VERSION_AVAILABLE,
                new Version("99.99.99"));
    }

    @Test
    public void shouldNotFindUpdatesAvailable() throws Exception {
        when(mockHttpURLConnection.getResponseCode()).thenReturn(200);
        when(mockHttpURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream("0.0.1".getBytes()));

        invokeMethod(updateManager, "checkForUpdates");

        assertThat(getField(updateManager, "newVersion")).isNull();
        verify(mockEventManager, never()).fireEvent(Event.NEW_VERSION_AVAILABLE, new Version("0.0.1"));
    }

    @Test
    public void shouldNotFindUpdatesOnConnectionError() throws Exception {
        doThrow(new RuntimeException("UpdateManagerTest.shouldNotFindUpdatesOnConnectionError()")).when(mockInternetManager)
            .openConnection(any());

        invokeMethod(updateManager, "checkForUpdates");

        assertThat(getField(updateManager, "newVersion")).isNull();
        verify(mockEventManager, never()).fireEvent(eq(Event.NEW_VERSION_AVAILABLE), any());
    }

   @Test
    public void shouldNotFindUpdatesOn404() throws Exception {
        when(mockHttpURLConnection.getResponseCode()).thenReturn(404);

        invokeMethod(updateManager, "checkForUpdates");

        assertThat(getField(updateManager, "newVersion")).isNull();
        verify(mockEventManager, never()).fireEvent(eq(Event.NEW_VERSION_AVAILABLE), any());
    }

    @Test
    public void shouldNotFindUpdatesAvailableOnEmptyVersionString() throws Exception {
        when(mockHttpURLConnection.getResponseCode()).thenReturn(200);
        when(mockHttpURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] {}));

        invokeMethod(updateManager, "checkForUpdates");

        assertThat(getField(updateManager, "newVersion")).isNull();
        verify(mockEventManager, never()).fireEvent(eq(Event.NEW_VERSION_AVAILABLE), any());
    }

    @Test
    public void shouldDownloadNewVersion() throws Exception {
        when(mockAppProperties.getWebsiteUrl()).thenReturn("http://www.website.url");

        invokeMethod(updateManager, "downloadNewVersion");

        // Wait for invocation
        sleep(500);

        verify(mockHostServices, times(1)).showDocument(mockAppProperties.getWebsiteUrl());
    }

    @Test
    public void shouldCheckForUpdatesOnApplicationInitialisedEvent() throws Exception {
        updateManager.eventReceived(Event.APPLICATION_INITIALISED);

        // Wait for invocation
        sleep(500);

        verify(mockInternetManager, times(1)).openConnection(any());
    }
}
