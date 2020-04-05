package main.view;

import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import main.utility.GameConstants;
import main.utility.Util;


public class StartScreen extends BorderPane {

    private Button tutorialBtn = new Button("");
    private Button playBtn = new Button("");
    private Button exitBtn = new Button("");
    private Button lvlEditor  = new Button("");
    private Button switchLanguage  = new Button("Switch Language: EN -> DE"); //TODO!!!

    public StartScreen(){
        switchLanguage.setDisable(true);
        Util.applyStartButtonFormat(tutorialBtn,playBtn,exitBtn,lvlEditor);
//        Separator sep = new Separator(Orientation.HORIZONTAL);
//        sep.setHalignment(HPos.CENTER);
        VBox vBox = new VBox(tutorialBtn,playBtn,lvlEditor,exitBtn);
        vBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT);
        vBox.setAlignment(Pos.BOTTOM_CENTER);
        Label versionLabel = new Label("Version"+ GameConstants.VERSION);
        versionLabel.setStyle("-fx-text-fill: white");
        Text helper = new Text(versionLabel.getText());
        helper.autosize();
        vBox.setTranslateY(-1.5*GameConstants.TEXTFIELD_HEIGHT);
        vBox.setTranslateX(-helper.getLayoutBounds().getWidth());
        this.setTop(versionLabel);
        this.setCenter(vBox);
        ImageView tutorialImageView = new ImageView(new Image(GameConstants.TUTORIAL_BTN_PATH));
        tutorialImageView.setScaleX(GameConstants.WIDTH_RATIO);
        tutorialImageView.setScaleY(GameConstants.HEIGHT_RATIO);
        tutorialBtn.setGraphic(tutorialImageView);
        tutorialBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");

        ImageView playImageView = new ImageView(new Image(GameConstants.CHALLENGES_BTN_PATH));
        playImageView.setScaleX(GameConstants.WIDTH_RATIO);
        playImageView.setScaleY(GameConstants.HEIGHT_RATIO);
        playBtn.setGraphic(playImageView);
        playBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");

        ImageView editorImageView = new ImageView(new Image(GameConstants.LVL_EDITOR_BTN_PATH));
        editorImageView.setScaleX(GameConstants.WIDTH_RATIO);
        editorImageView.setScaleY(GameConstants.HEIGHT_RATIO);
        lvlEditor.setGraphic(editorImageView);
        lvlEditor.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
        ImageView quitImageView = new ImageView(new Image(GameConstants.QUIT_BTN_PATH));
        quitImageView.setScaleX(GameConstants.WIDTH_RATIO);
        quitImageView.setScaleY(GameConstants.HEIGHT_RATIO);
        exitBtn.setGraphic(quitImageView);
        exitBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
//        HBox hb = new HBox(,switchLanguage);
//        Group g = new Group(exitBtn);
//        this.setBottom(g);
//        hb.setAlignment(Pos.BOTTOM_CENTER);
//        hb.setSpacing(100);
//        exitBtn.setAlignment(Pos.CENTER);
//        BorderPane.setAlignment(g, Pos.CENTER);
//        this.setRight(switchLanguage);
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
