package model.enums;

import utility.Util;

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
        return Util.getDisplayableString(name());
    }
}
