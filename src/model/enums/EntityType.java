package model.enums;

import utility.Util;

public enum EntityType {
    KNIGHT,
    SKELETON;

    public static EntityType getValueFromName(String s) {
        for(EntityType c : values()){
            if(c.name().toUpperCase().equals(s))return c;
        }
        return null;
    }

    public String getDisplayName() {
        return Util.getDisplayableString(name());
    }
}
