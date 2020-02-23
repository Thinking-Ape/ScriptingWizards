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
import java.util.regex.Pattern;

import static main.model.statement.Condition.ConditionType.NEGATION;
import static main.model.statement.Condition.ConditionType.SINGLE;

//TODO: make abstract?
public class CodeParser {

    private ComplexStatement behaviour;
    private List<String> codeLines;
    private Map<Integer,ComplexStatement> depthStatementMap;
    private boolean isPlayerCode = true;
    private Statement lastStatement;
    private Variable currentForVariable;

    public int getCurrentLine() {
        return currentLine;
    }

    private int currentLine = 0;

    public CodeParser(List<String> lines,boolean isPlayerCode) {
        this.codeLines = lines;
        this.isPlayerCode = isPlayerCode;
//        counter = 0;
    }

    public CodeParser() {
//        counter = 0;

    }

    public ComplexStatement parseProgramCode() throws IllegalAccessException, IllegalArgumentException{
        List<String> codeLineArray = new ArrayList<>();
//        int i = 0;
        //TODO: doesnt work for methodcall/assignment/declaration chained with for/if/while yet (cause it adds ;)
        for(String line : codeLines){
            if(!line.matches(StatementType.FOR.getRegex()))
            {
                if(line.matches(".*;"))
                for(String s : line.split(";")){
                    codeLineArray.add(s+";");
                }
                else codeLineArray.add(line);
            }
            else codeLineArray.add(line);
//            i++;
        }
        return parseProgramCode(codeLineArray);
    }
    public ComplexStatement parseProgramCode(List<String> codeLines) throws IllegalAccessException,IllegalArgumentException {
        behaviour = new ComplexStatement();
        int depth = 1;
        depthStatementMap = new HashMap<>();
        depthStatementMap.put(0,behaviour);
        for ( String code : codeLines){
            if(code.equals("")){
                depthStatementMap.get(depth - 1).addSubStatement(new SimpleStatement());
                continue;
            }
            currentLine++;
            currentForVariable = null;
            code = code.trim();
            code = Util.removeUnnecessarySpace(code);
            if(code.equals("}")){
                depth--;
                continue;
            }
//            if(code.matches(" *")){
//                System.out.println("Empty Line found (remove this in CodeParser -> parseProgramCode(...))");
////                continue;
////            }
            Statement statement = parseString(code,depth);
            if(statement==null )throw new IllegalArgumentException("Unknown statement: \""+code+"\"!");
            if (statement.isComplex()){
                if(statement.getStatementType() == StatementType.ELSE){
                    if(testIfElseCanStandHere(depth)) throw new IllegalArgumentException("Else cannot stand here");
                    ((ConditionalStatement)depthStatementMap.get(depth)).setElseStatement(((ConditionalStatement)statement));
                }
                if (depthStatementMap.containsKey(depth))
                    depthStatementMap.replace(depth, (ComplexStatement) statement);
                else
                    depthStatementMap.put(depth, (ComplexStatement) statement);

                depthStatementMap.get(depth - 1).addSubStatement(statement);

                if(depth > GameConstants.MAX_DEPTH)throw new IllegalStateException("You are not allowed to have a greater depth than "+ GameConstants.MAX_DEPTH +"!"); //TODO: evaluate if I want to keep that!
                depth++;
            }
            else {
                depthStatementMap.get(depth - 1).addSubStatement(statement);
                if(statement.getStatementType() == StatementType.DECLARATION){
                    Variable variable = ((Assignment)statement).getVariable();
                    boolean isSkeleton = variable.getVariableType() == VariableType.SKELETON;
//                    boolean isGhost = variable.getVariableType() == VariableType.GHOST;
                    if(isPlayerCode && (/*isGhost||*/isSkeleton ))throw new IllegalAccessException("You are not allowed to create Enemy Creatures as Player");
                    boolean isKnight = variable.getVariableType() == VariableType.KNIGHT;
                    if(!isPlayerCode && isKnight )throw new IllegalAccessException("You are not allowed to create Player Creatures as Enemy");
                    if(/*isGhost ||*/ isKnight || isSkeleton){
                        if(variable.getValue().getLeftNode()!=null){
                            VariableType variableType = VariableType.getVariableTypeFromString(variable.getValue().getLeftNode().getText().substring(4));
                            if(variableType != variable.getVariableType())throw new IllegalArgumentException(((Assignment) statement).getText()+ " is an illegal expression!");
                        }else {
                            VariableType variableType = VariableType.getVariableTypeFromString(variable.getValue().getText().substring(4,variable.getValue().getText().length()-2));
                            if(variableType != variable.getVariableType())throw new IllegalArgumentException(((Assignment) statement).getText()+ " is an illegal expression!");
                        }
                    }
                    statement.getParentStatement().addLocalVariable(((Assignment)statement).getVariable());
                }
            }
            lastStatement = statement;
        }
        behaviour.resetVariables(true); //TODO: mit map arbeiten stattdessen? (nur hier)
//        depthStatementMap = new HashMap<>();
        if(depth != 1)throw new IllegalStateException("Unbalanced amount of brackets!");
        return behaviour;
    }


    private boolean testIfElseCanStandHere(int depth) {
        if(!depthStatementMap.containsKey(depth))return true;
        boolean noIfBefore = depthStatementMap.get(depth).getStatementType()!=StatementType.IF;
        boolean noElseBefore = depthStatementMap.get(depth).getStatementType()!=StatementType.ELSE;
        //boolean noSimple = lastStatement.getStatementType()!=StatementType.SINGLE; //TODO: what does this do?
        boolean lastStatementIllegal = depth == lastStatement.getDepth()-1 && !lastStatement.isComplex();
//                    System.out.println(statement.getText() + " " +depth + " " + lastStatement.getDepth());
        if((noIfBefore && noElseBefore)|| lastStatementIllegal)return true;
        boolean noConditionSet = ((ConditionalStatement)depthStatementMap.get(depth)).getCondition()==null;
        if(noConditionSet  )
            return true;
        return false;
    }






    private Statement parseString(String code, int depth) throws IllegalAccessException,IllegalArgumentException {
        if(code.matches(" *")){
            return new SimpleStatement();
        }
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
            StringPair tempStatements1 = splitAtChar(code,';',false); //split once
            StringPair tempStatements2 = splitAtChar(tempStatements1.second(),';',false);
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
            Assignment assignment = parseAssignment(code,depth);
            return assignment;
        }
        else if(decMatcher.matches()){
            code = decMatcher.toMatchResult().group();
            code = Util.stripCode(code);
            Assignment assignment = parseAssignment(code,depth);
            return assignment;
        }
        else if(mcMatcher.matches()){
            code = mcMatcher.toMatchResult().group();
            code = Util.stripCode(code);
            MethodCall methodCall = parseMethodCall(code,depth);
            return methodCall;
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


    private StringPair splitAtString(String code, String regex) {

        String[] parts = code.split(regex);
        return  new StringPair(parts[0],parts[1]);
    }

    private MethodCall parseMethodCall(String code,int depth) throws IllegalArgumentException {
        //System.out.println(code);
        //TODO: Differentiate between methods with and methods without a TurnTaken!
        StringPair tempStatements = splitAtChar(code,'.',false);
        String objectName = tempStatements.first();
        tempStatements = splitAtChar(tempStatements.second(),'(',false);
        String methodName = tempStatements.first();
        String parameters = tempStatements.second().substring(0,tempStatements.second().length()-1);
//            throw new IllegalStateException(parameters+ " contains unknown Variables!");
        Variable v = depthStatementMap.get(depth-1).getVariable(objectName);
        if(v==null)
            throw new IllegalArgumentException("Variable inside MethodCall " +objectName +" not in scope!");
        else if (!(v.getVariableType() == VariableType.ARMY ||v.getVariableType() == VariableType.KNIGHT ||v.getVariableType() == VariableType.SKELETON)){
            throw new IllegalArgumentException("Only Knights, Armies or Skeletons may call methods!");
        }
        MethodType mType = MethodType.getMethodTypeFromName(methodName);
        if(mType == null) throw new IllegalArgumentException("Method " + methodName + " is not a valid method!");
        if(mType.getOutputType() != VariableType.ACTION) throw new IllegalArgumentException("Method " + methodName + " cannot stand here!");
        if(isPlayerCode && mType == MethodType.ATTACK) throw new IllegalArgumentException("Knights cannot attack!");
        testForCorrectParameters(parameters,mType,depth);
        if(!parameters.equals("")){
            if(mType == MethodType.EXECUTE_IF){
                String[] parameterList = parameters.split(",");
                checkConditionForUnknownVars(Condition.getConditionFromString(parameterList[0]), depth);
                checkExpressionTreeForUnknownVars(new ExpressionLeaf(parameterList[1]),depth);
                checkExpressionTreeForUnknownVars(new ExpressionLeaf(parameterList[2]),depth);
            }
            else checkExpressionTreeForUnknownVars(new ExpressionLeaf(parameters),depth);
        }
        return new MethodCall(MethodType.getMethodTypeFromCall(methodName+"("+parameters+")"),objectName,parameters);
    }

    private void testForCorrectParameters(String parameters, MethodType mType,int depth) {

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
            case TARGET_CELL_IS:
                CContent content = CContent.getValueFromName(parameters);
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
                if(parameters.matches(VariableType.TURN_DIRECTION.getAllowedRegex())||(depthStatementMap.get(depth-1).getVariable(parameters)!=null &&depthStatementMap.get(depth-1).getVariable(parameters).getVariableType()==VariableType.TURN_DIRECTION&&!depthStatementMap.get(depth-1).getVariable(parameters).getValue().getText().equals(""))){
                    return;
                }
                break;
            case EXECUTE_IF:
                String[] parameterList = parameters.split(",");
                checkConditionForUnknownVars(Condition.getConditionFromString(parameterList[0]), depth);
                if(parameterList.length < 3)throw new IllegalArgumentException("Not enough paramters in method Call!");
                boolean parameter1Okay = parameterList[1].matches(VariableType.COMMAND.getAllowedRegex())||(depthStatementMap.get(depth-1).getVariable(parameterList[1])!=null &&depthStatementMap.get(depth-1).getVariable(parameterList[1]).getVariableType()==VariableType.COMMAND&&!depthStatementMap.get(depth-1).getVariable(parameterList[1]).getValue().getText().equals(""));
                boolean parameter2Okay = parameterList[2].matches(VariableType.COMMAND.getAllowedRegex())||(depthStatementMap.get(depth-1).getVariable(parameterList[2])!=null &&depthStatementMap.get(depth-1).getVariable(parameterList[2]).getVariableType()==VariableType.COMMAND&&!depthStatementMap.get(depth-1).getVariable(parameterList[2]).getValue().getText().equals(""));
                if(!parameter1Okay)throw new IllegalArgumentException(parameterList[1] + " is not a valid Command!");
                else if(!parameter2Okay)throw new IllegalArgumentException(parameterList[2] + " is not a valid Command!");
                else {
                    ExpressionTree exp1 =Variable.evaluateVariable(parameterList[1],depthStatementMap.get(depth-1));
                    ExpressionTree exp2 =Variable.evaluateVariable(parameterList[2],depthStatementMap.get(depth-1));
                    testForCorrectParameters(exp1.getRightNode().getText(),MethodType.getMethodTypeFromName(exp1.getLeftNode().getText()),depth);
                    testForCorrectParameters(exp2.getRightNode().getText(),MethodType.getMethodTypeFromName(exp2.getLeftNode().getText()),depth);
                return;}
            case IS_LOOKING:
                if(parameters.matches(VariableType.DIRECTION.getAllowedRegex())||(depthStatementMap.get(depth-1).getVariable(parameters)!=null &&depthStatementMap.get(depth-1).getVariable(parameters).getVariableType()==VariableType.DIRECTION&&!depthStatementMap.get(depth-1).getVariable(parameters).getValue().getText().equals(""))){
                    return;
                }
                break;
        }
        if(parameters.equals("")) throw new IllegalArgumentException("Method "+ mType.getName()+" needs parameters!");
        throw new IllegalArgumentException(parameters+" is not a correct parameter!");
    }


    private Assignment parseAssignment(String statementString,int depth) throws IllegalAccessException {
        String variable="";
        String value="";
        if (statementString.contains("++")){
            variable=statementString.replaceAll("\\+\\+","");
            value = variable+"+1";
        }
        else if (statementString.contains("--")){
            variable=statementString.replaceAll("--","");
            value = variable+"-1";
        }
        else {
            StringPair tempStatements = splitAtChar(statementString,'=',false);
            variable = tempStatements.first();
            value = tempStatements.second();
        }
        value = value.trim();
        ExpressionTree valueTree = ExpressionTree.expressionTreeFromString(value.trim()); //0
        Condition condition = Condition.getConditionFromString(value.trim());
        //TODO: expand to make it more readable!
        if(valueTree.getLeftNode() != null){
            String vTypeString = valueTree.getLeftNode().getText().replaceAll("new ","");
         if( EntityType.getValueFromName(vTypeString)!=EntityType.NONE && !valueTree.getRightNode().getText().equals("")&& Direction.getValueFromString(valueTree.getRightNode().getText())==null)
             throw new IllegalArgumentException(valueTree.getRightNode().getText()+ " is not a Direction!");
        }
//            throw new IllegalStateException(valueTree.getText()+ " contains unknown Variables!");
        StringPair variableTypeAndName = splitAtChar(variable,' ',false);
        String variableName;
        String variableTypeString = "";
//        System.out.println("\""+variableTypeAndName.second()+"\"");
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

        if(variableTypeString.equals("")&&value.trim()=="")throw new IllegalArgumentException("You cannot assign an empty value!");
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
    private void testForCorrectValueType(VariableType variableType, String value, int depth) {
        value = value.trim();
        if(value.equals(""))throw new IllegalArgumentException("You cannot have an empty value!");
        if(depthStatementMap.get(depth-1) == null)throw new IllegalStateException("You cant have this statement here!");
        Variable v = depthStatementMap.get(depth-1).getVariable(value);
        if(v!=null){
            if(v.getVariableType() != variableType)throw new IllegalArgumentException(value + " has the wrong type!");
            if(depthStatementMap.get(depth-1).getStatementType() == StatementType.FOR){
                Variable forVar = ((ForStatement)depthStatementMap.get(depth-1)).getDeclaration().getVariable();
                if(forVar.getName().equals(value))testForCorrectValueType(variableType, forVar.getValue().getText(),depth);
                else testForCorrectValueType(variableType, value,depth-1);
            }
            else testForCorrectValueType(variableType, depthStatementMap.get(depth-1).getVariable(value).getValue().getText(),depth);
            return;
        }
        switch (variableType){
            case INT:
                if(value.matches("[+-]\\d+"))return;
                ExpressionTree tree = ExpressionTree.expressionTreeFromString(value);
                if(tree.getRightNode()!=null){
                    testForCorrectValueType(VariableType.INT,tree.getLeftNode().getText(),depth );
                    testForCorrectValueType(VariableType.INT,tree.getRightNode().getText(),depth );
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
                    if(tree.getRightNode() != null && depthStatementMap.get(depth-1).getVariable(tree.getRightNode().getText()) != null && depthStatementMap.get(depth-1).getVariable(tree.getRightNode().getText()).getVariableType() == VariableType.DIRECTION)return;
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
                CContent content = CContent.getValueFromName(value);
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
                    if(checkForDoppelgangers(parameters))throw  new IllegalArgumentException("You shall not add the same Entity more than one time!");
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
            case COMMAND:
                if(!value.matches(variableType.getAllowedRegex())){
                    if(value.matches(MethodType.EXECUTE_IF.getRegex()))
                        throw new IllegalArgumentException("ExecuteIf is not allowed as a command!");
                    throw new IllegalArgumentException(value + " is not a valid Command!");
                }
                else {
                    if(value.matches(MethodType.EXECUTE_IF.getRegex())){
                        Matcher m = Pattern.compile(MethodType.EXECUTE_IF.getRegex()).matcher(value);
                        if(m.matches())testForCorrectParameters(m.group(1),MethodType.EXECUTE_IF,depth);
                    }else {
                        String parameters = value.replaceFirst(".+\\(", "").replace(")", "");
                        value = value.replaceFirst("\\(.*\\)", "");
                    MethodType mt = MethodType.getMethodTypeFromName(value);
                    if(mt == null) throw new IllegalArgumentException("Unknown method: " +value);

                    testForCorrectParameters(parameters,mt,depth);
                    }

                    return;
                }

        }
    }

    private boolean checkForDoppelgangers(String[] parameters) {
        List<String> checkedList = new ArrayList<>();
        for(String p : parameters){
            if(checkedList.contains(p))return true;
            checkedList.add(p);
        }
        return false;
    }

    private void checkConditionForUnknownVars(Condition condition, int depth) {
        if(condition == null||condition.getText().equals(""))throw new IllegalArgumentException("You cannot have an empty Condition!");
        testForCorrectValueType(VariableType.BOOLEAN,condition.getText(),depth);
        if(condition.getText().matches(" *"))return;
        if(condition.getConditionType() != SINGLE){
            checkConditionForUnknownVars(((ConditionTree)condition).getRightCondition(),depth);
            if(condition.getConditionType()!=NEGATION)checkConditionForUnknownVars(((ConditionTree)condition).getLeftCondition(),depth);
            return;
        }
        else{
            if(condition.getText().matches("true|false"))return;
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

    private void checkExpressionTreeForUnknownVars(ExpressionTree valueTree,int depth) {
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

    public static StringPair splitAtChar(String code, char targetChar,boolean keepCharacter) {

        String first ="";
        String second="";
        boolean found=false;
        for(int i = 0; i < code.length(); i++){
            char c = code.charAt(i);
            if(!found)first = first.concat(c+"");
            else second = second.concat(c+"");

            if(c==targetChar){
                found=true;
            }
        }
        if(!keepCharacter && found)first = first.substring(0,first.length()-1);
        return  new StringPair(first,second);
    }
}
