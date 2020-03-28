package main.model;

public interface LevelChangeListener {

    void updateTemporaryChanges(LevelChange levelChange);
    void changesUndone();
    void updateAll();
}
