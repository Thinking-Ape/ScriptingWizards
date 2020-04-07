package main.model.statement.Condition;

import main.model.statement.Expression.ExpressionTree;
import main.utility.GameConstants;
import main.utility.Util;
import static main.model.statement.Condition.ConditionType.*;


public interface Condition {

    ConditionType getConditionType();
    String getText();

    static Condition getConditionFromString(String code) throws IllegalArgumentException {
        int depth = 0;
        if(code == null || code.matches(" *"))return null;
        if(!code.matches(GameConstants.BOOLEAN_REGEX))throw new IllegalArgumentException("Condition could not be parsed!");
        code = code.trim();
            //throw new IllegalArgumentException("You cannot have an empty condition");
        for(int i =0; i < code.length();i++){
            char c = code.charAt(i);
            char cc = ' ';
            if(c == '(') depth++;
            if(c == ')') depth--;
            if(i < code.length()-1) cc = code.charAt(i+1);

            boolean foundAnd = (c == AND.getFirstCharacter() && cc == AND.getSecondCharacter());
            boolean foundOr = (c == OR.getFirstCharacter() && cc == OR.getSecondCharacter());
            if( foundAnd && depth == 0){
                String firstArgument = Util.stripCode(code.substring(0,i));
                String secondArgument = Util.stripCode(code.substring(i+2));
//                return new ConditionTree(conditionFromString(firstArgument,level+1), ConditionType.AND, conditionFromString(secondArgument,level+1),level+1);
                return new ConditionTree(getConditionFromString(firstArgument), AND, getConditionFromString(secondArgument));
            } else if( foundOr && depth == 0){
                if(! (i+2 < code.length()-1)) throw new IllegalArgumentException("'" + c + "' is not allowed to stand here");
                String firstArgument = Util.stripCode(code.substring(0,i));
                String secondArgument = Util.stripCode(code.substring(i+2));
                return new ConditionTree(getConditionFromString(firstArgument), OR, getConditionFromString(secondArgument));
            }
        }for(int i =0; i < code.length();i++){
            char c = code.charAt(i);
            char cc = ' ';
            if(c == '(') depth++;
            if(c == ')') depth--;
            if(i < code.length()-1) cc = code.charAt(i+1);

            boolean foundNeg = (c == '!'&& cc != '=');
            if( foundNeg && depth == 0){
                if(! (i+2 < code.length()-1)) throw new IllegalArgumentException("'" + c + "' is not allowed to stand here");
                //TODO: NEGATIONS!!!
//                String firstArgument = stripCode(code.substring(0,i));
                String secondArgument = Util.stripCode(code.substring(i+1));
                return new ConditionTree(null, NEGATION, getConditionFromString(secondArgument));
            }
        }

        return getSimpleConditionFromString(code);
    }
    private static ConditionLeaf getSimpleConditionFromString(String code){//}, int depth) {
        code = code.trim();
        for(int i = 0; i < code.length(); i++){
            char c = code.charAt(i);
            char cc = GameConstants.ANY_CHAR;
            if(i < code.length()-1)cc = code.charAt(i+1);
            BooleanType simpleConditionType = BooleanType.getConditionTypeFromChars(c,cc);
            if(simpleConditionType== BooleanType.SIMPLE) continue;
            if(i == code.length()-1)throw new IllegalArgumentException(code + " lacks an argument");
            int j = simpleConditionType.getSecondCharacter() == GameConstants.ANY_CHAR ? 1 : 2;
            ExpressionTree leftTree = ExpressionTree.expressionTreeFromString(code.substring(0,i));
            ExpressionTree rightTree = ExpressionTree.expressionTreeFromString(code.substring(i+j));
            return new ConditionLeaf(leftTree,simpleConditionType,rightTree);

        }
        ExpressionTree expressionTree = ExpressionTree.expressionTreeFromString(code);
        return  new ConditionLeaf(expressionTree, BooleanType.SIMPLE,null);
    }
    boolean isLeaf();
}
