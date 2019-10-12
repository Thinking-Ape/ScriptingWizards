package view;

import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.enums.CContent;
import model.enums.EntityType;
import model.enums.ItemType;
import model.enums.MethodType;

public class SpellBookPane extends StackPane {

    //    private List<SpellBookLabel> spellBookEntryList;
    private ListView<Pane> spellListView = new ListView<>();

    public SpellBookPane(){
        spellListView.setPrefSize(500, 750);
        spellListView.setMaxSize(500, 750);
        spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Spawn a Knight:", "Declare a new Knight.")));
//        HBox knightHBox1 = new HBox();
        HBox knightHBox2 = new HBox();
//        knightHBox1.setAlignment(Pos.CENTER);
        knightHBox2.setAlignment(Pos.TOP_LEFT);
//        knightHBox1.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_TYPE,"Knight","A variable of this type will spawn a new Knight!"));
//        knightHBox1.getChildren().add(new Label(" "));
//        knightHBox1.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME,"<knight name>","The name to reference the knight and to call its methods."));
//        knightHBox1.getChildren().add(new Label(";"));
        String tabString = "\t";
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_TYPE,tabString+"Knight","A variable of this type will spawn a new Knight!"));
        knightHBox2.getChildren().add(new Label(" "));
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME,"<Knight name>","The name to reference the knight and to call its methods."));
        knightHBox2.getChildren().add(new Label(" = "));
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_VALUE,"new Knight","Calling the constructor of the class Knight."));
        knightHBox2.getChildren().add(new Label("("));
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.PARAMETERS,"<Direction>","Specify where the Knight is looking on spawn. Options are: NORTH, SOUTH, WEST or EAST. No argument means NORTH!"));
        knightHBox2.getChildren().add(new Label(");"));
        spellListView.getItems().addAll(knightHBox2);//new VBox(knightHBox1,knightHBox2));

        spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Declaration:", "Declare a new variable. You may also assign a value to this variable.")));
        HBox declarationHBox = new HBox();
        declarationHBox.setAlignment(Pos.TOP_LEFT);
        declarationHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_TYPE,tabString+"<variable type>","Options are: int, boolean (and Knight)"));
        declarationHBox.getChildren().add(new Label(" "));
        declarationHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME,"<variable name>","The name to reference this variable."));
        declarationHBox.getChildren().add(new Label(";"));
        HBox declarationHBox2 = new HBox();
        declarationHBox2.setAlignment(Pos.TOP_LEFT);
        declarationHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_TYPE,tabString+"<variable type>","Options are: int, boolean (and Knight)"));
        declarationHBox2.getChildren().add(new Label(" "));
        declarationHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME,"<variable name>","The name to reference this variable."));
        declarationHBox2.getChildren().add(new Label(" = "));
        declarationHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_VALUE,"<variable value>","The value of the variable:\nint : a whole number, randInt(<lowestValue>,<highestValue>) or any other int variable or int method call as well as a term thereof\nboolean : true, false or a boolean method call"));
        declarationHBox2.getChildren().add(new Label(";"));
        spellListView.getItems().add(new VBox(declarationHBox,declarationHBox2));

        spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Assignment:", "Assign a new value to an already declared variable.")));
        HBox assignmentHBox = new HBox();
        assignmentHBox.setAlignment(Pos.TOP_LEFT);
        assignmentHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME,tabString+"<variable name>","The name to reference this variable."));
        assignmentHBox.getChildren().add(new Label(" = "));
        assignmentHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_VALUE,"<variable value>","The new value of the variable:\nint : a whole number, randInt(<lowestValue>,<highestValue>) or any other int variable or int method call as well as a term thereof\nboolean : true, false or a boolean method call"));
        assignmentHBox.getChildren().add(new Label(";"));
        spellListView.getItems().add(assignmentHBox);

        spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Conditional Statements:", "Describes syntax of possible conditional statements.")));
        HBox ifHBox = new HBox();
        ifHBox.setAlignment(Pos.TOP_LEFT);
        ifHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT,tabString+"if","This marks the beginning of a conditional statement."));
        ifHBox.getChildren().add(new Label(" ("));
        String conditionTooltipString = "True, false, a boolean variable, boolean method call or comparisons thereof";
        ifHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.CONDITION,"<condition>",conditionTooltipString));
        ifHBox.getChildren().add(new Label(") {"));
        SpellBookLabel spellBookLabel = new SpellBookLabel(SpellBookLabelType.DEFAULT,tabString+"\t<consequent>","The code to execute if the condition is true");
        SpellBookLabel spellBookLabel2 = new SpellBookLabel(SpellBookLabelType.DEFAULT,tabString+" }","This will be added automatically!");

        HBox elseHBox = new HBox();
        HBox elseHBox2 = new HBox();
        elseHBox.setAlignment(Pos.TOP_LEFT);
        elseHBox2.setAlignment(Pos.TOP_LEFT);
        elseHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT,tabString+"else if","Optional: This marks the beginning of the alternative part of a prior conditional statement with another condition."));
        elseHBox.getChildren().add(new Label(" ("));
        elseHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.CONDITION,"<condition>",conditionTooltipString));
        elseHBox.getChildren().add(new Label(") {"));
        SpellBookLabel spellBookLabel3 = new SpellBookLabel(SpellBookLabelType.DEFAULT,tabString+"\t<alternative consequent>","The code to execute if the previous conditions were false but this condition is true");
        SpellBookLabel spellBookLabel4 = new SpellBookLabel(SpellBookLabelType.DEFAULT,tabString+" }","This will be added automatically!");

        elseHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT,tabString+"else","Optional: This marks the beginning of the alternative part of a prior conditional statement."));
        elseHBox2.getChildren().add(new Label(" {"));
        SpellBookLabel spellBookLabel5 = new SpellBookLabel(SpellBookLabelType.DEFAULT,tabString+"\t<alternative>","The code to execute if all previous conditions were false");
        SpellBookLabel spellBookLabel6 = new SpellBookLabel(SpellBookLabelType.DEFAULT,tabString+" }","This will be added automatically!");
        spellListView.getItems().add(new VBox(ifHBox,spellBookLabel,spellBookLabel2,elseHBox,spellBookLabel3,spellBookLabel4,elseHBox2,spellBookLabel5,spellBookLabel6));


        spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Loop Statements:", "Describes possible loop statements.")));
        HBox whileHBox = new HBox();
        whileHBox.setAlignment(Pos.TOP_LEFT);
        whileHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT,tabString+"while","This marks the beginning of the while statement. As long as the condition as true the consequent will be executed!"));
        whileHBox.getChildren().add(new Label(" ("));
        whileHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.CONDITION,"<condition>",conditionTooltipString));
        whileHBox.getChildren().add(new Label(") {"));
        SpellBookLabel spellBookLabel7 = new SpellBookLabel(SpellBookLabelType.DEFAULT,tabString+"\t<consequent>","The code to execute as long as the condition is true");
        SpellBookLabel spellBookLabel8 = new SpellBookLabel(SpellBookLabelType.DEFAULT,tabString+" }","This will be added automatically!");

        HBox forHBox = new HBox();
        forHBox.setAlignment(Pos.TOP_LEFT);
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT,tabString+"for","This marks the beginning of the for statement. As long as the condition part is true the consequent will be executed!"));
        forHBox.getChildren().add(new Label(" ("));
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT,"<int Declaration>","Declare a variable of type int. See \"Declaration\" for more information!"));
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.CONDITION,"<condition>",conditionTooltipString));
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT,"<int Assignment>","Assign a new value to the beforehand declared variable after each execution of the consequent.\nSee \"Assignment\" for more information, but note that the final \';\' is not needed here!"));
        forHBox.getChildren().add(new Label(") {"));
        SpellBookLabel spellBookLabel9 = new SpellBookLabel(SpellBookLabelType.DEFAULT,tabString+"\t<consequent>","The code to execute if the condition is true");
        SpellBookLabel spellBookLabel10 = new SpellBookLabel(SpellBookLabelType.DEFAULT,tabString+" }","This will be added automatically!");
        spellListView.getItems().add(new VBox(whileHBox,spellBookLabel7,spellBookLabel8,forHBox,spellBookLabel9,spellBookLabel10));

        spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Knight Methods:", "Possible methods for a Knight Object.")));

        VBox methodVBox = new VBox();
        for(MethodType methodType : MethodType.values()){
            HBox methodHBox = new HBox();
            methodHBox.setAlignment(Pos.TOP_LEFT);
            methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME,tabString+"<Knight name>","The name of a Knight Object!"));
            methodHBox.getChildren().add(new Label("."));
            methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.METHOD_CALL,methodType.getName(),methodType.getTooltip()));
            methodHBox.getChildren().add(new Label("("));
            switch (methodType){
                case MOVE:
                case USE_ITEM:
                case COLLECT:
                case CAN_MOVE:
                case WAIT:
                case DROP_ITEM:
                case TARGET_IS_DANGER:
                    break;
                case TURN:
                    methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.PARAMETERS,"<TurnDirection>","One of the following: LEFT, RIGHT or AROUND"));
                    break;
                case HAS_ITEM:
                    StringBuilder tooltipString = new StringBuilder("One of the following: ");
                    int i = 0;
                    for(ItemType iT : ItemType.values()){
                        if(i>0)tooltipString.append(", ");
                        i++;
                        tooltipString.append(iT.name().toUpperCase());
                    }
                    tooltipString.append(". If left blank will return true if any Item is held!");
                    methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.PARAMETERS,"<ItemType>", tooltipString.toString()));
                    break;
                case TARGET_CELL_IS:
                    tooltipString = new StringBuilder("One of the following: ");
                    i = 0;
                    for(CContent content : CContent.values()){
                        if(i>0)tooltipString.append(", ");
                        i++;
                        tooltipString.append(content.name().toUpperCase());
                    }
                    methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.PARAMETERS,"<CellContent>", tooltipString.toString()));
                    break;
                case TARGETS_ENTITY:
                    tooltipString = new StringBuilder("One of the following: ");
                    i = 0;
                    for(EntityType entity : EntityType.values()){
                        if(i>0)tooltipString.append(", ");
                        i++;
                        tooltipString.append(entity.name().toUpperCase());
                    }
                    tooltipString.append(". If left blank any ItemType will return true!");
                    methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.PARAMETERS,"<EntityType>", tooltipString.toString()));
                    break;
                case TARGETS_ITEM:
                    tooltipString = new StringBuilder("One of the following: ");
                    i = 0;
                    for(ItemType iT : ItemType.values()){
                        if(i>0)tooltipString.append(", ");
                        i++;
                        tooltipString.append(iT.name().toUpperCase());
                    }
                    tooltipString.append(". If left blank any EntityType will return true!");
                    methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.PARAMETERS,"<ItemType>", tooltipString.toString()));
                    break;
            }
            methodHBox.getChildren().add(new Label(");"));
            methodVBox.getChildren().add(methodHBox);
        }
        spellListView.getItems().add(methodVBox);
        // does actually belong here, as it is only concerned with visual effect and not with any functionality
        // maybe add actual functionality later -> move to controller!
        spellListView.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);

        spellListView.autosize();
        Rectangle rect = new Rectangle(spellListView.getPrefWidth()+10,spellListView.getPrefHeight()+25,Color.BLACK);//spellListView.getItems().size()*20,Color.WHITE);
//        this.setMouseTransparent(true);
        this.setAlignment(Pos.CENTER);
        VBox vBox = new VBox(new SpellBookLabel(SpellBookLabelType.HEADING,"Spell Book","Contains all spells you've unlocked!"),spellListView);
        vBox.setAlignment(Pos.CENTER);
        //makes the layer below this one also receive clicks!
        vBox.setPickOnBounds(false);
        this.setPickOnBounds(false);
        this.getChildren().addAll(rect,vBox);//rect,vBox);
    }
}
