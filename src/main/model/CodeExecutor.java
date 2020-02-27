package main.model;

import main.model.enums.*;
import main.model.statement.*;
import main.model.statement.Expression.ExpressionTree;
import main.utility.GameConstants;
import main.utility.Point;
import main.utility.Variable;
import main.utility.VariableType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import main.model.statement.Condition.*;

public class CodeExecutor {
    private int noStackOverflow; //TODO: evaluate
    private GameMap gameMap;
    private boolean hasWon=false;
    private boolean hasLost = false;

    private List<String> unlocks;

    public void setUnlockedStatementList(List<String> unlockedStatementList){
        this.unlocks  = unlockedStatementList;
    }

    boolean executeBehaviour(Statement statement, GameMap gameMap, boolean isPlayer) throws IllegalAccessException {
        this.gameMap  = gameMap;
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

                Point spawn = gameMap.findSpawn();

                if(statement.getStatementType() == StatementType.ASSIGNMENT) gameMap.getEntity(name).deleteIdentity();
                if(spawn.getX() != -1&&gameMap.isCellFree(spawn))gameMap.spawn(spawn,new Entity(name,direction, EntityType.KNIGHT));

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

    private void updateUnlocks(Statement statement) {

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

    private List<String> getUnlockedBooleanMethodsFromStatement(Statement complexStatement, Condition condition) {
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
        CellContent targetContent = gameMap.getContentAtXY(targetPos);
        if(actorEntity.getItem() == ItemType.KEY&&targetContent == CellContent.EXIT){
            actorEntity.setItem(ItemType.NONE);
            gameMap.setFlag(targetPos, CFlag.OPEN, true);
            hasWon = true;
            return;
        }
//        if(actorEntity.getItem() == ItemType.BEACON){
//            beaconEntity = actorEntity;
//        }
        if((actorEntity.getItem() == ItemType.SHOVEL||actorEntity.getItem() == ItemType.SWORD)&&GameConstants.ACTION_WITHOUT_CONSEQUENCE)gameMap.setFlag(actorPos , CFlag.ACTION,true );
        if(actorEntity.getItem() == ItemType.SHOVEL&&targetContent == CellContent.DIRT){
            gameMap.setContent(targetPos, CellContent.PATH);
            gameMap.setFlag(actorPos, CFlag.ACTION, true );
            gameMap.setFlag(targetPos, CFlag.DIRT_REMOVED, true );
        }
        if(actorEntity.getItem() == ItemType.SWORD){//&&gameMap.getEntity(targetPos) != NO_ENTITY){
            if(gameMap.getItem(targetPos) == ItemType.BOULDER)return;
            gameMap.kill(targetPos); //TODO: maybe -> targetCell.kill();??
            gameMap.setFlag(actorPos, CFlag.ACTION, true );
        }

    }

    private void tryToDropItem(Point actorPos) {
        String name = gameMap.getEntity(actorPos).getName();
        Point targetPos =  gameMap.getTargetPoint(name);
        Entity actorEntity = gameMap.getEntity(actorPos);
        CellContent targetContent = gameMap.getContentAtXY(targetPos);

        if((targetContent.isTraversable()||gameMap.gateIsOpen(targetPos))&&gameMap.isCellFree(targetPos)){
                gameMap.setItem(targetPos,actorEntity.getItem());
                actorEntity.setItem(ItemType.NONE);
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
        if(gameMap.getItem(targetPoint) != ItemType.NONE){
            ItemType item = ItemType.NONE;
            if(actorCell.getEntity().getItem()!=ItemType.NONE) item = actorCell.getEntity().getItem();
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
            CellContent targetContent =gameMap.getContentAtXY(targetPoint);
//            if(targetContent==CellContent.EXIT){
//                if(isPlayer)hasWon = true;
//                return;
//                //TODO: replace with better handling in controller!
//            }

            gameMap.removeEntity(actorPoint);
            gameMap.setEntity(targetPoint,  actorEntity );

            if(targetContent== CellContent.TRAP && gameMap.cellHasFlag(targetPoint,CFlag.ARMED)){
                gameMap.kill(targetPoint);
            }

            if(targetContent == CellContent.PRESSURE_PLATE){
                gameMap.setFlag(targetPoint,CFlag.TRIGGERED,true);
            }
            if(gameMap.getContentAtXY(actorPoint) == CellContent.PRESSURE_PLATE){
                gameMap.setFlag(actorPoint,CFlag.TRIGGERED,false);
            }
//            output = targetCell;
        }
//        return output;
    }

    private void executeMethodCall(MethodCall methodCall, boolean isPlayer) throws IllegalAccessException {
        List<String> nameList = new ArrayList<>();
        nameList.add(methodCall.getObjectName());
        if(methodCall.getParentStatement().getVariable(nameList.get(0)).getVariableType()==VariableType.ARMY){
            Variable v  = methodCall.getParentStatement().getVariable(nameList.get(0));
            nameList = new ArrayList<>(Arrays.asList(v.getValue().getRightNode().getText().split(",")));
        }
        for(String name : nameList){
//            if(!isPlayer) System.out.println(methodCall.getText()+" "+name);
        Point position = gameMap.getEntityPosition(name);
        if(position == null ){
            if(isPlayer&& gameMap.getAmountOfKnights() == 0)hasLost=true;
            continue;
        }
        switch (methodCall.getMethodType()){
            case ATTACK:
                if(isPlayer)throw new IllegalAccessException("You cannot attack as Player!");

                // Can't attack with an item in hand
                if(gameMap.getEntity(name).getItem()!=ItemType.NONE)break;
                if(GameConstants.ACTION_WITHOUT_CONSEQUENCE)gameMap.setFlag(position , CFlag.ACTION,true );
//                if(gameMap.getEntity(gameMap.getTargetPoint(name)) == NO_ENTITY ||gameMap.getEntity(gameMap.getTargetPoint(name)) == NO_ENTITY)break;

                if(gameMap.getItem(gameMap.getTargetPoint(name)) == ItemType.BOULDER)break;
                gameMap.setFlag(position , CFlag.ACTION,true );
                gameMap.kill(gameMap.getTargetPoint(name)); //TODO: stattdessen mit getTargetPoint()?
                break;

            case MOVE:
                tryToMoveCell(name,isPlayer); //TODO: stattdessen mit getTargetPoint()?
                break;
            case TURN:
                tryToTurnCell(position,methodCall.getExpressionTree().getRightNode().getText(),methodCall);//evaluateIntVariable(methodCall.getExpressionTree().getRightCondition().getText()));
                break;
            case USE_ITEM:
                if(gameMap.getEntity(position).getItem()==ItemType.NONE)break;
                tryToUseItem(position);
                break;
            case COLLECT:
                tryToCollect(position);
                break;
            case DROP_ITEM:
                if(gameMap.getEntity(position).getItem()==ItemType.NONE)break;
                tryToDropItem(position);
                break;
            case EXECUTE_IF:
                String booleanString = methodCall.getParameters()[0];
                if(booleanString.matches("(.+ )*"+name+":"+"true (.+ )*")){
                    ExpressionTree expressionTree = evaluateCommand(methodCall.getParameters()[1],methodCall);
                    MethodCall mc1 = new MethodCall(MethodType.getMethodTypeFromCall(expressionTree.getText()), name,expressionTree.getRightNode().getText() );
                    mc1.setParentStatement(methodCall.getParentStatement());
                    executeMethodCall(mc1, isPlayer);
//                    booleanString =booleanString.replaceFirst("true", "");
                }
                else if(booleanString.matches("(.+ )*"+name+":"+"false (.+ )*")){
                    ExpressionTree expressionTree = evaluateCommand(methodCall.getParameters()[2],methodCall);
                    MethodCall mc2 = new MethodCall(MethodType.getMethodTypeFromCall(expressionTree.getText()), name,expressionTree.getRightNode().getText() );
                    mc2.setParentStatement(methodCall.getParentStatement());
                    executeMethodCall(mc2, isPlayer);
//                    booleanString =booleanString.replaceFirst("false", "");
                }
                else throw new IllegalArgumentException("Something went wrong");
                break;
        }}
    }

    private ExpressionTree evaluateCommand(String parameter, MethodCall methodCall) {
        if(methodCall.getParentStatement().getVariable(parameter)!=null){
            return evaluateCommand(methodCall.getParentStatement().getVariable(parameter).getValue().getText(), methodCall);
        }
        else return ExpressionTree.expressionTreeFromString(parameter);
    }

    boolean hasWon() {
        return hasWon;
    }
    boolean hasLost() {
        return hasLost;
    }

    void reset(){
        hasWon = false;
        hasLost = false;
        noStackOverflow = 0;
    }

    public int getNoStackOverflow() {
        return noStackOverflow;
    }

    public List<String> getUnlockedStatementList() {
        return unlocks;
    }
}
