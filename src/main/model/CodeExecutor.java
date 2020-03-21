package main.model;

import main.model.gamemap.Cell;
import main.model.gamemap.Entity;
import main.model.gamemap.GameMap;
import main.model.enums.*;
import main.model.statement.*;
import main.model.statement.Expression.ExpressionTree;
import main.utility.GameConstants;
import main.utility.Point;
import main.utility.Variable;
import main.model.enums.VariableType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import main.model.statement.Condition.*;

import static main.utility.GameConstants.NO_ENTITY;

public abstract class CodeExecutor {
    private static int noStackOverflow; //TODO: evaluate
    private static GameMap currentGameMap;
    private static boolean hasWon=false;
    private static boolean hasLost = false;

    private static List<String> unlocks;
    private static boolean skeletonWasSpawned;

    public static void setUnlockedStatementList(List<String> unlockedStatementList){
        unlocks  = unlockedStatementList;
    }

    static boolean executeBehaviour(Statement statement, GameMap gameMap, boolean isPlayer, boolean canSpawnKnights) throws IllegalAccessException {
        skeletonWasSpawned = false;
        currentGameMap  = gameMap;
        boolean method_Called = false;
        noStackOverflow++;
        if(isPlayer)updateUnlocks(statement);
        if(statement.getStatementType()== StatementType.METHOD_CALL) {
            noStackOverflow = 0;
            MethodCall evaluatedMethodCall = (MethodCall)statement;
            method_Called = true;
            executeMethodCall(evaluatedMethodCall,isPlayer);
        }
        if(statement.getStatementType()== StatementType.DECLARATION || statement.getStatementType()== StatementType.ASSIGNMENT) {
            Assignment assignment = (Assignment)statement;
            if(assignment.getVariable().getVariableType() == VariableType.KNIGHT){
                method_Called = true;
                String name = assignment.getVariable().getName();
                Direction direction = evaluateDirection(assignment.getVariable().getValue().getRightNode(),assignment.getParentStatement());

                Point spawn = currentGameMap.findSpawn();

                if(statement.getStatementType() == StatementType.ASSIGNMENT) currentGameMap.getEntity(name).deleteIdentity();
                if(spawn.getX() != -1&&currentGameMap.isCellFree(spawn) && canSpawnKnights){
                    currentGameMap.spawn(spawn,new Entity(name,direction, EntityType.KNIGHT));
                }

            }else if(assignment.getVariable().getVariableType() == VariableType.SKELETON){ //TODO: stattdessen ENEMY?
                method_Called = true;
                if(currentGameMap.getEnemySpawnList().size()==0)return true;
                String name = assignment.getVariable().getName();
                Direction direction = evaluateDirection(assignment.getVariable().getValue().getRightNode(),assignment.getParentStatement());
                String spawnId = "";
                if(assignment.getVariable().getValue().getRightNode() != null){
                    String s =assignment.getVariable().getValue().getRightNode().getText();
                    if(s.matches(".*,.*")){

                        direction = Direction.valueOf(s.split(",")[0]);
                        spawnId =s.split(",")[1];
                    }
                    else direction = Direction.getValueFromString(assignment.getVariable().getValue().getRightNode().getText());

                }
                if(direction == null)direction = Direction.NORTH;
                int index = GameConstants.RANDOM.nextInt(currentGameMap.getEnemySpawnList().size());
                Point spawnPoint = new Point(currentGameMap.getEnemySpawnList().get(index).getX(),currentGameMap.getEnemySpawnList().get(index).getY());
                if(spawnId != ""){
                    for(Point point : currentGameMap.getEnemySpawnList()){
                        spawnPoint = point;
                        if(currentGameMap.getCellID(spawnPoint)==Integer.valueOf(spawnId)){
                            currentGameMap.spawn(spawnPoint,new Entity(name,direction,EntityType.SKELETON));
                            skeletonWasSpawned = true;
                        }
                    }
                }
                else {
                    currentGameMap.spawn(spawnPoint,new Entity(name,direction,EntityType.SKELETON));
                    skeletonWasSpawned = true;
                }
            }

        }
        return method_Called;
    }

    private static void updateUnlocks(Statement statement) {

        String unlock = "";
        List<String> unlock2 = new ArrayList<>();
        switch (statement.getStatementType()){
            case FOR:
            case WHILE:
            case IF:
            case ELSE:
                unlock2 = getUnlockedBooleanMethodsFromStatement(statement,((ComplexStatement)statement).getCondition());
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
                if(GameConstants.DEBUG)System.out.println("You have unlocked the following: "+statement.getText());
                return;
        }
        if(!unlocks.contains(unlock))unlocks.add(unlock);
        for(String s : unlock2){
            if(!unlocks.contains(s))unlocks.add(s);
        }
    }

    private static List<String> getUnlockedBooleanMethodsFromStatement(Statement complexStatement, Condition condition) {
        List<String> output = new ArrayList<>();
        if(condition == null)return output;
        Matcher m = Pattern.compile(".*("+GameConstants.VARIABLE_NAME_REGEX.substring(1, GameConstants.VARIABLE_NAME_REGEX.length()-1)+").*?").matcher(condition.getText());
        if(m.matches())
        for(int i = 1; i< m.groupCount()+1;i++){
            Variable v = complexStatement.getParentStatement().getVariable(m.group(i));
            if(v!=null && v.getVariableType() == VariableType.BOOLEAN){
                output.addAll(getUnlockedBooleanMethodsFromStatement(complexStatement,Condition.getConditionFromString(v.getValue().getText())));
            }
        }

         m = Pattern.compile(".*?([a-zA-Z]+)\\(.*\\).*?").matcher(condition.getText());
        if(m.matches())
        for(int i = 1; i< m.groupCount()+1;i++){
            output.add(m.group(i));
        }
        return output;
    }

    private static Direction evaluateDirection(ExpressionTree rightNode, ComplexStatement parentStatement) {
        Direction output = Direction.NORTH;
        Variable dirVar = parentStatement.getVariable(rightNode.getText());
        if(rightNode != null)output = Direction.getValueFromString(rightNode.getText());
        if(output == null){
            if(dirVar != null && dirVar.getVariableType() == VariableType.DIRECTION)return evaluateDirection(dirVar.getValue(),parentStatement);
            else return Direction.NORTH;
        }
        return output;
    }

    private static void tryToUseItem(Point actorPos) {
        String name = currentGameMap.getEntity(actorPos).getName();
        Point targetPos =  currentGameMap.getTargetPoint(name);
        Entity actorEntity = currentGameMap.getEntity(actorPos);
        CellContent targetContent = currentGameMap.getContentAtXY(targetPos);
        if(actorEntity.getItem() == ItemType.KEY&&targetContent == CellContent.EXIT){
            currentGameMap.setEntityItem(actorPos,ItemType.NONE);
            currentGameMap.setFlag(targetPos, CFlag.OPEN, true);
            hasWon = true;
            return;
        }
//        if(actorEntity.getItem() == ItemType.BEACON){
//            beaconEntity = actorEntity;
//        }
        if((actorEntity.getItem() == ItemType.SHOVEL||actorEntity.getItem() == ItemType.SWORD)&&GameConstants.ACTION_WITHOUT_CONSEQUENCE){
            currentGameMap.setFlag(actorPos , CFlag.ACTION,true );
            //this will have the effect that the target cell will be drawn, even though it did not change
            if(currentGameMap.getEntity(currentGameMap.getTargetPoint(name))==NO_ENTITY)currentGameMap.setFlag(currentGameMap.getTargetPoint(name),CFlag.ACTION,true);
        }
        if(actorEntity.getItem() == ItemType.SHOVEL&&targetContent == CellContent.DIRT){
            currentGameMap.setContent(targetPos, CellContent.PATH);
            currentGameMap.setFlag(actorPos, CFlag.ACTION, true );
            currentGameMap.setFlag(targetPos, CFlag.DIRT_REMOVED, true );
        }
        if(actorEntity.getItem() == ItemType.SWORD&&currentGameMap.getEntity(targetPos) != NO_ENTITY){
            if(currentGameMap.getItem(targetPos) == ItemType.BOULDER)return;
            currentGameMap.kill(targetPos); //TODO: maybe -> targetCell.kill();??
            currentGameMap.setFlag(actorPos, CFlag.ACTION, true );
        }

    }

    private static void tryToDropItem(Point actorPos) {
        String name = currentGameMap.getEntity(actorPos).getName();
        Point targetPos =  currentGameMap.getTargetPoint(name);
        Entity actorEntity = currentGameMap.getEntity(actorPos);
        Entity targetEntity = currentGameMap.getEntity(targetPos);
        CellContent targetContent = currentGameMap.getContentAtXY(targetPos);

        if((targetContent.isTraversable()||currentGameMap.gateIsOpen(targetPos))&&currentGameMap.getCellAtXYClone(targetPos.getX(),targetPos.getY() ).getItem()==ItemType.NONE){
            boolean correctDirection = false;
            if(targetEntity.getDirection() != null){
                correctDirection = targetEntity.getDirection() != actorEntity.getDirection();
                int ordinalSum = targetEntity.getDirection().ordinal() + actorEntity.getDirection().ordinal();
                correctDirection = correctDirection && (ordinalSum%2 == 0);
            }
            if(targetEntity== NO_ENTITY){
                currentGameMap.setItem(targetPos,actorEntity.getItem());
                currentGameMap.setEntityItem(actorPos,ItemType.NONE);
            }
            else if(targetEntity.getItem()==ItemType.NONE && correctDirection){
                currentGameMap.setEntityItem(targetPos,actorEntity.getItem());
                currentGameMap.setEntityItem(actorPos,ItemType.NONE);
            }
        }
    }

    /** Will swap any item a Knight is carrying with whatever item lies in front of him. Does not drop the item if there is no item in front of the Knight!
     *
     * @param
     * @return
     */
    private static boolean tryToCollect(Point actorPoint) {
        Cell actorCell = currentGameMap.getCellAtXYClone(actorPoint.getX(),actorPoint.getY());
        Point targetPoint =  currentGameMap.getTargetPoint(actorCell.getEntity().getName());
        if(currentGameMap.getItem(targetPoint) != ItemType.NONE){
            ItemType item = ItemType.NONE;
            if(actorCell.getEntity().getItem()!=ItemType.NONE) item = actorCell.getEntity().getItem();
            currentGameMap.setEntityItem(actorPoint,currentGameMap.getItem(targetPoint));
            currentGameMap.setItem(targetPoint,item);
//            targetCell.setFlagValue(CFlag.TRAVERSABLE,true);
//            targetCell.setFlagValue(CFlag.COLLECTIBLE,false);
            return true;
        }
        return false;
    }
    private static void tryToTurnCell(Point actorPoint, String direction, MethodCall mc){
        direction = evaluateTurnDirection(direction,mc);
        currentGameMap.changeEntityDirection(actorPoint,direction);

    }

    private static String evaluateTurnDirection(String direction, MethodCall mc) {
        String output = direction;
        Variable tdirVar = mc.getParentStatement().getVariable(direction);
        if(tdirVar!=null && tdirVar.getVariableType() == VariableType.TURN_DIRECTION)return evaluateTurnDirection(tdirVar.getValue().getText(),mc);
        else return output;
    }

    private static void tryToMoveCell(String name, boolean isPlayer) {
//        Cell output = entityCell;
        Point targetPoint = currentGameMap.getTargetPoint(name);
        Point actorPoint = currentGameMap.getEntityPosition(name);
//        Entity targetEntity = gameMap.getEntity(targetPoint);
        Entity actorEntity = currentGameMap.getEntity(actorPoint);
//        if(targetEntity != null && targetEntity.getEntityType() == EntityType.SKELETON && actorEntity.getEntityType() == EntityType.KNIGHT){
//            gameMap.kill(actorPoint);
//        }
//        if(targetEntity != null && targetEntity.getEntityType() == EntityType.KNIGHT && actorEntity.getEntityType() == EntityType.SKELETON){
//            gameMap.kill(targetPoint);
//        }

        boolean isOpen = currentGameMap.cellHasFlag(targetPoint, CFlag.OPEN) ^ currentGameMap.cellHasFlag(targetPoint, CFlag.INVERTED);
        if(currentGameMap.isGateWrongDirection(actorPoint, targetPoint))return;
        if((currentGameMap.getContentAtXY(targetPoint).isTraversable()||isOpen) && currentGameMap.isCellFree(targetPoint)){
            CellContent targetContent =currentGameMap.getContentAtXY(targetPoint);
//            if(targetContent==CellContent.EXIT){
//                if(isPlayer)hasWon = true;
//                return;
//                //TODO: replace with better handling in controller!
//            }

            currentGameMap.removeEntity(actorPoint);
            currentGameMap.setEntity(targetPoint,  actorEntity );

            if(targetContent== CellContent.TRAP && currentGameMap.cellHasFlag(targetPoint,CFlag.ARMED)){
                currentGameMap.kill(targetPoint);
            }

            if(targetContent == CellContent.PRESSURE_PLATE){
                currentGameMap.setFlag(targetPoint,CFlag.TRIGGERED,true);
            }
            if(currentGameMap.getContentAtXY(actorPoint) == CellContent.PRESSURE_PLATE){
                currentGameMap.setFlag(actorPoint,CFlag.TRIGGERED,false);
            }
//            output = targetCell;
        }
//        return output;
    }

    private static void executeMethodCall(MethodCall methodCall, boolean isPlayer) throws IllegalAccessException {
        List<String> nameList = new ArrayList<>();
        nameList.add(methodCall.getObjectName());
        if(methodCall.getParentStatement().getVariable(nameList.get(0)).getVariableType()==VariableType.ARMY){
            Variable v  = methodCall.getParentStatement().getVariable(nameList.get(0));
            nameList = new ArrayList<>(Arrays.asList(v.getValue().getRightNode().getText().split(",")));
        }
        for(String name : nameList){
//            if(!isPlayer) System.out.println(methodCall.getText()+" "+name);
        Point position = currentGameMap.getEntityPosition(name);
        if(position == null ){
            if(isPlayer&& currentGameMap.getAmountOfKnights() == 0)hasLost=true;
            continue;
        }
        switch (methodCall.getMethodType()){
            case ATTACK:
                if(isPlayer)throw new IllegalAccessException("You cannot attack as Player!");

                // Can't attack with an item in hand
                if(currentGameMap.getEntity(name).getItem()!=ItemType.NONE)break;
                if(GameConstants.ACTION_WITHOUT_CONSEQUENCE){
                    currentGameMap.setFlag(position , CFlag.ACTION,true );
                    //this will have the effect that the target cell will be drawn, even though it did not change
                    if(currentGameMap.getEntity(currentGameMap.getTargetPoint(name))==NO_ENTITY)currentGameMap.setFlag(currentGameMap.getTargetPoint(name),CFlag.ACTION,true);
                }
//                if(gameMap.getEntity(gameMap.getTargetPoint(name)) == NO_ENTITY ||gameMap.getEntity(gameMap.getTargetPoint(name)) == NO_ENTITY)break;
                if(currentGameMap.getEntity(currentGameMap.getTargetPoint(name))==NO_ENTITY)break;
                if(currentGameMap.getItem(currentGameMap.getTargetPoint(name)) == ItemType.BOULDER)break;
                currentGameMap.setFlag(position , CFlag.ACTION,true );
                currentGameMap.kill(currentGameMap.getTargetPoint(name)); //TODO: stattdessen mit getTargetPoint()?
                break;

            case MOVE:
                tryToMoveCell(name,isPlayer); //TODO: stattdessen mit getTargetPoint()?
                break;
            case TURN:
                tryToTurnCell(position,methodCall.getExpressionTree().getRightNode().getText(),methodCall);//evaluateIntVariable(methodCall.getExpressionTree().getRightCondition().getText()));
                break;
            case USE_ITEM:
                if(currentGameMap.getEntity(position).getItem()==ItemType.NONE)break;
                tryToUseItem(position);
                break;
            case COLLECT:
                tryToCollect(position);
                break;
            case DROP_ITEM:
                if(currentGameMap.getEntity(position).getItem()==ItemType.NONE)break;
                tryToDropItem(position);
                break;

        }}
    }

    private ExpressionTree evaluateCommand(String parameter, MethodCall methodCall) {
        if(methodCall.getParentStatement().getVariable(parameter)!=null){
            return evaluateCommand(methodCall.getParentStatement().getVariable(parameter).getValue().getText(), methodCall);
        }
        else return ExpressionTree.expressionTreeFromString(parameter);
    }

    static boolean hasWon() {
        return hasWon;
    }
    static boolean hasLost() {
        return hasLost;
    }

    static void reset(){
        hasWon = false;
        hasLost = false;
        noStackOverflow = 0;
    }

    public int getNoStackOverflow() {
        return noStackOverflow;
    }

    public static List<String> getUnlockedStatementList() {
        return unlocks;
    }

    public static boolean skeletonWasSpawned() {
        return skeletonWasSpawned;
    }

}
