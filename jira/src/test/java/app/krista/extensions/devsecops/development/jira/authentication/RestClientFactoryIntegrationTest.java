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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for RestClientFactory.
 * Tests REST client creation based on authentication type.
 */
@DisplayName("RestClientFactory Integration Tests")
class RestClientFactoryIntegrationTest {

    @Mock
    private JiraAttributes mockJiraAttributes;

    @Mock
    private OAuth2RestClient mockOAuth2RestClient;

    @Mock
    private RequestContext mockRequestContext;

    @Mock
    private Invoker mockInvoker;

    private RestClientFactory factory;
    private Map<String, Object> testAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testAttributes = new HashMap<>();
        testAttributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        testAttributes.put(JiraAttributes.USER_NAME, "test@example.com");
        testAttributes.put(JiraAttributes.AUTH_TOKEN, "test-token");
        
        when(mockInvoker.getAttributes()).thenReturn(testAttributes);
        
        factory = new RestClientFactory(mockJiraAttributes, mockOAuth2RestClient, 
                                       mockRequestContext, mockInvoker);
    }

    @Test
    @DisplayName("Should create AuthRestClient when basic credentials present and not invokeAsUser")
    void testGetInstanceAuthRestClient() {
        when(mockJiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
        when(mockRequestContext.invokeAsUser()).thenReturn(false);

        RestClient client = factory.getInstance();

        assertNotNull(client);
        assertTrue(client instanceof AuthRestClient);
    }

    @Test
    @DisplayName("Should create OAuth2RestClient when basic credentials not present")
    void testGetInstanceOAuth2RestClient() {
        when(mockJiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
        when(mockRequestContext.invokeAsUser()).thenReturn(false);

        RestClient client = factory.getInstance();

        assertNotNull(client);
        assertSame(mockOAuth2RestClient, client);
    }

    @Test
    @DisplayName("Should create OAuth2RestClient when invokeAsUser is true")
    void testGetInstanceOAuth2WhenInvokeAsUser() {
        when(mockJiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
        when(mockRequestContext.invokeAsUser()).thenReturn(true);

        RestClient client = factory.getInstance();

        assertNotNull(client);
        assertSame(mockOAuth2RestClient, client);
    }

    @Test
    @DisplayName("Should create AuthRestClient when explicitly passing false for invokeAsUser")
    void testGetInstanceWithExplicitFalse() {
        when(mockJiraAttributes.areBasicCredentialsPresent()).thenReturn(true);

        RestClient client = factory.getInstance(false);

        assertNotNull(client);
        assertTrue(client instanceof AuthRestClient);
    }

    @Test
    @DisplayName("Should create OAuth2RestClient when explicitly passing true for invokeAsUser")
    void testGetInstanceWithExplicitTrue() {
        when(mockJiraAttributes.areBasicCredentialsPresent()).thenReturn(true);

        RestClient client = factory.getInstance(true);

        assertNotNull(client);
        assertSame(mockOAuth2RestClient, client);
    }

    @Test
    @DisplayName("Should return correct invokeAsUser value from RequestContext")
    void testInvokeAsUser() {
        when(mockRequestContext.invokeAsUser()).thenReturn(true);

        boolean result = factory.invokeAsUser();

        assertTrue(result);
        verify(mockRequestContext).invokeAsUser();
    }

    @Test
    @DisplayName("Should return false when RequestContext invokeAsUser is false")
    void testInvokeAsUserFalse() {
        when(mockRequestContext.invokeAsUser()).thenReturn(false);

        boolean result = factory.invokeAsUser();

        assertFalse(result);
    }
}