package model.enums;

import util.VariableType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static util.VariableType.*;

public enum MethodType {
    MOVE("move\\(\\)",DEFAULT, "Moves this Knight one cell in the target direction, if the target cell is free."),
    TURN("turn\\(.+\\)",DEFAULT, "Turns the Knight in a given direction. Valid parameters are: LEFT, RIGHT, AROUND"),
    USE_ITEM("useItem\\(\\)",DEFAULT, "Uses any item this Knight previously collected!"), // maybe change to use() -> does activate levers when no items equipped?!
    COLLECT("collect\\(\\)",DEFAULT,"Collect any item that is in front of this Knight. If this Knight already has an Item equipped, swap them!"),
    CAN_MOVE("canMove\\(\\)",BOOLEAN,"Returns a boolean value corresponding to whether this Knight can move in its current Direction"),
    HAS_ITEM("hasItem\\(.+\\)",BOOLEAN,"Requires an ItemType Parameter. Returns a boolean value corresponding to whether this Knight has collected an Item of a type equal to the parameter."),
    TARGET_CELL_IS("targetCellIs\\(.+\\)",BOOLEAN,"Requires a CellContent Parameter. Returns a boolean value corresponding to whether the CellContent of this Knight's target cell is of a type equal to the parameter."), //TODO: DISALLOW ITEMS -> PATH
    TARGET_CONTAINS("targetContains\\(.+\\)",BOOLEAN, "Requires an KnightType or ItemType Parameter. Returns a boolean value corresponding to whether this Knight's target cell contains an Item or Knight of a type equal to the parameter."), //TODO: ADD ALL ITEMS + ITEM,ENEMY,ENTITY
    WAIT("wait\\(\\)",DEFAULT,"This Knight skips a turn."),
    TARGET_IS_DANGER("targetIsDanger\\(\\)",BOOLEAN,"Returns a boolean value corresponding to whether this Knight's target cell will kill this Knight when moving forward."),
    ;


    private final String regex;
    private final String tooltip;
    private final VariableType outputType;

    MethodType(String regex/*, int arguments*/,VariableType outputType, String tooltip) {
        this.regex = regex;
//        this.arguments = arguments;
        this.tooltip = tooltip;
        this.outputType = outputType;
    }

    public static MethodType getMethodTypeFromCall(String methodName) throws IllegalArgumentException{

        for(MethodType methodType : values()){
            Pattern mTPattern = Pattern.compile(methodType.getRegex());
            Matcher matcher = mTPattern.matcher(methodName);
            if(matcher.matches())return methodType;
        }
        return null;
        //TODO: is this necessary? ->  throw new IllegalArgumentException("Method: " + methodName + " is unknown or has wrong parameters!");
    }
    public static MethodType getMethodTypeFromName(String methodName) throws IllegalArgumentException{

        for(MethodType methodType : values()){
            if(methodType.getName().equals(methodName))return methodType;
        }
        return null;
        //TODO: is this necessary? ->  throw new IllegalArgumentException("Method: " + methodName + " is unknown or has wrong parameters!");
    }

    public String getName(){
        return regex.replaceAll("\\\\\\(.*\\\\\\)","");
    }



    public String getRegex() {
        return regex;
    }
//    public int getArguments() {
//        return arguments;
//    }

    public VariableType getOutputType() {
        return outputType;
    }

    public String getTooltip(){
        return tooltip;
    }
}
