package main.model;

import main.model.gamemap.Entity;
import main.model.gamemap.GameMap;
import main.model.enums.*;
import main.model.statement.*;
import main.model.statement.Condition.*;
import main.model.statement.Expression.ExpressionLeaf;
import main.model.statement.Expression.ExpressionTree;
import main.model.statement.Expression.ExpressionType;
import main.utility.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CodeEvaluator {

    private static Statement currentStatement;
    private static GameMap currentGameMap;

    public static Statement evaluateNext(ComplexStatement behaviour, GameMap gameMap) {
        currentGameMap = gameMap;
        currentStatement = behaviour.nextStatement();
        if(currentStatement==null)return null;

        Condition condition = new ConditionLeaf(null, BooleanType.SIMPLE, null);
        if(currentStatement.isComplex())condition = ((ComplexStatement)currentStatement).getCondition();
        switch (currentStatement.getStatementType()){
            //TODO: statementDepth map, List<Variable> in every statement, new Variable -> statemnetDepthMap.get(currentDepth -1).addVariable(variable) -> add also to substatements
            case COMPLEX:
                break;
            case FOR:
                ForStatement forStatement = (ForStatement)currentStatement;
                if(!testCondition(condition)){
                    forStatement.getParentStatement().skip();
                    forStatement.resetVariables(true);
                }
                break;
            case WHILE:
                if(!testCondition(condition)){
                    currentStatement.getParentStatement().skip();
                }
                break;
            case IF:
                ConditionalStatement ifStatement = (ConditionalStatement)currentStatement;
                if(!testCondition(condition)){
                    ifStatement.activateElse();
                    currentStatement.getParentStatement().skip();
                }
                break;
            case ELSE:
                ConditionalStatement elseStatement = (ConditionalStatement)currentStatement;
                if(!elseStatement.isActive()){
                    currentStatement.getParentStatement().skip();
                } else if(!testCondition(condition)){
                    elseStatement.activateElse();
                    currentStatement.getParentStatement().skip();
                }
                break;
            case METHOD_CALL:
                //TODO: necessary?
                MethodCall mC = (MethodCall)currentStatement;

                break;
            case DECLARATION:
                Assignment declaration = (Assignment)currentStatement;
                Variable variable = declaration.getVariable();
                currentStatement.getParentStatement().addLocalVariable(new Variable(variable.getVariableType(),variable.getName(),variable.getValue()));
                break;
            case ASSIGNMENT:
                Assignment assignment = (Assignment)currentStatement;
                Variable variable2 = null;

                VariableType variableType =assignment.getVariable().getVariableType();
                String varName = assignment.getVariable().getName();
                switch(assignment.getVariable().getVariableType()){
                    default:
                        variable2 = new Variable(variableType, varName, assignment.getVariable().getValue());
                        break;
                    case INT:
                        variable2 = new Variable(variableType,varName,new ExpressionLeaf(evaluateNumericalExpression(assignment.getVariable().getValue())+""));
                        break;
                    case BOOLEAN:
                        variable2 = new Variable(variableType,varName,new ExpressionLeaf(evaluateBoolVariable(assignment.getVariable().getValue().getText())+""));
                        break;
                    case KNIGHT:
                    case SKELETON:
                        //TODO: seems to do nothing
                        // TODO: why not just assignment.getVariable().getValue()?
                        //ExpressionTree.expressionTreeFromString( "code below".getText())
                        ExpressionTree varValue = assignment.getVariable().getValue();
                        variable2 = new Variable(variableType,varName,varValue);
                        break;
                    case VOID:
                        break;
                }
                assert variable2 != null;
                currentStatement.getParentStatement().updateVariable(variable2);
                break;
        }
        return currentStatement;
    }

    /** Evaluates whether a given Condition Object should represent a true or false boolean in regards to
     *  the current map
     * @throws IllegalAccessException This will only occur if there are incompatible types of Variables
     */
    //TODO: removeCurrentLevel IllegalAccessException
    public static boolean testCondition(Condition condition) {
        if(condition == null)return true;
        if(condition.getConditionType() == ConditionType.SINGLE){
            return evaluateBooleanExpression((ConditionLeaf)condition);
        }
        // if the above is wrong, the Condition must be a ConditionTree
        ConditionTree conditionTree = (ConditionTree)condition;
        boolean evaluateLeft = false;
        // if the ConditionType is a Negation, only the RightCondition is set
        if(condition.getConditionType() != ConditionType.NEGATION)
            evaluateLeft = testCondition(conditionTree.getLeftCondition());
        boolean evaluateRight = testCondition(conditionTree.getRightCondition());
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

    /** Evaluates the numerical value of a given ExpressionTree representing a mathematical term
     *
     * @param numericalExpression An ExpressionTree representing a mathematical term
     */
    private static int evaluateNumericalExpression(ExpressionTree numericalExpression) {
        if(numericalExpression.getText().matches(" *"))throw new IllegalArgumentException("A mathematical term cant be blank!");
        if(numericalExpression.getExpressionType() == ExpressionType.SIMPLE){
            return evaluateIntVariable(numericalExpression.getText());
        }
        // if the above is false the ExpressionTree must be a term with at least one operator
        int leftEvaluated = evaluateNumericalExpression(numericalExpression.getLeftNode());
        int rightEvaluated = evaluateNumericalExpression(numericalExpression.getRightNode());
        switch (numericalExpression.getExpressionType()){
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
        }
        throw new IllegalStateException("This should never be reached!");
    }

    /** Evaluates a single String value which might either be the name of a variable or a number
     *
     * @param variableString might either be the name of a variable or a number
     */
    static int evaluateIntVariable(String variableString) {
        variableString = variableString.trim();
        Matcher randomMatcher = Pattern.compile(GameConstants.RAND_INT_REGEX).matcher(variableString);
        if(randomMatcher.matches()){
            int bnd1 =evaluateNumericalExpression(ExpressionTree.expressionTreeFromString(randomMatcher.group(1)));
            int bnd2 =evaluateNumericalExpression(ExpressionTree.expressionTreeFromString(randomMatcher.group(2)));
            // my randInt method doesnt care for which number is bigger
            if(bnd2<bnd1){
                int tmpBnd = bnd1;
                bnd1 = bnd2;
                bnd2 = tmpBnd;
            }
            int bound = bnd2+1-bnd1;
            // negative bounds also work!
            int signum = 1;
            if(bound<0){
                bound = bound*-1;
                signum = -1;
            }
            return signum * (GameConstants.RANDOM.nextInt(bound)+bnd1);
        }
        if(variableString.charAt(variableString.length()-1)==';') {
            variableString = variableString.substring(0,variableString.length()-1);
            System.out.println("This should never have happened! Please report error-code 0 in CodeEvaluator!");
        }
        Variable variable =  currentStatement.getParentStatement().getVariable(variableString);
        // the variable declared within the for-Loop needs to be handled separately
        if(currentStatement.getStatementType() == StatementType.FOR) variable = ((ComplexStatement)currentStatement).getVariable(variableString);
        // the value of this variable might be a term that needs to be evaluated
        if(variable!= null)return evaluateNumericalExpression(variable.getValue());
        else return Integer.valueOf(variableString);
    }

    /** Evaluates a boolean expression represented by a ConditionLeaf Object. It may be a boolean MethodCall such as
     *  knight.targetsCell(EXIT) or contain comparisons, but it cannot contain any &&, || or !.
     */
    private static boolean evaluateBooleanExpression(ConditionLeaf conditionLeaf) {
        ExpressionTree leftTree = conditionLeaf.getLeftTree();
        ExpressionTree rightTree = conditionLeaf.getRightTree();
        // whether the ConditionLeaf represents a MethodCall such as knight.targetsCell(EXIT)
        if(conditionLeaf.getSimpleConditionType() == BooleanType.CAL){
            String objectName = leftTree.getText();
            // if the method does not have parameters it will not have 2 further ExpressionTrees
            String methodName = rightTree.getLeftNode() == null ? rightTree.getText().substring(0,rightTree.getText().length()-2) : rightTree.getLeftNode().getText();
            String parameters = rightTree.getRightNode() == null ? "" : rightTree.getRightNode().getText();
            //if(GameConstants.SHOW_BOOLEAN_METHODS)System.out.println(objectName+"."+methodName+"("+parameters+")");
            return evaluateBooleanMethodCall(objectName,methodName,parameters);
        }
        // true, false or simply a boolean variable
        if(conditionLeaf.getSimpleConditionType() == BooleanType.SIMPLE){
            return evaluateBoolVariable(leftTree.getText());
        }
        int leftEvaluated , rightEvaluated;
        // different types of comparisons (self-explanatory)
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
                boolean vt1Found = vt1 !=VariableType.VOID && vt1 != VariableType.INT;
                boolean vt2Found = vt2 !=VariableType.VOID && vt2 != VariableType.INT;
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
                vt1Found = vt1 !=VariableType.VOID && vt1 != VariableType.INT;
                vt2Found = vt2 !=VariableType.VOID && vt2 != VariableType.INT;
                if(vt1Found && vt2Found) return leftTree.getText().equals(rightTree.getText());

                leftEvaluated =  evaluateNumericalExpression(leftTree);
                rightEvaluated = evaluateNumericalExpression(rightTree);
                return leftEvaluated == rightEvaluated;
        }
        throw new IllegalArgumentException(leftTree.getText()+" is not a valid boolean! This should not have been reached though!");
    }

    /** Evaluates whether a given String is either true or false or (in case its the name of a variable)
     *  its boolean value
     *
     * @param variableString either true, false or the name of a boolean variable
     */
    private static boolean evaluateBoolVariable(String variableString) {
        if(variableString.matches(VariableType.BOOLEAN.getAllowedRegex()))return Boolean.valueOf(variableString);
        variableString = variableString.trim();
        Variable variable =  currentStatement.getParentStatement().getVariable(variableString);
        if(variable.getValue().getText().matches(VariableType.BOOLEAN.getAllowedRegex()))
            return Boolean.valueOf(variable.getValue().getText());
        else {
            Condition condition = Condition.getConditionFromString(variable.getValue().getText());
            return testCondition(condition);
        }
    }

    /** Evaluates the boolean value of a MethodCall that returns a boolean
     *
     * @param objectName either the name of a Knight or a Skeleton
     * @param methodName The name of the Method
     * @param parameterString Possible parameters
     */
    private static boolean evaluateBooleanMethodCall(String objectName, String methodName, String parameterString) {
        Variable variable = currentStatement.getParentStatement().getVariable(objectName);
        if(variable == null)throw new IllegalArgumentException("Variable "+objectName +" does not exist!");
        VariableType vType = variable.getVariableType();
        if(vType == VariableType.KNIGHT||vType == VariableType.SKELETON || vType == VariableType.ARMY){
            String[] nameList = new String[]{objectName};
            if(vType == VariableType.ARMY)nameList = variable.getValue().getRightNode().getText().split(",");
                    boolean output = true;
            for(int i = 0; i < nameList.length; i++){
                objectName = nameList[i];
            Point actorPoint = currentGameMap.getEntityPosition(objectName);
            if(actorPoint == null) continue;
            Point targetPoint = currentGameMap.getTargetPoint(objectName);
            if(targetPoint.getX()==-1)return false;
            CellContent targetContent = currentGameMap.getContentAtXY(targetPoint);
            Entity actorEntity = currentGameMap.getEntity(actorPoint);
            Entity targetEntity = currentGameMap.getEntity(targetPoint);
            switch (MethodType.getMethodTypeFromName(methodName)){
                case DROP_ITEM:
                case WAIT:
                case MOVE:
                case TURN:
                case USE_ITEM:
                case COLLECT:
                    throw new IllegalStateException("Method: \"" + methodName + "\" is not allowed here!"); //TODO: exceptions should occur in CodeParser
                case CAN_MOVE:
                    if(currentGameMap.isGateWrongDirection(actorPoint,targetPoint))output = false;
                    // ^ means XOR. Java allows this boolean operator. I currently do not!
                    boolean eitherOpenOrInverted = (currentGameMap.cellHasFlag(targetPoint, CFlag.OPEN) ^ currentGameMap.cellHasFlag(targetPoint, CFlag.INVERTED));
                    output = output && (currentGameMap.isCellFree(targetPoint) && (targetContent.isTraversable() || eitherOpenOrInverted));
                    continue;
                case HAS_ITEM:
                    if(parameterString.equals(""))output = output && (actorEntity.getItem()!= ItemType.NONE);
                    else output = output && (actorEntity.getItem()==ItemType.getValueFromName(parameterString));
                    continue;
                case TARGETS_CELL:
                    output = output && (targetContent == CellContent.getValueFromName(parameterString));
                    continue;
                case TARGET_IS_DANGER:
                    output = output && (currentGameMap.cellHasFlag(targetPoint,CFlag.PREPARING)|| currentGameMap.cellHasFlag(targetPoint,CFlag.ARMED));
                    //||!(targetEntity.getEntityType()!= EntityType.SKELETON) <- Skeletons no longer kill you when you walk into them
                    continue;
                case TARGETS_ENTITY:
                    if(parameterString.equals(""))output = output && (targetEntity.getEntityType()!= EntityType.NONE);
                    else output = output && (targetEntity.getEntityType()==EntityType.getValueFromName(parameterString));
                    continue;
                case TARGETS_ITEM:
                    if(parameterString.equals(""))output = output && (currentGameMap.getItem(targetPoint)!=ItemType.NONE);
                    else output = output && (currentGameMap.getItem(targetPoint)==ItemType.getValueFromName(parameterString));
                    continue;
                // has become pretty useless after deleting executeIf

                case IS_LOOKING:
                    output = output && ((actorEntity.getDirection() == Direction.getValueFromString(parameterString)));
                    continue;
            }

            throw new IllegalStateException("Method \"" + methodName+"("+parameterString+")\" could not be evaluated");
        }return output;}
        throw new IllegalStateException(objectName + " has wrong variable type!");
    }

}
