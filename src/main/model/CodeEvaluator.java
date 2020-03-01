package main.model;

import main.model.enums.*;
import main.model.statement.*;
import main.model.statement.Condition.*;
import main.model.statement.Expression.ExpressionLeaf;
import main.model.statement.Expression.ExpressionTree;
import main.model.statement.Expression.ExpressionType;
import main.utility.GameConstants;
import main.utility.Point;
import main.utility.Variable;
import main.utility.VariableType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.utility.GameConstants.NO_ENTITY;

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
                if(!testCondition(forCondition)){
                    forStatement.getParentStatement().skip();
                    forStatement.resetVariables(true);
                }
                break;
            case WHILE:
                //TODO: all the same -> method
                WhileStatement whileStatement = (WhileStatement)currentStatement;
                Condition whileCondition = whileStatement.getCondition();
                if(!testCondition(whileCondition)){
                    currentStatement.getParentStatement().skip();
                }
                break;
            case IF:
                //TODO: all the same -> method
                ConditionalStatement ifStatement = (ConditionalStatement)currentStatement;
                Condition ifCondition = ifStatement.getCondition();
                if(!testCondition(ifCondition)){
                    ifStatement.activateElse();
                    currentStatement.getParentStatement().skip();
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
                }
                break;
            case METHOD_CALL:
                MethodCall mC = (MethodCall)currentStatement;

                break;
            case DECLARATION:
                Assignment declaration = (Assignment)currentStatement;
                Variable variable = declaration.getVariable();
                if(declaration.getVariable().getVariableType()==VariableType.KNIGHT && gameMap.isCellFree(gameMap.findSpawn()))lastStatementSummonedKnight = true;
                currentStatement.getParentStatement().addLocalVariable(new Variable(variable.getVariableType(),variable.getName(),variable.getValue()));
                break;
            case ASSIGNMENT:
                Assignment assignment = (Assignment)currentStatement;
                Variable variable2 = null;

                if(assignment.getVariable().getVariableType()==VariableType.KNIGHT)lastStatementSummonedKnight = true;
                switch(assignment.getVariable().getVariableType()){
                    default:variable2 = new Variable(assignment.getVariable().getVariableType(), assignment.getVariable().getName(), assignment.getVariable().getValue());
                    break;
                    case INT: variable2 = new Variable(assignment.getVariable().getVariableType(),assignment.getVariable().getName(),ExpressionTree.expressionTreeFromString(evaluateNumericalExpression(assignment.getVariable().getValue())+""));
                        break;
                    case BOOLEAN: variable2 = new Variable(assignment.getVariable().getVariableType(),assignment.getVariable().getName(),ExpressionTree.expressionTreeFromString(evaluateBoolVariable(assignment.getVariable().getValue().getText())+""));
                        break;
                    case KNIGHT:
                    case SKELETON:
                        //TODO: seems to do nothing
                        variable2 = new Variable(assignment.getVariable().getVariableType(),assignment.getVariable().getName(),ExpressionTree.expressionTreeFromString(assignment.getVariable().getValue().getText()));
//                        throw new IllegalAccessException("Not implemented yet!");
                        break;
                    case ACTION:

                        break;
                }
                assert variable2 != null;
                currentStatement.getParentStatement().updateVariable(variable2);
                break;
        }
        return currentStatement;
    }





    /*private Condition replaceArmyVariables(Condition conditionFromString, String name, String s) {
        if(conditionFromString.isLeaf()){
            ConditionLeaf conditionLeaf = (ConditionLeaf)conditionFromString;
            if(conditionLeaf.getSimpleConditionType() != BooleanType.CAL &&conditionLeaf.getSimpleConditionType() != BooleanType.SIMPLE){


            }
        }
        for(Condition c : )
    }*/


    public boolean testCondition(Condition condition) throws IllegalAccessException {
        if(condition == null)return true;
        if(condition.getConditionType() == ConditionType.SINGLE){
            return evaluateBooleanExpression((ConditionLeaf)condition);
        }
        boolean evaluateLeft = false;
        if(condition.getConditionType() != ConditionType.NEGATION) evaluateLeft = testCondition(((ConditionTree)condition).getLeftCondition());
        boolean evaluateRight = testCondition(((ConditionTree)condition).getRightCondition());
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
        if(node.getText().equals(""))throw new IllegalArgumentException("Cant be blank!");
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
            int bnd1 =evaluateNumericalExpression(ExpressionTree.expressionTreeFromString(matcher.group(1)));
            int bnd2 =evaluateNumericalExpression(ExpressionTree.expressionTreeFromString(matcher.group(2)));
            int rndInt = GameConstants.RANDOM.nextInt(bnd2+1-bnd1)+bnd1; //TODO: random doesnt work as intended yet!

            return rndInt;
        }
        if(variableString.charAt(variableString.length()-1)==';')variableString = variableString.substring(0,variableString.length()-1); //TODO: warum sind die ';' mit dabei?
        Variable variable =  currentStatement.getParentStatement().getVariable(variableString);
        if(currentStatement.getStatementType() == StatementType.FOR) variable = ((ComplexStatement)currentStatement).getVariable(variableString);
        if(variable!= null)return evaluateNumericalExpression(variable.getValue());
        else return Integer.valueOf(variableString);
    }
    private boolean evaluateBoolVariable(String variableString) throws IllegalAccessException {
        if(variableString.equals("true")||variableString.equals("false"))return Boolean.valueOf(variableString);
        variableString = variableString.trim();
        Variable variable =  currentStatement.getParentStatement().getVariable(variableString);
        if(variable.getValue().getText().equals("true")||variable.getValue().getText().equals("false"))
            return Boolean.valueOf(variable.getValue().getText());
        else {
            Condition condition = Condition.getConditionFromString(variable.getValue().getText());
            return testCondition(condition);
        }
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
        if(conditionLeaf.getSimpleConditionType() == BooleanType.CAL){
            String objectName = leftTree.getText(); //TODO: ????Does Cal have to be an Expression? -> just objectString.methodString(parameterString)??:
            String methodName = rightTree.getLeftNode() == null ? rightTree.getText().substring(0,rightTree.getText().length()-2) : rightTree.getLeftNode().getText();
            String parameters = rightTree.getRightNode() == null ? "" : rightTree.getRightNode().getText();
            if(GameConstants.SHOW_BOOLEAN_METHODS)System.out.println(objectName+"."+methodName+"("+parameters+")");
            return evaluateBooleanMethodCall(objectName,methodName,parameters);
        }
        if(conditionLeaf.getSimpleConditionType() == BooleanType.SIMPLE){
            return evaluateBoolVariable(leftTree.getText());
        }
        int leftEvaluated , rightEvaluated;
        switch (conditionLeaf.getSimpleConditionType()){
            case GR_EQ:
                leftEvaluated =  evaluateNumericalExpression(leftTree);
                rightEvaluated = evaluateNumericalExpression(rightTree);
                return leftEvaluated >= rightEvaluated;
            case LE_EQ:
                leftEvaluated =  evaluateNumericalExpression(leftTree);
                rightEvaluated = evaluateNumericalExpression(rightTree);
                return leftEvaluated <= rightEvaluated;
            case GR:
                leftEvaluated =  evaluateNumericalExpression(leftTree);
                rightEvaluated = evaluateNumericalExpression(rightTree);
                return leftEvaluated > rightEvaluated;
            case LE:
                leftEvaluated =  evaluateNumericalExpression(leftTree);
                rightEvaluated = evaluateNumericalExpression(rightTree);
                return leftEvaluated < rightEvaluated;
            case NEQ:
                Variable var1 = currentStatement.getParentStatement().getVariable(leftTree.getText());
                Variable var2 = currentStatement.getParentStatement().getVariable(rightTree.getText());
                VariableType vt1 = VariableType.getVariableTypeFromValue(leftTree.getText());
                VariableType vt2 = VariableType.getVariableTypeFromValue(rightTree.getText());
                boolean var1Found = var1!=null && var1.getVariableType() != VariableType.INT;
                boolean var2Found = var2!=null && var2.getVariableType() != VariableType.INT;

                if(var1Found || var2Found)return evaluateBooleanExpression(new ConditionLeaf(var1Found ? var1.getValue() : leftTree, conditionLeaf.getSimpleConditionType(),var2Found ? var2.getValue() : rightTree));
                boolean vt1Found = vt1 !=VariableType.ACTION && vt1 != VariableType.INT;
                boolean vt2Found = vt2 !=VariableType.ACTION && vt2 != VariableType.INT;
                if(vt1Found && vt2Found) return !leftTree.getText().equals(rightTree.getText());

                leftEvaluated =  evaluateNumericalExpression(leftTree);
                rightEvaluated = evaluateNumericalExpression(rightTree);
                return leftEvaluated != rightEvaluated;
            case EQ:
                var1 = currentStatement.getParentStatement().getVariable(leftTree.getText());
                var2 = currentStatement.getParentStatement().getVariable(rightTree.getText());
                vt1 = VariableType.getVariableTypeFromValue(leftTree.getText());
                vt2 = VariableType.getVariableTypeFromValue(rightTree.getText());
                var1Found = var1!=null && var1.getVariableType() != VariableType.INT;
                var2Found = var2!=null && var2.getVariableType() != VariableType.INT;

                if(var1Found || var2Found)return evaluateBooleanExpression(new ConditionLeaf(var1Found ? var1.getValue() : leftTree, conditionLeaf.getSimpleConditionType(),var2Found ? var2.getValue() : rightTree));
                vt1Found = vt1 !=VariableType.ACTION && vt1 != VariableType.INT;
                vt2Found = vt2 !=VariableType.ACTION && vt2 != VariableType.INT;
                if(vt1Found && vt2Found) return leftTree.getText().equals(rightTree.getText());

                leftEvaluated =  evaluateNumericalExpression(leftTree);
                rightEvaluated = evaluateNumericalExpression(rightTree);
                return leftEvaluated == rightEvaluated;
        }
        throw new IllegalArgumentException(leftTree.getText()+" is not a valid boolean!");
    }

    //TODO: Does Cal have to be an Expression? -> just objectString.methodString(parameterString)??
    private boolean evaluateBooleanMethodCall(String objectName, String methodName, String parameterString) throws IllegalAccessException {
        Variable variable = currentStatement.getParentStatement().getVariable(objectName);
        if(variable == null)throw new IllegalArgumentException("Variable "+objectName +" does not exist!");
        VariableType vType = variable.getVariableType();
        if(vType == VariableType.KNIGHT||vType == VariableType.SKELETON || vType == VariableType.ARMY){
            String[] nameList = new String[]{objectName};
            if(vType == VariableType.ARMY)nameList = variable.getValue().getRightNode().getText().split(",");
                    boolean output = true;
            for(int i = 0; i < nameList.length; i++){
                objectName = nameList[i];
            Point actorPoint = gameMap.getEntityPosition(objectName);
            if(actorPoint == null) continue;
            Point targetPoint = gameMap.getTargetPoint(objectName);
            if(targetPoint.getX()==-1)return false;
            CellContent targetContent = gameMap.getContentAtXY(targetPoint);
            Entity actorEntity = gameMap.getEntity(actorPoint);
            Entity targetEntity = gameMap.getEntity(targetPoint);
            switch (MethodType.getMethodTypeFromName(methodName)){
                case DROP_ITEM:
                case WAIT:
                case MOVE:
                case TURN:
                case USE_ITEM:
                case COLLECT:
                    throw new IllegalAccessException("Method: \"" + methodName + "\" is not allowed here!"); //TODO: exceptions should occur in CodeParser
                case CAN_MOVE:
                    if(gameMap.isGateWrongDirection(actorPoint,targetPoint))output = false;
                    output = output && (gameMap.isCellFree(targetPoint) && (targetContent.isTraversable() || (gameMap.cellHasFlag(targetPoint, CFlag.OPEN) ^ gameMap.cellHasFlag(targetPoint, CFlag.INVERTED))));
                    continue;
                case HAS_ITEM:
                    if(parameterString.equals(""))output = output && (actorEntity.getItem()!= ItemType.NONE);
                    else output = output && (actorEntity.getItem()==ItemType.getValueFromName(parameterString));
                    continue;
                case TARGETS_CELL:
                    output = output && (targetContent == CellContent.getValueFromName(parameterString));
                    continue;
                case TARGET_IS_DANGER:
                    output = output && (gameMap.cellHasFlag(targetPoint,CFlag.PREPARING)||gameMap.cellHasFlag(targetPoint,CFlag.ARMED)); //||!(targetEntity.getEntityType()!= EntityType.SKELETON)
                    continue;
                case TARGETS_ENTITY:
                    if(parameterString.equals(""))output = output && (targetEntity.getEntityType()!= EntityType.NONE);
                    else output = output && (targetEntity.getEntityType()==EntityType.getValueFromName(parameterString));
                    continue;
                case TARGETS_ITEM:
                    if(parameterString.equals(""))output = output && (gameMap.getItem(targetPoint)!=ItemType.NONE);
                    else output = output && (gameMap.getItem(targetPoint)==ItemType.getValueFromName(parameterString));
                    continue;
                case IS_LOOKING:
                    output = output && ((actorEntity.getDirection() == Direction.getValueFromString(parameterString)));
                    continue;
            }

            throw new IllegalStateException("Method \"" + methodName+"("+parameterString+")\" could not be evaluated");
        }return output;}
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
