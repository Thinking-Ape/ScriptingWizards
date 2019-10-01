package util;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import model.Cell;

import java.awt.*;
import java.nio.file.Paths;
import java.util.List;
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
    public static final String RAND_INT_REGEX = "randInt\\((\\d+),(\\d+)\\)";
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
//    public static final int MAX_CHARS_PER_CODEFIELD = 50;

    //abhängig vom canvas...
    //public static final int CELL_SIZE =

    public static boolean stringInEnum(Class c, String s){
        boolean contained = false;
        Enum[] enumValues = (Enum[])c.getEnumConstants();
        for(Enum en : enumValues){
            boolean stringsEqualToUpper =  en.name().equals(s.toUpperCase());
            if(stringsEqualToUpper) contained = true;
        }
        return contained;
    }
    public static Color getColorFromDepth(int depth) {
        switch (depth){
            case 1: return Color.LIGHTGREEN;
            case 2: return Color.LIGHTYELLOW;
            case 3: return Color.PALEGOLDENROD;
            case 4: return Color.GOLDENROD;
        }
        return Color.DARKRED;
    }

    public static Cell[][] mirror(Cell[][] map) {
        Cell[][] output = new Cell[map[0].length][map.length];
        for(int i = 0; i < map[0].length;i++)for (int j = 0; j< map.length;j++){
            output[i][j]=map[j][i];
        }
        return output;
    }
    public static <T> boolean arrayContains(T[] list, T item){
        for(int i = 0; i < list.length;i++){
            if(list[i].equals(item))return true;
        }
        return false;
    }

    public static <T> boolean listContains(List<T> list, T item){
        for(int i = 0; i < list.size();i++){
            if(list.get(i).equals(item))return true;
        }
        return false;
    }

    public static void applyValueFormat(Label... valueLabels) {
        for(Label l : valueLabels){
            l.setStyle("-fx-background-color: rgba(0,0,0,0.1);");
        }
    }

    public static String getDisplayableString(String name) {
        String output = "";
        boolean big = true;
        for(int i = 0; i<name.length();i++){

            if(big &&name.charAt(i)!='_'){
                output+=name.substring(i,i+1).toUpperCase();
                big = false;
            }
            else if (name.charAt(i)!='_')output+=name.substring(i,i+1).toLowerCase();
            else{
                output+=' ';
                big = true;
            }
        }
        return output;
    }
}
