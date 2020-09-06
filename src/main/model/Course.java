package main.model;

import java.util.ArrayList;
import java.util.List;

public class Course {
//    private List<Integer> requiredCoursesList;
    private boolean needsPreviousCourse;
//    private int index;
    private List<Integer> levelList;
    private CourseDifficulty difficulty;
    private String name;
    public final int ID;


    public Course(boolean needsPreviousCourse, List<Integer> levelList, CourseDifficulty difficulty, String name , int id) {
//        this.requiredCoursesList = requiredCoursesList;
        this.needsPreviousCourse = needsPreviousCourse;
//        this.index = index;
        this.levelList = levelList;
        this.difficulty = difficulty;
        this.name = name;
        ID = id;
    }
    public Course(boolean needsPreviousCourse,  List<Integer> levelList, CourseDifficulty difficulty, String name ) {
//        this.requiredCoursesList = requiredCoursesList;
        this(needsPreviousCourse, levelList,  difficulty,  name,-1);
    }

    public boolean containsLevel(int levelId){
        return levelList.contains(levelId);
    }
//    public boolean containsCourse(int courseId){
//        return requiredCoursesList.contains(courseId);
//    }
    public void addLevelId(int levelId){
        if(!levelList.contains(levelId)) levelList.add(levelId);
    }
    public void removeLevelId(int levelId){
        if(levelList.contains(levelId)) levelList.remove((Integer)levelId);
    }
//    public void addRequiredCourse(int courseId){
//        if(!requiredCoursesList.contains(courseId)) requiredCoursesList.add(courseId);
//    }
//    public void removeRequiredCourse(int courseId){
//        if(requiredCoursesList.contains(courseId)) requiredCoursesList.remove((Integer)courseId);
//    }
    public void changeDifficulty(CourseDifficulty difficulty){
        this.difficulty = difficulty;
    }

    public CourseDifficulty getDifficulty(){
        return difficulty;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public void moveLevelDown(int index) {
        int currentId = levelList.get(index);
        int tempId = levelList.get(index-1);
        levelList.set(index-1, currentId);
        levelList.set(index, tempId);
    }
    public void moveLevelUp(int index) {
        int currentId = levelList.get(index);
        int tempId = levelList.get(index+1);
        levelList.set(index+1, currentId);
        levelList.set(index, tempId);
    }

    public int getAmountOfLevels() {
        return levelList.size();
    }
    public List<Integer> getAllLevelIds() {
        return new ArrayList<>(levelList);
    }

    public int getLevelIdAt(int index) {
        return levelList.get(index);
    }

    public void changeName(String s) {
        name = s;
    }

    public void setNeedsPreviousCourse(boolean needsPreviousCourse) {
        this.needsPreviousCourse = needsPreviousCourse;
    }
//    public void setIndex(int index) {
//        this.index = index;
//    }
//    public int getIndex() {
//        return index;
//    }

    public boolean needsPreviousCourse() {
        return needsPreviousCourse;
    }


//    public List<Integer> getReqCourseIds() {
//        return requiredCoursesList;
//    }

//    public void setRequiredIds(List<Integer> requiredIds) {
//        requiredCoursesList = requiredIds;
//    }
}
