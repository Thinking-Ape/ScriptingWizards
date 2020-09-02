package main.model.statement;

import main.utility.VariableType;
import main.model.statement.Expression.Expression;
import main.utility.Variable;



public class Assignment extends SimpleStatement {

    private Variable variable;

    public Assignment(String variableName, VariableType variableType, Expression value, boolean isDeclaration) {
        super(isDeclaration ? StatementType.DECLARATION : StatementType.ASSIGNMENT);

        this.variable = new Variable(variableType,variableName,value);
    }
    public Assignment(Variable variable, boolean isDeclaration) {
        super(isDeclaration ? StatementType.DECLARATION : StatementType.ASSIGNMENT);

        this.variable = new Variable(variable);
    }

    public Variable getVariable(){
        return variable;
    }

    @Override
    public String getCode(){
        VariableType variableType = variable.getVariableType();
        String vType = variableType.getName();
        //TODO: less important bug
//        if(variable.isSpecialized() && variableType == VariableType.KNIGHT)vType = "Guardian";
//        if(variable.isSpecialized() && variableType == VariableType.SKELETON)vType = "Ghost";
        String vTypeString =  statementType == StatementType.ASSIGNMENT ? "" : vType+" ";
        if(variable.getValue().getText().matches(" *"))return vTypeString+ variable.getName()+";";
        return vTypeString+ variable.getName()+" = "+variable.getValue().getText()+";";
    }

}
