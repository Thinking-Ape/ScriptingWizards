package main.test;

import main.model.CodeEvaluator;
import main.model.GameConstants;
import main.model.statement.*;
import main.parser.CodeParser;
import main.utility.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static main.model.GameConstants.FALSE_STATEMENT;
import static main.test.TestConstants.SUCCESS_STRING;

public class CodeEvaluatorTest {

    @Test
    public void testEvaluateStatement() {
        testForLoop();
        System.out.println(SUCCESS_STRING);
        testIfStatement();
        System.out.println(SUCCESS_STRING);
    }

    private void testForLoop() {
        ComplexStatement behaviour = testCodeParser("\"int i = 0;\",\"for(int j = 0; j <5; j++){\",\"i=i+j;\",\"}\"", "All statements are correctly evaluated and j = 4 and i = 4*5/2=10");
        printResult("\n"+behaviour.getAllText().trim());
        System.out.println("Evaluating Code:...");
        CodeEvaluator testEvaluator = new CodeEvaluator(true );
        StatementIterator statementIterator = behaviour.iterator();
        Statement currentBehaviour = behaviour;
        int iteration = 0;
        while (currentBehaviour != null){
            currentBehaviour = statementIterator.next();
            Statement evaluatedBehaviour = testEvaluator.evaluateStatement(currentBehaviour);
            if(evaluatedBehaviour == FALSE_STATEMENT)
                statementIterator.skip(currentBehaviour.getDepth()-1);
            if(currentBehaviour != null && currentBehaviour.getDepth() == 2){
                Variable i = testEvaluator.getVariableCopyWithName("i");
                Variable j = testEvaluator.getVariableCopyWithName("j");
                Assert.assertNotNull(i);
                Assert.assertNotNull(j);
                System.out.println("Loop step "+(iteration+1)+": i = "+i.getValue().getText()+", Expected: "+((iteration*iteration+iteration)/2));
                Assert.assertEquals((iteration*iteration+iteration)/2+"",i.getValue().getText());
                System.out.println("Loop step "+(iteration+1)+": j = "+j.getValue().getText()+", Expected: "+(iteration));
                Assert.assertEquals(iteration+"",j.getValue().getText());
                iteration++;
            }
        }
    }

    private void testIfStatement() {
        ComplexStatement behaviour = testCodeParser("\"int i;\",\"int j;\",\"if((1<2 && true) || !false){\",\"i=2;\",\"}\",\"else {\",\"i=1;\",\"}\",\"if(!i == 2 || false){\",\"j=2;\",\"}\",\"else {\",\"j=1;\",\"}\"", "All statements are correctly evaluated and i = 2");
        printResult("\n"+behaviour.getAllText().trim());
        System.out.println("Evaluating Code:...");
        CodeEvaluator testEvaluator = new CodeEvaluator(true );
        StatementIterator statementIterator = behaviour.iterator();
        Statement currentBehaviour = behaviour;
        while (currentBehaviour != null){
            if(currentBehaviour.getStatementType() == StatementType.ASSIGNMENT);
            currentBehaviour = statementIterator.next();
            Statement evaluatedBehaviour = testEvaluator.evaluateStatement(currentBehaviour);
            if(evaluatedBehaviour == FALSE_STATEMENT)
                statementIterator.skip(currentBehaviour.getDepth()-1);
        }
        String i = testEvaluator.getVariableCopyWithName("i").getValue().getText();
        System.out.println("i is "+ i + " and should be 2");
        Assert.assertEquals("2",i);
        String j = testEvaluator.getVariableCopyWithName("j").getValue().getText();
        System.out.println("j is "+ j + " and should be 1");
        Assert.assertEquals("1",j);
    }


    private void printResult(String s) {
        System.out.println("Result: "+s);
    }

    private ComplexStatement testCodeParser(String testInput, String expectedResult){
        System.out.println("Testing input: " +testInput +"\nExpecting: "+expectedResult);
        testInput = testInput.replaceAll("^\"(.*)\"$","$1");
        return CodeParser.parseProgramCode(List.of(testInput.split("\",\"")));
    }
}