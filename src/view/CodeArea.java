package view;


//import javafx.scene.control.TextArea;

import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;
import util.GameConstants;
import model.statement.ComplexStatement;
import model.statement.Statement;

import java.util.*;

public class CodeArea extends HBox {

    private VBox rectVBox = new VBox();
//    private VBox rectVBox2 = new VBox();
    private VBox codeVBox = new VBox();
//    private VBox codeVBox2 = new VBox(); is supposed to be scrollable now!
    private List<CodeField> codeFieldList = new ArrayList<>();
    private List<StackPane> rectStackList = new ArrayList<>();
    private boolean isEditable;
    private CodeField selectedCodeField = null;
    private StackPane firstStackPane = new StackPane();
    private ScrollBar scrollBar = new ScrollBar();
    private boolean isScrollable = false;
    private boolean hasListener = false;
//    private StackPane secondStackPane = new StackPane();

    public CodeArea (){
        isEditable = true;
        rectVBox.setAlignment(Pos.TOP_LEFT);
        codeVBox.setAlignment(Pos.TOP_LEFT);
//        rectVBox2.setAlignment(Pos.TOP_LEFT);
//        codeVBox2.setAlignment(Pos.TOP_LEFT);
//        rectStackList.add(new Rectangle(GameConstants.TEXTFIELD_WIDTH,GameConstants.TEXTFIELD_HEIGHT, GameConstants.getColorFromDepth(0)));
//        codeFieldList.add(new CodeField("",1,isEditable)); //TODO: depth = 1 oder 0?
//        draw();
    }

    public CodeArea (ComplexStatement behaviour,boolean isEditable) {
        this.isEditable = isEditable;
        rectVBox.setAlignment(Pos.TOP_LEFT);
        codeVBox.setAlignment(Pos.TOP_LEFT);
//        rectVBox2.setAlignment(Pos.TOP_LEFT);
//        codeVBox2.setAlignment(Pos.TOP_LEFT);
        codeFieldList.addAll(getCodeFieldsFromStatement(behaviour));
//        draw();
    }
    public CodeArea (ComplexStatement behaviour) {
        this(behaviour,true);
    }

    private List<CodeField> getCodeFieldsFromStatement(ComplexStatement complexStatement) {
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
                Rectangle rectangle = new Rectangle(GameConstants.TEXTFIELD_WIDTH-xTranslate,GameConstants.TEXTFIELD_HEIGHT, GameConstants.getColorFromDepth(j));
                rectangle.getTransforms().add(new Translate(xTranslate,0,0));
                rectStackPane.getChildren().add(rectangle);
            }
            output.add(rectStackPane);
        }
        return output;
    }

    public void addNewCodeFieldAtIndex(int index, CodeField codeField) {
        codeFieldList.add(index,codeField);
    }
    public void draw(){
        rectStackList = getRectanglesFromList(codeFieldList);
        codeVBox.getChildren().clear();
        rectVBox.getChildren().clear();
        int bound = (int) Math.round(scrollBar.getValue());
        for(int i = bound; i < codeFieldList.size()+bound; i++){
            if(i < GameConstants.MAX_CODE_LINES+bound){
                codeVBox.getChildren().add(codeFieldList.get(i));
                rectVBox.getChildren().add(rectStackList.get(i));
            }
//            else {
//                codeVBox2.getChildren().add(codeFieldList.get(i));
//                rectVBox2.getChildren().add(rectStackList.get(i));
//            }
        }
        if(codeFieldList.size()>GameConstants.MAX_CODE_LINES){
            makeScrollable();
        }
        else isScrollable = false;
        firstStackPane.getChildren().clear();
        firstStackPane.getChildren().addAll(rectVBox,codeVBox);
//        secondStackPane.getChildren().clear();
//        secondStackPane.getChildren().addAll(rectVBox2,codeVBox2);
//        this.getChildren().clear();
        this.getChildren().add(firstStackPane);//,secondStackPane);
    }

    private void makeScrollable() {
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setMin(0);
        scrollBar.setMax(codeFieldList.size()-GameConstants.MAX_CODE_LINES);
        scrollBar.setValue(0);
        scrollBar.setBlockIncrement(1);
        scrollBar.setVisibleAmount(0.5);
        rectVBox.autosize();
        scrollBar.setPrefHeight(rectVBox.getPrefHeight());
        this.getChildren().add(scrollBar);
        this.isScrollable = true;
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

    public void removeCodeField(CodeField codeField) {
        codeFieldList.remove(codeField);
    }

    public int getSize() {
        return codeFieldList.size();
    }

    public void select(int index, boolean selectEnd) {
//        for(int i = 0; i< codeFieldList.size(); i++){
            CodeField codeField = codeFieldList.get(index);
            if(index >= GameConstants.MAX_CODE_LINES&&isScrollable){
                scrollBar.setValue(index-GameConstants.MAX_CODE_LINES+1);
            }
//            if(index == i){
            if(codeField.isEditable())codeField.setStyle(null);
            else codeField.setStyle("-fx-background-color: rgba(200,200,255,0.5);");
            codeField.requestFocus();
            if(selectEnd)codeField.selectRange(codeField.getText().length(),codeField.getText().length());
            else codeField.selectRange(0,0);
            selectedCodeField = codeField;
//            }else {
//                codeField.resetStyle();
//            }
//        }
    }
    public void deselectAll(){
        for(CodeField codeField : codeFieldList){
            codeField.resetStyle();
        }
    }
    public void select(CodeField codeField, boolean selectEnd) {

        for(int i = 0; i< codeFieldList.size(); i++){
            if(codeField == codeFieldList.get(i))select(i,selectEnd);
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
    public CodeField getSelectedCodeField(){return selectedCodeField;}

    public void highlightError(int currentLine) {
        codeFieldList.get(currentLine).setStyle("-fx-background-color: rgba(255,150,150,0.6);");
    }

    public CodeArea createClone(){
        CodeArea codeAreaClone = new CodeArea();
        codeAreaClone.isEditable = isEditable;
        codeAreaClone.rectStackList = rectStackList;
        int i = 0;
        //            if(i != codeFieldList.size()){
        codeAreaClone.codeFieldList.addAll(codeFieldList);
        return codeAreaClone;
    }
    public void setEditable(boolean isEditable){
        this.isEditable = isEditable;
        for(CodeField codeField : codeFieldList){
            if(!codeField.getText().equals("}"))codeField.setEditable(isEditable);
            else codeField.setEditable(false);
        }
    }

    public ScrollBar getScrollBar(){
        return scrollBar;
    }

    public void scroll(int t1) {
        codeVBox.getChildren().clear();
        rectVBox.getChildren().clear();
        for(int i = t1; i < t1+GameConstants.MAX_CODE_LINES; i++){
            codeVBox.getChildren().add(codeFieldList.get(i));
            rectVBox.getChildren().add(rectStackList.get(i));
        }
    }
    public void addListenerToScrollbar(ChangeListener<Number> changeListener){
        // make sure the Listener is only added once!
        if(!hasListener){
            scrollBar.valueProperty().addListener(changeListener);
            hasListener = true;
        }
    }
}
