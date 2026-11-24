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

import app.krista.extensions.devsecops.development.jira.BaseIntegrationTest;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.catalog.entities.IssueType;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketPriority;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketStatus;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TransitionStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests for TicketInfoDao.
 * Tests all ticket information operations including priorities, statuses, transitions, and issue types.
 */
@DisplayName("TicketInfoDao Integration Tests")
class TicketInfoDaoIntegrationTest extends BaseIntegrationTest {

    @Mock
    private RestClient mockRestClient;

    private TicketInfoDao ticketInfoDao;

    @BeforeEach
    void setUp() {
        mockRestClient = mock(RestClient.class);
        when(restClientFactory.getInstance()).thenReturn(mockRestClient);
        when(restClientFactory.invokeAsUser()).thenReturn(false);
        ticketInfoDao = new TicketInfoDao(restClientFactory, mockRestClient);
    }

    @Test
    @DisplayName("Should get all priorities")
    void testGetPriorities() {
        JsonArray prioritiesArray = new JsonArray();

        JsonObject highPriority = new JsonObject();
        highPriority.addProperty("id", "1");
        highPriority.addProperty("name", "High");
        highPriority.addProperty("description", "High priority");
        prioritiesArray.add(highPriority);

        JsonObject lowPriority = new JsonObject();
        lowPriority.addProperty("id", "2");
        lowPriority.addProperty("name", "Low");
        lowPriority.addProperty("description", "Low priority");
        prioritiesArray.add(lowPriority);

        when(mockRestClient.fetch(eq("/priority"), any(HashMap.class), eq(String.class)))
                .thenReturn(prioritiesArray.toString());

        List<TicketPriority> priorities = ticketInfoDao.getPriorities();

        assertNotNull(priorities);
        assertEquals(2, priorities.size());
        assertEquals("1", priorities.get(0).id);
        assertEquals("High", priorities.get(0).name);
        assertEquals("High priority", priorities.get(0).description);
        assertEquals("2", priorities.get(1).id);
        assertEquals("Low", priorities.get(1).name);
    }

    @Test
    @DisplayName("Should get all statuses")
    void testGetStatuses() {
        JsonArray statusesArray = new JsonArray();

        JsonObject openStatus = new JsonObject();
        openStatus.addProperty("id", "1");
        openStatus.addProperty("name", "Open");
        openStatus.addProperty("description", "Issue is open");
        statusesArray.add(openStatus);

        JsonObject closedStatus = new JsonObject();
        closedStatus.addProperty("id", "2");
        closedStatus.addProperty("name", "Closed");
        closedStatus.addProperty("description", "Issue is closed");
        statusesArray.add(closedStatus);

        when(mockRestClient.fetch(eq("/status"), any(HashMap.class), eq(String.class)))
                .thenReturn(statusesArray.toString());

        List<TicketStatus> statuses = ticketInfoDao.getStatuses();

        assertNotNull(statuses);
        assertEquals(2, statuses.size());
        assertEquals("1", statuses.get(0).id);
        assertEquals("Open", statuses.get(0).name);
        assertEquals("Issue is open", statuses.get(0).description);
        assertEquals("2", statuses.get(1).id);
        assertEquals("Closed", statuses.get(1).name);
    }

    @Test
    @DisplayName("Should get transitions for issue")
    void testGetTransitions() {
        JsonObject response = new JsonObject();
        JsonArray transitionsArray = new JsonArray();

        JsonObject transition = new JsonObject();
        transition.addProperty("id", "11");
        transition.addProperty("name", "Start Progress");

        JsonObject toStatus = new JsonObject();
        toStatus.addProperty("id", "3");
        toStatus.addProperty("name", "In Progress");

        JsonObject statusCategory = new JsonObject();
        statusCategory.addProperty("name", "In Progress");
        toStatus.add("statusCategory", statusCategory);

        transition.add("to", toStatus);
        transitionsArray.add(transition);
        response.add("transitions", transitionsArray);

        when(mockRestClient.fetch(eq("issue/TEST-123/transitions"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<TransitionStatus> transitions = ticketInfoDao.getTransitions("TEST-123");

        assertNotNull(transitions);
        assertEquals(1, transitions.size());
        assertEquals("11", transitions.get(0).fromId);
        assertEquals("Start Progress", transitions.get(0).fromName);
        assertEquals("3", transitions.get(0).toId);
        assertEquals("In Progress", transitions.get(0).toName);
        assertEquals("In Progress", transitions.get(0).toCategoryName);
    }

    @Test
    @DisplayName("Should handle transitions with error messages")
    void testGetTransitions_WithErrorMessages() {
        JsonObject response = new JsonObject();
        JsonArray errorMessages = new JsonArray();
        errorMessages.add("Issue not found");
        response.add("errorMessages", errorMessages);

        when(mockRestClient.fetch(eq("issue/INVALID-123/transitions"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<TransitionStatus> transitions = ticketInfoDao.getTransitions("INVALID-123");

        assertNotNull(transitions);
        assertTrue(transitions.isEmpty());
    }

    @Test
    @DisplayName("Should handle transitions without status category")
    void testGetTransitions_WithoutStatusCategory() {
        JsonObject response = new JsonObject();
        JsonArray transitionsArray = new JsonArray();

        JsonObject transition = new JsonObject();
        transition.addProperty("id", "21");
        transition.addProperty("name", "Close Issue");

        JsonObject toStatus = new JsonObject();
        toStatus.addProperty("id", "6");
        toStatus.addProperty("name", "Closed");
        // No statusCategory

        transition.add("to", toStatus);
        transitionsArray.add(transition);
        response.add("transitions", transitionsArray);

        when(mockRestClient.fetch(eq("issue/TEST-456/transitions"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<TransitionStatus> transitions = ticketInfoDao.getTransitions("TEST-456");

        assertNotNull(transitions);
        assertEquals(1, transitions.size());
        assertNull(transitions.get(0).toCategoryName);
    }

    @Test
    @DisplayName("Should get all issue types")
    void testGetIssueTypes() {
        JsonArray issueTypesArray = new JsonArray();

        JsonObject bugType = new JsonObject();
        bugType.addProperty("id", "1");
        bugType.addProperty("name", "Bug");
        bugType.addProperty("description", "A problem");
        bugType.addProperty("subtask", "false");
        issueTypesArray.add(bugType);

        JsonObject taskType = new JsonObject();
        taskType.addProperty("id", "2");
        taskType.addProperty("name", "Task");
        taskType.addProperty("description", "A task");
        taskType.addProperty("subtask", "false");
        issueTypesArray.add(taskType);

        JsonObject subTaskType = new JsonObject();
        subTaskType.addProperty("id", "3");
        subTaskType.addProperty("name", "Sub-task");
        subTaskType.addProperty("description", "A sub-task");
        subTaskType.addProperty("subtask", "true");
        issueTypesArray.add(subTaskType);

        when(mockRestClient.fetch(eq("issuetype"), any(HashMap.class), eq(String.class)))
                .thenReturn(issueTypesArray.toString());

        List<IssueType> issueTypes = ticketInfoDao.getIssueTypes();

        assertNotNull(issueTypes);
        assertEquals(3, issueTypes.size());
        assertEquals("1", issueTypes.get(0).id);
        assertEquals("Bug", issueTypes.get(0).name);
        assertEquals("A problem", issueTypes.get(0).description);
        assertEquals("false", issueTypes.get(0).subTask);
        assertEquals("3", issueTypes.get(2).id);
        assertEquals("true", issueTypes.get(2).subTask);
    }

    @Test
    @DisplayName("Should handle empty priorities list")
    void testGetPriorities_Empty() {
        JsonArray prioritiesArray = new JsonArray();

        when(mockRestClient.fetch(eq("/priority"), any(HashMap.class), eq(String.class)))
                .thenReturn(prioritiesArray.toString());

        List<TicketPriority> priorities = ticketInfoDao.getPriorities();

        assertNotNull(priorities);
        assertTrue(priorities.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty statuses list")
    void testGetStatuses_Empty() {
        JsonArray statusesArray = new JsonArray();

        when(mockRestClient.fetch(eq("/status"), any(HashMap.class), eq(String.class)))
                .thenReturn(statusesArray.toString());

        List<TicketStatus> statuses = ticketInfoDao.getStatuses();

        assertNotNull(statuses);
        assertTrue(statuses.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty issue types list")
    void testGetIssueTypes_Empty() {
        JsonArray issueTypesArray = new JsonArray();

        when(mockRestClient.fetch(eq("issuetype"), any(HashMap.class), eq(String.class)))
                .thenReturn(issueTypesArray.toString());

        List<IssueType> issueTypes = ticketInfoDao.getIssueTypes();

        assertNotNull(issueTypes);
        assertTrue(issueTypes.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty transitions list")
    void testGetTransitions_Empty() {
        JsonObject response = new JsonObject();
        JsonArray transitionsArray = new JsonArray();
        response.add("transitions", transitionsArray);

        when(mockRestClient.fetch(eq("issue/TEST-789/transitions"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<TransitionStatus> transitions = ticketInfoDao.getTransitions("TEST-789");

        assertNotNull(transitions);
        assertTrue(transitions.isEmpty());
    }
}