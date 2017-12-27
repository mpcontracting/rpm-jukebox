package uk.co.mpcontracting.rpmjukebox.manager;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import uk.co.mpcontracting.rpmjukebox.controller.MainPanelController;
import uk.co.mpcontracting.rpmjukebox.event.Event;
import uk.co.mpcontracting.rpmjukebox.event.EventAwareObject;
import uk.co.mpcontracting.rpmjukebox.jetty.JettyServer;
import uk.co.mpcontracting.rpmjukebox.support.Constants;
import uk.co.mpcontracting.rpmjukebox.support.OsType;
import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;
import uk.co.mpcontracting.rpmjukebox.view.AbstractModalView;

@Slf4j
@Component
public class ApplicationManager extends EventAwareObject implements ApplicationContextAware, Constants {

    @Autowired
    private MessageManager messageManager;

    @Autowired
    private SettingsManager settingsManager;

    @Autowired
    private SearchManager searchManager;

    @Autowired
    private MainPanelController mainPanelController;

    @Autowired
    private MediaManager mediaManager;

    @Autowired
    private JettyServer jettyServer;

    private ApplicationContext context;
    private Stage stage;
    private boolean isInitialised;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    public void start(Stage stage) {
        log.info("Starting application");

        // Initialise views
        context.getBeansOfType(AbstractModalView.class).forEach((name, view) -> {
            view.initialiseView();
        });

        this.stage = stage;

        stage.setTitle(messageManager.getMessage(MESSAGE_WINDOW_TITLE));

        // If this is Windows, add a window icon
        if (settingsManager.getOsType() == OsType.WINDOWS) {
            stage.getIcons().add(new Image(getClass().getResourceAsStream(IMAGE_WINDOW_ICON)));
        }

        // Load the window settings
        settingsManager.loadWindowSettings(stage);

        stage.show();
        stage.requestFocus();

        // Initialise data in a new thread for GUI responsiveness
        ThreadRunner.run(() -> {
            try {
                searchManager.initialise();
                settingsManager.loadUserSettings();

                mainPanelController.closeMessageView();

                fireEvent(Event.APPLICATION_INITIALISED);
                isInitialised = true;
            } catch (Exception e) {
                log.error("Error initialising data", e);
            }
        });
    }

    public void stop() {
        log.info("Stopping application");

        mediaManager.cleanUpResources();
        searchManager.shutdown();

        if (isInitialised) {
            settingsManager.saveWindowSettings(stage);
            settingsManager.saveUserSettings();
        }

        try {
            jettyServer.stop();
        } catch (Exception e) {
            log.error("Error shutting down application", e);
        }
    }

    public void shutdown() {
        log.info("Shutting down the application");

        SpringApplication.exit(context, () -> {
            return 0;
        });
    }
}
