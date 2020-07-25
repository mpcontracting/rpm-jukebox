package uk.co.mpcontracting.rpmjukebox.manager;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.co.mpcontracting.rpmjukebox.configuration.AppProperties;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.HashGenerator;

import java.io.*;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getConfigDirectory;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getDateTimeInMillis;

@RunWith(MockitoJUnitRunner.class)
public class CacheManagerTest {

    @Mock
    private AppProperties appProperties;

    @Mock
    private SettingsManager settingsManager;

    @Mock
    private SystemSettings systemSettings;

    private final File cacheDirectory = new File(getConfigDirectory(), "cache");
    private final HashGenerator hashGenerator = new HashGenerator();

    private CacheManager underTest;

    @Before
    public void setup() {
        underTest = new CacheManager(appProperties, hashGenerator);
        underTest.wireSettingsManager(settingsManager);

        when(appProperties.getCacheDirectory()).thenReturn("cache");
        when(appProperties.getJettyPort()).thenReturn(43125);
        when(settingsManager.getFileFromConfigDirectory(anyString())).thenReturn(cacheDirectory);
        when(settingsManager.getSystemSettings()).thenReturn(systemSettings);
        when(systemSettings.getCacheSizeMb()).thenReturn(1);

        underTest.initialise();
    }

    @Test
    public void shouldReturnAValidInternalUrl() {
        String result = underTest.constructInternalUrl(CacheType.IMAGE, "12345", "http://www.example.com");

        assertThat(result).isEqualTo("http://localhost:43125/cache?cacheType=IMAGE&id=12345&url=http%3A%2F%2Fwww.example.com");
    }

    @Test
    public void shouldThrowExceptionConstructingInternalUrl() {
        doThrow(new RuntimeException("CacheManagerTest.shouldThrowExceptionConstructingInternalUrl"))
                .when(appProperties).getJettyPort();

        assertThatThrownBy(() -> underTest.constructInternalUrl(CacheType.TRACK, "12345", "http://www.example.com"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void shouldReadImageCache() {
        CacheType cacheType = CacheType.IMAGE;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldReadImageCache()";

        writeCacheFile(cacheType, id, cacheContent);

        File file = underTest.readCache(cacheType, id).orElse(null);
        String result = readCacheFile(file);

        assertThat(result).isEqualTo(cacheContent);
    }

    @Test
    public void shouldFailToReadImageCacheOnException() {
        CacheType cacheType = CacheType.IMAGE;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldFailToReadImageCacheOnException()";

        writeCacheFile(cacheType, id, cacheContent);

        ReflectionTestUtils.setField(underTest, "cacheDirectory", mock(File.class));

        File file = underTest.readCache(cacheType, id).orElse(null);

        assertThat(file).isNull();
    }

    @Test
    public void shouldReadTrackCache() {
        CacheType cacheType = CacheType.TRACK;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldReadTrackCache()";

        writeCacheFile(cacheType, id, cacheContent);

        File file = underTest.readCache(cacheType, id).orElse(null);
        String result = readCacheFile(file);

        assertThat(result).isEqualTo(cacheContent);
    }

    @Test
    @SneakyThrows
    public void shouldWriteImageCache() {
        CacheType cacheType = CacheType.IMAGE;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldWriteImageCache()";

        underTest.writeCache(cacheType, id, cacheContent.getBytes());

        File file = new File(cacheDirectory, hashGenerator.generateHash(id));
        String result = readCacheFile(file);

        assertThat(result).isEqualTo(cacheContent);
    }

    @Test
    @SneakyThrows
    public void shouldFailToWriteImageCacheOnException() {
        CacheType cacheType = CacheType.IMAGE;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldFailToWriteImageCacheOnException()";

        ReflectionTestUtils.setField(underTest, "cacheDirectory", mock(File.class));

        underTest.writeCache(cacheType, id, cacheContent.getBytes());

        File file = new File(cacheDirectory, hashGenerator.generateHash(id));

        assertThat(file.exists()).isFalse();
    }

    @Test
    public void shouldWriteTrackCache() {
        CacheType cacheType = CacheType.TRACK;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldWriteTrackCache()";

        underTest.writeCache(cacheType, id, cacheContent.getBytes());

        File file = new File(cacheDirectory, id);
        String result = readCacheFile(file);

        assertThat(result).isEqualTo(cacheContent);
    }

    @Test
    public void shouldReturnCacheMiss() {
        File file = underTest.readCache(CacheType.IMAGE, "12345").orElse(null);

        assertThat(file).isNull();
    }

    @Test
    public void shouldOverwriteExistingCacheFile() {
        CacheType cacheType = CacheType.TRACK;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldOverwriteExistingCacheFile()";

        writeCacheFile(cacheType, id, cacheContent);
        File originalFile = new File(cacheDirectory, id);
        originalFile.setLastModified(getDateTimeInMillis(1971, 1, 1, 0, 0));
        long originalModified = originalFile.lastModified();

        underTest.writeCache(cacheType, id, cacheContent.getBytes());

        File newFile = new File(cacheDirectory, id);

        assertThat(newFile.lastModified() > originalModified).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldTrimOlderFilesOnCacheWrite() {
        CacheType cacheType = CacheType.TRACK;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldTrimOlderFilesOnCacheWrite()";

        // Create 50 100Kb files with decrementing last modified date
        for (int i = 0; i < 50; i++) {
            File file = new File(cacheDirectory, id + i);
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                randomAccessFile.setLength(1024 * 100);
            }

            file.setLastModified(getDateTimeInMillis(1971, 1, 1, 0, 50 - i));
        }

        underTest.writeCache(cacheType, id, cacheContent.getBytes());

        File cachedFile = underTest.readCache(cacheType, id).orElse(null);

        assertThat(requireNonNull(cacheDirectory.listFiles()).length).isEqualTo(21);
        assertThat(cachedFile).isNotNull();
    }

    @Test
    @SneakyThrows
    public void shouldTrimOlderFilesWithEqualTimestampsOnCacheWrite() {
        CacheType cacheType = CacheType.TRACK;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldTrimOlderFilesOnCacheWrite()";

        // Create 50 100Kb files with equal last modified date
        for (int i = 0; i < 50; i++) {
            File file = new File(cacheDirectory, id + i);
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                randomAccessFile.setLength(1024 * 100);
            }

            file.setLastModified(getDateTimeInMillis(1971, 1, 1, 0, 0));
        }

        underTest.writeCache(cacheType, id, cacheContent.getBytes());

        File cachedFile = underTest.readCache(cacheType, id).orElse(null);

        assertThat(requireNonNull(cacheDirectory.listFiles()).length).isEqualTo(21);
        assertThat(cachedFile).isNotNull();
    }

    @After
    @SneakyThrows
    public void cleanup() {
        FileUtils.deleteDirectory(getConfigDirectory());
    }

    @SneakyThrows
    private void writeCacheFile(CacheType cacheType, String id, String cacheContent) {
        File file = new File(cacheDirectory, (cacheType == CacheType.TRACK ? id : hashGenerator.generateHash(id)));

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(cacheContent.getBytes());
        }
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
