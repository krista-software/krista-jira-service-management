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
import app.krista.extensions.devsecops.development.jira.TestDataFactory;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketProject;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Version;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for ProjectDao.
 * Tests all project-related operations including versions and validation.
 */
@DisplayName("ProjectDao Integration Tests")
class ProjectDaoIntegrationTest extends BaseIntegrationTest {

    @Mock
    private RestClient mockRestClient;

    private ProjectDao projectDao;

    @BeforeEach
    void setUp() {
        mockRestClient = mock(RestClient.class);
        when(restClientFactory.getInstance()).thenReturn(mockRestClient);
        when(restClientFactory.invokeAsUser()).thenReturn(false);
        projectDao = new ProjectDao(restClientFactory, mockRestClient);
    }

    @Test
    @DisplayName("Should get project by ID successfully")
    void testGetProjectById() {
        JsonObject projectJson = TestDataFactory.createProject("TEST", "Test Project");
        when(mockRestClient.fetch(eq("project/TEST"), any(Map.class), eq(String.class)))
                .thenReturn(projectJson.toString());

        TicketProject project = projectDao.getProjectByIdOrKey("TEST");

        assertNotNull(project);
        assertEquals("TEST", project.key);
        assertEquals("Test Project", project.name);
        verify(mockRestClient).fetch(eq("project/TEST"), any(Map.class), eq(String.class));
    }

    @Test
    @DisplayName("Should get project by key successfully")
    void testGetProjectByKey() {
        JsonObject projectJson = TestDataFactory.createProject("PROJ", "My Project");
        when(mockRestClient.fetch(eq("project/PROJ"), any(Map.class), eq(String.class)))
                .thenReturn(projectJson.toString());

        TicketProject project = projectDao.getProjectByIdOrKey("PROJ");

        assertNotNull(project);
        assertEquals("PROJ", project.key);
        assertEquals("My Project", project.name);
    }

    @Test
    @DisplayName("Should get all projects successfully")
    void testGetAllProjects() {
        JsonArray projectsArray = new JsonArray();
        projectsArray.add(TestDataFactory.createProject("TEST1", "Test Project 1"));
        projectsArray.add(TestDataFactory.createProject("TEST2", "Test Project 2"));
        projectsArray.add(TestDataFactory.createProject("TEST3", "Test Project 3"));

        when(mockRestClient.fetch(eq("project"), any(Map.class), eq(String.class)))
                .thenReturn(projectsArray.toString());

        List<TicketProject> projects = projectDao.getAllProjects();

        assertNotNull(projects);
        assertEquals(3, projects.size());
        assertEquals("TEST1", projects.get(0).key);
        assertEquals("TEST2", projects.get(1).key);
        assertEquals("TEST3", projects.get(2).key);
        verify(mockRestClient).fetch(eq("project"), argThat(map -> 
            map.get("maxResults").equals(150)), eq(String.class));
    }

    @Test
    @DisplayName("Should get issue types from project")
    void testGetIssueTypeFromProject() {
        JsonObject projectJson = TestDataFactory.createProject("TEST", "Test Project");
        JsonArray issueTypes = new JsonArray();
        JsonObject bugType = new JsonObject();
        bugType.addProperty("id", "10001");
        bugType.addProperty("name", "Bug");
        issueTypes.add(bugType);
        projectJson.add("issueTypes", issueTypes);

        when(mockRestClient.fetch(eq("project/TEST"), any(Map.class), eq(String.class)))
                .thenReturn(projectJson.toString());

        String issueTypesStr = projectDao.getIssueTypeFromProject("TEST");

        assertNotNull(issueTypesStr);
        assertTrue(issueTypesStr.contains("Bug"));
    }

    @Test
    @DisplayName("Should get versions for project with pagination")
    void testGetVersionsWithPagination() {
        JsonArray versionsArray = new JsonArray();
        versionsArray.add(TestDataFactory.createVersion("10001", "1.0.0", false));
        versionsArray.add(TestDataFactory.createVersion("10002", "1.1.0", true));

        when(mockRestClient.fetch(eq("project/TEST/versions"), any(Map.class), eq(String.class)))
                .thenReturn(versionsArray.toString());

        List<Version> versions = projectDao.getVersions("TEST", null, 10, 1);

        assertNotNull(versions);
        assertEquals(2, versions.size());
        assertEquals("1.0.0", versions.get(0).name);
        assertEquals("1.1.0", versions.get(1).name);
        assertFalse(versions.get(0).released);
        assertTrue(versions.get(1).released);
        
        verify(mockRestClient).fetch(eq("project/TEST/versions"), argThat(map -> 
            map.get("maxResults").equals(10L) && map.get("startAt").equals(0L)), eq(String.class));
    }

    @Test
    @DisplayName("Should get versions with query filter")
    void testGetVersionsWithQuery() {
        JsonArray versionsArray = new JsonArray();
        versionsArray.add(TestDataFactory.createVersion("10001", "1.0.0", false));

        when(mockRestClient.fetch(eq("project/TEST/versions"), any(Map.class), eq(String.class)))
                .thenReturn(versionsArray.toString());

        List<Version> versions = projectDao.getVersions("TEST", "1.0", 10, 1);

        assertNotNull(versions);
        assertEquals(1, versions.size());
        verify(mockRestClient).fetch(eq("project/TEST/versions"), argThat(map -> 
            map.get("query").equals("1.0")), eq(String.class));
    }

    @Test
    @DisplayName("Should get version by ID")
    void testGetVersionFromId() {
        JsonObject versionJson = TestDataFactory.createVersion("10001", "2.0.0", true);
        versionJson.addProperty("projectId", 10000);
        versionJson.addProperty("description", "Major release");

        when(mockRestClient.fetch(eq("version/10001"), any(Map.class), eq(String.class)))
                .thenReturn(versionJson.toString());

        Version version = projectDao.getVersionFromId("10001");

        assertNotNull(version);
        assertEquals("10001", version.id);
        assertEquals("2.0.0", version.name);
        assertTrue(version.released);
        assertEquals(10000.0, version.projectId);
        assertEquals("Major release", version.description);
    }

    @Test
    @DisplayName("Should check if project exists - returns true")
    void testIsProjectExist_True() {
        JsonObject projectJson = TestDataFactory.createProject("TEST", "Test Project");
        when(mockRestClient.fetch(eq("project/TEST"), any(Map.class), eq(String.class)))
                .thenReturn(projectJson.toString());

        Boolean exists = projectDao.isProjectExist("TEST");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should check if project exists - returns false")
    void testIsProjectExist_False() {
        when(mockRestClient.fetch(eq("project/NOTFOUND"), any(Map.class), eq(String.class)))
                .thenThrow(new NoSuchElementException("Project not found"));

        Boolean exists = projectDao.isProjectExist("NOTFOUND");

        assertFalse(exists);
    }

    @Test
    @DisplayName("Should check if project exists - handles IllegalStateException")
    void testIsProjectExist_IllegalStateException() {
        when(mockRestClient.fetch(eq("project/ERROR"), any(Map.class), eq(String.class)))
                .thenThrow(new IllegalStateException("Invalid state"));

        Boolean exists = projectDao.isProjectExist("ERROR");

        assertFalse(exists);
    }

    @Test
    @DisplayName("Should get versions for all projects")
    void testGetVersionsForAllProjects() {
        // Setup projects
        JsonArray projectsArray = new JsonArray();
        projectsArray.add(TestDataFactory.createProject("PROJ1", "Project 1"));
        projectsArray.add(TestDataFactory.createProject("PROJ2", "Project 2"));

        when(mockRestClient.fetch(eq("project"), any(Map.class), eq(String.class)))
                .thenReturn(projectsArray.toString());

        // Setup versions for each project
        JsonArray versions1 = new JsonArray();
        versions1.add(TestDataFactory.createVersion("1", "1.0", false));

        JsonArray versions2 = new JsonArray();
        versions2.add(TestDataFactory.createVersion("2", "2.0", true));

        when(mockRestClient.fetch(eq("project/PROJ1/versions"), any(Map.class), eq(String.class)))
                .thenReturn(versions1.toString());
        when(mockRestClient.fetch(eq("project/PROJ2/versions"), any(Map.class), eq(String.class)))
                .thenReturn(versions2.toString());

        List<Version> allVersions = projectDao.getVersionsForAllProjects(10, 1L);

        assertNotNull(allVersions);
        assertEquals(2, allVersions.size());
    }

    @Test
    @DisplayName("Should get project by ID response as string")
    void testGetProjectByIdResponse() {
        JsonObject projectJson = TestDataFactory.createProject("TEST", "Test Project");
        when(mockRestClient.fetch(eq("project/TEST"), any(Map.class), eq(String.class)))
                .thenReturn(projectJson.toString());

        String response = projectDao.getProjectByIdResponse("TEST");

        assertNotNull(response);
        assertTrue(response.contains("TEST"));
        assertTrue(response.contains("Test Project"));
    }

    @Test
    @DisplayName("Should handle empty issue types")
    void testGetIssueTypeFromProject_Empty() {
        JsonObject projectJson = TestDataFactory.createProject("TEST", "Test Project");
        // No issueTypes field

        when(mockRestClient.fetch(eq("project/TEST"), any(Map.class), eq(String.class)))
                .thenReturn(projectJson.toString());

        String issueTypesStr = projectDao.getIssueTypeFromProject("TEST");

        assertEquals("", issueTypesStr);
    }

    @Test
    @DisplayName("Should handle empty versions list")
    void testGetVersions_EmptyList() {
        JsonArray versionsArray = new JsonArray();

        when(mockRestClient.fetch(eq("project/TEST/versions"), any(Map.class), eq(String.class)))
                .thenReturn(versionsArray.toString());

        List<Version> versions = projectDao.getVersions("TEST", null, 10, 1);

        assertNotNull(versions);
        assertTrue(versions.isEmpty());
    }

    @Test
    @DisplayName("Should handle pagination with page 2")
    void testGetVersions_Page2() {
        JsonArray versionsArray = new JsonArray();
        versionsArray.add(TestDataFactory.createVersion("10003", "1.2.0", false));

        when(mockRestClient.fetch(eq("project/TEST/versions"), any(Map.class), eq(String.class)))
                .thenReturn(versionsArray.toString());

        List<Version> versions = projectDao.getVersions("TEST", null, 10, 2);

        assertNotNull(versions);
        assertEquals(1, versions.size());
        verify(mockRestClient).fetch(eq("project/TEST/versions"), argThat(map ->
            map.get("startAt").equals(10L)), eq(String.class));
    }

    @Test
    @DisplayName("Should handle empty query string")
    void testGetVersions_EmptyQuery() {
        JsonArray versionsArray = new JsonArray();
        versionsArray.add(TestDataFactory.createVersion("10001", "1.0.0", false));

        when(mockRestClient.fetch(eq("project/TEST/versions"), any(Map.class), eq(String.class)))
                .thenReturn(versionsArray.toString());

        List<Version> versions = projectDao.getVersions("TEST", "", 10, 1);

        assertNotNull(versions);
        verify(mockRestClient).fetch(eq("project/TEST/versions"), argThat(map ->
            !map.containsKey("query")), eq(String.class));
    }
}