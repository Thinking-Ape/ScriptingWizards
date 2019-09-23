package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import model.*;
import model.statement.ComplexStatement;
import model.util.GameConstants;
import parser.CodeParser;
import parser.JSONParser;
import view.SceneState;
import view.View;

import java.io.IOException;

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
        codeAreaController.setAllHandlersForCodeArea(false);
        view.getBackBtn().setOnAction(actionEvent -> {
            switch (view.getCurrentSceneState()){
                case LEVEL_EDITOR:
                    view.setSceneState(SceneState.START_SCREEN);
                    break;
                case LEVEL_SELECT:
                    throw new IllegalStateException("This should not have been possible!");
                case PLAY:
                    view.setSceneState(SceneState.LEVEL_SELECT);
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

        view.getLevelOverviewPane().getBackBtn().setOnAction(actionEvent -> {
            view.setSceneState(SceneState.START_SCREEN);
        });
        view.getStartScreen().getPlayBtn().setOnAction(actionEvent -> {
            view.setSceneState(SceneState.LEVEL_SELECT);
            view.getLevelOverviewPane().getPlayBtn().setOnAction(actionEvent1 -> {
                model.selectLevel(view.getLevelOverviewPane().getLevelListView().getSelectionModel().getSelectedItem().getLevelName());
                view.setSceneState(SceneState.PLAY);
            });
        });


//        view.getSpeedSlider().valueProperty().addListener(a -> {
//            model.getCurrentLevel().setSpeed(view.getSpeedSlider().getValue());
//        });
        view.getBtnExecute().setOnAction(actionEvent -> {
            view.getCodeArea().deselectAll();
            view.getCodeArea().setEditable(false);
            codeAreaController.setGameRunning(true);
            CodeParser codeParser = new CodeParser(view.getCodeArea().getAllText(),true);
            CodeParser aiCodeParser = null;
            if(view.getAICodeArea() != null) {
                view.getAICodeArea().setEditable(false);
                aiCodeParser = new CodeParser(view.getAICodeArea().getAllText(),false);
            }
            try {
                ComplexStatement behaviour = codeParser.parseProgramCode();
                ComplexStatement aiBehaviour = new ComplexStatement();
                if(aiCodeParser!=null)aiBehaviour = aiCodeParser.parseProgramCode();
                view.getLevelEditorModule().setDisableAllLevelBtns(true);
                //TODO: delete
                System.out.println(behaviour.print());
                System.out.println(aiBehaviour.print());
                if(view.getAICodeArea() != null)
                view.getAICodeArea().deselectAll();
                view.getCodeArea().deselectAll();
                model.getCurrentLevel().setPlayerBehaviour(behaviour);
                //TODO: model.getCurrentLevel().addListener(view);
                model.getCurrentLevel().setAiBehaviour(aiBehaviour);
//                model.getCurrentLevel().setCurrentMapToOriginal();
                timeline = new Timeline();

                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.getKeyFrames().addAll(new KeyFrame(Duration.seconds(GameConstants.TICK_SPEED*1/view.getSpeedSlider().getValue()), event ->
                {
                    try {
                        model.getCurrentLevel().executeTurn();
                        view.drawMap(model.getCurrentLevel().getCurrentMap());
                        if (model.getCurrentLevel().isWon()){
                            System.out.println("Success!");
                            int turns = model.getCurrentLevel().getTurnsTaken();
                            int loc = behaviour.getActualSize();
                            int turnStars = 1;
                            if(turns <= model.getCurrentLevel().getTurnsToStars()[0]) turnStars = 2;
                            if(turns <= model.getCurrentLevel().getTurnsToStars()[1]) turnStars = 3;
                            int locStars = 1;
                            if(loc <= model.getCurrentLevel().getLocToStars()[0]) locStars = 2;
                            if(loc <= model.getCurrentLevel().getLocToStars()[1]) locStars = 3;
                            double nStars = (turnStars + locStars)/2.0;
                            System.out.println("You earned " + (int)nStars + (Math.round(nStars)!=(int)nStars ? ".5" : "") + (nStars > 1 ? " Stars! (" : " Star! (")+turns + " Turns, " + loc + " Lines of Code)" );
                            //TODO: really not in editor??!
//                            if(view.getCurrentSceneState()==SceneState.PLAY){
                                JSONParser.storeProgressIfBetter(model.getCurrentLevel().getName(),turns,loc,model.getCurrentLevel().getPlayerBehaviour());
                                model.updateFinishedList();
//                            }
                            timeline.stop();
                            view.getLevelEditorModule().setDisableAllLevelBtns(false);
                        }
                        if (model.getCurrentLevel().isLost()){
                            System.out.println("You lost!");
                            timeline.stop();
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }));
                timeline.play();
                view.getBtnReset().setDisable(false);
                view.getBtnExecute().setDisable(true);
                editorController.setAllEditButtonsToDisable(true);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        view.getBtnReset().setOnAction(actionEvent -> {
            view.getLevelEditorModule().setDisableAllLevelBtns(false);
            view.getCodeArea().setEditable(true);
            if(view.getAICodeArea()!=null)view.getAICodeArea().setEditable(true);
            view.getCodeArea().select(0,true);
            codeAreaController.setGameRunning(false);
            timeline.stop();
            model.getCurrentLevel().reset();
//            view.notify(Event.MAP_CHANGED);
//            view.notify(Event.LEVEL_CHANGED);
            view.getBtnReset().setDisable(true);
            view.getBtnExecute().setDisable(false);
            editorController.setAllEditButtonsToDisable(false);
            editorController.setHandlersForMapCells();
//            setEditorHandlers();
        });

    }

}
