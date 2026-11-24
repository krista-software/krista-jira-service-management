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

package app.krista.extensions.devsecops.development.jira;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Integration tests for validateOAuthCredentialsFormat method in JiraAttributes
 * Tests the OAuth credentials format validation logic
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JiraAttributes.validateOAuthCredentialsFormat - Integration Tests")
class JiraAttributesValidateOAuthFormatIntegrationTest {

    @Mock
    private Invoker invoker;

    @Mock
    private RequestContext requestContext;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private AuthorizationContext authorizationContext;

    private JiraAttributes jiraAttributes;

    @BeforeEach
    void setUp() {
        Map<String, Object> defaultAttributes = new HashMap<>();
        when(invoker.getAttributes()).thenReturn(defaultAttributes);
        jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);
    }

    @Nested
    @DisplayName("Valid Client ID Format Tests")
    class ValidClientIdFormatTests {

        @Test
        @DisplayName("Should accept Client ID with alphanumeric characters only")
        void testValidateOAuthFormat_AlphanumericClientId() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "abc123XYZ789");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64)); // 64 chars - typical Atlassian length

            // Create a fresh instance and set attributes
            JiraAttributes testAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);
            testAttributes.setAttributes(attributes);

            // Mock testConnection to avoid actual connection
            JiraAttributes spyAttributes = spy(testAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert - should not throw exception
            // This will execute validateOAuthCredentialsFormat internally
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));

            // Verify testConnection was called (proves validateAttributes executed fully)
            verify(spyAttributes, times(1)).testConnection(attributes);
        }

        @Test
        @DisplayName("Should accept Client ID with hyphens")
        void testValidateOAuthFormat_ClientIdWithHyphens() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client-id-with-hyphens-123");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client ID with underscores")
        void testValidateOAuthFormat_ClientIdWithUnderscores() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id_with_underscores_123");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client ID with mixed valid characters")
        void testValidateOAuthFormat_ClientIdMixedValidChars() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "Client_ID-123-ABC_xyz");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client ID with uppercase letters only")
        void testValidateOAuthFormat_ClientIdUppercaseOnly() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client ID with lowercase letters only")
        void testValidateOAuthFormat_ClientIdLowercaseOnly() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "abcdefghijklmnopqrstuvwxyz");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client ID with numbers only")
        void testValidateOAuthFormat_ClientIdNumbersOnly() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "1234567890");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }
    }

    @Nested
    @DisplayName("Invalid Client ID Format Tests")
    class InvalidClientIdFormatTests {

        @Test
        @DisplayName("Should reject Client ID with spaces")
        void testValidateOAuthFormat_ClientIdWithSpaces() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client id with spaces");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with special characters (@)")
        void testValidateOAuthFormat_ClientIdWithAtSymbol() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client@id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with special characters (#)")
        void testValidateOAuthFormat_ClientIdWithHashSymbol() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client#id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with special characters ($)")
        void testValidateOAuthFormat_ClientIdWithDollarSign() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client$id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with special characters (%)")
        void testValidateOAuthFormat_ClientIdWithPercentSign() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client%id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with special characters (&)")
        void testValidateOAuthFormat_ClientIdWithAmpersand() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client&id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with special characters (*)")
        void testValidateOAuthFormat_ClientIdWithAsterisk() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client*id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with special characters (.)")
        void testValidateOAuthFormat_ClientIdWithDot() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client.id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with special characters (/)")
        void testValidateOAuthFormat_ClientIdWithSlash() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client/id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with special characters (+)")
        void testValidateOAuthFormat_ClientIdWithPlus() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client+id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with special characters (=)")
        void testValidateOAuthFormat_ClientIdWithEquals() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client=id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }
    }

    @Nested
    @DisplayName("Client Secret Length Tests")
    class ClientSecretLengthTests {

        @Test
        @DisplayName("Should accept Client Secret with 64 characters (typical Atlassian length)")
        void testValidateOAuthFormat_ClientSecret64Chars() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "valid_client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert - should not throw exception
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client Secret with 128 characters")
        void testValidateOAuthFormat_ClientSecret128Chars() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "valid_client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(128));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client Secret with exactly 32 characters (minimum)")
        void testValidateOAuthFormat_ClientSecret32Chars() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "valid_client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(32));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert - should not throw exception (just logs warning)
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept but warn for Client Secret with 31 characters (below minimum)")
        void testValidateOAuthFormat_ClientSecret31Chars() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "valid_client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(31));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert - should not throw exception (just logs warning)
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept but warn for Client Secret with 10 characters")
        void testValidateOAuthFormat_ClientSecret10Chars() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "valid_client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(10));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert - should not throw exception (just logs warning)
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept but warn for Client Secret with 1 character")
        void testValidateOAuthFormat_ClientSecret1Char() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "valid_client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a");

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert - should not throw exception (just logs warning)
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }
    }

    @Nested
    @DisplayName("Real-World Atlassian Client ID Patterns")
    class RealWorldClientIdTests {

        @Test
        @DisplayName("Should accept typical Atlassian Client ID format")
        void testValidateOAuthFormat_TypicalAtlassianClientId() {
            // Arrange - typical Atlassian OAuth 2.0 Client ID format
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "aBcDeFgHiJkLmNoPqRsTuVwXyZ123456");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client ID with UUID-like format")
        void testValidateOAuthFormat_UuidLikeClientId() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client ID with prefix pattern")
        void testValidateOAuthFormat_PrefixPatternClientId() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "oauth2_client_12345");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should reject empty Client ID - throws no credentials error")
        void testValidateOAuthFormat_EmptyClientId() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));
            attributes.put(JiraAttributes.CREDENTIALS_MODE, "own");

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            // Empty string is treated as not present, so it throws "no credentials" error
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertTrue(exception.getMessage().contains("Please choose ONE authentication method"));
            assertTrue(exception.getMessage().contains("For OAuth 2.0: Provide Client Id and Client Secret"));
        }

        @Test
        @DisplayName("Should accept very long Client ID (256 characters)")
        void testValidateOAuthFormat_VeryLongClientId() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "a".repeat(256));
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client ID with single character")
        void testValidateOAuthFormat_SingleCharClientId() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "a");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should reject Client ID with only special characters")
        void testValidateOAuthFormat_OnlySpecialCharsClientId() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "!@#$%^&*()");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should reject Client ID with Unicode characters")
        void testValidateOAuthFormat_UnicodeClientId() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id_with_émojis_😀");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.",
                exception.getMessage()
            );
        }
    }

    @Nested
    @DisplayName("validateAttributes Integration Tests")
    class ValidateAttributesIntegrationTests {

        @Test
        @DisplayName("Should throw exception when no credentials are provided")
        void testValidateAttributes_NoCredentials() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.CREDENTIALS_MODE, "own");

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertTrue(exception.getMessage().contains("Please choose ONE authentication method"));
            assertTrue(exception.getMessage().contains("For OAuth 2.0: Provide Client Id and Client Secret"));
            assertTrue(exception.getMessage().contains("For Basic Auth: Provide Admin Auth Token, Admin User Name, and Host Url"));
        }

        @Test
        @DisplayName("Should throw exception for invalid email format in userName")
        void testValidateAttributes_InvalidEmailFormat() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "invalid-email");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token_123");

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "User name is not a valid email address",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should accept valid email format in userName")
        void testValidateAttributes_ValidEmailFormat() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "user@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token_123");

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act & Assert
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should validate OAuth credentials and call testConnection")
        void testValidateAttributes_OAuthCredentialsFlow() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "valid_client_id_123");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));

            // Assert - verify testConnection was called
            verify(spyAttributes, times(1)).testConnection(attributes);
        }

        @Test
        @DisplayName("Should validate basic credentials and call testConnection")
        void testValidateAttributes_BasicCredentialsFlow() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "user@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token_123");

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Act
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));

            // Assert - verify testConnection was called
            verify(spyAttributes, times(1)).testConnection(attributes);
        }
    }

    @Nested
    @DisplayName("Partial Credentials Detection Integration Tests")
    class PartialCredentialsIntegrationTests {

        @Test
        @DisplayName("Should detect partial basic credentials - only userName")
        void testPartialBasicCredentials_OnlyUserName() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.USER_NAME, "user@example.com");

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            assertTrue(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should detect partial basic credentials - only authToken")
        void testPartialBasicCredentials_OnlyAuthToken() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            assertTrue(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should detect partial basic credentials - only hostUrl")
        void testPartialBasicCredentials_OnlyHostUrl() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            assertTrue(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should detect partial basic credentials - userName and authToken missing hostUrl")
        void testPartialBasicCredentials_MissingHostUrl() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.USER_NAME, "user@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            assertTrue(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should not detect partial when all basic credentials present")
        void testPartialBasicCredentials_AllPresent() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "user@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            assertFalse(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should not detect partial when no basic credentials present")
        void testPartialBasicCredentials_NonePresent() {
            // Arrange
            Map<String, Object> attributes = new HashMap<>();

            jiraAttributes.setAttributes(attributes);

            // Act & Assert
            assertFalse(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }
    }
}

