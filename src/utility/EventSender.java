package utility;

import java.util.ArrayList;
import java.util.List;

public abstract class EventSender {
    private List<EventListener> eventListeners = new ArrayList<>();

    public void addListener(EventListener eventListener){
        this.eventListeners.add(eventListener);
    }
//    public void removeListener(EventListener eventListener){
//        this.eventListeners.remove(eventListener);
//    }
    protected void notifyListener(Event event){
        for(EventListener eventListener : eventListeners){
            eventListener.notify(event);
        }
    }

    public void clearListeners(){
        eventListeners = new ArrayList<>();
    }
}
