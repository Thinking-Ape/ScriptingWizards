package view;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
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
import model.util.GameConstants;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class View implements PropertyChangeListener {

    private Stage stage;
    private Model model;
    //Testing

    private Canvas canvas;
    private double cell_size;
    private StartScreen startScreen;
    private Button btnExecute;
    private Button btnReset;
    TextArea codeTextArea;
    private CodeArea codeArea = new CodeArea();
    private CodeArea aiCodeArea;
    private VBox vBox;
    private Slider speedSlider;
    private Label msgLabel = new Label();
    private Shape[][] mapShapes;
    private GridPane actualMapGPane;
    private BorderPane rootPane;
    private Polyline highlight;
    private LevelEditorModule levelEditorModule;
    private int selectedRow=-1;
    private int selectedColumn=-1;
    private ChoiceBox<String> choiceBox;
    private Image testKnightImage;
    private Image enemyImage;
    private Map<String,Image> contentImageMap = new HashMap<>();

    private LevelOverviewPane levelOverviewPane;
    private Scene levelSelectScene;
    private Scene playScene;
    private Scene startScene;
    private Scene editorScene;
    private Scene tutorialScene;
    private Button backBtn = new Button("Back");
    private SceneState sceneState = SceneState.START_SCREEN;
    private HBox knightsLeftHBox;
    private SpellBookPane spellBookPane = new SpellBookPane();

    public View(Model model, Stage stage,boolean isEditor) {
        this.stage = stage;
        this.model = model;
        this.startScreen = new StartScreen();
        startScene = new Scene(startScreen);
        cell_size = model.getCurrentLevel().getOriginalMap().getBoundY()>model.getCurrentLevel().getOriginalMap().getBoundX()? GameConstants.MAX_CELL_SIZE/((double)model.getCurrentLevel().getOriginalMap().getBoundY()):GameConstants.MAX_CELL_SIZE/((double)model.getCurrentLevel().getOriginalMap().getBoundX());
        cell_size = Math.round(cell_size);

//        for(CContent content : CContent.values()){
//            if(new File("resources/images/"+content.getDisplayName()+".png").exists())
//            contentImageMap.put(content.getDisplayName(),new Image("file:resources/images/"+content.getDisplayName()+".png",cell_size,cell_size,true,true));
//        }
        knightsLeftHBox = new HBox();
        knightsLeftHBox.setSpacing(cell_size/4);
        for(int i = 0; i < model.getCurrentLevel().getMaxKnights();i++){
            knightsLeftHBox.getChildren().add(new Rectangle(cell_size/2,cell_size,Color.LIGHTGREY));
        }
        levelOverviewPane = new LevelOverviewPane(model, this);
        levelSelectScene = new Scene(levelOverviewPane);
        //TODO: model.getCurrentLevel().addListener(this);
        //Testing
        stage.setWidth(isEditor ? 1800 : 1300);
        stage.setHeight(900);
        codeArea.addNewCodeFieldAtIndex(0,new CodeField("",1,true));
        model.addChangeListener(this);
        actualMapGPane = new GridPane();
//        actualMapGPane.setHgap(1);
//        actualMapGPane.setVgap(1);
        rootPane = new BorderPane();
//        hBoxRoot.setSpacing(4);
        stage.setScene(startScene);
        vBox = new VBox();
        HBox hBox = new HBox();
        btnExecute = new Button("Execute");
        btnReset = new Button("Reset");
        btnReset.setDisable(true);
        VBox speedVBox = new VBox();
        Label speedLbl = new Label("Speed:");
        speedSlider = new Slider(1,5,3);
        speedSlider.setBlockIncrement(1);
        speedSlider.setMajorTickUnit(1);
        speedSlider.setMinorTickCount(0);
        speedSlider.setValue(3);
        speedSlider.setSnapToTicks(true);
        speedSlider.setShowTickLabels(true);
        speedVBox.getChildren().addAll(speedLbl,speedSlider);
        //TODO: delete
        Button debugBtn = new Button("Clipboard");

        debugBtn.setOnAction(event ->{
            String debug= "";
            for(String s : codeArea.getAllText()){
                debug += "\"" + s+"\",";
            }
            debug = debug.substring(0,debug.length()-1);
            StringSelection selection = new StringSelection(debug);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        });
        //TODO: delete

        hBox.getChildren().addAll(btnExecute,btnReset, speedVBox,debugBtn);
        if(model.getCurrentLevel().getAIBehaviour().getStatementListSize() > 0)
        aiCodeArea = new CodeArea(model.getCurrentLevel().getAIBehaviour(),isEditor);
        //"Knight knight = new Knight(WEST);","int turns = 0;","while(true){","if(knight.targetIsUnarmed() && knight.canMove()){","knight.move();","}","else if (knight.canMove() || knight.targetCellIs(GATE)){","knight.wait();","}","else if (knight.targetCellIs(EXIT)){","knight.useItem();","}","else if (knight.targetCellIs(KEY)){","knight.collect();","}","else if (turns < 2){","turns = turns + 1;","knight.turn(EAST);","}","else {","knight.turn(WEST);","}","}"
        if(true){try {
            codeArea = /*new CodeArea()*/Tester.evaluateCodeBox(//"Knight k = new Knight(EAST);","k.move();","k.turn(WEST);","k.move();","Knight k2 = new Knight(EAST);","k2.move();","k2.move();","k2.move();","k2.turn(WEST);","k2.move();");
                    //"Knight k = new Knight(WEST);","k.collect();","k.move();","k.useItem();","k.move();");
                    //"Knight k = new Knight(EAST);","k.move();","k.move();","k.turn(EAST);","if(k.targetIsUnarmed()){","k.move();","k.turn(WEST);","k.move();","k.move();","}","else {","k.turn(2);","k.move();","k.turn(EAST);","k.move();","k.move();","}");
                    //"int i = 10;","Knight k = new Knight(WEST);","k.move();","for(int j = 0;j < i;j = j + 1;){","k.wait();","}","k.turn(2);","k.move();");
                    "Knight knight = new Knight(WEST);","knight.collect();","knight.move();","int turns = 0;","while(true){","if((!knight.targetIsDanger()) && knight.canMove()){","knight.move();","}","else if (knight.canMove() || knight.targetCellIs(GATE)){","knight.wait();","}","else if (knight.targetContains(SKELETON)){","knight.useItem();","}","else if (knight.targetContains(KEY) && !knight.hasItem(SWORD)){","knight.collect();","}","else if (turns < 2){","turns = turns + 1;","knight.turn(RIGHT);","}","else {","knight.turn(LEFT);","}","}");
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }}
//        codeTextArea.setText("Knight knight = new Knight(WEST);\n" +
//                "for(int i = 0; i < 4; i++){\n" +
//                "knight.turn(5);\n" +
//                "}\n"+
//                "boolean aha = true;\n"+
//                "while(aha){\n" +
//                "if(knight.canMove()||knight.targetCellIs(GATE)){\n" +
//                "if(knight.targetIsUnarmed()&&knight.canMove()){\n" +
//                "knight.move();\n" +
//                "}\n" +
//                "else {\n" +
//                "knight.wait();\n" +
//                "}\n" +
//                "}\n" +
//                "else if(knight.targetCellIs(EXIT)){\n" +
//                "knight.useItem();\n" +
//                "}\n" +
//                "else if(knight.targetCellIs(KEY)) {\n" +
//                "aha = true;\n" +
//                "knight.collect();\n" +
//                "}\n" +
//                "else if(knight.hasItem(KEY)){\n" +
//                "knight.turn(WEST);\n" +
//                "}\n" +
//                "for(int i = 0; i < 4; i++){\n" +
//                "knight.turn(EAST);\n" +
//                "}\n" +
//                "}");
        vBox.getChildren().addAll(/*codeBoxCompound*/ codeArea,hBox,msgLabel);
//        canvas = new Canvas(500,500);
//        canvas.getGraphicsContext2D().setFill(Color.BLACK);
//        canvas.getGraphicsContext2D().fillRect(20,20,20,20);
//        canvas.getGraphicsContext2D().fill();
        VBox leftVBox = new VBox();
        if(aiCodeArea!=null)leftVBox.getChildren().add(aiCodeArea);
        //TODO: implement better!
        /*TextArea textArea = new TextArea();
        textArea.setMinHeight(500);//TODO: make better
        textArea.setWrapText(true);
        textArea.setText("Mögliche Methoden: \nnew Knight( | WEST | EAST | NORTH | SOUTH)\n");
        for(MethodType methodType :MethodType.values()){
            textArea.setText( textArea.getText()+ methodType.getDisplayName());
            switch (methodType){
                default:
                    textArea.setText( textArea.getText()+ "()");
                    break;
                case TURN:
                    textArea.setText( textArea.getText()+ "(EAST | WEST | AROUND)");
                    break;
                case HAS_ITEM:
                    textArea.setText( textArea.getText()+ "(");
                    for(CContent content : CContent.values()){
                        if (content.isCollectible())textArea.setText( textArea.getText()+ content.getDisplayName().toUpperCase()+" | ");
                    }
                    textArea.setText(textArea.getText(0,textArea.getText().length()-2));
                    textArea.setText( textArea.getText()+ ")");
                    break;
                case TARGET_CELL_IS:
                    textArea.setText( textArea.getText()+ "(");
                    for(CContent content : CContent.values()){
                        if (!content.isCollectible())textArea.setText( textArea.getText()+ content.getDisplayName().toUpperCase()+" | ");
                    }
                    textArea.setText(textArea.getText(0,textArea.getText().length()-2));
                    textArea.setText( textArea.getText()+ ")");
                    break;
                case TARGET_CONTAINS:
                    textArea.setText( textArea.getText()+ "(");
                    for(CContent content : CContent.values()){
                        if (!content.isCollectible())textArea.setText( textArea.getText()+ content.getDisplayName().toUpperCase()+" | ");
                    }
                    for(EntityType entityType : EntityType.values()){
                        textArea.setText( textArea.getText()+ entityType.name().toUpperCase()+" | ");
                    }
                    textArea.setText(textArea.getText(0,textArea.getText().length()-2));
                    textArea.setText( textArea.getText()+ ")");
                    break;
            }
            textArea.setText( textArea.getText()+ "\n");
        }
        leftVBox.getChildren().add(textArea);*/
        //TODO: ^
//        rootPane.setLeft(leftVBox);
//        rootPane.setRight(vBox);
//        if(isEditor) {
//            levelEditorModule = new LevelEditorModule(model.getCurrentLevel());
////            model.getCurrentLevel().addChangeListener(levelEditorModule);
//            rootPane.setCenter(new HBox(actualMapGPane,levelEditorModule.getRightVBox()));
//            rootPane.setTop(levelEditorModule.getTopHBox());
//            rootPane.setBottom(levelEditorModule.getBottomHBox());
//
//        }else{
//            rootPane.setCenter(canvas);
//        }

        codeArea.draw();
        if(aiCodeArea !=null)aiCodeArea.draw();
        codeArea.select(0,false);

        Level l = model.getCurrentLevel();

        if(l != null) drawMap(l.getCurrentMap());
    }


    public void drawMap(GameMap map){
        actualMapGPane.getChildren().clear();
        actualMapGPane.getChildren().addAll(getGridPaneFromMap(map).getChildren());
        knightsLeftHBox.getChildren().clear();
        for(int i = 0; i < model.getCurrentLevel().getMaxKnights()-model.getCurrentLevel().getUsedKnights();i++){
            knightsLeftHBox.getChildren().add(new Rectangle(cell_size/2,cell_size,Color.LIGHTGREY));
        }

    }

    private GridPane getGridPaneFromMap(GameMap map) {
        cell_size = model.getCurrentLevel().getOriginalMap().getBoundY()>model.getCurrentLevel().getOriginalMap().getBoundX()? GameConstants.MAX_CELL_SIZE/((double)model.getCurrentLevel().getOriginalMap().getBoundY()):GameConstants.MAX_CELL_SIZE/((double)model.getCurrentLevel().getOriginalMap().getBoundX());
        cell_size = Math.round(cell_size);
        testKnightImage = new Image("file:resources/images/Knight.png",cell_size,cell_size,true,true);
        enemyImage = new Image("file:resources/images/TestEnemy.png",cell_size,cell_size,true,true);
        File folder = new File(Paths.get(GameConstants.ROOT_PATH+"/images").toString());
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for(File file : listOfFiles){
            String s = file.getName().replaceAll("\\.png", "");
            contentImageMap.put(s,new Image("file:resources/images/"+s+".png",cell_size,cell_size,true,true));
        }
        GridPane gPane = new GridPane();
        gPane.getChildren().clear();
        gPane.setHgap(0);
        gPane.setVgap(0);
        mapShapes = new Shape[map.getBoundX()][map.getBoundY()];
        for (int x = 0; x < map.getBoundX(); x++){
            for (int y = 0; y < map.getBoundY(); y++){
                final Cell cell = map.getCellAtXYClone(x,y);
//                drawCell(map[row][column],column,row);
                Shape shape = mapShapes[x][y] = getCellShape(cell);
//                GridPane.setRowIndex(mapShapes[row][column],row);
//                GridPane.setColumnIndex(mapShapes[row][column],column);
                StackPane stackPane = new StackPane();
                String contentString = cell.getContent().getDisplayName();
                for(CFlag flag : CFlag.values()){
                    if(cell.hasFlag(flag))contentString +="_"+flag.getDisplayName();
                }
                if(shape != null && !contentImageMap.containsKey(contentString)) stackPane.getChildren().add(shape);
                else {
                    ImageView imageView = new ImageView(contentImageMap.get(contentString));
//                    imageView.setFitWidth(cell_size);
//                    imageView.setFitHeight(cell_size);
                    stackPane.getChildren().add(imageView);
                }
                if(cell.getItem()!=null) stackPane.getChildren().add(getItemShape(cell.getItem()));
//                if(cell.getEntity()!=null)stackPane.getChildren().add(getEntityShape(cell.getEntity()));
                if(cell.getEntity()!=null){

                    ImageView imageView = new ImageView();
                    switch (cell.getEntity().getEntityType()){

                        case KNIGHT:
                            imageView = new ImageView(testKnightImage);
                            break;
                        case SKELETON:
                            imageView = new ImageView(enemyImage);
                            break;
                    }
//                    imageView.setFitHeight(cell_size);
//                    imageView.setFitWidth(cell_size);
                    stackPane.getChildren().add(imageView);
                    switch (cell.getEntity().getDirection()){
                        case NORTH:
                            imageView.setRotate(90);
                            break;
                        case SOUTH:
                            imageView.setRotate(270);
                            break;
                        case EAST:
                            imageView.setRotate(180);
                            break;
                        case WEST:
                            break;
                    }
                }
                gPane.add(stackPane,x,y);
//                actualMapGPane.getChildren().add(mapShapes[row][column]);
            }
        }
        return gPane;
    }

    private Shape getItemShape(ItemType content) {
        Color color = Color.BLACK;
        switch(content){
            case KEY:
                color =Color.GOLD;
                break;
            case SWORD:
                color =Color.LIGHTGRAY;
                break;
            case SHOVEL:
                color =Color.DARKGRAY;
                break;
            case BOULDER:
                color =Color.GRAY;
                break;
        }
        Circle circle = new Circle(cell_size/2,cell_size/2,cell_size/2);
        circle.setFill(color);
        return circle;
    }

    private Shape getCellShape(Cell cell) {
        CContent content = cell.getContent();
        Shape shape;
        Color color = Color.WHITE;
//        canvas.getGraphicsContext2D().setFill(Color.WHITE);
//        canvas.getGraphicsContext2D().fillRect(column*cell_size,row*cell_size,cell_size,cell_size);
        switch(content){
            case EMPTY:
                color = Color.TRANSPARENT;
                break;
            case DIRT:
                color = Color.SANDYBROWN;
                break;
            case GATE:
                if(cell.hasFlag(CFlag.OPEN))color = Color.WHITE;
                else color = Color.DARKGRAY;
                break;
            case EXIT:
                if(cell.hasFlag(CFlag.OPEN))color = Color.CHOCOLATE;
                else color = Color.BROWN;
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
                if(cell.hasFlag(CFlag.TRIGGERED))color =Color.LIMEGREEN;
                else color = Color.LIGHTGREEN;
                break;
            case TRAP:
                if(cell.hasFlag(CFlag.PREPARING))color =Color.ORANGE;
                else if(cell.hasFlag(CFlag.ARMED))color =Color.RED;
                else color =Color.YELLOW;
                break;
            case WALL:
                color = Color.GRAY;
                break;
        }

//        .setBackground(new Background(new BackgroundFill(color,new CornerRadii(5,true),null)));

        shape = new Rectangle(0,0,cell_size,cell_size);
        shape.setFill(color);

//        ((Rectangle) shape).setHeight(cell_size);
//        ((Rectangle) shape).setWidth(cell_size);

        return shape;
    }

    //TODO: delete
    private Shape getEntityShape(Entity entity){
        Color color = Color.BLACK;
        Shape shape = null;
        switch (entity.getEntityType()){
            case KNIGHT:
                color  =Color.LIGHTGRAY;
                break;
            case SKELETON:
                color = Color.PURPLE;
                break;
        }
        switch (entity.getDirection()){
            case NORTH:
                shape = new Polygon(0,cell_size,cell_size/2,0,cell_size,cell_size);
                break;
            case SOUTH:
                shape = new Polygon(0,0,cell_size,0,cell_size/2,cell_size);
                break;
            case EAST:
                shape = new Polygon(0,cell_size,cell_size,cell_size/2,0,0);
                break;
            case WEST:
                shape = new Polygon(0,cell_size/2,cell_size,cell_size,cell_size,0);
                break;
        }
        shape.setFill(color);
        return shape;
    }


    public Button getBtnExecute() {
        return btnExecute;
    }
    //@Override
    //TODO: rework
//    public void notify(Event event) {
//        switch (event){
//            case LEVEL_CHANGED:
//                if(model.getCurrentLevel().hasAi()){
//                aiCodeArea = new CodeArea(model.getCurrentLevel().getAIBehaviour());
//                setAiCodeArea(aiCodeArea);
//                aiCodeArea.draw();
//                }
//                else {
//                    setAiCodeArea(null);
//                }
//            case MAP_CHANGED:
//                GameMap cells = model.getCurrentLevel().getCurrentMap();
//                drawMap(cells);
//                break;
//        }
//
//    }

    public CodeArea getCodeArea() {
        return codeArea;
    }

    public Button getBtnReset() {
        return btnReset;
    }

    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;
        vBox.getChildren().set(0,codeArea);
    }

    public Slider getSpeedSlider() {
        return speedSlider;
    }

    public Label getMsgLabel() {
        return msgLabel;
    }

    public CodeArea getAICodeArea() {
        return aiCodeArea;
    }

    public void setAiCodeArea(CodeArea newCodeArea) {
        this.aiCodeArea = newCodeArea;
        rootPane.setLeft(aiCodeArea);
    }
    public GridPane getActualMapGPane(){
        return actualMapGPane;
    }

    public void highlightInMap(int k,int h) {
        //TODO: actualMapGPANE sollte gridpane aus StackPanes sein!
//        for(int i = 0; i < actualMapGPane.getChildren().size();i++){
        if(highlight != null)actualMapGPane.getChildren().remove(highlight);
        highlight = new Polyline(0,0,0,cell_size,cell_size,cell_size,cell_size,0,0,0);
        highlight.setStroke(Color.WHITE);
        highlight.setSmooth(true);
        highlight.setStrokeWidth(2);
        highlight.setStrokeType(StrokeType.INSIDE);
        actualMapGPane.add(highlight,k,h);
        selectedColumn = k;
        selectedRow = h;
    }

    public void setCellTypeButtonActive(CContent content) {
        for(Node n : levelEditorModule.getCellTypeSelectionGPane().getChildren()){
            Button btn = (Button)n;
            if(btn.getText().equals(content.getDisplayName()))btn.setDisable(true);
            else btn.setDisable(false);
        }

    }
    public void setItemTypeButtonActive(ItemType item) {
        for(Node n : levelEditorModule.getCellTypeSelectionGPane().getChildren()){
            Button btn = (Button)n;
            if(btn.getText().equals(item.getDisplayName()))btn.setDisable(true);
            else btn.setDisable(false);
        }

    }
    public GridPane getCellTypeSelectionPane(){
        return levelEditorModule.getCellTypeSelectionGPane();
    }
    public int getSelectedRow(){
        return selectedRow >= 0 ? selectedRow : 0;
    }
    public int getSelectedColumn(){
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
        if(choiceBox!=null){
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
        for(Node n : levelEditorModule.getCellTypeSelectionGPane().getChildren()){
            Button btn = (Button)n;
            if(btn.getText().equals(content.getDisplayName()))btn.setDisable(true);
        }
    }
    public void setItemButtonInactive(ItemType item) {
        for(Node n : levelEditorModule.getCellItemSelectionGPane().getChildren()){
            Button btn = (Button)n;
            if(item == null){if(btn.getText().equals("None"))btn.setDisable(true);}
            else if(btn.getText().equals(item.getDisplayName()))btn.setDisable(true);
        }
    }

    public void setNormalButtonsInactive() {
        for(Node n : levelEditorModule.getCellTypeSelectionGPane().getChildren()){
            Button btn = (Button)n;
            if(!btn.getText().equals(CContent.EMPTY.getDisplayName())&&!btn.getText().equals(CContent.WALL.getDisplayName()))btn.setDisable(true);
        }
        for(Node n : levelEditorModule.getCellItemSelectionGPane().getChildren()){
            Button btn = (Button)n;
            if(!btn.getText().equals(CContent.EMPTY.getDisplayName())&&!btn.getText().equals(CContent.WALL.getDisplayName()))btn.setDisable(true);
        }
    }

    public GridPane getCellItemSelectionPane() {
        return levelEditorModule.getCellItemSelectionGPane();
    }

    private void updateLevelEditorModule(){
            levelEditorModule.getLevelNameTField().setText(model.getCurrentLevel().getName());
            levelEditorModule.getWidthValueLbl().setText(""+model.getCurrentLevel().getOriginalMap().getBoundX());
            levelEditorModule.getHeightValueLbl().setText(""+model.getCurrentLevel().getOriginalMap().getBoundY());
            levelEditorModule.setLOCToStarsValues(model.getCurrentLevel().getLocToStars());
            levelEditorModule.setTurnsToStarsValues(model.getCurrentLevel().getTurnsToStars());
            levelEditorModule.setRequiredLevels(model.getCurrentLevel().getRequiredLevels());
            levelEditorModule.getIsTutorialValueLbl().setText(model.getCurrentLevel().isTutorial()+"");
            levelEditorModule.getIndexValueLbl().setText((model.getCurrentLevel().getIndex()+1)+"");
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()){
            default:

            case "level":
                selectedRow = 0;
                selectedColumn = 0;
                if(evt.getPropertyName().equals("playerBehaviour"))break;
                if(model.getCurrentLevel().hasAi()){
                    aiCodeArea = new CodeArea(model.getCurrentLevel().getAIBehaviour());
                    setAiCodeArea(aiCodeArea);
                    aiCodeArea.draw();
                }
                else {
                    setAiCodeArea(null);
                }
                if(sceneState == SceneState.LEVEL_EDITOR)updateLevelEditorModule();
            case "map":
//                switch (cell.getContent()){
//                    default:
//                        levelEditorModule.deactivateCellDetails();
//                        break;
//                    case EXIT:
//                        levelEditorModule.activateExitOpenCheckbox();
//                        break;
//                    case TRAP:
//                        levelEditorModule.activateTrapChoicebox();
//                        break;
//                    case SPAWN:
//                        break;
//                    case ENEMY_SPAWN:
//                        levelEditorModule.activateCellIDHBox();
//                        break;
//                    case PRESSURE_PLATE:
//                        levelEditorModule.activateCellIDHBox();
//                        break;
//                    case GATE:
//                        levelEditorModule.activateLinkedCellBtns();
//                        break;
//                }
                drawMap(model.getCurrentLevel().getOriginalMap());
                break;
            case "name":
                levelEditorModule.getLevelNameTField().setText(""+ model.getCurrentLevel().getName());
                break;
            case "width":
                levelEditorModule.getWidthValueLbl().setText(model.getCurrentLevel().getOriginalMap().getBoundX()+"");
                drawMap(model.getCurrentLevel().getOriginalMap());
                break;
            case "height":
                levelEditorModule.getHeightValueLbl().setText(model.getCurrentLevel().getOriginalMap().getBoundY()+"");
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
                levelEditorModule.getMaxKnightsValueLbl().setText(""+model.getCurrentLevel().getMaxKnights());
                knightsLeftHBox.getChildren().clear();
                for(int i = 0; i < model.getCurrentLevel().getMaxKnights();i++){
                    knightsLeftHBox.getChildren().add(new Rectangle(cell_size/2,cell_size,Color.LIGHTGREY));
                }
                break;
            case "aiBehaviour":
//                System.out.println("First");
                ComplexStatement aiBehaviour = (ComplexStatement)evt.getNewValue();
                if(aiBehaviour.getStatementListSize()>0)levelEditorModule.getHasAiValueLbl().setText(""+true);
                else levelEditorModule.getHasAiValueLbl().setText(""+false);
                if(model.getCurrentLevel().hasAi() && ((ComplexStatement)evt.getOldValue()).getStatementListSize() == 0){
                    aiCodeArea = new CodeArea(model.getCurrentLevel().getAIBehaviour());
                    setAiCodeArea(aiCodeArea);
                    aiCodeArea.draw();
                }
                else if (!model.getCurrentLevel().hasAi()){
                    setAiCodeArea(null);
                }
                break;
            case "isTutorial":
                levelEditorModule.getIsTutorialValueLbl().setText(""+model.getCurrentLevel().isTutorial());
                break;
            case "index":
                levelEditorModule.getIndexValueLbl().setText(""+(model.getCurrentLevel().getIndex()+1));
            case "requiredLevels":
                List<String> requiredLevelsList = model.getCurrentLevel().getRequiredLevels();
                levelEditorModule.setRequiredLevels(requiredLevelsList);
                break;
        }
    }

    public StartScreen getStartScreen(){
        return startScreen;
    }


    public void setSceneState(SceneState sceneState) {
        this.sceneState = sceneState;
//        stage.getScene().setRoot(new Label());
        switch (sceneState){
            case START_SCREEN:
                stage.setScene(startScene);
                break;
            case LEVEL_EDITOR:
                prepareRootPane();
                if(aiCodeArea != null){
                aiCodeArea.setEditable(true);
                aiCodeArea.deselectAll();}
                codeArea.deselectAll();
                codeArea.select(0, true);
                stage.setScene(editorScene);
                break;
            case LEVEL_SELECT:
                stage.setScene(levelSelectScene);
                break;
            case PLAY:
                prepareRootPane();
                if(aiCodeArea != null){
                aiCodeArea.setEditable(false);
                aiCodeArea.deselectAll();}
                codeArea.deselectAll();
                codeArea.select(0, true);
                levelOverviewPane.updateUnlockedLevels(model, this);
                stage.setScene(playScene);
                break;
            case TUTORIAL:
                //TODO:
                prepareRootPane();
                if(aiCodeArea != null){
                    aiCodeArea.setEditable(false);
                    aiCodeArea.deselectAll();}
                codeArea.deselectAll();
                codeArea.select(0, true);
                levelOverviewPane.updateUnlockedLevels(model, this);
                stage.setScene(tutorialScene);
                break;

        }

    }

    private void prepareRootPane(){//(boolean isEditor) {

        VBox leftVBox = new VBox();
        if(aiCodeArea!=null)leftVBox.getChildren().add(aiCodeArea);
        rootPane = new BorderPane();
        rootPane.getChildren().clear();
        rootPane.setLeft(leftVBox);
        rootPane.setRight(vBox);
        switch (sceneState){
            case LEVEL_EDITOR:
                levelEditorModule = new LevelEditorModule(model.getCurrentLevel());
//            model.getCurrentLevel().addChangeListener(levelEditorModule);
                rootPane.setCenter(new HBox(new VBox(actualMapGPane,knightsLeftHBox),new VBox(levelEditorModule.getRightVBox(),spellBookPane)));
                rootPane.setTop(levelEditorModule.getTopHBox());
                rootPane.setBottom(new HBox(backBtn,levelEditorModule.getBottomHBox()));
                editorScene = new Scene(rootPane);
                break;
            case LEVEL_SELECT:
                throw new IllegalStateException("Missing error message please TODO! see View -> prepareRootPane()");
//                break;
            case PLAY:
                rootPane.setCenter(new HBox(new VBox(actualMapGPane,knightsLeftHBox),spellBookPane));
                rootPane.setBottom(backBtn);
                playScene = new Scene(rootPane);
                break;
            case START_SCREEN:
                throw new IllegalStateException("Missing error message please TODO! see View -> prepareRootPane()");
//                break;
            case TUTORIAL:
                rootPane.setCenter(new HBox(new VBox(actualMapGPane,knightsLeftHBox),spellBookPane));
                rootPane.setBottom(backBtn);
                tutorialScene = new Scene(rootPane);
                break;
        }
//        if(isEditor) {
//            levelEditorModule = new LevelEditorModule(model.getCurrentLevel());
////            model.getCurrentLevel().addChangeListener(levelEditorModule);
//            rootPane.setCenter(new HBox(new VBox(actualMapGPane,knightsLeftHBox),new VBox(levelEditorModule.getRightVBox(),spellBookPane)));
//            rootPane.setTop(levelEditorModule.getTopHBox());
//            rootPane.setBottom(new HBox(backBtn,levelEditorModule.getBottomHBox()));
//            editorScene = new Scene(rootPane);
//        }else{
//            rootPane.setCenter(new HBox(new VBox(actualMapGPane,knightsLeftHBox),spellBookPane));
//            rootPane.setBottom(backBtn);
//            playScene = new Scene(rootPane);
//        }

    }

    public void setAllItemTypeButtonActive() {
        for(Node btn : getCellItemSelectionPane().getChildren()){
            btn.setDisable(false);
        }
    }

    public void setAllItemTypeButtonInActive() {
        for(Node btn : getCellItemSelectionPane().getChildren()){
            btn.setDisable(true);
        }
    }

    public Image getImageFromMap(GameMap originalMap) {
        GridPane gridPane = getGridPaneFromMap(originalMap);
        gridPane.autosize();
        int dimension = gridPane.getHeight() > gridPane.getWidth() ? (int)Math.round(gridPane.getHeight()) : (int)Math.round(gridPane.getWidth());
        BufferedImage bufferedImage = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
        SwingFXUtils.fromFXImage(gridPane.snapshot(new SnapshotParameters(),new WritableImage(dimension, dimension)), bufferedImage);
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
}
/*

    private void drawCell(Cell cell, int column, int row) {
        CContent content = cell.getContent();

        canvas.getGraphicsContext2D().setFill(Color.WHITE);
        canvas.getGraphicsContext2D().fillRect(column*cell_size,row*cell_size,cell_size,cell_size);
        switch(content){
            case EMPTY:
                canvas.getGraphicsContext2D().setFill(Color.LIGHTGRAY);
                break;
            case BOULDER:
                canvas.getGraphicsContext2D().setFill(Color.DARKGRAY);
                break;
            case GATE:
                if(cell.hasFlag(CFlag.TRAVERSABLE))canvas.getGraphicsContext2D().setFill(Color.WHITE);
                else canvas.getGraphicsContext2D().setFill(Color.DARKGRAY);
                break;
            case EXIT:
                if(cell.hasFlag(CFlag.TRAVERSABLE))canvas.getGraphicsContext2D().setFill(Color.CHOCOLATE);
                else canvas.getGraphicsContext2D().setFill(Color.BROWN);
                break;
            case PATH:
                canvas.getGraphicsContext2D().setFill(Color.WHITE);
                break;
            case SPAWN:
                canvas.getGraphicsContext2D().setFill(Color.BLUE);
                break;
            case ENEMY_SPAWN:
                canvas.getGraphicsContext2D().setFill(Color.VIOLET);
                break;
            case PRESSURE_PLATE:
                if(cell.hasFlag(CFlag.TRIGGERED))canvas.getGraphicsContext2D().setFill(Color.LIMEGREEN);
                else canvas.getGraphicsContext2D().setFill(Color.LIGHTGREEN);
                break;
            case KEY:
                canvas.getGraphicsContext2D().setFill(Color.GREEN);
                break;
            case TRAP:
                if(cell.hasFlag(CFlag.PREPARING))canvas.getGraphicsContext2D().setFill(Color.ORANGE);
                else if(cell.hasFlag(CFlag.ARMED))canvas.getGraphicsContext2D().setFill(Color.RED);
                else canvas.getGraphicsContext2D().setFill(Color.YELLOW);
                break;
            case WALL:
            default:
                canvas.getGraphicsContext2D().setFill(Color.BLACK);
                break;
        }
        canvas.getGraphicsContext2D().fillRect(column*cell_size,row*cell_size,cell_size,cell_size);
        if(cell.getEntity() != null){
            switch (cell.getEntity().getEntityType()){
                case KNIGHT:
                    canvas.getGraphicsContext2D().setFill(Color.GRAY);
                    break;
                case SKELETON:
                    canvas.getGraphicsContext2D().setFill(Color.PURPLE);
                    break;
            }
            switch (cell.getEntity().getDirection()){
                case NORTH:
                    canvas.getGraphicsContext2D().fillPolygon(new double[]{column*cell_size,column*cell_size+cell_size/2.0,column*cell_size+cell_size},new double[]{(row+1)*cell_size,row*cell_size,(row+1)*cell_size},3);
                    break;
                case SOUTH:
                    canvas.getGraphicsContext2D().fillPolygon(new double[]{column*cell_size,column*cell_size+cell_size/2.0,column*cell_size+cell_size},new double[]{(row)*cell_size,(row+1)*cell_size,(row)*cell_size},3);
                    break;
                case EAST:
                    canvas.getGraphicsContext2D().fillPolygon(new double[]{column*cell_size,(column+1)*cell_size,column*cell_size},new double[]{(row)*cell_size,(row+1/2.0)*cell_size,(row+1)*cell_size},3);
                    break;
                case WEST:
                    canvas.getGraphicsContext2D().fillPolygon(new double[]{(column+1)*cell_size,(column)*cell_size,(column+1)*cell_size},new double[]{(row)*cell_size,(row+1/2.0)*cell_size,(row+1)*cell_size},3);
                    break;
                default:
                    canvas.getGraphicsContext2D().fillRect(column*cell_size,row*cell_size,cell_size,cell_size);
                    break;
            }
        }
    }

 */