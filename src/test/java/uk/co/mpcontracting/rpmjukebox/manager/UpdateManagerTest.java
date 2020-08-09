package uk.co.mpcontracting.rpmjukebox.manager;

import com.igormaznitsa.commons.version.Version;
import javafx.application.HostServices;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.javafx.GuiState;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateManagerTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private EventManager eventManager;

    @Mock
    private SettingsManager settingsManager;

    @Mock
    private InternetManager internetManager;

    @Mock
    private HttpURLConnection httpURLConnection;

    @Mock
    private HostServices hostServices;

    private final ThreadRunner threadRunner = new ThreadRunner(Executors.newSingleThreadExecutor());

    private UpdateManager underTest;

    @Before
    @SneakyThrows
    public void setup() {
        underTest = new UpdateManager(appProperties, threadRunner);
        underTest.wireSettingsManager(settingsManager);
        underTest.wireInternetManager(internetManager);

        setField(underTest, "eventManager", eventManager);
        setField(GuiState.class, "hostServices", hostServices);

        URL versionUrl = new URL("http://www.example.com");

        when(appProperties.getVersionUrl()).thenReturn(versionUrl.toString());
        when(settingsManager.getVersion()).thenReturn(new Version("1.0.0"));
        when(settingsManager.isAppStoreBuild()).thenReturn(false);
        when(internetManager.openConnection(versionUrl)).thenReturn(httpURLConnection);
    }

    @Test
    @SneakyThrows
    public void shouldFindUpdatesAvailable() {
        when(httpURLConnection.getResponseCode()).thenReturn(200);
        when(httpURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream("99.99.99".getBytes()));

        invokeMethod(underTest, "checkForUpdates");

        assertThat(getField(underTest, "newVersion")).isNotNull();
        verify(eventManager, times(1)).fireEvent(Event.NEW_VERSION_AVAILABLE,
                new Version("99.99.99"));
    }

    @Test
    @SneakyThrows
    public void shouldNotFindUpdatesAvailable() {
        when(httpURLConnection.getResponseCode()).thenReturn(200);
        when(httpURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream("0.0.1".getBytes()));

        invokeMethod(underTest, "checkForUpdates");

        assertThat(getField(underTest, "newVersion")).isNull();
        verify(eventManager, never()).fireEvent(Event.NEW_VERSION_AVAILABLE, new Version("0.0.1"));
    }

    @Test
    @SneakyThrows
    public void shouldNotFindUpdatesOnConnectionError() {
        doThrow(new RuntimeException("UpdateManagerTest.shouldNotFindUpdatesOnConnectionError()")).when(internetManager)
                .openConnection(any());

        invokeMethod(underTest, "checkForUpdates");

        assertThat(getField(underTest, "newVersion")).isNull();
        verify(eventManager, never()).fireEvent(eq(Event.NEW_VERSION_AVAILABLE), any());
    }

    @Test
    @SneakyThrows
    public void shouldNotFindUpdatesOn404() {
        when(httpURLConnection.getResponseCode()).thenReturn(404);

        invokeMethod(underTest, "checkForUpdates");

        assertThat(getField(underTest, "newVersion")).isNull();
        verify(eventManager, never()).fireEvent(eq(Event.NEW_VERSION_AVAILABLE), any());
    }

    @Test
    @SneakyThrows
    public void shouldNotFindUpdatesAvailableOnEmptyVersionString() {
        when(httpURLConnection.getResponseCode()).thenReturn(200);
        when(httpURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));

        invokeMethod(underTest, "checkForUpdates");

        assertThat(getField(underTest, "newVersion")).isNull();
        verify(eventManager, never()).fireEvent(eq(Event.NEW_VERSION_AVAILABLE), any());
    }

    @Test
    @SneakyThrows
    public void shouldDownloadNewVersion() {
        when(appProperties.getWebsiteUrl()).thenReturn("http://www.website.url");

        invokeMethod(underTest, "downloadNewVersion");

        // Wait for invocation
        sleep(500);

        verify(hostServices, times(1)).showDocument(appProperties.getWebsiteUrl());
    }

    @Test
    @SneakyThrows
    public void shouldNotCheckForUpdatesIfIsAppStoreBuild() {
        when(settingsManager.isAppStoreBuild()).thenReturn(true);

        invokeMethod(underTest, "checkForUpdates");

        verify(internetManager, never()).openConnection(any());
    }

    @Test
    @SneakyThrows
    public void shouldCheckForUpdatesOnApplicationInitialisedEvent() {
        underTest.eventReceived(Event.APPLICATION_INITIALISED);

        // Wait for invocation
        sleep(500);

        verify(internetManager, times(1)).openConnection(any());
    }
}
