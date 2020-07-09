package main.model;

import main.model.gamemap.enums.CellFlag;
import main.model.gamemap.enums.CellContent;
import main.model.gamemap.enums.ItemType;
import main.model.gamemap.Cell;
import main.model.gamemap.GameMap;
import main.model.statement.ComplexStatement;
import main.model.statement.Statement;
import main.model.statement.StatementIterator;
import main.utility.SimpleSet;
import main.utility.Util;
import java.util.*;

import static main.model.GameConstants.*;

public class Model {
    private Set<Level> levelSet = new SimpleSet<>();
//    private int currentLevelIndex = 0;

    //TODO::!!!!!
    //TODO::!!!!!
    //TODO::!!!!!
    //TODO::!!!!!
    //TODO::!!!!!
    //TODO::!!!!!
    //TODO::!!!!!
    private int currentId = 0;

    private LevelChangeSender levelChangeSender;
//    private List<Integer> unlockedLevelIdList = new ArrayList<>();
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

//    private Map<String,Integer> courseNameProgressMap;
    private Map<String,List<Integer>> courseNameToIdListMap;
    private Map<String,LevelDifficulty> courseNameLevelDifficultyMap;

    private Map<Integer,List<String>> bestCodeMap;
    private Map<Integer,Integer> bestTurnsMap;
    private Map<Integer,Integer> bestLOCMap;
    private Map<Integer, Integer> bestKnightsMap;

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
    public void init(Map<Integer,List<String>> bestCodeMap, Map<Integer,Integer> bestTurnsMap, Map<Integer,Integer> bestLOCMap,Map<Integer,Integer> bestKnightsMap,
                     List<String> unlockedStatementsList, boolean editorUnlocked,Map<String,List<Integer>> courseNameToIdListMap, Map<String,LevelDifficulty> courseNameLevelDifficultyMap, List<Level> levelList){
        this.courseNameToIdListMap = courseNameToIdListMap;
        this.courseNameLevelDifficultyMap =courseNameLevelDifficultyMap;
        this.bestCodeMap = bestCodeMap;
        this.bestLOCMap = bestLOCMap;
        this.bestTurnsMap = bestTurnsMap;
        this.bestKnightsMap = bestKnightsMap;
        this.levelSet = new SimpleSet<>(levelList);
//        this.tutorialProgress = tutorialProgress;
//        this.unlockedLevelIdList = unlockedLevelsList;
//        if(unlockedLevelsList.size()==0)unlockedLevelsList.add(levelSet.get(0).getID());
        this.unlockedStatementsList = unlockedStatementsList;
        this.editorUnlocked = editorUnlocked;
//        courseNameProgressMap =  new HashMap<>();
//        for(String courseName : getAllCourseNames()){
//            courseNameProgressMap.put(courseName, calculateProgressOfCourse(courseName));
//        }
    }

//    public void addLevelLast(Level level, boolean isNew) {
//        addLevel(level,isNew);
//    }

    //TODO: fix this mess next 5 methods!
    private void addLevel(int index, Level level, boolean isNew){
        if(isNew) {
            levelSet.add(level);
            bestLOCMap.put(level.getID(), -1);
            bestTurnsMap.put(level.getID(), -1);
            bestCodeMap.put(level.getID(), new ArrayList<>());
            bestKnightsMap.put(level.getID(), -1);
            if(!courseNameToIdListMap.get(getCurrentCourseName()).contains(level.getID()))
                courseNameToIdListMap.get(getCurrentCourseName()).add( level.getID());
            selectLevel(level.getName(),getCurrentCourseName());
            levelChangeSender.levelChanged(true);
        }
    }
    public void selectLevel(int id) {
        selectLevel(id, getCurrentCourseName());
    }
    public void selectLevel(int id,String courseName) {
        currentId = id;
        currentTutorialMessageIndex = 0;
        currentMap = getCurrentLevel().getOriginalMapCopy();
        levelChangeSender.levelChanged(false);
    }
    public void selectLevel(String name,String courseName){
        selectLevel(getIdOfLevelWithName(name),courseName);
    }
    public void selectLevel(String name){
        selectLevel(getIdOfLevelWithName(name),findCourseOfLevelId(getIdOfLevelWithName(name)));
    }

    private String findCourseOfLevelId(Integer idOfLevelWithName) {
        for(String courseName : getAllCourseNames()){
            if(courseNameToIdListMap.get(courseName).contains(idOfLevelWithName))return courseName;
        }
        return CHALLENGE_COURSE_NAME;
    }

    public void removeCurrentLevel() {
        for(Level l : levelSet){
            List<Integer> reqLevels = new ArrayList<>();
            for(int id : l.getRequiredLevelIdsCopy()){
                if(id != getCurrentLevel().getID())reqLevels.add(id);
            }
            l.setRequiredLevelIds(reqLevels);
        }
//        unlockedLevelIdList.remove(getCurrentId());
        Integer idToRemove = getCurrentId();
        String currentCourseName = getCurrentCourseName();
        if(getPrevId() != -1)
            selectLevel(getPrevId(),getCurrentCourseName());
        if(getPrevId() == -1) selectFirstLevelInCourse(getAllCourseNames().get(0));
        bestTurnsMap.remove(idToRemove);
        bestCodeMap.remove(idToRemove);
        bestLOCMap.remove(idToRemove);
        levelSet.remove(getLevelWithId(idToRemove));
        courseNameToIdListMap.get(currentCourseName).remove(idToRemove);
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
//            case IS_TUTORIAL:
//                getCurrentLevel().setIsTutorial((boolean) value);
            case COURSE:
                for(String courseName : courseNameToIdListMap.keySet()){
                    List<Integer> idList = courseNameToIdListMap.get(courseName);
                    if(idList.contains(getCurrentId())){
                        if(!courseName.equals(value))idList.remove(getCurrentId());
                    }
                    else if(courseName.equals(value)){
                        idList.add(getCurrentId());
                    }
                }

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
        if(targetIndex<getCurrentIndex())step = -1;
        for(int i = getCurrentIndex(); i != targetIndex; i+=step){
            if(step==1)moveCurrentLevelUp();
            else moveCurrentLevelDown();
        }
    }

    private void moveCurrentLevelDown() {
        List<Integer> idList = courseNameToIdListMap.get(getCurrentCourseName());
        int index = getIndexOfLevelWithId(currentId);
        int tempId = idList.get(index-1);
        idList.set(index-1, currentId);
        idList.set(index, tempId);
    }

    private void moveCurrentLevelUp() {
        List<Integer> idList = courseNameToIdListMap.get(getCurrentCourseName());
        int index = getIndexOfLevelWithId(currentId);
        int tempId = idList.get(index+1);
        idList.set(index+1, currentId);
        idList.set(index, tempId);
    }

    public void changeCurrentLevel(LevelDataType levelDataType, Object newValue){
        Object oldValue = null;
        switch (levelDataType){
            case AMOUNT_OF_RERUNS:
                oldValue = getCurrentLevel().getAmountOfPlays();
                break;
            case LEVEL_INDEX:
                oldValue = getIndexOfLevelWithId(currentId);
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
//            case IS_TUTORIAL:
//                oldValue = getCurrentLevel().isTutorial();
            case COURSE:
                for(String courseName : courseNameToIdListMap.keySet()){
                    List<Integer> idList = courseNameToIdListMap.get(courseName);
                    if(idList.contains(getCurrentId())){
                        oldValue = courseName;
                    }
                }
                if(oldValue == null)oldValue = GameConstants.CHALLENGE_COURSE_NAME;
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
        //TODO:!!!
//        if(output.containsKey(LevelDataType.IS_TUTORIAL)){
//            if((boolean)output.get(LevelDataType.IS_TUTORIAL).getNewValue())
//                output.put(LevelDataType.REQUIRED_LEVELS,new LevelChange(LevelDataType.REQUIRED_LEVELS,getCurrentLevel().getRequiredLevelIdsCopy(),new ArrayList<>()));
//        }
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

            boolean canSpawnKnights = getAmountOfKnightsSpawned() < getCurrentLevel().getMaxKnights() || !GameConstants.MAX_KNIGHTS_ACTIVATED;
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

    /*public void updateUnlockedLevelsList(boolean isEditor) {

        int foundLevels = 0;
        // If the current level was saved from within the LevelEditor -> Maybe Required Levels was changed
        // -> Unlock non-Tutoriallevels whose requirements are met

        if(isEditor) {
            int requiredLevelsSize = 0;
            for(Integer id : getCurrentLevel().getRequiredLevelIdsCopy()){
                if (this.getIndexOfLevelWithId(id) < this.getCurrentIndex())requiredLevelsSize++;
            }
             if(GameConstants.CHALLENGE_COURSE_NAME.equals(getDataFromCurrentLevel(LevelDataType.COURSE)))return;
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
            for(Level l : levelSet){
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
                boolean levelIsTut = getDataFromLevelWithId(LevelDataType.COURSE,getIndexOfLevelWithId(l.getID()) ).equals(GameConstants.CHALLENGE_COURSE_NAME);
                if(foundLevels >=  requiredLevelsSize&& !levelIsTut)
                    if(!unlockedLevelIdList.contains(l.getID())) unlockedLevelIdList.add(l.getID());
                // Unlock next tutorial if this level is a tutorial
                // A check whether this level is a tutorial is not needed as in that case (getNextTutorialIndex()==i) is false
                if((levelIsTut && getNextTutorialIndex()==i))
                    if(!unlockedLevelIdList.contains(l.getID())) unlockedLevelIdList.add(l.getID());
                i++;
            }
        }
    }*/

    public boolean putStatsIfBetter(int loc, int turns, double nStars) {
        int bestLoc = -1;
        int bestTurns = -1;
        if(bestLOCMap.containsKey(getCurrentId())){
            bestLoc =bestLOCMap.get(getCurrentId());
            bestTurnsMap.get(getCurrentId());
        }
        // new result worse than existing one
        int maxKnights = (int)getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS);
        if(nStars < Util.calculateStars(bestTurns,bestLoc,knightsSpawned,getCurrentLevel().getTurnsToStarsCopy() , getCurrentLevel().getLocToStarsCopy(),maxKnights) )return false;

        if(bestLOCMap.containsKey(getCurrentId())){
            bestLOCMap.replace(getCurrentId(),loc);
        } else bestLOCMap.put(getCurrentId(),loc);

        if(bestTurnsMap.containsKey(getCurrentId())){
            bestTurnsMap.replace(getCurrentId(),turns);
        } else bestTurnsMap.put(getCurrentId(),turns);

        if(bestCodeMap.containsKey(getCurrentId())){
            bestCodeMap.replace(getCurrentId(),playerBehaviour.getCodeLines());
        } else bestCodeMap.put(getCurrentId(),playerBehaviour.getCodeLines());
        if(bestKnightsMap.containsKey(getCurrentId())){
            bestKnightsMap.replace(getCurrentId(),knightsSpawned);
        } else bestKnightsMap.put(getCurrentId(),knightsSpawned);
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
        if(getCurrentIndex() == getAmountOfLevelsInCurrentCourse()-1)addLevel(getCurrentIndex(), level, b);
        else addLevel(getCurrentIndex()+1, level, b);
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


    public List<String> getCurrentlyBestCode() {
        return getBestCodeOfLevel(getCurrentId());
    }

   /* public List<Integer> getUnlockedLevelIds() {
        List<Integer> unlockedLevelIds = new ArrayList<>(unlockedLevelIdList);
        return unlockedLevelIds;
    }*/

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
    public int getBestKnightsOfLevel(int i) {
        int output = -1;
        if(bestKnightsMap.get(i)!=null)output = bestKnightsMap.get(i);
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
        for(Level l : levelSet){
            boolean levelIsTut = getDataFromLevelWithId(LevelDataType.COURSE,l.getID() ).equals(GameConstants.CHALLENGE_COURSE_NAME);
            if(levelIsTut)output++;
        }
        return output;
    }

    public int getNextTutorialIndex() {
        int output = 0;
        for(Level l : levelSet){
            boolean levelIsTut = getDataFromLevelWithId(LevelDataType.COURSE,l.getID() ).equals(GameConstants.CHALLENGE_COURSE_NAME);
                if(levelIsTut&&output>getCurrentIndex())return output;
                output++;
            }
        return -1;
    }


    public Integer createUniqueId() {
        int maxId = levelSet.iterator().next().getID();
        for(Level l : levelSet){
            if(l.getID() > maxId)maxId = l.getID();
        }
        return maxId+1;
    }

    public String getNameOfLevelWithId(int levelId) {
        if(levelId == -1)return null;
        for(Level l : levelSet){
            if(l.getID() == levelId)return l.getName();
        }
        return null;
    }

    public Integer getIdOfLevelWithName(String levelName) {
        for(Level l : levelSet){
            if(l.getName().equals(levelName))return l.getID();
        }
        return -1;
    }

    List<Integer> getOrderedIds() {
        List<Integer> output = new ArrayList<>();
        for(Level l : levelSet){
            output.add(l.getID());
        }
        return output;
    }

    public Integer getCurrentId() {
        return currentId;
    }

    public List<String> getCurrentRequiredLevels() {
        List<String> output = new ArrayList<>();
        for(Integer id : getCurrentLevel().getRequiredLevelIdsCopy()){
            if(this.getIndexOfLevelWithId(id) < getCurrentIndex())output.add(getNameOfLevelWithId(id));
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

    public int getAmountOfKnightsSpawned() {
        return knightsSpawned;
    }
    int getAmountOfSkeletonsSpawned() {
        return skeletonsSpawned;
    }


    public int getCurrentIndex() {
        return getIndexOfLevelWithId(currentId);
    }

    public Object getDataFromLevelWithId(LevelDataType dataType, int id){
        Level level = getLevelWithId(id);
        switch (dataType){
            case AMOUNT_OF_RERUNS:
                return level.getAmountOfPlays();
            case LEVEL_INDEX:
                return getIndexOfLevelWithId(id);
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
//            case IS_TUTORIAL:
//                return level.isTutorial();
            case COURSE:
                for(String cName : courseNameToIdListMap.keySet()){
                    List<Integer> idList = courseNameToIdListMap.get(cName);
                    if(idList.contains(level.getID())){
                        return cName;
                    }
                }
                return "";
            case TUTORIAL_LINES:
                return level.getTutorialMessagesCopy();
            case LEVEL_NAME:
                return level.getName();
            case HAS_AI:
                return level.hasAi();
        }
        return null;
    }

    private Level getLevelWithIndexInCourse(String courseName, int index) {
        return getLevelWithId(courseNameToIdListMap.get(courseName).get(index));
    }

    public Object getDataFromCurrentLevel(LevelDataType dataType){
        return getDataFromLevelWithId(dataType, getCurrentId());
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

    String getNameOfLevelWithIndexInCourse(String courseName, int i) {
        return getNameOfLevelWithId(courseNameToIdListMap.get(courseName).get(i));
    }

    public boolean hasLevelWithName(String t1) {
        for(Level l : levelSet){
            if(l.getName().equals(t1))return true;
        }
        return false;
    }

    public int getAmountOfLevels() {
        return levelSet.size();
    }

    int getIndexOfLevelInList(String levelName) {
        return getIndexOfLevelWithId(getIdOfLevelWithName(levelName));
    }
    public int getIndexOfLevelWithId(int id) {
        if(id == -1)return -1;
        for(List<Integer> idList : courseNameToIdListMap.values()){
            for(int i = 0; i < idList.size();i++){
                if(idList.get(i)==id)return i;
            }
        }
        return -1;
    }

    public boolean currentLevelHasChanged() {
        return levelChangeSender.hasChanged() || levelChangeSender.isLevelNew();
    }
    private Level getCurrentLevel() {
        return getLevelWithId(currentId);
    }

    private Level getLevelWithId(int id) {
        for(Level l : levelSet){
            if(l.getID() == id)
            return l;
        }
        return null;
    }

    public int getAmountOfCompletedLevels() {
        int amountOfCompletedLevels = 0;
        for(String s : getAllCourseNames()){
            amountOfCompletedLevels += calculateProgressOfCourse(s);
        }
       /* for(int id : getUnlockedLevelIds()){
            if(bestLOCMap.get(id)>-1 && bestTurnsMap.get(id)>-1)amountOfCompletedLevels++;
        }*/
        return amountOfCompletedLevels;
    }

    public int calculateProgressOfCourse(String courseName) {
        if(courseName.equals(CHALLENGE_COURSE_NAME))return getAmountOfLevelsInCourse(CHALLENGE_COURSE_NAME)-1;
        int output = -1;
        for(int id : courseNameToIdListMap.get(courseName)){
            if(getBestLocOfLevel(id)==-1)return output;
            output++;
        }
        return output;
    }

    public boolean isEditorUnlocked() {
        return editorUnlocked;
    }

    public List<String> getAllCourseNames() {
        return new ArrayList<>(courseNameToIdListMap.keySet());
    }

    public int getAmountOfLevelsInCourse(String courseName) {
        return courseNameToIdListMap.get(courseName).size();
    }

//    public void selectCourse(String currentCourse){
//        this.currentCourse = currentCourse;
//    }
//TODO: change these?
    public String getCurrentCourseName() {
        for(String courseName : courseNameToIdListMap.keySet()){
            if(courseNameToIdListMap.get(courseName).contains(currentId)){
                return courseName;
            }
        }
        return null;
    }

    private double getCurrentStarsOfLevel(int id){
        Level level = getLevelWithId(id);
        return Util.calculateStars(bestTurnsMap.get(id),bestLOCMap.get(id),bestKnightsMap.get(id),level.getTurnsToStarsCopy(),level.getLocToStarsCopy(),level.getMaxKnights());
    }

    public double getMinStarsOfCourse(String courseName) {
        double minStar = 3;
        if(courseNameToIdListMap.get(courseName).size()==0)return 0;
        for(Integer id : courseNameToIdListMap.get(courseName)){
            if(getCurrentStarsOfLevel(id)< minStar)minStar = getCurrentStarsOfLevel(id);
        }
        return minStar;
    }

    public LevelDifficulty getDifficultyOfCourse(String courseName) {
        return courseNameLevelDifficultyMap.get(courseName);
    }

    private void addCourse(String text,LevelDifficulty difficulty) {
        courseNameToIdListMap.put(text, new ArrayList<>());
        courseNameLevelDifficultyMap.put(text, difficulty);
    }

    private void deleteCourse(String selectedItem) {
        courseNameToIdListMap.remove(selectedItem);
        courseNameLevelDifficultyMap.remove(selectedItem);
    }

    public void applyCourseChanges(Map<String, LevelDifficulty> courseDifficultyMap) {
        for(String s : getAllCourseNames()){
            if(!courseNameLevelDifficultyMap.containsKey(s))courseNameLevelDifficultyMap.put(s, courseDifficultyMap.get(s));
            else if(getDifficultyOfCourse(s)!=courseDifficultyMap.get(s))courseNameLevelDifficultyMap.replace(s, courseDifficultyMap.get(s));
        }
    }

    public List<Integer> getOrderedIdsFromCourse(String s) {
        return new ArrayList<>(courseNameToIdListMap.get(s));
    }

    public List<Integer> getUnlockedLevelIds() {
        List<Integer> outputList = new ArrayList<>();
        for(String courseName : getAllCourseNames()){
            int count = 0;
            for(int i : courseNameToIdListMap.get(courseName)){
                outputList.add(i);
                if(count > calculateProgressOfCourse(courseName))break;
            }
        }
        return outputList;
    }

    public int getCurrentCourseProgress() {
        return calculateProgressOfCourse(getCurrentCourseName());
    }

    public void addAllCourses(List<String> newCourses) {
        for(String courseName : newCourses)
            courseNameToIdListMap.put(courseName,new ArrayList<>());
    }
    public void removeAllCourses(List<String> deletedCourses) {
        for(String courseName : deletedCourses)
            courseNameToIdListMap.remove(courseName);
    }

    public int getIdOfLevelInCurrentCourseAt(int i) {
        return courseNameToIdListMap.get(getCurrentCourseName()).get(i);
    }

    public int getNextId() {
        if(courseNameToIdListMap.get(getCurrentCourseName()).size() > getCurrentIndex()+1)
            return courseNameToIdListMap.get(getCurrentCourseName()).get(getCurrentIndex()+1);
        else return -1;
    }
    public int getPrevId() {
        if(0 <= getCurrentIndex()-1)
            return courseNameToIdListMap.get(getCurrentCourseName()).get(getCurrentIndex()-1);
        else return -1;
    }

    public int getAmountOfLevelsInCurrentCourse() {
        return getAmountOfLevelsInCourse(getCurrentCourseName());
    }

    public void selectFirstLevelInCourse(String course) {
        selectLevel(courseNameToIdListMap.get(course).get(0));
    }

//    public void storeProgress() {
//        if(courseNameProgressMap.containsKey(currentCourse))
//        if(courseNameProgressMap.get(currentCourse) < currentLevelIndex){
//            courseNameProgressMap.replace(currentCourse, currentLevelIndex);
//        }
//    }
}
