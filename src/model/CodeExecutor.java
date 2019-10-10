package model;

import model.enums.*;
import model.statement.*;
import model.statement.Expression.ExpressionTree;
import utility.GameConstants;
import utility.Point;
import utility.Variable;
import utility.VariableType;

public class CodeExecutor {
    int noStackOverflow; //TODO: evaluate
    private GameMap gameMap;
    private boolean hasWon=false;
    private boolean hasLost = false;


    boolean executeBehaviour(Statement statement,GameMap gameMap, boolean isPlayer) throws IllegalAccessException {
        this.gameMap  = gameMap;
        boolean method_Called = false;
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

                Point spawn = gameMap.findSpawn();
                if(spawn.getX() == -1)throw new IllegalStateException("No spawn found!");
                if(statement.getStatementType() == StatementType.ASSIGNMENT) gameMap.getEntity(name).deleteIdentity();
                if(gameMap.isCellFree(gameMap.findSpawn()))gameMap.spawn(spawn,new Entity(name,direction, EntityType.KNIGHT));

            }else if(assignment.getVariable().getVariableType() == VariableType.SKELETON){ //TODO: stattdessen ENEMY?
                method_Called = true;
                if(gameMap.getEnemySpawnList().size()==0)return true;
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
                int index = GameConstants.RANDOM.nextInt(gameMap.getEnemySpawnList().size());
                Point spawnPoint = new Point(gameMap.getEnemySpawnList().get(index).getX(),gameMap.getEnemySpawnList().get(index).getY());
                if(spawnId != ""){
                    for(Point point : gameMap.getEnemySpawnList()){
                        spawnPoint = point;
                        if(gameMap.getCellID(spawnPoint)==Integer.valueOf(spawnId)){
                            gameMap.spawn(spawnPoint,new Entity(name,direction,EntityType.SKELETON));
                        }
                    }
                }
                else gameMap.spawn(spawnPoint,new Entity(name,direction,EntityType.SKELETON));
            }

        }
        return method_Called;
    }

    private Direction evaluateDirection(ExpressionTree rightNode, ComplexStatement parentStatement) {
        Direction output = Direction.NORTH;
        Variable dirVar = parentStatement.getVariable(rightNode.getText());
        if(rightNode != null)output = Direction.getValueFromString(rightNode.getText());
        if(output == null){
            if(dirVar != null && dirVar.getVariableType() == VariableType.DIRECTION)return evaluateDirection(dirVar.getValue(),parentStatement);
            else return Direction.NORTH;
        }
        return output;
    }

    private void tryToUseItem(Point actorPos) {
        String name = gameMap.getEntity(actorPos).getName();
        Point targetPos =  gameMap.getTargetPoint(name);
        Entity actorEntity = gameMap.getEntity(actorPos);
        CContent targetContent = gameMap.getContentAtXY(targetPos);
        gameMap.setFlag(actorPos, CFlag.ACTION, true );
        if(actorEntity.getItem() == ItemType.KEY&&targetContent == CContent.EXIT){
            actorEntity.setItem(null);
            gameMap.setFlag(targetPos, CFlag.OPEN, true);
            hasWon = true;
            return;
        }
//        if(actorEntity.getItem() == ItemType.BEACON){
//            beaconEntity = actorEntity;
//        }
        if(actorEntity.getItem() == ItemType.SHOVEL&&targetContent == CContent.DIRT){
            gameMap.setContent(targetPos,CContent.PATH);
        }
        if(actorEntity.getItem() == ItemType.SWORD&&gameMap.getEntity(targetPos) != null){
            gameMap.kill(targetPos); //TODO: maybe -> targetCell.kill();??
        }

    }

    private void tryToDropItem(Point actorPos) {
        String name = gameMap.getEntity(actorPos).getName();
        Point targetPos =  gameMap.getTargetPoint(name);
        Entity actorEntity = gameMap.getEntity(actorPos);
        CContent targetContent = gameMap.getContentAtXY(targetPos);

        if(targetContent.isTraversable()&&gameMap.isCellFree(targetPos)){
                gameMap.setItem(targetPos,actorEntity.getItem());
                actorEntity.setItem(null);
        }
    }

    /** Will swap any item a Knight is carrying with whatever item lies in front of him. Does not drop the item if there is no item in front of the Knight!
     *
     * @param
     * @return
     */
    private boolean tryToCollect(Point actorPoint) {
        Cell actorCell = gameMap.getCellAtXYClone(actorPoint.getX(),actorPoint.getY());
        Point targetPoint =  gameMap.getTargetPoint(actorCell.getEntity().getName());
        if(gameMap.getItem(targetPoint) != null){
            ItemType item = null;
            if(actorCell.getEntity().getItem()!=null) item = actorCell.getEntity().getItem();
            gameMap.getEntity(actorPoint).setItem(gameMap.getItem(targetPoint));
            gameMap.setItem(targetPoint,item);
//            targetCell.setFlagValue(CFlag.TRAVERSABLE,true);
//            targetCell.setFlagValue(CFlag.COLLECTIBLE,false);
            return true;
        }
        return false;
    }
    private void tryToTurnCell(Point actorPoint,String direction,MethodCall mc){
        Entity entity = gameMap.getEntity(actorPoint);

        direction = evaluateTurnDirection(direction,mc);

        switch (entity.getDirection()){
            case NORTH:
                if(direction.equals("LEFT"))
                    entity.setDirection(Direction.WEST);
                else if(direction.equals("RIGHT"))
                    entity.setDirection(Direction.EAST);
                else if(direction.equals("AROUND"))
                    entity.setDirection(Direction.SOUTH);
                break;
            case SOUTH:
                if(direction.equals("LEFT"))
                    entity.setDirection(Direction.EAST);
                else if(direction.equals("RIGHT"))
                    entity.setDirection(Direction.WEST);
                else if(direction.equals("AROUND"))
                    entity.setDirection(Direction.NORTH);
                break;
            case EAST:
                if(direction.equals("LEFT"))
                    entity.setDirection(Direction.NORTH);
                else if(direction.equals("RIGHT"))
                    entity.setDirection(Direction.SOUTH);
                else if(direction.equals("AROUND"))
                    entity.setDirection(Direction.WEST);
                break;
            case WEST:
                if(direction.equals("LEFT"))
                    entity.setDirection(Direction.SOUTH);
                else if(direction.equals("RIGHT"))
                    entity.setDirection(Direction.NORTH);
                else if(direction.equals("AROUND"))
                    entity.setDirection(Direction.EAST);
                break;
        }
    }

    private String evaluateTurnDirection(String direction, MethodCall mc) {
        String output = direction;
        Variable tdirVar = mc.getParentStatement().getVariable(direction);
        if(tdirVar!=null && tdirVar.getVariableType() == VariableType.TURN_DIRECTION)return evaluateTurnDirection(tdirVar.getValue().getText(),mc);
        else return output;
    }

    private void tryToMoveCell(String name, boolean isPlayer) {
//        Cell output = entityCell;
        Point targetPoint = gameMap.getTargetPoint(name);
        Point actorPoint = gameMap.getEntityPosition(name);
//        Entity targetEntity = gameMap.getEntity(targetPoint);
        Entity actorEntity = gameMap.getEntity(actorPoint);
//        if(targetEntity != null && targetEntity.getEntityType() == EntityType.SKELETON && actorEntity.getEntityType() == EntityType.KNIGHT){
//            gameMap.kill(actorPoint);
//        }
//        if(targetEntity != null && targetEntity.getEntityType() == EntityType.KNIGHT && actorEntity.getEntityType() == EntityType.SKELETON){
//            gameMap.kill(targetPoint);
//        }

        boolean isOpen = gameMap.cellHasFlag(targetPoint, CFlag.OPEN) ^ gameMap.cellHasFlag(targetPoint, CFlag.INVERTED);
        if(gameMap.isGateWrongDirection(actorPoint, targetPoint))return;
        if((gameMap.getContentAtXY(targetPoint).isTraversable()||isOpen) && gameMap.isCellFree(targetPoint)){
            CContent targetContent =gameMap.getContentAtXY(targetPoint);
//            if(targetContent==CContent.EXIT){
//                if(isPlayer)hasWon = true;
//                return;
//                //TODO: replace with better handling in controller!
//            }
            if(targetContent==CContent.TRAP && gameMap.cellHasFlag(targetPoint,CFlag.ARMED)){
                gameMap.kill(actorPoint);
            }

            gameMap.removeEntity(actorPoint);
            gameMap.setEntity(targetPoint,  actorEntity );


            if(targetContent == CContent.PRESSURE_PLATE){
                gameMap.setFlag(targetPoint,CFlag.TRIGGERED,true);
            }
            if(gameMap.getContentAtXY(actorPoint) == CContent.PRESSURE_PLATE){
                gameMap.setFlag(actorPoint,CFlag.TRIGGERED,false);
            }
//            output = targetCell;
        }
//        return output;
    }

    private void executeMethodCall(MethodCall methodCall, boolean isPlayer) throws IllegalAccessException {
        String name = methodCall.getExpressionTree().getLeftNode().getText();
        Point position = gameMap.getEntityPosition(name);
        if(position == null ){
            if(isPlayer&& gameMap.getAmountOfKnights() == 0)hasLost=true;
            return;
        }
        switch (methodCall.getMethodType()){
            case ATTACK:
                // Can't attack with an item in hand
                if(gameMap.getEntity(name).getItem()!=null)return;
                if(isPlayer)throw new IllegalAccessException("You cannot attack as Player!");
                gameMap.setFlag(position , CFlag.ACTION,true );
                gameMap.kill(gameMap.getTargetPoint(name)); //TODO: stattdessen mit getTargetPoint()?
                break;

            case MOVE:
                tryToMoveCell(name,isPlayer); //TODO: stattdessen mit getTargetPoint()?
                break;
            case TURN:
                tryToTurnCell(position,methodCall.getExpressionTree().getRightNode().getText(),methodCall);//evaluateIntVariable(methodCall.getExpressionTree().getRightNode().getText()));
                break;
            case USE_ITEM:
                if(gameMap.getEntity(position).getItem()==null)break;
                tryToUseItem(position);
                break;
            case COLLECT:
                tryToCollect(position);
                break;
            case DROP_ITEM:
                if(gameMap.getEntity(position).getItem()==null)break;
                tryToDropItem(position);
                break;
        }
    }

    boolean hasWon() {
        return hasWon;
    }
    boolean hasLost() {
        return hasLost;
    }

    void reset(){
        hasWon = false;
        hasLost = false;}
}
