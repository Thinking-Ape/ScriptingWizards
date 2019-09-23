package model;

import model.statement.Evaluator;
import model.statement.ForStatement;
import model.statement.MethodCall;
import model.statement.Statement;
import model.util.VariableDepthMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//TODO: created by parser from text probably in a json fpr unity because levels are saved as json (line per line separated by ;??)
@Deprecated
public class Behaviour {
   private List<Statement> statementList;
   private int counter;


   public Behaviour (){
      statementList = new LinkedList<>();
      counter = 0;
   }

   public void addStatement(Statement statement){
      statementList.add(statement);
   }

   public void print() throws IllegalAccessException {
      int i = 0;
      for (Statement statement : statementList) {
         i++;
         System.out.println("Statement "+i+":");
         statement.print();
      }
   }
   public Statement getStatement(int index){
      return statementList.get(index);
   }
   public int getListSize(){
      return statementList.size();
   }
   public int getActualSize(){
      int actualSize= 0;
      for(int j = 0; j < getListSize(); j++ ){
         actualSize += getStatement(j).getActualSize();
      }
      return actualSize;
   }

   public Statement nextStatement(){
      return statementList.get(counter++);
   }


    public void addToIndex(Statement statement) {
    }
}
