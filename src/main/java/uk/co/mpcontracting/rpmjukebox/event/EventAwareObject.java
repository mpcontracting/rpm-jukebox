package uk.co.mpcontracting.rpmjukebox.event;

import static lombok.AccessLevel.PROTECTED;

import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class EventAwareObject implements ApplicationContextAware, EventListener {

  @Getter(PROTECTED)
  private ApplicationContext applicationContext;
  private EventProcessor eventProcessor;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;

    eventProcessor = applicationContext.getBean(EventProcessor.class);
    eventProcessor.addEventListener(this);
  }

  protected void fireEvent(final Event event, final Object... payload) {
    eventProcessor.fireEvent(event, payload);
  }

  protected void fireDelayedEvent(final Event event, final long delay, final TimeUnit timeUnit, final Object... payload) {
    eventProcessor.fireDelayedEvent(event, delay, timeUnit, payload);
  }

  @Override
  public void eventReceived(Event event, Object... payload) {
    // Override in subclass to receive events
  }
}
