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

import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("RestClientFactory Tests")
class RestClientFactoryTest {

    private RestClientFactory restClientFactory;
    private JiraAttributes jiraAttributes;
    private OAuth2RestClient oAuth2RestClient;
    private RequestContext requestContext;
    private Invoker invoker;

    @BeforeEach
    void setUp() {
        jiraAttributes = mock(JiraAttributes.class);
        oAuth2RestClient = mock(OAuth2RestClient.class);
        requestContext = mock(RequestContext.class);
        invoker = mock(Invoker.class);

        restClientFactory = new RestClientFactory(jiraAttributes, oAuth2RestClient, requestContext, invoker);
    }

    @Test
    @DisplayName("Should return OAuth2RestClient when basic credentials not present")
    void testGetInstance_ReturnsOAuth2Client_WhenNoBasicCredentials() {
        // Arrange
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);

        // Act
        RestClient result = restClientFactory.getInstance(false);

        // Assert
        assertNotNull(result);
        assertSame(oAuth2RestClient, result);
        verify(jiraAttributes).areBasicCredentialsPresent();
    }

    @Test
    @DisplayName("Should return OAuth2RestClient when invokeAsUser is true")
    void testGetInstance_ReturnsOAuth2Client_WhenInvokeAsUser() {
        // Arrange
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);

        // Act
        RestClient result = restClientFactory.getInstance(true);

        // Assert
        assertNotNull(result);
        assertSame(oAuth2RestClient, result);
    }

    @Test
    @DisplayName("Should return AuthRestClient when basic credentials present and not invokeAsUser")
    void testGetInstance_ReturnsAuthClient_WhenBasicCredentialsPresent() {
        // Arrange
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userName", "testUser");
        attributes.put("authToken", "testToken");
        attributes.put("hostUrl", "https://test.atlassian.net");
        when(invoker.getAttributes()).thenReturn(attributes);

        // Act
        RestClient result = restClientFactory.getInstance(false);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof AuthRestClient);
        verify(jiraAttributes).areBasicCredentialsPresent();
        verify(invoker).getAttributes();
    }

    @Test
    @DisplayName("Should use RequestContext.invokeAsUser when no parameter provided")
    void testGetInstance_UsesRequestContext() {
        // Arrange
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);

        // Act
        RestClient result = restClientFactory.getInstance();

        // Assert
        assertNotNull(result);
        assertSame(oAuth2RestClient, result);
        verify(requestContext).invokeAsUser();
    }

    @Test
    @DisplayName("Should return invokeAsUser from RequestContext")
    void testInvokeAsUser_ReturnsFromRequestContext() {
        // Arrange
        when(requestContext.invokeAsUser()).thenReturn(true);

        // Act
        boolean result = restClientFactory.invokeAsUser();

        // Assert
        assertTrue(result);
        verify(requestContext).invokeAsUser();
    }

    @Test
    @DisplayName("Should return false when RequestContext.invokeAsUser is false")
    void testInvokeAsUser_ReturnsFalse() {
        // Arrange
        when(requestContext.invokeAsUser()).thenReturn(false);

        // Act
        boolean result = restClientFactory.invokeAsUser();

        // Assert
        assertFalse(result);
        verify(requestContext).invokeAsUser();
    }

    @Test
    @DisplayName("Should have Service annotation")
    void testHasServiceAnnotation() {
        // Assert
        assertTrue(RestClientFactory.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }

    @Test
    @DisplayName("Should have Inject annotation on constructor")
    void testHasInjectAnnotation() throws NoSuchMethodException {
        // Act
        var constructor = RestClientFactory.class.getConstructor(
                JiraAttributes.class, OAuth2RestClient.class, RequestContext.class, Invoker.class);

        // Assert
        assertTrue(constructor.isAnnotationPresent(javax.inject.Inject.class));
    }

    @Test
    @DisplayName("Should create new AuthRestClient instance each time")
    void testGetInstance_CreatesNewAuthClientEachTime() {
        // Arrange
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("userName", "testUser");
        attributes.put("authToken", "testToken");
        attributes.put("hostUrl", "https://test.atlassian.net");
        when(invoker.getAttributes()).thenReturn(attributes);

        // Act
        RestClient result1 = restClientFactory.getInstance(false);
        RestClient result2 = restClientFactory.getInstance(false);

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotSame(result1, result2, "Should create new AuthRestClient instances");
    }

    @Test
    @DisplayName("Should return same OAuth2RestClient instance")
    void testGetInstance_ReturnsSameOAuth2ClientInstance() {
        // Arrange
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);

        // Act
        RestClient result1 = restClientFactory.getInstance(false);
        RestClient result2 = restClientFactory.getInstance(false);

        // Assert
        assertSame(result1, result2);
        assertSame(oAuth2RestClient, result1);
    }
}