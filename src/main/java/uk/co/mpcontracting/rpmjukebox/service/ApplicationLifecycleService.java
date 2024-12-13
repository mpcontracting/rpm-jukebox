package uk.co.mpcontracting.rpmjukebox.service;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static uk.co.mpcontracting.rpmjukebox.event.Event.APPLICATION_INITIALISED;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.IMAGE_WINDOW_ICON;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_INITIALISING_VIEWS;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_SPLASH_LOADING_USER_SETTINGS;
import static uk.co.mpcontracting.rpmjukebox.util.Constants.MESSAGE_WINDOW_TITLE;
import static uk.co.mpcontracting.rpmjukebox.util.OsType.WINDOWS;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.view.AbstractModalView;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationLifecycleService extends EventAwareObject implements ApplicationContextAware {

  private ApplicationContext applicationContext;
  private final Environment environment;

  private final ThreadRunner threadRunner;

  private final RpmJukebox rpmJukebox;

  private final MediaService mediaService;
  private final SearchService searchService;
  private final SettingsService settingsService;
  private final StringResourceService stringResourceService;

  private Stage stage;
  private boolean isInitialised;

  @Override
  public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @SneakyThrows
  @EventListener(ContextRefreshedEvent.class)
  public void initialise() {
    log.info("Initialising {}", getClass().getSimpleName());

    // Don't initialise anything if we're running tests
    if (Arrays.stream(environment.getActiveProfiles()).noneMatch("test"::equals)) {
      searchService.initialise();

      rpmJukebox.updateSplashProgress(stringResourceService.getString(MESSAGE_SPLASH_LOADING_USER_SETTINGS));

      settingsService.loadUserSettings();

      // Initialise modal views on UI thread
      rpmJukebox.updateSplashProgress(stringResourceService.getString(MESSAGE_SPLASH_INITIALISING_VIEWS));
      CountDownLatch countDownLatch = new CountDownLatch(1);

      threadRunner.runOnGui(() -> {
        applicationContext.getBeansOfType(AbstractModalView.class).forEach((name, view) -> view.initialise());
        countDownLatch.countDown();
      });

      countDownLatch.await();
    }
  }

  @SneakyThrows
  public void start(Stage stage) {
    log.info("Starting application");

    this.stage = stage;

    stage.setTitle(stringResourceService.getString(MESSAGE_WINDOW_TITLE));

    // If this is Windows, add a window icon
    if (settingsService.getOsType() == WINDOWS) {
      stage.getIcons().add(new Image(requireNonNull(getClass().getResourceAsStream(IMAGE_WINDOW_ICON))));
    }

    // Load the window settings
    settingsService.loadWindowSettings(stage);

    fireEvent(APPLICATION_INITIALISED);
    isInitialised = true;
  }

  public void stop() {
    log.info("Stopping application");

    mediaService.cleanUpResources();
    searchService.shutdown();

    if (isInitialised) {
      settingsService.saveWindowSettings(stage);
      settingsService.saveUserSettings();
      settingsService.saveSystemSettings();
    }
  }

  public void shutdown() {
    log.info("Shutting down the application");

    if (nonNull(applicationContext)) {
      SpringApplication.exit(applicationContext, () -> 0);
    } else {
      System.exit(0);
    }
  }
}
