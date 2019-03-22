package uk.co.mpcontracting.rpmjukebox.manager;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getConfigDirectory;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getDateTimeInMillis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.RandomAccessFile;
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

@RunWith(MockitoJUnitRunner.class)
public class CacheManagerTest  {

    @Mock
    private AppProperties mockAppProperties;

    @Mock
    private SettingsManager mockSettingsManager;

    @Mock
    private SystemSettings mockSystemSettings;

    private File cacheDirectory = new File(getConfigDirectory(), "cache");
    private HashGenerator hashGenerator;
    private CacheManager cacheManager;

    @Before
    public void setup() {
        hashGenerator = new HashGenerator();

        cacheManager = new CacheManager(mockAppProperties, hashGenerator);
        cacheManager.wireSettingsManager(mockSettingsManager);

        when(mockAppProperties.getCacheDirectory()).thenReturn("cache");
        when(mockAppProperties.getJettyPort()).thenReturn(43125);
        when(mockSettingsManager.getFileFromConfigDirectory(anyString())).thenReturn(cacheDirectory);
        when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);
        when(mockSystemSettings.getCacheSizeMb()).thenReturn(1);

        cacheManager.initialise();
    }

    @Test
    public void shouldReturnAValidInternalUrl() {
        String result = cacheManager.constructInternalUrl(CacheType.IMAGE, "12345", "http://www.example.com");

        assertThat(result).isEqualTo("http://localhost:43125/cache?cacheType=IMAGE&id=12345&url=http%3A%2F%2Fwww.example.com");
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionConstructingInternalUrl() {
        doThrow(new RuntimeException("CacheManagerTest.shouldThrowExceptionConstructingInternalUrl"))
            .when(mockAppProperties).getJettyPort();

        cacheManager.constructInternalUrl(CacheType.TRACK, "12345", "http://www.example.com");
    }

    @Test
    public void shouldReadImageCache() throws Exception {
        CacheType cacheType = CacheType.IMAGE;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldReadImageCache()";

        writeCacheFile(cacheType, id, cacheContent);

        File file = cacheManager.readCache(cacheType, id);
        String result = readCacheFile(file);

        assertThat(result).isEqualTo(cacheContent);
    }

    @Test
    public void shouldFailToReadImageCacheOnException() throws Exception {
        CacheType cacheType = CacheType.IMAGE;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldFailToReadImageCacheOnException()";

        writeCacheFile(cacheType, id, cacheContent);

        ReflectionTestUtils.setField(cacheManager, "cacheDirectory", mock(File.class));

        File file = cacheManager.readCache(cacheType, id);

        assertThat(file).isNull();
    }

    @Test
    public void shouldReadTrackCache() throws Exception {
        CacheType cacheType = CacheType.TRACK;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldReadTrackCache()";

        writeCacheFile(cacheType, id, cacheContent);

        File file = cacheManager.readCache(cacheType, id);
        String result = readCacheFile(file);

        assertThat(result).isEqualTo(cacheContent);
    }

    @Test
    public void shouldWriteImageCache() throws Exception {
        CacheType cacheType = CacheType.IMAGE;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldWriteImageCache()";

        cacheManager.writeCache(cacheType, id, cacheContent.getBytes());

        File file = new File(cacheDirectory, hashGenerator.generateHash(id));
        String result = readCacheFile(file);

        assertThat(result).isEqualTo(cacheContent);
    }

    @Test
    public void shouldFailToWriteImageCacheOnException() throws Exception {
        CacheType cacheType = CacheType.IMAGE;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldFailToWriteImageCacheOnException()";

        ReflectionTestUtils.setField(cacheManager, "cacheDirectory", mock(File.class));

        cacheManager.writeCache(cacheType, id, cacheContent.getBytes());

        File file = new File(cacheDirectory, hashGenerator.generateHash(id));

        assertThat(file.exists()).isFalse();
    }

    @Test
    public void shouldWriteTrackCache() throws Exception {
        CacheType cacheType = CacheType.TRACK;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldWriteTrackCache()";

        cacheManager.writeCache(cacheType, id, cacheContent.getBytes());

        File file = new File(cacheDirectory, id);
        String result = readCacheFile(file);

        assertThat(result).isEqualTo(cacheContent);
    }

    @Test
    public void shouldReturnCacheMiss() {
        File file = cacheManager.readCache(CacheType.IMAGE, "12345");

        assertThat(file).isNull();
    }

    @Test
    public void shouldOverwriteExistingCacheFile() throws Exception {
        CacheType cacheType = CacheType.TRACK;
        String id = "12345";
        String cacheContent = "CacheManagerTest.shouldOverwriteExistingCacheFile()";

        writeCacheFile(cacheType, id, cacheContent);
        File originalFile = new File(cacheDirectory, id);
        originalFile.setLastModified(getDateTimeInMillis(1971, 1, 1, 0, 0));
        long originalModified = originalFile.lastModified();

        cacheManager.writeCache(cacheType, id, cacheContent.getBytes());

        File newFile = new File(cacheDirectory, id);

        assertThat(newFile.lastModified() > originalModified).isTrue();
    }

    @Test
    public void shouldTrimOlderFilesOnCacheWrite() throws Exception {
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

        cacheManager.writeCache(cacheType, id, cacheContent.getBytes());

        File cachedFile = cacheManager.readCache(cacheType, id);

        assertThat(cacheDirectory.listFiles().length).isEqualTo(21);
        assertThat(cachedFile).isNotNull();
    }

    @Test
    public void shouldTrimOlderFilesWithEqualTimestampsOnCacheWrite() throws Exception {
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

        cacheManager.writeCache(cacheType, id, cacheContent.getBytes());

        File cachedFile = cacheManager.readCache(cacheType, id);

        assertThat(requireNonNull(cacheDirectory.listFiles()).length).isEqualTo(21);
        assertThat(cachedFile).isNotNull();
    }

    @After
    @SneakyThrows
    public void cleanup() {
        FileUtils.deleteDirectory(getConfigDirectory());
    }

    private void writeCacheFile(CacheType cacheType, String id, String cacheContent) throws Exception {
        File file = new File(cacheDirectory,
            (cacheType == CacheType.TRACK ? id : hashGenerator.generateHash(id)));
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(cacheContent.getBytes());
        }
    }

    private String readCacheFile(File file) throws Exception {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            reader.lines().forEach(builder::append);
        }

        return builder.toString();
    }
}
