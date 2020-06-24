package main.model.gamemap.enums;

import main.utility.Util;

public enum CellContent {

    WALL(false),
    EMPTY(false),
    PATH(true),
    EXIT(false),
    TRAP(true),
    SPAWN(true),
    ENEMY_SPAWN(true),
    DIRT(false),
    PRESSURE_PLATE(true),
    GATE(false);

    private boolean isTraversable;

    CellContent(boolean isTraversable){
        this.isTraversable = isTraversable;
    }

    public static CellContent getValueFromName(String s) {
        for(CellContent c : values()){
            if(c.name().toUpperCase().equals(s))return c;
        }
        return null;
    }

    public String getDisplayName(){
        return Util.getDisplayableString(name());
    }
    public boolean isTraversable() {
        return isTraversable;
    }
}
