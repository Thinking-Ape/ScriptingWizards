package main.test;

import main.model.CodeExecutor;
import main.model.GameConstants;
import main.model.gamemap.GameMap;
import main.model.gamemap.enums.Direction;
import main.model.statement.*;
import main.model.statement.Expression.Expression;
import main.parser.CodeParser;
import main.utility.Point;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static main.utility.VariableType.KNIGHT;

public class CodeExecutorTest {

    @Test
    public void testExecuteBehaviour() {
        simpleTest();
        System.out.println(TestConstants.SUCCESS_STRING);
    }

    private void simpleTest() {
        GameMap gameMap = new GameMap(TestConstants.TEST_MOVE_MAP);
        System.out.println("Map:");
        gameMap.printMap();
        Assignment spawnKnight = new Assignment("k", KNIGHT, Expression.expressionFromString("new Knight()"), true);
        MethodCall moveKnight = new MethodCall(MethodType.MOVE,"k","");
        MethodCall turnKnightLeft = new MethodCall(MethodType.TURN,"k","LEFT");
        MethodCall turnKnightRight = new MethodCall(MethodType.TURN,"k","RIGHT");
        MethodCall turnKnightAround = new MethodCall(MethodType.TURN,"k","AROUND");
        System.out.println("Testing code:\n" +spawnKnight.getAllText().trim()+"\n"+moveKnight.getAllText().trim()+"\n"+turnKnightLeft.getAllText().trim()+"\n"+moveKnight.getAllText().trim());
        System.out.println("Expecting: Knight spawns at (1,2), moves to (1,1) and then turns LEFT, looking WEST");
        printResult("");
        Point spawn = gameMap.findSpawn();
        Assert.assertTrue(spawn.getX() != -1);
        System.out.println("Spawn is at: "+spawn.getText());
        System.out.println("Entity at "+spawn.getText()+" is: \""+ gameMap.getEntity(spawn).getName() + "\" and should be \"\"");
        Assert.assertEquals(gameMap.getEntity(spawn), GameConstants.NO_ENTITY);
        System.out.println("... executing Code: " +spawnKnight.getAllText().trim());
        CodeExecutor.executeBehaviour(spawnKnight, gameMap, true, true);
        System.out.println("Entity at "+spawn.getText()+" is: \""+ gameMap.getEntity(spawn).getName() + "\" and should be \"k\"");
        Assert.assertNotNull(gameMap.getEntity("k"));
        Assert.assertEquals(gameMap.getEntityPosition("k"), gameMap.findSpawn());
        Point target = gameMap.getTargetPoint("k");
        System.out.println("Entity is facing: \""+ gameMap.getEntity(gameMap.findSpawn()).getDirection()+ "\" and should be \"NORTH\"");
        System.out.println("... executing Code: " +moveKnight.getAllText().trim());
        CodeExecutor.executeBehaviour(moveKnight, gameMap, true, true);
        System.out.println("Entity at "+target.getText()+" is: \""+ gameMap.getEntity(target).getName() + "\" and should be \"k\"");
        Assert.assertEquals(gameMap.getEntityPosition("k"), target);
        System.out.println("Entity at "+spawn.getText()+" is: \""+ gameMap.getEntity(new Point(1, 2)).getName() + "\" and should be \"\"");
        Assert.assertEquals(gameMap.getEntity(spawn), GameConstants.NO_ENTITY);
        System.out.println("... executing Code: " +turnKnightLeft.getAllText().trim());
        CodeExecutor.executeBehaviour(turnKnightLeft, gameMap, true, true);
        Assert.assertEquals(gameMap.getEntityPosition("k"), target);
        System.out.println("Entity "+gameMap.getEntity(target).getName()+" looks towards: \""+ gameMap.getEntity(target).getDirection().name() + "\" and should be \"WEST\"");
        Assert.assertEquals(gameMap.getEntity("k").getDirection(), Direction.WEST);
        System.out.println("... executing Code: " +moveKnight.getAllText().trim());
        CodeExecutor.executeBehaviour(moveKnight, gameMap, true, true);
        System.out.println("Entity at "+target.getText()+" is: \""+ gameMap.getEntity(target).getName() + "\" and should be \"k\"");
        Assert.assertEquals(gameMap.getEntityPosition("k"), target);
        System.out.println("... executing Code: " +turnKnightRight.getAllText().trim());
        CodeExecutor.executeBehaviour(turnKnightRight, gameMap, true, true);
        System.out.println("Entity "+gameMap.getEntity(target).getName()+" looks towards: \""+ gameMap.getEntity(target).getDirection().name() + "\" and should be \"NORTH\"");
        Assert.assertEquals(gameMap.getEntity("k").getDirection(), Direction.NORTH);
        System.out.println("... executing Code: " +turnKnightAround.getAllText().trim());
        CodeExecutor.executeBehaviour(turnKnightAround, gameMap, true, true);
        System.out.println("Entity "+gameMap.getEntity(target).getName()+" looks towards: \""+ gameMap.getEntity(target).getDirection().name() + "\" and should be \"SOUTH\"");
        Assert.assertEquals(gameMap.getEntity("k").getDirection(), Direction.SOUTH);
        System.out.println("... executing Code: " +turnKnightAround.getAllText().trim());
        CodeExecutor.executeBehaviour(turnKnightAround, gameMap, true, true);
        System.out.println("Entity "+gameMap.getEntity(target).getName()+" looks towards: \""+ gameMap.getEntity(target).getDirection().name() + "\" and should be \"NORTH\"");
        Assert.assertEquals(gameMap.getEntity("k").getDirection(), Direction.NORTH);
        gameMap.printMap();
    }
    private void printResult(String s) {
        System.out.println("Result: "+s);
    }

}