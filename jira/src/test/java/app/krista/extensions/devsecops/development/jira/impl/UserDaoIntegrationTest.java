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
import app.krista.extensions.devsecops.development.jira.BaseIntegrationTest;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import app.krista.extensions.devsecops.development.jira.services.JiraUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for UserDao.
 * Tests all user-related operations including search and validation.
 */
@DisplayName("UserDao Integration Tests")
class UserDaoIntegrationTest extends BaseIntegrationTest {

    @Mock
    private RestClient mockRestClient;

    private UserDao userDao;

    @BeforeEach
    void setUp() {
        mockRestClient = mock(RestClient.class);
        when(restClientFactory.getInstance()).thenReturn(mockRestClient);
        when(restClientFactory.invokeAsUser()).thenReturn(false);
        userDao = new UserDao(restClientFactory, mockRestClient);
    }

    @Test
    @DisplayName("Should search users by query")
    void testSearchUser() {
        JsonArray usersArray = new JsonArray();
        
        JsonObject user1 = createUserJson("123", "john.doe@example.com", "John Doe");
        JsonObject user2 = createUserJson("456", "jane.smith@example.com", "Jane Smith");
        usersArray.add(user1);
        usersArray.add(user2);

        when(mockRestClient.fetch(eq("user/search"), any(Map.class), eq(String.class)))
                .thenReturn(usersArray.toString());

        List<JiraUser> users = userDao.searchUser("john");

        assertNotNull(users);
        assertEquals(2, users.size());
        verify(mockRestClient).fetch(eq("user/search"), argThat(map -> 
            map.get("query").equals("john")), eq(String.class));
    }

    @Test
    @DisplayName("Should search users with pagination")
    void testSearchUserWithPagination() {
        // First page - 49 users (less than 50 to avoid infinite recursion)
        JsonArray usersArray = new JsonArray();
        for (int i = 0; i < 49; i++) {
            usersArray.add(createUserJson("id" + i, "user" + i + "@example.com", "User " + i));
        }

        when(mockRestClient.fetch(eq("user/search"), any(Map.class), eq(String.class)))
                .thenReturn(usersArray.toString());

        List<JiraUser> users = userDao.searchUser("test", 1, 50);

        assertNotNull(users);
        assertEquals(49, users.size());
        verify(mockRestClient).fetch(eq("user/search"), argThat(map ->
            map.get("startAt").equals(0L) && map.get("maxResults").equals(50L)), eq(String.class));
    }

    @Test
    @DisplayName("Should search users with pagination - multiple pages")
    void testSearchUserWithPagination_MultiplePages() {
        // First page - 50 users
        JsonArray page1 = new JsonArray();
        for (int i = 0; i < 50; i++) {
            page1.add(createUserJson("id" + i, "user" + i + "@example.com", "User " + i));
        }

        // Second page - 30 users
        JsonArray page2 = new JsonArray();
        for (int i = 50; i < 80; i++) {
            page2.add(createUserJson("id" + i, "user" + i + "@example.com", "User " + i));
        }

        when(mockRestClient.fetch(eq("user/search"), argThat(map -> 
            map.get("startAt").equals(0L)), eq(String.class)))
                .thenReturn(page1.toString());
        
        when(mockRestClient.fetch(eq("user/search"), argThat(map -> 
            map.get("startAt").equals(50L)), eq(String.class)))
                .thenReturn(page2.toString());

        List<JiraUser> users = userDao.searchUser("test", 1, 50);

        assertNotNull(users);
        assertEquals(80, users.size());
    }

    @Test
    @DisplayName("Should get users by query")
    void testGetUsersByQuery() {
        JsonArray usersArray = new JsonArray();
        usersArray.add(createUserJson("123", "test@example.com", "Test User"));

        when(mockRestClient.fetch(eq("user/search"), any(Map.class), eq(String.class)))
                .thenReturn(usersArray.toString());

        List<User> users = userDao.getUsersByQuery("test");

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("123", users.get(0).accountId);
    }

    @Test
    @DisplayName("Should get user by account ID")
    void testGetUserByAccountId() {
        JsonObject userJson = createUserJson("123", "test@example.com", "Test User");

        when(mockRestClient.fetch(eq("user"), any(Map.class), eq(String.class)))
                .thenReturn(userJson.toString());

        User user = userDao.getUserByAccountId("123");

        assertNotNull(user);
        assertEquals("123", user.accountId);
        assertEquals("test@example.com", user.email);
        assertEquals("Test User", user.name); // displayName is mapped to name in JiraUser.getUser()
        verify(mockRestClient).fetch(eq("user"), argThat(map ->
            map.get("accountId").equals("123")), eq(String.class));
    }

    @Test
    @DisplayName("Should check if user exists - returns true")
    void testIsUserExist_True() {
        JsonObject userJson = createUserJson("123", "test@example.com", "Test User");

        when(mockRestClient.fetch(eq("user"), any(Map.class), eq(String.class)))
                .thenReturn(userJson.toString());

        Boolean exists = userDao.isUserExist("123");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should check if user exists - returns false on exception")
    void testIsUserExist_False() {
        when(mockRestClient.fetch(eq("user"), any(Map.class), eq(String.class)))
                .thenThrow(new RuntimeException("User not found"));

        Boolean exists = userDao.isUserExist("invalid-id");

        assertFalse(exists);
    }

    @Test
    @DisplayName("Should handle empty search results")
    void testSearchUser_EmptyResults() {
        JsonArray usersArray = new JsonArray();

        when(mockRestClient.fetch(eq("user/search"), any(Map.class), eq(String.class)))
                .thenReturn(usersArray.toString());

        List<JiraUser> users = userDao.searchUser("nonexistent");

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("Should handle null query in search")
    void testSearchUser_NullQuery() {
        JsonArray usersArray = new JsonArray();
        usersArray.add(createUserJson("123", "test@example.com", "Test User"));

        when(mockRestClient.fetch(eq("user/search"), any(Map.class), eq(String.class)))
                .thenReturn(usersArray.toString());

        List<JiraUser> users = userDao.searchUser(null);

        assertNotNull(users);
        verify(mockRestClient).fetch(eq("user/search"), argThat(map ->
            map.get("query") == null || map.get("query").equals("")), eq(String.class));
    }

    @Test
    @DisplayName("Should handle empty query in search")
    void testSearchUser_EmptyQuery() {
        JsonArray usersArray = new JsonArray();
        usersArray.add(createUserJson("123", "test@example.com", "Test User"));

        when(mockRestClient.fetch(eq("user/search"), any(Map.class), eq(String.class)))
                .thenReturn(usersArray.toString());

        List<JiraUser> users = userDao.searchUser("");

        assertNotNull(users);
        verify(mockRestClient).fetch(eq("user/search"), argThat(map ->
            map.get("query").equals("")), eq(String.class));
    }

    @Test
    @DisplayName("Should handle pagination with page 2")
    void testSearchUserWithPagination_Page2() {
        JsonArray usersArray = new JsonArray();
        usersArray.add(createUserJson("123", "test@example.com", "Test User"));

        when(mockRestClient.fetch(eq("user/search"), any(Map.class), eq(String.class)))
                .thenReturn(usersArray.toString());

        List<JiraUser> users = userDao.searchUser("test", 2, 50);

        assertNotNull(users);
        verify(mockRestClient).fetch(eq("user/search"), argThat(map ->
            map.get("startAt").equals(50L)), eq(String.class));
    }

    @Test
    @DisplayName("Should handle pagination with custom page size")
    void testSearchUserWithPagination_CustomPageSize() {
        JsonArray usersArray = new JsonArray();
        for (int i = 0; i < 10; i++) {
            usersArray.add(createUserJson("id" + i, "user" + i + "@example.com", "User " + i));
        }

        when(mockRestClient.fetch(eq("user/search"), any(Map.class), eq(String.class)))
                .thenReturn(usersArray.toString());

        List<JiraUser> users = userDao.searchUser("test", 1, 10);

        assertNotNull(users);
        assertEquals(10, users.size());
        verify(mockRestClient).fetch(eq("user/search"), argThat(map ->
            map.get("maxResults").equals(10L)), eq(String.class));
    }

    @Test
    @DisplayName("Should handle MustAuthorizeException")
    void testSearchUser_MustAuthorizeException() {
        when(mockRestClient.fetch(eq("user/search"), any(Map.class), eq(String.class)))
                .thenThrow(new MustAuthorizeException("Not authorized"));

        assertThrows(MustAuthorizeException.class, () -> {
            userDao.searchUser("test");
        });
    }

    @Test
    @DisplayName("Should get users by query with empty results")
    void testGetUsersByQuery_EmptyResults() {
        JsonArray usersArray = new JsonArray();

        when(mockRestClient.fetch(eq("user/search"), any(Map.class), eq(String.class)))
                .thenReturn(usersArray.toString());

        List<User> users = userDao.getUsersByQuery("nonexistent");

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("Should handle user with missing fields")
    void testGetUserByAccountId_MissingFields() {
        JsonObject userJson = new JsonObject();
        userJson.addProperty("accountId", "123");
        userJson.addProperty("displayName", "Test User");
        userJson.addProperty("accountType", "atlassian");
        // Missing emailAddress - should be handled gracefully

        when(mockRestClient.fetch(eq("user"), any(Map.class), eq(String.class)))
                .thenReturn(userJson.toString());

        User user = userDao.getUserByAccountId("123");

        assertNotNull(user);
        assertEquals("123", user.accountId);
        assertEquals("Test User", user.name);
        assertEquals("", user.email); // Should be empty string when missing
    }

    private JsonObject createUserJson(String accountId, String email, String displayName) {
        JsonObject user = new JsonObject();
        user.addProperty("accountId", accountId);
        user.addProperty("emailAddress", email);
        user.addProperty("displayName", displayName);
        user.addProperty("accountType", "atlassian");
        user.addProperty("active", true);
        return user;
    }
}