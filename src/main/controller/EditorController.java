package main.controller;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import main.model.Cell;
import main.model.GameMap;
import main.model.Level;
import main.model.Model;
import main.model.enums.CContent;
import main.model.enums.CFlag;
import main.model.enums.ItemType;
import main.model.statement.ComplexStatement;
import main.model.statement.SimpleStatement;
import main.utility.GameConstants;
import main.parser.JSONParser;
import main.utility.Point;
import main.utility.Util;
import main.view.SceneState;
import main.view.View;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class EditorController implements PropertyChangeListener {

    View view;
    Model model;
    CodeAreaController codeAreaController;

    private boolean actionEventFiring = true;

    //    GameMap map;
    public EditorController(View view, Model model, CodeAreaController codeAreaController){
        this.model = model;
        this.view = view;
        view.addPropertyChangeListener(this);
        this.codeAreaController = codeAreaController;
//        this.map = model.getCurrentLevel().getOriginalMap();

    }

    void setAllEditButtonsToDisable(boolean b) {
        view.getLevelEditorModule().getNewLevelBtn().setDisable(b);
        view.getLevelEditorModule().getOpenLevelBtn().setDisable(b);
        view.getLevelEditorModule().getDeleteLevelBtn().setDisable(b);
        view.getLevelEditorModule().getSaveLevelBtn().setDisable(b);
        view.getLevelEditorModule().getEditLvlBtn().setDisable(b);
        if(b){
            view.setAllCellButtonsDisabled(true);
            view.setCContentButtonDisabled(CContent.EMPTY,true);
            view.setCContentButtonDisabled(CContent.WALL,true);
        }
    }

    public void setEditorHandlers() {
//        CodeAreaController codeAreaController2 = new CodeAreaController(view,model);
        if(model.getCurrentLevel().hasAi())view.getLevelEditorModule().getHasAiValueLbl().setText(""+true);
//            codeAreaController.setAllHandlersForCodeArea(true);
        else view.getLevelEditorModule().getHasAiValueLbl().setText(""+false);
        setHandlersForMapCells();
        setHandlersForCellTypeButtons();
        //TODO: make this section prettier -> look to setHandlersForMapCells
        for(Point p : view.getSelectedPointList()){
            int k = p.getX();
            int h = p.getY();
            CContent content = model.getCurrentLevel().getOriginalMap().getContentAtXY(k, h);
//            view.setCContentButtonDisabled(content,true);
            //END OF TODO

            int cellId  = model.getCurrentLevel().getOriginalMap().getCellID(k,h);
            if(cellId !=-1)view.getLevelEditorModule().getCellIdValueLbl().setText(cellId+"");
            else view.getLevelEditorModule().getCellIdValueLbl().setText("NONE");
            changeEditorModuleDependingOnCellContent(view.getSelectedPointList());
        }

        view.getLevelEditorModule().getSaveLevelBtn().setOnAction(event -> {
            Level level =model.getCurrentLevel();
            if(level.getOriginalMap().findSpawn().getX()==-1){
                Alert alert = new Alert(Alert.AlertType.NONE,"You cannot save a level without a spawn!",ButtonType.OK);
                alert.setTitle("Alert!");
                alert.setHeaderText("");
                alert.showAndWait();
                return;
            }
            try {
                JSONParser.saveLevel(level);
                JSONParser.saveIndexAndRequiredLevels(model.getLevelListCopy());
                model.updateFinishedList();
                Alert alert = new Alert(Alert.AlertType.NONE,"Level was saved!",ButtonType.OK);
                alert.setTitle("Success!");
                alert.setHeaderText("");
                alert.showAndWait();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        view.getLevelEditorModule().getEditTutorialTextBtn().setOnAction(evt -> {
            int index = Integer.parseInt(view.getLevelEditorModule().getTutorialNumberValueLbl().getText());
            Dialog<ButtonType> editTutorialDialog = new Dialog<>();
            String text = "";
            if(model.getCurrentLevel().getTutorialEntryList().size() > 0) text = model.getCurrentLevel().getTutorialEntryList().get(index-1);
            TextArea tutorialTextArea = new TextArea(text);
            tutorialTextArea.setWrapText(true);
            tutorialTextArea.setPromptText("Type your tutorial text here!");
            editTutorialDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            editTutorialDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            editTutorialDialog.getDialogPane().setContent(tutorialTextArea);
            Platform.runLater(tutorialTextArea::requestFocus);
            Optional<ButtonType> o  = editTutorialDialog.showAndWait();
            tutorialTextArea.requestFocus();
            if(o.isPresent()&& o.get() == ButtonType.OK){
                String tutorialText = tutorialTextArea.getText();
                model.getCurrentLevel().setTutorialLine(index-1,tutorialText);
                //TODO: evaluate necessity
//                setEditorHandlers();
            }
        });

        view.getLevelEditorModule().getDeleteTutorialTextBtn().setOnAction(evt -> {
            Optional<ButtonType> o  = new Alert(Alert.AlertType.NONE,"Do you really want to delete this tutorial text?",ButtonType.OK, ButtonType.CANCEL).showAndWait();
            if(o.isPresent()&& o.get() == ButtonType.OK){
                int index = Integer.parseInt(view.getLevelEditorModule().getTutorialNumberValueLbl().getText());
                model.getCurrentLevel().deleteTutorialLine(index-1);
                if(model.getCurrentLevel().getTutorialEntryList().size()<=2){
                    if(model.getCurrentLevel().getTutorialEntryList().size()==1){
                        view.getLevelEditorModule().getDeleteTutorialTextBtn().setDisable(true);
                        view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(true);
                        view.getLevelEditorModule().getNextTutorialTextBtn().setDisable(true);
                    }
                    else if(index == 1)view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(true);
                    else view.getLevelEditorModule().getNextTutorialTextBtn().setDisable(true);
                }
                //TODO: evaluate necessity
//                setEditorHandlers();
            }
        });

        view.getLevelEditorModule().getNewTutorialTextBtn().setOnAction(evt -> {
            int index = Integer.parseInt(view.getLevelEditorModule().getTutorialNumberValueLbl().getText());
            if(view.getLevelEditorModule().getTutorialTextArea().getText().equals("")){
                new Alert(Alert.AlertType.NONE,"Please enter some text into the current TextArea!",ButtonType.OK).showAndWait();
                return;
            }
            view.getLevelEditorModule().getTutorialNumberValueLbl().setText(""+(index+1));
//            if(index == model.getCurrentLevel().getTutorialEntryList().size()){
            view.getLevelEditorModule().getTutorialTextArea().setText("");

            model.getCurrentLevel().addTutorialLine(index,"");
//            }
//            else{
//                view.getLevelEditorModule().getTutorialTextArea().setText(model.getCurrentLevel().getTutorialEntryList().get(index));
//                if(index+1 == model.getCurrentLevel().getTutorialEntryList().size()){
//                    view.getLevelEditorModule().getNextOrNewTutorialTextBtn().setText("New Entry");
//                }
//            }
            view.getLevelEditorModule().getDeleteTutorialTextBtn().setDisable(false);
            view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(false);
            //TODO: evaluate necessity
//            setEditorHandlers();

        });

        view.getLevelEditorModule().getPrevTutorialTextBtn().setOnAction(evt -> {
            int index = Integer.parseInt(view.getLevelEditorModule().getTutorialNumberValueLbl().getText());
            view.getLevelEditorModule().getTutorialTextArea().setText(model.getCurrentLevel().getTutorialEntryList().get(index-2));
            view.getLevelEditorModule().getTutorialNumberValueLbl().setText((index-1)+"");
            view.getLevelEditorModule().getNextTutorialTextBtn().setDisable(false);
            if(index-1 == 1){
                view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(true);
            }
            //TODO: evaluate necessity
//            setEditorHandlers();

        });
        view.getLevelEditorModule().getNextTutorialTextBtn().setOnAction(evt -> {
            int index = Integer.parseInt(view.getLevelEditorModule().getTutorialNumberValueLbl().getText());
            view.getLevelEditorModule().getTutorialTextArea().setText(model.getCurrentLevel().getTutorialEntryList().get(index));
            view.getLevelEditorModule().getTutorialNumberValueLbl().setText((index+1)+"");
            view.getLevelEditorModule().getPrevTutorialTextBtn().setDisable(false);
            if(index+1 == model.getCurrentLevel().getTutorialEntryList().size()){
                view.getLevelEditorModule().getNextTutorialTextBtn().setDisable(true);
            }
            //TODO: evaluate necessity
//            setEditorHandlers();

        });

        view.getLevelEditorModule().getOpenLevelBtn().setOnAction(event -> {

            try {
                ChoiceDialog<String> levelsToOpenDialog = new ChoiceDialog<>();
                File folder = new File(Paths.get(GameConstants.LEVEL_ROOT_PATH).toString());
                String[] levelNames = JSONParser.getAllLevelNames();
                // wrong! //TODO: read data.json
//                File[] listOfFiles = folder.listFiles();
//                assert listOfFiles != null;
                int index = 0;
                for (int i = 0; i < levelNames.length; i++) {
                    levelsToOpenDialog.getItems().add(null);
                }
                for (String levelName : levelNames) {

                    if (index == -1) throw new IllegalStateException("This Level shouldnt exist!");
                    index++;
                    levelsToOpenDialog.getItems().set(model.getIndexOfLevelInList(levelName), levelName);
//                    int diff = levelsToOpenDialog.getItems().size() - index;
//                    if(diff > 0)   levelsToOpenDialog.getItems().set(index,levelName);
//                    else {
//                        for(int i = 0; i < -diff; i++){
//                            levelsToOpenDialog.getItems().add("");
//                        }
//                        levelsToOpenDialog.getItems().add(levelName);
//                    }
                }
                levelsToOpenDialog.setSelectedItem(model.getCurrentLevel().getName());
                Optional<String> s = levelsToOpenDialog.showAndWait();
                if (s.isPresent()) {
//                    Level level = JSONParser.parseLevelJSON(s.get()+".json");
////                    model.addLevel(level);
                    model.selectLevel(s.get());
                    //TODO: model.getCurrentLevel().addListener(view);
//                    model.setCurrentIndexLevel(1);
//                    view.notify(Event.MAP_CHANGED);
//                    view.notify(Event.LEVEL_CHANGED);
                }
//                setEditorHandlers();
            }
            catch (Exception e){
                //TODO delete
                throw e;
            }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
        });
        view.getLevelEditorModule().getDeleteLevelBtn().setOnAction(event -> {

            Alert deleteAlert = new Alert(Alert.AlertType.NONE, "You are about to permanently delete this level!", ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> btnType =deleteAlert.showAndWait();
            if(btnType.isPresent() && btnType.get() == ButtonType.OK){
                File file = new File(Paths.get(GameConstants.LEVEL_ROOT_PATH).toString()+"/"+model.getCurrentLevel().getName()+".json");
                if(file.delete()){
                    try {
                        model.removeLevel(model.getCurrentLevel());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    //TODO: model.getCurrentLevel().addListener(view);
//                    view.notify(Event.LEVEL_CHANGED);
//                    setEditorHandlers();
                }
            }
        });
        view.getLevelEditorModule().getResetLevelScoresBtn().setOnAction(event -> {

            Alert deleteAlert = new Alert(Alert.AlertType.NONE, "You are about to reset the score for this level!", ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> btnType =deleteAlert.showAndWait();
            if(btnType.isPresent() && btnType.get() == ButtonType.OK){
                try {
                    Alert infoAlert;
                    if(JSONParser.resetScoreForLevel(model.getCurrentLevel().getName())){
                        infoAlert = new Alert(Alert.AlertType.NONE, "Score was reset!", ButtonType.OK);

                    }
                    else infoAlert = new Alert(Alert.AlertType.NONE, "No score found!", ButtonType.OK);
                        infoAlert.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        view.getLevelEditorModule().getReloadLevelBtn().setOnAction(event -> {

            Alert deleteAlert = new Alert(Alert.AlertType.NONE, "You are about to reload this level! Unsaved changes will be discarded!", ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> btnType =deleteAlert.showAndWait();
            if(btnType.isPresent() && btnType.get() == ButtonType.OK){
                if(!Util.arrayContains(JSONParser.getAllLevelNames(),model.getCurrentLevel().getName())){
                    new Alert(Alert.AlertType.NONE, "This Level has not been saved yet!", ButtonType.OK).showAndWait();
                    return;
                }
                try {
                    model.replaceLevel(model.getCurrentLevel().getName(),JSONParser.parseLevelJSON(model.getCurrentLevel().getName()+".json"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
//                setEditorHandlers();
            }
        });
        view.getLevelEditorModule().getNewLevelBtn().setOnAction(event -> {

            Dialog<ButtonType> newLevelDialog = new Dialog<>();
            TextField nameTField = new TextField();
            TextField heightTField = new TextField();
            TextField widthTField = new TextField();
            heightTField.setText(3+"");
            widthTField.setText(3+"");
            nameTField.textProperty().addListener((observableValue, s, t1) -> {

                if(model.getLevelWithName(t1)!=null){
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
            if(o.isPresent()){
                if (o.get() == ButtonType.OK) {
                    //TODO: unify and correctify!
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
                            map[i][j] = new Cell(CContent.WALL);
                        }
                    }
//                    ComplexStatement aiCode = new ComplexStatement();
//                    aiCode.addSubStatement(new SimpleStatement());
                    ComplexStatement complexStatement = new ComplexStatement();
                    if(hasAiCheckBox.isSelected())complexStatement.addSubStatement(new SimpleStatement());
                    Integer[] turnsToStars = new Integer[2];
                    turnsToStars[0] = 0;
                    turnsToStars[1] = 0;
                    Integer[] locToStars = new Integer[2];
                    locToStars[0] = 0;
                    locToStars[1] = 0;
                    int index = model.getCurrentLevel().getIndex()+1;
                    for(int i = index; i <model.getAmountOfLevels();i++){
                        model.getLevelWithIndex(i).setIndex(i+1);
                    }
                    model.addLevel(new Level(nameTField.getText(),map,complexStatement,turnsToStars,locToStars,new String[0],3,index,false,null)); //TODO!

                    model.selectLevel(nameTField.getText());
                    //TODO: model.getCurrentLevel().addListener(view);
                    view.getLevelEditorModule().getHasAiValueLbl().setText(""+hasAiCheckBox.isSelected());
//                    codeAreaController2.setAllHandlersForCodeArea(true);
//                    setEditorHandlers();
                    try {
                        JSONParser.saveLevel(model.getCurrentLevel());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        view.getLevelEditorModule().getEditRequiredLevelsBtn().setOnAction(e -> {
            Dialog<ButtonType> chooseRequiredLvlsDialog = new Dialog<>();
            ListView<String> requiredLevelsListView = new ListView<>();
            requiredLevelsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            String[] levelNameList = JSONParser.getAllLevelNames();
            for(int i = 0; i < levelNameList.length; i++){
                if(model.getIndexOfLevelInList(levelNameList[i])<model.getCurrentLevel().getIndex())
                requiredLevelsListView.getItems().add(levelNameList[i]);
            }
            for(String requiredLevelName : model.getCurrentLevel().getRequiredLevels()){
                int i = 0;
                for(String s : requiredLevelsListView.getItems()){
                    if(s.equals(requiredLevelName)){
                        requiredLevelsListView.getSelectionModel().select(i);
                        break;
                    }
                    i++;
                }
            }
            chooseRequiredLvlsDialog.getDialogPane().setContent(requiredLevelsListView);
            chooseRequiredLvlsDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            chooseRequiredLvlsDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            Optional<ButtonType> o  = chooseRequiredLvlsDialog.showAndWait();
            if(o.isPresent()&& o.get() == ButtonType.OK){
                List<String> requiredLevelNames = new ArrayList<>();
                for(int i = 0; i < requiredLevelsListView.getItems().size();i++){
                    if(requiredLevelsListView.getSelectionModel().isSelected(i))requiredLevelNames.add(requiredLevelsListView.getItems().get(i));
                }
                model.getCurrentLevel().setRequiredLevels(requiredLevelNames);
                try {
                    JSONParser.updateUnlocks(model.getCurrentLevel());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }

            view.getLevelEditorModule().getRequiredLevelsLView().getItems().clear();
            view.getLevelEditorModule().getRequiredLevelsLView().getItems().addAll(model.getCurrentLevel().getRequiredLevels());

        });
        view.getLevelEditorModule().getMoveIndexDownBtn().setOnAction(actionEvent -> {
            int currentLevelIndex = model.getCurrentLevel().getIndex();
            if(currentLevelIndex == 0){
//                setEditorHandlers();
                return;
            }
            try {
                model.moveCurrentLevelDown();

//                String s = "";
//                for(String lName : model.getCurrentLevel().getRequiredLevels()){
//                    if(model.getLevelWithName(lName).getIndex() > model.getCurrentLevel().getIndex()){
//                        s = lName;
//                    }
//                }
//                model.getCurrentLevel().getRequiredLevels().remove(s);
            } catch (IOException e) {
                e.printStackTrace();
            }
//            setEditorHandlers();
        });
        view.getLevelEditorModule().getMoveIndexUpBtn().setOnAction(actionEvent -> {
            int currentLevelIndex = model.getCurrentLevel().getIndex();
            if(currentLevelIndex == model.getAmountOfLevels()-1){
//                setEditorHandlers();
                return;
            }
            try {
                model.moveCurrentLevelUp();
            } catch (IOException e) {
                e.printStackTrace();
            }
//            setEditorHandlers();
        });
        view.getLevelEditorModule().getEditLvlBtn().setOnAction(event -> {
            Dialog<ButtonType> changeLvlDialog = new Dialog<>();
//            TextField nameTField = new TextField(model.getCurrentLevel().getName());
//            TextField heightTField = new TextField(model.getCurrentLevel().getOriginalMap().getBoundY()+"");
//            TextField widthTField = new TextField(model.getCurrentLevel().getOriginalMap().getBoundX()+"");
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
            heightSlider.setValue(model.getCurrentLevel().getOriginalMap().getBoundY());
            widthSlider.setValue(model.getCurrentLevel().getOriginalMap().getBoundX());
            maxKnightsSlider.setValue(model.getCurrentLevel().getMaxKnights());
            TextField loc2StarsTField = new TextField(model.getCurrentLevel().getLocToStars()[0]+"");
            TextField loc3StarsTField = new TextField(model.getCurrentLevel().getLocToStars()[1]+"");
            TextField turns2StarsTField = new TextField(model.getCurrentLevel().getTurnsToStars()[0]+"");
            TextField turns3StarsTField = new TextField(model.getCurrentLevel().getTurnsToStars()[1]+"");

//            nameTField.textProperty().addListener((observableValue, s, t1) -> {
//                if(t1.matches("(\\d+.*)+")){
//                    nameTField.setText(s);
//                }
//            });
            addChangeListenerForIntTFields(/*heightTField,widthTField,*/loc2StarsTField,loc3StarsTField,turns2StarsTField,turns3StarsTField);
            CheckBox hasAiCheckBox = new CheckBox();
            CheckBox isTutorialCheckBox = new CheckBox();
            //new Label("Name:"),nameTField,
            HBox hBox = new HBox(sizeVBox,maxKnightsVBox,new VBox(new Label("*** max. LoC: "),new Label("** max. LoC: ")),new VBox(loc3StarsTField,loc2StarsTField),new VBox(new Label("*** max. Turns: "),new Label("** max. Turns: ")),new VBox(turns3StarsTField,turns2StarsTField),
                    new Label("Has AI"),hasAiCheckBox,new Label("Is Tutorial"),isTutorialCheckBox);
            hasAiCheckBox.setSelected(model.getCurrentLevel().getAIBehaviour().getStatementListSize() > 0);
            isTutorialCheckBox.setSelected(model.getCurrentLevel().isTutorial());
            // allow only those levels to be a tutorial whose predecessor is a tutorial to avoid weird required level dependencies!
            if(model.getCurrentLevel().getIndex()!= 0 && !model.getLevelWithIndex(model.getCurrentLevel().getIndex()-1).isTutorial())isTutorialCheckBox.setDisable(true);
            changeLvlDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            changeLvlDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            changeLvlDialog.getDialogPane().setContent(hBox);

            Optional<ButtonType> o  = changeLvlDialog.showAndWait();
            if(o.isPresent()&& o.get() == ButtonType.OK){
                int width = (int)widthSlider.getValue();
                int height = (int)heightSlider.getValue();
                width = width > GameConstants.MAX_LEVEL_SIZE ? GameConstants.MAX_LEVEL_SIZE : width < GameConstants.MIN_LEVEL_SIZE ? GameConstants.MIN_LEVEL_SIZE:width;
                height = height > GameConstants.MAX_LEVEL_SIZE ? GameConstants.MAX_LEVEL_SIZE : height < GameConstants.MIN_LEVEL_SIZE ? GameConstants.MIN_LEVEL_SIZE:height;
                model.getCurrentLevel().changeHeight(height);
                model.getCurrentLevel().changeWidth(width);
                model.getCurrentLevel().setMaxKnights((int)maxKnightsSlider.getValue());
                model.getCurrentLevel().changeLocToStars(new Integer[]{Integer.valueOf(loc2StarsTField.getText()),Integer.valueOf(loc3StarsTField.getText())});
                model.getCurrentLevel().changeTurnsToStars(new Integer[]{Integer.valueOf(turns2StarsTField.getText()),Integer.valueOf(turns3StarsTField.getText())});
                // Level name should not be changed via this menu as it makes a lot of changes necessary
                //                model.getCurrentLevel().setName(nameTField.getText());
                model.getCurrentLevel().setIsTutorial(isTutorialCheckBox.isSelected());
//                view.getLevelEditorModule().getHasAiValueLbl().setText(""+hasAiCheckBox.isSelected());
                if(!hasAiCheckBox.isSelected())model.getCurrentLevel().setAiBehaviour(new ComplexStatement());
                else if(!model.getCurrentLevel().hasAi()){
                    ComplexStatement complexStatement = new ComplexStatement();
                    complexStatement.addSubStatement(new SimpleStatement());
                    model.getCurrentLevel().setAiBehaviour(complexStatement);
                }
//                view.notify(Event.LEVEL_CHANGED);
//                view.setAiCodeArea(new CodeArea(model.getCurrentLevel().getAIBehaviour()));
//                view.getAICodeArea().draw();
                setEditorHandlers();
            }
        });

    view.getLevelEditorModule().getChangeLvlNameBtn().setOnAction(event -> {
        Dialog<ButtonType> changeLvlNameDialog = new Dialog<>();
        TextField nameTField = new TextField(model.getCurrentLevel().getName());

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
            String oldName = model.getCurrentLevel().getName();
            try {
                if(JSONParser.changeLevelName(oldName,nameTField.getText())){
                    new Alert(Alert.AlertType.NONE, "The level has been renamed!", ButtonType.OK).showAndWait();
                    model.getCurrentLevel().setName(nameTField.getText());
                }
                else
                    new Alert(Alert.AlertType.NONE, "The level could not be renamed!", ButtonType.OK).showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
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
                final CContent content = model.getCurrentLevel().getOriginalMap().getContentAtXY(x,y);
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

                    if(!testIfEmptyIsAllowed(model.getCurrentLevel().getOriginalMap(),k,h))view.setCContentButtonDisabled(CContent.EMPTY,true);
                    if(!testIfNormalContentIsAllowed(model.getCurrentLevel().getOriginalMap(),k,h))view.setAllCellButtonsDisabled(true);

                    int cellId  = model.getCurrentLevel().getOriginalMap().getCellID(k,h);
                    if(cellId !=-1)view.getLevelEditorModule().getCellIdValueLbl().setText(cellId+"");
                    else view.getLevelEditorModule().getCellIdValueLbl().setText("NONE");

                    changeEditorModuleDependingOnCellContent(selectedList);
                });}}
                view.getLevelEditorModule().getRemoveLinkedCellBtn().setOnAction(actionEvent -> {
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
                    model.getCurrentLevel().getOriginalMap().removeCellLinkedId(view.getSelectedPointList().get(0).getX(),view.getSelectedPointList().get(0).getY(),id);
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
//                            int old_id = model.getCurrentLevel().getOriginalMap()[view.getSelectedColumn()][view.getSelectedRow()].getCellId();
                            if(model.getCurrentLevel().getOriginalMap().testIfIdIsUnique(id)){
                                model.getCurrentLevel().getOriginalMap().setCellId(view.getSelectedPointList().get(0).getX(),view.getSelectedPointList().get(0).getY(),id);
                                view.getLevelEditorModule().getCellIdValueLbl().setText(id!=-1 ? ""+id : "NONE");
                            }
                            else new Alert(Alert.AlertType.NONE,"Id "+id+" already in use",ButtonType.OK).showAndWait();
                        }
                    }
                });
                view.getLevelEditorModule().getIsInvertedCBox().setOnAction(evt -> {
                    GameMap map = model.getCurrentLevel().getOriginalMap();
                    if(view.getSelectedPointList().size()!=1)return;
                    int column = view.getSelectedPointList().get(0).getX();
                    int  row = view.getSelectedPointList().get(0).getY();
                    map.setFlag(column, row, CFlag.INVERTED,view.getLevelEditorModule().getIsInvertedCBox().isSelected());
                });
                view.getLevelEditorModule().getIsTurnedCBox().setOnAction(evt -> {
                    GameMap map = model.getCurrentLevel().getOriginalMap();
                    if(view.getSelectedPointList().size()!=1)return;
                    int column = view.getSelectedPointList().get(0).getX();
                    int  row = view.getSelectedPointList().get(0).getY();
                    map.setFlag(column, row, CFlag.TURNED,view.getLevelEditorModule().getIsTurnedCBox().isSelected());
                });
                view.getLevelEditorModule().getAddLinkedCellBtn().setOnAction(actionEvent -> {
                    if( view.getSelectedPointList().size() > 1) return;
                    GameMap map = model.getCurrentLevel().getOriginalMap();
                    ChoiceDialog<Integer> idsDialog = new ChoiceDialog<>();
                    for(int x2 = 0; x2 < map.getBoundX();x2++){
                        for(int y2 = 0; y2 < map.getBoundY();y2++){
                            CContent content1 = map.getContentAtXY(x2,y2);
                            int cellId = map.getCellID(x2,y2);
                            if(cellId!=-1&&content1==CContent.PRESSURE_PLATE&&!view.getLinkedCellsListView().getItems().contains(cellId))idsDialog.getItems().add(cellId);
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
                            boolean cellHasLinkedId = map.cellHasLinkedCellId(column,row,s.get());
                            if (!cellHasLinkedId) {
                                map.addLinkedCellId(column,row,s.get());
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
        boolean leftSideNothing = h == 0 || currentMap.getContentAtXY(k,h-1)==CContent.EMPTY;
        boolean rightSideNothing = h == currentMap.getBoundY()-1 || currentMap.getContentAtXY(k,h+1)==CContent.EMPTY;
        boolean topSideNothing = k == 0 || currentMap.getContentAtXY(k-1,h)==CContent.EMPTY;
        boolean bottomSideNothing = k == currentMap.getBoundX()-1 || currentMap.getContentAtXY(k+1,h)==CContent.EMPTY;

        return !leftSideNothing && !rightSideNothing && !bottomSideNothing && !topSideNothing;
    }

    private boolean testIfEmptyIsAllowed(GameMap originalMap, int k, int h) {
        boolean leftSideWallOrNothing = h == 0 || originalMap.getContentAtXY(k,h-1)==CContent.WALL||originalMap.getContentAtXY(k,h-1)==CContent.EMPTY;
        boolean rightSideWallOrNothing = h == originalMap.getBoundY()-1 || originalMap.getContentAtXY(k,h+1)==CContent.WALL ||originalMap.getContentAtXY(k,h+1)==CContent.EMPTY;
        boolean topSideWallOrNothing = k == 0 || originalMap.getContentAtXY(k-1,h)==CContent.WALL|| originalMap.getContentAtXY(k-1,h)==CContent.EMPTY;
        boolean bottomSideWallOrNothing = k == originalMap.getBoundX()-1 || originalMap.getContentAtXY(k+1,h)==CContent.WALL || originalMap.getContentAtXY(k+1,h)==CContent.EMPTY;

        return leftSideWallOrNothing && rightSideWallOrNothing && bottomSideWallOrNothing && topSideWallOrNothing;
    }

    private void changeEditorModuleDependingOnCellContent(List<Point> points) {
        if(view.getSelectedPointList().size() == 0){
            view.setAllCellButtonsDisabled(true);
//            view.setAllItemTypeButtonActive();
            return;
        }
        view.setAllCellButtonsDisabled(false);
        GameMap map = model.getCurrentLevel().getOriginalMap();
        boolean traversable = true;
        boolean noItem = true;
        CContent content = null;
        for(Point p : points){
            int x = p.getX();
            int y = p.getY();
            if(!testIfNormalContentIsAllowed(model.getCurrentLevel().getOriginalMap(),x,y)){
                view.setAllCellButtonsDisabled(true);
                view.setCContentButtonDisabled(CContent.EMPTY,false);
                view.setCContentButtonDisabled(CContent.WALL,false);
            }
            if(!testIfEmptyIsAllowed(model.getCurrentLevel().getOriginalMap(),x,y))view.setCContentButtonDisabled(CContent.EMPTY,true);
            content = map.getContentAtXY(x, y);
            traversable = traversable && content.isTraversable();
        }

        if(!traversable) view.setAllItemBtnsDisable(true);
//        else view.setAllItemTypeButtonInActive();
        ItemType item = map.getItem(points.get(0).getX(),points.get(0).getY());
        if(points.size() == 1 && item != ItemType.NONE){
            view.setItemButtonDisabled(item,true);
//            view.setItemButtonDisabled(ItemType.NONE,false);
        }
        else view.setItemButtonDisabled(ItemType.NONE,true);
        //TODO!!
        if(view.getSelectedPointList().size() > 1){
            view.setCContentButtonDisabled(CContent.SPAWN,true);
            view.getLevelEditorModule().deactivateCellDetails();
            view.setItemButtonDisabled(ItemType.NONE,false);
            return;
        }
        final int x = points.get(0).getX();
        final int y = points.get(0).getY();
        view.setCContentButtonDisabled(content, true);
        if(content == CContent.TRAP){
            view.getLevelEditorModule().activateTrapChoicebox();

            ChoiceBox<String> choiceBox = view.getLevelEditorModule().getTrapChoiceBox();
            choiceBox.getItems().clear();
            choiceBox.getItems().add(0,"Unarmed");
            choiceBox.getItems().add(1,CFlag.PREPARING.getDisplayName());
            choiceBox.getItems().add(2,CFlag.ARMED.getDisplayName());
//            if(map.cellHasFlag(x,y,CFlag.UNARMED)) choiceBox.getSelectionModel().select(0);
            if(map.cellHasFlag(x,y,CFlag.PREPARING)) choiceBox.getSelectionModel().select(1);
            else if(map.cellHasFlag(x,y,CFlag.ARMED)) choiceBox.getSelectionModel().select(2);
            else {
                choiceBox.getSelectionModel().select(0);
            }
            choiceBox.setOnHidden(evt -> {
                // .setOnHidden fires twice for some reason??! this is a workaround!
                if(toggleActionEventFiring())return;
                CFlag flag = CFlag.getValueFrom(choiceBox.getValue().toUpperCase());
//                map.setFlag(x, y, CFlag.UNARMED,false);
                map.setFlag(x, y, CFlag.PREPARING,false);
                map.setFlag(x, y, CFlag.ARMED,false );
                if(flag != null)map.setFlag(x, y, flag,true );
                if(flag == CFlag.ARMED)map.setItem(x, y, ItemType.NONE);
            });
        }
        else if(content == CContent.PRESSURE_PLATE||content == CContent.ENEMY_SPAWN){
            view.getLevelEditorModule().activateCellIDHBox(content == CContent.PRESSURE_PLATE);
            view.getLevelEditorModule().getIsInvertedCBox().setSelected(map.cellHasFlag(x,y,CFlag.INVERTED));
        }

        else if(content == CContent.GATE){
            view.getLevelEditorModule().activateLinkedCellBtns();
            view.getLevelEditorModule().getIsTurnedCBox().setSelected(map.cellHasFlag(x,y,CFlag.TURNED));


            view.getLevelEditorModule().getIsInvertedCBox().setSelected(map.cellHasFlag(x,y,CFlag.INVERTED));

            ListView<Integer> listView =  view.getLevelEditorModule().getLinkedCellListView();
            listView.getItems().clear();
            //TODO: only gate has linked cells?
            int linkedCellListSize = map.getLinkedCellListSize(x,y);
            for(int l = 0; l < linkedCellListSize; l++){
                listView.getItems().add(l,map.getLinkedCellId(x,y,l));
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


//        if(content == CContent.ENEMY_SPAWN || content == CContent.SPAWN||content == CContent.EXIT){
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
        GameMap gameMap = model.getCurrentLevel().getOriginalMap();
        for(int i = 0; i < columnC; i++) for(int j = 0; j < rowC; j++){
            if(i*rowC+j > view.getCellTypeSelectionPane().getChildren().size()-1)return;
            Button btn = (Button) view.getCellTypeSelectionPane().getChildren().get(i*rowC+j);
            final CContent content = CContent.getValueFromName(btn.getText().toUpperCase().replaceAll(" ", "_"));
            btn.setOnAction(mouseEvent -> {
                for(Point p : view.getSelectedPointList()){
                int column = p.getX();
                int row = p.getY();
                gameMap.clearFlags(column,row);
                //TODO: darf es wirklich nur einen geben?
                if(content == null) throw new IllegalStateException("Content: " + btn.getText().toUpperCase().replaceAll(" ", "_") + " doesnt exist!");
                if(content == CContent.SPAWN){
                    for(int x = 0; x < gameMap.getBoundX();x++){
                        for(int y = 0; y < gameMap.getBoundY();y++){
                            if(gameMap.getContentAtXY(x,y) == CContent.SPAWN){
                                gameMap.setContent(x,y,CContent.PATH);
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
                    view.setCContentButtonDisabled(CContent.EMPTY,true);
                }
                changeEditorModuleDependingOnCellContent(view.getSelectedPointList());}
                gameMap.setContent(view.getSelectedPointList(),content);
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

                    CContent content = model.getCurrentLevel().getOriginalMap().getContentAtXY(column, row);
                    view.setAllItemBtnsDisable(false);
                    view.setItemButtonDisabled(item,true);
                    setHandlersForMapCells();
                }
                model.getCurrentLevel().getOriginalMap().setItem(view.getSelectedPointList(), item);
            });

        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals("map")&&view.getCurrentSceneState()== SceneState.LEVEL_EDITOR&&!codeAreaController.isGameRunning())
            setEditorHandlers();
    }
}