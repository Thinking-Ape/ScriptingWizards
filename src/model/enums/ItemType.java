package model.enums;

import util.GameConstants;

public enum ItemType {

    BOULDER,
//    TREASURE,
    KEY,
    SWORD,
    SHOVEL;

    public static ItemType getValueFromName(String s) {
        for(ItemType item : values()){
            if(item.name().toUpperCase().equals(s))return item;
        }
        return null;
    }

    public String getDisplayName() {
        return GameConstants.getDisplayableString(name());
    }
}
