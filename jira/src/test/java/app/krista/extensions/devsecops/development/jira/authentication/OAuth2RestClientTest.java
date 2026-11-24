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

import app.krista.sdk.atlassian.oauth.AccessibleResource;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("OAuth2RestClient Tests")
class OAuth2RestClientTest {

    private OAuth2RestClient oAuth2RestClient;
    private JiraService jiraService;

    @BeforeEach
    void setUp() {
        jiraService = mock(JiraService.class);
        oAuth2RestClient = new OAuth2RestClient(jiraService);
    }

    @Test
    @DisplayName("Should have Service annotation")
    void testHasServiceAnnotation() {
        // Assert
        assertTrue(OAuth2RestClient.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }

    @Test
    @DisplayName("Should be final class")
    void testIsFinalClass() {
        // Assert
        assertTrue(java.lang.reflect.Modifier.isFinal(OAuth2RestClient.class.getModifiers()));
    }

    @Test
    @DisplayName("Should have Inject annotation on constructor")
    void testHasInjectAnnotation() throws NoSuchMethodException {
        // Act
        var constructor = OAuth2RestClient.class.getConstructor(JiraService.class);

        // Assert
        assertTrue(constructor.isAnnotationPresent(javax.inject.Inject.class));
    }

    @Test
    @DisplayName("Should implement RestClient interface")
    void testImplementsRestClient() {
        // Assert
        assertTrue(RestClient.class.isAssignableFrom(OAuth2RestClient.class));
    }

    @Test
    @DisplayName("Should have correct Atlassian URL constant")
    void testAtlassianUrlConstant() {
        // Assert
        assertEquals("https://api.atlassian.com/ex/jira/", OAuth2RestClient.ATLASSIAN_URL);
    }

    @Test
    @DisplayName("Should have correct Jira endpoint constant")
    void testJiraEndpointConstant() {
        // Assert
        assertEquals("/rest/api/3/", OAuth2RestClient.JIRA_ENDPOINT);
    }

    @Test
    @DisplayName("Should get accessible resources and cache them")
    void testGetAccessibleResources_CachesResult() {
        // Arrange
        AccessibleResource mockResources = new AccessibleResource(
                "test-resource-id", List.of("scope1", "scope2"));
        when(jiraService.getAccessibleResources()).thenReturn(mockResources);

        // Act
        AccessibleResource result1 = oAuth2RestClient.getAccessibleResources();
        AccessibleResource result2 = oAuth2RestClient.getAccessibleResources();

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertSame(result1, result2);
        assertEquals("test-resource-id", result1.getId());
        verify(jiraService, times(1)).getAccessibleResources(); // Should only call once due to caching
    }

    @Test
    @DisplayName("Should get accessible resources with accountId and invokerId")
    void testGetAccessibleResources_WithParameters() {
        // Arrange
        AccessibleResource mockResources = new AccessibleResource(
                "test-resource-id-2", List.of("scope1", "scope2"));
        when(jiraService.getAccessibleResources("account-123", "invoker-456")).thenReturn(mockResources);

        // Act
        AccessibleResource result = oAuth2RestClient.getAccessibleResources("account-123", "invoker-456");

        // Assert
        assertNotNull(result);
        assertEquals("test-resource-id-2", result.getId());
        verify(jiraService).getAccessibleResources("account-123", "invoker-456");
    }

    @Test
    @DisplayName("Should cache accessible resources with parameters")
    void testGetAccessibleResources_WithParameters_CachesResult() {
        // Arrange
        AccessibleResource mockResources = new AccessibleResource(
                "test-resource-id-3", List.of("scope1", "scope2"));
        when(jiraService.getAccessibleResources("account-789", "invoker-012")).thenReturn(mockResources);

        // Act
        AccessibleResource result1 = oAuth2RestClient.getAccessibleResources("account-789", "invoker-012");
        AccessibleResource result2 = oAuth2RestClient.getAccessibleResources("account-789", "invoker-012");

        // Assert
        assertSame(result1, result2);
        verify(jiraService, times(1)).getAccessibleResources("account-789", "invoker-012");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when resource is null in attachFile")
    void testAttachFile_ThrowsException_WhenResourceNull() {
        // Arrange
        List<File> files = List.of(new File("test.txt"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                oAuth2RestClient.attachFile(null, files)
        );
        assertEquals("resource not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when resource is blank in attachFile")
    void testAttachFile_ThrowsException_WhenResourceBlank() {
        // Arrange
        List<File> files = List.of(new File("test.txt"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                oAuth2RestClient.attachFile("   ", files)
        );
        assertEquals("resource not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when files is null in attachFile")
    void testAttachFile_ThrowsException_WhenFilesNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                oAuth2RestClient.attachFile("TEST-123", null)
        );
        assertEquals("file not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when files is empty in attachFile")
    void testAttachFile_ThrowsException_WhenFilesEmpty() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                oAuth2RestClient.attachFile("TEST-123", List.of())
        );
        assertEquals("file not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should return Jira access token")
    void testGetJiraAccessToken_ReturnsToken() {
        // Arrange
        when(jiraService.getToken()).thenReturn("test-access-token-123");

        // Act
        String result = oAuth2RestClient.getJiraAccessToken();

        // Assert
        assertEquals("test-access-token-123", result);
        verify(jiraService).getToken();
    }

    @Test
    @DisplayName("Should return null when jiraService returns null")
    void testGetJiraAccessToken_ReturnsNull() {
        // Arrange
        when(jiraService.getJiraToken()).thenReturn(null);

        // Act
        String result = oAuth2RestClient.getJiraAccessToken();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should call getJiraToken for each invocation")
    void testGetJiraAccessToken_CallsServiceEachTime() {
        // Arrange
        when(jiraService.getToken()).thenReturn("token1", "token2");

        // Act
        String result1 = oAuth2RestClient.getJiraAccessToken();
        String result2 = oAuth2RestClient.getJiraAccessToken();

        // Assert
        assertEquals("token1", result1);
        assertEquals("token2", result2);
        verify(jiraService, times(2)).getToken();
    }

    @Test
    @DisplayName("Should return null when fetch encounters exception")
    void testFetch_ReturnsNull_OnException() {
        // Arrange
        AccessibleResource mockResources = new AccessibleResource(
                "test-resource-id", List.of("scope1"));
        when(jiraService.getAccessibleResources()).thenReturn(mockResources);
        when(jiraService.getJiraToken()).thenReturn("test-token");

        Map<String, Object> queryParams = new HashMap<>();

        // Mock the Jersey Client infrastructure
        try (MockedStatic<ClientBuilder> clientBuilderMock = mockStatic(ClientBuilder.class)) {
            Client mockClient = mock(Client.class);
            WebTarget mockWebTarget = mock(WebTarget.class);
            WebTarget mockPathTarget = mock(WebTarget.class);
            Invocation.Builder mockBuilder = mock(Invocation.Builder.class);

            clientBuilderMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.path(anyString())).thenReturn(mockPathTarget);
            when(mockPathTarget.queryParam(anyString(), any())).thenReturn(mockPathTarget);
            when(mockPathTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            when(mockBuilder.header(anyString(), anyString())).thenReturn(mockBuilder);
            // Simulate exception during HTTP request
            when(mockBuilder.get()).thenThrow(new RuntimeException("HTTP request failed"));

            // Act
            String result = oAuth2RestClient.fetch("issue/TEST-123", queryParams, String.class);

            // Assert
            assertNull(result);
        }
    }

    @Test
    @DisplayName("Should use correct query parameters in getResourceInvoker")
    void testGetResourceInvoker_UsesQueryParams() {
        // Arrange
        AccessibleResource mockResources = new AccessibleResource(
                "test-resource-id", List.of("scope1"));
        when(jiraService.getAccessibleResources()).thenReturn(mockResources);
        when(jiraService.getToken()).thenReturn("test-token");

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("maxResults", 50);
        queryParams.put("startAt", 0);

        // Mock the Jersey Client infrastructure
        try (MockedStatic<ClientBuilder> clientBuilderMock = mockStatic(ClientBuilder.class)) {
            Client mockClient = mock(Client.class);
            WebTarget mockWebTarget = mock(WebTarget.class);
            WebTarget mockPathTarget = mock(WebTarget.class);
            WebTarget mockQueryTarget = mock(WebTarget.class);
            Invocation.Builder mockBuilder = mock(Invocation.Builder.class);

            clientBuilderMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.path("search")).thenReturn(mockPathTarget);
            when(mockPathTarget.queryParam(anyString(), any())).thenReturn(mockQueryTarget);
            when(mockQueryTarget.queryParam(anyString(), any())).thenReturn(mockQueryTarget);
            when(mockQueryTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            when(mockBuilder.header(anyString(), anyString())).thenReturn(mockBuilder);

            // Act
            Invocation.Builder result = oAuth2RestClient.getResourceInvoker("search", queryParams);

            // Assert
            assertNotNull(result);
            verify(jiraService).getToken();
            verify(jiraService).getAccessibleResources();
        }
    }

    @Test
    @DisplayName("Should use correct query parameters in getJiraServiceResourceInvoker")
    void testGetJiraServiceResourceInvoker_UsesQueryParams() {
        // Arrange
        AccessibleResource mockResources = new AccessibleResource(
                "test-resource-id", List.of("scope1"));
        when(jiraService.getAccessibleResources()).thenReturn(mockResources);
        when(jiraService.getToken()).thenReturn("test-token");

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("limit", 10);

        // Mock the Jersey Client infrastructure
        try (MockedStatic<ClientBuilder> clientBuilderMock = mockStatic(ClientBuilder.class)) {
            Client mockClient = mock(Client.class);
            WebTarget mockWebTarget = mock(WebTarget.class);
            WebTarget mockPathTarget = mock(WebTarget.class);
            WebTarget mockQueryTarget = mock(WebTarget.class);
            Invocation.Builder mockBuilder = mock(Invocation.Builder.class);

            clientBuilderMock.when(ClientBuilder::newClient).thenReturn(mockClient);
            when(mockClient.target(anyString())).thenReturn(mockWebTarget);
            when(mockWebTarget.path("servicedesk")).thenReturn(mockPathTarget);
            when(mockPathTarget.queryParam("limit", 10)).thenReturn(mockQueryTarget);
            when(mockQueryTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockBuilder);
            when(mockBuilder.header(anyString(), anyString())).thenReturn(mockBuilder);

            // Act
            Invocation.Builder result = oAuth2RestClient.getJiraServiceResourceInvoker("servicedesk", queryParams);

            // Assert
            assertNotNull(result);
            verify(jiraService).getToken();
            verify(jiraService).getAccessibleResources();
        }
    }
}