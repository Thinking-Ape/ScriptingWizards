package model.util;

public enum VariableType {
    INT("int"),
    BOOLEAN("boolean"),
    KNIGHT("Knight"),       // oder auch alles zu Entity zusammenfassen?? und dann aber EntityType abprüfen?
    SKELETON("Skeleton"), // eventuell zu ENEMY zusammenfassen
//    GHOST("Ghost"), // eventuell zu ENEMY zusammenfassen
    //TODO:
    DIRECTION ("Direction"), // NORTH SOUTH EAST WEST
    TURN_DIRECTION ("TurnDirection"), // LEFT RIGHT AROUND
    CELL_CONTENT("CellContent"),
    ITEM_TYPE("ItemType"),
    ENTITY_TYPE("EntityType"),
    DEFAULT(".*"); // not working yet

    private String name;

    VariableType(String name){
        this.name = name;
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
