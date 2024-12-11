package uk.co.mpcontracting.rpmjukebox.event;

@FunctionalInterface
public interface EventListener {
  void eventReceived(Event event, Object... payload);
}
