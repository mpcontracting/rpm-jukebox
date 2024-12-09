package uk.co.mpcontracting.rpmjukebox.util;

import java.util.concurrent.ExecutorService;

public class ThreadRunner {

  private final ExecutorService executorService;

  public ThreadRunner(ExecutorService executorService) {
    this.executorService = executorService;
  }

  public void run(Runnable runnable) {
    executorService.submit(runnable);
  }
}
