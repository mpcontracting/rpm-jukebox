package uk.co.mpcontracting.rpmjukebox.test.util;

import java.util.concurrent.ExecutorService;
import uk.co.mpcontracting.rpmjukebox.util.ThreadRunner;

public class TestThreadRunner extends ThreadRunner {

  public TestThreadRunner(ExecutorService executorService) {
    super(executorService);
  }

  @Override
  public void runOnGui(Runnable runnable) {
    new Thread(runnable).start();
  }
}
