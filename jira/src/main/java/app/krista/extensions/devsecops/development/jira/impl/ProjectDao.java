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
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketProject;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Version;
import app.krista.extensions.devsecops.development.jira.services.JiraProject;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.*;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

@Service
public final class ProjectDao {

    private final RestClientFactory restClientFactory;
    private RestClient restClient;

    @Inject
    public ProjectDao(RestClientFactory restClientFactory) {
        this.restClientFactory = restClientFactory;
    }

    public ProjectDao(RestClientFactory restClientFactory, RestClient restClient) {
        this.restClientFactory = restClientFactory;
        this.restClient = restClient;
    }

    private RestClient getRestClient() {
        if (restClientFactory == null) {
            return restClient;
        }

        boolean invokeAsUserAuth = restClientFactory.invokeAsUser();
        if (restClient == null || (restClient instanceof OAuth2RestClient && !invokeAsUserAuth) || (restClient instanceof AuthRestClient && invokeAsUserAuth)) {
            restClient = restClientFactory.getInstance();
        }
        return restClient;
    }

    public String getIssueTypeFromProject(String projectIdOrKey) {
        JsonObject jsonObject = GSON.fromJson(getProjectByIdResponse(projectIdOrKey), JsonObject.class);
        return jsonObject.has("issueTypes") ? jsonObject.get("issueTypes").toString() : "";
    }

    public TicketProject getProjectByIdOrKey(String id) {
        String jsonResponse = getProjectByIdResponse(id);
        JiraProject jiraProject = JiraProject.create(GSON.fromJson(jsonResponse, JsonObject.class));
        return jiraProject.getProject();
    }

    public String getProjectByIdResponse(String id) {
        return getRestClient().fetch("project/" + id, new HashMap<>(), String.class);
    }

    public List<TicketProject> getAllProjects() {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("maxResults", 150);
        String jsonResponse = getRestClient().fetch("project", queryParams, String.class);
        List<TicketProject> finalResponse = new ArrayList<>();
        JsonArray projectArray = GSON.fromJson(jsonResponse, JsonArray.class);
        for (int i = 0; i < projectArray.size(); i++) {
            JiraProject jiraProject = JiraProject.create(projectArray.get(i).getAsJsonObject());
            finalResponse.add(jiraProject.getProject());
        }
        return finalResponse;
    }

    public List<Version> getVersions(String projectKey, String query, long size, long page) {
        long startAt = (page * size) - size;
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("maxResults", size);
        queryParams.put("startAt", startAt);
        if (query != null && !query.isEmpty()) {
            queryParams.put("query", query);
        }
        String res = getRestClient().fetch("project/" + projectKey + "/versions", queryParams, String.class);
        JsonArray jsonArray = GSON.fromJson(res, JsonArray.class);
        List<Version> versions = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            versions.add(parseReleaseVersion(jsonArray.get(i).getAsJsonObject()));
        }
        return versions;
    }

    public Version getVersionFromId(String id) {
        String res = getRestClient().fetch("version/" + id, new HashMap<>(), String.class);
        JsonObject jsonObject = GSON.fromJson(res, JsonObject.class);
        return parseReleaseVersion(jsonObject);
    }

    private Version parseReleaseVersion(JsonObject jsonObject) {
        Version version = new Version();
        version.id = jsonObject.get("id").getAsString();
        version.name = jsonObject.get("name").getAsString();
        version.projectId = jsonObject.get("projectId").getAsDouble();
        version.released = jsonObject.get("released").getAsBoolean();
        version.description = jsonObject.has("description") ? jsonObject.get("description").getAsString() : JiraConstants.NA;
        return version;
    }

    public List<Version> getVersionsForAllProjects(Integer pageSize, Long pageIndex) {
        List<Version> versions = new ArrayList<>();
        for (TicketProject ticketProject : getAllProjects()) {
            versions.addAll(getVersions(ticketProject.key, null, pageSize, pageIndex));
        }
        return versions;
    }

    public Boolean isProjectExist(String key) {
        try {
            getProjectByIdOrKey(key);
            return true;
        } catch (NoSuchElementException | IllegalStateException cause) {
            return false;
        }
    }
}

