package main.main;

import main.controller.Controller;
import javafx.application.Application;
import javafx.stage.Stage;
import main.model.*;
import main.parser.JSONParser;
import main.parser.CodeParser;
import main.utility.Util;
import main.view.View;

import java.util.List;
import java.util.Map;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        System.out.println("WARNING: Do NOT close this window! It may lead to save file corruption!");
        JSONParser.init();
//        List<Integer> unlockedLevelIdList = JSONParser.getUnlockedLevelIds();
        List<Level> levelList = JSONParser.parseAllLevels();
        Model model = Model.getInstance();
        ModelInformer.init(model);
        if(GameConstants.DEBUG)System.out.println("UNLOCKED LEVELS:");
        if(GameConstants.DEBUG)System.out.println("==================");

        if(GameConstants.DEBUG)System.out.println("==================");

        Map<Integer,List<String>> bestCodeLinesMap = JSONParser.getBestCodeForLevels(levelList);
        Map<Integer,Integer> bestTurnsMap = JSONParser.getBestTurnsForLevels(levelList);
        Map<Integer,Integer> bestLOCMap = JSONParser.getBestLocForLevels(levelList);
        Map<Integer,Integer> bestKnightsMap = JSONParser.getBestKnightsForLevels(levelList);
        List<String> unlockedStatementList = JSONParser.getUnlockedStatementList();
        Map<String,List<Integer>> idToCourseMap = JSONParser.getIdToCourseMap();
        Map<String, CourseDifficulty> courseNameToDifficultyMap = JSONParser.getCourseNameToDifficultyMap();


        List<Course> courseSet = JSONParser.parseAllCourses();
        boolean isEditorUnlocked = JSONParser.isEditorUnlocked();

//        for(String s : idToCourseMap.keySet()){
//            System.out.println(s);
//            for(int i : idToCourseMap.get(s))System.out.println(i);
//        }
        boolean hasSeenIntroduction = JSONParser.hasSeenIntroduction();
        model.init(bestCodeLinesMap,bestTurnsMap,bestLOCMap, bestKnightsMap, unlockedStatementList,isEditorUnlocked,courseSet,levelList,hasSeenIntroduction);

        Tester.runTests();

        System.out.println("Loading. This might take a few seconds...");

        View view = View.getInstance(primaryStage);

        model.initLevelChangeListener(view);
        model.selectLevel(0);

        Controller controller = Controller.getInstance(view,model);



        primaryStage.setOnCloseRequest(we -> {
            if(model.currentLevelHasChanged())controller.getEditorController().showSavingDialog();
            JSONParser.storeAllData();
            try{
                JSONParser.removeUnwantedLevels();
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
