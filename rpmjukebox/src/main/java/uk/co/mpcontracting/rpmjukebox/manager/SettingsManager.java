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
import java.util.Properties;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.ioc.annotation.Resource;
import uk.co.mpcontracting.ioc.factory.InitializingBean;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.settings.EqBand;
import uk.co.mpcontracting.rpmjukebox.settings.PlaylistSettings;
import uk.co.mpcontracting.rpmjukebox.settings.Settings;
import uk.co.mpcontracting.rpmjukebox.settings.Window;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

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
	private MainPanelController mainPanelController;

	@Resource(location = "classpath:/rpm-jukebox.properties")
	private Properties properties;

	private File configDirectory;
	@Getter private URL dataFile;
	
	private Gson gson;
	private boolean settingsLoaded;
	
	public SettingsManager() {
		// Initialise Gson
		gson = new GsonBuilder().setPrettyPrinting().create();
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising SettingsManager");

		
		// Look for the config directory and create it if it isn't there
		File homeDir = new File(System.getProperty("user.home"));
		
		// See if we have a command line override
		if (System.getProperty(PROP_DIRECTORY_CONFIG) != null) {
			log.info("Using system property config directory - " + System.getProperty(PROP_DIRECTORY_CONFIG));
			
			configDirectory = new File(homeDir, System.getProperty(PROP_DIRECTORY_CONFIG));
		} else {
			configDirectory = new File(homeDir, getPropertyString(PROP_DIRECTORY_CONFIG));
		}

		if (!configDirectory.exists()) {
			if (!configDirectory.mkdirs()) {
				throw new RuntimeException("Unable to create config directory - " + configDirectory.getAbsolutePath());
			}
		}
		
		// Get the data file location
		dataFile = new URL(getPropertyString(PROP_DATAFILE_URL));

		settingsLoaded = false;
	}
	
	public String getPropertyString(String key) {
		return properties.getProperty(key);
	}
	
	public Integer getPropertyInteger(String key) {
		String value = getPropertyString(key);
		
		if (value != null) {
			return Integer.valueOf(value);
		}
		
		return null;
	}
	
	public Double getPropertyDouble(String key) {
		String value = getPropertyString(key);
		
		if (value != null) {
			return Double.valueOf(value);
		}
		
		return null;
	}

	public File getFileFromConfigDirectory(String relativePath) {
		return new File(configDirectory, relativePath);
	}
	
	public boolean hasDataFileExpired() {
		mainPanelController.showMessageWindow(messageManager.getMessage(MESSAGE_CHECKING_DATA));
		
		// Wait at least 1.5 seconds so message window lasts
		// long enough to read
		try {
			Thread.sleep(1500);
		} catch (Exception e) {};
		
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
				connection = (HttpURLConnection)dataFile.openConnection();
				lastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(connection.getLastModified()), ZoneId.systemDefault());
			} catch (Exception e) {
				log.error("Unable to determine if data file has expired", e);
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}

		LocalDateTime lastIndexed = getLastIndexedDate();
		
		log.debug("Last modified - " + lastModified);
		log.debug("Last indexed - " + lastIndexed);
		
		// If last modified is at least 1 hour old and greater than last indexed, it's invalid
		return lastModified.plusHours(1).isBefore(LocalDateTime.now()) && lastModified.isAfter(lastIndexed);
	}

	public LocalDateTime getLastIndexedDate() {
		LocalDateTime lastIndexed = null;
		File lastIndexedFile = getFileFromConfigDirectory(getPropertyString(PROP_FILE_LAST_INDEXED));
		
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
	
	public void setLastIndexedDate(LocalDateTime localDateTime) {
		File lastIndexedFile = getFileFromConfigDirectory(getPropertyString(PROP_FILE_LAST_INDEXED));
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
			writer.write(Long.toString(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
			writer.newLine();
		} catch (Exception e) {
			log.error("Unable to write last indexed file", e);
		}
	}
	
	public void loadWindowSettings(Stage stage) {
		log.debug("Loading window settings");
		
		File settingsFile = getFileFromConfigDirectory(getPropertyString(PROP_FILE_WINDOW_SETTINGS));
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
			
			window = new Window(
				(bounds.getWidth() - width) / 2d,
				(bounds.getHeight() - height) / 2d,
				width,
				height
			);
		}

		stage.setX(window.getX());
		stage.setY(window.getY());
		stage.setWidth(window.getWidth());
		stage.setHeight(window.getHeight());
	}
	
	public void saveWindowSettings(Stage stage) {
		log.debug("Saving window settings");

		Window window = new Window(
			stage.getX(),
			stage.getY(),
			stage.getWidth(),
			stage.getHeight()
		);

		// Write the file
		File settingsFile = getFileFromConfigDirectory(getPropertyString(PROP_FILE_WINDOW_SETTINGS));
		
		try (FileWriter fileWriter = new FileWriter(settingsFile)) {
			fileWriter.write(gson.toJson(window));
		} catch (Exception e) {
			log.error("Unable to save window settings file", e);
		}
	}
	
	public void loadSettings() {
		log.debug("Loading settings");

		File settingsFile = getFileFromConfigDirectory(getPropertyString(PROP_FILE_SETTINGS));
		
		if (!settingsFile.exists()) {
			settingsLoaded = true;
			saveSettings();
			return;
		}
		
		// Read the file
		Settings settings = null;
		
		try (FileReader fileReader = new FileReader(settingsFile)) {
			settings = gson.fromJson(fileReader, Settings.class);
		} catch (Exception e) {
			log.error("Unable to load settings file", e);
			
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
				Playlist playlist = new Playlist(playlistSettings.getId(), playlistSettings.getName(), 
					getPropertyInteger(PROP_MAX_PLAYLIST_SIZE));
				
				// Override the name of the search results and favourites playlists
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
		
		settingsLoaded = true;
	}
	
	public void saveSettings() {
		log.debug("Saving settings");
		
		// Don't save settings if they weren't loaded successfully
		// so we stop file corruption
		if (!settingsLoaded) {
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

			PlaylistSettings playlistSettings = new PlaylistSettings(playlist.getPlaylistId(), playlist.getName());
			List<String> tracks = new ArrayList<String>();
			
			for (Track track : playlist) {
				tracks.add(track.getTrackId());
			}
			
			playlistSettings.setTracks(tracks);
			playlists.add(playlistSettings);
		}
		
		settings.setPlaylists(playlists);

		// Write the file
		File settingsFile = getFileFromConfigDirectory(getPropertyString(PROP_FILE_SETTINGS));
		
		try (FileWriter fileWriter = new FileWriter(settingsFile)) {
			fileWriter.write(gson.toJson(settings));
		} catch (Exception e) {
			log.error("Unable to save settings file", e);
		}
	}
}
