package model.garbage_dump.condition;

import model.enums.ConditionType_old;

public class SimpleCondition implements Condition{

    ConditionType_old conditionType= ConditionType_old.EQUALS;
    String argument1 ="";
    String argument2 = "";

    public SimpleCondition(){

    }
    public SimpleCondition(String argument1, ConditionType_old conditionType, String argument2){
        this.conditionType = conditionType;
        if(conditionType != ConditionType_old.TRUE && conditionType != ConditionType_old.FALSE ){
            this.argument1 = argument1;
            this.argument2 = argument2;
        }else{
            this.argument1 = "";
            this.argument2 = "";
        }
    }

    @Override
    public boolean isTrue() {
        //TODO: evaluate conditionType
        return true;
    }

    public String toText(){
        switch (conditionType){
            case EQUALS:
                return argument1 + " == " + argument2;

            case GREATER:
                return argument1 + " > " + argument2;

            case LESSER:
                return argument1 + " < " + argument2;

            case GREATER_EQ:
                return argument1 + " >= " + argument2;

            case LESSER_EQ:
                return argument1 + " <= " + argument2;

            case UNEQUALS:
                return argument1 + " != " + argument2;

            case TRUE:
                return "true";

            case FALSE:
                return "false";

        }
        return "";
    }
}
