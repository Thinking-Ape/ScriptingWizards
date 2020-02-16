package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.model.Level;
import main.model.Model;
import main.utility.GameConstants;
import main.parser.JSONParser;

import static main.utility.GameConstants.BUTTON_SIZE;


public class LevelOverviewPane extends VBox {
    //TODO!!!
    ListView<LevelEntry> levelListView = new ListView<>();
    Button playBtn = new Button();
    Button backBtn = new Button();

    public LevelOverviewPane(Model model, View view){
        updateUnlockedLevels(model, view);
        backBtn.setPrefSize(BUTTON_SIZE,BUTTON_SIZE*0.75);
        playBtn.setPrefSize(BUTTON_SIZE,BUTTON_SIZE);
        playBtn.setGraphic(new ImageView(GameConstants.EXECUTE_BTN_IMAGE_PATH));
        backBtn.setGraphic(new ImageView(GameConstants.BACK_BTN_IMAGE_PATH));
        backBtn.setStyle("-fx-background-color: rgba(0,0,0,0)");
        playBtn.setStyle("-fx-background-color: rgba(0,0,0,0)");
        HBox hBox = new HBox(backBtn,playBtn);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(BUTTON_SIZE);
        levelListView.setPrefHeight(GameConstants.SCREEN_HEIGHT*0.7);
        Label challengesLbl = new Label("Challenges");
        challengesLbl.setFont(GameConstants.CHALLENGER_FONT);
        challengesLbl.setStyle("-fx-background-color: lightgrey");
        this.getChildren().addAll(challengesLbl,levelListView,hBox);
        this.setAlignment(Pos.CENTER);
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
    public void updateUnlockedLevels(Model model, View view){
        levelListView.getItems().clear();
        for(String s : JSONParser.getUnlockedLevelNames()){
            Level l = model.getLevelWithName(s);
            if(GameConstants.SHOW_TUTORIAL_LEVELS_IN_PLAY||!l.isTutorial()){
                LevelEntry le = new LevelEntry(view.getImageFromMap(l.getOriginalMap()),s,"Has AI: "+l.hasAi()+", Max Knights: " + l.getMaxKnights()+"\nMax Turns for ***: "+l.getTurnsToStars()[1]+", Max Turns for **: "+l.getTurnsToStars()[0]+"\nMax LOC for ***: "+l.getLocToStars()[1]+", Max LOC for **: "+l.getLocToStars()[0]);
                levelListView.setFixedCellSize(BUTTON_SIZE*1.25);
                levelListView.getItems().add(le);
            }
        }
    }
}
