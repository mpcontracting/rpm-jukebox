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

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.HashGenerator;

@Slf4j
@Component
public class CacheManager implements Constants {

    @Autowired
    private SettingsManager settingsManager;

    @Value("${directory.cache}")
    private String directoryCache;

    @Value("${internal.jetty.port}")
    private int internalJettyPort;

    private File cacheDirectory;

    private Comparator<File> timestampComparator;

    @PostConstruct
    public void initialise() {
        log.info("Initialising CacheManager");

        // Look for the cache directory and create it if it isn't there
        cacheDirectory = settingsManager.getFileFromConfigDirectory(directoryCache);

        if (!cacheDirectory.exists()) {
            if (!cacheDirectory.mkdirs()) {
                throw new RuntimeException("Unable to create cache directory - " + cacheDirectory.getAbsolutePath());
            }
        }

        timestampComparator = (file1, file2) -> {
            if (file1.lastModified() == file2.lastModified()) {
                return 0;
            }

            return file1.lastModified() > file2.lastModified() ? 1 : -1;
        };
    }

    @SneakyThrows
    public String constructInternalUrl(CacheType cacheType, String id, String location) {
        return "http://localhost:" + internalJettyPort + "/cache?cacheType=" + cacheType + "&id=" + id + "&url="
            + URLEncoder.encode(location, "UTF-8");
    }

    @Synchronized
    public File readCache(CacheType cacheType, String id) {
        log.debug("Reading cache : Cache type - {}, ID - {}", cacheType, id);

        try {
            File file = new File(cacheDirectory, (cacheType == CacheType.TRACK ? id : HashGenerator.generateHash(id)));

            if (file.exists()) {
                log.debug("Found cached file : Cache type - {}, ID - {}", cacheType, id);

                file.setLastModified(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());

                return file;
            }

            log.debug("Cached file not found : Cache type - {}, ID - {}", cacheType, id);
        } catch (Exception e) {
            log.error("Unable to read cache : Cache type - {}, ID - {}", cacheType, id, e);
        }

        return null;
    }

    @Synchronized
    public void writeCache(CacheType cacheType, String id, byte[] fileContent) {
        log.debug("Writing cache : Cache type - {}, ID - {}", cacheType, id);

        try {
            File file = new File(cacheDirectory, (cacheType == CacheType.TRACK ? id : HashGenerator.generateHash(id)));

            if (file.exists()) {
                file.delete();
            }

            try (FileOutputStream outputStream = new FileOutputStream(file)) {
                outputStream.write(fileContent);
            } catch (Exception e) {
                log.error("Unable to write cache : Cache type - {}, ID - {}", cacheType, id, e);
            }

            trimCache();
        } catch (Exception e) {
            log.error("Unable to write cache : Cache type - {}, ID - {}", cacheType, id, e);
        }
    }

    private void trimCache() {
        int cacheSizeMb = settingsManager.getSystemSettings().getCacheSizeMb();

        log.debug("Trimming the cache to {}Mb", cacheSizeMb);

        List<File> files = new ArrayList<>();

        for (File file : cacheDirectory.listFiles()) {
            files.add(file);
        }

        Collections.sort(files, timestampComparator);

        while (getCacheSizeMb(files) > cacheSizeMb && !files.isEmpty()) {
            File file = files.get(0);

            if (!file.delete()) {
                log.warn("Unable to delete file - {}", file.getAbsolutePath());

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
