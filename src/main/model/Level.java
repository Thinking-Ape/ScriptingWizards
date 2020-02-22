package main.model;

import main.model.enums.CContent;
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
    private boolean isTutorial;
    private List<String> tutorialMessages;
    private int index;
    private GameMap originalMap;
    private GameMap currentMap;
    private List<String> requiredLevels;
    private Integer[] locToStars;
    private Integer[] turnsToStars;
    private int turnsTaken;
    private String name;
    private int maxKnights;
    private int usedKnights;
    private ComplexStatement aiBehaviour;
    private ComplexStatement playerBehaviour;

    private boolean isLost = false;
    private boolean aiFinished = false;
    //TODO: change to enum state? (LevelStates: RUNNING, AI_FINISHED, LOST, WON)
    private CodeExecutor executor;
    private CodeEvaluator evaluator;
    private boolean isStackOverflow;


    public Level(String name, Cell[][] originalArray, ComplexStatement aiBehaviour, Integer[] turnsToStars, Integer[] locToStars, String[] requiredLevels, int maxKnights, int index, boolean isTutorial,List<String> tutorialEntryList) {
        this.name = name;
        this.index = index;
        this.isTutorial = isTutorial;
        this.maxKnights = maxKnights;
        this.usedKnights = 0;
        this.originalMap = new GameMap(originalArray,this);
        this.currentMap = originalMap.clone();
        this.turnsTaken = 0;
        this.aiBehaviour = aiBehaviour;
        this.evaluator = new CodeEvaluator();
        this.executor = new CodeExecutor();
        this.turnsToStars = turnsToStars;
        this.locToStars = locToStars;
        this.requiredLevels = new ArrayList<>(Arrays.asList(requiredLevels));
        this.changeSupport = new PropertyChangeSupport(this);
        this.tutorialMessages = new ArrayList<>();
        if(isTutorial){
            if(tutorialEntryList.size() > 0)tutorialMessages.addAll(tutorialEntryList);
//            else tutorialMessages.add("");
        }
    }


    public Statement executeTurn() throws IllegalAccessException {
        turnsTaken++;
        int noStackOverflow = 0;
        removeTemporaryFlags();
        boolean method_Called_1 = false, method_Called_2 = false;
        Statement statement=playerBehaviour;
        isStackOverflow = false;
        while(!method_Called_1 && !isWon()&&!isLost()) {
            statement = evaluator.evaluateNext(playerBehaviour,currentMap);
            if(evaluator.lastStatementSummonedKnight()){
                usedKnights++;
            }
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
            if(usedKnights <= maxKnights)method_Called_1 = executor.executeBehaviour(statement,currentMap, true);
            else usedKnights--;

            if(usedKnights == maxKnights && currentMap.findSpawn().getX() != -1 && !currentMap.cellHasFlag(currentMap.findSpawn(), CFlag.DEACTIVATED))currentMap.setFlag(currentMap.findSpawn(), CFlag.DEACTIVATED,true);
        }

        while(!method_Called_2&& !isLost() && !isWon() &&!aiFinished&& GameConstants.IS_AI_ACTIVE&&aiBehaviour!=null) {
            Statement statement2 = evaluator.evaluateNext(aiBehaviour,currentMap);
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
            method_Called_2 = executor.executeBehaviour(statement2,currentMap,false);
        }
        applyGameLogicToCells();
        return statement;
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
        for(int x = 0; x < currentMap.getBoundX(); x++)for(int y = 0; y < currentMap.getBoundY(); y++) {
            final Cell cell = currentMap.getCellAtXYClone(x,y);
            CContent content = currentMap.getContentAtXY(x,y);
            if (content == CContent.PRESSURE_PLATE) {
                boolean invertedAndNotFree = (cell.getEntity() == NO_ENTITY && cell.getItem() != ItemType.BOULDER) && cell.hasFlag(CFlag.INVERTED);
                boolean notInvertedAndFree = (cell.getEntity() != NO_ENTITY || cell.getItem() == ItemType.BOULDER) && !cell.hasFlag(CFlag.INVERTED);
                if (invertedAndNotFree || notInvertedAndFree) currentMap.setFlag(x, y, CFlag.TRIGGERED, true);
                else currentMap.setFlag(x, y, CFlag.TRIGGERED, false);
            }
        }
        for(int x = 0; x < currentMap.getBoundX(); x++)for(int y = 0; y < currentMap.getBoundY(); y++){
            final Cell cell = currentMap.getCellAtXYClone(x,y);
            CContent content = currentMap.getContentAtXY(x,y);
            boolean notAllTriggered = false;
            if(content == CContent.GATE){
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

            if(cell.getContent() == CContent.TRAP && currentMap.getItem(x,y) != ItemType.BOULDER){
            if(cell.hasFlag(CFlag.PREPARING)&&cell.hasFlag(CFlag.ARMED))
                throw new IllegalStateException("A cell is not allowed to have more than 1 of these flags: armed or preparing!");
            if(cell.hasFlag(CFlag.PREPARING)){
                currentMap.setFlag(x,y,CFlag.PREPARING,false);
                currentMap.setFlag(x,y,CFlag.ARMED,true);
                currentMap.kill(x,y);
//                cell.setItem(null);
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
        usedKnights = 0;
        if(playerBehaviour != null)
        playerBehaviour.resetVariables(true);
        if(aiBehaviour != null) aiBehaviour.resetVariables(true);
//        noStackOverflow = 0;
//        isWon=false;
        isLost = false;
        aiFinished = false;
        currentMap = originalMap.clone();
        executor.reset();
        turnsTaken = 0;
        //notifyListener(Event.MAP_CHANGED);
        changeSupport.firePropertyChange("level", null,null);
    }



    public boolean isWon(){
        return executor.hasWon();
    }
    public boolean isLost(){
        if(executor.hasLost() || isLost)
        return executor.hasLost() || isLost;
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
        String oldName = this.name;
        this.name = name;
        if(!name.equals(oldName))changeSupport.firePropertyChange("name", oldName, name);
    }

    public void changeHeight(int newHeight) {
        int oldHeight = originalMap.getBoundY();
        Cell[][] newMap = new Cell[originalMap.getBoundX()][newHeight];
        for(int y = 0; y < newHeight; y++ ){
            for(int x = 0; x < originalMap.getBoundX(); x++){

                if(y < originalMap.getBoundY()){
                    if(y == newHeight-1 && originalMap.getContentAtXY(x,y) != CContent.EMPTY)newMap[x][y]=  new Cell(CContent.WALL);
                    else newMap[x][y]=originalMap.getCellAtXYClone(x,y);
                }
                else newMap[x][y] = new Cell(CContent.WALL);
            }
        }
        originalMap = new GameMap(newMap,this);
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
                    if(x == newWidth-1 && originalMap.getContentAtXY(x,y) != CContent.EMPTY)newMap[x][y]=  new Cell(CContent.WALL);
                    else newMap[x][y]=originalMap.getCellAtXYClone(x,y);
                }
                else newMap[x][y] = new Cell(CContent.WALL);
            }
        }
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
//        return index;
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
        return name;
    }

    public int getTurnsTaken() {
        return turnsTaken;
    }

    public List<String> getRequiredLevels() {
        return requiredLevels;
    }

    public Integer[] getLocToStars() {
        return locToStars;
    }
    public Integer[] getTurnsToStars() {
        return turnsToStars;
    }

    public void changeLocToStars(Integer[] locStarArray) {
        Integer[] oldLocTOStars = this.locToStars;
        this.locToStars = locStarArray;
        changeSupport.firePropertyChange("locToStars", oldLocTOStars, locStarArray);
    }
//TODO: only if they differ
    public void changeTurnsToStars(Integer[] turnsToStars) {
        Integer[] oldTurnsToStars = this.turnsToStars;
        this.turnsToStars = turnsToStars;
        changeSupport.firePropertyChange("turnsToStars",oldTurnsToStars, turnsToStars);
    }
    //TODO: only if they differ
    public void setRequiredLevels(List<String> requiredLevelNames) {
        this.requiredLevels = requiredLevelNames;
        changeSupport.firePropertyChange("requiredLevels", this.requiredLevels, requiredLevelNames);
    }

    public int getMaxKnights() {
        return maxKnights;
    }

    public void setMaxKnights(int maxKnights) {
        int oldMaxKnights = this.maxKnights;
        this.maxKnights = maxKnights;
        if(oldMaxKnights != maxKnights)changeSupport.firePropertyChange("maxKnights", oldMaxKnights, maxKnights);
    }

    public void addChangeListener(PropertyChangeListener pcl) {
        changeSupport.addPropertyChangeListener(pcl);
    }

    public void setUnlockedStatementList(List<String> unlockedStatementList){
        executor.setUnlockedStatementList(unlockedStatementList);
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

    public int getUsedKnights() {
        return usedKnights;
    }

    public boolean isTutorial() {
        return isTutorial;
    }

    public int getIndex() {
        return index;
    }

    public void setIsTutorial(boolean selected) {
        boolean old = isTutorial;
        isTutorial = selected;
        changeSupport.firePropertyChange("isTutorial", old,selected);
    }

    public void setIndex(int i) {
        int old = index;
        index = i;
        changeSupport.firePropertyChange("index", null,this);
    }
    public void addTutorialLine(int i,String tutorialLine){
        if(tutorialMessages.size()==0)
            tutorialMessages.add(tutorialLine);
        else tutorialMessages.add(i,tutorialLine);
    }
    public void setTutorialLine(int index, String tutorialLine){
        if(tutorialMessages.size() == 0)tutorialMessages.add(tutorialLine);
        else tutorialMessages.set(index,tutorialLine);
        changeSupport.firePropertyChange("tutorial", null,tutorialLine);
    }

    public void deleteTutorialLine(int index) {
        tutorialMessages.remove(index);
        changeSupport.firePropertyChange("tutorialDeletion", null,index);
    }

    public boolean isStackOverflow() {
        return isStackOverflow;
    }

    //TODO: getTutorialEntryListSize instead of this
    public List<String> getTutorialEntryList() {
        return tutorialMessages;
    }

    public List<String> getUnlockedStatementList() {
        return executor.getUnlockedStatementList();
    }
    public Statement getExecuteIfStatementWorkaround(){
        return evaluator.getExecuteIfWorkaround();
    }
}
