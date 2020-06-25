package main.exception;

public class StatementNotAllowedException extends IllegalArgumentException {
    public static final String errorMessage = "This statement is not allowed here!\nSkeletons are Enemy-only, Knights are Player-only!";

    @Override
    public String getMessage(){
        return errorMessage;
    }
}
