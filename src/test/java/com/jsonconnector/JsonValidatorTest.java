package com.jsonconnector;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JsonValidatorTest {

    @Test
    public void testValidJson() {
        assertTrue(JsonValidator.isValidJson("{\"test\": \"value\"}"));
        assertTrue(JsonValidator.isValidJson("[1,2,3]"));
        assertTrue(JsonValidator.isValidJson("\"simple string\""));
        assertTrue(JsonValidator.isValidJson("123"));
        assertTrue(JsonValidator.isValidJson("true"));
    }

    @Test
    public void testInvalidJson() {
        assertFalse(JsonValidator.isValidJson(""));
        assertFalse(JsonValidator.isValidJson(null));
        assertFalse(JsonValidator.isValidJson("{invalid json}"));
        assertFalse(JsonValidator.isValidJson("[1,2,3"));
        assertFalse(JsonValidator.isValidJson("{\"unclosed\": \"string"));
    }
}