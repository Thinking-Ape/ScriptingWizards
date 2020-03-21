package main.utility;

import javafx.scene.effect.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import main.model.gamemap.Entity;
import main.model.enums.EntityType;

//import javax.swing.text.html.ImageView;
import java.awt.*;
import java.nio.file.Paths;
import java.util.Random;

public abstract class GameConstants {
    public static final double SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    public static final double SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    public static final boolean DEBUG = true;
    public static final char ANY_CHAR = '?';
    public static final double TICK_SPEED = 0.65;
    public static final boolean SHOW_BOOLEAN_METHODS = false;
    public static final double TEXTFIELD_HEIGHT = SCREEN_HEIGHT/43;
    public static final double TEXTFIELD_WIDTH = SCREEN_WIDTH/5;
    public static final int CODE_OFFSET = 10;
    public static final boolean IS_AI_ACTIVE = true;
    public static final String RAND_INT_REGEX = "randInt\\((.*),(.*)\\)";
    public static final Random RANDOM = new Random();
    public static final String LEVEL_ROOT_PATH = Paths.get("resources/levels/").toString();
    public static final String IMAGES_PATH = "resources/images/";
    public static final String ROOT_PATH = Paths.get("resources/").toString();
    public static final int MAX_CODE_LINES = (int)(30*SCREEN_HEIGHT/1080.0);
    public static final int MAX_LEVEL_SIZE = 15;
    public static final int MIN_LEVEL_SIZE = 3;
    public static final double MAX_GAMEMAP_SIZE = SCREEN_HEIGHT/1.6;
    public static final int MAX_DEPTH = 4;
    public static final boolean SHOW_TUTORIAL_LEVELS_IN_PLAY = false;
    public static final boolean IS_FULLSCREEN = false;
    public static final String VERSION = "1.3b";
    public static final boolean ACTION_WITHOUT_CONSEQUENCE = true;
    public static final int MAX_LOOP_SIZE = 500;
    public static final String VARIABLE_NAME_REGEX = "[a-zA-Z_][a-zA-Z_0-9]*";
    public static final double PLAY_CELL_SIZE_FACTOR = 1.2;
    public static final Entity NO_ENTITY = new Entity("", null, EntityType.NONE);
//    public static final boolean EXECUTE_IF_IS_COMMAND = true;
    public static final String TOOLTIP_VARIABLE_NAME = "Reference this variable via this name.";
    public static final String TOOLTIP_BOOLEAN = "True, false, a boolean variable, boolean method call or comparisons of int:\n" +
            "a and b are int Variables:" +
            "- a < b (is a smaller than b?)" +
            "- a > b (is a greater than b?)" +
            "- a == b (is a equal to b?)" +
            "- a != b (is a unequal to b?)" +
            "- a <= b (is a smaller or equal to b?)" +
            "- a >= b (is a greater or equal to b?)" +
            "\n. You can link booleans or conditions in the following ways:\n" +
            "- boolean1 && boolean2 is true if boolean1 and boolean2 are true\n" +
            "- boolean1 || boolean2 is true if one or both of boolean1, boolean2 are true\n" +
            "- You can also write !boolean1 which is true if boolean1 is false\n" +
            "Lastly here is an example with the boolean variable b1: !( ((1<2) && false) || b1). Can you figure out when its true?";
    public static final String TOOLTIP_VARIABLE_VALUE = "The value of the variable:\nint : a whole number, randInt(<lowestValue>,<highestValue>) or any other int variable as well as a term thereof:\nboolean: "+TOOLTIP_BOOLEAN+"\nDirection: EAST, WEST, NORTH, SOUTH\nTurnDirection: LEFT, RIGHT, AROUND\nCommand: a Method Call (see Knight Methods) but without an object name and the '.' e.g. move()";
    public static final String TOOLTIP_VARIABLE_TYPE = "Options are: int, boolean, Direction, TurnDirection, Command (, Knight and Army)";
    public static final Effect GREEN_ADJUST = new ColorAdjust(-0.35, 0, 0, 0);
    public static final Effect VIOLET_ADJUST = new ColorAdjust(0.3, 0, 0.1, 0);
    public static final Effect LAST_ADJUST = new ColorAdjust(0.5, 0.1, 0.15, 0.1);
    public static final double LEVEL_ENTRY_SIZE = SCREEN_HEIGHT/10;
    public static final double SPELLBOOK_HEIGHT = SCREEN_HEIGHT/1.12;
    public static final double SPELLBOOK_WIDTH = SCREEN_WIDTH/3.25;
    public static final double BUTTON_SIZE = SCREEN_HEIGHT/10;
    public static final double SMALL_BUTTON_SIZE = SCREEN_HEIGHT/25;
    public static final double FONT_SIZE = 14*SCREEN_WIDTH/1920.0;
    public static final double SMALL_FONT_SIZE = 12*SCREEN_WIDTH/1920.0;
    public static final double BIGGEST_FONT_SIZE = 24.0*SCREEN_WIDTH/1920.0;
    public static final double BIG_FONT_SIZE = 17.0*SCREEN_WIDTH/1920.0;
    public static final double CHALLENGER_FONT_SIZE = 40.0*SCREEN_WIDTH/1920.0;
    public static final String BACK_BTN_IMAGE_PATH = "file:"+IMAGES_PATH+"Back_Btn.png";
    public static final String EXECUTE_BTN_IMAGE_PATH = "file:"+IMAGES_PATH+"Execute_Btn.png";
    public static final String PAUSE_BTN_IMAGE_PATH = "file:"+IMAGES_PATH+"Pause_Btn.png";
    public static final String RESET_BTN_IMAGE_PATH = "file:"+IMAGES_PATH+"Reset_Btn.png";
    public static final String SHOW_SPELLS_BTN_IMAGE_PATH = "file:"+IMAGES_PATH+"Show_Spells_Btn.png";
    public static final String WIZARD_IMAGE_PATH = "file:"+IMAGES_PATH+"TutorialWizard.png";
    public static final String[] TUTORIAL_LINES = new String[] {"Hello there! I see you are seeking guidance in order to escape this dungeon. I will aid you by introducing you to the magic language of JAVA!"
    , "You can navigate these messages by holding down the Alt-Key and pressing either the right or left arrow key!"
    , "If you want to escape from here you will need to summon magic Knights to do your bidding! The dungeon is filled with traps and these Knights will ensure you may leave it unhurt..."
    ,"Below this tutorial field is the game map."
    , "To the left of it you can see how many Knights you have avaiable for the current Level."

    ,"At the bottom of your screen you can see multiple interactable elements:"
    , "The first one is the Back Button:\n" + "It will bring you back to the previous menu"
   ,"The second one is the Execute or Pause Button:\n" +  "It will execute your code or pause it while running"
    , "The third is the Speed Slider:\nIt controls the rate at which the code is executed"
    ,"The fourth one is the Reset Button:\n" + "It will reset the level and can't be used currently as the game isnt running!"
    ,"The last one is the spellbook:\nIt contains all spells you have already successfully applied. It is empty right now."
    ,"To your right you can see your magic script. It is also currently empty, but you can click on it," +
            " after clicking on the Start Button that just appeared and type something if you like."};
    public static final Font SMALL_FONT = new Font("System Regular",GameConstants.SMALL_FONT_SIZE);
    public static final Font MEDIUM_FONT = new Font("System Regular",GameConstants.FONT_SIZE);
    public static final Font CODE_FONT = new Font("Arial",GameConstants.FONT_SIZE);
    public static final Font BIG_FONT = new Font("System Regular",GameConstants.BIG_FONT_SIZE);
    public static final Font BIGGEST_FONT = new Font("Arial",GameConstants.BIGGEST_FONT_SIZE);
    public static final double HEIGHT_RATIO = SCREEN_HEIGHT/1080.0;
    public static final double WIDTH_RATIO = SCREEN_WIDTH/1920.0;
    public static final Font CHALLENGER_FONT = new Font("Times New Roman",CHALLENGER_FONT_SIZE);
    public static final String RED_SCRIPT_ICON_PATH = "file:"+IMAGES_PATH+"Red_Script_Icon.png";
    public static final String BLUE_SCRIPT_ICON_PATH = "file:"+IMAGES_PATH+"Blue_Script_Icon_2.png";
    public static final String KNIGHT_TOKEN_PATH = "file:"+IMAGES_PATH+"Knight_Token.png";
    public static final Effect HIGHLIGHT_BTN_EFFECT = new Glow();
    public static final String PREV_BTN_IMAGE_PATH = "file:"+IMAGES_PATH+"Prev_Btn.png";
    public static final String NEXT_BTN_IMAGE_PATH = "file:"+IMAGES_PATH+"Next_Btn.png";
    public static final String EMPTY_TOKEN_PATH = "file:"+IMAGES_PATH+"Knight_Token_Used.png";;
    public static final String UP_BTN_IMAGE_PATH = "file:"+IMAGES_PATH+"Up_Btn.png";;
    public static final String DOWN_BTN_IMAGE_PATH = "file:"+IMAGES_PATH+"Down_Btn.png";;
    public static final Effect GLOW_BTN_EFFECT = new DropShadow(GameConstants.BIGGEST_FONT_SIZE, Color.WHITE);

    public static final String LEVEL_NAME_PROPERTY_NAME = "LevelName";
    public static final String MAX_KNIGHTS_PROPERTY_NAME = "MaxKnights";
    public static final String INDEX_PROPERTY_NAME = "Index";
    public static final String IS_TUTORIAL_PROPERTY_NAME = "IsTutorial";
    public static final String TURNS_TO_STARS_2_PROPERTY_NAME = "TurnsToStars2";
    public static final String LOC_TO_STARS_2_PROPERTY_NAME = "LocToStars2";
    public static final String TURNS_TO_STARS_3_PROPERTY_NAME = "TurnsToStars3";
    public static final String LOC_TO_STARS_3_PROPERTY_NAME = "LocToStars3";
    public static final String CURRENT_TUTORIAL_MESSAGE_PROPERTY_NAME = "CurrentTutorialMessage";
    public static final String CURRENT_TUTORIAL_INDEX_PROPERTY_NAME = "CurrentTutorialIndex";

//    public static final int MAX_CHARS_PER_CODEFIELD = 50;

    //abhängig vom canvas...
    //public static final int CELL_SIZE =

}
