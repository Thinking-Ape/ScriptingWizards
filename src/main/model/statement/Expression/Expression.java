package main.model.statement.Expression;

import javafx.util.Pair;
import main.utility.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Expression {

    private ExpressionType expressionType;

    public Expression(ExpressionType expressionType){
        if(expressionType == null) throw new IllegalArgumentException("ExpressionType cannot be null!");
        this.expressionType = expressionType;
    }

    public static Expression expressionFromString(String code){//}, int level) {
        code = code.trim();
        code = Util.removeUnnecessaryBrackets(code);


        Matcher parameterMatcher = Pattern.compile("^([^{}();,]+?),([^{}();]++)$").matcher(code);
        if(parameterMatcher.matches()){
            return new ExpressionTree(expressionFromString(parameterMatcher.group(1)),ExpressionType.PAR, expressionFromString(parameterMatcher.group(2)));
        }
        Pair<ExpressionType,Integer> expressionTypeAtPos = findExpressionTypeAtPos(code,ExpressionType.ADD,ExpressionType.SUB);
        if(expressionTypeAtPos.getValue() != -1 && expressionTypeAtPos.getValue() !=0){
            return expressionTreeWithType(code, expressionTypeAtPos);
        }
        expressionTypeAtPos = findExpressionTypeAtPos(code,ExpressionType.MULT,ExpressionType.MOD,ExpressionType.DIV);
        if(expressionTypeAtPos.getValue() != -1 && expressionTypeAtPos.getValue() !=0){
            return expressionTreeWithType(code, expressionTypeAtPos);
        }
        Pattern pattern = Pattern.compile("^([a-z A-Z]+)\\((.*)\\)$");
        Matcher matcher = pattern.matcher(code);
        if(matcher.matches()){
            return new ExpressionTree(new ExpressionLeaf(matcher.group(1)),ExpressionType.SIMPLE, expressionFromString(matcher.group(2)));
        }
        return new ExpressionLeaf(code);
    }

    private static ExpressionTree expressionTreeWithType(String code, Pair<ExpressionType,Integer> expressionTypeAtPos) {
        int i = expressionTypeAtPos.getValue();
        if(i == code.length()-1)throw new IllegalArgumentException(code + " lacks an argument");
        if(expressionTypeAtPos.getKey()==ExpressionType.SUB){
            Pair<ExpressionType,Integer> eI = findExpressionTypeAtPos(code.substring(0,i).trim(),ExpressionType.values());
            int lastSignPos = code.substring(0, i).trim().length()-1;
            if(eI.getValue()== lastSignPos)return new ExpressionTree(expressionFromString(code.substring(0,lastSignPos)),eI.getKey(), expressionFromString(code.substring(lastSignPos+1)));
        }
        return new ExpressionTree(expressionFromString(code.substring(0,i)),expressionTypeAtPos.getKey(), expressionFromString(code.substring(i+1)));
    }

    private static Pair<ExpressionType,Integer> findExpressionTypeAtPos(String code, ExpressionType... expressionTypes) {
        int depth = 0;
        ExpressionType expressionType;
        for(int i =code.length()-1; i >= 0 ;i--){
            char c = code.charAt(i);
            if(c == '(') {depth--;continue;}
            if(c == ')') {depth++;continue;}
            expressionType = ExpressionType.getExpressionTypeFromChars(c);

            if(expressionType!= ExpressionType.SIMPLE && depth == 0){
                if(Util.arrayContains(expressionTypes,expressionType)){
                    return new Pair<>(expressionType,i);
                }
            }
        }
        return new Pair<>(null,-1);
    }

    public ExpressionType getExpressionType(){
        return expressionType;
    }

    public abstract String getText();
    public abstract int getDepth();
    public abstract boolean isLeaf();
}
