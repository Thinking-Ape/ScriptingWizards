package main.model;

public interface LevelChangeListener {

    void updateAccordingToChanges(LevelChange levelChange);
    void changesUndone();
    void updateAll();
}
