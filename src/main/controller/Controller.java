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

import static main.model.GameConstants.CHALLENGE_COURSE_NAME;
import static main.model.GameConstants.WHITE_SHADOWED_STYLE;

public class Controller {
    private static Controller single_Instance = null;
    private boolean isGameRunning = false;
    private double mouse_PositionX;
    private double mouse_PositionY;
    private View view;
    private Model model;
    private Timeline timeline;
    private EditorController editorController;


    private Controller(View view,Model model) {
        this.view = view;
        this.model = model;
        new CodeAreaController(view,model);
        editorController = new EditorController(view,model);
        // If DEBUG is disabled the LevelEditor Button is only available if all Levels have been unlocked
        if(model.getMinStarsOfCourse(CHALLENGE_COURSE_NAME) < 3)view.getStartScreen().getLvlEditorBtn().setDisable(!(GameConstants.DEBUG || model.isEditorUnlocked()));

        view.getShowSpellBookBtn().setOnAction(evt -> view.toggleShowSpellBook());
        view.getSpellBookPane().getCloseBtn().setOnAction(evt -> view.toggleShowSpellBook());

        view.getSpellBookPane().getShowShortcutsBtn().setOnAction(evt -> {
            Alert a = new Alert(Alert.AlertType.NONE, GameConstants.SHORTCUT_INFORMATION, ButtonType.CLOSE);
            double widthFactor = 1.75;
            a.setWidth(GameConstants.CODEFIELD_WIDTH * widthFactor);
            a.getDialogPane().setMinWidth(GameConstants.CODEFIELD_WIDTH * widthFactor);
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
                view.getChallengeOverviewPane().getBackBtn(), view.getChallengeOverviewPane().getPlayBtn(),  view.getTutorialGroup().getNextBtn(), view.getTutorialGroup().getPrevBtn());


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
                    view.setSceneState(SceneState.COURSE_SELECT);
                    break;
                case COURSE_SELECT:
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
                    if (model.calculateProgressOfCourse(model.getCurrentCourseName()) == -1) view.setSceneState(SceneState.COURSE_SELECT);
                    else view.setSceneState(SceneState.TUTORIAL_LEVEL_SELECT);
                    view.getBtnExecute().setMouseTransparent(false);
                    view.getSpeedSlider().setMouseTransparent(false);
                    view.getShowSpellBookBtn().setMouseTransparent(false);
                    view.getCodeArea().setDisable(false);
                    view.getStage().requestFocus();
                    view.getMapGPane().setDisable(false);
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
        view.getStartScreen().getTutorialBtn().setOnAction(actionEvent -> {
            model.resetTutorialIndex();
            view.setSceneState(SceneState.COURSE_SELECT);
            view.getCourseOverviewPane().getBackBtn().setOnAction(evt -> {
                view.setSceneState(SceneState.START_SCREEN);
            });
            addHighlightingEffect(view.getCourseOverviewPane().getBackBtn(),view.getCourseOverviewPane().getPlayBtn());
            if(model.getAmountOfLevelsInCourse(view.getCourseOverviewPane().getCourseListView().getSelectionModel().getSelectedItem().getCourseName())==0)view.getCourseOverviewPane().getPlayBtn().setDisable(true);
            view.getCourseOverviewPane().getCourseListView().getSelectionModel().selectedItemProperty().addListener((observableValue, courseEntry, t1) ->{
                if(model.getAmountOfLevelsInCourse(t1.getCourseName())==0)view.getCourseOverviewPane().getPlayBtn().setDisable(true);}
            );
            view.getCourseOverviewPane().getPlayBtn().setOnAction(evt2 -> {
                String course = view.getCourseOverviewPane().getCourseListView().getSelectionModel().getSelectedItem().getCourseName();
                model.selectFirstLevelInCourse(course);
                if (model.getCurrentCourseProgress() != -1) {
                    view.setSceneState(SceneState.TUTORIAL_LEVEL_SELECT);

                } else {
                    view.setSceneState(SceneState.TUTORIAL);
                    editorController.setHandlersForMapCells();
                    setMessageNavigationHandler();
                }
                for( LevelOverviewPane tutorialOverviewPane : view.getTutorialLevelOverviewPaneListCopy()){
                    tutorialOverviewPane.getBackBtn().setOnAction(actionEvent2 ->
                            view.setSceneState(SceneState.COURSE_SELECT)
                    );
                    tutorialOverviewPane.getPlayBtn().setOnAction(actionEvent1 -> {
                        model.resetTutorialIndex();
                        String levelName = tutorialOverviewPane.getLevelListView().getSelectionModel().getSelectedItem().getLevelName();
                        if(tutorialOverviewPane.getIntroductionCheckbox().isSelected()){
                            view.prepareForIntroduction();
                            tutorialOverviewPane.getIntroductionCheckbox().setSelected(false);
                        }
                        view.setSceneState(SceneState.TUTORIAL);
                        model.selectLevel(levelName);
                        editorController.setHandlersForMapCells();
                        setMessageNavigationHandler();
                    });

                    addHighlightingEffect(tutorialOverviewPane.getBackBtn(),
                            tutorialOverviewPane.getPlayBtn());
                }


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
        });

        if (view.getChallengeOverviewPane().getLevelListView().getItems().size() == 0)
            view.getStartScreen().getPlayBtn().setDisable(true);
        view.getStartScreen().getPlayBtn().setOnAction(actionEvent -> {
            view.setSceneState(SceneState.LEVEL_SELECT);
            view.getChallengeOverviewPane().getBackBtn().setOnAction(actionEvent2 ->
                    view.setSceneState(SceneState.START_SCREEN)
            );
            view.getChallengeOverviewPane().getPlayBtn().setOnAction(actionEvent1 -> {
                String levelName = view.getChallengeOverviewPane().getLevelListView().getSelectionModel().getSelectedItem().getLevelName();
                view.setSceneState(SceneState.PLAY);
                model.selectLevel(levelName);
                editorController.setHandlersForMapCells();
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
                view.highlightInMap(List.of(new Point(0,0)));
            }
            isGameRunning = false;
            timeline.stop();
            model.reset();
            view.drawMap(model.getCurrentMapCopy());
            view.getCodeArea().setEditable(true);
            view.getCodeArea().select(0, Selection.END);
            editorController.setHandlersForMapCells();
        });
        view.getLoadBestCodeBtn().setOnAction(actionEvent -> {
            List<String> bestCode;

            boolean noCode = view.getCodeArea().getAllCode().size() == 1 && view.getCodeArea().getAllCode().get(0).equals("");
            Alert alert = new Alert(Alert.AlertType.NONE, "Do you really want to overwrite the current code?", ButtonType.OK, ButtonType.CANCEL);
            Optional<ButtonType> result = noCode ? null : alert.showAndWait();
            if (noCode || (result.isPresent() && result.get() == ButtonType.OK)) {
                view.getCodeArea().scrollTo(0);
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

    private void setMessageNavigationHandler() {
        view.getStage().getScene().setOnKeyPressed(evt -> {
            evt.consume();
            if (evt.getCode() == KeyCode.RIGHT && evt.isAltDown()) {
                if (!view.getTutorialGroup().getNextBtn().isDisabled()) view.getTutorialGroup().getNextBtn().fire();
            } else if (evt.getCode() == KeyCode.LEFT && evt.isAltDown()) {
                if (!view.getTutorialGroup().getPrevBtn().isDisabled()) view.getTutorialGroup().getPrevBtn().fire();
            }
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
        deleteLabel.setMinWidth(GameConstants.CODEFIELD_WIDTH);
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

    /** Controls what happens when the Execute/Pause Button is pressed. To execute code for the current level, this
     * button must be pressed. It will then turn into a Pause Button and will toggle between resuming and pausing the
     * execution of code upon further button presses.
     *
     */
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
                    int usedKnights = model.getAmountOfKnightsSpawned();
                    int maxKnights = (int) model.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS);
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
                    double nStars = Util.calculateStars(turns, loc, usedKnights, turnsToStars, locToStars, maxKnights);
                    boolean newResultIsBetter = false;
                    if (View.getCurrentSceneState() == SceneState.PLAY || View.getCurrentSceneState() == SceneState.TUTORIAL || (View.getCurrentSceneState() == SceneState.LEVEL_EDITOR && GameConstants.DEBUG)) {
                        newResultIsBetter = model.putStatsIfBetter(loc, turns, nStars);
//                        model.storeProgress();
//                        model.updateUnlockedLevelsList(false);
                        model.updateUnlockedStatements();
                    }
                    timeline.stop();
                    String winString = "You have won!" + "\nYou earned " + (int) nStars + (Math.round(nStars) != (int) nStars ? ".5" : "") + (nStars > 1 ? " Stars! (" : " Star! (") + turns + " Turns, " + loc + " Lines of Code)";

                    String courseName = ""+ model.getDataFromCurrentLevel(LevelDataType.COURSE);
                    // This is only to stop levels with enemies from having differently colored SpawnPoints in LevelOverview
                    model.reset();
                    String nextLevelName;
//                    boolean isTutorial = !courseName.equals(GameConstants.CHALLENGE_COURSE_NAME);
                    if (View.getCurrentSceneState() == SceneState.TUTORIAL) {
                        if (newResultIsBetter){
                            view.getCurrentTutorialLevelOverviewPane().updateCurrentLevel();
                            view.getCourseOverviewPane().updateCourseRating(courseName);
                        }
                        if (model.getNextId() != -1) {
                            nextLevelName = (String) model.getDataFromLevelWithId(LevelDataType.LEVEL_NAME,model.getNextId());
                            if (!view.getCurrentTutorialLevelOverviewPane().containsLevel(nextLevelName))
                                view.getCurrentTutorialLevelOverviewPane().addLevelWithId(model.getNextId());
                        }
                        else if (model.getNextId() == -1){

//                            for(int id : model.getUnlockedLevelIds()){
//                                int i = model.getIndexOfLevelWithId(id);
//                                String iCourseName = ""+ model.getDataFromLevelWithId(LevelDataType.COURSE,i);
//                                boolean iIsTutorial = !iCourseName.equals(GameConstants.CHALLENGE_COURSE_NAME);
//                                if(view.getChallengeOverviewPane().containsLevel(model.getNameOfLevelWithId(id)) || iIsTutorial)continue;
//                                view.getChallengeOverviewPane().addLevelWithIndex(i);
//                            }
//                            if(view.getChallengeOverviewPane().getLevelListView().getItems().size() == 0)view.getStartScreen().getPlayBtn().setDisable(true);
//                            else view.getStartScreen().getPlayBtn().setDisable(false);
                        }
                    } else if (View.getCurrentSceneState() == SceneState.PLAY) {
                        if (newResultIsBetter)
                            view.getChallengeOverviewPane().updateCurrentLevel();

//                        for(int id : model.getUnlockedLevelIds()){
//                            int i = model.getIndexOfLevelWithId(id);
//
//                            String iCourseName = ""+ model.getDataFromLevelWithId(LevelDataType.COURSE,i);
//                            boolean iIsTutorial = !iCourseName.equals(GameConstants.CHALLENGE_COURSE_NAME);
//                            if(view.getChallengeOverviewPane().containsLevel(model.getNameOfLevelWithId(id)) || iIsTutorial)continue;
//                            view.getChallengeOverviewPane().addLevelWithIndex(i);
//                        }
                        if(model.getMinStarsOfCourse(CHALLENGE_COURSE_NAME)==3)
                            model.unlockEditor();
                        view.getStartScreen().getLvlEditorBtn().setDisable(!model.isEditorUnlocked());
                    }


                    Platform.runLater(() -> {
                       showWinDialog(winString, View.getCurrentSceneState(), nStars);
                    });
                    isGameRunning = false;
                    if (view.getChallengeOverviewPane().getLevelListView().getItems().size() > 0)
                        view.getStartScreen().getPlayBtn().setDisable(false);
                    view.getSpellBookPane().updateSpellbookEntries(model.getUnlockedStatementList());}
                }
                if (model.isStackOverflow()) {
                    timeline.stop();
                    view.getMapGPane().setDisable(false);
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
            view.getMapGPane().setDisable(true);
            timeline.play();
            if (View.getCurrentSceneState() == SceneState.LEVEL_EDITOR)
                editorController.setAllEditButtonsToDisable(true);
        });
    }

    /** Toggles between pausing and resuming the execution of code upon pressing the Execute Button
     *
     */
    private void setPauseAndRunHandler() {
        view.getBtnExecute().setOnAction(evt -> {
            timeline.pause();
            view.getMapGPane().setDisable(false);
            ((ImageView) view.getBtnExecute().getGraphic()).setImage(new Image(GameConstants.EXECUTE_BTN_IMAGE_PATH));
            view.getBtnExecute().setOnAction(evt2 -> {
                timeline.play();
                view.getMapGPane().setDisable(true);
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
        winLabel.setMinWidth(GameConstants.CODEFIELD_WIDTH);
        winLabel.setStyle(WHITE_SHADOWED_STYLE);
        winLabel.setTextAlignment(TextAlignment.CENTER);
        winLabel.setFont(GameConstants.BIGGEST_FONT);
        VBox contentVBox = new VBox(starsIV, winLabel);
        contentVBox.setAlignment(Pos.CENTER);
        winDialog.getDialogPane().setContent(contentVBox);
        ButtonType replayBtn = new ButtonType("Replay", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType nextBtn = new ButtonType("Next", ButtonBar.ButtonData.NEXT_FORWARD);
        ButtonType backBtn = new ButtonType("Back To Menu", ButtonBar.ButtonData.BACK_PREVIOUS);

        winDialog.getDialogPane().getButtonTypes().addAll(backBtn, replayBtn);
        if (model.getNextId() != -1 && sceneState != SceneState.LEVEL_EDITOR) {
            winDialog.getDialogPane().getButtonTypes().add(2, nextBtn);
        }
        Optional<ButtonType> bnt = winDialog.showAndWait();
        if (bnt.isPresent()) {
            view.getBtnReset().fire();
            switch (bnt.get().getButtonData()) {
                case NEXT_FORWARD:
                    model.selectLevel(model.getNextId() );
                    editorController.setHandlersForMapCells();
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
