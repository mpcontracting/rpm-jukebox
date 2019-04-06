package uk.co.mpcontracting.rpmjukebox.manager;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import uk.co.mpcontracting.rpmjukebox.manager.NativeManager.NsUserNotificationsBridge;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.support.OsType;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.test.support.TestThreadRunner;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.co.mpcontracting.rpmjukebox.test.support.TestHelper.getConfigDirectory;

@RunWith(MockitoJUnitRunner.class)
public class NativeManagerTest {

    @Mock
    private SettingsManager mockSettingsManager;

    @Mock
    private Track mockTrack;

    private NativeManager nativeManager;
    private String nativeDylibLocation = "/native/NsUserNotificationsBridge.dylib";
    private File nativeDirectory = new File(getConfigDirectory(), "native");
    private ThreadRunner threadRunner = new TestThreadRunner(Executors.newSingleThreadExecutor());

    @Before
    public void setup() {
        nativeManager = new NativeManager(threadRunner);
        nativeManager.wireSettingsManager(mockSettingsManager);

        when(mockSettingsManager.getFileFromConfigDirectory("native")).thenReturn(nativeDirectory);
    }

    @Test
    @SneakyThrows
    public void shouldInitialiseNativeBridgeSuccessfullyOnOsx() {
        // This test only runs when being built on OSX
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            when(mockSettingsManager.getOsType()).thenReturn(OsType.OSX);
            when(mockSettingsManager.getFileFromConfigDirectory(nativeDylibLocation)).thenReturn(
                    new ClassPathResource(nativeDylibLocation).getFile());

            nativeManager.initialise();

            NsUserNotificationsBridge result = (NsUserNotificationsBridge) getField(nativeManager,
                    "nsUserNotificationsBridge");

            assertThat(result).isNotNull();
        }
    }

    @Test
    @SneakyThrows
    public void shouldInitialiseNativeBridgeSuccessfullyOnOsxDirectoryExists() {
        // This test only runs when being built on OSX
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            when(mockSettingsManager.getOsType()).thenReturn(OsType.OSX);
            when(mockSettingsManager.getFileFromConfigDirectory(nativeDylibLocation)).thenReturn(
                    new ClassPathResource(nativeDylibLocation).getFile());

            nativeDirectory.mkdirs();
            nativeManager.initialise();

            NsUserNotificationsBridge result = (NsUserNotificationsBridge) getField(nativeManager,
                    "nsUserNotificationsBridge");

            assertThat(result).isNotNull();
        }
    }

    @Test
    public void shouldNotInitialiseNativeBridgeOnWindows() {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.WINDOWS);

        nativeManager.initialise();

        NsUserNotificationsBridge result = (NsUserNotificationsBridge) getField(nativeManager,
                "nsUserNotificationsBridge");

        assertThat(result).isNull();
    }

    @Test
    public void shouldNotInitialiseNativeBridgeOnLinux() {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.LINUX);

        nativeManager.initialise();

        NsUserNotificationsBridge result = (NsUserNotificationsBridge) getField(nativeManager,
                "nsUserNotificationsBridge");

        assertThat(result).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldDisplayNotificationOnOsx() {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.OSX);

        AtomicBoolean notificationSent = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeoffset) -> {
            notificationSent.set(true);
            latch.countDown();
        };

        setField(nativeManager, "nsUserNotificationsBridge", nsUserNotificationsBridge);

        nativeManager.displayNotification(mockTrack);
        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(notificationSent.get()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldHandleExceptionInDisplayNotificationOnOsx() {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.OSX);

        CountDownLatch latch = new CountDownLatch(1);
        NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeoffset) -> {
            throw new Error("NativeManagerTest.shouldHandleExceptionInDisplayNotificationOnOsx()");
        };

        setField(nativeManager, "nsUserNotificationsBridge", nsUserNotificationsBridge);

        nativeManager.displayNotification(mockTrack);
        latch.await(500, TimeUnit.MILLISECONDS);
    }

    @Test
    @SneakyThrows
    public void shouldNotDisplayNotificationOnWindows() {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.WINDOWS);

        AtomicBoolean notificationSent = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeoffset) -> {
            notificationSent.set(true);
            latch.countDown();
        };

        setField(nativeManager, "nsUserNotificationsBridge", nsUserNotificationsBridge);

        nativeManager.displayNotification(mockTrack);
        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(notificationSent.get()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldNotDisplayNotificationOnLinux() {
        when(mockSettingsManager.getOsType()).thenReturn(OsType.LINUX);

        AtomicBoolean notificationSent = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeoffset) -> {
            notificationSent.set(true);
            latch.countDown();
        };

        setField(nativeManager, "nsUserNotificationsBridge", nsUserNotificationsBridge);

        nativeManager.displayNotification(mockTrack);
        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(notificationSent.get()).isFalse();
    }

    @After
    @SneakyThrows
    public void cleanup() {
        if (nativeDirectory.exists()) {
            FileUtils.deleteDirectory(getConfigDirectory());
        }
    }
}
