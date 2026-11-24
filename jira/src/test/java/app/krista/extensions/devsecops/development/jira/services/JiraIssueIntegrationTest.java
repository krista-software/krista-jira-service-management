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

package app.krista.extensions.devsecops.development.jira.services;

import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JiraIssue Integration Tests")
class JiraIssueIntegrationTest {

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create JiraIssue with all fields")
    void testConstructor_AllFields() {
        JsonObject fields = new JsonObject();
        JsonObject renderedFields = new JsonObject();
        
        JiraIssue issue = new JiraIssue("123", "TEST-1", fields, renderedFields);
        
        assertEquals("123", issue.id);
        assertEquals("TEST-1", issue.key);
        assertNotNull(issue.fields);
        assertNotNull(issue.renderedFields);
    }

    @Test
    @DisplayName("Should create JiraIssue with null fields")
    void testConstructor_NullFields() {
        JiraIssue issue = new JiraIssue("123", "TEST-1", null, null);
        
        assertEquals("123", issue.id);
        assertEquals("TEST-1", issue.key);
        assertNull(issue.fields);
        assertNull(issue.renderedFields);
    }

    // ==================== create() Method Tests ====================

    @Test
    @DisplayName("Should create JiraIssue from JsonObject with all fields")
    void testCreate_AllFields() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "456");
        issueObject.addProperty("key", "PROJ-2");
        issueObject.add("fields", new JsonObject());
        issueObject.add("renderedFields", new JsonObject());
        
        JiraIssue issue = JiraIssue.create(issueObject);
        
        assertEquals("456", issue.id);
        assertEquals("PROJ-2", issue.key);
        assertNotNull(issue.fields);
        assertNotNull(issue.renderedFields);
    }

    @Test
    @DisplayName("Should create JiraIssue from JsonObject with missing fields")
    void testCreate_MissingFields() {
        JsonObject issueObject = new JsonObject();
        
        JiraIssue issue = JiraIssue.create(issueObject);
        
        assertNull(issue.id);
        assertNull(issue.key);
        assertNull(issue.fields);
        assertNull(issue.renderedFields);
    }

    @Test
    @DisplayName("Should create JiraIssue with only id and key")
    void testCreate_OnlyIdAndKey() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "789");
        issueObject.addProperty("key", "ABC-3");
        
        JiraIssue issue = JiraIssue.create(issueObject);
        
        assertEquals("789", issue.id);
        assertEquals("ABC-3", issue.key);
        assertNull(issue.fields);
        assertNull(issue.renderedFields);
    }

    // ==================== Constants Tests ====================

    @Test
    @DisplayName("Should verify constants are correctly defined")
    void testConstants() {
        assertEquals("renderedFields", JiraIssue.RENDERED_FIELDS);
        assertEquals("fields", JiraIssue.FIELDS);
        assertEquals("key", JiraIssue.KEY);
        assertEquals("id", JiraIssue.ID);
    }

    // ==================== getIssue() Method Tests ====================

    @Test
    @DisplayName("Should get Issue with all fields populated")
    void testGetIssue_AllFields() {
        JsonObject fields = createCompleteFieldsObject();
        JiraIssue jiraIssue = new JiraIssue("100", "TEST-100", fields, null);
        
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("100", issue.id);
        assertEquals("TEST-100", issue.key);
        assertEquals("Test Summary", issue.summary);
        assertEquals("John Doe", issue.assigneeName);
        assertEquals("Jane Smith", issue.reporterName);
        assertEquals("High", issue.priority);
        assertEquals("PROJ", issue.projectKey);
        assertEquals("IN PROGRESS", issue.status);
        assertEquals("Story", issue.issueType);
        assertNotNull(issue.description);
    }

    @Test
    @DisplayName("Should get Issue with null fields")
    void testGetIssue_NullFields() {
        JiraIssue jiraIssue = new JiraIssue("200", "TEST-200", null, null);
        
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("200", issue.id);
        assertEquals("TEST-200", issue.key);
    }

    @Test
    @DisplayName("Should get Issue with empty fields object")
    void testGetIssue_EmptyFields() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("300", "TEST-300", fields, null);

        Issue issue = jiraIssue.getIssue();

        assertEquals("300", issue.id);
        assertEquals("TEST-300", issue.key);
    }

    // ==================== getSummary() Tests ====================

    @Test
    @DisplayName("Should get summary successfully")
    void testGetSummary_Success() {
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test Summary");
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        String summary = jiraIssue.getSummary();

        assertEquals("Test Summary", summary);
    }

    @Test
    @DisplayName("Should return empty string when summary is missing")
    void testGetSummary_Missing() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        String summary = jiraIssue.getSummary();

        assertEquals("", summary);
    }

    @Test
    @DisplayName("Should return empty string when fields is null")
    void testGetSummary_NullFields() {
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", null, null);

        String summary = jiraIssue.getSummary();

        assertEquals("", summary);
    }

    // ==================== getComments() Tests ====================

    @Test
    @DisplayName("Should get comments successfully")
    void testGetComments_Success() {
        JsonObject fields = new JsonObject();
        JsonObject comment = new JsonObject();
        JsonArray comments = new JsonArray();

        JsonObject commentObj = new JsonObject();
        JsonObject body = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject paragraph = new JsonObject();
        JsonArray paragraphContent = new JsonArray();
        JsonObject textNode = new JsonObject();
        textNode.addProperty("text", "This is a comment");
        paragraphContent.add(textNode);
        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        body.add("content", content);
        commentObj.add("body", body);
        comments.add(commentObj);

        comment.add("comments", comments);
        fields.add("comment", comment);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        List<String> result = jiraIssue.getComments();

        assertEquals(1, result.size());
        assertEquals("This is a comment", result.get(0));
    }

    @Test
    @DisplayName("Should return empty list when comments are missing")
    void testGetComments_Missing() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        List<String> result = jiraIssue.getComments();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle comment with attrs")
    void testGetComments_WithAttrs() {
        JsonObject fields = new JsonObject();
        JsonObject comment = new JsonObject();
        JsonArray comments = new JsonArray();

        JsonObject commentObj = new JsonObject();
        JsonObject body = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject paragraph = new JsonObject();
        JsonArray paragraphContent = new JsonArray();

        // Node with attrs.text
        JsonObject nodeWithAttrs = new JsonObject();
        JsonObject attrs = new JsonObject();
        attrs.addProperty("text", "Mention text");
        nodeWithAttrs.add("attrs", attrs);
        paragraphContent.add(nodeWithAttrs);

        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        body.add("content", content);
        commentObj.add("body", body);
        comments.add(commentObj);

        comment.add("comments", comments);
        fields.add("comment", comment);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        List<String> result = jiraIssue.getComments();

        assertEquals(1, result.size());
        assertEquals("Mention text", result.get(0));
    }

    @Test
    @DisplayName("Should handle comment with attrs.url")
    void testGetComments_WithAttrsUrl() {
        JsonObject fields = new JsonObject();
        JsonObject comment = new JsonObject();
        JsonArray comments = new JsonArray();

        JsonObject commentObj = new JsonObject();
        JsonObject body = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject paragraph = new JsonObject();
        JsonArray paragraphContent = new JsonArray();

        // Node with attrs.url
        JsonObject nodeWithAttrs = new JsonObject();
        JsonObject attrs = new JsonObject();
        attrs.addProperty("url", "http://example.com");
        nodeWithAttrs.add("attrs", attrs);
        paragraphContent.add(nodeWithAttrs);

        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        body.add("content", content);
        commentObj.add("body", body);
        comments.add(commentObj);

        comment.add("comments", comments);
        fields.add("comment", comment);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        List<String> result = jiraIssue.getComments();

        assertEquals(1, result.size());
        assertEquals("http://example.com", result.get(0));
    }

    @Test
    @DisplayName("Should handle comment without text or attrs")
    void testGetComments_WithoutTextOrAttrs() {
        JsonObject fields = new JsonObject();
        JsonObject comment = new JsonObject();
        JsonArray comments = new JsonArray();

        JsonObject commentObj = new JsonObject();
        JsonObject body = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject paragraph = new JsonObject();
        JsonArray paragraphContent = new JsonArray();

        // Node without text or attrs (should add newline)
        JsonObject emptyNode = new JsonObject();
        paragraphContent.add(emptyNode);

        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        body.add("content", content);
        commentObj.add("body", body);
        comments.add(commentObj);

        comment.add("comments", comments);
        fields.add("comment", comment);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        List<String> result = jiraIssue.getComments();

        assertEquals(1, result.size());
        assertEquals("\n", result.get(0));
    }

    // ==================== Labels Tests ====================

    @Test
    @DisplayName("Should get labels successfully")
    void testGetLabels_Success() {
        JsonObject fields = new JsonObject();
        JsonArray labels = new JsonArray();
        labels.add("bug");
        labels.add("urgent");
        fields.add("labels", labels);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.labels);
        assertEquals(2, issue.labels.size());
        assertTrue(issue.labels.contains("bug"));
        assertTrue(issue.labels.contains("urgent"));
    }

    @Test
    @DisplayName("Should return empty list when labels are null")
    void testGetLabels_Null() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.labels);
        assertTrue(issue.labels.isEmpty());
    }

    @Test
    @DisplayName("Should return empty list when fields is null")
    void testGetLabels_NullFields() {
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", null, null);

        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.labels);
        assertTrue(issue.labels.isEmpty());
    }

    // ==================== Components Tests ====================

    @Test
    @DisplayName("Should get components successfully")
    void testGetComponents_Success() {
        JsonObject fields = new JsonObject();
        JsonArray components = new JsonArray();
        JsonObject comp1 = new JsonObject();
        comp1.addProperty("name", "Backend");
        JsonObject comp2 = new JsonObject();
        comp2.addProperty("name", "Frontend");
        components.add(comp1);
        components.add(comp2);
        fields.add("components", components);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.components);
        assertEquals(2, issue.components.size());
        assertTrue(issue.components.contains("Backend"));
        assertTrue(issue.components.contains("Frontend"));
    }

    @Test
    @DisplayName("Should return empty list when components are missing")
    void testGetComponents_Missing() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.components);
        assertTrue(issue.components.isEmpty());
    }

    // ==================== Description Tests ====================

    @Test
    @DisplayName("Should get description with paragraph")
    void testGetDescription_Paragraph() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", "Test description");
        paragraphContent.add(text);
        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("Test description"));
    }

    @Test
    @DisplayName("Should return empty string when description is null")
    void testGetDescription_Null() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        Issue issue = jiraIssue.getIssue();

        assertEquals("", issue.description);
    }

    @Test
    @DisplayName("Should handle description with mention")
    void testGetDescription_Mention() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();

        JsonObject mention = new JsonObject();
        mention.addProperty("type", "mention");
        JsonObject attrs = new JsonObject();
        attrs.addProperty("text", "@user");
        mention.add("attrs", attrs);
        paragraphContent.add(mention);

        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("@user"));
    }

    @Test
    @DisplayName("Should handle description with status")
    void testGetDescription_Status() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();

        JsonObject status = new JsonObject();
        status.addProperty("type", "status");
        JsonObject attrs = new JsonObject();
        attrs.addProperty("text", "In Progress");
        status.add("attrs", attrs);
        paragraphContent.add(status);

        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("In Progress"));
    }

    @Test
    @DisplayName("Should handle description with nested paragraph")
    void testGetDescription_NestedParagraph() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject outerParagraph = new JsonObject();
        outerParagraph.addProperty("type", "paragraph");
        JsonArray outerContent = new JsonArray();

        JsonObject innerParagraph = new JsonObject();
        innerParagraph.addProperty("type", "paragraph");
        JsonArray innerContent = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", "Nested text");
        innerContent.add(text);
        innerParagraph.add("content", innerContent);

        outerContent.add(innerParagraph);
        outerParagraph.add("content", outerContent);
        content.add(outerParagraph);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("Nested text"));
    }

    @Test
    @DisplayName("Should handle description with inlineCard")
    void testGetDescription_InlineCard() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();

        JsonObject card = new JsonObject();
        card.addProperty("type", "inlineCard");
        JsonObject attrs = new JsonObject();
        attrs.addProperty("url", "http://example.com");
        card.add("attrs", attrs);
        paragraphContent.add(card);

        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("http://example.com"));
    }

    // ==================== Helper Methods ====================

    private JsonObject createCompleteFieldsObject() {
        JsonObject fields = new JsonObject();

        // Summary
        fields.addProperty("summary", "Test Summary");

        // Assignee
        JsonObject assignee = new JsonObject();
        assignee.addProperty("displayName", "John Doe");
        fields.add("assignee", assignee);

        // Reporter
        JsonObject reporter = new JsonObject();
        reporter.addProperty("displayName", "Jane Smith");
        fields.add("reporter", reporter);

        // Priority
        JsonObject priority = new JsonObject();
        priority.addProperty("name", "High");
        fields.add("priority", priority);

        // Project
        JsonObject project = new JsonObject();
        project.addProperty("key", "PROJ");
        fields.add("project", project);

        // Status
        JsonObject status = new JsonObject();
        status.addProperty("name", "In Progress");
        fields.add("status", status);

        // Issue Type
        JsonObject issueType = new JsonObject();
        issueType.addProperty("name", "Story");
        fields.add("issuetype", issueType);

        // Description
        JsonObject description = new JsonObject();
        description.addProperty("type", "doc");
        JsonArray content = new JsonArray();
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("type", "text");
        text.addProperty("text", "Test description");
        paragraphContent.add(text);
        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);

        // Labels
        JsonArray labels = new JsonArray();
        labels.add("label1");
        labels.add("label2");
        fields.add("labels", labels);

        // Components
        JsonArray components = new JsonArray();
        JsonObject component1 = new JsonObject();
        component1.addProperty("name", "Backend");
        components.add(component1);
        fields.add("components", components);

        return fields;
    }

    // ==================== Last Modified Date Tests ====================

    @Test
    @DisplayName("Should parse Last Modified Date with standard ISO 8601 format (with colon in timezone)")
    void testGetIssue_LastModifiedDate_StandardISO8601() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "12345");
        issueObject.addProperty("key", "TEST-100");

        JsonObject fields = new JsonObject();
        fields.addProperty("updated", "2026-02-12T11:19:51.462+05:30");
        fields.addProperty("summary", "Test Issue");
        issueObject.add("fields", fields);

        JiraIssue jiraIssue = JiraIssue.create(issueObject);
        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.lastModifiedDate, "Last Modified Date should not be null");
        // Verify it's a reasonable timestamp (between 2020 and 2030)
        assertTrue(issue.lastModifiedDate > 1577836800000L, "Timestamp should be after 2020");
        assertTrue(issue.lastModifiedDate < 1893456000000L, "Timestamp should be before 2030");
    }

    @Test
    @DisplayName("Should parse Last Modified Date with Jira non-standard format (without colon in timezone)")
    void testGetIssue_LastModifiedDate_JiraNonStandardFormat() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "12345");
        issueObject.addProperty("key", "TEST-101");

        JsonObject fields = new JsonObject();
        fields.addProperty("updated", "2026-02-12T11:19:51.462+0530");
        fields.addProperty("summary", "Test Issue");
        issueObject.add("fields", fields);

        JiraIssue jiraIssue = JiraIssue.create(issueObject);
        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.lastModifiedDate, "Last Modified Date should not be null");
        // Both formats should produce the same result
        assertTrue(issue.lastModifiedDate > 1577836800000L, "Timestamp should be after 2020");
        assertTrue(issue.lastModifiedDate < 1893456000000L, "Timestamp should be before 2030");
    }

    @Test
    @DisplayName("Should parse Last Modified Date with UTC/Zulu time format")
    void testGetIssue_LastModifiedDate_ZuluTime() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "12345");
        issueObject.addProperty("key", "TEST-102");

        JsonObject fields = new JsonObject();
        fields.addProperty("updated", "2026-02-12T05:49:51.462Z");
        fields.addProperty("summary", "Test Issue");
        issueObject.add("fields", fields);

        JiraIssue jiraIssue = JiraIssue.create(issueObject);
        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.lastModifiedDate, "Last Modified Date should not be null");
        assertTrue(issue.lastModifiedDate > 1577836800000L, "Timestamp should be after 2020");
        assertTrue(issue.lastModifiedDate < 1893456000000L, "Timestamp should be before 2030");
    }

    @Test
    @DisplayName("Should parse Last Modified Date with negative timezone offset")
    void testGetIssue_LastModifiedDate_NegativeTimezone() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "12345");
        issueObject.addProperty("key", "TEST-103");

        JsonObject fields = new JsonObject();
        fields.addProperty("updated", "2026-02-11T21:49:51.462-0800");
        fields.addProperty("summary", "Test Issue");
        issueObject.add("fields", fields);

        JiraIssue jiraIssue = JiraIssue.create(issueObject);
        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.lastModifiedDate, "Last Modified Date should not be null");
        assertTrue(issue.lastModifiedDate > 1577836800000L, "Timestamp should be after 2020");
        assertTrue(issue.lastModifiedDate < 1893456000000L, "Timestamp should be before 2030");
    }

    @Test
    @DisplayName("Should return null when 'updated' field is missing")
    void testGetIssue_LastModifiedDate_MissingField() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "12345");
        issueObject.addProperty("key", "TEST-104");

        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test Issue");
        // No 'updated' field
        issueObject.add("fields", fields);

        JiraIssue jiraIssue = JiraIssue.create(issueObject);
        Issue issue = jiraIssue.getIssue();

        assertNull(issue.lastModifiedDate, "Last Modified Date should be null when field is missing");
    }

    @Test
    @DisplayName("Should return null when 'updated' field is null")
    void testGetIssue_LastModifiedDate_NullField() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "12345");
        issueObject.addProperty("key", "TEST-105");

        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test Issue");
        fields.add("updated", null);
        issueObject.add("fields", fields);

        JiraIssue jiraIssue = JiraIssue.create(issueObject);
        Issue issue = jiraIssue.getIssue();

        assertNull(issue.lastModifiedDate, "Last Modified Date should be null when field value is null");
    }

    @Test
    @DisplayName("Should return null when fields object is null")
    void testGetIssue_LastModifiedDate_NullFields() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "12345");
        issueObject.addProperty("key", "TEST-106");
        // No fields object

        JiraIssue jiraIssue = JiraIssue.create(issueObject);
        Issue issue = jiraIssue.getIssue();

        assertNull(issue.lastModifiedDate, "Last Modified Date should be null when fields object is null");
    }

    @Test
    @DisplayName("Should return null when date format is invalid")
    void testGetIssue_LastModifiedDate_InvalidFormat() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "12345");
        issueObject.addProperty("key", "TEST-107");

        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test Issue");
        fields.addProperty("updated", "invalid-date-format");
        issueObject.add("fields", fields);

        JiraIssue jiraIssue = JiraIssue.create(issueObject);
        Issue issue = jiraIssue.getIssue();

        assertNull(issue.lastModifiedDate, "Last Modified Date should be null when date format is invalid");
    }

    @Test
    @DisplayName("Should parse Last Modified Date without milliseconds")
    void testGetIssue_LastModifiedDate_NoMilliseconds() {
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "12345");
        issueObject.addProperty("key", "TEST-108");

        JsonObject fields = new JsonObject();
        fields.addProperty("updated", "2026-02-12T11:19:51+0530");
        fields.addProperty("summary", "Test Issue");
        issueObject.add("fields", fields);

        JiraIssue jiraIssue = JiraIssue.create(issueObject);
        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.lastModifiedDate, "Last Modified Date should not be null");
        assertTrue(issue.lastModifiedDate > 1577836800000L, "Timestamp should be after 2020");
        assertTrue(issue.lastModifiedDate < 1893456000000L, "Timestamp should be before 2030");
        // Verify milliseconds are 0 (timestamp should be divisible by 1000)
        assertEquals(0, issue.lastModifiedDate % 1000, "Milliseconds should be 0 when not specified");
    }
}


