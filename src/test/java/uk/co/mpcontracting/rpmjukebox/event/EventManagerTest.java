package uk.co.mpcontracting.rpmjukebox.event;

import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.test.support.AbstractEventTest;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TEST_EVENT;

public class EventManagerTest extends AbstractEventTest {

    private Event receivedEvent;

    @Before
    public void setup() {
        receivedEvent = null;
    }

    @Test
    @SneakyThrows
    public void shouldFireEventOnAnEventListener() {
        EventManager underTest = EventManager.getInstance();
        underTest.addEventListener((event, payload) -> receivedEvent = event);

        underTest.fireEvent(Event.TEST_EVENT);

        WaitForAsyncUtils.waitForFxEvents();

        assertThat(receivedEvent).isEqualTo(TEST_EVENT);
    }
}
