package main.exception;

public class UnbalancedAmountOfBracketsException extends IllegalArgumentException {
    public static final String errorMessage = "Unbalanced amount of curly brackets!";
    @Override
    public String getMessage(){
        return errorMessage;
    }
}