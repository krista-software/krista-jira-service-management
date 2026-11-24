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

package app.krista.extensions.devsecops.development.jira;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating test data objects.
 * Provides consistent test data across all integration tests.
 */
public class TestDataFactory {

    public static final String TEST_PROJECT_KEY = "TEST";
    public static final String TEST_PROJECT_NAME = "Test Project";
    public static final String TEST_ISSUE_KEY = "TEST-123";
    public static final String TEST_EPIC_KEY = "TEST-100";
    public static final String TEST_USER_ACCOUNT_ID = "557058:a4e1c89b-14f3-4657-9fe3-a9c6e8a38893";
    public static final String TEST_USER_EMAIL = "test@example.com";
    public static final String TEST_USER_DISPLAY_NAME = "Test User";
    public static final String TEST_HOST_URL = "https://test.atlassian.net";

    /**
     * Creates a complete issue JSON with all fields.
     */
    public static JsonObject createCompleteIssue(String key, String issueType) {
        JsonObject issue = new JsonObject();
        issue.addProperty("id", "10001");
        issue.addProperty("key", key);
        issue.addProperty("self", TEST_HOST_URL + "/rest/api/2/issue/" + key);

        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test Issue Summary");
        fields.addProperty("description", "Test Issue Description");
        fields.addProperty("created", getCurrentTimestamp());
        fields.addProperty("updated", getCurrentTimestamp());

        // Issue Type
        JsonObject issueTypeObj = new JsonObject();
        issueTypeObj.addProperty("id", "10001");
        issueTypeObj.addProperty("name", issueType);
        fields.add("issuetype", issueTypeObj);

        // Status
        JsonObject status = new JsonObject();
        status.addProperty("id", "10000");
        status.addProperty("name", "To Do");
        fields.add("status", status);

        // Priority
        JsonObject priority = new JsonObject();
        priority.addProperty("id", "3");
        priority.addProperty("name", "Medium");
        fields.add("priority", priority);

        // Project
        JsonObject project = new JsonObject();
        project.addProperty("id", "10000");
        project.addProperty("key", TEST_PROJECT_KEY);
        project.addProperty("name", TEST_PROJECT_NAME);
        fields.add("project", project);

        // Assignee
        fields.add("assignee", createUser(TEST_USER_ACCOUNT_ID, TEST_USER_DISPLAY_NAME, TEST_USER_EMAIL));

        // Reporter
        fields.add("reporter", createUser(TEST_USER_ACCOUNT_ID, TEST_USER_DISPLAY_NAME, TEST_USER_EMAIL));

        // Labels
        JsonArray labels = new JsonArray();
        labels.add("test-label");
        fields.add("labels", labels);

        issue.add("fields", fields);
        return issue;
    }

    /**
     * Creates a user JSON object.
     */
    public static JsonObject createUser(String accountId, String displayName, String emailAddress) {
        JsonObject user = new JsonObject();
        user.addProperty("accountId", accountId);
        user.addProperty("displayName", displayName);
        user.addProperty("emailAddress", emailAddress);
        user.addProperty("active", true);
        user.addProperty("self", TEST_HOST_URL + "/rest/api/2/user?accountId=" + accountId);
        return user;
    }

    /**
     * Creates a project JSON object.
     */
    public static JsonObject createProject(String key, String name) {
        JsonObject project = new JsonObject();
        project.addProperty("id", "10000");
        project.addProperty("key", key);
        project.addProperty("name", name);
        project.addProperty("self", TEST_HOST_URL + "/rest/api/2/project/" + key);
        project.addProperty("projectTypeKey", "software");
        project.addProperty("style", "classic");
        project.addProperty("assigneeType", "PROJECT_LEAD");

        JsonObject lead = createUser(TEST_USER_ACCOUNT_ID, TEST_USER_DISPLAY_NAME, TEST_USER_EMAIL);
        project.add("lead", lead);

        return project;
    }

    /**
     * Creates a search result JSON with multiple issues.
     */
    public static JsonObject createSearchResult(int total, JsonObject... issues) {
        JsonObject result = new JsonObject();
        result.addProperty("startAt", 0);
        result.addProperty("maxResults", 50);
        result.addProperty("total", total);

        JsonArray issuesArray = new JsonArray();
        for (JsonObject issue : issues) {
            issuesArray.add(issue);
        }
        result.add("issues", issuesArray);

        return result;
    }

    /**
     * Creates a transition JSON object.
     */
    public static JsonObject createTransition(String id, String name, String toStatus) {
        JsonObject transition = new JsonObject();
        transition.addProperty("id", id);
        transition.addProperty("name", name);

        JsonObject to = new JsonObject();
        to.addProperty("id", "10001");
        to.addProperty("name", toStatus);
        transition.add("to", to);

        return transition;
    }

    /**
     * Creates transitions response.
     */
    public static JsonObject createTransitionsResponse(JsonObject... transitions) {
        JsonObject response = new JsonObject();
        JsonArray transitionsArray = new JsonArray();
        for (JsonObject transition : transitions) {
            transitionsArray.add(transition);
        }
        response.add("transitions", transitionsArray);
        return response;
    }

    /**
     * Creates a version/release JSON object.
     */
    public static JsonObject createVersion(String id, String name, boolean released) {
        JsonObject version = new JsonObject();
        version.addProperty("id", id);
        version.addProperty("name", name);
        version.addProperty("released", released);
        version.addProperty("archived", false);
        version.addProperty("projectId", 10000);
        version.addProperty("description", "Version " + name);
        version.addProperty("self", TEST_HOST_URL + "/rest/api/2/version/" + id);
        return version;
    }

    /**
     * Gets current timestamp in Jira format.
     */
    public static String getCurrentTimestamp() {
        return ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Creates test attributes map.
     */
    public static Map<String, Object> createTestAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JiraAttributes.HOST_URL, TEST_HOST_URL);
        attributes.put(JiraAttributes.USER_NAME, TEST_USER_EMAIL);
        attributes.put(JiraAttributes.AUTH_TOKEN, "test-token");
        attributes.put(JiraAttributes.CLIENT_ID, "test-client-id");
        attributes.put(JiraAttributes.CLIENT_SECRET, "test-client-secret");
        return attributes;
    }
}