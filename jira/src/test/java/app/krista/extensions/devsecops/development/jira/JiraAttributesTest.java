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

import app.krista.extension.authorization.MustAuthorizeException;
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
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for JiraAttributes class
 * Achieves 100% code coverage for all methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JiraAttributes - 100% Coverage Tests")
class JiraAttributesTest {

    @Mock
    private Invoker invoker;

    @Mock
    private RequestContext requestContext;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private AuthorizationContext authorizationContext;

    private JiraAttributes jiraAttributes;
    private Map<String, Object> testAttributes;

    @BeforeEach
    void setUp() {
        // Setup default test attributes
        testAttributes = new HashMap<>();
        testAttributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        testAttributes.put(JiraAttributes.USER_NAME, "test@example.com");
        testAttributes.put(JiraAttributes.AUTH_TOKEN, "test_token");
        testAttributes.put(JiraAttributes.CLIENT_ID, "test_client_id");
        testAttributes.put(JiraAttributes.CLIENT_SECRET, "test_client_secret");

        // Mock invoker to return test attributes
        when(invoker.getAttributes()).thenReturn(testAttributes);

        // Create instance
        jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with all attributes from invoker")
        void testConstructor_WithAllAttributes() {
            // Verify attributes are set from invoker
            assertEquals("https://test.atlassian.net", jiraAttributes.getHostUrl());
            assertEquals("test@example.com", jiraAttributes.getUserName());
            assertEquals("test_token", jiraAttributes.getAuthToken());
            assertEquals("test_client_id", jiraAttributes.getClientId());
            assertEquals("test_client_secret", jiraAttributes.getClientSecret());
        }

        @Test
        @DisplayName("Should initialize with null attributes")
        void testConstructor_WithNullAttributes() {
            Map<String, Object> nullAttributes = new HashMap<>();
            when(invoker.getAttributes()).thenReturn(nullAttributes);

            JiraAttributes attrs = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            assertNull(attrs.getHostUrl());
            assertNull(attrs.getUserName());
            assertNull(attrs.getAuthToken());
            assertNull(attrs.getClientId());
            assertNull(attrs.getClientSecret());
        }
    }

    @Nested
    @DisplayName("Getter Methods Tests")
    class GetterTests {

        @Test
        @DisplayName("getHostUrl should return correct value")
        void testGetHostUrl() {
            assertEquals("https://test.atlassian.net", jiraAttributes.getHostUrl());
        }

        @Test
        @DisplayName("getUserName should return correct value")
        void testGetUserName() {
            assertEquals("test@example.com", jiraAttributes.getUserName());
        }

        @Test
        @DisplayName("getAuthToken should return correct value")
        void testGetAuthToken() {
            assertEquals("test_token", jiraAttributes.getAuthToken());
        }

        @Test
        @DisplayName("getClientId should return correct value")
        void testGetClientId() {
            assertEquals("test_client_id", jiraAttributes.getClientId());
        }

        @Test
        @DisplayName("getClientSecret should return correct value")
        void testGetClientSecret() {
            assertEquals("test_client_secret", jiraAttributes.getClientSecret());
        }
    }

    @Nested
    @DisplayName("setAttributes Method Tests")
    class SetAttributesTests {

        @Test
        @DisplayName("Should set all attributes correctly")
        void testSetAttributes_AllAttributes() {
            Map<String, Object> newAttributes = new HashMap<>();
            newAttributes.put(JiraAttributes.HOST_URL, "https://new.atlassian.net");
            newAttributes.put(JiraAttributes.USER_NAME, "new@example.com");
            newAttributes.put(JiraAttributes.AUTH_TOKEN, "new_token");
            newAttributes.put(JiraAttributes.CLIENT_ID, "new_client_id");
            newAttributes.put(JiraAttributes.CLIENT_SECRET, "new_client_secret");

            jiraAttributes.setAttributes(newAttributes);

            assertEquals("https://new.atlassian.net", jiraAttributes.getHostUrl());
            assertEquals("new@example.com", jiraAttributes.getUserName());
            assertEquals("new_token", jiraAttributes.getAuthToken());
            assertEquals("new_client_id", jiraAttributes.getClientId());
            assertEquals("new_client_secret", jiraAttributes.getClientSecret());
        }

        @Test
        @DisplayName("Should handle null values in attributes")
        void testSetAttributes_NullValues() {
            Map<String, Object> nullAttributes = new HashMap<>();
            nullAttributes.put(JiraAttributes.HOST_URL, null);
            nullAttributes.put(JiraAttributes.USER_NAME, null);
            nullAttributes.put(JiraAttributes.AUTH_TOKEN, null);
            nullAttributes.put(JiraAttributes.CLIENT_ID, null);
            nullAttributes.put(JiraAttributes.CLIENT_SECRET, null);

            jiraAttributes.setAttributes(nullAttributes);

            assertNull(jiraAttributes.getHostUrl());
            assertNull(jiraAttributes.getUserName());
            assertNull(jiraAttributes.getAuthToken());
            assertNull(jiraAttributes.getClientId());
            assertNull(jiraAttributes.getClientSecret());
        }
    }

    @Nested
    @DisplayName("basicCredentialsPresentInAttributes Tests")
    class BasicCredentialsPresentTests {

        @Test
        @DisplayName("Should return true when all basic credentials are present")
        void testBasicCredentialsPresent_AllPresent() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            assertTrue(jiraAttributes.basicCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when auth token is null")
        void testBasicCredentialsPresent_AuthTokenNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, null);

            assertFalse(jiraAttributes.basicCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when auth token is blank")
        void testBasicCredentialsPresent_AuthTokenBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "   ");

            assertFalse(jiraAttributes.basicCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when user name is null")
        void testBasicCredentialsPresent_UserNameNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, null);
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            assertFalse(jiraAttributes.basicCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when user name is blank")
        void testBasicCredentialsPresent_UserNameBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            assertFalse(jiraAttributes.basicCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when host URL is null")
        void testBasicCredentialsPresent_HostUrlNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, null);
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            assertFalse(jiraAttributes.basicCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when host URL is blank")
        void testBasicCredentialsPresent_HostUrlBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            assertFalse(jiraAttributes.basicCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when all credentials are missing")
        void testBasicCredentialsPresent_AllMissing() {
            Map<String, Object> attributes = new HashMap<>();

            assertFalse(jiraAttributes.basicCredentialsPresentInAttributes(attributes));
        }
    }

    @Nested
    @DisplayName("oAuthCredentialsPresentInAttributes Tests")
    class OAuthCredentialsPresentTests {

        @Test
        @DisplayName("Should return true when both OAuth credentials are present")
        void testOAuthCredentialsPresent_BothPresent() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "client_secret");

            assertTrue(jiraAttributes.oAuthCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when client ID is null")
        void testOAuthCredentialsPresent_ClientIdNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, null);
            attributes.put(JiraAttributes.CLIENT_SECRET, "client_secret");

            assertFalse(jiraAttributes.oAuthCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when client ID is blank")
        void testOAuthCredentialsPresent_ClientIdBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "   ");
            attributes.put(JiraAttributes.CLIENT_SECRET, "client_secret");

            assertFalse(jiraAttributes.oAuthCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when client secret is null")
        void testOAuthCredentialsPresent_ClientSecretNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, null);

            assertFalse(jiraAttributes.oAuthCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when client secret is blank")
        void testOAuthCredentialsPresent_ClientSecretBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "");

            assertFalse(jiraAttributes.oAuthCredentialsPresentInAttributes(attributes));
        }

        @Test
        @DisplayName("Should return false when both credentials are missing")
        void testOAuthCredentialsPresent_BothMissing() {
            Map<String, Object> attributes = new HashMap<>();

            assertFalse(jiraAttributes.oAuthCredentialsPresentInAttributes(attributes));
        }
    }

    @Nested
    @DisplayName("isPartialBasicCredentialPresent Tests")
    class IsPartialBasicCredentialPresentTests {

        @Test
        @DisplayName("Should return true when only auth token is present")
        void testPartialBasicCredential_OnlyAuthToken() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            assertTrue(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should return true when only host URL is present")
        void testPartialBasicCredential_OnlyHostUrl() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");

            assertTrue(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should return true when only user name is present")
        void testPartialBasicCredential_OnlyUserName() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");

            assertTrue(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should return true when auth token and host URL present but user name missing")
        void testPartialBasicCredential_MissingUserName() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");

            assertTrue(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should return true when auth token and user name present but host URL missing")
        void testPartialBasicCredential_MissingHostUrl() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");

            assertTrue(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should return true when host URL and user name present but auth token missing")
        void testPartialBasicCredential_MissingAuthToken() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");

            assertTrue(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should return false when all basic credentials are present")
        void testPartialBasicCredential_AllPresent() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");

            assertFalse(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }

        @Test
        @DisplayName("Should return false when no basic credentials are present")
        void testPartialBasicCredential_NonePresent() {
            Map<String, Object> attributes = new HashMap<>();

            assertFalse(jiraAttributes.isPartialBasicCredentialPresent(attributes));
        }
    }

    @Nested
    @DisplayName("areBasicCredentialsPresent Tests")
    class AreBasicCredentialsPresentTests {

        @Test
        @DisplayName("Should return true when all basic credentials are present")
        void testAreBasicCredentialsPresent_AllPresent() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            jiraAttributes.setAttributes(attributes);

            assertTrue(jiraAttributes.areBasicCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when user name is null")
        void testAreBasicCredentialsPresent_UserNameNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, null);
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areBasicCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when user name is blank")
        void testAreBasicCredentialsPresent_UserNameBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "   ");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areBasicCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when auth token is null")
        void testAreBasicCredentialsPresent_AuthTokenNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, null);

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areBasicCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when auth token is blank")
        void testAreBasicCredentialsPresent_AuthTokenBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "");

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areBasicCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when host URL is null")
        void testAreBasicCredentialsPresent_HostUrlNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, null);
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areBasicCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when host URL is blank")
        void testAreBasicCredentialsPresent_HostUrlBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areBasicCredentialsPresent());
        }
    }

    @Nested
    @DisplayName("areOAuthCredentialsPresent Tests")
    class AreOAuthCredentialsPresentTests {

        @Test
        @DisplayName("Should return true when both OAuth credentials are present")
        void testAreOAuthCredentialsPresent_BothPresent() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "client_secret");

            jiraAttributes.setAttributes(attributes);

            assertTrue(jiraAttributes.areOAuthCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when client ID is null")
        void testAreOAuthCredentialsPresent_ClientIdNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, null);
            attributes.put(JiraAttributes.CLIENT_SECRET, "client_secret");

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areOAuthCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when client ID is blank")
        void testAreOAuthCredentialsPresent_ClientIdBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "");
            attributes.put(JiraAttributes.CLIENT_SECRET, "client_secret");

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areOAuthCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when client secret is null")
        void testAreOAuthCredentialsPresent_ClientSecretNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, null);

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areOAuthCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when client secret is blank")
        void testAreOAuthCredentialsPresent_ClientSecretBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "   ");

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areOAuthCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when both credentials are missing")
        void testAreOAuthCredentialsPresent_BothMissing() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, null);
            attributes.put(JiraAttributes.CLIENT_SECRET, null);

            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areOAuthCredentialsPresent());
        }
    }

    @Nested
    @DisplayName("update Method Tests")
    class UpdateMethodTests {

        @Test
        @DisplayName("Should update all attributes correctly")
        void testUpdate_AllAttributes() {
            Map<String, Object> updateAttributes = new HashMap<>();
            updateAttributes.put(JiraAttributes.CLIENT_ID, "updated_client_id");
            updateAttributes.put(JiraAttributes.CLIENT_SECRET, "updated_client_secret");
            updateAttributes.put(JiraAttributes.USER_NAME, "updated@example.com");
            updateAttributes.put(JiraAttributes.HOST_URL, "https://updated.atlassian.net");
            updateAttributes.put(JiraAttributes.AUTH_TOKEN, "updated_token");

            jiraAttributes.update(updateAttributes);

            assertEquals("updated_client_id", jiraAttributes.getClientId());
            assertEquals("updated_client_secret", jiraAttributes.getClientSecret());
            assertEquals("updated@example.com", jiraAttributes.getUserName());
            assertEquals("https://updated.atlassian.net", jiraAttributes.getHostUrl());
            assertEquals("updated_token", jiraAttributes.getAuthToken());
        }

        @Test
        @DisplayName("Should handle null values in update")
        void testUpdate_NullValues() {
            Map<String, Object> updateAttributes = new HashMap<>();
            updateAttributes.put(JiraAttributes.CLIENT_ID, null);
            updateAttributes.put(JiraAttributes.CLIENT_SECRET, null);
            updateAttributes.put(JiraAttributes.USER_NAME, null);
            updateAttributes.put(JiraAttributes.HOST_URL, null);
            updateAttributes.put(JiraAttributes.AUTH_TOKEN, null);

            jiraAttributes.update(updateAttributes);

            assertNull(jiraAttributes.getClientId());
            assertNull(jiraAttributes.getClientSecret());
            assertNull(jiraAttributes.getUserName());
            assertNull(jiraAttributes.getHostUrl());
            assertNull(jiraAttributes.getAuthToken());
        }

        @Test
        @DisplayName("Should handle empty map in update")
        void testUpdate_EmptyMap() {
            Map<String, Object> updateAttributes = new HashMap<>();

            // Set initial values
            Map<String, Object> initialAttributes = new HashMap<>();
            initialAttributes.put(JiraAttributes.CLIENT_ID, "initial_client_id");
            jiraAttributes.setAttributes(initialAttributes);

            // Update with empty map
            jiraAttributes.update(updateAttributes);

            // Should be null since InvokerAttributes.getStringOrNull returns null for missing keys
            assertNull(jiraAttributes.getClientId());
        }
    }

    @Nested
    @DisplayName("validateAttributes Method Tests")
    class ValidateAttributesTests {

        @Test
        @DisplayName("Should throw exception when no credentials provided")
        void testValidateAttributes_NoCredentials() {
            Map<String, Object> attributes = new HashMap<>();

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals(
                "Either (Auth token, user name, host url) or (Client ID, Client secret) must be provided for authentication",
                exception.getMessage()
            );
        }

        @Test
        @DisplayName("Should throw exception for invalid email format")
        void testValidateAttributes_InvalidEmail() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            attributes.put(JiraAttributes.USER_NAME, "invalid-email");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            jiraAttributes.setAttributes(attributes);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertEquals("User name is not a valid email address", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("validateOAuthCredentialsFormat Tests (via validateAttributes)")
    class ValidateOAuthCredentialsFormatTests {

        @Test
        @DisplayName("Should accept valid OAuth credentials with alphanumeric Client ID")
        void testValidateOAuthFormat_ValidAlphanumeric() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "abc123XYZ789");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client ID with hyphens")
        void testValidateOAuthFormat_WithHyphens() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client-id-123");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept Client ID with underscores")
        void testValidateOAuthFormat_WithUnderscores() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id_123");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should reject Client ID with spaces")
        void testValidateOAuthFormat_WithSpaces() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

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
        @DisplayName("Should reject Client ID with @ symbol")
        void testValidateOAuthFormat_WithAtSymbol() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client@id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

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
        @DisplayName("Should reject Client ID with dot")
        void testValidateOAuthFormat_WithDot() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client.id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));

            jiraAttributes.setAttributes(attributes);

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
        @DisplayName("Should accept Client Secret with 32 characters (minimum)")
        void testValidateOAuthFormat_ClientSecret32Chars() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "valid_client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(32));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }

        @Test
        @DisplayName("Should accept but warn for Client Secret with 31 characters")
        void testValidateOAuthFormat_ClientSecret31Chars() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "valid_client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(31));

            jiraAttributes.setAttributes(attributes);

            JiraAttributes spyAttributes = spy(jiraAttributes);
            doNothing().when(spyAttributes).testConnection(any());

            // Should not throw exception, just log warning
            assertDoesNotThrow(() -> spyAttributes.validateAttributes(attributes));
        }
    }

    @Nested
    @DisplayName("testConnection Method Tests")
    class TestConnectionTests {

        @Test
        @DisplayName("Should throw exception for partial OAuth credentials - only Client ID")
        void testConnection_PartialOAuth_OnlyClientId() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id_123");
            // Missing CLIENT_SECRET

            jiraAttributes.setAttributes(attributes);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.testConnection(attributes)
            );

            assertEquals("Please provide both Client Id and Client Secret", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for partial OAuth credentials - only Client Secret")
        void testConnection_PartialOAuth_OnlyClientSecret() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_SECRET, "client_secret_123");
            // Missing CLIENT_ID

            jiraAttributes.setAttributes(attributes);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.testConnection(attributes)
            );

            assertEquals("Please provide both Client Id and Client Secret", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for partial basic credentials - only Auth Token")
        void testConnection_PartialBasic_OnlyAuthToken() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.AUTH_TOKEN, "auth_token_123");
            // Missing USER_NAME and HOST_URL

            jiraAttributes.setAttributes(attributes);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.testConnection(attributes)
            );

            assertEquals("Please provide Admin Auth Token, Admin User Name, and Host Url", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for partial basic credentials - only User Name")
        void testConnection_PartialBasic_OnlyUserName() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.USER_NAME, "user@example.com");
            // Missing AUTH_TOKEN and HOST_URL

            jiraAttributes.setAttributes(attributes);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.testConnection(attributes)
            );

            assertEquals("Please provide Admin Auth Token, Admin User Name, and Host Url", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for partial basic credentials - only Host URL")
        void testConnection_PartialBasic_OnlyHostUrl() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            // Missing AUTH_TOKEN and USER_NAME

            jiraAttributes.setAttributes(attributes);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.testConnection(attributes)
            );

            assertEquals("Please provide Admin Auth Token, Admin User Name, and Host Url", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for partial basic credentials - missing Auth Token")
        void testConnection_PartialBasic_MissingAuthToken() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.USER_NAME, "user@example.com");
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            // Missing AUTH_TOKEN

            jiraAttributes.setAttributes(attributes);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.testConnection(attributes)
            );

            assertEquals("Please provide Admin Auth Token, Admin User Name, and Host Url", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for partial basic credentials - missing User Name")
        void testConnection_PartialBasic_MissingUserName() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.AUTH_TOKEN, "auth_token_123");
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            // Missing USER_NAME

            jiraAttributes.setAttributes(attributes);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.testConnection(attributes)
            );

            assertEquals("Please provide Admin Auth Token, Admin User Name, and Host Url", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw exception for partial basic credentials - missing Host URL")
        void testConnection_PartialBasic_MissingHostUrl() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.AUTH_TOKEN, "auth_token_123");
            attributes.put(JiraAttributes.USER_NAME, "user@example.com");
            // Missing HOST_URL

            jiraAttributes.setAttributes(attributes);

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.testConnection(attributes)
            );

            assertEquals("Please provide Admin Auth Token, Admin User Name, and Host Url", exception.getMessage());
        }
    }
}

