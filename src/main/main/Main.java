package main.main;

import main.controller.Controller;
import javafx.application.Application;
import javafx.stage.Stage;
import main.model.Level;
import main.model.Model;
import main.parser.CodeParser;
import main.parser.JSONParser;
import main.utility.GameConstants;
import main.utility.Util;
import main.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        JSONParser.init();
        List<String> unlockedLevelNameList = JSONParser.getUnlockedLevelNames();
        List<Level> levelList = JSONParser.parseAllResourceLevels();
//        levelList.sort((l1, l2) -> l1.getIndex() > l2.getIndex() ? 1 : -1);
        List<Level> unlockedLevelList = new ArrayList<>();
        for(Level l : levelList){
             Model.addLevel(l,false);
            if(GameConstants.DEBUG)System.out.println("UNLOCKED LEVELS:");
            if(GameConstants.DEBUG)System.out.println("==================");
             if(unlockedLevelNameList.contains(l.getName())){
                 unlockedLevelList.add(l);
                 if(GameConstants.DEBUG)System.out.println(l.getName());
             }
            if(GameConstants.DEBUG)System.out.println("==================");
//         }
        }


        //TODO: select different Level? //levelNameList[levelNameList.length-1]
        Map<Level,List<String>> bestCodeLinesMap = JSONParser.getBestCodeForLevels(levelList);
        Map<Level,Integer> bestLOCMap = JSONParser.getBestLOCForLevels(levelList);
        Map<Level,Integer> bestTurnsMap = JSONParser.getBestTurnsForLevels(levelList);

        List<String> unlockedStatementList = JSONParser.getUnlockedStatementList();
        Model.init(bestCodeLinesMap,bestTurnsMap,bestLOCMap, JSONParser.getTutorialProgressIndex(),unlockedLevelList,unlockedStatementList);

    if(GameConstants.DEBUG)
        Tester.runTests();
        View view = View.getInstance(primaryStage);
        Model.initLevelChangeListener(view);
        Model.selectLevel(0);
        Controller controller = Controller.instantiate(view);
        primaryStage.setOnCloseRequest(we -> {
            if(Model.currentLevelHasChanged())controller.getEditorController().showSavingDialog();
            JSONParser.storeAllData();
            try{
                CodeParser.parseProgramCode(view.getCodeArea().getAllText());
                JSONParser.storeCode(Util.trimStringList(view.getCodeArea().getAllText()));
            }catch (Exception e){
            }


        });
        /*Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setSceneState(new Scene(root, 300, 275));*/

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
