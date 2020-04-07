package main.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import main.model.Model;
import main.utility.GameConstants;

import java.util.ArrayList;
import java.util.List;

public class TutorialGroup extends Group {

    private TextArea currentTutorialMessage = new TextArea();
    private Button prevBtn = new Button();
    private Button hideBtn = new Button("Hide Wizard");
    private Button nextBtn = new Button();
    private Button endIntroductionBtn = new Button();
    private HBox hb;
    private StackPane sp;
    private int index = 0;
    private List<String> tutorialEntries = new ArrayList<>();
    private ImageView wizard;

    //TODO: ALIGNMENT NOT WORKING PROP
    public TutorialGroup(){
        ImageView imageView = new ImageView(new Image(GameConstants.EXECUTE_BTN_IMAGE_PATH));
        imageView.setFitHeight(GameConstants.BUTTON_SIZE);
        imageView.setFitWidth(GameConstants.BUTTON_SIZE);

        ImageView nextIV = new ImageView(new Image(GameConstants.NEXT_BTN_IMAGE_PATH));
        nextIV.setFitHeight(GameConstants.SMALL_BUTTON_SIZE);
        nextIV.setFitWidth(GameConstants.SMALL_BUTTON_SIZE);
        ImageView prevIV = new ImageView(new Image(GameConstants.PREV_BTN_IMAGE_PATH));
        prevIV.setFitHeight(GameConstants.SMALL_BUTTON_SIZE);
        prevIV.setFitWidth(GameConstants.SMALL_BUTTON_SIZE);
        prevBtn.setGraphic(prevIV);
        nextBtn.setGraphic(nextIV);

        if(Model.getTutorialProgress() == -1){

            nextBtn.setStyle("-fx-background-color: white;" +
                    "-fx-base: transparent;");

            prevBtn.setStyle("-fx-background-color: white;" +
                    "-fx-base: transparent;");
        }
        else {

            nextBtn.setStyle("-fx-background-color: transparent;" +
                    "-fx-base: transparent;");

            prevBtn.setStyle("-fx-background-color: transparent;" +
                    "-fx-base: transparent;");
        }
        endIntroductionBtn.setGraphic(imageView);
        endIntroductionBtn.setStyle("-fx-background-color: white;" +
                "-fx-base: transparent;");
        currentTutorialMessage.setEditable(false);

        currentTutorialMessage.setWrapText(true);
        currentTutorialMessage.setMouseTransparent(true);
        currentTutorialMessage.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
//        currentTutorialMessage.setTranslateX(30);
//        currentTutorialMessage.setTranslateY(90);
        currentTutorialMessage.setMaxSize(GameConstants.TEXTFIELD_WIDTH, GameConstants.TEXTFIELD_WIDTH/2.0);
        currentTutorialMessage.setFont(GameConstants.BIG_FONT);
        ImageView bubble_IView = new ImageView(new Image("file:resources/images/Speech_Bubble.png"));
        bubble_IView.setFitHeight(bubble_IView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        bubble_IView.setFitWidth(bubble_IView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        currentTutorialMessage.setTranslateX(-bubble_IView.getBoundsInLocal().getWidth()/20.0);
        currentTutorialMessage.setTranslateY(-bubble_IView.getBoundsInLocal().getHeight()/14.5);
        // ANOTHER BUG WORK AROUND! (Bug was that text got blurry in TextArea)
        Platform.runLater(()->{
            currentTutorialMessage.setCache(false);
            if(currentTutorialMessage.getChildrenUnmodifiable().size() == 0)return;
            ScrollPane sp = (ScrollPane)currentTutorialMessage.getChildrenUnmodifiable().get(0);
            sp.setCache(false);
            for (Node n : sp.getChildrenUnmodifiable()) {
                n.setCache(false);
            }});

//        String tabString = "\t";
//        Rectangle wizard = new Rectangle(50,50,Color.BLUE);
        wizard = new ImageView(new javafx.scene.image.Image(GameConstants.WIZARD_IMAGE_PATH));
        wizard.setFitHeight(wizard.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        wizard.setFitWidth(wizard.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);

        wizard.setMouseTransparent(true);
        bubble_IView.setMouseTransparent(true);
        currentTutorialMessage.setMouseTransparent(true);
        sp = new StackPane(bubble_IView, currentTutorialMessage);
        sp.setMouseTransparent(true);
        StackPane.setAlignment(bubble_IView, Pos.TOP_RIGHT);
        StackPane.setAlignment(currentTutorialMessage, Pos.TOP_RIGHT);
        sp.setAlignment(Pos.CENTER);
        sp.layout();
        sp.autosize();
        sp.setCache(false);
        HBox navigationHBox = new HBox(hideBtn, prevBtn, nextBtn);
        VBox vb = new VBox(sp, navigationHBox,endIntroductionBtn);
        vb.setAlignment(Pos.TOP_CENTER);
//        vb.setMouseTransparent(true);
        vb.setPickOnBounds(false);
        StackPane.setAlignment(currentTutorialMessage, Pos.BOTTOM_CENTER);
        navigationHBox.setAlignment(Pos.TOP_RIGHT);
//        prevBtn.setAlignment(Pos.TOP_RIGHT);
        vb.setSpacing(GameConstants.TEXTFIELD_HEIGHT);
        hb = new HBox(vb,wizard);
        hb.setAlignment(Pos.BOTTOM_RIGHT);
//        hb.setMouseTransparent(true);
        hb.setPickOnBounds(false);

//        hb.setAlignment(Pos.BOTTOM_RIGHT);
        this.getChildren().addAll(hb);
//        this.setMinSize(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        this.autosize();
        //makes the layer below this one also receive clicks!
        this.setPickOnBounds(false);
        navigationHBox.setPickOnBounds(false);
        hb.setPickOnBounds(false);
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);
        endIntroductionBtn.setVisible(false);


    }

    public TextArea getCurrentTutorialMessage() {
        return currentTutorialMessage;
    }

    public Button getPrevBtn() {
        return prevBtn;
    }

    public Button getNextBtn() {
        return nextBtn;
    }
    public Button getHideBtn() {
        return hideBtn;
    }

    public void next(){
        index++;
        currentTutorialMessage.setText(tutorialEntries.get(index));
        if(index == tutorialEntries.size()-1)nextBtn.setDisable(true);
        prevBtn.setDisable(false);
    }
    public void prev(){
        index--;
        currentTutorialMessage.setText(tutorialEntries.get(index));
        if(index == 0)prevBtn.setDisable(true);
        nextBtn.setDisable(false);
    }

    public void setEntries(List<String> tutorialEntryList) {
        index = 0;
        tutorialEntries.clear();
        tutorialEntries.addAll(tutorialEntryList);
        prevBtn.setDisable(true);
        if(tutorialEntryList.size() >0 ){
            currentTutorialMessage.setText(tutorialEntries.get(0).trim());
            if(tutorialEntryList.size()>1)
            nextBtn.setDisable(false);
            Platform.runLater(()->{
                if(currentTutorialMessage.getChildrenUnmodifiable().size() == 0)return;
                ScrollPane sp = (ScrollPane)currentTutorialMessage.getChildrenUnmodifiable().get(0);
            sp.setCache(false);
            for (Node n : sp.getChildrenUnmodifiable()) {
                n.setCache(false);
            }});
        }
    }
    public void leaveIntroduction(){
        hb.setBorder(null);
        hb.setBackground(null);
        endIntroductionBtn.setVisible(false);
        hideBtn.setVisible(true);
        nextBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
        prevBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
    }

    public void activateIntroduction(){
        hb.setBorder(new Border(new BorderStroke(Color.BLACK,new BorderStrokeStyle(StrokeType.OUTSIDE, StrokeLineJoin.ROUND, StrokeLineCap.ROUND,1,2,null),null,new BorderWidths(5))));
        hb.setBackground(new Background(new BackgroundImage(new Image(GameConstants.BG_DARK_TILE_PATH),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,BackgroundSize.DEFAULT)));
        endIntroductionBtn.setVisible(false);
        hideBtn.setVisible(false);
        nextBtn.setStyle("-fx-background-color: white;" +
                "-fx-base: transparent;");

        prevBtn.setStyle("-fx-background-color: white;" +
                "-fx-base: transparent;");
    }
    public boolean isLastMsg() {
        return index == tutorialEntries.size()-1;
    }

    public Button getEndIntroductionBtn() {
        return endIntroductionBtn;
    }
    public void toggleStackpaneVisibility(){
//        this.setPickOnBounds(nextBtn.isVisible());
        nextBtn.setVisible(!nextBtn.isVisible());
        prevBtn.setVisible(!prevBtn.isVisible());
        sp.setVisible(!sp.isVisible());
        wizard.setVisible(!wizard.isVisible());
        hideBtn.setText(nextBtn.isVisible()? "Hide Wizard":"Show Wizard");

    }

    public int getCurrentIndex() {
        return index;
    }
//
//    public void unbind() {
//        for(StringProperty stringProperty : tutorialEntries){
//            stringProperty.unbind();
//        }
//    }
//
//    public void bindEntriesTo(StringListProperty tutorialEntryList) {
//        tutorialEntryList.bindAll(tutorialEntries);
//        currentTutorialMessage.textProperty().bind();
//    }
}
