package view;

import javafx.scene.control.ScrollBar;

public class CodeScrollBar extends ScrollBar {

    int scrollAmount = 0;
    static CodeScrollBar codeScrollBar1;
    static CodeScrollBar codeScrollBar2;

    private CodeScrollBar(){
        super();
    }

    public int getScrollAmount() {
        return scrollAmount;
    }

    public void setScrollAmount(int scrollAmount) {
        this.scrollAmount = scrollAmount;
        setValue(scrollAmount);
    }
    public static CodeScrollBar getInstance(boolean isAi){

        if(!isAi){
            if(codeScrollBar1 == null){
                codeScrollBar1 = new CodeScrollBar();
            }
            return codeScrollBar1;
        }
        else{
        if(codeScrollBar2 == null){
            codeScrollBar2 = new CodeScrollBar();
        }
        return codeScrollBar2;}
    }
}
