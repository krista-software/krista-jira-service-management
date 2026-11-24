/*
 * JIRA SERVICE MANAGEMENT Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.devsecops.development.jira.util;

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JiraConstants utility class.
 * Tests constant values and GSON instance.
 */
@DisplayName("JiraConstants Integration Tests")
class JiraConstantsIntegrationTest {

    @Test
    @DisplayName("Should have valid GSON instance")
    void testGsonInstance() {
        assertNotNull(JiraConstants.GSON);
        assertTrue(JiraConstants.GSON instanceof Gson);
    }

    @Test
    @DisplayName("Should have correct NA constant")
    void testNAConstant() {
        assertEquals("NA", JiraConstants.NA);
    }

    @Test
    @DisplayName("Should have correct VALUE constant")
    void testValueConstant() {
        assertEquals("value", JiraConstants.VALUE);
    }

    @Test
    @DisplayName("GSON should serialize objects correctly")
    void testGsonSerialization() {
        TestObject obj = new TestObject("test", 123);
        String json = JiraConstants.GSON.toJson(obj);
        
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("\"value\":123"));
    }

    @Test
    @DisplayName("GSON should deserialize objects correctly")
    void testGsonDeserialization() {
        String json = "{\"name\":\"test\",\"value\":456}";
        TestObject obj = JiraConstants.GSON.fromJson(json, TestObject.class);
        
        assertNotNull(obj);
        assertEquals("test", obj.name);
        assertEquals(456, obj.value);
    }

    @Test
    @DisplayName("GSON should handle null values")
    void testGsonNullHandling() {
        TestObject obj = new TestObject(null, 0);
        String json = JiraConstants.GSON.toJson(obj);
        
        assertNotNull(json);
        // Gson by default doesn't serialize null fields
        assertFalse(json.contains("null"));
    }

    @Test
    @DisplayName("GSON should handle arrays")
    void testGsonArrayHandling() {
        String[] array = {"one", "two", "three"};
        String json = JiraConstants.GSON.toJson(array);
        
        assertNotNull(json);
        assertTrue(json.contains("one"));
        assertTrue(json.contains("two"));
        assertTrue(json.contains("three"));
    }

    @Test
    @DisplayName("NA constant should be used for missing values")
    void testNAUsage() {
        String description = null;
        String displayValue = description != null ? description : JiraConstants.NA;

        assertEquals("NA", displayValue);
    }

    // Helper class for testing
    private static class TestObject {
        String name;
        int value;

        TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}