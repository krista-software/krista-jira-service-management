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

package app.krista.extensions.devsecops.development.jira.services;

import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JiraIssue Integration Tests - Part 3")
class JiraIssueIntegrationTest3 {

    // ==================== Field Getter Tests ====================

    @Test
    @DisplayName("Should get assignee successfully")
    void testGetAssignee_Success() {
        JsonObject fields = new JsonObject();
        JsonObject assignee = new JsonObject();
        assignee.addProperty("displayName", "John Doe");
        fields.add("assignee", assignee);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("John Doe", issue.assigneeName);
    }

    @Test
    @DisplayName("Should return NA when assignee is missing")
    void testGetAssignee_Missing() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("NA", issue.assigneeName);
    }

    @Test
    @DisplayName("Should get reporter successfully")
    void testGetReporter_Success() {
        JsonObject fields = new JsonObject();
        JsonObject reporter = new JsonObject();
        reporter.addProperty("displayName", "Jane Smith");
        fields.add("reporter", reporter);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("Jane Smith", issue.reporterName);
    }

    @Test
    @DisplayName("Should return empty string when reporter is missing")
    void testGetReporter_Missing() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("", issue.reporterName);
    }

    @Test
    @DisplayName("Should get priority successfully")
    void testGetPriority_Success() {
        JsonObject fields = new JsonObject();
        JsonObject priority = new JsonObject();
        priority.addProperty("name", "High");
        fields.add("priority", priority);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("High", issue.priority);
    }

    @Test
    @DisplayName("Should return empty string when priority is missing")
    void testGetPriority_Missing() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("", issue.priority);
    }

    @Test
    @DisplayName("Should get project key successfully")
    void testGetProjectKey_Success() {
        JsonObject fields = new JsonObject();
        JsonObject project = new JsonObject();
        project.addProperty("key", "PROJ");
        fields.add("project", project);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("PROJ", issue.projectKey);
    }

    @Test
    @DisplayName("Should return empty string when project key is missing")
    void testGetProjectKey_Missing() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("", issue.projectKey);
    }

    @Test
    @DisplayName("Should get status successfully and convert to uppercase")
    void testGetStatus_Success() {
        JsonObject fields = new JsonObject();
        JsonObject status = new JsonObject();
        status.addProperty("name", "In Progress");
        fields.add("status", status);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("IN PROGRESS", issue.status);
    }

    @Test
    @DisplayName("Should return empty string when status is missing")
    void testGetStatus_Missing() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("", issue.status);
    }

    @Test
    @DisplayName("Should handle null status name")
    void testGetStatus_NullName() {
        JsonObject fields = new JsonObject();
        JsonObject status = new JsonObject();
        fields.add("status", status);
        
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();
        
        assertEquals("", issue.status);
    }

    @Test
    @DisplayName("Should get issue type successfully")
    void testGetType_Success() {
        JsonObject fields = new JsonObject();
        JsonObject issueType = new JsonObject();
        issueType.addProperty("name", "Bug");
        fields.add("issuetype", issueType);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("Bug", issue.issueType);
    }

    @Test
    @DisplayName("Should return empty string when issue type is missing")
    void testGetType_Missing() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        Issue issue = jiraIssue.getIssue();

        assertEquals("", issue.issueType);
    }

    @Test
    @DisplayName("Should get parent key successfully")
    void testGetParent_Success() {
        JsonObject fields = new JsonObject();
        JsonObject parent = new JsonObject();
        parent.addProperty("key", "PARENT-1");
        fields.add("parent", parent);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("PARENT-1", issue.parentKey);
    }

    @Test
    @DisplayName("Should return NA when parent is missing")
    void testGetParent_Missing() {
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);

        Issue issue = jiraIssue.getIssue();

        assertEquals(JiraConstants.NA, issue.parentKey);
    }

    @Test
    @DisplayName("Should return NA when parent is null")
    void testGetParent_Null() {
        JsonObject fields = new JsonObject();
        fields.add("parent", null);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals(JiraConstants.NA, issue.parentKey);
    }

    // ==================== Request Type Tests ====================

    @Test
    @DisplayName("Should get request type from requesttype field with name")
    void testGetRequestType_StandardFieldWithName() {
        JsonObject fields = new JsonObject();
        JsonObject requestType = new JsonObject();
        requestType.addProperty("name", "Service Request");
        fields.add("requesttype", requestType);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("Service Request", issue.requestType);
    }

    @Test
    @DisplayName("Should get request type from requesttype field as primitive")
    void testGetRequestType_StandardFieldPrimitive() {
        JsonObject fields = new JsonObject();
        fields.addProperty("requesttype", "Simple Request");

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("Simple Request", issue.requestType);
    }

    @Test
    @DisplayName("Should get request type from custom field with requestType object")
    void testGetRequestType_CustomFieldWithRequestType() {
        JsonObject fields = new JsonObject();
        JsonObject customField = new JsonObject();
        JsonObject requestType = new JsonObject();
        requestType.addProperty("name", "Custom Request");
        customField.add("requestType", requestType);
        fields.add("customfield_10010", customField);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("Custom Request", issue.requestType);
    }

    @Test
    @DisplayName("Should get request type from custom field with portal/key format")
    void testGetRequestType_PortalKeyFormat() {
        JsonObject fields = new JsonObject();
        fields.addProperty("customfield_10325", "1/2");

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("1/2", issue.requestType);
    }

    @Test
    @DisplayName("Should get request type from custom field with value")
    void testGetRequestType_CustomFieldWithValue() {
        JsonObject fields = new JsonObject();
        JsonObject customField = new JsonObject();
        customField.addProperty("value", "Request Value");
        fields.add("customfield_10020", customField);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("Request Value", issue.requestType);
    }

    @Test
    @DisplayName("Should skip SLA fields when looking for request type")
    void testGetRequestType_SkipSLAFields() {
        JsonObject fields = new JsonObject();

        // Add SLA field (should be skipped)
        JsonObject slaField = new JsonObject();
        slaField.addProperty("ongoingCycle", "value");
        slaField.addProperty("name", "SLA Name");
        fields.add("customfield_10001", slaField);

        // Add valid request type field
        JsonObject requestTypeField = new JsonObject();
        requestTypeField.addProperty("value", "Valid Request");
        fields.add("customfield_10002", requestTypeField);

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertEquals("Valid Request", issue.requestType);
    }

    @Test
    @DisplayName("Should return null when request type is not found")
    void testGetRequestType_NotFound() {
        JsonObject fields = new JsonObject();

        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", fields, null);
        Issue issue = jiraIssue.getIssue();

        assertNull(issue.requestType);
    }

    @Test
    @DisplayName("Should return null when exception occurs in getRequestType")
    void testGetRequestType_Exception() {
        JiraIssue jiraIssue = new JiraIssue("1", "TEST-1", null, null);
        Issue issue = jiraIssue.getIssue();

        assertNull(issue.requestType);
    }
}


