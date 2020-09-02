package main.view;


import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import main.controller.Selection;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;
import main.model.statement.SimpleStatement;
import main.model.GameConstants;
import main.model.statement.ComplexStatement;
import main.model.statement.Statement;
import main.utility.SimpleEventListener;
import main.utility.SimpleEventSender;
import main.utility.Util;

import java.util.*;

import static main.model.GameConstants.*;

public class CodeArea extends VBox {

    private VBox rectVBox = new VBox();
    private VBox codeVBox = new VBox();
    private List<CodeField> codeFieldList = new ArrayList<>();
    private boolean isEditable;
    private CodeField selectedCodeField = null;
    private StackPane firstStackPane = new StackPane();
    private CodeAreaType codeAreaType;
    private int scrollAmount = 0;

    private  Button upBtn = new Button();
    private Button downBtn = new Button();
    private ImageView iconIView;
    private static Image blueIconImage = new Image(GameConstants.BLUE_SCRIPT_ICON_PATH);
    private static Image redIconImage = new Image(GameConstants.RED_SCRIPT_ICON_PATH);
    private static Image redIconDeactivatedImage = new Image(GameConstants.RED_SCRIPT_ICON_DEACTIVATED_PATH);
    private static Image blueIconDeactivatedImage = new Image(GameConstants.BLUE_SCRIPT_ICON_DEACTIVATED_PATH);

    private static CodeArea playerCodeArea;
    private static CodeArea aiCodeArea;

    private SimpleEventSender eventSender;

    public static CodeArea getInstance(CodeAreaType codeAreaType){
        switch (codeAreaType){
            case PLAYER:
                if(playerCodeArea == null)playerCodeArea = new CodeArea(CodeAreaType.PLAYER);
                return playerCodeArea;
            case AI:
                if(aiCodeArea == null)aiCodeArea = new CodeArea( CodeAreaType.AI);
                return aiCodeArea;
        }
        throw new IllegalStateException("CodeAreaType "+codeAreaType+" has not been implemented yet!");
    }

    private CodeArea ( CodeAreaType codeAreaType){
        this(new ComplexStatement(),!codeAreaType.equals(CodeAreaType.AI),codeAreaType);
    }

    private CodeArea (ComplexStatement behaviour,boolean isEditable,  CodeAreaType codeAreaType) {
        this.isEditable = isEditable;
        this.codeAreaType = codeAreaType;
        rectVBox.setAlignment(Pos.TOP_LEFT);
        codeVBox.setAlignment(Pos.TOP_LEFT);
        codeFieldList.addAll(getCodeFieldsFromStatement(behaviour));

        ImageView upBtnIV = new ImageView(new Image(GameConstants.UP_BTN_IMAGE_PATH));
        ImageView downBtnIV = new ImageView(new Image(GameConstants.DOWN_BTN_IMAGE_PATH));
        upBtnIV.setFitHeight(SMALL_BUTTON_SIZE/2);
        upBtnIV.setFitWidth(SMALL_BUTTON_SIZE/2);
        downBtnIV.setFitHeight(SMALL_BUTTON_SIZE/2);
        downBtnIV.setFitWidth(SMALL_BUTTON_SIZE/2);
        upBtn.setGraphic(upBtnIV);
        upBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
        downBtn.setStyle("-fx-background-color: transparent;" +
                "-fx-base: transparent;");
        downBtn.setGraphic(downBtnIV);
        HBox hb1 = new HBox(upBtn);

        HBox hb2 = new HBox(downBtn);

        if( CodeAreaType.AI != codeAreaType){
            iconIView = new ImageView(blueIconImage);
            hb1.setAlignment(Pos.BOTTOM_RIGHT);
            hb2.setAlignment(Pos.TOP_RIGHT);
        }
        else{
            iconIView = new ImageView(redIconImage);
            hb1.setAlignment(Pos.BOTTOM_LEFT);
            hb2.setAlignment(Pos.TOP_LEFT);
        }

//        iconIView.setEffect(new DropShadow(GameConstants.BIG_FONT_SIZE, Color.WHITE));
        iconIView.setFitWidth(SMALL_BUTTON_SIZE);
        iconIView.setFitHeight(SMALL_BUTTON_SIZE);

        hb1.setPrefWidth(GameConstants.CODEFIELD_WIDTH);
        hb2.setPrefWidth(GameConstants.CODEFIELD_WIDTH);
        hb1.setPickOnBounds(false);
        hb2.setPickOnBounds(false);


        StackPane sp = new StackPane(iconIView,hb1);
        sp.setPickOnBounds(false);
        this.getChildren().addAll(new HBox(),new HBox(),new HBox());
        this.getChildren().set(0, sp);
        this.getChildren().set(2, hb2);

        upBtn.setDisable(true);
        downBtn.setDisable(true);
    }

    private List<CodeField> getCodeFieldsFromStatement(ComplexStatement complexStatement) throws IllegalArgumentException {
        if(complexStatement.getStatementListSize() == 0 && complexStatement.getParentStatement() == null)
            complexStatement.addSubStatement(new SimpleStatement());
        Statement statement;
        List<CodeField> output = new ArrayList<>();
        for(int i = 0; i < complexStatement.getStatementListSize(); i++){
            statement = complexStatement.getSubStatement(i);
            output.add(new CodeField(statement.getCode(),statement.getDepth(),isEditable));
            if(statement.isComplex()){
                output.addAll(getCodeFieldsFromStatement((ComplexStatement)statement));
                output.add(new CodeField("}",statement.getDepth(),false));
            }
        }
        return output;
    }

    private List<StackPane> getRectanglesFromCodeFieldList(List<CodeField> codeFieldList){
        List<StackPane> output = new ArrayList<>();

        for (CodeField codeField : codeFieldList) {
            int depth = codeField.getDepth();
            StackPane rectStackPane = new StackPane();
            rectStackPane.setAlignment(Pos.CENTER_LEFT);
            for (int j = 1; j <= depth; j++) {
                double xTranslate = GameConstants.CODE_OFFSET * (j - 1);
                Rectangle rectangle = new Rectangle(GameConstants.CODEFIELD_WIDTH - xTranslate, GameConstants.CODEFIELD_HEIGHT, Util.getColorFromDepth(j));
                rectangle.getTransforms().add(new Translate(xTranslate, 0, 0));
                rectStackPane.getChildren().add(rectangle);
            }
            output.add(rectStackPane);
        }
        return output;
    }

    private void draw(){
        List<StackPane> rectStackList = getRectanglesFromCodeFieldList(codeFieldList);
        codeVBox.getChildren().clear();
        rectVBox.getChildren().clear();
        int bound = getScrollAmount();
        for(int i = bound; i < MAX_CODE_LINES+bound; i++){
            if(i >= codeFieldList.size())break;
            codeVBox.getChildren().add(codeFieldList.get(i));
            rectVBox.getChildren().add(rectStackList.get(i));
        }
        firstStackPane.getChildren().clear();
        firstStackPane.getChildren().addAll(rectVBox,codeVBox);
        this.getChildren().set(1,firstStackPane);
        rectVBox.autosize();
        updateScrollButtons(bound);
        if(eventSender != null)
            eventSender.notifyListeners(this);
    }

    private void updateScrollButtons(int bound) {
        Effect effect = GameConstants.GLOW_BTN_EFFECT;
        if(bound > 0){
            getUpBtn().setDisable(false);
            getUpBtn().setEffect(effect);
        }
        else getUpBtn().setDisable(true);
        if(bound+MAX_CODE_LINES < codeFieldList.size()){
            getDownBtn().setDisable(false);
            getDownBtn().setEffect(effect);
        }
        else getDownBtn().setDisable(true);
    }


    public List<String> getAllCode() {
        List<String> output= new ArrayList<>();
        for(CodeField codeField : codeFieldList){
            output.add(codeField.getText());
        }
        return output;
    }

    public int indexOfCodeField(CodeField codeField) {
        return codeFieldList.indexOf(codeField);
    }

    public List<CodeField> getCodeFieldListClone() {
        return new ArrayList<>(codeFieldList);
    }

    public void removeCodeField(int index) {
        codeFieldList.remove(index);
        int scrollAmount = getScrollAmount();
        if(scrollAmount+MAX_CODE_LINES > codeFieldList.size() && codeFieldList.size() >= MAX_CODE_LINES)
            scrollTo(scrollAmount-1);
        draw();
        select(index, Selection.END);
    }

    public int getSize() {
        return codeFieldList.size();
    }

    public void select(int index, Selection selection) {
        deselectAll();
        if(index >= codeFieldList.size() && index != 0){
            select(codeFieldList.size()-1, selection);
            return;
        }
        else if(index < 0){
            select(0, selection);
            return;
        }
        CodeField codeField = codeFieldList.get(index);
        if(codeField.getText().matches(GameConstants.COMPLEX_STATEMENT_REGEX) && this.getBracketBalance() == 0){
            CodeField codeField1 = findNextBracket(index, codeField.getDepth());
            if(codeField1!=null)codeField1.setStyle("-fx-background-color: rgba(150,150,255,0.4);");
        }
        if(codeField.getText().matches("}") && this.getBracketBalance() == 0){
            CodeField codeField1 = findPreviousBracket(index, codeField.getDepth());
            if(codeField1!=null)codeField1.setStyle("-fx-background-color: rgba(150,150,255,0.4);");
        }
        if(codeField.isEditable())codeField.setStyle(null);
        else codeField.setStyle("-fx-background-color: rgba(150,150,255,1);");
        codeField.requestFocus();
        switch (selection){
            case NONE:
                break;
            case START: codeField.selectRange(0,0);
                break;
            case END:codeField.selectRange(codeField.getText().length(),codeField.getText().length());
                break;
        }
        selectedCodeField = codeField;
    }


    public void deselectAll(){
        for(CodeField codeField : codeFieldList){
            codeField.resetStyle();
        }
        selectedCodeField = null;
    }
    public void select(CodeField codeField, Selection selection) {

        for(int i = 0; i< codeFieldList.size(); i++){
            if(codeField == codeFieldList.get(i))select(i,selection);
        }
    }

    public int getBracketBalance() {
        int balance = 0;
        for(CodeField codeField : codeFieldList){
            if(codeField.getText().matches(".*\\{.*"))balance++;
            if(codeField.getText().equals("}"))balance--;
        }
        return balance;
    }

    private CodeField findNextBracket(int index,int depth) {
        for(int i = index; i<codeFieldList.size();i++){
            if(codeFieldList.get(i).getDepth()==depth&&codeFieldList.get(i).getText().equals("}")){
                return codeFieldList.get(i);
            }
        }
        return null;
    }

    private CodeField findPreviousBracket(int index, int depth) {
        for(int i = index; i>=0;i--){
            if(codeFieldList.get(i).getDepth()==depth&&codeFieldList.get(i).getText().matches(GameConstants.COMPLEX_STATEMENT_REGEX)){
                return codeFieldList.get(i);
            }
        }
        return null;
    }

    public int findNextBracketIndex(int index,int depth) {
        for(int i = index; i<codeFieldList.size();i++){
            if(codeFieldList.get(i).getDepth()==depth&&codeFieldList.get(i).getText().equals("}")){
                return i;
            }
        }
        return -1;
    }
    public CodeField getSelectedCodeField(){return selectedCodeField;}

    public void highlightError(int currentLine) {
        codeFieldList.get(currentLine).setStyle("-fx-background-color: rgba(255,150,150,0.6);");
    }

    public void setEditable(boolean isEditable, boolean isTotal){
        this.isEditable = isEditable;
        for(CodeField codeField : codeFieldList){
            if(!codeField.getText().equals("}") || isTotal)codeField.setEditable(isEditable);
            else codeField.setEditable(false);
        }
    }

    public void setEditable(boolean isEditable){
        setEditable(isEditable,false);
    }
    public void scrollTo(int t1) {
        if(codeFieldList.size() < MAX_CODE_LINES){
            scrollAmount = 0;
            return;
        }
        codeVBox.getChildren().clear();
        rectVBox.getChildren().clear();
        scrollAmount = t1;
        draw();
        if(selectedCodeField != null){
            if(indexOfCodeField(selectedCodeField)>=MAX_CODE_LINES+t1){
                deselectAll();
                select(MAX_CODE_LINES-1+t1, Selection.END);
            }
            else if(indexOfCodeField(selectedCodeField)<t1){
                deselectAll();
                select(t1, Selection.END);
            }
            else {
                select(selectedCodeField, Selection.END);
            }
        }
        else {
            this.requestFocus();
        }
        updateScrollButtons(t1);
    }

    public boolean isAi() {
        return  CodeAreaType.AI ==  codeAreaType;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void highlightCodeField(int index){
        deselectAll();
        if(index == -1) scrollTo(0);
        int i = 0;
        for (CodeField cf : codeFieldList){
            if(i == index){
            if(index >= MAX_CODE_LINES){
                scrollTo(index-MAX_CODE_LINES+1);
            }
            if( CodeAreaType.AI != codeAreaType)
                codeFieldList.get(index).setStyle("-fx-background-color: green");
            else
                codeFieldList.get(index).setStyle("-fx-background-color: violet");
            }
            else {
                cf.resetStyle();
            }
            i++;
        }
    }

    public Button getUpBtn() {
        return upBtn;
    }

    public Button getDownBtn() {
        return downBtn;
    }

    public int getScrollAmount() {
        return scrollAmount;
    }

    public void updateCodeFields(ComplexStatement behaviour){
        codeFieldList = getCodeFieldsFromStatement(behaviour);
        draw();
    }


    public void setEditable(int currentIndex, boolean b) {
        codeFieldList.get(currentIndex).setEditable(b);
    }

    public void clear() {
        codeFieldList.clear();
        codeFieldList.add(new CodeField("", 1, true));
        draw();
        select(0, Selection.END);
    }
    public void addListener(SimpleEventListener eventListener){
        eventSender = new SimpleEventSender(eventListener);
    }
    public void setIconActive(boolean active){
        if(!active){
            if(isAi())iconIView.setImage(redIconDeactivatedImage);
            else iconIView.setImage(blueIconDeactivatedImage);
            iconIView.setEffect(new DropShadow(GameConstants.BIGGEST_FONT_SIZE, Color.RED));
        }
        else{
            if(isAi())iconIView.setImage(redIconImage);
            else iconIView.setImage(blueIconImage);
            iconIView.setEffect(GameConstants.WHITE_BORDER_EFFECT);
        }
    }

    public ImageView getIcon() {
        return iconIView;
    }

    public CodeAreaType getCodeAreaType() {
        return codeAreaType;
    }

    public void disable(boolean isDisabled){
        this.setDisable(isDisabled);
        if(isDisabled){
            iconIView.setEffect(null);
            if(isAi())iconIView.setImage(redIconDeactivatedImage);
            else iconIView.setImage(blueIconDeactivatedImage);
        }
        else {

            if(isAi())iconIView.setImage(redIconImage);
            else iconIView.setImage(blueIconImage);
        }
    }

    public void addCodeField(int index) {
        CodeField codeField = codeFieldList.get(index > 0 ? index-1: 0);
        int depth = codeField.getDepth();
        if(codeField.getText().matches(COMPLEX_STATEMENT_REGEX)) depth++;
        codeFieldList.add(index,new CodeField("", depth, true) );
        draw();
        select(index, Selection.END);
    }
}
