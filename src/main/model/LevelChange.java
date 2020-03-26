package main.model;

public class LevelChange {

    private final Object oldValue;
    private final LevelDataType levelDataType;
    private final Object newValue;

    public LevelChange(LevelDataType levelDataType, Object oldValue, Object newValue){
        this.levelDataType = levelDataType;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    public LevelDataType getLevelDataType() {
        return levelDataType;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }
}
