package utility;

import java.awt.*;
import java.nio.file.Paths;
import java.util.Random;

public abstract class GameConstants {
    public static final boolean DEBUG = true;
    public static final char ANY_CHAR = '?';
    public static final double TICK_SPEED = 0.75;
    public static final boolean SHOW_BOOLEAN_METHODS = false;
    public static final double TEXTFIELD_HEIGHT = 25;
    public static final double TEXTFIELD_WIDTH = 375;
    public static final int CODE_OFFSET = 10;
    public static final boolean IS_AI_ACTIVE = true;
    public static final String RAND_INT_REGEX = "randInt\\((.*),(.*)\\)";
    public static final Random RANDOM = new Random();
    public static final String LEVEL_ROOT_PATH = Paths.get("resources/levels/").toString();
    public static final String ROOT_PATH = Paths.get("resources/").toString();
    public static final int MAX_CODE_LINES = 30;
    public static final int MAX_LEVEL_SIZE = 15;
    public static final int MIN_LEVEL_SIZE = 3;
    public static final double MAX_GAMEMAP_SIZE = 650;
    public static final int MAX_DEPTH = 4;
    public static final double SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    public static final double SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    public static final boolean SHOW_TUTORIAL_LEVELS_IN_PLAY = false;
    public static final boolean IS_FULLSCREEN = false;
    public static final String VERSION = "1.0.0";
//    public static final int MAX_CHARS_PER_CODEFIELD = 50;

    //abhängig vom canvas...
    //public static final int CELL_SIZE =

}
