package view;

import javafx.scene.control.TextField;
import javafx.scene.transform.Translate;
import model.GameConstants;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class CodeBoxLeaf implements CodeBoxComposite {

//    String text;
    private TextField textField;
    private CodeBoxCompound parentCodeBox = null;
    public  CodeBoxLeaf(String text, CodeBoxCompound parentCodeBox,int depth){
        this.parentCodeBox = parentCodeBox;


        textField = new TextField(text);
        textField.getTransforms().add(new Translate(depth*10,0,0));
//        textField.setBackground(new Background(new BackgroundFill(Color.WHITE.deriveColor(1,1,1,0.3),new CornerRadii(5,false),null)));
        textField.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
        textField.setOnMouseClicked(event ->{
            textField.setStyle(null);
        });
//        textField.setBackground(null);
        textField.setMaxHeight(GameConstants.TEXTFIELD_HEIGHT);
//        textField.setPrefWidth(GameConstants.TEXTFIELD_WIDTH);
        textField.setMaxWidth(GameConstants.TEXTFIELD_WIDTH-10*depth);
        textField.setMinHeight(GameConstants.TEXTFIELD_HEIGHT);
//        textField.setPrefWidth(GameConstants.TEXTFIELD_WIDTH);
        textField.setMinWidth(GameConstants.TEXTFIELD_WIDTH-10*depth);
        this.getParentCodeBox().getVBox().getChildren().add(textField);
//
//        this.text = text;
    }

    @Override
    public CodeBoxCompound getParentCodeBox() {
        return parentCodeBox;
    }

    @Override
    public List<String> getAllCode() {
        List<String> output = new ArrayList<>();
        output.add(textField.getText());
        return output;
    }

    @Override
    public String getText() {
        return textField.getText();
    }
}
