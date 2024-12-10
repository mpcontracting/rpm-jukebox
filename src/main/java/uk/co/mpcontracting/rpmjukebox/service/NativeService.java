package uk.co.mpcontracting.rpmjukebox.service;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static uk.co.mpcontracting.rpmjukebox.util.OsType.OSX;

import com.sun.jna.Library;
import com.sun.jna.Native;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;

@Slf4j
@Service
@RequiredArgsConstructor
public class NativeService {

  private final ThreadRunner threadRunner;
  private final SettingsService settingsService;

  private NsUserNotificationsBridge nsUserNotificationsBridge;

  @PostConstruct
  public void initialise() {
    log.info("Initialising NativeManager");

    // Make sure the native directory exists
    File nativeDirectory = settingsService.getFileFromConfigDirectory("native");

    if (nonNull(nativeDirectory) && !nativeDirectory.exists() && !nativeDirectory.mkdirs()) {
      log.warn("Unable to create native service directory - {}", nativeDirectory);
    }

    // Copy any native libraries to the config directory and load them
    if (settingsService.getOsType() == OSX) {
      try {
        String userNotifications = "/native/NsUserNotificationsBridge.dylib";
        File userNotificationsFile = settingsService.getFileFromConfigDirectory(userNotifications);
        Files.copy(requireNonNull(getClass().getResourceAsStream(userNotifications)), userNotificationsFile.toPath(), REPLACE_EXISTING);

        nsUserNotificationsBridge = Native.load(userNotificationsFile.getAbsolutePath(), NsUserNotificationsBridge.class);
      } catch (Throwable t) {
        log.error("Error loading native notifications bridge", t);
      }
    }
  }

  public void displayNotification(Track track) {
    if (settingsService.getOsType() == OSX) {
      threadRunner.run(() -> {
        try {
          nsUserNotificationsBridge.sendNotification(track.getTrackName(), track.getArtistName(), track.getAlbumName(), 0);
        } catch (Throwable e) {
          log.warn("Unable to send OSX notification", e);
        }
      });
    }
  }

  protected interface NsUserNotificationsBridge extends Library {
    void sendNotification(String title, String subtitle, String text, int timeoffset);
  }
}
