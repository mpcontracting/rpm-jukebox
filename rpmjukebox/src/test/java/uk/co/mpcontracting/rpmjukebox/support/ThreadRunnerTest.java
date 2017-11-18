package uk.co.mpcontracting.rpmjukebox.support;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import uk.co.mpcontracting.rpmjukebox.AbstractTest;

public class ThreadRunnerTest extends AbstractTest {

	private CountDownLatch latch;
	private String threadName;

	@Before
	public void setup() {
		latch = new CountDownLatch(1);
		threadName = null;
	}
	
	@Test
	public void shouldRunOffGuiThread() throws Exception {
		ThreadRunner.run(() -> {
			threadName = Thread.currentThread().getName();
			latch.countDown();
		});
		
		latch.await(2000, TimeUnit.MILLISECONDS);
		
		assertThat("Thread name should not contain 'JavaFX'", threadName.contains("JavaFX"), equalTo(false));
	}
	
	@Test
	public void shouldRunOnGuiThread() throws Exception {
		ThreadRunner.runOnGui(() -> {
			threadName = Thread.currentThread().getName();
			latch.countDown();
		});
		
		latch.await(2000, TimeUnit.MILLISECONDS);
		
		assertThat("Thread name should contain 'JavaFX'", threadName.contains("JavaFX"), equalTo(true));
	}
}
