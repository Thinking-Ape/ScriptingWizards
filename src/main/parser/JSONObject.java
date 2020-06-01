package main.parser;

import main.utility.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static main.parser.JSONParser.JSON_ARRAY_REGEX;
import static main.parser.JSONParser.JSON_OBJECT_REGEX;

public class JSONObject {
    Map<String,Object> keyValueMap;

    public JSONObject (String pairs){
        keyValueMap = new HashMap<>();
        if(!pairs.matches(JSON_OBJECT_REGEX))throw new IllegalArgumentException(pairs + " is no JSONObject!");
        pairs = Util.removeFirstAndLast(pairs);
        List<String> keyValueList = Util.splitValues( pairs);
        for(String keyValue : keyValueList){
            String key = keyValue.replaceAll("^(\".*?\"):.*$", "$1");
            String value = keyValue.replaceAll("^\".*?\":(.*)$", "$1");
            value = value.trim();
            key = key.trim();
            if(key.equals(""))continue;
            if(!key.matches("^\".*\"$"))throw new IllegalArgumentException(key + " is no legal key value!");
            key = key.replaceAll("^\"(.*)\"$","$1");

            if(value.matches(JSON_OBJECT_REGEX)){
                if(keyValueMap.containsKey(key))
                    keyValueMap.put(key, new JSONObject(value));
            }
            else if(value.matches(JSON_ARRAY_REGEX)){
                keyValueMap.put(key, new JSONArray(value));
            }
            else if(value.matches("^\".*\"$")){
                value = value.replaceAll("^\"(.*)\"$","$1");
                value = Util.unescapeEverything(value);
                keyValueMap.put(key, value);
            }
            else if(value.matches("^[+-]?\\d+$")){
                Integer i = Integer.parseInt(value);
                keyValueMap.put(key, i);
            }
            else if(value.matches("^(true|false)$")){
                keyValueMap.put(key, (Boolean.parseBoolean(value)));
            }
            else {
                throw new IllegalArgumentException("Value "+ value +" is no valid JSON value!");
            }
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
                return /*Util.escapeEverything(*/(String) keyValueMap.get(key);
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

    public void put(String key, Object value){
        if(keyValueMap.containsKey(key)){
            keyValueMap.replace(key,value);
        }else
        {
            keyValueMap.put(key, value);
        }
    }

    public String getString(String name) {
        if(keyValueMap.get(name) instanceof String)return /*Util.escapeEverything(*/keyValueMap.get(name) + "";
        else throw  new IllegalArgumentException("Value to name: "+ name + " is no String!" + "("+keyValueMap.get(name)+")");
    }

    public boolean has(String code) {
        return keyValueMap.containsKey(code);
    }

    public int getInt(String name) {
        if(keyValueMap.get(name) instanceof Integer)return (int)keyValueMap.get(name);
        else throw  new IllegalArgumentException("Value to name: "+ name + " is no integer! ("+keyValueMap.get(name)+")");
    }

    public boolean isEmpty() {
        return keyValueMap.isEmpty();
    }

    public boolean getBoolean(String name) {
        if(keyValueMap.get(name) instanceof Boolean)return (boolean)keyValueMap.get(name);
        else throw  new IllegalArgumentException("Value to name: "+ name + " is no boolean!");
    }
    public boolean getBoolean(String name, boolean defaultBool) {
        if(keyValueMap.containsKey(name)){
            if(keyValueMap.get(name) instanceof Boolean)return (boolean)keyValueMap.get(name);
            else throw  new IllegalArgumentException("Value to name: "+ name + " is no boolean!");
        }
        else return defaultBool;
    }
    public String toString(){
        StringBuilder output = new StringBuilder("{");
        for(String key : keyValueMap.keySet()){
            Object value = keyValueMap.get(key);
            if(value instanceof String){
                value =  "\"" + Util.escapeEverything((String) value).trim() + "\"";
            }
            output.append("\"").append(key).append("\"").append(":").append(value.toString());
            output.append(",");
        }
        return output.length() > 1 ? output.substring(0, output.length()-1)+"}" :output+"}";
    }
}