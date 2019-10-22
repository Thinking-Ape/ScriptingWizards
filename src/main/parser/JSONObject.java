package main.parser;

import javafx.scene.layout.Background;
import main.utility.Util;

import java.util.HashMap;
import java.util.Map;

import static main.parser.JSONParser.JSON_ARRAY_REGEX;
import static main.parser.JSONParser.JSON_OBJECT_REGEX;

public class JSONObject {
    Map<String,Object> keyValueMap;

    public JSONObject (String pairs){
        keyValueMap = new HashMap<>();
        if(!pairs.matches(JSON_OBJECT_REGEX))throw new IllegalArgumentException(pairs + " is no JSONObject!");
        String[] keyValueList = splitValues( Util.removeFirstAndLast(pairs));
        for(String keyValue : keyValueList){
            String key = keyValue.replaceAll("^(.*):.*$", "$1");
            String value = keyValue.replaceAll("^.*:(.*)$", "$1");
            value = value.trim();
            key = key.trim();
            if(!key.matches("^\".*\"$"))throw new IllegalArgumentException(pairs + " is no legal key value!");
            if(value.matches(JSON_OBJECT_REGEX)){
                keyValueMap.put(key, new JSONObject(value));
            }
            else if(value.matches(JSON_ARRAY_REGEX)){
                keyValueMap.put(key, new JSONArray(value));
            }
            else if(value.matches("^\".*\"$")){
                keyValueMap.put(key, value);
            }
            else if(value.matches("^\\d+$")){
                keyValueMap.put(key, Integer.parseInt(value));
            }
            else if(value.matches("^(true|false)$")){
                keyValueMap.put(key, Boolean.parseBoolean(value));
            }
            else throw new IllegalArgumentException("Value "+ value +" is no valid JSON value!");
        }
    }

    public JSONObject() {
        keyValueMap = new HashMap<>();
    }

    public JSONObject getJSONObject(String key, JSONObject defaultObject){
        if(keyValueMap.containsKey(key)){
            if(keyValueMap.get(key) instanceof JSONObject){
                return (JSONObject) keyValueMap.get(key);
            }
        }
        return defaultObject;
    }
    public JSONArray getJSONArray(String key, JSONArray defaultObject){
        if(keyValueMap.containsKey(key)){
            if(keyValueMap.get(key) instanceof JSONArray){
                return (JSONArray) keyValueMap.get(key);
            }
        }
        return defaultObject;
    }

    public String getString(String key, String defaultObject){
        if(keyValueMap.containsKey(key)){
            if(keyValueMap.get(key) instanceof String){
                return (String) keyValueMap.get(key);
            }
        }
        return defaultObject;
    }
    public int getInt(String key, int defaultInt){
        if(keyValueMap.containsKey(key)){
            if(keyValueMap.get(key) instanceof Integer){
                return (int) keyValueMap.get(key);
            }
        }
        return defaultInt;
    }

    public void putJSONObject(String key, JSONObject defaultObject){
        if(keyValueMap.containsKey(key)){
                keyValueMap.replace(key,defaultObject);
        }
        else
        keyValueMap.put(key,defaultObject);
    }
    public void putJSONArray(String key, JSONArray defaultObject){
        if(keyValueMap.containsKey(key)){
            keyValueMap.replace(key,defaultObject);
        }else
        keyValueMap.put(key,defaultObject);
    }

    public void putString(String key, String defaultObject){
        if(keyValueMap.containsKey(key)){
            keyValueMap.replace(key,defaultObject);
        }else
        keyValueMap.put(key,defaultObject);
    }
    public void putInt(String key, int defaultInt){
        if(keyValueMap.containsKey(key)){
            keyValueMap.replace(key,defaultInt);
        }else
        keyValueMap.put(key,defaultInt);
    }

    public void put(String key, Object value){
        if(keyValueMap.containsKey(key)){
            keyValueMap.replace(key,value);
        }else
            keyValueMap.put(key,value);
    }


    private String[] splitValues(String substring) {
        String[] output = new String[substring.length()/2+1];
        int depth =0;
        int index = 0;
        for(char c : substring.toCharArray()){
            if(c == '{'||c == '[')depth++;
            if(c == '}'||c == ']')depth--;
            if(c==',' && depth==0){
                index++;
                continue;
            }
            if(output[index] == null)output[index]="";
            output[index] +=c;
        }
        return output;
    }

    public String getString(String name) {
        if(keyValueMap.get(name) instanceof String)return keyValueMap.get(name) + "";
        else throw  new IllegalArgumentException("Value to name: "+ name + " is no String!");
    }

    public boolean has(String code) {
        return keyValueMap.containsKey(code);
    }

    public int getInt(String name) {
        if(keyValueMap.get(name) instanceof Integer)return (int)keyValueMap.get(name);
        else throw  new IllegalArgumentException("Value to name: "+ name + " is no integer!");
    }

    public boolean isEmpty() {
        return keyValueMap.isEmpty();
    }

    public boolean getBoolean(String name) {
        if(keyValueMap.get(name) instanceof Boolean)return (boolean)keyValueMap.get(name);
        else throw  new IllegalArgumentException("Value to name: "+ name + " is no boolean!");
    }
    public String toString(){
        StringBuilder output = new StringBuilder("{");
        for(String key : keyValueMap.keySet()){
            Object value = keyValueMap.get(key);
            output.append(key).append(":").append(value.toString());
            output.append(",");
        }
        return output.substring(0, output.length()-1)+"}";
    }
}
