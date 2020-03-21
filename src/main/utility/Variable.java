package main.utility;

import main.model.enums.*;
import main.model.statement.ComplexStatement;
import main.model.statement.Expression.ExpressionTree;

import static main.model.enums.EntityType.NONE;

public class Variable {
    private String name;
    private ExpressionTree value;
    private VariableType variableType;

    public Variable( VariableType variableType,String variableName, ExpressionTree value) {
        this.name = variableName.trim();
        if(Direction.getValueFromString(name)!=null || ItemType.getValueFromName(name) != ItemType.NONE || CellContent.getValueFromName(name) != null || EntityType.getValueFromName(name) != NONE || name.equals("AROUND")|| name.equals("LEFT")|| name.equals("RIGHT"))
            throw new IllegalArgumentException("A variable must not be named: " + name + "!" );
        this.value = value;
        this.variableType = variableType;
    }

    public String getName(){return name;}
    public ExpressionTree getValue() {
        return value;
    }

    public VariableType getVariableType() {
        return variableType;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Variable){
            Variable variable = (Variable)o;
            return name.equals(variable.name) && value.equals(variable.value);
        }
        else return super.equals(o);

    }

    public void update(ExpressionTree value) {
        //TODO: check whether value has the correct type!
        this.value = value;
    }
    public static ExpressionTree evaluateVariable(String varName, ComplexStatement parentStatement) {
        Variable variable = parentStatement.getVariable(varName);
        if(variable == null)return ExpressionTree.expressionTreeFromString(varName);
        if(parentStatement.getVariable(variable.getValue().getText())!=null)return evaluateVariable(variable.getValue().getText(),parentStatement);
        else return variable.getValue();
    }
}
