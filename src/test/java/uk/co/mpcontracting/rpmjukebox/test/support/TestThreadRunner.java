package uk.co.mpcontracting.rpmjukebox.test.support;

import uk.co.mpcontracting.rpmjukebox.support.ThreadRunner;

import java.util.concurrent.ExecutorService;

public class TestThreadRunner extends ThreadRunner {

    public TestThreadRunner(ExecutorService executorService) {
        super(executorService);
    }

    @Override
    public void runOnGui(Runnable runnable) {
        new Thread(runnable).start();
    }
}
