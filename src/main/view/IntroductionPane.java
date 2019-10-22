package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

// Experimenting with singletons
public class IntroductionPane extends VBox {

    private static IntroductionPane single_Instance = null;

    private TutorialGroup tutorialGroup;
    private Button startTutorialBtn;
    private Button backBtn = new Button("Back");
    private List<String> tutorialMessages = new ArrayList<>();

    private IntroductionPane(){
        fillTutorialMessages();
        tutorialGroup = new TutorialGroup();
        tutorialGroup.setEntries(tutorialMessages);
        startTutorialBtn = new Button("Start Tutorial");
        backBtn.setAlignment(Pos.BASELINE_LEFT);
        startTutorialBtn.setDisable(true);
        this.getChildren().addAll(new Label("Introduction:"),tutorialGroup,startTutorialBtn,backBtn);
        this.setAlignment(Pos.CENTER);
        this.setSpacing(25);
    }

    private void fillTutorialMessages() {
        tutorialMessages.add("Hello! I am so glad that I am not the only one trapped down here in this dungeon! My name is Javiutrix and I am a Wizard knowledgeable in the magic language of Java.");
        tutorialMessages.add("I don't know how you got here, but I was thrown down here by my rival Malegestis. To escape from here we will need to summon magic Knights to do the dirty work for us! The dungeon is filled with traps and we don't want to put our own lives at stake, do we?");
        tutorialMessages.add("I will teach you how to summon our Knights, move them through this dungeon, declare variables and also about some Control Structures." +
                "The latter will help us lazy wizards reduce the amount of spells we need to cast in order to achieve a certain goal or keep the code dynamic and suitable for more than just a single situation!");
    }

    public static IntroductionPane getInstance(){
        if(single_Instance == null)single_Instance = new IntroductionPane();
        return single_Instance;
    }

    public Button getStartTutorialBtn() {
        return startTutorialBtn;
    }

    public TutorialGroup getTutorialGroup() {
        return tutorialGroup;
    }

    public Button getBackBtn() {
        return backBtn;
    }
}
