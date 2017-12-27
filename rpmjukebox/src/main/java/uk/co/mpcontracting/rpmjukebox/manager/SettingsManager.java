package uk.co.mpcontracting.rpmjukebox.manager;

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
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.igormaznitsa.commons.version.Version;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.settings.EqBand;
import uk.co.mpcontracting.rpmjukebox.settings.PlaylistSettings;
import uk.co.mpcontracting.rpmjukebox.settings.Settings;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.settings.Window;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.OsType;

@Slf4j
@Component
public class SettingsManager implements InitializingBean, Constants {

    @Autowired
    private MessageManager messageManager;

    @Autowired
    private SearchManager searchManager;

    @Autowired
    private PlaylistManager playlistManager;

    @Autowired
    private MediaManager mediaManager;

    @Autowired
    private InternetManager internetManager;

    @Autowired
    private MainPanelController mainPanelController;

    @Value("${version}")
    private String versionString;

    @Value("${datafile.url}")
    private String dataFileUrl;

    @Value("${file.last.indexed}")
    private String fileLastIndexed;

    @Value("${file.window.settings}")
    private String fileWindowSettings;

    @Value("${file.system.settings}")
    private String fileSystemSettings;

    @Value("${file.user.settings}")
    private String fileUserSettings;

    @Value("${max.playlist.size}")
    private int maxPlaylistSize;

    @Value("${cache.size.mb}")
    private int cacheSizeMb;

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

    public SettingsManager() {
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
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Initialising SettingsManager");

        // Get the application version
        version = new Version(versionString);

        // Get the data file location
        dataFile = new URL(dataFileUrl);

        // Load the system settings
        loadSystemSettings();

        userSettingsLoaded = false;
    }

    public File getFileFromConfigDirectory(String relativePath) {
        return new File(RpmJukebox.getConfigDirectory(), relativePath);
    }

    public boolean hasDataFileExpired() {
        mainPanelController.showMessageView(messageManager.getMessage(MESSAGE_CHECKING_DATA), true);

        // Wait at least 1.5 seconds so message window lasts
        // long enough to read
        try {
            Thread.sleep(1500);
        } catch (Exception e) {
            // Do nothing
        }

        // Read the last modified date from the data file
        LocalDateTime lastModified = null;

        if ("file".equals(dataFile.getProtocol())) {
            try {
                lastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(new File(dataFile.toURI()).lastModified()),
                    ZoneId.systemDefault());
            } catch (Exception e) {
                log.error("Unable to determine if local data file has expired", e);
            }
        } else {
            HttpURLConnection connection = null;

            try {
                connection = internetManager.openConnection(dataFile);
                lastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(connection.getLastModified()),
                    ZoneId.systemDefault());
            } catch (Exception e) {
                log.error("Unable to determine if data file has expired", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        if (lastModified == null) {
            return false;
        }

        LocalDateTime lastIndexed = getLastIndexedDate();

        log.debug("Last modified - " + lastModified);
        log.debug("Last indexed - " + lastIndexed);

        // If last modified is at least 1 hour old and greater than last
        // indexed, it's invalid
        return lastModified.plusHours(1).isBefore(LocalDateTime.now()) && lastModified.isAfter(lastIndexed);
    }

    public LocalDateTime getLastIndexedDate() {
        LocalDateTime lastIndexed = null;
        File lastIndexedFile = getFileFromConfigDirectory(fileLastIndexed);

        if (lastIndexedFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(lastIndexedFile))) {
                lastIndexed = LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(reader.readLine())),
                    ZoneId.systemDefault());
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

    public void setLastIndexedDate(LocalDateTime localDateTime) {
        File lastIndexedFile = getFileFromConfigDirectory(fileLastIndexed);
        boolean alreadyExists = lastIndexedFile.exists();

        if (alreadyExists) {
            lastIndexedFile.renameTo(getFileFromConfigDirectory(fileLastIndexed + ".bak"));
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
            writer.write(Long.toString(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
            writer.newLine();
        } catch (Exception e) {
            log.error("Unable to write last indexed file", e);

            lastIndexedFile.delete();

            if (alreadyExists) {
                getFileFromConfigDirectory(fileLastIndexed + ".bak").renameTo(lastIndexedFile);
            }
        } finally {
            getFileFromConfigDirectory(fileLastIndexed + ".bak").delete();
        }
    }

    public void loadWindowSettings(Stage stage) {
        log.debug("Loading window settings");

        File settingsFile = getFileFromConfigDirectory(fileWindowSettings);
        Window window = null;

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

            window = new Window((bounds.getWidth() - width) / 2d, (bounds.getHeight() - height) / 2d, width, height);
        }

        stage.setX(window.getX());
        stage.setY(window.getY());
        stage.setWidth(window.getWidth());
        stage.setHeight(window.getHeight());
    }

    public void saveWindowSettings(Stage stage) {
        log.debug("Saving window settings");

        Window window = new Window(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());

        // Write the file
        File settingsFile = getFileFromConfigDirectory(fileWindowSettings);

        try (FileWriter fileWriter = new FileWriter(settingsFile)) {
            fileWriter.write(gson.toJson(window));
        } catch (Exception e) {
            log.error("Unable to save window settings file", e);
        }
    }

    public void loadSystemSettings() {
        log.debug("Loading system settings");

        File systemSettingsFile = getFileFromConfigDirectory(fileSystemSettings);

        if (!systemSettingsFile.exists()) {
            systemSettings = new SystemSettings();
            systemSettings.setCacheSizeMb(cacheSizeMb);

            saveSystemSettings();

            return;
        }

        // Read the file
        try (FileReader fileReader = new FileReader(systemSettingsFile)) {
            systemSettings = gson.fromJson(fileReader, SystemSettings.class);
        } catch (Exception e) {
            log.error("Unable to load system settings file", e);

            systemSettings = new SystemSettings();
            systemSettings.setCacheSizeMb(cacheSizeMb);

            saveSystemSettings();

            return;
        }
    }

    public void saveSystemSettings() {
        log.debug("Saving system settings");

        // Write the file
        File systemSettingsFile = getFileFromConfigDirectory(fileSystemSettings);

        try (FileWriter fileWriter = new FileWriter(systemSettingsFile)) {
            fileWriter.write(gson.toJson(systemSettings));
        } catch (Exception e) {
            log.error("Unable to save system settings file", e);
        }
    }

    public void loadUserSettings() {
        log.debug("Loading user settings");

        File userSettingsFile = getFileFromConfigDirectory(fileUserSettings);

        if (!userSettingsFile.exists()) {
            userSettingsLoaded = true;
            saveUserSettings();
            return;
        }

        // Read the file
        Settings settings = null;

        try (FileReader fileReader = new FileReader(userSettingsFile)) {
            settings = gson.fromJson(fileReader, Settings.class);
        } catch (Exception e) {
            log.error("Unable to load user settings file", e);

            return;
        }

        // General settings
        playlistManager.setShuffle(settings.isShuffle(), true);
        playlistManager.setRepeat(settings.getRepeat());

        // Equalizer
        if (settings.getEqBands() != null) {
            for (EqBand eqBand : settings.getEqBands()) {
                mediaManager.setEqualizerGain(eqBand.getBand(), eqBand.getValue());
            }
        }

        // Playlists
        List<Playlist> playlists = new ArrayList<Playlist>();

        if (settings.getPlaylists() != null) {
            for (PlaylistSettings playlistSettings : settings.getPlaylists()) {
                Playlist playlist = new Playlist(playlistSettings.getId(), playlistSettings.getName(), maxPlaylistSize);

                // Override the name of the search results and favourites
                // playlists
                if (playlist.getPlaylistId() == PLAYLIST_ID_SEARCH) {
                    playlist.setName(messageManager.getMessage(MESSAGE_PLAYLIST_SEARCH));
                } else if (playlist.getPlaylistId() == PLAYLIST_ID_FAVOURITES) {
                    playlist.setName(messageManager.getMessage(MESSAGE_PLAYLIST_FAVOURITES));
                }

                for (String trackId : playlistSettings.getTracks()) {
                    Track track = searchManager.getTrackById(trackId);

                    if (track != null) {
                        playlist.addTrack(track);
                    }
                }

                playlists.add(playlist);
            }
        }

        playlistManager.setPlaylists(playlists);

        userSettingsLoaded = true;
    }

    public void saveUserSettings() {
        log.debug("Saving user settings");

        // Don't save settings if they weren't loaded successfully
        // so we stop file corruption
        if (!userSettingsLoaded) {
            return;
        }

        // Build the setting object before serializing it to disk
        Settings settings = new Settings();

        // General settings
        settings.setShuffle(playlistManager.isShuffle());
        settings.setRepeat(playlistManager.getRepeat());

        // Equalizer
        Equalizer equalizer = mediaManager.getEqualizer();
        List<EqBand> eqBands = new ArrayList<EqBand>();

        for (int i = 0; i < equalizer.getNumberOfBands(); i++) {
            eqBands.add(new EqBand(i, equalizer.getGain(i)));
        }

        settings.setEqBands(eqBands);

        // Playlists
        List<PlaylistSettings> playlists = new ArrayList<PlaylistSettings>();

        for (Playlist playlist : playlistManager.getPlaylists()) {
            if (playlist.getPlaylistId() == PLAYLIST_ID_SEARCH) {
                continue;
            }

            playlists.add(new PlaylistSettings(playlist));
        }

        settings.setPlaylists(playlists);

        // Write the file
        File userSettingsFile = getFileFromConfigDirectory(fileUserSettings);
        boolean alreadyExists = userSettingsFile.exists();

        if (alreadyExists) {
            userSettingsFile.renameTo(getFileFromConfigDirectory(fileUserSettings + ".bak"));
        }

        try (FileWriter fileWriter = new FileWriter(userSettingsFile)) {
            fileWriter.write(gson.toJson(settings));
        } catch (Exception e) {
            log.error("Unable to save user settings file", e);

            userSettingsFile.delete();

            if (alreadyExists) {
                getFileFromConfigDirectory(fileUserSettings + ".bak").renameTo(userSettingsFile);
            }
        } finally {
            getFileFromConfigDirectory(fileLastIndexed + ".bak").delete();
        }
    }
}
