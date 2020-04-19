package main.model.statement;

import main.model.statement.Condition.Condition;


public class ForStatement extends ComplexStatement {
    private Assignment declaration;
    private Assignment assignment;

    public  ForStatement (Assignment declaration, Condition condition, Assignment assignment) {
        super();
        this.assignment = assignment;
        this.assignment.setParentStatement(this);
        this.declaration = declaration;
        this.declaration.setParentStatement(this);
        this.condition = condition;
        statementType = StatementType.FOR;
    }

    @Override
    public String getCode() {
        String output ="";
        output += "for(";
        output += declaration.getCode();
        output += condition.getText()+";";
        output += assignment.getCode().replaceAll(";", "");
        output += ") {";
        return output;
    }


    public Assignment getAssignment() {
        return assignment;
    }
    public Assignment getDeclaration() {
        return declaration;
    }

}
