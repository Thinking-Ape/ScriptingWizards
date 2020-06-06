package main.exception;

import main.model.VariableScope;
import main.utility.Util;

public class NotInScopeException extends IllegalArgumentException {
    private String errorMessage;
    public NotInScopeException(String varName, VariableScope scope) {
        errorMessage = "Variable "+varName +" is not in scope!";
        if(scope.getNameListAtDepth().size() == 0)return;
        String nearestVariableString = Util.findNearestEntryWithMaxDist(varName,scope.getNameListAtDepth());
        if(!nearestVariableString.equals("")){
            errorMessage+="\nMaybe you meant: "+nearestVariableString;
        }

    }
    @Override
    public String getMessage(){
        return errorMessage;
    }
}
