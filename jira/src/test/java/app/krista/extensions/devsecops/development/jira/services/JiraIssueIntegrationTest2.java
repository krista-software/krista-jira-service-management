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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JiraIssue Integration Tests - Part 2")
class JiraIssueIntegrationTest2 {

    // ==================== Description Advanced Tests ====================

    @Test
    @DisplayName("Should handle description with blockCard")
    void testGetDescription_BlockCard() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        
        JsonObject card = new JsonObject();
        card.addProperty("type", "blockCard");
        JsonObject attrs = new JsonObject();
        attrs.addProperty("url", "http://block.example.com");
        card.add("attrs", attrs);
        paragraphContent.add(card);
        
        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertTrue(issue.description.contains("http://block.example.com"));
    }

    @Test
    @DisplayName("Should handle description with date")
    void testGetDescription_Date() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        
        JsonObject date = new JsonObject();
        date.addProperty("type", "date");
        JsonObject attrs = new JsonObject();
        attrs.addProperty("timestamp", "1609459200000"); // Jan 1, 2021
        date.add("attrs", attrs);
        paragraphContent.add(date);
        
        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertNotNull(issue.description);
        assertFalse(issue.description.isEmpty());
    }

    @Test
    @DisplayName("Should handle description with orderedList")
    void testGetDescription_OrderedList() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        
        JsonObject orderedList = new JsonObject();
        orderedList.addProperty("type", "orderedList");
        JsonArray listItems = new JsonArray();
        
        JsonObject listItem = new JsonObject();
        JsonArray itemContent = new JsonArray();
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", "List item 1");
        paragraphContent.add(text);
        paragraph.add("content", paragraphContent);
        itemContent.add(paragraph);
        listItem.add("content", itemContent);
        listItems.add(listItem);
        
        orderedList.add("content", listItems);
        content.add(orderedList);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertTrue(issue.description.contains("List item 1"));
    }

    @Test
    @DisplayName("Should handle description with codeBlock")
    void testGetDescription_CodeBlock() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        
        JsonObject codeBlock = new JsonObject();
        codeBlock.addProperty("type", "codeBlock");
        JsonArray codeContent = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", "System.out.println(\"Hello\");");
        codeContent.add(text);
        codeBlock.add("content", codeContent);
        content.add(codeBlock);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertTrue(issue.description.contains("System.out.println"));
    }

    @Test
    @DisplayName("Should handle description with blockquote")
    void testGetDescription_Blockquote() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        
        JsonObject blockquote = new JsonObject();
        blockquote.addProperty("type", "blockquote");
        JsonArray quoteContent = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", "This is a quote");
        quoteContent.add(text);
        blockquote.add("content", quoteContent);
        content.add(blockquote);
        description.add("content", content);
        fields.add("description", description);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertTrue(issue.description.contains("This is a quote"));
    }

    @Test
    @DisplayName("Should handle description with expand")
    void testGetDescription_Expand() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject expand = new JsonObject();
        expand.addProperty("type", "expand");
        JsonArray expandContent = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", "Expanded content");
        expandContent.add(text);
        expand.add("content", expandContent);
        content.add(expand);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("Expanded content"));
    }

    @Test
    @DisplayName("Should handle description with table")
    void testGetDescription_Table() {
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
        JsonObject paragraph1 = new JsonObject();
        paragraph1.addProperty("type", "paragraph");
        JsonArray p1Content = new JsonArray();
        JsonObject text1 = new JsonObject();
        text1.addProperty("text", "Header1");
        p1Content.add(text1);
        paragraph1.add("content", p1Content);
        cell1Content.add(paragraph1);
        cell1.add("content", cell1Content);
        row1Cells.add(cell1);

        JsonObject cell2 = new JsonObject();
        JsonArray cell2Content = new JsonArray();
        JsonObject paragraph2 = new JsonObject();
        paragraph2.addProperty("type", "paragraph");
        JsonArray p2Content = new JsonArray();
        JsonObject text2 = new JsonObject();
        text2.addProperty("text", "Header2");
        p2Content.add(text2);
        paragraph2.add("content", p2Content);
        cell2Content.add(paragraph2);
        cell2.add("content", cell2Content);
        row1Cells.add(cell2);

        row1.add("content", row1Cells);
        tableRows.add(row1);

        table.add("content", tableRows);
        content.add(table);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("Header1"));
        assertTrue(issue.description.contains("Header2"));
    }

    @Test
    @DisplayName("Should handle description with mediaSingle (should be skipped)")
    void testGetDescription_MediaSingle() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject mediaSingle = new JsonObject();
        mediaSingle.addProperty("type", "mediaSingle");
        JsonArray mediaContent = new JsonArray();
        mediaSingle.add("content", mediaContent);
        content.add(mediaSingle);

        // Add a paragraph to ensure we get some content
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", "After media");
        paragraphContent.add(text);
        paragraph.add("content", paragraphContent);
        content.add(paragraph);

        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("After media"));
    }

    @Test
    @DisplayName("Should handle description with empty nested paragraph")
    void testGetDescription_EmptyNestedParagraph() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();

        JsonObject nestedParagraph = new JsonObject();
        nestedParagraph.addProperty("type", "paragraph");
        nestedParagraph.add("content", new JsonArray()); // Empty content

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
    @DisplayName("Should handle description with unknown type")
    void testGetDescription_UnknownType() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject unknown = new JsonObject();
        unknown.addProperty("type", "unknownType");
        JsonArray unknownContent = new JsonArray();
        unknown.add("content", unknownContent);
        content.add(unknown);

        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertNotNull(issue.description);
    }
}

