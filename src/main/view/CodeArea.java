package main.view;


import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import main.controller.Selection;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;
import main.model.statement.SimpleStatement;
import main.utility.GameConstants;
import main.model.statement.ComplexStatement;
import main.model.statement.Statement;
import main.utility.Util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import static main.utility.GameConstants.SMALL_BUTTON_SIZE;

public class CodeArea extends VBox {

    private VBox rectVBox = new VBox();
    private VBox codeVBox = new VBox();
    private List<CodeField> codeFieldList = new ArrayList<>();
    private List<StackPane> rectStackList = new ArrayList<>();
    private boolean isEditable;
    private CodeField selectedCodeField = null;
    private StackPane firstStackPane = new StackPane();
    private boolean isAi;
    //TODO: have only 2 CodeAreas and dont copy them!
    private static int scrollAmount = 0;
    private static int aiScrollAmount = 0;

    private final int MAX_CODE_LINES;
    private  Button upBtn = new Button();
    private Button downBtn = new Button();
    private ImageView iconIView;

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    public CodeArea (boolean isAi){
        this(new ComplexStatement(),!isAi,isAi);
    }

    public CodeArea (ComplexStatement behaviour,boolean isEditable, boolean isAi) {
        this.isEditable = isEditable;
        this.isAi = isAi;
        this.MAX_CODE_LINES = isAi ? GameConstants.MAX_CODE_LINES+1 : GameConstants.MAX_CODE_LINES;
        rectVBox.setAlignment(Pos.TOP_LEFT);
        codeVBox.setAlignment(Pos.TOP_LEFT);
        if(behaviour.getStatementListSize() == 0)behaviour.addSubStatement(new SimpleStatement());
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

        if(!isAi){
            iconIView = new ImageView(new Image(GameConstants.BLUE_SCRIPT_ICON_PATH));
            hb1.setAlignment(Pos.BOTTOM_RIGHT);
            hb2.setAlignment(Pos.TOP_RIGHT);
        }
        else{
            iconIView = new ImageView(new Image(GameConstants.RED_SCRIPT_ICON_PATH));
            hb1.setAlignment(Pos.BOTTOM_LEFT);
            hb2.setAlignment(Pos.TOP_LEFT);
        }

        iconIView.setFitWidth(SMALL_BUTTON_SIZE);
        iconIView.setFitHeight(SMALL_BUTTON_SIZE);

        hb1.setPrefWidth(GameConstants.TEXTFIELD_WIDTH);
        hb2.setPrefWidth(GameConstants.TEXTFIELD_WIDTH);
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
        Statement statement;
        List<CodeField> output = new ArrayList<>();
        for(int i = 0; i < complexStatement.getStatementListSize(); i++){
//            if(complexStatement.getStatementType() == StatementType.FOR)statement = ((ComplexStatement)complexStatement.getSubStatement(0)).getSubStatement(i);
//            else
            statement = complexStatement.getSubStatement(i);
            output.add(new CodeField(statement.getText(),statement.getDepth()-1,isEditable));
            if(statement.isComplex()){
                output.addAll(getCodeFieldsFromStatement((ComplexStatement)statement));
                output.add(new CodeField("}",statement.getDepth()-1,false));
            }
        }
        return output;
    }

    private List<StackPane> getRectanglesFromList(List<CodeField> codeFieldList){
        List<StackPane> output = new ArrayList<>();
//        List<Rectangle> tempList = new ArrayList<>();
//        Map<Integer,Integer> depthIndexMap = new HashMap<>();
//        int prevDepth = 1;

        for(int i = 0; i < codeFieldList.size(); i++){
            int depth = codeFieldList.get(i).getDepth();
            StackPane rectStackPane = new StackPane();
            rectStackPane.setAlignment(Pos.CENTER_LEFT);
            for(int j = 1; j <= depth; j++){
                double xTranslate = GameConstants.CODE_OFFSET*(j-1);
                Rectangle rectangle = new Rectangle(GameConstants.TEXTFIELD_WIDTH-xTranslate,GameConstants.TEXTFIELD_HEIGHT, Util.getColorFromDepth(j));
                rectangle.getTransforms().add(new Translate(xTranslate,0,0));
                rectStackPane.getChildren().add(rectangle);
            }
            output.add(rectStackPane);
        }
        return output;
    }

    void draw(){
        rectStackList = getRectanglesFromList(codeFieldList);
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
        propertyChangeSupport.firePropertyChange("", null, this);
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


    public List<String> getAllText() {
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

    private void removeCodeField(CodeField codeField) {
        codeFieldList.remove(codeField);
        int scrollAmount = getScrollAmount();
        if(scrollAmount+MAX_CODE_LINES > codeFieldList.size() && codeFieldList.size() >= MAX_CODE_LINES)
            scroll(scrollAmount-1);
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
            findNextBracket(index, codeField.getDepth()).setStyle("-fx-background-color: rgba(150,150,255,0.4);");
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

    public CodeField findNextBracket(int index,int depth) {
        for(int i = index; i<codeFieldList.size();i++){
            if(codeFieldList.get(i).getDepth()==depth&&codeFieldList.get(i).getText().equals("}")){
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

    public void setEditable(boolean isEditable){
        this.isEditable = isEditable;
        for(CodeField codeField : codeFieldList){
            if(!codeField.getText().equals("}"))codeField.setEditable(isEditable);
            else codeField.setEditable(false);
        }
    }

    public void scroll(int t1) {
        if(codeFieldList.size() < MAX_CODE_LINES){
            scrollAmount = 0;
            return;
        }
        codeVBox.getChildren().clear();
        rectVBox.getChildren().clear();
        if(isAi)aiScrollAmount = t1;
        else scrollAmount = t1;
//        if(t1+MAX_CODE_LINES > codeFieldList.size())t1 = 0;
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
        return isAi;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void highlightCodeField(int index){
        if(index == -1)scroll(0);
        int i = 0;
        for (CodeField cf : codeFieldList){
            if(cf.getText().equals("")&&cf.getDepth()>1){
                index++;
            }
            else if(i == index){
                if(!isAi)
                    codeFieldList.get(index).setStyle("-fx-background-color: green");
                else
                    codeFieldList.get(index).setStyle("-fx-background-color: violet");
                if(index >= MAX_CODE_LINES){
                    scroll(index-MAX_CODE_LINES+1);
                }
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
        return isAi ? aiScrollAmount : scrollAmount;
    }

    public void markCodeFields(Set<Integer> indexSet) {
        for(Integer i : indexSet){
            codeFieldList.get(i).setStyle("-fx-background-color: lightblue");
        }
    }
    public void updateCodeFields(ComplexStatement behaviour){
        codeFieldList = getCodeFieldsFromStatement(behaviour);
        draw();
    }

    public void resetStyle(int currentIndex) {
        codeFieldList.get(currentIndex).resetStyle();
    }

    public void setEditable(int currentIndex, boolean b) {
        codeFieldList.get(currentIndex).setEditable(b);
    }

    public void clear() {
        codeFieldList.clear();
        codeFieldList.add(new CodeField("", 1, true));
        draw();
    }
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener){
        propertyChangeSupport = new PropertyChangeSupport(this);
        propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
    }
    public void setIconActive(boolean active){
        if(!active)iconIView.setEffect(new ColorAdjust(0.1,0.1,0.1,-0.5));
        else iconIView.setEffect(null);
    }
}
