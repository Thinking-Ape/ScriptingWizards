package model;

import model.enums.*;
import model.statement.*;
import model.statement.Condition.*;
import model.statement.Expression.ExpressionLeaf;
import model.statement.Expression.ExpressionTree;
import model.statement.Expression.ExpressionType;
import util.GameConstants;
import util.Point;
import util.Variable;
import util.VariableType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEvaluator {

    private Statement currentStatement;
    private GameMap gameMap;
    private boolean lastStatementSummonedKnight;

    public CodeEvaluator(){
        this.currentStatement = null; //TODO: may cause problems?
       // this.gameMap = gameMap;
    }


    public Statement evaluateNext(ComplexStatement behaviour, GameMap gameMap) throws IllegalAccessException {
        this.gameMap =gameMap;
        lastStatementSummonedKnight = false;
        currentStatement = behaviour.nextStatement();
        if(currentStatement==null)return null;
        switch (currentStatement.getStatementType()){
            //TODO: statementDepth map, List<Variable> in every statement, new Variable -> statemnetDepthMap.get(currentDepth -1).addVariable(variable) -> add also to substatements
            case COMPLEX:
                break;
            case FOR:
                ForStatement forStatement = (ForStatement)currentStatement;
                Condition forCondition = forStatement.getCondition();
//                System.out.println(forStatement.getCondition().getParentStatement().getText());
//                currentStatement = forStatement.getCondition(); //TODO: UGLY CODE!!! doesnt work also!!
                if(!testCondition(forCondition)){
                    forStatement.getParentStatement().skip();
                    forStatement.resetVariables(true);
                }
//                currentStatement = forStatement;
                break;
            case WHILE:
                //TODO: all the same -> method
                WhileStatement whileStatement = (WhileStatement)currentStatement;
                Condition whileCondition = whileStatement.getCondition();
                if(!testCondition(whileCondition)){
                    currentStatement.getParentStatement().skip();
//                    whileStatement.resetVariables();
                }
                break;
            case IF:
                //TODO: all the same -> method
                ConditionalStatement ifStatement = (ConditionalStatement)currentStatement;
                Condition ifCondition = ifStatement.getCondition();
                if(!testCondition(ifCondition)){
                    ifStatement.activateElse();
                    currentStatement.getParentStatement().skip();
//                    ifStatement.resetVariables();
                }
                break;
            case ELSE:
                ConditionalStatement elseStatement = (ConditionalStatement)currentStatement;
                Condition elseCondition = elseStatement.getCondition();
                if(!elseStatement.isActive()){
                    currentStatement.getParentStatement().skip();
                } else if(!testCondition(elseCondition)){
                    elseStatement.activateElse();
                    currentStatement.getParentStatement().skip();
//                    elseStatement.resetVariables();
                }
                break;
            case METHOD_CALL:
                break;
            case DECLARATION:
                Assignment declaration = (Assignment)currentStatement;
                Variable variable = declaration.getVariable();
                if(declaration.getVariable().getVariableType()==VariableType.KNIGHT)lastStatementSummonedKnight = true;
                //variable.update(evaluateRandom(variable.getValue(),0));
                currentStatement.getParentStatement().addLocalVariable(new Variable(variable.getVariableType(),variable.getName(),variable.getValue()));
                break;
            case ASSIGNMENT:
                Assignment assignment = (Assignment)currentStatement;
                Variable variable2 = null;

                if(assignment.getVariable().getVariableType()==VariableType.KNIGHT)lastStatementSummonedKnight = true;
                switch(assignment.getVariable().getVariableType()){
                    case INT: variable2 = new Variable(assignment.getVariable().getVariableType(),assignment.getVariable().getName(),ExpressionTree.expressionTreeFromString(evaluateNumericalExpression(assignment.getVariable().getValue())+""));
                        break;
                    case BOOLEAN: variable2 = new Variable(assignment.getVariable().getVariableType(),assignment.getVariable().getName(),ExpressionTree.expressionTreeFromString(evaluateBoolVariable(assignment.getVariable().getValue().getText())+""));
                        break;
                    case KNIGHT:
                    case SKELETON:
                        //TODO: seems to do nothing
                        variable2 = new Variable(assignment.getVariable().getVariableType(),assignment.getVariable().getName(),ExpressionTree.expressionTreeFromString(assignment.getVariable().getValue().getText()));
//                        throw new IllegalAccessException("Not implemented yet!");
                    case DEFAULT:
//                        System.out.println(assignment.getVariable().getDisplayName()+" "+assignment.getVariable().getValue().getText());
                        break;
                }
                assert variable2 != null;
                currentStatement.getParentStatement().updateVariable(variable2);
                break;
        }
        return currentStatement;
    }


    public boolean testCondition(Condition condition) throws IllegalAccessException {
        if(condition == null)return true;
        if(condition.getConditionType() == ConditionType.SIMPLE){
            return evaluateBooleanExpression((ConditionLeaf)condition);
        }
        boolean evaluateLeft = false;
        if(condition.getConditionType() != ConditionType.NEGATION) evaluateLeft = testCondition(((ConditionTree)condition).getLeftNode());
        boolean evaluateRight = testCondition(((ConditionTree)condition).getRightNode());
        switch (condition.getConditionType()){
            case AND:
                return evaluateLeft && evaluateRight;
            case OR:
                return evaluateLeft || evaluateRight;
            case NEGATION:
                return !evaluateRight;

        }
        throw new IllegalArgumentException("Condition \""+ condition.getText() +"\" contains error!");
    }
    //TODO: parentStatement?
    private int evaluateNumericalExpression(ExpressionTree node) throws IllegalAccessException {
        if(node.getText().equals(""))return 0;
        if(node.getExpressionType() == ExpressionType.SIMPLE){
            return evaluateIntVariable(node.getText());
        }
        int leftEvaluated = evaluateNumericalExpression(node.getLeftNode());
        int rightEvaluated = evaluateNumericalExpression(node.getRightNode());
        switch (node.getExpressionType()){
            case ADD:
                return leftEvaluated + rightEvaluated;
            case SUB:
                return leftEvaluated - rightEvaluated;
            case DIV:
                return leftEvaluated / rightEvaluated;
            case MULT:
                return leftEvaluated * rightEvaluated;
            case MOD:
                return leftEvaluated % rightEvaluated;
            default:
                throw new IllegalAccessException(node.getText()+"of type " + node.getExpressionType()+ " could not be evaluated correctly!"); //this should never be reached
        }
    }

    private int evaluateIntVariable(String variableString) throws IllegalAccessException {
        variableString = variableString.trim();
        Matcher matcher = Pattern.compile(GameConstants.RAND_INT_REGEX).matcher(variableString);
        if(matcher.matches()){
            int bnd1 = Integer.valueOf(matcher.group(1));
            int bnd2 = Integer.valueOf(matcher.group(2));
            int rndInt = GameConstants.RANDOM.nextInt(bnd2+1-bnd1)+bnd1; //TODO: random doesnt work as intended yet!

            return rndInt;
        }
        if(variableString.charAt(variableString.length()-1)==';')variableString = variableString.substring(0,variableString.length()-1); //TODO: warum sind die ';' mit dabei?
        Variable variable =  currentStatement.getParentStatement().getVariable(variableString);
        if(currentStatement.getStatementType() == StatementType.FOR) variable = ((ComplexStatement)currentStatement).getVariable(variableString);
        if(variable!= null)return evaluateNumericalExpression(variable.getValue());
        else return Integer.valueOf(variableString);
    }
    private boolean evaluateBoolVariable(String variableString) {
        if(variableString.equals("true")||variableString.equals("false"))return Boolean.valueOf(variableString);
        variableString = variableString.trim();
        Variable variable =  currentStatement.getParentStatement().getVariable(variableString);
        return Boolean.valueOf(variable.getValue().getText());
    }

    private String removeBrackets(String expression) {
        String output="";
        for(char c : expression.toCharArray()){

            if(c!='('&&c!=')'&&c!=' ')output+=c;
        }
        return output;
    }

    private boolean evaluateBooleanExpression(ConditionLeaf conditionLeaf) throws IllegalAccessException {
        ExpressionTree leftTree = conditionLeaf.getLeftTree();
        ExpressionTree rightTree = conditionLeaf.getRightTree();
        if(conditionLeaf.getSimpleConditionType() == BooleanType.BOOLEAN){
            if(leftTree.getExpressionType() == ExpressionType.CAL){
                String objectName = leftTree.getLeftNode().getText(); //TODO: ????Does Cal have to be an Expression? -> just objectString.methodString(parameterString)??:
                String methodName = leftTree.getRightNode().getLeftNode() == null ? leftTree.getRightNode().getText().substring(0,leftTree.getRightNode().getText().length()-2) : leftTree.getRightNode().getLeftNode().getText();
                String parameters = leftTree.getRightNode().getLeftNode() == null ? "" : leftTree.getRightNode().getRightNode().getText();
                if(GameConstants.SHOW_BOOLEAN_METHODS)System.out.println(objectName+"."+methodName+"("+parameters+")");
                return evaluateBooleanMethodCall(objectName,methodName,parameters);
            }
            return evaluateBoolVariable(leftTree.getText());
        }
        int leftEvaluated =  evaluateNumericalExpression(leftTree);
        int rightEvaluated = evaluateNumericalExpression(rightTree);
        switch (conditionLeaf.getSimpleConditionType()){
            case GR_EQ:
                return leftEvaluated >= rightEvaluated;
            case LE_EQ:
                return leftEvaluated <= rightEvaluated;
            case GR:
                return leftEvaluated > rightEvaluated;
            case LE:
                return leftEvaluated < rightEvaluated;
            case NEQ:
                return leftEvaluated != rightEvaluated;
            case EQ:
                return leftEvaluated == rightEvaluated;
        }
        throw new IllegalArgumentException(leftTree.getText()+" is not a valid boolean!");
    }

    //TODO: Does Cal have to be an Expression? -> just objectString.methodString(parameterString)??
    private boolean evaluateBooleanMethodCall(String objectName, String methodName, String parameterString) throws IllegalAccessException {
        Variable variable = currentStatement.getParentStatement().getVariable(objectName);
        if(variable == null)throw new IllegalArgumentException("Variable "+objectName +" does not exist!");
        VariableType vType = variable.getVariableType();
        if(vType == VariableType.KNIGHT||vType == VariableType.SKELETON){
            Point actorPoint = gameMap.ecMapGet(objectName);
            if(actorPoint == null) return false;
            Point targetPoint = gameMap.getTargetPoint(objectName);
            CContent targetContent = gameMap.getContentAtXY(targetPoint);
            Entity actorEntity = gameMap.getEntity(actorPoint);
            Entity targetEntity = gameMap.getEntity(targetPoint);
            switch (MethodType.getMethodTypeFromName(methodName)){
                case MOVE:
                case TURN:
                case USE_ITEM:
                case COLLECT:
                    throw new IllegalAccessException("Method: \"" + methodName + "\" is not allowed here!"); //TODO: exceptions should occur in CodeParser
                case CAN_MOVE:
                    return gameMap.isCellFree(targetPoint) && (targetContent.isTraversable() || gameMap.cellHasFlag(targetPoint,CFlag.OPEN));
                case HAS_ITEM:
                    return actorEntity.getItem() != null && (actorEntity.getItem() == ItemType.getValueFromName(parameterString) || parameterString.equals("ANY"));
                case TARGET_CELL_IS:
                    return targetContent == CContent.getValueFromName(parameterString);
                case TARGET_IS_DANGER:
                    return gameMap.cellHasFlag(targetPoint,CFlag.PREPARING)||gameMap.cellHasFlag(targetPoint,CFlag.ARMED)||!(targetEntity==null || targetEntity.getEntityType()!= EntityType.SKELETON);
                case TARGET_CONTAINS:
                    return (targetEntity!=null && targetEntity.getEntityType()==EntityType.getValueFromName(parameterString)) || (gameMap.getItem(targetPoint)!=null&&gameMap.getItem(targetPoint)==ItemType.getValueFromName(parameterString));

            }

            throw new IllegalStateException("Method \"" + methodName+"("+parameterString+")\" could not be evaluated");
        }
        throw new IllegalStateException(objectName + " has wrong variable type!");
    }

    private ExpressionTree evaluateRandom(ExpressionTree expressionTree,int depth) { //TODO: depth unnecessary?? where it lacks it was depth
        if(expressionTree.getExpressionType() == ExpressionType.SIMPLE){

            Matcher matcher = Pattern.compile(GameConstants.RAND_INT_REGEX).matcher(expressionTree.getText());
            if(matcher.matches()){
                int bnd1 = Integer.valueOf(matcher.group(1));
                int bnd2 = Integer.valueOf(matcher.group(2));
                int rndInt = GameConstants.RANDOM.nextInt(bnd2+1-bnd1)+bnd1;
                return new ExpressionLeaf(""+rndInt);
            }
            else return expressionTree;
        }
        return new ExpressionTree(evaluateRandom(expressionTree.getLeftNode(),depth+1),expressionTree.getExpressionType(),evaluateRandom(expressionTree.getRightNode(),depth+1));

    }

    public boolean lastStatementSummonedKnight() {
        return lastStatementSummonedKnight;
    }
}
