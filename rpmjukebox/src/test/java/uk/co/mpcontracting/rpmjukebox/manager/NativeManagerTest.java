package uk.co.mpcontracting.rpmjukebox.manager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import uk.co.mpcontracting.rpmjukebox.manager.NativeManager.NsUserNotificationsBridge;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.OsType;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class NativeManagerTest extends AbstractTest {

	@Autowired
	private NativeManager nativeManager;
	
	@Autowired
	private SettingsManager settingsManager;

	@Mock
	private Track mockTrack;

	private NativeManager spyNativeManager;
	private SettingsManager spySettingsManager;

	@Before
	public void setup() {
		spyNativeManager = spy(nativeManager);
		spySettingsManager = spy(settingsManager);
		ReflectionTestUtils.setField(spyNativeManager, "settingsManager", spySettingsManager);
	}

	@Test
	public void shouldInitialiseNativeBridgeSuccessfullyOnOsx() throws Exception {
		// This test only runs when being built on OSX
		if (settingsManager.getOsType() == OsType.OSX) {
			when(spySettingsManager.getOsType()).thenReturn(OsType.OSX);

			ReflectionTestUtils.setField(spyNativeManager, "nsUserNotificationsBridge", null);
			spyNativeManager.afterPropertiesSet();
			NsUserNotificationsBridge result = (NsUserNotificationsBridge)ReflectionTestUtils.getField(spyNativeManager, "nsUserNotificationsBridge");
			
			assertThat("NSUserNotificationsBridge should not be null", result, notNullValue());
		}
	}
	
	@Test
	public void shouldInitialiseNativeBridgeSuccessfullyOnOsxDirectoryExists() throws Exception {
		// This test only runs when being built on OSX
		if (settingsManager.getOsType() == OsType.OSX) {
			settingsManager.getFileFromConfigDirectory("native").mkdirs();

			when(spySettingsManager.getOsType()).thenReturn(OsType.OSX);

			ReflectionTestUtils.setField(spyNativeManager, "nsUserNotificationsBridge", null);
			spyNativeManager.afterPropertiesSet();
			NsUserNotificationsBridge result = (NsUserNotificationsBridge)ReflectionTestUtils.getField(spyNativeManager, "nsUserNotificationsBridge");
			
			assertThat("NSUserNotificationsBridge should not be null", result, notNullValue());
		}
	}
	
	@Test
	public void shouldNotInitialiseNativeBridgeOnWindows() throws Exception {
		when(spySettingsManager.getOsType()).thenReturn(OsType.WINDOWS);

		ReflectionTestUtils.setField(spyNativeManager, "nsUserNotificationsBridge", null);
		spyNativeManager.afterPropertiesSet();
		NsUserNotificationsBridge result = (NsUserNotificationsBridge)ReflectionTestUtils.getField(spyNativeManager, "nsUserNotificationsBridge");
		
		assertThat("NSUserNotificationsBridge should be null", result, nullValue());
	}
	
	@Test
	public void shouldNotInitialiseNativeBridgeOnLinux() throws Exception {
		when(spySettingsManager.getOsType()).thenReturn(OsType.LINUX);

		ReflectionTestUtils.setField(spyNativeManager, "nsUserNotificationsBridge", null);
		spyNativeManager.afterPropertiesSet();
		NsUserNotificationsBridge result = (NsUserNotificationsBridge)ReflectionTestUtils.getField(spyNativeManager, "nsUserNotificationsBridge");
		
		assertThat("NSUserNotificationsBridge should be null", result, nullValue());
	}
	
	@Test
	public void shouldDisplayNotificationOnOsx() throws Exception {
		when(spySettingsManager.getOsType()).thenReturn(OsType.OSX);
		
		AtomicBoolean notificationSent = new AtomicBoolean(false);
		CountDownLatch latch = new CountDownLatch(1);
		NsUserNotificationsBridge nsUserNotificationsBridge = new NsUserNotificationsBridge() {
			@Override
			public int sendNotification(String title, String subtitle, String text, int timeoffset) {
				notificationSent.set(true);
				latch.countDown();
				
				return 0;
			}
		};

		ReflectionTestUtils.setField(spyNativeManager, "nsUserNotificationsBridge", nsUserNotificationsBridge);
		
		spyNativeManager.displayNotification(mockTrack);
		latch.await(2000, TimeUnit.MILLISECONDS);
		
		assertThat("Notification should have been displayed", notificationSent.get(), equalTo(true));
	}
	
	@Test
	public void shouldHandleExceptionInDisplayNotificationOnOsx() throws Exception {
		when(spySettingsManager.getOsType()).thenReturn(OsType.OSX);

		CountDownLatch latch = new CountDownLatch(1);
		NsUserNotificationsBridge nsUserNotificationsBridge = new NsUserNotificationsBridge() {
			@Override
			public int sendNotification(String title, String subtitle, String text, int timeoffset) {
				throw new Error("NativeManagerTest.shouldHandleExceptionInDisplayNotificationOnOsx()");
			}
		};

		ReflectionTestUtils.setField(spyNativeManager, "nsUserNotificationsBridge", nsUserNotificationsBridge);
		
		spyNativeManager.displayNotification(mockTrack);
		latch.await(500, TimeUnit.MILLISECONDS);
	}
	
	@Test
	public void shouldNotDisplayNotificationOnWindows() throws Exception {
		when(spySettingsManager.getOsType()).thenReturn(OsType.WINDOWS);
		
		AtomicBoolean notificationSent = new AtomicBoolean(false);
		CountDownLatch latch = new CountDownLatch(1);
		NsUserNotificationsBridge nsUserNotificationsBridge = new NsUserNotificationsBridge() {
			@Override
			public int sendNotification(String title, String subtitle, String text, int timeoffset) {
				notificationSent.set(true);
				latch.countDown();
				
				return 0;
			}
		};

		ReflectionTestUtils.setField(spyNativeManager, "nsUserNotificationsBridge", nsUserNotificationsBridge);
		
		spyNativeManager.displayNotification(mockTrack);
		latch.await(2000, TimeUnit.MILLISECONDS);
		
		assertThat("Notification should not have been displayed", notificationSent.get(), equalTo(false));
	}
	
	@Test
	public void shouldNotDisplayNotificationOnLinux() throws Exception {
		when(spySettingsManager.getOsType()).thenReturn(OsType.LINUX);
		
		AtomicBoolean notificationSent = new AtomicBoolean(false);
		CountDownLatch latch = new CountDownLatch(1);
		NsUserNotificationsBridge nsUserNotificationsBridge = new NsUserNotificationsBridge() {
			@Override
			public int sendNotification(String title, String subtitle, String text, int timeoffset) {
				notificationSent.set(true);
				latch.countDown();
				
				return 0;
			}
		};

		ReflectionTestUtils.setField(spyNativeManager, "nsUserNotificationsBridge", nsUserNotificationsBridge);
		
		spyNativeManager.displayNotification(mockTrack);
		latch.await(2000, TimeUnit.MILLISECONDS);
		
		assertThat("Notification should not have been displayed", notificationSent.get(), equalTo(false));
	}
}
