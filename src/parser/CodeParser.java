package parser;

import model.enums.*;
import utility.*;
import model.statement.*;
import model.statement.Condition.*;
import model.statement.Expression.ExpressionLeaf;
import model.statement.Expression.ExpressionTree;
import model.statement.Expression.ExpressionType;

import java.util.*;
import java.util.regex.Matcher;

import static model.statement.Condition.ConditionType.SINGLE;

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
            if(statement==null )throw new IllegalArgumentException("Unknown command: \""+code+"\"!");
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
//            System.out.println(condition.getText());
//            globalDepth++;
//            Condition condition = parseSimpleCondition(expressionTree.getText());
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
        if(!parameters.equals(""))checkExpressionTreeForUnknownVars(new ExpressionLeaf(parameters),depth);
//            throw new IllegalStateException(parameters+ " contains unknown Variables!");
        if(depthStatementMap.get(depth-1).getVariable(objectName)==null)
            throw new IllegalArgumentException("Variable " +objectName +" not in scope!");
        MethodType mType = MethodType.getMethodTypeFromName(methodName);
        if(mType == null) throw new IllegalArgumentException("Method " + mType + " is not a valid method!");
        testForCorrectParameters(parameters,mType,depth);
        return new MethodCall(MethodType.getMethodTypeFromCall(methodName+"("+parameters+")"),objectName,parameters);
    }

    private void testForCorrectParameters(String parameters, MethodType mType,int depth) {

        switch (mType){

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
                if(item != null ||parameters.equals("")){
                    return;
                }
                break;
            case TARGET_CELL_IS:
                CContent content = CContent.getValueFromName(parameters);
                if(content != null){
                    return;
                }
                break;
            case TARGET_CONTAINS_ENTITY:
                EntityType entityType = EntityType.getValueFromName(parameters);
                if(entityType != null|| parameters.equals("")){
                    return;
                }
                break;
            case TARGET_CONTAINS_ITEM:
                item = ItemType.getValueFromName(parameters);
                if(item != null || parameters.equals("")){
                    return;
                }
                break;
            case TURN:
                if(parameters.equals("AROUND") || parameters.equals("LEFT")|| parameters.equals("RIGHT")||(depthStatementMap.get(depth-1).getVariable(parameters)!=null &&depthStatementMap.get(depth-1).getVariable(parameters).getVariableType()==VariableType.TURN_DIRECTION)){
                    return;
                }
                break;
        }
        if(parameters.equals("")) throw new IllegalArgumentException("This method needs parameters!");
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
//        if(StatementType.getMatcher(StatementType.METHOD_CALL,value ).matches());
        ExpressionTree valueTree = ExpressionTree.expressionTreeFromString(value.trim()); //0
        Condition condition = Condition.getConditionFromString(value.trim());
        //TODO: expand to make it more readable!
        if(valueTree.getLeftNode() != null){
            String vTypeString = valueTree.getLeftNode().getText().replaceAll("new ","");
         if( EntityType.getValueFromName(vTypeString)!=null && !valueTree.getRightNode().getText().equals("")&&Direction.getValueFromString(valueTree.getRightNode().getText())==null)
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
//        System.out.println(variableName);
        //TODO: make cleaner
        boolean isDeclaration = true;

        VariableType variableType = VariableType.getVariableTypeFromString(variableTypeString.trim());
        if(variableType == VariableType.BOOLEAN)
            checkConditionForUnknownVars(condition,depth);
        else
            checkExpressionTreeForUnknownVars(valueTree,depth);
        testForCorrectValueType(variableType,value,depth);
        Variable variable1 = null;
        if(variableTypeString.equals("")){
            isDeclaration = false;
            ComplexStatement complexStatement = depthStatementMap.get(depth-1);
            if (complexStatement != null && currentForVariable == null){
                variable1 = complexStatement.getVariable(variableName.trim());
                if(variable1 == null)
                    throw new IllegalArgumentException("Variable " + variableName+ " not in scope!");
                variableType = variable1.getVariableType();
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
        Variable v = depthStatementMap.get(depth-1).getVariable(value);
        if(v!=null){
            if(v.getVariableType() != variableType)throw new IllegalArgumentException(value + " has the wrong type!");
            testForCorrectValueType(variableType, depthStatementMap.get(depth-1).getVariable(value).getValue().getText(),depth);
            return;
        }
        switch (variableType){
            case INT:
                if(!value.matches(GameConstants.RAND_INT_REGEX)&&!value.matches("[+-]?\\d+"))throw new IllegalArgumentException(value + " is no number!");
                else return;
            case BOOLEAN:
//                if(!value.matches("true|false"))throw new IllegalArgumentException(value + " is no boolean!");
//                else return;
                return;
            case KNIGHT:
                if(!value.matches("new Knight\\((EAST|WEST|NORTH|SOUTH)?\\)"))throw new IllegalArgumentException(value + " is not a valid Knight constructor!");
                break;
            case SKELETON:
                if(!value.matches("(new Skeleton\\((EAST|WEST|NORTH|SOUTH)?\\)|new Skeleton\\([0-9]+\\)|new Skeleton\\((EAST|WEST|NORTH|SOUTH),[0-9]+\\))"))throw new IllegalArgumentException(value + " is not a valid Skeleton constructor!");
                break;
            case DIRECTION:
                Direction dir = Direction.getValueFromString(value);
                if(dir == null)throw new IllegalArgumentException(value + " is no Direction!");
                else return;
            case TURN_DIRECTION:
                if(!value.matches("LEFT|AROUND|RIGHT"))throw new IllegalArgumentException(value + " is no TurnDirection!");
                else return;
            case CELL_CONTENT:
                CContent content = CContent.getValueFromName(value);
                if(content == null)throw new IllegalArgumentException(value + " is no CellContent!");
                else return;
            case ITEM_TYPE:
                ItemType itemType = ItemType.getValueFromName(value);
                if(itemType == null)throw new IllegalArgumentException(value + " is no ItemType!");
                else return;
            case ENTITY_TYPE:
                EntityType entityType = EntityType.getValueFromName(value);
                if(entityType == null)throw new IllegalArgumentException(value + " is no EntityType!");
                else return;
            case DEFAULT:
                break;
        }
    }

    private void checkConditionForUnknownVars(Condition condition, int depth) {
        if(condition == null)return;
        testForCorrectValueType(VariableType.BOOLEAN,condition.getText(),depth);
        if(condition.getText().matches(" *"))return;
        if(condition.getConditionType() != SINGLE){
            checkConditionForUnknownVars(((ConditionTree)condition).getRightNode(),depth);
            checkConditionForUnknownVars(((ConditionTree)condition).getLeftNode(),depth);
            return;
        }
        else{
            if(condition.getText().matches("true|false"))return;
            ConditionLeaf conditionLeaf = (ConditionLeaf)condition;
            switch(conditionLeaf.getSimpleConditionType()){
                case SIMPLE:
                    if(depthStatementMap.get(depth-1).getVariable(condition.getText())==null) throw new IllegalArgumentException("Variable "+condition.getText()+" not in scope!");
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
                    if(mT==null)throw new IllegalArgumentException("No such Method " + conditionLeaf.getLeftTree().getText()+"!");
                    if(mT.getOutputType()!=VariableType.BOOLEAN)throw new IllegalArgumentException("Method " + conditionLeaf.getLeftTree().getText()+" has illegal type!");
                    testForCorrectParameters(conditionLeaf.getRightTree().getRightNode().getText(), mT, depth);
                    return;
            }
        }
        throw new IllegalArgumentException(condition.getText() + " is not allowed to stand here!");
    }

    private void checkExpressionTreeForUnknownVars(ExpressionTree valueTree,int depth) {
        if(valueTree.getText().matches(" *"))return;
        if(valueTree.getExpressionType() != ExpressionType.SIMPLE){
            checkExpressionTreeForUnknownVars(valueTree.getRightNode(),depth);
            checkExpressionTreeForUnknownVars(valueTree.getLeftNode(),depth);
            return;
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
            if(valueTree.getText().matches("(-?\\d+|LEFT|RIGHT|AROUND|EAST|NORTH|SOUTH|WEST|"+GameConstants.RAND_INT_REGEX+")"))return;
            if(currentForVariable != null && valueTree.getText().equals(currentForVariable.getName())) return;
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
