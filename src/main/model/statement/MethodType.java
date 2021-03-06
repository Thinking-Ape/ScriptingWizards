package main.model.statement;

import main.utility.VariableType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.utility.VariableType.*;


public enum MethodType {
    MOVE("move\\(\\)", VOID,false,false, "Moves this Knight one cell in the target direction, if the target cell is free."),
    BACK_OFF("backOff\\(\\)", VOID, false,false,"Moves this Knight one cell backwards, if the target cell is free."),
    TURN("turn\\(.+\\)", VOID, true,true,"Turns the Knight in a given direction. Valid parameters are: LEFT, RIGHT, AROUND"),
    USE_ITEM("useItem\\(\\)", VOID, false,true,"Uses any item this Knight previously collected!"),
    COLLECT("collect\\(\\)", VOID,false,false,"Collect any item that is in front of this Knight. If this Knight already has an Item equipped, swap them!"),
    CAN_MOVE("canMove\\(\\)",BOOLEAN,false,false,"Returns a boolean value corresponding to whether this Knight can move in its current Direction"),
    IS_ALIVE("isAlive\\(\\)",BOOLEAN,false,false,"Returns a boolean value corresponding to whether this Knight is alive"),
    IS_POSSESSED("isPossessed\\(\\)",BOOLEAN,false,false,"Returns a boolean value corresponding to whether this Knight is possessed"),
    IS_SPECIALIZED("isSpecialized\\(\\)",BOOLEAN,false,false,"Returns a boolean value corresponding to whether this Knight is a Guardian"),
    IS_DEAD("isDead\\(\\)",BOOLEAN,false,false,"Returns a boolean value corresponding to whether this Knight is dead"),
    HAS_ITEM("hasItem\\(.*\\)",BOOLEAN,false,true,"Optionally takes an ItemType Parameter. Returns true if this Knight is holding an Item of the specified type or if left blank any Item at all."),
    TARGETS_CELL("targetsCell\\(.+\\)",BOOLEAN,true,true,"Requires a CellContent Parameter. Returns true, if this Knight's target cell is of a type equal to the parameter."), //TODO: DISALLOW ITEMS -> PATH
    TARGETS_ITEM("targetsItem\\(.*\\)",BOOLEAN, false,true,"Optionally takes an ItemType Parameter. Returns true if this Knight's target cell contains an Item of a type equal to the parameter. If left blank any ItemType will return true."),
    TARGETS_ENTITY("targetsEntity\\(.*\\)",BOOLEAN, false,true,"Optionally takes an EntityType Parameter. Returns true if this Knight's target cell contains an Entity of a type equal to the parameter. If left blank any EntityType will return true."),
    WAIT("wait\\(\\)", VOID,false,false,"This Knight skips a turn."),
    TARGETS_DANGER("targetsDanger\\(\\)",BOOLEAN,false,false,"Returns a boolean value corresponding to whether this Knight's target cell will kill it upon moving forward."),
    DROP_ITEM("dropItem\\(\\)", VOID,false,false,"Drops any item currently held by this Knight."),
    ATTACK("attack\\(\\)", VOID,false,false,"Only for Skeletons!"),
//    POSSESS("possess\\(\\)", VOID,false,false,"Only for Ghosts!"),
    DISPOSSESS("dispossess\\((.+)\\)", VOID,false,true,"Only for Ghosts!"),
    IS_LOOKING("isLooking\\((.+)\\)", BOOLEAN,true,true,"Returns a boolean value corresponding to whether this Entity is looking in the given Direction!"),
    TARGET_IS_LOOKING("targetIsLooking\\((.+)\\)", BOOLEAN,true,true,"Returns a boolean value corresponding to whether the targeted Entity is looking in the given Direction!");
//    GET_TARGET_ENTITY("getTarget\\(\\)", VariableType.ENTITY,false,false,"Returns the Entity in front for further Method calls!");

    private final String regex;
    private final String tooltip;
    private final VariableType outputType;
    private final boolean needsParameters;
    private final boolean hasParameters;

    MethodType(String regex,VariableType outputType, boolean needsParameters,boolean hasParameters, String tooltip) {
        this.regex = regex;
        this.tooltip = tooltip;
        this.outputType = outputType;
        this.needsParameters = needsParameters;
        this.hasParameters = hasParameters;
    }

    public static MethodType getMethodTypeFromCall(String methodName) throws IllegalArgumentException{

//        Matcher matcher2 = Pattern.compile("^"+GET_TARGET_ENTITY.regex+"\\.(.+)$").matcher(methodName);
//
//        if(matcher2.matches() && matcher2.group(1)!=null && getMethodTypeFromCall(matcher2.group(1))!=null)return getMethodTypeFromCall(matcher2.group(1));
//        else {
//            if(matcher2.matches() &&matcher2.group(1)!=null) System.out.println(matcher2.group(1));
//
//            System.out.println(matcher2.pattern().pattern()+" doesnt match: " + methodName);
//        }
        for(MethodType methodType : values()){
            Pattern mTPattern = Pattern.compile(methodType.getRegex());
            Matcher matcher = mTPattern.matcher(methodName);
            if(matcher.matches())return methodType;
        }
        return null;
    }
    public static MethodType getMethodTypeFromName(String methodName) throws IllegalArgumentException{
//        Matcher matcher = Pattern.compile("^"+GET_TARGET_ENTITY.regex+"\\.(.+)$").matcher(methodName);
//
//        if(matcher.matches() && matcher.group(1)!=null && getMethodTypeFromName(matcher.group(1))!=null)return getMethodTypeFromName(matcher.group(1));
//        else {
////            System.out.println(matcher.pattern().pattern()+" doesnt match: " + methodName);
//        }
        for(MethodType methodType : values()){
            if(methodType.getName().equals(methodName))return methodType;
        }
        return null;
    }

    public static String getAllActionRegex() {
        String output = "(";
        for(MethodType mt : values()){
            if(mt.getOutputType() == VOID)output+=mt.regex+"|";
        }
        return output.substring(0, output.length()-1)+")";
    }

    public String getName(){
        return regex.replaceAll("\\\\\\(.*\\\\\\)","").replaceAll("[\\^$]", "");
    }



    public String getRegex() {
        return regex;
    }

    public VariableType getOutputType() {
        return outputType;
    }

    public String getTooltip(){
        return tooltip;
    }

    public boolean needsParameters() {
        return needsParameters;
    }
    public boolean hasParameters() {
        return hasParameters;
    }
}
