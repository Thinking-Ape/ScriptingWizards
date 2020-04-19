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
import javafx.stage.Stage;
import main.model.*;
import main.model.gamemap.Cell;
import main.model.gamemap.Entity;
import main.model.gamemap.GameMap;
import main.model.gamemap.enums.CellContent;
import main.model.gamemap.enums.CellFlag;
import main.model.gamemap.enums.EntityType;
import main.model.gamemap.enums.ItemType;
import main.model.statement.ComplexStatement;
import main.parser.JSONParser;
import main.parser.CodeParser;
import main.utility.*;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static main.model.GameConstants.*;
import static main.model.GameConstants.TUTORIAL_LINES;

public class View implements LevelChangeListener {

    private final BackgroundImage backgroundImage = new BackgroundImage(new Image( GameConstants.BG_LIGHT_TILE_PATH ), BackgroundRepeat.REPEAT,null,BackgroundPosition.CENTER,BackgroundSize.DEFAULT );
    private LevelOverviewPane tutorialLevelOverviewPane;
    private Background brickBackground = new Background(backgroundImage);
    private Stage stage;
    private SimpleEventSender eventSender;
    private static double cell_size;
    private StartScreen startScreen;
    private Button btnExecute;
    private Button btnReset;
    private CodeArea codeArea = CodeArea.getInstance(CodeAreaType.PLAYER);
    private CodeArea aiCodeArea = CodeArea.getInstance(CodeAreaType.AI);
    private VBox vBox;
    private Slider speedSlider;
    private Label errorLabel = new Label();
    private Label errorLabelAI = new Label();
    private GridPane mapGPane;
    private StackPane levelPane;
    private VBox baseContentVBox;
    private List<Polyline> highlights = new ArrayList<>();
    private LevelEditorModule levelEditorModule;
    private List<Point> selectedPointList;
    private static Map<String, Image> contentImageMap = new HashMap<>();

    private LevelOverviewPane levelOverviewPane;

    private Button backBtn = new Button();
    private static SceneState sceneState = SceneState.START_SCREEN;
    private VBox knightsLeftVBox;
    private SpellBookPane spellBookPane = new SpellBookPane();
    private Label levelNameLabel = new Label();
    private Button showSpellBookBtn = new Button();

    private VBox leftVBox = new VBox();
    private VBox speedVBox;
    private TutorialGroup tutorialGroup;

    private Button loadBestCodeBtn = new Button("Load Best Code");
    private Button clearCodeBtn = new Button("Clear Code");
    private Button clearAICodeBtn = new Button("Clear Code");
    private Button storeCodeBtn = new Button("Store Code");


    private static List<Entity> entityActionList = new ArrayList<>();
    private static Map<String, Effect> entityColorMap = new HashMap<>();
    private boolean isIntroduction;

    private static View instance = null;

    public static View getInstance(Stage stage){
        if(instance == null)instance = new View(stage);
        return instance;
    }

    private View( Stage stage) {
        levelNameLabel.setFont(GameConstants.BIGGEST_FONT);
        levelNameLabel.setStyle("-fx-background-color: lightgray");
        errorLabel.setStyle("-fx-text-fill: red;-fx-background-color: white");
        errorLabel.setFont(GameConstants.BIG_FONT);
        errorLabel.setMaxWidth(GameConstants.TEXTFIELD_WIDTH);
        errorLabel.setVisible(false);
        errorLabelAI.setStyle("-fx-text-fill: red;-fx-background-color: white");
        errorLabelAI.setFont(GameConstants.BIG_FONT);
        errorLabelAI.setMaxWidth(GameConstants.TEXTFIELD_WIDTH);
        errorLabelAI.setVisible(false);

        selectedPointList = new ArrayList<>();
        selectedPointList.add(new Point(0, 0));
        this.levelEditorModule = new LevelEditorModule();
        this.stage = stage;
        this.startScreen = new StartScreen();
        tutorialGroup = new TutorialGroup();

        //TODO: why only one scene? Did the others not work?
        Scene startScene = new Scene(startScreen);

        Background startBackground = new Background(new BackgroundImage(new Image("file:resources/images/Project_Background.png", SCREEN_WIDTH, SCREEN_HEIGHT, true, true), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT));
        startScreen.setBackground(startBackground);
        GameMap gameMap = (GameMap) ModelInformer.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        cell_size = gameMap.getBoundY() > gameMap.getBoundX() ? GameConstants.MAX_GAMEMAP_SIZE / ((double) gameMap.getBoundY()) : GameConstants.MAX_GAMEMAP_SIZE / ((double) gameMap.getBoundX());
        cell_size = Math.round(cell_size);
        TextArea tutorialTextArea = new TextArea();
        tutorialTextArea.setEditable(false);
        knightsLeftVBox = new VBox();

        knightsLeftVBox.setSpacing(cell_size / 4);
        knightsLeftVBox.setMinWidth(cell_size/1.5);
        levelOverviewPane = new LevelOverviewPane(false);
        tutorialLevelOverviewPane = new LevelOverviewPane( true);

        stage.setWidth(GameConstants.SCREEN_WIDTH);
        stage.setHeight(GameConstants.SCREEN_HEIGHT);
        stage.setMaximized(true);
        stage.setResizable(false);
        if (GameConstants.IS_FULLSCREEN) {
            stage.setFullScreen(true);
        }
        stage.setFullScreenExitHint("");
        mapGPane = new GridPane();
        mapGPane.setBorder(new Border(new BorderImage(new Image(GameConstants.BG_DARK_TILE_PATH),new BorderWidths(10),null,new BorderWidths(10),false,BorderRepeat.REPEAT,null)));
        mapGPane.setBackground(new Background(new BackgroundImage(new Image(GameConstants.BG_DARK_TILE_PATH),BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT,BackgroundPosition.DEFAULT,BackgroundSize.DEFAULT)));

        levelPane = new StackPane();
        baseContentVBox = new VBox();
        stage.setScene(startScene);

        vBox = new VBox();
        btnExecute = new Button();

        ImageView backBtnIV = new ImageView(GameConstants.BACK_BTN_IMAGE_PATH);
        backBtnIV.setFitHeight(backBtnIV.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        backBtnIV.setFitWidth(backBtnIV.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        ImageView executeBtnIV = new ImageView(GameConstants.EXECUTE_BTN_IMAGE_PATH);
        executeBtnIV.setFitHeight(executeBtnIV.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        executeBtnIV.setFitWidth(executeBtnIV.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        ImageView resetBtnIV = new ImageView(GameConstants.RESET_BTN_IMAGE_PATH);
        resetBtnIV.setFitHeight(resetBtnIV.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        resetBtnIV.setFitWidth(resetBtnIV.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);

        ImageView spellBtnIV = new ImageView(GameConstants.SHOW_SPELLS_BTN_IMAGE_PATH);
        spellBtnIV.setFitHeight(spellBtnIV.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        spellBtnIV.setFitWidth(spellBtnIV.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);

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
        File f = new File(Path.of(GameConstants.ROOT_PATH+"/slider_style.css").toUri());
        if (f.exists()) speedSlider.getStylesheets().add( f.toURI().toString().replace("\\", "/"));

        speedSlider.setStyle("-fx-base: rgba(0,0,0,0)");
        speedVBox.setAlignment(Pos.BOTTOM_CENTER);
        speedVBox.getChildren().addAll(speedLbl, speedSlider);

        HBox hBox = new HBox();
        Button debugBtn = new Button("Clipboard");
        Button debugBtn2 = new Button("Enter Code");
        debugBtn2.setOnAction(evt -> {
            Optional<String> res = new TextInputDialog().showAndWait();
            if(res.isPresent()){
                try{
                    List<String> codeLines = new ArrayList<>();
                    String[] codeArray = res.get().split("\",\"");
                    for(String s : codeArray)codeLines.add(s.replaceAll("\"",""));
                    ComplexStatement c = CodeParser.parseProgramCode(codeLines);
                    codeArea.updateCodeFields(c);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        debugBtn.setOnAction(event -> {
            String debug = "";
            for (String s : codeArea.getAllCode()) {
                debug += "\"" + s + "\",";
            }
            debug = debug.substring(0, debug.length() - 1);
            StringSelection selection = new StringSelection(debug);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        });
        if(DEBUG)hBox.getChildren().addAll(debugBtn,debugBtn2 );
        hBox.setTranslateY(-SMALL_BUTTON_SIZE/2);
        hBox.setPickOnBounds(false);
        hBox.getChildren().addAll(loadBestCodeBtn, clearCodeBtn);

        leftVBox.getChildren().addAll( aiCodeArea,clearAICodeBtn, errorLabelAI);
        leftVBox.setAlignment(Pos.TOP_CENTER);

        vBox.getChildren().addAll(codeArea, hBox, errorLabel);

        btnExecute.setTooltip(new Tooltip("Will start or pause the game"));
        btnReset.setTooltip(new Tooltip("Will reset the game"));
        backBtn.setTooltip(new Tooltip("Return to Menu"));
        speedSlider.setTooltip(new Tooltip("Control the speed of the game"));
        showSpellBookBtn.setTooltip(new Tooltip("Show/Hide the Spellbook"));

        List<String> storedCode;
        try {
            storedCode = JSONParser.getStoredCode();
            if(storedCode.size()>0)
                codeArea.updateCodeFields(CodeParser.parseProgramCode(storedCode));
            else codeArea.updateCodeFields(new ComplexStatement());
        } catch (Exception e) {
            e.printStackTrace();
            codeArea.updateCodeFields(new ComplexStatement());
        }
    }


    public void drawMap(GameMap map) {
        entityColorMap = new HashMap<>();
        mapGPane.getChildren().clear();
        Point minBounds = new Point(0, 0);
        Point maxBounds = new Point(map.getBoundX(), map.getBoundY());
        StackPane[][] stackpaneField = getStackPaneFieldFromMap(map,Util.getAllPointsIn(minBounds,maxBounds));
        for(int x = 0; x < map.getBoundX(); x++)
            for(int y = 0; y < map.getBoundY(); y++)
                mapGPane.add(stackpaneField[x][y],x,y);
        redrawKnightsLeftVBox();
        if(eventSender != null)
            eventSender.notifyListeners(null);
    }


    public void drawAllChangedCells() {
        GameMap map = ModelInformer.getCurrentMapCopy();
        StackPane[][] stackpaneField = getStackPaneFieldFromMap(map,map.getAndResetChangedPointList());
        //x+1 and y+1 should not be a problem, as the outer rim never changes -> no out of bounds
        for(int x = 0; x < map.getBoundX(); x++)
        for(int y = 0; y < map.getBoundY(); y++){
            if(stackpaneField[x][y]==null)continue;
            mapGPane.getChildren().set(y+x*map.getBoundY(),new StackPane());
            mapGPane.add(stackpaneField[x][y],x,y);
        }

        redrawKnightsLeftVBox();
    }


    private void redrawKnightsLeftVBox() {
        knightsLeftVBox.getChildren().clear();
        knightsLeftVBox.setSpacing(cell_size/4);
        knightsLeftVBox.setMinWidth(cell_size/1.5);
        int maxKnights = (int)ModelInformer.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS);
        for (int i = 0; i < maxKnights; i++) {
            ImageView tokenIView = new ImageView(new  Image(GameConstants.KNIGHT_TOKEN_PATH));
            int amountOfKnights = ModelInformer.getAmountOfKnightsSpawned();
            if(i < maxKnights - amountOfKnights)
            tokenIView.setEffect(Util.getEffect(i+amountOfKnights,true));
            else
                tokenIView.setImage(new  Image(GameConstants.EMPTY_TOKEN_PATH));
            tokenIView.setFitHeight(cell_size/1.5);
            tokenIView.setFitWidth(cell_size/1.5);
            knightsLeftVBox.getChildren().add(tokenIView);
        }
    }

    private static StackPane[][] getStackPaneFieldFromMap(GameMap map, Set<Point> pointSet) {
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
                    if(cell.hasFlag(CellFlag.ACTION) && contentImageMap.containsKey(entityName+"_Action_"+number))entityName+="_Action_"+number;
                    ImageView entityImageView = getEntityImageView( cell,entityName);
                    //TODO: ImageViews durch Methoden einzeln generieren!

                    ImageView actionImageView = null;
                    if(cell.hasFlag(CellFlag.ACTION) && contentImageMap.containsKey(entityName))
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
            if(cell.hasFlag(CellFlag.KNIGHT_DEATH)) destructionIView.setImage(contentImageMap.get(CellFlag.KNIGHT_DEATH.getDisplayName()));
            else if (cell.hasFlag(CellFlag.SKELETON_DEATH))destructionIView.setImage(contentImageMap.get(CellFlag.SKELETON_DEATH.getDisplayName()));
            else if (cell.hasFlag(CellFlag.DIRT_REMOVED))destructionIView.setImage(contentImageMap.get(CellFlag.DIRT_REMOVED.getDisplayName()));
            else if (cell.hasFlag(CellFlag.KEY_DESTROYED))destructionIView.setImage(contentImageMap.get(CellFlag.KEY_DESTROYED.getDisplayName()));
            else if (cell.hasFlag(CellFlag.ITEM_DESTROYED))
                destructionIView.setImage(contentImageMap.get(CellFlag.ITEM_DESTROYED.getDisplayName()));

            if(destructionIView.getImage()!=null)
                output[x][y].getChildren().add(destructionIView);
            if(ModelInformer.getCurrentRound()>1 && destructionIView.getImage() != null)destructionIView.setEffect(new ColorAdjust(0,0,0,-0.5));
        }
        return output;
    }

    private static ImageView getEntityImageView(Cell cell, String entityName) {
        ImageView imageView = new ImageView(contentImageMap.get(entityName));
        imageView.setFitWidth(cell_size);
        imageView.setFitHeight(cell_size);
        //COLOR EXPERIMENT:
        int knightCount = ModelInformer.getAmountOfKnightsSpawned();
        if(cell.getEntity().getEntityType() == EntityType.KNIGHT){
            entityColorMap.putIfAbsent(cell.getEntity().getName(), Util.getEffect(knightCount-1,true));
        }
        int skeletonCount = ModelInformer.getAmountOfSkeletonsSpawned();
        if(cell.getEntity().getEntityType() == EntityType.SKELETON){
            entityColorMap.putIfAbsent(cell.getEntity().getName(), Util.getEffect(skeletonCount-1,true));
        }
        imageView.setEffect(entityColorMap.get(cell.getEntity().getName()));

        //END OF COLOR EXPERIMENT

        return imageView;
    }

    private static ImageView getContentImageView(Cell cell){
        StringBuilder contentString = new StringBuilder(cell.getContent().getDisplayName());

        boolean isTurned = false;
        boolean isInverted = false;
        boolean isOpen = false;
        for (CellFlag flag : CellFlag.values()) {
            if (cell.hasFlag(flag)) {
                if(flag.isTemporary())
                    continue;
                if(flag == CellFlag.TURNED && (isTurned = true))continue;
                if(flag == CellFlag.INVERTED && cell.getContent()== CellContent.GATE){
                    isInverted = true;
                    if(!isOpen) contentString.append("_").append(CellFlag.OPEN.getDisplayName());
                    else contentString = new StringBuilder(contentString.toString().replace("_" + CellFlag.OPEN.getDisplayName(), ""));
                    continue;
                }
                if(flag == CellFlag.OPEN ){
                    isOpen = true;
                    if(!isInverted) contentString.append("_").append(CellFlag.OPEN.getDisplayName());
                    else contentString = new StringBuilder(contentString.toString().replace("_" + CellFlag.OPEN.getDisplayName(), ""));
                    continue;
                }
                if(flag == CellFlag.INVERTED && cell.getContent()== CellContent.PRESSURE_PLATE && ModelInformer.getTurnsTaken()==0){
                    isInverted = true;
                    contentString.append("_").append(CellFlag.INVERTED.getDisplayName()).append("_").append(CellFlag.TRIGGERED.getDisplayName());
                    continue;
                }
                contentString.append("_").append(flag.getDisplayName());
            }
        }
        ImageView imageView = new ImageView(contentImageMap.get(contentString.toString()));
        imageView.setFitWidth(cell_size);
        imageView.setFitHeight(cell_size);
        if(isTurned)imageView.setRotate(270);
        int amountOfKnights = ModelInformer.getAmountOfKnightsSpawned();
        int maxKnights = (int)ModelInformer.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS);
        if((amountOfKnights< maxKnights &&cell.getContent()== CellContent.SPAWN))
            imageView.setEffect(Util.getEffect(amountOfKnights,false));
        if(cell.getContent()== CellContent.ENEMY_SPAWN){
            int skelCount = ModelInformer.getAmountOfSkeletonsSpawned();
            imageView.setEffect(Util.getEffect(skelCount,false));
        }

        return imageView;
    }

    private static void calculateCellSize() {
        GameMap gameMap = (GameMap)ModelInformer.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        cell_size = gameMap.getBoundY() > gameMap.getBoundX() ?
                GameConstants.MAX_GAMEMAP_SIZE / ((double) gameMap.getBoundY()) : GameConstants.MAX_GAMEMAP_SIZE / ((double) gameMap.getBoundX());

        if(getCurrentSceneState() == SceneState.PLAY ||getCurrentSceneState() == SceneState.TUTORIAL)
            cell_size = cell_size*GameConstants.PLAY_CELL_SIZE_FACTOR;
        cell_size = Math.round(cell_size);
    }

    public CodeArea getAiCodeArea() {
        return aiCodeArea;
    }

    public GridPane getMapGPane() {
        return mapGPane;
    }

    public void highlightInMap(List<Point> points) {
        GameMap gameMap = (GameMap)ModelInformer.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        this.selectedPointList = new ArrayList<>(points);
        for (Polyline highlight: highlights) {
            levelPane.getChildren().remove(highlight);
        }
        highlights = new ArrayList<>();
        Polyline highlight;
        List<Line> edgeList = new ArrayList<>();
        for(int x = 0; x < gameMap.getBoundX(); x++)
        for(int y = 0; y < gameMap.getBoundY(); y++){
            if (!points.contains(new Point(x, y))) continue;
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

            levelPane.getChildren().add(highlight);
            StackPane.setAlignment(highlight, Pos.TOP_LEFT);
            mapGPane.autosize();
            highlight.autosize();
            //TODO: +10 wegen Border
            mapGPane.layout();
            highlight.setTranslateX(mapGPane.localToScene(mapGPane.getBoundsInLocal()).getMinX()+highlight.getLayoutBounds().getMinX()+10);
            highlight.setTranslateY(mapGPane.localToScene(mapGPane.getBoundsInLocal()).getMinY()+highlight.getLayoutBounds().getMinY()+10);
            highlights.add(highlight);
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
//            if (!btn.getCode().equals(CellContent.EMPTY.getDisplayName()) && !btn.getCode().equals(CellContent.WALL.getDisplayName()))
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
        GameMap gameMap = (GameMap)ModelInformer.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        levelEditorModule.getWidthValueLbl().setText("" + gameMap.getBoundX());
        levelEditorModule.getLevelNameValueLbl().setText("" + ModelInformer.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME));
        Integer[] locToStars = (Integer[]) ModelInformer.getDataFromCurrentLevel(LevelDataType.LOC_TO_STARS);
        Integer[] turnsToStars = (Integer[]) ModelInformer.getDataFromCurrentLevel(LevelDataType.TURNS_TO_STARS);
        levelEditorModule.getMaxLoc2StarsVLbl().setText(locToStars[0]+"");
        levelEditorModule.getMaxLoc3StarsVLbl().setText(locToStars[1]+"");
        levelEditorModule.getMaxTurns2StarsVLbl().setText(turnsToStars[0]+"");
        levelEditorModule.getMaxTurns3StarsVLbl().setText(turnsToStars[1]+"");
        levelEditorModule.getLevelNameValueLbl().setText("" + ModelInformer.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME));
        levelEditorModule.getIndexValueLbl().setText("" + ModelInformer.getCurrentIndex());
        levelEditorModule.getMaxKnightsValueLbl().setText("" + ModelInformer.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS));
        levelEditorModule.getAmountOfRerunsValueLbl().setText("" + ModelInformer.getDataFromCurrentLevel(LevelDataType.AMOUNT_OF_RERUNS));
        levelEditorModule.getIsTutorialValueLbl().setText("" + ModelInformer.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL));
        levelEditorModule.getHeightValueLbl().setText("" + gameMap.getBoundY());

        boolean isTutorial = (boolean)ModelInformer.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL);
        if(isTutorial){
            levelEditorModule.showRequiredLevelsHBox(false);
        }
        else{
            levelEditorModule.showRequiredLevelsHBox(true);
            levelEditorModule.setRequiredLevels(ModelInformer.getCurrentRequiredLevels());
        }
        levelEditorModule.getTutorialVBox().setVisible(isTutorial);
        List<String> tutorialLines = (List<String>)ModelInformer.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES);
        if(isTutorial){
            levelEditorModule.getTutorialTextArea().setText(tutorialLines.get(ModelInformer.getCurrentTutorialMessageIndex()));
            updateTutorialMessage();
        }
        levelEditorModule.getPrevTutorialTextBtn().setDisable(true);
        if(ModelInformer.getCurrentTutorialSize()<= 1){
            levelEditorModule.getNextTutorialTextBtn().setDisable(true);
            levelEditorModule.getPrevTutorialTextBtn().setDisable(true);
        }
        else if(ModelInformer.getCurrentTutorialMessageIndex()==0)
            levelEditorModule.getPrevTutorialTextBtn().setDisable(true);
        else if(ModelInformer.getCurrentTutorialSize()-1== ModelInformer.getCurrentTutorialMessageIndex())
            levelEditorModule.getNextTutorialTextBtn().setDisable(true);
        else {
            levelEditorModule.getNextTutorialTextBtn().setDisable(false);
            levelEditorModule.getPrevTutorialTextBtn().setDisable(false);
        }
        levelEditorModule.getHasAiValueLbl().setText(ModelInformer.getDataFromCurrentLevel(LevelDataType.HAS_AI)+"");

        levelEditorModule.toggleLevelIsSaved(!ModelInformer.currentLevelHasChanged());

        if(ModelInformer.getCurrentIndex() == 0){
            levelEditorModule.getMoveIndexDownBtn().setDisable(true);
        }
        else levelEditorModule.getMoveIndexDownBtn().setDisable(false);
        if(ModelInformer.getCurrentIndex() == ModelInformer.getAmountOfLevels()-1){
            levelEditorModule.getMoveIndexUpBtn().setDisable(true);
        }
        else levelEditorModule.getMoveIndexUpBtn().setDisable(false);
    }

    @Override
    public void updateAll() {
        selectedPointList = new ArrayList<>();
        selectedPointList.add(new Point(0, 0));
        boolean hasAi =(boolean) ModelInformer.getDataFromCurrentLevel(LevelDataType.HAS_AI);
        levelNameLabel.setText(ModelInformer.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME)+"");
        int plays = (int)ModelInformer.getDataFromCurrentLevel(LevelDataType.AMOUNT_OF_RERUNS);
        if(plays > 1)levelNameLabel.setText(levelNameLabel.getText()+" (will play "+plays+" times)");
        ComplexStatement aiBehaviour = (ComplexStatement)ModelInformer.getDataFromCurrentLevel(LevelDataType.AI_CODE);
        if (hasAi) {
            aiCodeArea.setVisible(true);
            if(sceneState == SceneState.LEVEL_EDITOR)clearAICodeBtn.setVisible(true);
            aiCodeArea.updateCodeFields(aiBehaviour);
            aiCodeArea.scrollTo(0);
        }
        else {
            aiCodeArea.setVisible(false);
            clearAICodeBtn.setVisible(false);
        }
        codeArea.scrollTo(0);
        drawMap((GameMap)ModelInformer.getDataFromCurrentLevel(LevelDataType.MAP_DATA));
        if (sceneState == SceneState.LEVEL_EDITOR) {
            updateLevelEditorModule();
        }
        if (sceneState == SceneState.TUTORIAL && ModelInformer.getTutorialProgress()>-1) {
            tutorialGroup.setEntries((List<String>)ModelInformer.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES));
        }
        spellBookPane.updateSpellbookEntries(ModelInformer.getUnlockedStatementList());
        if(ModelInformer.getBestTurnsOfLevel(ModelInformer.getCurrentId()) == -1) loadBestCodeBtn.setDisable(true);
        else loadBestCodeBtn.setDisable(false);
    }

    @Override
    public void updateTemporaryChanges(LevelChange levelChange) {

        switch (levelChange.getLevelDataType()){
            case MAP_DATA:
                GameMap gameMap = (GameMap)ModelInformer.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
                drawMap(gameMap);
                selectedPointList = pointListOutOfBounds();
                if(sceneState == SceneState.LEVEL_EDITOR)
                    Platform.runLater(()->highlightInMap(selectedPointList));
            case LOC_TO_STARS:
            case TURNS_TO_STARS:
            case REQUIRED_LEVELS:
            case IS_TUTORIAL:
                if(levelChange.getLevelDataType().equals(LevelDataType.IS_TUTORIAL)){
                    if((boolean)levelChange.getNewValue())
                        Platform.runLater(()->tutorialLevelOverviewPane.addLevelWithIndex(ModelInformer.getCurrentIndex()));
                    else tutorialLevelOverviewPane.removeCurrentLevel();
                }
            case TUTORIAL_LINES:
            case LEVEL_NAME:
            case HAS_AI:
            case LEVEL_INDEX:
            case AMOUNT_OF_RERUNS:
            case MAX_KNIGHTS:
                levelEditorModule.update(levelChange);
                if(levelChange.getLevelDataType().equals(LevelDataType.MAX_KNIGHTS)){
                    redrawKnightsLeftVBox();
                }
                break;
            case AI_CODE:
                ComplexStatement aiBehaviour = (ComplexStatement)ModelInformer.getDataFromCurrentLevel(LevelDataType.AI_CODE);
                boolean hasAI = (boolean)ModelInformer.getDataFromCurrentLevel(LevelDataType.HAS_AI);
                levelEditorModule.getHasAiValueLbl().setText("" + hasAI);
                if (hasAI && aiBehaviour.getStatementListSize() == 0) {
                    aiCodeArea.updateCodeFields(aiBehaviour);
                    //THIS IS THE ONLY WORKING SOLUTION I FOUND FOR FINDING OUT POSITIONS OF NODES IN SCENE!!
                    double d = getMapGPane().localToScene(getMapGPane().getBoundsInLocal()).getMinX();
                    Platform.runLater(() -> {
                        for(Polyline p: highlights){
                            p.setTranslateX(p.getTranslateX()-d+ getMapGPane().localToScene(getMapGPane().getBoundsInLocal()).getMinX());
                        }});
                    aiCodeArea.setVisible(true);
                    clearAICodeBtn.setVisible(true);
                } else if (!hasAI) {
                    //THIS IS THE ONLY WORKING SOLUTION I FOUND FOR FINDING OUT POSITIONS OF NODES IN SCENE!!
                    double d = getMapGPane().localToScene(getMapGPane().getBoundsInLocal()).getMinX();
                    aiCodeArea.updateCodeFields(new ComplexStatement());
                    Platform.runLater(() -> {
                        for(Polyline p: highlights){
                            p.setTranslateX(p.getTranslateX()-d+ getMapGPane().localToScene(getMapGPane().getBoundsInLocal()).getMinX());
                        }});
                    aiCodeArea.setVisible(false);
                    clearAICodeBtn.setVisible(false);
                }
                break;
        }
        String levelName = ModelInformer.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME)+"";
        if(levelChange.getLevelDataType()==LevelDataType.LEVEL_NAME)levelName = levelChange.getOldValue()+"";
        if(tutorialLevelOverviewPane.containsLevel(levelName))tutorialLevelOverviewPane.updateLevel(levelName);
        if(levelOverviewPane.containsLevel(levelName))levelOverviewPane.updateLevel(levelName);
        updateLevelEditorModule();
    }


    private List<Point> pointListOutOfBounds() {
        List<Point> output = new ArrayList<>();
        for(Point p : selectedPointList){
            GameMap gameMap = (GameMap)ModelInformer.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
            if(p.getX() >= gameMap.getBoundX()||p.getY() >= gameMap.getBoundY())continue;
            output.add(p);
        }
        return output;
    }

    public StartScreen getStartScreen() {
        return startScreen;
    }


    public void setSceneState(SceneState sceneState) {
        View.sceneState = sceneState;
        GameMap gameMap = (GameMap)ModelInformer.getDataFromCurrentLevel(LevelDataType.MAP_DATA);
        switch (sceneState) {
            case START_SCREEN:
                stage.getScene().setRoot(startScreen);
                break;
            case LEVEL_EDITOR:
                drawMap(gameMap);
                prepareRootPane();
                aiCodeArea.setEditable(true);
                Platform.runLater(()->codeArea.select(0, Selection.END));
                stage.getScene().setRoot(levelPane);
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
                drawMap(gameMap);
                prepareRootPane();
                Platform.runLater(()->codeArea.select(0, Selection.END));
                stage.getScene().setRoot(levelPane);
                break;
            case TUTORIAL:
                drawMap(gameMap);
                if(ModelInformer.getTutorialProgress()==-1){
                    isIntroduction = true;
                }
                prepareRootPane();
                Platform.runLater(()->codeArea.select(0, Selection.END));

                stage.getScene().setRoot(levelPane);
                break;

        }
    }

    private void prepareRootPane() {
        levelPane = new StackPane();
        levelPane.setPrefSize(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        HBox contentHBox = new HBox();
        VBox rightVBox = new VBox();
        VBox centerVBox = new VBox();
        rightVBox.setAlignment(Pos.TOP_RIGHT);
        knightsLeftVBox.setAlignment(Pos.TOP_RIGHT);

        rightVBox.setAlignment(Pos.TOP_CENTER);
        rightVBox.getChildren().addAll(vBox);
        baseContentVBox.getChildren().clear();

        HBox bottomHBox = new HBox(backBtn, btnExecute, speedVBox, btnReset, showSpellBookBtn);
        bottomHBox.setAlignment(Pos.BOTTOM_CENTER);
        switch (sceneState) {
            case LEVEL_EDITOR:
                HBox editorCenterHBox = new HBox(knightsLeftVBox, new VBox(mapGPane), new VBox(levelEditorModule.getRightVBox()));
                editorCenterHBox.autosize();
                editorCenterHBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/1.5);
                editorCenterHBox.setAlignment(Pos.TOP_CENTER);
                centerVBox.getChildren().addAll(levelEditorModule.getBottomHBox(), editorCenterHBox);
                centerVBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/2);
                baseContentVBox.getChildren().add(levelEditorModule.getTopHBox());
                levelEditorModule.getTutorialVBox().setVisible((boolean)ModelInformer.getDataFromCurrentLevel(LevelDataType.IS_TUTORIAL));
                updateLevelEditorModule();
                break;
            case LEVEL_SELECT:
                throw new IllegalStateException("Missing error message please TODO! see View -> prepareRootPane()");
            case TUTORIAL_LEVEL_SELECT:
                throw new IllegalStateException("Missing error message please TODO! see View -> prepareRootPane()");
            case PLAY:
                removeMapHighlights();
                HBox centerHBox = new HBox(knightsLeftVBox, mapGPane);
                centerHBox.autosize();
                centerHBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/1.5);
                centerHBox.setAlignment(Pos.TOP_CENTER);
                centerVBox.getChildren().addAll(levelNameLabel, centerHBox);
                break;
            case START_SCREEN:
                throw new IllegalStateException("Missing error message please TODO! see View -> prepareRootPane()");
            case TUTORIAL:
                removeMapHighlights();
                centerHBox = new HBox(knightsLeftVBox, mapGPane);
                centerHBox.autosize();
                centerHBox.setSpacing(GameConstants.TEXTFIELD_HEIGHT/1.5);
                centerHBox.setAlignment(Pos.TOP_CENTER);
                centerVBox.getChildren().addAll(levelNameLabel,centerHBox);

                if(ModelInformer.getTutorialProgress()==-1){
                    isIntroduction = true;
                    tutorialGroup.activateIntroduction();
                    btnExecute.setMouseTransparent(true);
                    codeArea.setDisable(true);
                    speedSlider.setMouseTransparent(true);
                    showSpellBookBtn.setMouseTransparent(true);
                }
                //TODO: move to controller?
                if(isIntroduction){
                    tutorialGroup.setEntries(Util.StringListFromArray(TUTORIAL_LINES));
                }
                else tutorialGroup.setEntries((List<String>)ModelInformer.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES));
                break;
        }
        contentHBox.getChildren().addAll(leftVBox, centerVBox, rightVBox);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setSpacing(BUTTON_SIZE/5);
        contentHBox.setPrefWidth(GameConstants.SCREEN_WIDTH);
        bottomHBox.setSpacing(BUTTON_SIZE);
        bottomHBox.setPickOnBounds(false);
        bottomHBox.setMaxHeight(BUTTON_SIZE);

        levelPane.autosize();
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
        levelPane.getChildren().add(baseContentVBox);
        if (getCurrentSceneState() == SceneState.TUTORIAL) {

            levelPane.getChildren().add(tutorialGroup);


            StackPane.setAlignment(tutorialGroup, Pos.BOTTOM_RIGHT);
            if(isIntroduction){
                StackPane.setAlignment(tutorialGroup, Pos.CENTER);}
            StackPane.setMargin(tutorialGroup, new Insets(5));
        }
        levelPane.getChildren().add(bottomHBox );
        levelPane.getChildren().add(spellBookPane);
        spellBookPane.setVisible(false);
        levelPane.setBackground(brickBackground);
    }

    private void removeMapHighlights() {
        highlightInMap(new ArrayList<>());
    }

    static Image getImageFromMap(GameMap originalMap) {
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

    private static Image makeTransparent(Image inputImage) {
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

    public static SceneState getCurrentSceneState() {
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

    public Button getShowSpellBookBtn() {
        return showSpellBookBtn;
    }

    public void toggleShowSpellBook() {
        spellBookPane.setTranslateX(0);
        spellBookPane.setTranslateY(0);
        if (spellBookPane.isVisible()) {
            spellBookPane.setVisible(false);
            for(Polyline high :highlights){
                high.setVisible(true);
            }
        } else {
            spellBookPane.setVisible(true);
            for(Polyline high :highlights){
                high.setVisible(false);
            }
        }
        if(codeArea.getSelectedCodeField()!=null)
            codeArea.getSelectedCodeField().requestFocus();

        boolean isVisible = getSpellBookPane().isVisible();
        getMapGPane().setMouseTransparent(isVisible);
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
        loadBestCodeBtn.setDisable(ModelInformer.getBestLocOfLevel(ModelInformer.getCurrentId())==-1 || b);
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

    public void addListener(SimpleEventListener eventListener) {
        eventSender = new SimpleEventSender(eventListener);
    }

    public List<Point> getSelectedPointList() {
        return selectedPointList;
    }

    public void deselect() {
        for (Polyline highlight: highlights) {
            levelPane.getChildren().remove(highlight);
        }
    }

    public boolean isIntroduction() {
        return isIntroduction;
    }

    public void leaveIntroductions() {
        tutorialGroup.leaveIntroduction();
        tutorialGroup.setEntries((List<String>)ModelInformer.getDataFromCurrentLevel(LevelDataType.TUTORIAL_LINES));
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
        final int n = 3;
        switch (getTutorialGroup().getCurrentIndex()){
            case n:
                removeEffectsOfControlElements();
                mapGPane.setEffect(dropShadow);
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
        mapGPane.setEffect(null);
        knightsLeftVBox.setEffect(null);
        getBtnExecute().setEffect(null);
        getSpeedSlider().setEffect(null);
        getBtnReset().setEffect(null);
        getShowSpellBookBtn().setEffect(null);
        codeArea.setEffect(null);
    }

    public void updateTutorialMessage() {
        if(getCurrentSceneState()==SceneState.LEVEL_EDITOR){
            levelEditorModule.getTutorialTextArea().setText(ModelInformer.getCurrentTutorialMessage());
            levelEditorModule.getTutorialNumberValueLbl().setText(ModelInformer.getCurrentTutorialMessageIndex()+"");
            if(ModelInformer.getCurrentTutorialSize()>ModelInformer.getCurrentTutorialMessageIndex()+1)levelEditorModule.getNextTutorialTextBtn().setDisable(false);
        }else {
            tutorialGroup.getCurrentTutorialMessage().setText(ModelInformer.getCurrentTutorialMessage());
            if(ModelInformer.getCurrentTutorialSize()>ModelInformer.getCurrentTutorialMessageIndex()+1)tutorialGroup.getNextBtn().setDisable(false);
        }
    }
}