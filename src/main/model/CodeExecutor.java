package main.model;

import main.model.gamemap.Cell;
import main.model.gamemap.Entity;
import main.model.gamemap.GameMap;
import main.model.gamemap.enums.*;
import main.model.statement.*;
import main.model.statement.Expression.ExpressionTree;
import main.utility.Point;
import main.utility.Variable;
import main.utility.VariableType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static main.model.GameConstants.NO_ENTITY;

public abstract class CodeExecutor {

    private static GameMap currentGameMap;
    private static boolean hasWon=false;
    private static boolean hasLost = false;
    private static boolean skeletonWasSpawned;
    private static boolean knightWasSpawned;


    public static boolean executeBehaviour(Statement statement, GameMap gameMap, boolean isPlayer, boolean canSpawnKnights) {
        if(!isPlayer)skeletonWasSpawned = false;
        else knightWasSpawned = false;
        currentGameMap  = gameMap;
        boolean methodWasCalled = false;
        if(statement.getStatementType()== StatementType.METHOD_CALL) {
            MethodCall evaluatedMethodCall = (MethodCall)statement;
            methodWasCalled = true;
            executeMethodCall(evaluatedMethodCall,isPlayer);
        }
        else if(statement.getStatementType()== StatementType.DECLARATION || statement.getStatementType()== StatementType.ASSIGNMENT) {
            Assignment assignment = (Assignment)statement;
            Variable var = assignment.getVariable();
            boolean replaced = false;
            if(var.getValue().getText().equals(""))return false;
            if(!currentGameMap.getEntity(var.getName()).equals(NO_ENTITY)){
                if(currentGameMap.getEntity(var.getName()).getEntityType().getDisplayName().equals(var.getVariableType().getName())){
                    currentGameMap.kill(currentGameMap.getEntityPosition(var.getName()));
                    replaced = true;
                }
            }
            if(var.getVariableType() == VariableType.KNIGHT){
                methodWasCalled = true;
                String name = assignment.getVariable().getName();
                Direction direction = null;
                ExpressionTree expressionTree = ((ExpressionTree)var.getValue());
                if(expressionTree.getRightNode()!=null)
                    direction= Direction.getValueFromString(expressionTree.getRightNode().getText());
                if(direction == null)
                    direction = Direction.NORTH;


                Point spawn = currentGameMap.findSpawn();

                if(statement.getStatementType() == StatementType.ASSIGNMENT) currentGameMap.getEntity(name).deleteIdentity();
                if(spawn.getX() != -1&&currentGameMap.getEntity(spawn)==NO_ENTITY && (canSpawnKnights||replaced)){
                    if(statement.getStatementType()== StatementType.ASSIGNMENT) replaced = true;
                    currentGameMap.spawn(spawn,new Entity(name,direction, EntityType.KNIGHT));
                    knightWasSpawned = !replaced;
                }

            }else if(var.getVariableType() == VariableType.SKELETON){
                if(statement.getStatementType()== StatementType.ASSIGNMENT) replaced = true;
                methodWasCalled = true;
                if(currentGameMap.getEnemySpawnList().size()==0)return true;
                String name = var.getName();
                Direction direction = Direction.NORTH;
                ExpressionTree expressionTree = ((ExpressionTree)var.getValue());
                if(expressionTree.getRightNode()!= null)
                    direction = Direction.getValueFromString(expressionTree.getRightNode().getText());
                String spawnId = "";
                if(expressionTree.getRightNode() != null){
                    String s =expressionTree.getRightNode().getText();
                    if(s.matches(".*,.*")){

                        direction = Direction.valueOf(s.split(",")[0]);
                        spawnId =s.split(",")[1];
                    }
                    else direction = Direction.getValueFromString(expressionTree.getRightNode().getText());

                }
                if(direction == null)direction = Direction.NORTH;
                int index = GameConstants.RANDOM.nextInt(currentGameMap.getEnemySpawnList().size());
                Point spawnPoint = new Point(currentGameMap.getEnemySpawnList().get(index).getX(),currentGameMap.getEnemySpawnList().get(index).getY());
                if(!spawnId.equals("")){
                    int i = Integer.valueOf(spawnId);
                    for(Point point : currentGameMap.getEnemySpawnList()){
                        spawnPoint = point;
                        if(currentGameMap.getCellID(spawnPoint)==i&&currentGameMap.getEntity(spawnPoint)==NO_ENTITY){
                            currentGameMap.spawn(spawnPoint,new Entity(name,direction,EntityType.SKELETON));
                            skeletonWasSpawned = !replaced;
                        }
                    }
                }
                else if(currentGameMap.getEntity(spawnPoint)==NO_ENTITY){
                    currentGameMap.spawn(spawnPoint,new Entity(name,direction,EntityType.SKELETON));
                    skeletonWasSpawned = !replaced;
                }
            }

        }
        return methodWasCalled;
    }


    private static void tryToUseItem(Point actorPos) {
        String name = currentGameMap.getEntity(actorPos).getName();
        Point targetPos =  currentGameMap.getTargetPoint(name);
        Entity actorEntity = currentGameMap.getEntity(actorPos);
        CellContent targetContent = currentGameMap.getContentAtXY(targetPos);
        if(actorEntity.getItem() == ItemType.KEY&&targetContent == CellContent.EXIT){
            currentGameMap.setEntityItem(actorPos,ItemType.NONE);
            currentGameMap.setFlag(targetPos, CellFlag.OPEN, true);
            hasWon = true;
            return;
        }
//        if(actorEntity.getItem() == ItemType.BEACON){
//            beaconEntity = actorEntity;
//        }
        if((actorEntity.getItem() == ItemType.SHOVEL||actorEntity.getItem() == ItemType.SWORD)&&GameConstants.ACTION_WITHOUT_CONSEQUENCE){
            currentGameMap.setFlag(actorPos , CellFlag.ACTION,true );
            //this will have the effect that the target cell will be drawn, even though it did not change
            if(currentGameMap.getEntity(currentGameMap.getTargetPoint(name))==NO_ENTITY)currentGameMap.setFlag(currentGameMap.getTargetPoint(name), CellFlag.HELPER_FLAG,true);
        }
        if(actorEntity.getItem() == ItemType.SHOVEL&&targetContent == CellContent.DIRT){
            currentGameMap.setContent(targetPos, CellContent.PATH);
            currentGameMap.setFlag(actorPos, CellFlag.ACTION, true );
            currentGameMap.setFlag(targetPos, CellFlag.DIRT_REMOVED, true );
        }
        if(actorEntity.getItem() == ItemType.SWORD&&(currentGameMap.getEntity(targetPos) != NO_ENTITY||currentGameMap.getItem(targetPos)!=ItemType.NONE)){
            if(currentGameMap.getItem(targetPos) == ItemType.BOULDER)return;
            currentGameMap.kill(targetPos); //TODO: maybe -> targetCell.kill();??
            currentGameMap.setFlag(actorPos, CellFlag.ACTION, true );
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
            return true;
        }
        return false;
    }
    private static void tryToTurnCell(Point actorPoint, String direction, MethodCall mc){
        currentGameMap.changeEntityDirection(actorPoint,direction);

    }

    private static void tryToMoveCell(String name, boolean forwards) {

        Point actorPoint = currentGameMap.getEntityPosition(name);
        Entity actorEntity = currentGameMap.getEntity(actorPoint);
        Point targetPoint = currentGameMap.getTargetPoint(name,forwards);

        boolean isOpen = currentGameMap.cellHasFlag(targetPoint, CellFlag.OPEN) ^ currentGameMap.cellHasFlag(targetPoint, CellFlag.INVERTED);
        if(currentGameMap.isGateWrongDirection(actorPoint, targetPoint))return;
        if((currentGameMap.getContentAtXY(targetPoint).isTraversable()||isOpen) && currentGameMap.isCellFree(targetPoint)){
            CellContent targetContent =currentGameMap.getContentAtXY(targetPoint);
            currentGameMap.removeEntity(actorPoint);
            currentGameMap.setEntity(targetPoint,  actorEntity );

            if(targetContent== CellContent.TRAP && currentGameMap.cellHasFlag(targetPoint, CellFlag.ARMED)){
                currentGameMap.kill(targetPoint);
            }

            if(targetContent == CellContent.PRESSURE_PLATE){
                currentGameMap.setFlag(targetPoint, CellFlag.TRIGGERED,true);
            }
            if(currentGameMap.getContentAtXY(actorPoint) == CellContent.PRESSURE_PLATE){
                currentGameMap.setFlag(actorPoint, CellFlag.TRIGGERED,false);
            }
//            output = targetCell;
        }
//        return output;
    }

    private static void executeMethodCall(MethodCall methodCall, boolean isPlayer) {
        List<String> nameList = new ArrayList<>();
        nameList.add(methodCall.getObjectName());
        Matcher matcher = Pattern.compile("\\((.*,.*)\\)").matcher(methodCall.getObjectName());
        if(matcher.matches()){
            nameList = new ArrayList<>(Arrays.asList(matcher.group(1).split(",")));
        }
        for(String name : nameList){
//            if(!isPlayer) System.out.println(methodCall.getCode()+" "+name);
        Point position = currentGameMap.getEntityPosition(name);
        if(position.getX() == -1 ){
            if(isPlayer&& currentGameMap.getAmountOfEntities(EntityType.KNIGHT) == 0)hasLost=true;
            continue;
        }
        switch (methodCall.getMethodType()){
            case ATTACK:
                if(isPlayer)throw new IllegalStateException("You cannot attack as Player!");

                // Can't attack with an item in hand
                if(currentGameMap.getEntity(name).getItem()!=ItemType.NONE)break;
                if(GameConstants.ACTION_WITHOUT_CONSEQUENCE){
                    currentGameMap.setFlag(position , CellFlag.ACTION,true );
                    //this will have the effect that the target cell will be drawn, even though it did not change
                    if(currentGameMap.getEntity(currentGameMap.getTargetPoint(name))==NO_ENTITY)currentGameMap.setFlag(currentGameMap.getTargetPoint(name), CellFlag.ACTION,true);
                }
//                if(gameMap.getEntity(gameMap.getTargetPoint(name)) == NO_ENTITY ||gameMap.getEntity(gameMap.getTargetPoint(name)) == NO_ENTITY)break;
                if(!(currentGameMap.getEntity(currentGameMap.getTargetPoint(name))==NO_ENTITY)&&!(currentGameMap.getItem(currentGameMap.getTargetPoint(name)) == ItemType.BOULDER))
                    currentGameMap.setFlag(position , CellFlag.ACTION,true );
                currentGameMap.kill(currentGameMap.getTargetPoint(name)); //TODO: stattdessen mit getTargetPoint()?
                break;

            case MOVE:
                tryToMoveCell(name,true); //TODO: stattdessen mit getTargetPoint()?
                break;
            case BACK_OFF:
                tryToMoveCell(name,false); //TODO: stattdessen mit getTargetPoint()?
                break;
            case TURN:
                tryToTurnCell(position,methodCall.getExpressionTree().getRightNode().getText(),methodCall);//evaluateIntVariable(methodCall.getExpressionTree().getRightCondition().getCode()));
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

        }
        }
    }

    public static boolean knightWasSpawned() {
        return knightWasSpawned;
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
    }


    public static boolean skeletonWasSpawned() {
        return skeletonWasSpawned;
    }

}
