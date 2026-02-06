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
import app.krista.extensions.devsecops.development.jira.catalog.entities.IssueType;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketPriority;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketStatus;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TransitionStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

@Service
public class TicketInfoDao {

    private final RestClientFactory restClientFactory;
    private RestClient restClient;

    @Inject
    public TicketInfoDao(RestClientFactory restClientFactory) {
        this.restClientFactory = restClientFactory;
    }

    public TicketInfoDao(RestClientFactory restClientFactory, RestClient restClient) {
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

    public List<TicketPriority> getPriorities() {
        String res = getRestClient().fetch("/priority", new HashMap<>(), String.class);
        JsonArray statusArr = GSON.fromJson(res, JsonArray.class);
        List<TicketPriority> priorities = new ArrayList<>();
        for (int i = 0; i < statusArr.size(); i++) {
            TicketPriority status = new TicketPriority();
            status.id = statusArr.get(i).getAsJsonObject().get("id").getAsString();
            status.description = statusArr.get(i).getAsJsonObject().get("description").getAsString();
            status.name = statusArr.get(i).getAsJsonObject().get("name").getAsString();
            priorities.add(status);
        }
        return priorities;
    }

    public List<TicketStatus> getStatuses() {
        String res = getRestClient().fetch("/status", new HashMap<>(), String.class);
        JsonArray statusArr = GSON.fromJson(res, JsonArray.class);
        List<TicketStatus> statuses = new ArrayList<>();
        for (int i = 0; i < statusArr.size(); i++) {
            TicketStatus status = new TicketStatus();
            status.id = statusArr.get(i).getAsJsonObject().get("id").getAsString();
            status.description = statusArr.get(i).getAsJsonObject().get("description").getAsString();
            status.name = statusArr.get(i).getAsJsonObject().get("name").getAsString();
            statuses.add(status);
        }
        return statuses;
    }

    public List<TransitionStatus> getTransitions(String issueKey) {
        String res = getRestClient().fetch("issue/" + issueKey + "/transitions", new HashMap<>(), String.class);
        JsonObject resObj = GSON.fromJson(res, JsonObject.class);
        if (resObj.has("errorMessages")) {
            return List.of();
        }
        List<TransitionStatus> transitionStatusList = new ArrayList<>();
        JsonArray transitions = resObj.getAsJsonArray("transitions");
        for (int i = 0; i < transitions.size(); i++) {
            TransitionStatus transitionStatus = new TransitionStatus();
            JsonObject trans = transitions.get(i).getAsJsonObject();
            transitionStatus.fromId = trans.get("id").getAsString();
            transitionStatus.fromName = trans.get("name").getAsString();
            JsonObject to = trans.getAsJsonObject("to");
            transitionStatus.toId = to.get("id").getAsString();
            transitionStatus.toName = to.get("name").getAsString();
            JsonObject category = to.getAsJsonObject("statusCategory");
            if (category != null) {
                transitionStatus.toCategoryName = category.get("name").getAsString();
            }
            transitionStatusList.add(transitionStatus);
        }
        return transitionStatusList;
    }

    public List<IssueType> getIssueTypes() {
        String res = getRestClient().fetch("issuetype", new HashMap<>(), String.class);
        JsonArray statusArr = GSON.fromJson(res, JsonArray.class);
        List<IssueType> issueTypes = new ArrayList<>();
        for (int i = 0; i < statusArr.size(); i++) {
            IssueType issueType = new IssueType();
            issueType.id = statusArr.get(i).getAsJsonObject().get("id").getAsString();
            issueType.description = statusArr.get(i).getAsJsonObject().get("description").getAsString();
            issueType.name = statusArr.get(i).getAsJsonObject().get("name").getAsString();
            issueType.subTask = statusArr.get(i).getAsJsonObject().get("subtask").getAsString();
            issueTypes.add(issueType);
        }
        return issueTypes;
    }
}
