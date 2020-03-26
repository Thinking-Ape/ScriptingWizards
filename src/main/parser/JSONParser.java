package main.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import main.model.LevelChange;
import main.model.LevelDataType;
import main.model.gamemap.Cell;
import main.model.enums.CellContent;
import main.model.enums.ItemType;
import main.utility.GameConstants;
import main.model.gamemap.GameMap;
import main.model.Level;
import main.model.enums.CFlag;
import main.model.statement.ComplexStatement;
import main.utility.Point;
import main.utility.Util;

import static main.model.LevelDataType.*;
import static main.utility.GameConstants.NO_ENTITY;

public abstract class JSONParser {

    public static final String JSON_OBJECT_REGEX = "^\\{.*\\}$";
    public static final String JSON_ARRAY_REGEX = "^\\[.*\\]$";

//    private static void saveLevel(Level level) {
//        JSONObject levelJSONObject = new JSONObject();
//        JSONArray mapLines = new JSONArray();
//        GameMap map = level.getOriginalMapCopy();
//
//        for(int y = 0; y < map.getBoundY(); y++){
//
//            JSONArray mapRow = new JSONArray();
//            for(int x = 0; x < map.getBoundX(); x++){
//                final Cell cell = map.getCellAtXYClone(x,y);
//                JSONObject cellJSONObject = new JSONObject();
//                JSONArray flagArray = new JSONArray();
//                for(CFlag cFlag : CFlag.values()){
//                    if(cell.hasFlag(cFlag)) flagArray.put(cFlag.name().toLowerCase());
//                }
//                if(flagArray.length()>0){cellJSONObject.put("flags",flagArray);}
//                if(cell.getCellId() !=-1){cellJSONObject.put("id",cell.getCellId());}
//                if(cell.getItem()!=ItemType.NONE)cellJSONObject.put("item", cell.getItem().name().toLowerCase());
//                if(cell.getLinkedCellsSize()>0){
//                    JSONArray linkedIdsJSONArray = new JSONArray();
//                    for(int i = 0; i < cell.getLinkedCellsSize();i++){
//                        linkedIdsJSONArray.put(cell.getLinkedCellId(i));
//                    }
//                    cellJSONObject.put("linkedIds",linkedIdsJSONArray);
//                }
//                if(cellJSONObject.isEmpty())mapRow.put(cell.getContent().name().toLowerCase());
//                else {
//                    cellJSONObject.put("content",cell.getContent().name().toLowerCase());
//                    mapRow.put(cellJSONObject);
//                }
//            }
//            mapLines.put(mapRow);
//        }
//
//        JSONArray requiredLevelsArray = new JSONArray();
//        fillJSONArrayWithObjects(requiredLevelsArray,level.getRequiredLevelNamesCopy().toArray());
//        JSONArray locToStarsArray = new JSONArray();
//        fillJSONArrayWithObjects(locToStarsArray,level.getLocToStarsCopy());
//        JSONArray turnsToStarsArray = new JSONArray();
//        fillJSONArrayWithObjects(turnsToStarsArray,level.getTurnsToStarsCopy());
////        levelJSONObject.put("name",level.getDisplayName());
//        if(requiredLevelsArray.length() > 0)
//        levelJSONObject.put("requiredLevels",requiredLevelsArray);
//        levelJSONObject.put("locToStars",locToStarsArray);
//        levelJSONObject.put("turnsToStars",turnsToStarsArray);
//        levelJSONObject.put("map",mapLines);
//        levelJSONObject.put("maxKnights",level.getMaxKnights());
//        levelJSONObject.put("index",level.getIndex());
//        levelJSONObject.put("isTutorial",level.isTutorial());
//        if(level.isTutorial()){
//            JSONArray tutorialJSONArray = new JSONArray();
//            for(String entry : level.getTutorialEntryListCopy()){
//                tutorialJSONArray.put(entry);
//            }
//            levelJSONObject.put("tutorialEntries", tutorialJSONArray);
//        }
//        JSONArray aiJSONArray = new JSONArray();
//        if(level.getAIBehaviourCopy()!=null){
//            for(String s : level.getAIBehaviourCopy().print().split("\\n")){
//                if(s.equals(""))continue;
//                aiJSONArray.put(s);
//            }
//            levelJSONObject.put("ai",aiJSONArray);
//            }
//        try (FileWriter file = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+level.getName()+".json")) {
//            file.write(levelJSONObject.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private static void fillJSONArrayWithObjects(JSONArray requiredLevelsArray, Object[] requiredLevels) {
        for(Object o : requiredLevels){
            requiredLevelsArray.put(o);
        }
    }

    public static Pair<Integer,Level> parseLevelJSON(String filePathString) throws IOException {

        Path filePath = Path.of(GameConstants.LEVEL_ROOT_PATH,filePathString);
        if(GameConstants.DEBUG)System.out.println(filePathString);
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        int index = jsonObject.getInt("index");
        boolean isTutorial = jsonObject.getBoolean("isTutorial");
        Point spawn = new Point(-1,-1);

        JSONArray mapLines = jsonObject.getJSONArray("map",null);

        Cell[][] originalState = new Cell[mapLines.getJSONArray(0).length()][mapLines.length()];
        List<Point> enemySpawnList = new ArrayList<>();
        for (int row = 0; row < mapLines.length(); row++) {

            JSONArray mapLine = mapLines.getJSONArray(row);

            for(int column = 0; column < mapLine.length(); column++) {

                Cell cell;

                JSONObject cellDetailsObject = mapLine.getJSONObject(column,null);

                if(cellDetailsObject == null) {
                    cell = parseCell(mapLine.getString(column,""));
                } else {
                    cell = parseCell(cellDetailsObject);
                }
                originalState[column][row] = cell;

            }
        }
        String name = filePathString;
        name = name.replaceAll(".json","");
        JSONArray aiLineArray = jsonObject.getJSONArray("ai",null);
        List<String> aiLines = new ArrayList<>();
        ComplexStatement complexStatement = new ComplexStatement();
        if(aiLineArray!=null){
        for (int i = 0; i < aiLineArray.length(); i++){
            aiLines.add(aiLineArray.getString(i));
        }
            complexStatement =  CodeParser.parseProgramCode(aiLines,false);
        }
        assert spawn.getX()!=-1;
        JSONArray turnsToStarsArray = jsonObject.getJSONArray("turnsToStars",new JSONArray());
        Integer[] turnsToStars = new Integer[turnsToStarsArray.length()];
        fillArrayFromJSON(turnsToStars,turnsToStarsArray,true);
        JSONArray locToStarsArray = jsonObject.getJSONArray("locToStars", new JSONArray());
        Integer[] locToStars = new Integer[locToStarsArray.length()];
        fillArrayFromJSON(locToStars,locToStarsArray,true);
        JSONArray requiredLevelsArray = jsonObject.getJSONArray("requiredLevels",null);
        List<String> tutorialEntryList = new ArrayList<>();
        if(isTutorial){
            JSONArray tutorialJSONArray = jsonObject.getJSONArray("tutorialEntries",null);
            if(tutorialJSONArray!=null)
            for(int i = 0; i < tutorialJSONArray.length(); i++){
                tutorialEntryList.add((""+tutorialJSONArray.getString(i)));
            }
        }
        String[] requiredLevels;
        if(requiredLevelsArray != null){
            requiredLevels = new String[requiredLevelsArray.length()];
            fillArrayFromJSON(requiredLevels,requiredLevelsArray,false);
        }else requiredLevels= new String[0];
        int maxKnights = jsonObject.getInt("maxKnights");
        if(maxKnights == 0)maxKnights = 3;
        Level level = new Level(name,originalState,complexStatement,turnsToStars,locToStars,requiredLevels,maxKnights,isTutorial,tutorialEntryList);
        return new Pair<>(index,level);
    }

    private static void fillArrayFromJSON(Object[] turnsToStars, JSONArray turnsToStarsArray,boolean isInteger) {

        for(int i = 0; i < turnsToStarsArray.length(); i++){
            if(isInteger) turnsToStars[i] = turnsToStarsArray.getInt(i,-1);
            else turnsToStars[i] = turnsToStarsArray.getString(i,"");
        }
    }

    private static Cell parseCell(String string) {
        CellContent content;
        if(Util.stringInEnum(CellContent.class,string))content= CellContent.valueOf(string.toUpperCase());
        else throw new IllegalArgumentException("CellContent " + string + " does not exist!");
        return new Cell(content);
    }
    private static Cell parseCell(JSONObject jsonObject) {
        JSONArray flagsArray = jsonObject.getJSONArray("flags",null);
        String contentString = jsonObject.getString("content","");
        int id = jsonObject.getInt("id",-1);
        String itemString = jsonObject.getString("item","");
        JSONArray linkedCellsArray = jsonObject.getJSONArray("linkedIds",null);
        if(flagsArray == null && id == -1 && linkedCellsArray == null && contentString == null) throw new IllegalArgumentException("Cant create a Cell from JSONArray without flags and content");
        List<CFlag> flags = new ArrayList<>();
        if(flagsArray != null)for(int i = 0; i < flagsArray.length(); i++) {
            CFlag flag = CFlag.valueOf(flagsArray.getString(i,"").toUpperCase());
            flags.add(flag);
        }
        CellContent content = CellContent.EMPTY;
        if(Util.stringInEnum(CellContent.class,contentString))content= CellContent.valueOf(contentString.toUpperCase());


        List<Integer> idList = new ArrayList<>();
        if(linkedCellsArray != null) for(int i = 0; i < linkedCellsArray.length(); i++) {
            int linkedCellId = linkedCellsArray.getInt(i,-1);
            if(linkedCellId!=-1){
                idList.add(linkedCellId);
            }
        }
        ItemType item = ItemType.getValueFromName(itemString.toUpperCase());
        return new Cell(content,item,NO_ENTITY,flags,idList,id);
    }

    public static List<Level> parseAllResourceLevels() throws IOException, IllegalAccessException {

        File folder = new File(Paths.get(GameConstants.LEVEL_ROOT_PATH).toString());
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        List<Level> outputList = new ArrayList<>(listOfFiles.length);
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = null;
        try {
            jsonString = String.join("", Files.readAllLines(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject(jsonString);
        //TODO:
        JSONArray unlocksArray = jsonObject.getJSONArray(JSONConstants.UNLOCKED_LEVELS, new JSONArray());


        for(File file : listOfFiles){
            Pair<Integer,Level> levelAndIndex = parseLevelJSON(file.getName());
            Level l = levelAndIndex.getValue();
            int index = levelAndIndex.getKey();
            //TODO!
//            if(unlocksArray != null){
//                String s;
//                for(int i = 0; i<unlocksArray.length();i++){
//                    s=unlocksArray.getJSONObject(i).getString(JSONConstants.LEVEL_NAME,"");
//                    if(s.equals(l.getName())){
//                        int loc  = unlocksArray.getJSONObject(i).getInt(JSONConstants.BEST_LOC);
//                        int turns = unlocksArray.getJSONObject(i).getInt(JSONConstants.BEST_TURNS);
////                        l.setBestTurnsAndLOC(turns,loc);
//                    }
//                }}
            outputList.set(index,l);
        }
        return outputList;
    }

    public static String[] getUnlockedLevelNames(){
        String[] output;

        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = null;
        try {
            jsonString = String.join("", Files.readAllLines(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks", new JSONArray());
        output = new String[unlocksArray.length()];
        for(int i = 0; i < unlocksArray.length();i++){
            output[i]=unlocksArray.getJSONObject(i).getString("name");
        }
        return output;
    }

    public static void storeProgressIfBetter(String name, int turns, int loc, ComplexStatement playerBehaviour) throws IOException {

        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks", new JSONArray());
        String s;
        int oldLoc;
        int oldTurns;
        JSONArray behaviourJArray = new JSONArray();
        boolean found = false;
        for(int i = 0; i<unlocksArray.length();i++){
            if(found)break;
            s=unlocksArray.getJSONObject(i).getString("name");
            oldLoc = unlocksArray.getJSONObject(i).getInt("loc");
            oldTurns = unlocksArray.getJSONObject(i).getInt("turns");
//            behaviourJArray = unlocksArray.getJSONObject(i).getJSONArray("code");
            if(s.equals(name)){
                JSONObject levelJSONO  = new JSONObject();
                levelJSONO.put("name",name);
                if((loc <= oldLoc ||oldLoc==-1) && (turns <= oldTurns ||oldTurns==-1)){
                    levelJSONO.put("loc",loc);
                    levelJSONO.put("turns",turns);
                    for(String codeLine : playerBehaviour.print().split("\\n")){
                        if(codeLine.equals(""))continue;
                        behaviourJArray.put(codeLine);
                    }
                    levelJSONO.put("code",behaviourJArray);
                }
                else{
                    levelJSONO.put("loc",oldLoc);
                    levelJSONO.put("turns",oldTurns);
                    if(unlocksArray.getJSONObject(i).has("code"))levelJSONO.put("code",unlocksArray.getJSONObject(i).getJSONArray("code", new JSONArray()));
                }
                unlocksArray.put(i,levelJSONO);
                found =true;
            }
        }
        if(!found)return;
        jsonObject.put("unlocks",unlocksArray);
        try (FileWriter file = new FileWriter(GameConstants.ROOT_PATH +"/data.json")) {
            file.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void updateUnlocks(Level unlockedLevel) throws IOException {
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks",new JSONArray());
        //TODO: outsource to method!
//        String[] unlockedLevels = getUnlockedLevelNames();
        boolean found;
        for(int i = 0; i < unlockedLevel.getRequiredLevelNamesCopy().size(); i++){
            found = false;
            for(int j = 0; j < unlocksArray.length();j++){
                if(unlocksArray.getJSONObject(j).getString("name").equals(unlockedLevel.getRequiredLevelNamesCopy().get(i))){
                    found = true;
                    //TODO: default -1 correct?
                    if(unlocksArray.getJSONObject(j).getInt("loc",-1) == -1 ||unlocksArray.getJSONObject(j).getInt("turns",-1) == -1 ){
                        for(int k = 0; k < unlocksArray.length();k++){
                            if(unlocksArray.getJSONObject(k).getString("name").equals(unlockedLevel.getName())){
                                unlocksArray.remove(k);
                            }
                        }
                        jsonObject.put("unlocks",unlocksArray);
                        try (FileWriter file = new FileWriter(GameConstants.ROOT_PATH +"/data.json")) {
                            file.write(jsonObject.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }
            }
            //TODO: remove code double!!
            if(!found){
                for(int k = 0; k < unlocksArray.length();k++){
                    if(unlocksArray.getJSONObject(k).getString("name").equals(unlockedLevel.getName())){
                        unlocksArray.remove(k);
                    }
                }
                jsonObject.put("unlocks",unlocksArray);
                try (FileWriter file = new FileWriter(GameConstants.ROOT_PATH +"/data.json")) {
                    file.write(jsonObject.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        for(int i = 0; i < unlocksArray.length();i++){
            if(unlocksArray.getJSONObject(i).getString("name").equals(unlockedLevel.getName()))return;
        }
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("name",unlockedLevel.getName());
        jsonObject2.put("loc",-1);
        jsonObject2.put("turns",-1);
        jsonObject2.put("code","");
        unlocksArray.put(jsonObject2);
        jsonObject.put("unlocks",unlocksArray);
        try (FileWriter file = new FileWriter(GameConstants.ROOT_PATH +"/data.json")) {
            file.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //TODO: replace with other DATASTRUCTURE!!
    public static void removeLevelFromData(String name) throws IOException {
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks",new JSONArray());
        for(int i = 0; i < unlocksArray.length();i++){
            if(unlocksArray.getJSONObject(i).getString("name").equals(name))unlocksArray.remove(i);
        }
        try (FileWriter file = new FileWriter(GameConstants.ROOT_PATH +"/data.json")) {
            file.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String[] getAllLevelNames() {
        File folder = new File(Paths.get(GameConstants.LEVEL_ROOT_PATH).toString());
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        String[] outputList = new String[listOfFiles.length];
        int i = 0;
        for(File file : listOfFiles){
            outputList[i]=(file.getName().replace(".json", ""));
            i++;
        }
        return outputList;
    }

    //TODO: DELETE
    public static boolean resetScoreForLevel(String name) throws IOException {
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));

        JSONObject jsonObject = new JSONObject(jsonString);
        jsonObject.put("tutorialProgress", -1);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks",new JSONArray());
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getJSONObject(i).getString("name");
            if(s.equals(name)){
                JSONObject levelJSONO  = new JSONObject();
                levelJSONO.put("name",name);
                levelJSONO.put("loc",-1);
                levelJSONO.put("turns",-1);
                levelJSONO.put("code","");
                unlocksArray.put(i,levelJSONO);
                try (FileWriter file = new FileWriter(GameConstants.ROOT_PATH +"/data.json")) {
                    file.write(jsonObject.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        }
        return false;
    }

//    public static void saveIndexAndRequiredLevels(List<Level> levelList) throws IOException {
//        for(Level level : levelList){
//            Path filePath = Path.of(GameConstants.LEVEL_ROOT_PATH,level.getName()+".json");
//            String jsonString = String.join("", Files.readAllLines(filePath));
//
//            JSONObject levelJSONObject = new JSONObject(jsonString);
//            JSONArray requiredLevelsArray = new JSONArray();
//            fillJSONArrayWithObjects(requiredLevelsArray,level.getRequiredLevelNamesCopy().toArray());
//            levelJSONObject.put("index", level.getIndex());
//            if(requiredLevelsArray.length() > 0)  levelJSONObject.put("requiredLevels", requiredLevelsArray);
//            else levelJSONObject.put("requiredLevels", new JSONArray());
//            try (FileWriter file = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+level.getName()+".json")) {
//                file.write(levelJSONObject.toString());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    public static void saveRequiredLevels(Level level) throws IOException {
//        Path filePath = Path.of(GameConstants.LEVEL_ROOT_PATH,level.getName()+".json");
//        String jsonString = String.join("", Files.readAllLines(filePath));
//
//        JSONObject levelJSONObject = new JSONObject(jsonString);
//        JSONArray requiredLevelsArray = new JSONArray();
//        fillJSONArrayWithObjects(requiredLevelsArray,level.getRequiredLevelNamesCopy().toArray());
//        if(requiredLevelsArray.length() > 0)  levelJSONObject.put("requiredLevels", requiredLevelsArray);
//        else levelJSONObject.put("requiredLevels", new JSONArray());
//        try (FileWriter file = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+level.getName()+".json")) {
//            file.write(levelJSONObject.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private static boolean changeLevelFileName(String oldName, String newName) throws IOException {
        Path source = Paths.get(GameConstants.LEVEL_ROOT_PATH,oldName+".json");
        Path newPath = Paths.get(GameConstants.LEVEL_ROOT_PATH,newName+".json");
        File file = new File(newPath.toUri());
        if(file.exists())return false;
        Files.move(source, source.resolveSibling(newName+".json"));
        return true;
    }

    public static void saveTutorialProgress(int index) throws IOException {
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        jsonObject.put("tutorialProgress", index);
        try (FileWriter file = new FileWriter(GameConstants.ROOT_PATH +"/data.json")) {
            file.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getTutorialProgressIndex() throws IOException {
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        return jsonObject.getInt("tutorialProgress",-1);
    }

    public static List<String> getBestCode(String levelName) throws IOException {
        List<String> output = new ArrayList<>();
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks",null);
        if(unlocksArray == null)return output;
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getJSONObject(i).getString("name","");
            if(s.equals(levelName)){
                JSONArray codeArray = unlocksArray.getJSONObject(i).getJSONArray("code",null);
                if(codeArray == null)return new ArrayList<>();
                for(int j = 0; j<codeArray.length();j++){
                    output.add(codeArray.getString(j,""));
                }
                return output;
            }
        }
        return output;
    }

    public static List<String> getUnlockedStatementList() throws IOException {
        List<String> output = new ArrayList<>();
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray unlocksArray = jsonObject.getJSONArray("statements",null);
        if(unlocksArray == null)return new ArrayList<>();
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getString(i,"");
            output.add(s) ;
        }
        return output;
    }

    public static void saveStatementProgress(List<String> unlockedStatementList) throws IOException {
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = new JSONArray();
        for(String s : unlockedStatementList){
            jsonArray.put(s);
        }
        jsonObject.put("statements",jsonArray);
        try (FileWriter file = new FileWriter(GameConstants.ROOT_PATH +"/data.json")) {
            file.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static int[] getBestResults(String levelName) throws IOException {
        int[] output = new int[2];
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks",null);
        if(unlocksArray == null)return output;
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getJSONObject(i).getString("name","");
            if(s.equals(levelName)){
                int loc  = unlocksArray.getJSONObject(i).getInt("loc");
                int turns = unlocksArray.getJSONObject(i).getInt("turns");
                output[0]=loc;
                output[1]=turns;
                return output;
            }
        }
        return output;
    }

    public static void storeCode(List<String> allCode) throws IOException {
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = new JSONArray();
        for(String s : allCode){
            jsonArray.put(s.trim());
        }
        jsonObject.put("storedCode",jsonArray);
        try (FileWriter file = new FileWriter(GameConstants.ROOT_PATH +"/data.json")) {
            file.write(jsonObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<String> getStoredCode() throws IOException {
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray jsonArray = jsonObject.getJSONArray("storedCode", new JSONArray());
        List<String> output = new ArrayList<>();
        for(int i = 0; i <jsonArray.length();i++){
            output.add(jsonArray.getString(i ));
        }return output;
    }

    //TODO: not complete yet and should be divided into mltiple methods!
    public static void saveLevelChanges(Map<LevelDataType,LevelChange> changes, String levelName) throws IOException {
        Path dataFilePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String dataJsonString = String.join("", Files.readAllLines(dataFilePath));
        JSONObject dataJSONObject = new JSONObject(dataJsonString);

        // All Levelfiles
        File folder = new File(Paths.get(GameConstants.LEVEL_ROOT_PATH).toString());
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        List<Integer> indexChangeList = new ArrayList<>();
        int newIndex = -1;
        int oldIndex = -1;
        int step = 1;

        Path levelFilePath = Path.of(GameConstants.LEVEL_ROOT_PATH,levelName+".json");

        String jsonString = "";
        boolean isCreation = changes.containsKey(LEVEL_CREATION);
        if(!isCreation)jsonString = String.join("", Files.readAllLines(levelFilePath));
        JSONObject currentLevelJSONObject = new JSONObject(jsonString);
        if(changes.containsKey(LEVEL_INDEX)||isCreation) {
            newIndex = (int)changes.get(LEVEL_INDEX).getNewValue();
            oldIndex = (int)changes.get(LEVEL_INDEX).getOldValue();
                    //currentLevelJSONObject.getInt(JSONConstants.INDEX,listOfFiles.length);
            if(oldIndex>newIndex)step = -1;
            for(int i = oldIndex+step; i ==newIndex;i += step ){
                indexChangeList.add(i);
            }
        }
        //TODO:
        for(File file : listOfFiles) {
            levelFilePath = Path.of(GameConstants.LEVEL_ROOT_PATH,file.getName());
            jsonString = String.join("", Files.readAllLines(levelFilePath));
            JSONObject jsonObject = new JSONObject(jsonString);
            String name = file.getName().replaceAll("\\.json", "");
            int index = jsonObject.getInt(JSONConstants.INDEX);
            if(indexChangeList.contains(index)){
                jsonObject.put(JSONConstants.INDEX, index+step);
                try (FileWriter fileWriter = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+name+".json")) {
                    fileWriter.write(jsonObject.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(index == oldIndex)
                jsonObject.put(JSONConstants.INDEX, newIndex);
        }

        if(changes.containsKey(MAX_KNIGHTS)||isCreation) {
            int maxKnights = (int)changes.get(MAX_KNIGHTS).getNewValue();
            currentLevelJSONObject.put(JSONConstants.MAX_KNIGHTS, maxKnights);
        }
        if(changes.containsKey(IS_TUTORIAL)||isCreation) {
            boolean isTutorial = (boolean)changes.get(IS_TUTORIAL).getNewValue();
            currentLevelJSONObject.put(JSONConstants.IS_TUTORIAL, isTutorial);
        }
        if(changes.containsKey(TURNS_TO_STARS)||isCreation) {
            Integer[] turnsToStars = (Integer[])changes.get(TURNS_TO_STARS).getNewValue();
            JSONArray turnsToStarsArray = new JSONArray();
            for(int i : turnsToStars){
                turnsToStarsArray.put(i);
            }
            currentLevelJSONObject.put(JSONConstants.TURNS_TO_STARS, turnsToStarsArray);
        }
        if(changes.containsKey(LOC_TO_STARS)||isCreation) {
            Integer[] locToStars = (Integer[])changes.get(LOC_TO_STARS).getNewValue();
            JSONArray locToStarsArray = new JSONArray();
            for(int i : locToStars){
                locToStarsArray.put(i);
            }
            currentLevelJSONObject.put(JSONConstants.LOC_TO_STARS, locToStarsArray);
        }
        if(changes.containsKey(AI_CODE)||isCreation) {
            ComplexStatement behaviour = (ComplexStatement) changes.get(AI_CODE).getNewValue();
            List<String> aiCodeLines = behaviour.getCodeLines();
            JSONArray aiJSONArray = new JSONArray();
            for(String s : aiCodeLines){
                aiJSONArray.put(s);
            }
            currentLevelJSONObject.put(JSONConstants.AI_CODE, aiJSONArray);
        }
        if(changes.containsKey(REQUIRED_LEVELS)||isCreation) {
            List<String> reqLevelsLines = (List<String>)changes.get(REQUIRED_LEVELS).getNewValue();
            JSONArray requiredJSONArray = new JSONArray();
            for(String s : reqLevelsLines){
                requiredJSONArray.put(s);
            }
            currentLevelJSONObject.put(JSONConstants.REQUIRED_LEVELS, requiredJSONArray);
        }
        if(changes.containsKey(TUTORIAL_LINES)||isCreation) {
            List<String> tutorialLines = (List<String>)changes.get(TUTORIAL_LINES).getNewValue();
            JSONArray tutorialJSONArray = new JSONArray();
            for(String s : tutorialLines){
                tutorialJSONArray.put(s);
            }
            currentLevelJSONObject.put(JSONConstants.TUTORIAL_LINES, tutorialJSONArray);
        }
        if(changes.containsKey(MAP_DATA)||isCreation) {
            GameMap gameMap = (GameMap)changes.get(MAP_DATA).getNewValue();
            //TODO: more Methods
            JSONArray mapLines = new JSONArray();
            for(int y = 0; y < gameMap.getBoundY(); y++){

                JSONArray mapRow = new JSONArray();
                for(int x = 0; x < gameMap.getBoundX(); x++){
                    final Cell cell = gameMap.getCellAtXYClone(x,y);
                    JSONObject cellJSONObject = new JSONObject();
                    JSONArray flagArray = new JSONArray();
                    for(CFlag cFlag : CFlag.values()){
                        if(cell.hasFlag(cFlag)) flagArray.put(cFlag.name().toLowerCase());
                    }
                    if(flagArray.length()>0){cellJSONObject.put("flags",flagArray);}
                    if(cell.getCellId() !=-1){cellJSONObject.put("id",cell.getCellId());}
                    if(cell.getItem()!=ItemType.NONE)cellJSONObject.put("item", cell.getItem().name().toLowerCase());
                    if(cell.getLinkedCellsSize()>0){
                        JSONArray linkedIdsJSONArray = new JSONArray();
                        for(int i = 0; i < cell.getLinkedCellsSize();i++){
                            linkedIdsJSONArray.put(cell.getLinkedCellId(i));
                        }
                        cellJSONObject.put("linkedIds",linkedIdsJSONArray);
                    }
                    if(cellJSONObject.isEmpty())mapRow.put(cell.getContent().name().toLowerCase());
                    else {
                        cellJSONObject.put("content",cell.getContent().name().toLowerCase());
                        mapRow.put(cellJSONObject);
                    }
                }
                mapLines.put(mapRow);
            }
            currentLevelJSONObject.put(JSONConstants.MAP_DATA, mapLines);
        }

        try (FileWriter fileWriter = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+levelName+".json")) {
            fileWriter.write(currentLevelJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }


        // Array of all Levels currently in data
        JSONArray unlockedLevelsArray = dataJSONObject.getJSONArray(JSONConstants.UNLOCKED_LEVELS, null);
        // Current Level in data as JSONObject
        int indexOfLevel = currentLevelJSONObject.getInt(JSONConstants.INDEX);


//        if(changes.containsKey(UNLOCKED_STATEMENTS)) {
//            levelChange =  changes.get(UNLOCKED_STATEMENTS);
//            List<String> unlockedStatementList = (List<String>) (levelChange.getNewValue());
//            JSONArray unlockedStatementsArray = new JSONArray();
//            for (String s : unlockedStatementList) {
//                unlockedStatementsArray.put(s);
//            }
//            dataJSONObject.put(JSONConstants.UNLOCKED_STATEMENTS, unlockedStatementsArray);
//        }
//        if(changes.containsKey(TUTORIAL_PROGRESS)) {
//            levelChange =  changes.get(TUTORIAL_PROGRESS);
//            int tutorialProg = (int)levelChange.getNewValue();
//            dataJSONObject.put(JSONConstants.TUTORIAL_PROGRESS,tutorialProg);
//        }
//        if(changes.containsKey(BEST_CODE)) {
//            levelChange =  changes.get(BEST_CODE);
//            List<String> bestCodeList = (List<String>) levelChange.getNewValue();
//            JSONArray bestCodeArray = new JSONArray();
//            for(String s : bestCodeList){
//                bestCodeArray.put(s);
//            }
//            levelJSONObject.put(JSONConstants.BEST_CODE,bestCodeArray);
//            unlockedLevelsArray.put(indexOfLevel,levelJSONObject);
//        }
//        if(changes.containsKey(BEST_LOC)) {
//            levelChange =  changes.get(BEST_LOC);
//            int bestLoc = (int) levelChange.getNewValue();
//            levelJSONObject.put(JSONConstants.BEST_LOC,bestLoc);
//            unlockedLevelsArray.put(indexOfLevel,levelJSONObject);
//        }
//        if(changes.containsKey(BEST_TURNS)) {
//            levelChange =  changes.get(BEST_TURNS);
//            int bestTurns = (int) levelChange.getNewValue();
//            levelJSONObject.put(JSONConstants.BEST_TURNS,bestTurns);
//            unlockedLevelsArray.put(indexOfLevel,levelJSONObject);
//        }

        if(changes.containsKey(LEVEL_NAME)&&!isCreation) {
            String oldName = currentLevelJSONObject.getString(JSONConstants.LEVEL_NAME);
            currentLevelJSONObject.put(JSONConstants.LEVEL_NAME,levelName);
            unlockedLevelsArray.put(indexOfLevel,currentLevelJSONObject);
            changeLevelFileName(oldName,levelName);
        }
        dataJSONObject.put(JSONConstants.UNLOCKED_LEVELS, unlockedLevelsArray);
        try (FileWriter fileWriter = new FileWriter(GameConstants.ROOT_PATH +"/"+"data"+".json")) {
            fileWriter.write(dataJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        }
    }

}
