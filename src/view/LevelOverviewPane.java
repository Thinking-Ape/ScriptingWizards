package view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Level;
import model.Model;
import utility.GameConstants;
import parser.JSONParser;


public class LevelOverviewPane extends VBox {
    //TODO!!!
    ListView<LevelEntry> levelListView = new ListView<>();
    Button playBtn = new Button("Play");
    Button backBtn = new Button("Back");

    public LevelOverviewPane(Model model, View view){
        updateUnlockedLevels(model, view);
        HBox hBox = new HBox(backBtn,playBtn);
        hBox.setSpacing(100);
        this.getChildren().addAll(levelListView,hBox);
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
                levelListView.setFixedCellSize(125);
                levelListView.getItems().add(le);
            }
        }
    }
}
