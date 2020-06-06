package main.exception;

public class VariableNotInitializedException extends IllegalArgumentException {
    public static final String errorMessage = "You might have forgotten to initialize the variable!";
    @Override
    public String getMessage(){
        return errorMessage;
    }
}
