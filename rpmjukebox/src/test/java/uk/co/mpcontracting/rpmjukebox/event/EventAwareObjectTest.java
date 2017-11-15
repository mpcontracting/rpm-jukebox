package uk.co.mpcontracting.rpmjukebox.event;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class EventAwareObjectTest /*extends ApplicationTest*/  {

	private CountDownLatch latch;
	private Event receivedEvent;
	
	/*@Override
	public void start(Stage stage) throws Exception {

	}*/

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
		
		eventAwareObject.fireEvent(Event.APPLICATION_INITIALISED);
		
		latch.await(2000, TimeUnit.MILLISECONDS);
		
		assertThat("Received event should be " + Event.APPLICATION_INITIALISED, receivedEvent, equalTo(Event.APPLICATION_INITIALISED));
	}
}
