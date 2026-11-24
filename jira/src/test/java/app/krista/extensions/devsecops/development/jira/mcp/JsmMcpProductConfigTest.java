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

package app.krista.extensions.devsecops.development.jira.mcp;

import app.krista.model.base.FreeForm;
import app.krista.sdk.atlassian.mcp.McpProductConfig;
import app.krista.sdk.atlassian.mcp.McpSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsmMcpProductConfig")
class JsmMcpProductConfigTest {

    private JsmMcpProductConfig config;

    @BeforeEach
    void setUp() {
        config = new JsmMcpProductConfig();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for basic product configuration methods
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("product configuration")
    class ProductConfigurationTests {

        @Test
        @DisplayName("productName should return 'Jira Service Management'")
        void shouldReturnJsmAsProductName() {
            assertEquals("Jira Service Management", config.productName());
        }

        @Test
        @DisplayName("queryLanguage should return 'JQL'")
        void shouldReturnJqlAsQueryLanguage() {
            assertEquals("JQL", config.queryLanguage());
        }

        @Test
        @DisplayName("queryLanguageFullName should return 'JQL (Jira Query Language)'")
        void shouldReturnFullJqlName() {
            assertEquals("JQL (Jira Query Language)", config.queryLanguageFullName());
        }

        @Test
        @DisplayName("searchHttpMethod should return 'POST'")
        void shouldReturnPostAsSearchMethod() {
            assertEquals("POST", config.searchHttpMethod());
        }

        @Test
        @DisplayName("basicSearchPath should return '/search/jql'")
        void shouldReturnCorrectBasicSearchPath() {
            assertEquals("/search/jql", config.basicSearchPath());
        }

        @Test
        @DisplayName("searchUrlTemplate should contain cloud ID placeholder and jira path")
        void shouldContainCloudIdPlaceholder() {
            String template = config.searchUrlTemplate();

            assertNotNull(template);
            assertTrue(template.contains("%s"), "URL template should have a cloud ID placeholder");
            assertTrue(template.contains("jira"), "URL template should reference jira");
            assertTrue(template.contains("search/jql"), "URL template should target search/jql");
            assertEquals("https://api.atlassian.com/ex/jira/%s/rest/api/3/search/jql", template);
        }

        @Test
        @DisplayName("userSearchPath should return '/user/search'")
        void shouldReturnUserSearchPath() {
            assertEquals("/user/search", config.userSearchPath());
        }

        @Test
        @DisplayName("oauthApiBaseTemplate should return the Jira cloud API base URL template")
        void shouldReturnOauthApiBaseTemplate() {
            String template = config.oauthApiBaseTemplate();

            assertNotNull(template);
            assertEquals("https://api.atlassian.com/ex/jira/%s/rest/api/3", template);
            assertTrue(template.contains("%s"), "Template should have cloud ID placeholder");
        }

        @Test
        @DisplayName("standardFields should include expected Jira fields")
        void shouldReturnStandardFields() {
            List<String> fields = config.standardFields();

            assertNotNull(fields);
            assertEquals(6, fields.size());
            assertTrue(fields.contains("summary"));
            assertTrue(fields.contains("status"));
            assertTrue(fields.contains("assignee"));
            assertTrue(fields.contains("priority"));
            assertTrue(fields.contains("issuetype"));
            assertTrue(fields.contains("project"));
        }

        @Test
        @DisplayName("standardFields should be immutable")
        void standardFieldsShouldBeImmutable() {
            List<String> fields = config.standardFields();

            assertThrows(UnsupportedOperationException.class,
                    () -> fields.add("extra"));
        }

        @Test
        @DisplayName("resultColumns should include expected column definitions")
        void shouldReturnResultColumns() {
            List<McpProductConfig.ColumnDef> columns = config.resultColumns();

            assertNotNull(columns);
            assertEquals(6, columns.size());
            assertTrue(columns.stream().anyMatch(c -> "key".equals(c.key())));
            assertTrue(columns.stream().anyMatch(c -> "type".equals(c.key())));
            assertTrue(columns.stream().anyMatch(c -> "summary".equals(c.key())));
            assertTrue(columns.stream().anyMatch(c -> "status".equals(c.key())));
            assertTrue(columns.stream().anyMatch(c -> "priority".equals(c.key())));
            assertTrue(columns.stream().anyMatch(c -> "assignee".equals(c.key())));
        }

        @Test
        @DisplayName("key column should have 'link' type")
        void shouldHaveLinkTypeForKeyColumn() {
            McpProductConfig.ColumnDef keyColumn = config.resultColumns().stream()
                    .filter(c -> "key".equals(c.key()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(keyColumn);
            assertEquals("Key", keyColumn.label());
            assertEquals("link", keyColumn.type());
        }

        @Test
        @DisplayName("type column should have 'pill' type")
        void shouldHavePillTypeForTypeColumn() {
            McpProductConfig.ColumnDef typeColumn = config.resultColumns().stream()
                    .filter(c -> "type".equals(c.key()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(typeColumn);
            assertEquals("Type", typeColumn.label());
            assertEquals("pill", typeColumn.type());
        }

        @Test
        @DisplayName("summary column should have 'text' type")
        void shouldHaveTextTypeForSummaryColumn() {
            McpProductConfig.ColumnDef summaryColumn = config.resultColumns().stream()
                    .filter(c -> "summary".equals(c.key()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(summaryColumn);
            assertEquals("Summary", summaryColumn.label());
            assertEquals("text", summaryColumn.type());
        }

        @Test
        @DisplayName("status column should have 'pill' type")
        void shouldHavePillTypeForStatusColumn() {
            McpProductConfig.ColumnDef statusColumn = config.resultColumns().stream()
                    .filter(c -> "status".equals(c.key()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(statusColumn);
            assertEquals("Status", statusColumn.label());
            assertEquals("pill", statusColumn.type());
        }

        @Test
        @DisplayName("priority column should have 'pill' type")
        void shouldHavePillTypeForPriorityColumn() {
            McpProductConfig.ColumnDef priorityColumn = config.resultColumns().stream()
                    .filter(c -> "priority".equals(c.key()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(priorityColumn);
            assertEquals("Priority", priorityColumn.label());
            assertEquals("pill", priorityColumn.type());
        }

        @Test
        @DisplayName("assignee column should have 'text' type")
        void shouldHaveTextTypeForAssigneeColumn() {
            McpProductConfig.ColumnDef assigneeColumn = config.resultColumns().stream()
                    .filter(c -> "assignee".equals(c.key()))
                    .findFirst()
                    .orElse(null);

            assertNotNull(assigneeColumn);
            assertEquals("Assignee", assigneeColumn.label());
            assertEquals("text", assigneeColumn.type());
        }

        @Test
        @DisplayName("resultColumns should be immutable")
        void resultColumnsShouldBeImmutable() {
            List<McpProductConfig.ColumnDef> columns = config.resultColumns();

            assertThrows(UnsupportedOperationException.class,
                    () -> columns.add(new McpProductConfig.ColumnDef("extra", "Extra", "text")));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for prompt generation
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("prompt generation")
    class PromptGenerationTests {

        @Test
        @DisplayName("queryGenerationPrompt should return non-empty string")
        void shouldReturnNonEmptyQueryPrompt() {
            String prompt = config.queryGenerationPrompt("Show all open incidents");

            assertNotNull(prompt);
            assertFalse(prompt.isEmpty());
        }

        @Test
        @DisplayName("queryGenerationPrompt should include the question")
        void shouldIncludeQuestionInQueryPrompt() {
            String question = "Show all open incidents";
            String prompt = config.queryGenerationPrompt(question);

            assertTrue(prompt.contains(question),
                    "Query prompt should include the user's question");
        }

        @Test
        @DisplayName("queryGenerationPrompt should reference JQL")
        void shouldReferenceJqlInQueryPrompt() {
            String prompt = config.queryGenerationPrompt("test question");

            assertTrue(prompt.contains("JQL"),
                    "Query prompt should reference JQL query language");
        }

        @Test
        @DisplayName("queryGenerationPrompt should mention JSON output format")
        void shouldMentionJsonOutputFormat() {
            String prompt = config.queryGenerationPrompt("test question");

            assertTrue(prompt.contains("JSON"),
                    "Query prompt should mention JSON output format");
        }

        @Test
        @DisplayName("queryGenerationPrompt should reference Jira Service Management")
        void shouldReferenceJsmInQueryPrompt() {
            String prompt = config.queryGenerationPrompt("test question");

            assertTrue(prompt.contains("Jira Service Management"),
                    "Query prompt should reference Jira Service Management");
        }

        @Test
        @DisplayName("queryGenerationPrompt should mention service request(s)")
        void shouldMentionServiceRequests() {
            String prompt = config.queryGenerationPrompt("test question");

            assertTrue(prompt.contains("service request(s)"),
                    "Query prompt should mention service request(s)");
        }

        @Test
        @DisplayName("queryGenerationPrompt should include examples with JSM keys")
        void shouldIncludeExamplesWithJsmKeys() {
            String prompt = config.queryGenerationPrompt("test question");

            assertTrue(prompt.contains("JSM-100"), "Should include JSM-100 example");
            assertTrue(prompt.contains("JSM-200"), "Should include JSM-200 example");
        }

        @Test
        @DisplayName("queryGenerationPrompt should mention currentUser()")
        void shouldMentionCurrentUser() {
            String prompt = config.queryGenerationPrompt("test question");

            assertTrue(prompt.contains("currentUser()"),
                    "Should mention currentUser() function");
        }

        @Test
        @DisplayName("queryGenerationPrompt should include specific/field JSON fields")
        void shouldIncludeSpecificAndFieldJsonKeys() {
            String prompt = config.queryGenerationPrompt("test question");

            assertTrue(prompt.contains("\"query\""), "Should mention query JSON field");
            assertTrue(prompt.contains("\"specific\""), "Should mention specific JSON field");
            assertTrue(prompt.contains("\"field\""), "Should mention field JSON field");
        }

        @Test
        @DisplayName("answerExtractionPrompt should return non-empty string")
        void shouldReturnNonEmptyAnswerPrompt() {
            String prompt = config.answerExtractionPrompt("What is the priority?", "some data");

            assertNotNull(prompt);
            assertFalse(prompt.isEmpty());
        }

        @Test
        @DisplayName("answerExtractionPrompt should include the question and data")
        void shouldIncludeQuestionAndDataInAnswerPrompt() {
            String question = "What is the priority of JSM-100?";
            String data = "{\"key\":\"JSM-100\",\"priority\":\"High\"}";
            String prompt = config.answerExtractionPrompt(question, data);

            assertTrue(prompt.contains(question),
                    "Answer prompt should include the user's question");
            assertTrue(prompt.contains(data),
                    "Answer prompt should include the issue data");
        }

        @Test
        @DisplayName("answerExtractionPrompt should reference Jira Service Management")
        void shouldReferenceJsmInAnswerPrompt() {
            String prompt = config.answerExtractionPrompt("test", "data");

            assertTrue(prompt.contains("Jira Service Management"),
                    "Answer prompt should reference Jira Service Management");
        }

        @Test
        @DisplayName("answerExtractionPrompt should include JSM data label")
        void shouldIncludeJsmDataLabel() {
            String prompt = config.answerExtractionPrompt("test", "data");

            assertTrue(prompt.contains("JSM data:"),
                    "Answer prompt should include JSM data label");
        }

        @Test
        @DisplayName("answerExtractionPrompt should include formatting rules")
        void shouldIncludeFormattingRules() {
            String prompt = config.answerExtractionPrompt("test", "data");

            assertTrue(prompt.contains("Rules:"), "Should include Rules section");
            assertTrue(prompt.contains("one or two sentences"), "Should mention concise answers");
            assertTrue(prompt.contains("Do NOT wrap"), "Should mention no markdown wrapping");
            assertTrue(prompt.contains("Do NOT repeat"), "Should mention no question repetition");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for buildSearchRequestBody
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("buildSearchRequestBody")
    class BuildSearchRequestBodyTests {

        @Test
        @DisplayName("should return JSON body with standard fields when allFields is false")
        void shouldReturnJsonBodyWithStandardFields() {
            String body = config.buildSearchRequestBody("assignee = currentUser()", 10, false);

            assertNotNull(body);
            assertTrue(body.contains("\"jql\""));
            assertTrue(body.contains("assignee = currentUser()"));
            assertTrue(body.contains("\"maxResults\":10"));
            assertTrue(body.contains("\"fields\""));
            assertTrue(body.contains("\"summary\""));
            assertTrue(body.contains("\"status\""));
            assertTrue(body.contains("\"assignee\""));
            assertTrue(body.contains("\"priority\""));
            assertTrue(body.contains("\"issuetype\""));
            assertTrue(body.contains("\"project\""));
            assertFalse(body.contains("\"*all\""), "Standard mode should not include *all");
            assertFalse(body.contains("\"expand\""), "Standard mode should not include expand");
        }

        @Test
        @DisplayName("should return JSON body with *all fields when allFields is true")
        void shouldReturnJsonBodyWithAllFields() {
            String body = config.buildSearchRequestBody("project = JSM", 50, true);

            assertNotNull(body);
            assertTrue(body.contains("\"jql\""));
            assertTrue(body.contains("project = JSM"));
            assertTrue(body.contains("\"maxResults\":50"));
            assertTrue(body.contains("\"*all\""), "All-fields mode should include *all");
            assertTrue(body.contains("\"expand\":\"names\""), "All-fields mode should expand names");
        }

        @Test
        @DisplayName("should include correct maxResults value")
        void shouldIncludeCorrectMaxResults() {
            String body = config.buildSearchRequestBody("status = Open", 25, false);

            assertTrue(body.contains("\"maxResults\":25"));
        }

        @Test
        @DisplayName("should handle empty query string")
        void shouldHandleEmptyQuery() {
            String body = config.buildSearchRequestBody("", 10, false);

            assertNotNull(body);
            assertTrue(body.contains("\"jql\":\"\""));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for parseSearchResponse
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("parseSearchResponse")
    class ParseSearchResponseTests {

        @Test
        @DisplayName("should parse search response with results")
        void shouldParseSearchResponseWithResults() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-101",
                          "fields": {
                            "summary": "Server outage reported",
                            "issuetype": {"name": "Incident"},
                            "status": {"name": "Open"},
                            "priority": {"name": "High"},
                            "assignee": {"displayName": "Jane Doe"}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            McpSearchResult result = config.parseSearchResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(result);
            assertEquals("jql", result.type());
            assertEquals(1, result.total());
            assertEquals(1, result.shown());
            assertFalse(result.rows().isEmpty());

            Map<String, Object> row = result.rows().get(0);
            assertEquals("JSM-101", row.get("key"));
            assertEquals("Incident", row.get("type"));
            assertEquals("Server outage reported", row.get("summary"));
            assertEquals("Open", row.get("status"));
            assertEquals("High", row.get("priority"));
            assertEquals("Jane Doe", row.get("assignee"));
            assertEquals("https://mycompany.atlassian.net/browse/JSM-101", row.get("url"));
        }

        @Test
        @DisplayName("should parse search response with multiple results")
        void shouldParseSearchResponseWithMultipleResults() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-1",
                          "fields": {
                            "summary": "First issue",
                            "issuetype": {"name": "Task"},
                            "status": {"name": "Done"},
                            "priority": {"name": "Low"},
                            "assignee": {"displayName": "Alice"}
                          }
                        },
                        {
                          "key": "JSM-2",
                          "fields": {
                            "summary": "Second issue",
                            "issuetype": {"name": "Bug"},
                            "status": {"name": "In Progress"},
                            "priority": {"name": "Medium"},
                            "assignee": {"displayName": "Bob"}
                          }
                        }
                      ],
                      "total": 50
                    }
                    """;

            McpSearchResult result = config.parseSearchResponse(json, "https://example.atlassian.net");

            assertEquals(50, result.total());
            assertEquals(2, result.shown());
            assertEquals(2, result.rows().size());

            assertEquals("JSM-1", result.rows().get(0).get("key"));
            assertEquals("JSM-2", result.rows().get(1).get("key"));
        }

        @Test
        @DisplayName("should parse search response with empty issues")
        void shouldParseSearchResponseWithEmptyIssues() {
            String json = "{\"issues\": [], \"total\": 0}";

            McpSearchResult result = config.parseSearchResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(result);
            assertEquals("jql", result.type());
            assertEquals(0, result.total());
            assertEquals(0, result.shown());
            assertTrue(result.rows().isEmpty());
        }

        @Test
        @DisplayName("should handle missing 'issues' array gracefully")
        void shouldHandleMissingIssuesArray() {
            String json = "{\"total\": 0}";

            McpSearchResult result = config.parseSearchResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(result);
            assertEquals(0, result.total());
            assertTrue(result.rows().isEmpty());
        }

        @Test
        @DisplayName("should handle missing 'total' field gracefully")
        void shouldHandleMissingTotalField() {
            String json = "{\"issues\": []}";

            McpSearchResult result = config.parseSearchResponse(json, "https://mycompany.atlassian.net");

            assertEquals(0, result.total());
        }

        @Test
        @DisplayName("should throw exception for error response with errorMessages")
        void shouldThrowExceptionForErrorResponse() {
            String json = "{\"errorMessages\": [\"The value 'INVALID' does not exist for the field 'project'.\"]}";

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> config.parseSearchResponse(json, "https://mycompany.atlassian.net"));

            assertTrue(exception.getMessage().contains("INVALID"));
        }

        @Test
        @DisplayName("should not throw for empty errorMessages array")
        void shouldNotThrowForEmptyErrorMessages() {
            String json = "{\"errorMessages\": [], \"issues\": [], \"total\": 0}";

            McpSearchResult result = config.parseSearchResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(result);
            assertEquals(0, result.total());
        }

        @Test
        @DisplayName("should handle null nested fields gracefully")
        void shouldHandleNullNestedFields() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-999",
                          "fields": {
                            "summary": "Unassigned ticket",
                            "issuetype": null,
                            "status": null,
                            "priority": null,
                            "assignee": null
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            McpSearchResult result = config.parseSearchResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(result);
            assertEquals(1, result.shown());

            Map<String, Object> row = result.rows().get(0);
            assertEquals("JSM-999", row.get("key"));
            assertEquals("Unassigned ticket", row.get("summary"));
            assertEquals("", row.get("type"));
            assertEquals("", row.get("status"));
            assertEquals("", row.get("priority"));
            assertEquals("", row.get("assignee"));
        }

        @Test
        @DisplayName("should handle missing key in issue")
        void shouldHandleMissingKeyInIssue() {
            String json = """
                    {
                      "issues": [
                        {
                          "fields": {
                            "summary": "No key issue",
                            "issuetype": {"name": "Task"},
                            "status": {"name": "Open"},
                            "priority": {"name": "Low"},
                            "assignee": {"displayName": "Test"}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            McpSearchResult result = config.parseSearchResponse(json, "https://example.atlassian.net");

            Map<String, Object> row = result.rows().get(0);
            assertEquals("", row.get("key"));
            assertEquals("https://example.atlassian.net/browse/", row.get("url"));
        }

        @Test
        @DisplayName("should handle issue with no fields object (null fields)")
        void shouldHandleIssueWithNoFieldsObject() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-888"
                        }
                      ],
                      "total": 1
                    }
                    """;

            McpSearchResult result = config.parseSearchResponse(json, "https://example.atlassian.net");

            assertNotNull(result);
            assertEquals(1, result.shown());

            Map<String, Object> row = result.rows().get(0);
            assertEquals("JSM-888", row.get("key"));
            // nestedStr/str with null obj should return ""
            assertEquals("", row.get("type"));
            assertEquals("", row.get("summary"));
            assertEquals("", row.get("status"));
            assertEquals("", row.get("priority"));
            assertEquals("", row.get("assignee"));
        }

        @Test
        @DisplayName("should handle issue with json-null key")
        void shouldHandleIssueWithJsonNullKey() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": null,
                          "fields": {
                            "summary": "Null key",
                            "issuetype": {"name": "Task"},
                            "status": {"name": "Open"},
                            "priority": {"name": "Low"},
                            "assignee": {"displayName": "Test"}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            McpSearchResult result = config.parseSearchResponse(json, "https://example.atlassian.net");

            Map<String, Object> row = result.rows().get(0);
            assertEquals("", row.get("key"));
        }

        @Test
        @DisplayName("should handle sparse fields object missing outer nested keys")
        void shouldHandleSparseFieldsMissingOuterKeys() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-790",
                          "fields": {
                            "summary": "Sparse fields"
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            McpSearchResult result = config.parseSearchResponse(json, "https://example.atlassian.net");

            Map<String, Object> row = result.rows().get(0);
            assertEquals("JSM-790", row.get("key"));
            assertEquals("Sparse fields", row.get("summary"));
            assertEquals("", row.get("type"));
            assertEquals("", row.get("status"));
            assertEquals("", row.get("priority"));
            assertEquals("", row.get("assignee"));
        }

        @Test
        @DisplayName("should handle nested field with missing inner key")
        void shouldHandleNestedFieldWithMissingInnerKey() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-789",
                          "fields": {
                            "summary": "Test",
                            "issuetype": {},
                            "status": {"id": "1"},
                            "priority": {"id": "2"},
                            "assignee": {"accountId": "abc"}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            McpSearchResult result = config.parseSearchResponse(json, "https://example.atlassian.net");

            Map<String, Object> row = result.rows().get(0);
            assertEquals("", row.get("type"));
            assertEquals("", row.get("status"));
            assertEquals("", row.get("priority"));
            assertEquals("", row.get("assignee"));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for parseStandardResponse
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("parseStandardResponse")
    class ParseStandardResponseTests {

        @Test
        @DisplayName("should parse standard response into FreeForm list")
        void shouldParseStandardResponse() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-50",
                          "fields": {
                            "summary": "VPN access request",
                            "issuetype": {"name": "Service Request"},
                            "status": {"name": "Waiting for Support"},
                            "priority": {"name": "Medium"},
                            "assignee": {"displayName": "Support Agent"}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<FreeForm> rows = config.parseStandardResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(rows);
            assertEquals(1, rows.size());
        }

        @Test
        @DisplayName("should return empty list for empty issues")
        void shouldReturnEmptyListForEmptyIssues() {
            String json = "{\"issues\": []}";

            List<FreeForm> rows = config.parseStandardResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(rows);
            assertTrue(rows.isEmpty());
        }

        @Test
        @DisplayName("should handle missing 'issues' array gracefully")
        void shouldHandleMissingIssuesArray() {
            String json = "{}";

            List<FreeForm> rows = config.parseStandardResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(rows);
            assertTrue(rows.isEmpty());
        }

        @Test
        @DisplayName("should throw exception for error response")
        void shouldThrowExceptionForErrorResponse() {
            String json = "{\"errorMessages\": [\"JQL syntax error\"]}";

            assertThrows(IllegalArgumentException.class,
                    () -> config.parseStandardResponse(json, "https://mycompany.atlassian.net"));
        }

        @Test
        @DisplayName("should not throw for empty errorMessages array")
        void shouldNotThrowForEmptyErrorMessages() {
            String json = "{\"errorMessages\": [], \"issues\": []}";

            List<FreeForm> rows = config.parseStandardResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(rows);
            assertTrue(rows.isEmpty());
        }

        @Test
        @DisplayName("should handle null nested fields in standard response")
        void shouldHandleNullNestedFieldsInStandardResponse() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-77",
                          "fields": {
                            "summary": "Null fields test",
                            "issuetype": null,
                            "status": null,
                            "priority": null,
                            "assignee": null
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<FreeForm> rows = config.parseStandardResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(rows);
            assertEquals(1, rows.size());
        }

        @Test
        @DisplayName("should handle issue with no fields object in standard response")
        void shouldHandleIssueWithNoFieldsObjectInStandardResponse() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-900"
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<FreeForm> rows = config.parseStandardResponse(json, "https://example.atlassian.net");

            assertNotNull(rows);
            assertEquals(1, rows.size());
        }

        @Test
        @DisplayName("should parse multiple issues in standard response")
        void shouldParseMultipleIssuesInStandardResponse() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-1",
                          "fields": {
                            "summary": "First",
                            "issuetype": {"name": "Task"},
                            "status": {"name": "Open"},
                            "priority": {"name": "High"},
                            "assignee": {"displayName": "A"}
                          }
                        },
                        {
                          "key": "JSM-2",
                          "fields": {
                            "summary": "Second",
                            "issuetype": {"name": "Bug"},
                            "status": {"name": "Closed"},
                            "priority": {"name": "Low"},
                            "assignee": {"displayName": "B"}
                          }
                        }
                      ],
                      "total": 2
                    }
                    """;

            List<FreeForm> rows = config.parseStandardResponse(json, "https://example.atlassian.net");

            assertEquals(2, rows.size());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for parseAllFieldsResponse
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("parseAllFieldsResponse")
    class ParseAllFieldsResponseTests {

        @Test
        @DisplayName("should parse all fields response with names mapping")
        void shouldParseAllFieldsResponseWithNames() {
            String json = """
                    {
                      "names": {
                        "summary": "Summary",
                        "status": "Status",
                        "customfield_10001": "Sprint"
                      },
                      "issues": [
                        {
                          "key": "JSM-300",
                          "fields": {
                            "summary": "Hardware request",
                            "status": {"name": "Open"},
                            "customfield_10001": "Sprint 5"
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(result);
            assertEquals(1, result.size());

            Map<String, String> row = result.get(0);
            assertEquals("JSM-300", row.get("key"));
            assertEquals("https://mycompany.atlassian.net/browse/JSM-300", row.get("url"));
            assertEquals("Hardware request", row.get("Summary"));
            assertEquals("Open", row.get("Status"));
            assertEquals("Sprint 5", row.get("Sprint"));
        }

        @Test
        @DisplayName("should use field key as fallback when names mapping is missing")
        void shouldUseFallbackFieldKeyWhenNamesMissing() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-400",
                          "fields": {
                            "summary": "No names mapping",
                            "customfield_99999": "Custom value"
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertNotNull(result);
            assertEquals(1, result.size());

            Map<String, String> row = result.get(0);
            assertEquals("No names mapping", row.get("summary"));
            assertEquals("Custom value", row.get("customfield_99999"));
        }

        @Test
        @DisplayName("should return empty list for empty issues")
        void shouldReturnEmptyListForEmptyIssues() {
            String json = "{\"issues\": [], \"names\": {}}";

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should handle missing 'issues' array gracefully")
        void shouldHandleMissingIssuesArray() {
            String json = "{}";

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw exception for error response")
        void shouldThrowExceptionForErrorResponse() {
            String json = "{\"errorMessages\": [\"Field not found\"]}";

            assertThrows(IllegalArgumentException.class,
                    () -> config.parseAllFieldsResponse(json, "https://mycompany.atlassian.net"));
        }

        @Test
        @DisplayName("should not throw for empty errorMessages array")
        void shouldNotThrowForEmptyErrorMessages() {
            String json = "{\"errorMessages\": [], \"issues\": []}";

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://mycompany.atlassian.net");

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should extract displayName from JSON object fields")
        void shouldExtractDisplayNameFromObjectFields() {
            String json = """
                    {
                      "names": {"assignee": "Assignee"},
                      "issues": [
                        {
                          "key": "JSM-500",
                          "fields": {
                            "assignee": {"displayName": "John Smith", "accountId": "abc123"}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertEquals("John Smith", result.get(0).get("Assignee"));
        }

        @Test
        @DisplayName("should extract name from JSON object fields when displayName is missing")
        void shouldExtractNameFromObjectFields() {
            String json = """
                    {
                      "names": {"status": "Status"},
                      "issues": [
                        {
                          "key": "JSM-501",
                          "fields": {
                            "status": {"name": "In Progress", "id": "3"}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertEquals("In Progress", result.get(0).get("Status"));
        }

        @Test
        @DisplayName("should extract value from JSON object fields when displayName and name are missing")
        void shouldExtractValueFromObjectFields() {
            String json = """
                    {
                      "names": {"customfield_10050": "Custom Field"},
                      "issues": [
                        {
                          "key": "JSM-502",
                          "fields": {
                            "customfield_10050": {"value": "Option A"}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertEquals("Option A", result.get(0).get("Custom Field"));
        }

        @Test
        @DisplayName("should return empty string for JSON object with no recognized keys")
        void shouldReturnEmptyForObjectWithNoRecognizedKeys() {
            String json = """
                    {
                      "names": {"customfield_10060": "Unknown"},
                      "issues": [
                        {
                          "key": "JSM-503",
                          "fields": {
                            "customfield_10060": {"id": "123", "self": "https://..."}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            // Object has no displayName/name/value, so extractFieldValue returns ""
            // Blank values are skipped, so the field should not be present
            assertFalse(result.get(0).containsKey("Unknown"));
        }

        @Test
        @DisplayName("should handle null field values")
        void shouldHandleNullFieldValues() {
            String json = """
                    {
                      "names": {"assignee": "Assignee"},
                      "issues": [
                        {
                          "key": "JSM-504",
                          "fields": {
                            "assignee": null
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            // null values produce empty string which is blank, so field is skipped
            assertFalse(result.get(0).containsKey("Assignee"));
        }

        @Test
        @DisplayName("should handle JSON array field values")
        void shouldHandleArrayFieldValues() {
            String json = """
                    {
                      "names": {"labels": "Labels"},
                      "issues": [
                        {
                          "key": "JSM-505",
                          "fields": {
                            "labels": ["urgent", "production", "network"]
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertEquals("urgent, production, network", result.get(0).get("Labels"));
        }

        @Test
        @DisplayName("should handle JSON array with object elements")
        void shouldHandleArrayWithObjectElements() {
            String json = """
                    {
                      "names": {"components": "Components"},
                      "issues": [
                        {
                          "key": "JSM-506",
                          "fields": {
                            "components": [{"name": "Backend"}, {"name": "Database"}]
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertEquals("Backend, Database", result.get(0).get("Components"));
        }

        @Test
        @DisplayName("should handle empty JSON array")
        void shouldHandleEmptyArray() {
            String json = """
                    {
                      "names": {"labels": "Labels"},
                      "issues": [
                        {
                          "key": "JSM-507",
                          "fields": {
                            "labels": []
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            // Empty array produces empty string which is blank, so field is skipped
            assertFalse(result.get(0).containsKey("Labels"));
        }

        @Test
        @DisplayName("should handle issue without fields object")
        void shouldHandleIssueWithoutFieldsObject() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-508"
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("JSM-508", result.get(0).get("key"));
            assertEquals("https://example.atlassian.net/browse/JSM-508", result.get(0).get("url"));
        }

        @Test
        @DisplayName("should handle names as non-object gracefully")
        void shouldHandleNamesAsNonObject() {
            String json = """
                    {
                      "names": "not-an-object",
                      "issues": [
                        {
                          "key": "JSM-509",
                          "fields": {
                            "summary": "Test"
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertNotNull(result);
            assertEquals(1, result.size());
            // Since names is not an object, field keys are used as-is
            assertEquals("Test", result.get(0).get("summary"));
        }

        @Test
        @DisplayName("should handle object field with null displayName falling through to name")
        void shouldHandleNullDisplayNameFallingToName() {
            String json = """
                    {
                      "names": {"priority": "Priority"},
                      "issues": [
                        {
                          "key": "JSM-510",
                          "fields": {
                            "priority": {"displayName": null, "name": "Critical"}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertEquals("Critical", result.get(0).get("Priority"));
        }

        @Test
        @DisplayName("should handle object field with null displayName and null name falling through to value")
        void shouldHandleNullDisplayNameAndNullNameFallingToValue() {
            String json = """
                    {
                      "names": {"customfield_123": "Custom"},
                      "issues": [
                        {
                          "key": "JSM-511",
                          "fields": {
                            "customfield_123": {"displayName": null, "name": null, "value": "Fallback"}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertEquals("Fallback", result.get(0).get("Custom"));
        }

        @Test
        @DisplayName("should skip blank primitive values")
        void shouldSkipBlankPrimitiveValues() {
            String json = """
                    {
                      "names": {"description": "Description"},
                      "issues": [
                        {
                          "key": "JSM-512",
                          "fields": {
                            "description": "   "
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            // Blank string is skipped
            assertFalse(result.get(0).containsKey("Description"));
        }

        @Test
        @DisplayName("should handle issue with fields as non-object (e.g. null)")
        void shouldHandleFieldsAsNonObject() {
            String json = """
                    {
                      "issues": [
                        {
                          "key": "JSM-600",
                          "fields": null
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("JSM-600", result.get(0).get("key"));
            // Only key and url should be present (no field iteration happened)
            assertEquals(2, result.get(0).size());
        }

        @Test
        @DisplayName("should handle object field with value key being JSON null")
        void shouldHandleObjectFieldWithNullValueKey() {
            String json = """
                    {
                      "names": {"customfield_200": "Custom"},
                      "issues": [
                        {
                          "key": "JSM-601",
                          "fields": {
                            "customfield_200": {"value": null}
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            // value is null → extractFieldValue returns "" for obj with no recognized keys → blank → skipped
            assertFalse(result.get(0).containsKey("Custom"));
        }

        @Test
        @DisplayName("should handle array with mixed null and valid elements")
        void shouldHandleArrayWithMixedNullAndValidElements() {
            String json = """
                    {
                      "names": {"fixVersions": "Fix Versions"},
                      "issues": [
                        {
                          "key": "JSM-513",
                          "fields": {
                            "fixVersions": [{"name": "v1.0"}, null, {"name": "v2.0"}]
                          }
                        }
                      ],
                      "total": 1
                    }
                    """;

            List<Map<String, String>> result = config.parseAllFieldsResponse(json, "https://example.atlassian.net");

            assertEquals("v1.0, v2.0", result.get(0).get("Fix Versions"));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for default interface methods (buildBasicSearchUrl, buildBasicBaseUrl)
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("URL building (default interface methods)")
    class UrlBuildingTests {

        @Test
        @DisplayName("buildBasicSearchUrl should append search path to host URL")
        void shouldAppendSearchPathToHostUrl() {
            String url = config.buildBasicSearchUrl("https://mycompany.atlassian.net");

            assertEquals("https://mycompany.atlassian.net/search/jql", url);
        }

        @Test
        @DisplayName("buildBasicSearchUrl should strip trailing slashes")
        void shouldStripTrailingSlashes() {
            String url = config.buildBasicSearchUrl("https://mycompany.atlassian.net///");

            assertEquals("https://mycompany.atlassian.net/search/jql", url);
        }

        @Test
        @DisplayName("buildBasicBaseUrl should return host URL with trailing slashes stripped")
        void shouldReturnHostUrlWithSlashesStripped() {
            String url = config.buildBasicBaseUrl("https://mycompany.atlassian.net");

            assertEquals("https://mycompany.atlassian.net", url);
        }

        @Test
        @DisplayName("buildBasicBaseUrl should strip trailing slashes")
        void shouldStripTrailingSlashesFromBaseUrl() {
            String url = config.buildBasicBaseUrl("https://mycompany.atlassian.net///");

            assertEquals("https://mycompany.atlassian.net", url);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for McpProductConfig interface compliance
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("McpProductConfig interface compliance")
    class InterfaceComplianceTests {

        @Test
        @DisplayName("should implement McpProductConfig interface")
        void shouldImplementMcpProductConfig() {
            assertTrue(config instanceof McpProductConfig,
                    "JsmMcpProductConfig should implement McpProductConfig");
        }

        @Test
        @DisplayName("should be a final class")
        void shouldBeFinalClass() {
            assertTrue(java.lang.reflect.Modifier.isFinal(JsmMcpProductConfig.class.getModifiers()),
                    "JsmMcpProductConfig should be declared final");
        }
    }
}
