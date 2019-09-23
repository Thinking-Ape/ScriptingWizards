package model.statement.Condition;

import model.util.GameConstants;

public enum BooleanType {
    BOOLEAN(GameConstants.ANY_CHAR,GameConstants.ANY_CHAR),
    GR_EQ('>','='),
    LE_EQ('<','='),
    GR('>', GameConstants.ANY_CHAR),
    LE('<',GameConstants.ANY_CHAR),
    NEQ('!','='),
    EQ('=','=');

    BooleanType(char c1, char c2){
        this.c1 = c1;
        this.c2 = c2;
    }
    private final char c1,c2;


    public static BooleanType getConditionTypeFromChars(char c1, char c2){
        for(BooleanType conditionType : values()){
            if(c1 == conditionType.getFirstCharacter()){
                if(conditionType.getSecondCharacter() == GameConstants.ANY_CHAR || c2 == conditionType.getSecondCharacter()) return conditionType;
            }
        }
        return BooleanType.BOOLEAN;
    }

    public char getFirstCharacter() {
        return c1;
    }
    public char getSecondCharacter() {
        return c2;
    }

}
