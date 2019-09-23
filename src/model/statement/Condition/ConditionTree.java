package model.statement.Condition;

public class ConditionTree implements Condition {

//    int depth;
    private ConditionType conditionType;
    private Condition leftNode;
    private Condition rightNode;

    public ConditionTree(Condition leftNode, ConditionType operatorType, Condition rightNode){//}, int depth) {
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.conditionType = operatorType;
//        this.depth = depth;
    }
//    protected ConditionTree(ConditionType conditionType, int depth) throws IllegalAccessException {
//        this(null,conditionType,null,depth);
//    }


//    public ExpressionTree getRightTree() throws IllegalAccessException {
//        throw new IllegalAccessException("This operation shall only be used for ConditionLeaf");
//    }
//    public ExpressionTree getLeftTree() throws IllegalAccessException {
//        throw new IllegalAccessException("This operation shall only be used for ConditionLeaf");
//    }

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
//    public int getDepth(){return depth;}


    @Override
    public String getText() {
        String leftNodeText="";
        if(leftNode != null){
            leftNodeText = leftNode.getText();
            if(leftNode.getConditionType() != ConditionType.SIMPLE) leftNodeText = "(" + leftNodeText + ")";
        }
        String rightNodeText = rightNode.getText();
        if(rightNode.getConditionType() != ConditionType.SIMPLE && rightNode.getConditionType()!= ConditionType.NEGATION) rightNodeText = "(" + rightNodeText + ")";
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
}
