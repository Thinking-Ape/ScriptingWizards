package main.view;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Translate;
import main.model.Level;
import main.model.LevelChange;
import main.model.Model;
import main.model.enums.CellContent;
import main.model.enums.ItemType;
import main.model.gamemap.GameMap;
import main.utility.GameConstants;
import main.utility.Util;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static main.utility.GameConstants.*;
import static main.utility.GameConstants.TUTORIAL_LINES;


public class LevelEditorModule {

    private Label widthValueLbl = new Label("");
    private Label heightLbl = new Label("Height: ");
    private Label maxKnightsValueLbl = new Label("");
    private Label maxKnightsLbl = new Label("Max Knights: ");
    private Label amountOfRerunsValueLbl = new Label("");
    private Label amountOfRerunsLbl = new Label("Amount of Plays: ");
    private Label heightValueLbl = new Label("");
    private Label widthLbl = new Label("Width: ");
    private Label hasAiLbl = new Label("Has AI: ");
    private Label hasAiValueLbl = new Label("true");
    private Label levelNameLbl = new Label("Level Name: ");
    private Label levelNameValueLbl = new Label("");
    private Button changeLvlBtn = new Button("Edit");
    private Label maxLoc3StarsLbl = new Label("*** max. LoC: ");
    private Label maxLoc3StarsVLbl = new Label("");
    private Label maxLoc2StarsLbl = new Label("** max. LoC: ");
    private Label maxLoc2StarsVLbl = new Label("");
    private VBox maxLocVbox = new VBox(maxLoc3StarsLbl,maxLoc2StarsLbl);
    private VBox maxLocValueVbox = new VBox(maxLoc3StarsVLbl,maxLoc2StarsVLbl);
    private Label maxTurns3StarsLbl = new Label("*** max. Turns: ");
    private Label maxTurns3StarsVLbl = new Label("");
    private Label maxTurns2StarsLbl = new Label("** max. Turns: ");
    private Label maxTurns2StarsVLbl = new Label("");
    private VBox maxTurnsVbox = new VBox(maxTurns3StarsLbl,maxTurns2StarsLbl);
    private VBox maxTurnsValueVbox = new VBox(maxTurns3StarsVLbl,maxTurns2StarsVLbl);
    private Label requiredLevelsLabel = new Label("Required Levels");
    private ListView<String> requiredLevelsLView = new ListView<>();
    private Button editRequiredLevelsBtn = new Button("Edit Required Levels");
    private Label indexLbl = new Label("Index: ");
    private Label indexValueLbl = new Label("");
    private Label isTutorialLbl = new Label("Is Tutorial: ");
    private Label isTutorialValueLbl = new Label("");
    private Button moveIndexUpBtn = new Button("Move Level Up");
    private Button moveIndexDownBtn = new Button("Move Level Down");

    private GridPane cellTypeSelectionGPane = new GridPane();
    private GridPane cellItemSelectionGPane = new GridPane();
    private Label cellIdValueLbl = new Label("");
    private Label cellIdLbl = new Label("Cell Id:");
    private Button addLinkedCellBtn = new Button("+ Linked Cell");
    private Button removeLinkedCellBtn = new Button("- Linked Cell");
    private Button changeCellIdBtn = new Button("Change Cell Cell Id");
    private ListView<Integer> linkedCellListView = new ListView<>();
    private ChoiceBox<String> trapChoiceBox = new ChoiceBox<>();
    private VBox cellTypeVBox = new VBox(new Label("Cell Content:"),cellTypeSelectionGPane);
    private VBox cellItemVBox = new VBox(new Label("Item:"),cellItemSelectionGPane);//,cellIDHBox, addLinkedCellBtn,removeLinkedCellBtn,linkedCellListView,exitOpenCheckBox);

    private Button saveLevelBtn = new Button("Save Level");
    private Button deleteLevelBtn = new Button("Delete Level");
    private Button openLevelBtn = new Button("Open Level");
    private Button newLevelBtn = new Button("New Level");
    private Button copyLevelBtn = new Button("Copy Level");
    private Button resetLevelScoresBtn = new Button("Reset Level Score");
    private Button reloadLevelBtn = new Button("Reload Level");
    private HBox bottomHBox = new HBox(openLevelBtn, saveLevelBtn,newLevelBtn, copyLevelBtn, deleteLevelBtn, reloadLevelBtn, resetLevelScoresBtn);
    private VBox requiredLVBOX = new VBox(requiredLevelsLabel,requiredLevelsLView);
    private Button changeLvlNameBtn = new Button("Change Level Name");
    private HBox topHBox =new HBox(new HBox(levelNameLbl,levelNameValueLbl),changeLvlNameBtn,new Separator(Orientation.VERTICAL),new VBox(new HBox(widthLbl, widthValueLbl), new HBox(heightLbl, heightValueLbl)),new VBox(new HBox(amountOfRerunsLbl,amountOfRerunsValueLbl),new HBox(maxKnightsLbl,maxKnightsValueLbl)),new HBox(),  new HBox(maxTurnsVbox,maxTurnsValueVbox),new HBox(maxLocVbox,maxLocValueVbox), new HBox(hasAiLbl,hasAiValueLbl),new HBox(isTutorialLbl,isTutorialValueLbl),changeLvlBtn,requiredLVBOX,editRequiredLevelsBtn,new Separator(Orientation.VERTICAL), new HBox(indexLbl,indexValueLbl),new HBox(moveIndexUpBtn,moveIndexDownBtn));

    private Label tutorialTextLbl = new Label("Tutorial Text Nr.");
    private Label tutorialNumberValueLbl = new Label("1");
    private HBox tutorialTopHBox = new HBox(tutorialTextLbl,tutorialNumberValueLbl);
    private TextArea tutorialTextArea = new TextArea();
    private Button editTutorialTextBtn = new Button("Edit Text");
    private Button nextTutorialTextBtn = new Button("Next Entry");
    private Button newTutorialTextBtn = new Button("New Entry");
    private Button prevTutorialTextBtn = new Button("Previous Entry");
    private Button deleteTutorialTextBtn = new Button("Delete Entry");
    private HBox editTutHBox = new HBox(editTutorialTextBtn,deleteTutorialTextBtn,newTutorialTextBtn);
    private HBox prevNextTutHBox = new HBox(prevTutorialTextBtn,nextTutorialTextBtn);
    private VBox tutorialVBox = new VBox(tutorialTopHBox,tutorialTextArea,editTutHBox,prevNextTutHBox);
    private VBox cellDetailVBox = new VBox();
    private HBox itemAndDetailHBox = new HBox(cellItemVBox,cellDetailVBox);
    private VBox rightVBox = new VBox(cellTypeVBox, itemAndDetailHBox,tutorialVBox);
    private CheckBox isTurnedCBox = new CheckBox("Is Turned");
    private CheckBox isInvertedCBox = new CheckBox("Is Open");
    private Label cellDetailLbl = new Label("Cell Details:");

//    private HBox checkBoxHbox = new HBox(isTurnedCBox,isInvertedCBox);

    public LevelEditorModule(){
//        if(level.getAIBehaviourCopy().getStatementListSize()==0) hasAICheckBox.setSelected(false);
//        else hasAICheckBox.setSelected(true);
        Util.applyValueFormat(tutorialNumberValueLbl,indexValueLbl,isTutorialValueLbl,widthValueLbl,heightValueLbl,levelNameValueLbl,hasAiValueLbl,cellIdValueLbl,maxLoc2StarsVLbl,maxLoc3StarsVLbl,maxTurns2StarsVLbl,maxTurns3StarsVLbl,maxKnightsValueLbl,amountOfRerunsValueLbl);
        levelNameValueLbl.setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
        Util.applyFontFormatRecursively(topHBox);
        isTurnedCBox.setStyle("-fx-background-color: white");
        isInvertedCBox.setStyle("-fx-background-color: white");
        topHBox.setSpacing(TEXTFIELD_HEIGHT+5);
        topHBox.setBackground(new Background(new BackgroundFill(Color.LIGHTGREY,null,Insets.EMPTY)));
        cellTypeVBox.setAlignment(Pos.CENTER);
        cellTypeVBox.setStyle("-fx-background-color: lightgray");
        tutorialVBox.setStyle("-fx-background-color: lightgray");
        tutorialVBox.setAlignment(Pos.TOP_CENTER);
        cellDetailVBox.setStyle("-fx-background-color: lightgray");
        cellTypeVBox.autosize();
        cellTypeVBox.setMaxWidth(BUTTON_SIZE*2);
        cellItemVBox.setAlignment(Pos.TOP_CENTER);
        cellItemVBox.setStyle("-fx-background-color: lightgray");
        cellItemSelectionGPane.setAlignment(Pos.TOP_CENTER);
        itemAndDetailHBox.setSpacing(TEXTFIELD_HEIGHT);
        itemAndDetailHBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.setAlignment(Pos.CENTER);
        rightVBox.setSpacing(BUTTON_SIZE/2);
        tutorialTextArea.setEditable(false);
        tutorialTextArea.setMaxWidth(GameConstants.TEXTFIELD_WIDTH*0.8);
        tutorialTextArea.setMinWidth(GameConstants.TEXTFIELD_WIDTH*0.8);
        tutorialTextArea.setMaxHeight(TEXTFIELD_HEIGHT*7);
        tutorialTextArea.setMinHeight(TEXTFIELD_HEIGHT*6);
        tutorialTextArea.setFont(new Font(tutorialTextArea.getFont().getName(), FONT_SIZE));

        tutorialTextArea.setMouseTransparent(true);
        tutorialTextArea.setWrapText(true);
        linkedCellListView.setMaxWidth(TEXTFIELD_HEIGHT*2);
        linkedCellListView.setMaxHeight(TEXTFIELD_HEIGHT);
        editTutHBox.setAlignment(Pos.TOP_CENTER);
        prevNextTutHBox.setAlignment(Pos.TOP_CENTER);
        cellDetailVBox.setAlignment(Pos.TOP_CENTER);
        cellDetailVBox.setSpacing(5);

        prevTutorialTextBtn.setDisable(true);

//        cellIDHBox.setVisible(false);
//        addLinkedCellBtn.setVisible(false);
//        removeLinkedCellBtn.setVisible(false);
//        linkedCellListView.setVisible(false);
//        exitOpenCheckBox.setVisible(false);


        requiredLevelsLView.setMaxHeight(TEXTFIELD_HEIGHT*1.2);
        requiredLevelsLView.setMinHeight(TEXTFIELD_HEIGHT*1.2);
        requiredLevelsLView.setMaxWidth(TEXTFIELD_WIDTH*0.5);
        requiredLevelsLView.setMinWidth(TEXTFIELD_WIDTH*0.5);
        requiredLevelsLabel.setFont(new Font(requiredLevelsLabel.getFont().getName(),GameConstants.FONT_SIZE));
//        indexValueLbl.setText(""+(level.getIndex()+1));
//        isTutorialValueLbl.setText(""+level.isTutorial());
        requiredLVBOX.getTransforms().add(new Translate(0,-TEXTFIELD_HEIGHT*0.8,0));
        topHBox.setMaxHeight(TEXTFIELD_HEIGHT*2);//level.getRequiredLevelIdsCopy().length*25+25);
        //TODO!
//        update(level);
        int i = 0;
        int j = 0;
        for (CellContent content : CellContent.values()){

            if(i>4){
                i=0;
                j++;
            }
            Button button = new Button(content.getDisplayName());
            button.setMinWidth(BUTTON_SIZE*1.25);
            cellTypeSelectionGPane.add(button,j,i);
            i++;
        }
        for (ItemType item : ItemType.values()){

            if(i>4){
                i=0;
                j++;
            }
            Button button = new Button(item.getDisplayName());
            button.setMinWidth(BUTTON_SIZE*1.25);
            cellItemSelectionGPane.add(button,j,i);
            i++;
        }
//        Button button = new Button("None");
//        button.setMinWidth(125);
//        cellItemSelectionGPane.add(button,j,i);
        bottomHBox.setAlignment(Pos.TOP_CENTER);
        topHBox.setAlignment(Pos.BASELINE_CENTER);
        if(Model.getAmountOfLevels()==1)deleteLevelBtn.setDisable(true);
    }

    void update(LevelChange change) {
        switch (change.getLevelDataType()){
            case AMOUNT_OF_RERUNS:
                amountOfRerunsValueLbl.setText(change.getNewValue()+"");
                break;
            case LEVEL_INDEX:
                indexValueLbl.setText(change.getNewValue()+"");
                break;
            case MAX_KNIGHTS:
                maxKnightsValueLbl.setText(change.getNewValue()+"");
                break;
            case MAP_DATA:
                GameMap gameMap = (GameMap) change.getNewValue();
                heightValueLbl.setText(""+gameMap.getBoundY());
                widthValueLbl.setText(""+gameMap.getBoundX());
                break;
            case AI_CODE:
                throw new IllegalStateException("This module doesnt change when the AI changes!");
            case HAS_AI:
                hasAiValueLbl.setText(change.getNewValue()+"");
                break;
            case LOC_TO_STARS:
                Integer[] locToStars = (Integer[])change.getNewValue();
                maxLoc2StarsVLbl.setText(locToStars[0]+"");
                maxLoc3StarsVLbl.setText(locToStars[1]+"");
                break;
            case TURNS_TO_STARS:
                Integer[] turnsToStars = (Integer[])change.getNewValue();
                maxTurns2StarsVLbl.setText(turnsToStars[0]+"");
                maxTurns3StarsVLbl.setText(turnsToStars[1]+"");
                break;
            case REQUIRED_LEVELS:
                requiredLevelsLView.getItems().clear();
                List<Integer> requiredLevels = (List<Integer>)change.getNewValue();
                for(int i = 0; i < requiredLevels.size(); i++){
                    requiredLevelsLView.getItems().add(Model.getNameOfLevelWithId(requiredLevels.get(i)));
                }
                break;
            case IS_TUTORIAL:
                isTutorialValueLbl.setText(change.getNewValue()+"");
                break;
            case TUTORIAL_LINES:
                List<String> tutorialLines = (List<String>)change.getNewValue();
                if(tutorialLines.size()== 0)nextTutorialTextBtn.setDisable(true);
                //TODO
                break;
            case LEVEL_NAME:
                levelNameValueLbl.setText(change.getNewValue()+"");
                break;
        }

    }

    public VBox getRightVBox(){
        return rightVBox;
    }
    public HBox getBottomHBox(){
        return bottomHBox;
    }

    public HBox getTopHBox() {
        return topHBox;
    }

    public GridPane getCellTypeSelectionGPane() {
        return cellTypeSelectionGPane;
    }
    public GridPane getCellItemSelectionGPane() {
        return cellItemSelectionGPane;
    }

    public Label getCellIdValueLbl() {
        return cellIdValueLbl;
    }

    //TODO: ListView<Integer>?
    public ListView<Integer> getLinkedCellListView() {
        return linkedCellListView;
    }

    public Button getSaveLevelBtn() {
        return saveLevelBtn;
    }

   /* public void bindProperties(StringProperty[] properties) {
        for(StringProperty stringProperty : properties)
        switch (stringProperty.getName()){
            case GameConstants.LEVEL_NAME_PROPERTY_NAME:
                levelNameValueLbl.textProperty().unbind();
                levelNameValueLbl.textProperty().bind(stringProperty);
                break;
            case GameConstants.MAX_KNIGHTS_PROPERTY_NAME:
                maxKnightsValueLbl.textProperty().unbind();
                maxKnightsValueLbl.textProperty().bind(stringProperty);
                break;
            case GameConstants.IS_TUTORIAL_PROPERTY_NAME:
                isTutorialValueLbl.textProperty().unbind();
                isTutorialValueLbl.textProperty().bind(stringProperty);
                break;
            case GameConstants.INDEX_PROPERTY_NAME:
                indexValueLbl.textProperty().unbind();
                indexValueLbl.textProperty().bind(stringProperty);
                break;
            case TURNS_TO_STARS_3_PROPERTY_NAME:
                maxTurns3StarsVLbl.textProperty().unbind();
                maxTurns3StarsVLbl.textProperty().bind(stringProperty);
                break;
            case TURNS_TO_STARS_2_PROPERTY_NAME:
                maxTurns2StarsVLbl.textProperty().unbind();
                maxTurns2StarsVLbl.textProperty().bind(stringProperty);
                break;
            case LOC_TO_STARS_3_PROPERTY_NAME:
                maxLoc3StarsVLbl.textProperty().unbind();
                maxLoc3StarsVLbl.textProperty().bind(stringProperty);
                break;
            case LOC_TO_STARS_2_PROPERTY_NAME:
                maxLoc2StarsVLbl.textProperty().unbind();
                maxLoc2StarsVLbl.textProperty().bind(stringProperty);
                break;
            case CURRENT_TUTORIAL_MESSAGE_PROPERTY_NAME:
                tutorialTextArea.textProperty().unbind();
                tutorialTextArea.textProperty().bind(stringProperty);
                break;

            case CURRENT_TUTORIAL_INDEX_PROPERTY_NAME:
                tutorialNumberValueLbl.textProperty().unbind();
                tutorialNumberValueLbl.textProperty().bind(stringProperty);
                break;
        }
    }*/

    public Button getOpenLevelBtn() {
        return openLevelBtn;
    }

    public Label getWidthValueLbl() {
        return widthValueLbl;
    }

    public Label getHeightValueLbl() {
        return heightValueLbl;
    }

    public Button getEditLvlBtn() {
        return changeLvlBtn;
    }
//    public HBox getCellIDHBox(){
//        return cellIDHBox;
//    }

    public Button getAddLinkedCellBtn() {
        return addLinkedCellBtn;
    }
    public Button getRemoveLinkedCellBtn() {
        return removeLinkedCellBtn;
    }

    public Button getNewLevelBtn() {
        return newLevelBtn;
    }
//    public CheckBox getExitOpenCheckBox(){
//        return exitOpenCheckBox;
//    }
    public Label getHasAiValueLbl(){
        return hasAiValueLbl;
    }

    public void activateCellIDHBox(boolean isPressurePlate){
        deactivateCellDetails();
        if(isPressurePlate) {
            isInvertedCBox.setText("Is Inverted");
            cellDetailVBox.getChildren().addAll(cellDetailLbl, isInvertedCBox, cellIdLbl, cellIdValueLbl, changeCellIdBtn);
        }
        else cellDetailVBox.getChildren().addAll(cellDetailLbl,cellIdLbl, cellIdValueLbl,changeCellIdBtn);
    }
    public void activateLinkedCellBtns(){
        deactivateCellDetails();
        isInvertedCBox.setText("Is Open");
        cellDetailVBox.getChildren().addAll(cellDetailLbl,isTurnedCBox,isInvertedCBox,addLinkedCellBtn,removeLinkedCellBtn,linkedCellListView);
    }
//    public void addTurnable(){
//        if(!cellDetailVBox.getChildren().contains(isTurnedCBox))cellDetailVBox.getChildren().add(isTurnedCBox);
//    }

//    public void activateExitOpenCheckbox(){
//        deactivateCellDetails();
//        cellDetailVBox.getChildren().add(exitOpenCheckBox);
//    }
    public void activateTrapChoicebox(){
        deactivateCellDetails();
        cellDetailVBox.getChildren().addAll(cellDetailLbl,trapChoiceBox);
    }
    public void deactivateCellDetails(){
        cellDetailVBox.getChildren().removeAll(cellIdLbl, cellIdValueLbl,changeCellIdBtn);
        cellDetailVBox.getChildren().removeAll(addLinkedCellBtn,removeLinkedCellBtn,linkedCellListView,isTurnedCBox,isInvertedCBox);
        cellDetailVBox.getChildren().remove(cellDetailLbl);
        cellDetailVBox.getChildren().remove(trapChoiceBox);
    }

    public Button getDeleteLevelBtn() {
        return deleteLevelBtn;
    }
    public Button getEditRequiredLevelsBtn() {
        return editRequiredLevelsBtn;
    }

    public ListView<String> getRequiredLevelsLView() {
        return requiredLevelsLView;
    }

    public ChoiceBox<String> getTrapChoiceBox() {
        return trapChoiceBox;
    }

    public Button getChangeCellIdBtn() {
        return changeCellIdBtn;
    }

    public void setLOCToStarsValues(Integer[] locToStars) {
        maxLoc2StarsVLbl.setText(locToStars[0]+"");
        maxLoc3StarsVLbl.setText(locToStars[1]+"");
    }
    public void setTurnsToStarsValues(Integer[] turnsToStars) {
        maxTurns2StarsVLbl.setText(turnsToStars[0]+"");
        maxTurns3StarsVLbl.setText(turnsToStars[1]+"");
    }
    public void setRequiredLevels(List<String> requiredLevels) {
        requiredLevelsLView.getItems().clear();
        if(requiredLevels == null) return;
        for(String s : requiredLevels){
            this.requiredLevelsLView.getItems().add(s);
        }
    }

    public Label getMaxKnightsValueLbl() {
        return maxKnightsValueLbl;
    }

    public void setDisableAllLevelBtns(boolean b) {
        deleteLevelBtn.setDisable(b);
        openLevelBtn.setDisable(b);
        newLevelBtn.setDisable(b);
        copyLevelBtn.setDisable(b);
        saveLevelBtn.setDisable(b);
        reloadLevelBtn.setDisable(b);
        resetLevelScoresBtn.setDisable(b);
        changeLvlBtn.setDisable(b);
        editRequiredLevelsBtn.setDisable(b);
        moveIndexUpBtn.setDisable(b);
        moveIndexDownBtn.setDisable(b);
        changeLvlNameBtn.setDisable(b);
        deleteTutorialTextBtn.setDisable(b);
        prevTutorialTextBtn.setDisable(b);
        editTutorialTextBtn.setDisable(b);
        nextTutorialTextBtn.setDisable(b);
        newTutorialTextBtn.setDisable(b);
    }

    public Button getResetLevelScoresBtn() {
        return resetLevelScoresBtn;
    }

    public Button getReloadLevelBtn() {
        return reloadLevelBtn;
    }
    public Button getCopyLevelBtn() {
        return copyLevelBtn;
    }

    public Label getIndexValueLbl() {
        return indexValueLbl;
    }

    public Label getIsTutorialValueLbl() {
        return isTutorialValueLbl;
    }

    public Button getMoveIndexUpBtn() {
        return moveIndexUpBtn;
    }

    public Button getMoveIndexDownBtn() {
        return moveIndexDownBtn;
    }
    public Button getChangeLvlNameBtn(){
        return changeLvlNameBtn;
    }

    public Label getTutorialNumberValueLbl() {
        return tutorialNumberValueLbl;
    }

    public TextArea getTutorialTextArea() {
        return tutorialTextArea;
    }

    public Button getEditTutorialTextBtn() {
        return editTutorialTextBtn;
    }

    public Button getNewTutorialTextBtn() {
        return newTutorialTextBtn;
    } public Button getNextTutorialTextBtn() {
        return nextTutorialTextBtn;
    }

    public Button getPrevTutorialTextBtn() {
        return prevTutorialTextBtn;
    }

    public Button getDeleteTutorialTextBtn() {
        return deleteTutorialTextBtn;
    }
    public VBox getTutorialVBox() {
        return tutorialVBox;
    }

    public CheckBox getIsTurnedCBox() {
        return isTurnedCBox;
    }
    public CheckBox getIsInvertedCBox() {
        return isInvertedCBox;
    }

    public void toggleLevelIsSaved(boolean confirmed){
        if(confirmed)levelNameValueLbl.setStyle(LEVEL_IS_SAVED_STYLE);
        else levelNameValueLbl.setStyle(LEVEL_NOT_SAVED_STYLE);
    }
    public Label getLevelNameValueLbl(){
        return levelNameValueLbl;
    }

    public Label getMaxLoc3StarsVLbl() {
        return maxLoc3StarsVLbl;
    }

    public Label getMaxLoc2StarsVLbl() {
        return maxLoc2StarsVLbl;
    }

    public Label getMaxTurns3StarsVLbl() {
        return maxTurns3StarsVLbl;
    }

    public Label getMaxTurns2StarsVLbl() {
        return maxTurns2StarsVLbl;
    }

    public void showRequiredLevelsHBox(boolean b) {
//        if(b && !topHBox.getChildren().contains(requiredLevelsHBox)){
//            topHBox.getChildren().add(12,requiredLevelsHBox);
//        }
//        else topHBox.getChildren().remove(requiredLevelsHBox );
        requiredLVBOX.setVisible(b);
        editRequiredLevelsBtn.setVisible(b);
    }

    public Label getAmountOfRerunsValueLbl() {
        return amountOfRerunsValueLbl;
    }
}
