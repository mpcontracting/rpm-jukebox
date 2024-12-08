package uk.co.mpcontracting.rpmjukebox.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventProcessor {
  private final ScheduledExecutorService scheduledExecutorService;

  private final List<EventListener> eventListeners = new ArrayList<>();

  public void addEventListener(EventListener eventListener) {
    eventListeners.add(eventListener);
  }

  public void fireEvent(Event event, Object... payload) {
    Platform.runLater(() ->
        eventListeners.forEach(eventListener -> eventListener.eventReceived(event, payload)));
  }

  public void fireDelayedEvent(Event event, long delay, TimeUnit timeUnit, Object... payload) {
    scheduledExecutorService.schedule(() -> fireEvent(event, payload), delay, timeUnit);
  }
}
