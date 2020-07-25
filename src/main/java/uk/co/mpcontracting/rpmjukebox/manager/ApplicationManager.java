package uk.co.mpcontracting.rpmjukebox.manager;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import uk.co.mpcontracting.rpmjukebox.RpmJukebox;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.jetty.JettyServer;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.OsType;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.view.AbstractModalView;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationManager extends EventAwareObject implements ApplicationContextAware, Constants {

    private final ThreadRunner threadRunner;
    private final Environment environment;
    private final RpmJukebox rpmJukebox;
    private final MessageManager messageManager;

    private JettyServer jettyServer;
    private SettingsManager settingsManager;
    private SearchManager searchManager;
    private MediaManager mediaManager;

    private ApplicationContext context;
    private Stage stage;
    private boolean isInitialised;

    @Autowired
    public void wireJettyServer(JettyServer jettyServer) {
        this.jettyServer = jettyServer;
    }

    @Autowired
    public void wireSettingsManager(SettingsManager settingsManager) {
        this.settingsManager = settingsManager;
    }

    @Autowired
    public void wireSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    @Autowired
    public void wireMediaManager(MediaManager mediaManager) {
        this.mediaManager = mediaManager;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @SneakyThrows
    @EventListener(ContextRefreshedEvent.class)
    public void initialise() {
        if (Arrays.stream(environment.getActiveProfiles()).noneMatch("test"::equals)) {
            searchManager.initialise();

            rpmJukebox.updateSplashProgress(messageManager.getMessage(MESSAGE_SPLASH_LOADING_USER_SETTINGS));

            settingsManager.loadUserSettings();

            // Initialise views on UI thread
            rpmJukebox.updateSplashProgress(messageManager.getMessage(MESSAGE_SPLASH_INITIALISING_VIEWS));
            CountDownLatch latch = new CountDownLatch(1);

            threadRunner.runOnGui(() -> {
                context.getBeansOfType(AbstractModalView.class).forEach((name, view) -> view.initialise());

                latch.countDown();
            });

            latch.await();
        }
    }

    public void start(Stage stage) {
        log.info("Starting application");

        this.stage = stage;

        stage.setTitle(messageManager.getMessage(MESSAGE_WINDOW_TITLE));

        // If this is Windows, add a window icon
        if (settingsManager.getOsType() == OsType.WINDOWS) {
            stage.getIcons().add(new Image(getClass().getResourceAsStream(IMAGE_WINDOW_ICON)));
        }

        // Load the window settings
        settingsManager.loadWindowSettings(stage);

        fireEvent(Event.APPLICATION_INITIALISED);
        isInitialised = true;
    }

    public void stop() {
        log.info("Stopping application");

        mediaManager.cleanUpResources();
        searchManager.shutdown();

        if (isInitialised) {
            settingsManager.saveWindowSettings(stage);
            settingsManager.saveUserSettings();
            settingsManager.saveSystemSettings();
        }

        try {
            jettyServer.stop();
        } catch (Exception e) {
            log.error("Error shutting down application", e);
        }
    }

    public void shutdown() {
        log.info("Shutting down the application");

        if (context != null) {
            SpringApplication.exit(context, () -> 0);
        } else {
            System.exit(0);
        }
    }
}
