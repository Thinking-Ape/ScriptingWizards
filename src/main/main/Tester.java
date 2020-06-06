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
        int i = 0;
        System.out.println("aha" + i++);
        System.out.println("aha" + i);
        String[] test = "k".replaceAll(VariableType.ARMY.getAllowedRegex(), "$1").split(",");
        for(int j = 0; j < test.length; j++){
            System.out.println(test[j]);
        }
        boolean a = Util.stringInEnum(CellContent.class,"banane");
        boolean b = Util.stringInEnum(CellContent.class,"EMPTY");
        boolean c = Util.stringInEnum(CellContent.class,"empty");
        List<String> stringListA = new ArrayList<>();
        stringListA.add("hallo1");
        stringListA.add("hallo2");
        stringListA.add("hallo3");
        List<String> stringListB = new ArrayList<>();
        stringListB.add("hallo1");
        stringListB.add("hallo2");
        stringListB.add("hallo3");
        List<String> stringListC = new ArrayList<>();
        stringListC.add("hallo1");
        stringListC.add("hallo2");
        stringListC.add("hallo4");
        List<String> stringListD = new ArrayList<>();
        stringListD.add("hallo1");
        stringListD.add("hallo2");
        System.out.println(a+", "+ b+", " +c);
        System.out.println(stringListA.equals(stringListB));
        System.out.println(stringListA.equals(stringListA));
        System.out.println(stringListA.equals(stringListC));
        System.out.println(stringListA.equals(stringListD));
        new CodeEvaluatorTest().testEvaluateStatement();
        new CodeParserTest().testParseProgramCode();
        new CodeExecutorTest().testExecuteBehaviour();

        System.out.println(Util.dist("allo".toCharArray(),"hallo".toCharArray()));
        System.out.println(Util.dist("hall".toCharArray(),"hallo".toCharArray()));
        System.out.println(Util.dist("eag".toCharArray(),"hallo".toCharArray()));
        System.out.println(Util.dist("wa".toCharArray(),"wait".toCharArray()));
    }
}
