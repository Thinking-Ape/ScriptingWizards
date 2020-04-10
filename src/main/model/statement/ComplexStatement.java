package main.model.statement;

import main.model.statement.Condition.Condition;
import main.parser.CodeParser;
import main.view.CodeAreaType;

import java.util.ArrayList;
import java.util.List;

public class ComplexStatement implements Statement {

    List<Statement> statementList = new ArrayList<>();
    StatementType statementType;
    int counter=-1;
    ComplexStatement parentStatement = null;
    protected Condition condition;



    @Override
    public void setParentStatement(ComplexStatement parentStatement) {
        this.parentStatement = parentStatement;
    }

    public ComplexStatement(){
        this.statementType = StatementType.COMPLEX;
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
    public String print() {
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

    public List<String> getCodeLines(){
        List<String> output = new ArrayList<>();
        if(parentStatement != null)output.add(getText());
        for(Statement s : statementList)output.addAll(s.getCodeLines());
        if(isComplex() && parentStatement != null )output.add("}");
        return output;
    }

    public ComplexStatement copy(CodeAreaType codeAreaType) {
        return CodeParser.parseProgramCode(getCodeLines(),codeAreaType);
    }

    @Override
    public String getText() {
        return "";
    }

    public int getStatementListSize(){
        return statementList.size();
    }

    public int getActualSize(){
        int size = 0;
        for(Statement statement : statementList){
            size += statement.getActualSize();
        }
        return parentStatement == null ? size : size +2;
    }

    public int getDepth(){
        return parentStatement == null ? 0 : parentStatement.getDepth() + 1;
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

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ComplexStatement)return this.getCodeLines().equals(((ComplexStatement)obj).getCodeLines());
        return super.equals(obj);
    }

}
