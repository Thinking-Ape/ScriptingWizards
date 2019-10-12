package utility;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.TextAlignment;
import model.Cell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public abstract class Util {
    private static final Pattern isInteger = Pattern.compile("[+-]?\\d+");

    public static final int tryParseInt(String value) {
        if (value == null || !isInteger.matcher(value).matches()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch(NumberFormatException nfe) {
            return 0;
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

    public static <T> boolean listContains(List<T> list, T item){
        for(int i = 0; i < list.size();i++){
            if(list.get(i).equals(item))return true;
        }
        return false;
    }

    public static String stripCode(String substring) {
        substring = substring.trim();
        if(substring.equals("")) return substring;
        if(substring.charAt(substring.length()-1) == ';')
            return stripCode(substring.substring(0,substring.length()-1));
        else return removeUnnecessaryBrackets(removeUnnecessarySpace(substring));
    }

    public static String removeUnnecessarySpace(String code) {
        String output = "";
        for (int i = 0; i < code.length(); i++){
            char c = code.charAt(i);
            if(i == code.length()-1){
                if(c != ' ') output+=c;
                break;
            }
            char cc = code.charAt(i+1);
            if(c == ' ' && (cc == ' '|| cc ==';'))continue;
            if(c == ' ' && cc == ')')continue;
            output+=c;
        }
        return output;
    }

    public static String removeUnnecessaryBrackets(String code) {
        if(code.length()<=2)return code;
        int depth = 0;
        int amountOfDepthZeros = 0;
        for(int i =0; i < code.length();i++){
            char c = code.charAt(i);
            if(c == '(') {depth++;}
            if(depth == 0) amountOfDepthZeros++;
            if(c == ')') {depth--;}
        }
        if(amountOfDepthZeros==0)
            return removeUnnecessaryBrackets(code.substring(1,code.length()-1));
        else return code;
    }

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

    public static void applyValueFormat(Label... valueLabels) {
        for(Label l : valueLabels){
            l.setStyle("-fx-background-color: rgba(0,0,0,0.1);");
        }
    }

    public static void applyStartButtonFormat(Button... startBtns) {
        for(Button b : startBtns){
            b.setPrefHeight(100);
            b.setPrefWidth(300);
            b.setAlignment(Pos.CENTER);
            b.setTextAlignment(TextAlignment.CENTER);
        }

    }

    public static <T> String getRegEx(T[] values) {
        StringBuilder output = new StringBuilder("(");
        for(T t : values){
            output.append(t.toString().toUpperCase()+"|");
        }
        output.replace(output.length()-1, output.length(), ")");
        return output.toString();
    }

    public static List<Double[]> orderLines(List<Line> edgeList,List<Double[]> outputArray) {
        Line startEdge = edgeList.get(0);
        List<Line> startList = new ArrayList<>(edgeList);
        startList.remove(startEdge);
        List<Double> output = findAntecessor(startEdge,startList, new ArrayList<>());
        outputArray.add(new Double[output.size()]);
        int i = 0;
        for(Double d : output){
            outputArray.get(outputArray.size()-1)[i]=d;
//            System.out.print(i%2==0?"X: "+d:", Y: "+d+"\n");
            i++;
        }
        if(startList.size() == 0 ){
//            List<Double[]> outputArray = new ArrayList<>();
            return outputArray;
        }
        else {
            return orderLines(startList, outputArray);
        }
    }

    private static List<Double> findAntecessor(Line startEdge, List<Line> edgeList,List<Double> output){
        output.add(startEdge.getStartX());
        output.add(startEdge.getStartY());

        for(Line line : edgeList){
            if(line.getStartX() == startEdge.getEndX() && line.getStartY() == startEdge.getEndY()){
                edgeList.remove(line);
                return findAntecessor(line,edgeList, output);
            }
        }
        output.add(startEdge.getEndX());
        output.add(startEdge.getEndY());
        return output;
    }

    public static List<Point> getPointsInRectangle(Point point1, Point point2) {
        List<Point> output = new ArrayList<>();
        int maxX = point1.getX() > point2.getX() ? point1.getX() : point2.getX();
        int maxY = point1.getY() > point2.getY() ? point1.getY() : point2.getY();
        int minX = point1.getX() < point2.getX() ? point1.getX() : point2.getX();
        int minY = point1.getY() < point2.getY() ? point1.getY() : point2.getY();

        for(int x = minX ; x <= maxX ; x++)for(int y = minY; y <= maxY;y++){
            output.add(new Point(x, y));
        }
        return output;
    }

    public static String[] getParametersFromString(String text) {
        if(text.length() == 0)throw new IllegalArgumentException("You shall not pass an empty String!");
        int depth = 0;
        int i = 0;
        String[] output = new String[text.length()];
        output[0]="";
        for(char c : text.toCharArray()){
            if(c == ')')depth--;
            if(c == '(')depth++;
            if(depth == 0 && c == ','){
                i++;
                output[i]="";
            }else
                output[i] = output[i]+c;
        }
        return output;
    }
}
