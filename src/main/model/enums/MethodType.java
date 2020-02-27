package main.model.enums;

import main.utility.GameConstants;
import main.utility.VariableType;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.utility.VariableType.*;


public enum MethodType {
    MOVE("move\\(\\)", ACTION, "Moves this Knight one cell in the target direction, if the target cell is free."),
    TURN("turn\\(.+\\)", ACTION, "Turns the Knight in a given direction. Valid parameters are: LEFT, RIGHT, AROUND"),
    USE_ITEM("useItem\\(\\)", ACTION, "Uses any item this Knight previously collected!"), // maybe change to use() -> does activate levers when no items equipped?!
    COLLECT("collect\\(\\)", ACTION,"Collect any item that is in front of this Knight. If this Knight already has an Item equipped, swap them!"),
    CAN_MOVE("canMove\\(\\)",BOOLEAN,"Returns a boolean value corresponding to whether this Knight can move in its current Direction"),
    HAS_ITEM("hasItem\\(.*\\)",BOOLEAN,"Optionally takes an ItemType Parameter. Returns true if this Knight is holding an Item of the specified type or if left blank any Item at all."),
    TARGETS_CELL("targetsCell\\(.+\\)",BOOLEAN,"Requires a CellContent Parameter. Returns true, if this Knight's target cell is of a type equal to the parameter."), //TODO: DISALLOW ITEMS -> PATH
    TARGETS_ITEM("targetsItem\\(.*\\)",BOOLEAN, "Optionally takes an ItemType Parameter. Returns true if this Knight's target cell contains an Item of a type equal to the parameter. If left blank any ItemType will return true."),
    TARGETS_ENTITY("targetsEntity\\(.*\\)",BOOLEAN, "Optionally takes an EntityType Parameter. Returns true if this Knight's target cell contains an Entity of a type equal to the parameter. If left blank any EntityType will return true."),
    WAIT("wait\\(\\)", ACTION,"This Knight skips a turn."),
    TARGET_IS_DANGER("targetIsDanger\\(\\)",BOOLEAN,"Returns a boolean value corresponding to whether this Knight's target cell will kill it upon moving forward."),
    DROP_ITEM("dropItem\\(\\)", ACTION,"Drops any item currently held by this Knight."),
    ATTACK("attack\\(\\)", ACTION,"Only for Skeletons!"),
    EXECUTE_IF("executeIf\\((.+,.+,.+)\\)", ACTION,"If the conditions are met, execute the first Command, else execute the second!\nCan be applied to Armies in order to have every Knight within it\nexecute a different Command depending on whether they meet the condition individually!"),
    IS_LOOKING("isLooking\\((.+)\\)", BOOLEAN,"Returns a boolean value corresponding to whether this Entity is looking in the given Direction!");

// WARNING: NEVER HAVE A VARIABLE TYPE COMMAND! BECAUSE OF CIRCULAR REFERENCES!

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

    public static String getAllActionRegex() {
        String output = "(";
        for(MethodType mt : values()){
            if(mt.getOutputType() == ACTION && (mt != EXECUTE_IF|| GameConstants.EXECUTE_IF_IS_COMMAND))output+=mt.regex+"|";
        }
        return output.substring(0, output.length()-1)+")";
    }

    public String getName(){
        return regex.replaceAll("\\\\\\(.*\\\\\\)","").replaceAll("[\\^$]", "");
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
