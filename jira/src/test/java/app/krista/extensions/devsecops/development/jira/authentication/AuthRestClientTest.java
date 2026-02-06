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

import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Invocation;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthRestClient Tests")
class AuthRestClientTest {

    private AuthRestClient authRestClient;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        attributes.put(JiraAttributes.USER_NAME, "test-user");
        attributes.put(JiraAttributes.AUTH_TOKEN, "test-token");
        attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net/rest/api/3");
    }

    @Test
    @DisplayName("Should create AuthRestClient with valid attributes")
    void testConstructor_Success() {
        // Act
        authRestClient = new AuthRestClient(attributes, false, false);

        // Assert
        assertNotNull(authRestClient);
    }

    @Test
    @DisplayName("Should be final class")
    void testIsFinalClass() {
        // Assert
        assertTrue(java.lang.reflect.Modifier.isFinal(AuthRestClient.class.getModifiers()));
    }

    @Test
    @DisplayName("Should implement RestClient interface")
    void testImplementsRestClient() {
        // Assert
        assertTrue(RestClient.class.isAssignableFrom(AuthRestClient.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when invokeAsUser is true and not from validateAttributes")
    void testGetResourceInvoker_ThrowsException_WhenInvokeAsUserTrue() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, true, false);
        Map<String, Object> queryParams = new HashMap<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.getResourceInvoker("issue/TEST-123", queryParams)
        );
        assertTrue(exception.getMessage().contains("User cannot be authenticated"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when resource is null in fetch")
    void testFetch_ThrowsException_WhenResourceNull() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);
        Map<String, Object> queryParams = new HashMap<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.fetch(null, queryParams, String.class)
        );
        assertEquals("resource could not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when resource is null in fetchJiraService")
    void testFetchJiraService_ThrowsException_WhenResourceNull() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);
        Map<String, Object> queryParams = new HashMap<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.fetchJiraService(null, queryParams, String.class)
        );
        assertEquals("resource could not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when resource is null in fetchMiscellaneousService")
    void testFetchMiscellaneousService_ThrowsException_WhenResourceNull() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);
        Map<String, Object> queryParams = new HashMap<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.fetchMiscellaneousService(null, queryParams, String.class)
        );
        assertEquals("resource could not be null", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when resource is null in attachFile")
    void testAttachFile_ThrowsException_WhenResourceNull() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);
        List<File> files = List.of(new File("test.txt"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.attachFile(null, files)
        );
        assertEquals("resource not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when resource is blank in attachFile")
    void testAttachFile_ThrowsException_WhenResourceBlank() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);
        List<File> files = List.of(new File("test.txt"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.attachFile("   ", files)
        );
        assertEquals("resource not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when files is null in attachFile")
    void testAttachFile_ThrowsException_WhenFilesNull() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.attachFile("TEST-123", null)
        );
        assertEquals("file not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when files is empty in attachFile")
    void testAttachFile_ThrowsException_WhenFilesEmpty() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.attachFile("TEST-123", List.of())
        );
        assertEquals("file not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should return null for getJiraAccessToken")
    void testGetJiraAccessToken_ReturnsNull() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);

        // Act
        String result = authRestClient.getJiraAccessToken();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should extract base URL from hostUrl with /rest/ path")
    void testGetBaseUrl_WithRestPath() throws Exception {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);
        java.lang.reflect.Method method = AuthRestClient.class.getDeclaredMethod("getBaseUrl", String.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(authRestClient, "https://test.atlassian.net/rest/api/3");

        // Assert
        assertEquals("https://test.atlassian.net", result);
    }

    @Test
    @DisplayName("Should extract base URL from hostUrl ending with /rest")
    void testGetBaseUrl_EndingWithRest() throws Exception {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);
        java.lang.reflect.Method method = AuthRestClient.class.getDeclaredMethod("getBaseUrl", String.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(authRestClient, "https://test.atlassian.net/rest");

        // Assert
        assertEquals("https://test.atlassian.net", result);
    }

    @Test
    @DisplayName("Should return same URL when no /rest in hostUrl")
    void testGetBaseUrl_NoRestPath() throws Exception {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);
        java.lang.reflect.Method method = AuthRestClient.class.getDeclaredMethod("getBaseUrl", String.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(authRestClient, "https://test.atlassian.net");

        // Assert
        assertEquals("https://test.atlassian.net", result);
    }

    @Test
    @DisplayName("Should return empty string when hostUrl is null")
    void testGetBaseUrl_NullHostUrl() throws Exception {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);
        java.lang.reflect.Method method = AuthRestClient.class.getDeclaredMethod("getBaseUrl", String.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(authRestClient, (String) null);

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should return empty string when hostUrl is empty")
    void testGetBaseUrl_EmptyHostUrl() throws Exception {
        // Arrange
        authRestClient = new AuthRestClient(attributes, false, false);
        java.lang.reflect.Method method = AuthRestClient.class.getDeclaredMethod("getBaseUrl", String.class);
        method.setAccessible(true);

        // Act
        String result = (String) method.invoke(authRestClient, "");

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should allow invokeAsUser when callFromValidateAttributes is true")
    void testConstructor_AllowsInvokeAsUser_WhenFromValidateAttributes() {
        // Act
        authRestClient = new AuthRestClient(attributes, true, true);

        // Assert - Should not throw exception
        assertNotNull(authRestClient);
    }

    @Test
    @DisplayName("Should throw exception for getJiraServiceResourceInvoker when invokeAsUser is true")
    void testGetJiraServiceResourceInvoker_ThrowsException_WhenInvokeAsUserTrue() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, true, false);
        Map<String, Object> queryParams = new HashMap<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.getJiraServiceResourceInvoker("servicedesk", queryParams)
        );
        assertTrue(exception.getMessage().contains("User cannot be authenticated"));
    }

    @Test
    @DisplayName("Should throw exception for fetchJiraService when invokeAsUser is true")
    void testFetchJiraService_ThrowsException_WhenInvokeAsUserTrue() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, true, false);
        Map<String, Object> queryParams = new HashMap<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.fetchJiraService("servicedesk", queryParams, String.class)
        );
        assertTrue(exception.getMessage().contains("User cannot be authenticated"));
    }

    @Test
    @DisplayName("Should throw exception for fetchMiscellaneousService when invokeAsUser is true")
    void testFetchMiscellaneousService_ThrowsException_WhenInvokeAsUserTrue() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, true, false);
        Map<String, Object> queryParams = new HashMap<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.fetchMiscellaneousService("servicedesk", queryParams, String.class)
        );
        assertTrue(exception.getMessage().contains("User cannot be authenticated"));
    }

    @Test
    @DisplayName("Should throw exception for parseAndValidateJQL when invokeAsUser is true")
    void testParseAndValidateJQL_ThrowsException_WhenInvokeAsUserTrue() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, true, false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.parseAndValidateJQL("project = TEST", true, "account-id", "invoker-id")
        );
        assertTrue(exception.getMessage().contains("User cannot be authenticated"));
    }

    @Test
    @DisplayName("Should throw exception for fetchIssues with invokeAsUser when invokeAsUser is true")
    void testFetchIssues_WithInvokeAsUser_ThrowsException() {
        // Arrange
        authRestClient = new AuthRestClient(attributes, true, false);
        Map<String, Object> queryParams = new HashMap<>();

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authRestClient.fetchIssues(true, "account-id", "invoker-id", "search", queryParams, String.class)
        );
        assertTrue(exception.getMessage().contains("User cannot be authenticated"));
    }
}