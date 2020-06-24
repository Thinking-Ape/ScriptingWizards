package main.model.statement;

import main.model.statement.Expression.Expression;
import main.model.statement.Expression.ExpressionTree;
import main.model.statement.Expression.ExpressionLeaf;
import main.model.statement.Expression.ExpressionType;
import main.utility.Util;

public class MethodCall extends SimpleStatement {
    private MethodType methodType;
    private ExpressionTree expressionTree;

    public MethodCall(MethodType methodType,String objectName,String parameters){
        super(StatementType.METHOD_CALL);
        this.methodType = methodType;
        this.expressionTree = new ExpressionTree(new ExpressionLeaf(objectName), ExpressionType.SIMPLE, Expression.expressionFromString(parameters));
    }

    public ExpressionTree getExpressionTree(){
        return expressionTree;
    }

    public MethodType getMethodType(){return methodType;}

    @Override
    public String getCode(){
        String methodString;
        String objectName = getExpressionTree().getLeftNode().getText();
        if(!objectName.equals(""))methodString = getExpressionTree().getLeftNode().getText()+"."+methodType.getName()+"("+getExpressionTree().getRightNode().getText()+");";
        else methodString = methodType.getName()+"("+getExpressionTree().getRightNode().getText()+");";

        return methodString;
    }

    public String getObjectName() {
        return expressionTree.getLeftNode().getText();
    }
    public String[] getParameters() {
        if(!expressionTree.getRightNode().getText().contains(","))return new String[] {expressionTree.getRightNode().getText()};
        return Util.getParametersFromString(expressionTree.getRightNode().getText());
    }
}
