package main.controller.events;

public interface ControllerEventListener {

    void handleEvent(ControllerEvent event, Object data);
}
