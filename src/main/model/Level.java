package main.model;

import main.model.gamemap.Cell;
import main.model.gamemap.GameMap;
import main.model.statement.ComplexStatement;
import main.view.CodeAreaType;

import java.util.*;


public class Level {

    private List<String> tutorialMessages;
    private GameMap originalMap;
    private List<Integer> requiredLevelIds;
    private Integer[] locToStars;
    private Integer[] turnsToStars;
    private String name;
    private int maxKnights;
    private boolean isTutorial;
    private ComplexStatement aiBehaviour;
    private int amountOfPlays;
    private final int ID;

    public Level(String name, Cell[][] originalArray, ComplexStatement aiBehaviour, Integer[] turnsToStars, Integer[] locToStars, List<Integer> requiredLevelIds, int maxKnights,
                 boolean isTutorial, List<String> tutorialEntryList, int id, int amountOfReruns) {
        this.amountOfPlays = amountOfReruns;
        this.ID =id;
        this.name =name;
        this.maxKnights = maxKnights;
        this.isTutorial = isTutorial;
        this.turnsToStars = turnsToStars;
        this.locToStars = locToStars;
        this.originalMap = new GameMap(originalArray);
        this.aiBehaviour = aiBehaviour;
        this.requiredLevelIds = new ArrayList<>(requiredLevelIds);
        this.tutorialMessages = new ArrayList<>();
        if(isTutorial){
            if(tutorialEntryList.size() > 0){
                tutorialMessages.addAll(tutorialEntryList);
            }
            else tutorialMessages.add("");
        }
        else tutorialMessages.add("");
    }

    public ComplexStatement getAIBehaviourCopy() {
        return aiBehaviour.copy(CodeAreaType.AI);
    }

    void setAiBehaviour(ComplexStatement aiBehaviour) {
        this.aiBehaviour = aiBehaviour;
    }

    void setName(String name) {
        this.name = name;
    }

    public boolean hasAi() {
        return aiBehaviour.getStatementListSize() > 0;
    }

    public GameMap getOriginalMapCopy(){
        return originalMap.copy();
    }

    public String getName() {
        return name;
    }

    public List<Integer> getRequiredLevelIdsCopy() {
        return new ArrayList<>(requiredLevelIds);
    }

    public Integer[] getLocToStarsCopy() {
        return new Integer[]{locToStars[0], locToStars[1]};
    }
    public Integer[] getTurnsToStarsCopy() {
        return new Integer[]{turnsToStars[0], turnsToStars[1]};
    }

    void setLocToStars(Integer[] locToStars) {
        this.locToStars = locToStars;

    }

    void setTurnsToStars(Integer[] turnsToStars) {
        this.turnsToStars = turnsToStars;
    }

    void setRequiredLevelIds(List<Integer> requiredLevelNames) {
        this.requiredLevelIds = requiredLevelNames;
    }

    public int getMaxKnights() {
        return maxKnights;
    }

    void setMaxKnights(int maxKnights) {
        this.maxKnights = maxKnights;
    }

    boolean isTutorial() {
        return isTutorial;
    }

    void setIsTutorial(boolean isTut) {
        isTutorial= isTut;
    }

    public Integer getID(){
        return ID;
    }

    public List<String> getTutorialMessagesCopy() {
        return new ArrayList<>(tutorialMessages);
    }

    void setGameMap(GameMap value) {
        originalMap = value;
    }

    void setTutorialMessages(List<String> tutorialMessages) {
        this.tutorialMessages = tutorialMessages;
    }

    public int getAmountOfPlays() {
        return amountOfPlays;
    }

    void setAmountOfPlays(int value) {
        amountOfPlays = value;
    }
}