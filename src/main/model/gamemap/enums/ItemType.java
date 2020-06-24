package main.model.gamemap.enums;

import main.utility.Util;

public enum ItemType {

    BOULDER,
    KEY,
    SWORD,
    SHOVEL,
    NONE;

    public static ItemType getValueFromName(String s) {
        for(ItemType item : values()){
            if(item.name().toUpperCase().equals(s))return item;
        }
        return NONE;
    }

    public String getDisplayName() {
        return Util.getDisplayableString(name());
    }
}
