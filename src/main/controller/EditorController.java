package main.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import main.model.LevelChange;
import main.model.LevelDataType;
import main.model.gamemap.Cell;
import main.model.gamemap.GameMap;
import main.model.Level;
import main.model.Model;
import main.model.enums.CellContent;
import main.model.enums.CFlag;
import main.model.enums.ItemType;
import main.model.statement.ComplexStatement;
import main.model.statement.SimpleStatement;
import main.utility.GameConstants;
import main.parser.JSONParser;
import main.utility.Point;
import main.utility.SimpleEventListener;
import main.utility.Util;
import main.view.SceneState;
import main.view.View;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class EditorController implements SimpleEventListener {

    private View view;

    private boolean actionEventFiring = true;

    public EditorController(View view){
        this.view = view;
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
        if((boolean)Model.getDataFromCurrentLevel(LevelDataType.HAS_AI))view.getLevelEditorModule().getHasAiValueLbl().setText(""+true);
        else view.getLevelEditorModule().getHasAiValueLbl().setText(""+false);
        setHandlersForMapCells();
        setHandlersForCellTypeButtons();
        //TODO: make this section prettier -> look to setHandlersForMapCells
        for(Point p : view.getSelectedPointList()){
            int k = p.getX();
            int h = p.getY();
            GameMap gameMapClone = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
            //END OF TODO

            int cellId  = gameMapClone.getCellID(k,h);
            if(cellId !=-1)view.getLevelEditorModule().getCellIdValueLbl().setText(cellId+"");
            else view.getLevelEditorModule().getCellIdValueLbl().setText("NONE");
            changeEditorModuleDependingOnCellContent(view.getSelectedPointList());
        }


        view.getLevelEditorModule().getEditTutorialTextBtn().setOnAction(evt -> {
            Dialog<ButtonType> editTutorialDialog = new Dialog<>();
            String text = "";

            List<String> tutLines = (List<String>)Model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
            if(Model.getCurrentTutorialSize() > 0) text = tutLines.get(Model.getCurrentTutorialMessageIndex());
            TextArea tutorialTextArea = new TextArea();
            tutorialTextArea.setMaxSize(GameConstants.TEXTFIELD_WIDTH, GameConstants.TEXTFIELD_WIDTH/2.0);
            tutorialTextArea.setFont(GameConstants.BIG_FONT);
            tutorialTextArea.setWrapText(true);
            tutorialTextArea.setText(text);
            editTutorialDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            editTutorialDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            editTutorialDialog.getDialogPane().setContent(tutorialTextArea);
            editTutorialDialog.getDialogPane().setMaxWidth(GameConstants.TEXTFIELD_WIDTH*1.05);
            Platform.runLater(tutorialTextArea::requestFocus);
            tutorialTextArea.textProperty().addListener((observableValue, s, t1) ->{
                // Cant have more than MAX_TUTORIAL_LINES lines of tutorial
                if(Util.getRowCount(tutorialTextArea) > GameConstants.MAX_TUTORIAL_LINES)tutorialTextArea.setText(s);
            });

            Optional<ButtonType> o  = editTutorialDialog.showAndWait();
            tutorialTextArea.requestFocus();
            if(o.isPresent()&& o.get() == ButtonType.OK){
                tutLines = (List<String>)Model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
                String tutorialText = tutorialTextArea.getText();
                tutLines.set(Model.getCurrentTutorialMessageIndex(),tutorialText.trim());
                Model.changeCurrentLevel(LevelDataType.TUTORIAL_LINES,tutLines);
            }
        });

        view.getLevelEditorModule().getDeleteTutorialTextBtn().setOnAction(evt -> {
            Optional<ButtonType> o  = new Alert(Alert.AlertType.NONE,"Do you really want to delete this tutorial text?",ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if(o.isPresent()&& o.get() == ButtonType.OK){
                List<String> tutLines = (List<String>)Model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
                tutLines.remove(Model.getCurrentTutorialMessageIndex());
                Model.decreaseTutorialMessageIndex();
                Model.changeCurrentLevel(LevelDataType.TUTORIAL_LINES,tutLines);
            }
        });

        view.getLevelEditorModule().getNewTutorialTextBtn().setOnAction(evt -> {
            if(view.getLevelEditorModule().getTutorialTextArea().getText().equals("")){
                new Alert(Alert.AlertType.NONE,"Please enter some text into the current TextArea!",ButtonType.OK).showAndWait();
                return;
            }

            List<String> tutLines = (List<String>)Model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
            tutLines.add(Model.getCurrentTutorialMessageIndex()+1,"");
            Model.increaseTutorialMessageIndex();
            Model.changeCurrentLevel(LevelDataType.TUTORIAL_LINES,tutLines);
            view.getLevelEditorModule().getDeleteTutorialTextBtn().setDisable(false);
            view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(false);

        });

        view.getLevelEditorModule().getPrevTutorialTextBtn().setOnAction(evt -> {
            Model.prevTutorialMessage();
            int index = Model.getCurrentTutorialMessageIndex();
            view.updateTutorialMessage();
            view.getLevelEditorModule().getNextTutorialTextBtn().setDisable(false);
            if(index == 0){
                view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(true);
            }
        });
        view.getLevelEditorModule().getNextTutorialTextBtn().setOnAction(evt -> {
            Model.nextTutorialMessage();
            int index = Model.getCurrentTutorialMessageIndex();
            view.updateTutorialMessage();
            view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(false);
            if(index+1 == ((List<String>)Model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES)).size()){
                view.getLevelEditorModule().getNextTutorialTextBtn().setDisable(true);
            }
        });

        view.getLevelEditorModule().getSaveLevelBtn().setOnAction(event -> {
            saveChanges(Model.getAndConfirmCurrentChanges());
        });
        view.getLevelEditorModule().getOpenLevelBtn().setOnAction(event -> {
            if(Model.currentLevelHasChanged())
                showSavingDialog();

            ChoiceDialog<String> levelsToOpenDialog = new ChoiceDialog<>();
            for (int i = 0; i < Model.getAmountOfLevels(); i++) {
                levelsToOpenDialog.getItems().add(Model.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME,i)+"");
            }
            String levelName = (String)Model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME);
            levelsToOpenDialog.setSelectedItem(levelName);
            Optional<String> s = levelsToOpenDialog.showAndWait();
            s.ifPresent(Model::selectLevel);
        });
        view.getLevelEditorModule().getDeleteLevelBtn().setOnAction(event -> {

            Alert deleteAlert = new Alert(Alert.AlertType.NONE, "You are about to permanently delete this level!", ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> btnType =deleteAlert.showAndWait();
            if(btnType.isPresent() && btnType.get() == ButtonType.OK){
                String levelName = (String)Model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME);
                File file = new File(Paths.get(GameConstants.LEVEL_ROOT_PATH).toString()+"/"+levelName+".json");
                file.delete();
                Model.removeCurrentLevel();
            }
            if(Model.getAmountOfLevels()==1)view.getLevelEditorModule().getDeleteLevelBtn().setDisable(true);
        });
        view.getLevelEditorModule().getResetLevelScoresBtn().setOnAction(event -> {

            Alert deleteAlert = new Alert(Alert.AlertType.NONE, "You are about to reset the score for this level!", ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> btnType =deleteAlert.showAndWait();
            if(btnType.isPresent() && btnType.get() == ButtonType.OK){
                Alert infoAlert;
                Model.resetScoreOfCurrentLevel();
                infoAlert = new Alert(Alert.AlertType.NONE, "Score was reset!", ButtonType.OK);
                infoAlert.showAndWait();

            }
        });

        view.getLevelEditorModule().getReloadLevelBtn().setOnAction(event -> {

            Alert deleteAlert = new Alert(Alert.AlertType.NONE, "You are about to reload this level! Unsaved changes will be discarded!", ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> btnType =deleteAlert.showAndWait();
            if(btnType.isPresent() && btnType.get() == ButtonType.OK){
                    Model.reloadCurrentLevel();
            }
        });
        view.getLevelEditorModule().getNewLevelBtn().setOnAction(event -> {
            if(Model.currentLevelHasChanged())showSavingDialog();
            Dialog<ButtonType> newLevelDialog = new Dialog<>();
            TextField nameTField = new TextField();
            TextField heightTField = new TextField();
            TextField widthTField = new TextField();
            heightTField.setText(3+"");
            widthTField.setText(3+"");
            nameTField.textProperty().addListener((observableValue, s, t1) -> {

                if(Model.hasLevelWithName(t1)){
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
                Model.addLevelAtCurrentPos(new Level(nameTField.getText(),map,complexStatement,turnsToStars,locToStars,new String[0],1,false,null),true); //TODO!
                // There ain't no spawn when creating a new Level!
                view.getLevelEditorModule().getSaveLevelBtn().setDisable(true);
                view.getLevelEditorModule().getHasAiValueLbl().setText(""+hasAiCheckBox.isSelected());
                view.getLevelEditorModule().getDeleteLevelBtn().setDisable(false);
            }

        });

        view.getLevelEditorModule().getCopyLevelBtn().setOnAction(event -> {
            if(Model.currentLevelHasChanged())showSavingDialog();
            String name = Model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME)+"_Copy";
            if(Model.hasLevelWithName(name)){
                new Alert(Alert.AlertType.INFORMATION,"This level already exists!").showAndWait();
                return;
            }
            GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);

            int height = gameMap.getBoundY();
            int width = gameMap.getBoundX();
            Cell[][] map = new Cell[width][height];
            for(int i = 0; i < width;i++){
                for(int j = 0; j < height;j++){
                    map[i][j] = gameMap.getCellAtXYClone(i, j);
                }
            }
            ComplexStatement complexStatement = (ComplexStatement)Model.getDataFromCurrentLevel(LevelDataType.AI_CODE);
            Integer[] turnsToStars = (Integer[]) Model.getDataFromCurrentLevel(LevelDataType.TURNS_TO_STARS);
            Integer[] locToStars = (Integer[]) Model.getDataFromCurrentLevel(LevelDataType.LOC_TO_STARS);
            List<String> requiredLevelsList = (List<String>) Model.getDataFromCurrentLevel(LevelDataType.REQUIRED_LEVELS);
            String[] requiredLevelsArray = new String[requiredLevelsList.size()];
            int i = 0;
            for(String s : requiredLevelsList){
                requiredLevelsArray[i] = s;
                i++;
            }
            List<String> tutorialEntries = (List<String>) Model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
            int maxKnights = (int) Model.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS);
            boolean isTutorial = (boolean) Model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL);
            boolean hasAI = (boolean) Model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
            Model.addLevelAtCurrentPos(new Level(name,map,complexStatement,turnsToStars,locToStars,requiredLevelsArray,maxKnights,isTutorial,tutorialEntries),true);

            view.getLevelEditorModule().getDeleteLevelBtn().setDisable(false);
            if(((GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA)).findSpawn().getX()==-1){
                view.getLevelEditorModule().getSaveLevelBtn().setDisable(true);
            }
            else view.getLevelEditorModule().getSaveLevelBtn().setDisable(false);
            view.getLevelEditorModule().getHasAiValueLbl().setText(""+hasAI);
        });


        view.getLevelEditorModule().getEditRequiredLevelsBtn().setOnAction(this::handleEditRequiredLevelsBtn);
        view.getLevelEditorModule().getMoveIndexDownBtn().setOnAction(actionEvent -> {
            int currentLevelIndex = Model.getCurrentIndex();
            if(currentLevelIndex == 0){
                return;
            }
            if((boolean)Model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,currentLevelIndex-1) &&
                    !(boolean) Model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL))
                new Alert(Alert.AlertType.NONE,"Can't move Level down, as Challenge Levels must not come in between Tutorial Levels",ButtonType.OK).showAndWait();
            else Model.changeCurrentLevel(LevelDataType.LEVEL_INDEX,currentLevelIndex-1);
        });
        view.getLevelEditorModule().getMoveIndexUpBtn().setOnAction(actionEvent -> {
            int currentLevelIndex = Model.getCurrentIndex();
            if(currentLevelIndex == Model.getAmountOfLevels()-1){
                return;
            }

            if(Model.getNextTutorialIndex()==-1 && (boolean) Model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL))
                new Alert(Alert.AlertType.NONE,"Can't move Level up, as Tutorial Levels must not come after a Challenge Level",ButtonType.OK).showAndWait();
            else Model.changeCurrentLevel(LevelDataType.LEVEL_INDEX,currentLevelIndex+1);
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
            maxKnightsSlider.valueProperty().addListener((observableValue, number, t1) -> maxKnightsLbl.setText(t1.intValue()+""));
            Util.applyValueFormat(heightLbl,widthLbl,maxKnightsLbl);
            widthSlider.valueProperty().addListener((observableValue, number, t1) -> widthLbl.setText(t1.intValue()+""));
            VBox sizeVBox = new VBox(new HBox(new Label("Width: "),widthLbl),widthSlider,new HBox(new Label("Height: "),heightLbl),heightSlider);
            sizeVBox.setAlignment(Pos.CENTER);
            VBox maxKnightsVBox = new VBox(new HBox(new Label("Max Knights: "),maxKnightsLbl),maxKnightsSlider);
            formatSlider(heightSlider,GameConstants.MIN_LEVEL_SIZE,GameConstants.MAX_LEVEL_SIZE);
            formatSlider(widthSlider,GameConstants.MIN_LEVEL_SIZE,GameConstants.MAX_LEVEL_SIZE);
            formatSlider(maxKnightsSlider,1,4);
            GameMap currentMapClone = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
            heightSlider.setValue(currentMapClone.getBoundY());
            widthSlider.setValue(currentMapClone.getBoundX());
            maxKnightsSlider.setValue((int)Model.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS));
            Integer[] locToStars = (Integer[]) Model.getDataFromCurrentLevel(LevelDataType.LOC_TO_STARS);
            Integer[] turnsToStars = (Integer[]) Model.getDataFromCurrentLevel(LevelDataType.TURNS_TO_STARS);
            TextField loc2StarsTField = new TextField(locToStars[0]+"");
            TextField loc3StarsTField = new TextField(locToStars[1]+"");
            TextField turns2StarsTField = new TextField(turnsToStars[0]+"");
            TextField turns3StarsTField = new TextField(turnsToStars[1]+"");

            addChangeListenerForIntTFields(loc2StarsTField,loc3StarsTField,turns2StarsTField,turns3StarsTField);
            CheckBox hasAiCheckBox = new CheckBox();
            CheckBox isTutorialCheckBox = new CheckBox();
            HBox hBox = new HBox(sizeVBox,maxKnightsVBox,new VBox(new Label("*** max. LoC: "),new Label("** max. LoC: ")),new VBox(loc3StarsTField,loc2StarsTField),new VBox(new Label("*** max. Turns: "),new Label("** max. Turns: ")),new VBox(turns3StarsTField,turns2StarsTField),
                    new Label("Has AI"),hasAiCheckBox,new Label("Is Tutorial"),isTutorialCheckBox);
            boolean currentLevelHasAI = (boolean) Model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
            hasAiCheckBox.setSelected(currentLevelHasAI);
            boolean currentLevelIsTut = (boolean)Model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL);
            isTutorialCheckBox.setSelected(currentLevelIsTut);
            boolean prevLevelIsTut = Model.getCurrentIndex() ==0 || (boolean)Model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,Model.getCurrentIndex()-1);
            boolean nextLevelIsTut = Model.getCurrentIndex() != Model.getAmountOfLevels()-1 && (boolean)Model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,Model.getCurrentIndex() +1);
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
                //TODO!!!!
                currentMapClone.changeHeight(height);
                currentMapClone.changeWidth(width);
                Model.changeCurrentLevel(LevelDataType.MAP_DATA,currentMapClone);
                Model.changeCurrentLevel(LevelDataType.MAX_KNIGHTS,(int)maxKnightsSlider.getValue());
                Model.changeCurrentLevel(LevelDataType.LOC_TO_STARS,new Integer[]{Integer.valueOf(loc2StarsTField.getText()),Integer.valueOf(loc3StarsTField.getText())});
                Model.changeCurrentLevel(LevelDataType.TURNS_TO_STARS,new Integer[]{Integer.valueOf(turns2StarsTField.getText()),Integer.valueOf(turns3StarsTField.getText())});
                Model.changeCurrentLevel(LevelDataType.IS_TUTORIAL,isTutorialCheckBox.isSelected());
                if(!hasAiCheckBox.isSelected())Model.changeCurrentLevel(LevelDataType.AI_CODE,new ComplexStatement());
                else if(!(boolean)Model.getDataFromCurrentLevel(LevelDataType.HAS_AI)){
                    ComplexStatement complexStatement = new ComplexStatement();
                    complexStatement.addSubStatement(new SimpleStatement());
                    Model.changeCurrentLevel(LevelDataType.AI_CODE,complexStatement);
                }
//                view.inform(Event.LEVEL_CHANGED);
//                view.setAiCodeArea(new CodeArea(Model.getCurrentLevel().getAIBehaviourCopy()));
//                view.getAICodeArea().draw();
                setEditorHandlers();
            }
        });

    view.getLevelEditorModule().getChangeLvlNameBtn().setOnAction(event -> {
        Dialog<ButtonType> changeLvlNameDialog = new Dialog<>();
        TextField nameTField = new TextField((String)Model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME));

            nameTField.textProperty().addListener((observableValue, s, t1) -> {
                if(t1.matches("(\\d+.*)+")||!t1.matches("[A-Z|a-z|ä|Ä|ö|Ö|ü|Ü|ß]*\\d*")){
                    nameTField.setText(s);
                }
            });
        HBox hBox = new HBox(new Label("Name:"),nameTField);
        changeLvlNameDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        changeLvlNameDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        changeLvlNameDialog.getDialogPane().setContent(hBox);

        Optional<ButtonType> o  = changeLvlNameDialog.showAndWait();
        if(o.isPresent()&& o.get() == ButtonType.OK){
            Model.changeCurrentLevel(LevelDataType.LEVEL_NAME,nameTField.getText());
        }
    });
}

    private void saveChanges(Map<LevelDataType, LevelChange> changes) {
        //TODO: make it so this button cannot be clicked in this case!

        try {
//            JSONParser.saveLevel(level);
            if(Model.isCurrentLevelNew()){
                JSONParser.saveCurrentLevel( );
                Platform.runLater(()->Model.resetLevelNew());
            }
            else JSONParser.saveLevelChanges(changes, (String)Model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME));
//            JSONParser.saveIndexAndRequiredLevels(Model.getLevelListCopy());
            Model.updateUnlockedLevelsList(true);
            Alert alert = new Alert(Alert.AlertType.NONE,"Level was saved!",ButtonType.OK);
            alert.setTitle("Success!");
            alert.setHeaderText("");
            alert.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showSavingDialog() {
        Alert savingAlert = new Alert(Alert.AlertType.NONE, "This level has unsaved changes! Do you want to save them?", ButtonType.YES,ButtonType.NO);
        Optional<ButtonType> btnType =savingAlert.showAndWait();
        if(btnType.isPresent() && btnType.get() == ButtonType.YES){
            if(((GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA)).findSpawn().getX()==-1){
                Model.reloadCurrentLevel();
                Alert alert = new Alert(Alert.AlertType.NONE,"You cannot save a level without a spawn!",ButtonType.OK);
                alert.setTitle("Alert!");
                alert.setHeaderText("");
                alert.showAndWait();
            }
            else saveChanges(Model.getAndConfirmCurrentChanges());
        }
        else if(btnType.isPresent()){
            Model.reloadCurrentLevel();
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
            textField.setMaxWidth(50); //TODO: shouldnt be here!
            textField.textProperty().addListener((observableValue, s, t1) -> {
            if(!t1.matches("\\d+")){
                if(s.matches("\\d+"))textField.setText(s);
                textField.setText(""+0);
            }
            if(t1.matches("0(\\d+)")){
                textField.setText(textField.getText().substring(1));
            }
            if(t1.length() > 3)textField.setText(s);
//            if(Integer.valueOf(textField.getText())>GameConstants.MAX_LEVEL_SIZE)
//                textField.setText(""+GameConstants.MAX_LEVEL_SIZE);

//            if(Integer.valueOf(textField.getText())<GameConstants.MIN_LEVEL_SIZE)
//                textField.setText(""+GameConstants.MIN_LEVEL_SIZE);
        });}
    }

    void setHandlersForMapCells() {
        int columnC = view.getActualMapGPane().getColumnCount();
        int rowC = view.getActualMapGPane().getRowCount();
        for(int x = 0; x < columnC; x++){
            for(int y = 0; y < rowC; y++){
                //TODO: 2-dim array?
                StackPane stackPane = (StackPane) view.getActualMapGPane().getChildren().get(x*rowC+y);

                final GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                final CellContent content = gameMap.getContentAtXY(x,y);
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
//                        selectedList.addAll();
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
                    final GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
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
                    view.getLinkedCellsListView().setMaxHeight(size <= 3 ? size * GameConstants.TEXTFIELD_HEIGHT : 3 * GameConstants.TEXTFIELD_HEIGHT);
                    gameMap.removeCellLinkedId(view.getSelectedPointList().get(0).getX(),view.getSelectedPointList().get(0).getY(),id);
                    if(view.getLinkedCellsListView().getItems().size()==0){
                        view.getLinkedCellsListView().setVisible(false);
                        view.getLevelEditorModule().getRemoveLinkedCellBtn().setDisable(true);
                    }
//                    setEditorHandlers();
                });
                view.getLinkedCellsListView().setOnMouseClicked(event -> {
                    if(view.getLinkedCellsListView().getSelectionModel().getSelectedItem()!=null){
                        view.getLevelEditorModule().getRemoveLinkedCellBtn().setVisible(true);
                    }
                    else view.getLevelEditorModule().getRemoveLinkedCellBtn().setVisible(false);
//                    setEditorHandlers();
                });
                view.getLevelEditorModule().getChangeCellIdBtn().setOnAction(event->{
                    final GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                    if(view.getSelectedPointList().size() > 1) return;
                    Dialog<ButtonType> newLevelDialog = new Dialog<>();
                    TextField idTField = new TextField();
                    addChangeListenerForIntTFields(idTField);
                    newLevelDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                    newLevelDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
                    newLevelDialog.getDialogPane().setContent(new HBox(new Label("Id:"),idTField));

                    Optional<ButtonType> o  = newLevelDialog.showAndWait();
                    if(o.isPresent()){
                        if (o.get() == ButtonType.OK) {
                            //TODO: unify and correctify!
                            int id = -1;
                            if(!idTField.getText().equals("")) id = Integer.valueOf(idTField.getText());
//                            int old_id = Model.getCurrentLevel().getOriginalMapCopy()[view.getSelectedColumn()][view.getSelectedRow()].getCellId();
                            if(gameMap.testIfIdIsUnique(id)){

                                gameMap.setCellId(view.getSelectedPointList().get(0).getX(),view.getSelectedPointList().get(0).getY(),id);
                                view.getLevelEditorModule().getCellIdValueLbl().setText(id!=-1 ? ""+id : "NONE");
                            }
                            else new Alert(Alert.AlertType.NONE,"Id "+id+" already in use",ButtonType.OK).showAndWait();
                        }
                    }
                });
                view.getLevelEditorModule().getIsInvertedCBox().setOnAction(evt -> {
                    GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                    if(view.getSelectedPointList().size()!=1)return;
                    int column = view.getSelectedPointList().get(0).getX();
                    int  row = view.getSelectedPointList().get(0).getY();
                    gameMap.setFlag(column, row, CFlag.INVERTED,view.getLevelEditorModule().getIsInvertedCBox().isSelected());
                    Model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
                });
                view.getLevelEditorModule().getIsTurnedCBox().setOnAction(evt -> {
                    GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                    if(view.getSelectedPointList().size()!=1)return;
                    int column = view.getSelectedPointList().get(0).getX();
                    int  row = view.getSelectedPointList().get(0).getY();
                    gameMap.setFlag(column, row, CFlag.TURNED,view.getLevelEditorModule().getIsTurnedCBox().isSelected());
                    Model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
                });
                view.getLevelEditorModule().getAddLinkedCellBtn().setOnAction(actionEvent -> {
                    if( view.getSelectedPointList().size() > 1) return;
                    GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
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
                                Model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
                                //TODO: duplicate code in view.getLevelEditorModule().getRemoveLinkedCellBtn().setOnAction(actionEvent -> {
                                int size = view.getLinkedCellsListView().getItems().size();
                                view.getLinkedCellsListView().setMaxHeight(size <= 3 ? size * GameConstants.TEXTFIELD_HEIGHT : 3 * GameConstants.TEXTFIELD_HEIGHT);
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
        GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        boolean traversable = true;
        boolean noItem = true;
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
//        else view.setAllItemTypeButtonInActive();
        ItemType item = gameMap.getItem(points.get(0).getX(),points.get(0).getY());
        if(points.size() == 1 && item != ItemType.NONE){
            view.setItemButtonDisabled(item,true);
//            view.setItemButtonDisabled(ItemType.NONE,false);
        }
        else view.setItemButtonDisabled(ItemType.NONE,true);
        //TODO!!
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
            choiceBox.getItems().add(1,CFlag.PREPARING.getDisplayName());
            choiceBox.getItems().add(2,CFlag.ARMED.getDisplayName());
//            if(map.cellHasFlag(x,y,CFlag.UNARMED)) choiceBox.getSelectionModel().select(0);
            if(gameMap.cellHasFlag(x,y,CFlag.PREPARING)) choiceBox.getSelectionModel().select(1);
            else if(gameMap.cellHasFlag(x,y,CFlag.ARMED)) choiceBox.getSelectionModel().select(2);
            else {
                choiceBox.getSelectionModel().select(0);
            }
            choiceBox.setOnHidden(evt -> {
                // .setOnHidden fires twice for some reason??! this is a workaround!
                if(toggleActionEventFiring())return;
                CFlag flag = CFlag.getValueFrom(choiceBox.getValue().toUpperCase());
//                map.setFlag(x, y, CFlag.UNARMED,false);
                gameMap.setFlag(x, y, CFlag.PREPARING,false);
                gameMap.setFlag(x, y, CFlag.ARMED,false );
                if(flag != null)gameMap.setFlag(x, y, flag,true );
                if(flag == CFlag.ARMED)gameMap.setItem(x, y, ItemType.NONE);
                Model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
            });
        }
        else if(content == CellContent.PRESSURE_PLATE||content == CellContent.ENEMY_SPAWN){
            view.getLevelEditorModule().activateCellIDHBox(content == CellContent.PRESSURE_PLATE);
            view.getLevelEditorModule().getIsInvertedCBox().setSelected(gameMap.cellHasFlag(x,y,CFlag.INVERTED));
        }

        else if(content == CellContent.GATE){
            view.getLevelEditorModule().activateLinkedCellBtns();
            view.getLevelEditorModule().getIsTurnedCBox().setSelected(gameMap.cellHasFlag(x,y,CFlag.TURNED));


            view.getLevelEditorModule().getIsInvertedCBox().setSelected(gameMap.cellHasFlag(x,y,CFlag.INVERTED));

            ListView<Integer> listView =  view.getLevelEditorModule().getLinkedCellListView();
            listView.getItems().clear();
            //TODO: only gate has linked cells?
            int linkedCellListSize = gameMap.getLinkedCellListSize(x,y);
            for(int l = 0; l < linkedCellListSize; l++){
                listView.getItems().add(l,gameMap.getLinkedCellId(x,y,l));
            }
            if(linkedCellListSize>0){
                listView.setMaxHeight(linkedCellListSize*GameConstants.TEXTFIELD_HEIGHT);
                listView.setMaxWidth(GameConstants.TEXTFIELD_WIDTH/4);
                view.getLinkedCellsListView().setVisible(true);
            }else {
                view.getLinkedCellsListView().setVisible(false);
                view.getLevelEditorModule().getRemoveLinkedCellBtn().setDisable(true);
            }
        }else view.getLevelEditorModule().deactivateCellDetails();


//        if(content == CellContent.ENEMY_SPAWN || content == CellContent.SPAWN||content == CellContent.EXIT){
//            view.getLevelEditorModule().addTurnable();
//            view.getLevelEditorModule().getIsTurnedCBox().setSelected(map.cellHasFlag(x,y,CFlag.TURNED));
//            view.getLevelEditorModule().getIsTurnedCBox().setOnAction(evt -> {
//                map.setFlag(x, y, CFlag.TURNED,view.getLevelEditorModule().getIsTurnedCBox().isSelected());
//            });
//        }
    }

    private boolean toggleActionEventFiring() {
        return actionEventFiring = !actionEventFiring;
    }

    //TODO: do the same as above
    private void setHandlersForCellTypeButtons() {
        int columnC = view.getCellTypeSelectionPane().getColumnCount();
        int rowC = view.getCellTypeSelectionPane().getRowCount();
        GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        for(int i = 0; i < columnC; i++) for(int j = 0; j < rowC; j++){
            if(i*rowC+j > view.getCellTypeSelectionPane().getChildren().size()-1)return;
            Button btn = (Button) view.getCellTypeSelectionPane().getChildren().get(i*rowC+j);
            final CellContent content = CellContent.getValueFromName(btn.getText().toUpperCase().replaceAll(" ", "_"));
            btn.setOnAction(mouseEvent -> {
                for(Point p : view.getSelectedPointList()){
                int column = p.getX();
                int row = p.getY();
                gameMap.clearFlags(column,row);
                //TODO: darf es wirklich nur einen geben?
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
                Model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
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
                for(Point p : view.getSelectedPointList()) {
                    int column = p.getX();
                    int row = p.getY();

                    CellContent content = gameMap.getContentAtXY(column, row);
                    view.setAllItemBtnsDisable(false);
                    view.setItemButtonDisabled(item,true);
                    setHandlersForMapCells();
                }
                gameMap.setMultipleItems(view.getSelectedPointList(), item);
                Model.changeCurrentLevel(LevelDataType.MAP_DATA,gameMap);
            });

        }
    }

    private void handleEditRequiredLevelsBtn(ActionEvent e) {
        Dialog<ButtonType> chooseRequiredLvlsDialog = new Dialog<>();
        ListView<String> requiredLevelsListView = new ListView<>();
        requiredLevelsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (int i = 0; i < Model.getCurrentIndex(); i++) {
            requiredLevelsListView.getItems().add(Model.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME, i)+"");
        }
        for (String requiredLevelName : (List<String>) Model.getDataFromCurrentLevel(LevelDataType.REQUIRED_LEVELS)) {
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
            List<String> requiredLevelNames = new ArrayList<>();
            for (int i = 0; i < requiredLevelsListView.getItems().size(); i++) {
                if (requiredLevelsListView.getSelectionModel().isSelected(i))
                    requiredLevelNames.add(requiredLevelsListView.getItems().get(i));
            }
            Model.changeCurrentLevel(LevelDataType.REQUIRED_LEVELS,requiredLevelNames);
        }

        view.getLevelEditorModule().getRequiredLevelsLView().getItems().clear();
        view.getLevelEditorModule().getRequiredLevelsLView().getItems().addAll((List<String>) Model.getDataFromCurrentLevel(LevelDataType.REQUIRED_LEVELS));
    }

    @Override
    public void update(Object o) {
        if(view.getCurrentSceneState() == SceneState.LEVEL_EDITOR)
        setEditorHandlers();
    }

}
