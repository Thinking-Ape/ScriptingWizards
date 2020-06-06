package main.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import main.model.*;
import main.model.gamemap.Cell;
import main.model.gamemap.GameMap;
import main.model.gamemap.enums.CellContent;
import main.model.gamemap.enums.CellFlag;
import main.model.gamemap.enums.ItemType;
import main.model.statement.ComplexStatement;
import main.model.statement.SimpleStatement;
import main.model.GameConstants;
import main.parser.JSONParser;
import main.utility.Point;
import main.utility.SimpleEventListener;
import main.utility.Util;
import main.view.SceneState;
import main.view.View;

import java.io.IOException;
import java.util.*;
import java.util.List;

public class EditorController implements SimpleEventListener {

    private View view;
    private Model model;

    private boolean actionEventFiring = true;

    public EditorController(View view,Model model){
        this.view = view;
        this.model = model;
        view.addListener(this);
    }

    void setAllEditButtonsToDisable(boolean b) {
        view.getLevelEditorModule().getNewLevelBtn().setDisable(b);
        view.getLevelEditorModule().getOpenLevelBtn().setDisable(b);
        view.getLevelEditorModule().getDeleteLevelBtn().setDisable(b);
        view.getLevelEditorModule().getSaveLevelBtn().setDisable(b);
        view.getLevelEditorModule().getEditLvlBtn().setDisable(b);
        if(b){
            view.setAllCellButtonsDisabled(true);
            view.setCContentButtonDisabled(CellContent.EMPTY,true);
            view.setCContentButtonDisabled(CellContent.WALL,true);
        }
    }

    public void setEditorHandlers() {
        if((boolean)model.getDataFromCurrentLevel(LevelDataType.HAS_AI))view.getLevelEditorModule().getHasAiValueLbl().setText(""+true);
        else view.getLevelEditorModule().getHasAiValueLbl().setText(""+false);
        setHandlersForMapCells();
        setHandlersForCellTypeButtons();
        for(Point p : view.getSelectedPointList()){
            int k = p.getX();
            int h = p.getY();
            GameMap gameMapClone = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
            int cellId  = gameMapClone.getCellID(k,h);
            if(cellId !=-1)view.getLevelEditorModule().getCellIdValueLbl().setText(cellId+"");
            else view.getLevelEditorModule().getCellIdValueLbl().setText("NONE");
            changeEditorModuleDependingOnCellContent(view.getSelectedPointList());
        }

        view.getStage().getScene().setOnKeyPressed(event -> {

            if(View.getCurrentSceneState() != SceneState.LEVEL_EDITOR)return;
            if(!event.isControlDown())return;
            switch (event.getCode()){
                case S:
                    view.getLevelEditorModule().getSaveLevelBtn().fire();
                    break;
                case O:
                    view.getLevelEditorModule().getOpenLevelBtn().fire();
                    break;
            }
        });

        view.getLevelEditorModule().getEditTutorialTextBtn().setOnAction(evt -> {
            Dialog<ButtonType> editTutorialDialog = new Dialog<>();
            String text = "";

            List<String> tutLines = (List<String>)model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
            if(model.getCurrentTutorialSize() > 0) text = tutLines.get(model.getCurrentTutorialMessageIndex());
            TextArea tutorialTextArea = new TextArea();
            tutorialTextArea.setMaxSize(GameConstants.CODEFIELD_WIDTH, GameConstants.CODEFIELD_WIDTH /2.0);
            tutorialTextArea.setFont(GameConstants.BIG_FONT);
            tutorialTextArea.setWrapText(true);
            tutorialTextArea.setText(text);
            editTutorialDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            editTutorialDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            editTutorialDialog.getDialogPane().setContent(tutorialTextArea);
            editTutorialDialog.getDialogPane().setMaxWidth(GameConstants.CODEFIELD_WIDTH *1.05);
            Platform.runLater(tutorialTextArea::requestFocus);
            tutorialTextArea.textProperty().addListener((observableValue, s, t1) ->{
                // Cant have more than MAX_TUTORIAL_LINES lines of tutorial
                if(Util.getRowCount(tutorialTextArea) > GameConstants.MAX_TUTORIAL_LINES)tutorialTextArea.setText(s);
            });

            Optional<ButtonType> o  = editTutorialDialog.showAndWait();
            tutorialTextArea.requestFocus();
            if(o.isPresent()&& o.get() == ButtonType.OK){
                tutLines = (List<String>)model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
                String tutorialText = tutorialTextArea.getText();
                tutLines.set(model.getCurrentTutorialMessageIndex(),tutorialText.trim());
                model.changeCurrentLevel(LevelDataType.TUTORIAL_LINES,tutLines);
            }
        });

        view.getLevelEditorModule().getDeleteTutorialTextBtn().setOnAction(evt -> {
            Optional<ButtonType> o  = new Alert(Alert.AlertType.NONE,"Do you really want to delete this tutorial text?",ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if(o.isPresent()&& o.get() == ButtonType.OK){
                List<String> tutLines = (List<String>)model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
                tutLines.remove(model.getCurrentTutorialMessageIndex());
                model.decreaseTutorialMessageIndex();
                model.changeCurrentLevel(LevelDataType.TUTORIAL_LINES,tutLines);
            }
        });

        view.getLevelEditorModule().getNewTutorialTextBtn().setOnAction(evt -> {
            if(view.getLevelEditorModule().getTutorialTextArea().getText().equals("")){
                new Alert(Alert.AlertType.NONE,"Please enter some text into the current TextArea!",ButtonType.OK).showAndWait();
                return;
            }

            List<String> tutLines = (List<String>)model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
            tutLines.add(model.getCurrentTutorialMessageIndex()+1,"");
            model.increaseTutorialMessageIndex();
            model.changeCurrentLevel(LevelDataType.TUTORIAL_LINES,tutLines);
            view.getLevelEditorModule().getDeleteTutorialTextBtn().setDisable(false);
            view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(false);

        });

        view.getLevelEditorModule().getPrevTutorialTextBtn().setOnAction(evt -> {
            model.prevTutorialMessage();
            int index = model.getCurrentTutorialMessageIndex();
            view.updateTutorialMessage();
            view.getLevelEditorModule().getNextTutorialTextBtn().setDisable(false);
            if(index == 0){
                view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(true);
            }
        });
        view.getLevelEditorModule().getNextTutorialTextBtn().setOnAction(evt -> {
            model.nextTutorialMessage();
            int index = model.getCurrentTutorialMessageIndex();
            view.updateTutorialMessage();
            view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(false);
            if(index+1 == ((List<String>)model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES)).size()){
                view.getLevelEditorModule().getNextTutorialTextBtn().setDisable(true);
            }
        });

        view.getLevelEditorModule().getSaveLevelBtn().setOnAction(event -> {
            saveChanges(model.getAndConfirmCurrentChanges());
        });
        view.getLevelEditorModule().getOpenLevelBtn().setOnAction(event -> {
            if(model.currentLevelHasChanged())
                showSavingDialog();

            ChoiceDialog<String> levelsToOpenDialog = new ChoiceDialog<>();
            for (int i = 0; i < model.getAmountOfLevels(); i++) {
                levelsToOpenDialog.getItems().add(model.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME,i)+"");
            }
            String levelName = (String)model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME);
            levelsToOpenDialog.setSelectedItem(levelName);
            Optional<String> s = levelsToOpenDialog.showAndWait();
            s.ifPresent(s1 -> {
                model.selectLevel(s1);
                Platform.runLater(() -> {
                    view.highlightInMap(List.of(new Point(0,0)));
                });
            });
        });
        view.getLevelEditorModule().getDeleteLevelBtn().setOnAction(event -> {

            Alert deleteAlert = new Alert(Alert.AlertType.NONE, "You are about to permanently delete this level!", ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> btnType =deleteAlert.showAndWait();
            if(btnType.isPresent() && btnType.get() == ButtonType.OK){
                String levelName = (String)model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME);
                view.getLevelOverviewPane().removeCurrentLevel();
                if(view.getLevelOverviewPane().getLevelListView().getItems().size() == 0)view.getStartScreen().getPlayBtn().setDisable(true);
                view.getTutorialLevelOverviewPane().removeCurrentLevel();
                try {
                    JSONParser.deleteLevel(levelName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                model.removeCurrentLevel();
            }
            if(model.getAmountOfLevels()==1)view.getLevelEditorModule().getDeleteLevelBtn().setDisable(true);

            Platform.runLater(() -> {
                view.highlightInMap(List.of(new Point(0,0)));
            });
        });
        view.getLevelEditorModule().getResetLevelScoresBtn().setOnAction(event -> {

            Alert deleteAlert = new Alert(Alert.AlertType.NONE, "You are about to reset the score for this level!", ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> btnType =deleteAlert.showAndWait();
            if(btnType.isPresent() && btnType.get() == ButtonType.OK){
                Alert infoAlert;
                model.resetScoreOfCurrentLevel();
                infoAlert = new Alert(Alert.AlertType.NONE, "Score was reset!", ButtonType.OK);
                infoAlert.showAndWait();

            }
        });

        view.getLevelEditorModule().getReloadLevelBtn().setOnAction(event -> {

            Alert deleteAlert = new Alert(Alert.AlertType.NONE, "You are about to reload this level! Unsaved changes will be discarded!", ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> btnType =deleteAlert.showAndWait();
            if(btnType.isPresent() && btnType.get() == ButtonType.OK){
                model.reloadCurrentLevel();
                Platform.runLater(() -> {
                    view.highlightInMap(List.of(new Point(0,0)));
                });
            }
        });
        view.getLevelEditorModule().getNewLevelBtn().setOnAction(event -> {
            if(model.currentLevelHasChanged())showSavingDialog();
            Dialog<ButtonType> newLevelDialog = new Dialog<>();
            TextField nameTField = new TextField();
            TextField heightTField = new TextField();
            TextField widthTField = new TextField();
            heightTField.setText(GameConstants.MIN_LEVEL_SIZE+"");
            widthTField.setText(GameConstants.MIN_LEVEL_SIZE+"");
            Util.setTextFieldWidth(heightTField,widthTField);
            nameTField.textProperty().addListener((observableValue, s, t1) -> {

                if(model.hasLevelWithName(t1)){
                    newLevelDialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(true);
                    newLevelDialog.setContentText("This level already exists!");
                }

                else {
                    newLevelDialog.getDialogPane().lookupButton(ButtonType.OK).setDisable(false);
                    newLevelDialog.setContentText("");
                }
                if(t1.matches("(\\d+.*)+")){
                    nameTField.setText(s);
                }
            });
            addChangeListenerForIntTFields(heightTField,widthTField);
            CheckBox hasAiCheckBox = new CheckBox();
            newLevelDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            newLevelDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            newLevelDialog.getDialogPane().setContent(new HBox(new Label("Name:"),nameTField,new Label("Width"),widthTField,new Label("Height"),heightTField,new Label("Has AI"),hasAiCheckBox));

            Optional<ButtonType> o  = newLevelDialog.showAndWait();
            if(o.isPresent()&& o.get() == ButtonType.OK) {
                if(heightTField.getText().equals("") || widthTField.getText().equals("")){
                    new Alert(Alert.AlertType.NONE,"Level could not be created due to a lack of width or height parameters!");
                }
                int height = Integer.valueOf(heightTField.getText());
                int width = Integer.valueOf(widthTField.getText());
                width = width > GameConstants.MAX_LEVEL_SIZE ? GameConstants.MAX_LEVEL_SIZE : width < GameConstants.MIN_LEVEL_SIZE ? GameConstants.MIN_LEVEL_SIZE:width;
                height = height > GameConstants.MAX_LEVEL_SIZE ? GameConstants.MAX_LEVEL_SIZE : height < GameConstants.MIN_LEVEL_SIZE ? GameConstants.MIN_LEVEL_SIZE:height;

                Cell[][] map = new Cell[width][height];
                for(int i = 0; i < width;i++){
                    for(int j = 0; j < height;j++){
                        map[i][j] = new Cell(CellContent.WALL);
                    }
                }

                ComplexStatement complexStatement = new ComplexStatement();
                if(hasAiCheckBox.isSelected())complexStatement.addSubStatement(new SimpleStatement());
                Integer[] turnsToStars = new Integer[2];
                turnsToStars[0] = 0;
                turnsToStars[1] = 0;
                Integer[] locToStars = new Integer[2];
                locToStars[0] = 0;
                locToStars[1] = 0;
                int id = model.createUniqueId();
                // Automatically makes this level a tutorial if the current Level is a tutorial. This makes it impossible
                // to create non-Tutorial levels between tutorials (which would end the tutorial prematurely)
                boolean isTutorial = (boolean)model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,model.getCurrentIndex());
                Level newLevel = new Level(nameTField.getText(),map,complexStatement,turnsToStars,locToStars,new ArrayList<>(),1,isTutorial,new ArrayList<>(),id,1);
                model.addLevelAtCurrentPos(newLevel,true);
                // There is no spawn when creating a new Level!
                view.getLevelEditorModule().getSaveLevelBtn().setDisable(true);
                view.getLevelEditorModule().getHasAiValueLbl().setText(""+hasAiCheckBox.isSelected());
                view.getLevelEditorModule().getDeleteLevelBtn().setDisable(false);
                Platform.runLater(() -> view.highlightInMap(List.of(new Point(0,0))));
            }

        });

        view.getLevelEditorModule().getCopyLevelBtn().setOnAction(event -> {
            if(model.currentLevelHasChanged())showSavingDialog();
            String name = model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME)+"_Copy";
            if(model.hasLevelWithName(name)){
                new Alert(Alert.AlertType.INFORMATION,"This level already exists!").showAndWait();
                return;
            }
            GameMap gameMap = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);

            int height = gameMap.getBoundY();
            int width = gameMap.getBoundX();
            Cell[][] map = new Cell[width][height];
            for(int i = 0; i < width;i++){
                for(int j = 0; j < height;j++){
                    map[i][j] = gameMap.getCellAtXYClone(i, j);
                }
            }
            ComplexStatement complexStatement = (ComplexStatement)model.getDataFromCurrentLevel(LevelDataType.AI_CODE);
            Integer[] turnsToStars = (Integer[]) model.getDataFromCurrentLevel(LevelDataType.TURNS_TO_STARS);
            Integer[] locToStars = (Integer[]) model.getDataFromCurrentLevel(LevelDataType.LOC_TO_STARS);
            List<Integer> requiredLevelsList = (List<Integer>) model.getDataFromCurrentLevel(LevelDataType.REQUIRED_LEVELS);

            List<String> tutorialEntries = (List<String>) model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
            int maxKnights = (int) model.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS);
            boolean isTutorial = (boolean) model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL);
            boolean hasAI = (boolean) model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
            int id =model.createUniqueId();
            model.addLevelAtCurrentPos(new Level(name,map,complexStatement,turnsToStars,locToStars,requiredLevelsList,maxKnights,isTutorial,tutorialEntries,id,1),true);

            view.getLevelEditorModule().getDeleteLevelBtn().setDisable(false);
            if(((GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA)).findSpawn().getX()==-1){
                view.getLevelEditorModule().getSaveLevelBtn().setDisable(true);
            }
            else view.getLevelEditorModule().getSaveLevelBtn().setDisable(false);
            view.getLevelEditorModule().getHasAiValueLbl().setText(""+hasAI);
            Platform.runLater(() -> view.highlightInMap(List.of(new Point(0,0))));
        });


        view.getLevelEditorModule().getEditRequiredLevelsBtn().setOnAction(this::handleEditRequiredLevelsBtn);
        view.getLevelEditorModule().getMoveIndexDownBtn().setOnAction(actionEvent -> {
            int currentLevelIndex = model.getCurrentIndex();
            if(currentLevelIndex == 0){
                return;
            }
            if((boolean)model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,currentLevelIndex-1) &&
                    !(boolean) model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL))
                new Alert(Alert.AlertType.NONE,"Can't move Level down, as Challenge Levels are not allowed to stand in between Tutorial Levels",ButtonType.OK).showAndWait();
            else model.changeCurrentLevel(LevelDataType.LEVEL_INDEX,currentLevelIndex-1);
            view.getLevelEditorModule().getMoveIndexUpBtn().setDisable(false);
        });
        view.getLevelEditorModule().getMoveIndexUpBtn().setOnAction(actionEvent -> {
            int currentLevelIndex = model.getCurrentIndex();
            if(currentLevelIndex == model.getAmountOfLevels()-1){
                return;
            }

            if(model.getNextTutorialIndex()==-1 && (boolean) model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL))
                new Alert(Alert.AlertType.NONE,"Can't move Level up, as Tutorial Levels are not allowed to stand after a Challenge Level",ButtonType.OK).showAndWait();
            else model.changeCurrentLevel(LevelDataType.LEVEL_INDEX,currentLevelIndex+1);
            view.getLevelEditorModule().getMoveIndexDownBtn().setDisable(false);
        });
        view.getLevelEditorModule().getEditLvlBtn().setOnAction(event -> {
            Dialog<ButtonType> changeLvlDialog = new Dialog<>();
            Slider heightSlider = new Slider();
            Label heightLbl = new Label();
            heightSlider.valueProperty().addListener((observableValue, number, t1) -> heightLbl.setText(t1.intValue()+""));
            Slider widthSlider = new Slider();
            Label widthLbl = new Label();
            Slider maxKnightsSlider = new Slider();
            Label maxKnightsLbl = new Label();
            Slider amountOfPlaysSlider = new Slider();
            Label amountOfPlaysLbl = new Label();
            maxKnightsSlider.valueProperty().addListener((observableValue, number, t1) -> maxKnightsLbl.setText(t1.intValue()+""));
            amountOfPlaysSlider.valueProperty().addListener((observableValue, number, t1) -> amountOfPlaysLbl.setText(t1.intValue()+""));
            Util.applyValueFormat(heightLbl,widthLbl,maxKnightsLbl,amountOfPlaysLbl);
            widthSlider.valueProperty().addListener((observableValue, number, t1) -> widthLbl.setText(t1.intValue()+""));
            VBox sizeVBox = new VBox(new HBox(new Label("Width: "),widthLbl),widthSlider,new HBox(new Label("Height: "),heightLbl),heightSlider);
            sizeVBox.setAlignment(Pos.CENTER);
            VBox sliderVBox = new VBox(new HBox(new Label("Max Knights: "),maxKnightsLbl),maxKnightsSlider,new HBox(new Label("Amount of Plays: "),amountOfPlaysLbl),amountOfPlaysSlider);
            formatSlider(heightSlider,GameConstants.MIN_LEVEL_SIZE,GameConstants.MAX_LEVEL_SIZE);
            formatSlider(widthSlider,GameConstants.MIN_LEVEL_SIZE,GameConstants.MAX_LEVEL_SIZE);
            formatSlider(maxKnightsSlider,1,GameConstants.MAX_KNIGHTS_AMOUNT);
            formatSlider(amountOfPlaysSlider,1,GameConstants.MAX_AMOUNT_OF_RUNS);
            GameMap currentMapClone = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
            heightSlider.setValue(currentMapClone.getBoundY());
            widthSlider.setValue(currentMapClone.getBoundX());
            maxKnightsSlider.setValue((int)model.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS));
            amountOfPlaysSlider.setValue((int)model.getDataFromCurrentLevel(LevelDataType.AMOUNT_OF_RERUNS));

            Integer[] locToStars = (Integer[]) model.getDataFromCurrentLevel(LevelDataType.LOC_TO_STARS);
            Integer[] turnsToStars = (Integer[]) model.getDataFromCurrentLevel(LevelDataType.TURNS_TO_STARS);
            TextField loc2StarsTField = new TextField(locToStars[0]+"");
            TextField loc3StarsTField = new TextField(locToStars[1]+"");
            TextField turns2StarsTField = new TextField(turnsToStars[0]+"");
            TextField turns3StarsTField = new TextField(turnsToStars[1]+"");

            addChangeListenerForIntTFields(loc2StarsTField,loc3StarsTField,turns2StarsTField,turns3StarsTField);
            Util.setTextFieldWidth(loc2StarsTField,loc3StarsTField,turns2StarsTField,turns3StarsTField);
            CheckBox hasAiCheckBox = new CheckBox();
            CheckBox isTutorialCheckBox = new CheckBox();
            HBox hBox = new HBox(sizeVBox,sliderVBox,new VBox(new Label("*** max. Turns: "),new Label("** max. Turns: ")),new VBox(turns3StarsTField,turns2StarsTField),new VBox(new Label("*** max. LoC: "),new Label("** max. LoC: ")),new VBox(loc3StarsTField,loc2StarsTField),
                    new Label("Has AI"),hasAiCheckBox,new Label("Is Tutorial"),isTutorialCheckBox);
            boolean currentLevelHasAI = (boolean) model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
            hasAiCheckBox.setSelected(currentLevelHasAI);
            boolean currentLevelIsTut = (boolean)model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL);
            isTutorialCheckBox.setSelected(currentLevelIsTut);
            boolean prevLevelIsTut = model.getCurrentIndex() ==0 || (boolean)model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,model.getCurrentIndex()-1);
            boolean nextLevelIsTut = model.getCurrentIndex() != model.getAmountOfLevels()-1 && (boolean)model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,model.getCurrentIndex() +1);
            // allow only those levels to be a tutorial whose predecessor is a tutorial to avoid weird required level dependencies!
            if(!prevLevelIsTut||nextLevelIsTut)isTutorialCheckBox.setDisable(true);
            changeLvlDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            changeLvlDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            changeLvlDialog.getDialogPane().setContent(hBox);

            Optional<ButtonType> o  = changeLvlDialog.showAndWait();
            if(o.isPresent()&& o.get() == ButtonType.OK){
                int width = (int)widthSlider.getValue();
                int height = (int)heightSlider.getValue();
                width = width > GameConstants.MAX_LEVEL_SIZE ? GameConstants.MAX_LEVEL_SIZE : width < GameConstants.MIN_LEVEL_SIZE ? GameConstants.MIN_LEVEL_SIZE:width;
                height = height > GameConstants.MAX_LEVEL_SIZE ? GameConstants.MAX_LEVEL_SIZE : height < GameConstants.MIN_LEVEL_SIZE ? GameConstants.MIN_LEVEL_SIZE:height;

                currentMapClone.changeHeight(height);
                currentMapClone.changeWidth(width);
                model.changeCurrentLevel(LevelDataType.MAP_DATA,currentMapClone);
                model.changeCurrentLevel(LevelDataType.MAX_KNIGHTS,(int)maxKnightsSlider.getValue());
                model.changeCurrentLevel(LevelDataType.AMOUNT_OF_RERUNS,(int)amountOfPlaysSlider.getValue());
                model.changeCurrentLevel(LevelDataType.LOC_TO_STARS,new Integer[]{Integer.valueOf(loc2StarsTField.getText()),Integer.valueOf(loc3StarsTField.getText())});
                model.changeCurrentLevel(LevelDataType.TURNS_TO_STARS,new Integer[]{Integer.valueOf(turns2StarsTField.getText()),Integer.valueOf(turns3StarsTField.getText())});
                model.changeCurrentLevel(LevelDataType.IS_TUTORIAL,isTutorialCheckBox.isSelected());
                if(!hasAiCheckBox.isSelected())model.changeCurrentLevel(LevelDataType.AI_CODE,new ComplexStatement());
                else if(!(boolean)model.getDataFromCurrentLevel(LevelDataType.HAS_AI)){
                    ComplexStatement complexStatement = new ComplexStatement();
                    complexStatement.addSubStatement(new SimpleStatement());
                    model.changeCurrentLevel(LevelDataType.AI_CODE,complexStatement);
                }
                setEditorHandlers();
            }
        });

    view.getLevelEditorModule().getChangeLvlNameBtn().setOnAction(event -> {
        Dialog<ButtonType> changeLvlNameDialog = new Dialog<>();
        TextField nameTField = new TextField((String)model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME));

        nameTField.textProperty().addListener((observableValue, s, t1) -> {
            if(t1.matches("(\\d+.*)+")||!t1.matches("[A-Za-zäÄöÖüÜß_]+((\\d|[A-Za-zäÄöÖüÜß_ ])*(\\d|[A-Za-zäÄöÖüÜß_]))?")){
                nameTField.setText(s);
            }
        });
        HBox hBox = new HBox(new Label("Name:"),nameTField);
        changeLvlNameDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        changeLvlNameDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        changeLvlNameDialog.getDialogPane().setContent(hBox);

        Optional<ButtonType> o  = changeLvlNameDialog.showAndWait();
        if(o.isPresent()&& o.get() == ButtonType.OK){
            model.changeCurrentLevel(LevelDataType.LEVEL_NAME,nameTField.getText());
        }
    });
}

    private void saveChanges(Map<LevelDataType, LevelChange> changes) {

        try {
            if(model.isCurrentLevelNew()){
                JSONParser.saveCurrentLevel( );
                Platform.runLater(()->model.resetLevelNew());
            }
            else JSONParser.saveLevelChanges(changes, (String)model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME));
            model.updateUnlockedLevelsList(true);
            if(!model.getUnlockedLevelIds().contains(model.getCurrentId())){
                view.getTutorialLevelOverviewPane().removeCurrentLevel();
                view.getLevelOverviewPane().removeCurrentLevel();
            } else {
                String levelName = model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME)+"";
                if((boolean)model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL)){
                    if(view.getTutorialLevelOverviewPane().containsLevel(levelName))
                        view.getTutorialLevelOverviewPane().updateLevel(levelName);
                    else view.getTutorialLevelOverviewPane().addLevelWithIndex(model.getCurrentIndex());
                }
                else {

                    if(view.getLevelOverviewPane().containsLevel(levelName))
                        view.getLevelOverviewPane().updateLevel(model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME)+"");
                    else view.getLevelOverviewPane().addLevelWithIndex(model.getCurrentIndex());
                }
            }
            if(view.getLevelOverviewPane().getLevelListView().getItems().size() == 0)view.getStartScreen().getPlayBtn().setDisable(true);
            else view.getStartScreen().getPlayBtn().setDisable(false);

            Alert alert = new Alert(Alert.AlertType.NONE,"Level was saved!",ButtonType.OK);
            alert.setTitle("Success!");
            alert.setHeaderText("");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showSavingDialog() {
        Alert savingAlert = new Alert(Alert.AlertType.NONE, "This level contains unsaved changes! Do you want to save them?", ButtonType.YES,ButtonType.NO);
        Optional<ButtonType> btnType =savingAlert.showAndWait();
        if(btnType.isPresent() && btnType.get() == ButtonType.YES){
            if(((GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA)).findSpawn().getX()==-1){
                model.reloadCurrentLevel();
                Alert alert = new Alert(Alert.AlertType.NONE,"You cannot save a level without a spawn!",ButtonType.OK);
                alert.setTitle("Alert!");
                alert.setHeaderText("");
                alert.showAndWait();
            }
            else saveChanges(model.getAndConfirmCurrentChanges());
        }
        else if(btnType.isPresent()){
            model.reloadCurrentLevel();
        }
    }

    private void formatSlider(Slider slider,int min, int max) {

        slider.setMax(max);
        slider.setMin(min);
        slider.setMajorTickUnit(1);
        slider.setMajorTickUnit(1);
        slider.setBlockIncrement(1);
        slider.setMinorTickCount(0);
        slider.setSnapToTicks(true);
        slider.setShowTickMarks(true);
    }

    private void addChangeListenerForIntTFields(TextField... textFields) {
        for(TextField textField : textFields){
            textField.textProperty().addListener((observableValue, s, t1) -> {
            if(!t1.matches("\\d+")){
                if(s.matches("\\d+"))textField.setText(s);
                textField.setText(""+0);
            }
            if(t1.matches("0(\\d+)")){
                textField.setText(textField.getText().substring(1));
            }
            if(t1.length() > 3)textField.setText(s);
        });}
    }

    void setHandlersForMapCells() {
        int columnC = view.getMapGPane().getColumnCount();
        int rowC = view.getMapGPane().getRowCount();
        for(int x = 0; x < columnC; x++){
            for(int y = 0; y < rowC; y++){
                StackPane stackPane = (StackPane) view.getMapGPane().getChildren().get(x*rowC+y);

                final GameMap gameMap = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                final int k = x;
                final int h = y;
                stackPane.setOnMousePressed(mouseEvent -> {
                    List<Point> selectedList = new ArrayList<>();
                    if(mouseEvent.isControlDown()) {
                        if(!mouseEvent.isShiftDown()) selectedList.addAll(view.getSelectedPointList());
                        if(mouseEvent.isShiftDown()) {
                            selectedList.addAll(Util.getPointsInRectangle(view.getSelectedPointList().get(view.getSelectedPointList().size()-1),new Point(k, h)));
                        }
                        else if(selectedList.contains(new Point(k,h)))selectedList.remove(new Point(k,h));
                        else selectedList.add(new Point(k,h));
                    }
                    else if(mouseEvent.isShiftDown()) {
                        selectedList.addAll(Util.getPointsInRectangle(view.getSelectedPointList().get(view.getSelectedPointList().size()-1),new Point(k, h)));
                    }
                    else selectedList.add(new Point(k,h));

                    view.highlightInMap(selectedList);

                    if(!testIfEmptyIsAllowed(gameMap,k,h))view.setCContentButtonDisabled(CellContent.EMPTY,true);
                    if(!testIfNormalContentIsAllowed(gameMap,k,h))view.setAllCellButtonsDisabled(true);

                    int cellId  = gameMap.getCellID(k,h);
                    if(cellId !=-1)view.getLevelEditorModule().getCellIdValueLbl().setText(cellId+"");
                    else view.getLevelEditorModule().getCellIdValueLbl().setText("NONE");

                    changeEditorModuleDependingOnCellContent(selectedList);
                });}}
                view.getLevelEditorModule().getRemoveLinkedCellBtn().setOnAction(actionEvent -> {
                    final GameMap gameMap = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                    if(view.getSelectedPointList().size() > 1)return;
                    int id;
                    int index = 0;
                    if(view.getLinkedCellsListView().getSelectionModel().getSelectedItem() == null) id = view.getLinkedCellsListView().getItems().get(index);
                    else {
                        id = view.getLinkedCellsListView().getSelectionModel().getSelectedItem();
                        index = view.getLinkedCellsListView().getSelectionModel().getSelectedIndex();
                    }
                    view.getLinkedCellsListView().getItems().remove(index);
                    int size = view.getLinkedCellsListView().getItems().size();
                    view.getLinkedCellsListView().setMaxHeight(size <= 3 ? size * GameConstants.CODEFIELD_HEIGHT : 3 * GameConstants.CODEFIELD_HEIGHT);
                    gameMap.removeCellLinkedId(view.getSelectedPointList().get(0).getX(),view.getSelectedPointList().get(0).getY(),id);
                    model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
                    if(view.getLinkedCellsListView().getItems().size()==0){
                        view.getLinkedCellsListView().setVisible(false);
                        view.getLevelEditorModule().getRemoveLinkedCellBtn().setDisable(true);
                    }
                });
                view.getLinkedCellsListView().setOnMouseClicked(event -> {
                    if(view.getLinkedCellsListView().getSelectionModel().getSelectedItem()!=null){
                        view.getLevelEditorModule().getRemoveLinkedCellBtn().setVisible(true);
                    }
                    else view.getLevelEditorModule().getRemoveLinkedCellBtn().setVisible(false);
                });

                view.getLevelEditorModule().getChangeCellIdBtn().setOnKeyPressed(event->{
                    event.consume();
                    if(view.getCodeArea().getSelectedCodeField() != null)view.getCodeArea().getSelectedCodeField().requestFocus();
                    if(view.getAiCodeArea().getSelectedCodeField() != null)view.getAiCodeArea().getSelectedCodeField().requestFocus();
                });

                view.getLevelEditorModule().getChangeCellIdBtn().setOnAction(event->{
                    final GameMap gameMap = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                    if(view.getSelectedPointList().size() > 1) return;
                    Dialog<ButtonType> newLevelDialog = new Dialog<>();
                    TextField idTField = new TextField();
                    addChangeListenerForIntTFields(idTField);
                    Util.setTextFieldWidth(idTField);
                    newLevelDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                    newLevelDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                    newLevelDialog.getDialogPane().setContent(new HBox(new Label("Id:"),idTField));

                    Optional<ButtonType> o  = newLevelDialog.showAndWait();
                    if(o.isPresent()){
                        if (o.get() == ButtonType.OK) {
                            int id = -1;
                            if(!idTField.getText().equals("")) id = Integer.valueOf(idTField.getText());
                            int x = view.getSelectedPointList().get(0).getX();
                            int y = view.getSelectedPointList().get(0).getY();
                            if(gameMap.testIfIdIsUnique(id)){
                                gameMap.deleteOldCellIdFromLinkedIds(gameMap.getCellID(x,y ));
                                gameMap.setCellId(x,y,id);
                                view.getLevelEditorModule().getCellIdValueLbl().setText(id!=-1 ? ""+id : "NONE");
                                model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
                            }
                            else new Alert(Alert.AlertType.NONE,"Id "+id+" already in use",ButtonType.OK).showAndWait();
                        }
                    }
                });
                view.getLevelEditorModule().getIsInvertedCBox().setOnAction(evt -> {
                    GameMap gameMap = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                    if(view.getSelectedPointList().size()!=1)return;
                    int column = view.getSelectedPointList().get(0).getX();
                    int  row = view.getSelectedPointList().get(0).getY();
                    gameMap.setFlag(column, row, CellFlag.INVERTED,view.getLevelEditorModule().getIsInvertedCBox().isSelected());
                    model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
                });
                view.getLevelEditorModule().getIsTurnedCBox().setOnAction(evt -> {
                    GameMap gameMap = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                    if(view.getSelectedPointList().size()!=1)return;
                    int column = view.getSelectedPointList().get(0).getX();
                    int  row = view.getSelectedPointList().get(0).getY();
                    gameMap.setFlag(column, row, CellFlag.TURNED,view.getLevelEditorModule().getIsTurnedCBox().isSelected());
                    model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
                });
                view.getLevelEditorModule().getAddLinkedCellBtn().setOnAction(actionEvent -> {
                    if( view.getSelectedPointList().size() > 1) return;
                    GameMap gameMap = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                    ChoiceDialog<Integer> idsDialog = new ChoiceDialog<>();
                    for(int x2 = 0; x2 < gameMap.getBoundX();x2++){
                        for(int y2 = 0; y2 < gameMap.getBoundY();y2++){
                            CellContent content1 = gameMap.getContentAtXY(x2,y2);
                            int cellId = gameMap.getCellID(x2,y2);
                            if(cellId!=-1&&content1== CellContent.PRESSURE_PLATE&&!view.getLinkedCellsListView().getItems().contains(cellId))idsDialog.getItems().add(cellId);
                        }
                    }
                    if(idsDialog.getItems().size()>0){
                        idsDialog.setSelectedItem(idsDialog.getItems().get(0));
                        Optional<Integer> s = idsDialog.showAndWait();
                        if(s.isPresent()) {
                            int column = view.getSelectedPointList().get(0).getX();
                            int row = view.getSelectedPointList().get(0).getY();
                            view.getLinkedCellsListView().setVisible(true);
                            view.getLevelEditorModule().getRemoveLinkedCellBtn().setDisable(false);
                            boolean cellHasLinkedId = gameMap.cellHasLinkedCellId(column,row,s.get());
                            if (!cellHasLinkedId) {
                                gameMap.addLinkedCellId(column,row,s.get());
                                model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
                                //TODO: duplicate code in view.getLevelEditorModule().getRemoveLinkedCellBtn().setOnAction(actionEvent -> {
                                int size = view.getLinkedCellsListView().getItems().size();
                                view.getLinkedCellsListView().setMaxHeight(size <= 3 ? size * GameConstants.CODEFIELD_HEIGHT : 3 * GameConstants.CODEFIELD_HEIGHT);
                            }
                        }
                    }
                    else new Alert(Alert.AlertType.NONE, "There are no more Pressure Plates with a unique Cell Id to add", ButtonType.OK).showAndWait();
                });
    }

    private boolean testIfNormalContentIsAllowed(GameMap currentMap, int k, int h) {
        boolean leftSideNothing = h == 0 || currentMap.getContentAtXY(k,h-1)== CellContent.EMPTY;
        boolean rightSideNothing = h == currentMap.getBoundY()-1 || currentMap.getContentAtXY(k,h+1)== CellContent.EMPTY;
        boolean topSideNothing = k == 0 || currentMap.getContentAtXY(k-1,h)== CellContent.EMPTY;
        boolean bottomSideNothing = k == currentMap.getBoundX()-1 || currentMap.getContentAtXY(k+1,h)== CellContent.EMPTY;

        return !leftSideNothing && !rightSideNothing && !bottomSideNothing && !topSideNothing;
    }

    private boolean testIfEmptyIsAllowed(GameMap originalMap, int k, int h) {
        boolean leftSideWallOrNothing = h == 0 || originalMap.getContentAtXY(k,h-1)== CellContent.WALL||originalMap.getContentAtXY(k,h-1)== CellContent.EMPTY;
        boolean rightSideWallOrNothing = h == originalMap.getBoundY()-1 || originalMap.getContentAtXY(k,h+1)== CellContent.WALL ||originalMap.getContentAtXY(k,h+1)== CellContent.EMPTY;
        boolean topSideWallOrNothing = k == 0 || originalMap.getContentAtXY(k-1,h)== CellContent.WALL|| originalMap.getContentAtXY(k-1,h)== CellContent.EMPTY;
        boolean bottomSideWallOrNothing = k == originalMap.getBoundX()-1 || originalMap.getContentAtXY(k+1,h)== CellContent.WALL || originalMap.getContentAtXY(k+1,h)== CellContent.EMPTY;

        return leftSideWallOrNothing && rightSideWallOrNothing && bottomSideWallOrNothing && topSideWallOrNothing;
    }

    private void changeEditorModuleDependingOnCellContent(List<Point> points) {
        if(view.getSelectedPointList().size() == 0){
            view.setAllCellButtonsDisabled(true);
            return;
        }
        view.setAllCellButtonsDisabled(false);
        GameMap gameMap = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        boolean traversable = true;
        CellContent content = null;
        for(Point p : points){
            int x = p.getX();
            int y = p.getY();
            if(!testIfNormalContentIsAllowed(gameMap,x,y)){
                view.setAllCellButtonsDisabled(true);
                view.setCContentButtonDisabled(CellContent.EMPTY,false);
                view.setCContentButtonDisabled(CellContent.WALL,false);
            }
            if(!testIfEmptyIsAllowed(gameMap,x,y))view.setCContentButtonDisabled(CellContent.EMPTY,true);
            content = gameMap.getContentAtXY(x, y);
            traversable = traversable && content.isTraversable();
        }

        if(!traversable) view.setAllItemBtnsDisable(true);
        ItemType item = gameMap.getItem(points.get(0).getX(),points.get(0).getY());
        if(points.size() == 1 && item != ItemType.NONE){
            view.setItemButtonDisabled(item,true);
        }
        else view.setItemButtonDisabled(ItemType.NONE,true);
        if(view.getSelectedPointList().size() > 1){
            view.setCContentButtonDisabled(CellContent.SPAWN,true);
            view.getLevelEditorModule().deactivateCellDetails();
            view.setItemButtonDisabled(ItemType.NONE,false);
            return;
        }
        final int x = points.get(0).getX();
        final int y = points.get(0).getY();
        view.setCContentButtonDisabled(content, true);
        if(content == CellContent.TRAP){
            view.getLevelEditorModule().activateTrapChoicebox();

            ChoiceBox<String> choiceBox = view.getLevelEditorModule().getTrapChoiceBox();
            choiceBox.getItems().clear();
            choiceBox.getItems().add(0,"Unarmed");
            choiceBox.getItems().add(1, CellFlag.PREPARING.getDisplayName());
            choiceBox.getItems().add(2, CellFlag.ARMED.getDisplayName());
            if(gameMap.cellHasFlag(x,y, CellFlag.PREPARING)) choiceBox.getSelectionModel().select(1);
            else if(gameMap.cellHasFlag(x,y, CellFlag.ARMED)) choiceBox.getSelectionModel().select(2);
            else {
                choiceBox.getSelectionModel().select(0);
            }
            choiceBox.setOnHidden(evt -> {
                // .setOnHidden fires twice for some reason??! this is a workaround!
                if(toggleActionEventFiring())return;
                CellFlag flag = CellFlag.getValueFrom(choiceBox.getValue().toUpperCase());
                gameMap.setFlag(x, y, CellFlag.PREPARING,false);
                gameMap.setFlag(x, y, CellFlag.ARMED,false );
                if(flag != null)gameMap.setFlag(x, y, flag,true );
                if(flag == CellFlag.ARMED)gameMap.setItem(x, y, ItemType.NONE);
                model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
            });
        }
        else if(content == CellContent.PRESSURE_PLATE||content == CellContent.ENEMY_SPAWN){
            view.getLevelEditorModule().activateCellIDHBox(content == CellContent.PRESSURE_PLATE);
            view.getLevelEditorModule().getIsInvertedCBox().setSelected(gameMap.cellHasFlag(x,y, CellFlag.INVERTED));
        }

        else if(content == CellContent.GATE){
            view.getLevelEditorModule().activateLinkedCellBtns();
            view.getLevelEditorModule().getIsTurnedCBox().setSelected(gameMap.cellHasFlag(x,y, CellFlag.TURNED));

            view.getLevelEditorModule().getIsInvertedCBox().setSelected(gameMap.cellHasFlag(x,y, CellFlag.INVERTED));

            ListView<Integer> listView =  view.getLevelEditorModule().getLinkedCellListView();
            listView.getItems().clear();
            //TODO: only gate has linked cells?
            int linkedCellListSize = gameMap.getLinkedCellListSize(x,y);
            for(int l = 0; l < linkedCellListSize; l++){
                listView.getItems().add(l,gameMap.getLinkedCellId(x,y,l));
            }
            if(linkedCellListSize>0){
                listView.setMaxHeight(linkedCellListSize*GameConstants.CODEFIELD_HEIGHT);
                listView.setMaxWidth(GameConstants.CODEFIELD_WIDTH /4);
                view.getLinkedCellsListView().setVisible(true);
            }else {
                view.getLinkedCellsListView().setVisible(false);
                view.getLevelEditorModule().getRemoveLinkedCellBtn().setDisable(true);
            }
        }else view.getLevelEditorModule().deactivateCellDetails();
    }

    private boolean toggleActionEventFiring() {
        return actionEventFiring = !actionEventFiring;
    }

    //TODO: do the same as above
    private void setHandlersForCellTypeButtons() {
        int columnC = view.getCellTypeSelectionPane().getColumnCount();
        int rowC = view.getCellTypeSelectionPane().getRowCount();
        GameMap gameMap = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        for(int i = 0; i < columnC; i++) for(int j = 0; j < rowC; j++){
            if(i*rowC+j > view.getCellTypeSelectionPane().getChildren().size()-1)return;
            Button btn = (Button) view.getCellTypeSelectionPane().getChildren().get(i*rowC+j);
            final CellContent content = CellContent.getValueFromName(btn.getText().toUpperCase().replaceAll(" ", "_"));
            btn.setOnAction(mouseEvent -> {
                for(Point p : view.getSelectedPointList()){
                int column = p.getX();
                int row = p.getY();
                gameMap.clearFlags(column,row);
                if(content == null) throw new IllegalStateException("Content: " + btn.getText().toUpperCase().replaceAll(" ", "_") + " doesnt exist!");
                if(content == CellContent.SPAWN){
                    for(int x = 0; x < gameMap.getBoundX();x++){
                        for(int y = 0; y < gameMap.getBoundY();y++){
                            if(gameMap.getContentAtXY(x,y) == CellContent.SPAWN){
                                gameMap.setContent(x,y, CellContent.PATH);
                            }
                        }
                    }
                }
                int id = gameMap.getCellID(column,row);
                if(id!=-1){
                    gameMap.setCellId(column,row,-1);
                    for(int x = 0; x < gameMap.getBoundX(); x++)for(int y = 0; y < gameMap.getBoundY(); y++)gameMap.removeCellLinkedId(x,y,id);
                }
                view.setCContentButtonDisabled(content,true);
                if(!testIfEmptyIsAllowed(gameMap,column,row)){
                    view.setCContentButtonDisabled(CellContent.EMPTY,true);
                }
                changeEditorModuleDependingOnCellContent(view.getSelectedPointList());}
                gameMap.setMultipleContents(view.getSelectedPointList(),content);
                model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
                if(gameMap.findSpawn().getX()==-1){
                    view.getLevelEditorModule().getSaveLevelBtn().setDisable(true);
                } else {
                    view.getLevelEditorModule().getSaveLevelBtn().setDisable(false);
                }
                setHandlersForMapCells();
                });
        }

        columnC = view.getCellItemSelectionPane().getColumnCount();
        rowC = view.getCellItemSelectionPane().getRowCount();
        for(int i = 0; i < columnC; i++) for(int j = 0; j < rowC; j++){
            if(i*rowC+j > view.getCellItemSelectionPane().getChildren().size()-1)return;
            Button btn = (Button) view.getCellItemSelectionPane().getChildren().get(i*rowC+j);
            final ItemType item = ItemType.getValueFromName(btn.getText().toUpperCase());
            btn.setOnMousePressed(mouseEvent -> {
                view.setAllItemBtnsDisable(false);
                view.setItemButtonDisabled(item,true);
                setHandlersForMapCells();
                gameMap.setMultipleItems(view.getSelectedPointList(), item);
                model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
            });

        }
    }

    private void handleEditRequiredLevelsBtn(ActionEvent e) {
        Dialog<ButtonType> chooseRequiredLvlsDialog = new Dialog<>();
        ListView<String> requiredLevelsListView = new ListView<>();
        requiredLevelsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (int i = 0; i < model.getCurrentIndex(); i++) {
            requiredLevelsListView.getItems().add(model.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME, i)+"");
        }
        for (String requiredLevelName : model.getCurrentRequiredLevels()) {
            int i = 0;
            for (String s : requiredLevelsListView.getItems()) {
                if (s.equals(requiredLevelName)) {
                    requiredLevelsListView.getSelectionModel().select(i);
                    break;
                }
                i++;
            }
        }
        chooseRequiredLvlsDialog.getDialogPane().setContent(requiredLevelsListView);
        chooseRequiredLvlsDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        chooseRequiredLvlsDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> o = chooseRequiredLvlsDialog.showAndWait();
        if (o.isPresent() && o.get() == ButtonType.OK) {
            List<Integer> requiredLevelNames = new ArrayList<>();
            for (int i = 0; i < requiredLevelsListView.getItems().size(); i++) {
                if (requiredLevelsListView.getSelectionModel().isSelected(i))
                    requiredLevelNames.add(model.getIdOfLevelWithName(requiredLevelsListView.getItems().get(i)));
            }
            model.changeCurrentLevel(LevelDataType.REQUIRED_LEVELS,requiredLevelNames);
        }

        view.getLevelEditorModule().getRequiredLevelsLView().getItems().clear();
        view.getLevelEditorModule().getRequiredLevelsLView().getItems().addAll(model.getCurrentRequiredLevels());
    }

    @Override
    public void update(Object o) {
        if(View.getCurrentSceneState() == SceneState.LEVEL_EDITOR)
        setEditorHandlers();
    }

}
