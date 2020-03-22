package main.model.enums;

import main.utility.Util;

public enum CellContent {

    WALL(false),//,false,"Wall"),
    EMPTY(false),//,false,"Empty"),
    PATH(true),//,false,"Path"),
    EXIT(false),//,false,"Exit"),
    TRAP(true),//,false,"Trap"),
    SPAWN(true),//,false,"Spawn"),
    ENEMY_SPAWN(true),//,false,"Enemy Spawn"),

//    SHIELD(false,true),
    DIRT(false),//,false,"Dirt"),
    PRESSURE_PLATE(true),//,false,"Pressure Plate"),
    //TODO: add levers?
    GATE(false);//,false,"Gate");

    private boolean isTraversable;
//    private boolean isCollectible;
//    private String name;

    CellContent(boolean isTraversable){//, boolean isCollectible,String name){
//        this.isCollectible = isCollectible;
        this.isTraversable = isTraversable;
//        this.name = name;
    }

    public static CellContent getValueFromName(String s) {
        for(CellContent c : values()){
            if(c.name().toUpperCase().equals(s))return c;
        }
//        System.out.println(s);
        return null;
    }

    public String getDisplayName(){
//        String output = toString().substring(0,1);
//        return output + toString().substring(1).toLowerCase();
        return Util.getDisplayableString(name());
    }
    public boolean isTraversable() {
        return isTraversable;
    }
//    public boolean isCollectible(){
//        return isCollectible;
//    }
}