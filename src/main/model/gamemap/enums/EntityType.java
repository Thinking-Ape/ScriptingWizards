package main.model.gamemap.enums;

import main.utility.Util;

public enum EntityType {
    KNIGHT,
    SKELETON,
    NONE;

    public static EntityType getValueFromName(String s) {
        for(EntityType c : values()){
            if(c.name().toUpperCase().equals(s))return c;
        }
        return NONE;
    }

    public String getDisplayName() {
        return Util.getDisplayableString(name());
    }
}
