package uk.co.mpcontracting.rpmjukebox.service;

import java.io.File;
import java.util.Optional;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.util.CacheType;

@Slf4j
@Service
public class CacheService {

  public String constructInternalUrl(CacheType cacheType, String id, String location) {
    return null;
  }

  @Synchronized
  public Optional<File> readCache(CacheType cacheType, String id) {
    return Optional.empty();
  }

  @Synchronized
  public void writeCache(CacheType cacheType, String id, byte[] fileContent) {

  }
}
