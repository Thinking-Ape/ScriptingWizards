package view;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import utility.GameConstants;
import utility.Util;

public class StartScreen extends BorderPane {

    private Button tutorialBtn = new Button("Tutorial");
    private Button playBtn = new Button("Free Play");
    private Button exitBtn = new Button("Quit");
    private Button lvlEditor  = new Button("Level Editor");
    private Button switchLanguage  = new Button("Switch Language: EN -> DE"); //TODO!!!

    public StartScreen(){
        Util.applyStartButtonFormat(tutorialBtn,playBtn,exitBtn,lvlEditor);
        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setHalignment(HPos.CENTER);
        VBox vBox = new VBox(tutorialBtn,playBtn,lvlEditor,sep,exitBtn);
        vBox.setSpacing(25);
        vBox.setAlignment(Pos.CENTER);
        this.setTop(new Label("Prototype v."+ GameConstants.VERSION));
        this.setCenter(vBox);
//        HBox hb = new HBox(,switchLanguage);
//        Group g = new Group(exitBtn);
//        this.setBottom(g);
//        hb.setAlignment(Pos.BOTTOM_CENTER);
//        hb.setSpacing(100);
//        exitBtn.setAlignment(Pos.CENTER);
//        BorderPane.setAlignment(g, Pos.CENTER);
        this.setRight(switchLanguage);
        switchLanguage.setAlignment(Pos.BASELINE_LEFT);

//        Group g = new Group(vBox,hb);
//        BorderPane.setMargin( g,new Insets(25));
    }

    public Button getTutorialBtn() {
        return tutorialBtn;
    }

    public Button getPlayBtn() {
        return playBtn;
    }

    public Button getExitBtn() {
        return exitBtn;
    }

    public Button getLvlEditorBtn() {
        return lvlEditor;
    }

    public Button getSwitchLanguage() {
        return switchLanguage;
    }
}
