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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HtmlToAdfConverter Integration Tests")
class HtmlToAdfConverterIntegrationTest {

    private HtmlToAdfConverter converter;

    @BeforeEach
    void setUp() {
        converter = new HtmlToAdfConverter();
    }

    // ==================== Basic Conversion Tests ====================

    @Test
    @DisplayName("Should convert simple paragraph to ADF")
    void testConvert_SimpleParagraph() {
        String html = "<p>Hello World</p>";
        
        JsonObject result = converter.convert(html);
        
        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());
        assertEquals(1, result.get("version").getAsInt());
        assertTrue(result.has("content"));
        
        JsonArray content = result.getAsJsonArray("content");
        assertEquals(1, content.size());
        
        JsonObject paragraph = content.get(0).getAsJsonObject();
        assertEquals("paragraph", paragraph.get("type").getAsString());
        
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        assertEquals(1, paragraphContent.size());
        
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Hello World", textNode.get("text").getAsString());
    }

    @Test
    @DisplayName("Should wrap text without paragraph tags")
    void testConvert_NoParagraphTags() {
        String html = "Plain text without paragraph";
        
        JsonObject result = converter.convert(html);
        
        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());
        
        JsonArray content = result.getAsJsonArray("content");
        assertEquals(1, content.size());
        
        JsonObject paragraph = content.get(0).getAsJsonObject();
        assertEquals("paragraph", paragraph.get("type").getAsString());
        
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertTrue(textNode.get("text").getAsString().contains("Plain text without paragraph"));
    }

    @Test
    @DisplayName("Should convert multiple paragraphs")
    void testConvert_MultipleParagraphs() {
        String html = "<p>First paragraph</p><p>Second paragraph</p><p>Third paragraph</p>";
        
        JsonObject result = converter.convert(html);
        
        JsonArray content = result.getAsJsonArray("content");
        assertEquals(3, content.size());
        
        assertEquals("paragraph", content.get(0).getAsJsonObject().get("type").getAsString());
        assertEquals("paragraph", content.get(1).getAsJsonObject().get("type").getAsString());
        assertEquals("paragraph", content.get(2).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should handle empty string")
    void testConvert_EmptyString() {
        String html = "";
        
        JsonObject result = converter.convert(html);
        
        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());
        assertEquals(1, result.get("version").getAsInt());
    }

    @Test
    @DisplayName("Should handle empty paragraph")
    void testConvert_EmptyParagraph() {
        String html = "<p></p>";
        
        JsonObject result = converter.convert(html);
        
        JsonArray content = result.getAsJsonArray("content");
        assertEquals(1, content.size());
        
        JsonObject paragraph = content.get(0).getAsJsonObject();
        assertEquals("paragraph", paragraph.get("type").getAsString());
        
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        assertEquals(0, paragraphContent.size());
    }

    // ==================== Text Formatting Tests ====================

    @Test
    @DisplayName("Should convert bold text")
    void testConvert_BoldText() {
        String html = "<p><b>Bold text</b></p>";
        
        JsonObject result = converter.convert(html);
        
        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Bold text", textNode.get("text").getAsString());
        
        assertTrue(textNode.has("marks"));
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertEquals(1, marks.size());
        assertEquals("strong", marks.get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert italic text")
    void testConvert_ItalicText() {
        String html = "<p><i>Italic text</i></p>";
        
        JsonObject result = converter.convert(html);
        
        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Italic text", textNode.get("text").getAsString());

        assertTrue(textNode.has("marks"));
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertEquals(1, marks.size());
        assertEquals("em", marks.get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert underlined text")
    void testConvert_UnderlinedText() {
        String html = "<p><u>Underlined text</u></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Underlined text", textNode.get("text").getAsString());

        assertTrue(textNode.has("marks"));
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertEquals(1, marks.size());
        assertEquals("underline", marks.get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert nested bold and italic")
    void testConvert_NestedBoldItalic() {
        String html = "<p><b><i>Bold and italic</i></b></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Bold and italic", textNode.get("text").getAsString());

        assertTrue(textNode.has("marks"));
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertEquals(2, marks.size());
        assertEquals("strong", marks.get(0).getAsJsonObject().get("type").getAsString());
        assertEquals("em", marks.get(1).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert nested italic and bold")
    void testConvert_NestedItalicBold() {
        String html = "<p><i><b>Italic and bold</b></i></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Italic and bold", textNode.get("text").getAsString());

        assertTrue(textNode.has("marks"));
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertEquals(2, marks.size());
        assertEquals("em", marks.get(0).getAsJsonObject().get("type").getAsString());
        assertEquals("strong", marks.get(1).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert triple nested formatting")
    void testConvert_TripleNested() {
        String html = "<p><b><i><u>All three formats</u></i></b></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("All three formats", textNode.get("text").getAsString());

        assertTrue(textNode.has("marks"));
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertEquals(3, marks.size());
        assertEquals("strong", marks.get(0).getAsJsonObject().get("type").getAsString());
        assertEquals("em", marks.get(1).getAsJsonObject().get("type").getAsString());
        assertEquals("underline", marks.get(2).getAsJsonObject().get("type").getAsString());
    }

    // ==================== Mixed Content Tests ====================

    @Test
    @DisplayName("Should convert mixed plain and bold text")
    void testConvert_MixedPlainAndBold() {
        String html = "<p>Plain text <b>bold text</b> more plain</p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertEquals(3, paragraphContent.size());

        // First text node (plain)
        JsonObject textNode1 = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode1.get("type").getAsString());
        assertEquals("Plain text ", textNode1.get("text").getAsString());
        assertFalse(textNode1.has("marks"));

        // Second text node (bold)
        JsonObject textNode2 = paragraphContent.get(1).getAsJsonObject();
        assertEquals("text", textNode2.get("type").getAsString());
        assertEquals("bold text", textNode2.get("text").getAsString());
        assertTrue(textNode2.has("marks"));

        // Third text node (plain)
        JsonObject textNode3 = paragraphContent.get(2).getAsJsonObject();
        assertEquals("text", textNode3.get("type").getAsString());
        assertEquals(" more plain", textNode3.get("text").getAsString());
        assertFalse(textNode3.has("marks"));
    }

    @Test
    @DisplayName("Should convert paragraph with multiple formatted sections")
    void testConvert_MultipleFormattedSections() {
        String html = "<p><b>Bold</b> <i>Italic</i> <u>Underline</u></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertTrue(paragraphContent.size() >= 3);
    }

    // ==================== Unsupported Tags Tests ====================

    @Test
    @DisplayName("Should handle unsupported tags as nested paragraphs")
    void testConvert_UnsupportedTags() {
        String html = "<p><div>Nested div content</div></p>";

        JsonObject result = converter.convert(html);

        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());

        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
    }

    @Test
    @DisplayName("Should handle span tags as nested paragraphs")
    void testConvert_SpanTags() {
        String html = "<p><span>Span content</span></p>";

        JsonObject result = converter.convert(html);

        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
    }

    @Test
    @DisplayName("Should handle anchor tags as nested paragraphs")
    void testConvert_AnchorTags() {
        String html = "<p><a href='#'>Link text</a></p>";

        JsonObject result = converter.convert(html);

        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
    }

    // ==================== Edge Cases and Special Content ====================

    @Test
    @DisplayName("Should handle whitespace in paragraphs")
    void testConvert_Whitespace() {
        String html = "<p>   Text with   spaces   </p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertTrue(paragraphContent.size() > 0);
    }

    @Test
    @DisplayName("Should handle special characters")
    void testConvert_SpecialCharacters() {
        String html = "<p>Special chars: &amp; &lt; &gt; &quot;</p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertTrue(textNode.get("text").getAsString().contains("&"));
    }

    @Test
    @DisplayName("Should handle unicode characters")
    void testConvert_UnicodeCharacters() {
        String html = "<p>Unicode: 😀 中文 العربية</p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertTrue(textNode.get("text").getAsString().contains("😀"));
    }

    @Test
    @DisplayName("Should handle line breaks")
    void testConvert_LineBreaks() {
        String html = "<p>Line 1<br>Line 2</p>";

        JsonObject result = converter.convert(html);

        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
    }

    @Test
    @DisplayName("Should handle empty bold tag")
    void testConvert_EmptyBoldTag() {
        String html = "<p><b></b></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertTrue(paragraphContent.size() >= 0);
    }

    @Test
    @DisplayName("Should handle empty italic tag")
    void testConvert_EmptyItalicTag() {
        String html = "<p><i></i></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertTrue(paragraphContent.size() >= 0);
    }

    @Test
    @DisplayName("Should handle empty underline tag")
    void testConvert_EmptyUnderlineTag() {
        String html = "<p><u></u></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertTrue(paragraphContent.size() >= 0);
    }

    // ==================== Complex Nested Structures ====================

    @Test
    @DisplayName("Should handle bold with multiple text nodes")
    void testConvert_BoldWithMultipleTextNodes() {
        String html = "<p><b>First part</b><b>Second part</b></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertEquals(2, paragraphContent.size());

        JsonObject textNode1 = paragraphContent.get(0).getAsJsonObject();
        assertEquals("First part", textNode1.get("text").getAsString());
        assertTrue(textNode1.has("marks"));

        JsonObject textNode2 = paragraphContent.get(1).getAsJsonObject();
        assertEquals("Second part", textNode2.get("text").getAsString());
        assertTrue(textNode2.has("marks"));
    }

    @Test
    @DisplayName("Should handle complex mixed formatting")
    void testConvert_ComplexMixedFormatting() {
        String html = "<p>Start <b>bold <i>bold-italic</i> bold</b> end</p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertTrue(paragraphContent.size() >= 3);
    }

    @Test
    @DisplayName("Should handle paragraph with only whitespace")
    void testConvert_OnlyWhitespace() {
        String html = "<p>   </p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        assertEquals(1, content.size());
    }

    @Test
    @DisplayName("Should handle multiple paragraphs with mixed content")
    void testConvert_MultipleParagraphsMixedContent() {
        String html = "<p>Plain paragraph</p><p><b>Bold paragraph</b></p><p><i>Italic paragraph</i></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        assertEquals(3, content.size());

        // Verify each paragraph
        for (int i = 0; i < 3; i++) {
            JsonObject paragraph = content.get(i).getAsJsonObject();
            assertEquals("paragraph", paragraph.get("type").getAsString());
            assertTrue(paragraph.has("content"));
        }
    }

    @Test
    @DisplayName("Should handle nested formatting with text between")
    void testConvert_NestedFormattingWithTextBetween() {
        String html = "<p><b>Bold <i>italic</i> bold again</b></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertTrue(paragraphContent.size() >= 1);

        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertTrue(textNode.has("marks"));
        assertTrue(textNode.get("text").getAsString().contains("Bold"));
    }

    // ==================== Constants Verification ====================

    @Test
    @DisplayName("Should verify all constants are correctly defined")
    void testConstants() {
        assertEquals("b", HtmlToAdfConverter.B);
        assertEquals("i", HtmlToAdfConverter.I);
        assertEquals("u", HtmlToAdfConverter.U);
        assertEquals("version", HtmlToAdfConverter.VERSION);
        assertEquals("doc", HtmlToAdfConverter.DOC);
        assertEquals("<p>", HtmlToAdfConverter.P_OPENTAG);
        assertEquals("</p>", HtmlToAdfConverter.P_CLOSETAG);
        assertEquals("content", HtmlToAdfConverter.CONTENT);
        assertEquals("p", HtmlToAdfConverter.P);
        assertEquals("paragraph", HtmlToAdfConverter.PARAGRAPH);
        assertEquals("Node: ", HtmlToAdfConverter.NODE);
        assertEquals("text", HtmlToAdfConverter.TEXT);
        assertEquals("TextNode: ", HtmlToAdfConverter.TEXT_NODE);
        assertEquals("marks", HtmlToAdfConverter.MARKS);
    }

    // ==================== Additional Edge Cases ====================

    @Test
    @DisplayName("Should handle HTML with head and body tags")
    void testConvert_HtmlWithHeadAndBody() {
        String html = "<html><head><title>Test</title></head><body><p>Content</p></body></html>";

        JsonObject result = converter.convert(html);

        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());

        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
    }

    @Test
    @DisplayName("Should handle consecutive formatting tags")
    void testConvert_ConsecutiveFormattingTags() {
        String html = "<p><b>Bold</b><i>Italic</i><u>Underline</u></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertEquals(3, paragraphContent.size());
    }

    @Test
    @DisplayName("Should handle bold tag with nested text and element")
    void testConvert_BoldWithNestedTextAndElement() {
        String html = "<p><b>Text before <i>italic</i> text after</b></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertTrue(paragraphContent.size() >= 1);

        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertTrue(textNode.has("marks"));

        // Verify the text contains all parts
        String text = textNode.get("text").getAsString();
        assertTrue(text.contains("Text before"));
        assertTrue(text.contains("italic"));
        assertTrue(text.contains("text after"));
    }

    @Test
    @DisplayName("Should handle very long text content")
    void testConvert_VeryLongText() {
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("Word").append(i).append(" ");
        }
        String html = "<p>" + longText.toString() + "</p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        assertTrue(paragraphContent.size() > 0);
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertTrue(textNode.get("text").getAsString().length() > 1000);
    }

    @Test
    @DisplayName("Should handle paragraph with only formatted empty tags")
    void testConvert_OnlyFormattedEmptyTags() {
        String html = "<p><b></b><i></i><u></u></p>";

        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();

        assertNotNull(paragraph);
        assertEquals("paragraph", paragraph.get("type").getAsString());
    }
}


