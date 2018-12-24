package uk.co.mpcontracting.rpmjukebox.support;

import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ThreadRunner {

    private ThreadRunner() {
    }

    public static void run(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static void runOnGui(Runnable runnable) {
        try {
            Platform.runLater(runnable);
        } catch (IllegalStateException e) {
            log.warn("JavaFX toolkit not initialized");
        }
    }
}
