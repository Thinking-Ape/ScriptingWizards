package main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.util.Duration;
import main.model.Level;
import main.model.Model;
import main.model.statement.ComplexStatement;
import main.model.statement.SimpleStatement;
import main.utility.GameConstants;
import main.parser.CodeParser;
import main.parser.JSONParser;
import main.view.CodeArea;
import main.view.SceneState;
import main.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Controller {
    private Model model;
    private View view;
    private Timeline timeline;

    public Controller(View view, Model model){
        this.model = model;
        this.view = view;
//        view.notify(Event.LEVEL_CHANGED); //TODO: better solution?

//        setActionHandlerForTextFields(view.getCodeBoxCompound().getVBox());
        CodeAreaController codeAreaController = new CodeAreaController(view,model);
        EditorController editorController = new EditorController(view,model,codeAreaController);
//        codeAreaController.setAllHandlersForCodeArea();
        view.getShowSpellBookBtn().setOnAction(evt -> {
            view.toggleShowSpellBook();
            boolean isVisible = view.getSpellBookPane().isVisible();
            view.getActualMapGPane().setMouseTransparent(isVisible);
            if(view.getCurrentSceneState() == SceneState.LEVEL_EDITOR){
                view.getCellItemSelectionPane().setMouseTransparent(isVisible);
                view.getCellTypeSelectionPane().setMouseTransparent(isVisible);
                view.getLevelEditorModule().getBottomHBox().setMouseTransparent(isVisible);
            }
            view.getBtnExecute().setMouseTransparent(isVisible);
            view.getBtnReset().setMouseTransparent(isVisible);
            view.getSpeedSlider().setMouseTransparent(isVisible);

        });
        view.getBackBtn().setOnAction(actionEvent -> {
            switch (view.getCurrentSceneState()){
                case LEVEL_EDITOR:
                    if(codeAreaController.isGameRunning())view.getBtnReset().fire();
                    view.getShowSpellBookBtn().setText("Show Spellbook");
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
                    view.setSceneState(SceneState.START_SCREEN);
                    break;
                case START_SCREEN:
                    System.exit(0);
                    break;
            }
        });

        view.getStartScreen().getLvlEditorBtn().setOnAction(actionEvent -> {
            view.setSceneState(SceneState.LEVEL_EDITOR);
            view.drawMap(model.getCurrentLevel().getOriginalMap());
            editorController.setEditorHandlers();
            Platform.runLater(()-> view.highlightInMap(view.getSelectedPointList()));
        });

        view.getStartScreen().getExitBtn().setOnAction(actionEvent -> {
            System.exit(0);
        });

        view.getStartScreen().getTutorialBtn().setOnAction(actionEvent -> {
            Level selectedLevel = model.getCurrentLevel();
            int minIndex = 0;
            try {
                minIndex = JSONParser.getTutorialProgressIndex();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for(Level l :model.getLevelListCopy()){
                if(l.isTutorial() && l.getIndex() < selectedLevel.getIndex() && l.getIndex() > minIndex)selectedLevel = l;
            }
            model.selectLevel(selectedLevel.getName());
            view.drawMap(model.getCurrentLevel().getOriginalMap());
            view.setSceneState(SceneState.TUTORIAL);
            view.getIntroductionPane().getStartTutorialBtn().setOnAction(evt -> {
                view.getStage().getScene().setRoot(view.getRootPane());
            });
            view.getIntroductionPane().getBackBtn().setOnAction(evt -> {
                view.setSceneState(SceneState.START_SCREEN);
            });
            view.getTutorialGroup().getNextBtn().setOnAction(evt -> {view.getTutorialGroup().next();
                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
                ScrollPane sp = (ScrollPane)view.getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }});
            view.getTutorialGroup().getPrevBtn().setOnAction(evt -> {view.getTutorialGroup().prev();
                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
                ScrollPane sp = (ScrollPane)view.getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }});
            view.getIntroductionPane().getTutorialGroup().getNextBtn().setOnAction(evt -> {
                view.getIntroductionPane().getTutorialGroup().next();
                if(view.getIntroductionPane().getTutorialGroup().isLastMsg())view.getIntroductionPane().getStartTutorialBtn().setDisable(false);
                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
                ScrollPane sp = (ScrollPane)view.getIntroductionPane().getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }
            });
            view.getIntroductionPane().getTutorialGroup().getPrevBtn().setOnAction(evt -> {
                view.getIntroductionPane().getTutorialGroup().prev();
                view.getIntroductionPane().getStartTutorialBtn().setDisable(true);
                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
                ScrollPane sp = (ScrollPane)view.getIntroductionPane().getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }
            });
        });

        view.getLevelOverviewPane().getBackBtn().setOnAction(actionEvent ->
            view.setSceneState(SceneState.START_SCREEN)
        );
        view.getStartScreen().getPlayBtn().setOnAction(actionEvent -> {
            view.setSceneState(SceneState.LEVEL_SELECT);
            view.getLevelOverviewPane().getPlayBtn().setOnAction(actionEvent1 -> {
                String levelName = view.getLevelOverviewPane().getLevelListView().getSelectionModel().getSelectedItem().getLevelName();
                view.setSceneState(SceneState.PLAY);
                model.selectLevel(levelName);
                view.drawMap(model.getCurrentLevel().getOriginalMap());
            });
        });


        view.getBtnExecute().setOnAction(actionEvent -> {

            CodeParser codeParser = new CodeParser(view.getCodeArea().getAllText(),true);
            CodeParser aiCodeParser = new CodeParser(view.getAICodeArea().getAllText(),false);
            try {
                ComplexStatement behaviour = codeParser.parseProgramCode();
                ComplexStatement aiBehaviour = new ComplexStatement();
                if(model.getCurrentLevel().hasAi())aiBehaviour = aiCodeParser.parseProgramCode();
                if(view.getCurrentSceneState()==SceneState.LEVEL_EDITOR) view.getLevelEditorModule().setDisableAllLevelBtns(true);
                //TODO: delete
                if(GameConstants.DEBUG)System.out.println(behaviour.print());
                if(GameConstants.DEBUG)System.out.println(aiBehaviour.print());
                model.getCurrentLevel().setPlayerBehaviour(behaviour);
                //TODO: model.getCurrentLevel().addListener(view);
                model.getCurrentLevel().setAiBehaviour(aiBehaviour);
                view.setNodesDisableWhenRunning(true);
                codeAreaController.setGameRunning(true);
//                model.getCurrentLevel().setCurrentMapToOriginal();
                timeline = new Timeline();

                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.getKeyFrames().addAll(new KeyFrame(Duration.seconds(GameConstants.TICK_SPEED*1/view.getSpeedSlider().getValue()), event ->
                {
                    try {
                        model.getCurrentLevel().executeTurn();
                        view.drawMap(model.getCurrentLevel().getCurrentMap());
                        view.deselect();
                        if (model.getCurrentLevel().isWon()){
                            int turns = model.getCurrentLevel().getTurnsTaken();
                            int loc = behaviour.getActualSize();
                            int turnStars = 1;
                            if(turns <= model.getCurrentLevel().getTurnsToStars()[0]) turnStars = 2;
                            if(turns <= model.getCurrentLevel().getTurnsToStars()[1]) turnStars = 3;
                            int locStars = 1;
                            if(loc <= model.getCurrentLevel().getLocToStars()[0]) locStars = 2;
                            if(loc <= model.getCurrentLevel().getLocToStars()[1]) locStars = 3;
                            double nStars = (turnStars + locStars)/2.0;
                            //TODO: really not in editor??!
//                            if(view.getCurrentSceneState()==SceneState.PLAY){
                                JSONParser.storeProgressIfBetter(model.getCurrentLevel().getName(),turns,loc,model.getCurrentLevel().getPlayerBehaviour());
                                model.updateFinishedList();
//                            }
                            timeline.stop();
                            String winString = "You have won!"+"\nYou earned "  + (int)nStars + (Math.round(nStars)!=(int)nStars ? ".5" : "") + (nStars > 1 ? " Stars! (" : " Star! (")+turns + " Turns, " + loc + " Lines of Code)";
//                            Platform.runLater(() ->new Alert(Alert.AlertType.NONE,winString, ButtonType.OK).showAndWait());

                            Platform.runLater(() -> {
                                try {
                                    showWinDialog(winString,view.getCurrentSceneState());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                            codeAreaController.setGameRunning(false);
//                            if(view.getCurrentSceneState() != SceneState.LEVEL_EDITOR)
                                JSONParser.saveStatementProgress(model.getCurrentLevel().getUnlockedStatementList());
                            view.getSpellBookPane().updateSpellbookEntries(model.getCurrentLevel().getUnlockedStatementList());

                        }
                        if (model.getCurrentLevel().isLost()){
                            timeline.stop();
                            Alert alert;
                            if(model.getCurrentLevel().isStackOverflow()){
                                alert = new Alert(Alert.AlertType.NONE,"You might have accidentally caused an endless loop! You are not allowed to use big loops without method calls!", ButtonType.OK);
                                Platform.runLater(() -> {
                                    Optional<ButtonType> result = alert.showAndWait();
                                    if(result.isPresent()){
                                        view.getBtnReset().fire();
                                    }
                                });
                            }
                            else{
                                alert = new Alert(Alert.AlertType.NONE,"You have lost!", ButtonType.OK);
                                Platform.runLater(() -> {
                                    Optional<ButtonType> result = alert.showAndWait();
                                    if(result.isPresent()){
                                        view.getBtnReset().fire();
                                    }
                                });
                            }
                        }
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
            }
        });
        view.getBtnReset().setOnAction(actionEvent -> {
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
            List<String> bestCode = new ArrayList<>();
            try {
                bestCode = JSONParser.getBestCode(model.getCurrentLevel().getName());
//                if(bestCode.size() !=0)model.getCurrentLevel().setPlayerBehaviour(new CodeParser().parseProgramCode(bestCode));
                if(bestCode.size() !=0){
                    CodeArea codeArea = new CodeArea(new CodeParser().parseProgramCode(bestCode),true,false);
                    view.setCodeArea(codeArea,false);
//                    codeArea.draw();
//                    codeAreaController.setAllHandlersForCodeArea(false);
                }
                else new Alert(Alert.AlertType.NONE,"No best code stored!",ButtonType.OK).showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        view.getClearCodeBtn().setOnAction(actionEvent -> {
            ComplexStatement complexStatement = new ComplexStatement();
            complexStatement.addSubStatement(new SimpleStatement());
            CodeArea codeArea = new CodeArea(complexStatement,true,false);
            view.setCodeArea(codeArea,false);
//            codeArea.draw();
//            codeAreaController.setAllHandlersForCodeArea(false);

        });

    }

    private void showWinDialog(String winString, SceneState sceneState) throws IOException {
        Dialog<ButtonType> winDialog = new Dialog<>();
        ButtonType replayBtn = new ButtonType("Replay", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType nextBtn = new ButtonType("Next", ButtonBar.ButtonData.NEXT_FORWARD);
        ButtonType backBtn = new ButtonType("Back To Menu",ButtonBar.ButtonData.BACK_PREVIOUS);

        winDialog.setContentText(winString);
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
