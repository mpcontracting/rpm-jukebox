package uk.co.mpcontracting.rpmjukebox.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getRandomEnum;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getConfigDirectory;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getDateTimeInMillis;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;
import static uk.co.mpcontracting.rpmjukebox.util.CacheType.IMAGE;
import static uk.co.mpcontracting.rpmjukebox.util.CacheType.TRACK;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLEncoder;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.mpcontracting.rpmjukebox.config.ApplicationProperties;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.util.CacheType;
import uk.co.mpcontracting.rpmjukebox.util.HashGenerator;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private SettingsService settingsService;

  @Mock
  private SystemSettings systemSettings;

  private final File cacheDirectory = new File(getConfigDirectory(), "cache");
  private final HashGenerator hashGenerator = new HashGenerator();

  private CacheService underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new CacheService(applicationProperties, hashGenerator, settingsService);

    lenient().when(applicationProperties.getCacheDirectory()).thenReturn("cache");
    lenient().when(applicationProperties.getJettyPort()).thenReturn(43125);
    lenient().when(settingsService.getFileFromConfigDirectory(anyString())).thenReturn(cacheDirectory);
    lenient().when(settingsService.getSystemSettings()).thenReturn(systemSettings);
    lenient().when(systemSettings.getCacheSizeMb()).thenReturn(1);

    underTest.initialise();
  }

  @AfterEach
  void afterEach() {
    try {
      if (cacheDirectory.exists()) {
        FileUtils.deleteDirectory(cacheDirectory);
      }
    } catch (IOException e) {
      System.out.println("WARN : Unable to clean up test cache directory - " + cacheDirectory.getParentFile().getAbsolutePath());
    }
  }

  @Test
  void shouldReturnAValidInternalUrl() {
    String jettyPort = Integer.toString(applicationProperties.getJettyPort());
    CacheType cacheType = getRandomEnum(CacheType.class);
    String id = getFaker().numerify("#####");
    String location = "http://" + getFaker().internet().url();
    String expected = String.format("http://localhost:%s/cache?cacheType=%s&id=%s&url=%s", jettyPort, cacheType.name(), id, URLEncoder.encode(location, UTF_8));

    String result = underTest.constructInternalUrl(cacheType, id, location);

    assertThat(result).isEqualTo(expected);
  }

  @Test
  void shouldThrowExceptionConstructingInternalUrl() {
    CacheType cacheType = getRandomEnum(CacheType.class);
    String id = getFaker().numerify("#####");
    String location = "http://" + getFaker().internet().url();

    doThrow(new RuntimeException("CacheManagerTest.shouldThrowExceptionConstructingInternalUrl"))
        .when(applicationProperties).getJettyPort();

    assertThatThrownBy(() -> underTest.constructInternalUrl(cacheType, id, location)).isInstanceOf(RuntimeException.class);
  }

  @ParameterizedTest
  @MethodSource("getCacheTypes")
  void shouldReadCache(CacheType cacheType) {
    String id = getFaker().numerify("#####");
    String cacheContent = getFaker().lorem().characters(20, 50);

    writeCacheFile(cacheType, id, cacheContent);

    File file = underTest.readCache(cacheType, id).orElse(null);
    String result = readCacheFile(file);

    assertThat(result).isEqualTo(cacheContent);
  }

  @ParameterizedTest
  @MethodSource("getCacheTypes")
  void shouldFailToReadCacheOnException(CacheType cacheType) {
    String id = getFaker().numerify("#####");
    String cacheContent = getFaker().lorem().characters(20, 50);

    writeCacheFile(cacheType, id, cacheContent);

    setField(underTest, "cacheDirectory", mock(File.class));

    File file = underTest.readCache(cacheType, id).orElse(null);

    assertThat(file).isNull();
  }

  @ParameterizedTest
  @MethodSource("getCacheTypes")
  void shouldWriteCache(CacheType cacheType) {
    String id = getFaker().numerify("#####");
    String cacheContent = getFaker().lorem().characters(20, 50);

    underTest.writeCache(cacheType, id, cacheContent.getBytes());

    File file = new File(cacheDirectory, getCacheFilename(cacheType, id));
    String result = readCacheFile(file);

    assertThat(result).isEqualTo(cacheContent);
  }

  @ParameterizedTest
  @MethodSource("getCacheTypes")
  void shouldFailToWriteCacheOnException(CacheType cacheType) {
    String id = getFaker().numerify("#####");
    String cacheContent = getFaker().lorem().characters(20, 50);

    setField(underTest, "cacheDirectory", mock(File.class));

    underTest.writeCache(cacheType, id, cacheContent.getBytes());

    File file = new File(cacheDirectory, getCacheFilename(cacheType, id));

    assertThat(file.exists()).isFalse();
  }

  @ParameterizedTest
  @MethodSource("getCacheTypes")
  void shouldReturnCacheMiss(CacheType cacheType) {
    File file = underTest.readCache(cacheType, getFaker().numerify("#####")).orElse(null);

    assertThat(file).isNull();
  }

  @ParameterizedTest
  @MethodSource("getCacheTypes")
  void shouldOverwriteExistingCacheFile(CacheType cacheType) {
    String id = getFaker().numerify("#####");
    String cacheContent = getFaker().lorem().characters(20, 50);

    writeCacheFile(cacheType, id, cacheContent);
    File originalFile = new File(cacheDirectory, getCacheFilename(cacheType, id));
    if (!originalFile.setLastModified(getDateTimeInMillis(1971, 1, 1, 0, 0))) {
      System.out.println("WARN : Unable to set last modified on cache file - " + originalFile.getAbsolutePath());
    }
    long originalModified = originalFile.lastModified();

    underTest.writeCache(cacheType, id, cacheContent.getBytes());

    File newFile = new File(cacheDirectory, getCacheFilename(cacheType, id));

    assertThat(newFile.lastModified() > originalModified).isTrue();
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("getCacheTypes")
  public void shouldTrimOlderFilesOnCacheWrite(CacheType cacheType) {
    String id = getFaker().numerify("#####");
    String cacheContent = getFaker().lorem().characters(20, 50);

    // Create 50 100Kb files with decrementing last modified date
    for (int i = 0; i < 50; i++) {
      File file = new File(cacheDirectory, id + i);
      try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
        randomAccessFile.setLength(1024 * 100);
      }

      if (!file.setLastModified(getDateTimeInMillis(1971, 1, 1, 0, 50 - i))) {
        System.out.println("WARN : Unable to set last modified on cache file - " + file.getAbsolutePath());
      }
    }

    underTest.writeCache(cacheType, id, cacheContent.getBytes());

    File cachedFile = underTest.readCache(cacheType, id).orElse(null);

    assertThat(requireNonNull(cacheDirectory.listFiles()).length).isEqualTo(21);
    assertThat(cachedFile).isNotNull();
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("getCacheTypes")
  public void shouldTrimOlderFilesWithEqualTimestampsOnCacheWrite(CacheType cacheType) {
    String id = getFaker().numerify("#####");
    String cacheContent = getFaker().lorem().characters(20, 50);

    // Create 50 100Kb files with equal last modified date
    for (int i = 0; i < 50; i++) {
      File file = new File(cacheDirectory, id + i);
      try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
        randomAccessFile.setLength(1024 * 100);
      }

      if (!file.setLastModified(getDateTimeInMillis(1971, 1, 1, 0, 0))) {
        System.out.println("WARN : Unable to set last modified on cache file - " + file.getAbsolutePath());
      }
    }

    underTest.writeCache(cacheType, id, cacheContent.getBytes());

    File cachedFile = underTest.readCache(cacheType, id).orElse(null);

    assertThat(requireNonNull(cacheDirectory.listFiles()).length).isEqualTo(21);
    assertThat(cachedFile).isNotNull();
  }

  private static Stream<Arguments> getCacheTypes() {
    return Stream.of(
        Arguments.of(IMAGE),
        Arguments.of(TRACK)
    );
  }



  @SneakyThrows
  private void writeCacheFile(CacheType cacheType, String id, String cacheContent) {
    File file = new File(cacheDirectory, getCacheFilename(cacheType, id));

    try (FileOutputStream outputStream = new FileOutputStream(file)) {
      outputStream.write(cacheContent.getBytes());
    }
  }

  private String getCacheFilename(CacheType cacheType, String id) {
    return cacheType == TRACK ? id : hashGenerator.generateHash(id);
  }

  @SneakyThrows
  private String readCacheFile(File file) {
    StringBuilder builder = new StringBuilder();

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      reader.lines().forEach(builder::append);
    }

    return builder.toString();
  }
}