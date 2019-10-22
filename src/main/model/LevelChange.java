package main.model;

public class LevelChange {

    private final Object oldValue;
    private final ChangeType changeType;
    private final Object newValue;

    public LevelChange(ChangeType changeType, Object oldValue, Object newValue){
        this.changeType = changeType;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    public ChangeType getChangeType() {
        return changeType;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }
}
