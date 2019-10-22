package main.controller.events;

import java.util.Set;

public class ControllerEventSender {

    Set<ControllerEventListener> listeners;

    public void fireEvent(ControllerEvent controllerEvent, Object data){
        for(ControllerEventListener cEL : listeners){
            cEL.handleEvent(controllerEvent, data);
        }
    }

    public void fireEvent(ControllerEvent controllerEvent){
        for(ControllerEventListener cEL : listeners){
            cEL.handleEvent(controllerEvent,null);
        }
    }
}
