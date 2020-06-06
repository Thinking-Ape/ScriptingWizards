package main.test;

import main.exception.*;
import main.model.GameConstants;
import main.model.statement.*;
import main.parser.CodeParser;
import main.utility.*;
import main.view.CodeAreaType;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;

import static main.test.TestConstants.SUCCESS_STRING;


public class CodeParserTest {

    @Test
    public void testParseProgramCode() {
        testKnightConstructor();
        System.out.println(SUCCESS_STRING);
        testNoSemicolon();
        System.out.println(SUCCESS_STRING);
        testNoCurlyBracket();
        System.out.println(SUCCESS_STRING);
        testUnbalancedAmountOfBrackets();
        System.out.println(SUCCESS_STRING);
        testInvalidCondition();
        System.out.println(SUCCESS_STRING);
        testForLoop();
        System.out.println(SUCCESS_STRING);
        testVariableNotInitialized();
        System.out.println(SUCCESS_STRING);
        testNoValueVariable();
        System.out.println(SUCCESS_STRING);
        testNoSkeletonAllowed();
        System.out.println(SUCCESS_STRING);
        testNoKnightAllowed();
        System.out.println(SUCCESS_STRING);

    }

    private void testKnightConstructor() {
        ComplexStatement behaviour = testCodeParser("Knight k = new Knight();", "Creates ComplexStatement with an"+ Assignment.class.getName() +"representing the constructor!");
        printResult(behaviour.getAllText().trim());
        System.out.println("Amount of substatements:"+behaviour.getStatementListSize());
        Assert.assertEquals(1,behaviour.getStatementListSize());
        System.out.println("Substatement class: "+behaviour.getSubStatement(0).getClass().getName());
        Assert.assertTrue(behaviour.getSubStatement(0) instanceof Assignment);
        Assignment ass = (Assignment)behaviour.getSubStatement(0);
        Assert.assertNotNull(ass.getVariable());
        System.out.println("Variable Name: " + ass.getVariable().getName());
        Assert.assertEquals("k",ass.getVariable().getName());
        System.out.println("Variable Type: " + ass.getVariable().getVariableType().getName());
        Assert.assertEquals(VariableType.KNIGHT,ass.getVariable().getVariableType());
        System.out.println("Variable Value: " + ass.getVariable().getValue().getText());
        Assert.assertEquals("new Knight()",ass.getVariable().getValue().getText());
    }

    private void testForLoop() {
        ComplexStatement behaviour = testCodeParser("\"int i = 0;\",\"for(int j = 0; j <5; j++){\",\"i=i+1;\",\"}\"", "All statements are correctly evaluated and j = 5 und i = 5*6/2=15");
        printResult("\n"+behaviour.getAllText().trim());
        System.out.println("Amount of substatements: "+behaviour.getStatementListSize());
        Assert.assertEquals(4,behaviour.getActualSize());
        System.out.println("Substatement 1: " + behaviour.getSubStatement(0).getCode()+ " class: "+behaviour.getSubStatement(0).getClass().getName());
        Assert.assertTrue(behaviour.getSubStatement(0) instanceof Assignment);
        System.out.println("Substatement 2: " + behaviour.getSubStatement(1).getCode()+ " class: "+behaviour.getSubStatement(1).getClass().getName());
        Assert.assertTrue(behaviour.getSubStatement(1) instanceof ForStatement);
        ForStatement forStatement = (ForStatement) behaviour.getSubStatement(1);
        System.out.println("Substatement of For-Loop: " + forStatement.getSubStatement(0).getCode()+ " class: "+forStatement.getSubStatement(0).getClass().getName());
        Assert.assertTrue(forStatement.getSubStatement(0) instanceof Assignment);
        Assignment ass = (Assignment)behaviour.getSubStatement(0);
        Assert.assertNotNull(ass.getVariable());
        System.out.println("Variable Name: " + ass.getVariable().getName());
        Assert.assertEquals("i",ass.getVariable().getName());
        System.out.println("Variable Type: " + ass.getVariable().getVariableType().getName());
        Assert.assertEquals(VariableType.INT,ass.getVariable().getVariableType());
        System.out.println("Variable Value: " + ass.getVariable().getValue().getText());
        Assert.assertEquals("0",ass.getVariable().getValue().getText());
        Assert.assertNotNull(forStatement.getDeclaration().getVariable());
        System.out.println("Variable Name: " + forStatement.getDeclaration().getVariable().getName());
        Assert.assertEquals("j",forStatement.getDeclaration().getVariable().getName());
        System.out.println("Variable Type: " + forStatement.getDeclaration().getVariable().getVariableType().getName());
        Assert.assertEquals(VariableType.INT,forStatement.getDeclaration().getVariable().getVariableType());
        System.out.println("Variable Value: " + forStatement.getDeclaration().getVariable().getValue().getText());
        Assert.assertEquals("0",forStatement.getDeclaration().getVariable().getValue().getText());
    }

    private void testNoSemicolon() {
        testForException("Knight k = new Knight()", NoSemicolonException.class, NoSemicolonException.errorMessage);
    }

    private void testNoCurlyBracket() {
        testForException("if(true)", NoCurlyBracketException.class, NoCurlyBracketException.errorMessage);
    }
    private void testUnbalancedAmountOfBrackets() {
        testForException("if(true){",UnbalancedAmountOfBracketsException.class, UnbalancedAmountOfBracketsException.errorMessage);
    }

    private void testInvalidCondition() {
        String condString = "1=2";
        testForException("if("+condString+"){", InvalidConditionException.class,Util.getConditionInvalidString(condString, GameConstants.REASON_SINGLE_EQUAL_SIGN));
    }

    private void testVariableNotInitialized() {
        testForException("\"int i;\",\"if(i<5) {\"", VariableNotInitializedException.class,VariableNotInitializedException.errorMessage);
    }
    private void testNoValueVariable() {
        testForException("int i&1;", IllegalVariableNameException.class,Util.getIllegalVariableString("i&1"));
    }
    private void testNoSkeletonAllowed() {
        testForException("Skeleton sk = new Skeleton();",StatementNotAllowedException.class,StatementNotAllowedException.errorMessage);
    }

    private void testNoKnightAllowed() {
        testForException("Knight k = new Knight();",StatementNotAllowedException.class,StatementNotAllowedException.errorMessage,true);
    }



    private void printResult(String s) {
        System.out.println("Result: "+s);
    }

    private ComplexStatement testCodeParser(String testInput, String expectedResult){
        return testCodeParser(testInput, expectedResult, false);
    }
    private ComplexStatement testCodeParser(String testInput, String expectedResult, boolean isEnemy){
        System.out.println("Testing input: " +testInput +"\nExpecting: "+expectedResult);
        testInput = testInput.replaceAll("^\"(.*)\"$","$1");
        CodeAreaType codeAreaType = isEnemy ? CodeAreaType.AI : CodeAreaType.PLAYER;
        return CodeParser.parseProgramCode(List.of(testInput.split("\",\"")), codeAreaType);
    }
    private void testForException(String testInput, Class errorClass, String errorMessage) {
        testForException(testInput, errorClass, errorMessage,false);
    }

    private void testForException(String testInput, Class errorClass, String errorMessage,boolean isEnemy) {
        try{
            testCodeParser(testInput,"Throws "+ errorClass.getName()+" with message: "+errorMessage,isEnemy);
        }catch (Exception e){
            printResult(e.getClass().getName()+": \""+e.getMessage()+"\"");
            Assert.assertSame(e.getClass(), errorClass);
            Assert.assertEquals(e.getMessage(),errorMessage);
            return;
        }
        printResult("No Exception!");
        Assert.fail();
    }
}