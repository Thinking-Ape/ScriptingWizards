package main.model.statement.Condition;

import main.model.statement.Expression.Expression;

public class ConditionLeaf implements Condition {

    private Expression leftExpression;
    private Expression rightExpression;
    private BooleanType simpleConditionType;

    public ConditionLeaf(Expression leftTree, BooleanType simpleConditionType, Expression rightTree) {
        this.leftExpression = leftTree;
        this.rightExpression = rightTree;
        this.simpleConditionType = simpleConditionType;
    }

    public Expression getLeftExpression(){
        return leftExpression;
    }
    public Expression getRightExpression(){
        return rightExpression;
    }
    public BooleanType getSimpleConditionType(){
        return simpleConditionType;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public ConditionType getConditionType() {
        return ConditionType.SINGLE;
    }

    @Override
    public String getText(){
        String output = "" + leftExpression.getText();
        switch (simpleConditionType){
            case SIMPLE:
                return output;
            case EQ:
                output += " == ";
                break;
            case GR:
                output += " > ";
                break;
            case LE:
                output += " < ";
                break;
            case GR_EQ:
                output += " >= ";
                break;
            case LE_EQ:
                output += " <= ";
                break;
            case NEQ:
                output += " != ";
                break;
            case CAL:
                output += ".";
                break;
        }
        return output + rightExpression.getText();

    }
}
