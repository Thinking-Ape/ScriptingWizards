package utility;

import model.enums.CContent;
import model.enums.Direction;
import model.enums.EntityType;
import model.enums.ItemType;
import model.statement.Expression.ExpressionTree;

public class Variable {
    private String name;
    private ExpressionTree value;
    private VariableType variableType;

    public Variable( VariableType variableType,String variableName, ExpressionTree value) {
        this.name = variableName.trim();
        if(Direction.getValueFromString(name)!=null || ItemType.getValueFromName(name) != null || CContent.getValueFromName(name) != null || EntityType.getValueFromName(name) != null || name.equals("AROUND")|| name.equals("LEFT")|| name.equals("RIGHT")|| name.equals("EAST")|| name.equals("NORTH")|| name.equals("SOUTH")|| name.equals("WEST"))
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
    public boolean equals(Variable variable){
        return name.equals(variable.name) && value.equals(variable.value);
    }

    public void update(ExpressionTree value) {
        //TODO: check whether value has the correct type!
        this.value = value;
    }
}
