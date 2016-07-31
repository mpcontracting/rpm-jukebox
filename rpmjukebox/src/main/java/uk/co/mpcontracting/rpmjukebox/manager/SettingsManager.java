package uk.co.mpcontracting.rpmjukebox.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
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
	
	@Getter private File configDirectory;
	@Getter private URL dataFile;
	
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
		dataFile = getClass().getResource(properties.getProperty(PROP_DATAFILE_URL));
	}
	
	public void saveSettings() {
		log.info("Saving settings");

		// Build the setting object before serializing it to disk
		DocumentFactory factory = DocumentFactory.getInstance();
		Document root = factory.createDocument("UTF-8");
		Element rpmJukeboxElement = factory.createElement("rpm-jukebox");

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
			Element playlistElement = factory.createElement("playlist");
			playlistElement.add(factory.createAttribute(playlistElement, "id", Integer.toString(playlist.getPlaylistId())));
			playlistElement.add(factory.createAttribute(playlistElement, "name", playlist.getName()));

			for (Integer playlistIndex : playlist.getTrackMap().keySet()) {
				Track track = playlist.getTrackMap().get(playlistIndex);
				Element trackElement = factory.createElement("track");
				trackElement.add(factory.createAttribute(trackElement, "index", playlistIndex.toString()));
				trackElement.add(factory.createAttribute(trackElement, "id", Integer.toString(track.getTrackId())));

				playlistElement.add(trackElement);
			}

			playlistsElement.add(playlistElement);
		}

		rpmJukeboxElement.add(playlistsElement);

		root.add(rpmJukeboxElement);

		// Write the file
		File settingsFile = new File(configDirectory, SETTINGS_FILE);
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

		File settingsFile = new File(configDirectory, SETTINGS_FILE);

		if (!settingsFile.exists()) {
			saveSettings();
			return;
		}

		DocumentFactory factory = DocumentFactory.getInstance();
		SAXReader reader = new SAXReader(factory);

		try {
			Document root = reader.read(settingsFile);
			Node rpmJukeboxNode = root.selectSingleNode("rpm-jukebox");

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
					Track track = searchManager.getTrackById(Integer.parseInt(trackElement.attributeValue("id")));

					if (track != null) {
						playlist.putTrack(Integer.parseInt(trackElement.attributeValue("index")), track);
					}
				}

				playlists.add(playlist);
			}

			playlistManager.setPlaylists(playlists);
		} catch (Exception e) {
			log.error("Unable to load settings file", e);
		}
	}
}
