package uk.co.mpcontracting.rpmjukebox.event;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static uk.co.mpcontracting.rpmjukebox.event.Event.TEST_EVENT;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestDataHelper.getFaker;
import static uk.co.mpcontracting.rpmjukebox.test.util.TestHelper.setField;

import java.util.concurrent.Executors;
import lombok.SneakyThrows;
import lombok.Synchronized;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;
import uk.co.mpcontracting.rpmjukebox.test.util.AbstractGuiTest;

class EventAwareObjectTest extends AbstractGuiTest {

  private EventProcessor testEventProcessor;
  private EventAwareObject underTest;

  private Event receivedEvent;

  @BeforeEach
  void beforeEach() {
    testEventProcessor = spy(new EventProcessor(Executors.newSingleThreadScheduledExecutor()));
    underTest = new EventAwareObject() {
      @Override
      @Synchronized
      protected EventProcessor getEventProcessor() {
        testEventProcessor.addEventListener(this);

        return testEventProcessor;
      }

      @Override
      public void eventReceived(Event event, Object... payload) {
        receivedEvent = event;
      }
    };

    receivedEvent = null;
  }

  @Test
  void shouldRunPostConstruct() {
    setField(underTest, "eventProcessor", eventProcessor);

    underTest.postConstruct();

    verify(eventProcessor).addEventListener(underTest);
  }

  @Test
  void shouldFireEventOnAnEventAwareObject() {
    underTest.fireEvent(TEST_EVENT);

    WaitForAsyncUtils.waitForFxEvents();

    assertThat(receivedEvent).isEqualTo(TEST_EVENT);
  }

  @Test
  @SneakyThrows
  void shouldFireDelayedEventOnAnEventAwareObject() {
    long delay = getFaker().number().numberBetween(100, 500);

    underTest.fireDelayedEvent(TEST_EVENT, delay, MILLISECONDS);

    Thread.sleep(delay * 2);

    assertThat(receivedEvent).isEqualTo(TEST_EVENT);
  }
}