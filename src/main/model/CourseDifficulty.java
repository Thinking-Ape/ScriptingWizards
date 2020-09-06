package main.model;

public enum CourseDifficulty {
    BEGINNER,
    ADVANCED,
    MASTER;

    public static CourseDifficulty valueOfInt(int diffString) {
        if(diffString == 1)return ADVANCED;
        else if(diffString == 2)return MASTER;
        else return BEGINNER;
    }
}
