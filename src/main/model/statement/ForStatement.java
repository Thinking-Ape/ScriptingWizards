package main.model.statement;

import main.model.statement.Condition.Condition;
import main.utility.SimpleSet;
import main.utility.Variable;

import java.util.Iterator;


public class ForStatement extends ComplexStatement {
    private Assignment declaration;
    private Assignment assignment;

    @Override
    public StatementType getStatementType() {
        return statementType;
    }

    public  ForStatement (Assignment declaration, Condition condition, Assignment assignment) {
        super();
        this.assignment = assignment;
        this.assignment.setParentStatement(this);
        this.declaration = declaration;
        this.declaration.setParentStatement(this);
        this.condition = condition;
        statementType = StatementType.FOR;
        counter = -2;
    }

    @Override
    public String getText() {
        String output ="";
        output += "for(";
        output += declaration.getText();
        output += condition.getText()+";";
        output += assignment.getText().replaceAll(";", "");
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
