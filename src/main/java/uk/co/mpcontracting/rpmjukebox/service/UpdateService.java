package uk.co.mpcontracting.rpmjukebox.service;

import static uk.co.mpcontracting.rpmjukebox.event.Event.APPLICATION_INITIALISED;

import com.igormaznitsa.commons.version.Version;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateService extends EventAwareObject {

  private final ThreadRunner threadRunner;
  private final ApplicationProperties applicationProperties;

  private final InternetService internetService;
  private final SettingsService settingsService;

  private Version newVersion;

  private void checkForUpdates() {
    log.debug("Checking for updates to version - {}", settingsService.getVersion());
    log.debug("Version url - {}", applicationProperties.getVersionUrl());

    try {
      HttpURLConnection connection = (HttpURLConnection) internetService.openConnection(
          URI.create(applicationProperties.getVersionUrl()).toURL());

      if (connection.getResponseCode() == 200) {
        StringBuilder response = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream())))) {
          reader.lines().forEach(response::append);
        }

        if (!response.toString().isEmpty()) {
          Version foundVersion = new Version(response.toString().trim());

          log.debug("Found version - {}", foundVersion);

          if (foundVersion.compareTo(settingsService.getVersion()) > 0) {
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

    threadRunner.run(() -> RpmJukebox.getAppHostServices().showDocument(applicationProperties.getWebsiteUrl()));
  }

  @Override
  public void eventReceived(Event event, Object... payload) {
    if (event == APPLICATION_INITIALISED) {
      threadRunner.run(this::checkForUpdates);
    }
  }
}
