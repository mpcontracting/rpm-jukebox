package uk.co.mpcontracting.rpmjukebox.support;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractTest;

public class ThreadRunnerTest extends AbstractTest {

    @Autowired
    private ThreadRunner threadRunner;

    private CountDownLatch latch;
    private String threadName;

    @Before
    public void setup() {
        latch = new CountDownLatch(1);
        threadName = null;
    }

    @Test
    public void shouldRunOffGuiThread() throws Exception {
        threadRunner.run(() -> {
            threadName = Thread.currentThread().getName();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat("Thread name should not contain 'JavaFX'", threadName.contains("JavaFX"), equalTo(false));
    }

    @Test
    public void shouldRunOnGuiThread() throws Exception {
        threadRunner.runOnGui(() -> {
            threadName = Thread.currentThread().getName();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat("Thread name should contain 'JavaFX'", threadName.contains("JavaFX"), equalTo(true));
    }
}
