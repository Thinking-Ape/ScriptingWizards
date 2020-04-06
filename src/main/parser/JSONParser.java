package main.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javafx.util.Pair;
import main.model.LevelChange;
import main.model.LevelDataType;
import main.model.Model;
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
import main.view.CodeAreaType;

import static main.model.LevelDataType.*;
import static main.utility.GameConstants.NO_ENTITY;

public abstract class JSONParser {

    public static final String JSON_OBJECT_REGEX = "^\\{.*\\}$";
    public static final String JSON_ARRAY_REGEX = "^\\[.*\\]$";


    final private static Path dataFilePath = Path.of(GameConstants.ROOT_PATH,JSONConstants.DATA+".json");
    private static String dataJSONString;
    private static JSONObject dataJSONObject;
    private static JSONArray unlocksArray;
    private static File levelFolder;
    private static File[] levelFileList;

    public static void init() throws IOException {

        dataJSONString = String.join("", Files.readAllLines(dataFilePath));
        dataJSONObject = new JSONObject(dataJSONString);
        unlocksArray = dataJSONObject.getJSONArray(JSONConstants.UNLOCKED_LEVELS,null);
        levelFolder = new File(Paths.get(GameConstants.LEVEL_ROOT_PATH).toString());
        levelFileList = levelFolder.listFiles();
    }

    public static void saveCurrentLevel() throws IOException {
        JSONObject levelJSONObject = new JSONObject();
        JSONArray mapLines = new JSONArray();
        GameMap map = (GameMap) Model.getDataFromCurrentLevel(MAP_DATA);

        for(int y = 0; y < map.getBoundY(); y++){

            JSONArray mapRow = new JSONArray();
            for(int x = 0; x < map.getBoundX(); x++){
                final Cell cell = map.getCellAtXYClone(x,y);
                JSONObject cellJSONObject = new JSONObject();
                JSONArray flagArray = new JSONArray();
                for(CFlag cFlag : CFlag.values()){
                    if(cell.hasFlag(cFlag)) flagArray.put(cFlag.name().toLowerCase());
                }
                if(flagArray.length()>0){cellJSONObject.put(JSONConstants.CELL_FLAGS,flagArray);}
                if(cell.getCellId() !=-1){cellJSONObject.put(JSONConstants.CELL_ID,cell.getCellId());}
                if(cell.getItem()!=ItemType.NONE)cellJSONObject.put(JSONConstants.CELL_ITEM, cell.getItem().name().toLowerCase());
                if(cell.getLinkedCellsSize()>0){
                    JSONArray linkedIdsJSONArray = new JSONArray();
                    for(int i = 0; i < cell.getLinkedCellsSize();i++){
                        linkedIdsJSONArray.put(cell.getLinkedCellId(i));
                    }
                    cellJSONObject.put(JSONConstants.LINKED_CELL_IDS,linkedIdsJSONArray);
                }
                if(cellJSONObject.isEmpty())mapRow.put(cell.getContent().name().toLowerCase());
                else {
                    cellJSONObject.put(JSONConstants.CELL_CONTENT,cell.getContent().name().toLowerCase());
                    mapRow.put(cellJSONObject);
                }
            }
            mapLines.put(mapRow);
        }

        JSONArray requiredLevelsArray = new JSONArray();
        Object[] requiredLevelNamesArray = ((List<String>) Model.getDataFromCurrentLevel(REQUIRED_LEVELS)).toArray();
        List<String> tutorialEntries = (List<String>) Model.getDataFromCurrentLevel(TUTORIAL_LINES);
        Integer[] locToStars = (Integer[]) Model.getDataFromCurrentLevel(LOC_TO_STARS);
        Integer[] turnsToStars = (Integer[]) Model.getDataFromCurrentLevel(TURNS_TO_STARS);
        int maxKnights = (int) Model.getDataFromCurrentLevel(MAX_KNIGHTS);
        boolean isTutorial = (boolean) Model.getDataFromCurrentLevel(IS_TUTORIAL);
        ComplexStatement aiBehaviour = (ComplexStatement) Model.getDataFromCurrentLevel(AI_CODE);

        if(!isTutorial)fillJSONArrayWithObjects(requiredLevelsArray,requiredLevelNamesArray);
        JSONArray locToStarsArray = new JSONArray();
        fillJSONArrayWithObjects(locToStarsArray,locToStars);
        JSONArray turnsToStarsArray = new JSONArray();
        fillJSONArrayWithObjects(turnsToStarsArray,turnsToStars);
//        levelJSONObject.put("name",level.getDisplayName());
        if(requiredLevelsArray.length() > 0)
        levelJSONObject.put(JSONConstants.REQUIRED_LEVELS,requiredLevelsArray);
        levelJSONObject.put(JSONConstants.LOC_TO_STARS,locToStarsArray);
        levelJSONObject.put(JSONConstants.TURNS_TO_STARS,turnsToStarsArray);
        levelJSONObject.put(JSONConstants.MAP_DATA,mapLines);
        levelJSONObject.put(JSONConstants.MAX_KNIGHTS,maxKnights);
        levelJSONObject.put(JSONConstants.INDEX,Model.getCurrentIndex());
        correctIndexes();
        levelJSONObject.put(JSONConstants.IS_TUTORIAL,isTutorial);
        if(isTutorial){
            JSONArray tutorialJSONArray = new JSONArray();
            for(String entry : tutorialEntries){
                tutorialJSONArray.put(entry);
            }
            levelJSONObject.put(JSONConstants.TUTORIAL_LINES, tutorialJSONArray);
        }
        JSONArray aiJSONArray = new JSONArray();
        if((boolean)Model.getDataFromCurrentLevel(HAS_AI)){
            for(String s : aiBehaviour.print().split("\\n")){
                if(s.equals(""))continue;
                aiJSONArray.put(s);
            }
            levelJSONObject.put(JSONConstants.AI_CODE,aiJSONArray);
            }
        try (FileWriter dataFileWriter = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+Model.getNameOfLevelWithIndex(Model.getCurrentIndex())+".json")) {
            dataFileWriter.write(levelJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataJSONString = String.join("", Files.readAllLines(dataFilePath));
        dataJSONObject = new JSONObject(dataJSONString);
    }

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
        int index = jsonObject.getInt(JSONConstants.INDEX);
        boolean isTutorial = jsonObject.getBoolean(JSONConstants.IS_TUTORIAL);
        Point spawn = new Point(-1,-1);

        JSONArray mapLines = jsonObject.getJSONArray(JSONConstants.MAP_DATA,null);

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
        JSONArray aiLineArray = jsonObject.getJSONArray(JSONConstants.AI_CODE,null);
        List<String> aiLines = new ArrayList<>();
        ComplexStatement complexStatement = new ComplexStatement();
        if(aiLineArray!=null){
        for (int i = 0; i < aiLineArray.length(); i++){
            aiLines.add(aiLineArray.getString(i));
        }
            complexStatement =  CodeParser.parseProgramCode(aiLines, CodeAreaType.AI);
        }
        assert spawn.getX()!=-1;
        JSONArray turnsToStarsArray = jsonObject.getJSONArray(JSONConstants.TURNS_TO_STARS,new JSONArray());
        Integer[] turnsToStars = new Integer[turnsToStarsArray.length()];
        fillArrayFromJSON(turnsToStars,turnsToStarsArray,true);
        JSONArray locToStarsArray = jsonObject.getJSONArray(JSONConstants.LOC_TO_STARS, new JSONArray());
        Integer[] locToStars = new Integer[locToStarsArray.length()];
        fillArrayFromJSON(locToStars,locToStarsArray,true);
        JSONArray requiredLevelsArray = jsonObject.getJSONArray(JSONConstants.REQUIRED_LEVELS,null);
        List<String> tutorialEntryList = new ArrayList<>();
        if(isTutorial){
            JSONArray tutorialJSONArray = jsonObject.getJSONArray(JSONConstants.TUTORIAL_LINES,null);
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
        int maxKnights = jsonObject.getInt(JSONConstants.MAX_KNIGHTS);
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
        JSONArray flagsArray = jsonObject.getJSONArray(JSONConstants.CELL_FLAGS,null);
        String contentString = jsonObject.getString(JSONConstants.CELL_CONTENT,"");
        int id = jsonObject.getInt(JSONConstants.CELL_ID,-1);
        String itemString = jsonObject.getString(JSONConstants.CELL_ITEM,"");
        JSONArray linkedCellsArray = jsonObject.getJSONArray(JSONConstants.LINKED_CELL_IDS,null);
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

    public static List<Level> parseAllResourceLevels() throws IOException {
        assert levelFileList != null;
        List<Level> outputList = new ArrayList<>(levelFileList.length);
        for(int i = 0; i< levelFileList.length;i++){
            outputList.add(null);
        }
        //TODO:
        for(File file : levelFileList){
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

    public static List<String> getUnlockedLevelNames(){
        List<String> output = new ArrayList<>();
        for(int i = 0; i < unlocksArray.length();i++){
            output.add(unlocksArray.getJSONObject(i).getString(JSONConstants.LEVEL_NAME));
        }
        return output;
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

    private static boolean changeLevelFileName(String oldName, String newName) throws IOException {
        Path source = Paths.get(GameConstants.LEVEL_ROOT_PATH,oldName+".json");
        Path newPath = Paths.get(GameConstants.LEVEL_ROOT_PATH,newName+".json");
        File file = new File(newPath.toUri());
        if(file.exists())return false;
        Files.move(source, source.resolveSibling(newName+".json"));
        return true;
    }


    public static int getTutorialProgressIndex() {
        return dataJSONObject.getInt(JSONConstants.TUTORIAL_PROGRESS,-1);
    }

    public static List<String> getBestCode(String levelName) {
        List<String> output = new ArrayList<>();
        if(unlocksArray == null)return output;
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getJSONObject(i).getString(JSONConstants.LEVEL_NAME,"");
            if(s.equals(levelName)){
                JSONArray codeArray = unlocksArray.getJSONObject(i).getJSONArray(JSONConstants.BEST_CODE,null);
                if(codeArray == null)return new ArrayList<>();
                for(int j = 0; j<codeArray.length();j++){
                    output.add(codeArray.getString(j,""));
                }
                return output;
            }
        }
        return output;
    }

    public static List<String> getUnlockedStatementList() {
        List<String> output = new ArrayList<>();
        JSONArray unlocksArray = dataJSONObject.getJSONArray(JSONConstants.UNLOCKED_STATEMENTS,null);
        if(unlocksArray == null)return new ArrayList<>();
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getString(i,"");
            output.add(s) ;
        }
        return output;
    }




    private static int getBestLOC(String levelName) {
        int output = -1;
        if(unlocksArray == null)return output;
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getJSONObject(i).getString(JSONConstants.LEVEL_NAME,"");
            if(s.equals(levelName)){
                output  = unlocksArray.getJSONObject(i).getInt(JSONConstants.BEST_LOC);
            }
        }
        return output;
    }
    private static int getBestTurns(String levelName) {
        int output = -1;
        if(unlocksArray == null)return output;
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getJSONObject(i).getString(JSONConstants.LEVEL_NAME,"");
            if(s.equals(levelName)){
                output = unlocksArray.getJSONObject(i).getInt(JSONConstants.BEST_TURNS);
            }
        }
        return output;
    }

    public static void storeCode(List<String> allCode) {
        JSONArray jsonArray = new JSONArray();
        for(String s : allCode){
            jsonArray.put(s.trim());
        }
        dataJSONObject.put(JSONConstants.STORED_CODE,jsonArray);
        try (FileWriter dataFileWriter = new FileWriter(dataFilePath.toString())){
            dataFileWriter.write(dataJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static List<String> getStoredCode() {
        JSONArray jsonArray = dataJSONObject.getJSONArray(JSONConstants.STORED_CODE, new JSONArray());
        List<String> output = new ArrayList<>();
        for(int i = 0; i <jsonArray.length();i++){
            output.add(jsonArray.getString(i ));
        }
        return output;
    }

    //TODO: not complete yet and should be divided into mltiple methods!
    public static void saveLevelChanges(Map<LevelDataType,LevelChange> changes, String levelName) throws IOException {
        // All Levelfiles
        assert levelFileList != null;
        int newIndex = -1;
        int oldIndex = -1;
        int step = 1;

        Path levelFilePath = Path.of(GameConstants.LEVEL_ROOT_PATH,levelName+".json");

        String jsonString = "";

        String oldName = levelName;
        if(changes.containsKey(LEVEL_NAME)) {
            oldName = (String)changes.get(LEVEL_NAME).getOldValue();
            changeLevelFileName(oldName,levelName);
            levelFileList = levelFolder.listFiles();
        }
        jsonString = String.join("", Files.readAllLines(levelFilePath));
        JSONObject currentLevelJSONObject = new JSONObject(jsonString);
        if(changes.containsKey(LEVEL_INDEX)) {
            newIndex = (int)changes.get(LEVEL_INDEX).getNewValue();
            correctIndexes();
            currentLevelJSONObject.put(JSONConstants.INDEX, newIndex);
        }
        if(changes.containsKey(MAX_KNIGHTS)) {
            int maxKnights = (int)changes.get(MAX_KNIGHTS).getNewValue();
            currentLevelJSONObject.put(JSONConstants.MAX_KNIGHTS, maxKnights);
        }
        if(changes.containsKey(IS_TUTORIAL)) {
            boolean isTutorial = (boolean)changes.get(IS_TUTORIAL).getNewValue();
            currentLevelJSONObject.put(JSONConstants.IS_TUTORIAL, isTutorial);
        }
        if(changes.containsKey(TURNS_TO_STARS)) {
            Integer[] turnsToStars = (Integer[])changes.get(TURNS_TO_STARS).getNewValue();
            JSONArray turnsToStarsArray = new JSONArray();
            for(int i : turnsToStars){
                turnsToStarsArray.put(i);
            }
            currentLevelJSONObject.put(JSONConstants.TURNS_TO_STARS, turnsToStarsArray);
        }
        if(changes.containsKey(LOC_TO_STARS)) {
            Integer[] locToStars = (Integer[])changes.get(LOC_TO_STARS).getNewValue();
            JSONArray locToStarsArray = new JSONArray();
            for(int i : locToStars){
                locToStarsArray.put(i);
            }
            currentLevelJSONObject.put(JSONConstants.LOC_TO_STARS, locToStarsArray);
        }
        if(changes.containsKey(AI_CODE)) {
            ComplexStatement behaviour = (ComplexStatement) changes.get(AI_CODE).getNewValue();
            List<String> aiCodeLines = behaviour.getCodeLines();
            JSONArray aiJSONArray = new JSONArray();
            for(String s : aiCodeLines){
                aiJSONArray.put(s);
            }
            currentLevelJSONObject.put(JSONConstants.AI_CODE, aiJSONArray);
        }
        if(changes.containsKey(REQUIRED_LEVELS)) {
            List<String> reqLevelsLines = (List<String>)changes.get(REQUIRED_LEVELS).getNewValue();
            JSONArray requiredJSONArray = new JSONArray();
            for(String s : reqLevelsLines){
                requiredJSONArray.put(s);
            }
            currentLevelJSONObject.put(JSONConstants.REQUIRED_LEVELS, requiredJSONArray);
        }
        if(changes.containsKey(TUTORIAL_LINES)) {
            List<String> tutorialLines = (List<String>)changes.get(TUTORIAL_LINES).getNewValue();
            JSONArray tutorialJSONArray = new JSONArray();
            for(String s : tutorialLines){
                tutorialJSONArray.put(s);
            }
            currentLevelJSONObject.put(JSONConstants.TUTORIAL_LINES, tutorialJSONArray);
        }
        if(changes.containsKey(MAP_DATA)) {
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
                    if(flagArray.length()>0){cellJSONObject.put(JSONConstants.CELL_FLAGS,flagArray);}
                    if(cell.getCellId() !=-1){cellJSONObject.put(JSONConstants.CELL_ID,cell.getCellId());}
                    if(cell.getItem()!=ItemType.NONE)cellJSONObject.put(JSONConstants.CELL_ITEM, cell.getItem().name().toLowerCase());
                    if(cell.getLinkedCellsSize()>0){
                        JSONArray linkedIdsJSONArray = new JSONArray();
                        for(int i = 0; i < cell.getLinkedCellsSize();i++){
                            linkedIdsJSONArray.put(cell.getLinkedCellId(i));
                        }
                        cellJSONObject.put(JSONConstants.LINKED_CELL_IDS,linkedIdsJSONArray);
                    }
                    if(cellJSONObject.isEmpty())mapRow.put(cell.getContent().name().toLowerCase());
                    else {
                        cellJSONObject.put(JSONConstants.CELL_CONTENT,cell.getContent().name().toLowerCase());
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
        JSONObject currentLevelJSONObjectInData = null;
        int indexOfLevel = currentLevelJSONObject.getInt(JSONConstants.INDEX);
        for(int i = 0; i< unlockedLevelsArray.length();i++){
            if(unlockedLevelsArray.getJSONObject(i).getString(JSONConstants.LEVEL_NAME, "").equals(oldName))
                currentLevelJSONObjectInData = unlockedLevelsArray.getJSONObject(i);
        }
        if(changes.containsKey(LEVEL_NAME)) {
            if(currentLevelJSONObjectInData != null){
                currentLevelJSONObjectInData.put(JSONConstants.LEVEL_NAME,levelName);
                unlockedLevelsArray.put(indexOfLevel,currentLevelJSONObjectInData);
            }
        }

        dataJSONObject.put(JSONConstants.UNLOCKED_LEVELS, unlockedLevelsArray);
        try (FileWriter dataFileWriter =new FileWriter(dataFilePath.toString())){
            dataFileWriter.write(dataJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataJSONString = String.join("", Files.readAllLines(dataFilePath));
        dataJSONObject = new JSONObject(dataJSONString);
//        }
    }

    public static Map<Level, List<String>> getBestCodeForLevels(List<Level> levels) {
        Map<Level,List<String>> output = new HashMap<>();
        for(Level l : levels){
            output.put(l,getBestCode(l.getName()));
        }
        return output;
    }

    public static Map<Level, Integer> getBestLOCForLevels(List<Level> levels) {
        Map<Level,Integer> output = new HashMap<>();
        for(Level l : levels){
            output.put(l,getBestLOC(l.getName()));
        }
        return output;
    }
    public static Map<Level, Integer> getBestTurnsForLevels(List<Level> levels) {
        Map<Level,Integer> output = new HashMap<>();
        for(Level l : levels){
            output.put(l,getBestTurns(l.getName()));
        }
        return output;
    }

    public static void storeAllData() {
        JSONArray jsonArray = new JSONArray();
        for(String s : Model.getUnlockedStatementList()){
            jsonArray.put(s);
        }
        dataJSONObject.put(JSONConstants.UNLOCKED_STATEMENTS,jsonArray);
        dataJSONObject.put(JSONConstants.TUTORIAL_PROGRESS, Model.getTutorialProgress());
        int skips = 0;
        List<String> unlockedLevelNames =Model.getUnlockedLevelNames();
        List<Integer> lockedIndexes = new ArrayList<>();

        for(int j = 0; j<unlocksArray.length();j++){
            String levelName = unlocksArray.getJSONObject(j).getString(JSONConstants.LEVEL_NAME,"");
            boolean found = false;
            for(int i = 0; i < unlockedLevelNames.size(); i++){
                String name = unlockedLevelNames.get(i);
                if(levelName.equals(name)){
                    found = true;
                }
            }
            if(!found)lockedIndexes.add(j);
        }
        for(Integer i : lockedIndexes){
            unlocksArray.remove(i);
        }
        for(int i = 0; i < unlockedLevelNames.size(); i++){
            String name = unlockedLevelNames.get(i);
            int index = Model.getIndexOfLevelInList(name);
            int loc = Model.getBestLocOfLevel(index);
            int turns = Model.getBestTurnsOfLevel(index);
            List<String> code  = Model.getBestCodeOfLevel(index);
            JSONArray behaviourJArray = new JSONArray();
            if(code == null)
                behaviourJArray.put("");
            else for(String codeLine : code){
                if(codeLine.equals(""))continue;
                behaviourJArray.put(codeLine);
            }
            boolean found = false;
            JSONObject levelJSONO = new JSONObject();
            for(int j = 0; j<unlocksArray.length();j++){
                if(found)break;
                String levelName = unlocksArray.getJSONObject(j).getString(JSONConstants.LEVEL_NAME,"");
                if(levelName.equals(name)){
                    found =true;
                    levelJSONO.put(JSONConstants.BEST_LOC,loc);
                    levelJSONO.put(JSONConstants.BEST_TURNS,turns);
                    levelJSONO.put(JSONConstants.BEST_CODE,behaviourJArray);
                    levelJSONO.put(JSONConstants.LEVEL_NAME,name);
                    unlocksArray.put(j,levelJSONO);
                }
            }
            if(!found){
                levelJSONO.put(JSONConstants.BEST_LOC,loc);
                levelJSONO.put(JSONConstants.BEST_TURNS,turns);
                levelJSONO.put(JSONConstants.BEST_CODE,behaviourJArray);
                levelJSONO.put(JSONConstants.LEVEL_NAME,name);
                unlocksArray.put(levelJSONO);
            }
        }
        dataJSONObject.put(JSONConstants.UNLOCKED_LEVELS,unlocksArray);
        try (FileWriter dataFileWriter = new FileWriter(dataFilePath.toString())){
            dataFileWriter.write(dataJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteLevel(String levelName) throws IOException {
        File file = new File(Paths.get(GameConstants.LEVEL_ROOT_PATH).toString()+"/"+levelName+".json");
        if(file.exists()){
            correctIndexes();
            deleteRequiredLevelEntry(levelName);
            file.delete();
        }
    }

    private static void deleteRequiredLevelEntry(String entry) throws IOException {
        for(File file : levelFileList) {
            Path levelFilePath = Path.of(GameConstants.LEVEL_ROOT_PATH,file.getName());
            String jsonString = String.join("", Files.readAllLines(levelFilePath));
            JSONObject jsonObject = new JSONObject(jsonString);
            String name = file.getName().replaceAll("\\.json", "");
            JSONArray jsonArray = jsonObject.getJSONArray(JSONConstants.REQUIRED_LEVELS, new JSONArray());
            JSONArray newRequiredLevels = new JSONArray();
            for(int i = 0; i<jsonArray.length(); i++){
                if(!jsonArray.getString(i ).equals(entry))newRequiredLevels.put(i,jsonArray.getString(i ));
            }
            jsonObject.put(JSONConstants.REQUIRED_LEVELS, newRequiredLevels);
            try (FileWriter fileWriter = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+name+".json")) {
                fileWriter.write(jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void correctIndexes() throws IOException {
        for(File file : levelFileList) {
            Path levelFilePath = Path.of(GameConstants.LEVEL_ROOT_PATH,file.getName());
            String jsonString = String.join("", Files.readAllLines(levelFilePath));
            JSONObject jsonObject = new JSONObject(jsonString);
            String name = file.getName().replaceAll("\\.json", "");
            int newIndex = Model.getIndexOfLevelInList(name);
//            int index = jsonObject.getInt(JSONConstants.INDEX);
            jsonObject.put(JSONConstants.INDEX, newIndex);
            try (FileWriter fileWriter = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+name+".json")) {
                fileWriter.write(jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
