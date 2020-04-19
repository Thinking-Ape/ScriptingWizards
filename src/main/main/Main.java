package main.main;

import main.controller.Controller;
import javafx.application.Application;
import javafx.stage.Stage;
import main.model.Level;
import main.model.Model;
import main.model.ModelInformer;
import main.parser.JSONParser;
import main.parser.CodeParser;
import main.model.GameConstants;
import main.utility.Util;
import main.view.View;

import java.util.List;
import java.util.Map;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        JSONParser.init();
        List<Integer> unlockedLevelIdList = JSONParser.getUnlockedLevelIds();
        List<Level> levelList = JSONParser.parseAllLevels();
        Model model = Model.getInstance();
        ModelInformer.init(model);
        if(GameConstants.DEBUG)System.out.println("UNLOCKED LEVELS:");
        if(GameConstants.DEBUG)System.out.println("==================");
        for(Level l : levelList){
             model.addLevelLast(l,false);
             if(unlockedLevelIdList.contains(l.getID())){
                 if(GameConstants.DEBUG)System.out.println(l.getName());
             }
        }
        if(GameConstants.DEBUG)System.out.println("==================");

        Map<Integer,List<String>> bestCodeLinesMap = JSONParser.getBestCodeForLevels(levelList);
        Map<Integer,Integer> bestTurnsMap = JSONParser.getBestTurnsForLevels(levelList);
        Map<Integer,Integer> bestLOCMap = JSONParser.getBestLocForLevels(levelList);
        List<String> unlockedStatementList = JSONParser.getUnlockedStatementList();

        model.init(bestCodeLinesMap,bestTurnsMap,bestLOCMap, JSONParser.getTutorialProgressIndex(),unlockedLevelIdList,unlockedStatementList);

        if(GameConstants.DEBUG)  Tester.runTests();

        View view = View.getInstance(primaryStage);

        model.initLevelChangeListener(view);
        model.selectLevel(0);

        Controller controller = Controller.getInstance(view,model);

        primaryStage.setOnCloseRequest(we -> {
            if(model.currentLevelHasChanged())controller.getEditorController().showSavingDialog();
            JSONParser.storeAllData();
            try{
                CodeParser.parseProgramCode(view.getCodeArea().getAllCode());
                JSONParser.storeCode(Util.trimStringList(view.getCodeArea().getAllCode()));
            }
            catch (Exception e){
                System.out.println("Saving failed because the player code was corrupted!");
            }


        });
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
