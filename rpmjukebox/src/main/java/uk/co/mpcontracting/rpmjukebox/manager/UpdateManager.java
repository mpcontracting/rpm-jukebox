package uk.co.mpcontracting.rpmjukebox.manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.igormaznitsa.commons.version.Version;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

@Slf4j
@Component
public class UpdateManager extends EventAwareObject implements Constants {

	@Autowired
	private SettingsManager settingsManager;
	
	@Autowired
	private RpmJukebox rpmJukebox;
	
	private Version newVersion;
	
	private void checkForUpdates() {
		log.debug("Checking for updates to version - " + settingsManager.getVersion());

		ThreadRunner.run(() -> {
			try {
				URL url = new URL(settingsManager.getPropertyString(PROP_VERSION_URL));
				
				log.debug("Version url - " + url);
				
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				
				if (connection.getResponseCode() == 200) {
					StringBuilder response = new StringBuilder();
					
					try (BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())))) {
						String nextLine = null;
						
						while ((nextLine = reader.readLine()) != null) {
							response.append(nextLine);
						}
					}
					
					if (response.toString().length() > 0) {
						Version foundVersion = new Version(response.toString().trim());
						
						log.debug("Found version - " + foundVersion);
						
						if (foundVersion.compareTo(settingsManager.getVersion()) > 0) {
							log.debug("New version available");
							
							newVersion = foundVersion;
							
							fireEvent(Event.NEW_VERSION_AVAILABLE, newVersion);
						}
					}
				} else {
					log.error("Unable to check for new version : Response code - " + connection.getResponseCode());
				}
			} catch (Exception e) {
				log.error("Error checking for new version", e);
			}
		});
	}
	
	public void downloadNewVersion() {
		log.debug("Downloading new version - " + newVersion);

		ThreadRunner.run(() -> {
			rpmJukebox.getHostServices().showDocument(settingsManager.getPropertyString(PROP_WEBSITE_URL));
		});
	}
	
	@Override
	public void eventReceived(Event event, Object... payload) {
		switch (event) {
			case APPLICATION_INITIALISED: {
				checkForUpdates();
				
				break;
			}
			default: {
				// Nothing
			}
		}
	}
}