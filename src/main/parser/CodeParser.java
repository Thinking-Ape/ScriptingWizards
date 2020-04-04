package main.parser;

import main.model.enums.*;
import main.model.statement.*;
import main.model.statement.Condition.Condition;
import main.model.statement.Condition.ConditionLeaf;
import main.model.statement.Condition.ConditionTree;
import main.model.statement.Expression.ExpressionTree;
import main.model.statement.Expression.ExpressionType;
import main.utility.GameConstants;
import main.utility.Util;
import main.utility.Variable;
import main.view.CodeAreaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.model.statement.Condition.ConditionType.NEGATION;
import static main.model.statement.Condition.ConditionType.SINGLE;

//TODO: make abstract?
public abstract class CodeParser {

    private static ComplexStatement behaviour;
    private static List<String> codeLines;
    private static Map<Integer,ComplexStatement> depthStatementMap;
    private static CodeAreaType codeAreaType;
    private static Statement lastStatement;
    private static Variable currentForVariable;
    private static VariableScope variableScope;

    public static ComplexStatement parseProgramCode(List<String> lines) {
        return parseProgramCode(lines, CodeAreaType.PLAYER);
    }

    /** Will try to convert the first given Parameter into a ComplexStatement consisting of multiple Substatements
     *  (Statements) representing the respective line of code
     *
     * @param lines The given lines of code
     * @param codeAreaType whether it is player code or code of the enemy. the first cannot spawn Skeletons, the latter
     *                          cant spawn Knights
     * @throws IllegalArgumentException TODO
     */
    public static ComplexStatement parseProgramCode(List<String> lines, CodeAreaType codeAreaType){
        variableScope = new VariableScope();
        codeLines = lines;
        CodeParser.codeAreaType = codeAreaType;
        behaviour = new ComplexStatement();
        int depth = 1;
        depthStatementMap = new HashMap<>();
        depthStatementMap.put(0,behaviour);
        int index = 0;
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
                if(depth > GameConstants.MAX_DEPTH)throw new IllegalStateException("You are not allowed to have a greater depth than "+ GameConstants.MAX_DEPTH +"!");
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
            index++;
        }
        if(depth != 1)throw new IllegalStateException("Unbalanced amount of brackets!");
        return behaviour;
    }

    //TODO:!!!
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
     *
     * @throws IllegalArgumentException TODO
     */
    private static Statement parseString(String code) {
        for(StatementType statementType : StatementType.values()){
            Matcher matcher = Pattern.compile(statementType.getRegex()).matcher(code);
            switch (statementType){
                case WHILE:
                    if(matcher.matches()){
                        String conditionString = matcher.group(1);
                        Condition condition = Condition.getConditionFromString(conditionString);
                        if(condition == null)throw new IllegalArgumentException("Couldnt parse Condition from input " + conditionString);
                        checkConditionForUnknownVars(condition);
                        return new WhileStatement(condition);
                    }
                    break;
                case FOR:
                    if(matcher.matches()){
                        String forInnerString = matcher.group(1);
                        Matcher innerForMatcher = Pattern.compile(GameConstants.FOR_INNER_REGEX).matcher(forInnerString);
                        if(innerForMatcher.matches()){
                            String declarationString = innerForMatcher.group(1);
                            String conditionString = innerForMatcher.group(2);
                            String assignmentString = innerForMatcher.group(3);
                            Matcher declarationMatcher = Pattern.compile(StatementType.DECLARATION.getRegex()).matcher(declarationString+";");
                            Matcher assignmentMatcher = Pattern.compile(StatementType.ASSIGNMENT.getRegex()).matcher(assignmentString+";");
                            if(!declarationMatcher.matches()) throw new IllegalArgumentException("String: "+declarationString+" should be a declaration!");
                            if(!assignmentMatcher.matches()) throw new IllegalArgumentException("String: "+assignmentString+" should be an assignment!");

                            Assignment declaration = parseDeclaration(declarationMatcher.group(1),declarationMatcher.group(2),declarationMatcher.group(4));
                            if(declaration == null) throw new IllegalArgumentException("Couldnt parse Declaration: "+declarationString);
                            variableScope.addVariable(new Variable(declaration.getVariable()));
                            Assignment assignment = parseAssignment(assignmentMatcher.group(1),assignmentMatcher.group(2),assignmentMatcher.group(3));
                            Condition condition =Condition.getConditionFromString(conditionString);
                            if(condition == null)throw new IllegalArgumentException("Couldnt parse Condition from input " + conditionString);
                            checkConditionForUnknownVars(condition);
                            if(assignment == null) throw new IllegalArgumentException("Couldnt parse Assignment: "+assignmentString);
                            if(declaration.getVariable().getVariableType()!=VariableType.INT)throw new IllegalArgumentException("Declaration "+declarationString+" must be an int Variable!");
                            variableScope.removeVariable(declaration.getVariable().getName());
                            return new ForStatement(declaration,condition,assignment);
                        }
                        else throw new IllegalArgumentException("Illegal input for For-Statement: "+forInnerString);
                    }
                    break;
                case IF:
                    if(matcher.matches()){
                        String conditionString = matcher.group(1);
                        Condition condition = Condition.getConditionFromString(conditionString);
                        if(condition == null)throw new IllegalArgumentException("Couldnt parse Condition from input " + conditionString);
                        checkConditionForUnknownVars(condition);
                        return new ConditionalStatement(condition,false);
                    }
                    break;
                case ELSE:
                    if(matcher.matches()){
                        String conditionString = matcher.group(2);
                        if(conditionString == null)return new ConditionalStatement(null, true);
                        Condition condition = Condition.getConditionFromString(conditionString);
                        if(condition == null)throw new IllegalArgumentException("Couldnt parse Condition from input " + conditionString);
                        checkConditionForUnknownVars(condition);
                        return new ConditionalStatement(condition,true);
                    }
                    break;
                case METHOD_CALL:
                    if(matcher.matches()){
                        String objectName = matcher.group(2);
                        String methodName = matcher.group(3);
                        String parameters = matcher.group(4);
//                        if(!objectName.matches(GameConstants.VARIABLE_NAME_REGEX))throw new IllegalArgumentException("A variable must not be named: "+methodName);
                        MethodCall methodCall = parseMethodCall(objectName, methodName, parameters);
                        return methodCall;
                    }
                    break;
                case DECLARATION:
                    if(matcher.matches()){
                        String varName = matcher.group(2);
                        String varType = matcher.group(1);
                        String value = matcher.group(4);
//                        if(!objectName.matches(GameConstants.VARIABLE_NAME_REGEX))throw new IllegalArgumentException("A variable must not be named: "+methodName);
                        //TODO:
                        Assignment declaration = parseDeclaration(varType,varName,value);
                        //TODO:

                        if(declaration == null)throw new IllegalArgumentException("Couldnt parse Assignment from input " + code);
                        return declaration;
                    }
                    break;
                case ASSIGNMENT:
                    if(matcher.matches()){
                        String varName = matcher.group(1);
                        String operation = matcher.group(2);
                        String value = matcher.group(3);
//                        if(!objectName.matches(GameConstants.VARIABLE_NAME_REGEX))throw new IllegalArgumentException("A variable must not be named: "+methodName);
                        Assignment assignment = parseAssignment(varName,operation,value);
                        //TODO:
                        if(assignment == null)throw new IllegalArgumentException("Couldnt parse Assignment from input " + code);
                        return assignment;
                    }
                    break;
//                case COMPLEX:
//                    break;
//                case SIMPLE:
//                    break;
            }
        }
        Statement tempStatement1;
        Statement tempStatement2;
        char lastChar = code.charAt(code.length()-1);
        if(lastChar!=';' && lastChar!='{'){
            tempStatement1 =  parseString(code+";");
            tempStatement2 = parseString(code + "{");
            if(tempStatement1 != null) throw new IllegalArgumentException("You might have forgotten a ';'!");
            if(tempStatement2 != null) throw new IllegalArgumentException("You might have forgotten a '{'");
        }
        return null;
    }

    /** Will try to convert the given code into a MethodCall Object
     */
    private static MethodCall parseMethodCall(String objectName, String methodName, String parameterString) {
        if(objectName.matches(" *"))throw new IllegalArgumentException("You cant have an empty object!");
        MethodType mType = MethodType.getMethodTypeFromName(methodName);
        if(mType == null) throw new IllegalArgumentException("Method " + methodName + " is not a valid method!");
        testForCorrectParameters(parameterString, mType);
        // Makes lines such as: knight.targetsCell(EXIT); illegal (they need the condition context!)
        if(mType.getOutputType() != VariableType.VOID) throw new IllegalArgumentException("Method " + methodName + " cannot stand here!");
        boolean isPlayerCode = codeAreaType != CodeAreaType.AI;
        if(isPlayerCode && mType == MethodType.ATTACK) throw new IllegalArgumentException("Knights cannot attack! Collect a sword and refer to useItem()!");
        return new MethodCall(mType,objectName,parameterString);
    }

/** Tests whether the given parameters are correct for the given MethodType. Possible MethodTypes are all Knight
     *  and Skeleton Methods, but not randInt(int i, int j)!
     */
    private static void testForCorrectParameters(String parameters, MethodType mType) {
        switch (mType){
            case ATTACK:
            case TARGET_IS_DANGER:
            case WAIT:
            case DROP_ITEM:
            case MOVE:
            case USE_ITEM:
            case COLLECT:
            case CAN_MOVE:
                if(!parameters.equals(""))throw new IllegalArgumentException("This method doesnt have parameters!");
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
        if(parameters.equals("")) throw new IllegalArgumentException("Method "+ mType.getName()+" needs parameters!");
        throw new IllegalArgumentException(parameters+" is not a correct parameter!");
    }


    private static Assignment parseAssignment(String varName, String operation, String valueString) {
        if(!varName.matches(GameConstants.WHOLE_VARIABLE_NAME_REGEX))throw new IllegalArgumentException("Variable name "+ varName+ " is illegal!");
        if(operation.equals("++"))valueString = varName+"+1";
        if(operation.equals("--"))valueString = varName+"-1";
        valueString = valueString.trim();
        ExpressionTree valueTree = ExpressionTree.expressionTreeFromString(valueString);
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
        if(!varName.matches(GameConstants.WHOLE_VARIABLE_NAME_REGEX))throw new IllegalArgumentException("Variable name "+ varName+ " is illegal!");
        valueString = valueString.trim();
        ExpressionTree valueTree = ExpressionTree.expressionTreeFromString(valueString);

        boolean valueNotEmpty = !valueString.equals("");
        VariableType variableType = VariableType.getVariableTypeFromString(variableTypeString);
        if(variableType == VariableType.VOID)throw new IllegalArgumentException("Variable type void is not allowed!");
        if(variableType == VariableType.BOOLEAN && valueNotEmpty){
            Condition condition = Condition.getConditionFromString(valueString);
            checkConditionForUnknownVars(condition );
        }
        else if(valueNotEmpty) checkExpressionTreeForUnknownVars(valueTree);
        if(valueNotEmpty) testForCorrectValueType(variableType,valueString);
        Variable variable = new Variable(variableType, varName, valueTree);
        boolean isSkeleton = variable.getVariableType() == VariableType.SKELETON;
        boolean isPlayerCode = codeAreaType != CodeAreaType.AI;
        if(isPlayerCode && (isSkeleton ))throw new IllegalArgumentException("You are not allowed to create Enemy Creatures as Player");
        boolean isKnight = variable.getVariableType() == VariableType.KNIGHT;
        if(!isPlayerCode && isKnight )throw new IllegalArgumentException("You are not allowed to create Player Creatures as Enemy");
        Assignment declaration = new Assignment(varName.trim(),variableType,valueTree,true);
        return declaration;
    }

   private static void testForCorrectValueType(VariableType variableType, String value) {
        value = value.trim();
        if(value.equals(""))throw new IllegalArgumentException("You cannot have an empty value!");
//        if(depthStatementMap.get(depth-1) == null)throw new IllegalStateException("You cant have this statement here!");
        Variable v = variableScope.getVariable(value);
        if(v!=null){
            if(v.getVariableType() != variableType)throw new IllegalArgumentException(value + " has the wrong type!");
//            if(depthStatementMap.get(depth-1).getStatementType() == StatementType.FOR){
//                Variable forVar = ((ForStatement)depthStatementMap.get(depth-1)).getDeclaration().getVariable();
//                if(forVar.getName().equals(value))testForCorrectValueType(variableType, forVar.getValue().getText(),depth);
//                else testForCorrectValueType(variableType, value,depth-1);
//            }
//            else
//            System.out.println(value+" "+variableScope.getVariable(value).getValue().getText());
            testForCorrectValueType(variableType, variableScope.getVariable(value).getValue().getText());
            return;
        }
        switch (variableType){
            case INT:
                if(value.matches("[+-]?\\d+"))return;
                ExpressionTree tree = ExpressionTree.expressionTreeFromString(value);
                if(tree.getRightNode()!=null){
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
                            ExpressionTree leftTree = conditionLeaf.getLeftTree();
                            ExpressionTree rightTree = conditionLeaf.getRightTree();
                            testForCorrectValueType(VariableType.INT,leftTree.getText());
                            testForCorrectValueType(VariableType.INT,rightTree.getText());
                            return;
                        case NEQ:
                        case EQ:
                            leftTree = conditionLeaf.getLeftTree();
                            rightTree = conditionLeaf.getRightTree();
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
                            MethodType mt = MethodType.getMethodTypeFromCall(conditionLeaf.getRightTree().getText());
                            if(mt== null)throw new IllegalArgumentException("Unknown Method:" + conditionLeaf.getText());
                            if(mt.getOutputType()== VariableType.BOOLEAN){
                                testForCorrectParameters(conditionLeaf.getRightTree().getRightNode().getText(), mt);
                                return;
                            }
                    }
                }
                return;
            case KNIGHT:
                if(!value.matches(variableType.getAllowedRegex())){
                    tree = ExpressionTree.expressionTreeFromString(value);
                    if(tree.getRightNode() != null && variableScope.getVariable(tree.getRightNode().getText()) != null && variableScope.getVariable(tree.getRightNode().getText()).getVariableType() == VariableType.DIRECTION)return;
                    throw new IllegalArgumentException(value + " is not a valid Knight constructor!");
                }
                ExpressionTree expressionTree = ExpressionTree.expressionTreeFromString(value);
                if(!expressionTree.getRightNode().getText().equals("")){
                    testForCorrectValueType(VariableType.DIRECTION,expressionTree.getRightNode().getText());
                }
                break;
            case SKELETON:
                //TODO
                if(!value.matches(variableType.getAllowedRegex())){
                    tree = ExpressionTree.expressionTreeFromString(value);
                    if(tree.getRightNode() != null) {
                        boolean varDirValid = variableScope.getVariable(tree.getRightNode().getText()) != null && variableScope.getVariable(tree.getRightNode().getText()).getVariableType() == VariableType.DIRECTION;
                        boolean dirValid = tree.getRightNode().getText().matches(VariableType.DIRECTION.getAllowedRegex());
                        if(varDirValid || dirValid)return;
                        // 2 Parameters
                        if(tree.getRightNode().getLeftNode() != null && tree.getRightNode().getRightNode() != null){
                            varDirValid = variableScope.getVariable(tree.getRightNode().getLeftNode().getText()) != null && variableScope.getVariable(tree.getRightNode().getLeftNode().getText()).getVariableType() == VariableType.DIRECTION;
                            dirValid = tree.getRightNode().getLeftNode().getText().matches(VariableType.DIRECTION.getAllowedRegex());
                            boolean varIntValid = variableScope.getVariable(tree.getRightNode().getRightNode().getText()) != null && variableScope.getVariable(tree.getRightNode().getRightNode().getText()).getVariableType() == VariableType.INT;
                            boolean intValid = tree.getRightNode().getLeftNode().getText().matches(VariableType.INT.getAllowedRegex());
                            if((varDirValid || dirValid) && (varIntValid || intValid))return;
                        }
                    }
                    throw new IllegalArgumentException(value + " is not a valid Skeleton constructor!");
                }

                expressionTree = ExpressionTree.expressionTreeFromString(value);
                if(!expressionTree.getRightNode().getText().equals("")){
                    if(expressionTree.getRightNode().getRightNode()!=null){
                        testForCorrectValueType(VariableType.DIRECTION,expressionTree.getRightNode().getLeftNode().getText());
                        testForCorrectValueType(VariableType.INT,expressionTree.getRightNode().getRightNode().getText());
                    }
                    else testForCorrectValueType(VariableType.DIRECTION,expressionTree.getRightNode().getText());
                }
                break;
            case DIRECTION:
                Direction dir = Direction.getValueFromString(value);
                if(dir == null)throw new IllegalArgumentException(value + " is no Direction!");
                else return;
            case TURN_DIRECTION:
                if(!value.matches(variableType.getAllowedRegex()))throw new IllegalArgumentException(value + " is no TurnDirection!");
                else return;
            case CELL_CONTENT:
                CellContent content = CellContent.getValueFromName(value);
                if(content == null)throw new IllegalArgumentException(value + " is no CellContent!");
                else return;
            case ITEM_TYPE:
                ItemType itemType = ItemType.getValueFromName(value);
                if(itemType == ItemType.NONE)throw new IllegalArgumentException(value + " is no ItemType!");
                else return;
            case ENTITY_TYPE:
                EntityType entityType = EntityType.getValueFromName(value);
                if(entityType == EntityType.NONE)throw new IllegalArgumentException(value + " is no EntityType!");
                else return;
            case ARMY:
                if(!value.matches(variableType.getAllowedRegex())){
                    tree = ExpressionTree.expressionTreeFromString(value);
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
//                    if(variableScope.getVariable(condition.getText())==null) throw new IllegalArgumentException("Boolean Variable "+condition.getText()+" not in scope!");
                    return;
                case GR_EQ:
                case LE_EQ:
                case GR:
                case LE:
                case NEQ:
                case EQ:
                    checkExpressionTreeForUnknownVars(conditionLeaf.getLeftTree());
                    checkExpressionTreeForUnknownVars(conditionLeaf.getRightTree());
                    return;
                case CAL:
                    checkExpressionTreeForUnknownVars(conditionLeaf.getLeftTree());
                    if(conditionLeaf.getRightTree().getLeftNode() == null)throw new IllegalArgumentException(conditionLeaf.getRightTree().getText() + " is not a valid Method!");
                    MethodType mT = MethodType.getMethodTypeFromName(conditionLeaf.getRightTree().getLeftNode().getText());
                    if(mT==null)throw new IllegalArgumentException("No such Method " + conditionLeaf.getRightTree().getLeftNode().getText()+"!");
                    if(mT.getOutputType()!=VariableType.BOOLEAN)throw new IllegalArgumentException("Method " + conditionLeaf.getLeftTree().getText()+" has illegal type!");
                    testForCorrectParameters(conditionLeaf.getRightTree().getRightNode().getText(), mT);
                    return;
            }
        }
        throw new IllegalArgumentException(condition.getText() + " is not allowed to stand here!");
    }

    private static void checkExpressionTreeForUnknownVars(ExpressionTree valueTree) {
        if(valueTree.getDepth()==1 && valueTree.getText().split(",").length > 1){
            for(String s : valueTree.getText().split(","))
                checkExpressionTreeForUnknownVars(ExpressionTree.expressionTreeFromString(s));
            return;
        }
        if(valueTree.getText().matches(" *"))return;
        if(valueTree.getExpressionType() != ExpressionType.SIMPLE){
            checkExpressionTreeForUnknownVars(valueTree.getRightNode());
            checkExpressionTreeForUnknownVars(valueTree.getLeftNode());
        }
        else{
            if(valueTree.getText().matches("new .*\\(.*\\)"))return;
//            if(valueTree.getText().matches("\\(.*\\)"))
//                return checkExpressionTreeForUnknownVars(ExpressionTree.expressionTreeFromString(valueTree.getText().substring(1,valueTree.getText().length()-1)),depth);
            //TODO: only allow the right ENUMS when writing those methods!
            if(MethodType.getMethodTypeFromCall(valueTree.getText())!=null)return;
//            for(ItemType it : ItemType.values()){
//                if(it.name().toUpperCase().equals(valueTree.getText()))return;
//            }
            //TODO: make this relative to: VariableType.(.+).getAllowedRegex() + "|"
            if(valueTree.getText().matches("(-?\\d+|true|false|LEFT|RIGHT|AROUND|EAST|NORTH|SOUTH|WEST|"+GameConstants.RAND_INT_REGEX+")"))return;
            if(currentForVariable != null && valueTree.getText().equals(currentForVariable.getName())) return;
            if(valueTree.getText().split("\\.").length > 1){
                for(String s : valueTree.getText().split("\\."))
                    checkExpressionTreeForUnknownVars(ExpressionTree.expressionTreeFromString(s));
                return;
            }
//            if(variableScope.getVariable(valueTree.getText())==null)
//                throw new IllegalArgumentException("Variable "+valueTree.getText()+" not in scope!");
        }
    }

}
/*if(statement.getStatementType() == StatementType.DECLARATION){
                    Variable variable = ((Assignment)statement).getVariable();
                    boolean isSkeleton = variable.getVariableType() == VariableType.SKELETON;
                    //TODO: Implement GHOST or delete
//                    boolean isGhost = variable.getVariableType() == VariableType.GHOST;
                    boolean isPlayerCode = codeAreaType != CodeAreaType.AI;
                    if(isPlayerCode && (/*isGhost||isSkeleton ))throw new IllegalArgumentException("You are not allowed to create Enemy Creatures as Player");
        boolean isKnight = variable.getVariableType() == VariableType.KNIGHT;
        if(!isPlayerCode && isKnight )throw new IllegalArgumentException("You are not allowed to create Player Creatures as Enemy");
        if(/*isGhost || isKnight || isSkeleton){
        if(variable.getValue().getLeftNode()!=null){
        VariableType variableType = VariableType.getVariableTypeFromString(variable.getValue().getLeftNode().getText().substring(4));
        if(variableType != variable.getVariableType())throw new IllegalArgumentException(((Assignment) statement).getText()+ " is an illegal expression!");
        }else {
        // TODO: dafuq?!
        System.out.println(variable.getValue().getText());
        VariableType variableType = VariableType.getVariableTypeFromString(variable.getValue().getText().substring(4,variable.getValue().getText().length()-2));
        if(variableType != variable.getVariableType())throw new IllegalArgumentException(((Assignment) statement).getText()+ " is an illegal expression!");
        }
        }
//                    variableScope.addVariable(variable);
        }*/