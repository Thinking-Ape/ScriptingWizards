package main.parser;

import main.utility.Util;

import java.util.ArrayList;
import java.util.List;
import static main.parser.JSONParser.JSON_ARRAY_REGEX;
import static main.parser.JSONParser.JSON_OBJECT_REGEX;

public class JSONArray {
    List<Object> keyList;


    public JSONArray() {
        keyList = new ArrayList<>();
    }
    public JSONArray(String pairs) {
        keyList = new ArrayList<>();
        if (!pairs.matches(JSON_ARRAY_REGEX)) throw new IllegalArgumentException(pairs + " is no JSONArray!");
        String[] valueList = JSONParser.splitValues(Util.removeFirstAndLast(pairs));
        for (String value : valueList) {
            value = value.trim();
            if (value.matches(JSON_OBJECT_REGEX)) {
                keyList.add(new JSONObject(value));
            } else if (value.matches(JSON_ARRAY_REGEX)) {
                keyList.add(new JSONArray(value));
            } else if (value.matches("^\".*\"$")) {
                keyList.add(value);
            } else if (value.matches("^\\d+$")) {
                keyList.add(Integer.parseInt(value));
            } else if (value.matches("^(true|false)$")) {
                keyList.add(Boolean.parseBoolean(value));
            } else throw new IllegalArgumentException("Value " + value + " is no valid JSON value!");
        }
    }

    public JSONObject getJSONObject(int index, JSONObject defaultObject){
        if(keyList.size() > index){
            if(keyList.get(index) instanceof JSONObject){
                return (JSONObject) keyList.get(index);
            }
        }
        return defaultObject;
    }
    public JSONArray getJSONArray(int index, JSONArray defaultObject){
        if(keyList.size() > index){
            if(keyList.get(index) instanceof JSONArray){
                return (JSONArray) keyList.get(index);
            }
        }
        return defaultObject;
    }

    public String getString(int index, String defaultObject){
        if(keyList.size() > index){
            if(keyList.get(index) instanceof String){
                return (String) keyList.get(index);
            }
        }
        return defaultObject;
    }
    public int getInt(int index, int defaultInt){
        if(keyList.size() > index){
            if(keyList.get(index) instanceof Integer){
                return (int) keyList.get(index);
            }
        }
        return defaultInt;
    }

    public void putJSONObject(int index, JSONObject defaultObject){
        if(keyList.size() > index){
            keyList.add(index,defaultObject);
        }
        else keyList.add(defaultObject);
    }
    public void putJSONArray(int index, JSONArray defaultObject){
        if(keyList.size() > index){
            keyList.add(index,defaultObject);
        }
        else keyList.add(defaultObject);
    }

    public void putString(int index, String defaultObject){
        if(keyList.size() > index){
            keyList.add(index,defaultObject);
        }
        else keyList.add(defaultObject);
    }
    public void putInt(int index, int defaultInt){
        if(keyList.size() > index){
            keyList.add(index,defaultInt);
        }
        else keyList.add(defaultInt);
    }
    public void putJSONObject(JSONObject defaultObject){
        keyList.add(defaultObject);
    }
    public void putJSONArray(JSONArray defaultObject){
        keyList.add(defaultObject);
    }

    public void putString(String defaultObject){
        keyList.add(defaultObject);
    }
    public void putInt(int defaultInt){
        keyList.add(defaultInt);
    }

    public void put(Object o){
        keyList.add(o);
    }

    public void put(int index, Object o){
        if(index < keyList.size())
        keyList.add(index,o);
        else keyList.add(o);
    }

    public int length() {
        return keyList.size();
    }

    public JSONObject getJSONObject(int i) {
        if(keyList.get(i) instanceof JSONObject)return (JSONObject) keyList.get(i);
        else throw new IllegalArgumentException(keyList.get(i)+ " is no JSONObject!");
    }

    public void remove(int i) {
        if(i < keyList.size())keyList.remove(i);
    }

    public Object get(int i) {
        if(i >= keyList.size())throw new IllegalArgumentException("Int " + i + " is out of range!");
        return keyList.get(i);
    }

    public JSONArray getJSONArray(int i) {
        if(keyList.get(i) instanceof JSONArray)return (JSONArray) keyList.get(i);
        else throw new IllegalArgumentException(keyList.get(i)+ " is no JSONArray!");
    }

    public String getString(int i) {

        if(keyList.get(i) instanceof String)return (String) keyList.get(i);
        else throw new IllegalArgumentException(keyList.get(i)+ " is no String!");
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder("[");

        for(Object value : keyList){
            output.append(value.toString());
            output.append(",");
        }
        return output.substring(0, output.length()-1)+"]";
    }
}