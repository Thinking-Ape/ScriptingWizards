package parser;

import model.enums.*;
import model.util.GameConstants;
import model.statement.*;
import model.statement.Condition.*;
import model.statement.Expression.ExpressionLeaf;
import model.statement.Expression.ExpressionTree;
import model.statement.Expression.ExpressionType;
import model.util.StringPair;
import model.util.Variable;
import model.util.VariableType;

import java.util.*;
import java.util.regex.Matcher;

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
            code = removeUnnecessarySpace(code);
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

    private String removeUnnecessarySpace(String code) {
        String output = "";
        for (int i = 0; i < code.length(); i++){
            char c = code.charAt(i);
            if(i == code.length()-1){
                if(c != ' ') output+=c;
                break;
            }
            char cc = code.charAt(i+1);
            if(c == ' ' && (cc == ' '|| cc ==';'))continue;
            if(c == ' ' && cc == ')')continue;
            output+=c;
        }
        return output;
    }

    private boolean testIfElseCanStandHere(int depth) {
        if(!depthStatementMap.containsKey(depth))return true;
        boolean noIfBefore = depthStatementMap.get(depth).getStatementType()!=StatementType.IF;
        boolean noElseBefore = depthStatementMap.get(depth).getStatementType()!=StatementType.ELSE;
        //boolean noSimple = lastStatement.getStatementType()!=StatementType.SIMPLE; //TODO: what does this do?
        boolean lastStatementIllegal = depth == lastStatement.getDepth()-1 && !lastStatement.isComplex();
//                    System.out.println(statement.getText() + " " +depth + " " + lastStatement.getDepth());
        if((noIfBefore && noElseBefore)|| lastStatementIllegal)return true;
        boolean noConditionSet = ((ConditionalStatement)depthStatementMap.get(depth)).getCondition()==null;
        if(noConditionSet  )
            return true;
        return false;
    }

    private Condition conditionFromString(String code, int level) throws IllegalArgumentException {
        int depth = 0;
        code = code.trim();
        if(code.matches(" *"))throw new IllegalArgumentException("You cannot have an empty condition!");
        for(int i =0; i < code.length();i++){
            char c = code.charAt(i);
            char cc = ' ';
            if(c == '(') depth++;
            if(c == ')') depth--;
            if(i < code.length()-1) cc = code.charAt(i+1);

            boolean foundAnd = (c == '&' && cc == '&');
            boolean foundOr = (c == '|' && cc == '|');
            if( foundAnd && depth == 0){
                String firstArgument = stripCode(code.substring(0,i));
                String secondArgument = stripCode(code.substring(i+2));
//                return new ConditionTree(conditionFromString(firstArgument,level+1), ConditionType.AND, conditionFromString(secondArgument,level+1),level+1);
                return new ConditionTree(conditionFromString(firstArgument,level), ConditionType.AND, conditionFromString(secondArgument,level));
            } else if( foundOr && depth == 0){
                if(! (i+2 < code.length()-1)) throw new IllegalArgumentException("'" + c + "' is not allowed to stand here");
                String firstArgument = stripCode(code.substring(0,i));
                String secondArgument = stripCode(code.substring(i+2));
                return new ConditionTree(conditionFromString(firstArgument,level), ConditionType.OR, conditionFromString(secondArgument,level));
            }
        }for(int i =0; i < code.length();i++){
            char c = code.charAt(i);
            char cc = ' ';
            if(c == '(') depth++;
            if(c == ')') depth--;
            if(i < code.length()-1) cc = code.charAt(i+1);

            boolean foundNeg = (c == '!'&& cc != '=');
            if( foundNeg && depth == 0){
                if(! (i+2 < code.length()-1)) throw new IllegalArgumentException("'" + c + "' is not allowed to stand here");
                //TODO: NEGATIONS!!!
//                String firstArgument = stripCode(code.substring(0,i));
                String secondArgument = stripCode(code.substring(i+1));
                return new ConditionTree(null, ConditionType.NEGATION, conditionFromString(secondArgument,level));
            }
        }

//        ExpressionTree expressionTree = ExpressionTree.expressionTreeFromString(stripCode(code),0);
//        System.out.println(expressionTree.getText());
        return parseSimpleCondition(code,level);
    }

    private static String stripCode(String substring) {
        substring = substring.trim();
        if(substring.equals("")) return  substring;
        if(substring.charAt(substring.length()-1) == ';')
            return substring.substring(0,substring.length()-1);
        if(substring.charAt(0)=='('&&substring.charAt(substring.length()-1)==')'){
            return substring.substring(1,substring.length()-1);
        }
        else return  substring;
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
            Condition condition = conditionFromString(code,depth);
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
            Condition condition = conditionFromString(statements[1],depth);
            return  new ForStatement(assignment,condition,parseAssignment(statements[2],depth));
    }
        else if(ifMatcher.matches()){
            code = ifMatcher.group(1);
            Condition condition = conditionFromString(code,depth);
            return new ConditionalStatement(condition,false);
        }
        else if(elseMatcher.matches()){
            code = elseMatcher.group(2);
            if(code!= null) {
                return new ConditionalStatement(conditionFromString(code,depth),true);
            }
            return new ConditionalStatement(null,true);
        }
        else if(mcMatcher.matches()){
            code = mcMatcher.toMatchResult().group();
            code = stripCode(code);
            MethodCall methodCall = parseMethodCall(code,depth);
            return methodCall;
        }
        else if(asMatcher.matches()){
            code = asMatcher.toMatchResult().group();
            code = stripCode(code);
            Assignment assignment = parseAssignment(code,depth);
            return assignment;
        }
        else if(decMatcher.matches()){
            code = decMatcher.toMatchResult().group();
            code = stripCode(code);
            Assignment assignment = parseAssignment(code,depth);
            return assignment;
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
        if(!parameters.equals("")&&checkExpressionTreeForUnknownVars(new ExpressionLeaf(parameters),depth))
            throw new IllegalStateException(parameters+ " contains unknown Variables!");
        if(depthStatementMap.get(depth-1).getVariable(objectName)==null)throw new IllegalArgumentException("Variable " +objectName +" not in scope!");
        MethodType mType = MethodType.getMethodTypeFromName(methodName);
        if(mType == null) throw new IllegalArgumentException("Method " + mType + " is not a valid method!");
        testForCorrectParameters(parameters,mType);
        return new MethodCall(MethodType.getMethodTypeFromCall(methodName+"("+parameters+")"),objectName,parameters);
    }

    private void testForCorrectParameters(String parameters, MethodType mType) {

        switch (mType){

            case TARGET_IS_DANGER:
            case WAIT:
            case MOVE:
            case USE_ITEM:
            case COLLECT:
            case CAN_MOVE:
                if(!parameters.equals(""))throw new IllegalArgumentException("This method doesnt have parameters!");
                return;
            case HAS_ITEM:
                ItemType item = ItemType.getValueFromName(parameters);
                if(item != null ||parameters.equals("ANY")){
                    return;
                }
                break;
            case TARGET_CELL_IS:
                CContent content = CContent.getValueFromName(parameters);
                if(content != null){
                    return;
                }
                break;
            case TARGET_CONTAINS:
                item = ItemType.getValueFromName(parameters);
                EntityType entityType = EntityType.getValueFromName(parameters);
                if(item != null ||entityType != null){
                    return;
                }
                break;
            case TURN:
                if(parameters.equals("AROUND") || parameters.equals("LEFT")|| parameters.equals("RIGHT")){
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
        ExpressionTree valueTree = ExpressionTree.expressionTreeFromString(value.trim()); //0
        //TODO: expand to make it more readable!

        if(valueTree.getLeftNode() != null){
            String vTypeString = valueTree.getLeftNode().getText().replaceAll("new ","");
         if( EntityType.getValueFromName(vTypeString)!=null && !valueTree.getRightNode().getText().equals("")&&Direction.getValueFromString(valueTree.getRightNode().getText())==null)  throw new IllegalArgumentException(valueTree.getRightNode().getText()+ " is not a Direction!");
        }
        if(checkExpressionTreeForUnknownVars(valueTree,depth))
            throw new IllegalStateException(valueTree.getText()+ " contains unknown Variables!");
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
        Variable variable1 = null;
        if(variableTypeString.equals("")){
            isDeclaration = false;
            ComplexStatement complexStatement = depthStatementMap.get(depth-1);
            if (complexStatement != null && currentForVariable == null){
                variable1 = complexStatement.getVariable(variableName.trim());
                if(variable1 == null) throw new IllegalArgumentException("Variable " + variableName+ " not in scope!");
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

    private boolean checkExpressionTreeForUnknownVars(ExpressionTree valueTree,int depth) {
        if(valueTree.getText().matches(" *"))return false;
        if(valueTree.getExpressionType() != ExpressionType.SIMPLE){
            return checkExpressionTreeForUnknownVars(valueTree.getRightNode(),depth)||checkExpressionTreeForUnknownVars(valueTree.getLeftNode(),depth);
        }
        else{
            if(valueTree.getText().matches("new .*\\(.*\\)"))return false;
//            if(valueTree.getText().matches("\\(.*\\)"))
//                return checkExpressionTreeForUnknownVars(ExpressionTree.expressionTreeFromString(valueTree.getText().substring(1,valueTree.getText().length()-1)),depth);
            //TODO: only allow the right ENUMS when writing those methods!
            if(valueTree.getText().matches("(-?\\d+|LEFT|RIGHT|AROUND|false|true|EAST|NORTH|SOUTH|WEST|"+GameConstants.RAND_INT_REGEX+")"))return false; //TODO: CHECK PARAMETERS!
            if(currentForVariable != null && valueTree.getText().equals(currentForVariable.getName())) return false;
            return depthStatementMap.get(depth-1).getVariable(valueTree.getText())==null;
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

    // ERWARTET KORREKTE KLAMMERUNG!!
    public ConditionLeaf parseSimpleCondition(String code,int depth) {
        code = code.trim();
        //TODO: Code Duplicate!!
        if(code.matches(".+\\..+\\(.*\\)")){
        String[] tempStatements = code.split("\\.");
        if(tempStatements.length > 1){
            String objectName = tempStatements[0];
        tempStatements = tempStatements[1].split("\\(");
        String methodName = tempStatements[0];
        String parameters = tempStatements[1].substring(0,tempStatements[1].length()-1);

            if(checkExpressionTreeForUnknownVars(new ExpressionLeaf(objectName),depth))
                throw new IllegalArgumentException(objectName+ " contains unknown Variables!");
            MethodType mType = MethodType.getMethodTypeFromName(methodName);
            if(mType == null)throw new IllegalArgumentException(methodName+ " is unknown!");
            testForCorrectParameters(parameters,mType);

        }}
        for(int i = 0; i < code.length(); i++){
            char c = code.charAt(i);
            char cc = GameConstants.ANY_CHAR;
            if(i < code.length()-1)cc = code.charAt(i+1);
            BooleanType simpleConditionType = BooleanType.getConditionTypeFromChars(c,cc);
            if(simpleConditionType== BooleanType.BOOLEAN) continue;
            if(i == code.length()-1)throw new IllegalArgumentException(code + " lacks an argument");
            int j = simpleConditionType.getSecondCharacter() == GameConstants.ANY_CHAR ? 1 : 2;
            ExpressionTree leftTree = ExpressionTree.expressionTreeFromString(code.substring(0,i));
            ExpressionTree rightTree = ExpressionTree.expressionTreeFromString(code.substring(i+j));
            if(checkExpressionTreeForUnknownVars(leftTree,depth))
                throw new IllegalArgumentException(leftTree.getText()+" contains unknown Variables");
            if(checkExpressionTreeForUnknownVars(rightTree,depth))
                throw new IllegalArgumentException(rightTree.getText()+" contains unknown Variables");
            return new ConditionLeaf(leftTree,simpleConditionType,rightTree);

        }
        ExpressionTree expressionTree = ExpressionTree.expressionTreeFromString(code);
        if(!code.matches(".+\\..+\\(.*\\)|true|false"))throw new IllegalArgumentException(code+ " is not a known boolean!");
        return  new ConditionLeaf(expressionTree, BooleanType.BOOLEAN,null);
    }

}
