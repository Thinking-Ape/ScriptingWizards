package main.model;

import main.model.gamemap.Entity;
import main.model.gamemap.GameMap;
import main.model.gamemap.enums.*;
import main.model.statement.*;
import main.model.statement.Condition.*;
import main.model.statement.Expression.Expression;
import main.model.statement.Expression.ExpressionLeaf;
import main.model.statement.Expression.ExpressionTree;
import main.model.statement.Expression.ExpressionType;
import main.utility.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEvaluator {

    private Statement currentStatement;
    private VariableScope variableScope;
    private List<String> unlockedStatements;
    private boolean isPlayer;

    public CodeEvaluator(boolean isPlayer){
        variableScope = new VariableScope();
        unlockedStatements = new ArrayList<>();
        this.isPlayer = isPlayer;
    }

    private void updateUnlocks(Statement statement) {

        String unlock = "";
        List<String> unlock2 = new ArrayList<>();
        switch (statement.getStatementType()){
            case FOR:
            case WHILE:
            case IF:
            case ELSE:
                unlock2 = getUnlockedBooleanMethodsFromStatement(((ComplexStatement)statement).getCondition());
                unlock = statement.getStatementType().name().toLowerCase();
                break;
            case METHOD_CALL:
                unlock = ((MethodCall)statement).getMethodType().getName();
                break;
            case ASSIGNMENT:
            case DECLARATION:
                unlock = ((Assignment)statement).getVariable().getVariableType().getName();
                break;
            case COMPLEX:
                break;
            case SIMPLE:
                if(GameConstants.DEBUG)System.out.println("You have unlocked the following: "+statement.getCode());
                return;
        }
        if(statement.getStatementType().equals(StatementType.FOR)){
            unlockedStatements.add(VariableType.INT.getName());
        }
        unlockedStatements.add(unlock);
        unlockedStatements.addAll(unlock2);
    }

    private List<String> getUnlockedBooleanMethodsFromStatement( Condition condition) {
        List<String> output = new ArrayList<>();
        if(condition == null)return output;
        Matcher m = Pattern.compile(".*("+GameConstants.VARIABLE_NAME_REGEX+").*?").matcher(condition.getText());
        if(m.matches())
            for(int i = 1; i< m.groupCount()+1;i++){
                Variable v = variableScope.getVariable(m.group(i));
                if(v!=null && v.getVariableType() == VariableType.BOOLEAN){
                    output.addAll(getUnlockedBooleanMethodsFromStatement(Condition.getConditionFromString(v.getValue().getText())));
                }
            }
        m = Pattern.compile(".*?([a-zA-Z]+)\\(.*\\).*?").matcher(condition.getText());
        if(m.matches())
            for(int i = 1; i< m.groupCount()+1;i++){
                output.add(m.group(i));
            }
        return output;
    }

    public Statement evaluateStatement(Statement statement) {
        currentStatement = statement;
        if(currentStatement==null)return null;
        Condition condition = new ConditionLeaf(null, BooleanType.SIMPLE, null);
        if(currentStatement.isComplex()){
            condition = ((ComplexStatement)currentStatement).getCondition();
        }
        if(isPlayer)updateUnlocks(currentStatement);
        variableScope.setCurrentDepth(statement.getDepth()-1);
        switch (currentStatement.getStatementType()){
            case COMPLEX:
                break;
            case FOR:
                ForStatement forStatement = (ForStatement)currentStatement;
                if(!variableScope.containsVariable(forStatement.getDeclaration().getVariable().getName())){
                    Variable forVariable =  forStatement.getDeclaration().getVariable();
                    ExpressionLeaf value = new ExpressionLeaf(evaluateNumericalExpression(forVariable.getValue())+"");
                    variableScope.addVariable(new Variable(VariableType.INT,forVariable.getName(),value));
                }
                else{
                    Variable forVariable =  forStatement.getAssignment().getVariable();
                    ExpressionLeaf value = new ExpressionLeaf(evaluateNumericalExpression(forVariable.getValue())+"");
                    variableScope.updateVariable(new Variable(VariableType.INT,forVariable.getName(),value));
                }
                if(!testCondition(condition)){
                    variableScope.removeVariable(forStatement.getDeclaration().getVariable().getName());
                    currentStatement = GameConstants.FALSE_STATEMENT;
                }
                break;
            case WHILE:
                if(!testCondition(condition))currentStatement = GameConstants.FALSE_STATEMENT;
                break;
            case IF:
                ConditionalStatement ifStatement = (ConditionalStatement)currentStatement;
                if(!testCondition(condition)){
                    ifStatement.activateElse();
                    currentStatement = GameConstants.FALSE_STATEMENT;
                }
                else {
                    ifStatement.deactivateElse();
                }
                break;
            case ELSE:
                ConditionalStatement elseStatement = (ConditionalStatement)currentStatement;
                if(!elseStatement.isActive()){
                    currentStatement = GameConstants.FALSE_STATEMENT;
                } else if(!testCondition(condition)){
                    elseStatement.activateElse();
                    currentStatement = GameConstants.FALSE_STATEMENT;
                }
                else elseStatement.deactivateElse();
                break;
            case METHOD_CALL:
                MethodCall mC = (MethodCall)currentStatement;
                String newParameters = "";
                switch (mC.getMethodType()){
                    case HAS_ITEM:
                    case TARGETS_CELL:
                    case TARGETS_ITEM:
                    case TURN:
                    case DISPOSSESS:
                    case TARGETS_ENTITY:
                        newParameters = evaluateVariable(mC.getParameters()[0]).getText();
                        break;
                    default:
                        break;
                }
                String objectName = mC.getObjectName();
                if(variableScope.getVariable(objectName).getVariableType() == VariableType.KNIGHT)
                    while(!variableScope.getVariable(objectName).getValue().getText().matches(VariableType.KNIGHT.getAllowedRegex())){
                        objectName = variableScope.getVariable(objectName).getValue().getText();
                    }
                if(variableScope.getVariable(objectName).getVariableType() == VariableType.SKELETON)
                    while(!variableScope.getVariable(objectName).getValue().getText().matches(VariableType.SKELETON.getAllowedRegex())){
                        objectName = variableScope.getVariable(objectName).getValue().getText();
                    }
                if(variableScope.getVariable(mC.getObjectName()).getVariableType()==VariableType.ARMY){
                    objectName = evaluateVariable(objectName).getText().replaceAll(VariableType.ARMY.getAllowedRegex(), "$1");
                    if(objectName.contains(","))objectName="("+objectName+")";
                }
                currentStatement = new MethodCall(mC.getMethodType(), objectName, newParameters);
                break;
            case DECLARATION:
                Assignment declaration = (Assignment)currentStatement;
                Variable variable = declaration.getVariable();
                VariableType variableType = variable.getVariableType();
                String varNames = variable.getName();
                Expression value = declaration.getVariable().getValue();
                if(!value.getText().equals("")) {
                    if(variableType != VariableType.KNIGHT &&variableType != VariableType.SKELETON)
                        value = evaluateVariable(value.getText());
                    if (variableType == VariableType.INT) {
                        value = Expression.expressionFromString(evaluateNumericalExpression(value) + "");
                    } else if (variableType == VariableType.BOOLEAN) {
                        value = Expression.expressionFromString(testCondition(Condition.getConditionFromString(value.getText())) + "");
                    }
                }
                String valueString =value.getText();
                if(declaration.getVariable().getVariableType() == VariableType.KNIGHT){
                    Matcher knightMatcher = Pattern.compile(VariableType.KNIGHT.getAllowedRegex()).matcher(valueString);
                    if(knightMatcher.matches()){
                        String[] directionAndId = knightMatcher.group(2).split(",");
                        if(directionAndId.length == 1){
                            String direction = evaluateVariable(directionAndId[0]).getText();
                            valueString = valueString.replace(directionAndId[0],direction);
                        }
                        else {
                            String direction = evaluateVariable(directionAndId[0]).getText();
                            int id =  evaluateIntVariable(directionAndId[1]);
                            valueString = valueString.replace(directionAndId[0],direction);
                            valueString = valueString.replace(directionAndId[1],id+"");
                        }
                    }

                    currentStatement = new Assignment(varNames, variableType, Expression.expressionFromString(valueString), true);
                }
                else if(declaration.getVariable().getVariableType() == VariableType.SKELETON) {
                    Matcher skeletonMatcher = Pattern.compile(VariableType.SKELETON.getAllowedRegex()).matcher(valueString);
                    if(skeletonMatcher.matches()){
                        String[] directionAndId = skeletonMatcher.group(1).split(",");
                        if(directionAndId.length == 1){
                            String direction = evaluateVariable(directionAndId[0]).getText();
                            valueString = valueString.replace(directionAndId[0],direction);
                        }
                        else {
                            String direction = evaluateVariable(directionAndId[0]).getText();
                            int id =  evaluateIntVariable(directionAndId[1]);
                            valueString = valueString.replace(directionAndId[0],direction);
                            valueString = valueString.replace(directionAndId[1],id+"");
                        }
                    }
                    currentStatement = new Assignment(varNames, variableType, Expression.expressionFromString(valueString), true);
                }
                if(!variableScope.containsVariable(variable.getName()))variableScope.addVariable(new Variable(variableType,varNames,value));
                else
                    variableScope.updateVariable(variable);
                break;
            case ASSIGNMENT:
                Assignment assignment = (Assignment)currentStatement;
                Variable variable2 = null;

                variableType = assignment.getVariable().getVariableType();
                varNames = assignment.getVariable().getName();
                valueString =assignment.getVariable().getValue().getText();
                switch(assignment.getVariable().getVariableType()){
                    default:
                        variable2 = new Variable(variableType, varNames, evaluateVariable(assignment.getVariable().getValue().getText()));
                        break;
                    case INT:
                        variable2 = new Variable(variableType,varNames,new ExpressionLeaf(evaluateNumericalExpression(assignment.getVariable().getValue())+""));
                        break;
                    case BOOLEAN:
                        Condition condition1 = evaluateVariablesInCondition(Condition.getConditionFromString(assignment.getVariable().getValue().getText()));
                        Expression value2 = assignment.getVariable().getValue();
                        if(condition1 != null)value2 = Expression.expressionFromString(condition1.getText());
                        value2 = Expression.expressionFromString(testCondition(Condition.getConditionFromString(value2.getText()))+"");
                        variable2 = new Variable(variableType,varNames,value2);

                        break;
                    case KNIGHT:
                            Matcher knightMatcher = Pattern.compile(VariableType.KNIGHT.getAllowedRegex()).matcher(valueString);
                            if(knightMatcher.matches()){
                                String[] directionAndId = knightMatcher.group(1).split(",");
                                if(directionAndId.length == 1){
                                    String direction = evaluateVariable(directionAndId[0]).getText();
                                    valueString = valueString.replace(directionAndId[0],direction);
                                }
                                else {
                                    String direction = evaluateVariable(directionAndId[0]).getText();
                                    int id =  evaluateIntVariable(directionAndId[1]);
                                    valueString = valueString.replace(directionAndId[0],direction);
                                    valueString = valueString.replace(directionAndId[1],id+"");
                                }
                            }
//                            else {
//                                varNames = valueString;
//                            }
                            variable2 = new Variable(variableType, varNames, Expression.expressionFromString(valueString));
                            break;
                    case SKELETON:
                        Matcher skeletonMatcher = Pattern.compile(VariableType.SKELETON.getAllowedRegex()).matcher(valueString);
                        if(skeletonMatcher.matches()){
                            String[] directionAndId = skeletonMatcher.group(1).split(",");
                            if(directionAndId.length == 1){
                                String direction = evaluateVariable(directionAndId[0]).getText();
                                valueString = valueString.replace(directionAndId[0],direction);
                            }
                            else {
                                String direction = evaluateVariable(directionAndId[0]).getText();
                                int id =  evaluateIntVariable(directionAndId[1]);
                                valueString = valueString.replace(directionAndId[0],direction);
                                valueString = valueString.replace(directionAndId[1],id+"");
                            }
                        }
//                        else {
//                            varNames = valueString;
//                        }
                        variable2 = new Variable(variableType, varNames, Expression.expressionFromString(valueString));
                    case VOID:
                        break;
                }
                assert variable2 != null;
                variableScope.updateVariable(variable2);
                if(variableType == VariableType.KNIGHT ||variableType == VariableType.SKELETON ||variableType == VariableType.ARMY){
                    currentStatement = new Assignment(varNames, variableType, variable2.getValue(), false);
                }
                break;
        }
        return currentStatement;
    }

    private Condition evaluateVariablesInCondition(Condition conditionFromString) {

        if(!conditionFromString.isLeaf()){
            Condition leftTree = null;
            Condition rightTree = null;
            ConditionTree tree = (ConditionTree)conditionFromString;
            if(tree.getLeftCondition()!= null)leftTree = evaluateVariablesInCondition(tree.getLeftCondition());
            if(tree.getRightCondition()!= null)rightTree = evaluateVariablesInCondition(tree.getRightCondition());
            return new ConditionTree(leftTree, conditionFromString.getConditionType(), rightTree);
        }
        else {
            Expression leftTree = null;
            Expression rightTree = null;
            ConditionLeaf leaf = (ConditionLeaf) conditionFromString;
            if(leaf.getLeftExpression()!= null){
                if(leaf.getSimpleConditionType() == BooleanType.CAL)leftTree = leaf.getLeftExpression();
                else leftTree = evaluateVariablesInExpressionTree(leaf.getLeftExpression());
            }
            if(leaf.getRightExpression()!= null)rightTree = evaluateVariablesInExpressionTree(leaf.getRightExpression());
            return new ConditionLeaf(leftTree, leaf.getSimpleConditionType(), rightTree);

        }
    }

    private Expression evaluateVariablesInExpressionTree(Expression expression) {
        if(expression.isLeaf()){
            return evaluateVariable(expression.getText());
        }
        else{
            ExpressionTree tree = (ExpressionTree)expression;
            Expression leftTree = tree.getLeftNode();
            Expression rightTree = tree.getRightNode();
            if(tree.getLeftNode() != null){
                leftTree = evaluateVariablesInExpressionTree(leftTree);
            }

            if(tree.getRightNode() != null){
                rightTree = evaluateVariablesInExpressionTree(rightTree);
            }
            return new ExpressionTree(leftTree,tree.getExpressionType(),rightTree);
        }
    }

    private Expression evaluateVariable(String value) {
        if(variableScope.containsVariable(value))
            return evaluateVariable(variableScope.getVariable(value).getValue().getText());
        else return new ExpressionLeaf(value);
    }

    /** Evaluates whether a given Condition Object should represent a true or false boolean in regards to
     *  the current map
     */
    private boolean testCondition(Condition condition) {
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
    private int evaluateNumericalExpression(Expression numericalExpression) {
        if(numericalExpression.getText().matches(" *"))throw new IllegalArgumentException("A mathematical term cant be blank!");
        if(numericalExpression.getExpressionType() == ExpressionType.SIMPLE){
            return evaluateIntVariable(numericalExpression.getText());
        }
        // if the above is false the ExpressionTree must be a term with at least one operator
        int leftEvaluated = evaluateNumericalExpression(((ExpressionTree)numericalExpression).getLeftNode());
        int rightEvaluated = evaluateNumericalExpression(((ExpressionTree)numericalExpression).getRightNode());
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
    private int evaluateIntVariable(String variableString) {
        variableString = variableString.trim();
        Matcher randomMatcher = Pattern.compile(GameConstants.RAND_INT_REGEX).matcher(variableString);
        if(randomMatcher.matches()){
            int bnd1 =evaluateNumericalExpression(Expression.expressionFromString(randomMatcher.group(1)));
            int bnd2 =evaluateNumericalExpression(Expression.expressionFromString(randomMatcher.group(2)));
            // my randInt method doesnt care for which number is bigger

            int output =  Util.getRandIntWithout(bnd1, bnd2, new ArrayList<>());
            if(!isPlayer && (int)ModelInformer.getDataFromCurrentLevel(LevelDataType.AMOUNT_OF_RERUNS)>1){
                output =  ModelInformer.getDifferentRandomNumberEachTime(bnd1,bnd2);
            }
            return output;
        }
        if(variableString.charAt(variableString.length()-1)==';') {
            variableString = variableString.substring(0,variableString.length()-1);
            System.out.println("This should never have happened! Please report error-code 0 in CodeEvaluator!");
        }
        Variable variable =  variableScope.getVariable(variableString);
        // the variable declared within the for-Loop needs to be handled separately
        if(currentStatement.getStatementType() == StatementType.FOR) variable = variableScope.getVariable(variableString);
        // the value of this variable might be a term that needs to be evaluated
        if(variable!= null)return evaluateNumericalExpression(variable.getValue());
        else return Integer.valueOf(variableString);
    }

    /** Evaluates a boolean expression represented by a ConditionLeaf Object. It may be a boolean MethodCall such as
     *  knight.targetsCell(EXIT) or contain comparisons, but it cannot contain any &&, || or !.
     */
    private boolean evaluateBooleanExpression(ConditionLeaf conditionLeaf) {
        Expression leftTree = conditionLeaf.getLeftExpression();
        Expression rightTree = conditionLeaf.getRightExpression();
        // whether the ConditionLeaf represents a MethodCall such as knight.targetsCell(EXIT)
        if(conditionLeaf.getSimpleConditionType() == BooleanType.CAL){
            String objectName = leftTree.getText();
            if(rightTree.isLeaf())throw new IllegalArgumentException("A method call can't be an ExpressionLeaf!");
            ExpressionTree tree = (ExpressionTree)rightTree;
            // if the method does not have parameters it will not have 2 further ExpressionTrees
            String methodName = tree.getLeftNode() == null ? tree.getText().substring(0,rightTree.getText().length()-2) : tree.getLeftNode().getText();
            String parameters = tree.getRightNode() == null ? "" : tree.getRightNode().getText();
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
                Variable var1 = variableScope.getVariable(leftTree.getText());
                Variable var2 = variableScope.getVariable(rightTree.getText());
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
                var1 = variableScope.getVariable(leftTree.getText());
                var2 = variableScope.getVariable(rightTree.getText());
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
    private boolean evaluateBoolVariable(String variableString) {
        if(variableString.matches(VariableType.BOOLEAN.getAllowedRegex()))return Boolean.valueOf(variableString);
        variableString = variableString.trim();
        Variable variable =  variableScope.getVariable(variableString);
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
    private boolean evaluateBooleanMethodCall(String objectName, String methodName, String parameterString) {
        GameMap currentGameMap = ModelInformer.getCurrentMapCopy();
        Variable variable = variableScope.getVariable(objectName);
        if(variable == null)throw new IllegalArgumentException("Variable "+objectName +" does not exist!");
        VariableType vType = variable.getVariableType();
        if(vType == VariableType.KNIGHT||vType == VariableType.SKELETON || vType == VariableType.ARMY){
            String[] nameList = new String[]{objectName};
            if(vType == VariableType.ARMY)nameList = variable.getValue().getText().replaceAll(VariableType.ARMY.getAllowedRegex(), "$1").split(",");
            boolean output = true;
            int deadEntities = 0;
            for(int i = 0; i < nameList.length; i++){
                objectName = nameList[i];
                Point actorPoint = currentGameMap.getEntityPosition(objectName);
                if(actorPoint == null|| actorPoint.getX() == -1){
                    if(MethodType.getMethodTypeFromName(methodName) == MethodType.IS_DEAD)continue;
                    deadEntities++;
                    if(deadEntities == nameList.length){
                        return false;
                    }
                    continue;
                }
                Point targetPoint = currentGameMap.getTargetPoint(objectName);
                MethodType mT = MethodType.getMethodTypeFromName(methodName);
                if(targetPoint.getX()==-1)return mT == MethodType.IS_DEAD;
                CellContent targetContent = currentGameMap.getContentAtXY(targetPoint);
                Entity actorEntity = currentGameMap.getEntity(actorPoint);
                Entity targetEntity = currentGameMap.getEntity(targetPoint);
                switch (mT){
                    case DROP_ITEM:
                    case WAIT:
                    case MOVE:
                    case TURN:
                    case USE_ITEM:
                    case COLLECT:
                    case BACK_OFF:
                    case ATTACK:
                    case DISPOSSESS:
                        throw new IllegalStateException("Method: \"" + methodName + "\" is not allowed here!");
                    case IS_ALIVE:

                    case IS_DEAD:
                        continue;
                    case IS_SPECIALIZED:
                        output = output &&(actorEntity.isSpecialized());
                        continue;
                    case IS_POSSESSED:
                        output = output &&(actorEntity.isPossessed());
                        continue;
                    case CAN_MOVE:
                        if(currentGameMap.isGateWrongDirection(actorPoint,targetPoint))output = false;
                        // ^ means XOR. Java allows this boolean operator. I currently do not ingame!
                        boolean eitherOpenOrInverted = (currentGameMap.cellHasFlag(targetPoint, CellFlag.OPEN) ^ currentGameMap.cellHasFlag(targetPoint, CellFlag.INVERTED));
                        output = output && (currentGameMap.isCellFree(targetPoint) && (targetContent.isTraversable() || eitherOpenOrInverted));
                        continue;
                    case HAS_ITEM:
                        if(parameterString.equals(""))output = output && (actorEntity.getItem()!= ItemType.NONE);
                        else output = output && (actorEntity.getItem()==ItemType.getValueFromName(parameterString));
                        continue;
                    case TARGETS_CELL:
                        output = output && (targetContent == CellContent.getValueFromName(parameterString));
                        continue;
                    case TARGETS_DANGER:
                        output = output && (currentGameMap.cellHasFlag(targetPoint, CellFlag.PREPARING)|| currentGameMap.cellHasFlag(targetPoint, CellFlag.ARMED));
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
            }
            return output;
        }
        throw new IllegalStateException(objectName + " has wrong variable type!");
    }
    public List<String> getUnlockedStatements(){
        return new ArrayList<>(unlockedStatements);
    }

    public Variable getVariableCopyWithName(String varName){
        Variable actualVar = variableScope.getVariable(varName);
        if(actualVar == null)return null;
        return new Variable(actualVar);
    }
}
