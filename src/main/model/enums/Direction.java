package main.model.enums;

public enum Direction {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    public static Direction getValueFromString(String text) {
        for(Direction d : values()){
            if(d.name().toUpperCase().equals(text))return d;
        }
        return null;
    }
}
