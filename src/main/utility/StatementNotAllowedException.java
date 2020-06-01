package main.utility;

public class StatementNotAllowedException extends IllegalArgumentException {
    public static final String errorMessage = "This statement is not allowed here! Skeletons are Enemy-only, Knights are Player-only!";

    @Override
    public String getMessage(){
        return errorMessage;
    }
}
