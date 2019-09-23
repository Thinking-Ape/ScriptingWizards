package model.statement;

import model.enums.MethodType;
import model.statement.Condition.ConditionLeaf;
import model.statement.Condition.ConditionTree;
import model.statement.Expression.ExpressionTree;
import model.statement.Expression.ExpressionType;
import model.util.Variable;
import model.util.VariableDepthMap;
import model.util.VariableType;

import java.util.HashMap;
import java.util.Map;

public class Evaluator {

    private VariableDepthMap variableDepthMap; //eventuell einfach eine Liste der aktuellen variablen?
    private ComplexStatement behaviour;
    private Statement currentStatement;

    public Evaluator(ComplexStatement behaviour){
        this.behaviour = behaviour;
        this.variableDepthMap = new VariableDepthMap();
        currentStatement = behaviour;
    }

    //TODO: actualSize mit oder ohne den Klammern??? (Mit passt besser, aber dann muss man mit den StatementIndizes uafpassen!!
//    private void indexAllStatements(Behaviour behaviour) throws IllegalAccessException {
//
//        int i = 0;
//        while(i < behaviour.getActualSize()){
//            Statement statement = behaviour.getStatement(i);
//            indexedStatementMap.put(i,statement);
//            if(statement.getActualSize() > 1){
//                indexComplexStatement(statement,i+1);
//            }
//            i += statement.getActualSize();
//        }
//    }

//    private void indexComplexStatement(Statement statement, int index) throws IllegalAccessException {
//        int i = 0;
//        while(i < statement.getActualSize()){
//            Statement subStatement = statement.getSubStatement(i);
//            indexedStatementMap.put(index+i,subStatement);
//            if(statement.getActualSize() > 1) indexComplexStatement(statement,index+i);
//            if(i == statement.getActualSize()-1) jumpToIndexMap.put(index+i,index);
//            i += subStatement.getActualSize();
//        }
//    }


    public boolean testCondition(ConditionTree condition) throws IllegalAccessException {
//        ExpressionTree leftTree = condition.getLeftTree();
//        ExpressionTree rightTree = condition.getRightTree();
        switch (condition.getOperatorType()){
            case SIMPLE:
                return evaluateBooleanExpression((ConditionLeaf)condition);
            case AND:
                return testCondition(condition.getLeftNode()) && testCondition(condition.getRightNode());
            case OR:
                return testCondition(condition.getLeftNode()) || testCondition(condition.getRightNode());
            case NEGATION:
                return !testCondition(condition.getRightNode());

        }
        throw new IllegalArgumentException("Condition \""+ condition.getText() +"\" contains error!");
    }

    private double evaluateNumericalExpression(ExpressionTree node) throws IllegalAccessException {
        if(node.getText().equals(""))return 0;
        switch (node.getExpressionType()){
            case ADD:
                return evaluateNumericalExpression(node.getLeftNode()) + evaluateNumericalExpression(node.getRightNode());
            case SUB:
                return evaluateNumericalExpression(node.getLeftNode()) - evaluateNumericalExpression(node.getRightNode());
            case DIV:
                return evaluateNumericalExpression(node.getLeftNode()) / evaluateNumericalExpression(node.getRightNode());
            case MULT:
                return evaluateNumericalExpression(node.getLeftNode()) * evaluateNumericalExpression(node.getRightNode());
            case SIMPLE:
                return evaluateVariable(node.getText());
            default:
                throw new IllegalAccessException(node.getText()+" could not be evaluated correctly!"); //this should never be reached
        }
    }

    private double evaluateVariable(String expression) throws IllegalAccessException {
        expression = expression.trim();

        if(expression.charAt(expression.length()-1)==';')expression = expression.substring(0,expression.length()-1); //TODO: warum sind die ';' mit dabei?
//        expression = removeBrackets(expression);
        if(variableDepthMap.contains(expression,currentStatement.getDepth()))return evaluateNumericalExpression(variableDepthMap.getValue(expression,currentStatement.getDepth())); //TODO: +-1
        else return Double.valueOf(expression);
    }

    private String removeBrackets(String expression) {
        String output="";
        for(char c : expression.toCharArray()){

            if(c!='('&&c!=')'&&c!=' ')output+=c;
        }
        return output;
    }

    private boolean evaluateBooleanExpression(ConditionLeaf conditionLeaf) throws IllegalAccessException {
//        ExpressionTree expressionTree = conditionLeaf.getLeftTree();
        ExpressionTree leftTree = conditionLeaf.getLeftTree();
        ExpressionTree rightTree = conditionLeaf.getRightTree();
        switch (conditionLeaf.getSimpleConditionType()){
            case GR_EQ:
                return evaluateNumericalExpression(leftTree) >= evaluateNumericalExpression(rightTree);
            case LE_EQ:
                return evaluateNumericalExpression(leftTree) <= evaluateNumericalExpression(rightTree);
            case GR:
                return evaluateNumericalExpression(leftTree) > evaluateNumericalExpression(rightTree);
            case LE:
                return evaluateNumericalExpression(leftTree) < evaluateNumericalExpression(rightTree);
            case NEQ:
                return evaluateNumericalExpression(leftTree) != evaluateNumericalExpression(rightTree);
            case EQ:
                return evaluateNumericalExpression(leftTree) == evaluateNumericalExpression(rightTree);
            case SIMPLE:
                if(leftTree.getText().equals("true")) return true; //TODO: eventuell beides unnötig
                else if(leftTree.getText().equals("false")) return false; //TODO: eventuell beides unnötig
                if(leftTree.getExpressionType() == ExpressionType.CAL) evaluateBooleanMethodCall(leftTree);

        }
        throw new IllegalArgumentException(leftTree.getText()+" is not a valid boolean!"); //TODO: Methods that return bool isKnight()
    }

    private void evaluateBooleanMethodCall(ExpressionTree expressionTree) throws IllegalAccessException {
        if(variableDepthMap.contains(expressionTree.getLeftNode().getText(),currentStatement.getDepth())){
            if(variableDepthMap.getType(expressionTree.getLeftNode().getText(),currentStatement.getDepth())== VariableType.KNIGHT){
                if(expressionTree.getRightNode().getExpressionType() != ExpressionType.CAL)throw new IllegalAccessException("Dafuq you doin?");
                else if(MethodType.getMethodType(expressionTree.getRightNode().getRightNode().getText()).equals(MethodType.GET_CELL_IN_FRONT.getName())){

                }
            }
        }
    }

    public Statement evaluateNext() throws IllegalAccessException {
        currentStatement = behaviour.nextStatement();
        if(currentStatement==null)return null;
        System.out.println(currentStatement.getStatementType());
        switch (currentStatement.getStatementType()){

            case FOR:
                ForStatement forStatement = (ForStatement)currentStatement;
                ConditionTree forCondition = forStatement.getCondition();
                if(!testCondition(forCondition)){
                    variableDepthMap.clearAtDepth(currentStatement.getDepth()+1);
                    behaviour.skip();
                }
                break;
            case WHILE:
                WhileStatement whileStatement = (WhileStatement)currentStatement;
                ConditionTree whileCondition = whileStatement.getCondition();
                if(!testCondition(whileCondition)){
                    variableDepthMap.clearAtDepth(currentStatement.getDepth()+1);
                    behaviour.skip();
                }
                break;
            case IF:
                IfStatement ifStatement = (IfStatement)currentStatement;
                ConditionTree ifCondition = ifStatement.getCondition();
                if(!testCondition(ifCondition)){
                    variableDepthMap.clearAtDepth(currentStatement.getDepth()+1);
                    behaviour.skip();
                }
                break;
            case ELSE:
            case COMPLEX:
            case METHOD_CALL:
                break;
            case DECLARATION:
                Assignment declaration = (Assignment)currentStatement;
                Variable variable = declaration.getVariable();
                if(variableDepthMap.contains(variable.getName(),currentStatement.getDepth())) throw new IllegalArgumentException("Variable " + variable.getName() + " is already defined in scope");
                else variableDepthMap.put(variable,currentStatement.getDepth());
                break;
            case ASSIGNMENT:
                Assignment assignment = (Assignment)currentStatement;
                Variable variable2 = new Variable(assignment.getVariable().getName(),ExpressionTree.expressionTreeFromString(evaluateNumericalExpression(assignment.getVariable().getExpression())+"",0),assignment.getVariable().getVariableType());
                if(variableDepthMap.contains(variable2.getName(),currentStatement.getDepth())) variableDepthMap.update(variable2,currentStatement.getDepth());
                else throw new IllegalArgumentException("Variable " + variable2.getName() + " hasnt been defined yet!");
                break;
        }
        return currentStatement;
    }
//
//    private Variable getVariableFromStatement(Assignment assignment){
//        String leftHandSide = assignment.getLeftTree().getLeftNode().getExpression();
//        String variableName = leftHandSide.replaceAll(".* ","");
//        String variableType = leftHandSide.replaceAll(" .*","");
//        return new Variable(variableName,assignment.getLeftTree().getRightNode(), VariableType.getVariableTypeFromString(variableType));
//
//    }
}
/*public ExpressionTree nextExpressionTree() throws IllegalAccessException {

        Statement statement = null;
        switch (statement.getStatementType()){
            case FOR:
                depth++;
                ForStatement forStatement = (ForStatement)statement;
                Assignment assignment1 = forStatement.getAssignment1();
                String leftHandSide = assignment1.walk().getLeftNode().getExpression();
//            String rightHandSide = assignment1.walk(depth).getRightNode().getExpression();
                String variableName = leftHandSide.replaceAll(".* ","");
//            String variableTypeString = leftHandSide.replaceAll(" .*","");
//            VariableType variableType = VariableType.getVariableTypeFromString(variableTypeString);
//            Variable variable = new Variable(variableName,ExpressionTree.expressionTreeFromString(rightHandSide,0),variableType);

//                variableDepthMap.put(variableName,assignment1.walk().getRightNode(),depth);
                ConditionTree condition = forStatement.getCondition();
                if(testCondition(condition)) return forStatement.walk();
                break;
            case WHILE:
                break;
            case IF:
                break;
            case ELSE:
                break;
            case METHOD_CALL:
                break;
            case ASSIGNMENT:
                break;
        }
        return null;
    }
    */
