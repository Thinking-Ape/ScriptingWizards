package main.view;

import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import main.utility.GameConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TutorialGroup extends Group {

    //    private List<SpellBookLabel> spellBookEntryList;
    private TextArea currentTutorialMessage = new TextArea();
    private Button prevBtn = new Button("<");
    private Button nextBtn = new Button(">");
    private HBox navigationHBox = new HBox(prevBtn,nextBtn);

    private int index = 0;
    private List<String> tutorialEntries = new ArrayList<>();

    //TODO: ALIGNMENT NOT WORKING PROP
    public TutorialGroup(){
        currentTutorialMessage.setEditable(false);
        currentTutorialMessage.setWrapText(true);
        currentTutorialMessage.setMouseTransparent(true);
        // ANOTHER BUG WORK AROUND! (Bug was that text got blurry in TextArea)
        currentTutorialMessage.setCache(false);
        currentTutorialMessage.setFont(new Font(currentTutorialMessage.getFont().getName(), GameConstants.FONT_SIZE));
//        String tabString = "\t";
//        Rectangle wizard = new Rectangle(50,50,Color.BLUE);
        ImageView wizard = new ImageView(new javafx.scene.image.Image(GameConstants.WIZARD_IMAGE_PATH));
        VBox vb = new VBox(currentTutorialMessage,navigationHBox);
        vb.setSpacing(10);
        HBox hb = new HBox(vb,wizard);
//        hb.setAlignment(Pos.BOTTOM_RIGHT);
        this.getChildren().addAll(hb);
//        this.setMinSize(GameConstants.SCREEN_WIDTH, GameConstants.SCREEN_HEIGHT);
        this.autosize();
        //makes the layer below this one also receive clicks!
        this.setPickOnBounds(false);
        navigationHBox.setPickOnBounds(false);
        hb.setPickOnBounds(false);
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);
    }

    public TextArea getCurrentTutorialMessage() {
        return currentTutorialMessage;
    }

    public Button getPrevBtn() {
        return prevBtn;
    }

    public Button getNextBtn() {
        return nextBtn;
    }

    public void next(){
        index++;
        currentTutorialMessage.setText(tutorialEntries.get(index));
        if(index == tutorialEntries.size()-1)nextBtn.setDisable(true);
        prevBtn.setDisable(false);
    }
    public void prev(){
        index--;
        currentTutorialMessage.setText(tutorialEntries.get(index));
        if(index == 0)prevBtn.setDisable(true);
        nextBtn.setDisable(false);
    }

    public void setEntries(List<String> tutorialEntryList) {
        index = 0;
        tutorialEntries.addAll(tutorialEntryList);
        if(tutorialEntryList.size() >0 ){
            currentTutorialMessage.setText(tutorialEntries.get(0));
            nextBtn.setDisable(false);
        }
    }

    public boolean isLastMsg() {
        return index == tutorialEntries.size()-1;
    }
}
