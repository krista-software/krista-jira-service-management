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

import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.sdk.atlassian.client.AbstractOAuth2RestClient;
import app.krista.sdk.atlassian.config.AtlassianProduct;
import app.krista.sdk.atlassian.oauth.AccessibleResource;
import app.krista.sdk.atlassian.oauth.AtlassianTokenSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * OAuth 2.0 (3LO) REST client for JSM, talking to the Atlassian API gateway.
 *
 * <p>The shared plumbing (cloud-id resolution and caching, gateway URL
 * construction, bearer decoration, multipart upload) lives in the SDK's
 * {@link AbstractOAuth2RestClient}; this class adds the JSM Service Desk
 * gateway targets and the JQL/issue operations.</p>
 */
@Service
public final class OAuth2RestClient extends AbstractOAuth2RestClient implements RestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth2RestClient.class);

    public static final String ATLASSIAN_URL = "https://api.atlassian.com/ex/jira/";
    public static final String JIRA_ENDPOINT = "/rest/api/3/";

    @Inject
    public OAuth2RestClient(JiraService jiraService) {
        super(jiraService, AtlassianProduct.JIRA_SERVICE_MANAGEMENT);
    }

    /**
     * Server-to-server (2LO) variant: same client, bearer tokens from a
     * {@link app.krista.sdk.atlassian.oauth.ClientCredentialsTokenProvider}
     * instead of the 3LO refresh-token flow.
     */
    public OAuth2RestClient(AtlassianTokenSource tokenSource) {
        super(tokenSource, AtlassianProduct.JIRA_SERVICE_MANAGEMENT);
    }

    /** Target for the Service Desk REST API on the gateway ({@code .../rest/servicedeskapi/}). */
    private WebTarget getJiraServiceManageWebTarget() {
        LOGGER.info("Jira Service Management Web Target Initialised");
        return gatewayTarget(AtlassianProduct.SERVICE_DESK_API_PATH);
    }

    /** Target for the legacy Service Desk management API on the gateway ({@code .../rest/servicedesk/1/servicedesk/}). */
    private WebTarget getJiraServiceAPIWebTarget() {
        LOGGER.info("Jira Service Management Web Target Initialised");
        return gatewayTarget(AtlassianProduct.SERVICE_DESK_MANAGE_PATH);
    }

    /** Legacy alias for {@link #getAccessibleResource()}. */
    public AccessibleResource getAccessibleResources() {
        return getAccessibleResource();
    }

    /** Legacy alias for {@link #getAccessibleResource(String, String)}. */
    public AccessibleResource getAccessibleResources(String accountId, String invokerId) {
        return getAccessibleResource(accountId, invokerId);
    }

    @Override
    public Invocation.Builder getResourceInvoker(String resource, Map<String, Object> queryParams) {
        LOGGER.info("Default Resource URI : {}", resource);
        WebTarget resourceTarget = applyQueryParams(getWebTarget().path(resource), queryParams);
        String token = getOAuthService().getToken();
        LOGGER.info("Getting Invocation Builder from get ResourceInvoker");
        return resourceTarget.request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, JiraConstants.BEARER + token);
    }

    @Override
    public String getJiraAccessToken() {
        return getOAuthService().getToken();
    }

    @Override
    public Invocation.Builder getJiraServiceResourceInvoker(String resource, Map<String, Object> queryParams) {
        LOGGER.info("Resource URI : {}", resource);
        WebTarget resourceTarget = applyQueryParams(getJiraServiceManageWebTarget().path(resource), queryParams);
        String token = getOAuthService().getToken();
        LOGGER.info("Getting Invocation Builder from get JiraServiceResourceInvoker");
        return resourceTarget.request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, JiraConstants.BEARER + token);
    }

    @Override
    public <T> T fetch(String resource, Map<String, Object> queryParams, Class<T> type) {
        LOGGER.info("Default Resource URI : {}", resource);
        WebTarget resourceTarget = applyQueryParams(getWebTarget().path(resource), queryParams);
        String token = getOAuthService().getToken();
        LOGGER.info("Getting Default Fetch on : {}", resourceTarget.getUri());
        try {
            Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, JiraConstants.BEARER + token);
            Response response = invoker.get();
            return response.readEntity(type);
        } catch (RuntimeException cause) {
            LOGGER.error("Failed At fetch : {}", cause.getMessage(), cause);
        }
        return null;
    }

    @Override
    public <T> T fetchMiscellaneousService(String resource, Map<String, Object> queryParams, Class<T> type) {
        LOGGER.info("Resource URI : {}", resource);
        WebTarget resourceTarget = applyQueryParams(getJiraServiceAPIWebTarget().path(resource), queryParams);
        String token = getOAuthService().getToken();
        LOGGER.info("Getting fetchMiscellaneousService after setting queryparam");
        Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, JiraConstants.BEARER + token);
        Response response = invoker.get();
        return response.readEntity(type);
    }

    @Override
    public <T> T fetchJiraService(String resource, Map<String, Object> queryParams, Class<T> type) {
        LOGGER.info("Resource URI : {}", resource);
        WebTarget resourceTarget = applyQueryParams(getJiraServiceManageWebTarget().path(resource), queryParams);
        String token = getOAuthService().getToken();
        LOGGER.info("Getting fetchJiraService after setting queryparam");
        Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, JiraConstants.BEARER + token);
        Response response = invoker.get();
        return response.readEntity(type);
    }

    /**
     * This is post method which Parses and validates JQL queries, if JQL is incorrect then it returns query and errors contain in JQL query.
     *
     * @param jql       query which needs to be validated and fetch metadata.
     * @param accountId denotes user information.
     * @param invokerId denotes invoker information.
     * @return String of JSON which contain metaData of JQL.
     */
    @Override
    public String parseAndValidateJQL(String jql, Boolean invokeAsUser, String accountId, String invokerId) {
        WebTarget resourceTarget = getWebTarget(accountId, invokerId).path("jql/parse");
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
    public <T> T fetchIssues(Boolean invokeAsUser, String accountId, String invokerId, String resource, Map<String, Object> queryParams, Class<T> type) {
        return fetchIssues(accountId, invokerId, resource, queryParams, type);
    }

    @Override
    public <T> T fetchIssues(String accountId, String invokerId, String resource, Map<String, Object> queryParams, Class<T> type) {
        WebTarget resourceTarget = applyQueryParams(getWebTarget(accountId, invokerId).path(resource), queryParams);
        Invocation.Builder invoker = resourceTarget.request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, JiraConstants.BEARER + getOAuthService().getToken(accountId, invokerId));
        Response response = invoker.get();
        return response.readEntity(type);
    }

    @Override
    public HttpResponse<JsonNode> attachFile(String resource, List<File> files) {
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("resource not found");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("file not found");
        }
        // Note: the duplicated slash after the API path is intentional — it preserves the legacy URL exactly.
        String callUrl = ATLASSIAN_URL + getAccessibleResources().getId() + JIRA_ENDPOINT + "/issue/" + resource + "/attachments";
        return uploadMultipart(callUrl, files);
    }
}
