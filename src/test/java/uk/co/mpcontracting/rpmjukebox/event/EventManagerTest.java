package uk.co.mpcontracting.rpmjukebox.event;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractEventTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TEST_EVENT;

public class EventManagerTest extends AbstractEventTest {

    private CountDownLatch latch;
    private Event receivedEvent;

    @Before
    public void setup() {
        latch = new CountDownLatch(1);
        receivedEvent = null;
    }

    @Test
    @SneakyThrows
    public void shouldFireEventOnAnEventListener() {
        EventManager eventManager = EventManager.getInstance();
        eventManager.addEventListener((event, payload) -> {
            receivedEvent = event;
            latch.countDown();
        });

        eventManager.fireEvent(Event.TEST_EVENT);

        latch.await(2000, TimeUnit.MILLISECONDS);

        assertThat(receivedEvent).isEqualTo(TEST_EVENT);
    }
}
