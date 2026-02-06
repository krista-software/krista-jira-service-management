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
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JiraService Tests")
class JiraServiceTest {

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

    // Helper interface to mock Account.getAccountId() method
    public interface AccountIdProvider {
        String getAccountId();
    }

    @Test
    @DisplayName("Should create OAuth service when credentials are present")
    void testGetOauthService_CreatesService_WhenCredentialsPresent() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");

        // Create a new instance to test
        JiraService testService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);

        // Act
        OAuth20Service result = testService.getOauthService();

        // Assert
        assertNotNull(result);
        verify(jiraAttributes, atLeast(1)).areOAuthCredentialsPresent();
        verify(jiraAttributes, atLeast(1)).getClientId();
        verify(jiraAttributes, atLeast(1)).getClientSecret();
    }

    @Test
    @DisplayName("Should return null when OAuth credentials not present")
    void testGetOauthService_ReturnsNull_WhenCredentialsNotPresent() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);

        // Act
        OAuth20Service result = jiraService.getOauthService();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should have correct scopes list")
    void testScopes_ContainsRequiredScopes() {
        // Assert
        assertTrue(JiraService.scopes.contains("read:jira-user"));
        assertTrue(JiraService.scopes.contains("read:jira-work"));
        assertTrue(JiraService.scopes.contains("write:jira-work"));
        assertTrue(JiraService.scopes.contains("manage:jira-data-provider"));
        assertTrue(JiraService.scopes.contains("manage:jira-project"));
        assertTrue(JiraService.scopes.contains("read:servicedesk-request"));
        assertTrue(JiraService.scopes.contains("write:servicedesk-request"));
        assertTrue(JiraService.scopes.contains("manage:servicedesk-customer"));
        assertEquals(8, JiraService.scopes.size());
    }

    @Test
    @DisplayName("Should have correct resource URL")
    void testResourceUrl_IsCorrect() {
        // Assert
        assertEquals("https://api.atlassian.com/oauth/token/accessible-resources", JiraService.RESOURCE_URL);
    }

    @Test
    @DisplayName("Should have Service annotation")
    void testHasServiceAnnotation() {
        // Assert
        assertTrue(JiraService.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }

    @Test
    @DisplayName("Should be final class")
    void testIsFinalClass() {
        // Assert
        assertTrue(java.lang.reflect.Modifier.isFinal(JiraService.class.getModifiers()));
    }

    @Test
    @DisplayName("Should have Inject annotation on constructor")
    void testHasInjectAnnotation() throws NoSuchMethodException {
        // Act
        var constructor = JiraService.class.getConstructor(
                RequestContext.class, SessionManager.class, Invoker.class,
                AuthorizationContext.class, JiraAttributes.class);

        // Assert
        assertTrue(constructor.isAnnotationPresent(javax.inject.Inject.class));
    }

    @Test
    @DisplayName("AccessibleResources should have correct constructor")
    void testAccessibleResources_Constructor() {
        // Act
        JiraService.AccessibleResources resources = new JiraService.AccessibleResources(
                "test-id", List.of("scope1", "scope2"));

        // Assert
        assertNotNull(resources);
        assertEquals("test-id", resources.getId());
    }

    @Test
    @DisplayName("AccessibleResources should be public static class")
    void testAccessibleResources_IsPublicStatic() {
        // Assert
        assertTrue(java.lang.reflect.Modifier.isPublic(JiraService.AccessibleResources.class.getModifiers()));
        assertTrue(java.lang.reflect.Modifier.isStatic(JiraService.AccessibleResources.class.getModifiers()));
    }

    @Test
    @DisplayName("Should get Jira access token with accountId and invokerId")
    void testGetJiraAccessToken_WithParameters_Success() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account#test-invoker")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);

            // Use reflection to set the oAuthService
            java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act
            OAuth2AccessToken result = jiraService.getJiraAccessToken("test-account", "test-invoker");

            // Assert
            assertNotNull(result);
            assertEquals("access-token", result.getAccessToken());
            verify(sessionManager).getRefToken("test-account#test-invoker");
            verify(sessionManager).storeRefToken("test-account#test-invoker", "new-refresh-token");
        } catch (Exception e) {
            fail("Reflection or execution failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should throw MustAuthorizeException when refresh token is null")
    void testGetJiraAccessToken_ThrowsException_WhenRefreshTokenNull() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn(null);

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);

        // Act & Assert
        assertThrows(MustAuthorizeException.class, () -> jiraService.getJiraAccessToken(null, null));
    }

    @Test
    @DisplayName("Should return null when ExecutionException occurs")
    void testGetJiraAccessToken_ReturnsNull_OnExecutionException() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account#test-invoker")).thenReturn("refresh-token");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token"))
                    .thenThrow(new ExecutionException("Test error", new Exception()));

            java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act
            OAuth2AccessToken result = jiraService.getJiraAccessToken("test-account", "test-invoker");

            // Assert
            assertNull(result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should throw RuntimeException when IOException occurs")
    void testGetJiraAccessToken_ThrowsRuntimeException_OnIOException() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account#test-invoker")).thenReturn("refresh-token");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token"))
                    .thenThrow(new IOException("Test error"));

            java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> jiraService.getJiraAccessToken("test-account", "test-invoker"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should throw RuntimeException when InterruptedException occurs")
    void testGetJiraAccessToken_ThrowsRuntimeException_OnInterruptedException() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account#test-invoker")).thenReturn("refresh-token");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token"))
                    .thenThrow(new InterruptedException("Test error"));

            java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> jiraService.getJiraAccessToken("test-account", "test-invoker"));
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should get Jira access token without parameters")
    void testGetJiraAccessToken_NoParameters_Success() {
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

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);

            java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act
            OAuth2AccessToken result = jiraService.getJiraAccessToken();

            // Assert
            assertNotNull(result);
            assertEquals("access-token", result.getAccessToken());
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should throw IllegalCallerException when client credentials missing")
    void testGetJiraAccessToken_ThrowsIllegalCallerException_WhenCredentialsMissing() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);
        when(jiraAttributes.getClientId()).thenReturn(null);
        when(jiraAttributes.getClientSecret()).thenReturn(null);
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn("refresh-token");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token"))
                    .thenThrow(new ExecutionException("Test error", new Exception()));

            java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act & Assert
            assertThrows(IllegalCallerException.class, () -> jiraService.getJiraAccessToken());
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should throw MustAuthorizeException on RuntimeException with credentials present")
    void testGetJiraAccessToken_ThrowsMustAuthorizeException_OnRuntimeException() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn("refresh-token");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token"))
                    .thenThrow(new RuntimeException("Test error"));

            java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act & Assert
            assertThrows(MustAuthorizeException.class, () -> jiraService.getJiraAccessToken());
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should get Jira token")
    void testGetJiraToken_Success() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("test-account-id#test-invoker-id")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token-123");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);

            java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act
            String result = jiraService.getJiraToken();

            // Assert
            assertEquals("access-token-123", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Should get Jira token for issues")
    void testGetJiraTokenForIssues_Success() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.getRefToken("account-123#invoker-456")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token-456");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);

            java.lang.reflect.Field field = JiraService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act
            String result = jiraService.getJiraTokenForIssues("account-123", "invoker-456");

            // Assert
            assertEquals("access-token-456", result);
        } catch (Exception e) {
            fail("Reflection failed: " + e.getMessage());
        }
    }
}
