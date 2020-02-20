package main.view;

import main.controller.Selection;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import main.main.Tester;
import main.model.*;
import main.model.Cell;
import main.model.enums.CContent;
import main.model.enums.CFlag;
import main.model.enums.EntityType;
import main.model.enums.ItemType;
import main.model.statement.ComplexStatement;
import main.parser.JSONParser;
import main.utility.GameConstants;
//import org.jetbrains.annotations.Contract;
import main.utility.Point;
import main.utility.Util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.utility.GameConstants.BUTTON_SIZE;
import static main.utility.GameConstants.NO_ENTITY;

public class View implements PropertyChangeListener {

    private final BackgroundImage backgroundImage = new BackgroundImage(new Image( "file:resources/images/background_tile.png" ), BackgroundRepeat.REPEAT,null,BackgroundPosition.CENTER,BackgroundSize.DEFAULT );
    private final LevelOverviewPane tutorialLevelOverviewPane;
    private Background brickBackground = new Background(backgroundImage);
    private Stage stage;
    private Model model;
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    //Testing

    //    private Canvas canvas;
    private double cell_size;
    private StartScreen startScreen;
    private Button btnExecute;
    private Button btnReset;
    //    TextArea codeTextArea;
    private CodeArea codeArea = new CodeArea(false);
    private CodeArea aiCodeArea;
    private VBox vBox;
    private Slider speedSlider;
    private Label msgLabel = new Label();
    private Shape[][] mapShapes;
    private GridPane actualMapGPane;
    private StackPane rootPane;
    private VBox baseContentVBox;
    private List<Polyline> highlights = new ArrayList<>();
    private LevelEditorModule levelEditorModule;
    private List<Point> selectedPointList = new ArrayList<>();
    private ChoiceBox<String> choiceBox;
    private Map<String, Image> contentImageMap = new HashMap<>();

    private LevelOverviewPane levelOverviewPane;
    //    private Scene levelSelectScene;
//    private Scene playScene;
    private Scene startScene;
    //    private Scene editorScene;
//    private Scene tutorialScene;
    private Button backBtn = new Button();
    private SceneState sceneState = SceneState.START_SCREEN;
    private VBox knightsLeftVBox;
    private SpellBookPane spellBookPane = new SpellBookPane();
    private Label levelNameLabel = new Label();
    private TextArea tutorialTextArea = new TextArea();
    private Button showSpellBookBtn = new Button();

    private VBox leftVBox = new VBox();
    private VBox centerVBox = new VBox();
    private VBox rightVBox = new VBox();
    private VBox speedVBox;
    private TutorialGroup tutorialGroup;

    private Button loadBestCodeBtn = new Button("Load Best Code");
    private Button clearCodeBtn = new Button("Clear Code");
    private IntroductionPane introductionPane = IntroductionPane.getInstance();

    //TODO: for visual purposes:
    List<Entity> entityActionList = new ArrayList<>();
    Map<String, Effect> entityColorMap = new HashMap<>();
    private boolean isIntroduction;


    public View(Model model, Stage stage, boolean isEditor) {
        msgLabel.setStyle("-fx-text-fill: red;-fx-background-color: white");
        msgLabel.setFont(GameConstants.BIG_FONT);
        spellBookPane.updateSpellbookEntries(model.getCurrentLevel().getUnlockedStatementList());
        selectedPointList = new ArrayList<>();
        selectedPointList.add(new Point(0, 0));
        this.stage = stage;
        this.model = model;
        this.startScreen = new StartScreen();
        tutorialGroup = new TutorialGroup();

        startScene = new Scene(startScreen);
        startScreen.setBackground(brickBackground);
        cell_size = model.getCurrentLevel().getOriginalMap().getBoundY() > model.getCurrentLevel().getOriginalMap().getBoundX() ? GameConstants.MAX_GAMEMAP_SIZE / ((double) model.getCurrentLevel().getOriginalMap().getBoundY()) : GameConstants.MAX_GAMEMAP_SIZE / ((double) model.getCurrentLevel().getOriginalMap().getBoundX());
        cell_size = Math.round(cell_size);
        tutorialTextArea.setEditable(false);
        knightsLeftVBox = new VBox();
//        knightsLeftVBox.setStyle("-fx-background-color: lightgrey");
        knightsLeftVBox.setSpacing(cell_size / 4);
        knightsLeftVBox.setMinWidth(cell_size/1.5);
        levelOverviewPane = new LevelOverviewPane(model, this,false);
        tutorialLevelOverviewPane = new LevelOverviewPane(model, this,true);
//        levelSelectScene = new Scene(levelOverviewPane);
        //TODO: model.getCurrentLevel().addListener(this);
        //Testing
        stage.setWidth(GameConstants.SCREEN_WIDTH);
        stage.setHeight(GameConstants.SCREEN_HEIGHT);
        stage.setMaximized(true);
        stage.setResizable(false);
        if (GameConstants.IS_FULLSCREEN) {
            stage.setFullScreen(true);
        }
        stage.setFullScreenExitHint("");
        codeArea.addNewCodeFieldAtIndex(0, new CodeField("", 1, true));
        model.addChangeListener(this);
        actualMapGPane = new GridPane();
        actualMapGPane.setBorder(new Border(new BorderImage(new Image("file:resources/images/Background_test.png"),new BorderWidths(10),null,new BorderWidths(10),false,BorderRepeat.REPEAT,null)));
        actualMapGPane.setBackground(new Background(new BackgroundImage(new Image("file:resources/images/background_Test.png"),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,BackgroundSize.DEFAULT)));
//        actualMapGPane.setHgap(1);
//        actualMapGPane.setVgap(1);
        rootPane = new StackPane();
        baseContentVBox = new VBox();
//        hBoxRoot.setSpacing(4);
        stage.setScene(startScene);
        vBox = new VBox();
        HBox hBox = new HBox();
        btnExecute = new Button();

        ImageView backBtnIV = new ImageView(GameConstants.BACK_BTN_IMAGE_PATH);
        backBtnIV.setScaleY(GameConstants.HEIGHT_RATIO);
        backBtnIV.setScaleX(GameConstants.WIDTH_RATIO);
        ImageView executeBtnIV = new ImageView(GameConstants.EXECUTE_BTN_IMAGE_PATH);
        executeBtnIV.setScaleY(GameConstants.HEIGHT_RATIO);
        executeBtnIV.setScaleX(GameConstants.WIDTH_RATIO);
        ImageView resetBtnIV = new ImageView(GameConstants.RESET_BTN_IMAGE_PATH);
        resetBtnIV.setScaleY(GameConstants.HEIGHT_RATIO);
        resetBtnIV.setScaleX(GameConstants.WIDTH_RATIO);

        ImageView spellBtnIV = new ImageView(GameConstants.SHOW_SPELLS_BTN_IMAGE_PATH);
        spellBtnIV.setScaleY(GameConstants.HEIGHT_RATIO);
        spellBtnIV.setScaleX(GameConstants.WIDTH_RATIO);

        showSpellBookBtn.setPrefHeight(GameConstants.BUTTON_SIZE*0.75);
        showSpellBookBtn.setGraphic(spellBtnIV);
        backBtn.setPrefSize(GameConstants.BUTTON_SIZE, GameConstants.BUTTON_SIZE/2);
        backBtn.setGraphic(backBtnIV);
        btnExecute.setGraphic(executeBtnIV);

        btnExecute.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        btnReset = new Button();
        btnReset.setGraphic(resetBtnIV);
        btnReset.setPrefSize(BUTTON_SIZE, BUTTON_SIZE);
        btnReset.setDisable(true);
        btnReset.setStyle("-fx-background-color: rgba(0,0,0,0)");
        btnExecute.setStyle("-fx-background-color: rgba(0,0,0,0)");
        backBtn.setStyle("-fx-background-color: rgba(0,0,0,0)");
        showSpellBookBtn.setStyle("-fx-background-color: rgba(0,0,0,0)");
        speedVBox = new VBox();
        Label speedLbl = new Label("Speed");
        speedLbl.setAlignment(Pos.CENTER);
        speedLbl.setFont(GameConstants.MEDIUM_FONT);
        speedLbl.setStyle("-fx-background-color: lightgray");
        speedSlider = new Slider(1, 5, 3);


        speedSlider.setPrefSize(BUTTON_SIZE*1.3, BUTTON_SIZE/2*1.3);
        speedSlider.setBlockIncrement(1);
        speedSlider.setMajorTickUnit(1);
        speedSlider.setMinorTickCount(0);
        speedSlider.setValue(3);
        speedSlider.setSnapToTicks(true);
        speedSlider.setShowTickLabels(false);
        speedSlider.setBackground(new Background(new BackgroundImage(new Image( "file:resources/images/Speed_Slider.png" ), BackgroundRepeat.NO_REPEAT,BackgroundRepeat.NO_REPEAT,BackgroundPosition.CENTER,BackgroundSize.DEFAULT )));
        File f = new File(GameConstants.ROOT_PATH+"/slider_style.css");
        speedSlider.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));
//        speedSlider.getStylesheets().add(getClass().getResource("slider_style.css").toExternalForm());

        speedSlider.setStyle("-fx-base: rgba(0,0,0,0)");
        speedVBox.setAlignment(Pos.BOTTOM_CENTER);
        speedVBox.getChildren().addAll(speedLbl, speedSlider);
        //TODO: delete
        Button debugBtn = new Button("Clipboard");

        debugBtn.setOnAction(event -> {
            String debug = "";
            for (String s : codeArea.getAllText()) {
                debug += "\"" + s + "\",";
            }
            debug = debug.substring(0, debug.length() - 1);
            StringSelection selection = new StringSelection(debug);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        });
        //TODO: delete

        hBox.getChildren().addAll(debugBtn, loadBestCodeBtn, clearCodeBtn);
        if (model.getCurrentLevel().getAIBehaviour().getStatementListSize() > 0)
            aiCodeArea = new CodeArea(model.getCurrentLevel().getAIBehaviour(), isEditor,true);
        else aiCodeArea = new CodeArea(true);
        //"Knight knight = new Knight(WEST);","int turns = 0;","while(true){","if(knight.targetIsUnarmed() && knight.canMove()){","knight.move();","}","else if (knight.canMove() || knight.targetCellIs(GATE)){","knight.wait();","}","else if (knight.targetCellIs(EXIT)){","knight.useItem();","}","else if (knight.targetCellIs(KEY)){","knight.collect();","}","else if (turns < 2){","turns = turns + 1;","knight.turn(EAST);","}","else {","knight.turn(WEST);","}","}"
        if (GameConstants.DEBUG) {
            try {
                codeArea = Tester.evaluateCodeBox(
                        //"Knight knight = new Knight(EAST);","TurnDirection d = LEFT;","TurnDirection dd = d;","knight.collect();","knight.move();","int turns = 0;","boolean b = knight.canMove();","boolean a = b && true;","if (a) {","knight.turn(dd);","}","while(true) {","if ((!knight.targetIsDanger()) && knight.canMove()) {","knight.move();","}","else if (knight.canMove() || knight.targetCellIs(GATE)) {","knight.wait();","}","else if (knight.targetsEntity(SKELETON)) {","knight.useItem();","}","else if (knight.targetsItem(KEY)) {","knight.collect();","}","else if (turns < 2) {","turns = turns + 1;","}","else {","knight.turn(LEFT);","}","}");
          //              "Knight k1 = new Knight();","k1.move();","Knight k2 = new Knight(EAST);","k2.move();","Knight k3 = new Knight(WEST);","Army army = new Army(k1,k2,k3);","boolean b = army.isLooking(EAST);","TurnDirection dir = RIGHT;","Command cc = executeIf(b,turn(LEFT),turn(dir));","boolean bb = true;","while(bb) {","army.executeIf(army.canMove(),move(),cc);","if (army.targetCellIs(PRESSURE_PLATE)) {","bb = false;","}","}");
                        "Knight k = new Knight(NORTH);","k.move();","Knight k2 = new Knight(EAST);","Army a = new Army(k,k2);","while(true) {","if (k.targetCellIs(PRESSURE_PLATE)) {","a.move();","a.executeIf(a.isLooking(EAST),move(),wait());","}","else if (a.canMove()) {","a.move();","}","else if (a.targetsItem()) {","a.collect();","}","else if (k2.targetCellIs(DIRT)) {","a.executeIf(a.isLooking(EAST),turn(RIGHT),useItem());","a.move();","}","else {","a.executeIf(a.isLooking(EAST),turn(LEFT),turn(RIGHT));","}","}");
//                        "Knight knight = new Knight(WEST);","knight.collect();","TurnDirection dir = RIGHT;","for(int i = 0;i <= 6;i = i + 1;) {","for(int j = 0;j < 12;j = j + 1;) {","knight.move();","}","knight.useItem();","knight.turn(dir);","knight.move();","knight.move();","knight.turn(dir);","if (dir == RIGHT) {","dir = LEFT;","}","else {","dir = RIGHT;","}","}");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        vBox.getChildren().addAll(codeArea, hBox, msgLabel);
        VBox leftVBox = new VBox();
        leftVBox.getChildren().add(aiCodeArea);


        aiCodeArea.draw();
        codeArea.select(0, Selection.START);

        Level l = model.getCurrentLevel();

        if (l != null) drawMap(l.getCurrentMap());

        btnExecute.setTooltip(new Tooltip("Will start or pause the game"));
        btnReset.setTooltip(new Tooltip("Will reset the game"));
        backBtn.setTooltip(new Tooltip("Return to Menu"));
        speedSlider.setTooltip(new Tooltip("Control the speed of the game"));
        showSpellBookBtn.setTooltip(new Tooltip("Show/Hide the Spellbook"));
    }


    public void drawMap(GameMap map) {
        actualMapGPane.getChildren().clear();
        actualMapGPane.getChildren().addAll(getGridPaneFromMap(map).getChildren());
        //TODO!
//        actualMapGPane.setBackground(brickBackground);
        knightsLeftVBox.getChildren().clear();
        knightsLeftVBox.setSpacing(cell_size/4);
        knightsLeftVBox.setMinWidth(cell_size/1.5);
        for (int i = 0; i < model.getCurrentLevel().getMaxKnights() - model.getCurrentLevel().getUsedKnights(); i++) {
            ImageView tokenIView = new ImageView(new  Image(GameConstants.KNIGHT_TOKEN_PATH));
            tokenIView.setFitHeight(cell_size/1.5);
            tokenIView.setFitWidth(cell_size/1.5);
            knightsLeftVBox.getChildren().add(tokenIView);
//            System.out.println(model.getCurrentLevel().getName()+", " +getCurrentSceneState().name()+": " +cell_size);
        }
        changeSupport.firePropertyChange("map", null, null);
    }

    private GridPane getGridPaneFromMap(GameMap map) {
        calculateCellSize();
        File folder = new File(Paths.get(GameConstants.ROOT_PATH + "/images").toString());
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            String s = file.getName().replaceAll("\\.png", "");
            contentImageMap.put(s, new Image("file:resources/images/" + s + ".png", cell_size, cell_size, true, true));
        }
        GridPane gPane = new GridPane();
        gPane.getChildren().clear();
        gPane.setHgap(0);
        gPane.setVgap(0);
        mapShapes = new Shape[map.getBoundX()][map.getBoundY()];
        for (int x = 0; x < map.getBoundX(); x++) {
            for (int y = 0; y < map.getBoundY(); y++) {
                final Cell cell = map.getCellAtXYClone(x, y);
//                drawCell(map[row][column],column,row);
                Shape shape = mapShapes[x][y] = getCellShape(cell);
//                GridPane.setRowIndex(mapShapes[row][column],row);
//                GridPane.setColumnIndex(mapShapes[row][column],column);
                StackPane stackPane = new StackPane();
                String contentString = cell.getContent().getDisplayName();

                boolean isTurned = false;
                boolean isInverted = false;
                boolean isOpen = false;
//                boolean isTriggered=false;
                for (CFlag flag : CFlag.values()) {
                    if (cell.hasFlag(flag)) {
                        if(flag.isTemporary())
                            continue;
//                            if(cell.hasFlag(CFlag.KNIGHT_DEATH) && contentImageMap.containsKey(entityName+"_Death"))entityName+="_Death";
                        if(flag == CFlag.TURNED && (isTurned = true))continue;
                        if(flag == CFlag.INVERTED && cell.getContent()==CContent.GATE){
                            isInverted = true;
                            if(!isOpen)contentString += "_" + CFlag.OPEN.getDisplayName();
                            else contentString = contentString.replace("_" + CFlag.OPEN.getDisplayName(),"");
                            continue;
                        }
                        if(flag == CFlag.OPEN ){
                            isOpen = true;
                            if(!isInverted)contentString += "_" + CFlag.OPEN.getDisplayName();
                            else contentString = contentString.replace("_" + CFlag.OPEN.getDisplayName(),"");
                            continue;
                        }
                        if(flag == CFlag.INVERTED && cell.getContent()==CContent.PRESSURE_PLATE && model.getCurrentLevel().getTurnsTaken()==0){
                            isInverted = true;
                            contentString += "_"+CFlag.INVERTED.getDisplayName()+ "_" + CFlag.TRIGGERED.getDisplayName();
                            continue;
                        }
//                        if(flag == CFlag.TRIGGERED ){
//                            isTriggered = true;
//                            if(!isInverted)contentString += "_" + CFlag.TRIGGERED.getDisplayName();
//                            else contentString = contentString.replace("_" + CFlag.TRIGGERED.getDisplayName(),"");
//                            continue;
//                        }
                        contentString += "_" + flag.getDisplayName();
                    }
                }
                if (shape != null && !contentImageMap.containsKey(contentString)) stackPane.getChildren().add(shape);
                else {
                    ImageView imageView = new ImageView(contentImageMap.get(contentString));
                    if(isTurned)imageView.setRotate(270);
                    if((model.getCurrentLevel().getUsedKnights() < model.getCurrentLevel().getMaxKnights()&&cell.getContent()==CContent.SPAWN))
                        switch (model.getCurrentLevel().getUsedKnights()){
                            case 1: imageView.setEffect(GameConstants.GREEN_ADJUST);
                                break;
                            case 2: imageView.setEffect(GameConstants.VIOLET_ADJUST);
                                break;
                            case 3: imageView.setEffect(GameConstants.LAST_ADJUST);
                                break;
                        }
                    if(cell.getContent()==CContent.ENEMY_SPAWN)
                        switch (entityColorMap.size() -model.getCurrentLevel().getUsedKnights()){
                            case 1: imageView.setEffect(GameConstants.GREEN_ADJUST);
                                break;
                            case 2: imageView.setEffect(GameConstants.VIOLET_ADJUST);
                                break;
                            case 3: imageView.setEffect(GameConstants.LAST_ADJUST);
                                break;
                        }
                    stackPane.getChildren().add(imageView);
                }
                String itemString = cell.getItem() != ItemType.NONE ? cell.getItem().getDisplayName() : "";
                if (cell.getItem() != ItemType.NONE && !contentImageMap.containsKey(itemString))
                    stackPane.getChildren().add(getItemShape(cell.getItem()));
                else {
                    ImageView imageView = new ImageView(contentImageMap.get(itemString));
//                    imageView.setFitWidth(cell_size);
//                    imageView.setFitHeight(cell_size);


                    stackPane.getChildren().add(imageView);
                }

                gPane.add(stackPane, x, y);
//                actualMapGPane.getChildren().add(mapShapes[row][column]);
            }
        }
        for (int x = 0; x < map.getBoundX(); x++) {
            for (int y = 0; y < map.getBoundY(); y++) {
                final Cell cell = map.getCellAtXYClone(x, y);
//                drawCell(map[row][column],column,row);
//                GridPane.setRowIndex(mapShapes[row][column],row);
//                GridPane.setColumnIndex(mapShapes[row][column],column);
                int number = 1;
                StackPane stackPane = new StackPane();
                stackPane.setMouseTransparent(true);
//                if(cell.getEntity()!=null)stackPane.getChildren().add(getEntityShape(cell.getEntity()));
                if (cell.getEntity() != NO_ENTITY) {
                    if(entityActionList.contains(cell.getEntity())){
                        number = 3;
                    }
                    String entityName = cell.getEntity().getEntityType().getDisplayName();
                    if(cell.getEntity().getItem()!= ItemType.NONE && contentImageMap.containsKey(entityName+"_"+cell.getEntity().getItem().getDisplayName()))entityName+="_"+cell.getEntity().getItem().getDisplayName();
                    if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName+"_Action_"+number))entityName+="_Action_"+number;
                    ImageView imageView = new ImageView(contentImageMap.get(entityName));

                    //COLOR EXPERIMENT:
                    int knightCount = 0;
                    int skeletonCount = 0;
                    for(String s : entityColorMap.keySet()){
                        if(model.getCurrentLevel().getCurrentMap().getEntity(s).getEntityType()==EntityType.KNIGHT)knightCount++;
                    }
                    if(cell.getEntity().getEntityType() == EntityType.KNIGHT){
                        switch (knightCount){
                            case 0: entityColorMap.putIfAbsent(cell.getEntity().getName(), new ColorAdjust());
                                break;
                            case 1: entityColorMap.putIfAbsent(cell.getEntity().getName(), GameConstants.GREEN_ADJUST);
                                break;
                            case 2: entityColorMap.putIfAbsent(cell.getEntity().getName(), GameConstants.VIOLET_ADJUST);
                                break;
                            case 3: entityColorMap.putIfAbsent(cell.getEntity().getName(), GameConstants.LAST_ADJUST);
                                break;
                        }
                    }
                    skeletonCount = entityColorMap.size()-knightCount;
                    if(cell.getEntity().getEntityType() == EntityType.SKELETON){
                        switch (skeletonCount){
                            case 0: entityColorMap.putIfAbsent(cell.getEntity().getName(), new ColorAdjust());
                                break;
                            case 1: entityColorMap.putIfAbsent(cell.getEntity().getName(), GameConstants.GREEN_ADJUST);
                                break;
                            case 2: entityColorMap.putIfAbsent(cell.getEntity().getName(), GameConstants.VIOLET_ADJUST);
                                break;
                            case 3: entityColorMap.putIfAbsent(cell.getEntity().getName(), GameConstants.LAST_ADJUST);
                                break;
                        }
                    }
                    imageView.setEffect(entityColorMap.get(cell.getEntity().getName()));

                    //END OF COLOR EXPERIMENT

                    stackPane.getChildren().add(imageView);
                    ImageView iView=null;
                    if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName))
                    {
                        if(entityActionList.contains(cell.getEntity()))
                            entityActionList.remove(cell.getEntity());
                        else entityActionList.add(cell.getEntity());
                        iView = new ImageView(contentImageMap.get(entityName.replace(""+number, ""+(number+1))));
                    }
                    else
                    if(entityActionList.contains(cell.getEntity()))
                        entityActionList.remove(cell.getEntity());
                    switch (cell.getEntity().getDirection()) {
                        case NORTH:
                            imageView.setRotate(90);
                            if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName)){
                                iView.setRotate(90);

                                iView.setEffect(entityColorMap.get(cell.getEntity().getName()));
                                gPane.add(iView, x, y-1);
                            }
                            break;
                        case SOUTH:
                            imageView.setRotate(270);
                            if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName)){
                                iView.setRotate(270);
                                iView.setEffect(entityColorMap.get(cell.getEntity().getName()));
                                gPane.add(iView, x, y+1);
                            }
                            break;
                        case EAST:
                            imageView.setRotate(180);
                            if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName)){
                                iView.setRotate(180);
                                iView.setEffect(entityColorMap.get(cell.getEntity().getName()));
                                gPane.add(iView, x+1, y);
                            }
                            break;
                        case WEST:
                            if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName)){
                                iView.setEffect(entityColorMap.get(cell.getEntity().getName()));
                                gPane.add(iView, x-1, y);
                            }
                            break;
                    }
                }

                if(cell.hasFlag(CFlag.KNIGHT_DEATH))
                    stackPane.getChildren().add(new ImageView(contentImageMap.get(CFlag.KNIGHT_DEATH.getDisplayName())));
                else if (cell.hasFlag(CFlag.SKELETON_DEATH))stackPane.getChildren().add(new ImageView(contentImageMap.get(CFlag.SKELETON_DEATH.getDisplayName())));
                else if (cell.hasFlag(CFlag.DIRT_REMOVED))stackPane.getChildren().add(new ImageView(contentImageMap.get(CFlag.DIRT_REMOVED.getDisplayName())));
                else if (cell.hasFlag(CFlag.KEY_DESTROYED))stackPane.getChildren().add(new ImageView(contentImageMap.get(CFlag.KEY_DESTROYED.getDisplayName())));
                else if (cell.hasFlag(CFlag.ITEM_DESTROYED))
                    stackPane.getChildren().add(new ImageView(contentImageMap.get(CFlag.ITEM_DESTROYED.getDisplayName())));
                gPane.add(stackPane, x, y);
//                actualMapGPane.getChildren().add(mapShapes[row][column]);
            }
        }
        return gPane;
    }

    private void calculateCellSize() {
        cell_size = model.getCurrentLevel().getOriginalMap().getBoundY() > model.getCurrentLevel().getOriginalMap().getBoundX() ? GameConstants.MAX_GAMEMAP_SIZE / ((double) model.getCurrentLevel().getOriginalMap().getBoundY()) : GameConstants.MAX_GAMEMAP_SIZE / ((double) model.getCurrentLevel().getOriginalMap().getBoundX());

        if(getCurrentSceneState() == SceneState.PLAY ||getCurrentSceneState() == SceneState.TUTORIAL)
            cell_size = cell_size*GameConstants.PLAY_CELL_SIZE_FACTOR;
        cell_size = Math.round(cell_size);
    }

    private Shape getItemShape(ItemType content) {
        Color color = Color.BLACK;
        switch (content) {
            case KEY:
                color = Color.GOLD;
                break;
            case SWORD:
                color = Color.LIGHTGRAY;
                break;
            case SHOVEL:
                color = Color.DARKGRAY;
                break;
            case BOULDER:
                color = Color.GRAY;
                break;
        }
        Circle circle = new Circle(cell_size / 2, cell_size / 2, cell_size / 2);
        circle.setFill(color);
        return circle;
    }

    private Shape getCellShape(Cell cell) {
        CContent content = cell.getContent();
        Shape shape;
        Color color = Color.WHITE;
//        canvas.getGraphicsContext2D().setFill(Color.WHITE);
//        canvas.getGraphicsContext2D().fillRect(column*cell_size,row*cell_size,cell_size,cell_size);
        switch (content) {
            case EMPTY:
                color = Color.TRANSPARENT;
                break;
            case DIRT:
                color = Color.SANDYBROWN;
                break;
            case GATE:
                if (cell.hasFlag(CFlag.OPEN)) color = Color.WHITE;
                else color = Color.DARKGRAY;
                break;
            case EXIT:
//                if (cell.hasFlag(CFlag.OPEN)) color = Color.CHOCOLATE;
//                else color = Color.BROWN;
                break;
            case PATH:
                color = Color.WHITE;
                break;
            case SPAWN:
                color = Color.BLUE;
                break;
            case ENEMY_SPAWN:
                color = Color.VIOLET;
                break;
            case PRESSURE_PLATE:
                if (cell.hasFlag(CFlag.TRIGGERED)) color = Color.LIMEGREEN;
                else color = Color.LIGHTGREEN;
                break;
            case TRAP:
                if (cell.hasFlag(CFlag.PREPARING)) color = Color.ORANGE;
                else if (cell.hasFlag(CFlag.ARMED)) color = Color.RED;
                else color = Color.YELLOW;
                break;
            case WALL:
                color = Color.GRAY;
                break;
        }

//        .setBackground(new Background(new BackgroundFill(color,new CornerRadii(5,true),null)));

        shape = new Rectangle(0, 0, cell_size, cell_size);
        shape.setFill(color);

//        ((Rectangle) shape).setHeight(cell_size);
//        ((Rectangle) shape).setWidth(cell_size);

        return shape;
    }

    //TODO: delete
    private Shape getEntityShape(Entity entity) {
        Color color = Color.BLACK;
        Shape shape = null;
        switch (entity.getEntityType()) {
            case KNIGHT:
                color = Color.LIGHTGRAY;
                break;
            case SKELETON:
                color = Color.PURPLE;
                break;
        }
        switch (entity.getDirection()) {
            case NORTH:
                shape = new Polygon(0, cell_size, cell_size / 2, 0, cell_size, cell_size);
                break;
            case SOUTH:
                shape = new Polygon(0, 0, cell_size, 0, cell_size / 2, cell_size);
                break;
            case EAST:
                shape = new Polygon(0, cell_size, cell_size, cell_size / 2, 0, 0);
                break;
            case WEST:
                shape = new Polygon(0, cell_size / 2, cell_size, cell_size, cell_size, 0);
                break;
        }
        shape.setFill(color);
        return shape;
    }


//    @Contract("null -> fail")
    public void setCodeArea(CodeArea codeArea,boolean isAi) {
        if(isAi){
            if (codeArea == null)
                throw new IllegalArgumentException("null is no longer allowed as AiCodeArea! Please use an empty CodeArea instead!");
            this.aiCodeArea = codeArea;
            leftVBox.getChildren().clear();
            if(codeArea.getSize()>0)
            {
                ImageView redIconIView = new ImageView(new Image(GameConstants.RED_SCRIPT_ICON_PATH));
                redIconIView.setFitWidth(BUTTON_SIZE/2.5);
                redIconIView.setFitHeight(BUTTON_SIZE/2.5);
                leftVBox.getChildren().addAll(redIconIView, aiCodeArea);
            }
            leftVBox.setAlignment(Pos.TOP_CENTER);
        }
        else {this.codeArea = codeArea;
        vBox.getChildren().set(0, codeArea);}
        codeArea.draw();
        changeSupport.firePropertyChange("codeArea", null, codeArea);
    }

    public CodeArea getAICodeArea() {
        return aiCodeArea;
    }

    public GridPane getActualMapGPane() {
        return actualMapGPane;
    }

    public void highlightInMap(List<Point> points) {
        this.selectedPointList = new ArrayList<>(points);
        for (Polyline highlight: highlights) {
            rootPane.getChildren().remove(highlight);
        }
        highlights = new ArrayList<>();
        Polyline highlight;
        List<Line> edgeList = new ArrayList<>();
        for(int x = 0; x < model.getCurrentLevel().getOriginalMap().getBoundX(); x++)
        for(int y = 0; y < model.getCurrentLevel().getOriginalMap().getBoundY(); y++) {
            if (!points.contains(new Point(x, y))) continue;
//            System.out.println("X: " +x + ", Y:" +y);
            double dx = x * cell_size;
            double dy = y * cell_size;
            if (y == 0 || !points.contains(new Point(x, y - 1))) edgeList.add(new Line(dx, dy, dx + cell_size, dy));
            if (x == 0 || !points.contains(new Point(x - 1, y))) edgeList.add(new Line(dx, dy + cell_size, dx, dy));
            if (x == model.getCurrentLevel().getOriginalMap().getBoundX() - 1 || !points.contains(new Point(x + 1, y)))
                edgeList.add(new Line(dx + cell_size, dy, dx + cell_size, dy + cell_size));
            if (y == model.getCurrentLevel().getOriginalMap().getBoundY() - 1 || !points.contains(new Point(x, y + 1)))
                edgeList.add(new Line(dx + cell_size, dy + cell_size, dx, dy + cell_size));
        }

        if(edgeList.size()== 0)return;
        for(Double[] doubles : Util.orderLines(edgeList, new ArrayList<>())){
            highlight = new Polyline();
            highlight.getPoints().addAll(doubles);
            highlight.setStroke(Color.WHITE);
            highlight.setSmooth(true);
            highlight.setStrokeWidth(2);
            highlight.setStrokeType(StrokeType.INSIDE);

            rootPane.getChildren().add(highlight);
            StackPane.setAlignment(highlight, Pos.TOP_LEFT);
            actualMapGPane.autosize();
            highlight.autosize();
            //TODO: +10 wegen Border
            actualMapGPane.layout();
            highlight.setTranslateX(actualMapGPane.localToScene(actualMapGPane.getBoundsInLocal()).getMinX()+highlight.getLayoutBounds().getMinX()+10);
            highlight.setTranslateY(actualMapGPane.localToScene(actualMapGPane.getBoundsInLocal()).getMinY()+highlight.getLayoutBounds().getMinY()+10);
            highlights.add(highlight);

//            System.out.println(""+actualMapGPane.localToScene(actualMapGPane.getBoundsInLocal()).getMinX()+" "+highlight.getLayoutBounds().getMinX());
        }
    }

    public GridPane getCellTypeSelectionPane() {
        return levelEditorModule.getCellTypeSelectionGPane();
    }

    public ListView<Integer> getLinkedCellsListView() {
        return levelEditorModule.getLinkedCellListView();
    }

    public LevelEditorModule getLevelEditorModule() {
        return levelEditorModule;
    }

    public void setCContentButtonDisabled(CContent content, boolean b) {
        for (Node n : levelEditorModule.getCellTypeSelectionGPane().getChildren()) {
            Button btn = (Button) n;
            if (btn.getText().equals(content.getDisplayName())) btn.setDisable(b);
        }
    }

    public void setItemButtonDisabled(ItemType item, boolean b) {
        for (Node n : levelEditorModule.getCellItemSelectionGPane().getChildren()) {
            Button btn = (Button) n;
            if (btn.getText().equals(item.getDisplayName())) btn.setDisable(b);
        }
    }

    public void setAllItemBtnsDisable(boolean b) {
        for (Node btn : getCellItemSelectionPane().getChildren()) {
            btn.setDisable(b);
        }
    }

    public void setAllCellButtonsDisabled(boolean b) {
        for (Node n : levelEditorModule.getCellTypeSelectionGPane().getChildren()) {
            Button btn = (Button) n;
//            if (!btn.getText().equals(CContent.EMPTY.getDisplayName()) && !btn.getText().equals(CContent.WALL.getDisplayName()))
            btn.setDisable(b);
        }
        for (Node n : levelEditorModule.getCellItemSelectionGPane().getChildren()) {
            Button btn = (Button) n;
            btn.setDisable(b);
        }
    }

    public GridPane getCellItemSelectionPane() {
        return levelEditorModule.getCellItemSelectionGPane();
    }

    private void updateLevelEditorModule() {
        levelEditorModule.getLevelNameTField().setText(model.getCurrentLevel().getName());
        levelEditorModule.getWidthValueLbl().setText("" + model.getCurrentLevel().getOriginalMap().getBoundX());
        levelEditorModule.getHeightValueLbl().setText("" + model.getCurrentLevel().getOriginalMap().getBoundY());
        levelEditorModule.setLOCToStarsValues(model.getCurrentLevel().getLocToStars());
        levelEditorModule.setTurnsToStarsValues(model.getCurrentLevel().getTurnsToStars());
        levelEditorModule.setRequiredLevels(model.getCurrentLevel().getRequiredLevels());
        levelEditorModule.getIsTutorialValueLbl().setText(model.getCurrentLevel().isTutorial() + "");
        levelEditorModule.getIndexValueLbl().setText((model.getCurrentLevel().getIndex() + 1) + "");
        levelEditorModule.getTutorialVBox().setVisible(model.getCurrentLevel().isTutorial());
        levelEditorModule.updateTutorialSection(model.getCurrentLevel());
        levelEditorModule.getMaxKnightsValueLbl().setText(model.getCurrentLevel().getMaxKnights()+"");
        levelEditorModule.getHasAiValueLbl().setText(model.getCurrentLevel().hasAi()+"");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            default:

            case "level":
                entityColorMap = new HashMap<>();
                selectedPointList = new ArrayList<>();
                selectedPointList.add(new Point(0, 0));
                if (model.getCurrentLevel().hasAi()) {
                    aiCodeArea = new CodeArea(model.getCurrentLevel().getAIBehaviour(),true);
                    setCodeArea(aiCodeArea,true);
                } else {
                    setCodeArea(new CodeArea(new ComplexStatement(), false),true);
                }
                if (sceneState == SceneState.LEVEL_EDITOR) updateLevelEditorModule();
                spellBookPane.updateSpellbookEntries(model.getCurrentLevel().getUnlockedStatementList());
                if(!levelNameLabel.getText().equals(model.getCurrentLevel().getName())){
                tutorialGroup.setEntries(model.getCurrentLevel().getTutorialEntryList());
                levelNameLabel.setText(model.getCurrentLevel().getName());}
            case "map":
                drawMap(model.getCurrentLevel().getOriginalMap());
                if(sceneState == SceneState.LEVEL_EDITOR)
                Platform.runLater(()->highlightInMap(selectedPointList));
                break;
            case "playerBehaviour":
//                codeArea = new CodeArea(model.getCurrentLevel().getPlayerBehaviour());
//                setCodeArea(codeArea);
//                codeArea.draw();
                break;
            case "name":
                levelEditorModule.getLevelNameTField().setText("" + model.getCurrentLevel().getName());
                break;
            case "width":
                levelEditorModule.getWidthValueLbl().setText(model.getCurrentLevel().getOriginalMap().getBoundX() + "");

                selectedPointList = pointListOutOfBounds();
                drawMap(model.getCurrentLevel().getOriginalMap());
                Platform.runLater(()->highlightInMap(selectedPointList));
                break;
            case "height":
                levelEditorModule.getHeightValueLbl().setText(model.getCurrentLevel().getOriginalMap().getBoundY() + "");

                selectedPointList = pointListOutOfBounds();
                drawMap(model.getCurrentLevel().getOriginalMap());
                Platform.runLater(()->highlightInMap(selectedPointList));
                break;
            case "locToStars":
                Integer[] locToStars = model.getCurrentLevel().getLocToStars();
                levelEditorModule.setLOCToStarsValues(locToStars);
                break;
            case "turnsToStars":
                Integer[] turnsToStars = model.getCurrentLevel().getTurnsToStars();
                levelEditorModule.setTurnsToStarsValues(turnsToStars);
                break;
            case "maxKnights":
                levelEditorModule.getMaxKnightsValueLbl().setText("" + model.getCurrentLevel().getMaxKnights());
                knightsLeftVBox.getChildren().clear();
                for (int i = 0; i < model.getCurrentLevel().getMaxKnights(); i++) {
                    ImageView tokenIView = new ImageView(new  Image(GameConstants.KNIGHT_TOKEN_PATH));
                    tokenIView.setFitHeight(cell_size);
                    tokenIView.setFitWidth(cell_size);
                    knightsLeftVBox.getChildren().add(tokenIView);
                }
                break;
            case "aiBehaviour":
//                System.out.println("First");
                ComplexStatement aiBehaviour = (ComplexStatement) evt.getNewValue();
                if (aiBehaviour.getStatementListSize() > 0) levelEditorModule.getHasAiValueLbl().setText("" + true);
                else levelEditorModule.getHasAiValueLbl().setText("" + false);
                if (model.getCurrentLevel().hasAi() && ((ComplexStatement) evt.getOldValue()).getStatementListSize() == 0) {
                    aiCodeArea = new CodeArea(model.getCurrentLevel().getAIBehaviour(),true);
                    //THIS IS THE ONLY WORKING SOLUTION I FOUND FOR FINDING OUT POSITIONS OF NODES IN SCENE!!
                    double d = getActualMapGPane().localToScene(getActualMapGPane().getBoundsInLocal()).getMinX();
                    setCodeArea(aiCodeArea,true);
                    Platform.runLater(() -> {
                        for(Polyline p: highlights){
                            p.setTranslateX(p.getTranslateX()-d+getActualMapGPane().localToScene(getActualMapGPane().getBoundsInLocal()).getMinX());
                        }});
                } else if (!model.getCurrentLevel().hasAi()) {
                    //THIS IS THE ONLY WORKING SOLUTION I FOUND FOR FINDING OUT POSITIONS OF NODES IN SCENE!!
                    double d = getActualMapGPane().localToScene(getActualMapGPane().getBoundsInLocal()).getMinX();
                    setCodeArea(new CodeArea(new ComplexStatement(), false),true);
                    Platform.runLater(() -> {
                    for(Polyline p: highlights){
                        p.setTranslateX(p.getTranslateX()-d+getActualMapGPane().localToScene(getActualMapGPane().getBoundsInLocal()).getMinX());
                    }});
                }
                break;
            case "isTutorial":
                levelEditorModule.getTutorialVBox().setVisible(model.getCurrentLevel().isTutorial());
                levelEditorModule.updateTutorialSection(model.getCurrentLevel());
                levelEditorModule.getIsTutorialValueLbl().setText("" + model.getCurrentLevel().isTutorial());
                break;
            case "index":
                levelEditorModule.getIndexValueLbl().setText("" + (model.getCurrentLevel().getIndex() + 1));
            case "requiredLevels":
                List<String> requiredLevelsList = model.getCurrentLevel().getRequiredLevels();
                levelEditorModule.setRequiredLevels(requiredLevelsList);
                break;
            case "tutorial":
                levelEditorModule.getTutorialTextArea().setText("" + evt.getNewValue());
                break;
            case "tutorialDeletion":
                int index = (int) evt.getNewValue();
                if (model.getCurrentLevel().getTutorialEntryList().size() > index)
                    levelEditorModule.getTutorialTextArea().setText("" + model.getCurrentLevel().getTutorialEntryList().get(index));
                else {
                    levelEditorModule.getTutorialTextArea().setText("" + model.getCurrentLevel().getTutorialEntryList().get(index - 1));
                    levelEditorModule.getTutorialNumberValueLbl().setText("" + (index));
                }
                break;
            case "linkedCellId":
            case "cellId":
                changeSupport.firePropertyChange("map", null, null);
                break;
        }
    }

    private List<Point> pointListOutOfBounds() {
        List<Point> output = new ArrayList<>();
        for(Point p : selectedPointList){
            if(p.getX() >= model.getCurrentLevel().getOriginalMap().getBoundX()||p.getY() >= model.getCurrentLevel().getOriginalMap().getBoundY())continue;
            output.add(p);
        }
        return output;
    }

    public StartScreen getStartScreen() {
        return startScreen;
    }


    public void setSceneState(SceneState sceneState) {
        this.sceneState = sceneState;
        switch (sceneState) {
            case START_SCREEN:
                stage.getScene().setRoot(startScreen);
                break;
            case LEVEL_EDITOR:
                prepareRootPane();
                drawMap(model.getCurrentLevel().getOriginalMap());
                aiCodeArea.setEditable(true);
                aiCodeArea.deselectAll();
                codeArea.deselectAll();
                Platform.runLater(()->codeArea.select(0, Selection.END));
                stage.getScene().setRoot(rootPane);
                break;
            case LEVEL_SELECT:
                stage.getScene().setRoot(levelOverviewPane);
                levelOverviewPane.getLevelListView().getSelectionModel().select(0);
                levelOverviewPane.setBackground(brickBackground);
                break;

            case TUTORIAL_LEVEL_SELECT:
                stage.getScene().setRoot(tutorialLevelOverviewPane);
                tutorialLevelOverviewPane.getLevelListView().getSelectionModel().select(0);
                tutorialLevelOverviewPane.setBackground(brickBackground);
                break;
            case PLAY:
                drawMap(model.getCurrentLevel().getOriginalMap());
                prepareRootPane();
                aiCodeArea.deselectAll();
                codeArea.deselectAll();
                Platform.runLater(()->codeArea.select(0, Selection.END));
                levelOverviewPane.updateUnlockedLevels(model, this);
                stage.getScene().setRoot(rootPane);
                break;
            case TUTORIAL:
                drawMap(model.getCurrentLevel().getOriginalMap());
                prepareRootPane();
                aiCodeArea.deselectAll();
                codeArea.deselectAll();
                Platform.runLater(()->codeArea.select(0, Selection.END));
//                levelOverviewPane.updateUnlockedLevels(model, this);

                stage.getScene().setRoot(rootPane);
                break;

        }
    }

    private void prepareRootPane() {
        rootPane = new StackPane();
        rootPane.setPrefSize(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        levelNameLabel.setText(model.getCurrentLevel().getName());
        levelNameLabel.setFont(GameConstants.BIGGEST_FONT);
        levelNameLabel.setStyle("-fx-background-color: lightgray");
        HBox contentHBox = new HBox();
        setCodeArea(aiCodeArea,true);
        setCodeArea(codeArea,false);
        rightVBox = new VBox();
        centerVBox = new VBox();
        rightVBox.setAlignment(Pos.TOP_RIGHT);
        knightsLeftVBox.setAlignment(Pos.TOP_RIGHT);
        //TODO

        ImageView blueIconIView = new ImageView(new Image(GameConstants.BLUE_SCRIPT_ICON_PATH));
        blueIconIView.setFitWidth(BUTTON_SIZE/2.5);
        blueIconIView.setFitHeight(BUTTON_SIZE/2.5);
        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.getChildren().addAll(blueIconIView, vBox);
        baseContentVBox.getChildren().clear();
        HBox topCenterHBox;

        HBox bottomHBox = new HBox(backBtn, btnExecute, speedVBox, btnReset, showSpellBookBtn);
        bottomHBox.setAlignment(Pos.BOTTOM_CENTER);
        switch (sceneState) {
            case LEVEL_EDITOR:
                levelEditorModule = new LevelEditorModule(model.getCurrentLevel());
                HBox editorCenterHBox = new HBox(knightsLeftVBox, new VBox(actualMapGPane), new VBox(levelEditorModule.getRightVBox()));
                editorCenterHBox.autosize();
                editorCenterHBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/1.5);
                editorCenterHBox.setAlignment(Pos.TOP_CENTER);
                centerVBox.getChildren().addAll(levelEditorModule.getBottomHBox(), editorCenterHBox);
                centerVBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/2);
//                editorScene = new Scene(rootPane);
                baseContentVBox.getChildren().add(levelEditorModule.getTopHBox());
                levelEditorModule.getTutorialVBox().setVisible(model.getCurrentLevel().isTutorial());
                break;
            case LEVEL_SELECT:
                throw new IllegalStateException("Missing error message please TODO! see View -> prepareRootPane()");
            case TUTORIAL_LEVEL_SELECT:
                throw new IllegalStateException("Missing error message please TODO! see View -> prepareRootPane()");
            case PLAY:
//                topCenterHBox = new HBox(levelNameLabel, );

                HBox centerHBox = new HBox(knightsLeftVBox, actualMapGPane);
                centerHBox.autosize();
                centerHBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/1.5);
                centerHBox.setAlignment(Pos.TOP_CENTER);
                centerVBox.getChildren().addAll(levelNameLabel, centerHBox);
//                centerVBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/2);
//                playScene = new Scene(rootPane);
                break;
            case START_SCREEN:
                throw new IllegalStateException("Missing error message please TODO! see View -> prepareRootPane()");
            case TUTORIAL:
                centerHBox = new HBox(knightsLeftVBox, actualMapGPane);
                centerHBox.autosize();
                centerHBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/1.5);
                centerHBox.setAlignment(Pos.TOP_CENTER);
                centerVBox.getChildren().addAll(levelNameLabel,centerHBox);
//                tutorialScene = new Scene(rootPane);
                try {
                    if(JSONParser.getTutorialProgressIndex()==-1){
                        isIntroduction = true;
                        tutorialGroup.activateIntroduction();
                        btnExecute.setMouseTransparent(true);
                        codeArea.setDisable(true);
                        speedSlider.setMouseTransparent(true);
                        showSpellBookBtn.setMouseTransparent(true);
//                        stage.getScene().setRoot(introductionPane);
//                        introductionPane.getTutorialGroup().getNextBtn().requestFocus();
                    }
//                    else {
//                        stage.getScene().setRoot(rootPane);
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(isIntroduction){
                    tutorialGroup.setEntries(Util.StringListFromArray(GameConstants.TUTORIAL_LINE_1,GameConstants.TUTORIAL_LINE_2,GameConstants.TUTORIAL_LINE_3,GameConstants.TUTORIAL_LINE_4,GameConstants.TUTORIAL_LINE_5));
                }
                else tutorialGroup.setEntries(model.getCurrentLevel().getTutorialEntryList());
                break;
        }
        contentHBox.getChildren().addAll(leftVBox, centerVBox, rightVBox);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setSpacing(BUTTON_SIZE/5);
        contentHBox.setPrefWidth(GameConstants.SCREEN_WIDTH);
        bottomHBox.setSpacing(BUTTON_SIZE);
        bottomHBox.setPickOnBounds(false);
        bottomHBox.setMaxHeight(BUTTON_SIZE);

        rootPane.autosize();
        baseContentVBox.autosize();
        contentHBox.autosize();
        centerVBox.autosize();
        bottomHBox.autosize();

        if(getCurrentSceneState() == SceneState.LEVEL_EDITOR)
            centerVBox.setPrefHeight(GameConstants.SCREEN_HEIGHT-levelEditorModule.getTopHBox().getLayoutBounds().getHeight());
        centerVBox.setAlignment(Pos.TOP_CENTER);
        StackPane.setAlignment(bottomHBox, Pos.BOTTOM_CENTER);
        bottomHBox.setTranslateY(-GameConstants.SCREEN_HEIGHT/50);

        baseContentVBox.getChildren().addAll(contentHBox);
        rootPane.getChildren().add(baseContentVBox);
        if (getCurrentSceneState() == SceneState.TUTORIAL) {

            rootPane.getChildren().add(tutorialGroup);

            StackPane.setAlignment(tutorialGroup, Pos.BOTTOM_RIGHT);
            if(isIntroduction){
                StackPane.setAlignment(tutorialGroup, Pos.CENTER);}
            StackPane.setMargin(tutorialGroup, new Insets(5));
        }
        rootPane.getChildren().add(bottomHBox );
        rootPane.getChildren().add(spellBookPane);
        spellBookPane.setVisible(false);
        rootPane.setBackground(brickBackground);
    }

    Image getImageFromMap(GameMap originalMap) {
        GridPane gridPane = getGridPaneFromMap(originalMap);
        gridPane.autosize();
        int dimension = gridPane.getHeight() > gridPane.getWidth() ? (int) Math.round(gridPane.getHeight()) : (int) Math.round(gridPane.getWidth());
        BufferedImage bufferedImage = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
        SwingFXUtils.fromFXImage(gridPane.snapshot(new SnapshotParameters(), new WritableImage(dimension, dimension)), bufferedImage);
        Image image = SwingFXUtils.toFXImage(bufferedImage, new WritableImage(dimension, dimension));


        return makeTransparent(image);
    }

    private Image makeTransparent(Image inputImage) {
        int W = (int) inputImage.getWidth();
        int H = (int) inputImage.getHeight();
        WritableImage outputImage = new WritableImage(W, H);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int argb = reader.getArgb(x, y);

                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;

                if (r >= 0xFF
                        && g >= 0xFF
                        && b >= 0xFF) {
                    argb &= 0x00FFFFFF;
                }

                writer.setArgb(x, y, argb);
            }
        }

        return outputImage;
    }

    public LevelOverviewPane getLevelOverviewPane() {
        return levelOverviewPane;
    }

    public Button getBackBtn() {
        return backBtn;
    }

    public SceneState getCurrentSceneState() {
        return sceneState;
    }

    public Button getBtnExecute() {
        return btnExecute;
    }

    public CodeArea getCodeArea() {
        return codeArea;
    }

    public Button getBtnReset() {
        return btnReset;
    }

    public Slider getSpeedSlider() {
        return speedSlider;
    }

    public Label getMsgLabel() {
        return msgLabel;
    }


    public StackPane getRootPane() {
        return rootPane;
    }

    public Button getShowSpellBookBtn() {
        return showSpellBookBtn;
    }

    public void toggleShowSpellBook() {
        spellBookPane.setTranslateX(0);
        spellBookPane.setTranslateY(0);
        if (spellBookPane.isVisible()) {
//            showSpellBookBtn.setText("Show Spellbook");
            spellBookPane.setVisible(false);
            for(Polyline high :highlights){
                high.setVisible(true);
            }
        } else {
//            showSpellBookBtn.setText("Hide Spellbook");
            spellBookPane.setVisible(true);
            for(Polyline high :highlights){
                high.setVisible(false);
            }
        }
        if(codeArea.getSelectedCodeField()!=null)
            codeArea.getSelectedCodeField().requestFocus();
        //TODO: find better solution
//        spellBookPane.setTranslateX(200);

        boolean isVisible = getSpellBookPane().isVisible();
        getActualMapGPane().setMouseTransparent(isVisible);
        if(getCurrentSceneState() == SceneState.LEVEL_EDITOR){
            getCellItemSelectionPane().setMouseTransparent(isVisible);
            getCellTypeSelectionPane().setMouseTransparent(isVisible);
            getLevelEditorModule().getBottomHBox().setMouseTransparent(isVisible);
        }
        getBtnExecute().setMouseTransparent(isVisible);
        getBtnReset().setMouseTransparent(isVisible);
        getSpeedSlider().setMouseTransparent(isVisible);
    }

    public SpellBookPane getSpellBookPane() {
        return spellBookPane;
    }

    public void setNodesDisableWhenRunning(boolean b) {
        backBtn.setDisable(b);
        speedSlider.setDisable(b);
        showSpellBookBtn.setDisable(b);
        loadBestCodeBtn.setDisable(b);
        clearCodeBtn.setDisable(b);
        btnExecute.setDisable(b);
        btnReset.setDisable(!b);
        codeArea.setDisable(b);
        aiCodeArea.setDisable(b);
    }

    public TutorialGroup getTutorialGroup() {
        return tutorialGroup;
    }

    public Button getLoadBestCodeBtn() {
        return loadBestCodeBtn;
    }

    public Button getClearCodeBtn() {
        return clearCodeBtn;
    }

    public IntroductionPane getIntroductionPane() {
        return introductionPane;
    }

    public Stage getStage() {
        return stage;
    }

    public void addPropertyChangeListener(PropertyChangeListener editorController) {
        changeSupport.addPropertyChangeListener(editorController);
    }

    public List<Point> getSelectedPointList() {
        return selectedPointList;
    }

    public void deselect() {
        for (Polyline highlight: highlights) {
            rootPane.getChildren().remove(highlight);
        }
    }

    public boolean isIntroduction() {
        return isIntroduction;
    }

    public void leaveInstructions() {
        tutorialGroup.leaveIntroduction();
        tutorialGroup.setEntries(model.getCurrentLevel().getTutorialEntryList());
        StackPane.setAlignment(tutorialGroup, Pos.BOTTOM_RIGHT);
        isIntroduction = false;

        btnExecute.setMouseTransparent(false);
        codeArea.setDisable(false);
        speedSlider.setMouseTransparent(false);
        showSpellBookBtn.setMouseTransparent(false);
    }

    public LevelOverviewPane getTutorialLevelOverviewPane() {
        return tutorialLevelOverviewPane;
    }
}
//KEYTHIEF: "Knight knight = new Knight(EAST);","int turns = 0;","while(true) {","if ((!knight.targetIsDanger()) && knight.canMove()) {","knight.move();","}","else if (knight.canMove() || knight.targetCellIs(GATE)) {","knight.wait();","}","else if (knight.targetContains(SKELETON) || knight.targetCellIs(EXIT)) {","knight.useItem();","}","else if (knight.targetsItem(SWORD)) {","knight.collect();","knight.turn(LEFT);","knight.move();","knight.move();","knight.turn(RIGHT);","}","else if (knight.targetContains(KEY)) {","knight.collect();","knight.turn(AROUND);","}","else if (turns < 3) {","turns = turns + 2;","knight.turn(LEFT);","}","else {","knight.turn(RIGHT);","turns = turns - 1;","}","}"
//COLLECTANDDROP: "Knight knight = new Knight(NORTH);","knight.move();","knight.turn(RIGHT);","knight.move();","knight.collect();","knight.turn(AROUND);","knight.move();","knight.turn(RIGHT);","knight.collect();","knight.turn(AROUND);","knight.dropItem();","knight.turn(AROUND);","knight.collect();","knight.move();","knight.move();","knight.dropItem();","knight.turn(AROUND);","knight.move();","knight.move();","knight.collect();","knight.turn(AROUND);","knight.move();","knight.move();","knight.turn(AROUND);","knight.dropItem();","knight.turn(AROUND);","knight.collect();","knight.move();","knight.turn(RIGHT);","knight.move();","knight.useItem();"