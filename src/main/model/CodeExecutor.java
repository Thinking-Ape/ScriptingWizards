package main.model;

import main.model.gamemap.Cell;
import main.model.gamemap.Entity;
import main.model.gamemap.GameMap;
import main.model.gamemap.enums.*;
import main.model.statement.*;
import main.model.statement.Expression.Expression;
import main.model.statement.Expression.ExpressionTree;
import main.utility.Point;
import main.utility.Util;
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

            if(currentGameMap.getEntity(var.getName()).isPossessed()||currentGameMap.findEntityPossessedBy(var.getName()) != NO_ENTITY)return true;
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
                Expression expression = var.getValue();
                if(expression.isLeaf()) return false;
                ExpressionTree expressionTree = ((ExpressionTree)var.getValue());
                if(expressionTree.getRightNode()!=null)
                    direction= Direction.getValueFromString(expressionTree.getRightNode().getText());

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
                int index = GameConstants.RANDOM.nextInt(currentGameMap.getSpawnList().size());
                Point spawnPoint = new Point(currentGameMap.getSpawnList().get(index).getX(),currentGameMap.getSpawnList().get(index).getY());

                boolean isSpecialized = (((ExpressionTree) var.getValue()).getLeftNode()).getText().matches("new Guardian");

                if(statement.getStatementType() == StatementType.ASSIGNMENT)
                    currentGameMap.getEntity(name).deleteIdentity();

                if(!spawnId.equals("")&& (canSpawnKnights||replaced)){
                    int i = Integer.valueOf(spawnId);
                    for(Point point : currentGameMap.getSpawnList()){
                        spawnPoint = point;
                        if(currentGameMap.getCellID(spawnPoint)==i&&currentGameMap.getEntity(spawnPoint)==NO_ENTITY){
                            if(statement.getStatementType()== StatementType.ASSIGNMENT) replaced = true;
                            currentGameMap.spawn(spawnPoint,new Entity(name,direction,EntityType.KNIGHT,isSpecialized));
                            knightWasSpawned = true;//!replaced;
                        }
                    }
                }
                else if(currentGameMap.getEntity(spawnPoint)==NO_ENTITY&& (canSpawnKnights||replaced)){
                    if(statement.getStatementType()== StatementType.ASSIGNMENT) replaced = true;
                    currentGameMap.spawn(spawnPoint,new Entity(name,direction,EntityType.KNIGHT,isSpecialized));
                    knightWasSpawned = true;//!replaced;
                }
            }else if(var.getVariableType() == VariableType.SKELETON){
                if(statement.getStatementType()== StatementType.ASSIGNMENT) replaced = true;
                methodWasCalled = true;
                if(currentGameMap.getEnemySpawnList().size()==0)return true;
                String name = var.getName();
                Direction direction = Direction.NORTH;
                ExpressionTree expressionTree = ((ExpressionTree)var.getValue());
                if(!expressionTree.getText().matches(" *new *(Skeleton|Ghost)\\(.*\\)")) return false;
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
                //TODO: new GHOUL new GHOST new SHADE new UNDEAD new BANSHEE
                Point spawnPoint = new Point(currentGameMap.getEnemySpawnList().get(index).getX(),currentGameMap.getEnemySpawnList().get(index).getY());
                boolean isSpecialized = (((ExpressionTree) var.getValue()).getLeftNode()).getText().matches("new Ghost");
                if(!spawnId.equals("")){
                    int i = Integer.valueOf(spawnId);
                    for(Point point : currentGameMap.getEnemySpawnList()){
                        spawnPoint = point;
                        if(currentGameMap.getCellID(spawnPoint)==i){
                            currentGameMap.spawn(spawnPoint,new Entity(name,direction,EntityType.SKELETON,isSpecialized));
//                            System.out.println("uhu1");
                            skeletonWasSpawned = true;//!replaced;
                        }
                    }
                }
                else {
                    currentGameMap.spawn(spawnPoint,new Entity(name,direction,EntityType.SKELETON,isSpecialized));
//                    System.out.println("uhu2");
                    skeletonWasSpawned = true;//!replaced;
                }
            }

        }
        return methodWasCalled;
    }


    private static void tryToUseItem(Point actorPos) {
        if(currentGameMap.getEntity(actorPos).getEntityType() == EntityType.SKELETON)return;
        if(currentGameMap.getEntity(actorPos).getItem()==ItemType.NONE && !currentGameMap.getEntity(actorPos).isSpecialized())return;
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
        if(actorEntity.isSpecialized()){
            currentGameMap.setFlag(actorPos , CellFlag.ACTION,true );
            if(currentGameMap.getEntity(currentGameMap.getTargetPoint(name))==NO_ENTITY)
                currentGameMap.setFlag(currentGameMap.getTargetPoint(name), CellFlag.HELPER_FLAG,true);
            if(!currentGameMap.isCellFree(targetPos) && currentGameMap.isCellFree(currentGameMap.getCellAfterTarget(name))){
                Entity entity = currentGameMap.getEntity(targetPos);
                currentGameMap.setEntity(targetPos, NO_ENTITY);
                currentGameMap.setEntity(currentGameMap.getCellAfterTarget(name), entity);
                currentGameMap.setItem(currentGameMap.getCellAfterTarget(name), currentGameMap.getItem(targetPos));
                currentGameMap.setItem(targetPos, ItemType.NONE);
            }
            else if(currentGameMap.getItem(targetPos)!=ItemType.BOULDER)currentGameMap.kill(targetPos);
        }
        if((actorEntity.getItem() == ItemType.SHOVEL||actorEntity.getItem() == ItemType.SWORD)&&GameConstants.ACTION_WITHOUT_CONSEQUENCE){
            currentGameMap.setFlag(actorPos , CellFlag.ACTION,true );
            //this will have the effect that the target cell will be drawn, even though it did not change
            if(currentGameMap.getEntity(currentGameMap.getTargetPoint(name))==NO_ENTITY)
                currentGameMap.setFlag(currentGameMap.getTargetPoint(name), CellFlag.HELPER_FLAG,true);
        }
        if(actorEntity.getItem() == ItemType.SHOVEL&&targetContent == CellContent.DIRT){
            currentGameMap.setContent(targetPos, CellContent.PATH);
            currentGameMap.setFlag(actorPos, CellFlag.ACTION, true );
            currentGameMap.setFlag(targetPos, CellFlag.DIRT_REMOVED, true );
        }
        if(actorEntity.getItem() == ItemType.SWORD&&(currentGameMap.getEntity(targetPos) != NO_ENTITY||currentGameMap.getItem(targetPos)!=ItemType.NONE)){
            if(currentGameMap.getItem(targetPos) == ItemType.BOULDER)return;
            currentGameMap.kill(targetPos);
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
        if(currentGameMap.isGateWrongDirection(actorPoint, targetPoint))
            return;
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
        }
    }

    private static void executeMethodCall(MethodCall methodCall, boolean isPlayer) {
        List<String> nameList = new ArrayList<>();
        nameList.add(methodCall.getObjectName());
        Matcher matcher = Pattern.compile("\\((.*,.*)\\)").matcher(methodCall.getObjectName());
        if(matcher.matches()){
            nameList = new ArrayList<>(Arrays.asList(matcher.group(1).split(",")));
        }
        for(String name : nameList){
            Entity currentEntity = currentGameMap.getEntity(name);
            if(currentEntity.isPossessed())
                continue;
            Point position = currentGameMap.getEntityPosition(name);
            if(position.getX() == -1 ){
                if(isPlayer&& currentGameMap.getAmountOfEntities(EntityType.KNIGHT) == 0)hasLost=true;
                Entity possessedEntity = currentGameMap.findEntityPossessedBy(name);
                if(!isPlayer && possessedEntity != NO_ENTITY){
                    currentEntity = possessedEntity;
                    name = currentEntity.getName();
                    position = currentGameMap.getEntityPosition(name);
                }
                else continue;
            }
            switch (methodCall.getMethodType()){
                case DISPOSSESS:
//                    if(isPlayer||!currentEntity.isSpecialized())throw new IllegalStateException("Only Ghosts can dispossess!");
                    if(methodCall.getParameters()[0].equals(""))tryToDispossess(position);
                    else tryToDispossess(position, Direction.getValueFromString(methodCall.getParameters()[0]));
                    break;
                case ATTACK:
                    if(isPlayer)break;
                    if(currentEntity.isSpecialized()){
                        tryToPossess(position);
                        break;
                    }
                    // Can't attack with an item in hand
                    if(currentEntity.getItem()!=ItemType.NONE)break;
                    if(GameConstants.ACTION_WITHOUT_CONSEQUENCE){
                        currentGameMap.setFlag(position , CellFlag.ACTION,true );
                        //this will have the effect that the target cell will be drawn, even though it did not change
                        if(currentGameMap.getEntity(currentGameMap.getTargetPoint(name))==NO_ENTITY)
                            currentGameMap.setFlag(currentGameMap.getTargetPoint(name), CellFlag.ACTION,true);
                    }
                    if(!(currentGameMap.getEntity(currentGameMap.getTargetPoint(name))==NO_ENTITY)&&!(currentGameMap.getItem(currentGameMap.getTargetPoint(name)) == ItemType.BOULDER))
                        currentGameMap.setFlag(position , CellFlag.ACTION,true );
                    if(currentGameMap.getEntity(currentGameMap.getTargetPoint(name)).isSpecialized() && Util.isOppositeDir(currentEntity.getDirection(),currentGameMap.getEntity(currentGameMap.getTargetPoint(name)).getDirection()))
                        break;

                    currentGameMap.kill(currentGameMap.getTargetPoint(name));
                    break;

                case MOVE:
                    tryToMoveCell(name,true);
                    break;
                case BACK_OFF:
                    tryToMoveCell(name,false);
                    break;
                case TURN:
                    tryToTurnCell(position,methodCall.getExpressionTree().getRightNode().getText(),methodCall);
                    break;
                case USE_ITEM:
                    tryToUseItem(position);
                    break;
                case COLLECT:
                    if(currentGameMap.getEntity(position).isSpecialized())break;
                    tryToCollect(position);
                    break;
                case DROP_ITEM:
                    if(currentGameMap.getEntity(position).isSpecialized())break;
                    if(currentGameMap.getEntity(position).getItem()==ItemType.NONE)break;
                    tryToDropItem(position);
                    break;

            }
        }
    }
    private static void tryToDispossess(Point actorPos) {
        Entity actorEntity = currentGameMap.getEntity(actorPos);
        tryToDispossess(actorPos, actorEntity.getDirection());
    }
    private static void tryToDispossess(Point actorPos, Direction dir) {
        String name = currentGameMap.getEntity(actorPos).getName();
        Point targetPos =  currentGameMap.getTargetPoint(name);
        Entity actorEntity = currentGameMap.getEntity(actorPos);
        if(!currentGameMap.isCellFree(currentGameMap.getCellInDirection(actorPos,dir))){
            if(currentGameMap.getEntity(targetPos) != NO_ENTITY && !(currentGameMap.getEntity(targetPos).getEntityType() == EntityType.SKELETON && currentGameMap.getEntity(targetPos).isSpecialized())){
                Entity targetEntity = currentGameMap.getEntity(targetPos);
                targetEntity.becomePossessedBy(actorEntity.dispossess(dir));
            }
            return;
        }
        Entity ghost = actorEntity.dispossess(dir);
        currentGameMap.setEntity(currentGameMap.getCellInDirection(actorPos,dir), ghost);
    }

    private static void tryToPossess(Point actorPos) {
        String name = currentGameMap.getEntity(actorPos).getName();
        Point targetPos =  currentGameMap.getTargetPoint(name);
        Entity actorEntity = currentGameMap.getEntity(actorPos);
        Entity targetEntity = currentGameMap.getEntity(targetPos);
        if(targetEntity == NO_ENTITY ||( targetEntity.getEntityType() == EntityType.SKELETON && targetEntity.isSpecialized()))return;
        if(targetEntity.isPossessed())return;
        currentGameMap.setFlag(actorPos, CellFlag.ACTION,true);
        targetEntity.becomePossessedBy(actorEntity);
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
