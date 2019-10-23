package main.model.statement;

import main.model.statement.Condition.Condition;
import main.utility.Variable;

import java.util.HashSet;

public class ForStatement extends ComplexStatement {
    private Assignment declaration;
    private Assignment assignment;

    @Override
    public StatementType getStatementType() {
        return statementType;
    }

    public  ForStatement (Assignment declaration, Condition condition, Assignment assignment) throws IllegalAccessException {
        super();
        this.assignment = assignment;
        this.assignment.setParentStatement(this);
        addLocalVariable(this.assignment.getVariable());
        this.declaration = declaration;
        this.declaration.setParentStatement(this);
        this.condition = condition;
//        this.condition.setParentStatement(this);
//        this.statementList.add(condition);
        statementType = StatementType.FOR;
        counter = -2;
    }


//    @Override
//    public void addSubStatement(Statement statement){
//        condition.addSubStatement(statement);
//    }

    @Override
    public String getText() {
        String output ="";
        output += "for(";
        output += declaration.getText();
        output += condition.getText()+";";
        output += assignment.getText();
        output += ") {";
        return output;
    }

    @Override
    /** Gives the next substatement in the List. If the counter reaches the end of the List,
     * the instance of this Object is returned instead. It then continues with the first
     * substatement in the statementlist again.
     * @return this, or a Statement from the @statementList
     */
    public Statement nextStatement(){
        if(counter == -2){
            counter++;
            return declaration;
        }
        Statement nextStatement = super.nextStatement();
        if(nextStatement == null){
            if(counter == statementList.size()){
                resetVariables(false);
                counter = -1;
                return assignment;
            }
            else {
                counter ++;
                return nextStatement();
            }
        }
        return nextStatement;
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public void resetVariables(boolean isTotal) {
        if(!isTotal) {
            for (Variable variable : localVariableSet) {
                if (variable.getName().equals(declaration.getVariable().getName())) continue;
                localVariableSet.remove(variable);
            }
        }
        else {
            localVariableSet = new HashSet<>();
            counter = -2;
        }
        for(Statement statement : statementList){
            if(statement.isComplex()){
                ((ComplexStatement)statement).resetVariables(true);
            }
        }
    }
//    public void totalReset(){
//        localVariableSet = new HashSet<>();
//        counter = -2;
//    }

    public Assignment getAssignment() {
        return assignment;
    }
    public Assignment getDeclaration() {
        return declaration;
    }
}