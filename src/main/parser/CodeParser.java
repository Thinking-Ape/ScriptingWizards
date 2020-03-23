package main.parser;

import main.model.enums.*;
import main.model.statement.*;
import main.model.statement.Condition.Condition;
import main.model.statement.Condition.ConditionLeaf;
import main.model.statement.Condition.ConditionTree;
import main.utility.*;
import main.model.statement.Expression.ExpressionLeaf;
import main.model.statement.Expression.ExpressionTree;
import main.model.statement.Expression.ExpressionType;

import java.util.*;
import java.util.regex.Matcher;

import static main.model.statement.Condition.ConditionType.NEGATION;
import static main.model.statement.Condition.ConditionType.SINGLE;

//TODO: make abstract?
public abstract class CodeParser {

    private static ComplexStatement behaviour;
    private static List<String> codeLines;
    private static Map<Integer,ComplexStatement> depthStatementMap;
    private static boolean isPlayerCode = true;
    private static Statement lastStatement;
    private static Variable currentForVariable;

    public static ComplexStatement parseProgramCode(List<String> lines) {
        return parseProgramCode(lines, true);
    }

    /** Will try to convert the first given Parameter into a ComplexStatement consisting of multiple Substatements
     *  (Statements) representing the respective line of code
     *
     * @param lines The given lines of code
     * @param isPlayerCodeValue whether it is player code or code of the enemy. the first cannot spawn Skeletons, the latter
     *                          cant spawn Knights
     * @throws IllegalAccessException TODO
     * @throws IllegalArgumentException TODO
     */
    public static ComplexStatement parseProgramCode(List<String> lines,boolean isPlayerCodeValue){
        codeLines = lines;
        isPlayerCode = isPlayerCodeValue;
        behaviour = new ComplexStatement();
        int depth = 1;
        depthStatementMap = new HashMap<>();
        depthStatementMap.put(0,behaviour);
        for ( String code : codeLines){
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
                continue;
            }
            Statement statement = parseString(code,depth);
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
                //TODO: evaluate if I want to keep that!
                if(depth > GameConstants.MAX_DEPTH)throw new IllegalStateException("You are not allowed to have a greater depth than "+ GameConstants.MAX_DEPTH +"!");
                depth++;
            }
            else {
                depthStatementMap.get(depth - 1).addSubStatement(statement);
                if(statement.getStatementType() == StatementType.DECLARATION){
                    Variable variable = ((Assignment)statement).getVariable();
                    boolean isSkeleton = variable.getVariableType() == VariableType.SKELETON;
                    //TODO: Implement GHOST or delete
//                    boolean isGhost = variable.getVariableType() == VariableType.GHOST;
                    if(isPlayerCode && (/*isGhost||*/isSkeleton ))throw new IllegalArgumentException("You are not allowed to create Enemy Creatures as Player");
                    boolean isKnight = variable.getVariableType() == VariableType.KNIGHT;
                    if(!isPlayerCode && isKnight )throw new IllegalArgumentException("You are not allowed to create Player Creatures as Enemy");
                    if(/*isGhost ||*/ isKnight || isSkeleton){
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
                    statement.getParentStatement().addLocalVariable(((Assignment)statement).getVariable());
                }
            }
            lastStatement = statement;
        }
        behaviour.resetVariables(true);
        if(depth != 1)throw new IllegalStateException("Unbalanced amount of brackets!");
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
        //TODO: what does this do?
        //boolean noSimple = lastStatement.getStatementType()!=StatementType.SINGLE;
        boolean lastStatementIllegal = (depth == lastStatement.getDepth()-1 && !lastStatement.isComplex());
        if((noIfBefore && noElseBefore)|| lastStatementIllegal) return false;
        final boolean conditionSet = depthStatementMap.get(depth).getCondition() != null;
        return conditionSet;
    }

    /** Will try to turn the given code into a Statement
     *
     * @param depth is needed to grant access to the parent statement and its variables
     * @throws IllegalAccessException TODO
     * @throws IllegalArgumentException TODO
     */
    private static Statement parseString(String code, int depth) {
        Matcher whileMatcher = StatementType.getMatcher(StatementType.WHILE,code);
        Matcher forMatcher = StatementType.getMatcher(StatementType.FOR,code);
        Matcher ifMatcher = StatementType.getMatcher(StatementType.IF,code);
        Matcher elseMatcher = StatementType.getMatcher(StatementType.ELSE,code);
        Matcher asMatcher = StatementType.getMatcher(StatementType.ASSIGNMENT,code);
        Matcher mcMatcher = StatementType.getMatcher(StatementType.METHOD_CALL,code);
        Matcher decMatcher = StatementType.getMatcher(StatementType.DECLARATION,code);
        if(whileMatcher.matches()){
            code = whileMatcher.group(1);
            Condition condition = Condition.getConditionFromString(code);
            checkConditionForUnknownVars(condition, depth);
            return new WhileStatement(condition);
        }
        else if(forMatcher.matches()){
            code = forMatcher.group(1);
            StringPair tempStatements1 = Util.splitAtChar(code,';',false); //split once
            StringPair tempStatements2 = Util.splitAtChar(tempStatements1.second(),';',false);
            String assignmentString = tempStatements2.second().replaceAll(";","");
            String[] statements = new String[]{tempStatements1.first(),tempStatements2.first(),assignmentString};
            Assignment assignment = parseAssignment(statements[0],depth);
            currentForVariable = assignment.getVariable();
            Condition condition = Condition.getConditionFromString(statements[1]);
            checkConditionForUnknownVars(condition, depth);
            return  new ForStatement(assignment,condition,parseAssignment(statements[2],depth));
    }
        else if(ifMatcher.matches()){
            code = ifMatcher.group(1);
            Condition condition = Condition.getConditionFromString(code);
            checkConditionForUnknownVars(condition, depth);
            return new ConditionalStatement(condition,false);
        }
        else if(elseMatcher.matches()){
            code = elseMatcher.group(2);
            if(code!= null) {
                Condition condition = Condition.getConditionFromString(code);
                checkConditionForUnknownVars(condition, depth);
                return new ConditionalStatement(condition,true);
            }
            return new ConditionalStatement(null,true);
        }
        else if(asMatcher.matches()){
            code = asMatcher.toMatchResult().group();
            code = Util.stripCode(code);
            return parseAssignment(code,depth);
        }
        else if(decMatcher.matches()){
            code = decMatcher.toMatchResult().group();
            code = Util.stripCode(code);
            return parseAssignment(code,depth);
        }
        else if(mcMatcher.matches()){
            code = mcMatcher.toMatchResult().group();
            code = Util.stripCode(code);
            return parseMethodCall(code,depth);
        }

        else {
            Statement tempStatement1;
            Statement tempStatement2;
            if(code.charAt(code.length()-1)!=';' && code.charAt(code.length()-1)!='{'){
                tempStatement1 =  parseString(code+";",depth);
                tempStatement2 = parseString(code + "{", depth);
                if(tempStatement1 != null) throw new IllegalArgumentException("You might have forgotten a ';'!");
                if(tempStatement2 != null) throw new IllegalArgumentException("You might have forgotten a '{'");
            }
            return null;
        }

    }

    /** Will try to convert the given code into a MethodCall Object
     * @param depth is needed to grant access to the parent statement and its variables
     */
    private static MethodCall parseMethodCall(String code,int depth) {
        StringPair tempStatements = Util.splitAtChar(code,'.',false);
        String objectName = tempStatements.first();
        tempStatements = Util.splitAtChar(tempStatements.second(),'(',false);
        String methodName = tempStatements.first();
        String parameters = tempStatements.second().substring(0,tempStatements.second().length()-1);
        Variable v = depthStatementMap.get(depth-1).getVariable(objectName);
        if(v==null)
            throw new IllegalArgumentException("Variable inside MethodCall " +objectName +" not in scope!");
        // Maybe give VariableType an attribute that regulates whether this variable can have MethodCalls?
        else if (!(v.getVariableType() == VariableType.ARMY ||v.getVariableType() == VariableType.KNIGHT ||v.getVariableType() == VariableType.SKELETON)){
            throw new IllegalArgumentException("Only Knights, Armies or Skeletons may call methods!");
        }
        MethodType mType = MethodType.getMethodTypeFromName(methodName);
        if(mType == null) throw new IllegalArgumentException("Method " + methodName + " is not a valid method!");
        // Makes lines such as: knight.targetsCell(EXIT); illegal (they need the condition context!)
        if(mType.getOutputType() != VariableType.ACTION) throw new IllegalArgumentException("Method " + methodName + " cannot stand here!");
        if(isPlayerCode && mType == MethodType.ATTACK) throw new IllegalArgumentException("Knights cannot attack! Collect a sword and refer to useItem()!");
        // TODO: Method below doesnt return a boolean and instead may throw errors <- bad handling?! <- create my own error handling system?
        testForCorrectParameters(parameters,mType,depth);
        if(!parameters.equals("")){
           checkExpressionTreeForUnknownVars(new ExpressionLeaf(parameters),depth);
        }
        return new MethodCall(MethodType.getMethodTypeFromCall(methodName+"("+parameters+")"),objectName,parameters);
    }

    /** Tests whether the given parameters are correct for the given MethodType. Possible MethodTypes are all Knight
     *  and Skeleton Methods, but not randInt(int i, int j)!
     * @param depth is needed to access the current parent statement and their variables
     */
    private static void testForCorrectParameters(String parameters, MethodType mType,int depth) {
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
                        (depthStatementMap.get(depth-1).getVariable(parameters) != null &&
                                depthStatementMap.get(depth-1).getVariable(parameters).getVariableType() == VariableType.TURN_DIRECTION &&
                                    !depthStatementMap.get(depth-1).getVariable(parameters).getValue().getText().equals(""))){
                    return;
                }
                break;

            case IS_LOOKING:
                if(parameters.matches(VariableType.DIRECTION.getAllowedRegex())||
                        (depthStatementMap.get(depth-1).getVariable(parameters)!=null &&
                                depthStatementMap.get(depth-1).getVariable(parameters).getVariableType()==VariableType.DIRECTION&&
                                    !depthStatementMap.get(depth-1).getVariable(parameters).getValue().getText().equals(""))){
                    return;
                }
                break;
        }
        if(parameters.equals("")) throw new IllegalArgumentException("Method "+ mType.getName()+" needs parameters!");
        throw new IllegalArgumentException(parameters+" is not a correct parameter!");
    }


    private static Assignment parseAssignment(String statementString,int depth) {
        String variable;
        String value;
        if (statementString.matches(GameConstants.VARIABLE_NAME_REGEX+" *\\+\\+")){
            variable=statementString.replaceAll("\\+\\+","");
            value = variable+"+1";
        }
        else if (statementString.matches(GameConstants.VARIABLE_NAME_REGEX+" *--")){
            variable=statementString.replaceAll("--","");
            value = variable+"-1";
        }
        else {
            StringPair tempStatements = Util.splitAtChar(statementString,'=',false);
            variable = tempStatements.first();
            value = tempStatements.second();
        }
        value = value.trim();
        ExpressionTree valueTree = ExpressionTree.expressionTreeFromString(value.trim()); //0
        Condition condition = Condition.getConditionFromString(value.trim());
        //TODO: expand to make it more readable!
        if(valueTree.getLeftNode() != null){
            String vTypeString = valueTree.getLeftNode().getText().replaceAll("new ","");
         if( EntityType.getValueFromName(vTypeString)!=EntityType.NONE &&
                 !valueTree.getRightNode().getText().equals("")&&
                 Direction.getValueFromString(valueTree.getRightNode().getText())==null)
             throw new IllegalArgumentException(valueTree.getRightNode().getText()+ " is not a Direction!");
        }
        StringPair variableTypeAndName = Util.splitAtChar(variable,' ',false);
        String variableName;
        String variableTypeString = "";
        if(variableTypeAndName.second().equals("")){
            variableName = variableTypeAndName.first();
        } else {
            variableTypeString = variableTypeAndName.first();
            variableName = variableTypeAndName.second();
        }
        if(!variableName.trim().matches(GameConstants.VARIABLE_NAME_REGEX))throw new IllegalArgumentException("A variable must not be named: "+variableName+"!");
//        System.out.println(variableName);
        //TODO: make cleaner
        boolean isDeclaration = true;

        VariableType variableType = VariableType.getVariableTypeFromString(variableTypeString.trim());
        if(variableType == VariableType.BOOLEAN &&!value.trim().equals(""))
            checkConditionForUnknownVars(condition,depth);
        else
            checkExpressionTreeForUnknownVars(valueTree,depth);
        if(!value.equals(""))testForCorrectValueType(variableType,value,depth);
        Variable variable1 = null;

        if(variableTypeString.equals("")&&value.trim().equals(""))throw new IllegalArgumentException("You cannot assign an empty value!");
        if(variableTypeString.equals("")){
            isDeclaration = false;
            ComplexStatement complexStatement = depthStatementMap.get(depth-1);
            if (complexStatement != null && currentForVariable == null){
                variable1 = complexStatement.getVariable(variableName.trim());
                if(variable1 == null)
                    throw new IllegalArgumentException("Variable inside Assignment " + variableName+ " not in scope!");
                variableType = variable1.getVariableType();

                testForCorrectValueType(variableType,value,depth);
            }
            else {
                ComplexStatement cS = depthStatementMap.get(depth-1);
                if(cS != null && cS.getVariable(variableName)!=null)throw new IllegalArgumentException("Variable " + variableName + " already in scope!");
                variableType = VariableType.INT;
            }
        }
//        if(!variableType.equals(""))((ComplexStatement)depthStatementMap.get(depth-1)).addLocalVariable(new Variable(variableType,variableName.trim(),valueTree));
        Assignment simpleStatement = new Assignment(variableName.trim(),variableType,valueTree,isDeclaration);
        return simpleStatement;
    }

    //TODO: find out, when to use this as currently there are multiple methods that do more or less the same -> merge!
    private static void testForCorrectValueType(VariableType variableType, String value, int depth) {
        value = value.trim();
        if(value.equals(""))throw new IllegalArgumentException("You cannot have an empty value!");
        if(depthStatementMap.get(depth-1) == null)throw new IllegalStateException("You cant have this statement here!");
        Variable v = depthStatementMap.get(depth-1).getVariable(value);
        if(v!=null){
            if(v.getVariableType() != variableType)throw new IllegalArgumentException(value + " has the wrong type!");
//            if(depthStatementMap.get(depth-1).getStatementType() == StatementType.FOR){
//                Variable forVar = ((ForStatement)depthStatementMap.get(depth-1)).getDeclaration().getVariable();
//                if(forVar.getName().equals(value))testForCorrectValueType(variableType, forVar.getValue().getText(),depth);
//                else testForCorrectValueType(variableType, value,depth-1);
//            }
//            else
            testForCorrectValueType(variableType, depthStatementMap.get(depth-1).getVariable(value).getValue().getText(),depth);
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
                        testForCorrectValueType(VariableType.INT,lowerBound,depth );
                        testForCorrectValueType(VariableType.INT,upperBound,depth );
                    }
                    else {
                        testForCorrectValueType(VariableType.INT,tree.getLeftNode().getText(),depth );
                        testForCorrectValueType(VariableType.INT,tree.getRightNode().getText(),depth );
                    }
                }
                else if(!value.matches(GameConstants.RAND_INT_REGEX)&&!value.matches(variableType.getAllowedRegex())){
                    v = currentForVariable;
                    if(v!=null){
                        if(v.getVariableType() != variableType)throw new IllegalArgumentException(value + " has the wrong type!");
                        testForCorrectValueType(variableType, v.getValue().getText(),depth);
                        return;
                    }
                    throw new IllegalArgumentException(value + " is no number!");
                }
                else return;
            case BOOLEAN:
                if(value.equals("true")||value.equals("false"))return;
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
                            testForCorrectValueType(VariableType.INT,leftTree.getText(),depth);
                            testForCorrectValueType(VariableType.INT,rightTree.getText(),depth);
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
                                    testForCorrectValueType(VariableType.INT,leftTree.getText(),depth);
                                    testForCorrectValueType(VariableType.INT,rightTree.getText(),depth);
                                    return;
                                case SIMPLE:
                                    v = depthStatementMap.get(depth-1).getVariable(leftTree.getText());
                                    if(v!=null){
                                        testForCorrectValueType(v.getVariableType(),v.getValue().getText(),depth);
                                    }
                                    else {
                                        if(leftTree.getText().matches(VariableType.INT.getAllowedRegex())){
                                            testForCorrectValueType(VariableType.INT,leftTree.getText(),depth);
                                        }
                                        else if(leftTree.getText().matches("(true|false)"))
                                            testForCorrectValueType(VariableType.BOOLEAN,leftTree.getText(),depth);
                                    }
                                    v = depthStatementMap.get(depth-1).getVariable(rightTree.getText());
                                    if(v!=null){
                                        testForCorrectValueType(v.getVariableType(),v.getValue().getText(),depth);
                                        return;
                                    }
                                    else {
                                        if(rightTree.getText().matches(VariableType.INT.getAllowedRegex())){
                                            testForCorrectValueType(VariableType.INT,rightTree.getText(),depth);
                                            return;
                                        }
                                        else if(rightTree.getText().matches("(true|false)")){
                                            testForCorrectValueType(VariableType.BOOLEAN,rightTree.getText(),depth);
                                            return;
                                        }

                                    }
                                    //TODO: Direction etc?
                                    break;
                            }
                            break;
                        case CAL:
                            MethodType mt = MethodType.getMethodTypeFromCall(conditionLeaf.getRightTree().getText());
                            if(mt== null)throw new IllegalArgumentException("Unknown Method:" + conditionLeaf.getText());
                            if(mt.getOutputType()== VariableType.BOOLEAN){
                                testForCorrectParameters(conditionLeaf.getRightTree().getRightNode().getText(), mt, depth);
                                return;
                            }
                    }
                }
//                if(!value.matches("true|false"))throw new IllegalArgumentException(value + " is no boolean!");
//                else return;
                return;
            case KNIGHT:
                if(!value.matches(variableType.getAllowedRegex())){
                    tree = ExpressionTree.expressionTreeFromString(value);
                    if(tree.getRightNode() != null && depthStatementMap.get(depth-1).getVariable(tree.getRightNode().getText()) != null && depthStatementMap.get(depth-1).getVariable(tree.getRightNode().getText()).getVariableType() == VariableType.DIRECTION)return;
                    throw new IllegalArgumentException(value + " is not a valid Knight constructor!");
                }
                break;
            case SKELETON:
                if(!value.matches(variableType.getAllowedRegex())){
                    tree = ExpressionTree.expressionTreeFromString(value);
                    if(tree.getRightNode() != null) {
                        boolean varDirValid = depthStatementMap.get(depth-1).getVariable(tree.getRightNode().getText()) != null && depthStatementMap.get(depth-1).getVariable(tree.getRightNode().getText()).getVariableType() == VariableType.DIRECTION;
                        boolean dirValid = tree.getRightNode().getText().matches(VariableType.DIRECTION.getAllowedRegex());
                        if(varDirValid || dirValid)return;
                        // 2 Parameters
                        if(tree.getRightNode().getLeftNode() != null && tree.getRightNode().getRightNode() != null){
                            varDirValid = depthStatementMap.get(depth-1).getVariable(tree.getRightNode().getLeftNode().getText()) != null && depthStatementMap.get(depth-1).getVariable(tree.getRightNode().getLeftNode().getText()).getVariableType() == VariableType.DIRECTION;
                            dirValid = tree.getRightNode().getLeftNode().getText().matches(VariableType.DIRECTION.getAllowedRegex());
                            boolean varIntValid = depthStatementMap.get(depth-1).getVariable(tree.getRightNode().getRightNode().getText()) != null && depthStatementMap.get(depth-1).getVariable(tree.getRightNode().getRightNode().getText()).getVariableType() == VariableType.INT;
                            boolean intValid = tree.getRightNode().getLeftNode().getText().matches(VariableType.INT.getAllowedRegex());
                            if((varDirValid || dirValid) && (varIntValid || intValid))return;
                        }
                    }
                    throw new IllegalArgumentException(value + " is not a valid Skeleton constructor!");
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

                    if(depthStatementMap.get(depth-1).getVariable(parameter) == null)
                        throw new IllegalArgumentException(parameter + " is not a valid variable!");
                    else {
                        boolean isKnight = depthStatementMap.get(depth-1).getVariable(parameter).getVariableType() == VariableType.KNIGHT && isPlayerCode;
                        boolean isSkeleton = depthStatementMap.get(depth-1).getVariable(parameter).getVariableType() == VariableType.SKELETON && !isPlayerCode;
                        if(isKnight || isSkeleton)continue;
                    }
                    throw new IllegalArgumentException(value + " is not a valid Army constructor!");}
                }
                break;
            case ACTION:
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

    private static void checkConditionForUnknownVars(Condition condition, int depth) {
        if(condition == null||condition.getText().equals(""))throw new IllegalArgumentException("You cannot have an empty Condition!");
        testForCorrectValueType(VariableType.BOOLEAN,condition.getText(),depth);
        if(condition.getText().matches(" *"))return;
        if(condition.getConditionType() != SINGLE){
            checkConditionForUnknownVars(((ConditionTree)condition).getRightCondition(),depth);
            if(condition.getConditionType()!=NEGATION)checkConditionForUnknownVars(((ConditionTree)condition).getLeftCondition(),depth);
            return;
        }
        else{
            if(condition.getText().matches(VariableType.BOOLEAN.getAllowedRegex()))return;
            ConditionLeaf conditionLeaf = (ConditionLeaf)condition;
            switch(conditionLeaf.getSimpleConditionType()){
                case SIMPLE:
                    if(depthStatementMap.get(depth-1).getVariable(condition.getText())==null) throw new IllegalArgumentException("Boolean Variable "+condition.getText()+" not in scope!");
                    return;
                case GR_EQ:
                case LE_EQ:
                case GR:
                case LE:
                case NEQ:
                case EQ:
                    checkExpressionTreeForUnknownVars(conditionLeaf.getLeftTree(),depth);
                    checkExpressionTreeForUnknownVars(conditionLeaf.getRightTree(),depth);
                    return;
                case CAL:
                    checkExpressionTreeForUnknownVars(conditionLeaf.getLeftTree(), depth);
                    if(conditionLeaf.getRightTree().getLeftNode() == null)throw new IllegalArgumentException(conditionLeaf.getRightTree().getText() + " is not a valid Method!");
                    MethodType mT = MethodType.getMethodTypeFromName(conditionLeaf.getRightTree().getLeftNode().getText());
                    if(mT==null)throw new IllegalArgumentException("No such Method " + conditionLeaf.getRightTree().getLeftNode().getText()+"!");
                    if(mT.getOutputType()!=VariableType.BOOLEAN)throw new IllegalArgumentException("Method " + conditionLeaf.getLeftTree().getText()+" has illegal type!");
                    testForCorrectParameters(conditionLeaf.getRightTree().getRightNode().getText(), mT, depth);
                    return;
            }
        }
        throw new IllegalArgumentException(condition.getText() + " is not allowed to stand here!");
    }

    private static void checkExpressionTreeForUnknownVars(ExpressionTree valueTree,int depth) {
        if(valueTree.getDepth()==1 && valueTree.getText().split(",").length > 1){
            for(String s : valueTree.getText().split(","))
                checkExpressionTreeForUnknownVars(ExpressionTree.expressionTreeFromString(s), depth);
            return;
        }
        if(valueTree.getText().matches(" *"))return;
        if(valueTree.getExpressionType() != ExpressionType.SIMPLE){
            checkExpressionTreeForUnknownVars(valueTree.getRightNode(),depth);
            checkExpressionTreeForUnknownVars(valueTree.getLeftNode(),depth);
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
                    checkExpressionTreeForUnknownVars(ExpressionTree.expressionTreeFromString(s), depth);
                return;
            }
            if(depthStatementMap.get(depth-1).getVariable(valueTree.getText())==null)
                throw new IllegalArgumentException("Variable "+valueTree.getText()+" not in scope!");
        }
    }

}
