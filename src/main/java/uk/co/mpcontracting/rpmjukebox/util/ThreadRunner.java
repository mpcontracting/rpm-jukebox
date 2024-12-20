package uk.co.mpcontracting.rpmjukebox.util;

import java.util.concurrent.ExecutorService;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ThreadRunner {

  private final ExecutorService executorService;

  public ThreadRunner(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public void run(Runnable runnable) {
    executorService.submit(runnable);
  }

  public void runOnGui(Runnable runnable) {
    try {
      Platform.runLater(runnable);
    } catch (IllegalStateException e) {
      log.warn("JavaFX toolkit not initialized");
    }
  }
}
