package main.model.statement;

import java.util.ArrayList;
import java.util.List;

public class StatementIterator {

    StatementIterator childIterator;
    ComplexStatement complexStatement;
    int counter;

    public StatementIterator(ComplexStatement complexStatement, boolean isChild){
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
//            else if (childIterator.isAtMax() && (childIterator.complexStatement.statementType == StatementType.IF||childIterator.complexStatement.statementType == StatementType.ELSE)){
//                counter++;
//                childIterator = null;
//                return next();
//            }
            Statement output = childIterator.next();
            if(output == null){
                childIterator = null;
                counter++;
            }
            else return output;
            return next();
        }
    }

    private boolean isAtMax() {
        return counter >= complexStatement.getStatementListSize();
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
