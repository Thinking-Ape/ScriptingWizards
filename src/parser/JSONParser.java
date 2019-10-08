package parser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import model.Cell;
import model.enums.ItemType;
import utility.GameConstants;
import model.GameMap;
import model.Level;
import model.enums.CContent;
import model.enums.CFlag;
import model.statement.ComplexStatement;
import utility.Point;
import org.json.JSONObject;
import org.json.JSONArray;
import utility.Util;

public abstract class JSONParser {

    public static void saveLevel(Level level) throws IllegalAccessException {
        JSONObject levelJSONObject = new JSONObject();
        JSONArray mapLines = new JSONArray();
        GameMap map = level.getOriginalMap();

        for(int y = 0; y < map.getBoundY(); y++){

            JSONArray mapRow = new JSONArray();
            for(int x = 0; x < map.getBoundX(); x++){
                final Cell cell = map.getCellAtXYClone(x,y);
                JSONObject cellJSONObject = new JSONObject();
                JSONArray flagArray = new JSONArray();
                for(CFlag cFlag : CFlag.values()){
                    if(cell.hasFlag(cFlag)) flagArray.put(cFlag.name().toLowerCase());
                }
                if(flagArray.length()>0){cellJSONObject.put("flags",flagArray);}
                if(cell.getCellId() !=-1){cellJSONObject.put("id",cell.getCellId());}
                if(cell.getItem()!=null)cellJSONObject.put("item", cell.getItem().name().toLowerCase());
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

        JSONArray requiredLevelsArray = new JSONArray();
        fillJSONArrayWithObjects(requiredLevelsArray,level.getRequiredLevels().toArray());
        JSONArray locToStarsArray = new JSONArray();
        fillJSONArrayWithObjects(locToStarsArray,level.getLocToStars());
        JSONArray turnsToStarsArray = new JSONArray();
        fillJSONArrayWithObjects(turnsToStarsArray,level.getTurnsToStars());
//        levelJSONObject.put("name",level.getDisplayName());
        if(requiredLevelsArray.length() > 0)
        levelJSONObject.put("requiredLevels",requiredLevelsArray);
        levelJSONObject.put("locToStars",locToStarsArray);
        levelJSONObject.put("turnsToStars",turnsToStarsArray);
        levelJSONObject.put("map",mapLines);
        levelJSONObject.put("maxKnights",level.getMaxKnights());
        levelJSONObject.put("index",level.getIndex());
        levelJSONObject.put("isTutorial",level.isTutorial());
        if(level.isTutorial()){
            JSONArray tutorialJSONArray = new JSONArray();
            for(String entry : level.getTutorialEntryList()){
                tutorialJSONArray.put(entry);
            }
            levelJSONObject.put("tutorialEntries", tutorialJSONArray);
        }
        JSONArray aiJSONArray = new JSONArray();
        if(level.getAIBehaviour()!=null){
            for(String s : level.getAIBehaviour().print().split("\\n")){
                if(s.equals(""))continue;
                aiJSONArray.put(s);
            }
            levelJSONObject.put("ai",aiJSONArray);
            }
        try (FileWriter file = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+level.getName()+".json")) {
            file.write(levelJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fillJSONArrayWithObjects(JSONArray requiredLevelsArray, Object[] requiredLevels) {
        for(Object o : requiredLevels){
            requiredLevelsArray.put(o);
        }
    }

    public static Level parseLevelJSON(String filePathString) throws IOException, IllegalAccessException {

        Path filePath = Path.of(GameConstants.LEVEL_ROOT_PATH,filePathString);
        System.out.println(filePathString);
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        int index = jsonObject.optInt("index");
        boolean isTutorial = jsonObject.optBoolean("isTutorial");
        Point spawn = new Point(-1,-1);

        JSONArray mapLines = jsonObject.getJSONArray("map");

        Cell[][] originalState = new Cell[mapLines.getJSONArray(0).length()][mapLines.length()];
        List<Point> enemySpawnList = new ArrayList<>();
        for (int row = 0; row < mapLines.length(); row++) {

            JSONArray mapLine = mapLines.getJSONArray(row);

            for(int column = 0; column < mapLine.length(); column++) {

                Cell cell;

                JSONObject cellDetailsObject = mapLine.optJSONObject(column);

                if(cellDetailsObject == null) {
                    cell = parseCell(mapLine.getString(column));
                } else {
                    cell = parseCell(cellDetailsObject);
                }
//                if(cell.getContent()==CContent.SPAWN)spawn = new Point(column,row);
//                if(cell.getContent()==CContent.ENEMY_SPAWN)enemySpawnList.add(new Point(column,row));
                originalState[column][row] = cell;

            }
        }
        String name = filePathString;
        name = name.replaceAll(".json","");
        JSONArray aiLineArray = jsonObject.optJSONArray("ai");
        List<String> aiLines = new ArrayList<>();
        ComplexStatement complexStatement = new ComplexStatement();
        if(aiLineArray!=null){
        for (int i = 0; i < aiLineArray.length(); i++){
            aiLines.add(aiLineArray.getString(i));
        }
            complexStatement =  new CodeParser(aiLines,false).parseProgramCode();
        }
        assert spawn.getX()!=-1;
        JSONArray turnsToStarsArray = jsonObject.optJSONArray("turnsToStars");
        Integer[] turnsToStars = new Integer[turnsToStarsArray.length()];
        fillArrayFromJSON(turnsToStars,turnsToStarsArray,true);
        JSONArray locToStarsArray = jsonObject.optJSONArray("locToStars");
        Integer[] locToStars = new Integer[locToStarsArray.length()];
        fillArrayFromJSON(locToStars,locToStarsArray,true);
        JSONArray requiredLevelsArray = jsonObject.optJSONArray("requiredLevels");
        List<String> tutorialEntryList = new ArrayList<>();
        if(isTutorial){
            JSONArray tutorialJSONArray = jsonObject.optJSONArray("tutorialEntries");
            if(tutorialJSONArray!=null)
            for(int i = 0; i < tutorialJSONArray.length(); i++){
                tutorialEntryList.add(""+tutorialJSONArray.get(i));
            }
        }
        String[] requiredLevels;
        if(requiredLevelsArray != null){
            requiredLevels = new String[requiredLevelsArray.length()];
            fillArrayFromJSON(requiredLevels,requiredLevelsArray,false);
        }else requiredLevels= new String[0];
        int maxKnights = jsonObject.optInt("maxKnights");
        if(maxKnights == 0)maxKnights = 3;
        return new Level(name,originalState,complexStatement,turnsToStars,locToStars,requiredLevels,maxKnights,index,isTutorial,tutorialEntryList);
    }

    private static void fillArrayFromJSON(Object[] turnsToStars, JSONArray turnsToStarsArray,boolean isInteger) {

        for(int i = 0; i < turnsToStarsArray.length(); i++){
            if(isInteger) turnsToStars[i] = turnsToStarsArray.optInt(i);
            else turnsToStars[i] = turnsToStarsArray.optString(i);
        }
    }

    private static Cell parseCell(String string) {
        CContent content;
        if(Util.stringInEnum(CContent.class,string))content= CContent.valueOf(string.toUpperCase());
        else throw new IllegalArgumentException("CellContent " + string + " does not exist!");
        return new Cell(content);
    }
    private static Cell parseCell(JSONObject jsonObject) {

        JSONArray flagsArray = jsonObject.optJSONArray("flags");
        String contentString = jsonObject.optString("content");
        String idString = jsonObject.optString("id");
        String itemString = jsonObject.optString("item");
        JSONArray linkedCellsArray = jsonObject.optJSONArray("linkedIds");
        if(flagsArray == null && idString.equals("") && linkedCellsArray == null && contentString == null) throw new IllegalArgumentException("Cant create a Cell from JSONArray without flags and content");
        List<CFlag> flags = new ArrayList<>();
        if(flagsArray != null)for(int i = 0; i < flagsArray.length(); i++) {
            CFlag flag = CFlag.valueOf(flagsArray.optString(i).toUpperCase());
            flags.add(flag);
        }
        CContent content = CContent.EMPTY;
        if(Util.stringInEnum(CContent.class,contentString))content= CContent.valueOf(contentString.toUpperCase());

        int id = -1;
        if(!idString.equals("")){
            id = Integer.valueOf(idString);
        }
        List<Integer> idList = new ArrayList<>();
        if(linkedCellsArray != null) for(int i = 0; i < linkedCellsArray.length(); i++) {
            String linkedCellId = linkedCellsArray.optString(i);
            if(!linkedCellId.equals("")){
                idList.add(Integer.valueOf(linkedCellId));
            }
        }
        ItemType item = ItemType.getValueFromName(itemString.toUpperCase());
        return new Cell(content,item,null,flags,idList,id);
    }

    public static List<Level> parseAllResourceLevels() throws IOException, IllegalAccessException {
        List<Level> outputList = new ArrayList<>();
        File folder = new File(Paths.get(GameConstants.LEVEL_ROOT_PATH).toString());
        File[] listOfFiles = folder.listFiles();
        assert listOfFiles != null;
        for(File file : listOfFiles){
            outputList.add(parseLevelJSON(file.getName()));
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
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks");
        output = new String[unlocksArray.length()];
        for(int i = 0; i < unlocksArray.length();i++){
            output[i]=unlocksArray.getJSONObject(i).getString("name");
        }
        return output;
    }

    public static void storeProgressIfBetter(String name, int turns, int loc, ComplexStatement playerBehaviour) throws IOException, IllegalAccessException {

        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks");
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
                    if(unlocksArray.getJSONObject(i).has("code"))levelJSONO.put("code",unlocksArray.getJSONObject(i).getJSONArray("code"));
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
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks");
        //TODO: outsource to method!
//        String[] unlockedLevels = getUnlockedLevelNames();
        boolean found;
        for(int i = 0; i < unlockedLevel.getRequiredLevels().size();i++){
            found = false;
            for(int j = 0; j < unlocksArray.length();j++){
                if(unlocksArray.getJSONObject(j).getString("name").equals(unlockedLevel.getRequiredLevels().get(i))){
                    found = true;
                    if(unlocksArray.getJSONObject(j).getInt("loc") == -1 ||unlocksArray.getJSONObject(j).getInt("turns") == -1 ){
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

    public static void removeLevelFromData(String name) throws IOException {
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks");
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
        String[] outputList = new String[listOfFiles.length];
        assert listOfFiles != null;
        int i = 0;
        for(File file : listOfFiles){
            outputList[i]=(file.getName().replace(".json", ""));
            i++;
        }
        return outputList;
    }

    public static boolean resetScoreForLevel(String name) throws IOException, IllegalAccessException {
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));

        JSONObject jsonObject = new JSONObject(jsonString);
        jsonObject.put("tutorialProgress", -1);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks");
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

    public static void saveIndexAndRequiredLevels(List<Level> levelList) throws IOException {
        for(Level level : levelList){
            Path filePath = Path.of(GameConstants.LEVEL_ROOT_PATH,level.getName()+".json");
            String jsonString = String.join("", Files.readAllLines(filePath));

            JSONObject levelJSONObject = new JSONObject(jsonString);
            JSONArray requiredLevelsArray = new JSONArray();
            fillJSONArrayWithObjects(requiredLevelsArray,level.getRequiredLevels().toArray());
            levelJSONObject.put("index", level.getIndex());
            if(requiredLevelsArray.length() > 0)  levelJSONObject.put("requiredLevels", requiredLevelsArray);
            else levelJSONObject.put("requiredLevels", new JSONArray());
            try (FileWriter file = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+level.getName()+".json")) {
                file.write(levelJSONObject.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveRequiredLevels(Level level) throws IOException {
        Path filePath = Path.of(GameConstants.LEVEL_ROOT_PATH,level.getName()+".json");
        String jsonString = String.join("", Files.readAllLines(filePath));

        JSONObject levelJSONObject = new JSONObject(jsonString);
        JSONArray requiredLevelsArray = new JSONArray();
        fillJSONArrayWithObjects(requiredLevelsArray,level.getRequiredLevels().toArray());
        if(requiredLevelsArray.length() > 0)  levelJSONObject.put("requiredLevels", requiredLevelsArray);
        else levelJSONObject.put("requiredLevels", new JSONArray());
        try (FileWriter file = new FileWriter(GameConstants.LEVEL_ROOT_PATH +"/"+level.getName()+".json")) {
            file.write(levelJSONObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean changeLevelName(String oldName, String newName) throws IOException {
        Path source = Paths.get(GameConstants.LEVEL_ROOT_PATH,oldName+".json");
        Path newPath = Paths.get(GameConstants.LEVEL_ROOT_PATH,newName+".json");
        File file = new File(newPath.toUri());
        if(file.exists())return false;
        Files.move(source, source.resolveSibling(newName+".json"));
        Path dataPath = Paths.get(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(dataPath));
        JSONObject dataObject = new JSONObject(jsonString);
        JSONArray unlocksArray = dataObject.getJSONArray("unlocks");
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getJSONObject(i).getString("name");
            if(s.equals(oldName)){
                unlocksArray.getJSONObject(i).put("name",newName);
            }
        }
        try (FileWriter dataFile = new FileWriter(GameConstants.ROOT_PATH +"/data.json")) {
            dataFile.write(dataObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        return jsonObject.optInt("tutorialProgress",-1);
    }

    public static List<String> getBestCode(String levelName) throws IOException {
        List<String> output = new ArrayList<>();
        Path filePath = Path.of(GameConstants.ROOT_PATH,"data.json");
        String jsonString = String.join("", Files.readAllLines(filePath));
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONArray unlocksArray = jsonObject.getJSONArray("unlocks");
        String s;
        for(int i = 0; i<unlocksArray.length();i++){
            s=unlocksArray.getJSONObject(i).getString("name");
            if(s.equals(levelName)){
                JSONArray codeArray = unlocksArray.getJSONObject(i).optJSONArray("code");
                if(codeArray == null)return new ArrayList<>();
                for(int j = 0; j<codeArray.length();j++){
                    output.add(codeArray.getString(j));
                }
                return output;
            }
        }
        return output;
    }
}
