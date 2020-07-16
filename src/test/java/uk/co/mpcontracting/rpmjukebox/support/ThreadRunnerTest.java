package uk.co.mpcontracting.rpmjukebox.support;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractGUITest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class ThreadRunnerTest extends AbstractGUITest {

    @Autowired
    private ThreadRunner underTest;

    private CountDownLatch latch;
    private String threadName;

    @Before
    public void setup() {
        latch = new CountDownLatch(1);
        threadName = null;
    }

    @Test
    public void shouldRunOffGuiThread() throws Exception {
        underTest.run(() -> {
            threadName = Thread.currentThread().getName();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(threadName).doesNotContain("JavaFX");
    }

    @Test
    public void shouldRunOnGuiThread() throws Exception {
        underTest.runOnGui(() -> {
            threadName = Thread.currentThread().getName();
            latch.countDown();
        });

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(threadName).contains("JavaFX");
    }
}
