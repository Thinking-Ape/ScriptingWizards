package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import main.model.GameConstants;
import main.utility.Util;


public class StartScreen extends BorderPane {

    private Button tutorialBtn = new Button("");
    private Button challengesBtn = new Button("");
    private Button exitBtn = new Button("");
    private Button lvlEditor  = new Button("");

    StartScreen(){
        Util.applyStartButtonFormat(tutorialBtn, challengesBtn,exitBtn,lvlEditor);
        Label versionLabel = new Label("Version"+ GameConstants.VERSION);
        versionLabel.setStyle("-fx-text-fill: white");

        ImageView tutorialImageView = new ImageView(new Image(GameConstants.TUTORIAL_BTN_PATH));
        tutorialImageView.setPreserveRatio(false);
        tutorialImageView.setFitWidth(tutorialImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        tutorialImageView.setFitHeight(tutorialImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        tutorialImageView.autosize();
        tutorialBtn.setGraphic(tutorialImageView);
        tutorialBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
        tutorialBtn.autosize();
        ImageView playImageView = new ImageView(new Image(GameConstants.CHALLENGES_BTN_PATH));
        playImageView.setPreserveRatio(false);
        playImageView.setFitWidth(playImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        playImageView.setFitHeight(playImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        playImageView.autosize();
        challengesBtn.setGraphic(playImageView);
        challengesBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
        challengesBtn.autosize();
        ImageView editorImageView = new ImageView(new Image(GameConstants.LVL_EDITOR_BTN_PATH));
        editorImageView.setPreserveRatio(false);
        editorImageView.setFitWidth(editorImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        editorImageView.setFitHeight(editorImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        editorImageView.autosize();
        lvlEditor.setGraphic(editorImageView);
        lvlEditor.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
        lvlEditor.autosize();
        ImageView quitImageView = new ImageView(new Image(GameConstants.QUIT_BTN_PATH));
        quitImageView.setPreserveRatio(false);
        quitImageView.setFitWidth(quitImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        quitImageView.setFitHeight(quitImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        quitImageView.autosize();
        exitBtn.setGraphic(quitImageView);
        exitBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
        exitBtn.autosize();

        Text helper = new Text(versionLabel.getText());
        helper.autosize();
        VBox vBox = new VBox();
        vBox.setTranslateY(-quitImageView.getLayoutBounds().getHeight());
        vBox.setTranslateX(-helper.getLayoutBounds().getWidth()/2);

        this.setMaxSize(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        this.autosize();
        this.layout();
        this.setTop(versionLabel);
        this.setBottom(vBox);
        exitBtn.setMaxHeight(quitImageView.getLayoutBounds().getHeight());
        lvlEditor.setMaxHeight(quitImageView.getLayoutBounds().getHeight());
        challengesBtn.setMaxHeight(quitImageView.getLayoutBounds().getHeight());
        vBox.setAlignment(Pos.BASELINE_CENTER);
        vBox.setMaxHeight(quitImageView.getLayoutBounds().getHeight()*10);
        vBox.setSpacing(0);
        vBox.getChildren().addAll(tutorialBtn, challengesBtn,lvlEditor,exitBtn);
    }

    public Button getTutorialBtn() {
        return tutorialBtn;
    }

    public Button getChallengesBtn() {
        return challengesBtn;
    }

    public Button getExitBtn() {
        return exitBtn;
    }

    public Button getLvlEditorBtn() {
        return lvlEditor;
    }
}
