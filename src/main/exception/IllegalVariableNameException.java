package main.exception;

import main.utility.Util;

public class IllegalVariableNameException extends IllegalArgumentException {
    public String errorMessage;

    public IllegalVariableNameException(String varName){
        errorMessage = Util.getIllegalVariableString(varName);
    }

    @Override
    public String getMessage(){
        return errorMessage;
    }
}
