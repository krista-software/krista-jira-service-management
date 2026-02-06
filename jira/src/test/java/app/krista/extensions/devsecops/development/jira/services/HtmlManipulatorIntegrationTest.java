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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HtmlManipulator Integration Tests")
class HtmlManipulatorIntegrationTest {

    // ==================== handleUnorderedLists Tests ====================

    @Test
    @DisplayName("Should convert basic unordered list to text")
    void testHandleUnorderedLists_Basic() {
        String html = "<ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>";
        
        String result = HtmlManipulator.handleUnorderedLists(html);
        
        assertTrue(result.contains("- Item 1<br>"));
        assertTrue(result.contains("- Item 2<br>"));
        assertTrue(result.contains("- Item 3<br>"));
    }

    @Test
    @DisplayName("Should handle multiple unordered lists")
    void testHandleUnorderedLists_Multiple() {
        String html = "<ul><li>First list item 1</li><li>First list item 2</li></ul>" +
                     "<p>Some text</p>" +
                     "<ul><li>Second list item 1</li><li>Second list item 2</li></ul>";
        
        String result = HtmlManipulator.handleUnorderedLists(html);
        
        assertTrue(result.contains("- First list item 1<br>"));
        assertTrue(result.contains("- First list item 2<br>"));
        assertTrue(result.contains("- Second list item 1<br>"));
        assertTrue(result.contains("- Second list item 2<br>"));
        assertTrue(result.contains("Some text"));
    }

    @Test
    @DisplayName("Should handle nested unordered lists")
    void testHandleUnorderedLists_Nested() {
        String html = "<ul><li>Parent 1<ul><li>Child 1</li><li>Child 2</li></ul></li><li>Parent 2</li></ul>";
        
        String result = HtmlManipulator.handleUnorderedLists(html);
        
        assertTrue(result.contains("- Parent 1"));
        assertTrue(result.contains("- Child 1"));
        assertTrue(result.contains("- Child 2"));
        assertTrue(result.contains("- Parent 2"));
    }

    @Test
    @DisplayName("Should handle empty unordered list")
    void testHandleUnorderedLists_Empty() {
        String html = "<ul></ul>";
        
        String result = HtmlManipulator.handleUnorderedLists(html);
        
        assertNotNull(result);
        assertFalse(result.contains("<li>"));
    }

    @Test
    @DisplayName("Should handle unordered list with special characters")
    void testHandleUnorderedLists_SpecialCharacters() {
        String html = "<ul><li>Item with &amp; ampersand</li><li>Item with &lt; less than</li><li>Item with &gt; greater than</li></ul>";

        String result = HtmlManipulator.handleUnorderedLists(html);

        // Jsoup keeps HTML entities encoded in the output
        assertTrue(result.contains("- Item with &amp; ampersand<br>"));
        assertTrue(result.contains("- Item with &lt; less than<br>"));
        assertTrue(result.contains("- Item with &gt; greater than<br>"));
    }

    @Test
    @DisplayName("Should handle unordered list with HTML content in items")
    void testHandleUnorderedLists_HtmlContent() {
        String html = "<ul><li><strong>Bold item</strong></li><li><em>Italic item</em></li><li><a href='#'>Link item</a></li></ul>";
        
        String result = HtmlManipulator.handleUnorderedLists(html);
        
        assertTrue(result.contains("- Bold item<br>"));
        assertTrue(result.contains("- Italic item<br>"));
        assertTrue(result.contains("- Link item<br>"));
    }

    @Test
    @DisplayName("Should handle empty string input for unordered lists")
    void testHandleUnorderedLists_EmptyString() {
        String html = "";
        
        String result = HtmlManipulator.handleUnorderedLists(html);
        
        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should handle HTML without unordered lists")
    void testHandleUnorderedLists_NoLists() {
        String html = "<p>Just a paragraph</p><div>And a div</div>";
        
        String result = HtmlManipulator.handleUnorderedLists(html);
        
        assertTrue(result.contains("Just a paragraph"));
        assertTrue(result.contains("And a div"));
        assertFalse(result.contains("<ul>"));
    }

    @Test
    @DisplayName("Should handle unordered list with single item")
    void testHandleUnorderedLists_SingleItem() {
        String html = "<ul><li>Only one item</li></ul>";
        
        String result = HtmlManipulator.handleUnorderedLists(html);
        
        assertTrue(result.contains("- Only one item<br>"));
    }

    @Test
    @DisplayName("Should handle unordered list with whitespace in items")
    void testHandleUnorderedLists_Whitespace() {
        String html = "<ul><li>  Item with spaces  </li><li>\n\tItem with tabs\n</li></ul>";
        
        String result = HtmlManipulator.handleUnorderedLists(html);
        
        assertTrue(result.contains("- Item with spaces<br>"));
        assertTrue(result.contains("- Item with tabs<br>"));
    }

    // ==================== handleOrderedList Tests ====================

    @Test
    @DisplayName("Should convert basic ordered list to text")
    void testHandleOrderedList_Basic() {
        String html = "<ol><li>First item</li><li>Second item</li><li>Third item</li></ol>";
        
        String result = HtmlManipulator.handleOrderedList(html);
        
        assertTrue(result.contains("1. First item<br>"));
        assertTrue(result.contains("2. Second item<br>"));
        assertTrue(result.contains("3. Third item<br>"));
    }

    @Test
    @DisplayName("Should handle multiple ordered lists")
    void testHandleOrderedList_Multiple() {
        String html = "<ol><li>First list item 1</li><li>First list item 2</li></ol>" +
                     "<p>Some text</p>" +
                     "<ol><li>Second list item 1</li><li>Second list item 2</li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertTrue(result.contains("1. First list item 1<br>"));
        assertTrue(result.contains("2. First list item 2<br>"));
        assertTrue(result.contains("1. Second list item 1<br>"));
        assertTrue(result.contains("2. Second list item 2<br>"));
        assertTrue(result.contains("Some text"));
    }

    @Test
    @DisplayName("Should handle nested ordered lists")
    void testHandleOrderedList_Nested() {
        String html = "<ol><li>Parent 1<ol><li>Child 1</li><li>Child 2</li></ol></li><li>Parent 2</li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        // Nested lists are processed, so we check that all items appear
        assertTrue(result.contains("Parent 1"));
        assertTrue(result.contains("Child 1"));
        assertTrue(result.contains("Child 2"));
        assertTrue(result.contains("Parent 2"));
    }

    @Test
    @DisplayName("Should handle empty ordered list")
    void testHandleOrderedList_Empty() {
        String html = "<ol></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertNotNull(result);
        assertFalse(result.contains("<li>"));
    }

    @Test
    @DisplayName("Should handle ordered list with special characters")
    void testHandleOrderedList_SpecialCharacters() {
        String html = "<ol><li>Item with &amp; ampersand</li><li>Item with &lt; less than</li><li>Item with &gt; greater than</li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        // Jsoup keeps HTML entities encoded in the output
        assertTrue(result.contains("1. Item with &amp; ampersand<br>"));
        assertTrue(result.contains("2. Item with &lt; less than<br>"));
        assertTrue(result.contains("3. Item with &gt; greater than<br>"));
    }

    @Test
    @DisplayName("Should handle ordered list with HTML content in items")
    void testHandleOrderedList_HtmlContent() {
        String html = "<ol><li><strong>Bold item</strong></li><li><em>Italic item</em></li><li><a href='#'>Link item</a></li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertTrue(result.contains("1. Bold item<br>"));
        assertTrue(result.contains("2. Italic item<br>"));
        assertTrue(result.contains("3. Link item<br>"));
    }

    @Test
    @DisplayName("Should handle empty string input for ordered lists")
    void testHandleOrderedList_EmptyString() {
        String html = "";

        String result = HtmlManipulator.handleOrderedList(html);

        assertNotNull(result);
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should handle HTML without ordered lists")
    void testHandleOrderedList_NoLists() {
        String html = "<p>Just a paragraph</p><div>And a div</div>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertTrue(result.contains("Just a paragraph"));
        assertTrue(result.contains("And a div"));
        assertFalse(result.contains("<ol>"));
    }

    @Test
    @DisplayName("Should handle ordered list with single item")
    void testHandleOrderedList_SingleItem() {
        String html = "<ol><li>Only one item</li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertTrue(result.contains("1. Only one item<br>"));
    }

    @Test
    @DisplayName("Should handle ordered list with whitespace in items")
    void testHandleOrderedList_Whitespace() {
        String html = "<ol><li>  Item with spaces  </li><li>\n\tItem with tabs\n</li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertTrue(result.contains("1. Item with spaces<br>"));
        assertTrue(result.contains("2. Item with tabs<br>"));
    }

    @Test
    @DisplayName("Should handle ordered list with many items")
    void testHandleOrderedList_ManyItems() {
        StringBuilder htmlBuilder = new StringBuilder("<ol>");
        for (int i = 1; i <= 15; i++) {
            htmlBuilder.append("<li>Item ").append(i).append("</li>");
        }
        htmlBuilder.append("</ol>");

        String result = HtmlManipulator.handleOrderedList(htmlBuilder.toString());

        assertTrue(result.contains("1. Item 1<br>"));
        assertTrue(result.contains("10. Item 10<br>"));
        assertTrue(result.contains("15. Item 15<br>"));
    }

    // ==================== Mixed and Edge Case Tests ====================

    @Test
    @DisplayName("Should handle HTML with both ordered and unordered lists")
    void testMixedLists() {
        String html = "<ul><li>Unordered item</li></ul><ol><li>Ordered item</li></ol>";

        String resultUl = HtmlManipulator.handleUnorderedLists(html);
        String resultOl = HtmlManipulator.handleOrderedList(html);

        assertTrue(resultUl.contains("- Unordered item<br>"));
        assertTrue(resultOl.contains("1. Ordered item<br>"));
    }

    @Test
    @DisplayName("Should handle unordered list with empty list items")
    void testHandleUnorderedLists_EmptyItems() {
        String html = "<ul><li></li><li>Item 2</li><li></li></ul>";

        String result = HtmlManipulator.handleUnorderedLists(html);

        assertTrue(result.contains("- <br>"));
        assertTrue(result.contains("- Item 2<br>"));
    }

    @Test
    @DisplayName("Should handle ordered list with empty list items")
    void testHandleOrderedList_EmptyItems() {
        String html = "<ol><li></li><li>Item 2</li><li></li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertTrue(result.contains("1. <br>"));
        assertTrue(result.contains("2. Item 2<br>"));
        assertTrue(result.contains("3. <br>"));
    }

    @Test
    @DisplayName("Should handle complex nested structure with unordered lists")
    void testHandleUnorderedLists_ComplexNested() {
        String html = "<div><p>Intro</p><ul><li>Level 1 - Item 1<ul><li>Level 2 - Item 1</li></ul></li><li>Level 1 - Item 2</li></ul><p>Outro</p></div>";

        String result = HtmlManipulator.handleUnorderedLists(html);

        assertTrue(result.contains("Intro"));
        assertTrue(result.contains("- Level 1 - Item 1"));
        assertTrue(result.contains("- Level 2 - Item 1"));
        assertTrue(result.contains("- Level 1 - Item 2"));
        assertTrue(result.contains("Outro"));
    }

    @Test
    @DisplayName("Should handle complex nested structure with ordered lists")
    void testHandleOrderedList_ComplexNested() {
        String html = "<div><p>Intro</p><ol><li>Level 1 - Item 1<ol><li>Level 2 - Item 1</li></ol></li><li>Level 1 - Item 2</li></ol><p>Outro</p></div>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertTrue(result.contains("Intro"));
        assertTrue(result.contains("Level 1 - Item 1"));
        assertTrue(result.contains("Level 2 - Item 1"));
        assertTrue(result.contains("Level 1 - Item 2"));
        assertTrue(result.contains("Outro"));
    }

    @Test
    @DisplayName("Should verify constants are used correctly")
    void testConstants() {
        assertEquals("ol", HtmlManipulator.OL);
        assertEquals("li", HtmlManipulator.LI);
        assertEquals("<br>", HtmlManipulator.BR);
    }

    @Test
    @DisplayName("Should handle unordered list with line breaks in content")
    void testHandleUnorderedLists_LineBreaks() {
        String html = "<ul><li>Item with<br>line break</li><li>Normal item</li></ul>";

        String result = HtmlManipulator.handleUnorderedLists(html);

        assertTrue(result.contains("- Item with"));
        assertTrue(result.contains("line break<br>"));
        assertTrue(result.contains("- Normal item<br>"));
    }

    @Test
    @DisplayName("Should handle ordered list with line breaks in content")
    void testHandleOrderedList_LineBreaks() {
        String html = "<ol><li>Item with<br>line break</li><li>Normal item</li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertTrue(result.contains("1. Item with"));
        assertTrue(result.contains("line break<br>"));
        assertTrue(result.contains("2. Normal item<br>"));
    }

    @Test
    @DisplayName("Should handle unordered list with unicode characters")
    void testHandleUnorderedLists_Unicode() {
        String html = "<ul><li>Item with emoji 😀</li><li>Item with Chinese 中文</li><li>Item with Arabic العربية</li></ul>";

        String result = HtmlManipulator.handleUnorderedLists(html);

        assertTrue(result.contains("- Item with emoji 😀<br>"));
        assertTrue(result.contains("- Item with Chinese 中文<br>"));
        assertTrue(result.contains("- Item with Arabic العربية<br>"));
    }

    @Test
    @DisplayName("Should handle ordered list with unicode characters")
    void testHandleOrderedList_Unicode() {
        String html = "<ol><li>Item with emoji 😀</li><li>Item with Chinese 中文</li><li>Item with Arabic العربية</li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertTrue(result.contains("1. Item with emoji 😀<br>"));
        assertTrue(result.contains("2. Item with Chinese 中文<br>"));
        assertTrue(result.contains("3. Item with Arabic العربية<br>"));
    }

    @Test
    @DisplayName("Should handle unordered list with quotes and apostrophes")
    void testHandleUnorderedLists_Quotes() {
        String html = "<ul><li>Item with \"double quotes\"</li><li>Item with 'single quotes'</li><li>Item with apostrophe's</li></ul>";

        String result = HtmlManipulator.handleUnorderedLists(html);

        assertTrue(result.contains("- Item with \"double quotes\"<br>"));
        assertTrue(result.contains("- Item with 'single quotes'<br>"));
        assertTrue(result.contains("- Item with apostrophe's<br>"));
    }

    @Test
    @DisplayName("Should handle ordered list with quotes and apostrophes")
    void testHandleOrderedList_Quotes() {
        String html = "<ol><li>Item with \"double quotes\"</li><li>Item with 'single quotes'</li><li>Item with apostrophe's</li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        assertTrue(result.contains("1. Item with \"double quotes\"<br>"));
        assertTrue(result.contains("2. Item with 'single quotes'<br>"));
        assertTrue(result.contains("3. Item with apostrophe's<br>"));
    }

    @Test
    @DisplayName("Should handle deeply nested unordered lists")
    void testHandleUnorderedLists_DeeplyNested() {
        String html = "<ul><li>L1<ul><li>L2<ul><li>L3</li></ul></li></ul></li></ul>";

        String result = HtmlManipulator.handleUnorderedLists(html);

        assertTrue(result.contains("- L1"));
        assertTrue(result.contains("- L2"));
        assertTrue(result.contains("- L3"));
    }

    @Test
    @DisplayName("Should handle deeply nested ordered lists")
    void testHandleOrderedList_DeeplyNested() {
        String html = "<ol><li>L1<ol><li>L2<ol><li>L3</li></ol></li></ol></li></ol>";

        String result = HtmlManipulator.handleOrderedList(html);

        // All levels are processed
        assertTrue(result.contains("L1"));
        assertTrue(result.contains("L2"));
        assertTrue(result.contains("L3"));
    }
}
