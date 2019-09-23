package model.statement;

import model.statement.Condition.ConditionTree;

import java.util.HashSet;

@Deprecated
public class ForStatementOld extends ComplexStatement {
    private ConditionTree condition; //TODO: REPLACE WITH conditional Statement??
    private Assignment assignment1;
    private Assignment assignment2;
//    private Assignment assignment;
//    private SimpleStatement increment;

//    private int counter;

    @Override
    public StatementType getStatementType() {
        return statementType;
    }

    public ForStatementOld(Assignment assignment1, ConditionTree condition, Assignment assignment2, int depth) throws IllegalAccessException {
        super();
        this.assignment1 = assignment1;
        this.assignment2 = assignment2;
        this.assignment1.setParentStatement(this);
        this.assignment2.setParentStatement(this);
        addLocalVariable(assignment1.getVariable());
        this.condition = condition;
        statementType = StatementType.FOR;
        counter = -2;
    }

//    @Override
//    public void print() throws IllegalAccessException {
//        System.out.print("for(");
//        System.out.print(assignment1.getText());
//        System.out.print(condition.getText()+";");
//        System.out.print(assignment2.getText());
//        System.out.println("){");
//        super.print();
//        System.out.println("}");
//    }
    @Override
    /** Gives the next substatement in the List. If the counter reaches the end of the List,
     * the instance of this Object is returned instead. It then continues with the first
     * substatement in the statementlist again.
     * @return this, or a Statement from the @statementList
     */
    public Statement nextStatement(){
        if(counter == -2){
            counter++;
            return assignment1;
        }
        if(counter == statementList.size()){
//            resetCounter();
            localVariableSet = new HashSet<>();
            counter = -1;
            return assignment2;
        }
        return super.nextStatement();
    }
    @Override
    public boolean isComplex(){
        return true;
    }

    public ConditionTree getCondition() {
        return condition;
    }

//    public void setCondition(ConditionTree condition) {
//        this.condition = condition;
//    }

    public Assignment getAssignment1() {
        return assignment1;
    }
    public Assignment getAssignment2() {
        return assignment2;
    }
}
