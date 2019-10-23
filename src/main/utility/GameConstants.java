package main.utility;

import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import main.model.Entity;
import main.model.enums.EntityType;

import java.awt.*;
import java.nio.file.Paths;
import java.util.Random;

public abstract class GameConstants {
    public static final double SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    public static final double SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    public static final boolean DEBUG = false;
    public static final char ANY_CHAR = '?';
    public static final double TICK_SPEED = 0.75;
    public static final boolean SHOW_BOOLEAN_METHODS = false;
    public static final double TEXTFIELD_HEIGHT = SCREEN_HEIGHT/45;
    public static final double TEXTFIELD_WIDTH = SCREEN_WIDTH/5;
    public static final int CODE_OFFSET = 10;
    public static final boolean IS_AI_ACTIVE = true;
    public static final String RAND_INT_REGEX = "randInt\\((.*),(.*)\\)";
    public static final Random RANDOM = new Random();
    public static final String LEVEL_ROOT_PATH = Paths.get("resources/levels/").toString();
    public static final String ROOT_PATH = Paths.get("resources/").toString();
    public static final int MAX_CODE_LINES = 30;
    public static final int MAX_LEVEL_SIZE = 15;
    public static final int MIN_LEVEL_SIZE = 3;
    public static final double MAX_GAMEMAP_SIZE = SCREEN_HEIGHT/1.6;
    public static final int MAX_DEPTH = 4;
    public static final boolean SHOW_TUTORIAL_LEVELS_IN_PLAY = false;
    public static final boolean IS_FULLSCREEN = false;
    public static final String VERSION = "1.0.0";
    public static final boolean ACTION_WITHOUT_CONSEQUENCE = true;
    public static final int MAX_LOOP_SIZE = 500;
    public static final String VARIABLE_NAME_REGEX = "^[a-zA-Z_][a-zA-Z_0-9]*$";
    public static final double PLAY_CELL_SIZE_FACTOR = 1.25;
    public static final Entity NO_ENTITY = new Entity("", null, EntityType.NONE);
    public static final boolean EXECUTE_IF_IS_COMMAND = true;
    public static final String TOOLTIP_VARIABLE_NAME = "Reference this variable via this name.";
    public static final String TOOLTIP_BOOLEAN = "True, false, a boolean variable, boolean method call or comparisons thereof";
    public static final String TOOLTIP_VARIABLE_VALUE = "The value of the variable:\nint : a whole number, randInt(<lowestValue>,<highestValue>) or any other int variable as well as a term thereof\n"+TOOLTIP_BOOLEAN+"\nDirection: EAST, WEST, NORTH, SOUTH\nTurnDirection: LEFT, RIGHT, AROUND\nCommand: a Method Call (see Knight Methods) but without an object name and the '.' e.g. move()";
    public static final String TOOLTIP_VARIABLE_TYPE = "Options are: int, boolean, Direction, TurnDirection, Command (, Knight and Army)";
    public static final Effect GREEN_ADJUST = new ColorAdjust(-0.35, 0, 0, 0);
    public static final Effect VIOLET_ADJUST = new ColorAdjust(0.3, 0, 0.1, 0);
    public static final Effect LAST_ADJUST = new ColorAdjust(0.5, 0.1, 0.15, 0.1);
    public static final double LEVEL_ENTRY_SIZE = SCREEN_HEIGHT/10;
    public static final double SPELLBOOK_HEIGHT = SCREEN_HEIGHT/1.3;
    public static final double SPELLBOOK_WIDTH = SCREEN_WIDTH/4;
    public static final double BUTTON_SIZE = SCREEN_HEIGHT/10;
    public static final double FONT_SIZE = Math.floor(12.0*SCREEN_WIDTH/1920.0);

//    public static final int MAX_CHARS_PER_CODEFIELD = 50;

    //abhängig vom canvas...
    //public static final int CELL_SIZE =

}
