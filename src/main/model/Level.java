package main.model;

import main.model.gamemap.Cell;
import main.model.gamemap.GameMap;
import main.model.statement.ComplexStatement;
import main.view.CodeAreaType;

import java.util.*;


public class Level {

    private List<String> tutorialMessages;
    private GameMap originalMap;
    private List<String> requiredLevels;
    private Integer[] locToStars; // actually int
    private Integer[] turnsToStars; // actually int
//    private StringProperty currentTutorialMessageProperty;
    private String name;
    private int maxKnights; // actually int
    private boolean isTutorial; // actually boolean
//    private StringProperty currentTutorialIndexProperty = new SimpleStringProperty(null,GameConstants.CURRENT_TUTORIAL_INDEX_PROPERTY_NAME,0+""); // actually int
    private ComplexStatement aiBehaviour;


    public Level(String name, Cell[][] originalArray, ComplexStatement aiBehaviour, Integer[] turnsToStars, Integer[] locToStars, String[] requiredLevels, int maxKnights,
                 boolean isTutorial, List<String> tutorialEntryList) {
        this.name =name;
        this.maxKnights = maxKnights;
        this.isTutorial = isTutorial;
//        this.indexProperty = new SimpleStringProperty(null,GameConstants.INDEX_PROPERTY_NAME,index+"");
        this.turnsToStars = turnsToStars;
        this.locToStars = locToStars;
//        this.currentTutorialIndexProperty = new SimpleStringProperty(null,GameConstants.CURRENT_TUTORIAL_INDEX_PROPERTY_NAME,""+0);
        this.originalMap = new GameMap(originalArray);
        this.aiBehaviour = aiBehaviour;
        this.requiredLevels = new ArrayList<>(Arrays.asList(requiredLevels));
//        this.changeSupport = new PropertyChangeSupport(this);
        this.tutorialMessages = new ArrayList<>();
        if(isTutorial){
            if(tutorialEntryList.size() > 0){
                tutorialMessages.addAll(tutorialEntryList);
            }

        }
    }

//    private Level (String name, GameMap originalMap, ComplexStatement aiBehaviour, Integer[]  turnsToStars, Integer[] locToStars, List<String> requiredLevels, int maxKnights,
//                  boolean isTutorial, List<String> tutorialEntryList) {
//        this.name = new SimpleStringProperty(null,GameConstants.LEVEL_NAME_PROPERTY_NAME, name);
//        this.maxKnights = new SimpleStringProperty(null,GameConstants.MAX_KNIGHTS_PROPERTY_NAME,maxKnights+"");
//        this.isTutorial = new SimpleStringProperty(null,GameConstants.IS_TUTORIAL_PROPERTY_NAME,isTutorial+"");
////        this.indexProperty = new SimpleStringProperty(null,GameConstants.INDEX_PROPERTY_NAME,index+"");
//        StringProperty turnsToStars3 = new SimpleStringProperty(null,GameConstants.TURNS_TO_STARS_3_PROPERTY_NAME,turnsToStars[1]+"");
//        StringProperty turnsToStars2 = new SimpleStringProperty(null,GameConstants.TURNS_TO_STARS_2_PROPERTY_NAME,turnsToStars[0]+"");
//        this.turnsToStars = new StringProperty[]{turnsToStars2,turnsToStars3};
//        StringProperty locToStars3 = new SimpleStringProperty(null,GameConstants.LOC_TO_STARS_3_PROPERTY_NAME,locToStars[1]+"");
//        StringProperty locToStars2 = new SimpleStringProperty(null,GameConstants.LOC_TO_STARS_2_PROPERTY_NAME,locToStars[0]+"");
//        this.locToStars = new StringProperty[]{locToStars2,locToStars3};
//        this.currentTutorialMessageProperty = new SimpleStringProperty(null,GameConstants.CURRENT_TUTORIAL_MESSAGE_PROPERTY_NAME,"");
//        this.currentTutorialIndexProperty = new SimpleStringProperty(null,GameConstants.CURRENT_TUTORIAL_INDEX_PROPERTY_NAME,""+0);
//        this.originalMap = originalMap;
//        this.currentMap = originalMap.copy();
//        this.turnsTaken = 0;
//        this.aiBehaviour = aiBehaviour;
//        this.requiredLevels = new ArrayList<>(requiredLevels);
//        this.tutorialMessages = new ArrayList<>();
//        if(isTutorial){
//            if(tutorialEntryList.size() > 0){
//                tutorialMessages.addAll(tutorialEntryList);
//                currentTutorialMessageProperty =
//                        new SimpleStringProperty(null,GameConstants.CURRENT_TUTORIAL_MESSAGE_PROPERTY_NAME, tutorialMessages.get(getCurrentTutorialMessageIndex()));
//            }
//        }
//    }





   /* //TODO:MOVE TO MODEL!
    public boolean isWon(){
        return CodeExecutor.hasWon();
    }
    //TODO: bad! REDO!
    public boolean isLost(){
        if(CodeExecutor.hasLost() || isLost)
        return true;
        else return false;
    }*/

    public ComplexStatement getAIBehaviourCopy() {
        return aiBehaviour.copy(CodeAreaType.AI);
    }

    public void setAiBehaviour(ComplexStatement aiBehaviour) {
        ComplexStatement oldAiBehaviour = this.aiBehaviour;
        this.aiBehaviour = aiBehaviour;
//        changeSupport.firePropertyChange(LevelDataType.AI_CODE.name(), oldAiBehaviour,aiBehaviour);
    }

    public void setName(String name) {
        this.name = name;
    }



    public boolean hasAi() {
        //TODO: add boolean value?
        return aiBehaviour.getStatementListSize() > 0;
    }

//    public int getIndex() {
//        return indexProperty;
//    }


    public GameMap getOriginalMapCopy(){
        return originalMap.copy();
    }


    public String getName() {
        return name;
    }

    public List<String> getRequiredLevelNamesCopy() {
        return new ArrayList<>(requiredLevels);
    }

    public Integer[] getLocToStarsCopy() {
        return new Integer[]{locToStars[0], locToStars[1]};
    }
    public Integer[] getTurnsToStarsCopy() {
        return new Integer[]{turnsToStars[0], turnsToStars[1]};
    }

    public void setLocToStars(Integer[] locToStars) {
        this.locToStars = locToStars;

    }

    public void setTurnsToStars(Integer[] turnsToStars) {
        this.turnsToStars = turnsToStars;
    }

    public void setRequiredLevels(List<String> requiredLevelNames) {
//        List<String> oldNames = new ArrayList<>(requiredLevelNames);
        this.requiredLevels = requiredLevelNames;
//        if(!Util.listsEqual(oldNames, requiredLevelNames))changeSupport.firePropertyChange(LevelDataType.REQUIRED_LEVELS.name(), oldNames, requiredLevelNames);
    }

    public int getMaxKnights() {
        return maxKnights;
    }

    public void setMaxKnights(int maxKnights) {
        this.maxKnights = maxKnights;
//        if(oldMaxKnights != maxKnights)changeSupport.firePropertyChange(LevelDataType.MAX_KNIGHTS.name(), oldMaxKnights, maxKnights);
    }

//    public void initLevelChangeListener(PropertyChangeListener pcl) {
//        changeSupport.addPropertyChangeListener(pcl);
//    }

    //TODO: WTF???
    /*public void setUnlockedStatementList(List<String> unlockedStatementList){
        CodeExecutor.setUnlockedStatementList(unlockedStatementList);
    }*/

//    //TODO:
//    @Override
//    public void propertyChange(PropertyChangeEvent evt) {
////        Pair<Point,Cell> pair = (Pair<Point, Cell>) evt.getNewValue();
//        //TODO: rework this
//        changeSupport.firePropertyChange(LevelDataType.MAP_DATA.name(), evt.getOldValue(), evt.getNewValue());
//        currentMap = originalMap.copy();
////        .setCell(pair.getKey().getX(),pair.getKey().getY(), pair.getValue());
//    }
//
//    public void removeAllListener() {
//        changeSupport = new PropertyChangeSupport(this);
//    }

    public boolean isTutorial() {
        return isTutorial;
    }

//    public int getIndex() {
//        return Integer.parseInt(indexProperty.get());
//    }

    public void setIsTutorial(boolean isTut) {
//        boolean old = isTutorial();
        isTutorial= isTut;
//        changeSupport.firePropertyChange(LevelDataType.IS_TUTORIAL.name(),old ,isTutorial());
    }

//    public void setIndexProperty(int i, boolean confirmed) {
//        int old = getIndex();
//        indexProperty.setValue(i+"");
//        if(!confirmed)changeSupport.firePropertyChange(LevelDataType.LEVEL_INDEX.name(), old,getIndex());
//    }
    /*public void addTutorialLine(String entry){
        List<String> old = new ArrayList<>(tutorialMessages);
        if(tutorialMessages.size()==0)
            tutorialMessages.add(entry);
        else tutorialMessages.add(getCurrentTutorialMessageIndex()+1,entry);
//        currentTutorialIndexProperty.setValue(getCurrentTutorialMessageIndex()+1+"");
//        currentTutorialMessageProperty.setValue(entry);

//        changeSupport.firePropertyChange(LevelDataType.TUTORIAL_LINES.name(),old ,tutorialMessages);
    }*/
    /*public void setTutorialLine(String entry){
        List<String> old = new ArrayList<>(tutorialMessages);
        if(tutorialMessages.size() == 0) tutorialMessages.add(entry);
        else tutorialMessages.set(getCurrentTutorialMessageIndex(),entry);
        currentTutorialMessageProperty.setValue(entry);

//        changeSupport.firePropertyChange(LevelDataType.TUTORIAL_LINES.name(),old ,tutorialMessages);
    }*/

    /*public void deleteCurrentTutorialLine() {
        List<String> old = new ArrayList<>(tutorialMessages);
        tutorialMessages.removeCurrentLevel(getCurrentTutorialMessageIndex());

        if(getCurrentTutorialMessageIndex() == tutorialMessages.size() )currentTutorialIndexProperty.setValue(getCurrentTutorialMessageIndex()-1+"");
        currentTutorialMessageProperty.setValue(tutorialMessages.get(getCurrentTutorialMessageIndex()));

//        changeSupport.firePropertyChange(LevelDataType.TUTORIAL_LINES.name(),old ,tutorialMessages);
    }*/


    //TODO: getTutorialEntryListSize instead of this
    public List<String> getTutorialEntryListCopy() {
        return new ArrayList<>(tutorialMessages);
    }


    public void setGameMap(GameMap value) {
        originalMap = value;
    }

    public void setTutorialMessages(List<String> tutorialMessages) {
        this.tutorialMessages = tutorialMessages;
    }

//    public void resetVariables() {
//        aiBehaviour.resetVariables(true);
//    }
/*
    public int getCurrentTutorialMessageIndex() {
        return Integer.valueOf(currentTutorialIndexProperty.get());
    }
*/


    /*public StringProperty[] getAllProperties() {
        return new StringProperty[] {name, maxKnights, isTutorial, locToStars[0], locToStars[1],
                turnsToStars[0], turnsToStars[1], currentTutorialMessageProperty, currentTutorialIndexProperty};
    }*/

//    public void prevTutorialMessage() {
//        currentTutorialIndexProperty.setValue(Integer.valueOf(currentTutorialIndexProperty.get())-1+"");
//        currentTutorialMessageProperty.setValue(tutorialMessages.get(getCurrentTutorialMessageIndex()));
//    }
//    public void nextTutorialMessage() {
//        currentTutorialIndexProperty.setValue(Integer.valueOf(currentTutorialIndexProperty.get())+1+"");
//        currentTutorialMessageProperty.setValue(tutorialMessages.get(getCurrentTutorialMessageIndex()));
//    }

//    public String getCurrentTutorialMsg() {
//        return currentTutorialMessageProperty.get();
//    }

//
//    public Level copy() {
//        return new Level(getName(), originalMap.copy(), aiBehaviour, getTurnsToStarsCopy(), getLocToStarsCopy()
//        , requiredLevels, getMaxKnights(), isTutorial(), tutorialMessages);
//    }
}
