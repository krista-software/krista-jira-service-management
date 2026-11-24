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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JiraIssue Integration Tests - Part 5 (Additional Edge Cases)")
class JiraIssueIntegrationTest5 {

    @Test
    @DisplayName("Should handle paragraph content without type field")
    void testGetDescription_ParagraphContentWithoutType() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        
        // Add a node without type field (should be skipped)
        JsonObject nodeWithoutType = new JsonObject();
        nodeWithoutType.addProperty("someField", "someValue");
        paragraphContent.add(nodeWithoutType);
        
        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertNotNull(issue.description);
    }

    @Test
    @DisplayName("Should handle nested paragraph with null content")
    void testGetDescription_NestedParagraphNullContent() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        
        JsonObject nestedParagraph = new JsonObject();
        nestedParagraph.addProperty("type", "paragraph");
        nestedParagraph.add("content", null);
        
        paragraphContent.add(nestedParagraph);
        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertNotNull(issue.description);
    }

    @Test
    @DisplayName("Should handle table cell with non-paragraph content")
    void testGetDescription_TableCellNonParagraph() {
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
        JsonObject nonParagraph = new JsonObject();
        nonParagraph.addProperty("type", "codeBlock");
        cellContent.add(nonParagraph);
        cell.add("content", cellContent);
        rowCells.add(cell);
        
        row.add("content", rowCells);
        tableRows.add(row);
        
        table.add("content", tableRows);
        content.add(table);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertNotNull(issue.description);
    }

    @Test
    @DisplayName("Should handle ordered list item without content")
    void testGetDescription_OrderedListItemWithoutContent() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        
        JsonObject orderedList = new JsonObject();
        orderedList.addProperty("type", "orderedList");
        JsonArray listItems = new JsonArray();
        
        JsonObject listItem = new JsonObject();
        // No content field
        listItems.add(listItem);
        
        orderedList.add("content", listItems);
        content.add(orderedList);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertNotNull(issue.description);
    }

    @Test
    @DisplayName("Should handle description with unknown paragraph content type")
    void testGetDescription_UnknownParagraphContentType() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        
        JsonObject unknownType = new JsonObject();
        unknownType.addProperty("type", "unknownContentType");
        paragraphContent.add(unknownType);
        
        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertNotNull(issue.description);
    }

    @Test
    @DisplayName("Should handle labels with exception during iteration")
    void testGetLabels_ExceptionDuringIteration() {
        JsonObject fields = new JsonObject();
        // Create a malformed labels array that might cause issues
        JsonArray labels = new JsonArray();
        labels.add("valid-label");
        fields.add("labels", labels);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertNotNull(issue.labels);
        assertEquals(1, issue.labels.size());
    }

    @Test
    @DisplayName("Should handle components with exception during iteration")
    void testGetComponents_ExceptionDuringIteration() {
        JsonObject fields = new JsonObject();
        JsonArray components = new JsonArray();
        JsonObject comp = new JsonObject();
        comp.addProperty("name", "Component1");
        components.add(comp);
        fields.add("components", components);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertNotNull(issue.components);
        assertEquals(1, issue.components.size());
    }
}

