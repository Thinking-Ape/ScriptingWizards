package main.main;

import main.controller.Controller;
import javafx.application.Application;
import javafx.stage.Stage;
import main.model.Level;
import main.model.Model;
import main.parser.JSONParser;
import main.utility.Util;
import main.view.View;

import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Model model = Model.getInstance();
        String[] levelNameList = JSONParser.getUnlockedLevelNames();
        List<Level> levelList = JSONParser.parseAllResourceLevels();
        levelList.sort((l1, l2) -> l1.getIndex() > l2.getIndex() ? 1 : -1);
        for(Level l : levelList){
//         if(GameConstants.arrayContains(levelNameList,l.getDisplayName())){
//             System.out.println("ok");
             model.addLevel(l);
//         }
        }
        model.selectLevel(levelNameList[levelNameList.length-1]);

        Tester.runTests(model);
        View view = View.getInstance(model, primaryStage);
//        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//            public void handle(WindowEvent we) {
//                try {
//                    JSONParser.saveIndexAndRequiredLevels(model.getLevelListCopy());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
        Controller.instantiate(view,model);

        /*Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setSceneState(new Scene(root, 300, 275));*/

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
