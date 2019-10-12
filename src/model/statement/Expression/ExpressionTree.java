package model.statement.Expression;

import javafx.util.Pair;
import utility.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExpressionTree {
    private ExpressionType expressionType;
    private ExpressionTree leftNode;
    private ExpressionTree rightNode;

    public ExpressionTree(ExpressionTree leftNode, ExpressionType expressionType, ExpressionTree rightNode){
        this.leftNode = leftNode;
        this.rightNode = rightNode;
        this.expressionType = expressionType;
    }
    protected ExpressionTree(ExpressionType expressionType){
        this(null,expressionType,null);
    }

    public ExpressionType getExpressionType() {
        return expressionType;
    }

    public ExpressionTree getLeftNode() {
        return leftNode;
    }

    public ExpressionTree getRightNode() {
        return rightNode;
    }
    //public int getDepth(){return depth;}
    public String getText(){
        String leftNodeText = leftNode.getText();
        String rightNodeText = rightNode.getText();
        if(leftNode.getLeftNode() != null || leftNode.getRightNode() != null) leftNodeText = "("+leftNode.getText()+")";
        if(rightNode.getLeftNode() != null || rightNode.getRightNode() != null) rightNodeText = "("+rightNode.getText()+")"; // && expressionType!= ExpressionType.CAL)
        switch (expressionType){

            case ADD:
                return "" + leftNodeText + " + " + rightNodeText+"";
            case SUB:
                return "" + leftNodeText + " - " + rightNodeText+"";
            case DIV:
                return "" + leftNodeText + " / " + rightNodeText+"";
            case MULT:
                return "" + leftNodeText + " * " + rightNodeText+"";
            case MOD:
                return "" + leftNodeText + " % " + rightNodeText+"";
//            case CAL:
//                return leftNodeText +"." +rightNodeText;
            case SIMPLE:
                String expression =leftNodeText +"(" +rightNodeText+")";
                return expression;
        }
        return "";
    }
    public static ExpressionTree expressionTreeFromString(String code){//}, int level) {
        code = code.trim();
        code = Util.removeUnnecessaryBrackets(code);

//        Pair<ExpressionType,Integer> expressionTypeAtPos = findExpressionTypeAtPos(code,ExpressionType.CAL);
//        if(expressionTypeAtPos.getValue() != -1 && expressionTypeAtPos.getValue() !=0){
//            return expressionTreeWithType(code, expressionTypeAtPos);
//        }
        Pair<ExpressionType,Integer> expressionTypeAtPos = findExpressionTypeAtPos(code,ExpressionType.ADD,ExpressionType.SUB);
        if(expressionTypeAtPos.getValue() != -1 && expressionTypeAtPos.getValue() !=0){
            ExpressionTree e = expressionTreeWithType(code, expressionTypeAtPos);
//            System.out.println(e.getText()+e.getExpressionType());
            return e;
        }
        expressionTypeAtPos = findExpressionTypeAtPos(code,ExpressionType.MULT,ExpressionType.MOD,ExpressionType.DIV);
        if(expressionTypeAtPos.getValue() != -1 && expressionTypeAtPos.getValue() !=0){
            return expressionTreeWithType(code, expressionTypeAtPos);
        }
        Pattern pattern = Pattern.compile("^([a-z A-Z]+)\\((.*)\\)$");
        Matcher matcher = pattern.matcher(code);
        if(matcher.matches()){
            return new ExpressionTree(new ExpressionLeaf(matcher.group(1)),ExpressionType.SIMPLE,expressionTreeFromString(matcher.group(2)));
        }
//        else System.out.println(code);
        return new ExpressionLeaf(code);
    }

    private static ExpressionTree expressionTreeWithType(String code, Pair<ExpressionType,Integer> expressionTypeAtPos) {
        int i = expressionTypeAtPos.getValue();
        if(i == code.length()-1)throw new IllegalArgumentException(code + " lacks an argument");
        if(expressionTypeAtPos.getKey()==ExpressionType.SUB){
            Pair<ExpressionType,Integer> eI = findExpressionTypeAtPos(code.substring(0,i).trim(),ExpressionType.values());
            int lastSignPos = code.substring(0, i).trim().length()-1;
            if(eI.getValue()== lastSignPos)return new ExpressionTree(expressionTreeFromString(code.substring(0,lastSignPos)),eI.getKey(),expressionTreeFromString(code.substring(lastSignPos+1)));
        }
        return new ExpressionTree(expressionTreeFromString(code.substring(0,i)),expressionTypeAtPos.getKey(),expressionTreeFromString(code.substring(i+1)));
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

    public boolean equals(ExpressionTree expressionTree){
        return getText().equals(expressionTree.getText());
    }

    public int getDepth() {
        int left = 1 + (leftNode == null ? 0 : leftNode.getDepth());
        int right = 1 + (leftNode == null ? 0 : leftNode.getDepth());
        return left > right ? left : right;
    }
}
