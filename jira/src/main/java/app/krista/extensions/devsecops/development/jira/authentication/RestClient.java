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

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;

import javax.ws.rs.client.Invocation;
import java.io.File;
import java.util.List;
import java.util.Map;

public interface RestClient {

    Invocation.Builder getResourceInvoker(String resource, Map<String, Object> queryParams);

    Invocation.Builder getJiraServiceResourceInvoker(String resource, Map<String, Object> queryParams);

    <T> T fetch(String resource, Map<String, Object> queryParams, Class<T> type);

    <T> T fetchMiscellaneousService(String resource, Map<String, Object> queryParams, Class<T> type);

    <T> T fetchJiraService(String resource, Map<String, Object> queryParams, Class<T> type);

    String parseAndValidateJQL(String jql, Boolean invokeAsUser, String accountId, String invokerId);

    <T> T fetchIssues(Boolean invokeAsUser, String accountId, String invokerId, String resource, Map<String, Object> queryParams, Class<T> type);

    <T> T fetchIssues(String accountId, String invokerId, String resource, Map<String, Object> queryParams, Class<T> type);

    HttpResponse<JsonNode> attachFile(String resource, List<File> files);

    String getJiraAccessToken();

}
