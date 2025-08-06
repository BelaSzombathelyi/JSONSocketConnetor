package com.jsonconnector;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * JSON formatting and parsing utilities
 */
public class JsonFormatter {
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public static String format(String json) {
        try {
            JsonElement element = JsonParser.parseString(json);
            return gson.toJson(element);
        } catch (Exception e) {
            return json; // Return original if formatting fails
        }
    }
    
    public static Map<String, Object> parseToMap(String json) {
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        return gson.fromJson(json, type);
    }
}