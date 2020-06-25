package main.main;

import main.model.gamemap.enums.CellContent;
import main.test.CodeEvaluatorTest;
import main.test.CodeExecutorTest;
import main.test.CodeParserTest;
import main.utility.VariableType;
import main.utility.Util;

import java.util.ArrayList;
import java.util.List;

public abstract class Tester {

    public static void runTests(){
        // Remove those in case of build as junit is considered an automatic module that cannot be used with jlink
        new CodeEvaluatorTest().testEvaluateStatement();
        new CodeParserTest().testParseProgramCode();
        new CodeExecutorTest().testExecuteBehaviour();
    }
}
