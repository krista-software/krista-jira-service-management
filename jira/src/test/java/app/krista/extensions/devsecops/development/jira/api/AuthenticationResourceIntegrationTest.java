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

import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.base.FreeForm;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

    @BeforeEach
    void setUp() {
        sessionManager = mock(SessionManager.class);
        jiraService = mock(JiraService.class);
        requestContext = mock(RequestContext.class);
        authorizationContext = mock(AuthorizationContext.class);
        authorizationListener = mock(AuthorizationListener.class);
        eventHandler = mock(EventHandler.class);
        oAuth20Service = mock(OAuth20Service.class);

        when(jiraService.getOauthService()).thenReturn(oAuth20Service);

        authenticationResource = new AuthenticationResource(
                sessionManager,
                jiraService,
                requestContext,
                authorizationContext,
                authorizationListener,
                eventHandler
        );
    }

    @Test
    @DisplayName("Should successfully handle OAuth callback when authenticated")
    void testGetCallBack_Success_Authenticated() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-auth-code";
        String state = "test-state";
        String invokerId = "test-invoker-id";
        String refreshToken = "test-refresh-token";

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn(refreshToken);
        when(oAuth20Service.getAccessToken(code)).thenReturn(accessToken);
        when(requestContext.getInvokerId()).thenReturn(invokerId);
        when(authorizationContext.isAuthenticated()).thenReturn(true);

        // Act
        String result = authenticationResource.getCallBack(code, state);

        // Assert
        assertEquals("Authentication successful. Please close the window and proceed ahead.", result);
        verify(sessionManager).storeRefToken(state + "#" + invokerId, refreshToken);
        verify(authorizationListener).authorized();
    }

    @Test
    @DisplayName("Should return unsuccessful message when not authenticated")
    void testGetCallBack_NotAuthenticated() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-auth-code";
        String state = "test-state";
        String invokerId = "test-invoker-id";
        String refreshToken = "test-refresh-token";

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn(refreshToken);
        when(oAuth20Service.getAccessToken(code)).thenReturn(accessToken);
        when(requestContext.getInvokerId()).thenReturn(invokerId);
        when(authorizationContext.isAuthenticated()).thenReturn(false);

        // Act
        String result = authenticationResource.getCallBack(code, state);

        // Assert
        assertEquals("Authentication is unsuccessful", result);
        verify(sessionManager).storeRefToken(state + "#" + invokerId, refreshToken);
        verify(authorizationListener, never()).authorized();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when requestContext throws exception")
    void testGetCallBack_ThrowsException() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-auth-code";
        String state = "test-state";
        String refreshToken = "test-refresh-token";

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn(refreshToken);
        when(oAuth20Service.getAccessToken(code)).thenReturn(accessToken);
        when(requestContext.getInvokerId()).thenThrow(new RuntimeException("Context error"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authenticationResource.getCallBack(code, state)
        );

        assertTrue(exception.getMessage().contains("Error occurred during callback"));
        assertNotNull(exception.getCause());
    }

    @Test
    @DisplayName("Should get auth token response successfully")
    void testGetAuthTokenResponse_Success() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-code";
        String refreshToken = "test-refresh-token";

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn(refreshToken);
        when(oAuth20Service.getAccessToken(code)).thenReturn(accessToken);

        // Act
        Map<String, Object> result = authenticationResource.getAuthTokenResponse(code);

        // Assert
        assertNotNull(result);
        assertEquals(refreshToken, result.get(JiraConstants.REFRESH_TOKEN));
        verify(oAuth20Service).getAccessToken(code);
    }

    @Test
    @DisplayName("Should handle null code in getAuthTokenResponse")
    void testGetAuthTokenResponse_NullCode() {
        // Act & Assert - Should throw NullPointerException due to Objects.requireNonNull
        assertThrows(NullPointerException.class, () ->
                authenticationResource.getAuthTokenResponse(null)
        );
    }

    @Test
    @DisplayName("Should handle IOException in getAuthTokenResponse")
    void testGetAuthTokenResponse_IOException() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-code";
        when(oAuth20Service.getAccessToken(code)).thenThrow(new IOException("Network error"));

        // Act
        Map<String, Object> result = authenticationResource.getAuthTokenResponse(code);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle ExecutionException in getAuthTokenResponse")
    void testGetAuthTokenResponse_ExecutionException() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-code";
        when(oAuth20Service.getAccessToken(code)).thenThrow(new ExecutionException("Execution error", new RuntimeException()));

        // Act
        Map<String, Object> result = authenticationResource.getAuthTokenResponse(code);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should handle InterruptedException in getAuthTokenResponse")
    void testGetAuthTokenResponse_InterruptedException() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-code";
        when(oAuth20Service.getAccessToken(code)).thenThrow(new InterruptedException("Thread interrupted"));

        // Act
        Map<String, Object> result = authenticationResource.getAuthTokenResponse(code);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertTrue(Thread.interrupted()); // Verify interrupt flag was set
    }

    @Test
    @DisplayName("Should handle callback with special characters in state")
    void testGetCallBack_SpecialCharactersInState() throws IOException, ExecutionException, InterruptedException {
        // Arrange
        String code = "test-code";
        String state = "state-with-special-chars-!@#$%";
        String invokerId = "invoker-123";
        String refreshToken = "refresh-token";

        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        when(accessToken.getRefreshToken()).thenReturn(refreshToken);
        when(oAuth20Service.getAccessToken(code)).thenReturn(accessToken);
        when(requestContext.getInvokerId()).thenReturn(invokerId);
        when(authorizationContext.isAuthenticated()).thenReturn(true);

        // Act
        String result = authenticationResource.getCallBack(code, state);

        // Assert
        assertEquals("Authentication successful. Please close the window and proceed ahead.", result);
        verify(sessionManager).storeRefToken(state + "#" + invokerId, refreshToken);
    }

}