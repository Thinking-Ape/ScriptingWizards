package main.view;


//import javafx.scene.control.TextArea;

import main.controller.Selection;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;
import main.utility.GameConstants;
import main.model.statement.ComplexStatement;
import main.model.statement.Statement;
import main.utility.Util;

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
    private CodeScrollBar scrollBar;
    private boolean isScrollable = false;
    private boolean hasListener = false;
//    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private boolean isAi = false;
//    private StackPane secondStackPane = new StackPane();

    public CodeArea (boolean isAi){
        this(new ComplexStatement(),true,isAi);
    }

    public CodeArea (ComplexStatement behaviour,boolean isEditable, boolean isAi) {
        this.isEditable = isEditable;
        this.isAi = isAi;
        scrollBar = CodeScrollBar.getInstance(isAi);
        rectVBox.setAlignment(Pos.TOP_LEFT);
        codeVBox.setAlignment(Pos.TOP_LEFT);
        codeFieldList.addAll(getCodeFieldsFromStatement(behaviour));
//        scrollBar.setPrefHeight(rectVBox.getPrefHeight());
    }
    public CodeArea (ComplexStatement behaviour, boolean isAi) {
        this(behaviour,true,isAi);
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

    public void addNewCodeFieldAtIndex(int index, CodeField codeField) {
        codeFieldList.add(index,codeField);
    }
    void draw(){
        this.getChildren().clear();
        rectStackList = getRectanglesFromList(codeFieldList);
        codeVBox.getChildren().clear();
        rectVBox.getChildren().clear();
        int bound = (int) Math.round(scrollBar.getValue());
        for(int i = bound; i < GameConstants.MAX_CODE_LINES+bound; i++){
            if(i >= codeFieldList.size())break;
            codeVBox.getChildren().add(codeFieldList.get(i));
            rectVBox.getChildren().add(rectStackList.get(i));

        }
        if(codeFieldList.size()>GameConstants.MAX_CODE_LINES){
            makeScrollable();
        }
        else isScrollable = false;
        firstStackPane.getChildren().clear();
        firstStackPane.getChildren().addAll(rectVBox,codeVBox);
        this.getChildren().add(firstStackPane);
        this.getChildren().add(scrollBar);
        scrollBar.setOrientation(Orientation.VERTICAL);
        scrollBar.setMin(0);
        scrollBar.setMax(codeFieldList.size()-GameConstants.MAX_CODE_LINES);
        scrollBar.setBlockIncrement(1);
        scrollBar.setVisibleAmount(0.5);
        rectVBox.autosize();
        scrollBar.setPrefHeight(rectVBox.getLayoutBounds().getHeight());
        scrollBar.setDisable(GameConstants.MAX_CODE_LINES >= codeFieldList.size());
        if(codeFieldList.size() == 1)scrollBar.setVisible(false);
        else scrollBar.setVisible(true);
    }

    private void makeScrollable() {
        scrollBar.setDisable(false);
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
        if(scrollBar.getScrollAmount()+GameConstants.MAX_CODE_LINES > codeFieldList.size() && codeFieldList.size() >= GameConstants.MAX_CODE_LINES)
            scrollBar.setScrollAmount(scrollBar.getScrollAmount()-1);
    }

    public int getSize() {
        return codeFieldList.size();
    }

    public void select(int index, Selection selection) {
//        for(int i = 0; i< codeFieldList.size(); i++){
        if(index >= codeFieldList.size()){
            select(codeFieldList.size()-1, selection);
            return;
        }
        else if(index < 0){
            select(0, selection);
            return;
        }
            CodeField codeField = codeFieldList.get(index);
            if(index >= GameConstants.MAX_CODE_LINES+scrollBar.getScrollAmount()&&isScrollable){
                scrollBar.setScrollAmount(index-GameConstants.MAX_CODE_LINES+1);
            }else if (index < scrollBar.getScrollAmount()&&isScrollable){

                scrollBar.setScrollAmount(index);
//            scroll(index);
        }
//            if(index == i){
            if(codeField.isEditable())codeField.setStyle(null);
            else codeField.setStyle("-fx-background-color: rgba(200,200,255,0.5);");
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
    public CodeField getSelectedCodeField(){return selectedCodeField;}

    public void highlightError(int currentLine) {
        codeFieldList.get(currentLine).setStyle("-fx-background-color: rgba(255,150,150,0.6);");
    }

    public CodeArea createClone(){
        CodeArea codeAreaClone = new CodeArea(isAi);
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
//        if(t1+GameConstants.MAX_CODE_LINES > codeFieldList.size())t1 = 0;
        for(int i = t1; i < t1+GameConstants.MAX_CODE_LINES; i++){
            if(i >= codeFieldList.size())break;
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

    public boolean isAi() {
        return isAi;
    }

    public void moveCodeField(int currentIndex, boolean isUp) {
        CodeField codeField = codeFieldList.get(currentIndex);
//        int shift = 1;
        int end = indexOfCodeField(findNextBracket(currentIndex, codeField.getDepth()));
        if(!codeField.getText().matches(".*\\{"))end = currentIndex;

        int depth = codeField.getDepth();
        if(isUp) {
            if(currentIndex == 0)return;
            codeFieldList.remove(codeField);
            codeFieldList.add(currentIndex-1,codeField);
            for(int i = currentIndex+1; i <= end;i++){
//                boolean isClosingBracket = (codeFieldList.get(i).getDepth()==depth && codeFieldList.get(i).getText().equals("}"));
                if(codeFieldList.get(i).getDepth() >= depth){// || isClosingBracket){
                    CodeField tempCodeField = codeFieldList.get(i);
                    codeFieldList.remove(tempCodeField);
                    codeFieldList.add(i-1,tempCodeField);
                }
            }
        }else {
            if(end >= codeFieldList.size()-1)return;
            for(int i = end; i > currentIndex;i--){
//                boolean isClosingBracket = (codeFieldList.get(i).getDepth()==depth && codeFieldList.get(i).getText().equals("}"));
                if(codeFieldList.get(i).getDepth() >= depth){//||isClosingBracket){
                    CodeField tempCodeField = codeFieldList.get(i);
                    codeFieldList.remove(tempCodeField);
                    codeFieldList.add(i+1,tempCodeField);
                }
            }
            codeFieldList.remove(codeField);
            codeFieldList.add(currentIndex+1,codeField);
        }
    }

    public void removeCodeField(int currentIndex) {
        removeCodeField(codeFieldList.get(currentIndex));
    }

//    public void setBehaviuour(ComplexStatement complexStatement) {
//        this.codeFieldList = new ArrayList<>();
//        codeFieldList.addAll(getCodeFieldsFromStatement(complexStatement));
//        rectStackList = getRectanglesFromList(codeFieldList);
//    }
}