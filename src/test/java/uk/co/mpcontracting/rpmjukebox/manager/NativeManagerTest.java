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
    private SettingsManager settingsManager;

    @Mock
    private Track track;

    private final String nativeDylibLocation = "/native/NsUserNotificationsBridge.dylib";
    private final File nativeDirectory = new File(getConfigDirectory(), "native");
    private final ThreadRunner threadRunner = new TestThreadRunner(Executors.newSingleThreadExecutor());

    private NativeManager underTest;

    @Before
    public void setup() {
        underTest = new NativeManager(threadRunner);
        underTest.wireSettingsManager(settingsManager);

        when(settingsManager.getFileFromConfigDirectory("native")).thenReturn(nativeDirectory);
    }

    @Test
    @SneakyThrows
    public void shouldInitialiseNativeBridgeSuccessfullyOnOsx() {
        // This test only runs when being built on OSX
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            when(settingsManager.getOsType()).thenReturn(OsType.OSX);
            when(settingsManager.getFileFromConfigDirectory(nativeDylibLocation)).thenReturn(
                    new ClassPathResource(nativeDylibLocation).getFile());

            underTest.initialise();

            NsUserNotificationsBridge result = (NsUserNotificationsBridge) getField(underTest,
                    "nsUserNotificationsBridge");

            assertThat(result).isNotNull();
        }
    }

    @Test
    @SneakyThrows
    public void shouldInitialiseNativeBridgeSuccessfullyOnOsxDirectoryExists() {
        // This test only runs when being built on OSX
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            when(settingsManager.getOsType()).thenReturn(OsType.OSX);
            when(settingsManager.getFileFromConfigDirectory(nativeDylibLocation)).thenReturn(
                    new ClassPathResource(nativeDylibLocation).getFile());

            nativeDirectory.mkdirs();
            underTest.initialise();

            NsUserNotificationsBridge result = (NsUserNotificationsBridge) getField(underTest,
                    "nsUserNotificationsBridge");

            assertThat(result).isNotNull();
        }
    }

    @Test
    public void shouldNotInitialiseNativeBridgeOnWindows() {
        when(settingsManager.getOsType()).thenReturn(OsType.WINDOWS);

        underTest.initialise();

        NsUserNotificationsBridge result = (NsUserNotificationsBridge) getField(underTest,
                "nsUserNotificationsBridge");

        assertThat(result).isNull();
    }

    @Test
    public void shouldNotInitialiseNativeBridgeOnLinux() {
        when(settingsManager.getOsType()).thenReturn(OsType.LINUX);

        underTest.initialise();

        NsUserNotificationsBridge result = (NsUserNotificationsBridge) getField(underTest,
                "nsUserNotificationsBridge");

        assertThat(result).isNull();
    }

    @Test
    @SneakyThrows
    public void shouldDisplayNotificationOnOsx() {
        when(settingsManager.getOsType()).thenReturn(OsType.OSX);

        AtomicBoolean notificationSent = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeOffset) -> {
            notificationSent.set(true);
            latch.countDown();
        };

        setField(underTest, "nsUserNotificationsBridge", nsUserNotificationsBridge);

        underTest.displayNotification(track);
        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(notificationSent.get()).isTrue();
    }

    @Test
    @SneakyThrows
    public void shouldHandleExceptionInDisplayNotificationOnOsx() {
        when(settingsManager.getOsType()).thenReturn(OsType.OSX);

        CountDownLatch latch = new CountDownLatch(1);
        NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeOffset) -> {
            throw new Error("NativeManagerTest.shouldHandleExceptionInDisplayNotificationOnOsx()");
        };

        setField(underTest, "nsUserNotificationsBridge", nsUserNotificationsBridge);

        underTest.displayNotification(track);
        latch.await(500, TimeUnit.MILLISECONDS);
    }

    @Test
    @SneakyThrows
    public void shouldNotDisplayNotificationOnWindows() {
        when(settingsManager.getOsType()).thenReturn(OsType.WINDOWS);

        AtomicBoolean notificationSent = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeOffset) -> {
            notificationSent.set(true);
            latch.countDown();
        };

        setField(underTest, "nsUserNotificationsBridge", nsUserNotificationsBridge);

        underTest.displayNotification(track);
        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(notificationSent.get()).isFalse();
    }

    @Test
    @SneakyThrows
    public void shouldNotDisplayNotificationOnLinux() {
        when(settingsManager.getOsType()).thenReturn(OsType.LINUX);

        AtomicBoolean notificationSent = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeOffset) -> {
            notificationSent.set(true);
            latch.countDown();
        };

        setField(underTest, "nsUserNotificationsBridge", nsUserNotificationsBridge);

        underTest.displayNotification(track);
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
