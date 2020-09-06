package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.model.*;
import main.model.gamemap.GameMap;
import main.utility.Util;

import static main.model.GameConstants.*;


public class LevelOverviewPane extends VBox {
    private ListView<LevelEntry> levelListView = new ListView<>();
    private Button playBtn = new Button();
    private Button backBtn = new Button();
//    private boolean isTutorial;
    private CheckBox introductionCheckbox = new CheckBox("Show Introduction");
    private String courseName;

    public LevelOverviewPane( String courseName){
        this.courseName = courseName;
        updateUnlockedLevels();
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
        introductionCheckbox.setStyle(WHITE_SHADOWED_STYLE);
        introductionCheckbox.setFont(BIGGEST_FONT);
        boolean isTutorial = !CHALLENGE_COURSE_NAME.equals(courseName);
        if(isTutorial){
            hBox.getChildren().add(introductionCheckbox);
        }
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(BUTTON_SIZE);
        levelListView.setPrefHeight(GameConstants.SCREEN_HEIGHT*0.76);
        Label challengesLbl = new Label(courseName);
        challengesLbl.setFont(GameConstants.GIANT_FONT);
        challengesLbl.setStyle("-fx-background-color: lightgrey");
        this.getChildren().addAll(challengesLbl,levelListView,hBox);
        this.setAlignment(Pos.CENTER);
        this.setSpacing(CODEFIELD_HEIGHT);

    }

    public ListView<LevelEntry> getLevelListView(){
        return levelListView;
    }

    public Button getPlayBtn() {
        return playBtn;
    }

    public Button getBackBtn() {
        return backBtn;
    }


    public void updateUnlockedLevels() {
        levelListView.getItems().clear();
        int index =0;
        boolean disabled = false;
        for(int levelId : ModelInformer.getOrderedIdsFromCourse(courseName)){
            if(!ModelInformer.levelExists(levelId))continue;
            if(ModelInformer.getProgressOfCourse(courseName)<index )disabled = true;
            index ++;
//            System.out.println(courseName + " "+ ModelInformer.getNameOfLevelWithId(levelId));
//            String levelCourseName = ModelInformer.getDataFromLevelWithId(LevelDataType.COURSE,levelId)+"";
//            if(!levelCourseName.equals(courseName))continue;
            int i = ModelInformer.getIndexOfLevelWithId(levelId);
            int loc =ModelInformer.getBestLocOfLevel(levelId);
            int turns =ModelInformer.getBestTurnsOfLevel(levelId);
            String levelName = ModelInformer.getNameOfLevelWithId(levelId);
            int knightsUsed = ModelInformer.getBestKnightsOfLevel(levelId);
            int maxKnights = (int) ModelInformer.getDataFromLevelWithId(LevelDataType.MAX_KNIGHTS,levelId);
            Integer[] turnsToStars = (Integer[]) ModelInformer.getDataFromLevelWithId(LevelDataType.TURNS_TO_STARS,levelId);
            Integer[] locToStars = (Integer[]) ModelInformer.getDataFromLevelWithId(LevelDataType.LOC_TO_STARS,levelId);
            double nStars = Util.calculateStars(turns,loc,knightsUsed, turnsToStars,locToStars, maxKnights);
            GameMap gameMap = (GameMap) ModelInformer.getDataFromLevelWithId(LevelDataType.MAP_DATA,levelId);
            // View.getIconFromMap(gameMap) requires long calculation!
            // Dont call this too often, because View.getIconFromMap(gameMap) is costly!
//            System.out.println(levelName);
            LevelEntry le = new LevelEntry(View.getIconFromMap(gameMap),levelName,
                    getLevelTooltip(turnsToStars,locToStars,maxKnights),getBestScoreString(turns,loc,nStars,knightsUsed),nStars);
            le.autosize();
            levelListView.setFixedCellSize(BUTTON_SIZE*1.25);
            levelListView.getItems().add(le);
            if(disabled)le.setDisable(true);
            updateWidth(le);
        }
    }
    public void addLevelWithId(int id){
        Image image = View.getIconFromMap((GameMap)ModelInformer.getDataFromLevelWithId(LevelDataType.MAP_DATA,id));
        Integer[] turnsToStars = (Integer[]) ModelInformer.getDataFromLevelWithId(LevelDataType.TURNS_TO_STARS,id);
        Integer[] locToStars = (Integer[]) ModelInformer.getDataFromLevelWithId(LevelDataType.LOC_TO_STARS,id);
        int maxKnights = (int) ModelInformer.getDataFromLevelWithId(LevelDataType.MAX_KNIGHTS,id);
        String levelName = (String)ModelInformer.getDataFromLevelWithId(LevelDataType.LEVEL_NAME,id);
        int turns = ModelInformer.getBestTurnsOfLevel(id);
        int loc = ModelInformer.getBestLocOfLevel(id);
        int usedKnights = ModelInformer.getBestKnightsOfLevel(id);
        double nStars = Util.calculateStars(turns, loc, usedKnights,turnsToStars,locToStars,maxKnights);
        LevelEntry le = new LevelEntry(image,levelName,
                getLevelTooltip(turnsToStars,locToStars,maxKnights),getBestScoreString(turns,loc,nStars,usedKnights),nStars);
        int index = ModelInformer.getIndexOfLevelWithId(id);
        if(index >= levelListView.getItems().size())levelListView.getItems().add(le);
        else levelListView.getItems().add(index,le);
        if(levelListView.getItems().size() == 1){
            updateWidth(le);
        }
        if(ModelInformer.getProgressOfCourse(courseName)<index || !ModelInformer.levelExists(id))le.setDisable(true);
    }

    private void updateWidth(LevelEntry le) {
        double width = levelListView.getMaxWidth();
        width = le.getMaxWidth()+GameConstants.CODEFIELD_HEIGHT *2 > width ? le.getMaxWidth()+GameConstants.CODEFIELD_HEIGHT *2 : width;
        levelListView.setMaxWidth(width);
    }

    public void updateCurrentLevel() {
        String levelName = (String)ModelInformer.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME);
        updateLevel(levelName);
    }

    public boolean containsLevel(String nextLevelName) {
        for(LevelEntry levelEntry : levelListView.getItems()){
            if(levelEntry.getLevelName().equals(nextLevelName)) return true;
        }
        return false;
    }

    public LevelEntry removeCurrentLevel() {
        LevelEntry levelEntryToRemove = null;
        for(LevelEntry levelEntry : levelListView.getItems()){
            if(levelEntry.getLevelName().equals(ModelInformer.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME).toString()))levelEntryToRemove=levelEntry;
        }
        levelListView.getItems().remove(levelEntryToRemove  );
        return levelEntryToRemove;
    }

//    public boolean containsCurrentLevel() {
//        return containsLevel(ModelInformer.getNameOfLevelWithIndex(ModelInformer.getCurrentIndex()));
//    }

    public boolean updateLevel(String levelName) {
        int index = -1;
        int i = 0;
        for(LevelEntry le : levelListView.getItems()){
            if(le.getLevelName().equals(levelName))index = i;
            i++;
        }
        if(index==-1)return false;
        int currentIndex = ModelInformer.getCurrentIndex();
        Integer[] turnsToStars = (Integer[]) ModelInformer.getDataFromCurrentLevel(LevelDataType.TURNS_TO_STARS);
        Integer[] locToStars = (Integer[]) ModelInformer.getDataFromCurrentLevel(LevelDataType.LOC_TO_STARS);
        int id = ModelInformer.getIdOfLevelWithName(levelName);
        int loc =ModelInformer.getBestLocOfLevel(id);
        int turns =ModelInformer.getBestTurnsOfLevel(id);
        int knightsUsed = ModelInformer.getBestKnightsOfLevel(id);
        int maxKnights = (int) ModelInformer.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS);
        double nStars = Util.calculateStars(turns,loc,knightsUsed,turnsToStars,locToStars,maxKnights);
        LevelEntry le = new LevelEntry(levelListView.getItems().get(index).getLevelImage(),ModelInformer.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME)+"",
                getLevelTooltip(turnsToStars,locToStars,maxKnights),getBestScoreString(turns,loc,nStars,knightsUsed),nStars);
        levelListView.getItems().set(index,le);

        if(ModelInformer.getProgressOfCourse(courseName)<index || !ModelInformer.levelExists(id))le.setDisable(true);
        else le.setDisable(false);
        return true;
    }

    private String getBestScoreString(int turns, int loc, double nStars, int usedKnights) {

        String starString =  (int)nStars + (Math.round(nStars)!=(int)nStars ? ".5" : "");
        return "Best Turns: "+turns+"\nBest LOC: "+loc+"\nUsed Knights: "+usedKnights+"\nEarned Stars: "+ starString;
    }

    private String getLevelTooltip(Integer[] turnsToStars, Integer[] locToStars, int maxKnights) {
        return "Max Turns for ***: "+turnsToStars[1]+", Max Turns for **: "+turnsToStars[0]+"\nMax LOC for ***: "+locToStars[1]+
                ", Max LOC for **: "+locToStars[0]+"\nOptimal Knights: "+maxKnights;
    }

    public CheckBox getIntroductionCheckbox(){
        return introductionCheckbox;
    }

    public String getCourseName() {
        return courseName;
    }

    public void sortEntries(LevelChange levelChange) {
//        for(LevelEntry lE : levelListView.getItems()){
//            System.out.println(lE.getLevelName());
//        }
        int levelId = ModelInformer.getCurrentId();
        int oldIndex = (int)levelChange.getOldValue();
        int newIndex = (int)levelChange.getNewValue();
        if(levelListView.getItems().size() == 0)return;
        //Problem ist, dass die anderen Kurse nicht angezeigt werden -> wie wäre es mit anzeigen, aber nicht freigschaltet?!
        LevelEntry levelEntry = levelListView.getItems().get(oldIndex);

        if(levelListView.getItems().size() > oldIndex && levelListView.getItems().get(oldIndex).getLevelName().equals(ModelInformer.getNameOfLevelWithId(levelId))){

            levelListView.getItems().remove(levelEntry );
            levelListView.getItems().add(newIndex,levelEntry);
//            System.out.println("Course: "+courseName);
//            System.out.println("OldIndex: "+oldIndex);
//            System.out.println("NewIndex: "+newIndex);
//            System.out.println("LevelName at Oldindex: "+levelListView.getItems().get(oldIndex).getLevelName());
//            throw new IllegalStateException("Current Level is not in expected position!");

        }
        updateUnlockedStatus();

    }

    public void updateUnlockedStatus() {
        for(LevelEntry lE : getLevelListView().getItems()){
            if(ModelInformer.getIndexOfLevelWithId(ModelInformer.getIdOfLevelWithName(lE.getLevelName())) > ModelInformer.getCurrentCourseProgress())lE.setDisable(true);
            else lE.setDisable(false);
        }
    }

}
