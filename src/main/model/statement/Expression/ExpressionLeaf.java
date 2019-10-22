package main.model.statement.Expression;

public class ExpressionLeaf extends ExpressionTree {

    private String expression;

    public ExpressionLeaf(String expression){//}, int depth){
        super(ExpressionType.SIMPLE);
        this.expression = expression.trim();
    }

    public String getText(){
        return expression;
    }
}
