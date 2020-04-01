package main.utility;

public class SimpleEventSender {
    private SimpleEventListener eventListener;

    public SimpleEventSender(SimpleEventListener eventListener) {
        this.eventListener=eventListener;
    }

    // make protected if this class is abstract
    public void notifyListeners(Object o){
        eventListener.update(o);
    }
    public void notifyListeners(){
        notifyListeners(null);
    }
}
