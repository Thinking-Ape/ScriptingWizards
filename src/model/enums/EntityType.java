package model.enums;

import util.GameConstants;

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
        return GameConstants.getDisplayableString(name());
    }
}
