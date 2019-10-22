package main.model.enums;

public enum Direction {
    NORTH,
    SOUTH,
    EAST,
    WEST;

    public static Direction getValueFromString(String text) {
        for(Direction d : values()){
            if(d.name().toUpperCase().equals(text))return d;
        }
        return null;
    }
}
