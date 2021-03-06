package main.model.gamemap.enums;

import main.utility.Util;

public enum CellFlag {
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
    ACTION(true),
    CHANGE_COLOR(true),
    HELPER_FLAG(true), //used to mark that a cell has changed
//    POSSESSING(true)
    ;
    boolean isTemporary;

    CellFlag(boolean isTemporary) {
        this.isTemporary = isTemporary;
    }

    public static CellFlag getValueFrom(String text) {
        for(CellFlag flag : values()){
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
