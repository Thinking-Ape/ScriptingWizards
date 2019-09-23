package model.statement;

import model.statement.Condition.ConditionTree;

import java.util.ArrayList;

@Deprecated
public class ElseStatement extends ComplexStatement {

    ConditionTree elifCondition = null;

    public ElseStatement(){
        super();
        this.statementType =StatementType.ELSE;
    }
    public ElseStatement(ConditionTree elifCondition){
        super();
        this.statementType =StatementType.ELSE;
        this.elifCondition = elifCondition;
    }
//    @Override
//    public void print() throws IllegalAccessException {
//        System.out.println("else{");
//
//        for (Statement statement : statementList){
//            System.out.print("  ");
//            statement.print();
//        }
//        System.out.println("}");
//    }

}

/* with iterator:
package model;

import model.enums.*;
import model.statement.*;
import model.statement.Condition.ConditionLeaf;
import model.statement.Condition.ConditionTree;
import model.statement.Expression.ExpressionTree;
import model.statement.Expression.ExpressionType;
import model.util.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Level extends EventSender {

    private Cell[][] originalMap; //eventuell in eigene Klasse auslagern
    private Cell[][] currentMap;
    private int turnsTaken; //TODO: ? maxTurns
    private int knightsUsed; //TODO: ? maxknights
    private final String NAME;
    private final int HEIGHT, WIDTH;// ,ID;
    private Map<String,Cell> entityCellMap;
//    private Map<EntityType, ComplexStatement> knightTypeBehaviourMap; // dann wird Entity anders?? kein behavior?
    private Point spawn;
    private Point[] enemySpawnArray;
//    private Evaluator evaluator;
    private int noStackOverflow = 0;
    private Statement currentStatement;

//    private VariableDepthMap variableDepthMap;
//    private Map<Integer,Statement> depthStatementMap = new HashMap<>();
    private ComplexStatement aiBehaviour;
    private ComplexStatement playerBehaviour;
    private StatementIterator playerIterator;
    private StatementIterator aiIterator;
    private boolean isWon = false;

    public Level(String name, Cell[][] originalMap, Point spawn, List<Point> enemySpawnList,ComplexStatement aiBehaviour) {
        this.NAME = name;
        this.HEIGHT = originalMap.length; //TODO: stimmt das?
        this.WIDTH = originalMap[0].length; //TODO: stimmt das?
        this.originalMap = originalMap;
        setStandardFlags(originalMap);
        this.currentMap = clone(originalMap);
        this.turnsTaken = 0;
        this.knightsUsed = 0;
        this.entityCellMap = new HashMap<>();
        this.spawn = spawn;
//        this.variableDepthMap = new VariableDepthMap();
        this.enemySpawnArray = new Point[enemySpawnList.size()];
        for(int i = 0; i < enemySpawnList.size(); i++){
            this.enemySpawnArray[i]=enemySpawnList.get(i);
        }
        this.aiBehaviour = aiBehaviour;
        this.aiIterator = new StatementIterator(aiBehaviour);
    }

    private void setStandardFlags(Cell[][] map) {
        for(Cell[] cellRow : map){
            for(Cell cell : cellRow){
                if(cell.getContent().isTraversable()){
                    cell.setFlagValue(CFlag.TRAVERSABLE,true);
                }
                if(cell.getContent().isCollectible()){
                    cell.setFlagValue(CFlag.COLLECTIBLE,true);
                }
            }
        }
    }

    private Cell[][] clone(Cell[][] originalState) {
        Cell[][] output=new Cell[originalState.length][originalState[0].length];
        for(int column = 0; column < originalState.length; column++){
            for(int row = 0; row < originalState[0].length;row++){
                output[column][row] = originalState[column][row].copy();
            }
        }
        return output;
    }

    //TODO: maybe Executor-Class that has a reference to the level?
    public boolean executeBehaviour(Statement statement) {
        boolean method_Called = false;

        if(statement.getStatementType()== StatementType.METHOD_CALL) {
            noStackOverflow = 0;
            MethodCall evaluatedMethodCall = (MethodCall)statement;
            method_Called = true;
            executeMethodCall(evaluatedMethodCall);
        }
        if(statement.getStatementType()== StatementType.DECLARATION) {
            Assignment assignment = (Assignment)statement;
            if(assignment.getVariable().getVariableType() == VariableType.KNIGHT){
                method_Called = true;
                String name = assignment.getVariable().getName();
                Direction direction = Direction.UP;
                if(assignment.getVariable().getValue().getRightNode() != null)direction = Direction.valueOf(assignment.getVariable().getValue().getRightNode().getText().toUpperCase());

                Cell cell = currentMap[spawn.getX()][spawn.getY()];
                cell.setEntity(new Entity(name,direction,EntityType.KNIGHT));
                if(!entityCellMap.containsKey(name)) entityCellMap.put(name,cell);
                else entityCellMap.replace(name,cell);
            }if(assignment.getVariable().getVariableType() == VariableType.SKELETON){ //TODO: stattdessen ENEMY?
                method_Called = true;
                String name = assignment.getVariable().getName();
                Direction direction = Direction.UP;
                String spawnId = "";
                if(assignment.getVariable().getValue().getRightNode() != null){
                    if(assignment.getVariable().getValue().getRightNode().getRightNode() == null)
                        direction = Direction.valueOf(assignment.getVariable().getValue().getRightNode().getText().toUpperCase());
                    else {
                        direction = Direction.valueOf(assignment.getVariable().getValue().getRightNode().getLeftNode().getText().toUpperCase());
                        spawnId = assignment.getVariable().getValue().getRightNode().getRightNode().getText();
                    }
                }

                Cell spawnCell= currentMap[enemySpawnArray[0].getX()][enemySpawnArray[0].getY()];
//                spawnCell.setFlagValue(CFlag.HURTING,true);
                if(spawnId != ""){
                    for(Point point : enemySpawnArray){
                        spawnCell = currentMap[point.getX()][point.getY()];
                        if(spawnCell.getCellId().equals(spawnId)){
                            spawnCell.setEntity(new Entity(name,direction,EntityType.SKELETON));
                        }
                    }
                }
                else spawnCell.setEntity(new Entity(name,direction,EntityType.SKELETON));
                if(!entityCellMap.containsKey(name)) entityCellMap.put(name,spawnCell);
                else entityCellMap.replace(name,spawnCell);
            }

        }
        return method_Called;
    }

    public void executeTurn() throws IllegalAccessException {
        boolean method_Called_1 = false, method_Called_2 = false;
        while(!method_Called_1 && !isWon) {
            if(noStackOverflow >= 100) throw new IllegalStateException("You're not allowed to call more than 100 Lines of Code without a MethodCall!");
            Statement statement = evaluateNext(playerIterator);
            if(statement==null)break;
            noStackOverflow++;

            method_Called_1 = executeBehaviour(statement);

        }
        while(!method_Called_2&& !isWon ) {
            if(noStackOverflow >= 100) throw new IllegalStateException("You're not allowed to call more than 100 Lines of Code without a MethodCall!");
            Statement statement = evaluateNext(aiIterator);
            if(statement==null)break;
            noStackOverflow++;

            method_Called_2 = executeBehaviour(statement);
        }
        //TODO: standalone method
//        if(method_Called_1)
        for (Cell[] cellRow : currentMap) for (Cell cell : cellRow){
            if(cell.getContent() == CContent.GATE){
                for (int i = 0; i < cell.getLinkedCellsSize();i++){
                    if(!findCellWithId(cell.getLinkedCellId(i)).hasFlag(CFlag.TRIGGERED)){
                        if(cell.hasFlag(CFlag.TRAVERSABLE)){
                            cell.setFlagValue(CFlag.TRAVERSABLE,false);
                        }
                        break;
                    }
                    cell.setFlagValue(CFlag.TRAVERSABLE,true);
                }
            }
            if(cell.hasFlag(CFlag.UNARMED)&&cell.hasFlag(CFlag.ARMED)||cell.hasFlag(CFlag.UNARMED)&&cell.hasFlag(CFlag.PREPARING)||cell.hasFlag(CFlag.PREPARING)&&cell.hasFlag(CFlag.ARMED))
                throw new IllegalStateException("A cell is not allowed to have more than 1 of these flags: activated, preparing or not_activated!");
            if(cell.hasFlag(CFlag.UNARMED)){
                cell.setFlagValue(CFlag.UNARMED,false);
                cell.setFlagValue(CFlag.PREPARING,true);
            }else if(cell.hasFlag(CFlag.PREPARING)){
                cell.setFlagValue(CFlag.PREPARING,false);
                cell.setFlagValue(CFlag.ARMED,true);
                if(cell.getContent() == CContent.TRAP)cell.setFlagValue(CFlag.HURTING,true);
            }else if(cell.hasFlag(CFlag.ARMED)){
                cell.setFlagValue(CFlag.ARMED,false);
                cell.setFlagValue(CFlag.UNARMED,true);
                if(cell.getContent() == CContent.TRAP)cell.setFlagValue(CFlag.HURTING,false);
            }
            if(cell.hasFlag(CFlag.HURTING))kill(cell);
        }
        notifyListener(Event.CELL_CHANGED);
    }

    private Cell findCellWithId(String linkedCellId) {
        for(Cell[] cellRow: currentMap) for (Cell cell : cellRow){
            if(cell.getCellId().equals(linkedCellId))return cell;
        }
        return null;
    }

    private void setCell(Integer column, Integer row, Cell cell) {
        currentMap[column][row]=cell;
    }

    private void executeMethodCall(MethodCall methodCall) {
        String name = methodCall.getExpressionTree().getLeftNode().getText();
        Cell knightCell = entityCellMap.get(name);
//        if(knightCell.getEntity() == null)return;
        switch (methodCall.getMethodType()){
            case MOVE:
                Cell cell = tryToMoveCell(knightCell); //TODO: stattdessen mit getTargetCell()?
                if(!(cell == knightCell)){
//                    notifyListener(Event.CELL_CHANGED);
                    entityCellMap.replace(name,cell);
                }
                break;
            case TURN:
//            case TURN_LEFT:
//                if()notifyListener(Event.CELL_CHANGED);
                tryToTurnCell(knightCell,Integer.valueOf(methodCall.getExpressionTree().getRightNode().getText()));
                break;
            case USE_ITEM:
                if(!knightCell.getEntity().getItem().isCollectible())
                    throw new IllegalStateException("This should not be a possible Item");
                tryToUseItem(knightCell);
                break;
            case COLLECT:
//                if(tryToCollect(knightCell))notifyListener(Event.CELL_CHANGED);
                tryToCollect(knightCell);
                break;
        }
    }

    private void tryToUseItem(Cell knightCell) {
        Cell targetCell =  getTargetCell(knightCell);
        if(knightCell.getEntity().getItem() == CContent.KEY&&targetCell.getContent() == CContent.EXIT){
            if(!targetCell.hasFlag(CFlag.TRAVERSABLE)){
                knightCell.getEntity().setItem(null);
                targetCell.setFlagValue(CFlag.TRAVERSABLE,true);
            }
        }
    }

    private boolean tryToCollect(Cell knightCell) {
        Cell targetCell =  getTargetCell(knightCell);
        if(targetCell.hasFlag(CFlag.COLLECTIBLE)){
            knightCell.getEntity().setItem(targetCell.getContent());
            targetCell.setContent(CContent.PATH);
            targetCell.setFlagValue(CFlag.TRAVERSABLE,true);
            targetCell.setFlagValue(CFlag.COLLECTIBLE,false);
            return true;
        }
        return false;
    }

//    private Cell findCellContainingEntityWithName(String name) {
//        for(Cell[] cellrow:currentMap){
//            for(Cell cell : cellrow){
//                if(cell.getEntity() != null && cell.getEntity().getName().equals(name))return cell;
//            }
//        }
//        return null;
//    }

    private boolean tryToTurnCell(Cell knightCell, int times) {
        times = times %4;
        Point coords = find(knightCell);
        if(coords.getX()==-1) return false;
        if (times == 0) return true;
        switch (knightCell.getEntity().getDirection()){
            case UP:
                knightCell.getEntity().setDirection(Direction.RIGHT);
                break;
            case DOWN:
                knightCell.getEntity().setDirection(Direction.LEFT);
                break;
            case RIGHT:
                knightCell.getEntity().setDirection(Direction.DOWN);
                break;
            case LEFT:
                knightCell.getEntity().setDirection(Direction.UP);
                break;
            }
        return tryToTurnCell(knightCell,times-1);
    }

    private Cell tryToMoveCell(Cell entityCell) {
        Cell output = entityCell;
        Cell targetCell = getTargetCell(entityCell);
        Entity targetEntity = targetCell.getEntity();
        if(targetEntity != null && targetEntity.getEntityType() == EntityType.SKELETON && entityCell.getEntity().getEntityType() == EntityType.KNIGHT){
            kill(entityCell);
        }
        if(targetEntity != null && targetEntity.getEntityType() == EntityType.KNIGHT && entityCell.getEntity().getEntityType() == EntityType.SKELETON){
            kill(targetCell);
        }

        if(targetCell.hasFlag(CFlag.TRAVERSABLE)){
            if(targetCell.getContent()==CContent.EXIT)win();
            if(targetCell.hasFlag(CFlag.HURTING)){
                kill(entityCell);
                return output;
            }
            targetCell.setEntity(entityCell.getEntity());
            targetCell.setFlagValue(CFlag.TRAVERSABLE,false);

            if(targetCell.getContent() == CContent.PRESSURE_PLATE){
                targetCell.setFlagValue(CFlag.TRIGGERED,true);
            }
            if(entityCell.getContent() == CContent.PRESSURE_PLATE){
                entityCell.setFlagValue(CFlag.TRIGGERED,false);
            }
            entityCell.setEntity(null);
            entityCell.setFlagValue(CFlag.TRAVERSABLE,true);

            output = targetCell;
        }
        return output;
    }

    private void win() {
        System.out.println("Success!");
        isWon = true;
    }

    private void kill(Cell knightCell) {
        if(knightCell.getEntity() == null)return;
        System.out.println(knightCell.getEntity().getEntityType() +" "+ knightCell.getEntity().getName()+" died!");
        entityCellMap.remove(knightCell.getEntity().getName());
        knightCell.setEntity(null);
        knightCell.setFlagValue(CFlag.TRAVERSABLE,true);
    }

    private Cell getTargetCell(Cell knightCell) {
        Point coords = find(knightCell);
        switch (knightCell.getEntity().getDirection()){
            case UP:
                return currentMap[coords.getX()-1][coords.getY()];
            case DOWN:
                return currentMap[coords.getX()+1][coords.getY()];
            case RIGHT:
                return currentMap[coords.getX()][coords.getY()+1];
            case LEFT:
                return currentMap[coords.getX()][coords.getY()-1];
        }
        return knightCell;
    }

    private Point find(Cell knightCell) {
        for(int i = 0; i < currentMap.length; i++){
            for(int j = 0; j < currentMap[0].length; j++){
                if(knightCell== currentMap[i][j])return new Point(i,j);
            }
        }
        return new Point(-1,-1);
    }

    public Cell[][] getOriginalMap() {
        return originalMap;
    }

    public void print() {
        for (Cell[] cellRow : originalMap) {
            for (Cell cell : cellRow) {
                System.out.print(cell.getContent().name() + ", ");
            }
            System.out.println();
        }
    }

    public void setPlayerBehaviour(ComplexStatement playerBehaviour) {
        this.playerBehaviour = playerBehaviour;
        this.playerIterator = new StatementIterator(playerBehaviour);
    }

    public ComplexStatement getPlayerBehaviour() {
        return playerBehaviour;
    }

    public Cell[][] getCurrentMap() {
        return currentMap;
    }

    public void reset(){
        playerBehaviour.resetCounter();
        aiBehaviour.resetCounter();
        playerBehaviour.resetVariables();
        aiBehaviour.resetVariables();
        noStackOverflow = 0;
        isWon=false;
        currentMap = clone(originalMap);
        notifyListener(Event.CELL_CHANGED);
//        variableDepthMap = new VariableDepthMap();
//        depthStatementMap = new HashMap<>();
    }


    public boolean testCondition(ConditionTree condition) throws IllegalAccessException {
//        ExpressionTree leftTree = condition.getLeftTree();
//        ExpressionTree rightTree = condition.getRightTree();
        switch (condition.getOperatorType()){
            case SIMPLE:
                return evaluateBooleanExpression((ConditionLeaf)condition);
            case AND:
                return testCondition(condition.getLeftNode()) && testCondition(condition.getRightNode());
            case OR:
                return testCondition(condition.getLeftNode()) || testCondition(condition.getRightNode());
            case NEGATION:
                return !testCondition(condition.getRightNode());

        }
        throw new IllegalArgumentException("Condition \""+ condition.getText() +"\" contains error!");
    }

    private int evaluateNumericalExpression(ExpressionTree node) throws IllegalAccessException {
        if(node.getText().equals(""))return 0;
        switch (node.getExpressionType()){
            case ADD:
                return evaluateNumericalExpression(node.getLeftNode()) + evaluateNumericalExpression(node.getRightNode());
            case SUB:
                return evaluateNumericalExpression(node.getLeftNode()) - evaluateNumericalExpression(node.getRightNode());
            case DIV:
                return evaluateNumericalExpression(node.getLeftNode()) / evaluateNumericalExpression(node.getRightNode());
            case MULT:
                return evaluateNumericalExpression(node.getLeftNode()) * evaluateNumericalExpression(node.getRightNode());
            case SIMPLE:
                return evaluateVariable(node.getText());
            default:
                throw new IllegalAccessException(node.getText()+"of type " + node.getExpressionType()+ " could not be evaluated correctly!"); //this should never be reached
        }
    }

    private int evaluateVariable(String variableString) throws IllegalAccessException {
        variableString = variableString.trim();

        if(variableString.charAt(variableString.length()-1)==';')variableString = variableString.substring(0,variableString.length()-1); //TODO: warum sind die ';' mit dabei?
//        variableString = removeBrackets(variableString);
        Variable variable = currentStatement.getParentStatement().getVariable(variableString);
        if(variable!= null)return evaluateNumericalExpression(variable.getValue());
//        if(variableDepthMap.contains(variableString,currentStatement.getDepth()))return evaluateNumericalvariableString(variableDepthMap.getValue(variableString,currentStatement.getDepth())); //TODO: +-1
        else return Integer.valueOf(variableString);
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
        switch (conditionLeaf.getSimpleConditionType()){
            case GR_EQ:
                return evaluateNumericalExpression(leftTree) >= evaluateNumericalExpression(rightTree);
            case LE_EQ:
                return evaluateNumericalExpression(leftTree) <= evaluateNumericalExpression(rightTree);
            case GR:
                return evaluateNumericalExpression(leftTree) > evaluateNumericalExpression(rightTree);
            case LE:
                return evaluateNumericalExpression(leftTree) < evaluateNumericalExpression(rightTree);
            case NEQ:
                return evaluateNumericalExpression(leftTree) != evaluateNumericalExpression(rightTree);
            case EQ:
                return evaluateNumericalExpression(leftTree) == evaluateNumericalExpression(rightTree);
            case SIMPLE:
                if(leftTree.getText().equals("true")) return true;
                else if(leftTree.getText().equals("false")) return false;
                if(leftTree.getExpressionType() == ExpressionType.CAL){
                    String objectName = leftTree.getLeftNode().getText(); //TODO: ????Does Cal have to be an Expression? -> just objectString.methodString(parameterString)??:
                    String methodName = leftTree.getRightNode().getLeftNode() == null ? leftTree.getRightNode().getText().substring(0,leftTree.getRightNode().getText().length()-2) : leftTree.getRightNode().getLeftNode().getText();
                    String parameters = leftTree.getRightNode().getLeftNode() == null ? "" : leftTree.getRightNode().getRightNode().getText();
                    System.out.println(objectName+"."+methodName+"("+parameters+")");
                    return evaluateBooleanMethodCall(objectName,methodName,parameters);
                }

        }
        throw new IllegalArgumentException(leftTree.getText()+" is not a valid boolean!");
    }

    //TODO: Does Cal have to be an Expression? -> just objectString.methodString(parameterString)??
    private boolean evaluateBooleanMethodCall(String objectName, String methodName, String parameterString) throws IllegalAccessException {
//        Variable variable = depthStatementMap.get(currentStatement.getDepth()-1).getVariable(objectName);
        Variable variable = currentStatement.getParentStatement().getVariable(objectName);
        if(variable == null)throw new IllegalArgumentException("Variable "+objectName +" does not exist!");
//        if(variableDepthMap.contains(expressionTree.getLeftNode().getText(),currentStatement.getDepth())){
//            VariableType vType = variableDepthMap.getType(expressionTree.getLeftNode().getText(),currentStatement.getDepth());
        VariableType vType = variable.getVariableType();
        if(vType == VariableType.KNIGHT||vType == VariableType.SKELETON){
//                if(expressionTree.getRightNode().getExpressionType() != ExpressionType.CAL)throw new IllegalAccessException("Dafuq you doin?");
//            Cell knightCell = entityCellMap.get(expressionTree.getLeftNode().getText());
            Cell knightCell = entityCellMap.get(objectName);
            if(knightCell == null) return false;
            Cell cell = getTargetCell(knightCell);
            switch (MethodType.getMethodType(methodName+"("+parameterString+")")){
                case MOVE:
                case TURN:
                case USE_ITEM:
                case COLLECT:
                    throw new IllegalAccessException("Method: \"" + methodName + "\" is not allowed here!"); //TODO: exceptions should occur in CodeParser
                case CAN_MOVE:
                    return cell.hasFlag(CFlag.TRAVERSABLE);
                case HAS_ITEM:
                    return knightCell.getEntity().getItem() == CContent.valueOf(parameterString);
                case TARGET_CELL_IS:
                    return cell.getContent() == CContent.valueOf(parameterString);
                case TARGET_IS_UNARMED:
                    return !cell.hasFlag(CFlag.PREPARING)&&!cell.hasFlag(CFlag.ARMED)&&(cell.getEntity()==null || cell.getEntity().getEntityType()!=EntityType.SKELETON);
                case TARGET_CONTAINS_ENTITY:
                    return cell.getEntity()!=null && cell.getEntity().getEntityType()==EntityType.valueOf(parameterString);

            }

            throw new IllegalStateException("Method \"" + methodName+"("+parameterString+")\" could not be evaluated");
        }
//        }
        throw new IllegalStateException(objectName + " has wrong variable type!");
    }

    public Statement evaluateNext(StatementIterator statementIterator) throws IllegalAccessException {
//        currentStatement = behaviour.nextStatement()
        currentStatement = statementIterator.next();
        if(currentStatement==null)return null;
        switch (currentStatement.getStatementType()){
            //TODO: statementDepth map, List<Variable> in every statement, new Variable -> statemnetDepthMap.get(currentDepth -1).addVariable(variable) -> add also to substatements
            case COMPLEX:
//                depthStatementMap.put(currentStatement.getDepth(),currentStatement);
                break;
            case FOR:
                //TODO: all the same -> method
                ForStatement forStatement = (ForStatement)currentStatement;
                ConditionTree forCondition = forStatement.getCondition();
//                if(!depthStatementMap.containsKey(currentStatement.getDepth()))
//                depthStatementMap.put(currentStatement.getDepth(),currentStatement);
                if(!testCondition(forCondition)){
//                    variableDepthMap.clearAtDepth(currentStatement.getDepth()+1);
//                    depthStatementMap.remove(currentStatement.getDepth());
//                    behaviour.skip(currentStatement.getDepth()-1);
                    statementIterator.skip(forStatement);
                }
                break;
            case WHILE:
                //TODO: all the same -> method
                WhileStatement whileStatement = (WhileStatement)currentStatement;
                ConditionTree whileCondition = whileStatement.getCondition();
//                if(!depthStatementMap.containsKey(currentStatement.getDepth()))
//                depthStatementMap.put(currentStatement.getDepth(),currentStatement);
                if(!testCondition(whileCondition)){
//                    variableDepthMap.clearAtDepth(currentStatement.getDepth()+1);
//                    depthStatementMap.remove(currentStatement.getDepth());
//                    behaviour.skip(currentStatement.getDepth()-1);
                    statementIterator.skip(whileStatement);
                }
                break;
            case IF:
                //TODO: all the same -> method
                ConditionalStatement ifStatement = (ConditionalStatement)currentStatement;
                ConditionTree ifCondition = ifStatement.getCondition();
//                if(!depthStatementMap.containsKey(currentStatement.getDepth()))
//                    depthStatementMap.replace(currentStatement.getDepth(),currentStatement);
//                depthStatementMap.put(currentStatement.getDepth(),currentStatement);
                if(!testCondition(ifCondition)){
//                    depthStatementMap.remove(currentStatement.getDepth());
//                    variableDepthMap.clearAtDepth(currentStatement.getDepth()+1);
                    ifStatement.activateElse();
//                    behaviour.skip(currentStatement.getDepth()-1);
                }
                break;
                //TODO: replace elses with ifs????
            case ELSE:
                ConditionalStatement elseStatement = (ConditionalStatement)currentStatement;
                ConditionTree elseCondition = elseStatement.getCondition();
//                if(!depthStatementMap.containsKey(currentStatement.getDepth()))
//                    depthStatementMap.replace(currentStatement.getDepth(),currentStatement);
//                depthStatementMap.put(currentStatement.getDepth(),currentStatement);
                if(!testCondition(elseCondition)){
//                    depthStatementMap.remove(currentStatement.getDepth());
//                    variableDepthMap.clearAtDepth(currentStatement.getDepth()+1);
                    elseStatement.activateElse();
//                    behaviour.skip(currentStatement.getDepth()-1);
                }
//                depthStatementMap.put(currentStatement.getDepth(),currentStatement);
                break;
            case METHOD_CALL:
                break;
            case DECLARATION:
                Assignment declaration = (Assignment)currentStatement;
                Variable variable = declaration.getVariable();
//                depthStatementMap.get(currentStatement.getDepth()-1).addVariable(variable);
//                System.out.println(currentStatement.getParentStatement().getStatementType().name());
                currentStatement.getParentStatement().addLocalVariable(variable);
//                if(variableDepthMap.containsExactlyAtDepth(variable.getName(),currentStatement.getDepth())) throw new IllegalArgumentException("Variable " + variable.getName() + " is already defined in scope");
//                else variableDepthMap.put(variable,currentStatement.getDepth());
                break;
            case ASSIGNMENT:
                Assignment assignment = (Assignment)currentStatement;
                Variable variable2 = new Variable(assignment.getVariable().getVariableType(),assignment.getVariable().getName(),ExpressionTree.expressionTreeFromString(evaluateNumericalExpression(assignment.getVariable().getValue())+"",0));
                currentStatement.getParentStatement().updateVariable(variable2);
//                depthStatementMap.get(currentStatement.getDepth()-1).updateVariable(variable2);
                //                if(variableDepthMap.contains(variable2.getName(),currentStatement.getDepth())) variableDepthMap.update(variable2,currentStatement.getDepth());
//                else throw new IllegalArgumentException("Variable " + variable2.getName() + " hasnt been defined yet!");
                break;
        }
        return currentStatement;
    }
    public boolean isWon(){
        return isWon;
    }
}
 */