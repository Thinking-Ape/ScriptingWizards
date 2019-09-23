package view;


//import javafx.scene.control.TextArea;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;
import model.util.GameConstants;
import model.statement.ComplexStatement;
import model.statement.Statement;

import java.util.*;

public class CodeArea extends HBox {

    private VBox rectVBox = new VBox();
    private VBox rectVBox2 = new VBox();
    private VBox codeVBox = new VBox();
    private VBox codeVBox2 = new VBox();
    private List<CodeField> codeFieldList = new ArrayList<>();
    private List<StackPane> rectStackList = new ArrayList<>();
    private boolean isEditable;
    private CodeField selectedCodeField = null;
    private StackPane firstStackPane = new StackPane();
    private StackPane secondStackPane = new StackPane();

    public CodeArea (){
        isEditable = true;
        rectVBox.setAlignment(Pos.TOP_LEFT);
        codeVBox.setAlignment(Pos.TOP_LEFT);
        rectVBox2.setAlignment(Pos.TOP_LEFT);
        codeVBox2.setAlignment(Pos.TOP_LEFT);
//        rectStackList.add(new Rectangle(GameConstants.TEXTFIELD_WIDTH,GameConstants.TEXTFIELD_HEIGHT, GameConstants.getColorFromDepth(0)));
//        codeFieldList.add(new CodeField("",1,isEditable)); //TODO: depth = 1 oder 0?
//        draw();
    }

    public CodeArea (ComplexStatement behaviour,boolean isEditable) {
        this.isEditable = isEditable;
        rectVBox.setAlignment(Pos.TOP_LEFT);
        codeVBox.setAlignment(Pos.TOP_LEFT);
        rectVBox2.setAlignment(Pos.TOP_LEFT);
        codeVBox2.setAlignment(Pos.TOP_LEFT);
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
//            CodeField codeField = codeFieldList.get(i);
//            if (prevDepth < codeField.getDepth()){
//                prevDepth = codeField.getDepth();
//                depthIndexMap.put(prevDepth,i);
//            }
//            if (prevDepth > codeField.getDepth()){
//                int index = depthIndexMap.get(prevDepth);
//                prevDepth = codeField.getDepth();
//                double xTranslate = GameConstants.CODE_OFFSET*(prevDepth);
//                Rectangle rectangle = new Rectangle(GameConstants.TEXTFIELD_WIDTH-xTranslate,GameConstants.TEXTFIELD_HEIGHT*(i-index), GameConstants.getColorFromDepth(prevDepth+1));
//                rectangle.getTransforms().add(new Translate(xTranslate,GameConstants.TEXTFIELD_HEIGHT*index,0));
//                tempList.add(rectangle);
//            }
            //TEST
            int depth = codeFieldList.get(i).getDepth();
            StackPane rectStackPane = new StackPane();
            rectStackPane.setAlignment(Pos.CENTER_LEFT);
            for(int j = 1; j <= depth; j++){
                double xTranslate = GameConstants.CODE_OFFSET*(j-1);
//                double yTranslate = GameConstants.TEXTFIELD_HEIGHT*(i%GameConstants.MAX_CODE_LINES);
                Rectangle rectangle = new Rectangle(GameConstants.TEXTFIELD_WIDTH-xTranslate,GameConstants.TEXTFIELD_HEIGHT, GameConstants.getColorFromDepth(j));
                rectangle.getTransforms().add(new Translate(xTranslate,0,0));
                rectStackPane.getChildren().add(rectangle);
            }
            output.add(rectStackPane);
            //TEST
        }
//        output.add(new Rectangle(GameConstants.TEXTFIELD_WIDTH,GameConstants.TEXTFIELD_HEIGHT*(codeFieldList.size()), GameConstants.getColorFromDepth(1)));
//        for(int i = tempList.size()-1; i >= 0 ; i--){
//            output.add(tempList.get(i));
//        }
        return output;
    }

    public void addNewCodeFieldAtIndex(int index, CodeField codeField) {
//            int prevDepth = index > 0 ? codeFieldList.get(index).depth : 1;
//            if(codeFieldList.get(index).getText().matches(".*\\{"))prevDepth++;
        codeFieldList.add(index,codeField);
    }
    public void draw(){
        rectStackList = getRectanglesFromList(codeFieldList);
        codeVBox.getChildren().clear();
        rectVBox.getChildren().clear();
        codeVBox2.getChildren().clear();
        rectVBox2.getChildren().clear();
        for(int i = 0; i < codeFieldList.size(); i++){
            if(i < GameConstants.MAX_CODE_LINES){
                codeVBox.getChildren().add(codeFieldList.get(i));
                rectVBox.getChildren().add(rectStackList.get(i));
            }
            else {
                codeVBox2.getChildren().add(codeFieldList.get(i));
                rectVBox2.getChildren().add(rectStackList.get(i));
            }
        }
        firstStackPane.getChildren().clear();
        firstStackPane.getChildren().addAll(rectVBox,codeVBox);
        secondStackPane.getChildren().clear();
        secondStackPane.getChildren().addAll(rectVBox2,codeVBox2);
        this.getChildren().clear();
        this.getChildren().addAll(firstStackPane,secondStackPane);
    }
//    public void setText(String... strings) {
////        textFieldList.clear();
////        codeNodeList.clear();
//        this.getChildren().clear();
//        for(String s : strings){
////            s.replaceAll("\n","");
//            addCodeField(s);
//        }
//    }
    public List<String> getAllText() {
        List<String> output= new ArrayList<>();
        for(CodeField codeField : codeFieldList){
            output.add(codeField.getText());
        }
        return output;
    }

//    public List<CodeField> getCodeFieldListClone() {
//        return codeFieldList;
//    }

//    public void resetStyle() {
//        for(CodeField codeField : codeFieldList){
//            codeField.resetStyle();
//        }
//    }

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
            codeField.setEditable(isEditable);
        }
    }
}
