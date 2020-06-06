package main.model.gamemap.enums;

public enum TurnDirection {
    LEFT,
    RIGHT,
    AROUND;

    public static TurnDirection getValueFromString(String text) {
        for(TurnDirection d : values()){
            if(d.name().toUpperCase().equals(text))return d;
        }
        return null;
    }
}
