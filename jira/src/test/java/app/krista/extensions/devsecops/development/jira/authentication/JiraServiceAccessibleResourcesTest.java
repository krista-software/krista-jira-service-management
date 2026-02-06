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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JiraService AccessibleResources Tests")
class JiraServiceAccessibleResourcesTest {

    private JiraService jiraService;
    private SessionManager sessionManager;
    private RequestContext requestContext;
    private AuthorizationContext authorizationContext;
    private JiraAttributes jiraAttributes;
    private Invoker invoker;
    private RoutingInfo routingInfo;
    private OAuth20Service oAuth20Service;
    private Account account;

    @BeforeEach
    void setUp() {
        sessionManager = mock(SessionManager.class);
        requestContext = mock(RequestContext.class);
        authorizationContext = mock(AuthorizationContext.class);
        jiraAttributes = mock(JiraAttributes.class);
        invoker = mock(Invoker.class);
        routingInfo = mock(RoutingInfo.class);
        oAuth20Service = mock(OAuth20Service.class);
        account = mock(Account.class);

        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn("http://localhost:8080");
        when(requestContext.getInvokerId()).thenReturn("test-invoker-id");
        when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
        when(account.getAccountId()).thenReturn("test-account-id");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
    }

    @Test
    @DisplayName("Should get accessible resources successfully")
    void testGetAccessibleResources_Success() throws Exception {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token");

        Response response = mock(Response.class);
        when(response.getCode()).thenReturn(200);
        String jsonResponse = "[{\"id\":\"resource-id-1\",\"scopes\":[\"read:jira-user\",\"read:jira-work\",\"write:jira-work\",\"manage:jira-data-provider\",\"manage:jira-project\",\"read:issue:jira\",\"write:issue:jira\",\"read:servicedesk-request\",\"write:servicedesk-request\",\"manage:servicedesk-customer\"]}]";
        when(response.getBody()).thenReturn(jsonResponse);

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");
        when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);
        when(mockOAuthService.execute(any(OAuthRequest.class))).thenReturn(response);

        java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
        field.setAccessible(true);
        field.set(jiraService, mockOAuthService);

        // Act
        JiraService.AccessibleResources result = jiraService.getAccessibleResources();

        // Assert
        assertNotNull(result);
        assertEquals("resource-id-1", result.getId());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when response code is 400")
    void testGetAccessibleResources_ThrowsException_On400() throws Exception {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token");

        Response response = mock(Response.class);
        when(response.getCode()).thenReturn(400);
        when(response.getBody()).thenReturn("Bad Request");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");
        when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);
        when(mockOAuthService.execute(any(OAuthRequest.class))).thenReturn(response);

        java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
        field.setAccessible(true);
        field.set(jiraService, mockOAuthService);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                jiraService.getAccessibleResources()
        );
        assertTrue(exception.getMessage().contains("Bad Request"));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when no resources found")
    void testGetAccessibleResources_ThrowsException_WhenNoResources() throws Exception {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token");

        Response response = mock(Response.class);
        when(response.getCode()).thenReturn(200);
        when(response.getBody()).thenReturn("[]");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");
        when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);
        when(mockOAuthService.execute(any(OAuthRequest.class))).thenReturn(response);

        java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
        field.setAccessible(true);
        field.set(jiraService, mockOAuthService);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                jiraService.getAccessibleResources()
        );
        assertTrue(exception.getMessage().contains("didn't found any resources"));
    }

    @Test
    @DisplayName("Should get accessible resources with accountId and invokerId")
    void testGetAccessibleResources_WithParameters_Success() throws Exception {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("account-123#invoker-456")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token");

        Response response = mock(Response.class);
        when(response.getCode()).thenReturn(200);
        String jsonResponse = "[{\"id\":\"resource-id-2\",\"scopes\":[\"read:jira-user\",\"read:jira-work\",\"write:jira-work\",\"manage:jira-data-provider\",\"manage:jira-project\",\"read:issue:jira\",\"write:issue:jira\",\"read:servicedesk-request\",\"write:servicedesk-request\",\"manage:servicedesk-customer\"]}]";
        when(response.getBody()).thenReturn(jsonResponse);

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");
        when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);
        when(mockOAuthService.execute(any(OAuthRequest.class))).thenReturn(response);

        java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
        field.setAccessible(true);
        field.set(jiraService, mockOAuthService);

        // Act
        JiraService.AccessibleResources result = jiraService.getAccessibleResources("account-123", "invoker-456");

        // Assert
        assertNotNull(result);
        assertEquals("resource-id-2", result.getId());
    }

    @Test
    @DisplayName("Should throw MustAuthorizeException when exception occurs")
    void testGetAccessibleResources_ThrowsMustAuthorizeException() throws Exception {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn(null);

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);

        // Act & Assert
        assertThrows(MustAuthorizeException.class, () ->
                jiraService.getAccessibleResources()
        );
    }

    @Test
    @DisplayName("Should throw IllegalStateException on IOException")
    void testGetAccessibleResources_ThrowsIllegalStateException_OnIOException() throws Exception {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");
        when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);
        when(mockOAuthService.execute(any(OAuthRequest.class))).thenThrow(new IOException("Test error"));

        java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
        field.setAccessible(true);
        field.set(jiraService, mockOAuthService);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                jiraService.getAccessibleResources()
        );
    }

    @Test
    @DisplayName("Should select resource with all scopes from multiple resources")
    void testGetAccessibleResources_SelectsResourceWithAllScopes() throws Exception {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token");

        Response response = mock(Response.class);
        when(response.getCode()).thenReturn(200);
        String jsonResponse = "[{\"id\":\"resource-id-1\",\"scopes\":[\"read:jira-user\"]},{\"id\":\"resource-id-2\",\"scopes\":[\"read:jira-user\",\"read:jira-work\",\"write:jira-work\",\"manage:jira-data-provider\",\"manage:jira-project\",\"read:issue:jira\",\"write:issue:jira\",\"read:servicedesk-request\",\"write:servicedesk-request\",\"manage:servicedesk-customer\"]}]";
        when(response.getBody()).thenReturn(jsonResponse);

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");
        when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);
        when(mockOAuthService.execute(any(OAuthRequest.class))).thenReturn(response);

        java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
        field.setAccessible(true);
        field.set(jiraService, mockOAuthService);

        // Act
        JiraService.AccessibleResources result = jiraService.getAccessibleResources();

        // Assert
        assertNotNull(result);
        assertEquals("resource-id-2", result.getId());
    }

    @Test
    @DisplayName("Should throw IllegalStateException when no resource has all required scopes")
    void testGetAccessibleResources_ThrowsException_WhenNoResourceHasAllScopes() throws Exception {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token");

        Response response = mock(Response.class);
        when(response.getCode()).thenReturn(200);
        String jsonResponse = "[{\"id\":\"resource-id-1\",\"scopes\":[\"read:jira-user\"]},{\"id\":\"resource-id-2\",\"scopes\":[\"read:jira-work\"]}]";
        when(response.getBody()).thenReturn(jsonResponse);

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");
        when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);
        when(mockOAuthService.execute(any(OAuthRequest.class))).thenReturn(response);

        java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
        field.setAccessible(true);
        field.set(jiraService, mockOAuthService);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                jiraService.getAccessibleResources()
        );
        assertTrue(exception.getMessage().contains("None of the accessible resources has all the scopes"));
    }
}