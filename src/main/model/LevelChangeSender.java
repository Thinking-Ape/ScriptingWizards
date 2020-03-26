package main.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelChangeSender {

    private boolean levelNew = false;
    private LevelChangeListener levelChangeListener;
    private Map<LevelDataType,LevelChange> levelChangeMap = new HashMap<>();

    public LevelChangeSender(LevelChangeListener levelChangeListener){
        this.levelChangeListener = levelChangeListener;
    }

    public void addLevelChange(LevelChange change){
        LevelDataType levelDataType = change.getLevelDataType();
//        Object oldValue = change.getOldValue();
        if(levelChangeMap.containsKey(levelDataType)){
            Object oldValue = levelChangeMap.get(levelDataType).getOldValue();
            boolean isEqual =oldValue.equals(change.getNewValue());
            if(change.getLevelDataType()==LevelDataType.LOC_TO_STARS ||change.getLevelDataType()==LevelDataType.TURNS_TO_STARS){
                Integer[] oldV = (Integer[])oldValue;
                Integer[] newV = (Integer[])change.getNewValue();
                isEqual = oldV[0].equals(newV[0]) && oldV[1].equals(newV[1]);
            }
            if(isEqual){
                levelChangeMap.remove(levelDataType);
                if(levelChangeMap.size()==0)
                    levelChangeListener.changesUndone();
            }
            else levelChangeMap.put(levelDataType, new LevelChange(levelDataType,oldValue,change.getNewValue()));
        }
        else {
            levelChangeMap.put(levelDataType, change);
        }
        levelChangeListener.updateAccordingToChanges(change);
    }

    public Map<LevelDataType,LevelChange> getAndConfirmChanges(){
        levelNew = false;
        Map<LevelDataType,LevelChange> output = new HashMap<>(levelChangeMap);
        levelChangeMap.clear();
        levelChangeListener.changesUndone();
        return output;
    }

    public List<LevelChange> resetChanges(){
        List<LevelChange> output = new ArrayList<>();
        levelNew = false;
        for(LevelChange change : levelChangeMap.values()){
            LevelChange levelChange = new LevelChange(change.getLevelDataType(), change.getOldValue(), change.getOldValue());
            output.add(levelChange);
            levelChangeListener.updateAccordingToChanges(levelChange);
        }
        levelChangeMap.clear();
        levelChangeListener.changesUndone();
        return output;
    }

    public boolean hasChanged() {
        return levelChangeMap.size()>0;
    }

    public void wholeLevelChanged() {
        levelNew = true;
        levelChangeMap.clear();
        levelChangeListener.updateAll();
        levelChangeListener.changesUndone();
    }

    public boolean isLevelNew(){
        return levelNew;
    }

//    public void setListener(LevelChangeListener levelChangeListener){
//        this.levelChangeListener = levelChangeListener;
//    }
//
//    public void clearListener(){
//        levelChangeListener = null;
//    }

}
