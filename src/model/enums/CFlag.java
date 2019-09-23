package model.enums;

import model.util.GameConstants;

public enum CFlag {
    OPEN,
    TRIGGERED,
    ARMED,
    UNARMED,
    PREPARING;
    //HURTING("Hurting"),
    //COLLECTIBLE("Collectible");

    public static CFlag getValueFrom(String text) {
        for(CFlag flag : values()){
            if(flag.name().toUpperCase().equals(text)){return flag;}
        }
        return null;
    }

    public String getDisplayName() {
        return GameConstants.getDisplayableString(name());
    }
}
