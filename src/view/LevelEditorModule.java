package view;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.transform.Translate;
import model.Level;
import model.enums.CContent;
import model.enums.ItemType;
import model.statement.ComplexStatement;
import model.util.GameConstants;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

    //    Button[][] mapButtons; //already in View
//    private Button[][] cellTypeButtons = new Button[][]{};
    private CheckBox exitOpenCheckBox = new CheckBox("Open");
//    private CheckBox hasAICheckBox = new CheckBox("Has AI");
    private GridPane cellTypeSelectionGPane = new GridPane();
    private GridPane cellItemSelectionGPane = new GridPane();
    private Label cellIdValueLbl = new Label("");
    private Label cellIdLbl = new Label("Cell Id:");
    private Button addLinkedCellBtn = new Button("Add Linked Cell");
    private Button removeLinkedCellBtn = new Button("Remove Linked Cell");
    private Button changeCellIdBtn = new Button("Change Cell ID");
    private HBox cellIDHBox = new HBox(cellIdLbl, cellIdValueLbl,changeCellIdBtn);
    //TODO: ListView<Integer>?
    private ListView<Integer> linkedCellListView = new ListView<>();
    private ChoiceBox<String> trapChoiceBox = new ChoiceBox<>();
    private VBox cellTypeVBox = new VBox(new Label("Cell Content:"),cellTypeSelectionGPane);
    private VBox cellItemVBox = new VBox(new Label("Item:"),cellItemSelectionGPane);//,cellIDHBox, addLinkedCellBtn,removeLinkedCellBtn,linkedCellListView,exitOpenCheckBox);
    private VBox rightVBox = new VBox(new HBox(cellTypeVBox,cellItemVBox));
    private Button saveLevelBtn = new Button("Save Level");
    private Button deleteLevelBtn = new Button("Delete Level");
    private Button openLevelBtn = new Button("Open Level");
    private Button newLevelBtn = new Button("New Level");
    private Button resetLevelScoresBtn = new Button("Reset Level Score");
    private Button reloadLevelBtn = new Button("Reload Level");
    private HBox bottomHBox = new HBox(newLevelBtn, openLevelBtn, saveLevelBtn, deleteLevelBtn, reloadLevelBtn, resetLevelScoresBtn);
    private VBox requiredLVBOX = new VBox(requiredLevelsLabel,requiredLevelsLView);
//    private TextField levelNameTField = new TextField();

    private HBox topHBox =new HBox(new HBox(hasAiLbl,hasAiValueLbl),new HBox(levelNameLbl,levelNameValueLbl),new VBox(new HBox(widthLbl, widthValueLbl), new HBox(heightLbl, heightValueLbl)),new HBox(maxKnightsLbl,maxKnightsValueLbl),new HBox(), new HBox(maxLocVbox,maxLocValueVbox), new HBox(maxTurnsVbox,maxTurnsValueVbox), new HBox(isTutorialLbl,isTutorialValueLbl),changeLvlBtn,requiredLVBOX,editRequiredLevelsBtn, new HBox(indexLbl,indexValueLbl),new HBox(moveIndexUpBtn,moveIndexDownBtn));

    public LevelEditorModule(Level level){
//        if(level.getAIBehaviour().getStatementListSize()==0) hasAICheckBox.setSelected(false);
//        else hasAICheckBox.setSelected(true);
        GameConstants.applyValueFormat(indexValueLbl,isTutorialValueLbl,widthValueLbl,heightValueLbl,levelNameValueLbl,hasAiValueLbl,cellIdValueLbl,maxLoc2StarsVLbl,maxLoc3StarsVLbl,maxTurns2StarsVLbl,maxTurns3StarsVLbl,maxKnightsValueLbl);
        topHBox.setSpacing(20);
        cellTypeVBox.setAlignment(Pos.CENTER);
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
    public HBox getCellIDHBox(){
        return cellIDHBox;
    }

    public Button getAddLinkedCellBtn() {
        return addLinkedCellBtn;
    }
    public Button getRemoveLinkedCellBtn() {
        return removeLinkedCellBtn;
    }

    public Button getNewLevelBtn() {
        return newLevelBtn;
    }
    public CheckBox getExitOpenCheckBox(){
        return exitOpenCheckBox;
    }
    public Label getHasAiValueLbl(){
        return hasAiValueLbl;
    }

    public void activateCellIDHBox(){
        deactivateCellDetails();
        rightVBox.getChildren().add(cellIDHBox);
    }
    public void activateLinkedCellBtns(){
        deactivateCellDetails();
        rightVBox.getChildren().addAll(addLinkedCellBtn,removeLinkedCellBtn,linkedCellListView);
    }

    public void activateExitOpenCheckbox(){
        deactivateCellDetails();
        rightVBox.getChildren().add(exitOpenCheckBox);
    }
    public void activateTrapChoicebox(){
        deactivateCellDetails();
        rightVBox.getChildren().add(trapChoiceBox);
    }
    public void deactivateCellDetails(){
        rightVBox.getChildren().remove(cellIDHBox);
        rightVBox.getChildren().removeAll(addLinkedCellBtn,removeLinkedCellBtn,linkedCellListView);
        rightVBox.getChildren().remove(exitOpenCheckBox);
        rightVBox.getChildren().remove(trapChoiceBox);
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
        getEditLvlBtn().setDisable(b);
        editRequiredLevelsBtn.setDisable(b);
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
//    @Override
//    public void propertyChange(PropertyChangeEvent evt) {
////        if(evt.getNewValue().equals(evt.getOldValue()))return;
//        switch (evt.getPropertyName()){
//            case "name":
//                levelNameValueLbl.setText(""+ evt.getNewValue());
//                break;
//            case "requiredLevels":
//                requiredLevelsLView.getItems().clear();
//                List<String> requiredLevelsList = (List<String>)evt.getNewValue();
//                requiredLevelsLView.getItems().addAll(requiredLevelsList);
//                break;
//            case "width":
//                widthValueLbl.setText(""+evt.getNewValue());
//                break;
//            case "height":
//                heightValueLbl.setText(""+evt.getNewValue());
//                break;
//            case "locToStars":
//                Integer[] locToStars = (Integer[])evt.getNewValue();
//                maxLoc3StarsVLbl.setText(""+locToStars[1]);
//                maxLoc2StarsVLbl.setText(""+locToStars[0]);
//                break;
//            case "turnsToStars":
//                Integer[] turnsToStars = (Integer[])evt.getNewValue();
//                maxTurns3StarsVLbl.setText(""+turnsToStars[1]);
//                maxTurns2StarsVLbl.setText(""+turnsToStars[0]);
//                break;
//            case "maxKnights":
//                maxKnightsValueLbl.setText(""+evt.getNewValue());
//                break;
//            case "aiBehaviour":
//                ComplexStatement aiBehaviour = (ComplexStatement)evt.getNewValue();
//                if(aiBehaviour.getStatementListSize()>0)hasAiValueLbl.setText(""+true);
//                else hasAiValueLbl.setText(""+false);
//                break;
//        }
//    }
}
