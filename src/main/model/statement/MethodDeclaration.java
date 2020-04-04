package main.model.statement;

import main.model.enums.MethodType;
import main.model.enums.VariableType;
import main.model.statement.Condition.Condition;
import main.model.statement.Expression.ExpressionLeaf;
import main.model.statement.Expression.ExpressionTree;
import main.model.statement.Expression.ExpressionType;
import main.utility.Util;
import main.utility.Variable;

import java.util.List;

public class MethodDeclaration extends ComplexStatement {
    private final String methodName;
    private final List<Variable> variableList;
    private final VariableType returnType;

    public MethodDeclaration(VariableType returnType, String methodName, List<Variable> variableList){
        this.returnType = returnType;
        this.methodName = methodName;
        this.variableList = variableList;
//        localVariableSet.addAll(variableList);
//        this.statementType = StatementType.METHOD_DECLARATION;
        //no condition!
        this.condition = null;
    }

    @Override
    public Condition getCondition() {
        return null;
    }

    @Override
    public String getText(){
        StringBuilder variableString = new StringBuilder();
        int i = 0;
        for(Variable v : variableList){
            variableString.append(v.getVariableType().getName()).append(" ").append(v.getName());
            if(i < variableList.size()-1) variableString.append(", ");
            i++;
        }
        return returnType.getName()+" "+methodName+"("+variableString.toString()+") {";
    }

    public String getMethodName() {
        return methodName;
    }
    public String[] getParameters() {
        String[] output = new String[variableList.size()];
        StringBuilder variableString = new StringBuilder();
        int i = 0;
        for(Variable v : variableList){
            variableString.append(v.getVariableType()).append(" ").append(v.getName());
            output[i] = variableString.toString();
            i++;
        }
        return output;
    }

    public List<Variable> getVariableList() {
        return variableList;
    }
}
