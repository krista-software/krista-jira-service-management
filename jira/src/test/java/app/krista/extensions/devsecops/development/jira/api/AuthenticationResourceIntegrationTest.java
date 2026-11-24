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

package app.krista.extensions.devsecops.development.jira.api;

import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.extension.authentication.AbstractAtlassianAuthResource;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthenticationResource Integration Tests")
class AuthenticationResourceIntegrationTest {

    private AuthenticationResource authenticationResource;
    private SessionManager sessionManager;
    private JiraService jiraService;
    private RequestContext requestContext;
    private AuthorizationContext authorizationContext;
    private AuthorizationListener authorizationListener;
    private EventHandler eventHandler;
    private OAuth20Service oAuth20Service;
    private JiraAttributes jiraAttributes;

    @BeforeEach
    void setUp() {
        sessionManager = mock(SessionManager.class);
        jiraService = mock(JiraService.class);
        requestContext = mock(RequestContext.class);
        authorizationContext = mock(AuthorizationContext.class);
        authorizationListener = mock(AuthorizationListener.class);
        eventHandler = mock(EventHandler.class);
        oAuth20Service = mock(OAuth20Service.class);
        jiraAttributes = mock(JiraAttributes.class);

        when(jiraService.getOauthService()).thenReturn(oAuth20Service);
        when(oAuth20Service.getApiKey()).thenReturn("client123");
        when(oAuth20Service.getApiSecret()).thenReturn("secret456");

        authenticationResource = new AuthenticationResource(
                sessionManager,
                jiraService,
                requestContext,
                authorizationContext,
                authorizationListener,
                eventHandler,
                jiraAttributes);
    }

    @Test
    @DisplayName("Should successfully handle OAuth callback with app-wide token storage")
    void testGetCallBack_Success_AppWide() throws IOException, ExecutionException, InterruptedException {
        // Arrange — no accountId in state → app-wide storage
        String code = "test-auth-code";
        String invokerId = "test-invoker-id";
        String refreshToken = "test-refresh-token";

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn(refreshToken);
        when(oAuth20Service.getAccessToken(code)).thenReturn(accessToken);
        when(requestContext.getInvokerId()).thenReturn(invokerId);

        // Act — null state → app-wide key
        String result = authenticationResource.getCallBack(code, null);

        // Assert
        assertEquals(AbstractAtlassianAuthResource.CALLBACK_SUCCESS_HTML, result);
        verify(sessionManager).store("client123#secret456#" + invokerId, refreshToken);
        verify(authorizationListener).authorized();
    }

    @Test
    @DisplayName("Should store token under user-specific key when state contains accountId")
    void testGetCallBack_Success_PerUser() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-auth-code";
        String state = "test-account-id";
        String invokerId = "test-invoker-id";
        String refreshToken = "test-refresh-token";

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn(refreshToken);
        when(oAuth20Service.getAccessToken(code)).thenReturn(accessToken);
        when(requestContext.getInvokerId()).thenReturn(invokerId);

        // Act — state present → user-specific key
        String result = authenticationResource.getCallBack(code, state);

        // Assert
        assertEquals(AbstractAtlassianAuthResource.CALLBACK_SUCCESS_HTML, result);
        verify(sessionManager).store(state + "#" + invokerId, refreshToken);
        verify(authorizationListener).authorized();
    }

    @Test
    @DisplayName("Should return HTML error page when no authorization code received")
    void testGetCallBack_NullCode() {
        // Act
        String result = authenticationResource.getCallBack(null, "test-state");

        // Assert
        assertTrue(result.contains("Authentication Failed"),
                "Expected HTML error page but got: " + result.substring(0, Math.min(200, result.length())));
        verifyNoInteractions(oAuth20Service);
    }

    @Test
    @DisplayName("Should return HTML error page when refresh token is missing")
    void testGetCallBack_NoRefreshToken() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-auth-code";
        String invokerId = "test-invoker-id";

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn(null);
        when(oAuth20Service.getAccessToken(code)).thenReturn(accessToken);
        when(requestContext.getInvokerId()).thenReturn(invokerId);

        // Act
        String result = authenticationResource.getCallBack(code, null);

        // Assert
        assertTrue(result.contains("Authentication Failed"),
                "Expected HTML error page but got: " + result.substring(0, Math.min(200, result.length())));
        verify(authorizationListener, never()).authorized();
    }

    @Test
    @DisplayName("Should return HTML error page when OAuth service is null")
    void testGetCallBack_NullOAuthService() {
        // Arrange
        when(jiraService.getOauthService()).thenReturn(null);
        when(requestContext.getInvokerId()).thenReturn("test-invoker");

        // Act
        String result = authenticationResource.getCallBack("test-code", "test-state");

        // Assert
        assertTrue(result.contains("Authentication Failed"),
                "Expected HTML error page but got: " + result.substring(0, Math.min(200, result.length())));
    }

    @Test
    @DisplayName("Should return HTML error page when an exception occurs during callback")
    void testGetCallBack_ExceptionReturnsErrorPage() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-auth-code";

        when(oAuth20Service.getAccessToken(code)).thenThrow(new RuntimeException("Token exchange failed"));
        when(requestContext.getInvokerId()).thenReturn("test-invoker");

        // Act
        String result = authenticationResource.getCallBack(code, "test-state");

        // Assert
        assertTrue(result.contains("Authentication Failed"),
                "Expected HTML error page but got: " + result.substring(0, Math.min(200, result.length())));
        assertTrue(result.contains("Token exchange failed"),
                "Expected error message in page but got: " + result.substring(0, Math.min(200, result.length())));
    }

    @Test
    @DisplayName("Should not fail when authorizationListener throws")
    void testGetCallBack_ListenerFailureIgnored() throws Exception {
        // Arrange
        String code = "test-auth-code";
        String invokerId = "test-invoker-id";
        String refreshToken = "test-refresh-token";

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn(refreshToken);
        when(oAuth20Service.getAccessToken(code)).thenReturn(accessToken);
        when(requestContext.getInvokerId()).thenReturn(invokerId);
        doThrow(new RuntimeException("listener boom")).when(authorizationListener).authorized();

        // Act — listener failure should NOT prevent success response
        String result = authenticationResource.getCallBack(code, null);

        // Assert — still returns success HTML
        assertEquals(AbstractAtlassianAuthResource.CALLBACK_SUCCESS_HTML, result);
        verify(sessionManager).store("client123#secret456#" + invokerId, refreshToken);
    }
}
