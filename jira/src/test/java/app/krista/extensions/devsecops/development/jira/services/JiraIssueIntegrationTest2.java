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
    @DisplayName("Should handle description with bulletList")
    void testGetDescription_BulletList() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject bulletList = new JsonObject();
        bulletList.addProperty("type", "bulletList");
        JsonArray listItems = new JsonArray();

        JsonObject listItem = new JsonObject();
        JsonArray itemContent = new JsonArray();
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", "Bullet item 1");
        paragraphContent.add(text);
        paragraph.add("content", paragraphContent);
        itemContent.add(paragraph);
        listItem.add("content", itemContent);
        listItems.add(listItem);

        bulletList.add("content", listItems);
        content.add(bulletList);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("Bullet item 1"));
    }

    @Test
    @DisplayName("Should handle description with heading")
    void testGetDescription_Heading() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject heading = new JsonObject();
        heading.addProperty("type", "heading");
        JsonObject headingAttrs = new JsonObject();
        headingAttrs.addProperty("level", 2);
        heading.add("attrs", headingAttrs);
        JsonArray headingContent = new JsonArray();
        JsonObject text = new JsonObject();
        text.addProperty("text", "Section Heading");
        headingContent.add(text);
        heading.add("content", headingContent);
        content.add(heading);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("Section Heading"));
    }

    @Test
    @DisplayName("Should not cut off content following a heading or bulletList")
    void testGetDescription_HeadingBulletListParagraph_NotCutOff() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        // Intro paragraph
        JsonObject intro = new JsonObject();
        intro.addProperty("type", "paragraph");
        JsonArray introContent = new JsonArray();
        JsonObject introText = new JsonObject();
        introText.addProperty("text", "Intro paragraph");
        introContent.add(introText);
        intro.add("content", introContent);
        content.add(intro);

        // Heading
        JsonObject heading = new JsonObject();
        heading.addProperty("type", "heading");
        JsonObject headingAttrs = new JsonObject();
        headingAttrs.addProperty("level", 2);
        heading.add("attrs", headingAttrs);
        JsonArray headingContent = new JsonArray();
        JsonObject headingText = new JsonObject();
        headingText.addProperty("text", "Steps");
        headingContent.add(headingText);
        heading.add("content", headingContent);
        content.add(heading);

        // Bullet list
        JsonObject bulletList = new JsonObject();
        bulletList.addProperty("type", "bulletList");
        JsonArray listItems = new JsonArray();
        JsonObject listItem = new JsonObject();
        JsonArray itemContent = new JsonArray();
        JsonObject bulletParagraph = new JsonObject();
        bulletParagraph.addProperty("type", "paragraph");
        JsonArray bulletParagraphContent = new JsonArray();
        JsonObject bulletText = new JsonObject();
        bulletText.addProperty("text", "Do the first thing");
        bulletParagraphContent.add(bulletText);
        bulletParagraph.add("content", bulletParagraphContent);
        itemContent.add(bulletParagraph);
        listItem.add("content", itemContent);
        listItems.add(listItem);
        bulletList.add("content", listItems);
        content.add(bulletList);

        // Closing paragraph
        JsonObject closing = new JsonObject();
        closing.addProperty("type", "paragraph");
        JsonArray closingContent = new JsonArray();
        JsonObject closingText = new JsonObject();
        closingText.addProperty("text", "Closing paragraph");
        closingContent.add(closingText);
        closing.add("content", closingContent);
        content.add(closing);

        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("Intro paragraph"));
        assertTrue(issue.description.contains("Steps"));
        assertTrue(issue.description.contains("Do the first thing"));
        assertTrue(issue.description.contains("Closing paragraph"));
    }

    @Test
    @DisplayName("Should keep bulletList items with bold text and inline email links (CS-8821)")
    void testGetDescription_BulletListWithMarkedTextAndLinks() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        // Intro paragraph
        JsonObject intro = new JsonObject();
        intro.addProperty("type", "paragraph");
        JsonArray introContent = new JsonArray();
        JsonObject introText = new JsonObject();
        introText.addProperty("type", "text");
        introText.addProperty("text", "Could you please provide access for the following team members?");
        introContent.add(introText);
        intro.add("content", introContent);
        content.add(intro);

        // Bullet list with two members, each: bold name + separator + linked email
        JsonObject bulletList = new JsonObject();
        bulletList.addProperty("type", "bulletList");
        JsonArray listItems = new JsonArray();
        listItems.add(memberListItem("David McKay (Garg)", " – ", "testuser@abc.com"));
        listItems.add(memberListItem("Jenny Da (Garg)", " - ", "jda@xyz.com"));
        bulletList.add("content", listItems);
        content.add(bulletList);

        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "CS-8821", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertTrue(issue.description.contains("David McKay (Garg)"));
        assertTrue(issue.description.contains("testuser@abc.com"));
        assertTrue(issue.description.contains("Jenny Da (Garg)"));
        assertTrue(issue.description.contains("jda@xyz.com"));
    }

    private JsonObject memberListItem(String boldName, String separator, String email) {
        JsonObject listItem = new JsonObject();
        JsonArray itemContent = new JsonArray();

        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();

        // Bold name text node
        JsonObject nameNode = new JsonObject();
        nameNode.addProperty("type", "text");
        nameNode.addProperty("text", boldName);
        JsonArray boldMarks = new JsonArray();
        JsonObject strong = new JsonObject();
        strong.addProperty("type", "strong");
        boldMarks.add(strong);
        nameNode.add("marks", boldMarks);
        paragraphContent.add(nameNode);

        // Separator text node
        JsonObject separatorNode = new JsonObject();
        separatorNode.addProperty("type", "text");
        separatorNode.addProperty("text", separator);
        paragraphContent.add(separatorNode);

        // Linked email text node (text with a link mark)
        JsonObject emailNode = new JsonObject();
        emailNode.addProperty("type", "text");
        emailNode.addProperty("text", email);
        JsonArray linkMarks = new JsonArray();
        JsonObject link = new JsonObject();
        link.addProperty("type", "link");
        JsonObject linkAttrs = new JsonObject();
        linkAttrs.addProperty("href", "mailto:" + email);
        link.add("attrs", linkAttrs);
        linkMarks.add(link);
        emailNode.add("marks", linkMarks);
        paragraphContent.add(emailNode);

        paragraph.add("content", paragraphContent);
        itemContent.add(paragraph);
        listItem.add("content", itemContent);
        return listItem;
    }

    @Test
    @DisplayName("Should keep inline text nodes of a paragraph on a single line")
    void testGetDescription_InlineNodesJoinedOnOneLine() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        paragraphContent.add(textNode("Hello "));
        paragraphContent.add(textNode("beautiful "));
        paragraphContent.add(textNode("world"));
        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("Hello beautiful world", issue.description);
    }

    @Test
    @DisplayName("Should prefix bullet list items with a bullet marker")
    void testGetDescription_BulletListMarker() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject bulletList = new JsonObject();
        bulletList.addProperty("type", "bulletList");
        JsonArray listItems = new JsonArray();
        listItems.add(memberListItem("David McKay (Garg)", " – ", "testuser@abc.com"));
        listItems.add(memberListItem("Jenny Da (Garg)", " - ", "jda@xyz.com"));
        bulletList.add("content", listItems);
        content.add(bulletList);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "CS-8821", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("- David McKay (Garg) – testuser@abc.com\n"
                + "- Jenny Da (Garg) - jda@xyz.com", issue.description);
    }

    @Test
    @DisplayName("Should prefix ordered list items with sequential number markers")
    void testGetDescription_OrderedListMarker() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject orderedList = new JsonObject();
        orderedList.addProperty("type", "orderedList");
        JsonArray listItems = new JsonArray();
        listItems.add(simpleListItem("First"));
        listItems.add(simpleListItem("Second"));
        listItems.add(simpleListItem("Third"));
        orderedList.add("content", listItems);
        content.add(orderedList);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("1. First\n2. Second\n3. Third", issue.description);
    }

    @Test
    @DisplayName("Should use Jira's rendered HTML description when available (CS-8821)")
    void testGetDescription_PrefersRenderedHtml() {
        JsonObject fields = new JsonObject();
        // Raw ADF is intentionally minimal to prove the rendered HTML path is used instead.
        JsonObject adfDescription = new JsonObject();
        JsonArray adfContent = new JsonArray();
        JsonObject adfParagraph = new JsonObject();
        adfParagraph.addProperty("type", "paragraph");
        JsonArray adfParagraphContent = new JsonArray();
        adfParagraphContent.add(textNode("raw adf fallback"));
        adfParagraph.add("content", adfParagraphContent);
        adfContent.add(adfParagraph);
        adfDescription.add("content", adfContent);
        fields.add("description", adfDescription);

        JsonObject renderedFields = new JsonObject();
        renderedFields.addProperty("description",
                "<p>Please provide access for the following team members?</p>"
                        + "<ul>"
                        + "<li><p><strong>David McKay (Garg)</strong> – <a href=\"mailto:testuser@abc.com\">testuser@abc.com</a></p></li>"
                        + "<li><p><strong>Jenny Da (Garg)</strong> – <a href=\"mailto:jda@xyz.com\">jda@xyz.com</a></p></li>"
                        + "</ul>");

        JiraIssue jiraIssue = new JiraIssue("1", "CS-8821", fields, renderedFields);
        Issue issue = jiraIssue.getIssue();

        // Rendered HTML is used (not the raw ADF), and the list/bold/link structure is preserved.
        assertFalse(issue.description.contains("raw adf fallback"));
        assertTrue(issue.description.contains("<ul>"));
        assertTrue(issue.description.contains("<li>"));
        assertTrue(issue.description.contains("<strong>David McKay (Garg)</strong>"));
        assertTrue(issue.description.contains("<a href=\"mailto:testuser@abc.com\">testuser@abc.com</a>"));
    }

    @Test
    @DisplayName("Should fall back to ADF parsing when rendered HTML is absent")
    void testGetDescription_FallsBackToAdf() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        paragraphContent.add(textNode("Fallback description"));
        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("Fallback description", issue.description);
    }

    @Test
    @DisplayName("Should separate a paragraph and a following bullet list with a blank line (CS-8821 layout)")
    void testGetDescription_ParagraphThenBulletList_BlankLineSeparated() {
        JsonObject fields = new JsonObject();
        JsonObject description = new JsonObject();
        JsonArray content = new JsonArray();

        JsonObject intro = new JsonObject();
        intro.addProperty("type", "paragraph");
        JsonArray introContent = new JsonArray();
        introContent.add(textNode("Please provide access for the following team members?"));
        intro.add("content", introContent);
        content.add(intro);

        JsonObject bulletList = new JsonObject();
        bulletList.addProperty("type", "bulletList");
        JsonArray listItems = new JsonArray();
        listItems.add(memberListItem("David McKay (Garg)", " – ", "testuser@abc.com"));
        listItems.add(memberListItem("Jenny Da (Garg)", " – ", "jda@xyz.com"));
        bulletList.add("content", listItems);
        content.add(bulletList);

        description.add("content", content);
        fields.add("description", description);

        JiraIssue jiraIssue = new JiraIssue("1", "CS-8821", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("Please provide access for the following team members?\n\n"
                + "- David McKay (Garg) – testuser@abc.com\n"
                + "- Jenny Da (Garg) – jda@xyz.com", issue.description);
    }

    private JsonObject textNode(String text) {
        JsonObject node = new JsonObject();
        node.addProperty("type", "text");
        node.addProperty("text", text);
        return node;
    }

    private JsonObject simpleListItem(String text) {
        JsonObject listItem = new JsonObject();
        JsonArray itemContent = new JsonArray();
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");
        JsonArray paragraphContent = new JsonArray();
        paragraphContent.add(textNode(text));
        paragraph.add("content", paragraphContent);
        itemContent.add(paragraph);
        listItem.add("content", itemContent);
        return listItem;
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

