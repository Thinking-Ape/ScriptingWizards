package view;

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
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import main.Tester;
import model.*;
import model.Cell;
import model.enums.CContent;
import model.enums.CFlag;
import model.enums.ItemType;
import model.statement.ComplexStatement;
import parser.JSONParser;
import utility.GameConstants;
import org.jetbrains.annotations.Contract;

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

public class View implements PropertyChangeListener {

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
    private Polyline highlight;
    private LevelEditorModule levelEditorModule;
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private ChoiceBox<String> choiceBox;
    private Image testKnightImage;
    private Image enemyImage;
    private Map<String, Image> contentImageMap = new HashMap<>();

    private LevelOverviewPane levelOverviewPane;
    //    private Scene levelSelectScene;
//    private Scene playScene;
    private Scene startScene;
    //    private Scene editorScene;
//    private Scene tutorialScene;
    private Button backBtn = new Button("Back");
    private SceneState sceneState = SceneState.START_SCREEN;
    private VBox knightsLeftVBox;
    private SpellBookPane spellBookPane = new SpellBookPane();
    private Label levelNameLabel = new Label();
    private TextArea tutorialTextArea = new TextArea();
    private Button showSpellBookBtn = new Button("Show Spellbook");

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


    public View(Model model, Stage stage, boolean isEditor) {
        this.stage = stage;
        this.model = model;
        this.startScreen = new StartScreen();
        tutorialGroup = new TutorialGroup();
        backBtn.setPrefSize(100, 50);
        showSpellBookBtn.setPrefHeight(75);
        startScene = new Scene(startScreen);
        cell_size = model.getCurrentLevel().getOriginalMap().getBoundY() > model.getCurrentLevel().getOriginalMap().getBoundX() ? GameConstants.MAX_GAMEMAP_SIZE / ((double) model.getCurrentLevel().getOriginalMap().getBoundY()) : GameConstants.MAX_GAMEMAP_SIZE / ((double) model.getCurrentLevel().getOriginalMap().getBoundX());
        cell_size = Math.round(cell_size);
        tutorialTextArea.setEditable(false);
        knightsLeftVBox = new VBox();
        knightsLeftVBox.setSpacing(cell_size / 4);
        for (int i = 0; i < model.getCurrentLevel().getMaxKnights(); i++) {
            knightsLeftVBox.getChildren().add(new Rectangle(cell_size / 2, cell_size, Color.LIGHTGREY));
        }
        levelOverviewPane = new LevelOverviewPane(model, this);
//        levelSelectScene = new Scene(levelOverviewPane);
        //TODO: model.getCurrentLevel().addListener(this);
        //Testing
        stage.setWidth(GameConstants.SCREEN_WIDTH);
        stage.setHeight(GameConstants.SCREEN_HEIGHT);
        stage.setMaximized(true);
        if (GameConstants.IS_FULLSCREEN) {
            stage.setFullScreen(true);
        }
        stage.setFullScreenExitHint("");
        codeArea.addNewCodeFieldAtIndex(0, new CodeField("", 1, true));
        model.addChangeListener(this);
        actualMapGPane = new GridPane();
//        actualMapGPane.setHgap(1);
//        actualMapGPane.setVgap(1);
        rootPane = new StackPane();
        baseContentVBox = new VBox();
//        hBoxRoot.setSpacing(4);
        stage.setScene(startScene);
        vBox = new VBox();
        HBox hBox = new HBox();
        btnExecute = new Button("Execute");
        btnExecute.setPrefSize(100, 100);
        btnReset = new Button("Reset");
        btnReset.setPrefSize(100, 100);
        btnReset.setDisable(true);
        speedVBox = new VBox();
        Label speedLbl = new Label("Speed:");
        speedSlider = new Slider(1, 5, 3);
        speedSlider.setBlockIncrement(1);
        speedSlider.setMajorTickUnit(1);
        speedSlider.setMinorTickCount(0);
        speedSlider.setValue(3);
        speedSlider.setSnapToTicks(true);
        speedSlider.setShowTickLabels(true);
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
        if (true) {
            try {
                codeArea = /*new CodeArea()*/Tester.evaluateCodeBox(//"Knight k = new Knight(EAST);","k.move();","k.turn(WEST);","k.move();","Knight k2 = new Knight(EAST);","k2.move();","k2.move();","k2.move();","k2.turn(WEST);","k2.move();");
                        //"Knight k = new Knight(WEST);","k.collect();","k.move();","k.useItem();","k.move();");
                        //"Knight k = new Knight(EAST);","k.move();","k.move();","k.turn(EAST);","if(k.targetIsUnarmed()){","k.move();","k.turn(WEST);","k.move();","k.move();","}","else {","k.turn(2);","k.move();","k.turn(EAST);","k.move();","k.move();","}");
                        //"int i = 10;","Knight k = new Knight(WEST);","k.move();","for(int j = 0;j < i;j = j + 1;){","k.wait();","}","k.turn(2);","k.move();");
                        //"Knight knight = new Knight(EAST);","TurnDirection d = LEFT;","TurnDirection dd = d;","knight.collect();","knight.move();","int turns = 0;","boolean b = knight.canMove();","boolean a = b && true;","if (a) {","knight.turn(dd);","}","while(true) {","if ((!knight.targetIsDanger()) && knight.canMove()) {","knight.move();","}","else if (knight.canMove() || knight.targetCellIs(GATE)) {","knight.wait();","}","else if (knight.targetContainsEntity(SKELETON)) {","knight.useItem();","}","else if (knight.targetContainsItem(KEY)) {","knight.collect();","}","else if (turns < 2) {","turns = turns + 1;","}","else {","knight.turn(LEFT);","}","}");
                        "Knight knight = new Knight(EAST);","int turns = 0;","boolean b = knight.targetContainsEntity(SKELETON);","while(true) {","if ((!knight.targetIsDanger()) && knight.canMove()) {","knight.move();","}","else if (knight.canMove() || knight.targetCellIs(GATE)) {","knight.wait();","}","else if (b || knight.targetCellIs(EXIT)) {","knight.useItem();","}","else if (knight.targetContainsItem(SWORD)) {","knight.collect();","knight.move();","knight.turn(LEFT);","knight.useItem();","knight.useItem();","knight.useItem();","knight.useItem();","knight.useItem();","knight.move();","knight.move();","knight.turn(RIGHT);","}","else if (knight.targetContainsItem(KEY)) {","knight.collect();","knight.turn(AROUND);","}","else if (turns < 3) {","turns = turns + 2;","knight.turn(LEFT);","}","else {","knight.turn(RIGHT);","turns = turns - 1;","}","}");
//                        "Knight knight = new Knight(WEST);","knight.collect();","TurnDirection dir = RIGHT;","for(int i = 0;i <= 6;i = i + 1;) {","for(int j = 0;j < 12;j = j + 1;) {","knight.move();","}","knight.useItem();","knight.turn(dir);","knight.move();","knight.move();","knight.turn(dir);","if (dir == RIGHT) {","dir = LEFT;","}","else {","dir = RIGHT;","}","}");
                        //"Knight knight = new Knight(NORTH);", "knight.collect();", "knight.move();", "knight.useItem();", "knight.move();");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        vBox.getChildren().addAll(codeArea, hBox, msgLabel);
        VBox leftVBox = new VBox();
        leftVBox.getChildren().add(aiCodeArea);


        aiCodeArea.draw();
        codeArea.select(0, false);

        Level l = model.getCurrentLevel();

        if (l != null) drawMap(l.getCurrentMap());
    }


    public void drawMap(GameMap map) {
        actualMapGPane.getChildren().clear();
        actualMapGPane.getChildren().addAll(getGridPaneFromMap(map).getChildren());
        knightsLeftVBox.getChildren().clear();
        for (int i = 0; i < model.getCurrentLevel().getMaxKnights() - model.getCurrentLevel().getUsedKnights(); i++) {
            knightsLeftVBox.getChildren().add(new Rectangle(cell_size / 2, cell_size, Color.LIGHTGREY));
        }
        changeSupport.firePropertyChange("map", null, null);
    }

    private GridPane getGridPaneFromMap(GameMap map) {
        cell_size = model.getCurrentLevel().getOriginalMap().getBoundY() > model.getCurrentLevel().getOriginalMap().getBoundX() ? GameConstants.MAX_GAMEMAP_SIZE / ((double) model.getCurrentLevel().getOriginalMap().getBoundY()) : GameConstants.MAX_GAMEMAP_SIZE / ((double) model.getCurrentLevel().getOriginalMap().getBoundX());
        cell_size = Math.round(cell_size);
        testKnightImage = new Image("file:resources/images/Knight.png", cell_size, cell_size, true, true);
        enemyImage = new Image("file:resources/images/Skeleton.png", cell_size, cell_size, true, true);
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
                for (CFlag flag : CFlag.values()) {
                    if (cell.hasFlag(flag)) {
                        if(flag.isTemporary())
                            continue;
//                            if(cell.hasFlag(CFlag.KNIGHT_DEATH) && contentImageMap.containsKey(entityName+"_Death"))entityName+="_Death";
                        if(flag == CFlag.TURNED && (isTurned = true))continue;
                        if(flag == CFlag.INVERTED){
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
                        contentString += "_" + flag.getDisplayName();
                    }
                }
                if (shape != null && !contentImageMap.containsKey(contentString)) stackPane.getChildren().add(shape);
                else {
                    ImageView imageView = new ImageView(contentImageMap.get(contentString));
                    if(isTurned)imageView.setRotate(270);
                    stackPane.getChildren().add(imageView);
                }
                String itemString = cell.getItem() != null ? cell.getItem().getDisplayName() : "";
                if (cell.getItem() != null && !contentImageMap.containsKey(itemString))
                    stackPane.getChildren().add(getItemShape(cell.getItem()));
                else {
                    ImageView imageView = new ImageView(contentImageMap.get(itemString));
//                    imageView.setFitWidth(cell_size);
//                    imageView.setFitHeight(cell_size);
                    stackPane.getChildren().add(imageView);
                }
                if(cell.hasFlag(CFlag.KNIGHT_DEATH))
                    stackPane.getChildren().add(new ImageView(contentImageMap.get(CFlag.KNIGHT_DEATH.getDisplayName())));
                else if (cell.hasFlag(CFlag.SKELETON_DEATH))stackPane.getChildren().add(new ImageView(contentImageMap.get(CFlag.SKELETON_DEATH.getDisplayName())));
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
                if (cell.getEntity() != null) {
                    if(entityActionList.contains(cell.getEntity())){
                        number = 3;
                    }
                    String entityName = cell.getEntity().getEntityType().getDisplayName();
                    if(cell.getEntity().getItem()!= null && contentImageMap.containsKey(entityName+"_"+cell.getEntity().getItem().getDisplayName()))entityName+="_"+cell.getEntity().getItem().getDisplayName();
                    if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName+"_Action_"+number))entityName+="_Action_"+number;
                    ImageView imageView = new ImageView(contentImageMap.get(entityName));

//                    imageView.setFitHeight(cell_size);
//                    imageView.setFitWidth(cell_size);
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
                                gPane.add(iView, x, y-1);
                            }
                            break;
                        case SOUTH:
                            imageView.setRotate(270);
                            if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName)){
                                iView.setRotate(270);
                                gPane.add(iView, x, y+1);
                            }
                            break;
                        case EAST:
                            imageView.setRotate(180);
                            if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName)){
                                iView.setRotate(180);
                                gPane.add(iView, x+1, y);
                            }
                            break;
                        case WEST:
                            if(cell.hasFlag(CFlag.ACTION) && contentImageMap.containsKey(entityName))gPane.add(iView, x-1, y);
                            break;
                    }
                }
                gPane.add(stackPane, x, y);
//                actualMapGPane.getChildren().add(mapShapes[row][column]);
            }
        }
        return gPane;
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


    @Contract("null -> fail")
    public void setCodeArea(CodeArea codeArea,boolean isAi) {
        if(isAi){
            if (codeArea == null)
                throw new IllegalArgumentException("null is no longer allowed as AiCodeArea! Please use an empty CodeArea instead!");
            this.aiCodeArea = codeArea;
            leftVBox.getChildren().clear();
            leftVBox.getChildren().addAll(new Rectangle(50, 50, Color.MAGENTA), new Label("Enemy Script"), aiCodeArea);
            leftVBox.setAlignment(Pos.TOP_LEFT);
        }
        else {this.codeArea = codeArea;
        vBox.getChildren().set(0, codeArea);}
        codeArea.draw();
        changeSupport.firePropertyChange("codeArea", null, codeArea);
    }

    public CodeArea getAICodeArea() {
        return aiCodeArea;
    }

//    @Contract("null -> fail")
//    public void setAiCodeArea(CodeArea newCodeArea) {
//        if (newCodeArea == null)
//            throw new IllegalArgumentException("null is no longer allowed as AiCodeArea! Please use an empty CodeArea instead!");
//        this.aiCodeArea = newCodeArea;
////        rootPane.setLeft(aiCodeArea);
////        aiCodeArea.setAlignment(Pos.TOP_LEFT);
//        //TODO: DOesnt work like this!!
//        leftVBox.getChildren().clear();
//        leftVBox.getChildren().addAll(new Rectangle(50, 50, Color.MAGENTA), new Label("Enemy Script"), aiCodeArea);
//        leftVBox.setAlignment(Pos.TOP_LEFT);
//    }

    public GridPane getActualMapGPane() {
        return actualMapGPane;
    }

    public void highlightInMap(int k, int h) {
        //TODO: actualMapGPANE sollte gridpane aus StackPanes sein!
//        for(int i = 0; i < actualMapGPane.getChildren().size();i++){
        if (highlight != null) actualMapGPane.getChildren().remove(highlight);
        highlight = new Polyline(0, 0, 0, cell_size, cell_size, cell_size, cell_size, 0, 0, 0);
        highlight.setStroke(Color.WHITE);
        highlight.setSmooth(true);
        highlight.setStrokeWidth(2);
        highlight.setStrokeType(StrokeType.INSIDE);
        actualMapGPane.add(highlight, k, h);
        selectedColumn = k;
        selectedRow = h;
    }

    public void setCellTypeButtonActive(CContent content) {
        for (Node n : levelEditorModule.getCellTypeSelectionGPane().getChildren()) {
            Button btn = (Button) n;
            if (btn.getText().equals(content.getDisplayName())) btn.setDisable(true);
            else btn.setDisable(false);
        }

    }

    public void setItemTypeButtonActive(ItemType item) {
        for (Node n : levelEditorModule.getCellTypeSelectionGPane().getChildren()) {
            Button btn = (Button) n;
            if (btn.getText().equals(item.getDisplayName())) btn.setDisable(true);
            else btn.setDisable(false);
        }

    }

    public GridPane getCellTypeSelectionPane() {
        return levelEditorModule.getCellTypeSelectionGPane();
    }

    public int getSelectedRow() {
        return selectedRow >= 0 ? selectedRow : 0;
    }

    public int getSelectedColumn() {
        return selectedColumn >= 0 ? selectedColumn : 0;
    }

    public VBox getCellVBox() {
        return levelEditorModule.getRightVBox();
    }

    public void addChoiceBox(ChoiceBox<String> choiceBox) {
        levelEditorModule.getRightVBox().getChildren().add(choiceBox);
        this.choiceBox = choiceBox;
    }

    public void removeChoiceBox() {
        if (choiceBox != null) {
            levelEditorModule.getRightVBox().getChildren().remove(choiceBox);
            this.choiceBox = null;
        }
    }


    public ListView<Integer> getLinkedCellsListView() {
        return levelEditorModule.getLinkedCellListView();
    }

    public LevelEditorModule getLevelEditorModule() {
        return levelEditorModule;
    }

    public void setCContentButtonInactive(CContent content) {
        for (Node n : levelEditorModule.getCellTypeSelectionGPane().getChildren()) {
            Button btn = (Button) n;
            if (btn.getText().equals(content.getDisplayName())) btn.setDisable(true);
        }
    }

    public void setItemButtonInactive(ItemType item) {
        for (Node n : levelEditorModule.getCellItemSelectionGPane().getChildren()) {
            Button btn = (Button) n;
            if (item == null) {
                if (btn.getText().equals("None")) btn.setDisable(true);
            } else if (btn.getText().equals(item.getDisplayName())) btn.setDisable(true);
        }
    }

    public void setNormalButtonsInactive() {
        for (Node n : levelEditorModule.getCellTypeSelectionGPane().getChildren()) {
            Button btn = (Button) n;
            if (!btn.getText().equals(CContent.EMPTY.getDisplayName()) && !btn.getText().equals(CContent.WALL.getDisplayName()))
                btn.setDisable(true);
        }
        for (Node n : levelEditorModule.getCellItemSelectionGPane().getChildren()) {
            Button btn = (Button) n;
            if (!btn.getText().equals(CContent.EMPTY.getDisplayName()) && !btn.getText().equals(CContent.WALL.getDisplayName()))
                btn.setDisable(true);
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
                selectedRow = 0;
                selectedColumn = 0;
                if (model.getCurrentLevel().hasAi()) {
                    aiCodeArea = new CodeArea(model.getCurrentLevel().getAIBehaviour(),true);
                    setCodeArea(aiCodeArea,true);
                } else {
                    setCodeArea(new CodeArea(new ComplexStatement(), false),true);
                }
                if (sceneState == SceneState.LEVEL_EDITOR) updateLevelEditorModule();
            case "map":
                drawMap(model.getCurrentLevel().getOriginalMap());
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
                drawMap(model.getCurrentLevel().getOriginalMap());
                break;
            case "height":
                levelEditorModule.getHeightValueLbl().setText(model.getCurrentLevel().getOriginalMap().getBoundY() + "");
                drawMap(model.getCurrentLevel().getOriginalMap());
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
                    knightsLeftVBox.getChildren().add(new Rectangle(cell_size / 2, cell_size, Color.LIGHTGREY));
                }
                break;
            case "aiBehaviour":
//                System.out.println("First");
                ComplexStatement aiBehaviour = (ComplexStatement) evt.getNewValue();
                if (aiBehaviour.getStatementListSize() > 0) levelEditorModule.getHasAiValueLbl().setText("" + true);
                else levelEditorModule.getHasAiValueLbl().setText("" + false);
                if (model.getCurrentLevel().hasAi() && ((ComplexStatement) evt.getOldValue()).getStatementListSize() == 0) {
                    aiCodeArea = new CodeArea(model.getCurrentLevel().getAIBehaviour(),true);
                    setCodeArea(aiCodeArea,true);
//                    aiCodeArea.draw();
                } else if (!model.getCurrentLevel().hasAi()) {
                    setCodeArea(new CodeArea(new ComplexStatement(), false),true);
                }
                break;
            case "isTutorial":
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
        }
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
                aiCodeArea.setEditable(true);
                aiCodeArea.deselectAll();
                codeArea.deselectAll();
                codeArea.select(0, true);
                stage.getScene().setRoot(rootPane);
                break;
            case LEVEL_SELECT:
                stage.getScene().setRoot(levelOverviewPane);
                levelOverviewPane.getLevelListView().getSelectionModel().select(0);
                break;
            case PLAY:
                prepareRootPane();
                aiCodeArea.setEditable(false);
                aiCodeArea.deselectAll();
                codeArea.deselectAll();
                codeArea.select(0, true);
                levelOverviewPane.updateUnlockedLevels(model, this);
                stage.getScene().setRoot(rootPane);
                break;
            case TUTORIAL:
                prepareRootPane();
                aiCodeArea.setEditable(false);
                aiCodeArea.deselectAll();
                codeArea.deselectAll();
                codeArea.select(0, true);
//                levelOverviewPane.updateUnlockedLevels(model, this);
                try {
                    if(JSONParser.getTutorialProgressIndex()==-1){
                        stage.getScene().setRoot(introductionPane);
                        introductionPane.getTutorialGroup().getNextBtn().requestFocus();
                    }
                    else {
                        stage.getScene().setRoot(rootPane);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    private void prepareRootPane() {
        rootPane = new StackPane();
        rootPane.setPrefSize(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        levelNameLabel.setText(model.getCurrentLevel().getName());
        HBox contentHBox = new HBox();
        setCodeArea(aiCodeArea,true);
        setCodeArea(codeArea,false);
        rightVBox.getChildren().clear();
        centerVBox.getChildren().clear();
        rightVBox.setAlignment(Pos.TOP_RIGHT);
        knightsLeftVBox.setAlignment(Pos.TOP_RIGHT);
        rightVBox.getChildren().addAll(new Rectangle(50, 50, Color.AZURE), new Label("Script"), vBox);
        baseContentVBox.getChildren().clear();
        HBox topCenterHBox;
        switch (sceneState) {
            case LEVEL_EDITOR:
                levelEditorModule = new LevelEditorModule(model.getCurrentLevel());
                HBox editorCenterHBox = new HBox(knightsLeftVBox, new VBox(actualMapGPane), new VBox(levelEditorModule.getRightVBox()));
                editorCenterHBox.autosize();
                editorCenterHBox.setSpacing(25);
                editorCenterHBox.setAlignment(Pos.TOP_CENTER);
                centerVBox.getChildren().addAll(levelEditorModule.getBottomHBox(), editorCenterHBox);
                centerVBox.setSpacing(10);
//                editorScene = new Scene(rootPane);
                baseContentVBox.getChildren().add(levelEditorModule.getTopHBox());
                levelEditorModule.getTutorialVBox().setVisible(model.getCurrentLevel().isTutorial());
                break;
            case LEVEL_SELECT:
                throw new IllegalStateException("Missing error message please TODO! see View -> prepareRootPane()");
            case PLAY:
//                topCenterHBox = new HBox(levelNameLabel, );
                centerVBox.getChildren().addAll(levelNameLabel, new HBox(knightsLeftVBox, actualMapGPane));
//                playScene = new Scene(rootPane);
                break;
            case START_SCREEN:
                throw new IllegalStateException("Missing error message please TODO! see View -> prepareRootPane()");
            case TUTORIAL:
//                topCenterHBox = new HBox(levelNameLabel, knightsLeftVBox);
                centerVBox.getChildren().addAll(levelNameLabel, new HBox(knightsLeftVBox, actualMapGPane));
//                tutorialScene = new Scene(rootPane);
                tutorialGroup.setEntries(model.getCurrentLevel().getTutorialEntryList());
                break;
        }
        contentHBox.getChildren().addAll(leftVBox, centerVBox, rightVBox);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setSpacing(50);
        contentHBox.setPrefWidth(GameConstants.SCREEN_WIDTH);
        HBox bottomHBox = new HBox(backBtn, btnExecute, speedVBox, btnReset, showSpellBookBtn);
        bottomHBox.setSpacing(100);
        bottomHBox.setAlignment(Pos.BOTTOM_CENTER);
        baseContentVBox.getChildren().addAll(contentHBox, bottomHBox);
        rootPane.getChildren().add(baseContentVBox);
        rootPane.getChildren().add(spellBookPane);
        if (getCurrentSceneState() == SceneState.TUTORIAL) {
//            rootPane.setAlignment(Pos.BOTTOM_RIGHT);
            rootPane.getChildren().add(tutorialGroup);
            StackPane.setAlignment(tutorialGroup, Pos.BOTTOM_RIGHT);
            StackPane.setMargin(tutorialGroup, new Insets(5));
        }
        spellBookPane.setVisible(false);
    }

    public void setAllItemTypeButtonActive() {
        for (Node btn : getCellItemSelectionPane().getChildren()) {
            btn.setDisable(false);
        }
    }

    public void setAllItemTypeButtonInActive() {
        for (Node btn : getCellItemSelectionPane().getChildren()) {
            btn.setDisable(true);
        }
    }

    public Image getImageFromMap(GameMap originalMap) {
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
        if (spellBookPane.isVisible()) {
            showSpellBookBtn.setText("Show Spellbook");
            spellBookPane.setVisible(false);
        } else {
            showSpellBookBtn.setText("Hide Spellbook");
            spellBookPane.setVisible(true);
        }
        codeArea.getSelectedCodeField().requestFocus();
        //TODO: find better solution
//        spellBookPane.setTranslateX(200);
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
}
//KEYTHIEF: "Knight knight = new Knight(EAST);","int turns = 0;","while(true) {","if ((!knight.targetIsDanger()) && knight.canMove()) {","knight.move();","}","else if (knight.canMove() || knight.targetCellIs(GATE)) {","knight.wait();","}","else if (knight.targetContains(SKELETON) || knight.targetCellIs(EXIT)) {","knight.useItem();","}","else if (knight.targetContainsItem(SWORD)) {","knight.collect();","knight.turn(LEFT);","knight.move();","knight.move();","knight.turn(RIGHT);","}","else if (knight.targetContains(KEY)) {","knight.collect();","knight.turn(AROUND);","}","else if (turns < 3) {","turns = turns + 2;","knight.turn(LEFT);","}","else {","knight.turn(RIGHT);","turns = turns - 1;","}","}"
//COLLECTANDDROP: "Knight knight = new Knight(NORTH);","knight.move();","knight.turn(RIGHT);","knight.move();","knight.collect();","knight.turn(AROUND);","knight.move();","knight.turn(RIGHT);","knight.collect();","knight.turn(AROUND);","knight.dropItem();","knight.turn(AROUND);","knight.collect();","knight.move();","knight.move();","knight.dropItem();","knight.turn(AROUND);","knight.move();","knight.move();","knight.collect();","knight.turn(AROUND);","knight.move();","knight.move();","knight.turn(AROUND);","knight.dropItem();","knight.turn(AROUND);","knight.collect();","knight.move();","knight.turn(RIGHT);","knight.move();","knight.useItem();"