package main.utility;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
//            System.out.println("."+pairs+"."+newPairs+".");
        }while (!newPairs.equals(newPairs2));

        do {
            newPairs2 = newPairs;
            newPairs = newPairs2.replaceAll("\\\\\\\\", "\\\\");
//            System.out.println("."+pairs+"."+newPairs+".");
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

    public static int countChars(String t1, char c) {

        int output = 1;
        char[] chars = t1.toCharArray();
        for(int i = 0; i < t1.length(); i++){
            if(chars[i]==c)output++;
        }
        return output;
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

    public static StackPane getStackPane(Cell cell, Map<String,Image> contentImageMap, Shape shape, double cell_size, Map<String,Effect> entityColorMap) {
        String contentString = cell.getContent().getDisplayName();
        StackPane stackPane = new StackPane();

        boolean isTurned = false;
        boolean isInverted = false;
        boolean isOpen = false;
        for (CFlag flag : CFlag.values()) {
            if (cell.hasFlag(flag)) {
                if(flag.isTemporary())
                    continue;
                if(flag == CFlag.TURNED && (isTurned = true))continue;
                if(flag == CFlag.INVERTED && cell.getContent()== CellContent.GATE){
                    isInverted = true;
                    if(!isOpen)contentString += "_" + CFlag.OPEN.getDisplayName();
                    else contentString = contentString.replace("_" + CFlag.OPEN.getDisplayName(),"");
                    continue;
                }
                if(flag == CFlag.OPEN ){
                    isOpen = true;
                    if(!isInverted)contentString += "_" + CFlag.OPEN.getDisplayName();
                    else contentString = contentString.replace("_" + CFlag.OPEN.getDisplayName(),"");
                    continue;
                }
                if(flag == CFlag.INVERTED && cell.getContent()== CellContent.PRESSURE_PLATE && Model.getTurnsTaken()==0){
                    isInverted = true;
                    contentString += "_"+CFlag.INVERTED.getDisplayName()+ "_" + CFlag.TRIGGERED.getDisplayName();
                    continue;
                }
                contentString += "_" + flag.getDisplayName();
            }
        }
        ImageView imageView = new ImageView(contentImageMap.get(contentString));
        imageView.setFitWidth(cell_size);
        imageView.setFitHeight(cell_size);
        if(isTurned)imageView.setRotate(270);
        int amountOfKnights = Model.getAmountOfKnights();
        if(amountOfKnights < (int)Model.getDataFromCurrentLevel(LevelDataType.MAX_KNIGHTS)&&cell.getContent()== CellContent.SPAWN)
            switch (amountOfKnights){
                case 1: imageView.setEffect(GameConstants.GREEN_ADJUST);
                    break;
                case 2: imageView.setEffect(GameConstants.VIOLET_ADJUST);
                    break;
                case 3: imageView.setEffect(GameConstants.LAST_ADJUST);
                    break;
            }
        if(cell.getContent()== CellContent.ENEMY_SPAWN)
            switch (entityColorMap.size() -amountOfKnights){
                case 1: imageView.setEffect(GameConstants.GREEN_ADJUST);
                    break;
                case 2: imageView.setEffect(GameConstants.VIOLET_ADJUST);
                    break;
                case 3: imageView.setEffect(GameConstants.LAST_ADJUST);
                    break;
            }
        stackPane.getChildren().add(imageView);

        return stackPane;
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


    public static StringPair splitAtChar(String code, char targetChar,boolean keepCharacter) {

        String first ="";
        String second="";
        boolean found=false;
        for(int i = 0; i < code.length(); i++){
            char c = code.charAt(i);
            if(!found)first = first.concat(c+"");
            else second = second.concat(c+"");

            if(c==targetChar){
                found=true;
            }
        }
        if(!keepCharacter && found)first = first.substring(0,first.length()-1);
        return  new StringPair(first,second);
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


//    public static ImageView getEntityImageView(int number, Cell cell, Model model,List<Entity> entityActionList, Map<String,Image> contentImageMap, double cell_size, Map<String,Effect> entityColorMap, String entityName) {
//
//    }

//    public static StackPane getStackPane2(Cell cell, Model model,List<Entity> entityActionList, Map<String,Image> contentImageMap, double cell_size, Map<String,Effect> entityColorMap) {
//        StackPane stackPane = new StackPane();
//        int number = 1;
//
//
//        return stackPane;
//    }
}
