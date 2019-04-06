package uk.co.mpcontracting.rpmjukebox.manager;

import com.igormaznitsa.commons.version.Version;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static uk.co.mpcontracting.rpmjukebox.event.Event.APPLICATION_INITIALISED;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateManager extends EventAwareObject implements Constants {

    private final AppProperties appProperties;
    private final ThreadRunner threadRunner;

    private SettingsManager settingsManager;
    private InternetManager internetManager;

    private Version newVersion;

    @Autowired
    public void wireSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Autowired
    public void wireInternetManager(InternetManager internetManager) {
        this.internetManager = internetManager;
    }

    private void checkForUpdates() {
        log.debug("Checking for updates to version - {}", settingsManager.getVersion());
        log.debug("Version url - {}", appProperties.getVersionUrl());

        try {
            HttpURLConnection connection = (HttpURLConnection) internetManager.openConnection(
                    new URL(appProperties.getVersionUrl()));

            if (connection.getResponseCode() == 200) {
                StringBuilder response = new StringBuilder();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())))) {
                    reader.lines().forEach(response::append);
                }

                if (response.toString().length() > 0) {
                    Version foundVersion = new Version(response.toString().trim());

                    log.debug("Found version - {}", foundVersion);

                    if (foundVersion.compareTo(settingsManager.getVersion()) > 0) {
                        log.debug("New version available");

                        newVersion = foundVersion;

                        fireEvent(Event.NEW_VERSION_AVAILABLE, newVersion);
                    }
                }
            } else {
                log.error("Unable to check for new version : Response code - {}", connection.getResponseCode());
            }
        } catch (Exception e) {
            log.error("Error checking for new version", e);
        }
    }

    public void downloadNewVersion() {
        log.debug("Downloading new version - {}", newVersion);

        threadRunner.run(() -> RpmJukebox.getAppHostServices().showDocument(appProperties.getWebsiteUrl()));
    }

    @Override
    public void eventReceived(Event event, Object... payload) {
        if (event == APPLICATION_INITIALISED) {
            threadRunner.run(this::checkForUpdates);
        }
    }
}
