package uk.co.mpcontracting.rpmjukebox.manager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.igormaznitsa.commons.version.Version;

import lombok.extern.slf4j.Slf4j;
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

	@Value("${version.url}")
	private URL versionUrl;
	
	@Value("${website.url}")
	private String websiteUrl;
	
	private Version newVersion;
	
	// Package level for testing purposes
	void checkForUpdates() {
		log.debug("Checking for updates to version - " + settingsManager.getVersion());
		log.debug("Version url - " + versionUrl);

		try {
			HttpURLConnection connection = (HttpURLConnection)versionUrl.openConnection();
			
			if (connection.getResponseCode() == 200) {
				StringBuilder response = new StringBuilder();
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())))) {
					reader.lines().forEach(line -> {
						response.append(line);
					});
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
	}
	
	public void downloadNewVersion() {
		log.debug("Downloading new version - " + newVersion);

		ThreadRunner.run(() -> {
			RpmJukebox.getAppHostServices().showDocument(websiteUrl);
		});
	}
	
	@Override
	public void eventReceived(Event event, Object... payload) {
		switch (event) {
			case APPLICATION_INITIALISED: {
				ThreadRunner.run(() -> {
					checkForUpdates();
				});

				break;
			}
			default: {
				// Nothing
			}
		}
	}
}
