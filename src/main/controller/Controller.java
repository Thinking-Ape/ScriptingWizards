package main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import main.model.LevelDataType;
import main.model.Model;
import main.model.statement.ComplexStatement;
import main.model.statement.Statement;
import main.parser.JSONParser;
import main.parser.CodeParser;
import main.model.GameConstants;
import main.utility.Point;
import main.utility.Util;
import main.view.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Controller {
    private static Controller single_Instance = null;
    private boolean isGameRunning = false;
    private double mouse_PositionX;
    private double mouse_PositionY;
    private View view;
    private Model model;
    private Timeline timeline;
    private EditorController editorController;
    private int minIndex = 0;

    private Controller(View view,Model model) {
        this.view = view;
        this.model = model;
        new CodeAreaController(view,model);
        editorController = new EditorController(view,model);
        // If DEBUG is disabled the LevelEditor Button is only available if all Levels have been unlocked
        if(model.getAmountOfCompletedLevels() < model.getAmountOfLevels())view.getStartScreen().getLvlEditorBtn().setDisable(!(GameConstants.DEBUG || model.isEditorUnlocked()));
        // Key Input should automatically be passed on to player CodeArea to make it easier to use
        view.getStage().getScene().setOnKeyPressed(event -> {
            if (!(view.getStage().getScene().getFocusOwner() instanceof CodeField)) {
                if (view.getCodeArea().getSelectedCodeField() == null)
                    view.getCodeArea().select(0, Selection.END);
                view.getCodeArea().getSelectedCodeField().requestFocus();

            }
        });
        view.getShowSpellBookBtn().setOnAction(evt -> view.toggleShowSpellBook());
        view.getSpellBookPane().getCloseBtn().setOnAction(evt -> view.toggleShowSpellBook());

        view.getSpellBookPane().getShowShortcutsBtn().setOnAction(evt -> {
            Alert a = new Alert(Alert.AlertType.NONE, GameConstants.SHORTCUT_INFORMATION, ButtonType.CLOSE);
            double widthFactor = 1.75;
            a.setWidth(GameConstants.TEXTFIELD_WIDTH * widthFactor);
            a.getDialogPane().setMinWidth(GameConstants.TEXTFIELD_WIDTH * widthFactor);
            a.showAndWait();
        });
        view.getSpellBookPane().getMoveBtn().setOnMouseDragged(evt -> {
            Button moveBtn = view.getSpellBookPane().getMoveBtn();
            view.getSpellBookPane().setTranslateX(view.getSpellBookPane().getTranslateX() + evt.getX() - mouse_PositionX - moveBtn.getWidth() / 2);
            view.getSpellBookPane().setTranslateY(view.getSpellBookPane().getTranslateY() + evt.getY() - mouse_PositionY - moveBtn.getHeight() / 2);
        });

        view.getSpellBookPane().getMoveBtn().setOnMouseClicked(evt -> {
            mouse_PositionX = evt.getX();
            mouse_PositionY = evt.getY();
        });
        addHighlightingEffect(view.getShowSpellBookBtn(), view.getBackBtn(), view.getBtnExecute(), view.getBtnReset(), view.getTutorialGroup().getEndIntroductionBtn(),
                view.getLevelOverviewPane().getBackBtn(), view.getLevelOverviewPane().getPlayBtn(), view.getTutorialLevelOverviewPane().getBackBtn(),
                view.getTutorialLevelOverviewPane().getPlayBtn(), view.getTutorialGroup().getNextBtn(), view.getTutorialGroup().getPrevBtn());

        view.getSpeedSlider().valueProperty().addListener((observableValue, s, t1) -> {
            if (timeline != null)
                timeline.setRate(GameConstants.TICK_SPEED * t1.doubleValue());
        });

        view.getBackBtn().setOnAction(actionEvent -> {
            boolean isVisible = view.getSpellBookPane().isVisible();
            if (isVisible) view.getShowSpellBookBtn().fire();
            switch (View.getCurrentSceneState()) {
                case LEVEL_EDITOR:
                    if (model.currentLevelHasChanged()) editorController.showSavingDialog();
                    if (isGameRunning) view.getBtnReset().fire();
                    view.setSceneState(SceneState.START_SCREEN);
                    break;
                case TUTORIAL_LEVEL_SELECT:
                    view.setSceneState(SceneState.START_SCREEN);
                    break;
                case LEVEL_SELECT:
                    throw new IllegalStateException("This should not have been possible!");
                case PLAY:

                    if (isGameRunning) view.getBtnReset().fire();
                    view.setSceneState(SceneState.LEVEL_SELECT);

                    break;
                case TUTORIAL:
                    if (isGameRunning) view.getBtnReset().fire();
                    if (model.getTutorialProgress() == -1) view.setSceneState(SceneState.START_SCREEN);
                    else view.setSceneState(SceneState.TUTORIAL_LEVEL_SELECT);
                    view.getBtnExecute().setMouseTransparent(false);
                    view.getSpeedSlider().setMouseTransparent(false);
                    view.getShowSpellBookBtn().setMouseTransparent(false);
                    view.getCodeArea().setDisable(false);
                    view.getStage().requestFocus();
                    break;
                case START_SCREEN:
                    System.exit(0);
                    break;
            }
        });

        view.getStartScreen().getLvlEditorBtn().setOnAction(actionEvent -> {
//            model.resetTutorialIndex();
            view.setSceneState(SceneState.LEVEL_EDITOR);
            editorController.setEditorHandlers();
            Platform.runLater(() -> view.highlightInMap(view.getSelectedPointList()));
        });

        view.getStartScreen().getExitBtn().setOnMouseEntered(event -> {
            ImageView quitImageView = new ImageView(new Image(GameConstants.QUIT_BTN_ACTIVATED_PATH));
            quitImageView.setPreserveRatio(false);
            quitImageView.setFitWidth(quitImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
            quitImageView.setFitHeight(quitImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
            quitImageView.autosize();
            view.getStartScreen().getExitBtn().setGraphic(quitImageView);
        });
        view.getStartScreen().getExitBtn().setOnMouseExited(event -> {
            ImageView quitImageView = new ImageView(new Image(GameConstants.QUIT_BTN_PATH));
            quitImageView.setPreserveRatio(false);
            quitImageView.setFitWidth(quitImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
            quitImageView.setFitHeight(quitImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
            quitImageView.autosize();
            view.getStartScreen().getExitBtn().setGraphic(quitImageView);
        });

        view.getStartScreen().getPlayBtn().setOnMouseEntered(event -> {
            ImageView playImageView = new ImageView(new Image(GameConstants.CHALLENGES_BTN_ACTIVATED_PATH));
            playImageView.setPreserveRatio(false);
            playImageView.setFitWidth(playImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
            playImageView.setFitHeight(playImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
            playImageView.autosize();
            view.getStartScreen().getPlayBtn().setGraphic(playImageView);
        });
        view.getStartScreen().getPlayBtn().setOnMouseExited(event -> {
            ImageView playImageView = new ImageView(new Image(GameConstants.CHALLENGES_BTN_PATH));
            playImageView.setPreserveRatio(false);
            playImageView.setFitWidth(playImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
            playImageView.setFitHeight(playImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
            playImageView.autosize();
            view.getStartScreen().getPlayBtn().setGraphic(playImageView);
        });

        view.getStartScreen().getLvlEditorBtn().setOnMouseEntered(event -> {
            ImageView editorImageView = new ImageView(new Image(GameConstants.LVL_EDITOR_BTN_ACTIVATED_PATH));
            editorImageView.setPreserveRatio(false);
            editorImageView.setFitWidth(editorImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
            editorImageView.setFitHeight(editorImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
            editorImageView.autosize();
            view.getStartScreen().getLvlEditorBtn().setGraphic(editorImageView);
        });
        view.getStartScreen().getLvlEditorBtn().setOnMouseExited(event -> {
            ImageView editorImageView = new ImageView(new Image(GameConstants.LVL_EDITOR_BTN_PATH));
            editorImageView.setPreserveRatio(false);
            editorImageView.setFitWidth(editorImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
            editorImageView.setFitHeight(editorImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
            editorImageView.autosize();
            view.getStartScreen().getLvlEditorBtn().setGraphic(editorImageView);
        });
        view.getStartScreen().getTutorialBtn().setOnMouseEntered(event -> {
            ImageView tutorialImageView = new ImageView(new Image(GameConstants.TUTORIAL_BTN_ACTIVATED_PATH));
            tutorialImageView.setPreserveRatio(false);
            tutorialImageView.setFitWidth(tutorialImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
            tutorialImageView.setFitHeight(tutorialImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
            tutorialImageView.autosize();
            view.getStartScreen().getTutorialBtn().setGraphic(tutorialImageView);

        });
        view.getStartScreen().getTutorialBtn().setOnMouseExited(event -> {
            ImageView tutorialImageView = new ImageView(new Image(GameConstants.TUTORIAL_BTN_PATH));
            tutorialImageView.setPreserveRatio(false);
            tutorialImageView.setFitWidth(tutorialImageView.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
            tutorialImageView.setFitHeight(tutorialImageView.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
            tutorialImageView.autosize();
            view.getStartScreen().getTutorialBtn().setGraphic(tutorialImageView);
        });
        view.getStartScreen().getExitBtn().setOnAction(actionEvent -> {
            JSONParser.storeAllData();
            try {
                CodeParser.parseProgramCode(view.getCodeArea().getAllCode());
                JSONParser.storeCode(Util.trimStringList(view.getCodeArea().getAllCode()));
            } catch (Exception e) {
                System.out.println("Could not store current code as it contains errors!");
            }
            System.exit(0);
        });
        view.getStage().getScene().setOnKeyPressed(evt -> {
            evt.consume();
            if (View.getCurrentSceneState() != SceneState.TUTORIAL) return;
            if (evt.getCode() == KeyCode.RIGHT && evt.isAltDown()) {
                if (!view.getTutorialGroup().getNextBtn().isDisabled()) view.getTutorialGroup().getNextBtn().fire();
            } else if (evt.getCode() == KeyCode.LEFT && evt.isAltDown()) {
                if (!view.getTutorialGroup().getPrevBtn().isDisabled()) view.getTutorialGroup().getPrevBtn().fire();
            }
        });
        view.getStartScreen().getTutorialBtn().setOnAction(actionEvent -> {
            model.resetTutorialIndex();
            if (model.getTutorialProgress() != -1) {
                view.setSceneState(SceneState.TUTORIAL_LEVEL_SELECT);

            } else {
                view.setSceneState(SceneState.TUTORIAL);
                model.selectLevel(0);
            }
            view.getTutorialLevelOverviewPane().getBackBtn().setOnAction(actionEvent2 ->
                    view.setSceneState(SceneState.START_SCREEN)
            );
            view.getTutorialLevelOverviewPane().getPlayBtn().setOnAction(actionEvent1 -> {
                model.resetTutorialIndex();
                String levelName = view.getTutorialLevelOverviewPane().getLevelListView().getSelectionModel().getSelectedItem().getLevelName();
                view.setSceneState(SceneState.TUTORIAL);
                model.selectLevel(levelName);
            });

            view.getTutorialGroup().getNextBtn().setOnAction(evt -> {
                view.getTutorialGroup().next();
                if (view.isIntroduction()) {
                    view.highlightButtons();
                }
                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
                ScrollPane sp = (ScrollPane) view.getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
                if (view.getTutorialGroup().isLastMsg() && view.isIntroduction())
                    view.getTutorialGroup().getEndIntroductionBtn().setVisible(true);
                view.getTutorialGroup().getEndIntroductionBtn().setOnAction(event -> {
                    view.leaveIntroductions();
                    view.getCodeArea().setEffect(null);
                });
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }
            });
            view.getTutorialGroup().getHideBtn().setOnAction(evt -> {
                view.getTutorialGroup().toggleStackpaneVisibility();

            });
            view.getTutorialGroup().getPrevBtn().setOnAction(evt -> {
                view.getTutorialGroup().prev();
                if (view.isIntroduction()) {
                    view.highlightButtons();
                }
                //ANOTHER WEIRD WORKAROUND FOR BLURRY TEXT -> JAVAFX IS FULL OF BUGS!!
                ScrollPane sp = (ScrollPane) view.getTutorialGroup().getCurrentTutorialMessage().getChildrenUnmodifiable().get(0);
                sp.setCache(false);
                for (Node n : sp.getChildrenUnmodifiable()) {
                    n.setCache(false);
                }
            });
        });

        if (view.getLevelOverviewPane().getLevelListView().getItems().size() == 0)
            view.getStartScreen().getPlayBtn().setDisable(true);
        view.getStartScreen().getPlayBtn().setOnAction(actionEvent -> {
            view.setSceneState(SceneState.LEVEL_SELECT);
            view.getLevelOverviewPane().getBackBtn().setOnAction(actionEvent2 ->
                    view.setSceneState(SceneState.START_SCREEN)
            );
            view.getLevelOverviewPane().getPlayBtn().setOnAction(actionEvent1 -> {
                String levelName = view.getLevelOverviewPane().getLevelListView().getSelectionModel().getSelectedItem().getLevelName();

                view.setSceneState(SceneState.PLAY);
                model.selectLevel(levelName);
            });
        });


        setExecuteHandler();

        view.getBtnReset().setOnAction(actionEvent -> {
            setExecuteHandler();

            view.getCodeArea().highlightCodeField(-1);
            boolean hasAi = (boolean) model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
            if (hasAi) view.getAiCodeArea().highlightCodeField(-1);
            view.setNodesDisableWhenRunning(false);
            if (View.getCurrentSceneState() == SceneState.LEVEL_EDITOR) {
                view.getLevelEditorModule().setDisableAllEditorBtns(false);
                if (hasAi) view.getAiCodeArea().setEditable(true);
                editorController.setAllEditButtonsToDisable(false);
                editorController.setHandlersForMapCells();
                view.highlightInMap(List.of(new Point(0,0)));
            }
            isGameRunning = false;
            timeline.stop();
            model.reset();
            view.drawMap(model.getCurrentMapCopy());
            view.getCodeArea().setEditable(true);
            view.getCodeArea().select(0, Selection.END);
        });
        view.getLoadBestCodeBtn().setOnAction(actionEvent -> {
            List<String> bestCode;

            boolean noCode = view.getCodeArea().getAllCode().size() == 1 && view.getCodeArea().getAllCode().get(0).equals("");
            Alert alert = new Alert(Alert.AlertType.NONE, "Do you really want to overwrite the current code?", ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = noCode ? null : alert.showAndWait();
            if (noCode || (result.isPresent() && result.get() == ButtonType.OK)) {
                bestCode = model.getCurrentlyBestCode();
                if (bestCode.size() != 0) {
                    view.getCodeArea().updateCodeFields(CodeParser.parseProgramCode(bestCode));
                    view.getBtnExecute().setDisable(false);
                    view.getStoreCodeBtn().setDisable(false);
                } else new Alert(Alert.AlertType.NONE, "No best code stored!", ButtonType.OK).showAndWait();
            }

        });

        view.getClearCodeBtn().setOnAction(actionEvent -> {
            showDialogToClearCode(view.getCodeArea());
        });

        view.getClearAICodeBtn().setOnAction(actionEvent -> {
            showDialogToClearCode(view.getAiCodeArea());
        });

    }

    public static Controller getInstance(View view, Model model) {
        if (single_Instance == null) single_Instance = new Controller(view,model);
        return single_Instance;
    }

    private void showDialogToClearCode(CodeArea codeArea) {
        Dialog<ButtonType> deleteDialog = new Dialog<>();
        Label deleteLabel = new Label("You are going to remove all code! Are you sure?");
        deleteLabel.setAlignment(Pos.CENTER);
        deleteLabel.setMinWidth(GameConstants.TEXTFIELD_WIDTH);
        deleteLabel.setTextAlignment(TextAlignment.CENTER);


        deleteDialog.getDialogPane().setContent(deleteLabel);
        ButtonType noBtn = new ButtonType("NO", ButtonBar.ButtonData.NO);
        ButtonType yesBtn = new ButtonType("YES", ButtonBar.ButtonData.YES);
        deleteDialog.getDialogPane().getButtonTypes().addAll(noBtn, yesBtn);
        Optional<ButtonType> result = deleteDialog.showAndWait();
        if (result.isPresent()) {
            if (result.get().getButtonData() == ButtonBar.ButtonData.YES) {
                codeArea.clear();
            }
        }
    }

    private void setExecuteHandler() {

        ((ImageView) view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.EXECUTE_BTN_IMAGE_PATH));
        view.getBtnExecute().setOnAction(actionEvent -> {
            final boolean hasAi = (boolean) model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
            if (hasAi) view.getAiCodeArea().scrollTo(0);
            view.getCodeArea().scrollTo(0);
            List<String> playerCode = view.getCodeArea().getAllCode();
            ComplexStatement behaviour = CodeParser.parseProgramCode(playerCode, CodeAreaType.PLAYER);
            ComplexStatement aiBehaviour = new ComplexStatement();
            if (hasAi) {
                List<String> aiCode = view.getAiCodeArea().getAllCode();
                aiBehaviour = CodeParser.parseProgramCode(aiCode, CodeAreaType.AI);
            }
            if (View.getCurrentSceneState() == SceneState.LEVEL_EDITOR)
                view.getLevelEditorModule().setDisableAllEditorBtns(true);
            if (GameConstants.DEBUG) System.out.println(behaviour.getAllText());
            if (GameConstants.DEBUG) System.out.println(aiBehaviour.getAllText());
            model.setCurrentPlayerBehaviour(behaviour);
            model.initIteratorsAndEvaluators(behaviour,aiBehaviour);
            view.setNodesDisableWhenRunning(true);
            isGameRunning = true;
            view.getBtnExecute().setDisable(false);

            view.getSpeedSlider().setDisable(false);

            ((ImageView) view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.PAUSE_BTN_IMAGE_PATH));
            setPauseAndRunHandler();
            List<Integer> turnsList = new ArrayList<>();
            List<Integer> locList = new ArrayList<>();
            timeline = new Timeline();

            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.setRate(GameConstants.TICK_SPEED * view.getSpeedSlider().getValue());
            view.deselect();
            final ComplexStatement aiB = aiBehaviour;
            model.getCurrentMapCopy().getAndResetChangedPointList();
            timeline.getKeyFrames().addAll(new KeyFrame(Duration.seconds(GameConstants.KEYFRAME_DURATION), event ->
            {
                Statement[] executedStatements = model.executeTurn();
                int index = behaviour.findIndexOf(executedStatements[0], 0);
                if (index != -1) view.getCodeArea().highlightCodeField(index);
                if (hasAi) {
                    int index2 = aiB.findIndexOf(executedStatements[1], 0);

                    if (index2 != -1) view.getAiCodeArea().highlightCodeField(index2);
                }
                view.drawAllChangedCells();
                if (model.isLost()) {
                    view.getCodeArea().highlightCodeField(-1);
                }
                if (model.aiIsFinished()) {
                    view.getAiCodeArea().highlightCodeField(-1);
                }
                if (model.isWon()) {
                    int turns = model.getTurnsTaken();
                    int loc = behaviour.getActualSize();
                    turnsList.add(turns);
                    locList.add(loc);
                    if((int)model.getDataFromCurrentLevel((LevelDataType.AMOUNT_OF_RERUNS))>model.getCurrentRound()){
                        model.increaseCurrentRound();
                        model.resetForNextRound();
                        model.setCurrentPlayerBehaviour(behaviour);
                        model.initIteratorsAndEvaluators(behaviour,aiB);
                        view.drawMap(model.getCurrentMapCopy());
                    }else{
                        turns = Util.avgOfIntList(turnsList);
                        loc = Util.avgOfIntList(locList);
                    Integer[] turnsToStars = (Integer[]) model.getDataFromCurrentLevel(LevelDataType.TURNS_TO_STARS);
                    Integer[] locToStars = (Integer[]) model.getDataFromCurrentLevel(LevelDataType.LOC_TO_STARS);
                    double nStars = Util.calculateStars(turns, loc, turnsToStars, locToStars);
                    boolean newResultIsBetter = false;
                    if (View.getCurrentSceneState() == SceneState.PLAY || View.getCurrentSceneState() == SceneState.TUTORIAL || (View.getCurrentSceneState() == SceneState.LEVEL_EDITOR && GameConstants.DEBUG)) {
                        newResultIsBetter = model.putStatsIfBetter(loc, turns, nStars);
                        model.updateUnlockedLevelsList(false);
                        model.updateUnlockedStatements();
                    }
                    timeline.stop();
                    String winString = "You have won!" + "\nYou earned " + (int) nStars + (Math.round(nStars) != (int) nStars ? ".5" : "") + (nStars > 1 ? " Stars! (" : " Star! (") + turns + " Turns, " + loc + " Lines of Code)";

                    boolean isTutorial = (boolean) model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL);
                    // This only to stop levels with enemies from having differently colored SpawnPoints in LevelOverview
                    model.reset();
                    String nextLevelName;
                    if (isTutorial && View.getCurrentSceneState() == SceneState.TUTORIAL) {
                        int nextIndex = model.getNextTutorialIndex();
                        if (newResultIsBetter)
                            view.getTutorialLevelOverviewPane().updateCurrentLevel();
                        minIndex = nextIndex - 1 > minIndex ? nextIndex - 1 : minIndex;
                        if (nextIndex != -1 && nextIndex > model.getTutorialProgress()) {
                            model.setTutorialProgress(nextIndex-1);
                            nextLevelName = (String) model.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME, nextIndex);
                            if (!view.getTutorialLevelOverviewPane().containsLevel(nextLevelName))
                                view.getTutorialLevelOverviewPane().addLevelWithIndex(nextIndex);
                        }
                        else if (nextIndex == -1){

                            for(int id : model.getUnlockedLevelIds()){
                                int i = model.getIndexOfLevelWithId(id);

                                if(view.getLevelOverviewPane().containsLevel(model.getNameOfLevelWithId(id)) || (boolean)model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,i))continue;
                                view.getLevelOverviewPane().addLevelWithIndex(i);
                            }
                            if(view.getLevelOverviewPane().getLevelListView().getItems().size() == 0)view.getStartScreen().getPlayBtn().setDisable(true);
                            else view.getStartScreen().getPlayBtn().setDisable(false);
                        }
                    } else if (View.getCurrentSceneState() == SceneState.PLAY) {
                        if (newResultIsBetter)
                            view.getLevelOverviewPane().updateCurrentLevel();

                        for(int id : model.getUnlockedLevelIds()){
                            int i = model.getIndexOfLevelWithId(id);
                            if(view.getLevelOverviewPane().containsLevel(model.getNameOfLevelWithId(id)) || (boolean)model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,i))continue;
                            view.getLevelOverviewPane().addLevelWithIndex(i);
                        }
                        if(model.getAmountOfLevels() == model.getAmountOfCompletedLevels())model.unlockEditor();
                        view.getStartScreen().getLvlEditorBtn().setDisable(!model.isEditorUnlocked());
                    }


                    Platform.runLater(() -> {
                       showWinDialog(winString, View.getCurrentSceneState(), nStars);
                    });
                    isGameRunning = false;
                    if (view.getLevelOverviewPane().getLevelListView().getItems().size() > 0)
                        view.getStartScreen().getPlayBtn().setDisable(false);
                    view.getSpellBookPane().updateSpellbookEntries(model.getUnlockedStatementList());}
                }
                if (model.isStackOverflow()) {
                    timeline.stop();
                    Alert alert = new Alert(Alert.AlertType.NONE, "You might have accidentally caused an endless loop! You are not allowed to use big loops without method calls!", ButtonType.OK);
                    Platform.runLater(() -> {
                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent()) {
                            view.getBtnReset().fire();
                        }
                    });
                }
//


            }));
            timeline.play();
            if (View.getCurrentSceneState() == SceneState.LEVEL_EDITOR)
                editorController.setAllEditButtonsToDisable(true);
        });
    }


    private void setPauseAndRunHandler() {
        view.getBtnExecute().setOnAction(evt -> {
            timeline.pause();
            ((ImageView) view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.EXECUTE_BTN_IMAGE_PATH));
            view.getBtnExecute().setOnAction(evt2 -> {
                timeline.play();
                setPauseAndRunHandler();
                ((ImageView) view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.PAUSE_BTN_IMAGE_PATH));
            });
        });
    }

    private void addHighlightingEffect(Button... buttons) {

        for (Button b : buttons) {
            b.setOnMouseExited(evt -> b.setEffect(null));
            b.setOnMouseEntered(evt -> b.setEffect(GameConstants.HIGHLIGHT_BTN_EFFECT));
        }

    }

    private void showWinDialog(String winString, SceneState sceneState, double stars) {
        Dialog<ButtonType> winDialog = new Dialog<>();
        winDialog.getDialogPane().setBackground(new Background(new BackgroundImage(new Image(GameConstants.BG_LIGHT_TILE_PATH), BackgroundRepeat.REPEAT, null, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
        ImageView starsIV = new ImageView(Util.getStarImageFromDouble(stars));

        Label winLabel = new Label(winString);
        winLabel.setAlignment(Pos.CENTER);
        winLabel.setMinWidth(GameConstants.TEXTFIELD_WIDTH);
        winLabel.setStyle("-fx-text-fill: white;-fx-effect: dropshadow(three-pass-box, black, 10, 0.6, 0.6, 0);");
        winLabel.setTextAlignment(TextAlignment.CENTER);
        winLabel.setFont(GameConstants.BIGGEST_FONT);
        VBox contentVBox = new VBox(starsIV, winLabel);
        contentVBox.setAlignment(Pos.CENTER);
        winDialog.getDialogPane().setContent(contentVBox);
        ButtonType replayBtn = new ButtonType("Replay", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType nextBtn = new ButtonType("Next", ButtonBar.ButtonData.NEXT_FORWARD);
        ButtonType backBtn = new ButtonType("Back To Menu", ButtonBar.ButtonData.BACK_PREVIOUS);

        winDialog.getDialogPane().getButtonTypes().addAll(backBtn, replayBtn);
        boolean nextLvlIsTut = false;
        if (model.getCurrentIndex() + 1 < model.getAmountOfLevels())
            nextLvlIsTut = (boolean) model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL, model.getCurrentIndex() + 1);
        if (model.getCurrentIndex() < model.getAmountOfLevels() - 1 && nextLvlIsTut && sceneState != SceneState.LEVEL_EDITOR) {
            winDialog.getDialogPane().getButtonTypes().add(2, nextBtn);
        }
        Optional<ButtonType> bnt = winDialog.showAndWait();
        if (bnt.isPresent()) {
            view.getBtnReset().fire();
            switch (bnt.get().getButtonData()) {
                case NEXT_FORWARD:
                    model.selectLevel(model.getCurrentIndex() + 1);
                case CANCEL_CLOSE:
                    break;
                case BACK_PREVIOUS:
                    view.getBackBtn().fire();
                    break;
            }
        }
    }

    public EditorController getEditorController() {
        return editorController;
    }

}
