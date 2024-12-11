package uk.co.mpcontracting.rpmjukebox.event;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TEST_EVENT;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;

import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class EventProcessorTest extends AbstractGuiTest {

  private Event receivedEvent;
  private EventProcessor underTest;

  @BeforeEach
  void beforeEach() {
    underTest = new EventProcessor(Executors.newSingleThreadScheduledExecutor());
    receivedEvent = null;
  }

  @Test
  void shouldFireEventOnAnEventListener() {
    underTest.addEventListener((event, payload) -> receivedEvent = event);
    underTest.fireEvent(TEST_EVENT);

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(receivedEvent).isEqualTo(TEST_EVENT);
  }

  @Test
  @SneakyThrows
  void shouldFireDelayedEventOnAnEventListener() {
    long delay = getFaker().number().numberBetween(100, 500);

    underTest.addEventListener((event, payload) -> receivedEvent = event);
    underTest.fireDelayedEvent(TEST_EVENT, delay, MILLISECONDS);

    Thread.sleep(delay * 2);

    assertThat(receivedEvent).isEqualTo(TEST_EVENT);
  }
}