package utility;

import model.enums.CContent;
import model.enums.EntityType;
import model.enums.ItemType;

public enum VariableType {
    INT("int","-?\\d+"),
    BOOLEAN("boolean","(true|false)"),
    KNIGHT("Knight","new Knight\\((EAST|WEST|NORTH|SOUTH)?\\)"),       // oder auch alles zu Entity zusammenfassen?? und dann aber EntityType abprüfen?
    SKELETON("Skeleton","(new Skeleton\\((EAST|WEST|NORTH|SOUTH)?\\)|new Skeleton\\((EAST|WEST|NORTH|SOUTH),-?\\d+\\))"), // eventuell zu ENEMY zusammenfassen
//    GHOST("Ghost"), // eventuell zu ENEMY zusammenfassen
    //TODO:
    DIRECTION ("Direction","(NORTH|SOUTH|EAST|WEST)"), // NORTH SOUTH EAST WEST
    TURN_DIRECTION ("TurnDirection","(LEFT|RIGHT|AROUND)"), // LEFT RIGHT AROUND
    CELL_CONTENT("CellContent", Util.getRegEx(CContent.values())),
    ITEM_TYPE("ItemType", Util.getRegEx(ItemType.values())),
    ENTITY_TYPE("EntityType", Util.getRegEx(EntityType.values())),
    DEFAULT(".*",""); // not working yet

    final private String name;

    public static VariableType getVariableTypeFromValue(String text) {
        for(VariableType vt : values()){
            if(text.matches(vt.allowedValues))return vt;
        }
        return DEFAULT;
    }

    public String getAllowedValues() {
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
        if (variableTypeString.equals(""))return DEFAULT;
        throw new IllegalArgumentException("VariableType "+ variableTypeString +" doesnt exist!");
    }
    public String getName(){
        return name;
    }
}
