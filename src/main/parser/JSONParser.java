package main.parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import main.model.*;
import main.model.gamemap.Cell;
import main.model.gamemap.enums.CellContent;
import main.model.gamemap.enums.ItemType;
import main.model.GameConstants;
import main.model.gamemap.GameMap;
import main.model.gamemap.enums.CellFlag;
import main.model.statement.ComplexStatement;
import main.utility.Point;
import main.utility.Util;
import main.view.CodeAreaType;

import static main.model.LevelDataType.*;
import static main.model.GameConstants.NO_ENTITY;

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
        GameMap map = (GameMap) ModelInformer.getDataFromCurrentLevel(MAP_DATA);

        for(int y = 0; y < map.getBoundY(); y++){

            JSONArray mapRow = new JSONArray();
            for(int x = 0; x < map.getBoundX(); x++){
                final Cell cell = map.getCellAtXYClone(x,y);
                JSONObject cellJSONObject = new JSONObject();
                JSONArray flagArray = new JSONArray();
                for(CellFlag cellFlag : CellFlag.values()){
                    if(cell.hasFlag(cellFlag)) flagArray.put(cellFlag.name().toLowerCase());
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
        List<Integer> requiredLevelIdsList = ((List<Integer>) ModelInformer.getDataFromCurrentLevel(REQUIRED_LEVELS));
        List<String> tutorialEntries = (List<String>) ModelInformer.getDataFromCurrentLevel(TUTORIAL_LINES);
        Integer[] locToStars = (Integer[]) ModelInformer.getDataFromCurrentLevel(LOC_TO_STARS);
        Integer[] turnsToStars = (Integer[]) ModelInformer.getDataFromCurrentLevel(TURNS_TO_STARS);
        int maxKnights = (int) ModelInformer.getDataFromCurrentLevel(MAX_KNIGHTS);
        boolean isTutorial = (boolean) ModelInformer.getDataFromCurrentLevel(IS_TUTORIAL);
        ComplexStatement aiBehaviour = (ComplexStatement) ModelInformer.getDataFromCurrentLevel(AI_CODE);

        if(!isTutorial)for(int i = 0; i < requiredLevelIdsList.size();i++){
            if(ModelInformer.getIndexOfLevelWithId(requiredLevelIdsList.get(i)) < ModelInformer.getCurrentIndex())
             requiredLevelsArray.put(requiredLevelIdsList.get(i));
        }
        JSONArray locToStarsArray = new JSONArray();
        fillJSONArrayWithObjects(locToStarsArray,locToStars);
        JSONArray turnsToStarsArray = new JSONArray();
        fillJSONArrayWithObjects(turnsToStarsArray,turnsToStars);
        if(requiredLevelsArray.length() > 0)
        levelJSONObject.put(JSONConstants.REQUIRED_LEVELS,requiredLevelsArray);
        levelJSONObject.put(JSONConstants.LOC_TO_STARS,locToStarsArray);
        levelJSONObject.put(JSONConstants.TURNS_TO_STARS,turnsToStarsArray);
        levelJSONObject.put(JSONConstants.MAP_DATA,mapLines);
        levelJSONObject.put(JSONConstants.MAX_KNIGHTS,maxKnights);
        levelJSONObject.put(JSONConstants.AMOUNT_OF_RERUNS,ModelInformer.getDataFromCurrentLevel(AMOUNT_OF_RERUNS));
        levelJSONObject.put(JSONConstants.ID,ModelInformer.getCurrentId());
        levelJSONObject.put(JSONConstants.IS_TUTORIAL,isTutorial);
        if(isTutorial){
            JSONArray tutorialJSONArray = new JSONArray();
            for(String entry : tutorialEntries){
                tutorialJSONArray.put(entry);
            }
            levelJSONObject.put(JSONConstants.TUTORIAL_LINES, tutorialJSONArray);
        }
        JSONArray aiJSONArray = new JSONArray();
        if((boolean)ModelInformer.getDataFromCurrentLevel(HAS_AI)){
            for(String s : aiBehaviour.getAllText().split("\\n")){
                if(s.equals(""))continue;
                aiJSONArray.put(s);
            }
            levelJSONObject.put(JSONConstants.AI_CODE,aiJSONArray);
            }
        try (FileWriter dataFileWriter = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+ModelInformer.getNameOfLevelWithIndex(ModelInformer.getCurrentIndex())+".json")) {
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

    public static Level parseLevelJSON(String filePathString) throws IOException {

        Path filePath = Path.of(GameConstants.LEVEL_ROOT_PATH,filePathString);
        if(GameConstants.DEBUG)System.out.println(filePathString);
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        boolean isTutorial = jsonObject.getBoolean(JSONConstants.IS_TUTORIAL);

        JSONArray mapLines = jsonObject.getJSONArray(JSONConstants.MAP_DATA,null);

        Cell[][] originalState = new Cell[mapLines.getJSONArray(0).length()][mapLines.length()];
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
        int id = jsonObject.getInt(JSONConstants.ID,-1);
        List<Integer> requiredLevels = new ArrayList<>();
        if(requiredLevelsArray != null)
            for(int i = 0; i < requiredLevelsArray.length();i++){
             requiredLevels.add(requiredLevelsArray.getInt(i, -1));
            }
        int maxKnights = jsonObject.getInt(JSONConstants.MAX_KNIGHTS);
        int amountOfReruns = jsonObject.getInt(JSONConstants.AMOUNT_OF_RERUNS,1);
        if(maxKnights == 0)maxKnights = 1;
        Level level = new Level(name,originalState,complexStatement,turnsToStars,locToStars,requiredLevels,maxKnights,isTutorial,tutorialEntryList,id,amountOfReruns);
        return level;
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
        List<CellFlag> flags = new ArrayList<>();
        if(flagsArray != null)for(int i = 0; i < flagsArray.length(); i++) {
            CellFlag flag = CellFlag.valueOf(flagsArray.getString(i,"").toUpperCase());
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

    public static List<Level> parseAllLevels() throws IOException {
        assert levelFileList != null;
        List<Level> levelList = new ArrayList<>(levelFileList.length);
        for(int i = 0; i< levelFileList.length;i++){
            levelList.add(null);
        }
        List<Integer> ordering = getOrderingFromData();
        for(File file : levelFileList){
            Level level = parseLevelJSON(file.getName());
            int index = -1;
            for(int i = 0; i < ordering.size();i++){
                if(level.getID().equals(ordering.get(i)))index = i;
            }
            levelList.set(index,level);
        }
        return levelList;
    }

    private static List<Integer> getOrderingFromData() {
        JSONArray ordering = dataJSONObject.getJSONArray(JSONConstants.ORDER,null);
        List<Integer> output = new ArrayList<>();
        for(int i = 0; i < ordering.length();i++){
            output.add(ordering.getInt(i, -1));
        }
        return output;
    }

    public static List<Integer> getUnlockedLevelIds(){
        List<Integer> levelIdList = new ArrayList<>();
        for(int i = 0; i < unlocksArray.length();i++){
            levelIdList.add(unlocksArray.getJSONObject(i).getInt(JSONConstants.ID));
        }
        return levelIdList;
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

    public static List<String> getBestCode(int levelId) {
        List<String> output = new ArrayList<>();
        if(unlocksArray == null)return output;
        int id;
        for(int i = 0; i<unlocksArray.length();i++){
            id=unlocksArray.getJSONObject(i).getInt(JSONConstants.ID,-1);
            if(id == levelId){
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
        List<String> unlockedStatementList = new ArrayList<>();
        JSONArray unlocksArray = dataJSONObject.getJSONArray(JSONConstants.UNLOCKED_STATEMENTS,null);
        if(unlocksArray == null)return new ArrayList<>();
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getString(i,"");
            unlockedStatementList.add(s) ;
        }
        return unlockedStatementList;
    }




    private static int getBestLOC(int levelId) {
        int output = -1;
        if(unlocksArray == null)return output;
        int id;
        for(int i = 0; i<unlocksArray.length();i++){
            id=unlocksArray.getJSONObject(i).getInt(JSONConstants.ID,-1);
            if(id == levelId){
                output  = unlocksArray.getJSONObject(i).getInt(JSONConstants.BEST_LOC);
            }
        }
        return output;
    }
    private static int getBestTurns(int levelId) {
        int output = -1;
        if(unlocksArray == null)return output;
        int id;
        for(int i = 0; i<unlocksArray.length();i++){
            id=unlocksArray.getJSONObject(i).getInt(JSONConstants.ID,-1);
            if(id == levelId){
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

    public static void saveLevelChanges(Map<LevelDataType,LevelChange> changes, String levelName) throws IOException {
        // All Levelfiles
        assert levelFileList != null;
        Path levelFilePath = Path.of(GameConstants.LEVEL_ROOT_PATH,levelName+".json");

        String jsonString;

        String oldName;
        if(changes.containsKey(LEVEL_NAME)) {
            oldName = (String)changes.get(LEVEL_NAME).getOldValue();
            changeLevelFileName(oldName,levelName);
            levelFileList = levelFolder.listFiles();
        }
        jsonString = String.join("", Files.readAllLines(levelFilePath));
        JSONObject currentLevelJSONObject = new JSONObject(jsonString);
        if(changes.containsKey(MAX_KNIGHTS)) {
            int maxKnights = (int)changes.get(MAX_KNIGHTS).getNewValue();
            currentLevelJSONObject.put(JSONConstants.MAX_KNIGHTS, maxKnights);
        }
        if(changes.containsKey(AMOUNT_OF_RERUNS)) {
            int amountReruns = (int)changes.get(AMOUNT_OF_RERUNS).getNewValue();
            currentLevelJSONObject.put(JSONConstants.AMOUNT_OF_RERUNS, amountReruns);
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
            List<Integer> reqLevelsLines = (List<Integer>)changes.get(REQUIRED_LEVELS).getNewValue();
            JSONArray requiredJSONArray = new JSONArray();
            for(int i : reqLevelsLines){
                requiredJSONArray.put(i);
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
            JSONArray mapLines = new JSONArray();
            for(int y = 0; y < gameMap.getBoundY(); y++){

                JSONArray mapRow = new JSONArray();
                for(int x = 0; x < gameMap.getBoundX(); x++){
                    final Cell cell = gameMap.getCellAtXYClone(x,y);
                    JSONObject cellJSONObject = new JSONObject();
                    JSONArray flagArray = new JSONArray();
                    for(CellFlag cellFlag : CellFlag.values()){
                        if(cell.hasFlag(cellFlag)) flagArray.put(cellFlag.name().toLowerCase());
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


        dataJSONObject.put(JSONConstants.UNLOCKED_LEVELS, unlockedLevelsArray);
        try (FileWriter dataFileWriter =new FileWriter(dataFilePath.toString())){
            dataFileWriter.write(dataJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataJSONString = String.join("", Files.readAllLines(dataFilePath));
        dataJSONObject = new JSONObject(dataJSONString);
    }

    public static Map<Integer, List<String>> getBestCodeForLevels(List<Level> levels) {
        Map<Integer,List<String>> bestCodeMap = new HashMap<>();
        for(Level l : levels){
            bestCodeMap.put(l.getID(),getBestCode(l.getID()));
        }
        return bestCodeMap;
    }

    public static Map<Integer, Integer> getBestLocForLevels(List<Level> levels) {
        Map<Integer,Integer> bestLocMap = new HashMap<>();
        for(Level l : levels){
            bestLocMap.put(l.getID(),getBestLOC(l.getID()));
        }
        return bestLocMap;
    }
    public static Map<Integer, Integer> getBestTurnsForLevels(List<Level> levels) {
        Map<Integer,Integer> bestTurnsMap = new HashMap<>();
        for(Level l : levels){
            bestTurnsMap.put(l.getID(),getBestTurns(l.getID()));
        }
        return bestTurnsMap;
    }

    public static void storeAllData() {
        JSONArray jsonArray = new JSONArray();
        for(String s : ModelInformer.getUnlockedStatementList()){
            jsonArray.put(s);
        }
        JSONArray orderJSONArray = new JSONArray();
        for(int i : ModelInformer.getOrderedIds()){
            orderJSONArray.put(i);
        }
        dataJSONObject.put(JSONConstants.UNLOCKED_STATEMENTS,jsonArray);
        dataJSONObject.put(JSONConstants.EDITOR_UNLOCKED,ModelInformer.isEditorUnlocked());
        dataJSONObject.put(JSONConstants.TUTORIAL_PROGRESS, ModelInformer.getTutorialProgress());
        dataJSONObject.put(JSONConstants.ORDER, orderJSONArray);
        List<Integer> unlockedLevelIds =ModelInformer.getUnlockedLevelIds();
        List<Integer> lockedIndexes = new ArrayList<>();

        for(int j = 0; j<unlocksArray.length();j++){
            int currentId = unlocksArray.getJSONObject(j).getInt(JSONConstants.ID,-1);
            boolean found = false;
            for(int i = 0; i < unlockedLevelIds.size(); i++){
                int unlockedId = unlockedLevelIds.get(i);
                if(currentId==unlockedId){
                    found = true;
                }
            }
            if(!found)lockedIndexes.add(j);
        }
        for(Integer i : lockedIndexes){
            unlocksArray.remove(i);
        }
        for(int i = 0; i < unlockedLevelIds.size(); i++){
            int id = unlockedLevelIds.get(i);
            int loc = ModelInformer.getBestLocOfLevel(id);
            int turns = ModelInformer.getBestTurnsOfLevel(id);
            List<String> code  = ModelInformer.getBestCodeOfLevel(id);
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
                int levelId = unlocksArray.getJSONObject(j).getInt(JSONConstants.ID,-1);
                if(levelId == id){
                    found =true;
                    levelJSONO.put(JSONConstants.BEST_LOC,loc);
                    levelJSONO.put(JSONConstants.BEST_TURNS,turns);
                    levelJSONO.put(JSONConstants.BEST_CODE,behaviourJArray);
                    levelJSONO.put(JSONConstants.ID,id);
                    unlocksArray.put(j,levelJSONO);
                }
            }
            if(!found){
                levelJSONO.put(JSONConstants.BEST_LOC,loc);
                levelJSONO.put(JSONConstants.BEST_TURNS,turns);
                levelJSONO.put(JSONConstants.BEST_CODE,behaviourJArray);
                levelJSONO.put(JSONConstants.ID,id);
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
            deleteRequiredLevelEntry(ModelInformer.getIdOfLevelWithName(levelName));
            file.delete();
        }
    }

    private static void deleteRequiredLevelEntry(int id) throws IOException {
        for(File file : levelFileList) {
            Path levelFilePath = Path.of(GameConstants.LEVEL_ROOT_PATH,file.getName());
            String jsonString = String.join("", Files.readAllLines(levelFilePath));
            JSONObject jsonObject = new JSONObject(jsonString);
            String name = file.getName().replaceAll("\\.json", "");
            JSONArray jsonArray = jsonObject.getJSONArray(JSONConstants.REQUIRED_LEVELS, new JSONArray());
            JSONArray newRequiredLevels = new JSONArray();
            for(int i = 0; i<jsonArray.length(); i++){
                if(jsonArray.getInt(i,-1) !=id)newRequiredLevels.put(i,jsonArray.getInt(i,-1 ));
            }
            jsonObject.put(JSONConstants.REQUIRED_LEVELS, newRequiredLevels);
            try (FileWriter fileWriter = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+name+".json")) {
                fileWriter.write(jsonObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isEditorUnlocked() {
        return dataJSONObject.getBoolean(JSONConstants.EDITOR_UNLOCKED, false);
    }
}
