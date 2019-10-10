package model.enums;

import utility.Util;

public enum CFlag {
    OPEN(false),
    TRIGGERED(false),
    ARMED(false),
    PREPARING(false),
    TURNED(false), //only used for GATE and merely fulfills a visual purpose
    DEACTIVATED(false),
    INVERTED(false),
    KNIGHT_DEATH(true),
    SKELETON_DEATH(true),
    ACTION(true); //only used for SPAWN when there are no more knights to spawn!
    //HURTING("Hurting"),
    //COLLECTIBLE("Collectible");

    boolean isTemporary;

    CFlag(boolean isTemporary) {
        this.isTemporary = isTemporary;
    }

    public static CFlag getValueFrom(String text) {
        for(CFlag flag : values()){
            if(flag.name().toUpperCase().equals(text)){return flag;}
        }
        return null;
    }

    public String getDisplayName() {
        return Util.getDisplayableString(name());
    }

    public boolean isTemporary() {
        return isTemporary;
    }
}
