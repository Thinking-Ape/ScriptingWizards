package model.statement.Expression;

import model.util.GameConstants;

public enum ExpressionType {

    CAL('.'), //eventuell wieder verwenden?
    ADD('+'),
    SUB('-'),
    DIV('/'),
    MULT('*'),
    MOD('%'),
//    ARGS(','), //TODO: doesnt work atm
    SIMPLE(GameConstants.ANY_CHAR);

   ExpressionType(char c1){
        this.c1 = c1;
//        this.c2 = c2;
    }
    private final char c1;


    public static ExpressionType getExpressionTypeFromChars(char c1){
        for(ExpressionType expressionType : values()){
            if(c1 == expressionType.getFirstCharacter()){
//                if(expressionType.getSecondCharacter() == GameConstants.ANY_CHAR || c2 == expressionType.getSecondCharacter()) return expressionType;
                return expressionType;
            }
        }
        return ExpressionType.SIMPLE;
    }

    public char getFirstCharacter() {
        return c1;
    }
//    public char getSecondCharacter() {
//        return c2;
//    }

   }
