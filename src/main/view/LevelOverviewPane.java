package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import main.model.LevelDataType;
import main.model.ModelInformer;
import main.model.gamemap.GameMap;
import main.model.GameConstants;
import main.utility.Util;

import static main.model.GameConstants.*;


public class LevelOverviewPane extends VBox {
    private ListView<LevelEntry> levelListView = new ListView<>();
    private Button playBtn = new Button();
    private Button backBtn = new Button();
    private boolean isTutorial;

    public LevelOverviewPane( boolean isTutorial){
        this.isTutorial = isTutorial;
        updateUnlockedLevels();
        backBtn.setPrefSize(BUTTON_SIZE,BUTTON_SIZE*0.75);
        playBtn.setPrefSize(BUTTON_SIZE,BUTTON_SIZE);
        ImageView backBtnIV = new ImageView(GameConstants.BACK_BTN_IMAGE_PATH);
        backBtnIV.setFitHeight(backBtnIV.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        backBtnIV.setFitWidth(backBtnIV.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        ImageView executeBtnIV = new ImageView(GameConstants.EXECUTE_BTN_IMAGE_PATH);
        executeBtnIV.setFitHeight(executeBtnIV.getLayoutBounds().getHeight()*GameConstants.HEIGHT_RATIO);
        executeBtnIV.setFitWidth(executeBtnIV.getLayoutBounds().getWidth()*GameConstants.WIDTH_RATIO);
        playBtn.setGraphic(executeBtnIV);
        backBtn.setGraphic(backBtnIV);
        backBtn.setStyle("-fx-background-color: rgba(0,0,0,0)");
        playBtn.setStyle("-fx-background-color: rgba(0,0,0,0)");
        HBox hBox = new HBox(backBtn,playBtn);
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(BUTTON_SIZE);
        levelListView.setPrefHeight(GameConstants.SCREEN_HEIGHT*0.76);

        Label challengesLbl = new Label(isTutorial ? "Tutorials" :"Challenges");
        challengesLbl.setFont(GameConstants.CHALLENGER_FONT);
        challengesLbl.setStyle("-fx-background-color: lightgrey");
        this.getChildren().addAll(challengesLbl,levelListView,hBox);
        this.setAlignment(Pos.CENTER);
        this.setSpacing(CODEFIELD_HEIGHT);
    }

    public ListView<LevelEntry> getLevelListView(){
        return levelListView;
    }

    public Button getPlayBtn() {
        return playBtn;
    }

    public Button getBackBtn() {
        return backBtn;
    }


    public void updateUnlockedLevels() {
        levelListView.getItems().clear();
        for(int levelId : ModelInformer.getUnlockedLevelIds()){
            int i = ModelInformer.getIndexOfLevelWithId(levelId);
            int loc =ModelInformer.getBestLocOfLevel(levelId);
            int turns =ModelInformer.getBestTurnsOfLevel(levelId);
            String levelName = ModelInformer.getNameOfLevelWithId(levelId);

            Integer[] turnsToStars = (Integer[]) ModelInformer.getDataFromLevelWithIndex(LevelDataType.TURNS_TO_STARS,i);
            Integer[] locToStars = (Integer[]) ModelInformer.getDataFromLevelWithIndex(LevelDataType.LOC_TO_STARS,i);
            double nStars = Util.calculateStars(turns,loc,turnsToStars,locToStars);
            GameMap gameMap = (GameMap) ModelInformer.getDataFromLevelWithIndex(LevelDataType.MAP_DATA,i);
            // View.getIconFromMap(gameMap) is an intensive calculation!
            // Dont call this too often, because View.getIconFromMap(gameMap) is costly!
            LevelEntry le = new LevelEntry(View.getIconFromMap(gameMap),levelName,
                    getLevelTooltip(turnsToStars,locToStars),getBestScoreString(turns,loc,nStars),nStars);
            le.autosize();
            boolean isTut = (boolean)ModelInformer.getDataFromLevelWithIndex(LevelDataType.IS_TUTORIAL,i);
            if((isTutorial && isTut && ModelInformer.getCurrentIndex() <= ModelInformer.getTutorialProgress()+1)||(!isTutorial && (GameConstants.SHOW_TUTORIAL_LEVELS_IN_PLAY||!isTut))){
                levelListView.setFixedCellSize(BUTTON_SIZE*1.25);
                levelListView.getItems().add(le);
            }
            updateWidth(le);
        }
    }
    public void addLevelWithIndex(int i){
        Image image = View.getIconFromMap((GameMap)ModelInformer.getDataFromLevelWithIndex(LevelDataType.MAP_DATA,i));
        Integer[] turnsToStars = (Integer[]) ModelInformer.getDataFromLevelWithIndex(LevelDataType.TURNS_TO_STARS,i);
        Integer[] locToStars = (Integer[]) ModelInformer.getDataFromLevelWithIndex(LevelDataType.LOC_TO_STARS,i);
//        double nStars = Util.calculateStars(bestResults[1],bestResults[0],turnsToStars,locToStars)
        String levelName = (String)ModelInformer.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME,i);
        LevelEntry le = new LevelEntry(image,levelName,
                getLevelTooltip(turnsToStars,locToStars),getBestScoreString(-1,-1,0),0);
        int index = 0;
        for(LevelEntry levelEntry : levelListView.getItems()){
            if(ModelInformer.getIndexOfLevelInList(levelEntry.getLevelName())<i)index ++;
        }
        levelListView.getItems().add(index,le);
        if(levelListView.getItems().size() == 1){
            updateWidth(le);
        }
    }

    private void updateWidth(LevelEntry le) {
        double width = levelListView.getMaxWidth();
        width = le.getMaxWidth()+GameConstants.CODEFIELD_HEIGHT *2 > width ? le.getMaxWidth()+GameConstants.CODEFIELD_HEIGHT *2 : width;
        levelListView.setMaxWidth(width);
    }

    public void updateCurrentLevel() {
        String levelName = (String)ModelInformer.getDataFromLevelWithIndex(LevelDataType.LEVEL_NAME,ModelInformer.getCurrentIndex());
        updateLevel(levelName);
    }

    public boolean containsLevel(String nextLevelName) {
        for(LevelEntry levelEntry : levelListView.getItems()){
            if(levelEntry.getLevelName().equals(nextLevelName)) return true;
        }
        return false;
    }

    public void removeCurrentLevel() {
        LevelEntry levelEntryToRemove = null;
        for(LevelEntry levelEntry : levelListView.getItems()){
            if(levelEntry.getLevelName().equals(ModelInformer.getDataFromCurrentLevel(LevelDataType.LEVEL_NAME).toString()))levelEntryToRemove=levelEntry;
        }
        levelListView.getItems().remove(levelEntryToRemove  );
    }

    public boolean containsCurrentLevel() {
        return containsLevel(ModelInformer.getNameOfLevelWithIndex(ModelInformer.getCurrentIndex()));
    }

    public boolean updateLevel(String levelName) {
        int index = -1;
        int i = 0;
        for(LevelEntry le : levelListView.getItems()){
            if(le.getLevelName().equals(levelName))index = i;
            i++;
        }
        if(index==-1)return false;
        int currentIndex = ModelInformer.getCurrentIndex();
        Integer[] turnsToStars = (Integer[]) ModelInformer.getDataFromLevelWithIndex(LevelDataType.TURNS_TO_STARS,currentIndex);
        Integer[] locToStars = (Integer[]) ModelInformer.getDataFromLevelWithIndex(LevelDataType.LOC_TO_STARS,currentIndex);
        int id = ModelInformer.getIdOfLevelWithName(levelName);
        int loc =ModelInformer.getBestLocOfLevel(id);
        int turns =ModelInformer.getBestTurnsOfLevel(id);
        double nStars = Util.calculateStars(turns,loc,turnsToStars,locToStars);
        LevelEntry le = new LevelEntry(levelListView.getItems().get(index).getLevelImage(),ModelInformer.getNameOfLevelWithIndex(currentIndex),
                getLevelTooltip(turnsToStars,locToStars),getBestScoreString(turns,loc,nStars),nStars);
        levelListView.getItems().set(index,le);
        return true;
    }

    private String getBestScoreString(int turns, int loc, double nStars) {

        String starString =  (int)nStars + (Math.round(nStars)!=(int)nStars ? ".5" : "");
        return "Best Turns: "+turns+"\nBest LOC: "+loc+"\nEarned Stars: "+ starString;
    }

    private String getLevelTooltip(Integer[] turnsToStars, Integer[] locToStars) {
        return "Max Turns for ***: "+turnsToStars[1]+", Max Turns for **: "+turnsToStars[0]+"\nMax LOC for ***: "+locToStars[1]+
                ", Max LOC for **: "+locToStars[0];
    }
}
