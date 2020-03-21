package main.model.statement;

import main.model.statement.Condition.Condition;
import main.utility.SimpleSet;
import main.utility.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ComplexStatement implements Statement {

    List<Statement> statementList = new ArrayList<>();
    StatementType statementType;
    int counter=-1;
    Set<Variable> localVariableSet = new SimpleSet<>();
    ComplexStatement parentStatement = null;
    protected Condition condition;

    @Override
    public void setParentStatement(ComplexStatement parentStatement) {
        this.parentStatement = parentStatement;
    }

    public void updateVariable(Variable variable) throws IllegalAccessException {
        Variable var = getVariable(variable.getName());
        if(var == null){
            if(parentStatement == null || (var = parentStatement.getVariable(variable.getName())) == null)throw new IllegalAccessException("Variable "+ variable.getName() + " does not exist!");
        }
        var.update(variable.getValue());
    }

    /** Will return a Variable that has the same name as the given parameter. If there are no local variables with that
     *  name, the search will be passed on to the parent statement
     *
     * @return The found Variable or null
     */
    public Variable getVariable(String variableName) {
        for(Variable var : localVariableSet){
            if(var.getName().equals(variableName)){
                return  var;
            }
        }
        return parentStatement == null ? null : parentStatement.getVariable(variableName);
    }

    public ComplexStatement(){
//        this.depth = depth;
        this.statementType = StatementType.COMPLEX;
    }
    public void resetVariables(boolean total) {
        counter = -1;
        localVariableSet = new SimpleSet<>();
        for(Statement statement : statementList){
            if(statement.isComplex()){
                ((ComplexStatement)statement).resetVariables(true);
            }
        }
    }

    public void addLocalVariable(Variable variable) throws IllegalAccessException {
        for(Variable variable1 : localVariableSet){
            if(variable.getName().equals(variable1.getName())){
                throw new IllegalAccessException("Variable " + variable.getName()+" already in scope!");
            }
        }
        if(parentStatement != null && parentStatement.getVariable(variable.getName())!=null){
                throw new IllegalAccessException("Variable " + variable.getName()+" already in scope!");
        }
        localVariableSet.add(variable);
    }

    @Override
    public ComplexStatement getParentStatement() {
        return parentStatement;
    }

    public void addSubStatement(Statement statement){
        statement.setParentStatement(this);
        statementList.add(statement);
    }
    public Statement getSubStatement(int index) {
        return statementList.get(index);
    }
    public StatementType getStatementType(){
        return statementType;
    }

    @Override
    public String print() throws IllegalAccessException {
        String output="";
        output +=getText()+"\n";
        for (int i = 0; i < getStatementListSize(); i++){
            for(int j = 0; j < getDepth()-1;j++){
                output +="  ";
            }
            output+=getSubStatement(i).print()+"\n";
        }
        for(int j = 0; j < getDepth()-2;j++){
            output+="  ";
        }
        if(StatementType.COMPLEX != this.getStatementType())output+=("}");
        return output;
    }


    @Override
    public String getText() {
//        throw new IllegalAccessException("You cannot call this Method for a Complex Statement!");
        return "";
    }

    public int getStatementListSize(){
        return statementList.size();
    }

    //TODO: Statement-Iterator-class?
    /** Gives the next substatement in the List. If the counter reaches the end of the List,
     * the instance of this Object is returned instead.
     * @return this, or a Statement from the @statementList
     */
    public Statement nextStatement(){
        if(counter == -1){
            counter++;
            return this;
        }
        if(counter == statementList.size()){
//            resetVariables(false);
            return null;
        }
        Statement nextStatement = statementList.get(counter).nextStatement();
        if(nextStatement == null){
            counter ++;
            return nextStatement();
        }
        return nextStatement;
    }
    public void skip(){
        counter++;
    }

    public int getActualSize(){
        int size = 0;
        for(Statement statement : statementList){
            size += statement.getActualSize();
        }
        return parentStatement == null ? size : size +2;
    }

    public int getDepth(){
        return parentStatement == null ? 1 : parentStatement.getDepth() + 1;
    }

    public boolean isComplex(){
        return true;
    }
    public Condition getCondition(){return condition;}

    public int findIndexOf(Statement executedStatement, int i) {

        for(Statement statement : statementList){
            if(statement.isComplex()){
                int f = ((ComplexStatement)statement).findIndexOf(executedStatement, 0);
                if(f != -1)return i+1+f;
                else i+=statement.getActualSize()-1;
            }
            else if(statement == executedStatement){
                return i;
            }

            i++;
        }
        return -1;
    }

    public StatementIterator iterator(){
        return new StatementIterator(this);
    }
}
