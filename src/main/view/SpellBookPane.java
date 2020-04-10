package main.view;

import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import main.model.statement.StatementType;
import main.model.enums.*;
import main.utility.GameConstants;
import main.model.enums.VariableType;

import java.util.List;

import static main.utility.GameConstants.*;

public class SpellBookPane extends VBox {

    private ListView<Pane> spellListView = new ListView<>();


    private Button closeBtn = new Button("x");
    private Button moveBtn = new Button("<+>");
    private Button showShortcutsBtn = new Button("Show Shortcuts");


    SpellBookPane(){
        this.setMaxSize(SPELLBOOK_WIDTH, GameConstants.SPELLBOOK_HEIGHT);
        spellListView.setPrefSize(SPELLBOOK_WIDTH, GameConstants.SPELLBOOK_HEIGHT);
        spellListView.setMaxSize(SPELLBOOK_WIDTH, GameConstants.SPELLBOOK_HEIGHT);
        // does actually belong here, as it is only concerned with visual effect and not with any functionality
        // maybe add actual functionality later -> move to controller!
        spellListView.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
        showShortcutsBtn.setFont(MEDIUM_FONT);
        moveBtn.setFont(MEDIUM_FONT);
        spellListView.autosize();
        HBox hBox = new HBox(new SpellBookLabel(SpellBookLabelType.HEADING,"Spell Book","Contains all spells you've unlocked!"), showShortcutsBtn,moveBtn,closeBtn);

        hBox.setSpacing(SPELLBOOK_WIDTH/6);
        hBox.setAlignment(Pos.TOP_RIGHT);
        this.getChildren().addAll(hBox,spellListView);
        this.setAlignment(Pos.CENTER);
        //makes the layer below this one also receive clicks!
        this.setPickOnBounds(false);
        this.setBackground(new Background(new BackgroundImage(new Image( GameConstants.BG_DARK_TILE_PATH ), BackgroundRepeat.REPEAT,null,BackgroundPosition.DEFAULT,BackgroundSize.DEFAULT )));
        this.setBorder(new Border(new BorderImage(new Image(GameConstants.BG_DARK_TILE_PATH),new BorderWidths(10),null,new BorderWidths(10),false,BorderRepeat.REPEAT,null)));

    }

    public void updateSpellbookEntries(List<String> unlockedSpells){
        spellListView.getItems().clear();
        String tabString = "\t";

        HBox knightHBox2 = new HBox();
        knightHBox2.setAlignment(Pos.TOP_LEFT);
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_TYPE, tabString + "Knight", "A variable of this type will spawn a new Knight!"));
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," ",""));
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME, "<Knight name>", "The name to reference the knight and to call its methods."));
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," = ",""));
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_VALUE, "new Knight", "Calling the constructor of the class Knight."));
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,"(",""));
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.PARAMETERS, "<Direction>", "Specify where the Knight is looking on spawn. Options are: NORTH, SOUTH, WEST or EAST. No argument means NORTH!"));
        knightHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,");",""));

        if(unlockedSpells.contains(VariableType.KNIGHT.getName())) {
            spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT, "Spawn a Knight:", "Declare a new Knight that you can move around the dungeon.")));
            spellListView.getItems().addAll(knightHBox2);
        }

        HBox armyHBox = new HBox();
        armyHBox.setAlignment(Pos.TOP_LEFT);
        armyHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_TYPE, tabString + "Army", "An army enables you to control multiple Knights at once!\nArmies can call Knight Methods: e.g. army.move();"));
        armyHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," ",""));
        armyHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME, "<Army name>", "The name to reference this army."));
        armyHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," = ",""));
        armyHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_VALUE, "new Army", "Calling the constructor of the class Army."));
        armyHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,"(",""));
        armyHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.PARAMETERS, "<One or more Knights separated by a ','>", "Add the names of all Knights you want in this Army and separate\nthem via a ',' e.g.: Army squad = new Army(knight1,lancelot);"));
        armyHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,");",""));
        if(unlockedSpells.contains(VariableType.ARMY.getName())) {
            spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT, "Declare an Army:", "An army enables you to control multiple Knights at once!\nArmies can call Knight Methods: e.g. army.move();")));
            spellListView.getItems().addAll(armyHBox);
        }


        HBox declarationHBox = new HBox();
        declarationHBox.setAlignment(Pos.TOP_LEFT);
        declarationHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_TYPE,tabString+"<variable type>",TOOLTIP_VARIABLE_TYPE));
        declarationHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," ",""));
        declarationHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME,"<variable name>",TOOLTIP_VARIABLE_NAME));
        declarationHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,";",""));
        HBox declarationHBox2 = new HBox();
        declarationHBox2.setAlignment(Pos.TOP_LEFT);
        declarationHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_TYPE,tabString+"<variable type>",TOOLTIP_VARIABLE_TYPE));
        declarationHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," ",""));
        declarationHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME,"<variable name>",TOOLTIP_VARIABLE_NAME));
        declarationHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," = ",""));
        declarationHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_VALUE,"<variable value>",TOOLTIP_VARIABLE_VALUE));
        declarationHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,";",""));


        HBox assignmentHBox = new HBox();
        assignmentHBox.setAlignment(Pos.TOP_LEFT);
        assignmentHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME,tabString+"<variable name>",TOOLTIP_VARIABLE_NAME));
        assignmentHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," = ",""));
        assignmentHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_VALUE,"<variable value>",TOOLTIP_VARIABLE_VALUE));
        assignmentHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,";",""));
        if(unlockedSpells.contains(VariableType.INT.getName())||unlockedSpells.contains(VariableType.BOOLEAN.getName())||
                unlockedSpells.contains(VariableType.DIRECTION.getName())||unlockedSpells.contains(VariableType.TURN_DIRECTION.getName())||unlockedSpells.contains(VariableType.CELL_CONTENT.getName())||
                unlockedSpells.contains(VariableType.ENTITY_TYPE.getName())||unlockedSpells.contains(VariableType.ITEM_TYPE.getName())) {
            spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Declaration:", "Declare a new variable. You may also assign a value to this variable.")));
            spellListView.getItems().add(new VBox(declarationHBox,declarationHBox2));

            spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Assignment:", "Assign a new value to an already declared variable.")));
            spellListView.getItems().add(assignmentHBox);
        }


        HBox ifHBox = new HBox();
        ifHBox.setAlignment(Pos.TOP_LEFT);
        ifHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT, tabString + "if", "This marks the beginning of a conditional statement."));
        ifHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," (",""));
        ifHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.CONDITION, "<condition>", TOOLTIP_BOOLEAN));
        ifHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,") {",""));
        SpellBookLabel spellBookLabel = new SpellBookLabel(SpellBookLabelType.DEFAULT, tabString + "\t<consequent>", "The code to execute if the condition is true");
        SpellBookLabel spellBookLabel2 = new SpellBookLabel(SpellBookLabelType.DEFAULT, tabString + " }", "This will be added automatically!");

        HBox elseHBox = new HBox();
        HBox elseHBox2 = new HBox();
        elseHBox.setAlignment(Pos.TOP_LEFT);
        elseHBox2.setAlignment(Pos.TOP_LEFT);
        elseHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT, tabString + "else if", "Optional: This marks the beginning of the alternative part of a prior conditional statement with another condition."));
        elseHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," (",""));
        elseHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.CONDITION, "<condition>", TOOLTIP_BOOLEAN));
        elseHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,") {",""));
        SpellBookLabel spellBookLabel3 = new SpellBookLabel(SpellBookLabelType.DEFAULT, tabString + "\t<alternative consequent>", "The code to execute if the previous conditions were false but this condition is true");
        SpellBookLabel spellBookLabel4 = new SpellBookLabel(SpellBookLabelType.DEFAULT, tabString + " }", "This will be added automatically!");

        elseHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT, tabString + "else", "Optional: This marks the beginning of the alternative part of a prior conditional statement."));
        elseHBox2.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," {",""));
        SpellBookLabel spellBookLabel5 = new SpellBookLabel(SpellBookLabelType.DEFAULT, tabString + "\t<alternative>", "The code to execute if all previous conditions were false");
        SpellBookLabel spellBookLabel6 = new SpellBookLabel(SpellBookLabelType.DEFAULT, tabString + " }", "This will be added automatically!");


        if(unlockedSpells.contains(StatementType.IF.name().toLowerCase())||unlockedSpells.contains(StatementType.ELSE.name().toLowerCase()))spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Conditional Statements:", "Describes syntax of possible conditional statements.")));
        if(unlockedSpells.contains(StatementType.IF.name().toLowerCase()))
            spellListView.getItems().add(new VBox(ifHBox,spellBookLabel,spellBookLabel2));
        if(unlockedSpells.contains(StatementType.ELSE.name().toLowerCase()))    spellListView.getItems().add(new VBox(elseHBox,spellBookLabel3,spellBookLabel4,elseHBox2,spellBookLabel5,spellBookLabel6));


        HBox whileHBox = new HBox();
        whileHBox.setAlignment(Pos.TOP_LEFT);
        whileHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT, tabString + "while", "This marks the beginning of the while statement. As long as the condition as true the consequent will be executed!"));
        whileHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," (",""));
        whileHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.CONDITION, "<condition>", TOOLTIP_BOOLEAN));
        whileHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,") {",""));
        SpellBookLabel spellBookLabel7 = new SpellBookLabel(SpellBookLabelType.DEFAULT, tabString + "\t<consequent>", "The code to execute as long as the condition is true");
        SpellBookLabel spellBookLabel8 = new SpellBookLabel(SpellBookLabelType.DEFAULT, tabString + " }", "This will be added automatically!");

        HBox forHBox = new HBox();
        forHBox.setAlignment(Pos.TOP_LEFT);
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT, tabString + "for", "This marks the beginning of the for statement. As long as the condition part is true the consequent will be executed!"));
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT," (",""));
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT, "<int Declaration>", "Declare a variable of type int. See \"Declaration\" for more information!"));
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,"; ",""));
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.CONDITION, "<condition>", TOOLTIP_BOOLEAN));
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,"; ",""));
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.STATEMENT, "<int Assignment>", "Assign a new value to the beforehand declared variable after each execution of the consequent.\nSee \"Assignment\" for more information, but note that the final \';\' is not needed here!"));
        forHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,") {",""));
        SpellBookLabel spellBookLabel9 = new SpellBookLabel(SpellBookLabelType.DEFAULT, tabString + "\t<consequent>", "The code to execute if the condition is true");
        SpellBookLabel spellBookLabel10 = new SpellBookLabel(SpellBookLabelType.DEFAULT, tabString + " }", "This will be added automatically!");


        if(unlockedSpells.contains(StatementType.WHILE.name().toLowerCase())||unlockedSpells.contains(StatementType.FOR.name().toLowerCase()))
            spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Loop Statements:", "Describes possible loop statements.")));
        if(unlockedSpells.contains(StatementType.WHILE.name().toLowerCase()))
            spellListView.getItems().add(new VBox(whileHBox,spellBookLabel7,spellBookLabel8));
        if(unlockedSpells.contains(StatementType.FOR.name().toLowerCase()))
        spellListView.getItems().add(new VBox(forHBox,spellBookLabel9,spellBookLabel10));




        VBox methodVBox = new VBox();
        boolean atLeastOne = false;
        for(MethodType methodType : MethodType.values()){
            if(methodType == MethodType.ATTACK)continue;
            HBox methodHBox = new HBox();
            methodHBox.setAlignment(Pos.TOP_LEFT);
            methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.VARIABLE_NAME,tabString+"<Knight name>","The name of a Knight Object!"));
            methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,".",""));
            methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.METHOD_CALL,methodType.getName(),methodType.getTooltip()));
            methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,"(",""));
            switch (methodType){
                case MOVE:
                case BACK_OFF:
                case USE_ITEM:
                case COLLECT:
                case CAN_MOVE:
                case IS_ALIVE:
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
                case TARGETS_CELL:
                    tooltipString = new StringBuilder("One of the following: ");
                    i = 0;
                    for(CellContent content : CellContent.values()){
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
                    tooltipString.append(". If left blank any EntityType will return true!");
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
                    tooltipString.append(". If left blank any ItemType will return true!");
                    methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.PARAMETERS,"<ItemType>", tooltipString.toString()));
                    break;
                case IS_LOOKING:
                    tooltipString = new StringBuilder("One of the following: ");
                    i = 0;
                    for(Direction d : Direction.values()){
                        if(i>0)tooltipString.append(", ");
                        i++;
                        tooltipString.append(d.name().toUpperCase());
                    }
                    methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.PARAMETERS,"<Direction>", tooltipString.toString()));
                    break;

            }
            methodHBox.getChildren().add(new SpellBookLabel(SpellBookLabelType.DEFAULT,");",""));

            if(unlockedSpells.contains(methodType.getName())){
                methodVBox.getChildren().add(methodHBox);
                atLeastOne = true;
            }
        }
        if(atLeastOne) spellListView.getItems().add(new HBox(new SpellBookLabel(SpellBookLabelType.DEFAULT,"Knight Methods:", "Possible methods for a Knight Object.")));
        spellListView.getItems().add(methodVBox);

    }

    public Button getCloseBtn() {
        return closeBtn;
    }
    public Button getMoveBtn() {
        return moveBtn;
    }
    public Button getShowShortcutsBtn() {
        return showShortcutsBtn;
    }

}
