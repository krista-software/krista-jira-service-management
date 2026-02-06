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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for FieldValidator utility class.
 * Tests field validation and default value handling.
 */
@DisplayName("FieldValidator Integration Tests")
class FieldValidatorIntegrationTest {

    @Test
    @DisplayName("Should return empty string for null input")
    void testGetNullOrEmptyDefault_Null() {
        String result = FieldValidator.getNullOrEmptyDefault(null);
        
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should return same string for non-null input")
    void testGetNullOrEmptyDefault_NonNull() {
        String input = "test value";
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        assertEquals("test value", result);
    }

    @Test
    @DisplayName("Should return empty string for empty input")
    void testGetNullOrEmptyDefault_Empty() {
        String result = FieldValidator.getNullOrEmptyDefault("");
        
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should return whitespace string as-is")
    void testGetNullOrEmptyDefault_Whitespace() {
        String input = "   ";
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        assertEquals("   ", result);
    }

    @Test
    @DisplayName("Should handle special characters")
    void testGetNullOrEmptyDefault_SpecialCharacters() {
        String input = "!@#$%^&*()";
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        assertEquals("!@#$%^&*()", result);
    }

    @Test
    @DisplayName("Should handle unicode characters")
    void testGetNullOrEmptyDefault_Unicode() {
        String input = "测试 テスト 🎉";
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        assertEquals("测试 テスト 🎉", result);
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testGetNullOrEmptyDefault_LongString() {
        String input = "a".repeat(10000);
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        assertEquals(input, result);
        assertEquals(10000, result.length());
    }
}