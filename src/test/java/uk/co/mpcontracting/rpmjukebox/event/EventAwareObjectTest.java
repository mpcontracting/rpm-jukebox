package uk.co.mpcontracting.rpmjukebox.event;

import org.junit.Before;
import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractEventTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TEST_EVENT;

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

        eventAwareObject.fireEvent(TEST_EVENT);

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(receivedEvent).isEqualTo(TEST_EVENT);
    }
}
