package uk.co.mpcontracting.rpmjukebox.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getTestResourceFile;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;

@ExtendWith(MockitoExtension.class)
class InternetServiceTest {

  @Mock
  private SettingsService settingsService;

  private InternetService underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new InternetService(settingsService);
  }

  @Test
  @SneakyThrows
  void shouldOpenConnectionToFile() {
    URL url = spy(URI.create("file:///" + getTestResourceFile("data/rpm-data.gz").getAbsolutePath()).toURL());

    underTest.openConnection(url);

    verify(settingsService, never()).getSystemSettings();
    verify(url).openConnection();
  }

  @Test
  @SneakyThrows
  void shouldOpenConnectionNoProxy() {
    when(settingsService.getSystemSettings()).thenReturn(SystemSettings.builder().build());

    URL url = mock(URL.class);
    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(url.openConnection()).thenReturn(httpURLConnection);

    underTest.openConnection(url);

    verify(url).openConnection();
    verify(url, never()).openConnection(any());
    verify(httpURLConnection, never()).setRequestProperty(anyString(), anyString());
  }

  @Test
  @SneakyThrows
  void shouldOpenConnectionNoProxyMissingPort() {
    SystemSettings systemSettings = SystemSettings.builder()
        .proxyHost("localhost")
        .build();

    when(settingsService.getSystemSettings()).thenReturn(systemSettings);

    URL url = mock(URL.class);
    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(url.openConnection()).thenReturn(httpURLConnection);

    underTest.openConnection(url);

    verify(url).openConnection();
    verify(url, never()).openConnection(any());
    verify(httpURLConnection, never()).setRequestProperty(anyString(), anyString());
  }

  @Test
  @SneakyThrows
  void shouldOpenConnectionNoProxyMissingHost() {
    SystemSettings systemSettings = SystemSettings.builder()
        .proxyPort(8080)
        .build();

    when(settingsService.getSystemSettings()).thenReturn(systemSettings);

    URL url = mock(URL.class);
    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(url.openConnection()).thenReturn(httpURLConnection);

    underTest.openConnection(url);

    verify(url).openConnection();
    verify(url, never()).openConnection(any());
    verify(httpURLConnection, never()).setRequestProperty(anyString(), anyString());
  }

  @Test
  @SneakyThrows
  void shouldOpenConnectionUnauthenticatedProxy() {
    SystemSettings systemSettings = SystemSettings.builder()
        .proxyHost("localhost")
        .proxyPort(8080)
        .build();

    when(settingsService.getSystemSettings()).thenReturn(systemSettings);

    URL url = mock(URL.class);
    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(url.openConnection(any())).thenReturn(httpURLConnection);

    underTest.openConnection(url);

    verify(url, never()).openConnection();
    verify(url).openConnection(any());
    verify(httpURLConnection, never()).setRequestProperty(anyString(), anyString());
  }

  @Test
  @SneakyThrows
  void shouldOpenConnectionUnauthenticatedProxyAuthenticatedFalse() {
    SystemSettings systemSettings = SystemSettings.builder()
        .proxyHost("localhost")
        .proxyPort(8080)
        .proxyRequiresAuthentication(false)
        .build();

    when(settingsService.getSystemSettings()).thenReturn(systemSettings);

    URL url = mock(URL.class);
    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(url.openConnection(any())).thenReturn(httpURLConnection);

    underTest.openConnection(url);

    verify(url, never()).openConnection();
    verify(url).openConnection(any());
    verify(httpURLConnection, never()).setRequestProperty(anyString(), anyString());
  }

  @Test
  @SneakyThrows
  void shouldOpenConnectionAuthenticatedProxy() {
    SystemSettings systemSettings = SystemSettings.builder()
        .proxyHost("localhost")
        .proxyPort(8080)
        .proxyRequiresAuthentication(true)
        .proxyUsername("username")
        .proxyPassword("password")
        .build();

    when(settingsService.getSystemSettings()).thenReturn(systemSettings);

    URL url = mock(URL.class);
    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(url.openConnection(any())).thenReturn(httpURLConnection);

    underTest.openConnection(url);

    verify(url, never()).openConnection();
    verify(url).openConnection(any());
    verify(httpURLConnection).setRequestProperty(anyString(), anyString());
  }
}