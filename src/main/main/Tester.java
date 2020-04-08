package main.main;

import main.model.enums.CellContent;
import main.model.enums.VariableType;
import main.utility.Util;

import java.util.ArrayList;
import java.util.List;

public abstract class Tester {

    public static void runTests(){
//        int i1 = 0;
//        int i2 = i1;
//        boolean b = i2 == 0;
//        i2 = 1;
//        System.out.println(b);
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
    }


//    public static CodeBoxCompound evaluateCodeBox(String... codeList ) throws IllegalAccessException {
//        OldCodeParser codeParser = new OldCodeParser();
//        ComplexStatement behaviour = codeParser.parseProgramCode(codeList);
//        return new CodeBoxCompound(behaviour);
//    }
//    public static CodeArea evaluateCodeBox(String... codeList ) {
//        ComplexStatement behaviour = OldCodeParser.parseProgramCode(Arrays.asList(codeList));
//        return new CodeArea(behaviour,true,false);
//    }
}
