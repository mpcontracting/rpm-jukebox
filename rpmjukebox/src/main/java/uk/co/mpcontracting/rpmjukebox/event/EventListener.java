package uk.co.mpcontracting.rpmjukebox.event;

public interface EventListener {
	void eventReceived(Event event, Object... payload);
}
