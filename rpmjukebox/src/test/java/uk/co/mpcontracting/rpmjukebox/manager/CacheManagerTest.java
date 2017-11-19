package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.RandomAccessFile;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.AbstractTest;
import uk.co.mpcontracting.rpmjukebox.settings.SystemSettings;
import uk.co.mpcontracting.rpmjukebox.support.CacheType;
import uk.co.mpcontracting.rpmjukebox.support.HashGenerator;

public class CacheManagerTest extends AbstractTest {

	@Autowired
	private CacheManager cacheManager;

	@Mock
	private CacheType mockCacheType;
	
	@Mock
	private SettingsManager mockSettingsManager;
	
	@Mock
	private SystemSettings mockSystemSettings;
	
	private File cacheDirectory;
	private CacheManager spyCacheManager;
	
	@Before
	public void setup() {
		spyCacheManager = spy(cacheManager);
		
		ReflectionTestUtils.setField(spyCacheManager, "settingsManager", mockSettingsManager);
		
		cacheDirectory = (File)ReflectionTestUtils.getField(spyCacheManager, "cacheDirectory");
		cacheDirectory.mkdirs();

		when(mockSettingsManager.getSystemSettings()).thenReturn(mockSystemSettings);
		when(mockSystemSettings.getCacheSizeMb()).thenReturn(1);
	}
	
	@Test
	public void shouldReturnAValidInternalUrl() {
		String result = spyCacheManager.constructInternalUrl(CacheType.IMAGE, "12345", "http://www.example.com");

		assertThat("URL should be 'http://localhost:43125/cache?cacheType=IMAGE&id=12345&url=http%3A%2F%2Fwww.example.com'", result, 
				equalTo("http://localhost:43125/cache?cacheType=IMAGE&id=12345&url=http%3A%2F%2Fwww.example.com"));
	}
	
	@Test(expected = RuntimeException.class)
	public void shouldThrowExceptionConstructingInternalUrl() {
		doThrow(new RuntimeException("CacheManagerTest.shouldThrowExceptionConstructingInternalUrl")).when(mockCacheType).toString();
		
		spyCacheManager.constructInternalUrl(mockCacheType, "12345", "http://www.example.com");
	}
	
	@Test
	public void shouldReadImageCache() throws Exception {
		CacheType cacheType = CacheType.IMAGE;
		String id = "12345";
		String cacheContent = "CacheManagerTest.shouldReadImageCache()";
		
		writeCacheFile(cacheType, id, cacheContent);
		
		File file = spyCacheManager.readCache(cacheType, id);
		String result = readCacheFile(file);
		
		assertThat("Cache content should be '" + cacheContent + "'", result, equalTo(cacheContent));
	}
	
	@Test
	public void shouldReadTrackCache() throws Exception {
		CacheType cacheType = CacheType.TRACK;
		String id = "12345";
		String cacheContent = "CacheManagerTest.shouldReadTrackCache()";
		
		writeCacheFile(cacheType, id, cacheContent);
		
		File file = spyCacheManager.readCache(cacheType, id);
		String result = readCacheFile(file);
		
		assertThat("Cache content should be '" + cacheContent + "'", result, equalTo(cacheContent));
	}

	@Test
	public void shouldWriteImageCache() throws Exception {
		CacheType cacheType = CacheType.IMAGE;
		String id = "12345";
		String cacheContent = "CacheManagerTest.shouldWriteImageCache()";
		
		spyCacheManager.writeCache(cacheType, id, cacheContent.getBytes());
		
		File file = new File(cacheDirectory, HashGenerator.generateHash(id));
		String result = readCacheFile(file);
		
		assertThat("Cache content should be '" + cacheContent + "'", result, equalTo(cacheContent));
	}
	
	@Test
	public void shouldWriteTrackCache() throws Exception {
		CacheType cacheType = CacheType.TRACK;
		String id = "12345";
		String cacheContent = "CacheManagerTest.shouldWriteTrackCache()";
		
		spyCacheManager.writeCache(cacheType, id, cacheContent.getBytes());
		
		File file = new File(cacheDirectory, id);
		String result = readCacheFile(file);
		
		assertThat("Cache content should be '" + cacheContent + "'", result, equalTo(cacheContent));
	}
	
	@Test
	public void shouldReturnCacheMiss() {
		File file = spyCacheManager.readCache(CacheType.IMAGE, "12345");
		
		assertThat("Cache file should be null", file, nullValue());
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

		spyCacheManager.writeCache(cacheType, id, cacheContent.getBytes());
		
		File newFile = new File(cacheDirectory, id);

		assertThat("Cached file should have a newer modified timestamp", newFile.lastModified() > originalModified, equalTo(true));
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

		spyCacheManager.writeCache(cacheType, id, cacheContent.getBytes());
		
		File cachedFile = spyCacheManager.readCache(cacheType, id);

		assertThat("Cached file directory should have 21 files", cacheDirectory.listFiles().length, equalTo(21));
		assertThat("Cached file exists", cachedFile, notNullValue());
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

		spyCacheManager.writeCache(cacheType, id, cacheContent.getBytes());
		
		File cachedFile = spyCacheManager.readCache(cacheType, id);

		assertThat("Cached file directory should have 21 files", cacheDirectory.listFiles().length, equalTo(21));
		assertThat("Cached file exists", cachedFile, notNullValue());
	}

	private void writeCacheFile(CacheType cacheType, String id, String cacheContent) throws Exception {
		File file = new File(cacheDirectory, (cacheType == CacheType.TRACK ? id : HashGenerator.generateHash(id)));
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			outputStream.write(cacheContent.getBytes());
		}
	}
	
	private String readCacheFile(File file) throws Exception {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			reader.lines().forEach(line -> {
				builder.append(line);
			});
		}
		
		return builder.toString();
	}
}
