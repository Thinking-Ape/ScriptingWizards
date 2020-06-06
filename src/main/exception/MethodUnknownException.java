package main.exception;

import main.model.statement.MethodType;
import main.utility.Util;

import java.util.ArrayList;
import java.util.List;

public class MethodUnknownException extends IllegalArgumentException{

    private String errorMessage;

    public MethodUnknownException(String methodName){
        errorMessage = "Method " + methodName + " is not a valid method!";
        List<String> knownMethodNames = new ArrayList<>();
        for(MethodType mt : MethodType.values()){
            knownMethodNames.add(mt.getName());
        }
        // Searches for a Method whose Levenshtein distance is less or equal 2 tp the given name
        String nearestMethodString = Util.findNearestEntryWithMaxDist(methodName,knownMethodNames);
        if(!nearestMethodString.equals("")){
             errorMessage+="\nMaybe you meant: "+nearestMethodString;
         }
    }

    @Override
    public String getMessage(){
        return errorMessage;
    }
}
