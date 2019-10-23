package main.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.util.Duration;
import main.utility.GameConstants;

public class SpellBookLabel extends Label {

    SpellBookLabelType sBLType;

    public SpellBookLabel(SpellBookLabelType sBLType, String entryText, String tooltipString){
        this.setAlignment(Pos.CENTER);
        this.setText(entryText);
        this.setFont(new Font(this.getFont().getName(), GameConstants.FONT_SIZE));
        this.sBLType = sBLType;
        Tooltip tooltip = new Tooltip(tooltipString);
        tooltip.setShowDelay(Duration.millis(50));
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
//                Label variableTypeLabel = new Label("<VariableType>");
//                Tooltip typeTooltip = new Tooltip("One of the following: ");
//                typeTooltip.setWrapText(true);
//                int i = 0;
//                for(VariableType vT : VariableType.values()){
//                    if (i == 0) typeTooltip.setText(typeTooltip.getText()+ vT.getName());
//                    else typeTooltip.setText(typeTooltip.getText()+", " + vT.getName());
//                    i++;
//                }
//                variableTypeLabel.setTooltip(typeTooltip);
//
//                Label variableNameLabel = new Label("<VariableName>");
//                Tooltip nameTooltip = new Tooltip("The name of the variable.");
//                variableNameLabel.setTooltip(nameTooltip);
//
//                Label equalsLabel = new Label(" = ");
//
//                Label valueLabel = new Label("<VariableValue>");
//                Tooltip valueTooltip = new Tooltip("The value of the variable:\nint : a whole number, randInt(<lowestValue>,<highestValue>) or any oth int method call\nboolean : true, false or a boolean method call\nKnight : new Knight(<direction>), where <direction> can be either NORTH, SOUTH, WEST or EAST");
//
//                this.getChildren().addAll(variableTypeLabel,variableNameLabel,equalsLabel);
    }
}
