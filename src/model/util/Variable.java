package model.util;

import model.statement.Expression.ExpressionTree;

public class Variable {
    private String name;
    private ExpressionTree value;
    private VariableType variableType;

    public Variable( VariableType variableType,String variableName, ExpressionTree value) {
        this.name = variableName.trim();
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
    public boolean equals(Variable variable){
        return name.equals(variable.name) && value.equals(variable.value);
    }

    public void update(ExpressionTree value) {
        //TODO: check whether value has the correct type!
        this.value = value;
    }
}
