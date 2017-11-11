package uk.co.mpcontracting.rpmjukebox.manager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.sun.jna.Library;
import com.sun.jna.Native;

import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.ioc.factory.InitializingBean;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.OsType;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

@Slf4j
@Component
public class NativeManager implements InitializingBean {

	@Autowired
	private SettingsManager settingsManager;
	
	private NsUserNotificationsBridge nsUserNotificationsBridge;

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising NativeManager");
		
		// Make sure the native directory exists
		File nativeDirectory = settingsManager.getFileFromConfigDirectory("native");

		if (!nativeDirectory.exists()) {
			nativeDirectory.mkdirs();
		}
		
		// Copy any native libraries to the config directory and load them
		if (settingsManager.getOsType() == OsType.OSX) {
			try {
				String userNotifications = "/native/NsUserNotificationsBridge.dylib";
				File userNotificationsFile = settingsManager.getFileFromConfigDirectory(userNotifications);
				Files.copy(getClass().getResourceAsStream(userNotifications), userNotificationsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	
				nsUserNotificationsBridge = (NsUserNotificationsBridge)Native.loadLibrary(userNotificationsFile.getAbsolutePath(), NsUserNotificationsBridge.class);
			} catch (Throwable t) {
				log.error("Error", t);
			}
		}
	}

	public void displayNotification(Track track) {
		if (settingsManager.getOsType() == OsType.OSX) {
			ThreadRunner.run(() -> {
				try {
					nsUserNotificationsBridge.sendNotification(track.getTrackName(), track.getArtistName(), track.getAlbumName(), 0);
				} catch (Exception e) {
					log.warn("Unable to send OSX notification", e);
				}
			});
		}
	}
	
	private interface NsUserNotificationsBridge extends Library {
		public int sendNotification(String title, String subtitle, String text, int timeoffset);
	}
}
