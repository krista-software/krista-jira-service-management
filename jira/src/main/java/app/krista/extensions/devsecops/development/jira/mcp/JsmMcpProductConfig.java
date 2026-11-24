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

package app.krista.extensions.devsecops.development.jira.mcp;

import app.krista.model.base.FreeForm;
import app.krista.sdk.atlassian.mcp.McpProductConfig;
import app.krista.sdk.atlassian.mcp.McpSearchResult;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Jira Service Management-specific MCP product configuration.
 *
 * <p>JSM is built on the Jira platform so it shares the same JQL syntax,
 * REST API endpoints, and response format as standard Jira. Only the
 * product name and NL prompt context differ.</p>
 */
public final class JsmMcpProductConfig implements McpProductConfig {

    private static final Gson GSON = new Gson();

    private static final String JSM_SEARCH_URL =
            "https://api.atlassian.com/ex/jira/%s/rest/api/3/search/jql";

    private static final List<String> STANDARD_FIELDS = List.of(
            "summary", "status", "assignee", "priority", "issuetype", "project"
    );

    private static final List<ColumnDef> COLUMNS = List.of(
            new ColumnDef("key",      "Key",      "link"),
            new ColumnDef("type",     "Type",     "pill"),
            new ColumnDef("summary",  "Summary",  "text"),
            new ColumnDef("status",   "Status",   "pill"),
            new ColumnDef("priority", "Priority", "pill"),
            new ColumnDef("assignee", "Assignee", "text")
    );

    @Override public String productName()          { return "Jira Service Management"; }
    @Override public String queryLanguage()         { return "JQL"; }
    @Override public String queryLanguageFullName()  { return "JQL (Jira Query Language)"; }
    @Override public String searchUrlTemplate()      { return JSM_SEARCH_URL; }
    @Override public String basicSearchPath()        { return "/search/jql"; }
    @Override public List<String> standardFields()   { return STANDARD_FIELDS; }
    @Override public List<ColumnDef> resultColumns() { return COLUMNS; }
    @Override public String searchHttpMethod()       { return "POST"; }
    @Override public String userSearchPath()       { return "/user/search"; }
    @Override public String oauthApiBaseTemplate() { return "https://api.atlassian.com/ex/jira/%s/rest/api/3"; }

    @Override
    public String queryGenerationPrompt(String question) {
        return "Given this Jira Service Management question, output a JSON object with exactly three fields:\n"
                + "  \"query\": the JQL query string to find the relevant issue(s) or service request(s)\n"
                + "  \"specific\": true if the question asks about a specific field or attribute of an issue "
                + "(e.g. priority, assignee, status, summary of a named ticket), "
                + "false if it asks for a list of issues (e.g. show open incidents, find all bugs)\n"
                + "  \"field\": when specific is true, the exact result-row key being asked about "
                + "(one of: key, type, summary, status, priority, assignee); null when specific is false\n\n"
                + "Rules:\n"
                + "- Output ONLY raw JSON. No markdown, no backticks, no explanation.\n"
                + "- Use standard Jira Cloud JQL syntax.\n"
                + "- Always wrap string values in double quotes (e.g. status = \"In Progress\").\n"
                + "- For the current user ALWAYS use currentUser() — never quote it.\n"
                + "- Issue keys (e.g. JSM-100) do NOT need quotes.\n"
                + "- Assignee matching: Jira Cloud requires the EXACT full display name. "
                + "If a [Context: current user email = X] hint appears in the question and the mentioned name "
                + "matches that email's prefix (e.g. email simran.sethi@co.com → name 'simran'), "
                + "use assignee = currentUser(). For any other person use their exact full display name.\n\n"
                + "Examples:\n"
                + "  Q: What is the priority of JSM-100?          -> {\"query\":\"key = JSM-100\",\"specific\":true,\"field\":\"priority\"}\n"
                + "  Q: Who is assigned to JSM-200?               -> {\"query\":\"key = JSM-200\",\"specific\":true,\"field\":\"assignee\"}\n"
                + "  Q: Show all tickets assigned to me           -> {\"query\":\"assignee = currentUser()\",\"specific\":false,\"field\":null}\n"
                + "  Q: Show all open incidents                   -> {\"query\":\"issuetype = Incident AND status != \\\"Done\\\"\",\"specific\":false,\"field\":null}\n\n"
                + "Question: " + question;
    }

    @Override
    public String answerExtractionPrompt(String question, String issueData) {
        return "Based on the following Jira Service Management issue data, answer the user's question directly and concisely.\n\n"
                + "User's question: " + question + "\n\n"
                + "JSM data:\n" + issueData
                + "\nRules:\n"
                + "- Answer in one or two sentences.\n"
                + "- If the specific field is not present, say so clearly.\n"
                + "- Do NOT wrap your answer in backticks or markdown.\n"
                + "- Do NOT repeat the question.";
    }

    @Override
    public String buildSearchRequestBody(String query, int maxResults, boolean allFields) {
        JsonArray fieldList = new JsonArray();
        if (allFields) {
            fieldList.add("*all");
        } else {
            for (String f : STANDARD_FIELDS) fieldList.add(f);
        }
        JsonObject body = new JsonObject();
        body.addProperty("jql", query);
        body.addProperty("maxResults", maxResults);
        body.add("fields", fieldList);
        if (allFields) body.addProperty("expand", "names");
        return body.toString();
    }

    @Override
    public McpSearchResult parseSearchResponse(String json, String baseUrl) {
        JsonObject raw = GSON.fromJson(json, JsonObject.class);
        if (raw.has("errorMessages")) {
            JsonArray errors = raw.getAsJsonArray("errorMessages");
            if (!errors.isEmpty()) throw new IllegalArgumentException(errors.get(0).getAsString());
        }
        JsonArray issues = raw.has("issues") ? raw.getAsJsonArray("issues") : new JsonArray();
        int total = raw.has("total") ? raw.get("total").getAsInt() : 0;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (JsonElement el : issues) {
            JsonObject issue = el.getAsJsonObject();
            JsonObject fields = issue.getAsJsonObject("fields");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("key",      str(issue, "key"));
            row.put("type",     nestedStr(fields, "issuetype", "name"));
            row.put("summary",  str(fields, "summary"));
            row.put("status",   nestedStr(fields, "status", "name"));
            row.put("priority", nestedStr(fields, "priority", "name"));
            row.put("assignee", nestedStr(fields, "assignee", "displayName"));
            row.put("url",      baseUrl + "/browse/" + str(issue, "key"));
            rows.add(row);
        }
        return new McpSearchResult("jql", total, rows.size(), rows);
    }

    @Override
    public List<FreeForm> parseStandardResponse(String json, String baseUrl) {
        JsonObject raw = GSON.fromJson(json, JsonObject.class);
        if (raw.has("errorMessages")) {
            JsonArray errors = raw.getAsJsonArray("errorMessages");
            if (!errors.isEmpty()) throw new IllegalArgumentException(errors.get(0).getAsString());
        }
        JsonArray issues = raw.has("issues") ? raw.getAsJsonArray("issues") : new JsonArray();
        List<FreeForm> rows = new ArrayList<>();
        for (JsonElement el : issues) {
            JsonObject issue = el.getAsJsonObject();
            JsonObject fields = issue.getAsJsonObject("fields");
            FreeForm row = new FreeForm();
            row.put("key",       " Text ", str(issue, "key"));
            row.put("issueType", " Text ", nestedStr(fields, "issuetype", "name"));
            row.put("summary",   " Text ", str(fields, "summary"));
            row.put("status",    " Text ", nestedStr(fields, "status", "name"));
            row.put("priority",  " Text ", nestedStr(fields, "priority", "name"));
            row.put("assignee",  " Text ", nestedStr(fields, "assignee", "displayName"));
            row.put("url",       " Text ", baseUrl + "/browse/" + str(issue, "key"));
            rows.add(row);
        }
        return rows;
    }

    @Override
    public List<Map<String, String>> parseAllFieldsResponse(String json, String baseUrl) {
        JsonObject raw = GSON.fromJson(json, JsonObject.class);
        if (raw.has("errorMessages")) {
            JsonArray errors = raw.getAsJsonArray("errorMessages");
            if (!errors.isEmpty()) throw new IllegalArgumentException(errors.get(0).getAsString());
        }
        Map<String, String> fieldNames = new LinkedHashMap<>();
        if (raw.has("names") && raw.get("names").isJsonObject()) {
            for (Map.Entry<String, JsonElement> e : raw.getAsJsonObject("names").entrySet()) {
                fieldNames.put(e.getKey(), e.getValue().getAsString());
            }
        }
        JsonArray issues = raw.has("issues") ? raw.getAsJsonArray("issues") : new JsonArray();
        List<Map<String, String>> result = new ArrayList<>();
        for (JsonElement el : issues) {
            JsonObject issue = el.getAsJsonObject();
            Map<String, String> row = new LinkedHashMap<>();
            row.put("key", str(issue, "key"));
            row.put("url", baseUrl + "/browse/" + str(issue, "key"));
            if (issue.has("fields") && issue.get("fields").isJsonObject()) {
                for (Map.Entry<String, JsonElement> entry : issue.getAsJsonObject("fields").entrySet()) {
                    String displayName = fieldNames.getOrDefault(entry.getKey(), entry.getKey());
                    String value = extractFieldValue(entry.getValue());
                    if (value != null && !value.isBlank()) row.put(displayName, value);
                }
            }
            result.add(row);
        }
        return result;
    }

    private static String str(JsonObject obj, String key) {
        return (obj != null && obj.has(key) && !obj.get(key).isJsonNull())
                ? obj.get(key).getAsString() : "";
    }

    private static String nestedStr(JsonObject obj, String outer, String inner) {
        if (obj == null || !obj.has(outer) || obj.get(outer).isJsonNull()) return "";
        return str(obj.getAsJsonObject(outer), inner);
    }

    private static String extractFieldValue(JsonElement element) {
        if (element == null || element.isJsonNull()) return "";
        if (element.isJsonPrimitive()) return element.getAsString();
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("displayName") && !obj.get("displayName").isJsonNull()) return obj.get("displayName").getAsString();
            if (obj.has("name")        && !obj.get("name").isJsonNull())        return obj.get("name").getAsString();
            if (obj.has("value")       && !obj.get("value").isJsonNull())       return obj.get("value").getAsString();
            return "";
        }
        if (element.isJsonArray()) {
            List<String> parts = new ArrayList<>();
            for (JsonElement e : element.getAsJsonArray()) {
                String v = extractFieldValue(e);
                if (v != null && !v.isBlank()) parts.add(v);
            }
            return String.join(", ", parts);
        }
        return "";
    }
}
