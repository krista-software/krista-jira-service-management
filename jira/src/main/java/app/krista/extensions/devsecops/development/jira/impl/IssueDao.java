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

package app.krista.extensions.devsecops.development.jira.impl;

import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.*;
import app.krista.extensions.devsecops.development.jira.services.HtmlToAdfConverter;
import app.krista.extensions.devsecops.development.jira.services.JiraIssue;
import app.krista.extensions.devsecops.development.jira.services.JiraUser;
import app.krista.extensions.devsecops.development.jira.util.DatePlus;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.FreeForm;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;
import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.VALUE;
import static app.krista.omni.sdk.complex_fields.impl.util.Constants.FIELDS;

@Service
public final class IssueDao {

    public static final String ERROR_MESSAGES = "errorMessages";
    public static final String ISSUES_NOT_FOUND = "Issues not found";
    public static final String RENDERED_FIELDS = "renderedFields";
    public static final String EXPAND = "expand";
    public static final String ISSUES = "issues";
    public static final int TOTAL_ISSUES_LIMIT = 3000;

    private static final DateTimeFormatter JIRA_DATE_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .appendPattern("[Z][XXX]")
            .toFormatter();
    private final RestClientFactory restClientFactory;
    private final ProjectDao projectDao;
    private final UserDao userDao;
    private final TicketInfoDao ticketInfoDao;
    private RestClient restClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(IssueDao.class);

    @Inject
    public IssueDao(ProjectDao projectDao, UserDao userDao, TicketInfoDao ticketInfoDao,
                    RestClientFactory restClientFactory) {
        this.restClientFactory = restClientFactory;
        this.restClient = restClientFactory.getInstance();
        this.projectDao = projectDao;
        this.userDao = userDao;
        this.ticketInfoDao = ticketInfoDao;
    }

    public IssueDao(RestClientFactory restClientFactory, RestClient restClient) {
        this.restClientFactory = restClientFactory;
        this.restClient = restClient;
        this.projectDao = new ProjectDao(restClientFactory, restClient);
        this.userDao = new UserDao(restClientFactory, restClient);
        this.ticketInfoDao = new TicketInfoDao(restClientFactory, restClient);
    }

    private static JsonObject getPriorityJsonObject(TicketPriority priority) {
        JsonObject priorityObj = new JsonObject();
        if (priority.id != null) {
            priorityObj.addProperty("id", priority.id);
        }
        if (priority.name != null) {
            priorityObj.addProperty("name", priority.name);
        }
        return priorityObj;
    }

    public RestClient getRestClient() {
        return getRestClient(null);
    }

    private RestClient getRestClient(Boolean invokeAsUser) {
        if (restClientFactory == null) {
            return restClient;
        }

        boolean invokeAsUserAuth = invokeAsUser != null ? invokeAsUser : restClientFactory.invokeAsUser();
        if (restClient == null || (restClient instanceof OAuth2RestClient && !invokeAsUserAuth)
                || (restClient instanceof AuthRestClient && invokeAsUserAuth)) {
            restClient = restClientFactory.getInstance(invokeAsUserAuth);
        }
        return restClient;
    }

    public String getAccountIdByEmail(String email) {
        try {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("query", email);
            String out = restClient.fetch("user/search", queryParams, String.class);
            JsonArray jsonArray = GSON.fromJson(out, JsonArray.class);
            if (!jsonArray.isEmpty()) {
                JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
                return jsonObject.get("accountId").getAsString();
            } else {
                System.err.println("No user found with email " + email);
                return "No user found with email " + email;
            }
        } catch (IllegalCallerException cause) {
            throw new IllegalCallerException(cause.getMessage(), cause);
        }
    }

    public Map<String, Object> createUser(String emailAddress) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("user", new HashMap<>());
        JsonObject userDetails = new JsonObject();
        userDetails.addProperty("emailAddress", emailAddress);
        Response response = invoker.post(Entity.entity(userDetails.toString(), MediaType.APPLICATION_JSON_TYPE));
        if (response.getStatus() == 201) {
            String jsonResponse = response.readEntity(String.class);
            JsonObject responseBody = GSON.fromJson(jsonResponse, JsonObject.class);
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("Self", responseBody.get("self").getAsString());
            userMap.put("Account Id", responseBody.get("accountId").getAsString());
            userMap.put("Email Address", responseBody.get("emailAddress").getAsString());
            userMap.put("Display Name", responseBody.get("displayName").getAsString());
            return userMap;
        } else {
            System.out.println("Failed to create user");
            return Map.of();
        }
    }

    public List<String> getUserGroups(String userKey) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("accountId", userKey);
        String out = restClient.fetch("user/groups", queryParams, String.class);
        if (!out.isEmpty()) {
            JsonObject responseBody = GSON.fromJson(out, JsonObject.class);
            JsonArray groupsArray = responseBody.getAsJsonArray("values");
            List<String> groupNames = new ArrayList<>();
            for (JsonElement groupElement : groupsArray) {
                JsonObject groupObject = groupElement.getAsJsonObject();
                groupNames.add(groupObject.get("name").getAsString());
            }
            return groupNames;
        } else {
            throw new RuntimeException("Failed to fetch user groups");
        }
    }

    private boolean addUserToGroup(String userKey, String groupName) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("groupname", groupName);
        Invocation.Builder invoker = getRestClient().getResourceInvoker("group/user", queryParams);
        JsonObject payload = new JsonObject();
        payload.addProperty("accountId", userKey);
        Response response = invoker.post(Entity.entity(payload.toString(), MediaType.APPLICATION_JSON_TYPE));
        return response.getStatus() == 201;
    }

    public boolean updateUser(String userKey, List<String> addGroupNames) {
        if (addGroupNames != null && !addGroupNames.isEmpty()) {
            for (String groupName : addGroupNames) {
                if (!addUserToGroup(userKey, groupName)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public boolean deleteUser(String userKey) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("user", Map.of("accountId", userKey));
        Response response = invoker.delete();
        return response.getStatus() == 204;
    }

    public Issue createIssue(String projectKey, String summary, String description, String parentKey,
                             String issueType) {
        return createIssue(projectKey, summary, description, parentKey, issueType, Map.of());

    }

    /**
     * Creates an issue with a request type name (simplified version).
     * This method automatically resolves the request type name to the correct
     * custom field ID and field value.
     *
     * @param projectKey      The project key
     * @param summary         The issue summary
     * @param description     The issue description
     * @param parentKey       The parent issue key (optional)
     * @param issueType       The issue type
     * @param requestTypeName The request type name (e.g., "Get IT help"). Can be
     *                        null or empty.
     * @return The created issue
     */
    public Issue createIssueWithRequestTypeName(String projectKey, String summary, String description,
                                                String parentKey, String issueType, String requestTypeName) {
        LOGGER.info("createIssueWithRequestTypeName called with: projectKey='{}', issueType='{}', requestTypeName='{}'",
                projectKey, issueType, requestTypeName);

        // If request type name is provided, try to create as a Service Desk Request
        // first
        if (requestTypeName != null && !requestTypeName.trim().isEmpty()) {
            try {
                LOGGER.info("Resolving request type information for Service Desk API...");

                // 1. Get Service Desk ID
                String serviceDeskId = getServiceDeskIdForProject(projectKey);

                if (serviceDeskId != null) {
                    LOGGER.info("Found Service Desk ID: {}", serviceDeskId);

                    // 2. Get Request Type ID
                    String requestTypeId = getRequestTypeIdByName(serviceDeskId, requestTypeName);

                    if (requestTypeId != null) {
                        LOGGER.info("Found Request Type ID: {}", requestTypeId);
                        return createCustomerRequest(serviceDeskId, requestTypeId, summary, description);
                    } else {
                        // STRICT VALIDATION: Throw error if Request Type provided but not found
                        String errorMsg = String.format(
                                "Request Type '%s' not found for Service Desk ID '%s' (Project '%s').",
                                requestTypeName, serviceDeskId, projectKey);
                        LOGGER.error(errorMsg);
                        throw new IllegalArgumentException(errorMsg);
                    }
                } else {
                    // STRICT VALIDATION: Throw error if Service Desk not found for project
                    String errorMsg = String.format(
                            "Service Desk not found for project '%s'. Cannot create request with type '%s'.",
                            projectKey, requestTypeName);

                    LOGGER.error(errorMsg);
                    throw new IllegalArgumentException(errorMsg);
                }
            } catch (IllegalArgumentException e) {
                // Re-throw our own validation exceptions
                throw e;
            } catch (Exception e) {
                LOGGER.error("Error attempting to create Service Desk request: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to create Service Desk request: " + e.getMessage(), e);
            }
        }

        // Only reachable if requestTypeName was empty/null
        LOGGER.info("No request type provided, creating standard issue via api/3/issue");
        return createIssue(projectKey, summary, description, parentKey, issueType, Map.of());
    }

    /**
     * Creates a customer request using the Service Desk API.
     * POST /rest/servicedeskapi/request
     */
    public Issue createCustomerRequest(String serviceDeskId, String requestTypeId, String summary, String description) {
        Invocation.Builder invoker = getRestClient().getJiraServiceResourceInvoker("request", new HashMap<>());

        JsonObject payload = new JsonObject();
        payload.addProperty("serviceDeskId", serviceDeskId);
        payload.addProperty("requestTypeId", requestTypeId);

        JsonObject requestFieldValues = new JsonObject();
        requestFieldValues.addProperty("summary", summary);

        // Service Desk API expects description as a String, not ADF JSON.
        // We strip basic HTML tags if present since it expects plain text or basic wiki
        // markup.
        // If the description is ADF JSON, we extract the text content.
        String descriptionText = "";

        if (description != null) {
            String trimmedDesc = description.trim();
            if (trimmedDesc.startsWith("{") && trimmedDesc.endsWith("}")) {
                try {
                    // Try to parse as ADF JSON
                    JsonObject adfJson = GSON.fromJson(trimmedDesc, JsonObject.class);
                    if (adfJson.has("type") && "doc".equals(adfJson.get("type").getAsString())
                            && adfJson.has("content")) {
                        descriptionText = extractTextFromADF(adfJson);
                        LOGGER.info("Extracted text from ADF description: {}", descriptionText);
                    } else {
                        // Not ADF or unknown JSON, treat as string? Or maybe it's just a JSON string.
                        // For safety, use the original string but maybe log warning.
                        descriptionText = description;
                    }
                } catch (Exception e) {
                    // Not valid JSON, treat as plain text/HTML
                    descriptionText = description;
                }
            } else {
                // Not JSON, treat as plain text/HTML
                descriptionText = description;
            }

            // Clean up HTML if present (valid for non-JSON inputs)
            if (descriptionText.contains("<") && descriptionText.contains(">")) {
                descriptionText = descriptionText.replaceAll("<br/?>", "\n")
                        .replaceAll("<[^>]+>", "");
            }
        }

        requestFieldValues.addProperty("description", descriptionText);
        if (description != null && description.contains("<") && description.contains(">")) {
            // Very basic strip for common block tags to avoid massive mess,
            // but Jsoup would be better if available.
            // Since we don't want to add deps, lets try sending raw first or just replacing
            // <br> with newlines
            descriptionText = description.replaceAll("<br/?>", "\n").replaceAll("<[^>]+>", "");
        }
        requestFieldValues.addProperty("description", descriptionText);

        payload.add("requestFieldValues", requestFieldValues);

        LOGGER.info("Sending to Service Desk API - Request body: {}", payload.toString());

        Response response = invoker.post(Entity.entity(payload.toString(), MediaType.APPLICATION_JSON));
        String jsonResponse = response.readEntity(String.class);

        LOGGER.info("Service Desk API Response: {}", jsonResponse);

        if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
            LOGGER.error("Failed to create customer request. Status: {}, Response: {}", response.getStatus(),
                    jsonResponse);
            throw new IllegalArgumentException(jsonResponse);
        }

        JsonObject jsonObject = GSON.fromJson(jsonResponse, JsonObject.class);
        try {
            // Service Desk API returns "issueKey" or "issueId"
            String key = jsonObject.get("issueKey").getAsString();
            LOGGER.info("Customer Request created successfully with key: {}", key);
            return getIssueFinal(key);
        } catch (Exception cause) {
            LOGGER.error("Failed to parse created issue key. Response: {}", jsonResponse);
            throw new IllegalArgumentException(jsonResponse);
        }
    }

    /**
     * Recursively extracts text from an Atlassian Document Format (ADF) JSON
     * object.
     */
    private String extractTextFromADF(JsonObject adfNode) {
        StringBuilder sb = new StringBuilder();
        if (adfNode.has("content")) {
            JsonArray contentArray = adfNode.getAsJsonArray("content");
            for (JsonElement element : contentArray) {
                if (element.isJsonObject()) {
                    JsonObject node = element.getAsJsonObject();
                    // Append text content if present
                    if (node.has("text")) {
                        sb.append(node.get("text").getAsString());
                    }

                    // Recurse
                    String childText = extractTextFromADF(node);
                    if (!childText.isEmpty()) {
                        sb.append(childText);
                    }

                    // Append newline after block elements
                    if (node.has("type")) {
                        String type = node.get("type").getAsString();
                        if ("paragraph".equals(type) || "heading".equals(type) || "codeBlock".equals(type)
                                || "listItem".equals(type)) {
                            sb.append("\n");
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Gets the Request Type ID by name.
     */
    public String getRequestTypeIdByName(String serviceDeskId, String requestTypeName) {
        try {
            String response = getRestClient().fetchJiraService(
                    "servicedesk/" + serviceDeskId + "/requesttype",
                    Map.of("start", 0, "limit", 100),
                    String.class);

            JsonObject jsonObject = GSON.fromJson(response, JsonObject.class);
            JsonArray requestTypes = jsonObject.getAsJsonArray("values");

            if (requestTypes != null) {
                for (JsonElement element : requestTypes) {
                    JsonObject requestTypeObj = element.getAsJsonObject();
                    String name = requestTypeObj.get("name").getAsString();
                    if (requestTypeName.equalsIgnoreCase(name)) {
                        return requestTypeObj.get("id").getAsString();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            LOGGER.error("Error fetching request types: {}", e.getMessage());
            return null;
        }
    }

    public Issue createIssue(String projectKey, String summary, String description, String parentKey, String issueType,
                             Map<String, Object> requestType) {
        LOGGER.info("createIssue called with requestType map: {}", requestType);

        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue", new HashMap<>());
        JsonObject fields = new JsonObject();
        JsonObject project = new JsonObject();
        JsonObject issueTypeMap = new JsonObject();
        issueTypeMap.addProperty("id", getIssuesTypeId(projectKey, issueType));
        project.addProperty("key", projectKey);
        fields.add("project", project);
        fields.add("issuetype", issueTypeMap);
        fields.addProperty("summary", summary);
        fields.add("description", getAtlassianDoc(description));

        if (requestType != null && !requestType.isEmpty()) {
            LOGGER.info("Adding request type to fields...");
            addRequestType(requestType, fields);
        } else {
            LOGGER.info("No request type to add (map is null or empty)");
        }

        if (parentKey != null) {
            JsonObject parent = new JsonObject();
            parent.addProperty("key", parentKey);
            fields.add("parent", parent);
        }
        if (issueType.equalsIgnoreCase("Epic") && findProjectStyle(projectKey).equalsIgnoreCase("classic")) {
            fields.addProperty("customfield_10011", description);
        }

        JsonObject data = new JsonObject();
        data.add("fields", fields);

        LOGGER.info("Sending to Jira API - Request body: {}", data.toString());

        Response response;
        response = invoker.post(Entity.entity(data.toString(), MediaType.APPLICATION_JSON));
        String jsonResponse = response.readEntity(String.class);

        LOGGER.info("Jira API Response: {}", jsonResponse);

        JsonObject jsonObject = GSON.fromJson(jsonResponse, JsonObject.class);
        try {
            String key = jsonObject.get("key").getAsString();
            LOGGER.info("Issue created successfully with key: {}", key);
            return getIssueFinal(key);
        } catch (Exception cause) {
            LOGGER.error("Failed to create issue. Response: {}", jsonResponse);
            throw new IllegalArgumentException(jsonResponse);
        }
    }

    /**
     * Adds request type field to the issue fields.
     * This method handles Jira Service Management request types.
     *
     * @param requestType Map containing:
     *                    - "Custom Field" (field ID like "customfield_10010")
     *                    - "Field Value" (portal/key like "1/2")
     * @param fields      The JsonObject to add the request type field to
     */
    private void addRequestType(Map<String, Object> requestType, JsonObject fields) {
        LOGGER.info("addRequestType called with map: {}", requestType);

        String requestTypeCustomField = (String) requestType.get("Custom Field");
        String fieldValue = (String) requestType.get("Field Value");

        LOGGER.info("Extracted from map - Custom Field: '{}', Field Value: '{}'", requestTypeCustomField, fieldValue);

        // If Custom Field and Field Value are provided directly, use them
        if (requestTypeCustomField != null && !requestTypeCustomField.trim().isEmpty()
                && fieldValue != null && !fieldValue.trim().isEmpty()) {

            fields.addProperty(requestTypeCustomField, fieldValue);
            LOGGER.info("✓ Successfully added request type field to Jira payload: {} = {}", requestTypeCustomField,
                    fieldValue);

        } else {
            // Log warning if request type information is incomplete
            LOGGER.error("✗ Request Type NOT added - Missing required information!");
            LOGGER.error("  Custom Field: '{}'", requestTypeCustomField);
            LOGGER.error("  Field Value: '{}'", fieldValue);
            LOGGER.error("To set a request type, provide both 'Custom Field' (e.g., 'customfield_10010') " +
                    "and 'Field Value' (e.g., '1/2' for portal/request type key)");
        }
    }

    private String getIssueId(String issueKey) {
        Issue issue = getIssueFinal(issueKey);
        return issue.id;

    }

    public String getRequestTypeFieldValue(String issueKey, String requestTypeName) {
        String issueId = getIssueId(issueKey);
        String res = getRestClient().fetchJiraService("request/" + issueId + "/requesttype", Map.of(), String.class);
        LOGGER.info("getRequestTypeFieldValue fetch successfully");
        JsonObject jsonObject = GSON.fromJson(res, JsonObject.class);
        return getPortalAndKeyByRequestType(
                jsonObject.has("validRequestTypes") ? jsonObject.getAsJsonArray("validRequestTypes") : new JsonArray(),
                requestTypeName);
    }

    private String getPortalAndKeyByRequestType(JsonArray validRequestTypes, String requestTypeName) {
        for (JsonElement element : validRequestTypes) {
            JsonObject jsonObject = element.getAsJsonObject();
            if (jsonObject.get("name").getAsJsonPrimitive().equals(new JsonPrimitive(requestTypeName))) {
                String portalKey = jsonObject.get("portalKey").getAsString();
                String key = jsonObject.get("key").getAsString();
                String fieldValue = portalKey + "/" + key;
                LOGGER.info("Portal key and Key {}", fieldValue);
                return fieldValue;
            }
        }
        return null;
    }

    /**
     * Gets the Service Desk ID for a project.
     *
     * @param projectKey The project key
     * @return The service desk ID, or null if not found
     */
    private String getServiceDeskIdForProject(String projectKey) {
        try {
            // Use fetchJiraService for modern API
            String response = getRestClient().fetchJiraService(
                    "servicedesk",
                    Map.of("start", 0, "limit", 100),
                    String.class);

            JsonObject jsonObject = GSON.fromJson(response, JsonObject.class);
            JsonArray serviceDesks = jsonObject.getAsJsonArray("values");

            if (serviceDesks != null) {
                for (JsonElement element : serviceDesks) {
                    JsonObject serviceDeskObj = element.getAsJsonObject();

                    // The projectKey might be a string or an object
                    String sdProjectKey = null;
                    if (serviceDeskObj.has("projectKey")) {
                        JsonElement projectKeyElement = serviceDeskObj.get("projectKey");
                        if (projectKeyElement.isJsonPrimitive()) {
                            sdProjectKey = projectKeyElement.getAsString();
                        } else if (projectKeyElement.isJsonObject()) {
                            sdProjectKey = projectKeyElement.getAsJsonObject().get("key").getAsString();
                        }
                    }

                    if (sdProjectKey != null && projectKey.equalsIgnoreCase(sdProjectKey)) {
                        String id = serviceDeskObj.get("id").getAsString();
                        LOGGER.info("Found service desk ID '{}' for project: {}", id, projectKey);
                        return id;
                    }
                }
            }

            LOGGER.warn("Service desk not found for project: {}", projectKey);
            return null;

        } catch (Exception e) {
            LOGGER.error("Error getting service desk ID for project '{}': {}", projectKey, e.getMessage(), e);
            return null;
        }
    }

    private String findProjectStyle(String projectKey) {
        String projectResponse = projectDao.getProjectByIdResponse(projectKey);
        JsonObject jsonObject = GSON.fromJson(projectResponse, JsonObject.class);
        return jsonObject.get("style").getAsString();
    }

    private String getIssuesTypeId(String projectKey, String issueType) {
        try {
            String jsonResponse = projectDao.getIssueTypeFromProject(projectKey);
            JsonArray issueTypes = GSON.fromJson(jsonResponse, JsonArray.class);
            for (int i = 0; i < issueTypes.size(); i++) {
                JsonObject issueTypeJson = issueTypes.get(i).getAsJsonObject();
                if (issueTypeJson.get("name").getAsString().equals(issueType)) {
                    return issueTypeJson.get("id").getAsString();
                }
            }
        } catch (Exception cause) {
            throw new IllegalArgumentException("The Issue type or the Project key provided are invalid", cause);
        }
        throw new IllegalArgumentException("Invalid issue type");
    }

    public Boolean validateIssueType(String projectKey, String issueType) {
        try {
            getIssuesTypeId(projectKey, issueType);
            return true;
        } catch (IllegalArgumentException cause) {
            return false;
        }
    }

    public void addComment(String issueKey, String commentInput, String visibility) {
        LOGGER.debug("Jira Comment : {}", commentInput);
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey + "/comment",
                new HashMap<>());
        String comment = commentInput.endsWith("<br>") ? commentInput.replaceAll("<br>$", "") : commentInput;
        LOGGER.debug("Jira Comment After BR Tag  : {}", comment);
        JsonObject bodyContent = getAtlassianDoc(comment);
        JsonObject fields = new JsonObject();
        fields.add("body", bodyContent);

        // Set comment visibility: true = Public, false = Internal
        String cleanVisibility = visibility != null ? visibility.trim() : "";
        boolean isPublic = !"Internal".equalsIgnoreCase(cleanVisibility);

        JsonObject visibilityValue = new JsonObject();
        visibilityValue.addProperty("internal", !isPublic); // Jira uses "internal": true for internal comments

        JsonObject visibilityProp = new JsonObject();
        visibilityProp.addProperty("key", "sd.public.comment");
        visibilityProp.add("value", visibilityValue); // pass the full object here

        JsonArray properties = new JsonArray();
        properties.add(visibilityProp);
        fields.add("properties", properties);

        LOGGER.debug("Visibility input: '{}', interpreted as public: {}", visibility, isPublic);
        LOGGER.debug("Comment properties JSON: {}", properties);
        LOGGER.debug("Final fields JSON (including body and properties): {}", fields);

        Response response = invoker.post(Entity.entity(fields.toString(), MediaType.APPLICATION_JSON));
        LOGGER.debug("Response: {}", response);
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed updating status " + response.readEntity(String.class));
        }
    }

    public List<Map<String, Object>> getComments(String issueKey) {
        String res = getRestClient().fetch("issue/" + issueKey + "/comment", new HashMap<>(), String.class);
        LOGGER.debug("Raw comment response for issue {}: {}", issueKey, res);
        JsonObject jsonObject = GSON.fromJson(res, JsonObject.class);
        JsonArray commentsArray = jsonObject.getAsJsonArray("comments");
        List<Map<String, Object>> comments = new ArrayList<>();
        if (commentsArray != null) {
            for (JsonElement element : commentsArray) {
                if (element.isJsonObject()) {
                    JsonObject commentObj = element.getAsJsonObject();
                    LOGGER.debug("Processing comment object: {}", commentObj.toString());
                    Map<String, Object> commentData = new LinkedHashMap<>();

                    // Extract comment body
                    if (commentObj.has("body")) {
                        JsonElement bodyElement = commentObj.get("body");
                        if (bodyElement.isJsonObject()) {
                            commentData.put("Comment Body", extractTextFromADF(bodyElement.getAsJsonObject()));
                        } else if (bodyElement.isJsonPrimitive()) {
                            commentData.put("Comment Body", bodyElement.getAsString());
                        }
                    }

                    // Extract author information
                    if (commentObj.has("author")) {
                        JsonObject authorObj = commentObj.getAsJsonObject("author");

                        // Author ID (Account ID)
                        if (authorObj.has("accountId")) {
                            commentData.put("Author ID", authorObj.get("accountId").getAsString());
                        } else {
                            commentData.put("Author ID", null);
                        }

                        // Author Name (Display Name)
                        if (authorObj.has("displayName")) {
                            commentData.put("Author Name", authorObj.get("displayName").getAsString());
                        } else {
                            commentData.put("Author Name", null);
                        }
                    } else {
                        commentData.put("Author ID", null);
                        commentData.put("Author Name", null);
                    }

                    // Extract created date
                    if (commentObj.has("created")) {
                        String createdStr = commentObj.get("created").getAsString();
                        try {
                            // Parse ISO 8601 date format and convert to timestamp
                            java.time.OffsetDateTime offsetDateTime = java.time.OffsetDateTime.parse(createdStr,
                                    JIRA_DATE_FORMATTER);
                            long timestamp = offsetDateTime.toInstant().toEpochMilli();
                            commentData.put("Date Created", timestamp);
                            LOGGER.debug("Parsed created date: {} -> {}", createdStr, timestamp);
                        } catch (Exception e) {
                            LOGGER.error("Failed to parse created date: {}", createdStr, e);
                            commentData.put("Date Created", null);
                        }
                    } else {
                        LOGGER.warn("Comment does not have 'created' field");
                        commentData.put("Date Created", null);
                    }

                    comments.add(commentData);
                }
            }
        }
        return comments;
    }

    private JsonObject getAtlassianDoc(String description) {
        HtmlToAdfConverter converter = new HtmlToAdfConverter();
        return converter.convert(description);
    }

    public List<Issue> searchByJQL(String jql, long size, long page) {
        Map<String, Object> qparams = new HashMap<>();
        qparams.put("jql", jql);
        qparams.put("fields", "*all");
        qparams.put("startAt", Math.max((page - 1) * size, 0));
        qparams.put("maxResults", size == 0 ? 20 : size);
        String out = restClient.fetch("search/jql", qparams, String.class);
        JsonObject jsonObject = GSON.fromJson(out, JsonObject.class);
        if (jsonObject.has(ISSUES)) {
            JsonArray issues = (JsonArray) jsonObject.get(ISSUES);
            return getIssues(issues);
        } else {
            throw new RuntimeException(jsonObject.has(ERROR_MESSAGES)
                    ? (jsonObject.get(ERROR_MESSAGES)).getAsJsonArray().get(0).getAsString()
                    : ISSUES_NOT_FOUND);
        }
    }

    public List<Issue> searchByJqlQuery(boolean invokeAsUser, String accountId, String invokerId, String jql, long size,
                                        long page) {
        List<Issue> totalIssues = new ArrayList<>();
        while (totalIssues.size() < TOTAL_ISSUES_LIMIT) {
            Map<String, Object> qparams = prepareQueryparam(jql, size, page);
            String out = getRestClient(invokeAsUser).fetchIssues(invokeAsUser, accountId, invokerId, "search/jql",
                    qparams, String.class);
            JsonObject jsonObject = GSON.fromJson(out, JsonObject.class);
            if (jsonObject.has(ISSUES)) {
                JsonArray issues = (JsonArray) jsonObject.get(ISSUES);
                totalIssues.addAll(getIssues(issues));
                if ((issues.size() < size)) {
                    break;
                }
                page++;
            } else {
                throw new RuntimeException(jsonObject.has(ERROR_MESSAGES)
                        ? (jsonObject.get(ERROR_MESSAGES)).getAsJsonArray().get(0).getAsString()
                        : ISSUES_NOT_FOUND);
            }
        }
        return totalIssues;
    }

    /**
     * This method which is used to validate JQL queries, if JQL is incorrect then
     * it returns Map of query and errors contain in JQL query.
     *
     * @param jql       query which needs to be validated and fetch metadata.
     * @param accountId denotes user information.
     * @param invokerId denotes invoker information.
     * @return Map of query and errors contain in JQL query
     */

    public Map<String, String> fetchErrorsInJQLQuery(boolean invokeAsUser, String accountId, String invokerId,
                                                     String jql) {
        Map<String, String> queryData = new HashMap<>();
        String jsonResponse = getRestClient(invokeAsUser).parseAndValidateJQL(jql, invokeAsUser, accountId, invokerId);
        if (jsonResponse != null && jsonResponse.isBlank()) {
            return Collections.emptyMap();
        }
        JsonObject jsonObject = GSON.fromJson(jsonResponse, JsonObject.class);
        if (jsonObject.get(JiraConstants.QUERIES) != null
                && jsonObject.get(JiraConstants.QUERIES).getAsJsonArray().get(0) != null) {
            JsonObject data = jsonObject.get(JiraConstants.QUERIES).getAsJsonArray().get(0).getAsJsonObject();
            if (data.get(JiraConstants.ERRORS) != null
                    && data.get(JiraConstants.ERRORS).getAsJsonArray().get(0) != null) {
                queryData.put(JiraConstants.ERRORS,
                        data.get(JiraConstants.ERRORS).getAsJsonArray().get(0).getAsString());
                if (data.get(JiraConstants.QUERY) != null) {
                    queryData.put(JiraConstants.QUERY, data.get(JiraConstants.QUERY).getAsString());
                }
            } else if (data.get(JiraConstants.QUERY) != null) {
                queryData.put(JiraConstants.QUERY, data.get(JiraConstants.QUERY).getAsString());
            }
            return queryData;
        }

        return Collections.emptyMap();
    }

    public List<Issue> getIssues(JsonArray issues) {
        List<Issue> issuesFinal = new ArrayList<>();
        for (int i = 0; i < issues.size(); i++) {
            JsonElement element = issues.get(i);
            if (element.isJsonObject()) {
                JsonObject asJsonObject = element.getAsJsonObject();
                JiraIssue issue = JiraIssue.create(asJsonObject);
                issuesFinal.add(issue.getIssue());
            }
        }
        return issuesFinal;
    }

    private Map<String, Object> prepareQueryparam(String jql, long size, long page) {
        Map<String, Object> qparams = new HashMap<>();
        qparams.put("jql", jql);
        qparams.put("fields", "*all");
        qparams.put("startAt", Math.max((page - 1) * size, 0));
        qparams.put("maxResults", size == 0 ? 50 : size);
        return qparams;
    }

    public void updateStatus(String issueKey, String statusName) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey + "/transitions",
                new HashMap<>());
        List<TransitionStatus> transitionStatusList = ticketInfoDao.getTransitions(issueKey);
        TransitionStatus status = transitionStatusList.stream()
                .filter(tstatus -> tstatus.toName.equalsIgnoreCase(statusName)).findFirst()
                .orElseThrow(() -> new RuntimeException("We can't change the status of the issue key " + issueKey
                        + " to " + statusName + " because no transitions are available to this status."));
        JsonObject transitions = new JsonObject();
        transitions.addProperty("id", status.fromId);
        JsonObject body = new JsonObject();
        body.add("transition", transitions);
        Response response;
        response = invoker.post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed updating status " + response.readEntity(String.class));
        }
    }

    public void addRequestParticipants(String issueKey, List<String> requestParticipants) {
        LOGGER.info("Add Request Participants Initialised");
        Invocation.Builder invoker = getRestClient()
                .getJiraServiceResourceInvoker("request/" + issueKey + "/participant", new HashMap<>());
        LOGGER.info("Got Invocation Builder service");
        JsonObject update = new JsonObject();
        JsonArray accountIdList = new JsonArray();
        for (String accountId : requestParticipants) {
            accountIdList.add(accountId);
        }
        update.add("accountIds", accountIdList);
        LOGGER.info("Json Body Created for Add Participants : {}", update.getAsString());
        Response response;
        response = invoker.post(Entity.entity(update.toString(), MediaType.APPLICATION_JSON));
        LOGGER.info("Response Post has been executed");
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed to add Request Participants : :" + response.readEntity(String.class));
        }
    }

    public void updateIssue(Issue request) {
        updateIssue(request, null);
    }

    public void updateIssue(Issue request, FreeForm customFields) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + request.key, new HashMap<>());
        Issue old = getIssueFinal(request.key);
        JsonObject update = getUpdateJsonObject(old, request);
        JsonObject fields = getFieldsJsonObject(old, request);
        if (request.status != null && !old.status.trim().equalsIgnoreCase(request.status.trim())) {
            updateStatus(request);
        }
        if (request.assigneeName != null && !old.assigneeName.trim().equalsIgnoreCase(request.assigneeName.trim())) {
            updateAssignee(request);
        }
        if (request.labels != null && !old.labels.equals(request.labels)) {
            updateLabels(request);
        }
        if (request.components != null && !old.components.equals(request.components)) {
            updateComponents(request);
        }
        if (customFields != null && !customFields.isEmpty()) {
            updateCustomFieldToFields(customFields, fields, request.key);
        }
        JsonObject actualObject = getActualUpdateObject(update, fields);
        Response response;
        response = invoker.put(Entity.entity(actualObject.toString(), MediaType.APPLICATION_JSON));
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed to update : :" + response.readEntity(String.class));
        }
    }

    private void updateCustomFieldToFields(FreeForm customFields, JsonObject fields, String issueKey) {
        JsonObject fieldsEditMetaData = getJiraFieldsData(issueKey);

        for (String customFieldName : customFields.keySet()) {
            updateCustomFieldByType(customFields, fields, fieldsEditMetaData, customFieldName);
        }
    }

    private void updateCustomFieldByType(FreeForm customFields, JsonObject fields, JsonObject fieldsEditMetaData,
                                         String customFieldName) {
        for (String fieldId : fieldsEditMetaData.keySet()) {
            JsonObject fieldMetaData = fieldsEditMetaData.getAsJsonObject(fieldId);
            String name = fieldMetaData.get("name").getAsString();
            String optionId;

            if (customFieldName.equals(name)) {
                Object updatedFieldValue = customFields.get(customFieldName);
                String fieldType = getFieldType(fieldMetaData.getAsJsonObject("schema"));

                switch (fieldType) {
                    case "date":
                        if (updatedFieldValue instanceof Number) {
                            updateDate(fields, fieldId, updatedFieldValue);
                        }
                        break;
                    case "string":
                        fields.addProperty(fieldId, updatedFieldValue.toString());
                        break;
                    case "option":
                        optionId = findOptionIdByName(fieldMetaData.getAsJsonArray("allowedValues"),
                                updatedFieldValue.toString());
                        if (optionId != null) {
                            JsonObject optionObject = new JsonObject();
                            optionObject.addProperty("id", optionId);
                            fields.add(fieldId, optionObject);
                        }
                        break;
                    case "array":
                        JsonArray arrayValue = new JsonArray();

                        String itemsType = fieldMetaData.getAsJsonObject("schema").get("items").getAsString();
                        if (!Objects.equals(itemsType, "string")) {
                            if (updatedFieldValue instanceof List<?>) {
                                for (Object value : (List<?>) updatedFieldValue) {
                                    if (value instanceof String) {
                                        optionId = findOptionIdByName(fieldMetaData.getAsJsonArray("allowedValues"),
                                                (String) value);
                                        if (optionId != null) {
                                            JsonObject optionObject = new JsonObject();
                                            optionObject.addProperty("id", optionId);
                                            arrayValue.add(optionObject);
                                        }
                                    }
                                }
                            }
                        } else {
                            if (updatedFieldValue instanceof List<?>) {
                                for (Object value : (List<?>) updatedFieldValue) {
                                    if (value instanceof String) {
                                        arrayValue.add((String) value);
                                    }
                                }
                            }
                        }

                        fields.add(fieldId, arrayValue);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private String findOptionIdByName(JsonArray allowedValues, String optionValue) {
        for (JsonElement element : allowedValues) {
            JsonObject option = element.getAsJsonObject();
            JsonElement valueElement = option.get("value");

            if (valueElement != null && optionValue.equals(valueElement.getAsString())) {
                return option.get("id").getAsString();
            }
        }
        return null;
    }

    private String getFieldType(JsonObject schema) {
        return schema.get("type").getAsString();
    }

    public JsonObject getJiraFieldsData(String issueKey) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(EXPAND, RENDERED_FIELDS);
        String res = getRestClient().fetch("issue/" + issueKey + "/editmeta", queryParams, String.class);
        JsonObject jsonObject = GSON.fromJson(res, JsonObject.class);
        return jsonObject.getAsJsonObject("fields");
    }

    private void updateDate(JsonObject fields, String fieldId, Object date) {
        fields.addProperty(fieldId, new SimpleDateFormat("yyyy-MM-dd").format(new Date((long) ((double) date))));
    }

    private void updateComponents(Issue issue) {
        issue.components.forEach(component -> addComponent(issue.key, component));
    }

    private void updateLabels(Issue issue) {
        issue.labels.forEach(label -> addLabel(label, issue.key));
    }

    public void addLabel(String label, String key) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + key, new HashMap<>());
        JsonObject labelObject = new JsonObject();
        labelObject.addProperty("add", label);
        JsonObject set = new JsonObject();
        JsonArray labelsArray = new JsonArray();
        labelsArray.add(labelObject);
        set.add("labels", labelsArray);
        JsonObject actualObject = new JsonObject();
        actualObject.add("update", set);
        Response response;
        response = invoker.put(Entity.entity(actualObject.toString(), MediaType.APPLICATION_JSON));
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed to update : :" + response.readEntity(String.class));
        }
    }

    private void updateAssignee(Issue issue) {
        try {
            List<JiraUser> jiraUsers = userDao.searchUser(issue.assigneeName);
            JiraUser user = jiraUsers.stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Assignee user not found"));
            assignTask(issue.key, user.accountId);
        } catch (Exception ignore) {
        }
    }

    private void updateStatus(Issue issue) {
        try {
            updateStatus(issue.key, issue.status);
        } catch (Exception ignore) {
        }
    }

    private JsonObject getActualUpdateObject(JsonObject update, JsonObject fields) {
        JsonObject actualObject = new JsonObject();
        actualObject.add("update", update);
        actualObject.add("fields", fields);
        return actualObject;
    }

    private JsonObject getUpdateJsonObject(Issue old, Issue request) {
        JsonObject update = new JsonObject();
        if (request.summary != null && !old.summary.trim().equalsIgnoreCase(request.summary.trim())) {
            JsonObject set = new JsonObject();
            set.addProperty("set", request.summary);
            JsonArray array = new JsonArray();
            array.add(set);
            update.add("summary", array);
        }
        return update;
    }

    private JsonObject getFieldsJsonObject(Issue old, Issue request) {
        JsonObject fields = new JsonObject();
        if (request.description != null && !old.description.trim().equalsIgnoreCase(request.description.trim())) {
            JsonObject description = getAtlassianDoc(request.description);
            fields.add("description", description);
        }
        if (request.priority != null && !old.priority.trim().equalsIgnoreCase(request.priority.trim())) {
            TicketPriority priority1 = new TicketPriority();
            priority1.name = request.priority;
            fields.add("priority", getPriorityJsonObject(priority1));
        }
        return fields;
    }

    public void assignTask(String issueKey, String userAccountId) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey + "/assignee",
                new HashMap<>());
        JsonObject fields = new JsonObject();
        fields.addProperty("accountId", userAccountId);
        Response response;
        response = invoker.put(Entity.entity(fields.toString(), MediaType.APPLICATION_JSON));
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed assigning task to user: :" + response.readEntity(String.class));
        }
    }

    public Issue getIssueFinal(String key) {
        LOGGER.info("Get Issue Final method stage 1 initialised");
        try {
            String res = getIssueResponse(key);
            LOGGER.info(" Get Final issue Fetched Response");
            JsonObject jsonObject = GSON.fromJson(res, JsonObject.class);
            LOGGER.info("Get Final Issue JsonObject");
            JiraIssue issue = JiraIssue.create(jsonObject);
            LOGGER.info("Get Final Issue After processing Jira Issue from Jon Object.");
            Issue issueObj = issue.getIssue();

            // If request type is a field value (like "1/2"), try to resolve it to the name
            if (issueObj.requestType != null && issueObj.requestType.contains("/")) {
                LOGGER.info("Request type field contains value '{}', attempting to resolve to name",
                        issueObj.requestType);
                String requestTypeName = getRequestTypeNameFromIssue(key, issueObj.requestType);
                if (requestTypeName != null) {
                    issueObj.requestType = requestTypeName;
                    LOGGER.info("Resolved request type to name: {}", requestTypeName);
                }
            }

            return issueObj;
        } catch (RuntimeException cause) {
            LOGGER.error("Error at Get Issue Final Method : {}", cause.getMessage(), cause);
        }
        return new Issue();
    }

    /**
     * Gets the request type name from an issue's request type field value.
     *
     * @param issueKey   The issue key
     * @param fieldValue The field value (e.g., "1/2")
     * @return The request type name, or null if not found
     */
    private String getRequestTypeNameFromIssue(String issueKey, String fieldValue) {
        try {
            String issueId = getIssueId(issueKey);
            String res = getRestClient().fetchMiscellaneousService("request/" + issueId + "/request-types", Map.of(),
                    String.class);
            JsonObject jsonObject = GSON.fromJson(res, JsonObject.class);
            JsonArray validRequestTypes = jsonObject.getAsJsonArray("validRequestTypes");

            if (validRequestTypes != null) {
                for (JsonElement element : validRequestTypes) {
                    JsonObject requestTypeObj = element.getAsJsonObject();
                    String portalKey = requestTypeObj.get("portalKey").getAsString();
                    String key = requestTypeObj.get("key").getAsString();
                    String currentFieldValue = portalKey + "/" + key;

                    if (currentFieldValue.equals(fieldValue)) {
                        String name = requestTypeObj.get("name").getAsString();
                        LOGGER.info("Found request type name '{}' for field value '{}'", name, fieldValue);
                        return name;
                    }
                }
            }

            LOGGER.warn("Could not find request type name for field value: {}", fieldValue);
            return null;

        } catch (Exception e) {
            LOGGER.error("Error getting request type name from issue: {}", e.getMessage(), e);
            return null;
        }
    }

    public List<String> getCustomIssue(String issueKey, String customField) {
        String fieldResponse = getFieldResponse();
        String key = getKeyByName(fieldResponse, customField);
        String issueResponse = getIssueResponse(issueKey);
        JsonObject jsonObject = GSON.fromJson(issueResponse, JsonObject.class);
        JsonElement jsonResponse = jsonObject.get(FIELDS).getAsJsonObject().get(key);
        List<String> outputList = new ArrayList<>();

        if (jsonResponse == null) {
            return outputList;
        }
        if (jsonResponse.isJsonObject()) {
            JsonObject obj = jsonResponse.getAsJsonObject();
            if (obj.has(VALUE)) {
                outputList.add(obj.get(VALUE).getAsString());
            }
            return outputList;
        }
        if (jsonResponse.isJsonArray()) {
            JsonArray jsonArray = jsonResponse.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                    outputList.add(element.getAsString());
                }
            }
            return outputList;
        }
        if (jsonResponse.isJsonPrimitive() && jsonResponse.getAsJsonPrimitive().isString()) {
            outputList.add(jsonResponse.getAsString());
        }
        return outputList;
    }

    public static String getKeyByName(String jsonResponse, String name) {
        JSONArray jsonArray = new JSONArray(jsonResponse);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.getString("name").equalsIgnoreCase(name)) {
                return jsonObject.getString("key");
            }
        }

        return null;
    }

    public boolean isIssueExists(String key) {
        try {
            getIssueFinal(key);
            return true;
        } catch (NoSuchElementException | IllegalStateException | IllegalArgumentException cause) {
            return false;
        }
    }

    public String getIssueResponse(String issueKey) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put(EXPAND, RENDERED_FIELDS);
        LOGGER.info("Getting rest client");
        return getRestClient().fetch("issue/" + issueKey, queryParams, String.class);
    }

    public String getFieldResponse() {
        return getRestClient().fetch("field", Map.of(), String.class);
    }

    public Issue updatePriority(TicketPriority priority, String issueKey) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey, new HashMap<>());
        JsonObject fields = new JsonObject();
        fields.add("priority", getPriorityJsonObject(priority));
        JsonObject body = new JsonObject();
        body.add("fields", fields);
        Response res;
        res = invoker.put(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        if (res.getStatus() >= HttpsURLConnection.HTTP_BAD_REQUEST) {
            throw new RuntimeException(
                    "Failed to update priority for ticket " + issueKey + res.readEntity(String.class));
        }
        return getIssueFinal(issueKey);
    }

    public void addWorklog(String timeSpent, long date, String issueKey, String comment) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey + "/worklog",
                new HashMap<>());
        JsonObject body = new JsonObject();
        body.addProperty("timeSpent", timeSpent);
        body.addProperty("started", DatePlus.getISODate(new Date(date)));
        if (comment != null && !comment.isEmpty()) {
            body.add("comment", getAtlassianDoc(comment));
        }
        Response res;
        res = invoker.post(Entity.entity(body.toString(), MediaType.APPLICATION_JSON));
        if (res.getStatus() >= HttpsURLConnection.HTTP_BAD_REQUEST) {
            throw new RuntimeException(res.readEntity(String.class));
        }
    }

    public boolean attachFileToTicket(List<File> files, String ticketId) {
        return getRestClient().attachFile(ticketId, files).getStatus() == 200;
    }

    public void assignIssueToEpic(String issueKey, String epicKey) {
        String epicLinkId = getEpicLinkId();
        JSONObject epicLink = new JSONObject();
        epicLink.put(epicLinkId, epicKey);
        JSONObject fields = new JSONObject();
        fields.put("fields", epicLink);

        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + issueKey + "/", new HashMap<>());
        Response response;
        response = invoker.put(Entity.entity(fields.toString(), MediaType.APPLICATION_JSON));

        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException(response.readEntity(String.class));
        }

    }

    private String getEpicLinkId() {
        String out = getRestClient().fetch("field", new HashMap<>(), String.class);
        JSONArray jsonArray = new JSONArray(out);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            if (jsonObject.get("name").equals("Epic Link")) {
                return jsonObject.get("key").toString();
            }
        }
        return "";

    }

    public void assignChildIssueToEpic(String childIssueKey, String parentEpicKey) {
        try {
            Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + childIssueKey, new HashMap<>());
            JSONObject parentdetails = new JSONObject();
            parentdetails.put("key", parentEpicKey);
            JSONObject parent = new JSONObject();
            parent.put("parent", parentdetails);
            JSONObject fields = new JSONObject();
            fields.put("fields", parent);
            Response response;
            response = invoker.put(Entity.entity(fields.toString(), MediaType.APPLICATION_JSON));
            if (response.getStatus() != HttpsURLConnection.HTTP_NO_CONTENT) {
                throw new RuntimeException(response.readEntity(String.class));
            }
        } catch (Exception cause) {
            System.out.println("Did not complete request");
            throw cause;
        } finally {
            System.out.println("Assign Child Issue to Epic finished");
        }
    }

    public void addComponent(String key, String component) {
        Invocation.Builder invoker = getRestClient().getResourceInvoker("issue/" + key, new HashMap<>());
        JsonObject actualObject = getactualJsonObject(component);
        Response response;
        response = invoker.put(Entity.entity(actualObject.toString(), MediaType.APPLICATION_JSON));
        if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            throw new RuntimeException("Failed to update : :" + response.readEntity(String.class));
        }
    }

    private JsonObject getactualJsonObject(String component) {
        JsonObject componentName = new JsonObject();
        componentName.addProperty("name", component);
        JsonObject componentObject = new JsonObject();
        componentObject.add("add", componentName);
        JsonObject set = new JsonObject();
        JsonArray components = new JsonArray();
        components.add(componentObject);
        set.add("components", components);
        JsonObject actualObject = new JsonObject();
        actualObject.add("update", set);
        return actualObject;
    }

    public List<Component> getComponents() {
        List<TicketProject> allProjects = projectDao.getAllProjects();
        List<Component> allComponents = new ArrayList<>();
        for (TicketProject project : allProjects) {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("maxResults", 150);
            String jsonResponse = getRestClient().fetch("project/" + project.key + "/components", queryParams,
                    String.class);
            if (!jsonResponse.isBlank()) {
                JsonArray jsonComponents = GSON.fromJson(jsonResponse, JsonArray.class);
                jsonComponents.forEach(jsonComponent -> allComponents
                        .add(new Component(jsonComponent.getAsJsonObject().get("id").getAsString(),
                                jsonComponent.getAsJsonObject().get("name").getAsString(),
                                jsonComponent.getAsJsonObject().get("project").getAsString())));
            }
        }
        return allComponents;
    }

    public List<Label> getLabels() {
        List<Label> allLabels = new ArrayList<>();
        String jsonResponse = getRestClient().fetch("label", new HashMap<>(), String.class);
        JsonObject jsonLabels = GSON.fromJson(jsonResponse, JsonObject.class);
        JsonArray values = jsonLabels.get("values").getAsJsonArray();
        values.forEach(value -> allLabels.add(new Label(value.getAsString())));
        return allLabels;
    }

    /**
     * This method validates the credentials that are currently stored in setup
     * attributes.
     */
    public void validateCredentials() {
        getRestClient(true).getJiraAccessToken();
    }
}
