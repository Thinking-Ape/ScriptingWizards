package main.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import main.view.LevelOverviewPane;
import main.view.SceneState;
import main.view.View;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static main.model.GameConstants.CHALLENGE_COURSE_NAME;

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
        view.getLevelEditorModule().getDeleteLevelBtn().setDisable(b||!ModelInformer.canDelete());
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
            if(k >= gameMapClone.getBoundX() || h >= gameMapClone.getBoundY())continue;
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
            if (model.currentLevelHasChanged())
                showSavingDialog();

            Dialog<ButtonType> openDialog = new Dialog<>();
            ButtonType btnTypeCancel = new ButtonType("Cancel",ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType btnTypeAccept = new ButtonType("Open",ButtonBar.ButtonData.OK_DONE);
            ChoiceBox<String> courseCBox = new ChoiceBox<>();
            courseCBox.getItems().addAll(model.getOrderedCourseNames().stream().filter(c -> model.getActualAmountOfLevelsInCourse(c) > 0).collect(Collectors.toList()));
            courseCBox.getSelectionModel().select(model.getCurrentCourseName());
            ChoiceBox<String> levelCBox = new ChoiceBox<>();
            levelCBox.getItems().addAll(model.getAllLevelNamesOfCourse(courseCBox.getSelectionModel().getSelectedItem()));
            if(levelCBox.getItems().size() > 0)levelCBox.getSelectionModel().select(model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME)+"");
            Label courseLbl = new Label("Course: ");
            Label levelLbl = new Label("Level: ");
            openDialog.getDialogPane().getButtonTypes().add(btnTypeCancel);
            openDialog.getDialogPane().getButtonTypes().add(btnTypeAccept);
            Image icon = new Image(GameConstants.BG_DARK_TILE_PATH);
            if(levelCBox.getItems().size() > 0)icon = View.getIconFromMap((GameMap)model.getDataFromLevelWithId(LevelDataType.MAP_DATA,model.getIdOfLevelWithName(levelCBox.getSelectionModel().getSelectedItem())));
            ImageView littleMap = new ImageView(icon);
            littleMap.setFitHeight(GameConstants.BUTTON_SIZE);
            littleMap.setFitWidth(GameConstants.BUTTON_SIZE);
            VBox openVBox = new VBox(new HBox(courseLbl,courseCBox),new HBox(levelLbl,levelCBox),new Separator(Orientation.HORIZONTAL),littleMap);
            openVBox.setSpacing(5);
            openVBox.setAlignment(Pos.CENTER);
            openDialog.getDialogPane().setContent(openVBox);

            courseCBox.getSelectionModel().selectedItemProperty().addListener((observableValue, stringSingleSelectionModel, t1) -> {
                if(t1 == null || stringSingleSelectionModel == null) return;
                if(t1.equals(stringSingleSelectionModel))return;
                levelCBox.getItems().clear();
                levelCBox.getItems().addAll(model.getAllLevelNamesOfCourse(courseCBox.getSelectionModel().getSelectedItem()));
                if(levelCBox.getItems().size() > 0){
                    levelCBox.getSelectionModel().select(0);
                    int id = model.getIdOfLevelWithName(levelCBox.getSelectionModel().getSelectedItem());
                    if(model.levelExists(id))
                        littleMap.setImage(View.getIconFromMap((GameMap)model.getDataFromLevelWithId(LevelDataType.MAP_DATA,id)));
                    else levelCBox.getItems().remove(levelCBox.getSelectionModel().getSelectedIndex() );
                }
            });
            levelCBox.getSelectionModel().selectedItemProperty().addListener((observableValue, stringSingleSelectionModel, t1) -> {
                if(t1 == null || stringSingleSelectionModel == null) return;
                if(t1.equals(stringSingleSelectionModel))return;
                littleMap.setImage(View.getIconFromMap((GameMap)model.getDataFromLevelWithId(LevelDataType.MAP_DATA,model.getIdOfLevelWithName(levelCBox.getSelectionModel().getSelectedItem()))));
            });

            Optional<ButtonType> buttonType = openDialog.showAndWait();
            if(buttonType.isPresent()&&buttonType.get().getButtonData() == ButtonBar.ButtonData.OK_DONE){
                if(levelCBox.getSelectionModel().isEmpty())return;
                model.selectLevel(levelCBox.getSelectionModel().getSelectedItem());
                Platform.runLater(() -> {
                    view.highlightInMap(List.of(new Point(0,0)));
                });
            }
//            ChoiceDialog<String> levelsToOpenDialog = new ChoiceDialog<>();
//            //TODO: better solution
//            for(String courseName : model.getAllCourseNames())
//            for (int i : model.getOrderedIdsFromCourse(courseName)) {
//                levelsToOpenDialog.getItems().add(model.getDataFromLevelWithId(LevelDataType.LEVEL_NAME,i)+"");
//            }
//            String levelName = (String)model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME);
//            levelsToOpenDialog.setSelectedItem(levelName);
//            Optional<String> s = levelsToOpenDialog.showAndWait();
//            s.ifPresent(s1 -> {
//                model.selectLevel(s1);
//                Platform.runLater(() -> {
//                    view.highlightInMap(List.of(new Point(0,0)));
//                });
//            });
        });
        view.getLevelEditorModule().getDeleteLevelBtn().setOnAction(event -> {

            Alert deleteAlert = new Alert(Alert.AlertType.NONE, "You are about to permanently delete this level!", ButtonType.OK,ButtonType.CANCEL);
            Optional<ButtonType> btnType =deleteAlert.showAndWait();
            if(btnType.isPresent() && btnType.get() == ButtonType.OK){
                String levelName = (String)model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME);
                view.getChallengeOverviewPane().removeCurrentLevel();
                if(view.getChallengeOverviewPane().getLevelListView().getItems().size() == 0)view.getStartScreen().getChallengesBtn().setDisable(true);
                if(!model.getCurrentCourseName().equals(CHALLENGE_COURSE_NAME))view.findTutorialLevelOverviewPane(model.getCurrentCourseName()).removeCurrentLevel();
                try {
                    JSONParser.deleteLevel(levelName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                model.removeCurrentLevel();
                view.getCourseOverviewPane().updateLevelAmounts();
            }
            if(!ModelInformer.canDelete())view.getLevelEditorModule().getDeleteLevelBtn().setDisable(true);

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
                view.updateEntryOfCourse(model.getCurrentCourseName());
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
                int id = model.createUniqueLevelId();
                // Automatically makes this level a tutorial if the current Level is a tutorial. This makes it impossible
                // to create non-Tutorial levels between tutorials (which would end the tutorial prematurely)

                String courseName = ""+ model.getDataFromCurrentLevel(LevelDataType.COURSE);
                Level newLevel = new Level(nameTField.getText(),map,complexStatement,turnsToStars,locToStars,new ArrayList<>(),1,new ArrayList<>(),id,1);
                model.addLevelAtCurrentPos(newLevel,true);
                view.getCourseOverviewPane().updateLevelAmounts();
                // There is no spawn when creating a new Level!
                view.getLevelEditorModule().getSaveLevelBtn().setDisable(true);
                view.getLevelEditorModule().getHasAiValueLbl().setText(""+hasAiCheckBox.isSelected());
                view.getLevelEditorModule().getDeleteLevelBtn().setDisable(!ModelInformer.canDelete());
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
            int amountOfReruns = (int)model.getDataFromCurrentLevel(LevelDataType.AMOUNT_OF_RERUNS);
            List<String> tutorialEntries = (List<String>) model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
            int maxKnights = (int) model.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS);

            String courseName = ""+ model.getDataFromCurrentLevel(LevelDataType.COURSE);
            boolean hasAI = (boolean) model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
            int id =model.createUniqueLevelId();
            model.addLevelAtCurrentPos(new Level(name,map,complexStatement,turnsToStars,locToStars,requiredLevelsList,maxKnights,tutorialEntries,id,amountOfReruns),true);
            view.getCourseOverviewPane().updateLevelAmounts();

            view.getLevelEditorModule().getDeleteLevelBtn().setDisable(!ModelInformer.canDelete());
            if(((GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA)).findSpawn().getX()==-1){
                view.getLevelEditorModule().getSaveLevelBtn().setDisable(true);
            }
            else view.getLevelEditorModule().getSaveLevelBtn().setDisable(false);
            view.getLevelEditorModule().getHasAiValueLbl().setText(""+hasAI);
            Platform.runLater(() -> view.highlightInMap(List.of(new Point(0,0))));
        });


        view.getLevelEditorModule().getEditCoursesBtn().setOnAction(this::handleEditCoursesBtn);
        view.getLevelEditorModule().getExportCoursesBtn().setOnAction(this::handleExportCoursesBtn);
//        view.getLevelEditorModule().getEditRequiredLevelsBtn().setOnAction(this::handleEditRequiredLevelsBtn);
        view.getLevelEditorModule().getMoveIndexDownBtn().setOnAction(actionEvent -> {
            int currentLevelIndex = model.getCurrentIndex();
            if(currentLevelIndex == 0){
                return;
            }

            String courseName = ""+ model.getDataFromLevelWithId(LevelDataType.COURSE,model.getPrevId());
            boolean isTutorial = !courseName.equals(CHALLENGE_COURSE_NAME);
            String cCourseName = ""+ model.getDataFromCurrentLevel(LevelDataType.COURSE);
            boolean cIsTutorial = !cCourseName.equals(CHALLENGE_COURSE_NAME);
            if(isTutorial &&
                    !cIsTutorial)
                new Alert(Alert.AlertType.NONE,"Can't move Level down, as Challenge Levels are not allowed to stand in between Tutorial Levels",ButtonType.OK).showAndWait();
            else model.changeCurrentLevel(LevelDataType.LEVEL_INDEX,currentLevelIndex-1);
            view.getLevelEditorModule().getMoveIndexUpBtn().setDisable(false);
        });
        view.getLevelEditorModule().getMoveIndexUpBtn().setOnAction(actionEvent -> {
            int currentLevelIndex = model.getCurrentIndex();
            if(currentLevelIndex >= model.getAmountOfLevelsInCurrentCourse()-1){
                return;
            }

            String courseName = ""+ model.getDataFromCurrentLevel(LevelDataType.COURSE);
            boolean isTutorial = !courseName.equals(CHALLENGE_COURSE_NAME);
//            if(model.getNextTutorialIndex()==-1 && isTutorial)
//                new Alert(Alert.AlertType.NONE,"Can't move Level up, as Tutorial Levels are not allowed to stand after a Challenge Level",ButtonType.OK).showAndWait();
            model.changeCurrentLevel(LevelDataType.LEVEL_INDEX,currentLevelIndex+1);
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
            sizeVBox.setAlignment(Pos.TOP_CENTER);
            VBox sliderVBox = new VBox(new HBox(new Label("Optimal Knights: "),maxKnightsLbl),maxKnightsSlider,new HBox(new Label("Amount of Plays: "),amountOfPlaysLbl),amountOfPlaysSlider);
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
            ChoiceBox<String> courseNameCBox = new ChoiceBox<>();
            HBox threeStarHBox = new HBox(new Label("*** max. Turns: "),turns3StarsTField,new Label("*** max. LoC: "),loc3StarsTField);
            HBox twoStarHBox = new HBox(new Label("** max. Turns: "),turns2StarsTField,new Label("** max. LoC: "),loc2StarsTField);
            HBox aiCourseHBox = new HBox(new Label("Has AI"),hasAiCheckBox,new Label(),new Label("Course: "),courseNameCBox);
            aiCourseHBox.setSpacing(GameConstants.SMALL_FONT_SIZE);
            twoStarHBox.setMinWidth(threeStarHBox.getLayoutBounds().getWidth());
            aiCourseHBox.setMinWidth(threeStarHBox.getLayoutBounds().getWidth());
            threeStarHBox.setAlignment(Pos.TOP_CENTER);
            twoStarHBox.setAlignment(Pos.TOP_CENTER);
            aiCourseHBox.setAlignment(Pos.TOP_CENTER);
            VBox starsAiCourseVBox = new VBox(threeStarHBox,twoStarHBox,aiCourseHBox);
            starsAiCourseVBox.setSpacing(GameConstants.SMALL_FONT_SIZE);
            HBox hBox = new HBox(sizeVBox,sliderVBox,starsAiCourseVBox);
            hBox.setAlignment(Pos.TOP_CENTER);
            boolean currentLevelHasAI = (boolean) model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
            hasAiCheckBox.setSelected(currentLevelHasAI);

            String courseName = ""+ model.getDataFromCurrentLevel(LevelDataType.COURSE);
            courseNameCBox.getItems().addAll(model.getOrderedCourseNames());
            courseNameCBox.getSelectionModel().select(courseName);
            // allow only those levels to be a tutorial whose predecessor is a tutorial to avoid weird required level dependencies!
//            if(!prevLevelIsTut||nextLevelIsTut)isTutorialCheckBox.setDisable(true);
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
                model.changeCurrentLevel(LevelDataType.MAX_KNIGHTS,(int)maxKnightsSlider.getValue());
                model.changeCurrentLevel(LevelDataType.AMOUNT_OF_RERUNS,(int)amountOfPlaysSlider.getValue());
                model.changeCurrentLevel(LevelDataType.LOC_TO_STARS,new Integer[]{Integer.valueOf(loc2StarsTField.getText()),Integer.valueOf(loc3StarsTField.getText())});
                model.changeCurrentLevel(LevelDataType.TURNS_TO_STARS,new Integer[]{Integer.valueOf(turns2StarsTField.getText()),Integer.valueOf(turns3StarsTField.getText())});
                model.changeCurrentLevel(LevelDataType.COURSE,courseNameCBox.getValue());
                if(!hasAiCheckBox.isSelected())model.changeCurrentLevel(LevelDataType.AI_CODE,new ComplexStatement());
                else if(!(boolean)model.getDataFromCurrentLevel(LevelDataType.HAS_AI)){
                    ComplexStatement complexStatement = new ComplexStatement();
                    complexStatement.addSubStatement(new SimpleStatement());
                    model.changeCurrentLevel(LevelDataType.AI_CODE,complexStatement);
                }
                model.changeCurrentLevel(LevelDataType.MAP_DATA,currentMapClone);
                setEditorHandlers();
            }
        });

    view.getLevelEditorModule().getChangeLvlNameBtn().setOnAction(event -> {
        Dialog<ButtonType> changeLvlNameDialog = new Dialog<>();
        TextField nameTField = new TextField((String)model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME));

        nameTField.textProperty().addListener((observableValue, s, t1) -> {
            if(t1.matches("(\\d+.*)+")||!t1.matches("[A-Za-zäÄöÖüÜß_]+((\\d|[A-Za-zäÄöÖüÜß_ ])*(\\d|[A-Za-zäÄöÖüÜß_ ]))?")){
                nameTField.setText(s);
            }
        });
        HBox hBox = new HBox(new Label("Name:"),nameTField);
        changeLvlNameDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        changeLvlNameDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        changeLvlNameDialog.getDialogPane().setContent(hBox);

        Optional<ButtonType> o  = changeLvlNameDialog.showAndWait();
        if(o.isPresent()&& o.get() == ButtonType.OK){
            model.changeCurrentLevel(LevelDataType.LEVEL_NAME,nameTField.getText().trim());
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
//            model.updateUnlockedLevelsList(true);
            if(!model.getUnlockedLevelIds().contains(model.getCurrentId())){
                view.getCurrentTutorialLevelOverviewPane().removeCurrentLevel();
                view.getChallengeOverviewPane().removeCurrentLevel();
            } else {
                String levelName = model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME)+"";
                String courseName = model.getDataFromCurrentLevel(LevelDataType.COURSE)+"";
                boolean isTutorial = !courseName.equals(CHALLENGE_COURSE_NAME);
                LevelOverviewPane tutOverviewPaneOfCourse = view.getTutorialLevelOverviewPaneOfCourse(courseName);
                if(isTutorial){
                    if(tutOverviewPaneOfCourse.containsLevel(levelName))
                        tutOverviewPaneOfCourse.updateLevel(levelName);
                    else{
                        view.getChallengeOverviewPane().removeCurrentLevel();
                        for(LevelOverviewPane tutPane : view.getTutorialLevelOverviewPaneListCopy())tutPane.removeCurrentLevel();
                        tutOverviewPaneOfCourse.addLevelWithId(model.getCurrentId());
                    }
                }
                else {
                    if(view.getChallengeOverviewPane().containsLevel(levelName))
                        view.getChallengeOverviewPane().updateLevel(model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME)+"");
                    else{
                        for(LevelOverviewPane tutPane : view.getTutorialLevelOverviewPaneListCopy())tutPane.removeCurrentLevel();
                        view.getChallengeOverviewPane().addLevelWithId(model.getCurrentId());
                    }
                }
            }
            if(view.getChallengeOverviewPane().getLevelListView().getItems().size() == 0)view.getStartScreen().getChallengesBtn().setDisable(true);
            else view.getStartScreen().getChallengesBtn().setDisable(false);

            if(changes.containsKey(LevelDataType.COURSE)){
                view.getCourseOverviewPane().updateAllCourses();
            }

            if(changes.containsKey(LevelDataType.LEVEL_INDEX)){
                view.sortAllLevelEntries(changes.get(LevelDataType.LEVEL_INDEX));
            }

            Alert alert = new Alert(Alert.AlertType.NONE,"Level was saved!",ButtonType.OK);
            alert.setTitle("Success!");
            alert.setHeaderText("");
            alert.showAndWait();
            Platform.runLater(() -> view.highlightInMap(view.getSelectedPointList()));
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
            if(model.isCurrentLevelNew())model.removeCurrentLevel();
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
//        view.getMapGPane().setMouseTransparent(false);
        int columnC = view.getMapGPane().getColumnCount();
        int rowC = view.getMapGPane().getRowCount();
        for(int x = 0; x < columnC; x++){
            for(int y = 0; y < rowC; y++){
                StackPane stackPane = (StackPane) view.getMapGPane().getChildren().get(x*rowC+y);

                final GameMap gameMap = (GameMap)model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                final int k = x;
                final int h = y;
                stackPane.setOnMouseEntered(mouseEvent -> {
                    Cell cell  = gameMap.getCellAtXYClone(k,h);
                    String cellContentString = "Content: "+ cell.getContent().toString();
                    String itemString = "Item: "+ cell.getItem().toString();

                    Tooltip tooltip = new Tooltip(cellContentString);
                    String cellIdString = "Cell Id: ";
                    if(cell.getCellId() != -1)cellIdString+=cell.getCellId();
                    else cellIdString += "NONE";
                    StringBuilder linkedCellString = new StringBuilder("Linked Cell Ids: ");

                    if(cell.getContent().isTraversable() ){
                        tooltip.setText(tooltip.getText()+"\n"+itemString);
                    }
                    if(cell.getContent() == CellContent.PRESSURE_PLATE ||cell.getContent() == CellContent.ENEMY_SPAWN ||cell.getContent() == CellContent.SPAWN){
                        tooltip.setText(tooltip.getText()+"\n"+cellIdString);
                        if(cell.getContent() == CellContent.PRESSURE_PLATE)tooltip.setText(tooltip.getText()+"\nIs Inverted: " + cell.hasFlag(CellFlag.INVERTED));
                    }
                    if(cell.getContent() == CellContent.TRAP){
                        String trapStatus = "Unarmed";
                        if(cell.hasFlag(CellFlag.ARMED))trapStatus = CellFlag.ARMED.getDisplayName();
                        if(cell.hasFlag(CellFlag.PREPARING))trapStatus = CellFlag.PREPARING.getDisplayName();
                        tooltip.setText(tooltip.getText()+"\nStatus: " + trapStatus);
                    }
                    if(cell.getContent() == CellContent.GATE){
                        for(int i = 0;i < cell.getLinkedCellsSize();i++){
                            linkedCellString.append(cell.getLinkedCellId(i));
                            if(i !=cell.getLinkedCellsSize()-1) linkedCellString.append(",");
                        }
                        tooltip.setText(tooltip.getText()+"\n"+linkedCellString);
                        tooltip.setText(tooltip.getText()+"\nIs Open: " + cell.hasFlag(CellFlag.OPEN));
                        tooltip.setText(tooltip.getText()+"\nIs Turned: " + cell.hasFlag(CellFlag.TURNED));
                    }
                    tooltip.setShowDelay(GameConstants.TOOLTIP_DELAY_LONG);
                    Tooltip.install(stackPane,tooltip );
                });
                if(View.getCurrentSceneState() != SceneState.LEVEL_EDITOR){
                    continue;
                }
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
                view.setCContentButtonDisabled(CellContent.EXIT,false);
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
            //TODO: now allowed?
//            view.setCContentButtonDisabled(CellContent.SPAWN,true);
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
        else if(content == CellContent.PRESSURE_PLATE||content == CellContent.ENEMY_SPAWN||content == CellContent.SPAWN){
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
                //TODO: now allowed?
//                if(content == CellContent.SPAWN){
//                    for(int x = 0; x < gameMap.getBoundX();x++){
//                        for(int y = 0; y < gameMap.getBoundY();y++){
//                            if(gameMap.getContentAtXY(x,y) == CellContent.SPAWN){
//                                gameMap.setContent(x,y, CellContent.PATH);
//                            }
//                        }
//                    }
//                }
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

    private void handleExportCoursesBtn(ActionEvent event) {
        Dialog<ButtonType> exportCourseNameDialog = new Dialog<>();
        ButtonType btnTypeCancel = new ButtonType("Cancel",ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType btnTypeAccept = new ButtonType("Accept",ButtonBar.ButtonData.OK_DONE);
        ListView<String> courseLView = new ListView<>();
//        for(String s : model.getAllCourseNames()){
        courseLView.getItems().addAll(model.getAllCourseNames());
//        }
        courseLView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        courseLView.getSelectionModel().selectRange(0,courseLView.getItems().size());

        exportCourseNameDialog.getDialogPane().getButtonTypes().add(btnTypeCancel);
        exportCourseNameDialog.getDialogPane().getButtonTypes().add(btnTypeAccept);
        exportCourseNameDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(true);
        TextField nameField = new TextField();
        String oldName = courseLView.getSelectionModel().getSelectedItem();
        nameField.setText("Filename");
        nameField.textProperty().addListener((observableValue, s, t1) -> {
            if(!t1.matches("^[A-Za-z\\d_ÜÖÄäöüß]+$"))nameField.setText(s);
            boolean fileExists = false;
            try {
                fileExists = JSONParser.exportFileExists(t1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(t1.matches(".*\\..*")||t1.equals("")||t1.matches("\\d+")||fileExists)exportCourseNameDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(true);
            else exportCourseNameDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(false);
        });
        exportCourseNameDialog.getDialogPane().setContent(new HBox(courseLView,nameField));
        Optional<ButtonType> buttonType = exportCourseNameDialog.showAndWait();
        if(buttonType.isPresent()&&buttonType.get().getButtonData() == ButtonBar.ButtonData.OK_DONE){
            JSONParser.exportCourses(courseLView.getSelectionModel().getSelectedItems().iterator(),nameField.getText());
            new Alert(Alert.AlertType.INFORMATION,"Courses exported successfully!").showAndWait();
        }
    }
    private void handleEditCoursesBtn(ActionEvent actionEvent) {
        Dialog<ButtonType> editCoursesDialog = new Dialog<>();
        editCoursesDialog.getDialogPane().getButtonTypes().add(new ButtonType("Close",ButtonBar.ButtonData.FINISH));
        ListView<Label> courseNameLView = new ListView<>();
        for(String courseName : model.getOrderedCourseNames()){
            if(courseName.equals(CHALLENGE_COURSE_NAME))continue;
            Label l =new Label(courseName);
            l.setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
            courseNameLView.getItems().add(l);
        }
        Button createCourseBtn = new Button("Create Course");
        Button deleteCourseBtn = new Button("Delete Course");
        Button changeNameBtn = new Button("Change Name");
        Button pushUpBtn = new Button("^");
        Button pushDownBtn = new Button("v");
        pushDownBtn.setMaxHeight(GameConstants.SMALL_BUTTON_SIZE/1.5);
        pushDownBtn.setMaxWidth(pushDownBtn.getMaxHeight());
        pushUpBtn.setMaxHeight(pushDownBtn.getMaxHeight());
        pushUpBtn.setMaxWidth(pushDownBtn.getMaxHeight());


//        Button editReqIdsBtn = new Button("Edit Required Courses");
        CheckBox needsPreviousCBox = new CheckBox("Needs Previous Course");
        needsPreviousCBox.setDisable(true);

        TextField changeNameTxtField = new TextField();
        if(courseNameLView.getItems().size()>0){
            courseNameLView.getSelectionModel().select(0);
            pushUpBtn.setDisable(true);
            changeNameTxtField.setText(courseNameLView.getSelectionModel().getSelectedItem().getText());
            if(model.getAmountOfLevelsInCourse(courseNameLView.getSelectionModel().getSelectedItem().getText())>0)
                deleteCourseBtn.setDisable(true);
        }

        courseNameLView.setMaxHeight(GameConstants.BUTTON_SIZE);

        ChoiceBox<CourseDifficulty> difficultyChoiceBox = new ChoiceBox<>();
        difficultyChoiceBox.getItems().addAll(CourseDifficulty.values());
        difficultyChoiceBox.getSelectionModel().select(0);

        Button applyButton = new Button("Apply");

//        Map<Integer,CourseDifficulty> courseDifficultyMap = new HashMap<>();
//        Map<Integer,String> courseNameMap = new HashMap<>();
//        Map<Integer,List<Integer>> courseRequiredIdsMap = new HashMap<>();
////        List<Course> courseChangeList = new ArrayList<>();
//        List<Course> newCourses = new ArrayList<>();
//        List<Integer> deletedCourses = new ArrayList<>();
        List<Course> courseCopies = model.getCourseCopies();
        needsPreviousCBox.selectedProperty().addListener((observableValue, aBoolean, t1) ->{
            Course currentCourse = null;
            for(Course c : courseCopies){
                if(c.getName().equals(courseNameLView.getSelectionModel().getSelectedItem().getText())){
                    currentCourse = c;
                }
            }
            if(currentCourse != null)
                currentCourse.setNeedsPreviousCourse(t1);

            checkForChanges(currentCourse.getName(),courseCopies,courseNameLView);

        });

        pushDownBtn.setOnAction(event -> {
            int index = courseNameLView.getSelectionModel().getSelectedIndex();
            Course currentCourse = null;
            for(Course c : courseCopies){
                if(c.getName().equals(courseNameLView.getSelectionModel().getSelectedItem().getText())){
                    currentCourse = c;
                }
            }
            if(currentCourse == null)return;
            final Course c = currentCourse;
            courseCopies.sort((l1,l2) -> {
                if(l1.getName().equals(c.getName())&&l2.getName().equals(courseCopies.get(courseNameLView.getSelectionModel().getSelectedIndex()+1).getName())) return 1;
                else return 0;
            });
            courseNameLView.getItems().sort((l1,l2) -> {
                if(l1.getText().equals(c.getName())&&l2.getText().equals(courseNameLView.getItems().get(courseNameLView.getSelectionModel().getSelectedIndex()+1).getText())) return 1;
                else return 0;
            });
            courseNameLView.getSelectionModel().select(index+1);
            checkForChanges(currentCourse.getName(),courseCopies,courseNameLView);

        });

        pushUpBtn.setOnAction(event -> {
            int index = courseNameLView.getSelectionModel().getSelectedIndex();
            Course currentCourse = null;
            for(Course c : courseCopies){
                if(c.getName().equals(courseNameLView.getSelectionModel().getSelectedItem().getText())){
                    currentCourse = c;
                }
            }
            if(currentCourse == null)return;
            final Course c = currentCourse;
            courseNameLView.getItems().sort((l1,l2) -> {
                if(l2.getText().equals(c.getName())&&l1.getText().equals(courseNameLView.getItems().get(courseNameLView.getSelectionModel().getSelectedIndex()-1).getText())) return 1;
                else return 0;
            });
            courseNameLView.getSelectionModel().select(index-1);
            checkForChanges(currentCourse.getName(),courseCopies,courseNameLView);

        });
        courseNameLView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        courseNameLView.getSelectionModel().selectedItemProperty().addListener((observableValue, stringMultipleSelectionModel, t1) -> {
            if(t1 == null) return;
            Course currentCourse = null;
            int id = -1;
            for(Course c : courseCopies){
                if(c.getName().equals(t1.getText())){
                    id = c.ID;
                    currentCourse = c;
                }
            }
            if(currentCourse == null)return;
            if(courseNameLView.getSelectionModel().getSelectedIndex() == 0)pushUpBtn.setDisable(true);
            else pushUpBtn.setDisable(false);
            if(courseNameLView.getSelectionModel().getSelectedIndex() == courseNameLView.getItems().size()-1)pushDownBtn.setDisable(true);
            else pushDownBtn.setDisable(false);
            needsPreviousCBox.setSelected(model.getCourseWithName(currentCourse.getName()).needsPreviousCourse());
            if(courseNameLView.getSelectionModel().getSelectedIndex() == 0)needsPreviousCBox.setDisable(true);
            else needsPreviousCBox.setDisable(false);
//            if(id == -1){
//                throw new IllegalStateException("Hösf");
////                for(int id2 : courseNameMap.keySet()){
////                    if(courseNameMap.get(id2).equals(t1.getText()))id = id2;
////                }
//            }
            boolean inNewCourses = false;

            if(!model.getAllCourseIds().contains(id))inNewCourses = true;
            boolean courseDeleted = courseCopies.stream().noneMatch(c -> c.getName().equals(t1.getText()));
            //auf neue Kursliste nvariable anpassen?
            if((model.getAllCourseNames().contains(t1.getText())&& model.getAmountOfLevelsInCourse(t1.getText())== 0&& !courseDeleted)||inNewCourses) {
                deleteCourseBtn.setDisable(false);
                difficultyChoiceBox.setDisable(false);
                changeNameTxtField.setDisable(false);
                changeNameTxtField.setText(courseNameLView.getSelectionModel().getSelectedItem().getText());
            }
            else {
                deleteCourseBtn.setDisable(true);
                if(courseDeleted){
                    difficultyChoiceBox.setDisable(true);
                    changeNameTxtField.setDisable(true);
                }
            }
            difficultyChoiceBox.getSelectionModel().select(currentCourse.getDifficulty());
            checkForChanges(currentCourse.getName(), courseCopies, courseNameLView);
        });


//        for(String courseName : model.getAllCourseNames()){
//            courseDifficultyMap.put(model.getCourseWithName(courseName).ID, model.getDifficultyOfCourse(courseName));
//        }
        difficultyChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable,s,t1) -> {
//model.getDifficultyOfCourse(courseNameLView.getSelectionModel().getSelectedItem().getText())
            Course currentCourse = null;
//            int id = -1;
            for(Course c : courseCopies){
                if(c.getName().equals(courseNameLView.getSelectionModel().getSelectedItem().getText())){
//                    id = c.ID;
                    currentCourse = c;
                }
            }
//            int id = model.getCourseWithName(courseNameLView.getSelectionModel().getSelectedItem().getText()).ID;
            currentCourse.changeDifficulty(difficultyChoiceBox.getValue());
            checkForChanges(currentCourse.getName(), courseCopies, courseNameLView);
        });
        changeNameBtn.setOnAction(event -> {
            Course currentCourse = null;
            for(Course c : courseCopies){
                if(c.getName().equals(courseNameLView.getSelectionModel().getSelectedItem().getText())){
                    currentCourse = c;
                }
            }
            Dialog<ButtonType> changeCourseNameDialog = new Dialog<>();
            ButtonType btnTypeCancel = new ButtonType("Cancel",ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType btnTypeAccept = new ButtonType("Accept",ButtonBar.ButtonData.OK_DONE);

            changeCourseNameDialog.getDialogPane().getButtonTypes().add(btnTypeCancel);
            changeCourseNameDialog.getDialogPane().getButtonTypes().add(btnTypeAccept);
            changeCourseNameDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(true);
            TextField nameField = new TextField();
            String oldName = courseNameLView.getSelectionModel().getSelectedItem().getText();
            nameField.setText(oldName);
            nameField.textProperty().addListener((observableValue, s, t1) -> {
                boolean contains = false;
                for(Label l : courseNameLView.getItems()){
                    if(l.getText().equals(t1))contains = true;
                }
                if(contains || t1.equals("")||t1.matches("\\d+"))changeCourseNameDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(true);
                else changeCourseNameDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(false);
            });
            changeCourseNameDialog.getDialogPane().setContent(nameField);
            Optional<ButtonType> buttonType = changeCourseNameDialog.showAndWait();
            if(buttonType.isPresent()&&buttonType.get().getButtonData() == ButtonBar.ButtonData.OK_DONE){
//                Label labelToAdd = new Label(nameField.getText());
                courseNameLView.getSelectionModel().getSelectedItem().setText(nameField.getText());
//                int id = model.getCourseWithName(oldName).ID;
                String oldCName = currentCourse.getName();
                currentCourse.changeName(nameField.getText());
                checkForChanges(oldCName, courseCopies, courseNameLView);
            }
        });

//        editReqIdsBtn.setOnAction(event -> {
//            Course currentCourse = null;
//            for(Course c : courseCopies){
//                if(c.getName().equals(courseNameLView.getSelectionModel().getSelectedItem().getText())){
//                    currentCourse = c;
//                }
//            }
//            Dialog<ButtonType> editReqIdDialog = new Dialog<>();
//            ButtonType btnTypeCancel = new ButtonType("Cancel",ButtonBar.ButtonData.CANCEL_CLOSE);
//            ButtonType btnTypeAccept = new ButtonType("Accept",ButtonBar.ButtonData.OK_DONE);
//
//            editReqIdDialog.getDialogPane().getButtonTypes().add(btnTypeCancel);
//            editReqIdDialog.getDialogPane().getButtonTypes().add(btnTypeAccept);
////            createCourseDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(true);
//            ListView<Label> reqCourseListView = new ListView<>();
//            reqCourseListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
//            String currentName = courseNameLView.getSelectionModel().getSelectedItem().getText();
//            for(Course course : courseCopies){
//                String courseName = course.getName();
//                if(courseName.equals(GameConstants.CHALLENGE_COURSE_NAME) || courseName.equals(currentName))continue;
//                Label l =new Label(courseName);
////                l.setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
//                reqCourseListView.getItems().add(l);
//            }
//            for(int i = 0; i < reqCourseListView.getItems().size();i++){
//                if(currentCourse.getReqCourseIds().contains(model.getCourseWithName(reqCourseListView.getItems().get(i).getText()).ID))reqCourseListView.getSelectionModel().select(i);
//            }
//            editReqIdDialog.getDialogPane().setContent(reqCourseListView);
//            Optional<ButtonType> buttonType = editReqIdDialog.showAndWait();
//            if(buttonType.isPresent()&&buttonType.get().getButtonData() == ButtonBar.ButtonData.OK_DONE){
////                Label labelToAdd = new Label(nameField.getText());
//                int id =currentCourse.ID;
//                int amountOfSelectedItems = reqCourseListView.getSelectionModel().getSelectedItems() == null ? 0 : reqCourseListView.getSelectionModel().getSelectedItems().size();
//                boolean changed = amountOfSelectedItems != model.getCourseWithId(id).getReqCourseIds().size();
//                for(int i :currentCourse.getReqCourseIds()){
//                    if(reqCourseListView.getSelectionModel().getSelectedItems().stream().noneMatch(l -> model.getCourseWithName(l.getText()).ID == i))changed = true;
//                }
//                if(changed)
//                    courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_NOT_SAVED_STYLE);
////                else courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
//                currentCourse.setRequiredIds(reqCourseListView.getSelectionModel().getSelectedItems().stream().map(l -> model.getCourseWithName(l.getText()).ID).collect(Collectors.toList()));
////                courseRequiredIdsMap.put(id, reqCourseListView.getSelectionModel().getSelectedItems().stream().map(l -> model.getCourseWithName(l.getText()).ID).collect(Collectors.toList()));
//            }
//        });
        applyButton.setOnAction(evt -> {
            for(Course c : courseCopies){
                if(c.getName().equals(courseNameLView.getItems().get(0).getText())){
                    c.setNeedsPreviousCourse(false);
                    break;
                }
            }

            List<String> nameList = new ArrayList<>();
            for(Label l : courseNameLView.getItems()){
                nameList.add(l.getText() );
            }
            model.adaptCourses(Util.sortCourses(courseCopies,nameList));

//            for(int cId : model.getAllCourseIds()){
            view.updateTutorialOverviewPaneList(courseCopies);
            view.getLevelEditorModule().getCourseValueLbl().setText(model.getCurrentCourseName());
//                if(courseNameMap.containsKey(cId))
            view.getCourseOverviewPane().updateAllCourseName();
            view.getCourseOverviewPane().updateDeletedEntries();
            view.getCourseOverviewPane().updateAddedEntries();
            view.getCourseOverviewPane().updateAllUnlockedStatus();
            view.getCourseOverviewPane().updateOrder();
//                if(courseRequiredIdsMap.containsKey(cId)){
//                    boolean isUnlocked = model.isCourseUnlocked(cId);
//                    view.getCourseOverviewPane().updateUnlockedStatus(cId, isUnlocked);
//                }
//                if(cId == model.getCurrentCourseId() && courseNameMap.containsKey(cId)){
//                }
//            }
//            model.removeAllCourses(deletedCourses);
//            model.addAllCourses(newCourses);
//            model.applyCourseChanges(courseDifficultyMap,courseNameMap,courseRequiredIdsMap);
//            List<String> deletedCourseNames = new ArrayList<>();
//            for(int id: deletedCourses){
//                deletedCourseNames.add(model.getCourseWithId(id).getName());
//            }
//            List<String> newCourseNames = new ArrayList<>();
//            for(Course c : newCourses){
//                newCourseNames.add(c.getName());
//            }
//            view.removeAllCourses(deletedCourseNames);
//            Map<String,CourseDifficulty> newCourseDiffMap = new HashMap<>();
//            for(String cName : newCourseNames){
//                CourseDifficulty lDiff = CourseDifficulty.BEGINNER;
//                for (Course newCourse : newCourses) {
//                    if (newCourse.getName().equals(cName)) lDiff = newCourse.getDifficulty();
//                }
//                newCourseDiffMap.put(cName, lDiff);
//            }
//            view.addAllCourses(newCourseNames,newCourseDiffMap);
            for(int i = 0; i< courseNameLView.getItems().size();i++) {
                Label l = courseNameLView.getItems().get(i);
                if(courseCopies.stream().noneMatch(course -> course.getName().equals(l.getText()))){
                    courseNameLView.getItems().remove(l );
                    i--;
                }
                else l.setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
            }
        });


        createCourseBtn.setOnAction(evt -> {

            Dialog<ButtonType> createCourseDialog = new Dialog<>();
            ButtonType btnTypeCancel = new ButtonType("Cancel",ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType btnTypeAccept = new ButtonType("Accept",ButtonBar.ButtonData.OK_DONE);

            createCourseDialog.getDialogPane().getButtonTypes().add(btnTypeCancel);
            createCourseDialog.getDialogPane().getButtonTypes().add(btnTypeAccept);
            createCourseDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(true);
            CheckBox needsPrevCBox = new CheckBox("Needs previous Course");
            if(model.getAllCourseNames().size() <= 1)needsPrevCBox.setDisable(true);
            TextField nameField = new TextField();
            nameField.textProperty().addListener((observableValue, s, t1) -> {
                boolean contains = model.getAllCourseNames().contains(t1);
                for(Label l : courseNameLView.getItems()){
                    if(l.getText().equals(t1))contains = true;
                }
                if(contains || t1.equals("")||t1.matches("\\d+"))createCourseDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(true);
                else createCourseDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(false);
            });
            ChoiceBox<CourseDifficulty> difficultyChoiceBox2 = new ChoiceBox<>();
            difficultyChoiceBox2.getItems().addAll(CourseDifficulty.values());
            difficultyChoiceBox2.getSelectionModel().select(0);
            createCourseDialog.getDialogPane().setContent(new HBox(nameField,difficultyChoiceBox2,needsPrevCBox));
            Optional<ButtonType> buttonType = createCourseDialog.showAndWait();
            if(buttonType.isPresent()&&buttonType.get().getButtonData() == ButtonBar.ButtonData.OK_DONE){
                Label labelToAdd = new Label(nameField.getText());
                if(model.getAllCourseNames().contains(nameField.getText()) && model.getDifficultyOfCourse(nameField.getText()) == difficultyChoiceBox2.getValue())
                    labelToAdd.setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
                else labelToAdd.setStyle(GameConstants.LEVEL_NOT_SAVED_STYLE);
                courseNameLView.getItems().add(labelToAdd);
                CourseDifficulty courseDifficulty = difficultyChoiceBox2.getValue();
                if(courseDifficulty == null) courseDifficulty = CourseDifficulty.BEGINNER;
                int id = model.getCourseWithName(nameField.getText()).ID;
                courseCopies.add(new Course(false, new ArrayList<>(), courseDifficulty, nameField.getText()  ));
//                courseDifficultyMap.put(id, courseDifficulty);
//                newCourses.add(new Course(new ArrayList<>(), new ArrayList<>(), courseDifficulty, nameField.getText()));
//                deletedCourses.remove((Integer)id);
            }
        });
        deleteCourseBtn.setOnAction(evt -> {
            String courseName = courseNameLView.getSelectionModel().getSelectedItem().getText();
//            courseDifficultyMap.remove(model.getCourseWithName(courseName).ID);
            Label lblToRmv = null;
            for(Label l : courseNameLView.getItems()){
                if(l.getText().equals(courseName))lblToRmv = l;
            }
            if(lblToRmv != null)lblToRmv.setStyle(GameConstants.WILL_BE_REMOVED_STYLE);
            if(!model.getAllCourseNames().contains(courseName))
                courseNameLView.getItems().remove(lblToRmv);
//            deletedCourses.add(model.getCourseWithName(courseName).ID);
//            newCourses.removeIf(c -> c.getName().equals(courseName));

            courseCopies.removeIf(c -> c.getName().equals(courseName));

        });

        VBox leftVBox = new VBox(new HBox(new VBox(pushUpBtn,pushDownBtn),courseNameLView),applyButton);
        VBox rightVBox = new VBox(createCourseBtn,deleteCourseBtn,difficultyChoiceBox,changeNameBtn,needsPreviousCBox);
        rightVBox.setSpacing(GameConstants.SMALL_FONT_SIZE);
        HBox contentHBox = new HBox(leftVBox,rightVBox);

        editCoursesDialog.getDialogPane().setContent(contentHBox);
        editCoursesDialog.showAndWait();
    }

    private void checkForChanges(String name, List<Course> courseCopies, ListView<Label> courseNameLView) {
        Course currentCourse = null;
        for(Course c : courseCopies){
            if(c.getName().equals(courseNameLView.getSelectionModel().getSelectedItem().getText())){
                currentCourse = c;
                break;
            }
        }
        if(currentCourse == null)return;

        if(!currentCourse.getName().equals(name))
            courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_NOT_SAVED_STYLE);
        else if(model.getCourseWithName(currentCourse.getName()).needsPreviousCourse() != currentCourse.needsPreviousCourse())
            courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_NOT_SAVED_STYLE);
        else if(currentCourse.getDifficulty() != model.getDifficultyOfCourse(currentCourse.getName()))
            courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_NOT_SAVED_STYLE);
        else if(model.getIndexOfCourseWithId(currentCourse.getID())-1 != courseNameLView.getSelectionModel().getSelectedIndex())
            courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_NOT_SAVED_STYLE);

        else
            courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
    }

    private void handleEditRequiredLevelsBtn(ActionEvent e) {
        Dialog<ButtonType> chooseRequiredLvlsDialog = new Dialog<>();
        ListView<String> requiredLevelsListView = new ListView<>();
        requiredLevelsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        for (int i = 0; i < model.getCurrentIndex(); i++) {
            requiredLevelsListView.getItems().add(model.getDataFromLevelWithId(LevelDataType.LEVEL_NAME, model.getIdOfLevelInCurrentCourseAt(i))+"");
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
/*
Dialog<ButtonType> editCoursesDialog = new Dialog<>();
        editCoursesDialog.getDialogPane().getButtonTypes().add(new ButtonType("Close",ButtonBar.ButtonData.FINISH));
        ListView<Label> courseNameLView = new ListView<>();
        for(String courseName : model.getAllCourseNames()){
            if(courseName.equals(GameConstants.CHALLENGE_COURSE_NAME))continue;
            Label l =new Label(courseName);
            l.setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
            courseNameLView.getItems().add(l);
        }
        Button createCourseBtn = new Button("Create Course");
        Button deleteCourseBtn = new Button("Delete Course");
        Button changeNameBtn = new Button("Change Name");
        Button editReqIdsBtn = new Button("Edit Required Ids");
        //TODO: textfeld hinzfügen!
        TextField changeNameTxtField = new TextField();
        if(courseNameLView.getItems().size()>0){
            courseNameLView.getSelectionModel().select(0);
            changeNameTxtField.setText(courseNameLView.getSelectionModel().getSelectedItem().getText());
            if(model.getAmountOfLevelsInCourse(courseNameLView.getSelectionModel().getSelectedItem().getText())>0)
                deleteCourseBtn.setDisable(true);
        }

        courseNameLView.setMaxHeight(GameConstants.BUTTON_SIZE);

        ChoiceBox<CourseDifficulty> difficultyChoiceBox = new ChoiceBox<>();
        difficultyChoiceBox.getItems().addAll(CourseDifficulty.values());
        difficultyChoiceBox.getSelectionModel().select(0);

        //TODO: courseChanges?!
        Button applyButton = new Button("Apply");

//        Map<Integer,CourseDifficulty> courseDifficultyMap = new HashMap<>();
//        Map<Integer,String> courseNameMap = new HashMap<>();
//        Map<Integer,List<Integer>> courseRequiredIdsMap = new HashMap<>();
////        List<Course> courseChangeList = new ArrayList<>();
//        List<Course> newCourses = new ArrayList<>();
//        List<Integer> deletedCourses = new ArrayList<>();
        List<Course> courseCopies = model.getCourseCopies();
        courseNameLView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        courseNameLView.getSelectionModel().selectedItemProperty().addListener((observableValue, stringMultipleSelectionModel, t1) -> {
            Course currentCourse = null;
            int id = -1;
            for(Course c : courseCopies){
                if(c.getName().equals(t1.getText())){
                    id = c.ID;
                    currentCourse = c;
                }
            }
            if(id == -1){
                throw new IllegalStateException("Hösf");
//                for(int id2 : courseNameMap.keySet()){
//                    if(courseNameMap.get(id2).equals(t1.getText()))id = id2;
//                }
            }
            boolean inNewCourses = false;

            if(!model.getAllCourseIds().contains(id))inNewCourses = true;
            boolean courseDeleted = courseCopies.stream().noneMatch(c -> c.getName().equals(t1.getText()));
            //auf neue Kursliste nvariable anpassen?
            if((model.getAllCourseNames().contains(t1.getText())&& model.getAmountOfLevelsInCourse(t1.getText() )== 0&& !courseDeleted)||inNewCourses) {
                deleteCourseBtn.setDisable(false);
                difficultyChoiceBox.setDisable(false);
                changeNameTxtField.setDisable(false);
                changeNameTxtField.setText(courseNameLView.getSelectionModel().getSelectedItem().getText());
            }
            else {
                deleteCourseBtn.setDisable(true);
                if(courseDeleted){
                    difficultyChoiceBox.setDisable(true);
                    changeNameTxtField.setDisable(true);
                }
            }
            difficultyChoiceBox.getSelectionModel().select(currentCourse.getDifficulty());
        });


//        for(String courseName : model.getAllCourseNames()){
//            courseDifficultyMap.put(model.getCourseWithName(courseName).ID, model.getDifficultyOfCourse(courseName));
//        }
        difficultyChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable,s,t1) -> {
//model.getDifficultyOfCourse(courseNameLView.getSelectionModel().getSelectedItem().getText())
            Course currentCourse = null;
//            int id = -1;
            for(Course c : courseCopies){
                if(c.getName().equals(courseNameLView.getSelectionModel().getSelectedItem().getText())){
//                    id = c.ID;
                    currentCourse = c;
                }
            }
            if(currentCourse.getDifficulty() == difficultyChoiceBox.getValue())
                courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
            else courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_NOT_SAVED_STYLE);
//            int id = model.getCourseWithName(courseNameLView.getSelectionModel().getSelectedItem().getText()).ID;
            currentCourse.changeDifficulty(difficultyChoiceBox.getValue());
        });
        changeNameBtn.setOnAction(event -> {
            Course currentCourse = null;
            for(Course c : courseCopies){
                if(c.getName().equals(courseNameLView.getSelectionModel().getSelectedItem().getText())){
                    currentCourse = c;
                }
            }
            Dialog<ButtonType> createCourseDialog = new Dialog<>();
            ButtonType btnTypeCancel = new ButtonType("Cancel",ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType btnTypeAccept = new ButtonType("Accept",ButtonBar.ButtonData.OK_DONE);

            createCourseDialog.getDialogPane().getButtonTypes().add(btnTypeCancel);
            createCourseDialog.getDialogPane().getButtonTypes().add(btnTypeAccept);
            createCourseDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(true);
            TextField nameField = new TextField();
            String oldName = courseNameLView.getSelectionModel().getSelectedItem().getText();
            nameField.setText(oldName);
            nameField.textProperty().addListener((observableValue, s, t1) -> {
                boolean contains = false;
                for(Label l : courseNameLView.getItems()){
                    if(l.getText().equals(t1))contains = true;
                }
                if(contains || t1.equals(""))createCourseDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(true);
                else createCourseDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(false);
            });
            createCourseDialog.getDialogPane().setContent(nameField);
            Optional<ButtonType> buttonType = createCourseDialog.showAndWait();
            if(buttonType.isPresent()&&buttonType.get().getButtonData() == ButtonBar.ButtonData.OK_DONE){
//                Label labelToAdd = new Label(nameField.getText());
                if(oldName.equals(nameField.getText()))
                    courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
                else courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_NOT_SAVED_STYLE);
                courseNameLView.getSelectionModel().getSelectedItem().setText(nameField.getText());
//                int id = model.getCourseWithName(oldName).ID;
                currentCourse.changeName(nameField.getText());
            }
        });

        editReqIdsBtn.setOnAction(event -> {
            Course currentCourse = null;
            for(Course c : courseCopies){
                if(c.getName().equals(courseNameLView.getSelectionModel().getSelectedItem().getText())){
                    currentCourse = c;
                }
            }
            Dialog<ButtonType> createCourseDialog = new Dialog<>();
            ButtonType btnTypeCancel = new ButtonType("Cancel",ButtonBar.ButtonData.CANCEL_CLOSE);
            ButtonType btnTypeAccept = new ButtonType("Accept",ButtonBar.ButtonData.OK_DONE);

            createCourseDialog.getDialogPane().getButtonTypes().add(btnTypeCancel);
            createCourseDialog.getDialogPane().getButtonTypes().add(btnTypeAccept);
//            createCourseDialog.getDialogPane().lookupButton(btnTypeAccept).setDisable(true);
            ListView<Label> reqCourseListView = new ListView<>();
            String currentName = courseNameLView.getSelectionModel().getSelectedItem().getText();
            for(Course course : courseCopies){
                String courseName = course.getName();
                if(courseName.equals(GameConstants.CHALLENGE_COURSE_NAME) || courseName.equals(currentName))continue;
                Label l =new Label(courseName);
                l.setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
                reqCourseListView.getItems().add(l);
            }
            for(int i = 0; i < reqCourseListView.getItems().size();i++){
                if(currentCourse.getReqCourseIds().contains(model.getCourseWithName(reqCourseListView.getItems().get(i).getText()).ID))reqCourseListView.getSelectionModel().select(i);
            }
            createCourseDialog.getDialogPane().setContent(reqCourseListView);
            Optional<ButtonType> buttonType = createCourseDialog.showAndWait();
            if(buttonType.isPresent()&&buttonType.get().getButtonData() == ButtonBar.ButtonData.OK_DONE){
//                Label labelToAdd = new Label(nameField.getText());
                int id =currentCourse.ID;
                int amountOfSelectedItems = reqCourseListView.getSelectionModel().getSelectedItems() == null ? 0 : reqCourseListView.getSelectionModel().getSelectedItems().size();
                boolean changed = amountOfSelectedItems == model.getCourseWithId(id).getReqCourseIds().size();
                for(int i :currentCourse.getReqCourseIds()){
                    if(reqCourseListView.getSelectionModel().getSelectedItems().stream().noneMatch(l -> model.getCourseWithName(l.getText()).ID == i))changed = true;
                }
                if(changed)
                    courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_NOT_SAVED_STYLE);
                else courseNameLView.getSelectionModel().getSelectedItem().setStyle(GameConstants.LEVEL_IS_SAVED_STYLE);
                currentCourse.setRequiredIds(reqCourseListView.getSelectionModel().getSelectedItems().stream().map(l -> model.getCourseWithName(l.getText()).ID).collect(Collectors.toList()));
//                courseRequiredIdsMap.put(id, reqCourseListView.getSelectionModel().getSelectedItems().stream().map(l -> model.getCourseWithName(l.getText()).ID).collect(Collectors.toList()));
            }
 */