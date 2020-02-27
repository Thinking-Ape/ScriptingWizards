package main.main;

import main.model.Model;
import main.model.statement.ComplexStatement;
import main.parser.CodeParser;
import main.utility.Util;
import main.view.CodeArea;

import java.util.Arrays;

public abstract class Tester {

    public static void runTests(Model model){
        int i = 0;
//        System.out.println("aha" + i++);
//        System.out.println("aha" + i);
//        boolean a = GameConstants.stringInEnum(CellContent.class,"banane");
//        boolean b = GameConstants.stringInEnum(CellContent.class,"EMPTY");
//        boolean c = GameConstants.stringInEnum(CellContent.class,"empty");
//        CodeParser codeParser = new CodeParser();
//        try {
            //"for( int i = 0;i<10;i++){","knight1.move();","}",
            //(a<5||c>=b)||false
            //TODO: negative Zahlen machen Probleme!!!
//            ComplexStatement behaviour = codeParser.parseProgramCode(new String[]{"Knight knight1 = new Knight();","int a = 11;","while(a>5){","knight1.move();","a--;","}","knight1.turn(3);","Knight knight2 = new Knight();","for(int i = 0;i<5;i++){","knight1.move();","knight2.move();","}","knight2.turn(2);"});
//            behaviour.print();
//            model.getCurrentLevel().setPlayerBehaviour(behaviour);
//            Timeline timeline = new Timeline();
//
//            timeline.setCycleCount(Timeline.INDEFINITE);
//            timeline.getKeyFrames().addAll(new KeyFrame(Duration.seconds(1.0), event ->
//            {
//                try {
//                    model.getCurrentLevel().executeTurn();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }));
//            timeline.play();
//            timeline.playFromStart();
//        }
//        catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        System.out.println(a+", "+ b+", " +c);
    }


//    public static CodeBoxCompound evaluateCodeBox(String... codeList ) throws IllegalAccessException {
//        CodeParser codeParser = new CodeParser();
//        ComplexStatement behaviour = codeParser.parseProgramCode(codeList);
//        return new CodeBoxCompound(behaviour);
//    }
    public static CodeArea evaluateCodeBox(String... codeList ) throws IllegalAccessException {
        CodeParser codeParser = new CodeParser();
        ComplexStatement behaviour = codeParser.parseProgramCode(Arrays.asList(codeList));
        return new CodeArea(behaviour,true,false);
    }
}
