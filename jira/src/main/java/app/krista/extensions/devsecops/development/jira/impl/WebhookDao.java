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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.*;

public class WebhookDao {

    /**
     * Parses the webhook response and creates a map with details.
     *
     * @param jsonString The JSON string of the webhook response.
     * @return A map containing the parsed response.
     */
    public Map<String, Object> parseWebhookResponse(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            String issueKey = extractIssueKey(jsonString);
            List<Map<String, Object>> parsedResponse = parseJsonToMapList(jsonString);
            sortByLargestMap(parsedResponse);
            return createResponseDetails(parsedResponse, issueKey);
        } catch (IOException cause) {
            System.err.println("Error parsing JSON response: " + cause.getMessage());
            return Collections.emptyMap();
        }
    }

    private String extractIssueKey(String jsonString) {
        JsonObject jsonObject = com.google.gson.JsonParser.parseString(jsonString).getAsJsonObject();
        return jsonObject.has(ISSUE_) ? jsonObject.getAsJsonObject(ISSUE_).get("key").getAsString() : "";
    }

    private Map<String, Object> createResponseDetails(List<Map<String, Object>> parsedResponse, String issueKey) {
        Map<String, Object> responseDetails = new HashMap<>();
        responseDetails.put(UPDATE, parsedResponse);
        responseDetails.put(ISSUE_KEY, issueKey);
        return responseDetails;
    }

    private void sortByLargestMap(List<Map<String, Object>> parsedResponse) {
        if (!parsedResponse.isEmpty()) {
            parsedResponse.sort((map1, map2) -> Integer.compare(map2.size(), map1.size()));
        }
    }

    private List<Map<String, Object>> parseJsonToMapList(String jsonResponse) throws IOException {
        JsonFactory factory = new JsonFactory();
        List<Map<String, Object>> resultList = new ArrayList<>();
        try (JsonParser parser = factory.createParser(new StringReader(jsonResponse))) {
            ObjectMapper mapper = new ObjectMapper();
            JsonToken token = parser.nextToken();
            if (token == JsonToken.START_OBJECT) {
                resultList.add(parseSingleObject(parser, mapper));
            } else if (token == JsonToken.START_ARRAY) {
                parseArray(parser, mapper, resultList);
            }
        }
        return resultList;
    }

    private Map<String, Object> parseSingleObject(JsonParser parser, ObjectMapper mapper) throws IOException {
        return mapper.readValue(parser, new TypeReference<>() {
        });
    }

    private void parseArray(JsonParser parser, ObjectMapper mapper, List<Map<String, Object>> resultList) throws IOException {
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            resultList.add(mapper.readValue(parser, new TypeReference<Map<String, Object>>() {
            }));
        }
    }
}