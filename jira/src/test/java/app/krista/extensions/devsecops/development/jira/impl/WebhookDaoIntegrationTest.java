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

package app.krista.extensions.devsecops.development.jira.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for WebhookDao.
 * Tests all webhook parsing scenarios including edge cases.
 */
@DisplayName("WebhookDao Integration Tests")
class WebhookDaoIntegrationTest {

    private WebhookDao webhookDao;

    @BeforeEach
    void setUp() {
        webhookDao = new WebhookDao();
    }

    @Test
    @DisplayName("Should parse webhook response with issue key")
    void testParseWebhookResponseWithIssueKey() {
        String jsonString = "{\"issue\":{\"key\":\"TEST-123\",\"fields\":{\"summary\":\"Test Issue\"}}}";

        Map<String, Object> result = webhookDao.parseWebhookResponse(jsonString);

        assertNotNull(result);
        assertEquals("TEST-123", result.get("Issue Key"));
        assertTrue(result.containsKey("Update"));
    }

    @Test
    @DisplayName("Should parse webhook response without issue key")
    void testParseWebhookResponseWithoutIssueKey() {
        String jsonString = "{\"timestamp\":1234567890,\"webhookEvent\":\"jira:issue_updated\"}";

        Map<String, Object> result = webhookDao.parseWebhookResponse(jsonString);

        assertNotNull(result);
        assertEquals("", result.get("Issue Key"));
        assertTrue(result.containsKey("Update"));
    }

    @Test
    @DisplayName("Should parse complex nested webhook response")
    void testParseComplexWebhookResponse() {
        String jsonString = "{\"issue\":{\"key\":\"PROJ-456\",\"fields\":{\"summary\":\"Complex Issue\",\"status\":{\"name\":\"In Progress\"}}},\"user\":{\"name\":\"testuser\"}}";

        Map<String, Object> result = webhookDao.parseWebhookResponse(jsonString);

        assertNotNull(result);
        assertEquals("PROJ-456", result.get("Issue Key"));
        assertTrue(result.containsKey("Update"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> updates = (List<Map<String, Object>>) result.get("Update");
        assertNotNull(updates);
        assertFalse(updates.isEmpty());
    }

    @Test
    @DisplayName("Should handle null input")
    void testParseWebhookResponseWithNull() {
        Map<String, Object> result = webhookDao.parseWebhookResponse(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty string input")
    void testParseWebhookResponseWithEmptyString() {
        Map<String, Object> result = webhookDao.parseWebhookResponse("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle whitespace only input")
    void testParseWebhookResponseWithWhitespace() {
        Map<String, Object> result = webhookDao.parseWebhookResponse("   ");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty JSON object")
    void testParseEmptyJsonObject() {
        String jsonString = "{}";

        Map<String, Object> result = webhookDao.parseWebhookResponse(jsonString);

        assertNotNull(result);
        assertEquals("", result.get("Issue Key"));
    }
}