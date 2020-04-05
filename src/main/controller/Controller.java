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
import main.model.gamemap.GameMap;
import main.model.statement.ComplexStatement;
import main.model.statement.Statement;
import main.parser.JSONParser;
import main.parser.CodeParser;
import main.utility.GameConstants;
import main.utility.Util;
import main.view.*;

import java.util.List;
import java.util.Optional;

public class Controller {
    private static Controller single_Instance = null;
    private boolean isGameRunning = false;
    private double mouse_PositionX;
    private double mouse_PositionY;
    private View view;
    private Timeline timeline;
    private CodeAreaController codeAreaController;
    private EditorController editorController;
    private int minIndex = 0;

    private Controller(View view) {
        this.view = view;
        codeAreaController = new CodeAreaController(view);
        editorController = new EditorController(view);
        int minIndex = Model.getTutorialProgress();
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
            a.setWidth(GameConstants.TEXTFIELD_WIDTH * 1.75);
            a.getDialogPane().setMinWidth(GameConstants.TEXTFIELD_WIDTH * 1.75);
            a.showAndWait();
        });
        // Not in use at the moment:
        /*view.getSpellBookPane().getCreateMethodBtn().setOnAction(evt ->{
            Dialog<ButtonType> createMethodDialog = new Dialog<>();

            createMethodDialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
            createMethodDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            createMethodDialog.getDialogPane().setStyle("-fx-background-color: rgba(50,50,50,1)");
            CodeArea newMethodCodeArea = CodeArea.getInstance(CodeAreaType.METHOD_CREATOR);
            newMethodCodeArea.updateCodeFields(new ComplexStatement());
            createMethodDialog.getDialogPane().setContent(newMethodCodeArea);
            newMethodCodeArea.select(0, Selection.END);
            codeAreaController.setAllHandlersForCodeArea(newMethodCodeArea);
            newMethodCodeArea.addPropertyChangeListener(codeAreaController);
            createMethodDialog.setContentText("Create a new Method");
            // +1 because of the gap in between Codefields
            // needs to be here for correct placement of the dialog
            createMethodDialog.setHeight(standardHeight+GameConstants.MAX_CODE_LINES*(GameConstants.TEXTFIELD_HEIGHT+1));
            Platform.runLater(()->{
                standardHeight = createMethodDialog.getHeight();
                newMethodCodeArea.getSelectedCodeField().requestFocus();

                createMethodDialog.setHeight(standardHeight+GameConstants.MAX_CODE_LINES*(GameConstants.TEXTFIELD_HEIGHT+1));
            });
            final Button btOk = (Button) createMethodDialog.getDialogPane().lookupButton(ButtonType.OK);
//            createMethodDialog.getDialogPane().setOnKeyPressed(evt2 ->{
//                int amount = newMethodCodeArea.getSize() > GameConstants.MAX_CODE_LINES ? GameConstants.MAX_CODE_LINES : newMethodCodeArea.getSize();
//                createMethodDialog.setHeight(standardHeight+amount*(GameConstants.TEXTFIELD_HEIGHT+1));
//                createMethodDialog.getDialogPane().setMaxHeight(standardHeight+amount*(GameConstants.TEXTFIELD_HEIGHT+1));
//            });
            btOk.addEventFilter(
                    ActionEvent.ACTION,
                    event -> {
                        //this stops the dialog from closing, when pressing Enter!
                        if(!btOk.isFocused())
                            event.consume();
                    }
            );
            Optional<ButtonType> o  = createMethodDialog.showAndWait();
            if(o.isPresent()&&o.get() == ButtonType.OK){
                ComplexStatement complexStatement = OldCodeParser.parseProgramCode(newMethodCodeArea.getAllText(),CodeAreaType.METHOD_CREATOR);
                Statement firstStatement = complexStatement.getSubStatement(0);

                if(firstStatement.getStatementType() == StatementType.METHOD_DECLARATION) Model.addMethod((MethodDeclaration)firstStatement);
            }
        });*/
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
            switch (view.getCurrentSceneState()) {
                case LEVEL_EDITOR:
                    if (Model.currentLevelHasChanged()) editorController.showSavingDialog();
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
                    if (minIndex == -1) view.setSceneState(SceneState.START_SCREEN);
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
            view.setSceneState(SceneState.LEVEL_EDITOR);
            editorController.setEditorHandlers();
            Platform.runLater(() -> view.highlightInMap(view.getSelectedPointList()));
        });

        view.getStartScreen().getExitBtn().setOnAction(actionEvent -> {
            JSONParser.storeAllData();
            try {
                CodeParser.parseProgramCode(view.getCodeArea().getAllText());
                JSONParser.storeCode(Util.trimStringList(view.getCodeArea().getAllText()));
            } catch (Exception e) {
                //??
                System.out.println("Could not store current code as it contains errors!");
            }
            System.exit(0);
        });
        view.getStage().getScene().setOnKeyPressed(evt -> {
            evt.consume();
            if (view.getCurrentSceneState() != SceneState.TUTORIAL) return;
            if (evt.getCode() == KeyCode.RIGHT && evt.isAltDown()) {
                if (!view.getTutorialGroup().getNextBtn().isDisabled()) view.getTutorialGroup().getNextBtn().fire();
            } else if (evt.getCode() == KeyCode.LEFT && evt.isAltDown()) {
                if (!view.getTutorialGroup().getPrevBtn().isDisabled()) view.getTutorialGroup().getPrevBtn().fire();
            }
        });
        view.getStartScreen().getTutorialBtn().setOnAction(actionEvent -> {
            if (Model.getTutorialProgress() != -1) {
                view.setSceneState(SceneState.TUTORIAL_LEVEL_SELECT);

            } else {
                view.setSceneState(SceneState.TUTORIAL);
                Model.selectLevel(0);
            }
            view.getTutorialLevelOverviewPane().getBackBtn().setOnAction(actionEvent2 ->
                    view.setSceneState(SceneState.START_SCREEN)
            );
            view.getTutorialLevelOverviewPane().getPlayBtn().setOnAction(actionEvent1 -> {
                String levelName = view.getTutorialLevelOverviewPane().getLevelListView().getSelectionModel().getSelectedItem().getLevelName();
                Model.selectLevel(levelName);
                view.setSceneState(SceneState.TUTORIAL);
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
                Model.selectLevel(levelName);
            });
        });


        setExecuteHandler();

        view.getBtnReset().setOnAction(actionEvent -> {
            setExecuteHandler();

            view.getCodeArea().highlightCodeField(-1);
            boolean hasAi = (boolean) Model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
            if (hasAi) view.getAICodeArea().highlightCodeField(-1);
            view.setNodesDisableWhenRunning(false);
            if (view.getCurrentSceneState() == SceneState.LEVEL_EDITOR) {
                view.getLevelEditorModule().setDisableAllLevelBtns(false);
                if (hasAi) view.getAICodeArea().setEditable(true);
                editorController.setAllEditButtonsToDisable(false);
                editorController.setHandlersForMapCells();
            }
            isGameRunning = false;
            timeline.stop();
            Model.reset();
            view.drawMap(Model.getCurrentMap());
            view.getCodeArea().setEditable(true);
            view.getCodeArea().select(0, Selection.END);
        });
        view.getLoadBestCodeBtn().setOnAction(actionEvent -> {
            List<String> bestCode;

            boolean noCode = view.getCodeArea().getAllText().size() == 1 && view.getCodeArea().getAllText().get(0).equals("");
            Alert alert = new Alert(Alert.AlertType.NONE, "Do you really want to overwrite the current code?", ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = noCode ? null : alert.showAndWait();
            if (noCode || (result.isPresent() && result.get() == ButtonType.OK)) {
                bestCode = Model.getCurrentlyBestCode();
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
            showDialogToClearCode(view.getAICodeArea());
        });

    }

    public static Controller instantiate(View view) {
        if (single_Instance == null) single_Instance = new Controller(view);
        return single_Instance;
    }

    private void showDialogToClearCode(CodeArea codeArea) {
        Dialog<ButtonType> deleteDialog = new Dialog<>();
        Label deleteLabel = new Label("You are going to removeCurrentLevel all code! Are you sure?");
        deleteLabel.setAlignment(Pos.CENTER);
        deleteLabel.setMinWidth(GameConstants.TEXTFIELD_WIDTH);
        deleteLabel.setTextAlignment(TextAlignment.CENTER);


        deleteDialog.getDialogPane().setContent(deleteLabel);
        ButtonType noBtn = new ButtonType("NO", ButtonBar.ButtonData.NO);
        ButtonType yesBtn = new ButtonType("YES", ButtonBar.ButtonData.YES);
        deleteDialog.getDialogPane().getButtonTypes().addAll(noBtn, yesBtn);
        Optional<ButtonType> result = deleteDialog.showAndWait();
        if (result.isPresent()) {
            switch (result.get().getButtonData()) {
                default:
                    break;
                case YES:
                    codeArea.clear();
                    break;
            }
        }
    }

    private void setExecuteHandler() {

        ((ImageView) view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.EXECUTE_BTN_IMAGE_PATH));
        view.getBtnExecute().setOnAction(actionEvent -> {
            view.getCodeArea().scollTo(0);
            final boolean hasAi = (boolean) Model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
            if (hasAi) view.getAICodeArea().scollTo(0);
            CodeArea playerCodeArea = CodeArea.getInstance(CodeAreaType.PLAYER);
            List<String> oldCode = view.getCodeArea().getAllText();
            ComplexStatement behaviour = CodeParser.parseProgramCode(view.getCodeArea().getAllText(), CodeAreaType.PLAYER);
            ComplexStatement aiBehaviour = new ComplexStatement();
            if (hasAi) aiBehaviour = CodeParser.parseProgramCode(view.getAICodeArea().getAllText(), CodeAreaType.AI);
            if (view.getCurrentSceneState() == SceneState.LEVEL_EDITOR)
                view.getLevelEditorModule().setDisableAllLevelBtns(true);
            if (GameConstants.DEBUG) System.out.println(behaviour.print());
            if (GameConstants.DEBUG) System.out.println(aiBehaviour.print());
            Model.setCurrentPlayerBehaviour(behaviour);
            Model.initAiIteratorAndEvaluator(aiBehaviour);
            Model.changeCurrentLevel(LevelDataType.AI_CODE, aiBehaviour);
            view.setNodesDisableWhenRunning(true);
            isGameRunning = true;
            view.getBtnExecute().setDisable(false);

            view.getSpeedSlider().setDisable(false);

            ((ImageView) view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.PAUSE_BTN_IMAGE_PATH));
            setPauseAndRunHandler();

            timeline = new Timeline();

            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.setRate(GameConstants.TICK_SPEED * view.getSpeedSlider().getValue());
            view.deselect();
            final ComplexStatement aiB = aiBehaviour;
            timeline.getKeyFrames().addAll(new KeyFrame(Duration.seconds(0.8), event ->
            {
                Statement[] executedStatements = Model.executeTurn();
                int index = behaviour.findIndexOf(executedStatements[0], 0);
                if (index != -1) view.getCodeArea().highlightCodeField(index);
                if (hasAi) {
                    int index2 = aiB.findIndexOf(executedStatements[1], 0);

                    if (index2 != -1) view.getAICodeArea().highlightCodeField(index2);
                }
                view.drawAllChangedCells();
                if (Model.isLost()) {
                    view.getCodeArea().highlightCodeField(-1);
                }
                if (Model.aiIsFinished()) {
                    view.getAICodeArea().highlightCodeField(-1);
                }
                if (Model.isWon()) {
                    int turns = Model.getTurnsTaken();
                    int loc = behaviour.getActualSize();
                    Integer[] turnsToStars = (Integer[]) Model.getDataFromCurrentLevel(LevelDataType.TURNS_TO_STARS);
                    Integer[] locToStars = (Integer[]) Model.getDataFromCurrentLevel(LevelDataType.LOC_TO_STARS);
                    String levelName = (String) Model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME);
                    double nStars = Util.calculateStars(turns, loc, turnsToStars, locToStars);
                    boolean isBetter = false;
                    if (view.getCurrentSceneState() == SceneState.PLAY || view.getCurrentSceneState() == SceneState.TUTORIAL || (view.getCurrentSceneState() == SceneState.LEVEL_EDITOR && GameConstants.DEBUG)) {
                        isBetter = Model.putStatsIfBetter(loc, turns, nStars);
                        Model.updateUnlockedLevelsList(false);
                    }
                    timeline.stop();
                    String winString = "You have won!" + "\nYou earned " + (int) nStars + (Math.round(nStars) != (int) nStars ? ".5" : "") + (nStars > 1 ? " Stars! (" : " Star! (") + turns + " Turns, " + loc + " Lines of Code)";
                    int amountOfTuts = Model.getAmountOfTutorials();
                    boolean isTutorial = (boolean) Model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL);
                    int nextIndex = Model.getNextTutorialIndex();

                    String nextLevelName;
                    GameMap nextLevelMap;
                    if (isTutorial && view.getCurrentSceneState() == SceneState.TUTORIAL) {
                        if (isBetter)
                            view.getTutorialLevelOverviewPane().updateCurrentLevel();
                        minIndex = nextIndex - 1 > minIndex ? nextIndex - 1 : minIndex;
                        if (nextIndex != -1) {
                            Model.nextTutorial();
                            nextLevelName = (String) Model.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME, nextIndex);
                            nextLevelMap = (GameMap) Model.getDataFromLevelWithIndex(LevelDataType.MAP_DATA, nextIndex);
                            if (!view.getTutorialLevelOverviewPane().containsLevel(nextLevelName))
                                view.getTutorialLevelOverviewPane().addLevel(nextIndex, view.getImageFromMap(nextLevelMap));
                        }
                    } else if (view.getCurrentSceneState() == SceneState.PLAY) {
                        if (isBetter)
                            view.getLevelOverviewPane().updateCurrentLevel();

                        if (nextIndex != -1) {
                            nextLevelName = (String) Model.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME, nextIndex);
                            nextLevelMap = (GameMap) Model.getDataFromLevelWithIndex(LevelDataType.MAP_DATA, nextIndex);
                            if (!view.getLevelOverviewPane().containsLevel(nextLevelName))
                                view.getLevelOverviewPane().addLevel(nextIndex, view.getImageFromMap(nextLevelMap));
                        }
                    }


                    Platform.runLater(() -> {
                       showWinDialog(winString, view.getCurrentSceneState(), nStars);
                    });
                    isGameRunning = false;
                    if (view.getLevelOverviewPane().getLevelListView().getItems().size() > 0)
                        view.getStartScreen().getPlayBtn().setDisable(false);
                    view.getSpellBookPane().updateSpellbookEntries(Model.getUnlockedStatementList());
                }
                if (Model.isStackOverflow()) {
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
            if (view.getCurrentSceneState() == SceneState.LEVEL_EDITOR)
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
        winDialog.getDialogPane().setBackground(new Background(new BackgroundImage(new Image("file:resources/images/background_tile.png"), BackgroundRepeat.REPEAT, null, BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
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
        winDialog.getDialogPane().getButtonTypes().addAll(backBtn, replayBtn);
        boolean nextLvlIsTut = false;
        if (Model.getCurrentIndex() + 1 < Model.getAmountOfLevels())
            nextLvlIsTut = (boolean) Model.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL, Model.getCurrentIndex() + 1);
        if (Model.getCurrentIndex() < Model.getAmountOfLevels() - 1 && (sceneState != SceneState.TUTORIAL || nextLvlIsTut) && sceneState != SceneState.LEVEL_EDITOR) {
            winDialog.getDialogPane().getButtonTypes().add(2, nextBtn);
        }
        Optional<ButtonType> bnt = winDialog.showAndWait();
        if (bnt.isPresent()) {
            switch (bnt.get().getButtonData()) {
                case NEXT_FORWARD:
                    view.getBtnReset().fire();
                    Model.selectLevel(Model.getCurrentIndex() + 1);
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

    public EditorController getEditorController() {
        return editorController;
    }

}
