package view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class StartScreen extends BorderPane {

    private Button tutorialBtn = new Button("Tutorial");
    private Button playBtn = new Button("Free Play");
    private Button exitBtn = new Button("Quit");
    private Button lvlEditor  = new Button("Level Editor");
    private Button switchLanguage  = new Button("Switch Language: EN -> DE"); //TODO!!!

    public StartScreen(){
        VBox vBox = new VBox(tutorialBtn,playBtn);
        vBox.setAlignment(Pos.CENTER);
        this.setTop(vBox);
        this.setCenter(lvlEditor);
        this.setBottom(new HBox(exitBtn,switchLanguage));
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
