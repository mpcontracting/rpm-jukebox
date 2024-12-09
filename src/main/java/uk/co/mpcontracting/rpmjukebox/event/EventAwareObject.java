package uk.co.mpcontracting.rpmjukebox.event;

import java.util.concurrent.TimeUnit;
import lombok.Synchronized;
import uk.co.mpcontracting.rpmjukebox.util.ContextHelper;

public class EventAwareObject implements EventListener {

  private EventProcessor eventProcessor;

  @Synchronized
  private EventProcessor getEventProcessor() {
    if (eventProcessor == null) {
      eventProcessor = ContextHelper.getBean(EventProcessor.class);
    }

    return eventProcessor;
  }

  protected void fireEvent(final Event event, final Object... payload) {
    getEventProcessor().fireEvent(event, payload);
  }

  protected void fireDelayedEvent(final Event event, final long delay, final TimeUnit timeUnit, final Object... payload) {
    getEventProcessor().fireDelayedEvent(event, delay, timeUnit, payload);
  }

  @Override
  public void eventReceived(Event event, Object... payload) {
    // Override in subclass to receive events
  }
}
