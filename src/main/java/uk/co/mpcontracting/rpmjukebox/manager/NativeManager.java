package uk.co.mpcontracting.rpmjukebox.manager;

import com.sun.jna.Library;
import com.sun.jna.Native;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.OsType;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
@RequiredArgsConstructor
public class NativeManager {

    private final ThreadRunner threadRunner;

    private SettingsManager settingsManager;

    private NsUserNotificationsBridge nsUserNotificationsBridge;

    @Autowired
    public void wireSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @PostConstruct
    public void initialise() {
        log.info("Initialising NativeManager");

        // Make sure the native directory exists
        File nativeDirectory = settingsManager.getFileFromConfigDirectory("native");

        if (!nativeDirectory.exists()) {
            if (!nativeDirectory.mkdirs()) {
                log.warn("Unable to mkdirs - {}", nativeDirectory);
            }
        }

        // Copy any native libraries to the config directory and load them
        if (settingsManager.getOsType() == OsType.OSX) {
            try {
                String userNotifications = "/native/NsUserNotificationsBridge.dylib";
                File userNotificationsFile = settingsManager.getFileFromConfigDirectory(userNotifications);
                Files.copy(getClass().getResourceAsStream(userNotifications), userNotificationsFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

                nsUserNotificationsBridge = Native.loadLibrary(userNotificationsFile.getAbsolutePath(),
                        NsUserNotificationsBridge.class);
            } catch (Throwable t) {
                log.error("Error loading native notifications bridge", t);
            }
        }
    }

    public void displayNotification(Track track) {
        if (settingsManager.getOsType() == OsType.OSX) {
            threadRunner.run(() -> {
                try {
                    nsUserNotificationsBridge.sendNotification(track.getTrackName(), track.getArtistName(),
                        track.getAlbumName(), 0);
                } catch (Throwable e) {
                    log.warn("Unable to send OSX notification", e);
                }
            });
        }
    }

    // Package level for testing purposes
    interface NsUserNotificationsBridge extends Library {
        void sendNotification(String title, String subtitle, String text, int timeoffset);
    }
}
