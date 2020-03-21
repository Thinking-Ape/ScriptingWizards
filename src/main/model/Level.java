package main.model;

import javafx.beans.property.*;
import main.model.gamemap.Cell;
import main.model.gamemap.GameMap;
import main.model.enums.CellContent;
import main.model.enums.CFlag;
import main.model.enums.ItemType;
import main.model.statement.ComplexStatement;
import main.model.statement.Statement;
import main.utility.GameConstants;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import static main.utility.GameConstants.NO_ENTITY;

public class Level implements PropertyChangeListener {

    private PropertyChangeSupport changeSupport;
    private List<String> tutorialMessages;
    private GameMap originalMap;
    private GameMap currentMap;
    private List<String> requiredLevels;
    private StringProperty[] locToStarsProperties; // actually int
    private StringProperty[] turnsToStarsProperties; // actually int
    private StringProperty currentTutorialMessageProperty;
    private StringProperty nameProperty;
    private StringProperty maxKnightsProperty; // actually int
    private StringProperty isTutorialProperty; // actually boolean
    private StringProperty indexProperty; // actually int
    private StringProperty currentTutorialIndexProperty = new SimpleStringProperty(null,GameConstants.CURRENT_TUTORIAL_INDEX_PROPERTY_NAME,0+""); // actually int
    private int bestLOC = -1;
    private int bestTurns = -1;
    private int turnsTaken;
    private ComplexStatement aiBehaviour;
    private ComplexStatement playerBehaviour;

    private boolean isLost = false;
    private boolean aiFinished = false;
    //TODO: change to enum state? (LevelStates: RUNNING, AI_FINISHED, LOST, WON)
    private boolean isStackOverflow;
    private int skeletonCount = 0;


    public Level(String name, Cell[][] originalArray, ComplexStatement aiBehaviour, Integer[] turnsToStars, Integer[] locToStars, String[] requiredLevels, int maxKnights,
                 int index, boolean isTutorial, List<String> tutorialEntryList) {
        this.nameProperty = new SimpleStringProperty(null,GameConstants.LEVEL_NAME_PROPERTY_NAME, name);
        this.maxKnightsProperty = new SimpleStringProperty(null,GameConstants.MAX_KNIGHTS_PROPERTY_NAME,maxKnights+"");
        this.isTutorialProperty = new SimpleStringProperty(null,GameConstants.IS_TUTORIAL_PROPERTY_NAME,isTutorial+"");
        this.indexProperty = new SimpleStringProperty(null,GameConstants.INDEX_PROPERTY_NAME,index+"");
        StringProperty turnsToStars3 = new SimpleStringProperty(null,GameConstants.TURNS_TO_STARS_3_PROPERTY_NAME,turnsToStars[1]+"");
        StringProperty turnsToStars2 = new SimpleStringProperty(null,GameConstants.TURNS_TO_STARS_2_PROPERTY_NAME,turnsToStars[0]+"");
        this.turnsToStarsProperties = new StringProperty[]{turnsToStars2,turnsToStars3};
        StringProperty locToStars3 = new SimpleStringProperty(null,GameConstants.LOC_TO_STARS_3_PROPERTY_NAME,locToStars[1]+"");
        StringProperty locToStars2 = new SimpleStringProperty(null,GameConstants.LOC_TO_STARS_2_PROPERTY_NAME,locToStars[0]+"");
        this.locToStarsProperties = new StringProperty[]{locToStars2,locToStars3};
        this.currentTutorialMessageProperty = new SimpleStringProperty(null,GameConstants.CURRENT_TUTORIAL_MESSAGE_PROPERTY_NAME,"");
        this.currentTutorialIndexProperty = new SimpleStringProperty(null,GameConstants.CURRENT_TUTORIAL_INDEX_PROPERTY_NAME,""+0);
        this.originalMap = new GameMap(originalArray,this);
        this.currentMap = originalMap.clone();
        this.turnsTaken = 0;
        this.aiBehaviour = aiBehaviour;
        this.requiredLevels = new ArrayList<>(Arrays.asList(requiredLevels));
        this.changeSupport = new PropertyChangeSupport(this);
        this.tutorialMessages = new ArrayList<>();
        if(isTutorial){
            if(tutorialEntryList.size() > 0){
                tutorialMessages.addAll(tutorialEntryList);
                currentTutorialMessageProperty =
                        new SimpleStringProperty(null,GameConstants.CURRENT_TUTORIAL_MESSAGE_PROPERTY_NAME, tutorialMessages.get(getCurrentTutorialIndex()));

            }

        }
    }

    public void setBestTurnsAndLOC(int bestTurns, int bestLOC){
        this.bestTurns = bestTurns;
        this.bestLOC = bestLOC;
    }

    public Statement[] executeTurn() throws IllegalAccessException {
        turnsTaken++;
        int noStackOverflow = 0;
        removeTemporaryFlags();
        boolean method_Called_1 = false, method_Called_2 = false;
        Statement statement=playerBehaviour;
        Statement statement2 = aiBehaviour;
        isStackOverflow = false;
        while(!method_Called_1 && !isWon()){ //&&!isLost()) {
            statement = CodeEvaluator.evaluateNext(playerBehaviour,currentMap);

            if(statement==null){
                isLost=true;
                break;
            }
            noStackOverflow++;
            if(noStackOverflow > GameConstants.MAX_LOOP_SIZE ){
                this.isStackOverflow = true;
                isLost = true;
                break;
            }
            boolean canSpawnKnights = currentMap.getAmountOfKnights() < getMaxKnights();
            method_Called_1 = CodeExecutor.executeBehaviour(statement,currentMap, true, canSpawnKnights);

            if(currentMap.getAmountOfKnights() == getMaxKnights() && currentMap.findSpawn().getX() != -1 && !currentMap.cellHasFlag(currentMap.findSpawn(), CFlag.DEACTIVATED))
                currentMap.setFlag(currentMap.findSpawn(), CFlag.DEACTIVATED,true);
        }

        while(!method_Called_2 && !isWon() &&!aiFinished&& GameConstants.IS_AI_ACTIVE&&aiBehaviour!=null) { //&& !isLost()) {
            statement2 = CodeEvaluator.evaluateNext(aiBehaviour,currentMap);
            if (statement2 == null) {
                aiFinished = true;
                break;
            }
            noStackOverflow++;
            if(noStackOverflow > GameConstants.MAX_LOOP_SIZE ){//|| executor.getNoStackOverflow() > GameConstants.MAX_LOOP_SIZE){
                this.isStackOverflow = true;
                isLost = true;
                break;
            }
            method_Called_2 = CodeExecutor.executeBehaviour(statement2,currentMap,false, false);
        }
        applyGameLogicToCells();
        return new Statement[]{statement,statement2};
    }

    private void removeTemporaryFlags() {
        for(int x = 0; x < currentMap.getBoundX(); x++)for(int y = 0; y < currentMap.getBoundY(); y++){
            final Cell cell = currentMap.getCellAtXYClone(x,y);
            for(CFlag flag : CFlag.values()){
                if(flag.isTemporary() && cell.hasFlag(flag))
                    currentMap.setFlag(x, y, flag,false);
            }
        }
    }


    private void applyGameLogicToCells() {
        if(CodeExecutor.skeletonWasSpawned())skeletonCount++;
        for(int x = 0; x < currentMap.getBoundX(); x++)
        for(int y = 0; y < currentMap.getBoundY(); y++) {
            final Cell cell = currentMap.getCellAtXYClone(x,y);
            CellContent content = currentMap.getContentAtXY(x,y);
            if (content == CellContent.PRESSURE_PLATE) {
                boolean invertedAndNotFree = (cell.getEntity() == NO_ENTITY && cell.getItem() != ItemType.BOULDER) && cell.hasFlag(CFlag.INVERTED);
                boolean notInvertedAndFree = (cell.getEntity() != NO_ENTITY || cell.getItem() == ItemType.BOULDER) && !cell.hasFlag(CFlag.INVERTED);
                if (invertedAndNotFree || notInvertedAndFree) currentMap.setFlag(x, y, CFlag.TRIGGERED, true);
                else currentMap.setFlag(x, y, CFlag.TRIGGERED, false);
            }

            if (content == CellContent.ENEMY_SPAWN) {
                if(CodeExecutor.skeletonWasSpawned()){
                    currentMap.setFlag(x, y, CFlag.CHANGE_COLOR, true);
                }
            }
        }
        for(int x = 0; x < currentMap.getBoundX(); x++)for(int y = 0; y < currentMap.getBoundY(); y++){
            final Cell cell = currentMap.getCellAtXYClone(x,y);
            CellContent content = currentMap.getContentAtXY(x,y);
            boolean notAllTriggered = false;
            if(content == CellContent.GATE){
                for (int i = 0; i < cell.getLinkedCellsSize();i++){
                    if(!currentMap.findCellWithId(currentMap.getLinkedCellId(x,y,i)).hasFlag(CFlag.TRIGGERED)){
                        if(!currentMap.cellHasFlag(x, y, CFlag.INVERTED))
                            currentMap.kill(x,y);

                        currentMap.setFlag(x,y,CFlag.OPEN,false);
                        notAllTriggered = true;

//                        break;
                    }
                }
                if(!notAllTriggered&&cell.getLinkedCellsSize()>0){
                    if(currentMap.cellHasFlag(x, y, CFlag.INVERTED))
                        currentMap.kill(x,y);
                    currentMap.setFlag(x,y,CFlag.OPEN,true);
                }
            }

            if(cell.getContent() == CellContent.TRAP && currentMap.getItem(x,y) != ItemType.BOULDER){
            if(cell.hasFlag(CFlag.PREPARING)&&cell.hasFlag(CFlag.ARMED))
                throw new IllegalStateException("A cell is not allowed to have more than 1 of these flags: armed or preparing!");
            if(cell.hasFlag(CFlag.PREPARING)){
                currentMap.setFlag(x,y,CFlag.PREPARING,false);
                currentMap.setFlag(x,y,CFlag.ARMED,true);
                currentMap.kill(x,y);
//                cell.setMultipleItems(null);
            }else if(cell.hasFlag(CFlag.ARMED)){
                currentMap.setFlag(x,y,CFlag.ARMED,false);
            } else currentMap.setFlag(x,y,CFlag.PREPARING,true);}
        }
    }

    public void setPlayerBehaviour(ComplexStatement playerBehaviour) {
        this.playerBehaviour = playerBehaviour;
        changeSupport.firePropertyChange("playerBehaviour", null,null);
    }

    public ComplexStatement getPlayerBehaviour() {
        return playerBehaviour;
    }

    public void reset()  {
//        playerBehaviour.resetCounter();
        if(playerBehaviour != null)
        playerBehaviour.resetVariables(true);
        if(aiBehaviour != null) aiBehaviour.resetVariables(true);
//        noStackOverflow = 0;
//        isWon=false;
        skeletonCount = 0;
        isLost = false;
        aiFinished = false;
        currentMap = originalMap.clone();
        CodeExecutor.reset();
        turnsTaken = 0;
        //notifyListener(Event.MAP_CHANGED);
        changeSupport.firePropertyChange("level", null,null);
    }



    public boolean isWon(){
        return CodeExecutor.hasWon();
    }
    public boolean isLost(){
        if(CodeExecutor.hasLost() || isLost)
        return CodeExecutor.hasLost() || isLost;
        else return false;
    }

    public ComplexStatement getAIBehaviour() {
        return aiBehaviour;
    }

    public void setAiBehaviour(ComplexStatement aiBehaviour) {
        ComplexStatement oldAiBehaviour = this.aiBehaviour;
        this.aiBehaviour = aiBehaviour;
        if(oldAiBehaviour.getStatementListSize() != aiBehaviour.getStatementListSize())changeSupport.firePropertyChange("aiBehaviour", oldAiBehaviour,aiBehaviour);
    }

    public void setName(String name) {
        String oldName = this.nameProperty.getValue();
        this.nameProperty.setValue(name);
        if(!name.equals(oldName))changeSupport.firePropertyChange("nameProperty", oldName, name);
    }

    public void changeHeight(int newHeight) {
        int oldHeight = originalMap.getBoundY();
        Cell[][] newMapArray = new Cell[originalMap.getBoundX()][newHeight];
        for(int y = 0; y < newHeight; y++ ){
            for(int x = 0; x < originalMap.getBoundX(); x++){

                if(y < originalMap.getBoundY()){
                    if(y == newHeight-1 && originalMap.getContentAtXY(x,y) != CellContent.EMPTY)newMapArray[x][y]=  new Cell(CellContent.WALL);
                    else newMapArray[x][y]=originalMap.getCellAtXYClone(x,y);
                }
                else newMapArray[x][y] = new Cell(CellContent.WALL);
            }
        }
        originalMap.removeAllListeners();
        currentMap.removeAllListeners();
        originalMap = new GameMap(newMapArray,this);
        currentMap = originalMap.clone();
        if(oldHeight != newHeight)changeSupport.firePropertyChange("height", oldHeight, newHeight);
        //notifyListener(Event.LEVEL_CHANGED);
    }
    public void changeWidth(int newWidth) {
        int oldWidth = originalMap.getBoundX();
        Cell[][] newMap = new Cell[newWidth][originalMap.getBoundY()];
        for(int y = 0; y < originalMap.getBoundY(); y++ ){
            for(int x = 0; x < newWidth; x++){
                if(x < originalMap.getBoundX()){
                    if(x == newWidth-1 && originalMap.getContentAtXY(x,y) != CellContent.EMPTY)newMap[x][y]=  new Cell(CellContent.WALL);
                    else newMap[x][y]=originalMap.getCellAtXYClone(x,y);
                }
                else newMap[x][y] = new Cell(CellContent.WALL);
            }
        }
        originalMap.removeAllListeners();
        currentMap.removeAllListeners();
        originalMap = new GameMap(newMap,this);
        currentMap = originalMap.clone();
        if(oldWidth != newWidth)changeSupport.firePropertyChange("width", oldWidth, newWidth);
        //notifyListener(Event.LEVEL_CHANGED);
    }

    public boolean hasAi() {
        //TODO: add boolean value?
        return aiBehaviour.getStatementListSize() > 0;
    }

//    public int getIndex() {
//        return indexProperty;
//    }


    public GameMap getOriginalMap(){
        return originalMap;
    }
    public GameMap getCurrentMap(){
        return currentMap;
    }

    // public Cell[][] getOriginalMapClone() {
    //     return clone(originalMap);
    //}

    //TODO: Hässlich!!
//    public void setFlagForCell(Cell cell, CFlag traversable, boolean t1) {
//        cell.setFlagValue(traversable,t1);
//        //notifyListener(Event.MAP_CHANGED);
//    }

    /*public Cell[][] getCurrentMapClone() {
        return clone(currentMap);
    }*/

//    public void setCurrentMapToOriginal() {
//        currentMap =  originalMap.clone();
//    }


    public String getName() {
        return nameProperty.getValue();
    }

    public int getTurnsTaken() {
        return turnsTaken;
    }

    public List<String> getRequiredLevels() {
        return requiredLevels;
    }

    public Integer[] getLocToStars() {
        return new Integer[]{Integer.valueOf(locToStarsProperties[0].get()),Integer.valueOf(locToStarsProperties[1].get())};
    }
    public Integer[] getTurnsToStars() {
        return new Integer[]{Integer.valueOf(turnsToStarsProperties[0].get()),Integer.valueOf(turnsToStarsProperties[1].get())};
    }

    public void changeLocToStars(Integer[] locStarArray) {
        StringProperty locToStars3 = new SimpleStringProperty(null,GameConstants.LOC_TO_STARS_3_PROPERTY_NAME,locStarArray[1]+"");
        StringProperty locToStars2 = new SimpleStringProperty(null,GameConstants.LOC_TO_STARS_2_PROPERTY_NAME,locStarArray[0]+"");
        this.locToStarsProperties = new StringProperty[]{locToStars2,locToStars3};
    }
//TODO: only if they differ
    public void changeTurnsToStars(Integer[] turnsToStars) {
        StringProperty turnsToStars3 = new SimpleStringProperty(null,GameConstants.TURNS_TO_STARS_3_PROPERTY_NAME,turnsToStars[1]+"");
        StringProperty turnsToStars2 = new SimpleStringProperty(null,GameConstants.TURNS_TO_STARS_2_PROPERTY_NAME,turnsToStars[0]+"");
        this.turnsToStarsProperties = new StringProperty[]{turnsToStars2,turnsToStars3};
    }
    //TODO: only if they differ
    public void setRequiredLevels(List<String> requiredLevelNames) {
        this.requiredLevels = requiredLevelNames;
        changeSupport.firePropertyChange("requiredLevels", this.requiredLevels, requiredLevelNames);
    }

    public int getMaxKnights() {
        return Integer.parseInt(maxKnightsProperty.get());
    }

    public void setMaxKnights(int maxKnights) {
//        int oldMaxKnights = getMaxKnights();
        this.maxKnightsProperty.setValue(maxKnights+"");
//        if(oldMaxKnights != maxKnights)changeSupport.firePropertyChange("maxKnightsProperty", oldMaxKnights, maxKnights);
    }

    public void addChangeListener(PropertyChangeListener pcl) {
        changeSupport.addPropertyChangeListener(pcl);
    }

    public void setUnlockedStatementList(List<String> unlockedStatementList){
        CodeExecutor.setUnlockedStatementList(unlockedStatementList);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
//        Pair<Point,Cell> pair = (Pair<Point, Cell>) evt.getNewValue();
        if(!(evt.getPropertyName().equals("cellId")||evt.getPropertyName().equals("linkedCellId")))changeSupport.firePropertyChange("map", evt.getOldValue(), evt.getNewValue());
        else changeSupport.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
        currentMap = originalMap.clone();
//        .setCell(pair.getKey().getX(),pair.getKey().getY(), pair.getValue());
    }

    public void removeAllListener() {
        changeSupport = new PropertyChangeSupport(this);
    }

    public boolean isTutorial() {
        return Boolean.parseBoolean(isTutorialProperty.get());
    }

    public int getIndex() {
        return Integer.parseInt(indexProperty.get());
    }

    public void setIsTutorial(boolean selected) {
//        boolean old = isTutorial();
        isTutorialProperty.setValue(selected+"");
//        changeSupport.firePropertyChange("isTutorial", old,selected);
    }

    public void setIndexProperty(int i) {
        indexProperty.setValue(i+"");
//        changeSupport.firePropertyChange("indexProperty", null,this);
    }
    public void addTutorialLine(String entry){
        if(tutorialMessages.size()==0)
            tutorialMessages.add(entry);
        else tutorialMessages.add(getCurrentTutorialIndex()+1,entry);
        currentTutorialIndexProperty.setValue(getCurrentTutorialIndex()+1+"");
        currentTutorialMessageProperty.setValue(entry);
    }
    public void setTutorialLine(String entry){
        if(tutorialMessages.size() == 0) tutorialMessages.add(entry);
        else tutorialMessages.set(getCurrentTutorialIndex(),entry);
        currentTutorialMessageProperty.setValue(entry);
    }

    public void deleteCurrentTutorialLine() {
        tutorialMessages.remove(getCurrentTutorialIndex());

        if(getCurrentTutorialIndex() == tutorialMessages.size() )currentTutorialIndexProperty.setValue(getCurrentTutorialIndex()-1+"");
        currentTutorialMessageProperty.setValue(tutorialMessages.get(getCurrentTutorialIndex()));
    }

    public boolean isStackOverflow() {
        return isStackOverflow;
    }

    //TODO: getTutorialEntryListSize instead of this
    public List<String> getTutorialEntryList() {
        return tutorialMessages;
    }

    public List<String> getUnlockedStatementList() {
        return CodeExecutor.getUnlockedStatementList();
    }


    public int getBestLOC() {
        return bestLOC;
    }
    public int getBestTurns() {
        return bestTurns;
    }

    public boolean isAiFinished() {
        return aiFinished;
    }

    public int getSkeletonCount() {
        return skeletonCount;
    }

    public int getCurrentTutorialIndex() {
        return Integer.valueOf(currentTutorialIndexProperty.get());
    }


    public StringProperty[] getAllProperties() {
        return new StringProperty[] {nameProperty,maxKnightsProperty,isTutorialProperty,indexProperty, locToStarsProperties[0], locToStarsProperties[1],
                turnsToStarsProperties[0], turnsToStarsProperties[1], currentTutorialMessageProperty, currentTutorialIndexProperty};
    }

    public void prevTutorialMessage() {
        currentTutorialIndexProperty.setValue(Integer.valueOf(currentTutorialIndexProperty.get())-1+"");
        currentTutorialMessageProperty.setValue(tutorialMessages.get(getCurrentTutorialIndex()));
    }
    public void nextTutorialMessage() {
        currentTutorialIndexProperty.setValue(Integer.valueOf(currentTutorialIndexProperty.get())+1+"");
        currentTutorialMessageProperty.setValue(tutorialMessages.get(getCurrentTutorialIndex()));
    }

    public String getCurrentTutorialMsg() {
        return currentTutorialMessageProperty.get();
    }
}
