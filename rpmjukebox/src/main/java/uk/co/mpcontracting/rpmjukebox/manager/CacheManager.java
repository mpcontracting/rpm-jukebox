package uk.co.mpcontracting.rpmjukebox.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.ioc.annotation.Autowired;
import uk.co.mpcontracting.ioc.annotation.Component;
import uk.co.mpcontracting.ioc.factory.InitializingBean;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.HashGenerator;

@Slf4j
@Component
public class CacheManager implements InitializingBean, Constants {

	@Autowired
	private SettingsManager settingsManager;
	
	private File cacheDirectory;
	private int internalJettyPort;
	private int cacheSizeMb;
	
	private Comparator<File> timestampComparator;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initialising CacheManager");
		
		// Look for the config directory and create it if it isn't there
		File homeDir = new File(System.getProperty("user.home"));
		cacheDirectory = new File(new File(homeDir, settingsManager.getPropertyString(PROP_DIRECTORY_CONFIG)), settingsManager.getPropertyString(PROP_DIRECTORY_CACHE));

		if (!cacheDirectory.exists()) {
			if (!cacheDirectory.mkdirs()) {
				throw new RuntimeException("Unable to create cache directory - " + cacheDirectory.getAbsolutePath());
			}
		}
		
		internalJettyPort = settingsManager.getPropertyInteger(PROP_INTERNAL_JETTY_PORT);
		cacheSizeMb = settingsManager.getPropertyInteger(PROP_CACHE_SIZE_MB);
		
		timestampComparator = new Comparator<File>() {
			@Override
			public int compare(File file1, File file2) {
				if (file1.lastModified() == file2.lastModified()) {
					return 0;
				}
				
				return (file1.lastModified() > file2.lastModified() ? 1 : -1);
			}
		};
	}
	
	@SneakyThrows
	public String constructInternalUrl(CacheType cacheType, String id, String location) {
		return "http://localhost:" + internalJettyPort + "/cache?cacheType=" + cacheType + "&id=" + id + 
			"&url=" + URLEncoder.encode(location, "UTF-8");
	}
	
	@Synchronized
	public File readCache(CacheType cacheType, String id) {
		log.debug("Reading cache : Cache type - " + cacheType + ", ID - " + id);

		try {
			File file = new File(cacheDirectory, (cacheType == CacheType.TRACK ? id : HashGenerator.generateHash(id)));
			
			if (file.exists()) {
				log.debug("Found cached file : Cache type - " + cacheType + ", ID - " + id);
				
				file.setLastModified(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
	
				return file;
			}
		
			log.debug("Cached file not found : Cache type - " + cacheType + ", ID - " + id);
		} catch (Exception e) {
			log.error("Unable to read cache : Cache type - " + cacheType + ", ID - " + id, e);
		}
		
		return null;
	}
	
	@Synchronized
	public void writeCache(CacheType cacheType, String id, byte[] fileContent) {
		log.debug("Writing cache : Cache type - " + cacheType + ", ID - " + id);
		
		try {
			File file = new File(cacheDirectory, (cacheType == CacheType.TRACK ? id : HashGenerator.generateHash(id)));
			
			if (file.exists()) {
				file.delete();
			}
			
			try (FileOutputStream outputStream = new FileOutputStream(file)) {
				outputStream.write(fileContent);
			} catch (Exception e) {
				log.error("Unable to write cache : Cache type - " + cacheType + ", ID - " + id, e);
			}
		
			trimCache();
		} catch (Exception e) {
			log.error("Unable to write cache : Cache type - " + cacheType + ", ID - " + id, e);
		}
	}
	
	private void trimCache() {
		log.debug("Trimming the cache to " + cacheSizeMb + "Mb");
		
		List<File> files = new ArrayList<File>();
		
		for (File file : cacheDirectory.listFiles()) {
			files.add(file);
		}
		
		Collections.sort(files, timestampComparator);

		while (getCacheSizeMb(files) > cacheSizeMb && !files.isEmpty()) {
			File file = files.get(0);

			if (!file.delete()) {
				log.warn("Unable to delete file - " + file.getAbsolutePath());
				
				break;
			}

			files.remove(file);
		}
	}
	
	private int getCacheSizeMb(List<File> files) {
		long cacheSizeBytes = 0;
		
		for (File file : files) {
			cacheSizeBytes += file.length();
		}

		return (int)cacheSizeBytes / 1024 / 1024;
	}
}
