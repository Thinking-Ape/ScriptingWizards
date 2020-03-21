package main.model.statement;

import main.model.statement.Condition.Condition;

public class WhileStatement extends ComplexStatement {

    public  WhileStatement (Condition condition){
        super();
        this.condition = condition;
        this.statementType = StatementType.WHILE;
    }
    @Override
    /** Gives the next substatement in the List. If the counter reaches the end of the List,
     * the instance of this Object is returned instead. It then continues with the first
     * substatement in the statementlist again.
     * @return this, or a Statement from the @statementList
     */
    public Statement nextStatement() {
        Statement statement = super.nextStatement();
        if(statement == null){
            if(counter == statementList.size()){
                resetVariables(true);
                counter = 0;
                return this;
            }
            else {
                counter ++;
                return nextStatement();
            }
        }
        return statement;
    }

    @Override
    public String getText() {
        return "while("+condition.getText()+") {";
    }
//    @Override
//    public void print() throws IllegalAccessException {
//        System.out.println(getText());
////        for (Statement statement : statementList){
////            for(int j = 0; j < statement.getDepth()-1;j++){
////                System.out.print("  ");
////            }
////            statement.print();
////        }
//        super.print();
//        System.out.println("}");
//    }
}
