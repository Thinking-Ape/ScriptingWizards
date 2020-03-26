package main.model;

import main.model.enums.CFlag;
import main.model.enums.CellContent;
import main.model.enums.ItemType;
import main.model.gamemap.Cell;
import main.model.gamemap.GameMap;
import main.model.statement.ComplexStatement;
import main.model.statement.Statement;
import main.utility.GameConstants;

import java.util.*;

import static main.utility.GameConstants.NO_ENTITY;

public abstract class Model {
    private static List<Level> levelList = new ArrayList<>();
    private static int currentLevelIndex = 0;
    private static LevelChangeSender levelChangeSender;

    //TODO:!!!!!
    private static List<Level> finishedLevelsList = new ArrayList<>();
    private static List<String> unlockedStatementsList = new ArrayList<>();
    private static Map<Level,Integer> levelStarMap = new HashMap<>();
    private static GameMap currentMap;
    private static ComplexStatement currentAiBehaviour;
    private static int turnsTaken = 0;
    private static ComplexStatement playerBehaviour;
    private static boolean isLost = false;
    private static boolean isWon = false;
    private static boolean aiFinished = false;
    private static boolean isStackOverflow;
    private static int skeletonCount = 0;
    //TODO:!!!!!
    private static Map<Level,List<String>> levelToAIMap;
    private static Map<Level,Integer> bestTurnsMap;
    private static Map<Level,Integer> bestLOCMap;
    private static int tutorialProgress = -1;

//    private static Model single_Instance = null;
    private static int currentTutorialIndex = 0;
    private static int amountOfKnights;

//    private Model(){
//        levelList = new ArrayList<>();
//        finishedLevelsList = new ArrayList<>();
//        levelStarMap = new HashMap<>();
//    }

//    public static Model getInstance() {
//        if(single_Instance == null)single_Instance = new Model();
//        return single_Instance;
//    }

    private static Level getCurrentLevel() {
        return levelList.get(currentLevelIndex);
    }

    public static void addLevel(Level level, boolean isNew) {
        levelList.add(level);
        if(isNew) {
            levelChangeSender.wholeLevelChanged();
            selectLevel(level.getName());
        }
    }

    public static void selectLevel(int index) {
        currentLevelIndex = index;
        currentTutorialIndex = 0;
        currentMap = getCurrentLevel().getOriginalMapCopy();
        currentAiBehaviour = getCurrentLevel().getAIBehaviourCopy();
        //TODO: dafuqqqqq??!
//        try {
//            currentEditedLevel.setUnlockedStatementList(JSONParser.getUnlockedStatementList());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        levelChangeSender.wholeLevelChanged();
    }
    public static void selectLevel(String name){
        selectLevel(getIndexOfLevelInList(name));
    }

    public static void removeCurrentLevel() {
        levelList.remove(currentLevelIndex);
        for(Level l : levelList){
            l.getRequiredLevelNamesCopy().remove(getCurrentLevel().getName());
        }
        selectLevel(currentLevelIndex);
    }

    public static int getAmountOfLevels() {
        return levelList.size();
    }

    public static int getIndexOfLevelInList(String levelName) {
        for(int i = 0; i < levelList.size(); i++){
            Level l = levelList.get(i);
            if(l.getName().equals(levelName))return i;
        }
        return -1;
    }

    //TODO: WRONG PLACE!
    /*public void updateFinishedList() throws IOException {
        finishedLevelsList.add(getCurrentLevel());
        for(Level l : levelList){
            int foundLevels = 0;
            for(String requiredLevelName : l.getRequiredLevelNamesCopy()){
                for(Level fL : finishedLevelsList)if(fL.getName().equals(requiredLevelName))foundLevels++;
            }
            if(foundLevels == l.getRequiredLevelNamesCopy().size())JSONParser.updateUnlocks(l);
        }
    }*/


   /* @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LevelDataType changeType = LevelDataType.valueOf(evt.getPropertyName());
//       if(changeType == null)throw new IllegalArgumentException("You might need to add the following LevelDataType: "+evt.getPropertyName() );

        changeSupport.firePropertyChange("level", evt.getOldValue(), evt.getNewValue());
        changeCurrentLevel(new LevelChange( changeType,evt.getOldValue(),evt.getNewValue()));
       //TODO: evaluate necessity
        //TODO: updateUnlocks if things changed??
    }
*/
    public static void initLevelChangeListener(LevelChangeListener levelChangeListener) {
       if(levelChangeSender == null) levelChangeSender = new LevelChangeSender(levelChangeListener);
       else throw new IllegalStateException("LevelChangeSender has already been initialized!");
    }

//    public static Level getLevelWithName(String s) {
//        for(Level l : levelList){
//            if(l.getName().toLowerCase().equals(s.toLowerCase()))return l;
//        }
//        return null;
//    }

    public static void reloadCurrentLevel() {
        levelChangeSender.resetChanges();
    }

    private static void updateLevelValues(LevelDataType levelDataType, Object value) {
        switch (levelDataType){
            case LEVEL_INDEX:
                int oldIndex = (int)value;
                moveCurrentLevel(oldIndex);
                break;
            case MAX_KNIGHTS:
                getCurrentLevel().setMaxKnights((int)value);
                break;
            case MAP_DATA:
                getCurrentLevel().setGameMap((GameMap)value);
                break;
//            case MAP_HEIGHT:
//                getCurrentLevel().changeHeight((int)value);
//                break;
//            case MAP_WIDTH:
//                getCurrentLevel().changeWidth((int)value);
//                break;
            case AI_CODE:
                getCurrentLevel().setAiBehaviour((ComplexStatement) value);
                break;
            case HAS_AI:
                break;
            case LOC_TO_STARS:
                getCurrentLevel().setLocToStars((Integer[])value);
                break;
            case TURNS_TO_STARS:
                getCurrentLevel().setTurnsToStars((Integer[])value);
                break;
            case REQUIRED_LEVELS:
                getCurrentLevel().setRequiredLevels((List<String>) value);
                break;
            case IS_TUTORIAL:
                getCurrentLevel().setIsTutorial((boolean) value);
                break;
            case TUTORIAL_LINES:
                getCurrentLevel().setTutorialMessages((List<String>) value);
                break;
            case LEVEL_NAME:
                String oldName = getCurrentLevel().getName();
                String name = (String) value;
                for(Level l : levelList){
                    if(l.getRequiredLevelNamesCopy().contains(oldName)){
                        l.getRequiredLevelNamesCopy().remove(oldName);
                        l.getRequiredLevelNamesCopy().add(name);
                    }
                }
                getCurrentLevel().setName(name);
                break;
        }
    }

    private static void moveCurrentLevel(int targetIndex) {
        int step = 1;
        if(targetIndex<currentLevelIndex)step = -1;
        for(int i = currentLevelIndex; i != targetIndex; i+=step){
            if(step==1)moveCurrentLevelUp();
            else moveCurrentLevelDown();
        }
    }

//    public Level getLevelWithIndex(int i) {
//        if(levelList.get(i-1).getIndex()!=i-1)throw new IllegalStateException("Level Index does not equal its position in the Level List minus 1!");
//        return levelList.get(i-1);
//    }

    public static void moveCurrentLevelDown() {
        Level tempLevel = levelList.get(currentLevelIndex-1);
        getCurrentLevel().getRequiredLevelNamesCopy().remove(tempLevel.getName());
        levelList.set(currentLevelIndex-1, getCurrentLevel());
        levelList.set(currentLevelIndex, tempLevel);
        currentLevelIndex--;
    }

//    public Level getLevelWithIndex(int i) {
//        for(Level l : levelList){
//            if(l.getIndex() == i) return l;
//        }
//        return null;
//    }

    public static void moveCurrentLevelUp() {
        Level tempLevel = levelList.get(currentLevelIndex+1);
        tempLevel.getRequiredLevelNamesCopy().remove(getCurrentLevel().getName());
        levelList.set(currentLevelIndex+1, getCurrentLevel());
        levelList.set(currentLevelIndex, tempLevel);
        currentLevelIndex++;
    }

//    public static List<Level> getLevelListCopy() {
//        List<Level> output = new ArrayList<>(levelList);
//        return output;
//    }

    public static void changeCurrentLevel(LevelDataType levelDataType, Object newValue){
        Object oldValue = null;
        switch (levelDataType){
            case LEVEL_INDEX:
                oldValue = currentLevelIndex;
                break;
            case MAX_KNIGHTS:
                oldValue = getCurrentLevel().getMaxKnights();
                break;
            case MAP_DATA:
                oldValue = getCurrentLevel().getOriginalMapCopy();
                break;
//            case MAP_HEIGHT:
//                oldValue = getCurrentLevel().getOriginalMapCopy().getBoundY();
//                break;
//            case MAP_WIDTH:
//                oldValue = getCurrentLevel().getOriginalMapCopy().getBoundX();
//                break;
            case AI_CODE:
                oldValue = getCurrentLevel().getAIBehaviourCopy();
                break;
            case HAS_AI:
                break;
            case LOC_TO_STARS:
                oldValue = getCurrentLevel().getLocToStarsCopy();
                break;
            case TURNS_TO_STARS:
                oldValue = getCurrentLevel().getTurnsToStarsCopy();
                break;
            case REQUIRED_LEVELS:
                oldValue = getCurrentLevel().getRequiredLevelNamesCopy();
                break;
            case IS_TUTORIAL:
                oldValue = getCurrentLevel().isTutorial();
                break;
            case TUTORIAL_LINES:
                oldValue = getCurrentLevel().getTutorialEntryListCopy();
                break;
            case LEVEL_NAME:
                oldValue = getCurrentLevel().getName();
                break;
            default:
                throw new IllegalStateException("You might have forgotten to add the following case: "+levelDataType);
        }
        if(newValue.equals(oldValue))return;
        LevelChange levelChange = new LevelChange(levelDataType, oldValue, newValue);
        updateLevelValues(levelChange.getLevelDataType(), levelChange.getNewValue());
        levelChangeSender.addLevelChange(levelChange);
    }

    public static boolean currentLevelHasChanged() {
        return levelChangeSender.hasChanged();
    }

    public static Map<LevelDataType,LevelChange> getAndConfirmCurrentChanges() {
        Map<LevelDataType,LevelChange> output = levelChangeSender.getAndConfirmChanges();
//        Level oldLevel = null;
        if(output.containsKey(LevelDataType.LEVEL_INDEX)){
            LevelChange change = output.get(LevelDataType.LEVEL_INDEX);
            int newIndex = (int)change.getNewValue();
            int oldIndex = (int)change.getOldValue();
            int step = 1;
            if(newIndex < oldIndex)step = -1;
            for(int i = 0; i < levelList.size(); i++){
                int absDiff = (i-oldIndex)*step;
                if ( absDiff > 0 && absDiff <= (newIndex-oldIndex)*step)moveCurrentLevel(step);
            }
        }
        if(output.containsKey(LevelDataType.LEVEL_NAME)){
            LevelChange change = output.get(LevelDataType.LEVEL_NAME);
            String name = getCurrentLevel().getName();
            String oldName = (String) change.getOldValue();
            for(Level l : levelList){
                if(l.getRequiredLevelNamesCopy().contains(oldName)){
                    l.getRequiredLevelNamesCopy().remove(oldName);
                    l.getRequiredLevelNamesCopy().add(name);
                }
            }
        }
        if(output.containsKey(LevelDataType.IS_TUTORIAL)){
            //TODO!
        }
//        levelChangeMap.clear();
//        changeSupport.firePropertyChange("confirmed", null, null);
        return output;
    }

    public static int getCurrentIndex() {
        return currentLevelIndex;
    }

    public static Object getDataFromLevelWithIndex(LevelDataType dataType, int index){
        Level level = levelList.get(index);
        switch (dataType){
            case LEVEL_INDEX:
                return index;
            case MAX_KNIGHTS:
                return level.getMaxKnights();
            case MAP_DATA:
                return level.getOriginalMapCopy();
//            case MAP_HEIGHT:
//                return level.getOriginalMapCopy().getBoundY();
//            case MAP_WIDTH:
//                return level.getOriginalMapCopy().getBoundX();
            case AI_CODE:
                return level.getAIBehaviourCopy();
            case LOC_TO_STARS:
                return level.getLocToStarsCopy();
            case TURNS_TO_STARS:
                return level.getTurnsToStarsCopy();
            case REQUIRED_LEVELS:
                return level.getRequiredLevelNamesCopy();
            case IS_TUTORIAL:
                return level.isTutorial();
            case TUTORIAL_LINES:
                return level.getTutorialEntryListCopy();
            case LEVEL_NAME:
                return level.getName();
            case HAS_AI:
                return level.hasAi();
            //TODO:
        }
        return null;
    }
    public static Object getDataFromCurrentLevel(LevelDataType dataType){
        return getDataFromLevelWithIndex(dataType, currentLevelIndex);
    }

    public static void nextTutorialMessage() {
        if(currentTutorialIndex < getCurrentLevel().getTutorialEntryListCopy().size()-1)
            currentTutorialIndex++;
    }

    public static int getCurrentTutorialIndex() {
        return currentTutorialIndex;
    }

    public static void prevTutorialMessage() {
        if(currentTutorialIndex>0)
            currentTutorialIndex--;
    }

    public static int getCurrentTutorialSize() {
        return getCurrentLevel().getTutorialEntryListCopy().size();
    }

    public static List<String> getUnlockedStatementList() {
        return new ArrayList<>(unlockedStatementsList);
    }

    public static String getNameOfLevelWithIndex(int i) {
        return levelList.get(i).getName();
    }

    public static boolean hasLevelWithName(String t1) {
        for(Level l : levelList){
            if(l.getName().equals(t1))return true;
        }
        return false;
    }

    public static void fillTursnAndLOCMap(Map<String,Integer> locMap,Map<String,Integer> turnsMap){
        //TODO
        Model.bestTurns = bestTurns;
        Model.bestLOC = bestLOC;
    }
    
    public static Statement[] executeTurn() {
        turnsTaken++;
        int noStackOverflow = 0;
        removeTemporaryFlags();
        boolean method_Called_1 = false, method_Called_2 = false;
        Statement statement=playerBehaviour;
        Statement statement2 = currentAiBehaviour;
        isStackOverflow = false;
        while(!method_Called_1 && !isWon){ //&&!isLost()) {
            statement = CodeEvaluator.evaluateNext(playerBehaviour,currentMap);

            if(statement==null){
                isLost=true;
                break;
            }
            noStackOverflow++;
            if(noStackOverflow > GameConstants.MAX_LOOP_SIZE ){
                isStackOverflow = true;
                isLost = true;
                break;
            }
            boolean canSpawnKnights = currentMap.getAmountOfKnights() < getCurrentLevel().getMaxKnights();
            method_Called_1 = CodeExecutor.executeBehaviour(statement,currentMap, true, canSpawnKnights);

            if(currentMap.getAmountOfKnights() == getCurrentLevel().getMaxKnights() && currentMap.findSpawn().getX() != -1 && !currentMap.cellHasFlag(currentMap.findSpawn(), CFlag.DEACTIVATED))
                currentMap.setFlag(currentMap.findSpawn(), CFlag.DEACTIVATED,true);
        }

        while(!method_Called_2 && !isWon &&!aiFinished&& GameConstants.IS_AI_ACTIVE&& currentAiBehaviour !=null) { //&& !isLost()) {
            statement2 = CodeEvaluator.evaluateNext(currentAiBehaviour,currentMap);
            if (statement2 == null) {
                aiFinished = true;
                break;
            }
            noStackOverflow++;
            if(noStackOverflow > GameConstants.MAX_LOOP_SIZE ){//|| executor.getNoStackOverflow() > GameConstants.MAX_LOOP_SIZE){
                isStackOverflow = true;
                isLost = true;
                break;
            }
            method_Called_2 = CodeExecutor.executeBehaviour(statement2,currentMap,false, false);
        }
        applyGameLogicToCells();
        return new Statement[]{statement,statement2};
    }
    
    private static void removeTemporaryFlags() {
        for(int x = 0; x < currentMap.getBoundX(); x++)for(int y = 0; y < currentMap.getBoundY(); y++){
            final Cell cell = currentMap.getCellAtXYClone(x,y);
            for(CFlag flag : CFlag.values()){
                if(flag.isTemporary() && cell.hasFlag(flag))
                    currentMap.setFlag(x, y, flag,false);
            }
        }
    }


    private static void applyGameLogicToCells() {
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

    public static void setCurrentPlayerBehaviour(ComplexStatement playerBehaviour) {
        Model.playerBehaviour = playerBehaviour;
//        changeSupport.firePropertyChange("playerBehaviour", null,null);
    }

    public static ComplexStatement getCurrentPlayerBehaviour() {
        return playerBehaviour;
    }

    public static boolean isLost() {
        return isLost;
    }

    public static boolean aiIsFinished() {
        return aiFinished;
    }

    public static boolean isWon() {
        return isWon;
    }

    public static int getTurnsTaken() {
        return turnsTaken;
    }

    public static int getCurrentBestTurns() {
        return bestTurnsMap.get(getCurrentLevel());
    }
    public static int getCurrentBestLOC() {
        return bestLOCMap.get(getCurrentLevel());
    }

    public static boolean isStackOverflow() {
        return isStackOverflow;
    }

    public static int getAmountOfKnights() {
        return amountOfKnights;
    }
}
