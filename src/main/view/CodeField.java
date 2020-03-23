package main.view;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import main.utility.GameConstants;

public class CodeField extends TextField {

    private int depth;
    private boolean isEmpty;
    private boolean hasListener = false;
//    Text text;

    public CodeField(String code, int depth, boolean isEditable){
        this.setEditable(isEditable);
        this.depth = depth;
        isEmpty = code.equals("");
        if(code.equals("}"))this.setEditable(false);
        setText(code);
        this.setFont(GameConstants.CODE_FONT);
        this.getTransforms().add(new Translate((depth-1)*GameConstants.CODE_OFFSET,0,0));
//        textField.setBackground(new Background(new BackgroundFill(Color.WHITE.deriveColor(1,1,1,0.3),new CornerRadii(5,false),null)));
        resetStyle();
//        textField.setOnMouseClicked(event ->{
//            textField.setStyle(null);
//        });
//        textField.setBackground(null);
        this.setMaxHeight(GameConstants.TEXTFIELD_HEIGHT);
//        textField.setPrefWidth(GameConstants.TEXTFIELD_WIDTH);
        this.setMaxWidth(GameConstants.TEXTFIELD_WIDTH-GameConstants.CODE_OFFSET*depth);
        this.setMinHeight(GameConstants.TEXTFIELD_HEIGHT);
//        textField.setPrefWidth(GameConstants.TEXTFIELD_WIDTH);
        this.setMinWidth(GameConstants.TEXTFIELD_WIDTH-GameConstants.CODE_OFFSET*depth);
//        text.textProperty().addListener((observable, oldValue, newValue) -> {
//            //TODO: just build atm
//            System.out.println("Text Changed to  " + newValue + "\n");
//            if(newValue.equals("bleep")){
//                System.out.println(observable.getValue());
//                text.setText(oldValue);
//            }
//        });
        this.autosize();
    }

    public void addListener(ChangeListener<String> changeListener){
        if(!hasListener)textProperty().addListener(changeListener) ;
        hasListener = true;
    }

    public int getDepth() {
        return depth;
    }

    public void resetStyle() {
            this.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
    }


    public void setEmptyFlag(boolean b) {
        isEmpty = b;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public CodeField clone(){
        return new CodeField(this.getText(),depth,isEditable());
    }

//    public String getText() {
//        for
//    }
}
