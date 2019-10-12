package model.statement.Condition;

import model.statement.Expression.ExpressionTree;

public class ConditionLeaf implements Condition {

    private ExpressionTree leftTree;
    private ExpressionTree rightTree;
    private BooleanType simpleConditionType;
    private ConditionType conditionType;

    public ConditionLeaf(ExpressionTree leftTree, BooleanType simpleConditionType, ExpressionTree rightTree) {
        conditionType = ConditionType.SINGLE;
//        if(rightTree != null&&rightTree.getText().charAt(0)=='(') System.out.println(rightTree.getText());
        this.leftTree = leftTree;
        this.rightTree = rightTree;
        this.simpleConditionType = simpleConditionType;
    }

    public ExpressionTree getLeftTree(){
        return leftTree;
    }
    public ExpressionTree getRightTree(){
        return rightTree;
    }
    @Override
    public String getText(){
        String output = "" + leftTree.getText();
        switch (simpleConditionType){
            case EQ:
                output += " == ";
                break;
            case GR:
                output += " > ";
                break;
            case LE:
                output += " < ";
                break;
            case SIMPLE:
                return output;
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
        return output + rightTree.getText();

    }

    @Override
    public boolean isSimple() {
        return true;
    }

    public BooleanType getSimpleConditionType(){
        return simpleConditionType;
    }

    @Override
    public ConditionType getConditionType() {
        return conditionType;
    }
//    public ConditionTree getLeftNode() throws IllegalAccessException {
//        throw new IllegalAccessException("You cannot use this Method on a ConditionLeaf");
//    }
//
//    public ConditionTree getRightNode() throws IllegalAccessException {
//
//        throw new IllegalAccessException("You cannot use this Method on a ConditionLeaf");
//    }
}
