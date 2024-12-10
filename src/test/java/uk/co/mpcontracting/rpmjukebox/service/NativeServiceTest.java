package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getConfigDirectory;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.getField;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;
import static uk.co.mpcontracting.rpmjukebox.util.OsType.LINUX;
import static uk.co.mpcontracting.rpmjukebox.util.OsType.OSX;
import static uk.co.mpcontracting.rpmjukebox.util.OsType.WINDOWS;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import uk.co.mpcontracting.rpmjukebox.model.Track;
import uk.co.mpcontracting.rpmjukebox.service.NativeService.NsUserNotificationsBridge;
import uk.co.mpcontracting.rpmjukebox.test.util.TestThreadRunner;
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;

@ExtendWith(MockitoExtension.class)
class NativeServiceTest {

  @Mock
  private SettingsService settingsService;

  @Mock
  private Track track;

  private final String nativeDylibLocation = "/native/NsUserNotificationsBridge.dylib";
  private final File nativeDirectory = new File(getConfigDirectory(), "native");
  private final ThreadRunner threadRunner = new TestThreadRunner(Executors.newSingleThreadExecutor());

  private NativeService underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new NativeService(threadRunner, settingsService);

    lenient().when(settingsService.getFileFromConfigDirectory("native")).thenReturn(nativeDirectory);
  }

  @AfterEach
  @SneakyThrows
  void afterEach() {
    if (nativeDirectory.exists()) {
      FileUtils.deleteDirectory(getConfigDirectory());
    }
  }

  @Test
  @SneakyThrows
  void shouldInitialiseNativeBridgeSuccessfullyOnOsx() {
    // This test only runs when being built on OSX
    if (System.getProperty("os.name").toLowerCase().contains("mac")) {
      when(settingsService.getOsType()).thenReturn(OSX);
      when(settingsService.getFileFromConfigDirectory(nativeDylibLocation)).thenReturn(new ClassPathResource(nativeDylibLocation).getFile());

      underTest.initialise();

      NsUserNotificationsBridge result = getField(underTest, "nsUserNotificationsBridge", NsUserNotificationsBridge.class);

      assertThat(result).isNotNull();
    }
  }

  @Test
  @SneakyThrows
  void shouldInitialiseNativeBridgeSuccessfullyOnOsxDirectoryExists() {
    // This test only runs when being built on OSX
    if (System.getProperty("os.name").toLowerCase().contains("mac")) {
      when(settingsService.getOsType()).thenReturn(OSX);
      when(settingsService.getFileFromConfigDirectory(nativeDylibLocation)).thenReturn(new ClassPathResource(nativeDylibLocation).getFile());

      if (!nativeDirectory.mkdirs()) {
        System.out.println("WARN : Unable to create native directory - " + nativeDirectory.getAbsolutePath());
      }

      underTest.initialise();

      NsUserNotificationsBridge result = getField(underTest, "nsUserNotificationsBridge", NsUserNotificationsBridge.class);

      assertThat(result).isNotNull();
    }
  }

  @Test
  void shouldNotInitialiseNativeBridgeOnWindows() {
    when(settingsService.getOsType()).thenReturn(WINDOWS);

    underTest.initialise();

    NsUserNotificationsBridge result = getField(underTest, "nsUserNotificationsBridge", NsUserNotificationsBridge.class);

    assertThat(result).isNull();
  }

  @Test
  void shouldNotInitialiseNativeBridgeOnLinux() {
    when(settingsService.getOsType()).thenReturn(LINUX);

    underTest.initialise();

    NsUserNotificationsBridge result = getField(underTest, "nsUserNotificationsBridge", NsUserNotificationsBridge.class);

    assertThat(result).isNull();
  }

  @Test
  @SneakyThrows
  void shouldDisplayNotificationOnOsx() {
    when(settingsService.getOsType()).thenReturn(OSX);

    AtomicBoolean notificationSent = new AtomicBoolean(false);
    CountDownLatch countDownLatch = new CountDownLatch(1);
    NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeOffset) -> {
      notificationSent.set(true);
      countDownLatch.countDown();
    };

    setField(underTest, "nsUserNotificationsBridge", nsUserNotificationsBridge);

    underTest.displayNotification(track);

    countDownLatch.await(2000, MILLISECONDS);

    assertThat(notificationSent.get()).isTrue();
  }

  @Test
  @SneakyThrows
  void shouldHandleExceptionInDisplayNotificationOnOsx() {
    when(settingsService.getOsType()).thenReturn(OSX);

    CountDownLatch countDownLatch = new CountDownLatch(1);
    NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeOffset) -> {
      throw new Error("NativeManagerTest.shouldHandleExceptionInDisplayNotificationOnOsx()");
    };

    setField(underTest, "nsUserNotificationsBridge", nsUserNotificationsBridge);

    underTest.displayNotification(track);

    countDownLatch.await(2000, MILLISECONDS);
  }

  @Test
  @SneakyThrows
  void shouldNotDisplayNotificationOnWindows() {
    when(settingsService.getOsType()).thenReturn(WINDOWS);

    AtomicBoolean notificationSent = new AtomicBoolean(false);
    CountDownLatch countDownLatch = new CountDownLatch(1);
    NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeOffset) -> {
      notificationSent.set(true);
      countDownLatch.countDown();
    };

    setField(underTest, "nsUserNotificationsBridge", nsUserNotificationsBridge);

    underTest.displayNotification(track);

    countDownLatch.await(2000, MILLISECONDS);

    assertThat(notificationSent.get()).isFalse();
  }

  @Test
  @SneakyThrows
  void shouldNotDisplayNotificationOnLinux() {
    when(settingsService.getOsType()).thenReturn(LINUX);

    AtomicBoolean notificationSent = new AtomicBoolean(false);
    CountDownLatch countDownLatch = new CountDownLatch(1);
    NsUserNotificationsBridge nsUserNotificationsBridge = (title, subtitle, text, timeOffset) -> {
      notificationSent.set(true);
      countDownLatch.countDown();
    };

    setField(underTest, "nsUserNotificationsBridge", nsUserNotificationsBridge);

    underTest.displayNotification(track);

    countDownLatch.await(2000, MILLISECONDS);

    assertThat(notificationSent.get()).isFalse();
  }
}