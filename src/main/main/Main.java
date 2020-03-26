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

import java.io.IOException;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        String[] levelNameList = JSONParser.getUnlockedLevelNames();
        List<Level> levelList = JSONParser.parseAllResourceLevels();
//        levelList.sort((l1, l2) -> l1.getIndex() > l2.getIndex() ? 1 : -1);
        for(Level l : levelList){
             Model.addLevel(l,false);
//         }
        }
        Model.selectLevel(levelNameList[levelNameList.length-1]);

      //TODO  if(GameConstants.DEBUG)
        Tester.runTests();
        View view = View.getInstance(primaryStage);
        primaryStage.setOnCloseRequest(we -> {
            try {
                CodeParser.parseProgramCode(view.getCodeArea().getAllText());
                JSONParser.storeCode(Util.trimStringList(view.getCodeArea().getAllText()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                if(GameConstants.DEBUG){
                    System.out.println("Code wasnt saved because of error: \n"+e.getMessage());
                }
            }
        });
        Controller.instantiate(view);

        /*Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setSceneState(new Scene(root, 300, 275));*/

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
