package main.model;

import main.model.gamemap.GameMap;
import java.util.List;


public abstract class ModelInformer {

    private static Model model;

    public static void init(Model model){
        ModelInformer.model = model;
    }

    public static int getTutorialProgress() {
        return model.getTutorialProgress();
    }

    public static List<String> getCurrentlyBestCode() {
        return model.getCurrentlyBestCode();
    }

    public static List<Integer> getUnlockedLevelIds() {
        return model.getUnlockedLevelIds();
    }

    public static int getBestLocOfLevel(int i) {
        return model.getBestLocOfLevel(i);
    }

    public static int getBestTurnsOfLevel(int i) {
        return model.getBestTurnsOfLevel(i);
    }

    public static GameMap getCurrentMapCopy(){
        return model.getCurrentMapCopy();
    }

    public static List<String> getBestCodeOfLevel(int i) {
        return model.getBestCodeOfLevel(i);
    }

    public static boolean isCurrentLevelNew() {
        return model.isCurrentLevelNew();
    }

    public static String getCurrentTutorialMessage() {
        return model.getCurrentTutorialMessage();
    }

    public static int getAmountOfTutorials() {
        return model.getAmountOfTutorials();
    }

    public static int getNextTutorialIndex() {
        return model.getNextTutorialIndex();
    }


    public static Integer createUniqueId() {
        return model.createUniqueId();
    }

    public static String getNameOfLevelWithId(int levelId) {
        return model.getNameOfLevelWithId(levelId);
    }

    public static Integer getIdOfLevelWithName(String levelName) {
        return model.getIdOfLevelWithName(levelName);
    }

    public static List<Integer> getOrderedIds() {
        return model.getOrderedIds();
    }

    public static Integer getCurrentId() {
        return model.getCurrentId();
    }

    public static List<String> getCurrentRequiredLevels() {
        return model.getCurrentRequiredLevels();
    }

    public static int getCurrentRound() {
        return model.getCurrentRound();
    }


    public static boolean isLost() {
        return model.isLost();
    }

    public static boolean aiIsFinished() {
        return model.aiIsFinished();
    }

    public static boolean isWon() {
        return model.isWon();
    }

    public static int getTurnsTaken() {
        return model.getTurnsTaken();
    }

    public static boolean isStackOverflow() {
        return model.isStackOverflow();
    }

    public static int getAmountOfKnightsSpawned() {
        return model.getAmountOfKnightsSpawned();
    }
    public static int getAmountOfSkeletonsSpawned() {
        return model.getAmountOfSkeletonsSpawned();
    }


    public static int getCurrentIndex() {
        return model.getCurrentIndex();
    }

    public static Object getDataFromLevelWithIndex(LevelDataType dataType, int index){
        return model.getDataFromLevelWithIndex(dataType, index);
    }
    public static Object getDataFromCurrentLevel(LevelDataType dataType){
        return model.getDataFromCurrentLevel(dataType);
    }

    public static int getCurrentTutorialMessageIndex() {
        return model.getCurrentTutorialMessageIndex();
    }

    public static int getCurrentTutorialSize() {
        return model.getCurrentTutorialSize();
    }

    public static List<String> getUnlockedStatementList() {
        return model.getUnlockedStatementList();
    }

    public static String getNameOfLevelWithIndex(int i) {
        return model.getNameOfLevelWithIndex(i);
    }

    public static boolean hasLevelWithName(String t1) {
        return model.hasLevelWithName(t1);
    }

    public static int getAmountOfLevels() {
        return model.getAmountOfLevels();
    }

    public static int getIndexOfLevelInList(String levelName) {
       return model.getIndexOfLevelInList(levelName);
    }
    public static int getIndexOfLevelWithId(int id) {
        return model.getIndexOfLevelWithId(id);
    }

    public static boolean currentLevelHasChanged() {
       return model.currentLevelHasChanged();
    }

    public static int getDifferentRandomNumberEachTime(int bnd1, int bnd2) {
        return model.getDifferentRandomNumberEachTime(bnd1, bnd2);
    }
}
