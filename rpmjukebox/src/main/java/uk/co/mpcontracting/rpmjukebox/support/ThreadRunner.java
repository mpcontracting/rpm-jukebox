package uk.co.mpcontracting.rpmjukebox.support;

import javafx.application.Platform;

public abstract class ThreadRunner {

    private ThreadRunner() {
    }

    public static void run(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static void runOnGui(Runnable runnable) {
        Platform.runLater(runnable);
    }
}
