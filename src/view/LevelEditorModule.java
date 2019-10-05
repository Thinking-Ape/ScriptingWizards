package view;

import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Translate;
import model.Level;
import model.enums.CContent;
import model.enums.ItemType;
import utility.Util;

import java.util.List;


public class LevelEditorModule {

    private Label widthValueLbl = new Label("");
    private Label heightLbl = new Label("Height: ");
    private Label maxKnightsValueLbl = new Label("");
    private Label maxKnightsLbl = new Label("Max Knights: ");
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
    private Button addLinkedCellBtn = new Button("Add Linked Cell");
    private Button removeLinkedCellBtn = new Button("Remove Linked Cell");
    private Button changeCellIdBtn = new Button("Change Cell ID");
    private ListView<Integer> linkedCellListView = new ListView<>();
    private ChoiceBox<String> trapChoiceBox = new ChoiceBox<>();
    private VBox cellTypeVBox = new VBox(new Label("Cell Content:"),cellTypeSelectionGPane);
    private VBox cellItemVBox = new VBox(new Label("Item:"),cellItemSelectionGPane);//,cellIDHBox, addLinkedCellBtn,removeLinkedCellBtn,linkedCellListView,exitOpenCheckBox);

    private Button saveLevelBtn = new Button("Save Level");
    private Button deleteLevelBtn = new Button("Delete Level");
    private Button openLevelBtn = new Button("Open Level");
    private Button newLevelBtn = new Button("New Level");
    private Button resetLevelScoresBtn = new Button("Reset Level Score");
    private Button reloadLevelBtn = new Button("Reload Level");
    private HBox bottomHBox = new HBox(newLevelBtn, openLevelBtn, saveLevelBtn, deleteLevelBtn, reloadLevelBtn, resetLevelScoresBtn);
    private VBox requiredLVBOX = new VBox(requiredLevelsLabel,requiredLevelsLView);
    private Button changeLvlNameBtn = new Button("Change Level Name");
    private HBox topHBox =new HBox(new HBox(levelNameLbl,levelNameValueLbl),changeLvlNameBtn,new Separator(Orientation.VERTICAL),new VBox(new HBox(widthLbl, widthValueLbl), new HBox(heightLbl, heightValueLbl)),new HBox(maxKnightsLbl,maxKnightsValueLbl),new HBox(), new HBox(maxLocVbox,maxLocValueVbox), new HBox(maxTurnsVbox,maxTurnsValueVbox), new HBox(hasAiLbl,hasAiValueLbl),new HBox(isTutorialLbl,isTutorialValueLbl),changeLvlBtn,requiredLVBOX,editRequiredLevelsBtn,new Separator(Orientation.VERTICAL), new HBox(indexLbl,indexValueLbl),new HBox(moveIndexUpBtn,moveIndexDownBtn));

    private Label tutorialTextLbl = new Label("Tutorial Text Nr.");
    private Label tutorialNumberValueLbl = new Label("1");
    private HBox tutorialTopHBox = new HBox(tutorialTextLbl,tutorialNumberValueLbl);
    private TextArea tutorialTextArea = new TextArea();
    private Button editTutorialTextBtn = new Button("Edit Text");
    private Button nextTutorialTextBtn = new Button("Next Entry");
    private Button newTutorialTextBtn = new Button("New Entry");
    private Button prevTutorialTextBtn = new Button("Previous Entry");
    private Button deleteTutorialTextBtn = new Button("Delete Text");
    private HBox editTutHBox = new HBox(editTutorialTextBtn,deleteTutorialTextBtn,newTutorialTextBtn);
    private HBox prevNextTutHBox = new HBox(prevTutorialTextBtn,nextTutorialTextBtn);
    private VBox tutorialVBox = new VBox(tutorialTopHBox,tutorialTextArea,editTutHBox,prevNextTutHBox);
    private VBox cellDetailVBox = new VBox();
    private VBox rightVBox = new VBox(cellTypeVBox,new HBox(cellItemVBox,cellDetailVBox),tutorialVBox);

    public LevelEditorModule(Level level){
//        if(level.getAIBehaviour().getStatementListSize()==0) hasAICheckBox.setSelected(false);
//        else hasAICheckBox.setSelected(true);
        Util.applyValueFormat(tutorialNumberValueLbl,indexValueLbl,isTutorialValueLbl,widthValueLbl,heightValueLbl,levelNameValueLbl,hasAiValueLbl,cellIdValueLbl,maxLoc2StarsVLbl,maxLoc3StarsVLbl,maxTurns2StarsVLbl,maxTurns3StarsVLbl,maxKnightsValueLbl);
        topHBox.setSpacing(20);
        cellTypeVBox.setAlignment(Pos.CENTER);
        cellItemVBox.setAlignment(Pos.CENTER);
        cellItemSelectionGPane.setAlignment(Pos.CENTER);
        rightVBox.setAlignment(Pos.CENTER);
        rightVBox.setSpacing(50);
        tutorialTextArea.setEditable(false);
        tutorialTextArea.setMaxWidth(300);
        tutorialTextArea.setMouseTransparent(true);
        tutorialTextArea.setWrapText(true);

        editTutHBox.setAlignment(Pos.TOP_CENTER);
        prevNextTutHBox.setAlignment(Pos.TOP_CENTER);
        updateTutorialSection(level);

//        cellIDHBox.setVisible(false);
//        addLinkedCellBtn.setVisible(false);
//        removeLinkedCellBtn.setVisible(false);
//        linkedCellListView.setVisible(false);
//        exitOpenCheckBox.setVisible(false);
        levelNameValueLbl.setText(level.getName());
        heightValueLbl.setText(""+level.getOriginalMap().getBoundY());
        widthValueLbl.setText(""+level.getOriginalMap().getBoundX());
        maxLoc3StarsVLbl.setText(""+level.getLocToStars()[1]);
        maxLoc2StarsVLbl.setText(""+level.getLocToStars()[0]);
        maxTurns3StarsVLbl.setText(""+level.getTurnsToStars()[1]);
        maxTurns2StarsVLbl.setText(""+level.getTurnsToStars()[0]);
        requiredLevelsLView.setMaxHeight(25);
        requiredLevelsLView.setMinHeight(25);
        indexValueLbl.setText(""+(level.getIndex()+1));
        isTutorialValueLbl.setText(""+level.isTutorial());
        maxKnightsValueLbl.setText(level.getMaxKnights()+"");
        requiredLVBOX.getTransforms().add(new Translate(0,-20,0));
        topHBox.setMaxHeight(50);//level.getRequiredLevels().length*25+25);
        for(int i = 0; i < level.getRequiredLevels().size();i++){
            requiredLevelsLView.getItems().add(level.getRequiredLevels().get(i));
        }
        int i = 0;
        int j = 0;
        for (CContent content : CContent.values()){

            if(i>4){
                i=0;
                j++;
            }
            Button button = new Button(content.getDisplayName());
            button.setMinWidth(125);
            cellTypeSelectionGPane.add(button,j,i);
            i++;
        }
        for (ItemType item : ItemType.values()){

            if(i>4){
                i=0;
                j++;
            }
            Button button = new Button(item.getDisplayName());
            button.setMinWidth(125);
            cellItemSelectionGPane.add(button,j,i);
            i++;
        }
        Button button = new Button("None");
        button.setMinWidth(125);
        cellItemSelectionGPane.add(button,j,i);
        bottomHBox.setAlignment(Pos.TOP_CENTER);
        topHBox.setAlignment(Pos.BASELINE_CENTER);
    }

    void updateTutorialSection(Level level) {
        boolean notEnoughEntries = false;
        if(level.getTutorialEntryList().size() <= 1){
            notEnoughEntries = true;
        }
        prevTutorialTextBtn.setDisable(notEnoughEntries);
        nextTutorialTextBtn.setDisable(notEnoughEntries);
        if(level.getTutorialEntryList().size() == 0 || notEnoughEntries)deleteTutorialTextBtn.setDisable(true);
        else {
            tutorialTextArea.setText(level.getTutorialEntryList().get(0));
            deleteTutorialTextBtn.setDisable(false);
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

    public Label getLevelNameTField() {
        return levelNameValueLbl;
    }

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

    public void activateCellIDHBox(){
        deactivateCellDetails();
        cellDetailVBox.getChildren().addAll(cellIdLbl, cellIdValueLbl,changeCellIdBtn);
    }
    public void activateLinkedCellBtns(){
        deactivateCellDetails();
        cellDetailVBox.getChildren().addAll(addLinkedCellBtn,removeLinkedCellBtn,linkedCellListView);
    }

//    public void activateExitOpenCheckbox(){
//        deactivateCellDetails();
//        cellDetailVBox.getChildren().add(exitOpenCheckBox);
//    }
    public void activateTrapChoicebox(){
        deactivateCellDetails();
        cellDetailVBox.getChildren().add(trapChoiceBox);
    }
    public void deactivateCellDetails(){
        cellDetailVBox.getChildren().removeAll(cellIdLbl, cellIdValueLbl,changeCellIdBtn);
        cellDetailVBox.getChildren().removeAll(addLinkedCellBtn,removeLinkedCellBtn,linkedCellListView);
//        cellDetailVBox.getChildren().remove(exitOpenCheckBox);
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
}
