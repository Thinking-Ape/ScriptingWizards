package main.model.statement.Expression;

import main.utility.GameConstants;

public enum ExpressionType {

    //TODO: CAL('.'),
    ADD('+'),
    SUB('-'),
    DIV('/'),
    MULT('*'),
    MOD('%'),
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

   }
