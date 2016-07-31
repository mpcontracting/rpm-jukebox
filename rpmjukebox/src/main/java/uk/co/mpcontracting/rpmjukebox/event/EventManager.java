package uk.co.mpcontracting.rpmjukebox.event;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import lombok.Synchronized;

public class EventManager {
    private static EventManager instance;
    
    private List<EventListener> eventListeners;
    
    private EventManager() {
        eventListeners = new ArrayList<EventListener>();
    }
    
    public static EventManager getInstance() {
        if (instance == null) {
            initialise();
        }
        
        return instance;
    }
    
    @Synchronized
    private static void initialise() {
        if (instance == null) {
            instance = new EventManager();
        }
    }
    
    public void addEventListener(EventListener eventListener) {
        eventListeners.add(eventListener);
    }
    
    public void fireEvent(final Event event, final Object... payload) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (EventListener eventListener : eventListeners) {
                    eventListener.eventReceived(event, payload);
                }
            }
        });
    }
}
