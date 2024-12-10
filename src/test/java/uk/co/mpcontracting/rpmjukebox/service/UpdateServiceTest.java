package uk.co.mpcontracting.rpmjukebox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;
import static uk.co.mpcontracting.rpmjukebox.event.Event.APPLICATION_INITIALISED;
import static uk.co.mpcontracting.rpmjukebox.event.Event.NEW_VERSION_AVAILABLE;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import com.igormaznitsa.commons.version.Version;
import de.felixroske.jfxsupport.GUIState;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.Executors;
import javafx.application.HostServices;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractEventAwareObjectTest;
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;

class UpdateServiceTest extends AbstractEventAwareObjectTest {

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private InternetService internetService;

  @Mock
  private SettingsService settingsService;

  @Mock
  private HttpURLConnection httpURLConnection;

  @Mock
  private HostServices hostServices;

  private final ThreadRunner threadRunner = new ThreadRunner(Executors.newSingleThreadExecutor());

  private UpdateService underTest;

  @BeforeEach
  @SneakyThrows
  void beforeEach() {
    underTest = new UpdateService(threadRunner, applicationProperties, internetService, settingsService);

    setField(GUIState.class, "hostServices", hostServices);

    URL versionUrl = URI.create("http://www.example.com").toURL();

    lenient().when(applicationProperties.getVersionUrl()).thenReturn(versionUrl.toString());
    lenient().when(internetService.openConnection(versionUrl)).thenReturn(httpURLConnection);
    lenient().when(settingsService.getVersion()).thenReturn(new Version("1.0.0"));
  }

  @Test
  @SneakyThrows
  void shouldFindUpdatesAvailable() {
    when(httpURLConnection.getResponseCode()).thenReturn(200);
    when(httpURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream("99.99.99".getBytes()));

    invokeMethod(underTest, "checkForUpdates");

    assertThat(getField(underTest, "newVersion", Version.class)).isNotNull();
    verify(eventProcessor).fireEvent(NEW_VERSION_AVAILABLE, new Version("99.99.99"));
  }

  @Test
  @SneakyThrows
  void shouldNotFindUpdatesAvailable() {
    when(httpURLConnection.getResponseCode()).thenReturn(200);
    when(httpURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream("0.0.1".getBytes()));

    invokeMethod(underTest, "checkForUpdates");

    assertThat(getField(underTest, "newVersion", Version.class)).isNull();
    verify(eventProcessor, never()).fireEvent(NEW_VERSION_AVAILABLE, new Version("0.0.1"));
  }

  @Test
  @SneakyThrows
  void shouldNotFindUpdatesOnConnectionError() {
    doThrow(new RuntimeException("UpdateManagerTest.shouldNotFindUpdatesOnConnectionError()")).when(internetService)
        .openConnection(any());

    invokeMethod(underTest, "checkForUpdates");

    assertThat(getField(underTest, "newVersion", Version.class)).isNull();
    verify(eventProcessor, never()).fireEvent(eq(NEW_VERSION_AVAILABLE), any());
  }

  @Test
  @SneakyThrows
  void shouldNotFindUpdatesOn404() {
    when(httpURLConnection.getResponseCode()).thenReturn(404);

    invokeMethod(underTest, "checkForUpdates");

    assertThat(getField(underTest, "newVersion", Version.class)).isNull();
    verify(eventProcessor, never()).fireEvent(eq(NEW_VERSION_AVAILABLE), any());
  }

  @Test
  @SneakyThrows
  void shouldNotFindUpdatesAvailableOnEmptyVersionString() {
    when(httpURLConnection.getResponseCode()).thenReturn(200);
    when(httpURLConnection.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{}));

    invokeMethod(underTest, "checkForUpdates");

    assertThat(getField(underTest, "newVersion", Version.class)).isNull();
    verify(eventProcessor, never()).fireEvent(eq(NEW_VERSION_AVAILABLE), any());
  }

  @Test
  @SneakyThrows
  void shouldDownloadNewVersion() {
    when(applicationProperties.getWebsiteUrl()).thenReturn("http://www.website.url");

    invokeMethod(underTest, "downloadNewVersion");

    // Wait for invocation
    Thread.sleep(500);

    verify(hostServices).showDocument(applicationProperties.getWebsiteUrl());
  }

  @Test
  @SneakyThrows
  void shouldCheckForUpdatesOnApplicationInitialisedEvent() {
    underTest.eventReceived(APPLICATION_INITIALISED);

    // Wait for invocation
    Thread.sleep(500);

    verify(internetService).openConnection(any());
  }
}