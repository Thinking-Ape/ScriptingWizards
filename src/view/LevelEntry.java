package view;

import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.transform.Scale;


public class LevelEntry extends HBox {

    Label textField;
    ImageView imageView;
    Label levelName;

    public LevelEntry(Image image, String name, String tooltipString) {

        imageView = new ImageView(image);
        imageView.autosize();
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        levelName = new Label(name);
        textField = new Label(tooltipString);
//        textField.setEditable(false);
        textField.setWrapText(true);
        this.getChildren().addAll(imageView,levelName,textField);
        this.setSpacing(50);
        this.autosize();
//        Tooltip.install(this, tooltip);
    }

    public String getLevelName() {
        return levelName.getText();
    }
}
