package uk.co.mpcontracting.rpmjukebox.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.model.Repeat.ALL;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getConfigDirectory;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getDateTimeInMillis;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getLocalDateTimeInMillis;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getTestResourceFile;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_FAVOURITES;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_SEARCH;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.igormaznitsa.commons.version.Version;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.settings.Settings;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.settings.Window;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class SettingsServiceTest extends AbstractGuiTest {

  @MockBean
  private RpmJukebox rpmJukebox;

  @Mock
  private ApplicationProperties applicationProperties;

  @MockBean
  private StringResourceService stringResourceService;

  @MockBean
  private InternetService internetService;

  @MockBean
  private MediaService mediaService;

  @MockBean
  private PlaylistService playlistService;

  @MockBean
  private SearchService searchService;

  @Mock
  private URL dataFile;

  private SettingsService underTest;

  @BeforeEach
  void beforeEach() {
    underTest = spy(new SettingsService(rpmJukebox, applicationProperties, stringResourceService));
    setField(underTest, "internetService", internetService);
    setField(underTest, "mediaService", mediaService);
    setField(underTest, "playlistService", playlistService);
    setField(underTest, "searchService", searchService);

    setField(underTest, "gson", new GsonBuilder().setPrettyPrinting().create());
    setField(underTest, "version", new Version("1.0.0"));
    setField(underTest, "dataFile", dataFile);

    lenient().when(applicationProperties.getVersion()).thenReturn("1.0.0");
    lenient().when(applicationProperties.getCacheSizeMb()).thenReturn(250);

    lenient().when(applicationProperties.getLastIndexedFile()).thenReturn("last-indexed");
    lenient().when(underTest.getFileFromConfigDirectory("last-indexed")).thenReturn(new File(getConfigDirectory(), "last-indexed"));

    lenient().when(applicationProperties.getWindowSettingsFile()).thenReturn("window.json");
    lenient().when(underTest.getFileFromConfigDirectory("window.json")).thenReturn(new File(getConfigDirectory(), "window.json"));

    lenient().when(applicationProperties.getSystemSettingsFile()).thenReturn("system.json");
    lenient().when(underTest.getFileFromConfigDirectory("system.json")).thenReturn(new File(getConfigDirectory(), "system.json"));

    lenient().when(applicationProperties.getUserSettingsFile()).thenReturn("rpm-jukebox.json");
    lenient().when(underTest.getFileFromConfigDirectory("rpm-jukebox.json")).thenReturn(new File(getConfigDirectory(), "rpm-jukebox.json"));
  }

  @Test
  void shouldGetFileFromConfigDirectory() {
    File expected = new File(RpmJukebox.getConfigDirectory(), "test");
    File result = underTest.getFileFromConfigDirectory("test");

    assertThat(result.getAbsolutePath()).isEqualTo(expected.getAbsolutePath());
  }

  @Test
  @SneakyThrows
  void shouldReturnNewVersionWhenSystemVersionIsNull() {
    File testSettings = getTestResourceFile("json/settingsManager-shouldReturnNewVersionWhenSystemVersionIsNull.json");
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getSystemSettingsFile());

    Files.copy(testSettings, settingsFile);

    underTest.loadSystemSettings();

    setField(underTest, "version", new Version("1.1.1"));

    assertThat(underTest.isNewVersion()).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldReturnNewVersionWhenSystemVersionIsLower() {
    File testSettings = getTestResourceFile("json/settingsManager-shouldReturnNewVersionWhenSystemVersionIsLower.json");
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getSystemSettingsFile());

    Files.copy(testSettings, settingsFile);

    underTest.loadSystemSettings();

    setField(underTest, "version", new Version("1.1.1"));

    assertThat(underTest.isNewVersion()).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldNotReturnNewVersionWhenSystemVersionIsEqual() {
    File testSettings = getTestResourceFile("json/settingsManager-shouldNotReturnNewVersionWhenSystemVersionIsEqual.json");
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getSystemSettingsFile());

    Files.copy(testSettings, settingsFile);

    underTest.loadSystemSettings();

    setField(underTest, "version", new Version("1.1.1"));

    assertThat(underTest.isNewVersion()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldShowHttpDataFileHasExpired() {
    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(dataFile.getProtocol()).thenReturn("http");
    when(internetService.openConnection(dataFile)).thenReturn(httpURLConnection);
    when(httpURLConnection.getLastModified()).thenReturn(getDateTimeInMillis(1975, 1, 1, 0, 0));
    doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(underTest).getLastIndexedDate();

    boolean result = underTest.hasDataFileExpired();

    assertThat(result).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldShowHttpDataFileHasNotExpiredAsLastModifiedNotOneHourOld() {
    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(dataFile.getProtocol()).thenReturn("http");
    when(internetService.openConnection(dataFile)).thenReturn(httpURLConnection);
    when(httpURLConnection.getLastModified())
        .thenReturn(getLocalDateTimeInMillis(LocalDateTime.now().minusMinutes(30)));
    doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(underTest).getLastIndexedDate();

    boolean result = underTest.hasDataFileExpired();

    assertThat(result).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldShowHttpDataFileHasNotExpiredAsLastModifiedBeforeLastIndexed() {
    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(dataFile.getProtocol()).thenReturn("http");
    when(internetService.openConnection(dataFile)).thenReturn(httpURLConnection);
    when(httpURLConnection.getLastModified()).thenReturn(getDateTimeInMillis(1971, 1, 1, 0, 0));
    doReturn(LocalDateTime.of(1975, 1, 1, 0, 0)).when(underTest).getLastIndexedDate();

    boolean result = underTest.hasDataFileExpired();

    assertThat(result).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldShowHttpDataFileHasNotExpiredOnLastModifiedError() {
    HttpURLConnection httpURLConnection = mock(HttpURLConnection.class);
    when(dataFile.getProtocol()).thenReturn("http");
    when(internetService.openConnection(dataFile)).thenReturn(httpURLConnection);
    doThrow(new RuntimeException("SettingsManagerTest.shouldShowHttpDataFileHasNotExpiredOnLastModifiedError()"))
        .when(httpURLConnection).getLastModified();

    boolean result = underTest.hasDataFileExpired();

    assertThat(result).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldShowHttpDataFileHasNotExpiredOnConnectionError() {
    when(dataFile.getProtocol()).thenReturn("http");
    doThrow(new RuntimeException("SettingsManagerTest.shouldShowHttpDataFileHasNotExpiredOnConnectionError()"))
        .when(internetService).openConnection(dataFile);

    boolean result = underTest.hasDataFileExpired();

    assertThat(result).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldShowFileSystemDataFileHasExpired() {
    File lastIndexedFile = new File(getConfigDirectory(), "last-indexed");
    lastIndexedFile.createNewFile();
    lastIndexedFile.setLastModified(getDateTimeInMillis(1975, 1, 1, 0, 0));
    when(dataFile.getProtocol()).thenReturn("file");
    when(dataFile.toURI()).thenReturn(new File(lastIndexedFile.getAbsolutePath()).toURI());
    doReturn(LocalDateTime.of(1971, 1, 1, 0, 0)).when(underTest).getLastIndexedDate();

    boolean result = underTest.hasDataFileExpired();

    assertThat(result).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldShowFileSystemDataFileHasNotExpiredOnFileReadError() {
    when(dataFile.getProtocol()).thenReturn("file");
    doThrow(new RuntimeException("SettingsManagerTest.shouldShowFileSystemDataFileHasNotExpiredOnFileReadError()"))
        .when(dataFile).toURI();

    boolean result = underTest.hasDataFileExpired();

    assertThat(result).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldGetLastIndexedDate() {
    LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(
        LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    ), ZoneId.systemDefault());
    File lastIndexedFile = underTest.getFileFromConfigDirectory(applicationProperties.getLastIndexedFile());

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
      writer.write(Long.toString(now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
      writer.newLine();
    }

    LocalDateTime result = underTest.getLastIndexedDate();

    assertThat(result).isEqualTo(now);
  }

  @Test
  void shouldGetDefaultLastIndexedDate() {
    doNothing().when(underTest).setLastIndexedDate(any());

    LocalDateTime result = underTest.getLastIndexedDate();

    assertThat(result).isAfter(LocalDateTime.now().minusMinutes(1));
  }

  @Test
  @SneakyThrows
  void shouldNotGetLastIndexedDateOnFileReadError() {
    File lastIndexedFile = underTest.getFileFromConfigDirectory(applicationProperties.getLastIndexedFile());

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
      writer.write("Unparseable");
      writer.newLine();
    }

    LocalDateTime result = underTest.getLastIndexedDate();

    assertThat(result).isNull();
  }

  @Test
  @SneakyThrows
  void shouldSetLastIndexedDate() {
    LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(
        LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    ), ZoneId.systemDefault());

    underTest.setLastIndexedDate(now);

    LocalDateTime lastIndexed;
    File lastIndexedFile = underTest.getFileFromConfigDirectory(applicationProperties.getLastIndexedFile());
    try (BufferedReader reader = new BufferedReader(new FileReader(lastIndexedFile))) {
      lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())),
          ZoneId.systemDefault());
    }

    assertThat(lastIndexed).isEqualTo(now);
  }

  @Test
  @SneakyThrows
  void shouldSetLastIndexedDateIfAlreadyExists() {
    File writeLastIndexedFile = new File(getConfigDirectory(), "last-indexed");
    LocalDateTime originalLastIndexed = LocalDateTime.of(1971, 1, 1, 0, 0);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(writeLastIndexedFile))) {
      writer.write(Long.toString(originalLastIndexed.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
      writer.newLine();
    }

    LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(
        LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    ), ZoneId.systemDefault());

    underTest.setLastIndexedDate(now);

    LocalDateTime lastIndexed;
    File lastIndexedFile = underTest.getFileFromConfigDirectory(applicationProperties.getLastIndexedFile());
    try (BufferedReader reader = new BufferedReader(new FileReader(lastIndexedFile))) {
      lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())),
          ZoneId.systemDefault());
    }

    assertThat(lastIndexed).isEqualTo(now);
  }

  @Test
  void shouldNotSetLastIndexedDateOnExceptionIfNotAlreadyExists() {
    LocalDateTime localDateTime = mock(LocalDateTime.class);
    doThrow(new RuntimeException("SettingsManagerTest.shouldNotSetLastIndexDateOnException()"))
        .when(localDateTime).atZone(any());

    underTest.setLastIndexedDate(localDateTime);

    File lastIndexedFile = underTest.getFileFromConfigDirectory(applicationProperties.getLastIndexedFile());

    assertThat(lastIndexedFile.exists()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldLeaveLastIndexedDateOnExceptionIfAlreadyExists() {
    File writeLastIndexedFile = new File(getConfigDirectory(), "last-indexed");
    LocalDateTime originalLastIndexed = LocalDateTime.of(1971, 1, 1, 0, 0);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(writeLastIndexedFile))) {
      writer.write(Long.toString(originalLastIndexed.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
      writer.newLine();
    }

    LocalDateTime localDateTime = mock(LocalDateTime.class);
    doThrow(new RuntimeException("SettingsManagerTest.shouldNotSetLastIndexDateOnException()"))
        .when(localDateTime).atZone(any());

    underTest.setLastIndexedDate(localDateTime);

    LocalDateTime lastIndexed ;
    File readLastIndexedFile = underTest.getFileFromConfigDirectory(applicationProperties.getLastIndexedFile());
    try (BufferedReader reader = new BufferedReader(new FileReader(readLastIndexedFile))) {
      lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())),
          ZoneId.systemDefault());
    }

    assertThat(lastIndexed).isEqualTo(originalLastIndexed);
  }

  @Test
  void shouldLoadWindowSettingsFromDefault() {
    Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
    double width = (bounds.getWidth() / 100d) * 75d;
    double height = (bounds.getHeight() / 100d) * 75d;
    double x = (bounds.getWidth() - width) / 2d;
    double y = (bounds.getHeight() - height) / 2d;

    Stage stage = mock(Stage.class);
    underTest.loadWindowSettings(stage);

    verify(stage).setX(x);
    verify(stage).setY(y);
    verify(stage).setWidth(width);
    verify(stage).setHeight(height);
  }

  @Test
  @SneakyThrows
  void shouldLoadWindowSettingsFromFile() {
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getWindowSettingsFile());
    Window window = Window.builder()
        .x(100)
        .y(200)
        .width(300)
        .height(400)
        .build();

    try (FileWriter fileWriter = new FileWriter(settingsFile)) {
      fileWriter.write(new Gson().toJson(window));
    }

    Stage stage = mock(Stage.class);
    underTest.loadWindowSettings(stage);

    verify(stage).setX(100d);
    verify(stage).setY(200d);
    verify(stage).setWidth(300d);
    verify(stage).setHeight(400d);
  }

  @Test
  @SneakyThrows
  void shouldNotLoadWindowSettingsFromFileOnException() {
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getWindowSettingsFile());
    Window window = Window.builder()
        .x(100)
        .y(200)
        .width(300)
        .height(400)
        .build();

    try (FileWriter fileWriter = new FileWriter(settingsFile)) {
      fileWriter.write(new Gson().toJson(window));
    }

    Gson gson = mock(Gson.class);
    Stage stage = mock(Stage.class);
    doThrow(new RuntimeException("SettingsManagerTest.shouldNotLoadWindowSettingsFromFileOnException()"))
        .when(gson).fromJson(any(FileReader.class), (Class<?>) any(Class.class));

    setField(underTest, "gson", gson);

    underTest.loadWindowSettings(stage);

    verify(stage, never()).setX(anyDouble());
    verify(stage, never()).setY(anyDouble());
    verify(stage, never()).setWidth(anyDouble());
    verify(stage, never()).setHeight(anyDouble());
  }

  @Test
  @SneakyThrows
  void shouldSaveWindowSettings() {
    Stage stage = mock(Stage.class);
    when(stage.getX()).thenReturn(100d);
    when(stage.getY()).thenReturn(200d);
    when(stage.getWidth()).thenReturn(300d);
    when(stage.getHeight()).thenReturn(400d);

    underTest.saveWindowSettings(stage);

    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getWindowSettingsFile());
    Window window;

    try (FileReader fileReader = new FileReader(settingsFile)) {
      window = new Gson().fromJson(fileReader, Window.class);
    }

    assertThat(window.getX()).isEqualTo(100d);
    assertThat(window.getY()).isEqualTo(200d);
    assertThat(window.getWidth()).isEqualTo(300d);
    assertThat(window.getHeight()).isEqualTo(400d);
  }

  @Test
  @SneakyThrows
  void shouldNotSaveWindowSettingsOnException() {
    Stage stage = mock(Stage.class);
    when(stage.getX()).thenReturn(100d);
    when(stage.getY()).thenReturn(200d);
    when(stage.getWidth()).thenReturn(300d);
    when(stage.getHeight()).thenReturn(400d);

    Gson gson = mock(Gson.class);
    doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveWindowSettingsOnException()")).when(gson)
        .toJson(any(Window.class));

    setField(underTest, "gson", gson);

    underTest.saveWindowSettings(stage);

    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getWindowSettingsFile());
    Window window;

    try (FileReader fileReader = new FileReader(settingsFile)) {
      window = new Gson().fromJson(fileReader, Window.class);
    }

    assertThat(window).isNull();
  }

  @Test
  void shouldLoadSystemSettingsFromDefault() {
    doNothing().when(underTest).saveSystemSettings();

    underTest.loadSystemSettings();

    SystemSettings systemSettings = underTest.getSystemSettings();

    assertThat(systemSettings.getCacheSizeMb()).isEqualTo(applicationProperties.getCacheSizeMb());
    assertThat(systemSettings.getProxyHost()).isNull();
    assertThat(systemSettings.getProxyPort()).isNull();
    assertThat(systemSettings.getProxyRequiresAuthentication()).isNull();
    assertThat(systemSettings.getProxyUsername()).isNull();
    assertThat(systemSettings.getProxyPassword()).isNull();

    verify(underTest).saveSystemSettings();
  }

  @Test
  @SneakyThrows
  void shouldLoadSystemSettingsFromFile() {
    File testSettings = getTestResourceFile("json/settingsManager-shouldLoadSystemSettingsFromFile.json");
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getSystemSettingsFile());

    Files.copy(testSettings, settingsFile);

    underTest.loadSystemSettings();

    SystemSettings systemSettings = underTest.getSystemSettings();

    assertThat(systemSettings.getCacheSizeMb()).isEqualTo(123);
    assertThat(systemSettings.getProxyHost()).isEqualTo("localhost");
    assertThat(systemSettings.getProxyPort()).isEqualTo(8080);
    assertThat(systemSettings.getProxyRequiresAuthentication()).isTrue();
    assertThat(systemSettings.getProxyUsername()).isEqualTo("username");
    assertThat(systemSettings.getProxyPassword()).isEqualTo("password");

    verify(underTest, never()).saveSystemSettings();
  }

  @Test
  @SneakyThrows
  void shouldNotLoadSystemSettingsFromAnInvalidFile() {
    doNothing().when(underTest).saveSystemSettings();

    File testSettings = getTestResourceFile(
        "json/settingsManager-shouldNotLoadSystemSettingsFromAnInvalidFile.json");
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getSystemSettingsFile());

    Files.copy(testSettings, settingsFile);

    underTest.loadSystemSettings();

    SystemSettings systemSettings = underTest.getSystemSettings();

    assertThat(systemSettings.getCacheSizeMb()).isEqualTo(applicationProperties.getCacheSizeMb());
    assertThat(systemSettings.getProxyHost()).isNull();
    assertThat(systemSettings.getProxyPort()).isNull();
    assertThat(systemSettings.getProxyRequiresAuthentication()).isNull();
    assertThat(systemSettings.getProxyUsername()).isNull();
    assertThat(systemSettings.getProxyPassword()).isNull();

    verify(underTest).saveSystemSettings();
  }

  @Test
  @SneakyThrows
  void shouldSaveSystemSettings() {
    setField(underTest, "systemSettings", SystemSettings.builder().build());

    SystemSettings systemSettings = underTest.getSystemSettings();
    systemSettings.setCacheSizeMb(123);
    systemSettings.setProxyHost("localhost");
    systemSettings.setProxyPort(8080);
    systemSettings.setProxyRequiresAuthentication(true);
    systemSettings.setProxyUsername("username");
    systemSettings.setProxyPassword("password");

    underTest.saveSystemSettings();

    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getSystemSettingsFile());
    SystemSettings result;

    try (FileReader fileReader = new FileReader(settingsFile)) {
      result = new Gson().fromJson(fileReader, SystemSettings.class);
    }

    assertThat(result.getCacheSizeMb()).isEqualTo(123);
    assertThat(result.getProxyHost()).isEqualTo("localhost");
    assertThat(result.getProxyPort()).isEqualTo(8080);
    assertThat(result.getProxyRequiresAuthentication()).isTrue();
    assertThat(result.getProxyUsername()).isEqualTo("username");
    assertThat(result.getProxyPassword()).isEqualTo("password");
  }

  @Test
  @SneakyThrows
  void shouldNotSaveSystemSettingsOnException() {
    Gson gson = mock(Gson.class);
    doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveSystemSettingsOnException()")).when(gson)
        .toJson(any(SystemSettings.class));

    setField(underTest, "gson", gson);
    setField(underTest, "systemSettings", SystemSettings.builder().build());

    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getSystemSettingsFile());
    settingsFile.delete();

    underTest.saveSystemSettings();

    SystemSettings result = null;

    if (settingsFile.exists()) {
      try (FileReader fileReader = new FileReader(settingsFile)) {
        result = new Gson().fromJson(fileReader, SystemSettings.class);
      }
    }

    assertThat(result).isNull();
  }

  @Test
  @SneakyThrows
  void shouldLoadUserSettingsFromFile() {
    File testSettings = getTestResourceFile("json/settingsManager-shouldLoadUserSettingsFromFile.json");
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getUserSettingsFile());

    Files.copy(testSettings, settingsFile);

    Track mockTrack = mock(Track.class);
//    when(searchService.getTrackById("92f9b8ad82601ab97c121239518730108eefa18055ead908e5cdaf369023984b"))
//        .thenReturn(of(mockTrack));

    underTest.loadUserSettings();
    boolean settingsLoaded = getField(underTest, "userSettingsLoaded", Boolean.class);

    assertThat(settingsLoaded).isTrue();

    verify(playlistService).setShuffle(true, true);
    verify(playlistService).setRepeat(ALL);
    verify(mediaService, times(10)).setEqualizerGain(anyInt(), anyDouble());
    verify(searchService, times(15)).getTrackById(anyString());
    verify(underTest, never()).saveUserSettings();
  }

  @Test
  void shouldLoadUserSettingsWithNoExistingFile() {
    doNothing().when(underTest).saveUserSettings();

    underTest.loadUserSettings();
    boolean settingsLoaded = getField(underTest, "userSettingsLoaded", Boolean.class);

    assertThat(settingsLoaded).isTrue();

    verify(underTest).saveUserSettings();
  }

  @Test
  @SneakyThrows
  void shouldLoadUserSettingsWithNoEqFromFile() {
    File testSettings = getTestResourceFile("json/settingsManager-shouldLoadUserSettingsWithNoEqFromFile.json");
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getUserSettingsFile());

    Files.copy(testSettings, settingsFile);

    underTest.loadUserSettings();
    boolean settingsLoaded = getField(underTest, "userSettingsLoaded", Boolean.class);

    assertThat(settingsLoaded).isTrue();

    verify(playlistService).setShuffle(true, true);
    verify(playlistService).setRepeat(ALL);
    verify(mediaService, never()).setEqualizerGain(anyInt(), anyDouble());
    verify(searchService, times(15)).getTrackById(anyString());
    verify(underTest, never()).saveUserSettings();
  }

  @Test
  @SneakyThrows
  void shouldLoadUserSettingsWithNoPlaylistsFromFile() {
    File testSettings = getTestResourceFile(
        "json/settingsManager-shouldLoadUserSettingsWithNoPlaylistsFromFile.json");
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getUserSettingsFile());

    Files.copy(testSettings, settingsFile);

    underTest.loadUserSettings();
    boolean settingsLoaded = getField(underTest, "userSettingsLoaded", Boolean.class);

    assertThat(settingsLoaded).isTrue();

    verify(playlistService).setShuffle(true, true);
    verify(playlistService).setRepeat(ALL);
    verify(mediaService, times(10)).setEqualizerGain(anyInt(), anyDouble());
    verify(searchService, never()).getTrackById(anyString());
    verify(underTest, never()).saveUserSettings();
  }

  @Test
  @SneakyThrows
  void shouldNotLoadUserSettingsFromAnInvalidFile() {
    File testSettings = getTestResourceFile("json/settingsManager-shouldNotLoadUserSettingsFromAnInvalidFile.json");
    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getUserSettingsFile());

    Files.copy(testSettings, settingsFile);

    underTest.loadUserSettings();
    boolean settingsLoaded = getField(underTest, "userSettingsLoaded", Boolean.class);

    assertThat(settingsLoaded).isFalse();

    verify(underTest, never()).saveUserSettings();
  }

  @Test
  @SneakyThrows
  void shouldSaveUserSettings() {
    setField(underTest, "userSettingsLoaded", true);

    when(playlistService.isShuffle()).thenReturn(true);
    when(playlistService.getRepeat()).thenReturn(ALL);

    Equalizer equalizer = mock(Equalizer.class);
    when(equalizer.getNumberOfBands()).thenReturn(5);
    when(mediaService.getEqualizer()).thenReturn(equalizer);

    Playlist searchPlaylist = mock(Playlist.class);
    when(searchPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

    Playlist favouritesPlaylist = mock(Playlist.class);
    when(favouritesPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

    List<Playlist> playlists = List.of(searchPlaylist, favouritesPlaylist);
    when(playlistService.getPlaylists()).thenReturn(playlists);

    underTest.saveUserSettings();

    verify(playlistService).isShuffle();
    verify(playlistService).getRepeat();
    verify(mediaService).getEqualizer();
    verify(equalizer, times(5)).getGain(anyInt());
    verify(playlistService).getPlaylists();

    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getUserSettingsFile());

    Settings settings;

    try (FileReader fileReader = new FileReader(settingsFile)) {
      settings = new Gson().fromJson(fileReader, Settings.class);
    }

    assertThat(settings.getPlaylists()).hasSize(1);
    assertThat(settings.getPlaylists().get(0).getId()).isEqualTo(PLAYLIST_ID_FAVOURITES);
  }

  @Test
  void shouldNotSaveUserSettingsIfNotUserSettingsLoaded() {
    setField(underTest, "userSettingsLoaded", false);

    underTest.saveUserSettings();

    verify(playlistService, never()).isShuffle();
  }

  @Test
  void shouldNotSaveUserSettingsOnException() {
    setField(underTest, "userSettingsLoaded", true);

    when(playlistService.isShuffle()).thenReturn(true);
    when(playlistService.getRepeat()).thenReturn(ALL);

    Equalizer equalizer = mock(Equalizer.class);
    when(equalizer.getNumberOfBands()).thenReturn(5);
    when(mediaService.getEqualizer()).thenReturn(equalizer);

    Playlist searchPlaylist = mock(Playlist.class);
    when(searchPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

    Playlist favouritesPlaylist = mock(Playlist.class);
    when(favouritesPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

    List<Playlist> playlists = List.of(searchPlaylist, favouritesPlaylist);
    when(playlistService.getPlaylists()).thenReturn(playlists);

    Gson gson = mock(Gson.class);
    doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveUserSettingsOnException()")).when(gson)
        .toJson(any(Settings.class));

    setField(underTest, "gson", gson);

    underTest.saveUserSettings();

    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getUserSettingsFile());

    assertThat(settingsFile.exists()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldNotSaveUserSettingsOnExceptionWhenFileAlreadyExists() {
    File newSettingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getUserSettingsFile());
    newSettingsFile.createNewFile();

    setField(underTest, "userSettingsLoaded", true);

    when(playlistService.isShuffle()).thenReturn(true);
    when(playlistService.getRepeat()).thenReturn(ALL);

    Equalizer equalizer = mock(Equalizer.class);
    when(equalizer.getNumberOfBands()).thenReturn(5);
    when(mediaService.getEqualizer()).thenReturn(equalizer);

    Playlist searchPlaylist = mock(Playlist.class);
    when(searchPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_SEARCH);

    Playlist favouritesPlaylist = mock(Playlist.class);
    when(favouritesPlaylist.getPlaylistId()).thenReturn(PLAYLIST_ID_FAVOURITES);

    List<Playlist> playlists = List.of(searchPlaylist, favouritesPlaylist);
    when(playlistService.getPlaylists()).thenReturn(playlists);

    Gson gson = mock(Gson.class);
    doThrow(new RuntimeException("SettingsManagerTest.shouldNotSaveSettingsOnException()")).when(gson)
        .toJson(any(Settings.class));

    setField(underTest, "gson", gson);

    underTest.saveUserSettings();

    File settingsFile = underTest.getFileFromConfigDirectory(applicationProperties.getUserSettingsFile());

    assertThat(settingsFile.exists()).isTrue();
  }
}