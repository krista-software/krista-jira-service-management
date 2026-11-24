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

import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Base class for integration tests providing common setup and utilities.
 * Provides mock Jira server responses and common test infrastructure.
 */
public abstract class BaseIntegrationTest {

    @Mock
    protected Invoker invoker;

    @Mock
    protected RequestContext requestContext;

    @Mock
    protected OAuth2RestClient oAuth2RestClient;

    @Mock
    protected RestClientFactory restClientFactory;

    @Mock
    protected JiraAttributes jiraAttributes;

    @Mock
    protected Client mockClient;

    @Mock
    protected WebTarget mockWebTarget;

    @Mock
    protected Invocation.Builder mockBuilder;

    @Mock
    protected Response mockResponse;

    protected Map<String, Object> testAttributes;
    protected RestClient testRestClient;

    @BeforeEach
    public void baseSetUp() {
        MockitoAnnotations.openMocks(this);
        setupTestAttributes();
        setupMockRestClient();
        setupDefaultMockBehavior();
    }

    /**
     * Sets up test attributes with common Jira configuration.
     */
    protected void setupTestAttributes() {
        testAttributes = new HashMap<>();
        testAttributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        testAttributes.put(JiraAttributes.USER_NAME, "test@example.com");
        testAttributes.put(JiraAttributes.AUTH_TOKEN, "test-token");
        testAttributes.put(JiraAttributes.CLIENT_ID, "test-client-id");
        testAttributes.put(JiraAttributes.CLIENT_SECRET, "test-client-secret");
    }

    /**
     * Sets up mock REST client for testing.
     */
    protected void setupMockRestClient() {
        testRestClient = new AuthRestClient(testAttributes, false, false);
        when(restClientFactory.getInstance()).thenReturn(testRestClient);
        when(restClientFactory.getInstance(anyBoolean())).thenReturn(testRestClient);
        when(restClientFactory.invokeAsUser()).thenReturn(false);
    }

    /**
     * Sets up default mock behavior for common operations.
     */
    protected void setupDefaultMockBehavior() {
        when(invoker.getAttributes()).thenReturn(testAttributes);
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
    }

    /**
     * Creates a mock successful response with JSON body.
     */
    protected Response createMockResponse(int status, String jsonBody) {
        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(status);
        when(response.readEntity(String.class)).thenReturn(jsonBody);
        return response;
    }

    /**
     * Creates a mock successful response with JSON object.
     */
    protected Response createMockResponse(int status, JsonObject jsonObject) {
        return createMockResponse(status, jsonObject.toString());
    }

    /**
     * Creates a mock error response.
     */
    protected Response createErrorResponse(int status, String errorMessage) {
        JsonObject error = new JsonObject();
        error.addProperty("errorMessages", errorMessage);
        return createMockResponse(status, error);
    }

    /**
     * Creates a sample Jira issue JSON object.
     */
    protected JsonObject createSampleIssue(String key, String summary, String status) {
        JsonObject issue = new JsonObject();
        issue.addProperty("id", "10001");
        issue.addProperty("key", key);
        issue.addProperty("self", "https://test.atlassian.net/rest/api/2/issue/" + key);

        JsonObject fields = new JsonObject();
        fields.addProperty("summary", summary);

        JsonObject statusObj = new JsonObject();
        statusObj.addProperty("name", status);
        fields.add("status", statusObj);

        issue.add("fields", fields);
        return issue;
    }

    /**
     * Creates a sample Jira project JSON object.
     */
    protected JsonObject createSampleProject(String key, String name) {
        JsonObject project = new JsonObject();
        project.addProperty("id", "10000");
        project.addProperty("key", key);
        project.addProperty("name", name);
        project.addProperty("self", "https://test.atlassian.net/rest/api/2/project/" + key);
        return project;
    }

    /**
     * Creates a sample Jira user JSON object.
     */
    protected JsonObject createSampleUser(String accountId, String displayName, String emailAddress) {
        JsonObject user = new JsonObject();
        user.addProperty("accountId", accountId);
        user.addProperty("displayName", displayName);
        user.addProperty("emailAddress", emailAddress);
        user.addProperty("active", true);
        return user;
    }

    /**
     * Parses JSON string to JsonObject.
     */
    protected JsonObject parseJson(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }
}