package main.model.statement;

import java.util.HashMap;
import java.util.Map;

//TODO:
public class StatementIterator {

    ComplexStatement complexStatement;
//    int counter = 0;
    Map<Statement,Integer> statementCounterMap = new HashMap<>();
    Statement currentStatement;

    public StatementIterator (ComplexStatement complexStatement){
        this.complexStatement = complexStatement;
        currentStatement = complexStatement;
    }

    public Statement next() throws IllegalAccessException {
        if(currentStatement.isComplex())
            currentStatement = walk((ComplexStatement)currentStatement);
        else currentStatement = walk(currentStatement.getParentStatement());
        return currentStatement;
    }


    //TODO: not working
    public Statement walk(ComplexStatement complexStatement) throws IllegalAccessException {
       if(statementCounterMap.containsKey(complexStatement)){
           int counter = statementCounterMap.get(complexStatement);

           if(counter < complexStatement.getStatementListSize()){
               if(complexStatement.getStatementType() == StatementType.IF||complexStatement.getStatementType() == StatementType.ELSE){
                   ConditionalStatement conditionalStatement = ((ConditionalStatement)complexStatement);

                   if(!conditionalStatement.isActive() && conditionalStatement.hasElseStatement()){
                       return walk(conditionalStatement.getElseStatement());
                   } else if (!conditionalStatement.isActive()){
                       int parentCounter = statementCounterMap.get(complexStatement.getParentStatement());
                       statementCounterMap.replace(complexStatement.getParentStatement(),parentCounter+1);
                       return walk(complexStatement.getParentStatement());
                   }
               }
               if(!complexStatement.getSubStatement(counter).isComplex()){
                   statementCounterMap.replace(complexStatement,counter+1);
                   return complexStatement.getSubStatement(counter);
               }
               return walk((ComplexStatement)(complexStatement.getSubStatement(counter)));
           }
           else {
               switch (complexStatement.getStatementType()){
                   case FOR:
                   case WHILE:
                       statementCounterMap.replace(complexStatement,0);
                       return complexStatement;
                   case IF:
                   case ELSE:
                   case COMPLEX:
                       if(complexStatement.getParentStatement() != null){
                           int parentCounter = statementCounterMap.get(complexStatement.getParentStatement());
                           statementCounterMap.replace(complexStatement.getParentStatement(),parentCounter+1);
                           return walk(complexStatement.getParentStatement());
                       }
                       else return null;
                   case METHOD_CALL:
                   case ASSIGNMENT:
                   case DECLARATION:
                       throw new IllegalAccessException("These Statementtypes are not allowed here!");
               }
           }
       } else{
           statementCounterMap.put(complexStatement,0);
           return complexStatement;
       }
       throw new IllegalStateException("This point should never be reached!");
    }
    public void skip(ComplexStatement statement){
        int counter = statementCounterMap.get(statement.getParentStatement());
        statementCounterMap.replace(statement.getParentStatement(),counter+1);
    }
}
