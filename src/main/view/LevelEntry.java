package main.view;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import main.utility.GameConstants;


public class LevelEntry extends HBox {

    Label textField;
    Label bestScoresLbl;
    ImageView imageView;
    Label levelName;

    public LevelEntry(Image image, String name, String tooltipString,String bestScores) {

        imageView = new ImageView(image);
        imageView.autosize();
        imageView.setFitHeight(GameConstants.LEVEL_ENTRY_SIZE);
        imageView.setFitWidth(GameConstants.LEVEL_ENTRY_SIZE);
        levelName = new Label(name);
        textField = new Label(tooltipString);
//        textField.setEditable(false);
        textField.setWrapText(true);
        bestScoresLbl = new Label(bestScores);
        this.getChildren().addAll(imageView,levelName,textField,bestScoresLbl);
        this.setSpacing(GameConstants.LEVEL_ENTRY_SIZE/2);
        this.autosize();
//        Tooltip.install(this, tooltip);
    }

    public String getLevelName() {
        return levelName.getText();
    }
}
