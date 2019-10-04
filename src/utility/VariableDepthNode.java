package utility;

import java.util.ArrayList;
import java.util.List;

public class VariableDepthNode {

    private VariableDepthNode node;

    private List<Variable> variableList;
    private int depth;

    public VariableDepthNode(Variable variable, int depth) {
//        this.valueList = new ArrayList<>();
        this.variableList = new ArrayList<>();
//        this.valueList.add(value);
        this.variableList.add(variable);
        this.depth = depth;
    }

    public boolean delegatePut(Variable variable, int depth) throws IllegalAccessException {
        if(this.depth < depth) {
            if(node == null){
                node = new VariableDepthNode(variable,depth);
                return  true;
            }
            else return node.delegatePut(variable,depth);
        }
        if(this.depth > depth) throw new IllegalAccessException("Looking in the wrong VariableDepthNode. Depth" + depth + "is smaller than " +this.depth+"!");
        for(Variable var : variableList){
            if(var.equals(variable))return false;
        }
        variableList.add(variable);
//        valueList.add(value);
        return true;
    }

    public int getDepth() {
        return depth;
    }

//    public ExpressionTree delegateGetValue(String variableName, int depth){
//        int index = 0;
//        ExpressionTree expressionTree = null;
//        if(depth >= this.depth)
//        for(Variable var : variableList){
//            if(var.getDisplayName().equals(variableName))expressionTree = var.getValue();
//            index ++;
//        }
//        if(depth > this.depth && node != null) if(node.delegateGetValue(variableName,depth) != null) expressionTree = node.delegateGetValue(variableName,depth);
//        return expressionTree;
//    }

//    public Variable findAtDepth(String variableName) {
//        return findAtDepth(variableName,)
//    }

    public boolean delegateRemove(String variableName, int depth) {
        int index = 0;
        if(depth == this.depth) {
            for (Variable var : variableList) {
                if (var.getName().equals(variableName)) {
                    variableList.remove(index);
//                    valueList.remove(index);
                    return true;
                }
                index++;
            }
            return false;
        } else {
            if(node == null) return false;
            else return node.delegateRemove(variableName,depth);
        }
    }

    public void delegateClearAtDepth(int depth) {
        if(this.depth == depth){
            variableList.clear();
//            valueList.clear();
        } else if(this.depth < depth && node != null)node.delegateClearAtDepth(depth);
        //no variables to clear
    }

    //TODO: auch mit findAtDepth ersetzen!
    public boolean delegateUpdate(Variable variable, int depth) {
//        boolean found = false;
        int i = 0;
        if(depth >= this.depth) {
            for (Variable var : variableList) {
                if (var.getName().equals(variable.getName())) {
                    if(node != null){
                        boolean tempBool = node.delegateUpdate(variable,depth);
                        if(!tempBool){
                            variableList.set(i,variable);
//                            valueList.set(i,expression);
                            return true;
                        }
                    }
                    else {
                        variableList.set(i,variable);
                        return true;
                    }
                }

                i++;
                }
            if(node == null) return false;
            return node.delegateUpdate(variable,depth);

            }
        return false;
    }

    public Variable findAtDepth(String variableName, int depth) {
        if(this.depth <= depth) {
            for (Variable var : variableList) {
                if (var.getName().equals(variableName)) return var;
            }
            if(node == null) return null;
            return node.findAtDepth(variableName,depth);
        } else {
            return null;
        }
    }

    public Variable findAtExactDepth(String variableName, int depth) {
        if(this.depth == depth) {
            for (Variable var : variableList) {
                if (var.getName().equals(variableName)) return var;
            }
        } else {
            if(node == null) return null;
            return node.findAtExactDepth(variableName,depth);
        }
        return null;
    }
}
