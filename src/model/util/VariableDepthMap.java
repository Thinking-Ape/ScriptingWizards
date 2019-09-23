package model.util;

import model.statement.Expression.ExpressionTree;

public class VariableDepthMap {

    private VariableDepthNode  variableDepthNode;

    public boolean put(Variable variable, int depth) throws IllegalAccessException {
        if(variableDepthNode == null){
            variableDepthNode = new VariableDepthNode(variable,depth);
            return true;
        }
        else return variableDepthNode.delegatePut(variable,depth);
    }
//    public boolean contains(String variableName){
//        if(variableDepthNode == null) return false;
//        else return variableDepthNode.findAtDepth(variableName);
//    }
    public boolean contains(String variableName,int depth){
        if(variableDepthNode == null) return false;
        else return variableDepthNode.findAtDepth(variableName,depth) != null;
    }

    public ExpressionTree getValue(String variableName, int depth) {
        if(variableDepthNode == null) return null;
        else return variableDepthNode.findAtDepth(variableName,depth).getValue();
    }
    public VariableType getType(String variable, int depth) {
        if(variableDepthNode == null) return null;
        else return variableDepthNode.findAtDepth(variable,depth).getVariableType();
    }

    public boolean remove(String variableName, int depth) {
        if(variableDepthNode == null){
            return false;
        }
        else return variableDepthNode.delegateRemove(variableName,depth);
    }

    public void clearAtDepth(int depth) {
        if(variableDepthNode == null){
            return;
        }
        else variableDepthNode.delegateClearAtDepth(depth);
    }

    public boolean update(Variable variable, int depth) {
        if(variableDepthNode == null) return  false;
        else return variableDepthNode.delegateUpdate(variable,depth);

    }

    public boolean containsExactlyAtDepth(String variableName, int depth) {
        if(variableDepthNode == null) return false;
        else return variableDepthNode.findAtExactDepth(variableName,depth) != null;
    }
}
