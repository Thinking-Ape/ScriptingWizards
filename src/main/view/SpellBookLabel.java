package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import main.model.GameConstants;

import static main.model.GameConstants.TOOLTIP_DELAY;

public class SpellBookLabel extends Label {

    SpellBookLabelType sBLType;

    public SpellBookLabel(SpellBookLabelType sBLType, String entryText, String tooltipString){
        this.setAlignment(Pos.CENTER);
        this.setText(entryText);
        this.setFont(GameConstants.MEDIUM_FONT);
        this.sBLType = sBLType;
        Tooltip tooltip = new Tooltip(tooltipString);
        tooltip.setShowDelay(TOOLTIP_DELAY);
        if(!tooltipString.equals(""))this.setTooltip(tooltip);
        switch(sBLType){
            case VARIABLE_TYPE:
                this.setStyle("-fx-text-fill: green");//ladder(background, white 49%, black 50%);");
                break;
            case VARIABLE_VALUE:
                this.setStyle("-fx-text-fill: red");
                break;
            case VARIABLE_NAME:
                this.setStyle("-fx-text-fill: blue");
                break;
            case METHOD_CALL:
                this.setStyle("-fx-text-fill: grey");
                break;
            case PARAMETERS:
                this.setStyle("-fx-text-fill: purple");
                break;
            case STATEMENT:
                this.setStyle("-fx-text-fill: darkorange");
                break;
            case CONDITION:
                this.setStyle("-fx-text-fill: teal");
                break;
            case DEFAULT:
                break;
            case HEADING:
                this.setStyle("-fx-background-color: white");
                break;
        }
    }
}
