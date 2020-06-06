package main.model.statement.Expression;

import main.model.GameConstants;

public enum ExpressionType {

    //TODO: CAL('.'),
    ADD('+'),
    SUB('-'),
    DIV('/'),
    MULT('*'),
    MOD('%'),
    PAR(','),
    SIMPLE(GameConstants.ANY_CHAR);

   ExpressionType(char c1){
        this.c1 = c1;
    }
    private final char c1;


    public static ExpressionType getExpressionTypeFromChars(char c1){
        for(ExpressionType expressionType : values()){
            if(c1 == expressionType.getFirstCharacter()){
                return expressionType;
            }
        }
        return ExpressionType.SIMPLE;
    }

    public char getFirstCharacter() {
        return c1;
    }

   }
