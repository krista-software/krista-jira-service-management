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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class JiraIssue {
    public static final String RENDERED_FIELDS = "renderedFields";
    public static final String FIELDS = "fields";
    public static final String KEY = "key";
    public static final String ID = "id";
    public final String id;
    public final String key;
    public final Object fields;
    public final Object renderedFields;

    private final Logger LOGGER = LoggerFactory.getLogger(JiraIssue.class);

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
        } catch (Exception ignored) {
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
     * Extracts the description text from the JSON object fields and returns it as a
     * concatenated string.
     * If an error occurs during the extraction process, an empty string is
     * returned.
     *
     * @return The description text extracted from the JSON object fields, or an
     * empty string if an error occurs.
     */
    private String getDescription() {
        StringBuilder descriptionBuilder = new StringBuilder();
        try {
            JsonObject descriptionObject = getDescriptionObject();
            if (descriptionObject != null) {
                JsonArray contentArray = descriptionObject.getAsJsonArray(JiraConstants.CONTENT);
                if (contentArray != null) {
                    processContentArray(contentArray, descriptionBuilder);
                }
            }
        } catch (Exception cause) {
            LOGGER.debug("Error in getDescription method: {}", cause);
            return "";
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
                processContentObject(contentObject, type, descriptionBuilder);
            }
        }
    }

    private void processContentObject(JsonObject contentObject, String type, StringBuilder descriptionBuilder) {
        JsonArray content = contentObject.getAsJsonArray(JiraConstants.CONTENT);
        switch (type) {
            case "paragraph":
            case "codeBlock":
            case "blockquote":
            case "expand":
                appendParagraphs(content, descriptionBuilder);
                break;
            case "orderedList":
                appendOrderedList(content, descriptionBuilder);
                break;
            case "table":
                descriptionBuilder.append(createJiraTableToCSV(content)).append("\n");
                break;
            default:
                break;
        }
    }

    private void appendParagraphs(JsonArray paragraphContentArray, StringBuilder descriptionBuilder) {
        for (int k = 0; k < paragraphContentArray.size(); k++) {
            JsonObject paragraphContentObject = paragraphContentArray.get(k).getAsJsonObject();
            if (paragraphContentObject.has(JiraConstants.TEXT)) {
                descriptionBuilder.append(paragraphContentObject.get(JiraConstants.TEXT).getAsString()).append("\n");
            } else if (paragraphContentObject.has(JiraConstants._TYPE)) {
                appendByType(paragraphContentObject, descriptionBuilder);
            }
        }
    }

    private void appendByType(JsonObject paragraphContentObject, StringBuilder descriptionBuilder) {
        String type = paragraphContentObject.get(JiraConstants._TYPE).getAsString();
        switch (type) {
            case "mention":
            case "status":
                appendMentionOrStatus(paragraphContentObject, descriptionBuilder);
                break;
            case "paragraph":
                appendParagraph(paragraphContentObject, descriptionBuilder);
                break;
            case "inlineCard":
            case "blockCard":
                appendCard(paragraphContentObject, descriptionBuilder);
                break;
            case "date":
                appendDate(paragraphContentObject, descriptionBuilder);
                break;
            default:
                break;
        }
    }

    private void appendMentionOrStatus(JsonObject paragraphContentObject, StringBuilder descriptionBuilder) {
        descriptionBuilder.append(
                        paragraphContentObject.getAsJsonObject(JiraConstants.ATTRS).get(JiraConstants.TEXT).getAsString())
                .append("\n");
    }

    private void appendParagraph(JsonObject paragraphContentObject, StringBuilder descriptionBuilder) {
        String text = "";
        if (paragraphContentObject.has(JiraConstants.CONTENT)
                && !paragraphContentObject.getAsJsonArray(JiraConstants.CONTENT).isJsonNull()
                && !paragraphContentObject.getAsJsonArray(JiraConstants.CONTENT).isEmpty()) {
            text = paragraphContentObject.getAsJsonArray(JiraConstants.CONTENT).get(0).getAsJsonObject()
                    .get(JiraConstants.TEXT).getAsString();
        }
        descriptionBuilder.append(text).append("\n");
    }

    private void appendCard(JsonObject paragraphContentObject, StringBuilder descriptionBuilder) {
        descriptionBuilder.append(paragraphContentObject.getAsJsonObject(JiraConstants.ATTRS).get("url").getAsString())
                .append("\n");
    }

    private void appendDate(JsonObject paragraphContentObject, StringBuilder descriptionBuilder) {
        String timestamp = paragraphContentObject.getAsJsonObject(JiraConstants.ATTRS).get("timestamp").getAsString();
        descriptionBuilder.append(convertTimestampToDate(Long.parseLong(timestamp))).append("\n");
    }

    private void appendOrderedList(JsonArray orderedListItems, StringBuilder descriptionBuilder) {
        for (int l = 0; l < orderedListItems.size(); l++) {
            JsonObject listItem = orderedListItems.get(l).getAsJsonObject();
            if (listItem.has(JiraConstants.CONTENT)) {
                JsonArray listItemContent = listItem.getAsJsonArray(JiraConstants.CONTENT);
                for (int m = 0; m < listItemContent.size(); m++) {
                    JsonObject listItemContentObject = listItemContent.get(m).getAsJsonObject();
                    if (listItemContentObject.has(JiraConstants._TYPE)
                            && listItemContentObject.get(JiraConstants._TYPE).getAsString().equals("paragraph")) {
                        appendParagraphs(listItemContentObject.getAsJsonArray(JiraConstants.CONTENT),
                                descriptionBuilder);
                    }
                }
            }
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