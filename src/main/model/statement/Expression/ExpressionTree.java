package main.model.statement.Expression;


public class ExpressionTree extends Expression{
    private Expression leftNode;
    private Expression rightNode;

    public ExpressionTree(Expression leftNode, ExpressionType expressionType, Expression rightNode){
        super(expressionType);
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }

    public Expression getLeftNode() {
        return leftNode;
    }

    public Expression getRightNode() {
        return rightNode;
    }
    public String getText(){
        String leftNodeText = leftNode.getText();
        String rightNodeText = rightNode.getText();
        if(!leftNode.isLeaf())
            leftNodeText = "("+leftNode.getText()+")";
        // parameters are put in brackets twice else!
        if(!rightNode.isLeaf() && !rightNodeText.matches(".*,.*"))
            rightNodeText = "("+rightNode.getText()+")";
        switch (getExpressionType()){
            case ADD:
                return "" + leftNodeText + " + " + rightNodeText+"";
            case SUB:
                return "" + leftNodeText + " - " + rightNodeText+"";
            case DIV:
                return "" + leftNodeText + " / " + rightNodeText+"";
            case MULT:
                return "" + leftNodeText + " * " + rightNodeText+"";
            case MOD:
                return "" + leftNodeText + " % " + rightNodeText+"";
            case PAR:
                return leftNodeText +"," +rightNodeText;
            case SIMPLE:
                String expression =leftNodeText +"(" +rightNodeText+")";
                return expression;
        }
        return "";
    }


    public boolean equals(ExpressionTree expressionTree){
        return getText().equals(expressionTree.getText());
    }

    public int getDepth() {
        int left = 1 + (leftNode == null ? 0 : leftNode.getDepth());
        int right = 1 + (rightNode == null ? 0 : rightNode.getDepth());
        return left > right ? left : right;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}
