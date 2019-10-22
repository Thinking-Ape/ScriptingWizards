package main.model;

import main.parser.JSONParser;
import main.utility.Util;
import main.view.View;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.*;

public class Model implements PropertyChangeListener {
    private Set<Level> levelSet;
    private Level currentEditedLevel;
    private int currentLevelIndex;
    private List<Level> finishedLevelsList;
    private PropertyChangeSupport changeSupport;
    private List<String> unlockedStatementsList;

    public Model(){
        levelSet = new HashSet<>();
        finishedLevelsList = new ArrayList<>();
        currentLevelIndex = 0;
        changeSupport = new PropertyChangeSupport(this);
    }

    public Level getCurrentLevel() {
        for(Level l : levelSet){
         if(l.getIndex() == currentLevelIndex)return l;
        }
        return null;
    }

    public void addLevel(Level level) {
        levelSet.add(level);
    }

//    public void setCurrentIndexLevel(int i) {
//        currentLevelIndex = i;
//    }

    public void selectLevel(String name) {
        if(getCurrentLevel()!=null)getCurrentLevel().removeAllListener();
        int oldLevelIndex = currentLevelIndex;
        for(Level l : levelSet)
            //l.clearListeners();
        if(l.getName().equals(name)){
            currentLevelIndex=l.getIndex();
            getCurrentLevel().addChangeListener(this);

            try {
                getCurrentLevel().setUnlockedStatementList(JSONParser.getUnlockedStatementList());
            } catch (IOException e) {
                e.printStackTrace();
            }
            changeSupport.firePropertyChange("level", oldLevelIndex, currentLevelIndex);
            return;
        }
    }

    public void removeLevel(Level currentLevel) throws IOException, IllegalAccessException {
        int index = currentLevel.getIndex();
        levelSet.remove(currentLevel);
        JSONParser.removeLevelFromData(currentLevel.getName());
        for(Level l : levelSet){
            if(l.getIndex() > index){
                l.setIndex(l.getIndex()-1);
            }
            if(Util.listContains(l.getRequiredLevels(), currentLevel.getName()))
            l.getRequiredLevels().remove(currentLevel.getName());
        }
        currentLevelIndex--;
        changeSupport.firePropertyChange("level", currentLevel, getCurrentLevel());
    }

    public int getAmountOfLevels() {
        return levelSet.size();
    }

    //TODO: delete! or change
    public int getIndexOfLevelInList(String levelName) {
        for(Level l : levelSet){
            if(l.getName().equals(levelName))return l.getIndex();
        }
        return -1;
    }

    public void updateFinishedList() throws IOException {
        finishedLevelsList.add(getCurrentLevel());
        for(Level l : levelSet){
            int foundLevels = 0;
            for(String requiredLevelName : l.getRequiredLevels()){
                for(Level fL : finishedLevelsList)if(fL.getName().equals(requiredLevelName))foundLevels++;
            }
            if(foundLevels == l.getRequiredLevels().size())JSONParser.updateUnlocks(l);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("name")){
            for(Level l : levelSet){
                if(l.getRequiredLevels().contains(evt.getOldValue())){
                    l.getRequiredLevels().remove(evt.getOldValue());
                    l.getRequiredLevels().add((String)evt.getNewValue());
                    try {
                        JSONParser.saveRequiredLevels(l);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
       changeSupport.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        //TODO: updateUnlocks if things changed??
    }

    public void addChangeListener(View view) {
        changeSupport.addPropertyChangeListener(view);
    }

    public Level getLevelWithName(String s) {
        for(Level l : levelSet){
            if(l.getName().toLowerCase().equals(s.toLowerCase()))return l;
        }
        return null;
    }

    public void replaceLevel(String name, Level newLevel) {
        Level levelToRemove = null;
        for(Level l : levelSet){
            if(l.getName().equals(name)){
                levelToRemove = l;
                break;
            }
        }
        if(levelToRemove == null) throw new IllegalStateException("Level "+name+ " doesnt exist!" );
        levelSet.remove(levelToRemove);
        levelToRemove.removeAllListener();
        levelSet.add(newLevel);
        newLevel.setIndex(currentLevelIndex);
        newLevel.addChangeListener(this);
        changeSupport.firePropertyChange("level", null, getCurrentLevel());
//        for(int i = 0; i < levelSet.size(); i++){
//            if(levelSet.get(i).getName().equals(name)){
//                Level currentLevel = levelSet.get(i);
//                levelSet.replace(i, newLevel);
//            }
//        }
    }

//    public Level getLevelWithIndex(int i) {
//        if(levelSet.get(i-1).getIndex()!=i-1)throw new IllegalStateException("Level Index does not equal its position in the Level List minus 1!");
//        return levelSet.get(i-1);
//    }

    public void moveCurrentLevelDown() throws IOException {

        getCurrentLevel().getRequiredLevels().remove(getLevelWithIndex(currentLevelIndex-1).getName());
        currentLevelIndex--;

        for(Level l : levelSet){
            if(l.getIndex() == currentLevelIndex)l.setIndex(currentLevelIndex+1);
            else if(l.getIndex() == currentLevelIndex+1)l.setIndex(currentLevelIndex);
        }
//        Level oldLevel = levelSet.get(currentLevelIndex-1);
    }

    public Level getLevelWithIndex(int i) {
        for(Level l : levelSet){
            if(l.getIndex() == i) return l;
        }
        return null;
    }

    public void moveCurrentLevelUp() throws IOException {
        currentLevelIndex++;
        getCurrentLevel().getRequiredLevels().remove(getLevelWithIndex(currentLevelIndex-1).getName());
        for(Level l : levelSet){
            if(l.getIndex() == currentLevelIndex)l.setIndex(currentLevelIndex-1);
            else if(l.getIndex() == currentLevelIndex-1)l.setIndex(currentLevelIndex);
        }
//        Level oldLevel = levelSet.get(currentLevelIndex+1);

    }

    public List<Level> getLevelListCopy() {
        List<Level> output = new ArrayList<>(levelSet);

        return output;
    }
}
