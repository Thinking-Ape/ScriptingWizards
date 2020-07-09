package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.model.GameConstants;
import main.model.LevelChange;
import main.model.LevelDifficulty;
import main.model.ModelInformer;
import main.utility.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static main.model.GameConstants.*;


public class CourseOverviewPane extends VBox {
    private ListView<CourseEntry> courseListView = new ListView<>();
    private Button playBtn = new Button();
    private Button backBtn = new Button();

    public CourseOverviewPane(){
        updateAllCourses();
        backBtn.setPrefSize(BUTTON_SIZE,BUTTON_SIZE*0.75);
        playBtn.setPrefSize(BUTTON_SIZE,BUTTON_SIZE);
        ImageView backBtnIV = new ImageView(GameConstants.BACK_BTN_IMAGE_PATH);
        backBtnIV.setFitHeight(backBtnIV.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        backBtnIV.setFitWidth(backBtnIV.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        ImageView executeBtnIV = new ImageView(GameConstants.EXECUTE_BTN_IMAGE_PATH);
        executeBtnIV.setFitHeight(executeBtnIV.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        executeBtnIV.setFitWidth(executeBtnIV.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        playBtn.setGraphic(executeBtnIV);
        backBtn.setGraphic(backBtnIV);
        backBtn.setStyle("-fx-background-color: rgba(0,0,0,0)");
        playBtn.setStyle("-fx-background-color: rgba(0,0,0,0)");
        HBox hBox = new HBox(backBtn,playBtn);

        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(BUTTON_SIZE);
        courseListView.setPrefHeight(GameConstants.SCREEN_HEIGHT*0.76);
        Label coursesLbl = new Label("Courses");
        coursesLbl.setFont(GameConstants.GIANT_FONT);
        coursesLbl.setStyle("-fx-background-color: lightgrey");
        this.getChildren().addAll(coursesLbl, courseListView,hBox);
        this.setAlignment(Pos.CENTER);
        this.setSpacing(CODEFIELD_HEIGHT);

    }

    public ListView<CourseEntry> getCourseListView(){
        return courseListView;
    }

    public Button getPlayBtn() {
        return playBtn;
    }

    public Button getBackBtn() {
        return backBtn;
    }


    public void updateAllCourses() {
        courseListView.getItems().clear();
        for(String courseName : ModelInformer.getAllCourseNames()){
            // View.getIconFromMap(gameMap) requires long calculation!
            // Dont call this too often, because View.getIconFromMap(gameMap) is costly!
            if(!courseName.equals(CHALLENGE_COURSE_NAME)){
                CourseEntry le = new CourseEntry(courseName, ModelInformer.getAmountOfLevelsInCourse(courseName),LevelDifficulty.BEGINNER,ModelInformer.getMinStarsOfCourse(courseName));
                le.autosize();
                courseListView.setFixedCellSize(BUTTON_SIZE*1.25);
                courseListView.getItems().add(le);
                updateWidth(le);
            }
        }
    }
//    public void updateCourseRating(LevelChange lC) {
//        for(CourseEntry courseEntry : courseListView.getItems()){
//            if(!courseEntry.getCourseName().equals(lC.getNewValue())){
//                courseEntry.getL
//            }
//        }
//    }
//    public void addCourseAtIndex(int i){
//        CourseEntry le = new CourseEntry();
//        int index = 0;
//        for(CourseEntry levelEntry : courseListView.getItems()){
//            if(ModelInformer.getIndexOfLevelInList(levelEntry.getLevelName())<i)index ++;
//        }
//        courseListView.getItems().add(index,le);
//        if(courseListView.getItems().size() == 1){
//            updateWidth(le);
//        }
//    }

    private void updateWidth(CourseEntry le) {
        double width = courseListView.getMaxWidth();
        width = le.getMaxWidth()+GameConstants.CODEFIELD_HEIGHT *2 > width ? le.getMaxWidth()+GameConstants.CODEFIELD_HEIGHT *2 : width;
        courseListView.setMaxWidth(width);
    }

//    public void updateCurrentCourse() {
//        String levelName = (String)ModelInformer.getDataFromLevelWithId(LevelDataType.LEVEL_NAME,ModelInformer.getCurrentIndex());
//        updateLevel(levelName);
//    }

//    public boolean containsLevel(String nextLevelName) {
//        for(LevelEntry levelEntry : courseListView.getItems()){
//            if(levelEntry.getCourseName().equals(nextLevelName)) return true;
//        }
//        return false;
//    }

//    public void removeCurrentCourse() {
//        LevelEntry levelEntryToRemove = null;
//        for(LevelEntry levelEntry : courseListView.getItems()){
//            if(levelEntry.getCourseName().equals(ModelInformer.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME).toString()))levelEntryToRemove=levelEntry;
//        }
//        courseListView.getItems().remove(levelEntryToRemove  );
//    }

//    public boolean containsCurrentLevel() {
//        return containsLevel(ModelInformer.getNameOfLevelWithIndex(ModelInformer.getCurrentIndex()));
//    }

//    public boolean updateCourseRating(String levelName) {
//        int index = -1;
//        int i = 0;
//        for(CourseEntry le : courseListView.getItems()){
//            if(le.getCourseName().equals(levelName))index = i;
//            i++;
//        }
//        if(index==-1)return false;
//        int currentIndex = ModelInformer.getCurrentIndex();
//        Integer[] turnsToStars = (Integer[]) ModelInformer.getDataFromLevelWithId(LevelDataType.TURNS_TO_STARS,currentIndex);
//        Integer[] locToStars = (Integer[]) ModelInformer.getDataFromLevelWithId(LevelDataType.LOC_TO_STARS,currentIndex);
//        int id = ModelInformer.getIdOfLevelWithName(levelName);
//        int loc =ModelInformer.getBestLocOfLevel(id);
//        int turns =ModelInformer.getBestTurnsOfLevel(id);
//        int knightsUsed = ModelInformer.getBestKnightsOfLevel(id);
//        int maxKnights = (int) ModelInformer.getDataFromLevelWithId(LevelDataType.MAX_KNIGHTS,currentIndex);
//        double nStars = Util.calculateStars(turns,loc,knightsUsed,turnsToStars,locToStars,maxKnights);
//        LevelEntry le = new LevelEntry(courseListView.getItems().get(index).getLevelImage(),ModelInformer.getNameOfLevelWithIndex(currentIndex),
//                getLevelTooltip(turnsToStars,locToStars,maxKnights),getBestScoreString(turns,loc,nStars,knightsUsed),nStars);
//        courseListView.getItems().set(index,le);
//        return true;
//    }

    private String getBestScoreString(int turns, int loc, double nStars, int usedKnights) {

        String starString =  (int)nStars + (Math.round(nStars)!=(int)nStars ? ".5" : "");
        return "Best Turns: "+turns+"\nBest LOC: "+loc+"\nUsed Knights: "+usedKnights+"\nEarned Stars: "+ starString;
    }

    private String getLevelTooltip(Integer[] turnsToStars, Integer[] locToStars, int maxKnights) {
        return "Max Turns for ***: "+turnsToStars[1]+", Max Turns for **: "+turnsToStars[0]+"\nMax LOC for ***: "+locToStars[1]+
                ", Max LOC for **: "+locToStars[0]+"\nMax Knights: "+maxKnights;
    }

    void removeAllCourses(List<String> deletedCourses) {
        List<CourseEntry> entriesToDelete = new ArrayList<>();
        for(CourseEntry courseEntry : courseListView.getItems()){
            if(deletedCourses.contains(courseEntry.getCourseName()))entriesToDelete.add(courseEntry);
        }
        courseListView.getItems().removeAll( entriesToDelete);
    }
    void addAllCourses(List<String> newCourses, Map<String,LevelDifficulty> courseToDifficultyMap ) {
        for(String courseName : newCourses){
            CourseEntry courseEntry =new CourseEntry(courseName,0,courseToDifficultyMap.get(courseName),0);
            courseListView.getItems().add( courseEntry);
        }
    }

    public void updateCourseRating(String courseName) {
        for(CourseEntry courseEntry :courseListView.getItems()){
            // View.getIconFromMap(gameMap) requires long calculation!
            // Dont call this too often, because View.getIconFromMap(gameMap) is costly!
            if(courseName.equals(courseEntry.getCourseName())){
                courseEntry.updateImage(Util.getStarImageFromDouble(ModelInformer.getMinStarsOfCourse(courseName)));
            }
        }
    }
}
