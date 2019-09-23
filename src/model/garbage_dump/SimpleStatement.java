package model.garbage_dump.condition;

import model.statement.Statement;
import model.statement.StatementType;
import model.util.ExpressionTree;
import model.util.ExpressionType;

@Deprecated
public class SimpleStatement implements Statement {

    private ExpressionTree expressionTree;
    private StatementType statementType;

    public SimpleStatement(ExpressionTree expressionTree){
        this.expressionTree = expressionTree;
//        this.statementType = expressionTree.getExpressionType() == ExpressionType.CAL ? StatementType.METHOD_CALL : StatementType.ASSIGNMENT;
    }

    @Override
    public void addSubStatement(Statement statement) throws IllegalAccessException {
        throw new IllegalAccessException("You cannot run this Method for an Assignment");
    }

    @Override
    public Statement getSubStatement(int index) throws IllegalAccessException {
        throw new IllegalAccessException("You cannot run this Method for an Assignment");
    }

    @Override
    public StatementType getStatementType() {
        return statementType;
    }

    @Override
    public ExpressionTree walk(int depth) {
        return expressionTree;
    }

    @Override
    public void print() {
        System.out.println(expressionTree.getText());
    }
}
