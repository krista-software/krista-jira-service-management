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

package app.krista.extensions.devsecops.development.jira.authentication;

import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Map;

public final class AuthRestClient implements RestClient {

    private final Map<String, Object> attributes;
    private final boolean invokeAsUser;
    private final boolean callFromValidateAttributes;
    private WebTarget webTarget;
    private WebTarget webTargetServiceDesk;
    private WebTarget webTargetServiceManage;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthRestClient.class);

    public AuthRestClient(Map<String, Object> attributes, boolean invokeAsUser, boolean callFromValidateAttributes) {
        this.attributes = attributes;
        this.invokeAsUser = invokeAsUser;
        this.callFromValidateAttributes = callFromValidateAttributes;
    }

    private WebTarget getWebTarget(Boolean invokeAsUser) {
        // LOGGER.info("Basic Auth web target Initialised : {}",
        // attributes.get(JiraAttributes.AUTH_TOKEN));
        LOGGER.info("Basic Auth web target Initialised");
        if ((invokeAsUser != null ? invokeAsUser : this.invokeAsUser) && !callFromValidateAttributes) {
            throw new IllegalArgumentException(
                    "User cannot be authenticated with their own account if client ID and client secret are not provided.");
        }
        if (webTarget == null) {
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(
                    (String) attributes.get(JiraAttributes.USER_NAME),
                    (String) attributes.get(JiraAttributes.AUTH_TOKEN));
            Client client = ClientBuilder.newClient();
            client.register(feature);
            webTarget = client.target((String) attributes.get(JiraAttributes.HOST_URL));
        }
        LOGGER.info("Basic Auth web target Created with HOST URL : {}", attributes.get(JiraAttributes.HOST_URL));
        return webTarget;
    }

    private String getBaseUrl(String hostUrl) {
        if (hostUrl == null || hostUrl.isEmpty()) {
            return "";
        }
        int index = hostUrl.indexOf("/rest/");
        if (index > 0) {
            return hostUrl.substring(0, index);
        }
        // Handle case where hostUrl might end with /rest without trailing slash or path
        if (hostUrl.endsWith("/rest")) {
            return hostUrl.substring(0, hostUrl.length() - 5);
        }
        return hostUrl;
    }

    private WebTarget getJiraServiceManageWebTarget(Boolean invokeAsUser) {
        LOGGER.info("Basic Auth Service desk api Initialised");
        if ((invokeAsUser != null ? invokeAsUser : this.invokeAsUser) && !callFromValidateAttributes) {
            throw new IllegalArgumentException(
                    "User cannot be authenticated with their own account if client ID and client secret are not provided.");
        }
        if (webTargetServiceManage == null) {
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(
                    (String) attributes.get(JiraAttributes.USER_NAME),
                    (String) attributes.get(JiraAttributes.AUTH_TOKEN));
            Client client = ClientBuilder.newClient();
            client.register(feature);

            String hostUrl = (String) attributes.get(JiraAttributes.HOST_URL);
            String baseUrl = getBaseUrl(hostUrl);
            webTargetServiceManage = client.target(baseUrl + "/rest/servicedeskapi");
        }
        LOGGER.info("Basic Auth Service Created on : {}", webTargetServiceManage.getUri());
        return webTargetServiceManage;
    }

    private WebTarget getJiraServiceAPIWebTarget(Boolean invokeAsUser) {
        LOGGER.info("Basic Auth Service Desk api Initialised");
        if ((invokeAsUser != null ? invokeAsUser : this.invokeAsUser) && !callFromValidateAttributes) {
            throw new IllegalArgumentException(
                    "User cannot be authenticated with their own account if client ID and client secret are not provided.");
        }
        if (webTargetServiceDesk == null) {
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(
                    (String) attributes.get(JiraAttributes.USER_NAME),
                    (String) attributes.get(JiraAttributes.AUTH_TOKEN));
            Client client = ClientBuilder.newClient();
            client.register(feature);

            String hostUrl = (String) attributes.get(JiraAttributes.HOST_URL);
            String baseUrl = getBaseUrl(hostUrl);
            webTargetServiceDesk = client.target(baseUrl + "/rest/servicedesk/1/servicedesk");
        }
        LOGGER.info("Basic Auth Service Desk Created on : {}", webTargetServiceDesk.getUri());
        return webTargetServiceDesk;
    }

    @Override
    public Invocation.Builder getResourceInvoker(String resource, Map<String, Object> queryParams) {
        LOGGER.info("Basic Auth Web Target Default Invocation Builder Resource Endpoint : {}", resource);
        WebTarget resourceTarget = getWebTarget(null).path(resource);
        for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
            resourceTarget = resourceTarget.queryParam(entrySet.getKey(), entrySet.getValue());
        }
        LOGGER.info("Basic auth Default Parameter mapped to header");
        return resourceTarget.request(MediaType.APPLICATION_JSON).header("Content-Type", "application/json");
    }

    @Override
    public Invocation.Builder getJiraServiceResourceInvoker(String resource, Map<String, Object> queryParams) {
        LOGGER.info("Basic Auth Service Invocation Builder Resource Endpoint : {}", resource);
        WebTarget resourceTarget = getJiraServiceManageWebTarget(null).path(resource);
        for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
            resourceTarget = resourceTarget.queryParam(entrySet.getKey(), entrySet.getValue());
        }
        LOGGER.info("Basic auth Parameter mapped to header");
        return resourceTarget.request(MediaType.APPLICATION_JSON).header("Content-Type", "application/json");
    }

    public <T> T fetch(String resource, Map<String, Object> queryParams, Class<T> type) {
        LOGGER.info("Basic Auth Default fetch Jira Resource Endpoint : {}", resource);
        if (resource == null)
            throw new IllegalArgumentException("resource could not be null");
        WebTarget resourceTarget = getWebTarget(null).path(resource);
        for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
            resourceTarget = resourceTarget.queryParam(entrySet.getKey(), entrySet.getValue());
        }
        LOGGER.info("Web target Default received at fetchJira");
        Invocation.Builder requestBuilder = resourceTarget.request(MediaType.APPLICATION_JSON);
        Response response = requestBuilder.get();
        LOGGER.info("response executed successfully at fetchJira Default");
        T entityType = response.readEntity(type);
        if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            return entityType;
        } else if (response.getStatus() == 404) {
            throw new IllegalStateException("Please Provide Valid Host URL");
        } else if (response.getStatus() == 400) {
            throw new IllegalStateException("Please Provide Valid Auth Token OR User Name");
        }
        throw new IllegalStateException((String) entityType);

    }

    @Override
    public <T> T fetchMiscellaneousService(String resource, Map<String, Object> queryParams, Class<T> type) {
        LOGGER.info("Basic Auth fetch fetchMiscellaneousService Jira Resource Endpoint : {}", resource);
        if (resource == null)
            throw new IllegalArgumentException("resource could not be null");
        WebTarget resourceTarget = getJiraServiceAPIWebTarget(null).path(resource);
        for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
            resourceTarget = resourceTarget.queryParam(entrySet.getKey(), entrySet.getValue());
        }
        LOGGER.info("Web target received at fetchMiscellaneousService");
        Invocation.Builder requestBuilder = resourceTarget.request(MediaType.APPLICATION_JSON);
        Response response = requestBuilder.get();
        LOGGER.info("response executed successfully at fetchMiscellaneousService");
        T entityType = response.readEntity(type);
        if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            return entityType;
        } else if (response.getStatus() == 404) {
            throw new IllegalStateException("Please Provide Valid Host URL");
        } else if (response.getStatus() == 400) {
            throw new IllegalStateException("Please Provide Valid Auth Token OR User Name");
        }
        throw new IllegalStateException((String) entityType);

    }

    @Override
    public <T> T fetchJiraService(String resource, Map<String, Object> queryParams, Class<T> type) {
        LOGGER.info("Basic Auth fetch Jira Service Resource Endpoint : {}", resource);
        if (resource == null)
            throw new IllegalArgumentException("resource could not be null");
        WebTarget resourceTarget = getJiraServiceManageWebTarget(null).path(resource);
        for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
            resourceTarget = resourceTarget.queryParam(entrySet.getKey(), entrySet.getValue());
        }
        LOGGER.info("Web target received at fetchJira Service");
        Invocation.Builder requestBuilder = resourceTarget.request(MediaType.APPLICATION_JSON);
        Response response = requestBuilder.get();
        LOGGER.info("response executed successfully at fetchJira Service");
        T entityType = response.readEntity(type);
        if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            return entityType;
        } else if (response.getStatus() == 404) {
            throw new IllegalStateException("Please Provide Valid Host URL");
        } else if (response.getStatus() == 400) {
            throw new IllegalStateException("Please Provide Valid Auth Token OR User Name");
        }
        throw new IllegalStateException((String) entityType);
    }

    /**
     * This is post method which Parses and validates JQL queries, if JQL is
     * incorrect then it returns JSON of query and errors contain in JQL query.
     *
     * @param jql       query which needs to be validated and fetch metadata.
     * @param accountId denotes user information.
     * @param invokerId denotes invoker information.
     * @return String of JSON which contain metaData of JQL.
     */

    @Override
    public String parseAndValidateJQL(String jql, Boolean invokeAsUser, String accountId, String invokerId) {
        WebTarget resourceTarget = getWebTarget(invokeAsUser).path("jql/parse");
        Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON);
        JsonObject data = new JsonObject();
        JsonArray queries = new JsonArray();
        queries.add(jql);
        data.add("queries", queries);
        try (Response response = invoker.post(Entity.entity(data.toString(), MediaType.APPLICATION_JSON))) {
            String jsonResponse = response.readEntity(String.class);
            if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
                return jsonResponse;
            } else if (response.getStatus() == 404) {
                throw new IllegalStateException("Please Provide Valid Host URL");
            } else if (response.getStatus() == 400) {
                throw new IllegalStateException("Please Provide Valid Auth Token OR User Name");
            }
            return "";
        }
    }

    @Override
    public <T> T fetchIssues(Boolean invokeAsUser, String accountId, String invokerId, String resource,
                             Map<String, Object> queryParams, Class<T> type) {
        WebTarget resourceTarget = getWebTarget(invokeAsUser).path(resource);
        return fetchIssue(queryParams, type, resourceTarget);
    }

    @Override
    public <T> T fetchIssues(String accountId, String invokerId, String resource, Map<String, Object> queryParams,
                             Class<T> type) {
        WebTarget resourceTarget = getWebTarget(null).path(resource);
        return fetchIssue(queryParams, type, resourceTarget);
    }

    private <T> T fetchIssue(Map<String, Object> queryParams, Class<T> type, WebTarget resourceTarget) {
        for (Map.Entry<String, Object> entrySet : queryParams.entrySet()) {
            resourceTarget = resourceTarget.queryParam(entrySet.getKey(), entrySet.getValue());
        }
        Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON);
        Response response = invoker.get();
        T entityType = response.readEntity(type);
        if (response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
            return entityType;
        }
        throw new IllegalStateException((String) entityType);
    }

    @Override
    public HttpResponse<JsonNode> attachFile(String resource, List<File> files) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("resource not found");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("file not found");
        }
        String callUrl = attributes.get(JiraAttributes.HOST_URL) + "/issue/" + resource + "/attachments";
        HttpResponse<JsonNode> response;
        try {
            response = Unirest.post(callUrl)
                    .basicAuth((String) attributes.get(JiraAttributes.USER_NAME),
                            (String) attributes.get(JiraAttributes.AUTH_TOKEN))
                    .header(JiraConstants.ACCEPT, MediaType.APPLICATION_JSON)
                    .header(JiraConstants.X_ATLASSIAN_TOKEN, JiraConstants.NO_CHECK)
                    .field("file", files)
                    .asJson();
        } catch (UnirestException cause) {
            throw new IllegalStateException(cause);
        }
        return response;
    }

    @Override
    public String getJiraAccessToken() {
        return null;
    }

}
