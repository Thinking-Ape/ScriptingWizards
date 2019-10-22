package main.model.statement;

import main.model.statement.Condition.Condition;

public class ConditionalStatement extends ComplexStatement {

    private ConditionalStatement elseStatement = null;
    private boolean isActive;

    public ConditionalStatement(Condition condition,boolean isElse){
        super();
        this.condition = condition;
        isActive = !isElse;
        this.statementType = isElse ? StatementType.ELSE :StatementType.IF;
    }

    public Condition getCondition(){return condition;}

    public void setElseStatement(ConditionalStatement elseStatement){
        this.elseStatement = elseStatement;
        elseStatement.setParentStatement(getParentStatement());
    }

//    @Override
//    public void print() throws IllegalAccessException {
//        System.out.println(getText());
//        super.print();
//        System.out.println("}");
//    }

    public void setActive(boolean isActive) {
        if(this.statementType == StatementType.ELSE){
            this.isActive = isActive;
        }
        //TODO: handle exception?
        else
        System.out.println("This should not have happened");
    }

//    public boolean elseActivated() {
//        return jumpingToElse;
//    }

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
    public void resetVariables(boolean isTotal) {
        if(StatementType.ELSE == this.statementType)this.setActive(false);
        super.resetVariables(true);
    }

    @Override
    public String getText() {
        String output;
        if(statementType == StatementType.ELSE){
            if(condition == null)output = "else {";
            else output = "else if (" + condition.getText()+") {";
        }
        else output = "if ("+condition.getText()+") {";
        return output;
    }
}
