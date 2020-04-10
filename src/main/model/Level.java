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
    private int amountOfReruns;
    private final int id;


    public Level(String name, Cell[][] originalArray, ComplexStatement aiBehaviour, Integer[] turnsToStars, Integer[] locToStars, List<Integer> requiredLevelIds, int maxKnights,
                 boolean isTutorial, List<String> tutorialEntryList, int id, int amountOfReruns) {
        this.amountOfReruns = amountOfReruns;
        this.id=id;
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

    public void setAiBehaviour(ComplexStatement aiBehaviour) {
        this.aiBehaviour = aiBehaviour;
    }

    public void setName(String name) {
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

    public void setLocToStars(Integer[] locToStars) {
        this.locToStars = locToStars;

    }

    public void setTurnsToStars(Integer[] turnsToStars) {
        this.turnsToStars = turnsToStars;
    }

    public void setRequiredLevelIds(List<Integer> requiredLevelNames) {
        this.requiredLevelIds = requiredLevelNames;
    }

    public int getMaxKnights() {
        return maxKnights;
    }

    public void setMaxKnights(int maxKnights) {
        this.maxKnights = maxKnights;
    }

    public boolean isTutorial() {
        return isTutorial;
    }

    public void setIsTutorial(boolean isTut) {
        isTutorial= isTut;
    }


    public Integer getId(){
        return id;
    }

    public List<String> getTutorialEntryListCopy() {
        return new ArrayList<>(tutorialMessages);
    }


    public void setGameMap(GameMap value) {
        originalMap = value;
    }

    public void setTutorialMessages(List<String> tutorialMessages) {
        this.tutorialMessages = tutorialMessages;
    }

    public void removeRequiredLevelId(Integer id) {
        requiredLevelIds.remove(id);
    }

    public int getAmountOfReruns() {
        return amountOfReruns;
    }

    public void setAmountOfReruns(int value) {
        amountOfReruns = value;
    }
}
