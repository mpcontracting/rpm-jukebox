package uk.co.mpcontracting.rpmjukebox.event;

import javafx.application.Platform;
import lombok.Synchronized;

import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private static EventManager instance;

    private List<EventListener> eventListeners;

    private EventManager() {
        eventListeners = new ArrayList<>();
    }

    public static EventManager getInstance() {
        if (instance == null) {
            initialise();
        }

        return instance;
    }

    @Synchronized
    private static void initialise() {
        instance = new EventManager();
    }

    void addEventListener(EventListener eventListener) {
        eventListeners.add(eventListener);
    }

    public void fireEvent(final Event event, final Object... payload) {
        Platform.runLater(() -> {
            eventListeners.forEach(eventListener -> eventListener.eventReceived(event, payload));
        });
    }
}
