package uk.co.mpcontracting.rpmjukebox;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.LogManager;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.manager.MediaManager;
import uk.co.mpcontracting.rpmjukebox.manager.SearchManager;
import uk.co.mpcontracting.rpmjukebox.manager.SettingsManager;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;

@Slf4j
@Component
public class RpmJukebox extends Application implements Constants {

	@Autowired
	private SettingsManager settingsManager;
	
	@Autowired
	private SearchManager searchManager;
	
	@Autowired
	private MediaManager mediaManager;

	@Getter private Stage stage;
	private boolean isInitialised;
	
	public RpmJukebox() {
		// Make sure logging directory exists
		File logDirectory = new File(System.getProperty("user.home") + File.separator + ".rpmjukebox" + File.separator + "log");
		
		if (!logDirectory.exists()) {
			logDirectory.mkdirs();
		}
		
		// Initialise the logging
		try (InputStream inputStream = getClass().getResourceAsStream("/logging.properties")) {
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		// Initialise the IoC layer
		FxmlContext.initialize(Arrays.asList("uk.co.mpcontracting.rpmjukebox"), this);
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		log.info("Starting application");

		this.stage = stage;

		Parent parent = (Parent)FxmlContext.loadFxml("mainpanel.fxml");

		stage.setScene(new Scene(parent));
		stage.setTitle("RPM Jukebox");

		// If this is Windows, add a window icon
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			stage.getIcons().add(new Image(getClass().getResourceAsStream(IMAGE_WINDOW_ICON)));
		}
		
		stage.show();

		parent.requestFocus();
		
		// Initialise data in a new thread for GUI responsiveness
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					searchManager.initialise();
					settingsManager.loadSettings();
					
					FxmlContext.getBean(MainPanelController.class).closeMessageWindow();
	
					EventManager.getInstance().fireEvent(Event.APPLICATION_INITIALISED);
					isInitialised = true;
				} catch (Exception e) {
					log.error("Error initialising data", e);
				}
			}
		}).start();
	}
	
	@Override
	public void stop() throws Exception {
		log.info("Stopping application");

		mediaManager.cleanUpResources();

		if (isInitialised) {
			settingsManager.saveSettings();
		}

		super.stop();
	}
	
	public static void main(String [] args) {
		try {
			launch(args);
		} catch (Exception e) {
			log.error("Error starting RPM Jukebox", e);
		}
	}
}
