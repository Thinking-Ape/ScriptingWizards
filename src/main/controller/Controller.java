package main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import main.model.Level;
import main.model.Model;
import main.model.statement.ComplexStatement;
import main.model.statement.SimpleStatement;
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

    public Controller(View view, Model model){
        this.model = model;
        this.view = view;
//        view.notify(Event.LEVEL_CHANGED); //TODO: better solution?

//        setActionHandlerForTextFields(view.getCodeBoxCompound().getVBox());
        CodeAreaController codeAreaController = new CodeAreaController(view,model);
        EditorController editorController = new EditorController(view,model,codeAreaController);
//        codeAreaController.setAllHandlersForCodeArea();
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
                view.getLevelOverviewPane().getBackBtn(),view.getLevelOverviewPane().getPlayBtn(),view.getTutorialLevelOverviewPane().getBackBtn(),view.getTutorialLevelOverviewPane().getPlayBtn());

        view.getBackBtn().setOnAction(actionEvent -> {
            boolean isVisible = view.getSpellBookPane().isVisible();
            if(isVisible)view.getShowSpellBookBtn().fire();
            switch (view.getCurrentSceneState()){
                case LEVEL_EDITOR:
                    if(codeAreaController.isGameRunning())view.getBtnReset().fire();
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
                    view.setSceneState(SceneState.START_SCREEN);
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
            int minIndex = 0;
            try {
                minIndex = JSONParser.getTutorialProgressIndex();
            } catch (IOException e) {
                e.printStackTrace();
            }
            int amountOfTuts = 0;
            for(Level l :model.getLevelListCopy()){
                if(l.isTutorial() && l.getIndex() < selectedLevel.getIndex() && l.getIndex() > minIndex)selectedLevel = l;
                if(l.isTutorial())amountOfTuts++;
            }
            model.selectLevel(selectedLevel.getName());
            if(minIndex == amountOfTuts-1){
                view.setSceneState(SceneState.TUTORIAL_LEVEL_SELECT);
                view.getTutorialLevelOverviewPane().getBackBtn().setOnAction(actionEvent2 ->
                    view.setSceneState(SceneState.START_SCREEN)
                );
                view.getTutorialLevelOverviewPane().getPlayBtn().setOnAction(actionEvent1 -> {
                    String levelName = view.getTutorialLevelOverviewPane().getLevelListView().getSelectionModel().getSelectedItem().getLevelName();
                    model.selectLevel(levelName);
                    view.setSceneState(SceneState.PLAY);
                });
            }
            else view.setSceneState(SceneState.TUTORIAL);
//            view.getIntroductionPane().getStartTutorialBtn().setOnAction(evt -> {
//                view.getStage().getScene().setRoot(view.getRootPane());
//            });
//            view.getIntroductionPane().getBackBtn().setOnAction(evt -> {
//                view.setSceneState(SceneState.START_SCREEN);
//            });
            view.getTutorialGroup().getNextBtn().setOnAction(evt -> {view.getTutorialGroup().next();
                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
                ScrollPane sp = (ScrollPane)view.getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
                if(view.getTutorialGroup().isLastMsg()&&view.isIntroduction())view.getTutorialGroup().getEndIntroductionBtn().setVisible(true);
                view.getTutorialGroup().getEndIntroductionBtn().setOnAction(event-> {
                    view.leaveInstructions();
                });
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }});
            view.getTutorialGroup().getHideBtn().setOnAction(evt -> {
                view.getTutorialGroup().toggleStackpaneVisibility();
            });
            view.getTutorialGroup().getPrevBtn().setOnAction(evt -> {view.getTutorialGroup().prev();
                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
                ScrollPane sp = (ScrollPane)view.getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }});
//            view.getIntroductionPane().getTutorialGroup().getNextBtn().setOnAction(evt -> {
//                view.getIntroductionPane().getTutorialGroup().next();
//                if(view.getIntroductionPane().getTutorialGroup().isLastMsg())view.getIntroductionPane().getStartTutorialBtn().setDisable(false);
//                if(view.)
//                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
//                ScrollPane sp = (ScrollPane)view.getIntroductionPane().getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
//                sp.setCache(false);
//                for (Node n : sp.getChildrenUnmodifiable()) {
//                    n.setCache(false);
//                }
//            });
//            view.getIntroductionPane().getTutorialGroup().getPrevBtn().setOnAction(evt -> {
//                view.getIntroductionPane().getTutorialGroup().prev();
//                view.getIntroductionPane().getStartTutorialBtn().setDisable(true);
//                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
//                ScrollPane sp = (ScrollPane)view.getIntroductionPane().getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
//                sp.setCache(false);
//                for (Node n : sp.getChildrenUnmodifiable()) {
//                    n.setCache(false);
//                }
//            });
        });


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

                            double nStars = Util.calculateStars(turns,loc,model.getCurrentLevel().getTurnsToStars(),model.getCurrentLevel().getLocToStars());
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
                                    showWinDialog(winString,view.getCurrentSceneState(),nStars);
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
                                alert = new Alert(Alert.AlertType.NONE,"", ButtonType.OK);
                                alert.getDialogPane().setBackground(new Background(new BackgroundImage(new Image( "file:resources/images/background_tile.png" ), BackgroundRepeat.REPEAT,null, BackgroundPosition.CENTER, BackgroundSize.DEFAULT )));
                                Label lostLabel = new Label("You have lost!");
                                lostLabel.setAlignment(Pos.CENTER);
                                lostLabel.setMinWidth(GameConstants.TEXTFIELD_WIDTH);
                                lostLabel.setStyle("-fx-text-fill: white;-fx-effect: dropshadow(three-pass-box, black, 10, 0.6, 0.6, 0);");
                                alert.getDialogPane().setContent(lostLabel);
                                lostLabel.setFont(GameConstants.BIGGEST_FONT);
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
                    view.getBtnExecute().setDisable(false);
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

    private void addHighlightingEffect(Button... buttons) {

        for(Button b : buttons){
            b.setOnMouseExited(evt -> b.setEffect(null));
            b.setOnMouseEntered(evt -> b.setEffect(GameConstants.HIGHLIGHT_BTN_EFFECT));
        }

    }

    private void showWinDialog(String winString, SceneState sceneState,double stars) throws IOException {
        Dialog<ButtonType> winDialog = new Dialog<>();
        winDialog.getDialogPane().setBackground(new Background(new BackgroundImage(new Image( "file:resources/images/background_tile.png" ), BackgroundRepeat.REPEAT,null, BackgroundPosition.CENTER, BackgroundSize.DEFAULT )));
        ImageView starsIV = new ImageView();
        if(stars==1){
            starsIV.setImage(new Image("file:"+GameConstants.IMAGES_PATH+"1StarRating.png"));
        }
        else if(stars==1.5){
            starsIV.setImage(new Image("file:"+GameConstants.IMAGES_PATH+"1_5StarRating.png"));
        }
        else if(stars==2){
            starsIV.setImage(new Image("file:"+GameConstants.IMAGES_PATH+"2StarRating.png"));
        }
        else if(stars==2.5){
            starsIV.setImage(new Image("file:"+GameConstants.IMAGES_PATH+"2_5StarRating.png"));
        }
        else{
            starsIV.setImage(new Image("file:"+GameConstants.IMAGES_PATH+"3StarRating.png"));
        }
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
