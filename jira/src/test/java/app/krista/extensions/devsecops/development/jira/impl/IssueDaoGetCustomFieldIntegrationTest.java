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

import app.krista.extensions.devsecops.development.jira.BaseIntegrationTest;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for IssueDao custom field methods:
 * - getCustomIssue() — retrieves custom field values from a Jira issue
 * - getKeyByName() — resolves a field name to its Jira field key
 * - extractStringFromJsonObject() — (private, tested indirectly via getCustomIssue)
 */
@DisplayName("IssueDao Custom Field Integration Tests")
class IssueDaoGetCustomFieldIntegrationTest extends BaseIntegrationTest {

    @Mock
    private RestClient mockRestClient;

    private IssueDao issueDao;

    private static final String ISSUE_KEY = "TEST-123";
    private static final String CUSTOM_FIELD_NAME = "Components";
    private static final String CUSTOM_FIELD_KEY = "customfield_10001";

    @BeforeEach
    void setUp() {
        mockRestClient = mock(RestClient.class);
        when(restClientFactory.getInstance()).thenReturn(mockRestClient);
        when(restClientFactory.invokeAsUser()).thenReturn(false);
        issueDao = new IssueDao(restClientFactory, mockRestClient);
    }

    // ========================================================================
    // getKeyByName() tests
    // ========================================================================

    @Nested
    @DisplayName("getKeyByName")
    class GetKeyByNameTests {

        @Test
        @DisplayName("Should resolve field name to key")
        void testGetKeyByName_matchFound() {
            String fieldJson = createFieldMetadataJson("Components", "components",
                    "{\"type\":\"array\",\"items\":\"component\",\"system\":\"components\"}");

            String result = IssueDao.getKeyByName(fieldJson, "Components");

            assertEquals("components", result);
        }

        @Test
        @DisplayName("Should resolve field name case-insensitively")
        void testGetKeyByName_caseInsensitive() {
            String fieldJson = createFieldMetadataJson("Project Owner", "customfield_10050",
                    "{\"type\":\"user\"}");

            String result = IssueDao.getKeyByName(fieldJson, "project owner");

            assertEquals("customfield_10050", result);
        }

        @Test
        @DisplayName("Should return null when field name not found")
        void testGetKeyByName_noMatch() {
            String fieldJson = createFieldMetadataJson("Summary", "summary", "{\"type\":\"string\"}");

            String result = IssueDao.getKeyByName(fieldJson, "NonExistentField");

            assertNull(result);
        }

        @Test
        @DisplayName("Should handle empty field array")
        void testGetKeyByName_emptyArray() {
            String result = IssueDao.getKeyByName("[]", "Components");

            assertNull(result);
        }

        @Test
        @DisplayName("Should return first match from multiple fields")
        void testGetKeyByName_multipleFields() {
            String fieldJson = "[" +
                    "{\"name\":\"Summary\",\"key\":\"summary\",\"schema\":{\"type\":\"string\"}}," +
                    "{\"name\":\"Components\",\"key\":\"components\",\"schema\":{\"type\":\"array\"}}," +
                    "{\"name\":\"Priority\",\"key\":\"priority\",\"schema\":{\"type\":\"priority\"}}" +
                    "]";

            String result = IssueDao.getKeyByName(fieldJson, "Components");

            assertEquals("components", result);
        }

        @Test
        @DisplayName("Should handle field without schema")
        void testGetKeyByName_noSchema() {
            String fieldJson = "[{\"name\":\"Custom\",\"key\":\"customfield_99\"}]";

            String result = IssueDao.getKeyByName(fieldJson, "Custom");

            assertEquals("customfield_99", result);
        }
    }

    // ========================================================================
    // getCustomIssue() tests
    // ========================================================================

    @Nested
    @DisplayName("getCustomIssue")
    class GetCustomIssueTests {

        @Test
        @DisplayName("Should return empty list when field name not found in metadata")
        void testGetCustomIssue_fieldNotFound() {
            mockFieldResponse("Summary", "summary", "{\"type\":\"string\"}");

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, "NonExistentField");

            assertEquals(0, result.size());
            // Should NOT call issue endpoint since field key is null
            verify(mockRestClient, never()).fetch(startsWith("issue/"), any(HashMap.class), eq(String.class));
        }

        @Test
        @DisplayName("Should return empty list when field value is null in issue")
        void testGetCustomIssue_fieldValueNull() {
            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"array\"}");
            mockIssueResponseWithNullField(ISSUE_KEY, CUSTOM_FIELD_KEY);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertTrue(result.isEmpty());
        }

        // --- JsonObject field value tests (dropdown selects, people fields) ---

        @Test
        @DisplayName("Should extract 'value' from JsonObject (dropdown select)")
        void testGetCustomIssue_jsonObjectWithValue() {
            JsonObject fieldValue = new JsonObject();
            fieldValue.addProperty("self", "https://test.atlassian.net/rest/api/3/customFieldOption/10001");
            fieldValue.addProperty("value", "Option A");
            fieldValue.addProperty("id", "10001");

            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"option\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertEquals(1, result.size());
            assertEquals("Option A", result.get(0));
        }

        @Test
        @DisplayName("Should extract 'displayName' from JsonObject (people selector)")
        void testGetCustomIssue_jsonObjectWithDisplayName() {
            JsonObject fieldValue = new JsonObject();
            fieldValue.addProperty("accountId", "712020:abc123");
            fieldValue.addProperty("displayName", "John Doe");
            fieldValue.addProperty("emailAddress", "john@example.com");
            fieldValue.addProperty("active", true);

            mockFieldResponse("Project Owner", CUSTOM_FIELD_KEY, "{\"type\":\"user\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, "Project Owner");

            assertEquals(1, result.size());
            assertEquals("John Doe", result.get(0));
        }

        @Test
        @DisplayName("Should extract 'name' from JsonObject when no value or displayName")
        void testGetCustomIssue_jsonObjectWithNameOnly() {
            JsonObject fieldValue = new JsonObject();
            fieldValue.addProperty("self", "https://test.atlassian.net/rest/api/3/priority/3");
            fieldValue.addProperty("id", "3");
            fieldValue.addProperty("name", "Medium");

            mockFieldResponse("Priority", CUSTOM_FIELD_KEY, "{\"type\":\"priority\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, "Priority");

            assertEquals(1, result.size());
            assertEquals("Medium", result.get(0));
        }

        @Test
        @DisplayName("Should prefer 'value' over 'displayName' and 'name'")
        void testGetCustomIssue_jsonObjectPriorityOrder() {
            JsonObject fieldValue = new JsonObject();
            fieldValue.addProperty("value", "ValueField");
            fieldValue.addProperty("displayName", "DisplayNameField");
            fieldValue.addProperty("name", "NameField");

            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"option\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertEquals(1, result.size());
            assertEquals("ValueField", result.get(0));
        }

        @Test
        @DisplayName("Should return empty list when JsonObject has no extractable keys")
        void testGetCustomIssue_jsonObjectNoExtractableKeys() {
            JsonObject fieldValue = new JsonObject();
            fieldValue.addProperty("id", "10001");
            fieldValue.addProperty("self", "https://test.atlassian.net/rest/api/3/something/10001");

            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"unknown\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle JsonObject with null 'value' key, fall back to displayName")
        void testGetCustomIssue_jsonObjectNullValue() {
            JsonObject fieldValue = new JsonObject();
            fieldValue.add("value", JsonNull.INSTANCE);
            fieldValue.addProperty("displayName", "Fallback Name");

            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"option\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertEquals(1, result.size());
            assertEquals("Fallback Name", result.get(0));
        }

        // --- JsonArray field value tests (components, labels, multi-select) ---

        @Test
        @DisplayName("Should extract 'name' from JsonArray of objects (Components)")
        void testGetCustomIssue_jsonArrayOfObjectsWithName() {
            JsonArray fieldValue = new JsonArray();
            fieldValue.add(createComponentObject("Active Directory", "10000"));
            fieldValue.add(createComponentObject("Email Service", "10001"));

            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"array\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertEquals(2, result.size());
            assertEquals("Active Directory", result.get(0));
            assertEquals("Email Service", result.get(1));
        }

        @Test
        @DisplayName("Should extract strings from JsonArray of string primitives (Labels)")
        void testGetCustomIssue_jsonArrayOfStrings() {
            JsonArray fieldValue = new JsonArray();
            fieldValue.add("bug");
            fieldValue.add("critical");
            fieldValue.add("production");

            mockFieldResponse("Labels", CUSTOM_FIELD_KEY, "{\"type\":\"array\",\"items\":\"string\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, "Labels");

            assertEquals(3, result.size());
            assertEquals("bug", result.get(0));
            assertEquals("critical", result.get(1));
            assertEquals("production", result.get(2));
        }

        @Test
        @DisplayName("Should handle empty JsonArray")
        void testGetCustomIssue_emptyJsonArray() {
            JsonArray fieldValue = new JsonArray();

            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"array\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should handle JsonArray with mixed object types")
        void testGetCustomIssue_jsonArrayMixedTypes() {
            JsonArray fieldValue = new JsonArray();
            fieldValue.add("plain-string");
            fieldValue.add(createComponentObject("Component One", "10000"));

            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"array\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertEquals(2, result.size());
            assertEquals("plain-string", result.get(0));
            assertEquals("Component One", result.get(1));
        }

        @Test
        @DisplayName("Should extract 'displayName' from JsonArray of user objects")
        void testGetCustomIssue_jsonArrayOfUserObjects() {
            JsonArray fieldValue = new JsonArray();

            JsonObject user1 = new JsonObject();
            user1.addProperty("accountId", "user1");
            user1.addProperty("displayName", "Alice Smith");
            fieldValue.add(user1);

            JsonObject user2 = new JsonObject();
            user2.addProperty("accountId", "user2");
            user2.addProperty("displayName", "Bob Jones");
            fieldValue.add(user2);

            mockFieldResponse("Watchers", CUSTOM_FIELD_KEY, "{\"type\":\"array\",\"items\":\"user\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, "Watchers");

            assertEquals(2, result.size());
            assertEquals("Alice Smith", result.get(0));
            assertEquals("Bob Jones", result.get(1));
        }

        @Test
        @DisplayName("Should skip unextractable objects in JsonArray")
        void testGetCustomIssue_jsonArrayWithUnextractableObjects() {
            JsonArray fieldValue = new JsonArray();
            fieldValue.add(createComponentObject("Good Component", "10000"));

            JsonObject badObj = new JsonObject();
            badObj.addProperty("id", "10001");
            badObj.addProperty("self", "https://test.atlassian.net/something");
            fieldValue.add(badObj);

            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"array\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertEquals(1, result.size());
            assertEquals("Good Component", result.get(0));
        }

        @Test
        @DisplayName("Should handle numeric primitives in JsonArray")
        void testGetCustomIssue_jsonArrayOfNumbers() {
            JsonArray fieldValue = new JsonArray();
            fieldValue.add(42);
            fieldValue.add(100);

            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"array\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertEquals(2, result.size());
            assertEquals("42", result.get(0));
            assertEquals("100", result.get(1));
        }

        // --- JsonPrimitive field value tests ---

        @Test
        @DisplayName("Should extract string from JsonPrimitive")
        void testGetCustomIssue_stringPrimitive() {
            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"string\"}");
            mockIssueResponseWithPrimitiveField(ISSUE_KEY, CUSTOM_FIELD_KEY, "Simple text value");

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            assertEquals(1, result.size());
            assertEquals("Simple text value", result.get(0));
        }

        @Test
        @DisplayName("Should return empty list for non-string primitive (number)")
        void testGetCustomIssue_numberPrimitive() {
            mockFieldResponse(CUSTOM_FIELD_NAME, CUSTOM_FIELD_KEY, "{\"type\":\"number\"}");
            mockIssueResponseWithNumericField(ISSUE_KEY, CUSTOM_FIELD_KEY, 42.5);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, CUSTOM_FIELD_NAME);

            // Non-string primitives are not extracted by the string primitive branch
            assertTrue(result.isEmpty());
        }

        // --- Real-world scenario: KE-2943 Components bug ---

        @Test
        @DisplayName("KE-2943: Should extract component names from real Jira Components response")
        void testGetCustomIssue_realWorldComponentsResponse() {
            JsonArray components = new JsonArray();

            JsonObject comp1 = new JsonObject();
            comp1.addProperty("self", "https://antbrains.atlassian.net/rest/api/3/component/10000");
            comp1.addProperty("id", "10000");
            comp1.addProperty("name", "Active Directory");
            comp1.addProperty("description", "Created by Jira Service Management");
            components.add(comp1);

            JsonObject comp2 = new JsonObject();
            comp2.addProperty("self", "https://antbrains.atlassian.net/rest/api/3/component/10001");
            comp2.addProperty("id", "10001");
            comp2.addProperty("name", "VPN Access");
            components.add(comp2);

            mockFieldResponse("Components", "components",
                    "{\"type\":\"array\",\"items\":\"component\",\"system\":\"components\"}");
            mockIssueResponseWithField(ISSUE_KEY, "components", components);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, "Components");

            assertEquals(2, result.size());
            assertEquals("Active Directory", result.get(0));
            assertEquals("VPN Access", result.get(1));
        }

        @Test
        @DisplayName("Should handle JsonArray of multi-select option objects with 'value' key")
        void testGetCustomIssue_jsonArrayOfOptionObjects() {
            JsonArray fieldValue = new JsonArray();

            JsonObject opt1 = new JsonObject();
            opt1.addProperty("self", "https://test.atlassian.net/rest/api/3/customFieldOption/10100");
            opt1.addProperty("value", "Option X");
            opt1.addProperty("id", "10100");
            fieldValue.add(opt1);

            JsonObject opt2 = new JsonObject();
            opt2.addProperty("self", "https://test.atlassian.net/rest/api/3/customFieldOption/10101");
            opt2.addProperty("value", "Option Y");
            opt2.addProperty("id", "10101");
            fieldValue.add(opt2);

            mockFieldResponse("Multi Select", CUSTOM_FIELD_KEY, "{\"type\":\"array\",\"items\":\"option\"}");
            mockIssueResponseWithField(ISSUE_KEY, CUSTOM_FIELD_KEY, fieldValue);

            List<String> result = issueDao.getCustomIssue(ISSUE_KEY, "Multi Select");

            assertEquals(2, result.size());
            assertEquals("Option X", result.get(0));
            assertEquals("Option Y", result.get(1));
        }
    }

    // ========================================================================
    // Helper methods
    // ========================================================================

    /**
     * Creates a JSON string representing Jira field metadata array with a single field.
     */
    private String createFieldMetadataJson(String name, String key, String schema) {
        if (schema != null) {
            return "[{\"name\":\"" + name + "\",\"key\":\"" + key + "\",\"schema\":" + schema + "}]";
        }
        return "[{\"name\":\"" + name + "\",\"key\":\"" + key + "\"}]";
    }

    /**
     * Mocks the field metadata endpoint to return a single field definition.
     */
    private void mockFieldResponse(String fieldName, String fieldKey, String schema) {
        String fieldJson = createFieldMetadataJson(fieldName, fieldKey, schema);
        when(mockRestClient.fetch(eq("field"), any(Map.class), eq(String.class)))
                .thenReturn(fieldJson);
    }

    /**
     * Mocks the issue endpoint to return an issue with the given field set to a JsonElement.
     */
    private void mockIssueResponseWithField(String issueKey, String fieldKey, com.google.gson.JsonElement fieldValue) {
        JsonObject issue = new JsonObject();
        JsonObject fields = new JsonObject();
        fields.add(fieldKey, fieldValue);
        issue.add("fields", fields);

        when(mockRestClient.fetch(eq("issue/" + issueKey), any(HashMap.class), eq(String.class)))
                .thenReturn(issue.toString());
    }

    /**
     * Mocks the issue endpoint to return an issue with the given field set to null.
     */
    private void mockIssueResponseWithNullField(String issueKey, String fieldKey) {
        JsonObject issue = new JsonObject();
        JsonObject fields = new JsonObject();
        fields.add(fieldKey, JsonNull.INSTANCE);
        issue.add("fields", fields);

        when(mockRestClient.fetch(eq("issue/" + issueKey), any(HashMap.class), eq(String.class)))
                .thenReturn(issue.toString());
    }

    /**
     * Mocks the issue endpoint to return an issue with a string primitive field value.
     */
    private void mockIssueResponseWithPrimitiveField(String issueKey, String fieldKey, String value) {
        JsonObject issue = new JsonObject();
        JsonObject fields = new JsonObject();
        fields.addProperty(fieldKey, value);
        issue.add("fields", fields);

        when(mockRestClient.fetch(eq("issue/" + issueKey), any(HashMap.class), eq(String.class)))
                .thenReturn(issue.toString());
    }

    /**
     * Mocks the issue endpoint to return an issue with a numeric primitive field value.
     */
    private void mockIssueResponseWithNumericField(String issueKey, String fieldKey, double value) {
        JsonObject issue = new JsonObject();
        JsonObject fields = new JsonObject();
        fields.addProperty(fieldKey, value);
        issue.add("fields", fields);

        when(mockRestClient.fetch(eq("issue/" + issueKey), any(HashMap.class), eq(String.class)))
                .thenReturn(issue.toString());
    }

    /**
     * Creates a Jira component-style JsonObject with name and id.
     */
    private JsonObject createComponentObject(String name, String id) {
        JsonObject component = new JsonObject();
        component.addProperty("self", "https://test.atlassian.net/rest/api/3/component/" + id);
        component.addProperty("id", id);
        component.addProperty("name", name);
        component.addProperty("description", "Created by Jira Service Management");
        return component;
    }
}
