package main.exception;

public class NoSemicolonException extends IllegalArgumentException {
    public static final String errorMessage = "You might have forgotten a semicolon!";
    @Override
    public String getMessage(){
        return errorMessage;
    }
}
