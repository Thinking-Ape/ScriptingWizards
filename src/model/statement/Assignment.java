package model.statement;

import utility.VariableType;
import model.statement.Expression.ExpressionTree;
import utility.Variable;


public class Assignment extends SimpleStatement {

    private Variable variable;

    public Assignment(String variableName, VariableType variableType, ExpressionTree value,boolean isDeclaration) {
        super(isDeclaration ? StatementType.DECLARATION : StatementType.ASSIGNMENT);

       this.variable = new Variable(variableType,variableName,value);
    }

    public Variable getVariable(){
        return variable;
    }

    @Override
    public String getText(){
        VariableType variableType = variable.getVariableType();
        String vTypeString =  statementType == StatementType.ASSIGNMENT ? "" : variableType.getName()+" ";
        if(variable.getValue().getText().matches(" *"))return vTypeString+ variable.getName()+";";
        return vTypeString+ variable.getName()+" = "+variable.getValue().getText()+";";
    }

}
