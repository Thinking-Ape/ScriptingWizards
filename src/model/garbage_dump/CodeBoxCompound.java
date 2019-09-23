package view;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Translate;
import model.GameConstants;
import model.statement.ComplexStatement;
import model.statement.Statement;
import model.statement.StatementType;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class CodeBoxCompound extends StackPane implements CodeBoxComposite {

    //TODO: Add a Button to add a new TextField!
    private List<CodeBoxComposite> codeBoxCompositeList = new ArrayList<>();
    private TextField textField = new TextField();
    private Rectangle rectangle;
    private VBox vBox = new VBox();
    private CodeBoxCompound parentCodeBox = null;

    public CodeBoxCompound(String... textList){
        int depth = 0;
        for(String s : textList){
            if(s.matches(".*\\{")){
                depth++;
            }
            else if(s.equals("{")){
                depth--;
            }
            //TODO:
        }
        TextField textField = new TextField(/*text*/);
        textField.setPrefHeight(GameConstants.TEXTFIELD_HEIGHT);
        textField.setPrefWidth(GameConstants.TEXTFIELD_WIDTH);
        rectangle = new Rectangle(textField.getWidth(),textField.getHeight(), Color.RED);
        this.getChildren().add(rectangle);
        this.vBox.getChildren().add(textField);
        this.getChildren().add(vBox);
    }
    public CodeBoxCompound(ComplexStatement complexStatement) throws IllegalAccessException {
        this(complexStatement,null);
    }
    public CodeBoxCompound(ComplexStatement complexStatement,CodeBoxCompound parentCodeBox) throws IllegalAccessException {
        this.parentCodeBox = parentCodeBox;
//        vBox.spacingProperty().setValue(1);
//        vBox.setMaxWidth(GameConstants.TEXTFIELD_WIDTH-50*complexStatement.getDepth());
        vBox.setAlignment(Pos.CENTER_LEFT);
        addStatementAsText(complexStatement);
        Statement statement;
        for(int i = 0; i < complexStatement.getStatementListSize(); i++){
            if(complexStatement.getStatementType() == StatementType.FOR)statement = ((ComplexStatement)complexStatement.getSubStatement(0)).getSubStatement(i);
            else statement = complexStatement.getSubStatement(i);
            if(statement.isComplex()){
                CodeBoxCompound codeBoxCompound = new CodeBoxCompound((ComplexStatement)statement,this);
                this.codeBoxCompositeList.add(codeBoxCompound);
                CodeBoxLeaf codeBoxLeaf = new CodeBoxLeaf("}",this,complexStatement.getDepth()-1);
                this.codeBoxCompositeList.add(codeBoxLeaf);
            }
            else {
                CodeBoxLeaf codeBoxLeaf = new CodeBoxLeaf(statement.getText(),this,complexStatement.getDepth()-1);
                this.codeBoxCompositeList.add(codeBoxLeaf);
            }
        }
    }

    private void addStatementAsText(ComplexStatement complexStatement) throws IllegalAccessException {
//        System.out.println(complexStatement.getStatementType()+" "+complexStatement.getDepth());
        if(this.getParentCodeBox()!=null){
            double xTranslate = 10*(complexStatement.getDepth()-1);
            textField = new TextField(complexStatement.getText());
            textField.setMaxHeight(GameConstants.TEXTFIELD_HEIGHT);
//            textField.setMaxWidth(GameConstants.TEXTFIELD_WIDTH-xTranslate);
            textField.setMaxWidth(GameConstants.TEXTFIELD_WIDTH-xTranslate+10);
//            textField.setBackground(null);
//            textField.setBackground(new Background(new BackgroundFill(Color.WHITE.deriveColor(1,1,1,0.3),new CornerRadii(5,false),null)));
//            textField.setLayoutX(10*complexStatement.getDepth());
            textField.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
            textField.getTransforms().add(new Translate(10*(complexStatement.getDepth()-2),0,0));
            textField.setOnMouseClicked(event ->{
                textField.setStyle(null);
            });
            rectangle = new Rectangle(GameConstants.TEXTFIELD_WIDTH-xTranslate,GameConstants.TEXTFIELD_HEIGHT*(complexStatement.getActualSize()-2), getColorFromDepth(complexStatement.getDepth())); //TODO: getActualSize necessary?
            rectangle.getTransforms().add(new Translate(xTranslate,0,0));
            this.setAlignment(Pos.CENTER_LEFT);
//            vBox.getTransforms().add(new Translate(10,0,0));
            this.getChildren().add(rectangle);
            this.getChildren().add(vBox);
            this.getParentCodeBox().vBox.getChildren().add(textField);
            this.getParentCodeBox().vBox.getChildren().add(this);
        } else{
            rectangle = new Rectangle(GameConstants.TEXTFIELD_WIDTH,GameConstants.TEXTFIELD_HEIGHT*(complexStatement.getActualSize()-2), getColorFromDepth(complexStatement.getDepth())); //TODO: getActualSize necessary?
            this.getChildren().add(rectangle);
            this.getChildren().add(vBox);
        }
    }

    private TextField formatTextfield(TextField textField) {
        double translateX = textField.getTranslateX();
        TextField textField1 = new TextField(textField.getText());
        textField1.setMaxHeight(GameConstants.TEXTFIELD_HEIGHT);
        textField1.setMaxWidth(GameConstants.TEXTFIELD_WIDTH);
        textField1.getTransforms().add(new Translate(translateX,0,0));
        return textField1;
    }

    public static Color getColorFromDepth(int depth) {
        switch (depth){
            case 1: return Color.LIGHTGREEN;
            case 2: return Color.LIGHTYELLOW;
            case 3: return Color.PALEGOLDENROD;
            case 4: return Color.GOLDENROD;
        }
        return Color.DARKRED;
    }

//    public void addCodeBoxComposite(CodeBoxComposite codeBoxComposite){
//        this.codeBoxCompositeList.add(codeBoxComposite);
//        this.vBox.getChildren().clear();
//        TextField textField = new TextField(text);
//        textField.setPrefHeight(GameConstants.TEXTFIELD_HEIGHT);
//        textField.setPrefWidth(GameConstants.TEXTFIELD_WIDTH);
//        this.vBox.getChildren().add(textField);
//        for(CodeBoxComposite cBC : codeBoxCompositeList){
//            textField = new TextField(cBC.getText());
//            textField.setPrefHeight(GameConstants.TEXTFIELD_HEIGHT);
//            textField.setPrefWidth(GameConstants.TEXTFIELD_WIDTH);
//            vBox.getChildren().add(textField);
//        }
//        rectangle.setHeight(GameConstants.TEXTFIELD_HEIGHT*codeBoxCompositeList.size());
//    }

    @Override
    public String getText() {
        String output = textField.getText();
//        for(CodeBoxComposite  codeBoxCompound : codeBoxCompositeList){
//            output += "\n   "+codeBoxCompound.getText();
//        }
//        output +="\n}";
        return output;
    }

    public List<String> getAllCode() {
        List<String> output = new ArrayList<>();
        if(!textField.getText().equals(""))output.add(textField.getText());
        for(CodeBoxComposite codeBoxComposite : codeBoxCompositeList){
            if(!codeBoxComposite.getAllCode().get(0).equals(""))
            output.addAll(codeBoxComposite.getAllCode());
        }
//        if(!textField.getText().equals(""))output.add("}");
        return output;
    }

    @Override
    public CodeBoxCompound getParentCodeBox() {
        return parentCodeBox;
    }

    public VBox getVBox() {
        return vBox;
    }

    public void resetStyleOfAllTextFields(){
        resetStyleOfAllTextFields(vBox.getChildren());
    }
    public void resetStyleOfAllTextFields(List<Node> nodes) {
        for(Node node : nodes){
            if(node instanceof VBox){
                resetStyleOfAllTextFields(((VBox)node).getChildren());
            }
            else if(node instanceof TextField){
                node.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
            } else if (node instanceof CodeBoxCompound){
                resetStyleOfAllTextFields(((CodeBoxCompound) node).getVBox().getChildren());
            }
        }
    }
}

