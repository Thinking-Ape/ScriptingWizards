package main.model;

import main.model.gamemap.GameMap;

import java.util.ArrayList;
import java.util.List;

import static main.model.GameConstants.CHALLENGE_COURSE_NAME;


public abstract class ModelInformer {

    private static Model model;

    public static void init(Model model){
        ModelInformer.model = model;
    }

//    public static int getTutorialProgress() {
//        return model.getTutorialProgress();
//    }

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
    public static int getBestKnightsOfLevel(int i) {
        return model.getBestKnightsOfLevel(i);
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
        return model.createUniqueLevelId();
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

    public static Object getDataFromLevelWithId(LevelDataType dataType, int id){
        return model.getDataFromLevelWithId(dataType, id);
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

//    public static String getNameOfLevelWithIndex(int i) {
//        return model.getNameOfLevelWithIndexInCourse(i);
//    }

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

    public static boolean isEditorUnlocked() {
        return model.isEditorUnlocked();
    }

    public static List<String> getAllCourseNames() {
        return model.getOrderedCourseNames();
    }

    public static int getAmountOfLevelsInCourse(String courseName) {
        return model.getAmountOfLevelsInCourse(courseName);
    }

    public static String getCurrentCourseName() {
        return model.getCurrentCourseName();
    }

    public static double getMinStarsOfCourse(String courseName) {
        return model.getMinStarsOfCourse(courseName);
    }

    public static CourseDifficulty getDifficultyOfCourse(String courseName) {
        return model.getDifficultyOfCourse(courseName);
    }

    public static List<Integer> getOrderedIdsFromCourse(String s) {
        return model.getOrderedIdsFromCourse(s);
    }

    public static int getCurrentCourseProgress() {
        return model.getCurrentCourseProgress();
    }

    public static int getAmountOfLevelsInCurrentCourse() {
        return model.getAmountOfLevelsInCurrentCourse();
    }

    public static int getIdOfCourse(String s) {
        return model.getCourseWithName(s).getID();
    }

//    public static List<Integer> getReqIdsFromCourse(String s) {
//
//        return model.getCourseWithName(s).getReqCourseIds();
//    }

    public static boolean isCourseUnlocked(int cId) {
        return model.isCourseUnlocked(cId);
    }

    public static String getCourseName(int cId) {
        return model.getCourseWithId(cId).getName();
    }

    public static boolean currentLevelHasStoredCode() {
        return model.getCurrentlyBestCode().size() > 0;
    }

    public static boolean hasSeenIntroduction() {
        return model.hasSeenIntroduction();
    }

    public static Level getCopyOfLevel(int id) {
        return model.getCopyOfLevelWithId(id);
    }

    public static boolean getNeedsPreviousCourse(String courseName) {
        return model.getCourseWithName(courseName).needsPreviousCourse();
    }

    public static int getIndexOfCourse(String courseName) {
        return model.getIndexOfCourseWithId(getIdOfCourse(courseName));
    }

    public static int getProgressOfCourse(String courseName) {
        return model.calculateProgressOfCourse(courseName);
    }

    public static List<String> getAllLevelNames() {
        List<String> output = new ArrayList<>();
        for(String s : getAllCourseNames()) output.addAll(model.getAllLevelNamesOfCourse(s));
        return output;
    }

    public static boolean levelExists(int levelId) {
//        System.out.println(levelId + ": " +model.getIndexOfLevelWithId(levelId));
        return model.levelExists(levelId);
    }

    public static boolean canDelete() {
        return !((model.getAmountOfLevelsInCourse(CHALLENGE_COURSE_NAME)== 1 && model.getCurrentCourseName().equals(CHALLENGE_COURSE_NAME))||model.getAmountOfLevels()==1);
    }
}
