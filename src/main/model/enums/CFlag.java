package main.model.enums;

import main.utility.Util;

public enum CFlag {
    OPEN(false),
    INVERTED(false),
    TRIGGERED(false),
    ARMED(false),
    PREPARING(false),
    TURNED(false), //only used for GATE and merely fulfills a visual purpose
    DEACTIVATED(false),
    KNIGHT_DEATH(true),
    SKELETON_DEATH(true),
    KEY_DESTROYED(true),
    ITEM_DESTROYED(true),
    DIRT_REMOVED(true),
    ACTION(true), //only used for SPAWN when there are no more knights to spawn!
    //HURTING("Hurting"),
    //COLLECTIBLE("Collectible");
    CHANGE_COLOR(true),
    HELPER_FLAG(true);
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
