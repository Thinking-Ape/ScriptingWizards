package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.model.Level;
import main.model.LevelDataType;
import main.model.Model;
import main.model.gamemap.GameMap;
import main.utility.GameConstants;
import main.parser.JSONParser;
import main.utility.Util;

import java.io.IOException;

import static main.utility.GameConstants.*;


public class LevelOverviewPane extends VBox {
    //TODO!!!
    private ListView<LevelEntry> levelListView = new ListView<>();
    private Button playBtn = new Button();
    private Button backBtn = new Button();
    private boolean isTutorial = false;

    public LevelOverviewPane( View view,boolean isTutorial){
        this.isTutorial = isTutorial;
        updateUnlockedLevels( view);
        backBtn.setPrefSize(BUTTON_SIZE,BUTTON_SIZE*0.75);
        playBtn.setPrefSize(BUTTON_SIZE,BUTTON_SIZE);
        ImageView backBtnIV = new ImageView(GameConstants.BACK_BTN_IMAGE_PATH);
        backBtnIV.setScaleY(GameConstants.HEIGHT_RATIO);
        backBtnIV.setScaleX(GameConstants.WIDTH_RATIO);
        ImageView executeBtnIV = new ImageView(GameConstants.EXECUTE_BTN_IMAGE_PATH);
        executeBtnIV.setScaleY(GameConstants.HEIGHT_RATIO);
        executeBtnIV.setScaleX(GameConstants.WIDTH_RATIO);
        playBtn.setGraphic(executeBtnIV);
        backBtn.setGraphic(backBtnIV);
        backBtn.setStyle("-fx-background-color: rgba(0,0,0,0)");
        playBtn.setStyle("-fx-background-color: rgba(0,0,0,0)");
        HBox hBox = new HBox(backBtn,playBtn);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(BUTTON_SIZE);
        levelListView.setPrefHeight(GameConstants.SCREEN_HEIGHT*0.76);

        Label challengesLbl = new Label(isTutorial ? "Tutorials" :"Challenges");
        challengesLbl.setFont(GameConstants.CHALLENGER_FONT);
        challengesLbl.setStyle("-fx-background-color: lightgrey");
        this.getChildren().addAll(challengesLbl,levelListView,hBox);
        this.setAlignment(Pos.CENTER);
        this.setSpacing(TEXTFIELD_HEIGHT);
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


    public void updateUnlockedLevels( View view) {
        levelListView.getItems().clear();
        double width=0;
//        String[] levelNames =JSONParser.getUnlockedLevelNames();
//        String[] sortedLevelNames = new String[levelNames.length];
        for(int i = 0; i < Model.getAmountOfLevels(); i++){

            int loc =Model.getBestLocOfLevel(i);
            int turns =Model.getBestTurnsOfLevel(i);

            Integer[] turnsToStars = (Integer[]) Model.getDataFromLevelWithIndex(LevelDataType.TURNS_TO_STARS,i);
            Integer[] locToStars = (Integer[]) Model.getDataFromLevelWithIndex(LevelDataType.LOC_TO_STARS,i);
            double nStars = Util.calculateStars(turns,loc,turnsToStars,locToStars);
            //TODO: improve -> see make view static
            GameMap gameMap = (GameMap) Model.getDataFromLevelWithIndex(LevelDataType.MAP_DATA,i);
            String levelName = (String)Model.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME,i);
            boolean hasAI = (boolean)Model.getDataFromLevelWithIndex(LevelDataType.HAS_AI,i);
            int maxKnights = (int)Model.getDataFromLevelWithIndex(LevelDataType.MAX_KNIGHTS,i);
            LevelEntry le = new LevelEntry(view.getImageFromMap(gameMap),levelName,"Has AI: "+hasAI+", Max Knights: " +maxKnights+"\nMax Turns for ***: "+turnsToStars[1]+", Max Turns for **: "
                    +turnsToStars[0]+"\nMax LOC for ***: "+locToStars[1]+", Max LOC for **: "+locToStars[0],"Best Turns: "+turns+"\nBest LOC: "+loc+"\nEarned Stars: "+ (int)nStars + (Math.round(nStars)!=(int)nStars ? ".5" : ""),nStars);
            le.autosize();
            boolean isTut = (boolean)Model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,i);
            if(isTutorial && isTut && Model.getCurrentIndex() <= JSONParser.getTutorialProgressIndex()+1){
                levelListView.setFixedCellSize(BUTTON_SIZE*1.25);
                levelListView.getItems().add(le);
                width = le.getMaxWidth() > width ? le.getMaxWidth() : width;
            }
            else if(!isTutorial && (GameConstants.SHOW_TUTORIAL_LEVELS_IN_PLAY||!isTut)){
                levelListView.setFixedCellSize(BUTTON_SIZE*1.25);
                levelListView.getItems().add(le);
                width = le.getMaxWidth() > width ? le.getMaxWidth() : width;
            }

        }
        levelListView.setMaxWidth(width+GameConstants.TEXTFIELD_HEIGHT*2);
    }
    public void addLevel(int i, Image image){
        Integer[] turnsToStars = (Integer[]) Model.getDataFromLevelWithIndex(LevelDataType.TURNS_TO_STARS,i);
        Integer[] locToStars = (Integer[]) Model.getDataFromLevelWithIndex(LevelDataType.LOC_TO_STARS,i);
//        double nStars = Util.calculateStars(bestResults[1],bestResults[0],turnsToStars,locToStars)
        String levelName = (String)Model.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME,i);
        boolean hasAI = (boolean)Model.getDataFromLevelWithIndex(LevelDataType.HAS_AI,i);
        int maxKnights = (int)Model.getDataFromLevelWithIndex(LevelDataType.MAX_KNIGHTS,i);
        LevelEntry le = new LevelEntry(image,levelName,"Has AI: "+hasAI+", Max Knights: " + maxKnights+
                "\nMax Turns for ***: "+turnsToStars[1]+", Max Turns for **: "+turnsToStars[0]+"\nMax LOC for ***: "+locToStars[1]+
                ", Max LOC for **: "+locToStars[0],"Best Turns: "+-1+"\nBest LOC: "+-1+"\nEarned Stars: "+ 0,0);
        levelListView.getItems().add(le);
    }

    public void updateCurrentLevel(int amountOfTuts, Image starImage) {
        int index = isTutorial ? Model.getCurrentIndex() : Model.getCurrentIndex() - amountOfTuts;
        levelListView.getItems().get(index).updateImage(starImage);
    }

    public boolean containsLevel(String nextLevelName) {
        for(LevelEntry levelEntry : levelListView.getItems()){
            if(levelEntry.getLevelName().equals(nextLevelName)) return true;
        }
        return false;
    }
}
