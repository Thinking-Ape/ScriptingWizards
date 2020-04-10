package main.model.statement;

import main.model.statement.Condition.Condition;

public class WhileStatement extends ComplexStatement {

    public WhileStatement(Condition condition){
        super();
        this.condition = condition;
        this.statementType = StatementType.WHILE;
    }

    @Override
    public String getText() {
        return "while("+condition.getText()+") {";
    }

}
