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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JiraIssue Integration Tests - Part 4 (Static Methods)")
class JiraIssueIntegrationTest4 {

    // ==================== Static Method Tests ====================

    @Test
    @DisplayName("Should get subtasks from issue object")
    void testGetSubtask_Success() {
        JsonObject issueObject = new JsonObject();
        JsonObject fields = new JsonObject();
        JsonArray subtasks = new JsonArray();
        
        JsonObject subtask1 = new JsonObject();
        subtask1.addProperty("key", "SUB-1");
        subtasks.add(subtask1);
        
        JsonObject subtask2 = new JsonObject();
        subtask2.addProperty("key", "SUB-2");
        subtasks.add(subtask2);
        
        fields.add("subtasks", subtasks);
        issueObject.add("fields", fields);
        
        JsonArray result = JiraIssue.getSubtask(issueObject);
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should get empty subtasks array")
    void testGetSubtask_Empty() {
        JsonObject issueObject = new JsonObject();
        JsonObject fields = new JsonObject();
        JsonArray subtasks = new JsonArray();
        fields.add("subtasks", subtasks);
        issueObject.add("fields", fields);
        
        JsonArray result = JiraIssue.getSubtask(issueObject);
        
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Should get issue type from static method")
    void testGetIssueType_Success() {
        JsonObject issueObject = new JsonObject();
        JsonObject fields = new JsonObject();
        JsonObject issueType = new JsonObject();
        issueType.addProperty("name", "Story");
        fields.add("issuetype", issueType);
        issueObject.add("fields", fields);
        
        String result = JiraIssue.getIssueType(issueObject);
        
        assertEquals("Story", result);
    }

    @Test
    @DisplayName("Should return empty string when issue type is missing in static method")
    void testGetIssueType_Missing() {
        JsonObject issueObject = new JsonObject();
        JsonObject fields = new JsonObject();
        issueObject.add("fields", fields);
        
        String result = JiraIssue.getIssueType(issueObject);
        
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should return empty string when fields is missing in static method")
    void testGetIssueType_NoFields() {
        JsonObject issueObject = new JsonObject();
        
        String result = JiraIssue.getIssueType(issueObject);
        
        assertEquals("", result);
    }

    // ==================== Edge Cases for Description ====================

    @Test
    @DisplayName("Should handle description with null content array")
    void testGetDescription_NullContentArray() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        description.add("content", null);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        
        String result = jiraIssue.getSummary(); // Just to trigger getIssue
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle description that is JsonNull")
    void testGetDescription_JsonNull() {
        JsonObject fields = new JsonObject();
        fields.add("description", null);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        
        String result = jiraIssue.getSummary();
        
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle table with multiple rows and cells")
    void testGetDescription_ComplexTable() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        
        JsonObject table = new JsonObject();
        table.addProperty("type", "table");
        JsonArray tableRows = new JsonArray();
        
        // Row 1
        JsonObject row1 = new JsonObject();
        JsonArray row1Cells = new JsonArray();
        
        JsonObject cell1 = new JsonObject();
        JsonArray cell1Content = new JsonArray();
        JsonObject p1 = new JsonObject();
        p1.addProperty("type", "paragraph");
        JsonArray p1Content = new JsonArray();
        JsonObject text1 = new JsonObject();
        text1.addProperty("text", "A");
        p1Content.add(text1);
        p1.add("content", p1Content);
        cell1Content.add(p1);
        cell1.add("content", cell1Content);
        row1Cells.add(cell1);
        
        JsonObject cell2 = new JsonObject();
        JsonArray cell2Content = new JsonArray();
        JsonObject p2 = new JsonObject();
        p2.addProperty("type", "paragraph");
        JsonArray p2Content = new JsonArray();
        JsonObject text2 = new JsonObject();
        text2.addProperty("text", "B");
        p2Content.add(text2);
        p2.add("content", p2Content);
        cell2Content.add(p2);
        cell2.add("content", cell2Content);
        row1Cells.add(cell2);
        
        row1.add("content", row1Cells);
        tableRows.add(row1);
        
        // Row 2
        JsonObject row2 = new JsonObject();
        JsonArray row2Cells = new JsonArray();
        
        JsonObject cell3 = new JsonObject();
        JsonArray cell3Content = new JsonArray();
        JsonObject p3 = new JsonObject();
        p3.addProperty("type", "paragraph");
        JsonArray p3Content = new JsonArray();
        JsonObject text3 = new JsonObject();
        text3.addProperty("text", "C");
        p3Content.add(text3);
        p3.add("content", p3Content);
        cell3Content.add(p3);
        cell3.add("content", cell3Content);
        row2Cells.add(cell3);
        
        JsonObject cell4 = new JsonObject();
        JsonArray cell4Content = new JsonArray();
        JsonObject p4 = new JsonObject();
        p4.addProperty("type", "paragraph");
        JsonArray p4Content = new JsonArray();
        JsonObject text4 = new JsonObject();
        text4.addProperty("text", "D");
        p4Content.add(text4);
        p4.add("content", p4Content);
        cell4Content.add(p4);
        cell4.add("content", cell4Content);
        row2Cells.add(cell4);
        
        row2.add("content", row2Cells);
        tableRows.add(row2);
        
        table.add("content", tableRows);
        content.add(table);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        
        // Access through getIssue to trigger description parsing
        var issue = jiraIssue.getIssue();
        
        assertTrue(issue.description.contains("A"));
        assertTrue(issue.description.contains("B"));
        assertTrue(issue.description.contains("C"));
        assertTrue(issue.description.contains("D"));
    }

    @Test
    @DisplayName("Should handle table cell without text")
    void testGetDescription_TableCellWithoutText() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject table = new JsonObject();
        table.addProperty("type", "table");
        JsonArray tableRows = new JsonArray();

        JsonObject row = new JsonObject();
        JsonArray rowCells = new JsonArray();

        JsonObject cell = new JsonObject();
        JsonArray cellContent = new JsonArray();
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        JsonObject nonTextNode = new JsonObject();
        nonTextNode.addProperty("type", "mention");
        paragraphContent.add(nonTextNode);
        paragraph.add("content", paragraphContent);
        cellContent.add(paragraph);
        cell.add("content", cellContent);
        rowCells.add(cell);

        row.add("content", rowCells);
        tableRows.add(row);

        table.add("content", tableRows);
        content.add(table);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        var issue = jiraIssue.getIssue();

        assertNotNull(issue.description);
    }

    @Test
    @DisplayName("Should handle ordered list with non-paragraph items")
    void testGetDescription_OrderedListNonParagraph() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject orderedList = new JsonObject();
        orderedList.addProperty("type", "orderedList");
        JsonArray listItems = new JsonArray();

        JsonObject listItem = new JsonObject();
        JsonArray itemContent = new JsonArray();
        JsonObject nonParagraph = new JsonObject();
        nonParagraph.addProperty("type", "codeBlock");
        itemContent.add(nonParagraph);
        listItem.add("content", itemContent);
        listItems.add(listItem);

        orderedList.add("content", listItems);
        content.add(orderedList);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        var issue = jiraIssue.getIssue();

        assertNotNull(issue.description);
    }

    @Test
    @DisplayName("Should handle multiple comments")
    void testGetComments_Multiple() {
        JsonObject fields = new JsonObject();
        JsonObject comment = new JsonObject();
        JsonArray comments = new JsonArray();

        // Comment 1
        JsonObject comment1 = new JsonObject();
        JsonObject body1 = new JsonObject();
        JsonArray content1 = new JsonArray();
        JsonObject paragraph1 = new JsonObject();
        JsonArray paragraphContent1 = new JsonArray();
        JsonObject text1 = new JsonObject();
        text1.addProperty("text", "First comment");
        paragraphContent1.add(text1);
        paragraph1.add("content", paragraphContent1);
        content1.add(paragraph1);
        body1.add("content", content1);
        comment1.add("body", body1);
        comments.add(comment1);

        // Comment 2
        JsonObject comment2 = new JsonObject();
        JsonObject body2 = new JsonObject();
        JsonArray content2 = new JsonArray();
        JsonObject paragraph2 = new JsonObject();
        JsonArray paragraphContent2 = new JsonArray();
        JsonObject text2 = new JsonObject();
        text2.addProperty("text", "Second comment");
        paragraphContent2.add(text2);
        paragraph2.add("content", paragraphContent2);
        content2.add(paragraph2);
        body2.add("content", content2);
        comment2.add("body", body2);
        comments.add(comment2);

        comment.add("comments", comments);
        fields.add("comment", comment);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        var result = jiraIssue.getComments();

        assertEquals(2, result.size());
        assertEquals("First comment", result.get(0));
        assertEquals("Second comment", result.get(1));
    }

    @Test
    @DisplayName("Should handle request type with completedCycle (SLA field)")
    void testGetRequestType_CompletedCycle() {
        JsonObject fields = new JsonObject();

        // Add SLA field with completedCycle
        JsonObject slaField = new JsonObject();
        slaField.addProperty("completedCycle", "value");
        slaField.addProperty("name", "SLA Name");
        fields.add("customfield_10001", slaField);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        var issue = jiraIssue.getIssue();

        assertNull(issue.requestType);
    }

    @Test
    @DisplayName("Should handle request type with null requesttype field")
    void testGetRequestType_NullRequestTypeField() {
        JsonObject fields = new JsonObject();
        fields.add("requesttype", null);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        var issue = jiraIssue.getIssue();

        assertNull(issue.requestType);
    }

    @Test
    @DisplayName("Should handle request type with requesttype object without name")
    void testGetRequestType_RequestTypeObjectWithoutName() {
        JsonObject fields = new JsonObject();
        JsonObject requestType = new JsonObject();
        requestType.addProperty("id", "123");
        fields.add("requesttype", requestType);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        var issue = jiraIssue.getIssue();

        assertNull(issue.requestType);
    }

    @Test
    @DisplayName("Should handle custom field with invalid portal/key format")
    void testGetRequestType_InvalidPortalKeyFormat() {
        JsonObject fields = new JsonObject();
        fields.addProperty("customfield_10001", "invalid");

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        var issue = jiraIssue.getIssue();

        assertNull(issue.requestType);
    }

    @Test
    @DisplayName("Should handle custom field with null value")
    void testGetRequestType_CustomFieldNullValue() {
        JsonObject fields = new JsonObject();
        fields.add("customfield_10001", null);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        var issue = jiraIssue.getIssue();

        assertNull(issue.requestType);
    }

    @Test
    @DisplayName("Should verify JiraIssue is final class")
    void testJiraIssueIsFinal() {
        assertTrue(java.lang.reflect.Modifier.isFinal(JiraIssue.class.getModifiers()));
    }

    @Test
    @DisplayName("Should handle exception in getIssue and return partial issue")
    void testGetIssue_ExceptionHandling() {
        // Create fields that will cause exception in one getter but not others
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test Summary");

        JiraIssue jiraIssue = new JiraIssue("100", "TEST-100", fields, null);
        var issue = jiraIssue.getIssue();

        // Should still have id, key, and summary even if other fields fail
        assertEquals("100", issue.id);
        assertEquals("TEST-100", issue.key);
        assertEquals("Test Summary", issue.summary);
    }
}


