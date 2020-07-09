package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import main.model.GameConstants;
import main.utility.Util;


public class LevelEntry extends HBox {

    private ImageView imageView;
    private Label levelName;
    private ImageView bestStars;

    LevelEntry(Image image, String name, String tooltipString,String bestScores,double nStars) {
        imageView = new ImageView(image);
        imageView.setFitHeight(GameConstants.LEVEL_ENTRY_SIZE);
        imageView.setFitWidth(GameConstants.LEVEL_ENTRY_SIZE);
        imageView.autosize();

        levelName = new Label(name);
        levelName.setFont(GameConstants.BIG_FONT);
        levelName.layout();
        levelName.autosize();
        Label textField = new Label(tooltipString);
        textField.setFont(GameConstants.MEDIUM_FONT);
        textField.layout();
        textField.autosize();
        textField.setWrapText(true);
        Label bestScoresLbl = new Label(bestScores);
        bestScoresLbl.setFont(GameConstants.MEDIUM_FONT);
        bestScoresLbl.layout();
        bestScoresLbl.autosize();

        bestStars = new ImageView(Util.getStarImageFromDouble(nStars));
        bestStars.setPreserveRatio(true);
        bestStars.setFitHeight(GameConstants.LEVEL_ENTRY_SIZE/1.5);
        bestStars.autosize();
        this.getChildren().addAll(imageView,levelName, textField, bestScoresLbl,bestStars);
        this.setSpacing(GameConstants.LEVEL_ENTRY_SIZE/2);
        this.autosize();
        this.setAlignment(Pos.CENTER_LEFT);
        this.setMaxWidth(GameConstants.LEVEL_ENTRY_SIZE*3+bestStars.getLayoutBounds().getWidth()+Util.getLabelWidth(bestScoresLbl)+Util.getLabelWidth(textField)+Util.getLabelWidth(levelName)+imageView.getLayoutBounds().getWidth());
    }
    public String getLevelName() {
        return levelName.getText();
    }

    public void updateImage(Image starImage) {
        bestStars.setImage(starImage);
    }

    Image getLevelImage() {
        return imageView.getImage();
    }
}
