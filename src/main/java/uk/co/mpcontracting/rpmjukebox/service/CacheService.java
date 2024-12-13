package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.co.mpcontracting.rpmjukebox.util.CacheType.TRACK;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.util.CacheType;
import uk.co.mpcontracting.rpmjukebox.util.HashGenerator;
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

  private final ApplicationProperties applicationProperties;
  private final ThreadRunner threadRunner;
  private final HashGenerator hashGenerator;
  private final InternetService internetService;
  private final SettingsService settingsService;

  private File cacheDirectory;
  private Comparator<File> timestampComparator;

  @PostConstruct
  public void initialise() {
    log.info("Initialising {}", getClass().getSimpleName());

    // Look for the cache directory and create it if it isn't there
    cacheDirectory = settingsService.getFileFromConfigDirectory(applicationProperties.getCacheDirectory());

    if (nonNull(cacheDirectory) && !cacheDirectory.exists() && !cacheDirectory.mkdirs()) {
      throw new RuntimeException("Unable to create cache directory - " + cacheDirectory.getAbsolutePath());
    }

    timestampComparator = (file1, file2) -> {
      if (file1.lastModified() == file2.lastModified()) {
        return 0;
      }

      return file1.lastModified() > file2.lastModified() ? 1 : -1;
    };
  }

  public String getFileLocation(CacheType cacheType, String id, String location) {
    return readCache(cacheType, id)
        .map(file -> file.toURI().toString())
        .orElseGet(() -> {
          threadRunner.run(() -> {
            log.debug("Getting file from the Internet - {} - {}", cacheType, location);

            try {
              URL url = URI.create(location).toURL();

              try (InputStream inputStream = internetService.openConnection(url).getInputStream()) {
                byte[] bytes = inputStream.readAllBytes();

                writeCache(cacheType, id, bytes);
              }

              log.debug("File written to cache {} - {}", cacheType, location);
            } catch (IOException e) {
              log.warn("Unable to get file from the Internet - {} - {}", cacheType, location);
            }
          });

          return location;
        });
  }

  @Synchronized
  protected Optional<File> readCache(CacheType cacheType, String id) {
    log.debug("Reading cache : Cache type - {}, ID - {}", cacheType, id);

    try {
      File file = new File(cacheDirectory, (cacheType == TRACK ? id : hashGenerator.generateHash(id)));

      if (file.exists()) {
        log.debug("Found cached file : Cache type - {}, ID - {}", cacheType, id);

        if (!file.setLastModified(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())) {
          log.warn("Unable to set last modified on cached file - {}", file.getAbsolutePath());
        }

        return of(file);
      }

      log.debug("Cached file not found : Cache type - {}, ID - {}", cacheType, id);
    } catch (Exception e) {
      log.error("Unable to read cache : Cache type - {}, ID - {}", cacheType, id, e);
    }

    return empty();
  }

  @Synchronized
  protected void writeCache(CacheType cacheType, String id, byte[] fileContent) {
    log.debug("Writing cache : Cache type - {}, ID - {}", cacheType, id);

    try {
      File file = new File(cacheDirectory, (cacheType == TRACK ? id : hashGenerator.generateHash(id)));

      log.info("File - {}", file.getAbsolutePath());

      if (file.exists()) {
        if (!file.delete()) {
          log.warn("Unable to delete cached file - {}", file.getAbsolutePath());
        }
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
    int cacheSizeMb = settingsService.getSystemSettings().getCacheSizeMb();

    log.debug("Trimming the cache to {}Mb", cacheSizeMb);

    List<File> files = new ArrayList<>(Arrays.asList(requireNonNull(cacheDirectory.listFiles())));

    files.sort(timestampComparator);

    while (getCacheSizeMb(files) > cacheSizeMb && !files.isEmpty()) {
      File file = files.getFirst();

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

    return (int) cacheSizeBytes / 1024 / 1024;
  }
}
