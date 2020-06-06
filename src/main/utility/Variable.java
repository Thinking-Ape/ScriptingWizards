package main.utility;

import main.model.gamemap.enums.*;
import main.model.statement.Expression.Expression;

import static main.model.gamemap.enums.EntityType.NONE;

public class Variable {
    private String name;
    private Expression value;
    private VariableType variableType;

    public Variable (Variable v){
        this.name = v.name;
        this.variableType = v.variableType;
        this.value = Expression.expressionFromString(v.value.getText());
    }

    public Variable( VariableType variableType,String variableName, Expression value) {
        this.name = variableName.trim();
        if(Direction.getValueFromString(name)!=null || ItemType.getValueFromName(name) != ItemType.NONE || CellContent.getValueFromName(name) != null || EntityType.getValueFromName(name) != NONE || name.equals("AROUND")|| name.equals("LEFT")|| name.equals("RIGHT"))
            throw new IllegalArgumentException("A variable must not be named: " + name + "!" );
        this.value = value;
        this.variableType = variableType;
    }

    public String getName(){return name;}
    public Expression getValue() {
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

    public void update(Expression value) {
        this.value = value;
    }
}
