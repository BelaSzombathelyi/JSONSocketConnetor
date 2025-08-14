package com.jsonconnector;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * JSON validation utility
 */
public class JsonValidator {
    
    private static final Gson gson = new Gson();
    
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        
        try {
            gson.fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}