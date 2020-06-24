package main.parser;

import main.exception.*;
import main.model.VariableScope;
import main.model.gamemap.enums.CellContent;
import main.model.gamemap.enums.Direction;
import main.model.gamemap.enums.EntityType;
import main.model.gamemap.enums.ItemType;
import main.model.statement.MethodType;
import main.utility.*;
import main.model.statement.*;
import main.model.statement.Condition.Condition;
import main.model.statement.Condition.ConditionLeaf;
import main.model.statement.Condition.ConditionTree;
import main.model.statement.Expression.Expression;
import main.model.statement.Expression.ExpressionTree;
import main.model.statement.Expression.ExpressionType;
import main.model.GameConstants;
import main.view.CodeAreaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.model.statement.Condition.ConditionType.NEGATION;
import static main.model.statement.Condition.ConditionType.SINGLE;

public abstract class CodeParser {

    private static Map<Integer,ComplexStatement> depthStatementMap;
    private static CodeAreaType codeAreaType;
    private static Statement lastStatement;
    private static Variable currentForVariable;
    private static VariableScope variableScope;

    public static ComplexStatement parseProgramCode(List<String> codeLines) {
        return parseProgramCode(codeLines, CodeAreaType.PLAYER);
    }

    /** Will try to convert the first given Parameter into a ComplexStatement consisting of multiple Substatements
     *  (Statements) representing the respective line of code
     *
     * @param lines The given lines of code
     * @param codeAreaType whether it is player code or code of the enemy. the first cannot spawn Skeletons, the latter
     *                     cant spawn Knights
     */
    public static ComplexStatement parseProgramCode(List<String> lines, CodeAreaType codeAreaType){
        variableScope = new VariableScope();
        List<String> codeLines = lines;
        CodeParser.codeAreaType = codeAreaType;
        ComplexStatement behaviour = new ComplexStatement();
        int depth = 1;
        depthStatementMap = new HashMap<>();
        depthStatementMap.put(0, behaviour);
        for ( String code : codeLines){
            if(Util.textIsTooLongForCodefield(code,depth))throw new IllegalArgumentException("This codeLine is too long: "+code);
            // empty lines are added but ignored when evaluating how many lines of code where used to complete the level
            if(code.matches(" *")){
                depthStatementMap.get(depth - 1).addSubStatement(new SimpleStatement());
                continue;
            }
            currentForVariable = null;
            code = code.trim();
            code = Util.removeUnnecessarySpace(code);
            // code lines representing a closing bracket ('}') are ignored as they don't hold any relevant information
            if(code.equals("}")){
                depth--;
                variableScope.setCurrentDepth(depth-1);
                continue;
            }
            // Parse the current Statement
            Statement statement = parseString(code);

            if(statement ==null )throw new IllegalArgumentException("Unknown statement: \""+code+"\"!");
            if (statement.isComplex()){
                if(statement.getStatementType() == StatementType.ELSE){
                    if(!testIfElseCanStandHere(depth)) throw new IllegalArgumentException("Else cannot stand here");
                    ((ConditionalStatement)depthStatementMap.get(depth)).setElseStatement(((ConditionalStatement)statement));
                }
                if (depthStatementMap.containsKey(depth))
                    depthStatementMap.replace(depth, (ComplexStatement) statement);
                else
                    depthStatementMap.put(depth, (ComplexStatement) statement);

                depthStatementMap.get(depth - 1).addSubStatement(statement);
                if(depth > GameConstants.MAX_STATEMENT_DEPTH)throw new IllegalStateException("You are not allowed to have a greater depth than "+ GameConstants.MAX_STATEMENT_DEPTH +"!");
                depth++;
                variableScope.setCurrentDepth(depth-1);
                if(statement.getStatementType() == StatementType.FOR){
                    variableScope.addVariable(new Variable(((ForStatement)statement).getDeclaration().getVariable()));
                }
            }
            else {
                depthStatementMap.get(depth - 1).addSubStatement(statement);
                if(statement.getStatementType() == StatementType.DECLARATION){
                    Variable variable = ((Assignment)statement).getVariable();
                    variableScope.addVariable(new Variable(variable));
                }
                else if(statement.getStatementType() == StatementType.ASSIGNMENT){
                    Variable variable = ((Assignment)statement).getVariable();
                    if(variableScope.getVariable(variable.getName()).getValue().getText().equals(""))variableScope.updateVariable(new Variable(variable));
                }
            }
            lastStatement = statement;
        }
        if(depth != 1)throw new UnbalancedAmountOfBracketsException();
        return behaviour;
    }

    /** Tests whether an else-Statement is allowed at this position and returns the respective boolean
     *
     * @param depth the depth of the current else-Statement
     */
    private static boolean testIfElseCanStandHere(int depth) {
        // will return false, if the depthStatementMap has not added a Statement at that depth yet (if there was an if
        // before that would be the case!
        if(!depthStatementMap.containsKey(depth)) return false;
        boolean noIfBefore = depthStatementMap.get(depth).getStatementType()!=StatementType.IF;
        boolean noElseBefore = depthStatementMap.get(depth).getStatementType()!=StatementType.ELSE;
        boolean lastStatementIllegal = (depth == lastStatement.getDepth() && !lastStatement.isComplex());
        if((noIfBefore && noElseBefore)|| lastStatementIllegal) return false;
        final boolean conditionSet = depthStatementMap.get(depth).getCondition() != null;
        return conditionSet;
    }

    /** Will try to turn the given code into a Statement
     */
    private static Statement parseString(String code) {
        for(StatementType statementType : StatementType.values()){
            Matcher matcher = Pattern.compile(statementType.getRegex()).matcher(code);
            if(!matcher.matches()){
                continue;
            }
            switch (statementType){
                case WHILE:
                        String conditionString = matcher.group(1);
                        Condition condition = Condition.getConditionFromString(conditionString);
                        if(condition == null)throw new IllegalArgumentException("Couldnt parse Condition from input " + conditionString);
                        checkConditionForUnknownVars(condition);
                        return new WhileStatement(condition);
                case FOR:
                    String forInnerString = matcher.group(1);
                    Matcher innerForMatcher = Pattern.compile(GameConstants.FOR_INNER_REGEX).matcher(forInnerString);
                    if(innerForMatcher.matches()){
                        String declarationString = innerForMatcher.group(1);
                        conditionString = innerForMatcher.group(2);
                        String assignmentString = innerForMatcher.group(3);
                        Matcher declarationMatcher = Pattern.compile(StatementType.DECLARATION.getRegex()).matcher(declarationString+";");
                        Matcher assignmentMatcher = Pattern.compile(StatementType.ASSIGNMENT.getRegex()).matcher(assignmentString+";");
                        if(!declarationMatcher.matches()) throw new IllegalArgumentException("String: "+declarationString+" should be a declaration!");
                        if(!assignmentMatcher.matches()) throw new IllegalArgumentException("String: "+assignmentString+" should be an assignment!");

                        Assignment declaration = parseDeclaration(declarationMatcher.group(1),declarationMatcher.group(2),declarationMatcher.group(4));
                        if(declaration == null) throw new IllegalArgumentException("Couldnt parse Declaration: "+declarationString);
                        variableScope.addVariable(new Variable(declaration.getVariable()));
                        Assignment assignment = parseAssignment(assignmentMatcher.group(1),assignmentMatcher.group(2),assignmentMatcher.group(3));
                        condition =Condition.getConditionFromString(conditionString);
                        if(condition == null)throw new IllegalArgumentException("Couldnt parse Condition from input " + conditionString);
                        checkConditionForUnknownVars(condition);
                        if(assignment == null) throw new IllegalArgumentException("Couldnt parse Assignment: "+assignmentString);
                        if(declaration.getVariable().getVariableType()!=VariableType.INT)throw new IllegalArgumentException("Declaration "+declarationString+" must be an int Variable!");
                        variableScope.removeVariable(declaration.getVariable().getName());
                        return new ForStatement(declaration,condition,assignment);
                    }
                    else throw new IllegalArgumentException("Illegal input for For-Statement: "+forInnerString);
                case IF:
                    conditionString = matcher.group(1);
                    condition = Condition.getConditionFromString(conditionString);
                    if(condition == null)throw new IllegalArgumentException("Couldnt parse Condition from input " + conditionString);
                    checkConditionForUnknownVars(condition);
                    return new ConditionalStatement(condition,false);
                case ELSE:
                    conditionString = matcher.group(2);
                    if(conditionString == null)return new ConditionalStatement(null, true);
                    condition = Condition.getConditionFromString(conditionString);
                    if(condition == null)throw new IllegalArgumentException("Couldnt parse Condition from input " + conditionString);
                    checkConditionForUnknownVars(condition);
                    return new ConditionalStatement(condition,true);
                case METHOD_CALL:
                    String objectName = matcher.group(2);
                    String methodName = matcher.group(3);
                    String parameters = matcher.group(4);
                    MethodCall methodCall = parseMethodCall(objectName, methodName, parameters);
                    return methodCall;
                case DECLARATION:
                    String varName = matcher.group(2);
                    String varType = matcher.group(1);
                    String value = matcher.group(4);
                    Assignment declaration = parseDeclaration(varType,varName,value);
                    if(declaration == null)throw new IllegalArgumentException("Couldnt parse Assignment from input " + code);
                    return declaration;
                case ASSIGNMENT:
                    varName = matcher.group(1);
                    String operation = matcher.group(2);
                    value = matcher.group(3);
                    Assignment assignment = parseAssignment(varName,operation,value);
                    if(assignment == null)throw new IllegalArgumentException("Couldnt parse Assignment from input " + code);
                    return assignment;
            }
        }
        char lastChar = code.charAt(code.length()-1);
        if(lastChar!=';' && lastChar!='{'){
            Statement tempStatement2 = parseString(code + "{");
            if(tempStatement2 != null) throw new NoCurlyBracketException();
            Statement tempStatement1 =  parseString(code+";");
            if(tempStatement1 != null) throw new NoSemicolonException();
        }
        if(code.matches("^ *[a-zA-Z]+\\(.*\\) *; *$"))throw new IllegalArgumentException("Your MethodCall might need an object!");
        return null;
    }

    /** Will try to convert the given code into a MethodCall Object
     */
    private static MethodCall parseMethodCall(String objectName, String methodName, String parameterString) {
        if(objectName == null)throw new IllegalArgumentException("You're lacking an object for your Method!");
        if(objectName.matches(" *"))throw new IllegalArgumentException("You cant have an empty object!");
        boolean isPlayerCode = codeAreaType != CodeAreaType.AI;
        if(variableScope.getVariable(objectName) == null)throw new NotInScopeException(objectName,variableScope);
        if(variableScope.getVariable(objectName).getValue().getText().equals(""))throw new IllegalArgumentException("Variable "+ objectName+" has not been initialized");
        if(variableScope.getVariable(objectName).getVariableType() != VariableType.ARMY){
            if(isPlayerCode && variableScope.getVariable(objectName).getVariableType() != VariableType.KNIGHT)throw new IllegalArgumentException("Object "+objectName+ " must be a Knight");
            if(!isPlayerCode && variableScope.getVariable(objectName).getVariableType() != VariableType.SKELETON)throw new IllegalArgumentException("Object "+objectName+ " must be a Skeleton");
        }
        MethodType mType = MethodType.getMethodTypeFromName(methodName);
        if(mType == null) throw new MethodUnknownException(methodName);
        testForCorrectParameters(parameterString, mType);
        // Makes lines such as: knight.targetsCell(EXIT); illegal (they need the condition context!)
        if(mType.getOutputType() != VariableType.VOID) throw new IllegalArgumentException("Method " + methodName + " cannot stand here!");

        if(isPlayerCode && mType == MethodType.ATTACK) throw new IllegalArgumentException("Knights cannot attack! Collect a sword and refer to useItem()!");
        return new MethodCall(mType,objectName,parameterString);
    }

/** Tests whether the given parameters are correct for the given MethodType. Possible MethodTypes are all Knight
     *  and Skeleton Methods, but not randInt(int i, int j)!
     */
    private static void testForCorrectParameters(String parameters, MethodType mType) {
        switch (mType){
            case ATTACK:
            case TARGETS_DANGER:
            case WAIT:
            case DROP_ITEM:
            case MOVE:
            case BACK_OFF:
            case USE_ITEM:
            case COLLECT:
            case CAN_MOVE:
            case IS_ALIVE:
                if(!parameters.equals(""))throw new IllegalParameterException(mType,parameters);
                return;
            case HAS_ITEM:
                ItemType item = ItemType.getValueFromName(parameters);
                if(parameters.equals("NONE"))return;
                if(item != ItemType.NONE ||parameters.equals("")){
                    return;
                }
                break;
            case TARGETS_CELL:
                CellContent content = CellContent.getValueFromName(parameters);
                if(content != null){
                    return;
                }
                break;
            case TARGETS_ENTITY:
                EntityType entityType = EntityType.getValueFromName(parameters);
                if(parameters.equals("NONE"))return;
                if(entityType != EntityType.NONE|| parameters.equals("")){
                    return;
                }
                break;
            case TARGETS_ITEM:
                item = ItemType.getValueFromName(parameters);
                if(parameters.equals("NONE"))return;
                if(item != ItemType.NONE || parameters.equals("")){
                    return;
                }
                break;
            case TURN:
                if(parameters.matches(VariableType.TURN_DIRECTION.getAllowedRegex())||
                        (variableScope.getVariable(parameters) != null &&
                                variableScope.getVariable(parameters).getVariableType() == VariableType.TURN_DIRECTION &&
                                    !variableScope.getVariable(parameters).getValue().getText().equals(""))){
                    return;
                }
                break;

            case IS_LOOKING:
                if(parameters.matches(VariableType.DIRECTION.getAllowedRegex())||
                        (variableScope.getVariable(parameters)!=null &&
                                variableScope.getVariable(parameters).getVariableType()==VariableType.DIRECTION&&
                                    !variableScope.getVariable(parameters).getValue().getText().equals(""))){
                    return;
                }
                break;
        }
        throw new IllegalParameterException(mType,parameters);
    }


    private static Assignment parseAssignment(String varName, String operation, String valueString) {
        if(!varName.matches(GameConstants.WHOLE_VARIABLE_NAME_REGEX))throw new IllegalVariableNameException(varName);
        if(operation.equals("++"))valueString = varName+"+1";
        if(operation.equals("--"))valueString = varName+"-1";
        valueString = valueString.trim();
        Expression valueTree = Expression.expressionFromString(valueString);
        if(variableScope.getVariable(varName)==null)throw new NotInScopeException(varName,variableScope);
        VariableType variableType = variableScope.getVariable(varName).getVariableType();
        if(variableType == VariableType.BOOLEAN && !valueString.equals("")){
            Condition condition = Condition.getConditionFromString(valueString);
            checkConditionForUnknownVars(condition );
        }
        else if(!valueString.equals(""))checkExpressionTreeForUnknownVars(valueTree);

        testForCorrectValueType(variableType,valueString);
        if(valueString.matches(" *"))throw new IllegalArgumentException("You cannot assign an empty value!");

        Assignment assignment = new Assignment(varName.trim(),variableType,valueTree,false);
        return assignment;
    }

    private static Assignment parseDeclaration(String variableTypeString, String varName, String valueString) {
        if(valueString == null)valueString = "";
        if(!varName.matches(GameConstants.WHOLE_VARIABLE_NAME_REGEX))throw new IllegalVariableNameException(varName);
        valueString = valueString.trim();
        Expression valueTree = Expression.expressionFromString(valueString);

        boolean valueNotEmpty = !valueString.equals("");
        VariableType variableType = VariableType.getVariableTypeFromString(variableTypeString);
        if(variableType == VariableType.VOID)throw new IllegalArgumentException("Variable type void is not allowed!");
        if(variableType == VariableType.CELL_CONTENT || variableType == VariableType.ITEM_TYPE || variableType == VariableType.ENTITY_TYPE)
            throw new IllegalArgumentException("Variable type "+ variableType +" is not implemented yet!");
        if(variableType == VariableType.BOOLEAN && valueNotEmpty){
            Condition condition = Condition.getConditionFromString(valueString);
            checkConditionForUnknownVars(condition );
        }
        else if(valueNotEmpty) checkExpressionTreeForUnknownVars(valueTree);
        if(valueNotEmpty) testForCorrectValueType(variableType,valueString);
        Variable variable = new Variable(variableType, varName, valueTree);
        boolean isSkeleton = variable.getVariableType() == VariableType.SKELETON;
        boolean isPlayerCode = codeAreaType != CodeAreaType.AI;
        if(isPlayerCode && (isSkeleton ))throw new StatementNotAllowedException();
        boolean isKnight = variable.getVariableType() == VariableType.KNIGHT;
        if(!isPlayerCode && isKnight )throw new StatementNotAllowedException();
        Assignment declaration = new Assignment(varName.trim(),variableType,valueTree,true);
        return declaration;
    }

   private static void testForCorrectValueType(VariableType variableType, String value) {
        value = value.trim();
        if(value.equals(""))throw new VariableNotInitializedException();
        Variable v = variableScope.getVariable(value);
        if(v!=null){
            if(v.getVariableType() != variableType)throw new IllegalArgumentException(value + " has the wrong type!");
            testForCorrectValueType(variableType, variableScope.getVariable(value).getValue().getText());
            return;
        }
        switch (variableType){
            case INT:
                if(value.matches("[+-]?\\d+"))return;
                Expression expression = Expression.expressionFromString(value);

                if(!expression.isLeaf()){
                    ExpressionTree tree = (ExpressionTree)expression;
                    if(value.matches(GameConstants.RAND_INT_REGEX)){
                        String lowerBound = tree.getRightNode().getText().split(",")[0];
                        String upperBound = tree.getRightNode().getText().split(",")[1];
                        testForCorrectValueType(VariableType.INT,lowerBound);
                        testForCorrectValueType(VariableType.INT,upperBound);
                    }
                    else {
                        testForCorrectValueType(VariableType.INT,tree.getLeftNode().getText());
                        testForCorrectValueType(VariableType.INT,tree.getRightNode().getText());
                    }
                }
                else if(!value.matches(GameConstants.RAND_INT_REGEX)&&!value.matches(variableType.getAllowedRegex())){
                    v = currentForVariable;
                    if(v!=null){
                        if(v.getVariableType() != variableType)throw new IllegalArgumentException(value + " has the wrong type!");
                        testForCorrectValueType(variableType, v.getValue().getText());
                        return;
                    }
                    throw new IllegalArgumentException(value + " is no number!");
                }
                else throw new IllegalArgumentException(value + " is no number!");
            case BOOLEAN:
                if(value.matches(VariableType.BOOLEAN.getAllowedRegex()))return;
                Condition condition = Condition.getConditionFromString(value);
                if(condition.isLeaf()){
                    ConditionLeaf conditionLeaf = (ConditionLeaf)condition;
                    switch (conditionLeaf.getSimpleConditionType()){
                        case SIMPLE:
                            return;
                        case GR_EQ:
                        case LE_EQ:
                        case GR:
                        case LE:
                            Expression leftTree = conditionLeaf.getLeftExpression();
                            Expression rightTree = conditionLeaf.getRightExpression();
                            testForCorrectValueType(VariableType.INT,leftTree.getText());
                            testForCorrectValueType(VariableType.INT,rightTree.getText());
                            return;
                        case NEQ:
                        case EQ:
                            leftTree = conditionLeaf.getLeftExpression();
                            rightTree = conditionLeaf.getRightExpression();
                            switch (leftTree.getExpressionType()){
                                case ADD:
                                case SUB:
                                case DIV:
                                case MULT:
                                case MOD:
                                    testForCorrectValueType(VariableType.INT,leftTree.getText());
                                    testForCorrectValueType(VariableType.INT,rightTree.getText());
                                    return;
                                case SIMPLE:
                                    v = variableScope.getVariable(leftTree.getText());
                                    VariableType vTypeLeft = VariableType.getVariableTypeFromValue(leftTree.getText());
                                    VariableType vTypeRight = VariableType.getVariableTypeFromValue(rightTree.getText());
                                    if(v!=null){
                                        testForCorrectValueType(v.getVariableType(),v.getValue().getText());
                                        vTypeLeft = VariableType.getVariableTypeFromValue(v.getValue().getText());
                                    }
                                    v = variableScope.getVariable(rightTree.getText());
                                    if(v!=null){
                                        testForCorrectValueType(v.getVariableType(),v.getValue().getText());
                                        vTypeRight = VariableType.getVariableTypeFromValue(v.getValue().getText());
                                    }
                                    if(vTypeLeft == vTypeRight) return;
                                    break;
                            }
                            break;
                        case CAL:
                            MethodType mt = MethodType.getMethodTypeFromCall(conditionLeaf.getRightExpression().getText());
                            if(mt== null){
                                if(MethodType.getMethodTypeFromCall(conditionLeaf.getRightExpression().getText()+"()")!=null)
                                    throw new IllegalArgumentException("You might have forgotten brackets: " + conditionLeaf.getText());
                                else throw new MethodUnknownException(conditionLeaf.getRightExpression().getText());
                            }
                            if(mt.getOutputType()== VariableType.BOOLEAN){
                                Expression expression1 = conditionLeaf.getRightExpression();
                                if(expression1.isLeaf())throw new IllegalArgumentException("This MethodCall is invalid!");

                                testForCorrectParameters(((ExpressionTree)expression1).getRightNode().getText(), mt);
                                return;
                            }
                    }
                }
                return;
            case KNIGHT:
                Expression expression1 = Expression.expressionFromString(value);
                if(!value.matches(variableType.getAllowedRegex())){
                    if(!expression1.isLeaf()) {
                        ExpressionTree tree = (ExpressionTree)expression1;
                        if (variableScope.getVariable(tree.getRightNode().getText()) != null && variableScope.getVariable(tree.getRightNode().getText()).getVariableType() == VariableType.DIRECTION)
                            return;
                    }
                    throw new IllegalArgumentException(value + " is not a valid Knight constructor!");
                }
                if(!expression1.isLeaf()) {
                    ExpressionTree tree = (ExpressionTree)expression1;
                if(!tree.getRightNode().getText().equals("")){
                    testForCorrectValueType(VariableType.DIRECTION,tree.getRightNode().getText());
                }}
                break;
            case SKELETON:
                if(!value.matches(variableType.getAllowedRegex())){
                    expression1 = Expression.expressionFromString(value);
                    if(!expression1.isLeaf()) {
                        ExpressionTree tree = (ExpressionTree)expression1;
                        boolean varDirValid = variableScope.getVariable(tree.getRightNode().getText()) != null && variableScope.getVariable(tree.getRightNode().getText()).getVariableType() == VariableType.DIRECTION;
                        boolean dirValid = tree.getRightNode().getText().matches(VariableType.DIRECTION.getAllowedRegex());
                        if(varDirValid || dirValid)return;
                        // 2 Parameters
                        if(!tree.getRightNode().isLeaf()){
                            ExpressionTree tree2 = (ExpressionTree)tree.getRightNode();
                            varDirValid = variableScope.getVariable(tree2.getLeftNode().getText()) != null && variableScope.getVariable(tree2.getLeftNode().getText()).getVariableType() == VariableType.DIRECTION;
                            dirValid = tree2.getLeftNode().getText().matches(VariableType.DIRECTION.getAllowedRegex());
                            boolean varIntValid = variableScope.getVariable(tree2.getRightNode().getText()) != null && variableScope.getVariable(tree2.getRightNode().getText()).getVariableType() == VariableType.INT;
                            boolean intValid = tree2.getLeftNode().getText().matches(VariableType.INT.getAllowedRegex());
                            if((varDirValid || dirValid) && (varIntValid || intValid))return;
                        }
                    }
                    throw new IllegalArgumentException(value + " is not a valid Skeleton constructor!");
                }

                expression1 = Expression.expressionFromString(value);
                if(!expression1.isLeaf()){
                    ExpressionTree expressionTree = (ExpressionTree)expression1;
                if(!expressionTree.getRightNode().getText().equals("")){
                    if(!expressionTree.getRightNode().isLeaf()){
                        ExpressionTree rightTree = (ExpressionTree)expressionTree.getRightNode();
                        testForCorrectValueType(VariableType.DIRECTION,rightTree.getLeftNode().getText());
                        testForCorrectValueType(VariableType.INT,rightTree.getRightNode().getText());
                    }
                    else testForCorrectValueType(VariableType.DIRECTION,expressionTree.getRightNode().getText());
                }}
                break;
            case DIRECTION:
                Direction dir = Direction.getValueFromString(value);
                if(dir == null)throw new IllegalParameterException(variableType,value);
                else return;
            case TURN_DIRECTION:
                if(!value.matches(variableType.getAllowedRegex()))throw new IllegalParameterException(variableType,value);
                else return;
            case CELL_CONTENT:
                CellContent content = CellContent.getValueFromName(value);
                if(content == null)throw new IllegalParameterException(variableType,value);
                else return;
            case ITEM_TYPE:
                ItemType itemType = ItemType.getValueFromName(value);
                if(itemType == ItemType.NONE)throw new IllegalParameterException(variableType,value);
                else return;
            case ENTITY_TYPE:
                EntityType entityType = EntityType.getValueFromName(value);
                if(entityType == EntityType.NONE)throw new IllegalParameterException(variableType,value);
                else return;
            case ARMY:
                if(!value.matches(variableType.getAllowedRegex())){
                    Expression exp = Expression.expressionFromString(value);
                    if(exp.isLeaf())throw new IllegalArgumentException("Your Army needs parameters!");
                    ExpressionTree tree = (ExpressionTree)exp;
                    if(tree.getRightNode() == null)throw new IllegalArgumentException("Your Army needs parameters!");
                    String[] parameters =tree.getRightNode().getText().split(",");
                    if(checkForDoppelgangers(parameters))throw  new IllegalArgumentException("You shall not add the same Entity more than once!");
                    for(String parameter : parameters){

                    if(variableScope.getVariable(parameter) == null)
                        throw new IllegalArgumentException(parameter + " is not a valid variable!");
                    else {
                        boolean isPlayerCode = codeAreaType != CodeAreaType.AI;
                        boolean isKnight = variableScope.getVariable(parameter).getVariableType() == VariableType.KNIGHT && isPlayerCode;
                        boolean isSkeleton = variableScope.getVariable(parameter).getVariableType() == VariableType.SKELETON && !isPlayerCode;
                        if(isKnight || isSkeleton)continue;
                    }
                    throw new IllegalArgumentException(value + " is not a valid Army constructor!");}
                }
                break;
            case VOID:
                break;
        }
    }

    private static boolean checkForDoppelgangers(String[] parameters) {
        List<String> checkedList = new ArrayList<>();
        for(String p : parameters){
            if(checkedList.contains(p))return true;
            checkedList.add(p);
        }
        return false;
    }

    private static void checkConditionForUnknownVars(Condition condition) {
        if(condition == null||condition.getText().equals(""))throw new IllegalArgumentException("You cannot have an empty Condition!");

        testForCorrectValueType(VariableType.BOOLEAN,condition.getText());
        if(condition.getText().matches(" *"))return;
        if(condition.getConditionType() != SINGLE){
            checkConditionForUnknownVars(((ConditionTree)condition).getRightCondition());
            if(condition.getConditionType()!=NEGATION)checkConditionForUnknownVars(((ConditionTree)condition).getLeftCondition());
            return;
        }
        else{
            if(condition.getText().matches(VariableType.BOOLEAN.getAllowedRegex()))return;
            ConditionLeaf conditionLeaf = (ConditionLeaf)condition;
            switch(conditionLeaf.getSimpleConditionType()){
                case SIMPLE:
                    if(condition.getText().matches("[^=]+=[^=]+")) throw new InvalidConditionException(condition.getText(),GameConstants.REASON_SINGLE_EQUAL_SIGN);
                    if(variableScope.getVariable(condition.getText())==null) throw new NotInScopeException(condition.getText(),variableScope);
                    return;
                case GR_EQ:
                case LE_EQ:
                case GR:
                case LE:
                case NEQ:
                case EQ:
                    checkExpressionTreeForUnknownVars(conditionLeaf.getLeftExpression());
                    checkExpressionTreeForUnknownVars(conditionLeaf.getRightExpression());
                    return;
                case CAL:
                    if(variableScope.getVariable(conditionLeaf.getLeftExpression().getText())==null)throw new NotInScopeException(conditionLeaf.getLeftExpression().getText(),variableScope);
                    checkExpressionTreeForUnknownVars(conditionLeaf.getLeftExpression());
                    ExpressionTree rightNode = (ExpressionTree)conditionLeaf.getRightExpression();
                    if(rightNode.getLeftNode() == null)throw new MethodUnknownException(conditionLeaf.getRightExpression().getText());
                    Expression leftNode = conditionLeaf.getLeftExpression();
                    MethodType mT = MethodType.getMethodTypeFromName(rightNode.getLeftNode().getText());
                    if(mT==null)throw new IllegalArgumentException("No such Method " + leftNode.getText()+"!");
                    if(mT.getOutputType()!=VariableType.BOOLEAN)throw new IllegalArgumentException("Method " + conditionLeaf.getLeftExpression().getText()+" has illegal type!");
                    testForCorrectParameters(rightNode.getRightNode().getText(), mT);
                    return;
            }
        }
        throw new IllegalArgumentException(condition.getText() + " is not allowed to stand here!");
    }

    private static void checkExpressionTreeForUnknownVars(Expression valueTree) {
        if(valueTree.getDepth()==1 && valueTree.getText().split(",").length > 1){
            for(String s : valueTree.getText().split(","))
                checkExpressionTreeForUnknownVars(Expression.expressionFromString(s));
            return;
        }
        if(valueTree.getText().matches(" *"))return;
        if(valueTree.getExpressionType() != ExpressionType.SIMPLE){
            checkExpressionTreeForUnknownVars(((ExpressionTree)valueTree).getRightNode());
            checkExpressionTreeForUnknownVars(((ExpressionTree)valueTree).getLeftNode());
        }
        else{
            if(valueTree.getText().matches("new .*\\(.*\\)"))return;
            if(MethodType.getMethodTypeFromCall(valueTree.getText())!=null)return;
            if(valueTree.getText().matches("(-?\\d+|true|false|LEFT|RIGHT|AROUND|EAST|NORTH|SOUTH|WEST|"+GameConstants.RAND_INT_REGEX+")"))return;
            if(currentForVariable != null && valueTree.getText().equals(currentForVariable.getName())) return;
            if(valueTree.getText().split("\\.").length > 1){
                for(String s : valueTree.getText().split("\\."))
                    checkExpressionTreeForUnknownVars(Expression.expressionFromString(s));
                return;
            }
        }
    }

}