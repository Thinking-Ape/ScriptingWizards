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
import main.model.Model;
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

    public LevelOverviewPane(Model model, View view,boolean isTutorial){
        this.isTutorial = isTutorial;
        updateUnlockedLevels(model, view);
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
    public void updateUnlockedLevels(Model model, View view) {
        levelListView.getItems().clear();
        double width=0;

        for(String s : JSONParser.getUnlockedLevelNames()){
            Level l = model.getLevelWithName(s);
            int[] bestResults = new int[0];
            try {
                bestResults = JSONParser.getBestResults(l.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            double nStars = Util.calculateStars(bestResults[1],bestResults[0],l.getTurnsToStars(),l.getLocToStars());
            LevelEntry le = new LevelEntry(view.getImageFromMap(l.getOriginalMap()),s,"Has AI: "+l.hasAi()+", Max Knights: " + l.getMaxKnights()+"\nMax Turns for ***: "+l.getTurnsToStars()[1]+", Max Turns for **: "+l.getTurnsToStars()[0]+"\nMax LOC for ***: "+l.getLocToStars()[1]+", Max LOC for **: "+l.getLocToStars()[0],"Best Turns: "+bestResults[1]+"\nBest LOC: "+bestResults[0]+"\nEarned Stars: "+ (int)nStars + (Math.round(nStars)!=(int)nStars ? ".5" : ""),nStars);
            le.autosize();
            try {
                if(isTutorial && l.isTutorial() && l.getIndex() <= JSONParser.getTutorialProgressIndex()+1){
                    levelListView.setFixedCellSize(BUTTON_SIZE*1.25);
                    levelListView.getItems().add(le);
                    width = le.getMaxWidth() > width ? le.getMaxWidth() : width;
                }
                else if(!isTutorial && (GameConstants.SHOW_TUTORIAL_LEVELS_IN_PLAY||!l.isTutorial())){
                    levelListView.setFixedCellSize(BUTTON_SIZE*1.25);
                    levelListView.getItems().add(le);
                    width = le.getMaxWidth() > width ? le.getMaxWidth() : width;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        levelListView.setMaxWidth(width+GameConstants.TEXTFIELD_HEIGHT*2);
    }
    public void addLevel(Level l, Image image){
        LevelEntry le = new LevelEntry(image,l.getName(),"Has AI: "+l.hasAi()+", Max Knights: " + l.getMaxKnights()+
                "\nMax Turns for ***: "+l.getTurnsToStars()[1]+", Max Turns for **: "+l.getTurnsToStars()[0]+"\nMax LOC for ***: "+l.getLocToStars()[1]+
                ", Max LOC for **: "+l.getLocToStars()[0],"Best Turns: "+-1+"\nBest LOC: "+-1+"\nEarned Stars: "+ 0,0);
        levelListView.getItems().add(le);
    }

    public void updateLevel(Level currentLevel, int amountOfTuts, Image starImage) {
        int index = isTutorial ? currentLevel.getIndex() : currentLevel.getIndex() - amountOfTuts;
        levelListView.getItems().get(index).updateImage(starImage);
    }
}
