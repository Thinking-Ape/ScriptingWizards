package main.model.statement;

import main.model.enums.MethodType;
import main.model.statement.Expression.ExpressionTree;
import main.model.statement.Expression.ExpressionLeaf;
import main.model.statement.Expression.ExpressionType;
import main.utility.Util;

public class MethodCall extends SimpleStatement {
    private MethodType methodType;
    private ExpressionTree expressionTree; //TODO: erscheint eigentlich unnötig! weder das Object noch die parameter sind expressions (letztere eventuell aber doch!)

    public MethodCall(MethodType methodType,String objectName,String parameters){
        super(StatementType.METHOD_CALL);
        this.methodType = methodType;
        this.expressionTree = new ExpressionTree(new ExpressionLeaf(objectName), ExpressionType.SIMPLE,ExpressionTree.expressionTreeFromString(parameters)); //1,1,0 wen bisher vergesssen immer 0
    }

    public ExpressionTree getExpressionTree(){
        return expressionTree;
    }

    public MethodType getMethodType(){return methodType;}

    @Override
    public String getText(){
        String methodString = "";
        String objectName = getExpressionTree().getLeftNode().getText();
        if(!objectName.equals(""))methodString = getExpressionTree().getLeftNode().getText()+"."+methodType.getName()+"("+getExpressionTree().getRightNode().getText()+");";
        else methodString = methodType.getName()+"("+getExpressionTree().getRightNode().getText()+");";

        return methodString;
    }

    public String getObjectName() {
        return expressionTree.getLeftNode().getText();
    }
    public String[] getParameters() {
//        if(expressionTree.getRightNode() == null)return new String[]{""};
        if(!expressionTree.getRightNode().getText().contains(","))return new String[] {expressionTree.getRightNode().getText()};
        return Util.getParametersFromString(expressionTree.getRightNode().getText());
    }
}
