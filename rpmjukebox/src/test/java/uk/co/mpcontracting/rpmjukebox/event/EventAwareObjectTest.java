package uk.co.mpcontracting.rpmjukebox.event;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class EventAwareObjectTest extends AbstractEventTest {

	private CountDownLatch latch;
	private Event receivedEvent;

	@Before
	public void setup() {
		latch = new CountDownLatch(1);
		receivedEvent = null;
	}
	
	@Test
	public void shouldFireAnEventOnAnEventAwareObject() throws Exception {
		EventAwareObject eventAwareObject = new EventAwareObject() {
			@Override
			public void eventReceived(Event event, Object... payload) {
				receivedEvent = event;
				latch.countDown();
			}
		};
		
		eventAwareObject.fireEvent(Event.TEST_EVENT);
		
		latch.await(2000, TimeUnit.MILLISECONDS);
		
		assertThat("Received event should be " + Event.TEST_EVENT, receivedEvent, equalTo(Event.TEST_EVENT));
	}
}
