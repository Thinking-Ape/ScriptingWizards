package main.model.statement.Expression;

public class ExpressionLeaf extends Expression {

    private String expression;

    public ExpressionLeaf(String expression){
        super(ExpressionType.SIMPLE);
        this.expression = expression.trim();
    }

    public String getText(){
        return expression;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
}
