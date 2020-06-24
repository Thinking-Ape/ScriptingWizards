package main.model;

import main.model.gamemap.enums.CellFlag;
import main.model.gamemap.enums.CellContent;
import main.model.gamemap.enums.ItemType;
import main.model.gamemap.Cell;
import main.model.gamemap.GameMap;
import main.model.statement.ComplexStatement;
import main.model.statement.Statement;
import main.model.statement.StatementIterator;
import main.utility.Util;

import java.util.*;

import static main.model.GameConstants.FALSE_STATEMENT;
import static main.model.GameConstants.NO_ENTITY;

public class Model {
    private List<Level> levelList = new ArrayList<>();
    private int currentLevelIndex = 0;

    private LevelChangeSender levelChangeSender;
    private List<Integer> unlockedLevelIdList = new ArrayList<>();
    private List<String> unlockedStatementsList = new ArrayList<>();
    private GameMap currentMap;
    private ComplexStatement playerBehaviour;

    private int turnsTaken = 0;
    private int skeletonsSpawned = 0;
    private int knightsSpawned = 0;
    private boolean isLost = false;
    private boolean isWon = false;
    private boolean aiFinished = false;
    private boolean isStackOverflow = false;

    private Map<Integer,List<String>> bestCodeMap;
    private Map<Integer,Integer> bestTurnsMap;
    private Map<Integer,Integer> bestLOCMap;
    private int tutorialProgress = -1;

    private int currentTutorialMessageIndex = 0;

    private StatementIterator playerIterator;
    private StatementIterator aiIterator;

    private CodeEvaluator playerEvaluator;
    private CodeEvaluator aiEvaluator;

    // used to ensure levels with random outcomes are not completed by chance
    private Map<Integer,List<Integer>> aiLineRandIntMap = new HashMap<>();
    private int currentRound = 1;

    private static Model modelSingleton = null;
    private boolean editorUnlocked;

    public static Model getInstance(){
        if(modelSingleton == null) modelSingleton = new Model();
        return modelSingleton;
    }

    //needs to be run AFTER Levels have been added!
    public void init(Map<Integer,List<String>> bestCodeMap, Map<Integer,Integer> bestTurnsMap, Map<Integer,Integer> bestLOCMap, int tutorialProgress,
                     List<Integer> unlockedLevelsList, List<String> unlockedStatementsList, boolean editorUnlocked){
        this.bestCodeMap = bestCodeMap;
        this.bestLOCMap = bestLOCMap;
        this.bestTurnsMap = bestTurnsMap;
        this.tutorialProgress = tutorialProgress;
        this.unlockedLevelIdList = unlockedLevelsList;
        if(unlockedLevelsList.size()==0)unlockedLevelsList.add(levelList.get(0).getID());
        this.unlockedStatementsList = unlockedStatementsList;
        this.editorUnlocked = editorUnlocked;
    }

    public void addLevelLast(Level level, boolean isNew) {
        addLevel(levelList.size(),level,isNew);
    }
    private void addLevel(int i,Level level, boolean isNew){
        if(i == levelList.size())levelList.add(level);
        else levelList.add(i,level);
        if(isNew) {
            selectLevel(level.getName());
            levelChangeSender.levelChanged(true);
        }
    }

    public void selectLevel(int index) {
        currentLevelIndex = index;
        currentTutorialMessageIndex = 0;
        currentMap = getCurrentLevel().getOriginalMapCopy();
        levelChangeSender.levelChanged(false);
    }
    public void selectLevel(String name){
        selectLevel(getIndexOfLevelInList(name));
    }

    public void removeCurrentLevel() {
        for(Level l : levelList){
            List<Integer> reqLevels = new ArrayList<>();
            for(int id : l.getRequiredLevelIdsCopy()){
                if(id != getCurrentLevel().getID())reqLevels.add(id);
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

    public void initLevelChangeListener(LevelChangeListener levelChangeListener) {
       if(levelChangeSender == null) levelChangeSender = new LevelChangeSender(levelChangeListener);
       else throw new IllegalStateException("LevelChangeSender has already been initialized!");
    }


    public void reloadCurrentLevel() {
        if(levelChangeSender.isLevelNew()){
            removeCurrentLevel();
        }
        else {
            List<LevelChange> resets = levelChangeSender.resetChanges();
            for(LevelChange l : resets)updateLevelValues(l.getLevelDataType(),l.getOldValue());
        }
    }

    private void updateLevelValues(LevelDataType levelDataType, Object value) {
        switch (levelDataType){
            case AMOUNT_OF_RERUNS:
                getCurrentLevel().setAmountOfPlays((int)value);
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
                String name = (String) value;
                getCurrentLevel().setName(name);
                break;
        }
    }

    private void moveCurrentLevel(int targetIndex) {
        int step = 1;
        if(targetIndex<currentLevelIndex)step = -1;
        for(int i = currentLevelIndex; i != targetIndex; i+=step){
            if(step==1)moveCurrentLevelUp();
            else moveCurrentLevelDown();
        }
    }

    private void moveCurrentLevelDown() {
        Level tempLevel = levelList.get(currentLevelIndex-1);
        levelList.set(currentLevelIndex-1, getCurrentLevel());
        levelList.set(currentLevelIndex, tempLevel);
        currentLevelIndex--;
    }

    private void moveCurrentLevelUp() {
        Level tempLevel = levelList.get(currentLevelIndex+1);
        levelList.set(currentLevelIndex+1, getCurrentLevel());
        levelList.set(currentLevelIndex, tempLevel);
        currentLevelIndex++;
    }

    public void changeCurrentLevel(LevelDataType levelDataType, Object newValue){
        Object oldValue;
        switch (levelDataType){
            case AMOUNT_OF_RERUNS:
                oldValue = getCurrentLevel().getAmountOfPlays();
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
                oldValue = getCurrentLevel().getTutorialMessagesCopy();
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

    public Map<LevelDataType,LevelChange> getAndConfirmCurrentChanges() {
        Map<LevelDataType,LevelChange> output = levelChangeSender.getAndConfirmChanges();
        if(output.containsKey(LevelDataType.IS_TUTORIAL)){
            if((boolean)output.get(LevelDataType.IS_TUTORIAL).getNewValue())
                output.put(LevelDataType.REQUIRED_LEVELS,new LevelChange(LevelDataType.REQUIRED_LEVELS,getCurrentLevel().getRequiredLevelIdsCopy(),new ArrayList<>()));
        }
        return output;
    }

    public void nextTutorialMessage() {
        if(currentTutorialMessageIndex < getCurrentLevel().getTutorialMessagesCopy().size()-1)
            currentTutorialMessageIndex++;
    }

    public void prevTutorialMessage() {
        if(currentTutorialMessageIndex >0)
            currentTutorialMessageIndex--;
    }

    public Statement[] executeTurn() {
        turnsTaken++;
        int noStackOverflow = 0;
        removeTemporaryFlags();
        boolean playerCalledMethod = false, aiCalledMethod = false;
        Statement playerStatement = null;
        Statement aiStatement = null;
        isStackOverflow = false;

        while(!playerCalledMethod && !isWon && !isLost) {
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
            playerCalledMethod = CodeExecutor.executeBehaviour(evaluatedStatement,currentMap, true, canSpawnKnights);
            if(CodeExecutor.knightWasSpawned())knightsSpawned++;
            isWon = CodeExecutor.hasWon();
            isLost = CodeExecutor.hasLost() || isLost;
            // this should be impossible!
            if(currentMap.findSpawn().getX() != -1)
            // no need to set the flag more than once!
            if(!currentMap.cellHasFlag(currentMap.findSpawn(), CellFlag.DEACTIVATED))
            if(getAmountOfKnightsSpawned() == getCurrentLevel().getMaxKnights())
            currentMap.setFlag(currentMap.findSpawn(), CellFlag.DEACTIVATED,true);
        }
        boolean hasAI = getCurrentLevel().hasAi();
        if(hasAI && GameConstants.IS_AI_ACTIVE)
        while(!aiCalledMethod && !isWon && !aiFinished) {
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

            aiCalledMethod = CodeExecutor.executeBehaviour(evaluatedStatement,currentMap,false, false);
            if(CodeExecutor.skeletonWasSpawned())skeletonsSpawned++;
        }
        applyGameLogicToCells();
        return new Statement[]{playerStatement,aiStatement};
    }
    
    private void removeTemporaryFlags() {
        for(int x = 0; x < currentMap.getBoundX(); x++)for(int y = 0; y < currentMap.getBoundY(); y++){
            final Cell cell = currentMap.getCellAtXYClone(x,y);
            for(CellFlag flag : CellFlag.values()){
                if(flag.isTemporary() && cell.hasFlag(flag))
                    currentMap.setFlag(x, y, flag,false);
            }
        }
    }


    private void applyGameLogicToCells() {
        for(int x = 0; x < currentMap.getBoundX(); x++)
            for(int y = 0; y < currentMap.getBoundY(); y++) {
                final Cell cell = currentMap.getCellAtXYClone(x,y);
                CellContent content = currentMap.getContentAtXY(x,y);
                if (content == CellContent.PRESSURE_PLATE) {
                    boolean invertedAndNotFree = (cell.getEntity() == NO_ENTITY && cell.getItem() != ItemType.BOULDER) && cell.hasFlag(CellFlag.INVERTED);
                    boolean notInvertedAndFree = (cell.getEntity() != NO_ENTITY || cell.getItem() == ItemType.BOULDER) && !cell.hasFlag(CellFlag.INVERTED);
                    if (invertedAndNotFree || notInvertedAndFree) currentMap.setFlag(x, y, CellFlag.TRIGGERED, true);
                    else currentMap.setFlag(x, y, CellFlag.TRIGGERED, false);
                }

                if (content == CellContent.ENEMY_SPAWN) {
                    if(CodeExecutor.skeletonWasSpawned()){
                        currentMap.setFlag(x, y, CellFlag.CHANGE_COLOR, true);
                    }
                }
            }
        for(int x = 0; x < currentMap.getBoundX(); x++)for(int y = 0; y < currentMap.getBoundY(); y++){
            final Cell cell = currentMap.getCellAtXYClone(x,y);
            CellContent content = currentMap.getContentAtXY(x,y);
            boolean notAllTriggered = false;
            if(content == CellContent.GATE){
                for (int i = 0; i < cell.getLinkedCellsSize();i++){
                    if(!currentMap.findCellWithId(currentMap.getLinkedCellId(x,y,i)).hasFlag(CellFlag.TRIGGERED)){
                        if(!currentMap.cellHasFlag(x, y, CellFlag.INVERTED))
                            currentMap.kill(x,y);

                        currentMap.setFlag(x,y, CellFlag.OPEN,false);
                        notAllTriggered = true;
                    }
                }
                if(!notAllTriggered&&cell.getLinkedCellsSize()>0){
                    if(currentMap.cellHasFlag(x, y, CellFlag.INVERTED))
                        currentMap.kill(x,y);
                    currentMap.setFlag(x,y, CellFlag.OPEN,true);
                }
            }

            if(cell.getContent() == CellContent.TRAP && currentMap.getItem(x,y) != ItemType.BOULDER){
                if(cell.hasFlag(CellFlag.PREPARING)&&cell.hasFlag(CellFlag.ARMED))
                    throw new IllegalStateException("A cell is not allowed to have more than 1 of these flags: armed or preparing!");
                if(cell.hasFlag(CellFlag.PREPARING)){
                    currentMap.setFlag(x,y, CellFlag.PREPARING,false);
                    currentMap.setFlag(x,y, CellFlag.ARMED,true);
                    currentMap.kill(x,y);
                }else if(cell.hasFlag(CellFlag.ARMED)){
                    currentMap.setFlag(x,y, CellFlag.ARMED,false);
                } else currentMap.setFlag(x,y, CellFlag.PREPARING,true);}
        }
    }

    public void setCurrentPlayerBehaviour(ComplexStatement playerBehaviour) {
        this.playerBehaviour = playerBehaviour;
    }

    public void reset() {
        resetForNextRound();
        aiLineRandIntMap = new HashMap<>();
        currentRound = 1;
    }

    public void resetForNextRound() {
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

    public void updateUnlockedLevelsList(boolean isEditor) {

        int foundLevels = 0;
        // If the current level was saved from within the LevelEditor -> Maybe Required Levels was changed
        // -> Unlock non-Tutoriallevels whose requirements are met

        if(isEditor) {
            int requiredLevelsSize = 0;
            for(Integer id : getCurrentLevel().getRequiredLevelIdsCopy()){
                if (this.getIndexOfLevelWithId(id) < this.getCurrentIndex())requiredLevelsSize++;
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
            if(!bestLOCMap.containsKey(getCurrentId())||bestLOCMap.get(getCurrentId()).equals(-1))return;

            int i = 0;
            for(Level l : levelList){
                int requiredLevelsSize = 0;
                for(Integer id : l.getRequiredLevelIdsCopy()){
                    if (this.getIndexOfLevelWithId(id) < this.getIndexOfLevelWithId(l.getID()))requiredLevelsSize++;
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
                    if(!unlockedLevelIdList.contains(l.getID())) unlockedLevelIdList.add(l.getID());
                // Unlock next tutorial if this level is a tutorial
                // A check whether this level is a tutorial is not needed as in that case (getNextTutorialIndex()==i) is false
                if((l.isTutorial() && getNextTutorialIndex()==i))
                    if(!unlockedLevelIdList.contains(l.getID())) unlockedLevelIdList.add(l.getID());
                i++;
            }
        }
    }

    public boolean putStatsIfBetter(int loc, int turns, double nStars) {
        int bestLoc = -1;
        int bestTurns = -1;
        if(bestLOCMap.containsKey(getCurrentId())){
            bestLoc =bestLOCMap.get(getCurrentId());
            bestTurnsMap.get(getCurrentId());
        }
        // new result worse than existing one
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

    public void resetScoreOfCurrentLevel() {
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

    public void resetTutorialIndex() {
        currentTutorialMessageIndex = 0;
    }

    public void initIteratorsAndEvaluators(ComplexStatement playerBehaviour,ComplexStatement aiBehaviour) {
        aiIterator = aiBehaviour.iterator();
        aiEvaluator = new CodeEvaluator(false);
        playerIterator = playerBehaviour.iterator();
        playerEvaluator = new CodeEvaluator(true);
    }

    public void increaseTutorialMessageIndex() {
        currentTutorialMessageIndex++;
    }
    public void decreaseTutorialMessageIndex() {
        currentTutorialMessageIndex--;
    }

    public void resetLevelNew() {
        levelChangeSender.resetLevelNew();
    }

    public void addLevelAtCurrentPos(Level level, boolean b) {
        if(currentLevelIndex == levelList.size()-1)addLevelLast(level, b);
        else addLevel(getCurrentIndex()+1, level, b);
    }

    public void setTutorialProgress(int nextIndex) {
        tutorialProgress = nextIndex;
    }

    public void increaseCurrentRound() {
        currentRound++;
    }


    public void updateUnlockedStatements() {
        for(String unlock : playerEvaluator.getUnlockedStatements()){
            if(this.unlockedStatementsList.contains(unlock))continue;
            this.unlockedStatementsList.add(unlock);
        }
    }

    public void unlockEditor() {
        editorUnlocked = true;
    }

    // Getters

    public int getTutorialProgress() {
        return tutorialProgress;
    }

    public List<String> getCurrentlyBestCode() {
        return getBestCodeOfLevel(getCurrentId());
    }

    public List<Integer> getUnlockedLevelIds() {
        List<Integer> unlockedLevelIds = new ArrayList<>(unlockedLevelIdList);
        return unlockedLevelIds;
    }

    public int getBestLocOfLevel(int i) {
        int output = -1;
        if(bestLOCMap.get(i)!=null)output = bestLOCMap.get(i);
        return output;
    }

    public int getBestTurnsOfLevel(int i) {
        int output = -1;
        if(bestTurnsMap.get(i)!=null)output = bestTurnsMap.get(i);
        return output;
    }

    public GameMap getCurrentMapCopy(){
        return currentMap.copy();
    }

    public List<String> getBestCodeOfLevel(int i) {
        return bestCodeMap.get(i);
    }

    public boolean isCurrentLevelNew() {
        return levelChangeSender.isLevelNew();
    }

    public String getCurrentTutorialMessage() {
        return getCurrentLevel().getTutorialMessagesCopy().get(currentTutorialMessageIndex);
    }

    public int getAmountOfTutorials() {
        int output = 0;
        for(Level l : levelList){
            if(l.isTutorial())output++;
        }
        return output;
    }

    public int getNextTutorialIndex() {
        int output = 0;
        for(Level l : levelList){
                if(l.isTutorial()&&output>currentLevelIndex)return output;
                output++;
            }
        return -1;
    }


    public Integer createUniqueId() {
        int maxId = levelList.get(0).getID();
        for(Level l : levelList){
            if(l.getID() > maxId)maxId = l.getID();
        }
        return maxId+1;
    }

    public String getNameOfLevelWithId(int levelId) {
        if(levelId == -1)return null;
        for(Level l : levelList){
            if(l.getID() == levelId)return l.getName();
        }
        return null;
    }

    public Integer getIdOfLevelWithName(String levelName) {
        for(Level l : levelList){
            if(l.getName().equals(levelName))return l.getID();
        }
        return -1;
    }

    List<Integer> getOrderedIds() {
        List<Integer> output = new ArrayList<>();
        for(Level l : levelList){
            output.add(l.getID());
        }
        return output;
    }

    public Integer getCurrentId() {
        return getCurrentLevel().getID();
    }

    public List<String> getCurrentRequiredLevels() {
        List<String> output = new ArrayList<>();
        for(Integer id : getCurrentLevel().getRequiredLevelIdsCopy()){
            if(this.getIndexOfLevelWithId(id) < currentLevelIndex)output.add(getNameOfLevelWithId(id));
        }
        return output;
    }

    int getDifferentRandomNumberEachTime(int bnd1, int bnd2) {
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

    public int getCurrentRound() {
        return currentRound;
    }


    public boolean isLost() {
        return isLost;
    }

    public boolean aiIsFinished() {
        return aiFinished;
    }

    public boolean isWon() {
        return isWon;
    }

    public int getTurnsTaken() {
        return turnsTaken;
    }

    public boolean isStackOverflow() {
        return isStackOverflow;
    }

    int getAmountOfKnightsSpawned() {
        return knightsSpawned;
    }
    int getAmountOfSkeletonsSpawned() {
        return skeletonsSpawned;
    }


    public int getCurrentIndex() {
        return currentLevelIndex;
    }

    public Object getDataFromLevelWithIndex(LevelDataType dataType, int index){
        Level level = levelList.get(index);
        switch (dataType){
            case AMOUNT_OF_RERUNS:
                return level.getAmountOfPlays();
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
                return level.getTutorialMessagesCopy();
            case LEVEL_NAME:
                return level.getName();
            case HAS_AI:
                return level.hasAi();
        }
        return null;
    }
    public Object getDataFromCurrentLevel(LevelDataType dataType){
        return getDataFromLevelWithIndex(dataType, currentLevelIndex);
    }

    public int getCurrentTutorialMessageIndex() {
        return currentTutorialMessageIndex;
    }

    public int getCurrentTutorialSize() {
        return getCurrentLevel().getTutorialMessagesCopy().size();
    }

    public List<String> getUnlockedStatementList() {
        return new ArrayList<>(unlockedStatementsList);
    }

    String getNameOfLevelWithIndex(int i) {
        return levelList.get(i).getName();
    }

    public boolean hasLevelWithName(String t1) {
        for(Level l : levelList){
            if(l.getName().equals(t1))return true;
        }
        return false;
    }

    public int getAmountOfLevels() {
        return levelList.size();
    }

    int getIndexOfLevelInList(String levelName) {
        for(int i = 0; i < levelList.size(); i++){
            Level l = levelList.get(i);
            if(l.getName().equals(levelName))return i;
        }
        return -1;
    }
    public int getIndexOfLevelWithId(int id) {
        if(id == -1)return -1;
        for(int i = 0; i < levelList.size(); i++){
            Level l = levelList.get(i);
            if(l.getID()==id)return i;
        }
        return -1;
    }

    public boolean currentLevelHasChanged() {
        return levelChangeSender.hasChanged() || levelChangeSender.isLevelNew();
    }
    private Level getCurrentLevel() {
        return levelList.get(currentLevelIndex);
    }

    public int getAmountOfCompletedLevels() {
        int amountOfCompletedLevels = 0;
        for(int id : getUnlockedLevelIds()){
            if(bestLOCMap.get(id)>-1 && bestTurnsMap.get(id)>-1)amountOfCompletedLevels++;
        }
        return amountOfCompletedLevels;
    }
    public boolean isEditorUnlocked() {
        return editorUnlocked;
    }

}
