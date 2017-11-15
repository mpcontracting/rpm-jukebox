package uk.co.mpcontracting.rpmjukebox;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.logging.LogManager;

import org.springframework.beans.factory.annotation.Autowired;

import javafx.application.Application;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.jetty.JettyServer;
import uk.co.mpcontracting.rpmjukebox.manager.MediaManager;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.support.Constants;

@Slf4j
//@Component
public class RpmJukeboxInitialiser extends Application implements Constants {

	@Autowired
	private SettingsManager settingsManager;
	
	@Autowired
	private SearchManager searchManager;
	
	@Autowired
	private MediaManager mediaManager;
	
	@Autowired
	private MainPanelController mainPanelController;
	
	@Autowired
	private JettyServer jettyServer;

	@Getter private Stage stage;
	private boolean isInitialised;
	
	public RpmJukeboxInitialiser() {
		initialiseLogging();
	
		// Initialise the IoC layer
		//FxmlContext.initialize(Arrays.asList("uk.co.mpcontracting.rpmjukebox"), this);
	}
	
	private void initialiseLogging() {
		try {
			File settingsDirectory = null;
			
			// See if we have a command line override
			if (System.getProperty("directory.config") != null) {
				settingsDirectory = new File(System.getProperty("user.home") + File.separator + System.getProperty("directory.config"));
			} else {
				settingsDirectory = new File(System.getProperty("user.home") + File.separator + ".rpmjukebox");
			}

			// Make sure logging directory exists
			File logDirectory = new File(settingsDirectory, "log");
			
			if (!logDirectory.exists()) {
				logDirectory.mkdirs();
			}
			
			// Copy the logging.properties file to ~/.rpmjukebox if it doesn't already exist
			File loggingFile = new File(settingsDirectory, "logging.properties");

			if (!loggingFile.exists()) {
				Files.copy(getClass().getResourceAsStream("/logging.properties"), loggingFile.toPath());
			}
			
			// Initialise the logging
			try (FileInputStream inputStream = new FileInputStream(loggingFile)) {
				LogManager.getLogManager().readConfiguration(inputStream);
			}
			
			// Add a spacer to the logging file to separate startups
			log.info("====================================================");
			log.info("====================================================");
			log.info("====================================================");
			log.info("Java Version - " + System.getProperty("java.version"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		log.info("Starting application");

		this.stage = stage;

		/*Parent parent = (Parent)FxmlContext.loadFxml("mainpanel.fxml");

		stage.setScene(new Scene(parent));
		stage.setTitle("RPM Jukebox");

		// If this is Windows, add a window icon
		if (settingsManager.getOsType() == OsType.WINDOWS) {
			stage.getIcons().add(new Image(getClass().getResourceAsStream(IMAGE_WINDOW_ICON)));
		}
		
		// Load the window settings
		settingsManager.loadWindowSettings(stage);

		stage.show();

		parent.requestFocus();
		
		// Initialise data in a new thread for GUI responsiveness
		ThreadRunner.run(() -> {
			try {
				searchManager.initialise();
				settingsManager.loadSettings();
				
				mainPanelController.closeMessageWindow();

				EventManager.getInstance().fireEvent(Event.APPLICATION_INITIALISED);
				isInitialised = true;
			} catch (Exception e) {
				log.error("Error initialising data", e);
			}
		});*/
	}
	
	@Override
	public void stop() throws Exception {
		log.info("Stopping application");

		mediaManager.cleanUpResources();

		if (isInitialised) {
			settingsManager.saveWindowSettings(stage);
			settingsManager.saveSettings();
		}

		jettyServer.stop();

		super.stop();
		
		System.exit(0);
	}
	
	public static void main(String [] args) {
		try {
			launch(args);
		} catch (Exception e) {
			log.error("Error starting RPM Jukebox", e);
		}
	}
}