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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public final class JiraIssue {
    public static final String RENDERED_FIELDS = "renderedFields";
    public static final String FIELDS = "fields";
    public static final String KEY = "key";
    public static final String ID = "id";
    public static final String UPDATED = "updated";
    public final String id;
    public final String key;
    public final Object fields;
    public final Object renderedFields;

    private final Logger LOGGER = LoggerFactory.getLogger(JiraIssue.class);

    /**
     * DateTimeFormatter that handles Jira's date format variations.
     * Supports both standard ISO 8601 formats and Jira's non-standard timezone offsets.
     * Examples: "2026-02-12T11:19:51.462+05:30", "2026-02-12T11:19:51.462+0530", "2026-02-12T11:19:51.462Z"
     */
    private static final DateTimeFormatter JIRA_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendPattern("[.SSS][Z][XXX][XX]")
            .toFormatter();

    public JiraIssue(String id, String key, Object fields, Object renderedFields) {
        this.id = id;
        this.key = key;
        this.fields = fields;
        this.renderedFields = renderedFields;
    }

    public static JiraIssue create(JsonObject issueObject) {
        String id = issueObject.has(ID) ? issueObject.get(ID).getAsString() : null;
        String key = issueObject.has(KEY) ? issueObject.get(KEY).getAsString() : null;
        JsonElement fields = issueObject.has(FIELDS) ? issueObject.get(FIELDS) : null;
        JsonElement renderedFields = issueObject.has(RENDERED_FIELDS) ? issueObject.get(RENDERED_FIELDS) : null;

        return new JiraIssue(id, key, fields, renderedFields);
    }

    public Issue getIssue() {
        Issue issue = new Issue();
        try {
            issue.assigneeName = getAssignee();
            issue.comments = getComments();
            issue.description = getDescription();
            issue.key = key;
            issue.priority = getPriority();
            issue.projectKey = getProjectKey();
            issue.reporterName = getReporter();
            issue.status = getStatus();
            issue.issueType = getType();
            issue.summary = getSummary();
            issue.id = id;
            issue.parentKey = getParent();
            issue.labels = getLabels();
            issue.components = getComponents();
            issue.requestType = getRequestType();
            issue.lastModifiedDate = getLastModifiedDate();
            LOGGER.debug("Issue object populated - Key: {}, Last Modified Date: {}", issue.key, issue.lastModifiedDate);
        } catch (Exception e) {
            LOGGER.error("Error populating issue object for key {}: {}", key, e.getMessage(), e);
            return issue;
        }
        return issue;
    }

    private List<String> getComponents() {
        try {
            JsonArray jsonArray = ((JsonObject) this.fields).getAsJsonArray("components");
            List<String> components = new ArrayList<>();
            jsonArray.forEach(
                    jsonComponent -> components.add(jsonComponent.getAsJsonObject().get("name").getAsString()));
            return components;
        } catch (Exception cause) {
            return new ArrayList<>();
        }
    }

    private List<String> getLabels() {
        try {
            JsonArray jsonArray = this.fields == null ? null : ((JsonObject) this.fields).getAsJsonArray("labels");
            List<String> labels = new ArrayList<>();
            if (jsonArray != null) {
                jsonArray.forEach(jsonLabel -> labels.add(jsonLabel.getAsString()));
            }
            return labels;
        } catch (Exception cause) {
            LOGGER.debug(cause.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> getComments() {
        try {
            JsonArray jsonArray = ((JsonObject) this.fields).getAsJsonObject("comment").get("comments")
                    .getAsJsonArray();
            List<String> comments = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject bodyObject = jsonArray.get(i).getAsJsonObject().getAsJsonObject("body");
                JsonArray contentArray = bodyObject.getAsJsonArray("content");
                StringBuilder commentBuilder = new StringBuilder();
                for (int j = 0; j < contentArray.size(); j++) {
                    JsonObject contentObject = contentArray.get(j).getAsJsonObject();
                    JsonArray contentArray2 = (JsonArray) contentObject.get("content");
                    for (int k = 0; k < contentArray2.size(); k++) {
                        String text;
                        JsonObject jsonObject = contentArray2.get(k).getAsJsonObject();
                        if (jsonObject.has("text")) {
                            text = jsonObject.get("text").getAsString();
                        } else if (jsonObject.has("attrs")) {
                            JsonObject attrsObject = (JsonObject) jsonObject.get("attrs");
                            text = attrsObject.has("text") ? attrsObject.get("text").getAsString()
                                    : attrsObject.has("url") ? attrsObject.get("url").getAsString() : "";
                        } else {
                            text = "\n";
                        }
                        commentBuilder.append(text);
                    }
                }
                comments.add(commentBuilder.toString());
            }
            return comments;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    private String getAssignee() {
        try {
            return ((JsonObject) this.fields).get("assignee").getAsJsonObject().get("displayName").getAsString();
        } catch (Exception ignored) {
            return "NA";
        }
    }

    /**
     * Extracts the issue description as HTML.
     *
     * <p>The {@code Description} entity field is a {@code RichText} (HTML) field, so the
     * description is taken from Jira's own rendered HTML ({@code renderedFields.description},
     * returned via {@code expand=renderedFields}) and returned as sanitized HTML. Because Jira
     * renders the Atlassian Document Format itself, the original layout — paragraphs, bullet /
     * ordered lists, bold, links, headings, tables — is preserved faithfully and renders
     * natively in the RichText field instead of appearing as literal markup on one collapsed
     * line. Falls back to parsing the ADF document directly when the rendered HTML is
     * unavailable (e.g. it was not requested).
     *
     * @return The description as HTML, or an empty string if none is available or an error
     * occurs.
     */
    private String getDescription() {
        try {
            String renderedHtml = getRenderedDescriptionHtml();
            LOGGER.info("Rendered description HTML received for issue {}: {}", key, renderedHtml);
            if (renderedHtml != null && !renderedHtml.isBlank()) {
                String sanitized = Jsoup.clean(renderedHtml, Safelist.relaxed()).trim();
                LOGGER.info("Sanitized description HTML for issue {}: {}", key, sanitized);
                if (!sanitized.isBlank()) {
                    return sanitized;
                }
            }
            LOGGER.info("No rendered HTML description for issue {}; falling back to ADF parsing", key);
            return getDescriptionFromAdf();
        } catch (Exception cause) {
            LOGGER.debug("Error in getDescription method: {}", cause);
            return "";
        }
    }

    private String getRenderedDescriptionHtml() {
        if (this.renderedFields instanceof JsonElement rendered && rendered.isJsonObject()) {
            JsonObject renderedObject = rendered.getAsJsonObject();
            if (renderedObject.has(JiraConstants._DESCRIPTION)
                    && !renderedObject.get(JiraConstants._DESCRIPTION).isJsonNull()) {
                return renderedObject.get(JiraConstants._DESCRIPTION).getAsString();
            }
        }
        return null;
    }

    private String getDescriptionFromAdf() {
        StringBuilder descriptionBuilder = new StringBuilder();
        JsonObject descriptionObject = getDescriptionObject();
        if (descriptionObject != null) {
            JsonArray contentArray = descriptionObject.getAsJsonArray(JiraConstants.CONTENT);
            if (contentArray != null) {
                processContentArray(contentArray, descriptionBuilder);
            }
        }
        return descriptionBuilder.toString().trim();
    }

    private JsonObject getDescriptionObject() {
        if (this.fields != null && ((JsonObject) this.fields).has(JiraConstants._DESCRIPTION)
                && !((JsonObject) this.fields).get(JiraConstants._DESCRIPTION).isJsonNull()) {
            return ((JsonObject) this.fields).get(JiraConstants._DESCRIPTION).getAsJsonObject();
        }
        return null;
    }

    private void processContentArray(JsonArray contentArray, StringBuilder descriptionBuilder) {
        for (int i = 0; i < contentArray.size(); i++) {
            JsonObject contentObject = contentArray.get(i).getAsJsonObject();
            String type = contentObject.get(JiraConstants._TYPE).getAsString();

            if (!type.equals("mediaSingle") && contentObject.has(JiraConstants.CONTENT)) {
                StringBuilder block = new StringBuilder();
                processContentObject(contentObject, type, block);
                appendBlock(block.toString(), descriptionBuilder);
            }
        }
    }

    /**
     * Appends a rendered top-level block, separating it from the previous block with a blank
     * line. The blank line is required so that renderers which collapse a single newline into
     * a space (e.g. Markdown/HTML) still show paragraphs and lists on their own lines and
     * render bullet/ordered lists as lists rather than one run-on paragraph.
     */
    private void appendBlock(String blockText, StringBuilder descriptionBuilder) {
        int end = blockText.length();
        while (end > 0 && blockText.charAt(end - 1) == '\n') {
            end--;
        }
        blockText = blockText.substring(0, end);
        if (blockText.isEmpty()) {
            return;
        }
        if (descriptionBuilder.length() > 0) {
            descriptionBuilder.append("\n\n");
        }
        descriptionBuilder.append(blockText);
    }

    private void processContentObject(JsonObject contentObject, String type, StringBuilder descriptionBuilder) {
        JsonArray content = contentObject.getAsJsonArray(JiraConstants.CONTENT);
        switch (type) {
            case "paragraph":
            case "heading":
            case "codeBlock":
            case "blockquote":
            case "expand":
                appendParagraphs(content, descriptionBuilder);
                break;
            case "orderedList":
                appendList(content, descriptionBuilder, true);
                break;
            case "bulletList":
                appendList(content, descriptionBuilder, false);
                break;
            case "table":
                descriptionBuilder.append(createJiraTableToCSV(content)).append("\n");
                break;
            default:
                break;
        }
    }

    /**
     * Renders one block (paragraph, heading, codeBlock, blockquote, expand) as a single
     * line. Inline nodes within the block are concatenated so that formatting spread across
     * multiple text nodes (e.g. bold name + separator + linked email) stays on one line, and
     * a single newline is appended at the block boundary.
     */
    private void appendParagraphs(JsonArray paragraphContentArray, StringBuilder descriptionBuilder) {
        String line = renderInline(paragraphContentArray);
        if (line.isEmpty()) {
            return;
        }
        descriptionBuilder.append(line);
        if (line.charAt(line.length() - 1) != '\n') {
            descriptionBuilder.append("\n");
        }
    }

    /**
     * Concatenates the inline content of a block into a single string without inserting a
     * newline between sibling inline nodes. Nested block-level paragraphs (e.g. inside a
     * blockquote or expand) are rendered on their own line.
     */
    private String renderInline(JsonArray inlineNodes) {
        StringBuilder line = new StringBuilder();
        for (int k = 0; k < inlineNodes.size(); k++) {
            JsonObject node = inlineNodes.get(k).getAsJsonObject();
            if (node.has(JiraConstants.TEXT)) {
                line.append(node.get(JiraConstants.TEXT).getAsString());
            } else if (node.has(JiraConstants._TYPE)) {
                appendInlineByType(node, line);
            }
        }
        return line.toString();
    }

    private void appendInlineByType(JsonObject node, StringBuilder line) {
        String type = node.get(JiraConstants._TYPE).getAsString();
        switch (type) {
            case "mention":
            case "status":
                line.append(node.getAsJsonObject(JiraConstants.ATTRS).get(JiraConstants.TEXT).getAsString());
                break;
            case "inlineCard":
            case "blockCard":
                line.append(node.getAsJsonObject(JiraConstants.ATTRS).get("url").getAsString());
                break;
            case "date":
                String timestamp = node.getAsJsonObject(JiraConstants.ATTRS).get("timestamp").getAsString();
                line.append(convertTimestampToDate(Long.parseLong(timestamp)));
                break;
            case "paragraph":
                if (node.has(JiraConstants.CONTENT)
                        && !node.getAsJsonArray(JiraConstants.CONTENT).isJsonNull()
                        && !node.getAsJsonArray(JiraConstants.CONTENT).isEmpty()) {
                    line.append(renderInline(node.getAsJsonArray(JiraConstants.CONTENT))).append("\n");
                }
                break;
            default:
                break;
        }
    }

    /**
     * Renders an ordered or bullet list, prefixing each item with its marker ("1. ", "2. ", …
     * for ordered lists, "- " for bullet lists) so the list structure is preserved in the
     * extracted description.
     */
    private void appendList(JsonArray listItems, StringBuilder descriptionBuilder, boolean ordered) {
        int number = 1;
        for (int l = 0; l < listItems.size(); l++) {
            JsonObject listItem = listItems.get(l).getAsJsonObject();
            if (!listItem.has(JiraConstants.CONTENT)) {
                continue;
            }
            String marker = ordered ? (number + ". ") : "- ";
            JsonArray listItemContent = listItem.getAsJsonArray(JiraConstants.CONTENT);
            for (int m = 0; m < listItemContent.size(); m++) {
                JsonObject listItemContentObject = listItemContent.get(m).getAsJsonObject();
                if (listItemContentObject.has(JiraConstants._TYPE)
                        && listItemContentObject.get(JiraConstants._TYPE).getAsString().equals("paragraph")) {
                    String line = renderInline(listItemContentObject.getAsJsonArray(JiraConstants.CONTENT));
                    descriptionBuilder.append(marker).append(line).append("\n");
                }
            }
            number++;
        }
    }

    private String createJiraTableToCSV(JsonArray tableRows) {
        StringBuilder csvBuilder = new StringBuilder();

        for (JsonElement rowElement : tableRows) {
            JsonObject tableRow = rowElement.getAsJsonObject();
            JsonArray tableCells = tableRow.getAsJsonArray(JiraConstants.CONTENT);

            boolean isFirstCell = true;
            for (JsonElement cellElement : tableCells) {
                if (!isFirstCell) {
                    csvBuilder.append(",");
                }
                String cellText = getCellText(cellElement.getAsJsonObject());
                csvBuilder.append(cellText);
                isFirstCell = false;
            }
            csvBuilder.append("\n");
        }

        return csvBuilder.toString();
    }

    private String getCellText(JsonObject tableCell) {
        StringBuilder cellBuilder = new StringBuilder();
        JsonArray cellContentArray = tableCell.getAsJsonArray(JiraConstants.CONTENT);

        for (JsonElement contentElement : cellContentArray) {
            JsonObject cellContentObject = contentElement.getAsJsonObject();
            if (cellContentObject.get(JiraConstants._TYPE).getAsString().equals("paragraph")) {
                JsonArray paragraphContentArray = cellContentObject.getAsJsonArray(JiraConstants.CONTENT);
                for (JsonElement paragraphContentElement : paragraphContentArray) {
                    JsonObject paragraphContentObject = paragraphContentElement.getAsJsonObject();
                    if (paragraphContentObject.has(JiraConstants.TEXT)) {
                        cellBuilder.append(paragraphContentObject.get(JiraConstants.TEXT).getAsString());
                    }
                }
            }
        }

        return cellBuilder.toString();
    }

    private String convertTimestampToDate(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDate date = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return date.format(formatter);
    }

    /**
     * Extracts the last modified date from Jira issue fields.
     * <p>
     * This method retrieves the 'updated' field from the Jira API response and converts it
     * to epoch milliseconds for use with @Field.Date annotation.
     * </p>
     * <p>
     * Handles various Jira date formats including:
     * <ul>
     *   <li>"2026-02-12T11:19:51.462+05:30" (standard ISO 8601)</li>
     *   <li>"2026-02-12T11:19:51.462+0530" (Jira's non-standard format without colon)</li>
     *   <li>"2026-02-12T11:19:51.462Z" (UTC/Zulu time)</li>
     * </ul>
     * </p>
     *
     * @return The last modified date as epoch milliseconds (Long), or null if:
     *         <ul>
     *           <li>fields object is null</li>
     *           <li>'updated' field is not present in the response</li>
     *           <li>'updated' field value is null</li>
     *           <li>date parsing fails</li>
     *         </ul>
     */
    private Long getLastModifiedDate() {
        if (this.fields == null) {
            LOGGER.warn("Fields object is null, cannot extract Last Modified Date");
            return null;
        }

        JsonObject fieldsObj = (JsonObject) this.fields;
        if (!fieldsObj.has(UPDATED)) {
            LOGGER.warn("'updated' field not found in Jira response");
            return null;
        }

        JsonElement updatedElement = fieldsObj.get(UPDATED);
        if (updatedElement == null || updatedElement.isJsonNull()) {
            LOGGER.warn("'updated' field is null in Jira response");
            return null;
        }

        try {
            String updatedStr = updatedElement.getAsString();
            LOGGER.trace("Parsing Last Modified Date from Jira API: {}", updatedStr);

            // Use robust DateTimeFormatter that handles timezone variations
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(updatedStr, JIRA_DATE_FORMATTER);
            Long epochMilli = offsetDateTime.toInstant().toEpochMilli();

            LOGGER.debug("Successfully parsed Last Modified Date: {} -> epoch: {}", updatedStr, epochMilli);
            return epochMilli;
        } catch (DateTimeParseException e) {
            LOGGER.error("Failed to parse Last Modified Date. Invalid date format: {}",
                    updatedElement.getAsString(), e);
            return null;
        } catch (IllegalStateException | UnsupportedOperationException e) {
            LOGGER.error("Failed to extract Last Modified Date. Invalid JSON element type: {}",
                    updatedElement, e);
            return null;
        }
    }

    private String getPriority() {
        try {
            return ((JsonObject) this.fields).get("priority").getAsJsonObject().get("name").getAsString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String getProjectKey() {
        try {
            return ((JsonObject) this.fields).get("project").getAsJsonObject().get(KEY).getAsString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String getReporter() {
        try {
            return ((JsonObject) this.fields).get("reporter").getAsJsonObject().get("displayName").getAsString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String getStatus() {
        try {
            String s = ((JsonObject) this.fields).get("status").getAsJsonObject().get("name").getAsString();
            return s != null ? s.toUpperCase() : "";
        } catch (Exception ignored) {
            return "";
        }
    }

    private String getType() {
        try {
            return ((JsonObject) this.fields).get("issuetype").getAsJsonObject().get("name").getAsString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String getParent() {
        try {
            JsonObject parent = ((JsonObject) this.fields).get("parent").getAsJsonObject();
            if (parent != null) {
                return parent.get(KEY).getAsString();
            }
        } catch (Exception ignored) {
        }
        return JiraConstants.NA;
    }

    public String getSummary() {
        try {
            return ((JsonObject) this.fields).get("summary").getAsString();
        } catch (Exception ignored) {
            return "";
        }
    }

    private String getRequestType() {
        try {
            // Request Type is typically stored in a custom field for Jira Service
            // Management
            // Common field names: customfield_10010, customfield_10325, etc.
            JsonObject fieldsObj = (JsonObject) this.fields;

            // Try standard requesttype field first
            if (fieldsObj.has("requesttype")) {
                JsonElement requestTypeElement = fieldsObj.get("requesttype");
                if (requestTypeElement != null && !requestTypeElement.isJsonNull()) {
                    if (requestTypeElement.isJsonObject()) {
                        JsonObject requestTypeObj = requestTypeElement.getAsJsonObject();
                        if (requestTypeObj.has("name")) {
                            return requestTypeObj.get("name").getAsString();
                        }
                    } else if (requestTypeElement.isJsonPrimitive()) {
                        return requestTypeElement.getAsString();
                    }
                }
            }

            // PASS 1: Look for high-confidence Request Type fields (containing
            // "requestType" object)
            for (String fieldKey : fieldsObj.keySet()) {
                if (fieldKey.startsWith("customfield_")) {
                    JsonElement customField = fieldsObj.get(fieldKey);
                    if (customField != null && !customField.isJsonNull() && customField.isJsonObject()) {
                        JsonObject customFieldObj = customField.getAsJsonObject();
                        if (customFieldObj.has("requestType")) {
                            JsonObject requestTypeObj = customFieldObj.getAsJsonObject("requestType");
                            if (requestTypeObj.has("name")) {
                                return requestTypeObj.get("name").getAsString();
                            }
                        }
                    }
                }
            }

            // PASS 2: Look for other patterns (portal/key format or direct name), but avoid
            // known SLA fields
            // SLA fields often have "ongoingCycle", "completedCycle", "friendly" etc.
            for (String fieldKey : fieldsObj.keySet()) {
                if (fieldKey.startsWith("customfield_")) {
                    JsonElement customField = fieldsObj.get(fieldKey);
                    if (customField != null && !customField.isJsonNull()) {
                        // Check if it's a string value (like "1/2" format)
                        if (customField.isJsonPrimitive()) {
                            String value = customField.getAsString();
                            if (value.contains("/") && value.split("/").length == 2) {
                                LOGGER.debug("Found potential request type field {} with value: {}", fieldKey, value);
                                return value;
                            }
                        }
                        // Check if it's an object with "name" field directly
                        else if (customField.isJsonObject()) {
                            JsonObject customFieldObj = customField.getAsJsonObject();

                            // Skip if it looks like an SLA field
                            if (customFieldObj.has("ongoingCycle") || customFieldObj.has("completedCycle")) {
                                continue;
                            }

                            // REMOVED: Aggressive fallback matching any object with "name".
                            /*
                             * if (customFieldObj.has("name")) {
                             * return customFieldObj.get("name").getAsString();
                             * }
                             */

                            // Check if it has a "value" field (often used in single-selects, sometimes
                            // Request Type in older versions)
                            // But usually Request Type has specific structure. If value is "1/2", it would
                            // be caught by primitive check above
                            if (customFieldObj.has("value")) {
                                return customFieldObj.get("value").getAsString();
                            }
                        }
                    }
                }
            }

            return null;
        } catch (Exception e) {
            LOGGER.debug("Error getting request type: {}", e.getMessage());
            return null;
        }
    }

    public static JsonArray getSubtask(JsonObject object) {
        JsonObject fields = object.getAsJsonObject(FIELDS);
        return fields.getAsJsonArray("subtasks");
    }

    public static String getIssueType(JsonObject object) {
        try {
            JsonObject fields = object.getAsJsonObject(FIELDS);
            return fields.getAsJsonObject("issuetype").get("name").getAsString();
        } catch (Exception ignored) {
            return "";
        }
    }

}