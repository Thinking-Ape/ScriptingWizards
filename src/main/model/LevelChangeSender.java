package main.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelChangeSender {

    private LevelChangeListener levelChangeListener;
    private Map<LevelDataType,LevelChange> levelChangeMap = new HashMap<>();

    public LevelChangeSender(LevelChangeListener levelChangeListener){
        this.levelChangeListener = levelChangeListener;
    }

    public void addLevelChange(LevelChange change){
        LevelDataType levelDataType = change.getLevelDataType();
        Object oldValue = change.getOldValue();
        if(levelChangeMap.containsKey(levelDataType)){
            if(oldValue.equals(change.getNewValue())){
                levelChangeMap.remove(levelDataType);
                if(levelChangeMap.size()==0) levelChangeListener.changesUndone();
            }
            else levelChangeMap.put(levelDataType, new LevelChange(levelDataType,oldValue,change.getNewValue()));
        }
        else {
            levelChangeMap.put(levelDataType, change);
        }
        levelChangeListener.updateAccordingToChanges(change);
    }

    public Map<LevelDataType,LevelChange> getAndConfirmChanges(){
        Map<LevelDataType,LevelChange> output = new HashMap<>(levelChangeMap);
        levelChangeMap.clear();
        levelChangeListener.changesUndone();
        return output;
    }

    public void resetChanges(){
        for(LevelChange change : levelChangeMap.values()){
            levelChangeListener.updateAccordingToChanges(new LevelChange(change.getLevelDataType(), change.getOldValue(), change.getOldValue()));
        }
        levelChangeMap.clear();
        levelChangeListener.changesUndone();
    }

    public boolean hasChanged() {
        return levelChangeMap.size()>0;
    }

    public void wholeLevelChanged() {
        levelChangeMap.clear();
        levelChangeListener.updateAll();
        levelChangeListener.changesUndone();
    }

//    public void setListener(LevelChangeListener levelChangeListener){
//        this.levelChangeListener = levelChangeListener;
//    }
//
//    public void clearListener(){
//        levelChangeListener = null;
//    }

}
