package model.statement.Condition;

import utility.GameConstants;

public enum ConditionType {
    AND('&','&'),
    OR('|','|'),
    NEGATION('!',GameConstants.ANY_CHAR),
    SINGLE(GameConstants.ANY_CHAR,GameConstants.ANY_CHAR);

    ConditionType(char c1, char c2){
        this.c1 = c1;
        this.c2 = c2;
    }
    private final char c1,c2;


    public static ConditionType getConditionTypeFromChars(char c1, char c2){
        for(ConditionType conditionType : values()){
            if(c1 == conditionType.getFirstCharacter()){
                if(conditionType.getSecondCharacter() == GameConstants.ANY_CHAR || c2 == conditionType.getSecondCharacter()) return conditionType;
            }
        }
        return ConditionType.SINGLE;
    }

    public char getFirstCharacter() {
        return c1;
    }
    public char getSecondCharacter() {
        return c2;
    }

    }
