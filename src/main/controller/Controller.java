package main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import main.model.Level;
import main.model.Model;
import main.model.statement.ComplexStatement;
import main.model.statement.SimpleStatement;
import main.model.statement.Statement;
import main.utility.GameConstants;
import main.parser.CodeParser;
import main.parser.JSONParser;
import main.utility.Util;
import main.view.CodeArea;
import main.view.CodeField;
import main.view.SceneState;
import main.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Controller {
    private double mouse_PositionX;
    private double mouse_PositionY;
    private Model model;
    private View view;
    private Timeline timeline;
    private CodeAreaController codeAreaController;
    private EditorController editorController;
    private int minIndex = 0;

    public Controller(View view, Model model){
        this.model = model;
        this.view = view;
//        view.notify(Event.LEVEL_CHANGED); //TODO: better solution?

//        setActionHandlerForTextFields(view.getCodeBoxCompound().getVBox());
        codeAreaController = new CodeAreaController(view,model);
        editorController = new EditorController(view,model,codeAreaController);
//        codeAreaController.setAllHandlersForCodeArea();
        try {
            minIndex = JSONParser.getTutorialProgressIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
        view.getStage().getScene().setOnKeyPressed(event -> {
            if(!(view.getStage().getScene().getFocusOwner() instanceof CodeField)){
                if(view.getCodeArea().getSelectedCodeField() == null)
                    view.getCodeArea().select(0, Selection.END);
                view.getCodeArea().getSelectedCodeField().requestFocus();

            }
        });
        view.getShowSpellBookBtn().setOnAction(evt -> {
            view.toggleShowSpellBook();

        });
        view.getSpellBookPane().getCloseBtn().setOnAction(evt ->{
            view.toggleShowSpellBook();
        });
        view.getSpellBookPane().getMoveBtn().setOnMouseDragged(evt ->{
            Button moveBtn =  view.getSpellBookPane().getMoveBtn();
            view.getSpellBookPane().setTranslateX(view.getSpellBookPane().getTranslateX()+evt.getX()-mouse_PositionX-moveBtn.getWidth()/2);
            view.getSpellBookPane().setTranslateY(view.getSpellBookPane().getTranslateY()+evt.getY()-mouse_PositionY-moveBtn.getHeight()/2);
        });

        view.getSpellBookPane().getMoveBtn().setOnMouseClicked(evt ->{
            mouse_PositionX = evt.getX();
            mouse_PositionY = evt.getY();
        });
        addHighlightingEffect(view.getShowSpellBookBtn(),view.getBackBtn(),view.getBtnExecute(), view.getBtnReset(), view.getTutorialGroup().getEndIntroductionBtn(),
                view.getLevelOverviewPane().getBackBtn(),view.getLevelOverviewPane().getPlayBtn(),view.getTutorialLevelOverviewPane().getBackBtn(),
                view.getTutorialLevelOverviewPane().getPlayBtn(),view.getTutorialGroup().getNextBtn(),view.getTutorialGroup().getPrevBtn());

//        System.out.println("ONCE!");
        view.getSpeedSlider().valueProperty().addListener((observableValue,s,t1) ->{
//            timeline.set
            if(timeline!=null)
            timeline.setRate(GameConstants.TICK_SPEED*t1.doubleValue());
        });

        view.getBackBtn().setOnAction(actionEvent -> {
            boolean isVisible = view.getSpellBookPane().isVisible();
            if(isVisible)view.getShowSpellBookBtn().fire();
            switch (view.getCurrentSceneState()){
                case LEVEL_EDITOR:
                    if(codeAreaController.isGameRunning())view.getBtnReset().fire();
//                    view.getShowSpellBookBtn().setText("Show Spellbook");
                    view.setSceneState(SceneState.START_SCREEN);
                    break;
                case TUTORIAL_LEVEL_SELECT:
//                    view.getShowSpellBookBtn().setText("Show Spellbook");

                    view.setSceneState(SceneState.START_SCREEN);
                    break;
                case LEVEL_SELECT:
                    throw new IllegalStateException("This should not have been possible!");
                case PLAY:

                    if(codeAreaController.isGameRunning())view.getBtnReset().fire();
                    view.setSceneState(SceneState.LEVEL_SELECT);

                    break;
                case TUTORIAL:
                    if(codeAreaController.isGameRunning())view.getBtnReset().fire();
                    if(minIndex == -1)view.setSceneState(SceneState.START_SCREEN);
                    else view.setSceneState(SceneState.TUTORIAL_LEVEL_SELECT);
                    view.getBtnExecute().setMouseTransparent(false);
                    view.getSpeedSlider().setMouseTransparent(false);
                    view.getShowSpellBookBtn().setMouseTransparent(false);
                    view.getCodeArea().setDisable(false);
                    break;
                case START_SCREEN:
                    System.exit(0);
                    break;
            }
        });

        view.getStartScreen().getLvlEditorBtn().setOnAction(actionEvent -> {

            view.setSceneState(SceneState.LEVEL_EDITOR);


            editorController.setEditorHandlers();
            Platform.runLater(()-> view.highlightInMap(view.getSelectedPointList()));
        });

        view.getStartScreen().getExitBtn().setOnAction(actionEvent -> {
            System.exit(0);
        });

        view.getStartScreen().getTutorialBtn().setOnAction(actionEvent -> {
            Level selectedLevel = model.getCurrentLevel();

            int amountOfTuts = 0;
            for(Level l :model.getLevelListCopy()){
                if(l.isTutorial() && l.getIndex() < selectedLevel.getIndex() && l.getIndex() > minIndex)selectedLevel = l;
                if(l.isTutorial())amountOfTuts++;
            }
            model.selectLevel(selectedLevel.getName());
            view.getTutorialLevelOverviewPane().getBackBtn().setOnAction(actionEvent2 ->
                    view.setSceneState(SceneState.START_SCREEN)
            );
            view.getTutorialLevelOverviewPane().getPlayBtn().setOnAction(actionEvent1 -> {
                String levelName = view.getTutorialLevelOverviewPane().getLevelListView().getSelectionModel().getSelectedItem().getLevelName();
                model.selectLevel(levelName);
                view.setSceneState(SceneState.TUTORIAL);
            });
            if(minIndex != -1){
                view.setSceneState(SceneState.TUTORIAL_LEVEL_SELECT);

            }
            else view.setSceneState(SceneState.TUTORIAL);

            view.getTutorialGroup().getNextBtn().setOnAction(evt -> {
                view.getTutorialGroup().next();
                if(view.isIntroduction()){
                    view.highlightButtons();
                }
                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
                ScrollPane sp = (ScrollPane)view.getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
                if(view.getTutorialGroup().isLastMsg()&&view.isIntroduction())view.getTutorialGroup().getEndIntroductionBtn().setVisible(true);
                view.getTutorialGroup().getEndIntroductionBtn().setOnAction(event-> {
                    view.leaveIntroductions();
                });
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }});
            view.getTutorialGroup().getHideBtn().setOnAction(evt -> {
                view.getTutorialGroup().toggleStackpaneVisibility();

            });
            view.getTutorialGroup().getPrevBtn().setOnAction(evt -> {
                view.getTutorialGroup().prev();
                if(view.isIntroduction()){
                    view.highlightButtons();
                }
                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
                ScrollPane sp = (ScrollPane)view.getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }});
        });

        if(view.getLevelOverviewPane().getLevelListView().getItems().size()==0)view.getStartScreen().getPlayBtn().setDisable(true);
        view.getStartScreen().getPlayBtn().setOnAction(actionEvent -> {
            view.setSceneState(SceneState.LEVEL_SELECT);
            view.getLevelOverviewPane().getBackBtn().setOnAction(actionEvent2 ->
                    view.setSceneState(SceneState.START_SCREEN)
            );
            view.getLevelOverviewPane().getPlayBtn().setOnAction(actionEvent1 -> {
                String levelName = view.getLevelOverviewPane().getLevelListView().getSelectionModel().getSelectedItem().getLevelName();
                model.selectLevel(levelName);
                view.setSceneState(SceneState.PLAY);
            });
        });


            setExecuteHandler();

        view.getBtnReset().setOnAction(actionEvent -> {
            setExecuteHandler();

            view.getCodeArea().highlightCodeField(-1);
            if(model.getCurrentLevel().hasAi())view.getAICodeArea().highlightCodeField(-1);
            view.setNodesDisableWhenRunning(false);
            if(view.getCurrentSceneState() == SceneState.LEVEL_EDITOR){
                view.getLevelEditorModule().setDisableAllLevelBtns(false);
                if(model.getCurrentLevel().hasAi())view.getAICodeArea().setEditable(true);
                editorController.setAllEditButtonsToDisable(false);
                editorController.setHandlersForMapCells();}
            codeAreaController.setGameRunning(false);
            timeline.stop();
            model.getCurrentLevel().reset();
            view.getCodeArea().setEditable(true);
            view.getCodeArea().select(0,Selection.END);
        });
        view.getLoadBestCodeBtn().setOnAction(actionEvent -> {
            List<String> bestCode;
            try {
                boolean noCode = view.getCodeArea().getAllText().size() == 1 && view.getCodeArea().getAllText().get(0).equals("");
                Alert alert = new Alert(Alert.AlertType.NONE,"Do you really want to overwrite the current code?",ButtonType.OK, ButtonType.CANCEL);
//                alert.getDialogPane().setStyle("-fx-font-size:"+GameConstants.BIG_FONT_SIZE);
                Optional<ButtonType> result = noCode ? null : alert.showAndWait();
                if(noCode ||(result.isPresent() && result.get() == ButtonType.OK)){
                bestCode = JSONParser.getBestCode(model.getCurrentLevel().getName());
//                if(bestCode.size() !=0)model.getCurrentLevel().setPlayerBehaviour(new CodeParser().parseProgramCode(bestCode));
                if(bestCode.size() !=0) {
                    CodeArea codeArea = new CodeArea(new CodeParser().parseProgramCode(bestCode), true, false);
                    view.setCodeArea(codeArea, false);
                    view.getBtnExecute().setDisable(false);
                    view.getStoreCodeBtn().setDisable(false);
//                    codeArea.draw();
//                    codeAreaController.setAllHandlersForCodeArea(false);
                }else new Alert(Alert.AlertType.NONE,"No best code stored!",ButtonType.OK).showAndWait();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        view.getStoreCodeBtn().setOnAction(actionEvent -> {

            try {
                JSONParser.storeCode(view.getCodeArea().getAllText());
//                if(bestCode.size() !=0)model.getCurrentLevel().setPlayerBehaviour(new CodeParser().parseProgramCode(bestCode));

               new Alert(Alert.AlertType.NONE,"Code stored!",ButtonType.OK).showAndWait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        view.getClearCodeBtn().setOnAction(actionEvent -> {
            Dialog<ButtonType> deleteDialog = new Dialog<>();
//            deleteDialog.getDialogPane().setBackground(new Background(new BackgroundImage(new Image( "file:resources/images/background_tile.png" ), BackgroundRepeat.REPEAT,null, BackgroundPosition.CENTER, BackgroundSize.DEFAULT )));

            Label deleteLabel = new Label("You are going to remove all code! Are you sure?");
            deleteLabel.setAlignment(Pos.CENTER);
            deleteLabel.setMinWidth(GameConstants.TEXTFIELD_WIDTH);
//            deleteLabel.setStyle("-fx-text-fill: white;-fx-effect: dropshadow(three-pass-box, black, 10, 0.6, 0.6, 0);");
            deleteLabel.setTextAlignment(TextAlignment.CENTER);
//            deleteLabel.setFont(GameConstants.BIG_FONT);


            deleteDialog.getDialogPane().setContent(deleteLabel);
            ButtonType noBtn = new ButtonType("NO", ButtonBar.ButtonData.NO);
            ButtonType yesBtn = new ButtonType("YES", ButtonBar.ButtonData.YES);
            deleteDialog.getDialogPane().getButtonTypes().addAll(noBtn,yesBtn);
            Optional<ButtonType> result = deleteDialog.showAndWait();
            if(result.isPresent()){
                switch (result.get().getButtonData()){
                    default:
                        break;
                    case YES:
                        ComplexStatement complexStatement = new ComplexStatement();
                        complexStatement.addSubStatement(new SimpleStatement());
                        CodeArea codeArea = new CodeArea(complexStatement,true,false);
                        view.setCodeArea(codeArea,false);
                        break;
                }
            }
//            codeArea.draw();
//            codeAreaController.setAllHandlersForCodeArea(false);

        });

    }

    private void setExecuteHandler() {

        ((ImageView)view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.EXECUTE_BTN_IMAGE_PATH));
        view.getBtnExecute().setOnAction(actionEvent -> {
        CodeParser codeParser = new CodeParser(view.getCodeArea().getAllText(),true);
        CodeParser aiCodeParser = new CodeParser(view.getAICodeArea().getAllText(),false);
        Level currentLevel = model.getCurrentLevel();
        try {
            ComplexStatement behaviour = codeParser.parseProgramCode();
            ComplexStatement aiBehaviour = new ComplexStatement();
            if(currentLevel.hasAi())aiBehaviour = aiCodeParser.parseProgramCode();
            if(view.getCurrentSceneState()==SceneState.LEVEL_EDITOR) view.getLevelEditorModule().setDisableAllLevelBtns(true);
            //TODO: delete
            if(GameConstants.DEBUG)System.out.println(behaviour.print());
            if(GameConstants.DEBUG)System.out.println(aiBehaviour.print());
            currentLevel.setPlayerBehaviour(behaviour);
            //TODO: currentLevel.addListener(view);
            currentLevel.setAiBehaviour(aiBehaviour);
            view.setNodesDisableWhenRunning(true);
            codeAreaController.setGameRunning(true);
//                currentLevel.setCurrentMapToOriginal();
            view.getBtnExecute().setDisable(false);

            view.getSpeedSlider().setDisable(false);
//            actionEvent.consume();

            ((ImageView)view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.PAUSE_BTN_IMAGE_PATH));
            setPauseAndRunHandler();

            timeline = new Timeline();

            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.setRate(GameConstants.TICK_SPEED*view.getSpeedSlider().getValue());
            timeline.getKeyFrames().addAll(new KeyFrame(Duration.seconds(0.8), event ->
            {
                try {
                    Statement[] executedStatements = currentLevel.executeTurn();
                    int index = behaviour.findIndexOf(executedStatements[0],0);
                    //sadly with the current implementation, executeIfs cannot be detected as they are changed inside the CodeEvaluator
                    if(index == -1) index = behaviour.findIndexOf(currentLevel.getExecuteIfStatementWorkaround(),0);
                    if(index != -1)view.getCodeArea().highlightCodeField(index);
                    if(model.getCurrentLevel().hasAi()){
                        int index2 = model.getCurrentLevel().getAIBehaviour().findIndexOf(executedStatements[1],0);
                        if(index2 == -1) index2 = model.getCurrentLevel().getAIBehaviour().findIndexOf(currentLevel.getExecuteIfStatementWorkaround(),0);

                        if(index2 != -1)view.getAICodeArea().highlightCodeField(index2);
                    }

                    view.drawMap(currentLevel.getCurrentMap());
                    view.deselect();
                    if (currentLevel.isWon()){
                        int turns = currentLevel.getTurnsTaken();
                        int loc = behaviour.getActualSize();

                        double nStars = Util.calculateStars(turns,loc,currentLevel.getTurnsToStars(),currentLevel.getLocToStars());
                        //TODO: really not in editor??!
//                            if(view.getCurrentSceneState()==SceneState.PLAY){

                        JSONParser.storeProgressIfBetter(currentLevel.getName(),turns,loc,currentLevel.getPlayerBehaviour());
                        model.updateFinishedList();
//                            }
                        timeline.stop();
                        String winString = "You have won!"+"\nYou earned "  + (int)nStars + (Math.round(nStars)!=(int)nStars ? ".5" : "") + (nStars > 1 ? " Stars! (" : " Star! (")+turns + " Turns, " + loc + " Lines of Code)";
//                            Platform.runLater(() ->new Alert(Alert.AlertType.NONE,winString, ButtonType.OK).showAndWait());
                        int amountOfTuts = 0;
                        for(Level l :model.getLevelListCopy()){
                            if(l.isTutorial())amountOfTuts++;
                        }
                        if(currentLevel.isTutorial()&&view.getCurrentSceneState()==SceneState.TUTORIAL){
                            if(nStars >= Util.calculateStars(currentLevel.getBestTurns(),currentLevel.getBestLOC(),currentLevel.getTurnsToStars(),currentLevel.getLocToStars()))
                                view.getTutorialLevelOverviewPane().updateLevel(currentLevel,amountOfTuts,Util.getStarImageFromDouble(nStars));
//                            minIndex = (minIndex > currentLevel.getIndex()) ? minIndex : currentLevel.getIndex();
                            int nextIndex = currentLevel.getIndex()+1;
                            minIndex = nextIndex-1 > minIndex ? nextIndex-1 : minIndex;
                            if(nextIndex < amountOfTuts-1){
                                Level nextLevel = model.getLevelWithIndex(nextIndex);
                                if(!view.getTutorialLevelOverviewPane().containsLevel(nextLevel))
                                    view.getTutorialLevelOverviewPane().addLevel(nextLevel, view.getImageFromMap(nextLevel.getOriginalMap()));
                            }
                        }
                        else if(view.getCurrentSceneState()==SceneState.PLAY){
                            if(nStars >= Util.calculateStars(currentLevel.getBestTurns(),currentLevel.getBestLOC(),currentLevel.getTurnsToStars(),currentLevel.getLocToStars()))
                                view.getLevelOverviewPane().updateLevel(currentLevel,amountOfTuts,Util.getStarImageFromDouble(nStars));
                            int nextIndex = currentLevel.getIndex()+1;

                            if(nextIndex < model.getAmountOfLevels()-1){
                                Level nextLevel = model.getLevelWithIndex(nextIndex);

                                if(!view.getLevelOverviewPane().containsLevel(nextLevel))
                                view.getLevelOverviewPane().addLevel(nextLevel, view.getImageFromMap(nextLevel.getOriginalMap()));
                            }
                        }


                        Platform.runLater(() -> {
                            try {
                                showWinDialog(winString,view.getCurrentSceneState(),nStars);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        codeAreaController.setGameRunning(false);
//                            if(view.getCurrentSceneState() != SceneState.LEVEL_EDITOR)
                        JSONParser.saveStatementProgress(currentLevel.getUnlockedStatementList());
                        if(view.getLevelOverviewPane().getLevelListView().getItems().size()>0)view.getStartScreen().getPlayBtn().setDisable(false);
                        view.getSpellBookPane().updateSpellbookEntries(currentLevel.getUnlockedStatementList());
                    }
                    if(currentLevel.isStackOverflow()){
                        timeline.stop();
                        Alert alert = new Alert(Alert.AlertType.NONE,"You might have accidentally caused an endless loop! You are not allowed to use big loops without method calls!", ButtonType.OK);
                        Platform.runLater(() -> {
                            Optional<ButtonType> result = alert.showAndWait();
                            if(result.isPresent()){
                                view.getBtnReset().fire();
                            }
                        });
                    }
//
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }));
            timeline.play();
            if(view.getCurrentSceneState()==SceneState.LEVEL_EDITOR)editorController.setAllEditButtonsToDisable(true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }});
    }

    private void setPauseAndRunHandler() {
        view.getBtnExecute().setOnAction(evt -> {
            timeline.pause();
            ((ImageView)view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.EXECUTE_BTN_IMAGE_PATH));
            view.getBtnExecute().setOnAction(evt2 -> {
                timeline.play();
                setPauseAndRunHandler();
                ((ImageView)view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.PAUSE_BTN_IMAGE_PATH));
            });
        });
    }

    private void addHighlightingEffect(Button... buttons) {

        for(Button b : buttons){
            b.setOnMouseExited(evt -> b.setEffect(null));
            b.setOnMouseEntered(evt -> b.setEffect(GameConstants.HIGHLIGHT_BTN_EFFECT));
        }

    }

    private void showWinDialog(String winString, SceneState sceneState,double stars) throws IOException {
        Dialog<ButtonType> winDialog = new Dialog<>();
        winDialog.getDialogPane().setBackground(new Background(new BackgroundImage(new Image( "file:resources/images/background_tile.png" ), BackgroundRepeat.REPEAT,null, BackgroundPosition.CENTER, BackgroundSize.DEFAULT )));
        ImageView starsIV = new ImageView(Util.getStarImageFromDouble(stars));

        Label winLabel = new Label(winString);
        winLabel.setAlignment(Pos.CENTER);
        winLabel.setMinWidth(GameConstants.TEXTFIELD_WIDTH);
        winLabel.setStyle("-fx-text-fill: white;-fx-effect: dropshadow(three-pass-box, black, 10, 0.6, 0.6, 0);");
        winLabel.setTextAlignment(TextAlignment.CENTER);
        winLabel.setFont(GameConstants.BIGGEST_FONT);
        VBox contentVBox = new VBox(starsIV,winLabel);
        contentVBox.setAlignment(Pos.CENTER);
        winDialog.getDialogPane().setContent(contentVBox);
        ButtonType replayBtn = new ButtonType("Replay", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType nextBtn = new ButtonType("Next", ButtonBar.ButtonData.NEXT_FORWARD);
        ButtonType backBtn = new ButtonType("Back To Menu",ButtonBar.ButtonData.BACK_PREVIOUS);

//        ImageView resetBtnIV = new ImageView(GameConstants.RESET_BTN_IMAGE_PATH);
//        resetBtnIV.setScaleY(GameConstants.HEIGHT_RATIO);
//        resetBtnIV.setScaleX(GameConstants.WIDTH_RATIO);
//        ((Button)winDialog.getDialogPane().lookup("replayBtn")).setGraphic(resetBtnIV);

//        ImageView nextBtnIV = new ImageView(GameConstants.EXECUTE_BTN_IMAGE_PATH);
//        nextBtnIV.setScaleY(GameConstants.HEIGHT_RATIO);
//        nextBtnIV.setScaleX(GameConstants.WIDTH_RATIO);
//        ((Button)winDialog.getDialogPane().lookupButton(ButtonType.NEXT)).setGraphic(nextBtnIV);
//
//        ImageView backBtnIV = new ImageView(GameConstants.BACK_BTN_IMAGE_PATH);
//        backBtnIV.setScaleY(GameConstants.HEIGHT_RATIO);
//        backBtnIV.setScaleX(GameConstants.WIDTH_RATIO);
//        ((Button)winDialog.getDialogPane().lookupButton(ButtonType.PREVIOUS)).setGraphic(backBtnIV);

//        winDialog.setContentText(winString);
        winDialog.getDialogPane().getButtonTypes().addAll(backBtn,replayBtn);
        Level nextLvl = model.getLevelWithIndex(model.getCurrentLevel().getIndex()+1);
        if(nextLvl != null &&(sceneState != SceneState.TUTORIAL || nextLvl.isTutorial()) && sceneState != SceneState.LEVEL_EDITOR) {
            winDialog.getDialogPane().getButtonTypes().add(2, nextBtn);
        }
        if(sceneState == SceneState.TUTORIAL)
            JSONParser.saveTutorialProgress(model.getCurrentLevel().getIndex());

        Optional<ButtonType>  bnt = winDialog.showAndWait();
        if(bnt.isPresent()){
            switch (bnt.get().getButtonData()){
                case NEXT_FORWARD:
                    view.getBtnReset().fire();
                    model.selectLevel(nextLvl.getName());
                case CANCEL_CLOSE:
                    view.getBtnReset().fire();
                    break;
                case BACK_PREVIOUS:
                    view.getBtnReset().fire();
                    view.setSceneState(SceneState.START_SCREEN);
                    break;
            }
        }
    }

}
