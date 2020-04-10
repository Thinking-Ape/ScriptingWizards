package main.view;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.transform.Translate;
import main.utility.GameConstants;

public class CodeField extends TextField {

    private int depth;
    // this is needed, because if isEmpty() would return .equals("") the current line would be deleted when there is
    // only 1 char left and backspace is pressed -> unwanted behaviour
    private boolean isEmpty;
    private boolean hasListener = false;
    CodeField(String code, int depth, boolean isEditable){
        this.setEditable(isEditable);
        this.depth = depth;
        isEmpty = code.equals("");
        if(code.equals("}"))this.setEditable(false);
        setText(code);
        this.setFont(GameConstants.CODE_FONT);
        this.getTransforms().add(new Translate((depth-1)*GameConstants.CODE_OFFSET,0,0));
        resetStyle();
        this.setMaxHeight(GameConstants.TEXTFIELD_HEIGHT);
        this.setMaxWidth(GameConstants.TEXTFIELD_WIDTH-GameConstants.CODE_OFFSET*depth);
        this.setMinHeight(GameConstants.TEXTFIELD_HEIGHT);
        this.setMinWidth(GameConstants.TEXTFIELD_WIDTH-GameConstants.CODE_OFFSET*depth);
        this.autosize();
    }

    public void addListener(ChangeListener<String> changeListener){
        if(!hasListener)textProperty().addListener(changeListener) ;
        hasListener = true;
    }

    public int getDepth() {
        return depth;
    }

    void resetStyle() {
            this.setStyle("-fx-background-color: rgba(255,255,255,0.1);");
    }


    public void setEmptyFlag(boolean b) {
        isEmpty = b;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

}
