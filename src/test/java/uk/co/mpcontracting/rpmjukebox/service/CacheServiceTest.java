package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;
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
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

  @Mock
  private ApplicationProperties applicationProperties;

  @Mock
  private InternetService internetService;

  @Mock
  private SettingsService settingsService;

  @Mock
  private SystemSettings systemSettings;

  private final File cacheDirectory = new File(getConfigDirectory(), "cache");
  private final ThreadRunner threadRunner = new ThreadRunner(Executors.newCachedThreadPool());
  private final HashGenerator hashGenerator = new HashGenerator();

  private CacheService underTest;

  @BeforeEach
  void beforeEach() {
    underTest = spy(new CacheService(applicationProperties, threadRunner, hashGenerator, internetService, settingsService));

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

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("getCacheTypes")
  void shouldGetLocationFromCache(CacheType cacheType) {
    String id = getFaker().numerify("######");
    String location = "https://" + getFaker().internet().url();
    File file = mock(File.class);
    URI uri = mock(URI.class);
    String path = getFaker().internet().url();

    when(underTest.readCache(cacheType, id)).thenReturn(of(file));
    when(file.toURI()).thenReturn(uri);
    when(uri.toString()).thenReturn(path);

    String result = underTest.getFileLocation(cacheType, id, location);

    assertThat(result).isEqualTo(path);
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("getCacheTypes")
  void shouldGetLocationFromSource(CacheType cacheType) {
    String id = getFaker().numerify("######");
    String location = "https://" + getFaker().internet().url();
    URL url = URI.create(location).toURL();
    URLConnection urlConnection = mock(URLConnection.class);
    InputStream inputStream = mock(InputStream.class);
    byte[] bytes = new byte[getFaker().number().numberBetween(100, 500)];
    Arrays.fill(bytes, (byte) getFaker().number().numberBetween(0, 255));

    when(internetService.openConnection(url)).thenReturn(urlConnection);
    when(urlConnection.getInputStream()).thenReturn(inputStream);
    when(inputStream.readAllBytes()).thenReturn(bytes);
    when(underTest.readCache(cacheType, id)).thenReturn(empty());

    CountDownLatch countDownLatch = new CountDownLatch(1);

    doAnswer(invocationOnMock -> {
      countDownLatch.countDown();

      return null;
    }).when(underTest).writeCache(eq(cacheType), eq(id), any());

    String result = underTest.getFileLocation(cacheType, id, location);

    countDownLatch.await();

    assertThat(result).isEqualTo(location);
    verify(underTest).writeCache(cacheType, id, bytes);
  }

  @SneakyThrows
  @ParameterizedTest
  @MethodSource("getCacheTypes")
  void shouldGetLocationFromSourceNotWriteCacheOnConnectionException(CacheType cacheType) {
    String id = getFaker().numerify("######");
    String location = "https://" + getFaker().internet().url();
    URL url = URI.create(location).toURL();
    URLConnection urlConnection = mock(URLConnection.class);
    InputStream inputStream = mock(InputStream.class);
    byte[] bytes = new byte[getFaker().number().numberBetween(100, 500)];
    Arrays.fill(bytes, (byte) getFaker().number().numberBetween(0, 255));

    when(internetService.openConnection(url)).thenReturn(urlConnection);
    when(urlConnection.getInputStream()).thenReturn(inputStream);
    when(underTest.readCache(cacheType, id)).thenReturn(empty());

    CountDownLatch countDownLatch = new CountDownLatch(1);

    doAnswer(invocationOnMock -> {
      countDownLatch.countDown();

      throw new IOException("CacheServiceTest.shouldGetAlbumImageUrlFromSourceNotWriteCacheOnConnectionException()");
    }).when(inputStream).readAllBytes();

    String result = underTest.getFileLocation(cacheType, id, location);

    countDownLatch.await();

    assertThat(result).isEqualTo(location);
    verify(underTest, never()).writeCache(cacheType, id, bytes);
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