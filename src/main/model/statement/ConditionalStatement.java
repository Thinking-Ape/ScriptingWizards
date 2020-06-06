package main.model.statement;

import main.model.GameConstants;
import main.model.statement.Condition.Condition;

public class ConditionalStatement extends ComplexStatement {

    private ConditionalStatement elseStatement = null;
    private boolean isActive;

    public ConditionalStatement(Condition condition, boolean isElse){
        super();
        this.condition = condition;
        isActive = !isElse;
        this.statementType = isElse ? StatementType.ELSE :StatementType.IF;
    }

    public void setElseStatement(ConditionalStatement elseStatement){
        this.elseStatement = elseStatement;
        elseStatement.setParentStatement(getParentStatement());
    }

    public void setActive(boolean isActive) {
        if(this.statementType == StatementType.ELSE){
            this.isActive = isActive;
        }
        else if(GameConstants.DEBUG) System.err.println("Tried to activate a non-else statement!");
    }


    public boolean hasElseStatement() {
        return elseStatement == null;
    }

    public ConditionalStatement getElseStatement() {
        return elseStatement;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activateElse() {
        if(elseStatement != null)
            elseStatement.setActive(true);
    }

    @Override
    public String getCode() {
        String output;
        if(statementType == StatementType.ELSE){
            if(condition == null)output = "else {";
            else output = "else if (" + condition.getText()+") {";
        }
        else output = "if ("+condition.getText()+") {";
        return output;
    }

    public void deactivateElse() {
        if(elseStatement != null)
        {
            elseStatement.setActive(false);
            elseStatement.deactivateElse();
        }
    }
}
