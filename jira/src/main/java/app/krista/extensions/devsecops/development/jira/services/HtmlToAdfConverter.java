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

import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.List;

/**
 * A class that converts HTML to ADF (Atlassian Document Format). This class is designed to work with simple HTML
 * that includes paragraphs and supported tags (currently "b", "i", and "u").
 */
public class HtmlToAdfConverter {
    public static final String B = "b";
    public static final String I = "i";
    public static final String U = "u";
    private static final List<String> SUPPORTED_TAGS = Arrays.asList(B, I, U);
    public static final String VERSION = "version";
    public static final String DOC = "doc";
    public static final String P_OPENTAG = "<p>";
    public static final String P_CLOSETAG = "</p>";
    public static final String CONTENT = "content";
    public static final String P = "p";
    public static final String PARAGRAPH = "paragraph";
    public static final String NODE = "Node: ";
    public static final String TEXT = "text";
    public static final String TEXT_NODE = "TextNode: ";
    public static final String MARKS = "marks";

    /**
     * Converts a string of HTML into ADF.
     * If the HTML does not contain a paragraph element, it is wrapped with paragraph tags.
     *
     * @param html The HTML to be converted to ADF.
     * @return A JsonObject representing the HTML in ADF.
     */
    public JsonObject convert(String html) {
        Document document = Jsoup.parse(html);
        if (document.select(P).isEmpty()) {
            html = P_OPENTAG + html + P_CLOSETAG;
            document = Jsoup.parse(html);  // re-parse the updated html
        }
        JsonObject adf = new JsonObject();

        adf.addProperty(JiraConstants._TYPE, DOC);
        adf.addProperty(VERSION, 1);

        JsonArray content = new JsonArray();
        adf.add(CONTENT, content);

        Elements paragraphs = document.select(P);
        for (Element paragraph : paragraphs) {
            content.add(processElement(paragraph));
        }

        return adf;
    }

    /**
     * Process an Element as a paragraph, adding its child nodes to a content array.
     *
     * @param element The Element to process.
     * @return A JsonObject representing the processed Element as a paragraph.
     */
    private JsonObject processElement(Element element) {
        JsonObject paragraphJson = new JsonObject();
        paragraphJson.addProperty(JiraConstants._TYPE, PARAGRAPH);

        JsonArray contentArray = new JsonArray();
        for (Node childNode : element.childNodes()) {
            JsonObject nodeJson = processNode(childNode);
            contentArray.add(nodeJson);
        }
        paragraphJson.add(CONTENT, contentArray);

        return paragraphJson;
    }

    /**
     * Process a Node and return a corresponding JsonObject.
     * If the Node is a TextNode, it is processed as such.
     * If the Node is an Element, it is processed as such.
     * If the Node is neither, an empty JsonObject is returned.
     *
     * @param node Node to be processed.
     * @return A JsonObject representing the processed Node.
     */
    private JsonObject processNode(Node node) {
        if (node instanceof TextNode) {
            return processTextNode((TextNode) node);
        } else if (node instanceof Element) {
            return processElementNode((Element) node);
        }

        return new JsonObject();  // Return an empty JsonObject if the node is neither a TextNode nor an Element
    }

    /**
     * Process a TextNode and return a JsonObject representing the node.
     *
     * @param textNode TextNode to be processed.
     * @return A JsonObject representing the processed TextNode.
     */
    private JsonObject processTextNode(TextNode textNode) {
        JsonObject nodeJson = new JsonObject();
        nodeJson.addProperty(JiraConstants._TYPE, TEXT);
        nodeJson.addProperty(TEXT, textNode.getWholeText());
        return nodeJson;
    }

    /**
     * Process an Element and return a JsonObject representing the node.
     * If the Element's tag name is supported, its children nodes are processed and a JsonObject with marks is returned.
     * Otherwise, the Element is processed as a paragraph.
     *
     * @param element Element to be processed.
     * @return A JsonObject representing the processed Element.
     */
    private JsonObject processElementNode(Element element) {
        JsonObject nodeJson = new JsonObject();
        String tagName = element.tagName();

        if (SUPPORTED_TAGS.contains(tagName)) {
            nodeJson.addProperty(JiraConstants._TYPE, TEXT);

            JsonArray marksArray = new JsonArray();
            addMark(marksArray, tagName);
            nodeJson.add(MARKS, marksArray);

            nodeJson.addProperty(TEXT, processElementChildrenAndReturnText(element, marksArray));

        } else {
            nodeJson = processElement(element);
        }

        return nodeJson;
    }

    /**
     * Process the children of an Element and return the text content as a string.
     * The JsonObjects representing the children nodes are also added to the given marks array.
     *
     * @param element    Element whose children are to be processed.
     * @param marksArray JsonArray to which the children's JsonObjects are added.
     * @return A String representing the text content of the children nodes.
     */
    private String processElementChildrenAndReturnText(Element element, JsonArray marksArray) {
        StringBuilder textContent = new StringBuilder();
        for (Node childNode : element.childNodes()) {
            JsonObject childNodeJson = processNode(childNode);
            if (childNodeJson.has(MARKS)) {
                for (JsonElement markElement : childNodeJson.getAsJsonArray(MARKS)) {
                    marksArray.add(markElement);
                }
            }
            if (childNodeJson.has(TEXT)) {
                textContent.append(childNodeJson.get(TEXT).getAsString());
            }
        }
        return textContent.toString();
    }

    /**
     * Add a mark to a JsonArray of marks based on the tag name.
     * The following tag names are supported: "b" (results in a "strong" mark), "i" (results in an "em" mark),
     * and "u" (results in an "underline" mark).
     *
     * @param marksArray The JsonArray to which the mark is to be added.
     * @param tagName    The tag name based on which the mark is to be added.
     */
    private void addMark(JsonArray marksArray, String tagName) {
        JsonObject mark = new JsonObject();
        switch (tagName) {
            case B:
                mark.addProperty(JiraConstants._TYPE, JiraConstants.STRONG);
                break;
            case I:
                mark.addProperty(JiraConstants._TYPE, JiraConstants.EM);
                break;
            case U:
                mark.addProperty(JiraConstants._TYPE, JiraConstants.UNDERLINE);
                break;
        }
        marksArray.add(mark);
    }

}