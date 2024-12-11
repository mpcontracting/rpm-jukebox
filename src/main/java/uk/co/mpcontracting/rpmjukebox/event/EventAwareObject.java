package uk.co.mpcontracting.rpmjukebox.event;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.mpcontracting.rpmjukebox.util.ContextHelper;

public abstract class EventAwareObject implements EventListener {

  @Autowired
  private EventProcessor eventProcessor;

  @PostConstruct
  public void postConstruct() {
    eventProcessor.addEventListener(this);
  }

  @Synchronized
  protected EventProcessor getEventProcessor() {
    if (eventProcessor == null) {
      eventProcessor = ContextHelper.getBean(EventProcessor.class);
      eventProcessor.addEventListener(this);
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
