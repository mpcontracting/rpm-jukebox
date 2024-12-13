package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_PLAYLIST_FAVOURITES;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_PLAYLIST_SEARCH;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_CHECKING_DATA;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_LOADING_SYSTEM_SETTINGS;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_FAVOURITES;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.PLAYLIST_ID_SEARCH;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.igormaznitsa.commons.version.Version;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.settings.EqBand;
import uk.co.mpcontracting.rpmjukebox.settings.PlaylistSettings;
import uk.co.mpcontracting.rpmjukebox.settings.Settings;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.settings.Window;
import uk.co.mpcontracting.rpmjukebox.util.OsType;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettingsService {

  private final RpmJukebox rpmJukebox;
  private final ApplicationProperties applicationProperties;
  private final StringResourceService stringResourceService;

  @Lazy
  @Autowired
  private InternetService internetService;

  @Lazy
  @Autowired
  private MediaService mediaService;

  @Lazy
  @Autowired
  private PlaylistService playlistService;

  @Lazy
  @Autowired
  private SearchService searchService;

  @Getter
  private OsType osType;

  @Getter
  private Version version;

  @Getter
  private URL dataFile;

  @Getter
  private SystemSettings systemSettings;

  @Getter
  private Gson gson;

  private boolean userSettingsLoaded;

  @SneakyThrows
  @PostConstruct
  public void initialise() {
    log.info("Initialising SettingsService");

    // Determine the OS type
    String osName = System.getProperty("os.name").toLowerCase();

    if (osName.contains("windows")) {
      osType = OsType.WINDOWS;
    } else if (osName.contains("mac")) {
      osType = OsType.OSX;
    } else if (osName.contains("linux")) {
      osType = OsType.LINUX;
    } else {
      osType = OsType.UNKNOWN;
    }

    // Initialise Gson
    gson = new GsonBuilder().setPrettyPrinting().create();

    // Get the application version
    version = new Version(applicationProperties.getVersion());

    // Get the data file location
    dataFile = URI.create(applicationProperties.getDataFileUrl()).toURL();

    // Load the system settings
    loadSystemSettings();

    userSettingsLoaded = false;
  }

  File getFileFromConfigDirectory(String relativePath) {
    return new File(RpmJukebox.getConfigDirectory(), relativePath);
  }

  boolean isNewVersion() {
    boolean isNewVersion = version.compareTo(new Version(systemSettings.getVersion())) > 0;

    log.debug("Is new version - {}", isNewVersion);

    return isNewVersion;
  }

  boolean hasDataFileExpired() {
    rpmJukebox.updateSplashProgress(stringResourceService.getString(MESSAGE_SPLASH_CHECKING_DATA));

    // Read the last modified date from the data file
    LocalDateTime lastModified = null;

    if ("file".equals(dataFile.getProtocol())) {
      try {
        lastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(new File(dataFile.toURI()).lastModified()), ZoneId.systemDefault());
      } catch (Exception e) {
        log.error("Unable to determine if local data file has expired", e);
      }
    } else {
      HttpURLConnection connection = null;

      try {
        connection = (HttpURLConnection) internetService.openConnection(dataFile);
        lastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(connection.getLastModified()), ZoneId.systemDefault());
      } catch (Exception e) {
        log.error("Unable to determine if data file has expired", e);
      } finally {
        if (nonNull(connection)) {
          connection.disconnect();
        }
      }
    }

    if (isNull(lastModified)) {
      return false;
    }

    LocalDateTime lastIndexed = getLastIndexedDate();

    log.debug("Last modified - {}", lastModified);
    log.debug("Last indexed - {}", lastIndexed);

    // If last modified is at least 1 hour old and greater than last
    // indexed, it's invalid
    return lastModified.plusHours(1).isBefore(LocalDateTime.now()) && lastModified.isAfter(lastIndexed);
  }

  LocalDateTime getLastIndexedDate() {
    LocalDateTime lastIndexed = null;
    File lastIndexedFile = getFileFromConfigDirectory(applicationProperties.getLastIndexedFile());

    if (lastIndexedFile.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(lastIndexedFile))) {
        lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())), ZoneId.systemDefault());
      } catch (Exception e) {
        log.error("Unable to read last indexed file", e);
      }
    } else {
      // Set last indexed to now
      lastIndexed = LocalDateTime.now();
      setLastIndexedDate(lastIndexed);
    }

    return lastIndexed;
  }

  void setLastIndexedDate(LocalDateTime localDateTime) {
    File lastIndexedFile = getFileFromConfigDirectory(applicationProperties.getLastIndexedFile());
    boolean alreadyExists = lastIndexedFile.exists();

    if (alreadyExists) {
      if (!lastIndexedFile.renameTo(getFileFromConfigDirectory(applicationProperties.getLastIndexedFile() + ".bak"))) {
        log.warn("Unable to rename file to .bak - {}", lastIndexedFile.getAbsolutePath());
      }
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
      writer.write(Long.toString(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
      writer.newLine();
    } catch (Exception e) {
      log.error("Unable to write last indexed file", e);

      if (!lastIndexedFile.delete()) {
        log.warn("Unable to delete last indexed file - {}", lastIndexedFile.getAbsolutePath());
      }

      if (alreadyExists) {
        if (!getFileFromConfigDirectory(applicationProperties.getLastIndexedFile() + ".bak").renameTo(lastIndexedFile)) {
          log.warn("Unable to rename file from .bak - {}", applicationProperties.getLastIndexedFile() + ".bak");
        }
      }
    } finally {
      if (!getFileFromConfigDirectory(applicationProperties.getLastIndexedFile() + ".bak").delete()) {
        log.warn("Unable to rename file in finally - {}", applicationProperties.getLastIndexedFile() + ".bak");
      }
    }
  }

  void loadWindowSettings(Stage stage) {
    log.debug("Loading window settings");

    File settingsFile = getFileFromConfigDirectory(applicationProperties.getWindowSettingsFile());
    Window window;

    if (settingsFile.exists()) {
      // Read the file
      try (FileReader fileReader = new FileReader(settingsFile)) {
        window = gson.fromJson(fileReader, Window.class);
      } catch (Exception e) {
        log.error("Unable to load window settings file", e);

        return;
      }
    } else {
      // By default, set width and height to 75% of screen size
      Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
      double width = (bounds.getWidth() / 100d) * 75d;
      double height = (bounds.getHeight() / 100d) * 75d;

      window = Window.builder()
          .x((bounds.getWidth() - width) / 2d)
          .y((bounds.getHeight() - height) / 2d)
          .width(width)
          .height(height)
          .build();
    }

    stage.setX(window.getX());
    stage.setY(window.getY());
    stage.setWidth(window.getWidth());
    stage.setHeight(window.getHeight());
  }

  void saveWindowSettings(Stage stage) {
    log.debug("Saving window settings");

    Window window = Window.builder()
        .x(stage.getX())
        .y(stage.getY())
        .width(stage.getWidth())
        .height(stage.getHeight())
        .build();

    // Write the file
    File settingsFile = getFileFromConfigDirectory(applicationProperties.getWindowSettingsFile());

    try (FileWriter fileWriter = new FileWriter(settingsFile)) {
      fileWriter.write(gson.toJson(window));
    } catch (Exception e) {
      log.error("Unable to save window settings file", e);
    }
  }

  void loadSystemSettings() {
    log.debug("Loading system settings");

    rpmJukebox.updateSplashProgress(stringResourceService.getString(MESSAGE_SPLASH_LOADING_SYSTEM_SETTINGS));

    File systemSettingsFile = getFileFromConfigDirectory(applicationProperties.getSystemSettingsFile());

    if (!systemSettingsFile.exists()) {
      systemSettings = SystemSettings.builder()
          .cacheSizeMb(applicationProperties.getCacheSizeMb())
          .build();

      saveSystemSettings();

      return;
    }

    // Read the file
    try (FileReader fileReader = new FileReader(systemSettingsFile)) {
      systemSettings = gson.fromJson(fileReader, SystemSettings.class);
    } catch (Exception e) {
      log.error("Unable to load system settings file", e);

      systemSettings = SystemSettings.builder()
          .cacheSizeMb(applicationProperties.getCacheSizeMb())
          .build();

      saveSystemSettings();
    }
  }

  public void saveSystemSettings() {
    log.debug("Saving system settings");

    // Update the version
    systemSettings.setVersion(applicationProperties.getVersion());

    // Write the file
    File systemSettingsFile = getFileFromConfigDirectory(applicationProperties.getSystemSettingsFile());

    try (FileWriter fileWriter = new FileWriter(systemSettingsFile)) {
      fileWriter.write(gson.toJson(systemSettings));
    } catch (Exception e) {
      log.error("Unable to save system settings file", e);
    }
  }

  void loadUserSettings() {
    log.debug("Loading user settings");

    File userSettingsFile = getFileFromConfigDirectory(applicationProperties.getUserSettingsFile());

    if (!userSettingsFile.exists()) {
      userSettingsLoaded = true;
      saveUserSettings();
      return;
    }

    // Read the file
    Settings settings;

    try (FileReader fileReader = new FileReader(userSettingsFile)) {
      settings = gson.fromJson(fileReader, Settings.class);
    } catch (Exception e) {
      log.error("Unable to load user settings file", e);

      return;
    }

    // General settings
    playlistService.setShuffle(settings.isShuffle(), true);
    playlistService.setRepeat(settings.getRepeat());

    // Equalizer
    ofNullable(settings.getEqBands()).ifPresent(
        eqBands -> eqBands.forEach(eqBand -> mediaService.setEqualizerGain(eqBand.getBand(), eqBand.getValue())));

    // Playlists
    List<Playlist> playlists = new ArrayList<>();

    ofNullable(settings.getPlaylists())
        .ifPresent(playlistSettingsList -> playlistSettingsList.forEach(playlistSettings -> {
          Playlist playlist = new Playlist(playlistSettings.getId(), playlistSettings.getName(),
              applicationProperties.getMaxPlaylistSize());

          // Override the name of the search results and favourites
          // playlists
          if (playlist.getPlaylistId() == PLAYLIST_ID_SEARCH) {
            playlist.setName(stringResourceService.getString(MESSAGE_PLAYLIST_SEARCH));
          } else if (playlist.getPlaylistId() == PLAYLIST_ID_FAVOURITES) {
            playlist.setName(stringResourceService.getString(MESSAGE_PLAYLIST_FAVOURITES));
          }

          playlistSettings.getTracks().forEach(trackId -> searchService.getTrackById(trackId).ifPresent(playlist::addTrack));

          playlists.add(playlist);
        }));

    playlistService.setPlaylists(playlists);

    userSettingsLoaded = true;
  }

  void saveUserSettings() {
    log.debug("Saving user settings");

    // Don't save settings if they weren't loaded successfully
    // so we stop file corruption
    if (!userSettingsLoaded) {
      return;
    }

    // Equalizer
    Equalizer equalizer = mediaService.getEqualizer();
    List<EqBand> eqBands = new ArrayList<>();

    for (int i = 0; i < equalizer.getNumberOfBands(); i++) {
      eqBands.add(EqBand.builder()
          .band(i)
          .value(equalizer.getGain(i))
          .build());
    }

    // Playlists
    List<PlaylistSettings> playlists = new ArrayList<>();

    playlistService.getPlaylists().stream().filter(playlist -> playlist.getPlaylistId() != PLAYLIST_ID_SEARCH)
        .forEach(playlist -> playlists.add(new PlaylistSettings(playlist)));

    Settings settings = Settings.builder()
        .shuffle(playlistService.isShuffle())
        .repeat(playlistService.getRepeat())
        .eqBands(eqBands)
        .playlists(playlists)
        .build();

    // Write the file
    File userSettingsFile = getFileFromConfigDirectory(applicationProperties.getUserSettingsFile());
    boolean alreadyExists = userSettingsFile.exists();

    if (alreadyExists) {
      if (!userSettingsFile.renameTo(getFileFromConfigDirectory(applicationProperties.getUserSettingsFile() + ".bak"))) {
        log.warn("Unable to rename user settings file - {}", userSettingsFile);
      }
    }

    try (FileWriter fileWriter = new FileWriter(userSettingsFile)) {
      fileWriter.write(gson.toJson(settings));
    } catch (Exception e) {
      log.error("Unable to save user settings file", e);

      if (!userSettingsFile.delete()) {
        log.warn("Unable to delete user settings file - {}", userSettingsFile);
      }

      if (alreadyExists) {
        if (!getFileFromConfigDirectory(applicationProperties.getUserSettingsFile() + ".bak").renameTo(userSettingsFile)) {
          log.warn("Unable to rename backed up user settings file");
        }
      }
    } finally {
      if (!getFileFromConfigDirectory(applicationProperties.getUserSettingsFile() + ".bak").delete()) {
        log.warn("Unable to delete backed up user settings file");
      }
    }
  }
}
