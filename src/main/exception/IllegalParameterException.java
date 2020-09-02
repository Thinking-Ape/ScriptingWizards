package main.exception;

import main.model.gamemap.enums.*;
import main.model.statement.MethodType;
import main.utility.Util;
import main.utility.VariableType;

public class IllegalParameterException extends IllegalArgumentException {
    private  String errorMessage;
    public IllegalParameterException(MethodType mT, String parameter) {
        switch (mT){
            case MOVE:
            case BACK_OFF:
            case USE_ITEM:
            case COLLECT:
            case CAN_MOVE:
            case IS_ALIVE:
            case IS_POSSESSED:
            case IS_SPECIALIZED:
            case IS_DEAD:
            case WAIT:
            case TARGETS_DANGER:
            case DROP_ITEM:
            case ATTACK:
                if(!parameter.equals("")){
                    errorMessage = "Method " + mT.getName() + " doesn't have parameters!";
                }
                break;
            case TURN:
                if(parameter.equals("")){
                    errorMessage = "Method " + mT.getName() + " needs parameters!";
                }
                else errorMessage = getErrorMessage(VariableType.TURN_DIRECTION,parameter);
                break;
            case DISPOSSESS:
                errorMessage = getErrorMessage(VariableType.DIRECTION,parameter);
                break;
            case HAS_ITEM:
                errorMessage = getErrorMessage(VariableType.ITEM_TYPE,parameter);
                break;
            case TARGETS_CELL:
                if(parameter.equals("")){
                    errorMessage = "Method " + mT.getName() + " needs parameters!";
                }
                else errorMessage = getErrorMessage(VariableType.CELL_CONTENT,parameter);
                break;
            case TARGETS_ITEM:
                errorMessage = getErrorMessage(VariableType.ITEM_TYPE,parameter);
                break;
            case TARGETS_ENTITY:
                errorMessage = getErrorMessage(VariableType.ENTITY_TYPE,parameter);
                break;
            case IS_LOOKING:
                if(parameter.equals("")){
                    errorMessage = "Method " + mT.getName() + " needs parameters!";
                }
                break;
        }
    }

    public IllegalParameterException(VariableType vT,String parameter){
        errorMessage = getErrorMessage(vT,parameter);
    }

    private String getErrorMessage(VariableType vT, String parameter) {
        String errorMessage;
        errorMessage = "Parameter "+parameter+" incorrect!";
        switch (vT){
            case INT:
                break;
            case VOID:
                break;
            case KNIGHT:
                break;
            case SKELETON:
                break;
            case DIRECTION:
                String nearestDirectionString = Util.findNearestEntryWithMaxDist(parameter,Util.getNamesOfEnumValues(Direction.values()));
                if(!nearestDirectionString.equals("")){
                    errorMessage+="\nMaybe you meant: "+nearestDirectionString;
                }
                break;
            case TURN_DIRECTION:
                String nearestTurnString = Util.findNearestEntryWithMaxDist(parameter,Util.getNamesOfEnumValues(TurnDirection.values()));
                if(!nearestTurnString.equals("")){
                    errorMessage+="\nMaybe you meant: "+nearestTurnString;
                }
                break;

            case CELL_CONTENT:
                String nearestCCString = Util.findNearestEntryWithMaxDist(parameter,Util.getNamesOfEnumValues(CellContent.values()));
                if(!nearestCCString.equals("")){
                    errorMessage+="\nMaybe you meant: "+nearestCCString;
                }
                break;

            case ITEM_TYPE:
                String nearestItemString = Util.findNearestEntryWithMaxDist(parameter,Util.getNamesOfEnumValues(ItemType.values()));
                if(!nearestItemString.equals("")){
                    errorMessage+="\nMaybe you meant: "+nearestItemString;
                }
                break;
            case ENTITY_TYPE:
                String nearestEntityString = Util.findNearestEntryWithMaxDist(parameter,Util.getNamesOfEnumValues(EntityType.values()));
                if(!nearestEntityString.equals("")){
                    errorMessage+="\nMaybe you meant: "+nearestEntityString;
                }
                break;
            case ARMY:
                break;
            case BOOLEAN:
                break;
        }
        return errorMessage;
    }

    @Override
    public String getMessage(){
        return errorMessage;
    }
}
