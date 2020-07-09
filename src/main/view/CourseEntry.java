package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import main.model.GameConstants;
import main.model.LevelDifficulty;
import main.utility.Util;


public class CourseEntry extends HBox {

    private Label courseName;
    private ImageView minStarsIView;

    CourseEntry(String courseName, int amountOfLevels, LevelDifficulty difficultyRating, double minStars) {
//        imageView = new ImageView(image);
//        imageView.setFitHeight(GameConstants.LEVEL_ENTRY_SIZE);
//        imageView.setFitWidth(GameConstants.LEVEL_ENTRY_SIZE);
//        imageView.autosize();

        this.courseName = new Label(courseName);
        this.courseName.setFont(GameConstants.BIGGEST_FONT);
        this.courseName.layout();
        this.courseName.setStyle(GameConstants.WHITE_SHADOWED_STYLE);
        this.courseName.autosize();
        Label textField = new Label("Amount Of Levels: "+amountOfLevels);
        textField.setFont(GameConstants.MEDIUM_FONT);
        textField.layout();
        textField.autosize();
        textField.setWrapText(true);
        Label difficultyLbl = new Label("Difficulty: "+difficultyRating.name());
        difficultyLbl.setFont(GameConstants.MEDIUM_FONT);
        difficultyLbl.layout();
        difficultyLbl.autosize();

        this.minStarsIView = new ImageView(Util.getStarImageFromDouble(minStars));
        this.minStarsIView.setPreserveRatio(true);
        this.minStarsIView.setFitHeight(GameConstants.LEVEL_ENTRY_SIZE/1.5);
        this.minStarsIView.autosize();
        this.getChildren().addAll(this.courseName, textField, difficultyLbl, minStarsIView);
        this.setSpacing(GameConstants.LEVEL_ENTRY_SIZE/2);
        this.autosize();
        this.setAlignment(Pos.CENTER_LEFT);
        this.setMaxWidth(GameConstants.LEVEL_ENTRY_SIZE*3+ this.minStarsIView.getLayoutBounds().getWidth()+Util.getLabelWidth(difficultyLbl)+Util.getLabelWidth(textField)+Util.getLabelWidth(this.courseName));
    }
    public String getCourseName() {
        return courseName.getText();
    }

    public void updateImage(Image starImage) {
        minStarsIView.setImage(starImage);
    }

//    Image getLevelImage() {
//        return imageView.getImage();
//    }
}
