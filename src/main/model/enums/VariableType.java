package main.model.enums;

import main.utility.GameConstants;
import main.utility.Util;

public enum VariableType {
    INT("int","^-?\\d+$"),
    VOID("void",""),
    KNIGHT("Knight","^new +Knight\\( *+(.*?) *+\\)$"),       // oder auch alles zu Entity zusammenfassen?? und dann aber EntityType abprüfen?
    SKELETON("Skeleton","^new +Skeleton\\( *+(.*|(.+?),-?\\d+) *+\\)$"),
    DIRECTION ("Direction","^(NORTH|SOUTH|EAST|WEST)$"),
    TURN_DIRECTION ("TurnDirection","^(LEFT|RIGHT|AROUND)$"),
    CELL_CONTENT("CellContent", "^"+Util.getRegEx(CellContent.values())+"$"),
    ITEM_TYPE("ItemType", "^"+Util.getRegEx(ItemType.values())+"$"),
    ENTITY_TYPE("EntityType", "^"+Util.getRegEx(EntityType.values())+"$"),
    ARMY("Army","^new Army\\( *+(.+|(.+?,)+.+) *+\\)$"),
    BOOLEAN("boolean","^(true|false)$"),
    //THIS MUST BE THE LAST ENTRY BECAUSE OF A CIRCULAR REFERENCE TO METHODTYPE!!!!
//    COMMAND("Command",MethodType.getAllActionRegex()),

    ;

    final private String name;

    public static VariableType getVariableTypeFromValue(String text) {
        for(VariableType vt : values()){
            if(text.matches(vt.allowedValues))return vt;
        }
        return VOID;
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
        if (variableTypeString.equals(""))return VOID;
        throw new IllegalArgumentException("VariableType "+ variableTypeString +" doesnt exist!");
    }
    public String getName(){
        return name;
    }
}
