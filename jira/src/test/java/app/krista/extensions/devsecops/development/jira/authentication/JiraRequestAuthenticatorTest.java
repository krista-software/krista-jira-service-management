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

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.ksdk.context.AuthorizationContext;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JiraRequestAuthenticator Tests")
class JiraRequestAuthenticatorTest {

    private JiraRequestAuthenticator authenticator;
    private JiraService jiraService;
    private AuthorizationContext authorizationContext;
    private OAuth20Service oAuth20Service;

    @BeforeEach
    void setUp() {
        jiraService = mock(JiraService.class);
        authorizationContext = mock(AuthorizationContext.class);
        oAuth20Service = mock(OAuth20Service.class);

        when(jiraService.getOauthService()).thenReturn(oAuth20Service);

        authenticator = new JiraRequestAuthenticator(jiraService, authorizationContext);
    }

    @Test
    @DisplayName("Should return null for getScheme")
    void testGetScheme_ReturnsNull() {
        // Act
        String result = authenticator.getScheme();

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return empty set for getSupportedProtocols")
    void testGetSupportedProtocols_ReturnsEmptySet() {
        // Act
        var result = authenticator.getSupportedProtocols();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(Collections.emptySet(), result);
    }

    @Test
    @DisplayName("Should return state from callback path")
    void testGetAuthenticatedAccountId_ReturnsStateFromCallback() throws IOException {
        // Arrange
        HttpRequest httpRequest = mock(HttpRequest.class);
        URI uri = URI.create("http://localhost/callback");
        when(httpRequest.getUri()).thenReturn(uri);

        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
        queryParams.put("state", List.of("test-state-123"));
        when(httpRequest.getQueryParameters()).thenReturn(queryParams);

        // Act
        String result = authenticator.getAuthenticatedAccountId(httpRequest);

        // Assert
        assertEquals("test-state-123", result);
        verify(httpRequest).bufferBody();
    }

    @Test
    @DisplayName("Should return null when path is not callback")
    void testGetAuthenticatedAccountId_ReturnsNull_WhenNotCallbackPath() {
        // Arrange
        HttpRequest httpRequest = mock(HttpRequest.class);
        URI uri = URI.create("http://localhost/other-path");
        when(httpRequest.getUri()).thenReturn(uri);

        // Act
        String result = authenticator.getAuthenticatedAccountId(httpRequest);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when query parameters are null")
    void testGetAuthenticatedAccountId_ReturnsNull_WhenQueryParamsNull() throws IOException {
        // Arrange
        HttpRequest httpRequest = mock(HttpRequest.class);
        URI uri = URI.create("http://localhost/callback");
        when(httpRequest.getUri()).thenReturn(uri);
        when(httpRequest.getQueryParameters()).thenReturn(null);

        // Act
        String result = authenticator.getAuthenticatedAccountId(httpRequest);

        // Assert
        assertNull(result);
        verify(httpRequest).bufferBody();
    }

    @Test
    @DisplayName("Should return null when state parameter is missing")
    void testGetAuthenticatedAccountId_ReturnsNull_WhenStateMissing() throws IOException {
        // Arrange
        HttpRequest httpRequest = mock(HttpRequest.class);
        URI uri = URI.create("http://localhost/callback");
        when(httpRequest.getUri()).thenReturn(uri);

        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
        when(httpRequest.getQueryParameters()).thenReturn(queryParams);

        // Act
        String result = authenticator.getAuthenticatedAccountId(httpRequest);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when state value is null")
    void testGetAuthenticatedAccountId_ReturnsNull_WhenStateValueNull() throws IOException {
        // Arrange
        HttpRequest httpRequest = mock(HttpRequest.class);
        URI uri = URI.create("http://localhost/callback");
        when(httpRequest.getUri()).thenReturn(uri);

        MultivaluedMap<String, String> queryParams = new MultivaluedHashMap<>();
        queryParams.put("state", Collections.singletonList(null));
        when(httpRequest.getQueryParameters()).thenReturn(queryParams);

        // Act
        String result = authenticator.getAuthenticatedAccountId(httpRequest);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when IOException occurs")
    void testGetAuthenticatedAccountId_ReturnsNull_OnIOException() throws IOException {
        // Arrange
        HttpRequest httpRequest = mock(HttpRequest.class);
        URI uri = URI.create("http://localhost/callback");
        when(httpRequest.getUri()).thenReturn(uri);
        doThrow(new IOException("Test exception")).when(httpRequest).bufferBody();

        // Act
        String result = authenticator.getAuthenticatedAccountId(httpRequest);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null when RuntimeException occurs")
    void testGetAuthenticatedAccountId_ReturnsNull_OnRuntimeException() {
        // Arrange
        HttpRequest httpRequest = mock(HttpRequest.class);
        when(httpRequest.getUri()).thenThrow(new RuntimeException("Test exception"));

        // Act
        String result = authenticator.getAuthenticatedAccountId(httpRequest);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return false for setServiceAuthorization")
    void testSetServiceAuthorization_ReturnsFalse() {
        // Act
        boolean result = authenticator.setServiceAuthorization("test");

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return empty map for getAttributeFields")
    void testGetAttributeFields_ReturnsEmptyMap() {
        // Act
        var result = authenticator.getAttributeFields();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return null for getMustAuthenticateResponse with ProtoRequest")
    void testGetMustAuthenticateResponse_WithProtoRequest_ReturnsNull() {
        // Arrange
        MustAuthenticateException exception = new MustAuthenticateException("Test");
        ProtoRequest protoRequest = mock(ProtoRequest.class);

        // Act
        var result = authenticator.getMustAuthenticateResponse(exception, protoRequest);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null for getMustAuthenticateResponse without ProtoRequest")
    void testGetMustAuthenticateResponse_WithoutProtoRequest_ReturnsNull() {
        // Arrange
        MustAuthenticateException exception = new MustAuthenticateException("Test");

        // Act
        var result = authenticator.getMustAuthenticateResponse(exception);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should return null for getMustAuthorizeResponse with ProtoRequest")
    void testGetMustAuthorizeResponse_WithProtoRequest_ReturnsNull() {
        // Arrange
        MustAuthorizeException exception = new MustAuthorizeException("Test");
        ProtoRequest protoRequest = mock(ProtoRequest.class);

        // Act
        var result = authenticator.getMustAuthorizeResponse(exception, protoRequest);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should have Service annotation")
    void testHasServiceAnnotation() {
        // Assert
        assertTrue(JiraRequestAuthenticator.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }

    @Test
    @DisplayName("Should be final class")
    void testIsFinalClass() {
        // Assert
        assertTrue(java.lang.reflect.Modifier.isFinal(JiraRequestAuthenticator.class.getModifiers()));
    }

    @Test
    @DisplayName("Should have Inject annotation on constructor")
    void testHasInjectAnnotation() throws NoSuchMethodException {
        // Act
        var constructor = JiraRequestAuthenticator.class.getConstructor(JiraService.class, AuthorizationContext.class);

        // Assert
        assertTrue(constructor.isAnnotationPresent(javax.inject.Inject.class));
    }

    @Test
    @DisplayName("Should implement RequestAuthenticator interface")
    void testImplementsRequestAuthenticator() {
        // Assert
        assertTrue(RequestAuthenticator.class.isAssignableFrom(JiraRequestAuthenticator.class));
    }
}