package main.utility;

public class NoCurlyBracketException extends IllegalArgumentException {
    public static final String errorMessage = "You might have forgotten a curly bracket!";
    @Override
    public String getMessage(){
        return errorMessage;
    }
}
