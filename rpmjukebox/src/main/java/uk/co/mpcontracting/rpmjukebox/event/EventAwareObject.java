package uk.co.mpcontracting.rpmjukebox.event;

public class EventAwareObject implements EventListener {
    private EventManager eventManager;
    
    protected EventAwareObject() {
        eventManager = EventManager.getInstance();
        eventManager.addEventListener(this);
    }

    protected void fireEvent(final Event event, final Object... payload) {
        eventManager.fireEvent(event, payload);
    }
    
    @Override
    public void eventReceived(Event event, Object... payload) {
        // Override in sub-class to receive events
    }
}
