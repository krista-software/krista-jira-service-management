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
import app.krista.sdk.atlassian.constants.AtlassianConstants;
import app.krista.sdk.atlassian.oauth.AbstractAtlassianOAuthService;
import app.krista.sdk.atlassian.oauth.AccessibleResource;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    private Account account;

    @BeforeEach
    void setUp() {
        sessionManager = mock(SessionManager.class);
        requestContext = mock(RequestContext.class);
        authorizationContext = mock(AuthorizationContext.class);
        jiraAttributes = mock(JiraAttributes.class);
        invoker = mock(Invoker.class);
        routingInfo = mock(RoutingInfo.class);
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
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");

        // Create a new instance to test
        JiraService testService = new JiraService(requestContext, sessionManager, invoker, authorizationContext,
                jiraAttributes);

        // Act
        OAuth20Service result = testService.getOauthService();

        // Assert
        assertNotNull(result);
        verify(jiraAttributes, atLeast(1)).areOAuthCredentialsPresent();
        verify(jiraAttributes, atLeast(1)).getEffectiveClientId();
        verify(jiraAttributes, atLeast(1)).getEffectiveClientSecret();
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
        assertEquals("https://api.atlassian.com/oauth/token/accessible-resources", AtlassianConstants.ACCESSIBLE_RESOURCES_URL);
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
        AccessibleResource resources = new AccessibleResource(
                "test-id", List.of("scope1", "scope2"));

        // Assert
        assertNotNull(resources);
        assertEquals("test-id", resources.getId());
    }

    @Test
    @DisplayName("AccessibleResource should be a public class")
    void testAccessibleResources_IsPublicStatic() {
        // Assert (the SDK type is a public top-level class)
        assertTrue(java.lang.reflect.Modifier.isPublic(AccessibleResource.class.getModifiers()));
    }

    @Test
    @DisplayName("Should get Jira access token with accountId and invokerId")
    void testGetJiraAccessToken_WithParameters_Success() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");
        when(requestContext.invokeAsUser()).thenReturn(false); // Should use clientId#clientSecret
        String expectedKey = "test-client-id#test-client-secret#test-invoker";
        when(sessionManager.get(expectedKey)).thenReturn("refresh-token");

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
            java.lang.reflect.Field field = AbstractAtlassianOAuthService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act
            OAuth2AccessToken result = jiraService.getJiraAccessToken("test-account", "test-invoker");

            // Assert
            assertNotNull(result);
            assertEquals("access-token", result.getAccessToken());
            verify(sessionManager).get(expectedKey);
            verify(sessionManager).store(expectedKey, "new-refresh-token");
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
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");
        when(requestContext.getInvokerId()).thenReturn("test-invoker");
        when(sessionManager.get("test-client-id#test-client-secret#test-invoker")).thenReturn(null);

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);

        // Act & Assert
        assertThrows(MustAuthorizeException.class, () -> jiraService.getJiraAccessToken(null, "test-invoker"));
    }

    @Test
    @DisplayName("Should throw MustAuthorizeException when ExecutionException occurs")
    void testGetJiraAccessToken_ThrowsMustAuthorizeException_OnExecutionException() {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.get("test-client-id#test-client-secret#test-invoker")).thenReturn("refresh-token");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token"))
                    .thenThrow(new ExecutionException("Test error", new Exception()));

            java.lang.reflect.Field field = AbstractAtlassianOAuthService.class.getDeclaredField("oAuthService");
            field.setAccessible(true);
            field.set(jiraService, mockOAuthService);

            // Act & Assert
            assertThrows(MustAuthorizeException.class,
                    () -> jiraService.getJiraAccessToken("test-account", "test-invoker"));
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
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.get("test-client-id#test-client-secret#test-invoker")).thenReturn("refresh-token");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        try {
            when(mockOAuthService.refreshAccessToken("refresh-token"))
                    .thenThrow(new IOException("Test error"));

            java.lang.reflect.Field field = AbstractAtlassianOAuthService.class.getDeclaredField("oAuthService");
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
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.get("test-client-id#test-client-secret#test-invoker")).thenReturn("refresh-token");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        try {
            when(mockOAuthService.refreshAccessToken("refresh-token"))
                    .thenThrow(new InterruptedException("Test error"));

            java.lang.reflect.Field field = AbstractAtlassianOAuthService.class.getDeclaredField("oAuthService");
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
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");
        when(requestContext.getInvokerId()).thenReturn("test-invoker");
        when(requestContext.invokeAsUser()).thenReturn(false);
        String expectedKey = "test-client-id#test-client-secret#test-invoker";
        when(sessionManager.get(expectedKey)).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);

            java.lang.reflect.Field field = AbstractAtlassianOAuthService.class.getDeclaredField("oAuthService");
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
        when(jiraAttributes.getEffectiveClientId()).thenReturn(null);
        when(jiraAttributes.getClientSecret()).thenReturn(null);
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn(null);
        when(sessionManager.get("null#null#test-invoker-id")).thenReturn("refresh-token");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("");
        when(mockOAuthService.getApiSecret()).thenReturn("");

        try {
            when(mockOAuthService.refreshAccessToken("refresh-token"))
                    .thenThrow(new ExecutionException("Test error", new Exception()));

            java.lang.reflect.Field field = AbstractAtlassianOAuthService.class.getDeclaredField("oAuthService");
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
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.get("test-client-id#test-client-secret#test-invoker-id"))
                .thenReturn("refresh-token");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        try {
            when(mockOAuthService.refreshAccessToken("refresh-token"))
                    .thenThrow(new RuntimeException("Test error"));

            java.lang.reflect.Field field = AbstractAtlassianOAuthService.class.getDeclaredField("oAuthService");
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
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.get("test-client-id#test-client-secret#test-invoker-id"))
                .thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token-123");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);

            java.lang.reflect.Field field = AbstractAtlassianOAuthService.class.getDeclaredField("oAuthService");
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
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");
        when(sessionManager.get("test-client-id#test-client-secret#invoker-456")).thenReturn("refresh-token");

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn("new-refresh-token");
        when(accessToken.getAccessToken()).thenReturn("access-token-456");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        try {
            when(mockOAuthService.refreshAccessToken("refresh-token")).thenReturn(accessToken);

            java.lang.reflect.Field field = AbstractAtlassianOAuthService.class.getDeclaredField("oAuthService");
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

    @Test
    @DisplayName("Should throw MustAuthorizeException when refresh token is null (no-arg, invokeAsUser true)")
    void testGetJiraAccessToken_ThrowsMustAuthorizeException_WhenNoRefreshTokenAndInvokeAsUser() {
        // Arrange
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(sessionManager.get("test-account-id#test-invoker-id")).thenReturn(null);

        // Act & Assert - throws because refresh token is null, regardless of
        // invokeAsUser
        assertThrows(MustAuthorizeException.class, () -> jiraService.getJiraAccessToken());
    }

    @Test
    @DisplayName("Should throw MustAuthorizeException when refresh token is null (with params, invokeAsUser true)")
    void testGetJiraAccessToken_WithParams_ThrowsMustAuthorizeException_WhenNoRefreshTokenAndInvokeAsUser() {
        // Arrange
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(sessionManager.get("test-account#test-invoker")).thenReturn(null);

        // Act & Assert - throws because refresh token is null, regardless of
        // invokeAsUser
        assertThrows(MustAuthorizeException.class,
                () -> jiraService.getJiraAccessToken("test-account", "test-invoker"));
    }

    // ── NEW: getOauthService() rebuild behaviour (|| fix + callback URL change) ──

    @Test
    @DisplayName("Should rebuild OAuth service when only clientId changes")
    void testGetOauthService_Rebuilds_WhenOnlyClientIdChanges() {
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getEffectiveClientId()).thenReturn("original-client-id");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("original-client-secret");

        JiraService testService = new JiraService(requestContext, sessionManager, invoker,
                authorizationContext, jiraAttributes);
        OAuth20Service first = testService.getOauthService();
        assertNotNull(first);

        // Change only clientId — with the || fix this must trigger a rebuild
        when(jiraAttributes.getEffectiveClientId()).thenReturn("new-client-id");

        OAuth20Service second = testService.getOauthService();
        assertNotSame(first, second, "Should rebuild when clientId changes");
    }

    @Test
    @DisplayName("Should rebuild OAuth service when only clientSecret changes")
    void testGetOauthService_Rebuilds_WhenOnlyClientSecretChanges() {
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getEffectiveClientId()).thenReturn("client-id");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("original-secret");

        JiraService testService = new JiraService(requestContext, sessionManager, invoker,
                authorizationContext, jiraAttributes);
        OAuth20Service first = testService.getOauthService();
        assertNotNull(first);

        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("new-secret");

        OAuth20Service second = testService.getOauthService();
        assertNotSame(first, second, "Should rebuild when clientSecret changes");
    }

    @Test
    @DisplayName("Should not rebuild OAuth service when credentials and callback URL are unchanged")
    void testGetOauthService_DoesNotRebuild_WhenNothingChanges() {
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getEffectiveClientId()).thenReturn("client-id");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("client-secret");

        JiraService testService = new JiraService(requestContext, sessionManager, invoker,
                authorizationContext, jiraAttributes);
        OAuth20Service first = testService.getOauthService();
        OAuth20Service second = testService.getOauthService();

        assertSame(first, second, "Should not rebuild when nothing changes");
    }

    @Test
    @DisplayName("Should rebuild OAuth service when managed mode toggles (callback URL changes)")
    void testGetOauthService_Rebuilds_WhenCallbackUrlChanges() {
        // Start with managed=false (own credentials callback URL)
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.isManagedCredentialsEnabled()).thenReturn(false);
        when(jiraAttributes.getEffectiveClientId()).thenReturn("client-id");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("client-secret");

        JiraService testService = new JiraService(requestContext, sessionManager, invoker,
                authorizationContext, jiraAttributes);
        OAuth20Service first = testService.getOauthService();
        assertNotNull(first);

        // Toggle to managed=true — callback URL changes from invoker-specific to generic
        when(jiraAttributes.isManagedCredentialsEnabled()).thenReturn(true);

        OAuth20Service second = testService.getOauthService();
        assertNotSame(first, second, "Should rebuild when managed mode (and thus callback URL) changes");
    }

    // ── NEW: buildSecuredAuthorizationUrl() tests ──

    @Nested
    @DisplayName("buildSecuredAuthorizationUrl Tests")
    class BuildSecuredAuthorizationUrlTests {

        @BeforeEach
        void setUpOAuth() {
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
            when(jiraAttributes.getEffectiveClientId()).thenReturn("client-id");
            when(jiraAttributes.getEffectiveClientSecret()).thenReturn("client-secret");
        }

        @Test
        @DisplayName("Returns non-null URL for own-credentials mode")
        void testBuildSecuredAuthorizationUrl_OwnCredentials_ReturnsUrl() {
            when(jiraAttributes.isManagedCredentialsEnabled()).thenReturn(false);
            when(requestContext.getInvokerId()).thenReturn("test-invoker-id");

            JiraService testService = new JiraService(requestContext, sessionManager, invoker,
                    authorizationContext, jiraAttributes);

            String url = testService.buildSecuredAuthorizationUrl("account-123");

            assertNotNull(url);
            assertFalse(url.isBlank());
            assertTrue(url.startsWith("https://"), "Should be an HTTPS URL");
        }

        @Test
        @DisplayName("Returns non-null URL for managed (Quick Setup) mode")
        void testBuildSecuredAuthorizationUrl_ManagedMode_ReturnsUrl() {
            when(jiraAttributes.isManagedCredentialsEnabled()).thenReturn(true);
            when(requestContext.getInvokerId()).thenReturn("test-invoker-id");

            JiraService testService = new JiraService(requestContext, sessionManager, invoker,
                    authorizationContext, jiraAttributes);

            String url = testService.buildSecuredAuthorizationUrl("account-456");

            assertNotNull(url);
            assertFalse(url.isBlank());
            assertTrue(url.startsWith("https://"), "Should be an HTTPS URL");
        }

        @Test
        @DisplayName("Managed and own-credentials URLs differ (different state/callback)")
        void testBuildSecuredAuthorizationUrl_ManagedVsOwn_ProduceDifferentUrls() {
            when(requestContext.getInvokerId()).thenReturn("test-invoker-id");

            when(jiraAttributes.isManagedCredentialsEnabled()).thenReturn(false);
            JiraService ownService = new JiraService(requestContext, sessionManager, invoker,
                    authorizationContext, jiraAttributes);
            String ownUrl = ownService.buildSecuredAuthorizationUrl("account-789");

            when(jiraAttributes.isManagedCredentialsEnabled()).thenReturn(true);
            JiraService managedService = new JiraService(requestContext, sessionManager, invoker,
                    authorizationContext, jiraAttributes);
            String managedUrl = managedService.buildSecuredAuthorizationUrl("account-789");

            assertNotEquals(ownUrl, managedUrl,
                    "Managed and own-credential authorization URLs must differ");
        }
    }

    @Test
    @DisplayName("Should throw MustAuthorizeException when user token is missing (Fallback to app-wide token is disabled)")
    void testGetJiraAccessToken_NoFallbackToAppWideToken() throws Exception {
        // Arrange
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getEffectiveClientId()).thenReturn("test-client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("test-client-secret");

        when(requestContext.invokeAsUser()).thenReturn(true);

        String userKey = "test-account#test-invoker";
        // App key is no longer used for fallback in user context
        String appKey = "test-client-id#test-invoker";

        when(sessionManager.get(userKey)).thenReturn(null);
        // Even if app token exists, it won't be picked up by the current logic for
        // users
        when(sessionManager.get(appKey)).thenReturn("app-refresh-token");

        OAuth20Service mockOAuthService = mock(OAuth20Service.class);
        when(mockOAuthService.getApiKey()).thenReturn("test-client-id");
        when(mockOAuthService.getApiSecret()).thenReturn("test-client-secret");

        jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        java.lang.reflect.Field field = AbstractAtlassianOAuthService.class.getDeclaredField("oAuthService");
        field.setAccessible(true);
        field.set(jiraService, mockOAuthService);

        // Act & Assert
        assertThrows(MustAuthorizeException.class,
                () -> jiraService.getJiraAccessToken("test-account", "test-invoker"));
        verify(sessionManager, atLeastOnce()).get(userKey);
        verify(sessionManager, never()).get(appKey);
    }
}
