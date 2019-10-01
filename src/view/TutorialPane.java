package view;

import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.Level;
import model.enums.CContent;
import model.enums.EntityType;
import model.enums.ItemType;
import model.enums.MethodType;
import util.GameConstants;

import java.util.ArrayList;
import java.util.List;

public class TutorialPane extends StackPane {

    //    private List<SpellBookLabel> spellBookEntryList;
    private TextArea currentTutorialMessage = new TextArea();
    private Button prevBtn = new Button("<");
    private Button nextBtn = new Button(">");
    private HBox navigationHBox = new HBox(prevBtn,nextBtn);

    private int index = 0;
    private List<String> tutorialEntries = new ArrayList<>();

    //TODO: ALIGNMENT NOT WORKING PROP
    public TutorialPane(){
        currentTutorialMessage.setEditable(false);
        currentTutorialMessage.setWrapText(true);
//        String tabString = "\t";
        Rectangle wizard = new Rectangle(50,50,Color.BLUE);
        HBox hb = new HBox(new VBox(currentTutorialMessage,navigationHBox),wizard);
        hb.setAlignment(Pos.BOTTOM_RIGHT);
        hb.setTranslateY(GameConstants.SCREEN_HEIGHT-400);
        this.getChildren().add(hb);

//        this.setMouseTransparent(true);
        this.setAlignment(Pos.BOTTOM_RIGHT);
        //makes the layer below this one also receive clicks!
        this.setPickOnBounds(false);
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
}
