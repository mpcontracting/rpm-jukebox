package uk.co.mpcontracting.rpmjukebox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import de.felixroske.jfxsupport.SplashScreen;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.view.MainPanelView;

@Slf4j
@SpringBootApplication
public class RpmJukebox extends AbstractJavaFxApplicationSupport implements Constants {

	@Getter private static File configDirectory;

	public static void main(String[] args) {
		// Look for the config directory and create it if it isn't there
		File homeDir = new File(System.getProperty("user.home"));
		
		// See if we have a command line override
		if (System.getProperty("directory.config") != null) {
			configDirectory = new File(homeDir, System.getProperty("directory.config"));
			
		} else {
			configDirectory = new File(homeDir, ".rpmjukebox");
		}

		if (!configDirectory.exists()) {
			if (!configDirectory.mkdirs()) {
				throw new RuntimeException("Unable to create config directory - " + configDirectory.getAbsolutePath());
			}
		}

		log.info("Config directory - " + configDirectory);

		// Initialise the logging
		initialiseLogging();

		// Add a spacer to the logging file to separate startups
		/*log.info("====================================================");
		log.info("====================================================");
		log.info("====================================================");
		log.info("Java Version - " + System.getProperty("java.version"));*/
		
		launchApp(RpmJukebox.class, MainPanelView.class, new RpmJukeboxSplash(), args);
	}
	
	private static void initialiseLogging() {
		try {
			// Copy the logging.properties file if it doesn't already exist
			File loggingFile = new File(configDirectory, "logback.xml");

			if (!loggingFile.exists()) {
				// Load into memory and replace the log root
				StringBuilder builder = new StringBuilder();
				
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(RpmJukebox.class.getResourceAsStream("/logback-config.xml")))) {
					reader.lines().forEach(line -> {
						if (line.contains("${}")) {
							builder.append(StringUtils.replace(line, "${}", new File(configDirectory, "log").getAbsolutePath()));
						} else {
							builder.append(line);
						}
						
						builder.append("\r\n");
					});
				}

				try (FileWriter writer = new FileWriter(loggingFile)) {
					writer.write(builder.toString());
				}
			}
			
			// Notify Spring where the logging config is
			System.setProperty("logging.config", loggingFile.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class RpmJukeboxSplash extends SplashScreen {
		@Override
		public boolean visible() {
			return false;
		}
	}
}
