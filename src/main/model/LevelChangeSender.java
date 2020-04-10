package main.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LevelChangeSender {

    private boolean levelNew = false;
    private LevelChangeListener levelChangeListener;
    private Map<LevelDataType,LevelChange> levelChangeMap = new HashMap<>();

    LevelChangeSender(LevelChangeListener levelChangeListener){
        this.levelChangeListener = levelChangeListener;
    }

    void addLevelChange(LevelChange change){
        LevelDataType levelDataType = change.getLevelDataType();
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
            }
            else levelChangeMap.put(levelDataType, new LevelChange(levelDataType,oldValue,change.getNewValue()));
        }
        else {
            levelChangeMap.put(levelDataType, change);
        }
        levelChangeListener.updateTemporaryChanges(change);
    }

    public Map<LevelDataType,LevelChange> getAndConfirmChanges(){
        Map<LevelDataType,LevelChange> output = new HashMap<>(levelChangeMap);
        levelChangeMap.clear();
        levelChangeListener.updateAll();
        return output;
    }

    public List<LevelChange> resetChanges(){
        List<LevelChange> output = new ArrayList<>();
        for(LevelChange change : levelChangeMap.values()){
            LevelChange levelChange = new LevelChange(change.getLevelDataType(), change.getOldValue(), change.getOldValue());
            output.add(levelChange);
            levelChangeListener.updateTemporaryChanges(levelChange);
        }
        levelChangeMap.clear();
        levelChangeListener.updateAll();
        return output;
    }

    public boolean hasChanged() {
        return levelChangeMap.size()>0;
    }

    public void levelChanged(boolean levelNew) {
        this.levelNew = levelNew;
        levelChangeMap.clear();
        levelChangeListener.updateAll();
    }

    public boolean isLevelNew(){
        return levelNew;
    }

    public void resetLevelNew(){
        levelNew = false;
        levelChangeListener.updateAll();
    }

//    public void setListener(LevelChangeListener levelChangeListener){
//        this.levelChangeListener = levelChangeListener;
//    }
//
//    public void clearListener(){
//        levelChangeListener = null;
//    }

}
