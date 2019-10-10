package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;
import model.*;
import model.statement.ComplexStatement;
import model.statement.SimpleStatement;
import utility.GameConstants;
import parser.CodeParser;
import parser.JSONParser;
import view.CodeArea;
import view.SceneState;
import view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                    view.getShowSpellBookBtn().setText("Show Spellbook");
                    view.setSceneState(SceneState.START_SCREEN);
                    break;
                case LEVEL_SELECT:
                    throw new IllegalStateException("This should not have been possible!");
                case PLAY:
                    view.setSceneState(SceneState.LEVEL_SELECT);
                    break;
                case TUTORIAL:
                    view.setSceneState(SceneState.START_SCREEN);
                    break;
                case START_SCREEN:
                    System.exit(0);
                    break;
            }
        });

        view.getStartScreen().getLvlEditorBtn().setOnAction(actionEvent -> {
            view.setSceneState(SceneState.LEVEL_EDITOR);
            editorController.setEditorHandlers();
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
                model.selectLevel(view.getLevelOverviewPane().getLevelListView().getSelectionModel().getSelectedItem().getLevelName());
                view.setSceneState(SceneState.PLAY);
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
                System.out.println(behaviour.print());
                System.out.println(aiBehaviour.print());
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
                            Platform.runLater(() ->new Alert(Alert.AlertType.NONE,"You have won!"+"\nYou earned "  + (int)nStars + (Math.round(nStars)!=(int)nStars ? ".5" : "") + (nStars > 1 ? " Stars! (" : " Star! (")+turns + " Turns, " + loc + " Lines of Code)", ButtonType.OK).showAndWait());
//                            if(view.getCurrentSceneState() == SceneState.LEVEL_EDITOR)view.getLevelEditorModule().setDisableAllLevelBtns(false);
                            if(view.getCurrentSceneState() == SceneState.TUTORIAL){
                                JSONParser.saveTutorialProgress(model.getCurrentLevel().getIndex());
                                Level l = model.getLevelWithIndex(model.getCurrentLevel().getIndex()+1);
                                if(l != null && l.isTutorial())
                                    model.selectLevel(l.getName());
                            }
                            if(view.getCurrentSceneState() == SceneState.PLAY){
                                Level l = model.getLevelWithIndex(model.getCurrentLevel().getIndex()+1);
                                if(l != null){
                                    model.selectLevel(l.getName());
                                    view.setNodesDisableWhenRunning(false);
                                    view.getCodeArea().setEditable(true);
                                    codeAreaController.setGameRunning(false);
                                }

                            }
                        }
                        if (model.getCurrentLevel().isLost()){
                            timeline.stop();
                            if(model.getCurrentLevel().isStackOverflow())
                                Platform.runLater(()->new Alert(Alert.AlertType.NONE,"You might have accidentally caused an endless loop! You are not allowed to use big loops without method calls!", ButtonType.OK).showAndWait());
                            else Platform.runLater(()->new Alert(Alert.AlertType.NONE,"You have lost!", ButtonType.OK).showAndWait());
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
            view.setNodesDisableWhenRunning(false);
            view.getCodeArea().select(0,true);
        });
        view.getLoadBestCodeBtn().setOnAction(actionEvent -> {
            List<String> bestCode = new ArrayList<>();
            try {
                bestCode = JSONParser.getBestCode(model.getCurrentLevel().getName());
//                if(bestCode.size() !=0)model.getCurrentLevel().setPlayerBehaviour(new CodeParser().parseProgramCode(bestCode));
                if(bestCode.size() !=0){
                    CodeArea codeArea = new CodeArea(new CodeParser().parseProgramCode(bestCode),false);
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
            CodeArea codeArea = new CodeArea(complexStatement,false);
            view.setCodeArea(codeArea,false);
//            codeArea.draw();
//            codeAreaController.setAllHandlersForCodeArea(false);

        });

    }

}
