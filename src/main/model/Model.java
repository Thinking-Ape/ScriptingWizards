package main.model;

import main.model.enums.CFlag;
import main.model.enums.CellContent;
import main.model.enums.ItemType;
import main.model.gamemap.Cell;
import main.model.gamemap.GameMap;
import main.model.statement.ComplexStatement;
import main.model.statement.MethodDeclaration;
import main.model.statement.Statement;
import main.model.statement.StatementIterator;
import main.utility.GameConstants;
import main.utility.Util;
import main.utility.Variable;

import java.util.*;

import static main.utility.GameConstants.FALSE_STATEMENT;
import static main.utility.GameConstants.NO_ENTITY;

public abstract class Model {
    private static List<Level> levelList = new ArrayList<>();
    private static int currentLevelIndex = 0;
    private static LevelChangeSender levelChangeSender;

    //TODO:!!!!!
    private static List<Integer> unlockedLevelIdList = new ArrayList<>();
    private static List<String> unlockedStatementsList = new ArrayList<>();
    private static List<MethodDeclaration> createdMethodsList = new ArrayList<>();
    private static GameMap currentMap;
    private static ComplexStatement playerBehaviour;

    private static int turnsTaken = 0;
    private static int skeletonsSpawned = 0;
    private static int knightsSpawned = 0;
    private static boolean isLost = false;
    private static boolean isWon = false;
    private static boolean aiFinished = false;
    private static boolean isStackOverflow = false;

    private static Map<Integer,List<String>> bestCodeMap;
    private static Map<Integer,Integer> bestTurnsMap;
    private static Map<Integer,Integer> bestLOCMap;
    private static int tutorialProgress = -1;

    private static int currentTutorialMessageIndex = 0;

    private static StatementIterator playerIterator;
    private static StatementIterator aiIterator;

    private static CodeEvaluator playerEvaluator;
    private static CodeEvaluator aiEvaluator;

    // attempt to ensure aps with random outcomes are not cheated
    private static Map<Integer,List<Integer>> aiLineRandIntMap = new HashMap<>();
    private static int currentRound = 1;

    //needs to be run AFTER Levels have been added!
    public static void init(Map<Integer,List<String>> bestCodeMap, Map<Integer,Integer> bestTurnsMap, Map<Integer,Integer> bestLOCMap, int tutorialProgress,
                            List<Integer> unlockedLevelsList, List<String> unlockedStatementsList ){
        Model.bestCodeMap = bestCodeMap;
        Model.bestLOCMap = bestLOCMap;
        Model.bestTurnsMap = bestTurnsMap;
        Model.tutorialProgress = tutorialProgress;
        Model.unlockedLevelIdList = unlockedLevelsList;
        if(unlockedLevelsList.size()==0)unlockedLevelsList.add(levelList.get(0).getId());
        Model.unlockedStatementsList = unlockedStatementsList;
        // not implemented at the moment:
//        Model.createdMethodsList = new ArrayList<>();
    }

    private static Level getCurrentLevel() {
        return levelList.get(currentLevelIndex);
    }

    public static void addLevelLast(Level level, boolean isNew) {
        addLevel(levelList.size(),level,isNew);
    }
    private static  void addLevel(int i,Level level, boolean isNew){
        if(i == levelList.size())levelList.add(level);
        else levelList.add(i,level);
        if(isNew) {
            selectLevel(level.getName());
            levelChangeSender.levelChanged(true);
        }
    }

    public static void selectLevel(int index) {
        currentLevelIndex = index;
        currentTutorialMessageIndex = 0;
        currentMap = getCurrentLevel().getOriginalMapCopy();
        levelChangeSender.levelChanged(false);
    }
    public static void selectLevel(String name){
        selectLevel(getIndexOfLevelInList(name));
    }

    public static void removeCurrentLevel() {
        for(Level l : levelList){
            List<Integer> reqLevels = new ArrayList<>();
            for(int id : l.getRequiredLevelIdsCopy()){
                if(id != getCurrentLevel().getId())reqLevels.add(id);
            }
            l.setRequiredLevelIds(reqLevels);
        }
        unlockedLevelIdList.remove(getCurrentId());
        bestTurnsMap.remove(getCurrentId());
        bestCodeMap.remove(getCurrentId());
        bestLOCMap.remove(getCurrentId());
        levelList.remove(currentLevelIndex);
        if(currentLevelIndex != 0)currentLevelIndex--;
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
    public static int getIndexOfLevelWithId(int id) {
        if(id == -1)return -1;
        for(int i = 0; i < levelList.size(); i++){
            Level l = levelList.get(i);
            if(l.getId()==id)return i;
        }
        return -1;
    }

    public static void initLevelChangeListener(LevelChangeListener levelChangeListener) {
       if(levelChangeSender == null) levelChangeSender = new LevelChangeSender(levelChangeListener);
       else throw new IllegalStateException("LevelChangeSender has already been initialized!");
    }


    public static void reloadCurrentLevel() {
        if(levelChangeSender.isLevelNew()){
            removeCurrentLevel();
        }
        else {
            List<LevelChange> resets = levelChangeSender.resetChanges();
            for(LevelChange l : resets)updateLevelValues(l.getLevelDataType(),l.getOldValue());
        }
    }

    private static void updateLevelValues(LevelDataType levelDataType, Object value) {
        switch (levelDataType){
            case AMOUNT_OF_RERUNS:
                getCurrentLevel().setAmountOfReruns((int)value);
                break;
            case LEVEL_INDEX:
                int oldIndex = (int)value;
                moveCurrentLevel(oldIndex);
                break;
            case MAX_KNIGHTS:
                getCurrentLevel().setMaxKnights((int)value);
                break;
            case MAP_DATA:
                getCurrentLevel().setGameMap((GameMap)value);
                currentMap = getCurrentLevel().getOriginalMapCopy();
                break;
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
                getCurrentLevel().setRequiredLevelIds((List<Integer>) value);
                break;
            case IS_TUTORIAL:
                getCurrentLevel().setIsTutorial((boolean) value);

                break;
            case TUTORIAL_LINES:
                getCurrentLevel().setTutorialMessages((List<String>) value);
                break;
            case LEVEL_NAME:
//                String oldName = getCurrentLevel().getName();
                String name = (String) value;
//                for(Level l : levelList){
//                    if(l.getRequiredLevelIdsCopy().contains(oldName)){
//                        l.getRequiredLevelIdsCopy().remove(oldName);
//                        l.getRequiredLevelIdsCopy().add(name);
//                    }
//                }
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

    private static void moveCurrentLevelDown() {
        Level tempLevel = levelList.get(currentLevelIndex-1);
//        getCurrentLevel().removeRequiredLevelId(tempLevel.getId());
        levelList.set(currentLevelIndex-1, getCurrentLevel());
        levelList.set(currentLevelIndex, tempLevel);
        currentLevelIndex--;
    }

    private static void moveCurrentLevelUp() {
        Level tempLevel = levelList.get(currentLevelIndex+1);
//        tempLevel.removeRequiredLevelId(getCurrentLevel().getId());
        levelList.set(currentLevelIndex+1, getCurrentLevel());
        levelList.set(currentLevelIndex, tempLevel);
        currentLevelIndex++;
    }

    public static void changeCurrentLevel(LevelDataType levelDataType, Object newValue){
        Object oldValue = null;
        switch (levelDataType){
            case AMOUNT_OF_RERUNS:
                oldValue = getCurrentLevel().getAmountOfReruns();
                break;
            case LEVEL_INDEX:
                oldValue = currentLevelIndex;
                break;
            case MAX_KNIGHTS:
                oldValue = getCurrentLevel().getMaxKnights();
                break;
            case MAP_DATA:
                oldValue = getCurrentLevel().getOriginalMapCopy();
                break;
            case AI_CODE:
                oldValue = getCurrentLevel().getAIBehaviourCopy();
                break;
            case HAS_AI:
                oldValue = getCurrentLevel().hasAi();
                break;
            case LOC_TO_STARS:
                oldValue = getCurrentLevel().getLocToStarsCopy();
                break;
            case TURNS_TO_STARS:
                oldValue = getCurrentLevel().getTurnsToStarsCopy();
                break;
            case REQUIRED_LEVELS:
                oldValue = getCurrentLevel().getRequiredLevelIdsCopy();
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
        if(levelDataType == LevelDataType.TURNS_TO_STARS || levelDataType == LevelDataType.LOC_TO_STARS){
            Integer[] oldToStars = ((Integer[])oldValue);
            Integer[] newToStars = ((Integer[])newValue);
            if(oldToStars[0].equals(newToStars[0])&&oldToStars[1].equals(newToStars[1]))return;
        }
        if(newValue.equals(oldValue))return;
        LevelChange levelChange = new LevelChange(levelDataType, oldValue, newValue);
        updateLevelValues(levelChange.getLevelDataType(), levelChange.getNewValue());
        levelChangeSender.addLevelChange(levelChange);
    }

    public static boolean currentLevelHasChanged() {
        return levelChangeSender.hasChanged() || levelChangeSender.isLevelNew();
    }

    public static Map<LevelDataType,LevelChange> getAndConfirmCurrentChanges() {
        Map<LevelDataType,LevelChange> output = levelChangeSender.getAndConfirmChanges();
        if(output.containsKey(LevelDataType.LEVEL_NAME)){
            LevelChange change = output.get(LevelDataType.LEVEL_NAME);
//            String name = getCurrentLevel().getName();
//            String oldName = (String) change.getOldValue();
//            for(Level l : levelList){
//                if(l.getRequiredLevelIdsCopy().contains(oldName)){
//                    l.getRequiredLevelIdsCopy().remove(oldName);
//                    l.getRequiredLevelIdsCopy().add(name);
//                }
//            }
        }
        if(output.containsKey(LevelDataType.LEVEL_INDEX)){

        }
        if(output.containsKey(LevelDataType.IS_TUTORIAL)){
            if((boolean)output.get(LevelDataType.IS_TUTORIAL).getNewValue())
                output.put(LevelDataType.REQUIRED_LEVELS,new LevelChange(LevelDataType.REQUIRED_LEVELS,getCurrentLevel().getRequiredLevelIdsCopy(),new ArrayList<>()));
        }
        return output;
    }

    public static int getCurrentIndex() {
        return currentLevelIndex;
    }

    public static Object getDataFromLevelWithIndex(LevelDataType dataType, int index){
        Level level = levelList.get(index);
        switch (dataType){
            case AMOUNT_OF_RERUNS:
                return level.getAmountOfReruns();
            case LEVEL_INDEX:
                return index;
            case MAX_KNIGHTS:
                return level.getMaxKnights();
            case MAP_DATA:
                return level.getOriginalMapCopy();
            case AI_CODE:
                return level.getAIBehaviourCopy();
            case LOC_TO_STARS:
                return level.getLocToStarsCopy();
            case TURNS_TO_STARS:
                return level.getTurnsToStarsCopy();
            case REQUIRED_LEVELS:
                return level.getRequiredLevelIdsCopy();
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
        if(currentTutorialMessageIndex < getCurrentLevel().getTutorialEntryListCopy().size()-1)
            currentTutorialMessageIndex++;
    }

    public static int getCurrentTutorialMessageIndex() {
        return currentTutorialMessageIndex;
    }

    public static void prevTutorialMessage() {
        if(currentTutorialMessageIndex >0)
            currentTutorialMessageIndex--;
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

    private static Level getLevelWithName(String name) {
        for(Level l : levelList){
            if(l.getName().equals(name))return l;
        }
        return null;
    }

    public static Statement[] executeTurn() {
        turnsTaken++;
        int noStackOverflow = 0;
        removeTemporaryFlags();
        boolean method_Called_Player = false, method_Called_AI = false;
        Statement playerStatement = null;
        Statement aiStatement = null;
        isStackOverflow = false;

        while(!method_Called_Player && !isWon && !isLost) {
            Statement evaluatedStatement;
            playerStatement = playerIterator.next();
            evaluatedStatement = playerEvaluator.evaluateStatement(playerStatement);
            if(evaluatedStatement==null){
                isLost=true;
                break;
            }
            if(evaluatedStatement == FALSE_STATEMENT)
                playerIterator.skip(playerStatement.getDepth()-1);
            noStackOverflow++;
            if(noStackOverflow > GameConstants.MAX_LOOP_SIZE ){
                isStackOverflow = true;
                isLost = true;
                break;
            }
            // If the evaluated Statement is complex there is nothing to execute!
            if(evaluatedStatement.isComplex())continue;

            boolean canSpawnKnights = getAmountOfKnightsSpawned() < getCurrentLevel().getMaxKnights();
            method_Called_Player = CodeExecutor.executeBehaviour(evaluatedStatement,currentMap, true, canSpawnKnights);
            if(CodeExecutor.knightWasSpawned())knightsSpawned++;
            isWon = CodeExecutor.hasWon();
            isLost = CodeExecutor.hasLost() || isLost;
            // this should be impossible!
            if(currentMap.findSpawn().getX() != -1)
            // no need to set the flag more than once!
            if(!currentMap.cellHasFlag(currentMap.findSpawn(), CFlag.DEACTIVATED))
            if(getAmountOfKnightsSpawned() == getCurrentLevel().getMaxKnights())
            currentMap.setFlag(currentMap.findSpawn(), CFlag.DEACTIVATED,true);
        }
        if(getCurrentLevel().hasAi() && GameConstants.IS_AI_ACTIVE)
        while(!method_Called_AI && !isWon && !aiFinished) {
            Statement evaluatedStatement;
            aiStatement = aiIterator.next();
            evaluatedStatement = aiEvaluator.evaluateStatement(aiStatement);
            if (evaluatedStatement == null) {
                aiFinished = true;
                break;
            }
            if(evaluatedStatement == FALSE_STATEMENT)
                aiIterator.skip(aiStatement.getDepth()-1);

            noStackOverflow++;
            if(noStackOverflow > GameConstants.MAX_LOOP_SIZE ){
                isStackOverflow = true;
                isLost = true;
                break;
            }
            // If the evaluated Statement is complex there is nothing to execute!
            if(evaluatedStatement.isComplex())continue;

            method_Called_AI = CodeExecutor.executeBehaviour(evaluatedStatement,currentMap,false, false);
            if(CodeExecutor.skeletonWasSpawned())skeletonsSpawned++;
        }
        applyGameLogicToCells();
        return new Statement[]{playerStatement,aiStatement};
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
        playerIterator = new StatementIterator(playerBehaviour, false);
        playerEvaluator = new CodeEvaluator(true);
        Model.playerBehaviour = playerBehaviour;
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

//    public static int getCurrentBestTurns() {
//        return getBestTurnsOfLevel(currentLevelIndex);
//    }
//    public static int getCurrentBestLOC() {
//        return getBestLocOfLevel(currentLevelIndex);
//    }

    public static boolean isStackOverflow() {
        return isStackOverflow;
    }

    public static int getAmountOfKnightsSpawned() {
        return knightsSpawned;
    }

    public static void reset() {
        resetForNextRound();
        aiLineRandIntMap = new HashMap<>();
        currentRound = 1;
//        if(getCurrentLevel().hasAi())
//        if(currentAiBehaviour!=null)
//            currentAiBehaviour.resetVariables(true);
    }

    public static void resetForNextRound() {
        turnsTaken = 0;
        knightsSpawned = 0;
        skeletonsSpawned = 0;
        isLost = false;
        isWon = false;
        aiFinished = false;
        isStackOverflow = false;
        currentMap = getCurrentLevel().getOriginalMapCopy();
        CodeExecutor.reset();
    }

    public static int getAmountOfSkeletonsSpawned() {
        return skeletonsSpawned;
    }

    public static void updateUnlockedLevelsList(boolean isEditor) {

        int foundLevels = 0;
        // If the current level was saved from within the LevelEditor -> Maybe Required Levels was changed
        // -> Unlock non-Tutoriallevels whose requirements are met

        if(isEditor) {
            int requiredLevelsSize = 0;
            for(Integer id : getCurrentLevel().getRequiredLevelIdsCopy()){
                if (Model.getIndexOfLevelWithId(id) < Model.getCurrentIndex())requiredLevelsSize++;
            }
             if(getCurrentLevel().isTutorial())return;
            for (Integer s : getCurrentLevel().getRequiredLevelIdsCopy()) {
                if (bestLOCMap.get(s) != null && bestLOCMap.get(s) > -1) {
                    foundLevels++;
                }
            }
            if (foundLevels >=requiredLevelsSize)
            {
                if (!unlockedLevelIdList.contains(getCurrentId()))
                    unlockedLevelIdList.add(getCurrentId());
            }
            else {
                unlockedLevelIdList.remove(getCurrentId());
            }
        }
        // If the current Level was finished from outside of the LevelEditor -> unlock levels that required it
        else {
            // When completing a level from leveleditor without having it unlocked in DEBUG-Mode
            if(!unlockedLevelIdList.contains(getCurrentId()))return;
                //throw new IllegalStateException("You just completed a level you hadnt even unlocked in the first place!");
            // This also should be impossible!
            if(!bestLOCMap.containsKey(getCurrentId())||bestLOCMap.get(getCurrentId()).equals(-1))return;
//                throw new IllegalStateException("Well this should not have happened! My sincerest apologies!");

            int i = 0;
            for(Level l : levelList){
                int requiredLevelsSize = 0;
                for(Integer id : l.getRequiredLevelIdsCopy()){
                    if (Model.getIndexOfLevelWithId(id) < Model.getIndexOfLevelWithId(l.getId()))requiredLevelsSize++;
                }
                foundLevels = 0;
                // Count all unlocked levels that have a score > -1 (Have been solved already) and are required for a given level
                // If the amount is equal to the amount of required levels -> unlock that level
                for(Integer requiredLevelId : l.getRequiredLevelIdsCopy()){
                    for(int unlockedLevelId : unlockedLevelIdList)
                        if(unlockedLevelId == requiredLevelId && bestLOCMap.containsKey(unlockedLevelId) && bestLOCMap.get(unlockedLevelId)>-1)
                            foundLevels++;
                }
                // Doesnt apply to tutorials
                if(foundLevels >=  requiredLevelsSize&& !l.isTutorial())
                    if(!unlockedLevelIdList.contains(l.getId())) unlockedLevelIdList.add(l.getId());
                // Unlock next tutorial if this level is a tutorial
                // A check whether this level is a tutorial is not needed as in that case (getNextTutorialIndex()==i) is false
                if((l.isTutorial() && getNextTutorialIndex()==i))
                    if(!unlockedLevelIdList.contains(l.getId())) unlockedLevelIdList.add(l.getId());
                i++;
            }
        }
    }
//
//    private static Level getLevelWithId(Integer s) {
//        for(Level l : levelList){
//            if(l.getId() == s)return l;
//        }
//        return null;
//    }

    public static boolean putStatsIfBetter(int loc, int turns, double nStars) {
        int bestLoc = -1;
        int bestTurns = -1;
        if(bestLOCMap.containsKey(getCurrentId())){
            bestLoc =bestLOCMap.get(getCurrentId());
            bestTurnsMap.get(getCurrentId());
        }
        // new result not better than existing one
        if(nStars < Util.calculateStars(bestTurns,bestLoc,getCurrentLevel().getTurnsToStarsCopy() , getCurrentLevel().getLocToStarsCopy()) )return false;

        if(bestLOCMap.containsKey(getCurrentId())){
            bestLOCMap.replace(getCurrentId(),loc);
        } else bestLOCMap.put(getCurrentId(),loc);

        if(bestTurnsMap.containsKey(getCurrentId())){
            bestTurnsMap.replace(getCurrentId(),turns);
        } else bestTurnsMap.put(getCurrentId(),turns);

        if(bestCodeMap.containsKey(getCurrentId())){
            bestCodeMap.replace(getCurrentId(),playerBehaviour.getCodeLines());
        } else bestCodeMap.put(getCurrentId(),playerBehaviour.getCodeLines());
        return true;
    }

    public static int getTutorialProgress() {
        return tutorialProgress;
    }

    public static List<String> getCurrentlyBestCode() {
        return getBestCodeOfLevel(currentLevelIndex);
    }

    public static List<Integer> getUnlockedLevelIds() {
        List<Integer> unlockedLevelIds = new ArrayList<>(unlockedLevelIdList);
        return unlockedLevelIds;
    }

    public static void resetScoreOfCurrentLevel() {
        if(bestLOCMap.containsKey(getCurrentId())){
            bestLOCMap.replace(getCurrentId(),-1);
        } else bestLOCMap.put(getCurrentId(),-1);

        if(bestTurnsMap.containsKey(getCurrentId())){
            bestTurnsMap.replace(getCurrentId(),-1);
        } else bestTurnsMap.put(getCurrentId(),-1);

        if(bestCodeMap.containsKey(getCurrentId())){
            bestCodeMap.replace(getCurrentId(),new ArrayList<>());
        } else bestCodeMap.put(getCurrentId(),new ArrayList<>());
    }

    public static int getBestLocOfLevel(int i) {
        int output = -1;
        if(bestLOCMap.get(i)!=null)output = bestLOCMap.get(i);
        return output;
    }

    public static int getBestTurnsOfLevel(int i) {
        int output = -1;
        if(bestTurnsMap.get(i)!=null)output = bestTurnsMap.get(i);
        return output;
    }

    public static GameMap getCurrentMap(){
        return currentMap.copy();
    }

    public static void addUnlockedStatement(String unlock) {
        if(!unlockedStatementsList.contains(unlock))unlockedStatementsList.add(unlock);
    }

    public static List<String> getBestCodeOfLevel(int i) {
        return bestCodeMap.get(i);
    }

    public static boolean isCurrentLevelNew() {
        return levelChangeSender.isLevelNew();
    }

    public static String getCurrentTutorialMessage() {
        return getCurrentLevel().getTutorialEntryListCopy().get(currentTutorialMessageIndex);
    }

    public static void resetTutorialIndex() {
        currentTutorialMessageIndex = 0;
    }

    public static void initAiIteratorAndEvaluator(ComplexStatement aiBehaviour) {
        aiIterator = new StatementIterator(aiBehaviour, false);
        aiEvaluator = new CodeEvaluator(false);
    }

    public static int getAmountOfTutorials() {
        int output = 0;
        for(Level l : levelList){
            if(l.isTutorial())output++;
        }
        return output;
    }

    public static int getTutorialSlot(String levelName) {
        int output = 0;
        for(Level l : levelList){
            if(levelName.equals(l.getName())){
                if(!l.isTutorial())return -1;
                return output;
            }
            if(l.isTutorial())output++;
        }
        return -1;
    }

    public static int getNextTutorialIndex() {
        int output = 0;
        for(Level l : levelList){
                if(l.isTutorial()&&output>currentLevelIndex)return output;
                output++;
            }
        return -1;
    }

    public static void increaseTutorialMessageIndex() {
        currentTutorialMessageIndex++;
    }
    public static void decreaseTutorialMessageIndex() {
        currentTutorialMessageIndex--;
    }

    public static void resetLevelNew() {
        levelChangeSender.resetLevelNew();
    }

    public static void addLevelAtCurrentPos(Level level, boolean b) {
        if(currentLevelIndex == levelList.size()-1)addLevelLast(level, b);
        else addLevel(getCurrentIndex()+1, level, b);
    }

    public static List<Variable> getVariableListFromMethod(String methodName) {
        for(MethodDeclaration mD : createdMethodsList){
            if(mD.getMethodName().equals(methodName)){
                return mD.getVariableList();
            }
        }
        return null;
    }

    public static void setTutorialProgress(int nextIndex) {
        tutorialProgress = nextIndex;
    }

    public static Integer createUniqueId() {
        int maxId = levelList.get(0).getId();
        for(Level l : levelList){
            if(l.getId() > maxId)maxId = l.getId();
        }
        return maxId+1;
    }

    public static String getNameOfLevelWithId(int levelId) {
        if(levelId == -1)return null;
        for(Level l : levelList){
            if(l.getId() == levelId)return l.getName();
        }
        return null;
    }

    public static Integer getIdOfLevelWithName(String levelName) {
        for(Level l : levelList){
            if(l.getName().equals(levelName))return l.getId();
        }
        return -1;
    }

    public static List<Integer> getOrderedIds() {
        List<Integer> output = new ArrayList<>();
        for(Level l : levelList){
            output.add(l.getId());
        }
        return output;
    }

    public static Integer getCurrentId() {
        return getCurrentLevel().getId();
    }

    public static List<String> getCurrentRequiredLevels() {
        List<String> output = new ArrayList<>();
        for(Integer id : getCurrentLevel().getRequiredLevelIdsCopy()){
            if(Model.getIndexOfLevelWithId(id) < currentLevelIndex)output.add(getNameOfLevelWithId(id));
        }
        return output;
    }

    public static int getDifferentRandomNumberEachTime(int bnd1, int bnd2) {
        int currentAIIndex = aiIterator.getCurrentIndex();
        int output;
        if(aiLineRandIntMap.containsKey(currentAIIndex)){
            output = Util.getRandIntWithout(bnd1,bnd2,aiLineRandIntMap.get(currentAIIndex));
            aiLineRandIntMap.get(currentAIIndex).add(output);
        }
        else {
            output = Util.getRandIntWithout(bnd1,bnd2,new ArrayList<>());
            List<Integer>list = new ArrayList<>();
            list.add(output);
            aiLineRandIntMap.put(currentAIIndex, list);
        }
        return output;
    }

    public static int getCurrentRound() {
        return currentRound;
    }

    public static void increaseCurrentRound() {
        currentRound++;
    }


//    public static void updateSpawnedEntities() {
//        if(CodeExecutor.knightWasSpawned())knightsSpawned++;
//        if(CodeExecutor.skeletonWasSpawned())skeletonsSpawned++;
//    }
}
