package uk.co.mpcontracting.rpmjukebox.manager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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

import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.ioc.annotation.Resource;
import uk.co.mpcontracting.ioc.factory.InitializingBean;
import uk.co.mpcontracting.rpmjukebox.model.Equalizer;
import uk.co.mpcontracting.rpmjukebox.model.Playlist;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
@Component
public class SettingsManager implements InitializingBean, Constants {

	@Autowired
	private SearchManager searchManager;

	@Autowired
	private PlaylistManager playlistManager;
	
	@Autowired
	private MediaManager mediaManager;

	@Resource(location = "classpath:/rpm-jukebox.properties")
	private Properties properties;
	
	private File configDirectory;
	@Getter private URL dataFile;
	@Getter private boolean dataFileExpired;
	
	private boolean settingsLoaded;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising SettingsManager");
		
		// Look for the config directory and create it if it isn't there
		File homeDir = new File(System.getProperty("user.home"));
		configDirectory = new File(homeDir, ".rpmjukebox");

		if (!configDirectory.exists()) {
			if (!configDirectory.mkdir()) {
				throw new RuntimeException("Unable to create config directory - " + configDirectory.getAbsolutePath());
			}
		}

		// Get the data file location
		dataFile = new URL(properties.getProperty(PROP_DATAFILE_URL));
		
		// Determine whether the data file has expired
		dataFileExpired = hasDataFileExpired();
		
		settingsLoaded = false;
	}
	
	private boolean hasDataFileExpired() {
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
		
		log.info("Last modified - " + lastModified);
		log.info("Last indexed - " + lastIndexed);
		
		// If last modified is at least 1 hour old and greater than last indexed, it's invalid
		return lastModified.minusHours(1).isAfter(LocalDateTime.now()) && lastModified.isAfter(lastIndexed);
	}

	public File getFileFromConfigDirectory(String relativePath) {
		return new File(configDirectory, relativePath);
	}
	
	public LocalDateTime getLastIndexedDate() {
		LocalDateTime lastIndexed = null;
		File lastIndexedFile = getFileFromConfigDirectory(LAST_INDEXED_FILE);
		
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
		File lastIndexedFile = getFileFromConfigDirectory(LAST_INDEXED_FILE);
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(lastIndexedFile))) {
			writer.write(Long.toString(localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()));
			writer.newLine();
		} catch (Exception e) {
			log.error("Unable to write last indexed file", e);
		}
	}
	
	public void saveSettings() {
		log.info("Saving settings");

		// Don't save settings if they weren't loaded successfully
		// so we stop file corruption
		if (!settingsLoaded) {
			return;
		}
		
		// Build the setting object before serializing it to disk
		DocumentFactory factory = DocumentFactory.getInstance();
		Document root = factory.createDocument("UTF-8");
		Element rpmJukeboxElement = factory.createElement("rpm-jukebox");
		
		// Settings
		Element settingsElement = factory.createElement("settings");
		
		Element shuffleElement = factory.createElement("shuffle");
		shuffleElement.add(factory.createAttribute(shuffleElement, "enabled", Boolean.toString(playlistManager.isShuffle())));
		settingsElement.add(shuffleElement);
		
		Element repeatElement = factory.createElement("repeat");
		repeatElement.add(factory.createAttribute(repeatElement, "enabled", Boolean.toString(playlistManager.isRepeat())));
		settingsElement.add(repeatElement);
		
		rpmJukeboxElement.add(settingsElement);

		// Equalizer
		Equalizer equalizer = mediaManager.getEqualizer();
		Element equalizerElement = factory.createElement("equalizer");

		for (int i = 0; i < equalizer.getNumberOfBands(); i++) {
			Element bandElement = factory.createElement("band");
			bandElement.add(factory.createAttribute(bandElement, "id", Integer.toString(i)));
			bandElement.add(factory.createAttribute(bandElement, "value", Double.toString(equalizer.getGain(i))));

			equalizerElement.add(bandElement);
		}

		rpmJukeboxElement.add(equalizerElement);

		// Playlists
		Element playlistsElement = factory.createElement("playlists");

		for (Playlist playlist : playlistManager.getPlaylists()) {
			if (playlist.getPlaylistId() == SEARCH_PLAYLIST_ID) {
				continue;
			}
			
			Element playlistElement = factory.createElement("playlist");
			playlistElement.add(factory.createAttribute(playlistElement, "id", Integer.toString(playlist.getPlaylistId())));
			playlistElement.add(factory.createAttribute(playlistElement, "name", playlist.getName()));

			for (Track track : playlist.getTracks()) {
				Element trackElement = factory.createElement("track");
				trackElement.add(factory.createAttribute(trackElement, "id", track.getTrackId()));

				playlistElement.add(trackElement);
			}

			playlistsElement.add(playlistElement);
		}

		rpmJukeboxElement.add(playlistsElement);

		root.add(rpmJukeboxElement);

		// Write the file
		File settingsFile = getFileFromConfigDirectory(SETTINGS_FILE);
		XMLWriter writer = null;

		try {
			writer = new XMLWriter(new FileOutputStream(settingsFile), OutputFormat.createPrettyPrint());
			writer.write(root);
			writer.flush();
		} catch (Exception e) {
			log.error("Unable to save settings file", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e) {
					log.error("Unable to close output stream", e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void loadSettings() {
		log.info("Loading settings");

		File settingsFile = getFileFromConfigDirectory(SETTINGS_FILE);

		if (!settingsFile.exists()) {
			settingsLoaded = true;
			saveSettings();
			return;
		}

		DocumentFactory factory = DocumentFactory.getInstance();
		SAXReader reader = new SAXReader(factory);

		try {
			Document root = reader.read(settingsFile);
			Node rpmJukeboxNode = root.selectSingleNode("rpm-jukebox");

			// Settings
			Node settingsNode = rpmJukeboxNode.selectSingleNode("settings");
			
			Element shuffleElement = (Element)settingsNode.selectSingleNode("shuffle");
			playlistManager.setSuffle(Boolean.parseBoolean(shuffleElement.attributeValue("enabled")));
			
			Element repeatElement = (Element)settingsNode.selectSingleNode("repeat");
			playlistManager.setRepeat(Boolean.parseBoolean(repeatElement.attributeValue("enabled")));
			
			// Equalizer
			Node equalizerNode = rpmJukeboxNode.selectSingleNode("equalizer");

			for (Element bandElement : (List<Element>)equalizerNode.selectNodes("band")) {
				mediaManager.setEqualizerGain(Integer.parseInt(bandElement.attributeValue("id")), Double.parseDouble(bandElement.attributeValue("value")));
			}

			// Playlists
			List<Playlist> playlists = new ArrayList<Playlist>();

			Node playlistsNode = rpmJukeboxNode.selectSingleNode("playlists");

			for (Element playlistElement : (List<Element>)playlistsNode.selectNodes("playlist")) {
				Playlist playlist = new Playlist(Integer.parseInt(playlistElement.attributeValue("id")),
					playlistElement.attributeValue("name"));

				for (Element trackElement : (List<Element>)playlistElement.selectNodes("track")) {
					Track track = searchManager.getTrackById(trackElement.attributeValue("id"));

					if (track != null) {
						playlist.addTrack(track);
					}
				}

				playlists.add(playlist);
			}

			playlistManager.setPlaylists(playlists);
			
			settingsLoaded = true;
		} catch (Exception e) {
			log.error("Unable to load settings file", e);
		}
	}
}
