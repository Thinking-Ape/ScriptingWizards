package main.model;

public enum LevelDataType {

    LEVEL_INDEX,
    MAX_KNIGHTS,
    MAP_DATA,
    AI_CODE,
    HAS_AI,
    LOC_TO_STARS,
    TURNS_TO_STARS,
    REQUIRED_LEVELS,
    IS_TUTORIAL,
    TUTORIAL_LINES,
    AMOUNT_OF_RERUNS,
    LEVEL_NAME;



    public static LevelDataType getLevelDataTypeFromString(String changeTypeString){
        for(LevelDataType levelDataType : LevelDataType.values()){
            if(changeTypeString.equals(levelDataType.name()))return levelDataType;
        }
        return null;
    }

}
