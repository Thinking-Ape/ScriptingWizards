package main.utility;

import main.model.gamemap.enums.CellContent;
import main.model.gamemap.enums.EntityType;
import main.model.gamemap.enums.ItemType;

public enum VariableType {
    INT("int","^-?\\d+$"),
    VOID("void",""),
    KNIGHT("Knight","^new +(Knight|Guardian)\\( *+(.*|(.+?),-?\\d+) *+\\)$"),
    SKELETON("Skeleton","^new +(Skeleton|Ghost)\\( *+(.*|(.+?),-?\\d+) *+\\)$"),
    DIRECTION ("Direction","^(NORTH|SOUTH|EAST|WEST)$"),
    TURN_DIRECTION ("TurnDirection","^((LEFT)|(RIGHT)|(AROUND))$"),
    CELL_CONTENT("CellContent", "^"+Util.getRegEx(CellContent.values())+"$"),
    ITEM_TYPE("ItemType", "^"+Util.getRegEx(ItemType.values())+"$"),
    ENTITY_TYPE("EntityType", "^"+Util.getRegEx(EntityType.values())+"$"),
    ARMY("Army","^new Army\\( *+(.+|(.+?,)+.+) *+\\)$"),
    BOOLEAN("boolean","^(true|false)$"),
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
        if(variableTypeString.equals("Guardian"))return KNIGHT;
        if(variableTypeString.equals("Ghost"))return SKELETON;
        throw new IllegalArgumentException("VariableType "+ variableTypeString +" doesnt exist!");
    }
    public String getName(){
        return name;
    }
}
