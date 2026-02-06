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

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JiraApi20 Tests")
class JiraApi20Test {

    @Test
    @DisplayName("Should return singleton instance")
    void testInstance_ReturnsSingleton() {
        // Act
        JiraApi20 instance1 = JiraApi20.instance();
        JiraApi20 instance2 = JiraApi20.instance();

        // Assert
        assertNotNull(instance1);
        assertNotNull(instance2);
        assertSame(instance1, instance2, "Should return the same singleton instance");
    }

    @Test
    @DisplayName("Should extend DefaultApi20")
    void testExtendsDefaultApi20() {
        // Act
        JiraApi20 instance = JiraApi20.instance();

        // Assert
        assertTrue(instance instanceof DefaultApi20);
    }

    @Test
    @DisplayName("Should return correct access token endpoint")
    void testGetAccessTokenEndpoint() {
        // Arrange
        JiraApi20 instance = JiraApi20.instance();

        // Act
        String endpoint = instance.getAccessTokenEndpoint();

        // Assert
        assertNotNull(endpoint);
        assertEquals("https://auth.atlassian.com/oauth/token", endpoint);
    }

    @Test
    @DisplayName("Should return correct authorization base URL")
    void testGetAuthorizationBaseUrl() {
        // Arrange
        JiraApi20 instance = JiraApi20.instance();

        // Act
        String baseUrl = instance.getAuthorizationBaseUrl();

        // Assert
        assertNotNull(baseUrl);
        assertEquals("https://auth.atlassian.com/authorize", baseUrl);
    }

    @Test
    @DisplayName("Should return OAuth2AccessTokenJsonExtractor instance")
    void testGetAccessTokenExtractor() {
        // Arrange
        JiraApi20 instance = JiraApi20.instance();

        // Act
        OAuth2AccessTokenJsonExtractor extractor = instance.getAccessTokenExtractor();

        // Assert
        assertNotNull(extractor);
        assertSame(OAuth2AccessTokenJsonExtractor.instance(), extractor);
    }

    @Test
    @DisplayName("Should have consistent endpoint URLs across multiple calls")
    void testEndpointConsistency() {
        // Arrange
        JiraApi20 instance = JiraApi20.instance();

        // Act
        String endpoint1 = instance.getAccessTokenEndpoint();
        String endpoint2 = instance.getAccessTokenEndpoint();
        String authUrl1 = instance.getAuthorizationBaseUrl();
        String authUrl2 = instance.getAuthorizationBaseUrl();

        // Assert
        assertEquals(endpoint1, endpoint2);
        assertEquals(authUrl1, authUrl2);
    }

    @Test
    @DisplayName("Should be final class")
    void testIsFinalClass() {
        // Assert
        assertTrue(java.lang.reflect.Modifier.isFinal(JiraApi20.class.getModifiers()));
    }

    @Test
    @DisplayName("Should have accessible constructor for singleton pattern")
    void testHasAccessibleConstructor() throws NoSuchMethodException {
        // Act
        var constructor = JiraApi20.class.getDeclaredConstructor();

        // Assert - Constructor exists and can be accessed via instance() method
        assertNotNull(constructor);
        assertNotNull(JiraApi20.instance());
    }

    @Test
    @DisplayName("Should return HTTPS URLs for security")
    void testUsesHTTPS() {
        // Arrange
        JiraApi20 instance = JiraApi20.instance();

        // Act & Assert
        assertTrue(instance.getAccessTokenEndpoint().startsWith("https://"));
        assertTrue(instance.getAuthorizationBaseUrl().startsWith("https://"));
    }

    @Test
    @DisplayName("Should use Atlassian auth domain")
    void testUsesAtlassianDomain() {
        // Arrange
        JiraApi20 instance = JiraApi20.instance();

        // Act & Assert
        assertTrue(instance.getAccessTokenEndpoint().contains("auth.atlassian.com"));
        assertTrue(instance.getAuthorizationBaseUrl().contains("auth.atlassian.com"));
    }

    @Test
    @DisplayName("Should have correct OAuth paths")
    void testOAuthPaths() {
        // Arrange
        JiraApi20 instance = JiraApi20.instance();

        // Act & Assert
        assertTrue(instance.getAccessTokenEndpoint().endsWith("/oauth/token"));
        assertTrue(instance.getAuthorizationBaseUrl().endsWith("/authorize"));
    }
}