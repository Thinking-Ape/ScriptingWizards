package main.model;

import main.utility.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariableScope {

    private Map<Integer, List<Variable>> depthVariableListMap;
    private int currentDepth;

    public VariableScope(){
        depthVariableListMap = new HashMap<>();
        currentDepth = 0;
        depthVariableListMap.putIfAbsent(0, new ArrayList<>());
    }

    public void setCurrentDepth(int depth){
        if(depth < currentDepth){
            for(int i = depth+1; i <= currentDepth;i++){
                depthVariableListMap.remove(i);
                depthVariableListMap.putIfAbsent(i, new ArrayList<>());
            }
        }
        currentDepth = depth;
        depthVariableListMap.putIfAbsent(currentDepth, new ArrayList<>());
    }


    public void addVariable(Variable variable){
        // This should be impossible
        if(this.containsVariable(variable.getName()))throw new IllegalStateException("Variable "+variable.getName()+" is already declared!");
        depthVariableListMap.get(currentDepth).add(variable);
    }

    public boolean containsVariable(String variableName) {
        for(int i = 0; i <= currentDepth; i++){
            for(Variable var : depthVariableListMap.get(i))if(var.getName().equals(variableName)) return true;
        }
        return false;
    }

    public void updateVariable(Variable variable){
        if(!containsVariable(variable.getName()))throw new IllegalStateException("Variable "+variable.getName()+" doesnt exist!");
        getVariable(variable.getName() ).update(variable.getValue());
    }

    public Variable getVariable(String variableName){
        for(int i = 0; i <= currentDepth; i++){
            for(Variable v : depthVariableListMap.get(i))
                if(v.getName().equals(variableName))return v;
        }
        return null;
    }

    public void removeVariable(String variableName) {
        Variable varToRemove = null;
        for(int i = 0; i <= currentDepth; i++){
            for(Variable var : depthVariableListMap.get(i))
                if(var.getName().equals(variableName))varToRemove=var;
            if( varToRemove != null){
                depthVariableListMap.get(i).remove(varToRemove);
                return;
            }
        }
    }
}
