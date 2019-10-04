package model.statement.Condition;

import utility.GameConstants;

public enum BooleanType {
    SIMPLE(GameConstants.ANY_CHAR,GameConstants.ANY_CHAR),
    GR_EQ('>','='),
    LE_EQ('<','='),
    GR('>', GameConstants.ANY_CHAR),
    LE('<',GameConstants.ANY_CHAR),
    NEQ('!','='),
    EQ('=','='),
    CAL('.',GameConstants.ANY_CHAR);

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
        return BooleanType.SIMPLE;
    }

    public char getFirstCharacter() {
        return c1;
    }
    public char getSecondCharacter() {
        return c2;
    }

}
