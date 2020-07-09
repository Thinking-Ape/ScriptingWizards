package main.model;

public enum LevelDifficulty {
    BEGINNER,
    ADVANCED,
    MASTER;

    public static LevelDifficulty valueOfInt(int diffString) {
        if(diffString == 1)return ADVANCED;
        else if(diffString == 2)return MASTER;
        else return BEGINNER;
    }
}
