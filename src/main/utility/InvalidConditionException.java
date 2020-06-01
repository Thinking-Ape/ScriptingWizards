package main.utility;

public class InvalidConditionException extends IllegalArgumentException {
    public String errorMessage;

    public InvalidConditionException(String condition, String reason){
        errorMessage = Util.getConditionInvalidString(condition,reason);
    }

    @Override
    public String getMessage(){
        return errorMessage;
    }
}
