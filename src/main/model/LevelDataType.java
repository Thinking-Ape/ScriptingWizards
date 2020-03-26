package main.model;

public enum LevelDataType {

    LEVEL_INDEX,
    MAX_KNIGHTS,
    MAP_DATA,
//    MAP_HEIGHT,
//    MAP_WIDTH,
    AI_CODE,
    HAS_AI,
    LOC_TO_STARS,
    TURNS_TO_STARS,
    REQUIRED_LEVELS,
    IS_TUTORIAL,
    TUTORIAL_LINES,

//    UNLOCKED_STATEMENTS,
//    TUTORIAL_PROGRESS,
//    BEST_CODE,
//    BEST_LOC,
//    BEST_TURNS,
    LEVEL_NAME;
//    LEVEL_CREATION,
//    LEVEL_CHANGE ;

    public static LevelDataType getLevelDataTypeFromString(String changeTypeString){
        for(LevelDataType levelDataType : LevelDataType.values()){
            if(changeTypeString.equals(levelDataType.name()))return levelDataType;
        }
        //TODO: better solution?
        return null;
    }

}
