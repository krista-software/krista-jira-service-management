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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import app.krista.extensions.devsecops.development.jira.services.JiraUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

@Service
public final class UserDao {

    private final RestClientFactory restClientFactory;
    private RestClient restClient;

    @Inject
    public UserDao(RestClientFactory restClientFactory) {
        this.restClientFactory = restClientFactory;
    }

    public UserDao(RestClientFactory restClientFactory, RestClient restClient) {
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

    public List<JiraUser> searchUser(String query) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("query", query);
        String jsonResponse = getRestClient().fetch("user/search", queryParams, String.class);
        List<JiraUser> users = new ArrayList<>();
        JsonArray userArray = GSON.fromJson(jsonResponse, JsonArray.class);
        for (int i = 0; i < userArray.size(); i++) {
            users.add(JiraUser.create(userArray.get(i).getAsJsonObject()));
        }
        return users;
    }

    public List<JiraUser> searchUser(String query, long page, long size) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("query", query);
        queryParams.put("startAt", Math.max((page - 1) * size, 0));
        queryParams.put("maxResults", size == 0 ? 50 : size);
        String jsonResponse = getRestClient().fetch("user/search", queryParams, String.class);
        List<JiraUser> totalUsers = new ArrayList<>();
        JsonArray userArray = GSON.fromJson(jsonResponse, JsonArray.class);
        if (userArray != null) {
            if (userArray.size() < 50) {
                totalUsers.addAll(getUsersAdd(userArray));
            } else {
                totalUsers.addAll(getUsersAdd(userArray));
                totalUsers.addAll(searchUser(query, page + 1, size));
            }
        } else {
            throw new RuntimeException();
        }
        return totalUsers;
    }

    public List<JiraUser> getUsersAdd(JsonArray userArray) {
        List<JiraUser> users = new ArrayList<>();
        for (int i = 0; i < userArray.size(); i++) {
            users.add(JiraUser.create(userArray.get(i).getAsJsonObject()));
        }
        return users;
    }

    public List<User> getUsersByQuery(String query) {
        return searchUser(query, 1, 50).stream().map(JiraUser::getUser).collect(Collectors.toList());
    }

    public User getUserByAccountId(String accountId) {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("accountId", accountId);
        String jsonResponse = getRestClient().fetch("user", queryParams, String.class);
        return JiraUser.create(GSON.fromJson(jsonResponse, JsonObject.class)).getUser();
    }

    public Boolean isUserExist(String accountId) {
        try {
            getUserByAccountId(accountId);
            return true;
        } catch (IllegalCallerException cause) {
            throw new IllegalCallerException(cause.getMessage());
        } catch (MustAuthorizeException cause) {
            throw cause;
        } catch (Exception cause) {
            return false;
        }
    }

}

