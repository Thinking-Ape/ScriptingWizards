package model.enums;

import utility.Util;

public enum CFlag {
    OPEN,
    TRIGGERED,
    ARMED,
    UNARMED,
    PREPARING,
    TURNED, //only used for GATE and merely fulfills a visual purpose
    USED_UP; //only used for SPAWN when there are no more knights to spawn!
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
