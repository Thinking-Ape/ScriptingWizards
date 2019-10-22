package main.utility;

import main.model.enums.CContent;
import main.model.enums.EntityType;
import main.model.enums.ItemType;
import main.model.enums.MethodType;

public enum VariableType {
    INT("int","[-+]?\\d+"),
    ACTION(".*",""),
    KNIGHT("Knight","new Knight\\((EAST|WEST|NORTH|SOUTH)?\\)"),       // oder auch alles zu Entity zusammenfassen?? und dann aber EntityType abprüfen?
    SKELETON("Skeleton","(new Skeleton\\((EAST|WEST|NORTH|SOUTH)?\\)|new Skeleton\\((EAST|WEST|NORTH|SOUTH),-?\\d+\\))"),
    DIRECTION ("Direction","(NORTH|SOUTH|EAST|WEST)"), // NORTH SOUTH EAST WEST
    TURN_DIRECTION ("TurnDirection","(LEFT|RIGHT|AROUND)"), // LEFT RIGHT AROUND
    CELL_CONTENT("CellContent", Util.getRegEx(CContent.values())),
    ITEM_TYPE("ItemType", Util.getRegEx(ItemType.values())),
    ENTITY_TYPE("EntityType", Util.getRegEx(EntityType.values())),
    ARMY("Army","new Army\\(( *"+GameConstants.VARIABLE_NAME_REGEX+" *, *)*"+GameConstants.VARIABLE_NAME_REGEX+" *\\)"),
    BOOLEAN("boolean","(true|false)"),

    //THIS MUST BE THE LAST ENTRY BECAUSE OF A CIRCULAR REFERENCE TO METHODTYPE!!!!
    COMMAND("Command",MethodType.getAllActionRegex()),

    ;

    final private String name;

    public static VariableType getVariableTypeFromValue(String text) {
        for(VariableType vt : values()){
            if(text.matches(vt.allowedValues))return vt;
        }
        return ACTION;
    }

    public String getAllowedRegex() {
        return allowedValues;
    }

    final private String allowedValues;

    VariableType(String name, String allowedValues){
        this.name = name;
        this.allowedValues = allowedValues;
    }


    public static VariableType getVariableTypeFromString(String variableTypeString) {
        for(VariableType variableType : values()){
            if(variableTypeString.equals(variableType.name))return variableType;
        }
        if (variableTypeString.equals(""))return ACTION;
        throw new IllegalArgumentException("VariableType "+ variableTypeString +" doesnt exist!");
    }
    public String getName(){
        return name;
    }
}
