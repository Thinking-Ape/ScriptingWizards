package main.model.statement.Condition;

public class ConditionTree implements Condition {

    private ConditionType conditionType;
    private Condition leftNode;
    private Condition rightNode;

    public ConditionTree(Condition leftNode, ConditionType operatorType, Condition rightNode){
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.conditionType = operatorType;
    }

    @Override
    public ConditionType getConditionType() {
        return conditionType;
    }

    public Condition getLeftNode() {
        return leftNode;
    }

    public Condition getRightNode() {
        return rightNode;
    }


    @Override
    public String getText() {
        String leftNodeText="";
        if(leftNode != null){
            leftNodeText = leftNode.getText();
            if(leftNode.getConditionType() != ConditionType.SINGLE) leftNodeText = "(" + leftNodeText + ")";
        }
        String rightNodeText = rightNode.getText();
        if(rightNode.getConditionType() != ConditionType.SINGLE && rightNode.getConditionType()!= ConditionType.NEGATION) rightNodeText = "(" + rightNodeText + ")";
        switch (conditionType){
            case AND:
                return leftNodeText + " && " + rightNodeText;
            case OR:
                return leftNodeText + " || " + rightNodeText;
            case NEGATION:
                return "!" + rightNodeText;
        }
        return "";
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
