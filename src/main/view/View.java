package main.view;

import javafx.scene.effect.DropShadow;
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
import main.model.*;
import main.model.gamemap.Cell;
import main.model.gamemap.Entity;
import main.model.gamemap.GameMap;
import main.model.enums.CellContent;
import main.model.enums.CFlag;
import main.model.enums.EntityType;
import main.model.enums.ItemType;
import main.model.statement.ComplexStatement;
import main.parser.CodeParser;
import main.parser.JSONParser;
import main.utility.GameConstants;
//import org.jetbrains.annotations.Contract;
import main.utility.Point;
import main.utility.Util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import static main.utility.GameConstants.*;
import static main.utility.GameConstants.TUTORIAL_LINES;

public class View implements LevelChangeListener {

    private final Background startBackground = new Background(new BackgroundImage(new Image( "file:resources/images/project_background.png", SCREEN_WIDTH,SCREEN_HEIGHT,true,true ), BackgroundRepeat.NO_REPEAT,null,BackgroundPosition.CENTER,BackgroundSize.DEFAULT ));
    private final BackgroundImage backgroundImage = new BackgroundImage(new Image( "file:resources/images/background_tile.png" ), BackgroundRepeat.REPEAT,null,BackgroundPosition.CENTER,BackgroundSize.DEFAULT );
    private final LevelOverviewPane tutorialLevelOverviewPane;
    private Background brickBackground = new Background(backgroundImage);
    private Stage stage;
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    //Testing

    //    private Canvas canvas;
    private double cell_size;
    private StartScreen startScreen;
    private Button btnExecute;
    private Button btnReset;
    //    TextArea codeTextArea;
    private CodeArea codeArea = new CodeArea(false);
    private CodeArea aiCodeArea = new CodeArea(true);
    private VBox vBox;
    private Slider speedSlider;
    private Label errorLabel = new Label();
    private Label errorLabelAI = new Label();
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
    private Button clearAICodeBtn = new Button("Clear Code");
    private Button storeCodeBtn = new Button("Store Code");

    //TODO: for visual purposes:
    List<Entity> entityActionList = new ArrayList<>();
    Map<String, Effect> entityColorMap = new HashMap<>();
    private boolean isIntroduction;

    private static View instance = null;

    public static View getInstance(Stage stage){
        if(instance == null)instance = new View(stage);
        return instance;
    }

    private View( Stage stage) {
        errorLabel.setStyle("-fx-text-fill: red;-fx-background-color: white");
        errorLabel.setFont(GameConstants.BIG_FONT);
        errorLabel.setMaxWidth(GameConstants.TEXTFIELD_WIDTH);
        errorLabel.setVisible(false);
        errorLabelAI.setStyle("-fx-text-fill: red;-fx-background-color: white");
        errorLabelAI.setFont(GameConstants.BIG_FONT);
        errorLabelAI.setMaxWidth(GameConstants.TEXTFIELD_WIDTH);
        errorLabelAI.setVisible(false);
        spellBookPane.updateSpellbookEntries(Model.getUnlockedStatementList());
        selectedPointList = new ArrayList<>();
        selectedPointList.add(new Point(0, 0));
        this.levelEditorModule = new LevelEditorModule();
        this.stage = stage;
        this.startScreen = new StartScreen();
        tutorialGroup = new TutorialGroup();

        startScene = new Scene(startScreen);
        startScreen.setBackground(startBackground);
        GameMap gameMap = (GameMap) Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        cell_size = gameMap.getBoundY() > gameMap.getBoundX() ? GameConstants.MAX_GAMEMAP_SIZE / ((double) gameMap.getBoundY()) : GameConstants.MAX_GAMEMAP_SIZE / ((double) gameMap.getBoundX());
        cell_size = Math.round(cell_size);
        tutorialTextArea.setEditable(false);
        knightsLeftVBox = new VBox();
//        knightsLeftVBox.setStyle("-fx-background-color: lightgrey");
        knightsLeftVBox.setSpacing(cell_size / 4);
        knightsLeftVBox.setMinWidth(cell_size/1.5);
        levelOverviewPane = new LevelOverviewPane(this,false);
        tutorialLevelOverviewPane = new LevelOverviewPane( this,true);
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
        actualMapGPane = new GridPane();
        actualMapGPane.setBorder(new Border(new BorderImage(new Image("file:resources/images/Background_test.png"),new BorderWidths(10),null,new BorderWidths(10),false,BorderRepeat.REPEAT,null)));
        actualMapGPane.setBackground(new Background(new BackgroundImage(new Image("file:resources/images/Background_test.png"),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,BackgroundSize.DEFAULT)));
//        actualMapGPane.setHgap(1);
//        actualMapGPane.setVgap(1);
        rootPane = new StackPane();
        baseContentVBox = new VBox();
//        hBoxRoot.setSpacing(4);
        stage.setScene(startScene);
        vBox = new VBox();
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
        if (f.exists()) speedSlider.getStylesheets().add("file:///" + f.getAbsolutePath().replace("\\", "/"));

        speedSlider.setStyle("-fx-base: rgba(0,0,0,0)");
        speedVBox.setAlignment(Pos.BOTTOM_CENTER);
        speedVBox.getChildren().addAll(speedLbl, speedSlider);

        HBox hBox = new HBox();
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
        if(DEBUG)hBox.getChildren().add(debugBtn );
        hBox.setTranslateY(-SMALL_BUTTON_SIZE/2);
        hBox.setPickOnBounds(false);
        hBox.getChildren().addAll(loadBestCodeBtn, clearCodeBtn);

        leftVBox.getChildren().addAll( aiCodeArea,clearAICodeBtn, errorLabelAI);
        leftVBox.setAlignment(Pos.TOP_CENTER);

        vBox.getChildren().addAll(codeArea, hBox, errorLabel);

        drawMap(gameMap);

        btnExecute.setTooltip(new Tooltip("Will start or pause the game"));
        btnReset.setTooltip(new Tooltip("Will reset the game"));
        backBtn.setTooltip(new Tooltip("Return to Menu"));
        speedSlider.setTooltip(new Tooltip("Control the speed of the game"));
        showSpellBookBtn.setTooltip(new Tooltip("Show/Hide the Spellbook"));
    }


    public void drawMap(GameMap map) {
        actualMapGPane.getChildren().clear();
        Point minBounds = new Point(0, 0);
        Point maxBounds = new Point(map.getBoundX(), map.getBoundY());
        StackPane[][] stackpaneField = getStackPaneFieldFromMap(map,Util.getAllPointsIn(minBounds,maxBounds));
        for(int x = 0; x < map.getBoundX(); x++)
            for(int y = 0; y < map.getBoundY(); y++)
                actualMapGPane.add(stackpaneField[x][y],x,y);
        redrawKnightsLeftVBox();
        changeSupport.firePropertyChange("map", null, null);
    }


    public void drawAllChangedCells() {
        GameMap map = Model.getCurrentMap();
        StackPane[][] stackpaneField = getStackPaneFieldFromMap(map,map.getAndResetChangedPointList());
        //x+1 and y+1 should not be a problem, as the outer rim never changes -> no out of bounds
        for(int x = 0; x < map.getBoundX(); x++)
        for(int y = 0; y < map.getBoundY(); y++){
            if(stackpaneField[x][y]==null)continue;
            actualMapGPane.getChildren().set(y+x*map.getBoundY(),new StackPane());
            actualMapGPane.add(stackpaneField[x][y],x,y);
        }

        //TODO!
//        actualMapGPane.setBackground(brickBackground);
        redrawKnightsLeftVBox();
//        changeSupport.firePropertyChange("map", null, null);
    }


    public void redrawKnightsLeftVBox() {
        knightsLeftVBox.getChildren().clear();
        knightsLeftVBox.setSpacing(cell_size/4);
        knightsLeftVBox.setMinWidth(cell_size/1.5);
        int maxKnights = (int)Model.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS);
        for (int i = 0; i < maxKnights; i++) {
            ImageView tokenIView = new ImageView(new  Image(GameConstants.KNIGHT_TOKEN_PATH));
            int amountOfKnights = Model.getAmountOfKnights();
            if(i+ amountOfKnights == 1)tokenIView.setEffect(GREEN_ADJUST);
            if(i+amountOfKnights == 2)tokenIView.setEffect(VIOLET_ADJUST);
            if(i+amountOfKnights == 3)tokenIView.setEffect(LAST_ADJUST);
            if(i >= maxKnights - amountOfKnights)
                tokenIView.setImage(new  Image(GameConstants.EMPTY_TOKEN_PATH));
            tokenIView.setFitHeight(cell_size/1.5);
            tokenIView.setFitWidth(cell_size/1.5);
            knightsLeftVBox.getChildren().add(tokenIView);
//            System.out.println(Model.getCurrentLevel().getName()+", " +getCurrentSceneState().name()+": " +cell_size);
        }
    }

    private StackPane[][] getStackPaneFieldFromMap(GameMap map, Set<Point> pointSet) {
        //TODO: cell_size = calculateCellSize
        calculateCellSize();
        File folder = new File(Paths.get(GameConstants.ROOT_PATH + "/images").toString());
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for (File file : listOfFiles) {
            String s = file.getName().replaceAll("\\.png", "");
            if(!contentImageMap.containsKey(s))
                contentImageMap.put(s, new Image("file:resources/images/" + s + ".png"));
        }
        StackPane[][] output = new StackPane[map.getBoundX()][map.getBoundY()];
        mapShapes = new Shape[map.getBoundX()][map.getBoundY()];
        for(Point p : pointSet){
            int x = p.getX();
            int y = p.getY();
                final Cell cell = map.getCellAtXYClone(x, y);
                StackPane stackPane = new StackPane();
                ImageView contentImageView = getContentImageView(cell);
                stackPane.getChildren().add(contentImageView  );
                String itemString = cell.getItem() != ItemType.NONE ? cell.getItem().getDisplayName() : "";

                ImageView itemImageView = new ImageView(contentImageMap.get(itemString));
                itemImageView.setFitWidth(cell_size);
                itemImageView.setFitHeight(cell_size);
                if(!itemString.equals(""))stackPane.getChildren().add(itemImageView);
                output[x][y] = stackPane;
        }
        for (Point p : pointSet) {
            int x = p.getX();
            int y = p.getY();
                final Cell cell = map.getCellAtXYClone(x, y);
                if (cell.getEntity() != NO_ENTITY) {
                    int number = 1;
                    if(entityActionList.contains(cell.getEntity())){
                        number = 3;
                    }
                    String entityName = cell.getEntity().getEntityType().getDisplayName();
                    if(cell.getEntity().getItem()!= ItemType.NONE && contentImageMap.containsKey(entityName+"_"+cell.getEntity().getItem().getDisplayName()))entityName+="_"+cell.getEntity().getItem().getDisplayName();
                    if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName+"_Action_"+number))entityName+="_Action_"+number;
                    ImageView entityImageView = getEntityImageView( cell,entityName);
                    //TODO: ImageViews durch Methoden einzeln generieren!

                    ImageView actionImageView = null;
                    if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName))
                    {
                        if(entityActionList.contains(cell.getEntity()))
                            entityActionList.remove(cell.getEntity());
                        else entityActionList.add(cell.getEntity());
                        actionImageView = new ImageView(contentImageMap.get(entityName.replace(""+number, ""+(number+1))));
                        actionImageView.setFitWidth(cell_size);
                        actionImageView.setFitHeight(cell_size);
                    }
                    else
                        entityActionList.remove(cell.getEntity());

                    switch (cell.getEntity().getDirection()) {
                        case NORTH:
                            entityImageView.setRotate(90);
                            if(actionImageView !=null){
                                actionImageView.setRotate(90);

                                actionImageView.setEffect(entityColorMap.get(cell.getEntity().getName()));
                                if(output[x][y-1]==null)output[x][y-1] = new StackPane(actionImageView);
                                else output[x][y-1].getChildren().add(actionImageView);
                            }
                            break;
                        case SOUTH:
                            entityImageView.setRotate(270);
                            if(actionImageView !=null){
                                actionImageView.setRotate(270);
                                actionImageView.setEffect(entityColorMap.get(cell.getEntity().getName()));
                                if(output[x][y+1]==null)output[x][y+1] = new StackPane(actionImageView);
                                else output[x][y+1].getChildren().add(actionImageView);
                            }
                            break;
                        case EAST:
                            entityImageView.setRotate(180);
                            if(actionImageView !=null){
                                actionImageView.setRotate(180);
                                actionImageView.setEffect(entityColorMap.get(cell.getEntity().getName()));
                                if(output[x+1][y]==null)output[x+1][y] = new StackPane(actionImageView);
                                else output[x+1][y].getChildren().add(actionImageView);
                            }
                            break;
                        case WEST:
                            if(actionImageView !=null){
                                actionImageView.setEffect(entityColorMap.get(cell.getEntity().getName()));
                                if(output[x-1][y]==null)output[x-1][y] = new StackPane(actionImageView);
                                else output[x-1][y].getChildren().add(actionImageView);
                            }
                            break;
                    }

                    output[x][y].getChildren().add(entityImageView);
                }
            ImageView destructionIView = new ImageView();
            destructionIView.setFitWidth(cell_size);
            destructionIView.setFitHeight(cell_size);
            if(cell.hasFlag(CFlag.KNIGHT_DEATH)) destructionIView.setImage(contentImageMap.get(CFlag.KNIGHT_DEATH.getDisplayName()));
            else if (cell.hasFlag(CFlag.SKELETON_DEATH))destructionIView.setImage(contentImageMap.get(CFlag.SKELETON_DEATH.getDisplayName()));
            else if (cell.hasFlag(CFlag.DIRT_REMOVED))destructionIView.setImage(contentImageMap.get(CFlag.DIRT_REMOVED.getDisplayName()));
            else if (cell.hasFlag(CFlag.KEY_DESTROYED))destructionIView.setImage(contentImageMap.get(CFlag.KEY_DESTROYED.getDisplayName()));
            else if (cell.hasFlag(CFlag.ITEM_DESTROYED))
                destructionIView.setImage(contentImageMap.get(CFlag.ITEM_DESTROYED.getDisplayName()));

            if(destructionIView.getImage()!=null)
                output[x][y].getChildren().add(destructionIView);
//                actualMapGPane.getChildren().add(mapShapes[row][column]);
        }
        return output;
    }

    private ImageView getEntityImageView(Cell cell, String entityName) {
        ImageView imageView = new ImageView(contentImageMap.get(entityName));
        imageView.setFitWidth(cell_size);
        imageView.setFitHeight(cell_size);
        //COLOR EXPERIMENT:
        int knightCount = 0;
        int skeletonCount = 0;
        for(String s : entityColorMap.keySet()){
            if(Model.getCurrentMap().getEntity(s).getEntityType()== EntityType.KNIGHT)knightCount++;
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

        return imageView;
    }

    private ImageView getContentImageView(Cell cell){
        StringBuilder contentString = new StringBuilder(cell.getContent().getDisplayName());

        boolean isTurned = false;
        boolean isInverted = false;
        boolean isOpen = false;
        for (CFlag flag : CFlag.values()) {
            if (cell.hasFlag(flag)) {
                if(flag.isTemporary())
                    continue;
                if(flag == CFlag.TURNED && (isTurned = true))continue;
                if(flag == CFlag.INVERTED && cell.getContent()== CellContent.GATE){
                    isInverted = true;
                    if(!isOpen) contentString.append("_").append(CFlag.OPEN.getDisplayName());
                    else contentString = new StringBuilder(contentString.toString().replace("_" + CFlag.OPEN.getDisplayName(), ""));
                    continue;
                }
                if(flag == CFlag.OPEN ){
                    isOpen = true;
                    if(!isInverted) contentString.append("_").append(CFlag.OPEN.getDisplayName());
                    else contentString = new StringBuilder(contentString.toString().replace("_" + CFlag.OPEN.getDisplayName(), ""));
                    continue;
                }
                if(flag == CFlag.INVERTED && cell.getContent()== CellContent.PRESSURE_PLATE && Model.getTurnsTaken()==0){
                    isInverted = true;
                    contentString.append("_").append(CFlag.INVERTED.getDisplayName()).append("_").append(CFlag.TRIGGERED.getDisplayName());
                    continue;
                }
                contentString.append("_").append(flag.getDisplayName());
            }
        }
        ImageView imageView = new ImageView(contentImageMap.get(contentString.toString()));
        imageView.setFitWidth(cell_size);
        imageView.setFitHeight(cell_size);
        if(isTurned)imageView.setRotate(270);
        int amountOfKnights = Model.getAmountOfKnights();
        int maxKnights = (int)Model.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS);
        if((amountOfKnights< maxKnights &&cell.getContent()== CellContent.SPAWN))
            switch (amountOfKnights){
                case 1: imageView.setEffect(GameConstants.GREEN_ADJUST);
                    break;
                case 2: imageView.setEffect(GameConstants.VIOLET_ADJUST);
                    break;
                case 3: imageView.setEffect(GameConstants.LAST_ADJUST);
                    break;
            }
        if(cell.getContent()== CellContent.ENEMY_SPAWN){
            //switch (entityColorMap.size() -Model.getCurrentLevel().getUsedKnights()){
            int skelCount = Model.getAmountOfSkeletons();
            switch (skelCount){
                case 1: imageView.setEffect(GameConstants.GREEN_ADJUST);
                    break;
                case 2: imageView.setEffect(GameConstants.VIOLET_ADJUST);
                    break;
                case 3: imageView.setEffect(GameConstants.LAST_ADJUST);
                    break;
            }}

        return imageView;
    }

    private void calculateCellSize() {
        GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        cell_size = gameMap.getBoundY() > gameMap.getBoundX() ?
                GameConstants.MAX_GAMEMAP_SIZE / ((double) gameMap.getBoundY()) : GameConstants.MAX_GAMEMAP_SIZE / ((double) gameMap.getBoundX());

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
        CellContent content = cell.getContent();
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

    public CodeArea getAICodeArea() {
        return aiCodeArea;
    }

    public GridPane getActualMapGPane() {
        return actualMapGPane;
    }

    public void highlightInMap(List<Point> points) {
        GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        this.selectedPointList = new ArrayList<>(points);
        for (Polyline highlight: highlights) {
            rootPane.getChildren().remove(highlight);
        }
        highlights = new ArrayList<>();
        Polyline highlight;
        List<Line> edgeList = new ArrayList<>();
        for(int x = 0; x < gameMap.getBoundX(); x++)
        for(int y = 0; y < gameMap.getBoundY(); y++){
            if (!points.contains(new Point(x, y))) continue;
//            System.out.println("X: " +x + ", Y:" +y);
            double dx = x * cell_size;
            double dy = y * cell_size;
            if (y == 0 || !points.contains(new Point(x, y - 1))) edgeList.add(new Line(dx, dy, dx + cell_size, dy));
            if (x == 0 || !points.contains(new Point(x - 1, y))) edgeList.add(new Line(dx, dy + cell_size, dx, dy));
            if (x == gameMap.getBoundX() - 1 || !points.contains(new Point(x + 1, y)))
                edgeList.add(new Line(dx + cell_size, dy, dx + cell_size, dy + cell_size));
            if (y == gameMap.getBoundY() - 1 || !points.contains(new Point(x, y + 1)))
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

    public void setCContentButtonDisabled(CellContent content, boolean b) {
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
//            if (!btn.getText().equals(CellContent.EMPTY.getDisplayName()) && !btn.getText().equals(CellContent.WALL.getDisplayName()))
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
        GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        levelEditorModule.getWidthValueLbl().setText("" + gameMap.getBoundX());
        levelEditorModule.getLevelNameValueLbl().setText("" + Model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME));
        Integer[] locToStars = (Integer[]) Model.getDataFromCurrentLevel(LevelDataType.LOC_TO_STARS);
        Integer[] turnsToStars = (Integer[]) Model.getDataFromCurrentLevel(LevelDataType.TURNS_TO_STARS);
        levelEditorModule.getMaxLoc2StarsVLbl().setText(locToStars[0]+"");
        levelEditorModule.getMaxLoc3StarsVLbl().setText(locToStars[1]+"");
        levelEditorModule.getMaxTurns2StarsVLbl().setText(turnsToStars[0]+"");
        levelEditorModule.getMaxTurns3StarsVLbl().setText(turnsToStars[1]+"");
        levelEditorModule.getLevelNameValueLbl().setText("" + Model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME));
        levelEditorModule.getIndexValueLbl().setText("" + Model.getCurrentIndex());
        levelEditorModule.getMaxKnightsValueLbl().setText("" + Model.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS));
        levelEditorModule.getIsTutorialValueLbl().setText("" + Model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL));
        levelEditorModule.getHeightValueLbl().setText("" + gameMap.getBoundY());
        levelEditorModule.setRequiredLevels((List<String>)Model.getDataFromCurrentLevel(LevelDataType.REQUIRED_LEVELS));
        boolean isTutorial = (boolean)Model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL);
        levelEditorModule.getTutorialVBox().setVisible(isTutorial);
        List<String> tutorialLines = (List<String>)Model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
        if(isTutorial)levelEditorModule.getTutorialTextArea().setText(tutorialLines.get(Model.getCurrentTutorialIndex()));
        levelEditorModule.getPrevTutorialTextBtn().setDisable(true);
        if(Model.getCurrentTutorialSize()== 0)levelEditorModule.getNextTutorialTextBtn().setDisable(true);
//        levelEditorModule.updateTutorialSection(Model.getCurrentLevel());
        levelEditorModule.getHasAiValueLbl().setText(Model.getDataFromCurrentLevel(LevelDataType.HAS_AI)+"");
        updateTutorialMessage();
    }

    @Override
    public void updateAll() {
        entityColorMap = new HashMap<>();
        selectedPointList = new ArrayList<>();
        selectedPointList.add(new Point(0, 0));
        boolean hasAi =(boolean) Model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
        ComplexStatement aiBehaviour = (ComplexStatement)Model.getDataFromCurrentLevel(LevelDataType.AI_CODE);
        if (hasAi) {
            aiCodeArea.setVisible(true);
            if(sceneState == SceneState.LEVEL_EDITOR)clearAICodeBtn.setVisible(true);
            aiCodeArea.updateCodeFields(aiBehaviour);
            aiCodeArea.scroll(0);
        }
        else {
            aiCodeArea.setVisible(false);
            clearAICodeBtn.setVisible(false);
        }
        codeArea.scroll(0);
        if (sceneState == SceneState.LEVEL_EDITOR) {
            updateLevelEditorModule();
        }
        spellBookPane.updateSpellbookEntries(Model.getUnlockedStatementList());
        drawMap(Model.getCurrentMap());
        highlightInMap(selectedPointList);
    }

    @Override
    public void updateAccordingToChanges(LevelChange levelChange) {

        switch (levelChange.getLevelDataType()){
            case MAP_DATA:
                GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                drawMap(gameMap);
                if(sceneState == SceneState.LEVEL_EDITOR)
                    Platform.runLater(()->highlightInMap(selectedPointList));
                selectedPointList = pointListOutOfBounds();
                Platform.runLater(()->highlightInMap(selectedPointList));
            case LOC_TO_STARS:
            case TURNS_TO_STARS:
            case REQUIRED_LEVELS:
            case IS_TUTORIAL:
            case TUTORIAL_LINES:
            case LEVEL_NAME:
            case HAS_AI:
            case LEVEL_INDEX:
            case MAX_KNIGHTS:
                levelEditorModule.update(levelChange);
                if(levelChange.getLevelDataType().equals(LevelDataType.MAX_KNIGHTS)){
                    redrawKnightsLeftVBox();
                }
                break;
            case AI_CODE:
                ComplexStatement aiBehaviour = (ComplexStatement)Model.getDataFromCurrentLevel(LevelDataType.AI_CODE);
                boolean hasAI = (boolean)Model.getDataFromCurrentLevel(LevelDataType.HAS_AI);
                levelEditorModule.getHasAiValueLbl().setText("" + hasAI);
                if (hasAI && aiBehaviour.getStatementListSize() == 0) {
                    aiCodeArea.updateCodeFields(aiBehaviour);
                    //THIS IS THE ONLY WORKING SOLUTION I FOUND FOR FINDING OUT POSITIONS OF NODES IN SCENE!!
                    double d = getActualMapGPane().localToScene(getActualMapGPane().getBoundsInLocal()).getMinX();
                    Platform.runLater(() -> {
                        for(Polyline p: highlights){
                            p.setTranslateX(p.getTranslateX()-d+getActualMapGPane().localToScene(getActualMapGPane().getBoundsInLocal()).getMinX());
                        }});
                    aiCodeArea.setVisible(true);
                    clearAICodeBtn.setVisible(true);
                } else if (!hasAI) {
                    //THIS IS THE ONLY WORKING SOLUTION I FOUND FOR FINDING OUT POSITIONS OF NODES IN SCENE!!
                    double d = getActualMapGPane().localToScene(getActualMapGPane().getBoundsInLocal()).getMinX();
                    aiCodeArea.updateCodeFields(new ComplexStatement());
                    Platform.runLater(() -> {
                        for(Polyline p: highlights){
                            p.setTranslateX(p.getTranslateX()-d+getActualMapGPane().localToScene(getActualMapGPane().getBoundsInLocal()).getMinX());
                        }});
                    aiCodeArea.setVisible(false);
                    clearAICodeBtn.setVisible(false);
                }
                break;
        }
        levelEditorModule.toggleLevelIsSaved(!Model.currentLevelHasChanged());
        updateLevelEditorModule();
    }

    @Override
    public void changesUndone() {
        //TODO:
        updateAll(); //????
        levelEditorModule.toggleLevelIsSaved(true);
    }

    private List<Point> pointListOutOfBounds() {
        List<Point> output = new ArrayList<>();
        for(Point p : selectedPointList){
            GameMap gameMap = (GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
            if(p.getX() >= gameMap.getBoundX()||p.getY() >= gameMap.getBoundY())continue;
            output.add(p);
        }
        return output;
    }

    public StartScreen getStartScreen() {
        return startScreen;
    }


    public void setSceneState(SceneState sceneState) {
        Model.resetTutorialIndex();
        this.sceneState = sceneState;
        switch (sceneState) {
            case START_SCREEN:
                stage.getScene().setRoot(startScreen);
                break;
            case LEVEL_EDITOR:
                prepareRootPane();
                drawMap((GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA));
                aiCodeArea.setEditable(true);
//                aiCodeArea.deselectAll();
//                codeArea.deselectAll();
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
                drawMap((GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA));
                prepareRootPane();
                aiCodeArea.deselectAll();
                codeArea.deselectAll();
                Platform.runLater(()->codeArea.select(0, Selection.END));
                stage.getScene().setRoot(rootPane);
                break;
            case TUTORIAL:
                drawMap((GameMap)Model.getDataFromCurrentLevel(LevelDataType.MAP_DATA));
                prepareRootPane();
                aiCodeArea.deselectAll();
                codeArea.deselectAll();
                Platform.runLater(()->codeArea.select(0, Selection.END));
//                levelOverviewPane.updateUnlockedLevels(Model, this);

                stage.getScene().setRoot(rootPane);
                break;

        }
    }

    private void prepareRootPane() {
        rootPane = new StackPane();
        rootPane.setPrefSize(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        levelNameLabel.setText((String)Model.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME));
        levelNameLabel.setFont(GameConstants.BIGGEST_FONT);
        levelNameLabel.setStyle("-fx-background-color: lightgray");
        HBox contentHBox = new HBox();
        rightVBox = new VBox();
        centerVBox = new VBox();
        rightVBox.setAlignment(Pos.TOP_RIGHT);
        knightsLeftVBox.setAlignment(Pos.TOP_RIGHT);
        //TODO
        if(sceneState != SceneState.LEVEL_EDITOR)
            clearAICodeBtn.setVisible(false);
        if ((boolean)Model.getDataFromCurrentLevel(LevelDataType.HAS_AI)){
            aiCodeArea.updateCodeFields((ComplexStatement)Model.getDataFromCurrentLevel(LevelDataType.AI_CODE));
            if(sceneState == SceneState.LEVEL_EDITOR)clearAICodeBtn.setVisible(true);
        }
        else {
            aiCodeArea.setVisible(false);
            clearAICodeBtn.setVisible(false);
        }
        List<String> storedCode;
        try {
            storedCode = JSONParser.getStoredCode();
            if(storedCode.size()>0)
                codeArea.updateCodeFields(CodeParser.parseProgramCode(storedCode));
        } catch (Exception e) {
            e.printStackTrace();
        }
        codeArea.select(0, Selection.START);

        aiCodeArea.draw();
        codeArea.draw();

        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.getChildren().addAll(vBox);
        baseContentVBox.getChildren().clear();


        HBox bottomHBox = new HBox(backBtn, btnExecute, speedVBox, btnReset, showSpellBookBtn);
        bottomHBox.setAlignment(Pos.BOTTOM_CENTER);
        switch (sceneState) {
            case LEVEL_EDITOR:
                HBox editorCenterHBox = new HBox(knightsLeftVBox, new VBox(actualMapGPane), new VBox(levelEditorModule.getRightVBox()));
                editorCenterHBox.autosize();
                editorCenterHBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/1.5);
                editorCenterHBox.setAlignment(Pos.TOP_CENTER);
                centerVBox.getChildren().addAll(levelEditorModule.getBottomHBox(), editorCenterHBox);
                centerVBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/2);
//                editorScene = new Scene(rootPane);
                baseContentVBox.getChildren().add(levelEditorModule.getTopHBox());
                levelEditorModule.getTutorialVBox().setVisible((boolean)Model.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL));
                updateAll();
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

                //TODO: move to controller?
                if(isIntroduction){
                    tutorialGroup.setEntries(Util.StringListFromArray(TUTORIAL_LINES));
                }
                else tutorialGroup.setEntries((List<String>)Model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES));
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

    public Image getImageFromMap(GameMap originalMap) {
        GridPane gridPane = new GridPane();

        StackPane[][] stackpaneField = getStackPaneFieldFromMap(originalMap,Util.getAllPointsIn(new Point(0,0),new Point(originalMap.getBoundX(),originalMap.getBoundY())));
        for(int y = 0; y < originalMap.getBoundY(); y++)
            for(int x = 0; x < originalMap.getBoundX(); x++)
                gridPane.add(stackpaneField[x][y],x,y);
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

    public Label getErrorLabel() {
        return errorLabel;
    }
    public Label getErrorLabelAI() {
        return errorLabelAI;
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
    public Button getClearAICodeBtn() {
        return clearAICodeBtn;
    }
    public Button getStoreCodeBtn() {
        return storeCodeBtn;
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

    public void leaveIntroductions() {
        tutorialGroup.leaveIntroduction();
        tutorialGroup.setEntries((List<String>)Model.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES));
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

    public void highlightButtons() {
        Effect dropShadow = new DropShadow(GameConstants.BIGGEST_FONT_SIZE, Color.YELLOW);
        final int n = 2;
        switch (getTutorialGroup().getCurrentIndex()){
            case n:
                removeEffectsOfControlElements();
                actualMapGPane.setEffect(dropShadow);
                break;
            case n+1:
                removeEffectsOfControlElements();
                knightsLeftVBox.setEffect(dropShadow);
                break;
            case n+2:
                removeEffectsOfControlElements();
                break;
            case n+3:
                removeEffectsOfControlElements();
                getBackBtn().setEffect(dropShadow);
                break;
            case n+4:
                removeEffectsOfControlElements();
                getBtnExecute().setEffect(dropShadow);
                break;
            case n+5:
                removeEffectsOfControlElements();
                getSpeedSlider().setEffect(dropShadow);
                break;
            case n+6:
                removeEffectsOfControlElements();
                getBtnReset().setEffect(dropShadow);
                break;
            case n+7:
                removeEffectsOfControlElements();
                getShowSpellBookBtn().setEffect(dropShadow);
                break;

            case n+8:
                removeEffectsOfControlElements();
                codeArea.setEffect(dropShadow);
                break;
            default: break;
        }
    }

    private void removeEffectsOfControlElements() {
        getBackBtn().setEffect(null);
        actualMapGPane.setEffect(null);
        knightsLeftVBox.setEffect(null);
        getBtnExecute().setEffect(null);
        getSpeedSlider().setEffect(null);
        getBtnReset().setEffect(null);
        getShowSpellBookBtn().setEffect(null);
        codeArea.setEffect(null);
    }

    public void updateTutorialMessage() {
        if(getCurrentSceneState()==SceneState.LEVEL_EDITOR){
            levelEditorModule.getTutorialTextArea().setText(Model.getCurrentTutorialMessage());
            levelEditorModule.getTutorialNumberValueLbl().setText(Model.getCurrentTutorialIndex()+"");
            if(Model.getCurrentTutorialSize()>Model.getCurrentTutorialIndex()+1)levelEditorModule.getNextTutorialTextBtn().setDisable(false);
        }else {
            tutorialGroup.getCurrentTutorialMessage().setText(Model.getCurrentTutorialMessage());
            if(Model.getCurrentTutorialSize()>Model.getCurrentTutorialIndex()+1)tutorialGroup.getNextBtn().setDisable(false);
        }
    }
}
//KEYTHIEF: "Knight knight = new Knight(EAST);","int turns = 0;","while(true) {","if ((!knight.targetIsDanger()) && knight.canMove()) {","knight.move();","}","else if (knight.canMove() || knight.targetCellIs(GATE)) {","knight.wait();","}","else if (knight.targetContains(SKELETON) || knight.targetCellIs(EXIT)) {","knight.useItem();","}","else if (knight.targetsItem(SWORD)) {","knight.collect();","knight.turn(LEFT);","knight.move();","knight.move();","knight.turn(RIGHT);","}","else if (knight.targetContains(KEY)) {","knight.collect();","knight.turn(AROUND);","}","else if (turns < 3) {","turns = turns + 2;","knight.turn(LEFT);","}","else {","knight.turn(RIGHT);","turns = turns - 1;","}","}"
//COLLECTANDDROP: "Knight knight = new Knight(NORTH);","knight.move();","knight.turn(RIGHT);","knight.move();","knight.collect();","knight.turn(AROUND);","knight.move();","knight.turn(RIGHT);","knight.collect();","knight.turn(AROUND);","knight.dropItem();","knight.turn(AROUND);","knight.collect();","knight.move();","knight.move();","knight.dropItem();","knight.turn(AROUND);","knight.move();","knight.move();","knight.collect();","knight.turn(AROUND);","knight.move();","knight.move();","knight.turn(AROUND);","knight.dropItem();","knight.turn(AROUND);","knight.collect();","knight.move();","knight.turn(RIGHT);","knight.move();","knight.useItem();"