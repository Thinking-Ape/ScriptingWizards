package main.utility;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import main.model.LevelDataType;
import main.model.gamemap.Cell;
import main.model.Model;
import main.model.enums.CFlag;
import main.model.enums.CellContent;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Util {
    private static final Pattern isInteger = Pattern.compile("[+-]?\\d+");

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

    public static List<String> splitValues(String text) {
        List<String> output = new ArrayList<>();
        int depth =0;
        int index = 0;
        boolean inQuote = false;
        int i =0;
        int lastIndex = 0;
        int bSCount = 0;
        for(char c : text.toCharArray()){
            i++;
            if(c=='\\'){
                bSCount++;
            }
            if(c=='"'){
                if(bSCount%2==1)
                    inQuote = true;
                else inQuote = !inQuote;
            }
            if(c != '\\')bSCount = 0;
            if(c == '{'||c == '['||c=='(')if(!inQuote)depth++;
            if(c == '}'||c == ']'||c==')')if(!inQuote)depth--;
            if(depth == -1)throw new IllegalArgumentException("String " +text.substring(0,i)+" has unbalanced brackets!");
            if(c==',' && depth==0)if(!inQuote){
                if(output.size() <= index)output.add("");
                output.set(index, text.substring(lastIndex, i-1));
                lastIndex = i;
                index++;
            }
        }
        output.add(text.substring(lastIndex ));
        return output;
    }

    public static String stripCode(String substring) {
        if(substring == null)return "";
        substring = substring.trim();
        if(substring.equals("")) return substring;
        if(substring.charAt(substring.length()-1) == ';')
            return stripCode(substring.substring(0,substring.length()-1));
        else return removeUnnecessaryBrackets(removeUnnecessarySpace(substring));
    }

    public static String removeUnnecessarySpace(String code) {
        String output = "";
        boolean foundNeg = false;
        for (int i = 0; i < code.length(); i++){
            char c = code.charAt(i);
            if(i == code.length()-1){
                if(c != ' ') output+=c;
                break;
            }
            char cc = code.charAt(i+1);
            if(c == ' ' && (cc == ' '|| cc ==';'))continue;
            if(c == ' ' && cc == ')')continue;
            if(foundNeg) foundNeg = false;
            else output+=c;
            if(c == '!' && cc == ' ')foundNeg = true;

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
            case 1: return new Color(255/255.0, 255/255.0, 255/255.0, 1);
            case 4: return new Color(135/255.0, 135/255.0, 135/255.0, 1);
            case 3: return new Color(175/255.0, 175/255.0, 175/255.0, 1);
            case 2: return new Color(215/255.0, 215/255.0, 215/255.0, 1);
        }
        return new Color(95/255.0, 95/255.0, 95/255.0, 1);
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
            l.setFont(GameConstants.SMALL_FONT);
        }
    }

    public static void applyStartButtonFormat(Button... startBtns) {
        for(Button b : startBtns){
            b.setPrefHeight(GameConstants.BUTTON_SIZE);
            b.setPrefWidth(GameConstants.BUTTON_SIZE*3);
            b.setAlignment(Pos.CENTER);
            b.setTextAlignment(TextAlignment.CENTER);
            b.setFont(GameConstants.BIGGEST_FONT);
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
            i++;
        }
        if(startList.size() == 0 ){
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

    public static String removeFirstAndLast(String key) {
        if(key.length()<2)return key;
        return key.trim().substring(1, key.length()-1);
    }

    public static String escapeEverything(String text) {
        return text.replaceAll("\\\\", "\\\\\\\\").replaceAll("\n", "\\\\n").replaceAll("\t", "\\\\t").replaceAll("\"", "\\\\\"");
    }

    public static String unescapeEverything(String pairs) {
        // this regex removes the escape sequences and adds linebreaks, quotes etc. for more information check regex101.com
        String newPairs = pairs;
        String newPairs2;
        do {
            newPairs2 = newPairs;
            newPairs = newPairs2.replaceAll("(([^\\\\](\\\\\\\\)?+)\\\\+n|(^(\\\\\\\\)?+)\\\\+n)", "$2\n").replaceAll("(([^\\\\](\\\\\\\\)?+)\\\\+t|(^(\\\\\\\\)?+)\\\\+t)", "$2\t").replaceAll("(([^\\\\](\\\\\\\\)?+)\\\\+\"|(^(\\\\\\\\)?+)\\\\+\")", "$2\"");

        }while (!newPairs.equals(newPairs2));

        do {
            newPairs2 = newPairs;
            newPairs = newPairs2.replaceAll("\\\\\\\\", "\\\\");

        }while (!newPairs.equals(newPairs2));
        return newPairs;
    }

    public static void applyFontFormatRecursively(Pane topHBox) {
        for(Node n : topHBox.getChildren()){
            if(n instanceof Button)((Button) n).setFont(GameConstants.SMALL_FONT);
            if(n instanceof Label)((Label) n).setFont(GameConstants.SMALL_FONT);
            if(Pane.class.isAssignableFrom(n.getClass()))applyFontFormatRecursively((Pane)n);
        }
    }

    public static List<String> StringListFromArray(String... tutorialLines) {
        List<String> output = new ArrayList<>();
        for(String tutorialLine : tutorialLines) output.add(tutorialLine);
        return output;

    }

    public static int getRowCount(TextArea textArea) {
        int currentRowCount = 0;
        Text helper = new Text();

        if(textArea.isWrapText()) {
            // text needs to be on the scene
            Text text = (Text) textArea.lookup(".text");
            if(text == null) {
                return currentRowCount;
            }
            helper.setFont(textArea.getFont());
            for (CharSequence paragraph : textArea.getParagraphs()) {
                helper.setText(paragraph.toString());
                Bounds localBounds = helper.getBoundsInLocal();

                double paragraphWidth = localBounds.getWidth();
                if(paragraphWidth > text.getWrappingWidth()) {
                    double oldHeight = localBounds.getHeight();
                    // this actually sets the automatic size adjustment into motion...
                    helper.setWrappingWidth(text.getWrappingWidth());
                    double newHeight = helper.getBoundsInLocal().getHeight();
                    // ...and we reset it after computation
                    helper.setWrappingWidth(0.0D);

                    int paragraphLineCount = Double.valueOf(newHeight / oldHeight).intValue();
                    currentRowCount += paragraphLineCount;
                } else {
                    currentRowCount += 1;
                }
            }
        } else {
            currentRowCount = textArea.getParagraphs().size();
        }
        return currentRowCount;
    }

    public static double calculateStars(int turns, int loc, Integer[] bestTurns, Integer[] bestLocs) {
        if(loc == -1 && turns == -1)return 0;
        int turnStars = 1;
        if(turns <= bestTurns[0]) turnStars = 2;
        if(turns <= bestTurns[1]) turnStars = 3;
        int locStars = 1;
        if(loc <= bestLocs[0]) locStars = 2;
        if(loc <= bestLocs[1]) locStars = 3;
        double nStars = (turnStars + locStars)/2.0;
        return nStars;
    }

    public static Image getStarImageFromDouble(double stars) {
        if(stars==0){
            return new Image("file:"+GameConstants.IMAGES_PATH+"0StarRating.png");
        }
        else if(stars==1){
            return new Image("file:"+GameConstants.IMAGES_PATH+"1StarRating.png");
        }
        else if(stars==1.5){
            return new Image("file:"+GameConstants.IMAGES_PATH+"1_5StarRating.png");
        }
        else if(stars==2){
            return new Image("file:"+GameConstants.IMAGES_PATH+"2StarRating.png");
        }
        else if(stars==2.5){
            return new Image("file:"+GameConstants.IMAGES_PATH+"2_5StarRating.png");
        }
        else{
            return new Image("file:"+GameConstants.IMAGES_PATH+"3StarRating.png");
        }
    }

    public static double getLabelWidth(Label label) {
        Text text = new Text(label.getText());
        return text.getLayoutBounds().getWidth();
    }

    public static List<String> trimStringList(List<String> allText) {
        if(allText.size() == 0)return allText;
        if(allText.get(allText.size()-1).matches(" *"))return trimStringList(allText.subList(0, allText.size()-1));
        else return allText;
    }

    public static Set<Point> getAllPointsIn(Point minBounds, Point maxBounds) {
        Set<Point> output = new SimpleSet<>();
        int minX = minBounds.getX();
        int maxX = maxBounds.getX();
        int minY = minBounds.getY();
        int maxY = maxBounds.getY();
        for(int x = minX; x < maxX; x++)
            for(int y = minY; y < maxY; y++){
                output.add(new Point(x, y));
            }
        return output;
    }

    public static <T> List<T> moveItems(List<T> codeLines, int startIndex, int endIndex, int amount) {
        List<T> output = new ArrayList<>(codeLines);
        int step = 1;
        if(amount<0) {
            amount = -amount;
            step = -1;
        }
        if(step == 1){
            int tempInt = startIndex;
            startIndex = endIndex;
            endIndex = tempInt;
        }
        for(int i = 0; i < amount; i++){
            for(int j = startIndex; j != endIndex-step; j = j-step){
                if(step == 1 && j == codeLines.size()-1)continue;
                if(step == -1 && j == 0)continue;
                T temp = output.get(j+step);
                output.set(j+step,output.get(j));
                output.set(j,temp);
            }
        }
        return output;
    }

    public static void printList(List<String> codeLines) {
        for(String s : codeLines){
            System.out.println(s);
        }
    }

    public static <T> boolean listsEqual(List<T> list1, List<T> list2) {
        if(list1.size()!=list2.size())return false;
        boolean same = true;
        for(int i = 0; i < list1.size();i++){
            same = same && list1.get(i).equals(list2.get(i));
        }
        return same;
    }

    public static boolean textIsTooLongForCodefield(String code, int depth) {
        Text text = new Text(code);
        text.setFont(GameConstants.CODE_FONT);
        if(text.getLayoutBounds().getWidth() > GameConstants.TEXTFIELD_WIDTH-GameConstants.CODE_OFFSET*depth-GameConstants.SCREEN_WIDTH/110)return true;
        else return false;
    }

    public static Effect getEffect(int i, boolean isEntity) {
        double opacityChange = Model.getCurrentRound()>1 ? -0.5 : 0;
        if(!isEntity)opacityChange = 0;
        //just random numbers
        // color to similar
        if(i >= 2)i++;
        // this too
        if(i >= 5)i*=2;
        double c1 = 0.67*Math.sin(i/(double)GameConstants.MAX_KNIGHTS_AMOUNT*Math.PI*6.8)*Math.sin(i/(double)GameConstants.MAX_KNIGHTS_AMOUNT*Math.PI*4.2);
        double c2 = -0.15*Math.sin(i/(double)GameConstants.MAX_KNIGHTS_AMOUNT*Math.PI*8.4);
        return new ColorAdjust(c1,0,c2,opacityChange);
    }

    public static int getRandIntWithout(int bnd1, int bnd2, List<Integer> excludes) {

        if(bnd2<bnd1){
            int tmpBnd = bnd1;
            bnd1 = bnd2;
            bnd2 = tmpBnd;
        }
        int bound = bnd2+1-bnd1;
        // negative bounds also work!
        int sign = 1;
        if(bound<0){
            bound = bound*-1;
            sign = -1;
        }
        List<Integer> possibleInts = new ArrayList<>();
        for(int i = bnd1; i <= bnd2; i++){
            possibleInts.add(i);
        }
        possibleInts.removeAll(excludes);
        if(possibleInts.size() == 0)return sign*GameConstants.RANDOM.nextInt(bound)+bnd1;
        int index = GameConstants.RANDOM.nextInt(possibleInts.size());
        return possibleInts.get(index);
    }

    public static int avgOfIntList(List<Integer> turnsList) {
        int output = 0;
        if(turnsList.size() == 0)return 0;
        for(Integer i : turnsList){
            output += i;
        }
        return output / turnsList.size();
    }

}
