package uk.co.mpcontracting.rpmjukebox;

import java.util.Arrays;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventManager;
import uk.co.mpcontracting.rpmjukebox.support.FxmlContext;

@Slf4j
public class RpmJukebox extends Application {

	private boolean isInitialised;
	
	public RpmJukebox() {
		FxmlContext.initialize(Arrays.asList("uk.co.mpcontracting.rpmjukebox"));
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		log.info("Starting application");

		stage.setScene(new Scene((Parent)FxmlContext.loadFxml("rpmjukebox.fxml")));
		stage.setTitle("RPM Jukebox");
		stage.show();
		
		// Initialise data in a new thread for GUI responsiveness
		new Thread(new Runnable() {
			@Override
			public void run() {
				//context.initialize(SearchManager.class);

				//((SettingsManager)context.getBean(SettingsManager.class)).loadSettings();

				EventManager.getInstance().fireEvent(Event.APPLICATION_INITIALISED);
				isInitialised = true;
			}
		}).start();
	}
	
	@Override
	public void stop() throws Exception {
		log.info("Stopping application");

		//((MediaManager)context.getBean(MediaManager.class)).cleanUpResources();

		if (isInitialised) {
			//((SettingsManager)context.getBean(SettingsManager.class)).saveSettings();
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
