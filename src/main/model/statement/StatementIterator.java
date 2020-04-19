package main.model.statement;

public class StatementIterator {

    private StatementIterator childIterator;
    private ComplexStatement complexStatement;
    private int counter;

    StatementIterator(ComplexStatement complexStatement, boolean isChild){
        this.complexStatement = complexStatement;
        counter = isChild ? -1 : 0;
    }

    public Statement next(){
        if(counter == -1){
            counter++;
            return complexStatement;
        }
        if(counter >= complexStatement.getStatementListSize()){
            counter = 0;
            if(complexStatement.getStatementType() == StatementType.COMPLEX||complexStatement.getStatementType() == StatementType.IF||complexStatement.getStatementType() == StatementType.ELSE)return null;
            return complexStatement;
        }
        Statement currentStatement = complexStatement.getSubStatement(counter);
        if(!currentStatement.isComplex()){
            counter++;
            return currentStatement;
        }
        else {
            if(childIterator == null){
                childIterator = new StatementIterator((ComplexStatement)currentStatement, true);
            }
            Statement nextStatement = childIterator.next();
            if(nextStatement == null){
                childIterator = null;
                counter++;
            }
            else return nextStatement;
            return next();
        }
    }

    public void skip(int depth){
        if(depth == -1)throw new IllegalArgumentException("Depth must not be smaller than 0!");
        StatementIterator currentChild = this;
        for(int i = 0; i < depth; i++){
            currentChild = currentChild.childIterator;
        }
        currentChild.counter++;
        currentChild.childIterator = null;
    }

    public int getCurrentIndex() {
        int output = counter;
        StatementIterator currentChildIt = childIterator;
        while(currentChildIt != null){
         output += currentChildIt.counter;
         currentChildIt = currentChildIt.childIterator;
        }
        return output;
    }
}
