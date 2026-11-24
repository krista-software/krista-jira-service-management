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
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

            // Also update invoker mock to simulate real-world scenario where
            // invoker's live map reflects current state
            when(invoker.getAttributes()).thenReturn(nullAttributes);
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

            when(invoker.getAttributes()).thenReturn(attributes);
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

            when(invoker.getAttributes()).thenReturn(attributes);
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

            when(invoker.getAttributes()).thenReturn(attributes);
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

            when(invoker.getAttributes()).thenReturn(attributes);
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

            when(invoker.getAttributes()).thenReturn(attributes);
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

            when(invoker.getAttributes()).thenReturn(attributes);
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
            attributes.put(JiraAttributes.CREDENTIALS_MODE, "own");

            when(invoker.getAttributes()).thenReturn(attributes);
            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areOAuthCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when client ID is blank")
        void testAreOAuthCredentialsPresent_ClientIdBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "");
            attributes.put(JiraAttributes.CLIENT_SECRET, "client_secret");
            attributes.put(JiraAttributes.CREDENTIALS_MODE, "own");

            when(invoker.getAttributes()).thenReturn(attributes);
            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areOAuthCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when client secret is null")
        void testAreOAuthCredentialsPresent_ClientSecretNull() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, null);
            attributes.put(JiraAttributes.CREDENTIALS_MODE, "own");

            when(invoker.getAttributes()).thenReturn(attributes);
            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areOAuthCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when client secret is blank")
        void testAreOAuthCredentialsPresent_ClientSecretBlank() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "   ");
            attributes.put(JiraAttributes.CREDENTIALS_MODE, "own");

            when(invoker.getAttributes()).thenReturn(attributes);
            jiraAttributes.setAttributes(attributes);

            assertFalse(jiraAttributes.areOAuthCredentialsPresent());
        }

        @Test
        @DisplayName("Should return false when both credentials are missing")
        void testAreOAuthCredentialsPresent_BothMissing() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, null);
            attributes.put(JiraAttributes.CLIENT_SECRET, null);
            attributes.put(JiraAttributes.CREDENTIALS_MODE, "own");

            when(invoker.getAttributes()).thenReturn(attributes);
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

            // Also update invoker mock so getters don't fall back to old values
            when(invoker.getAttributes()).thenReturn(updateAttributes);
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

            // Update invoker mock to return empty map so getters don't fall back to old values
            when(invoker.getAttributes()).thenReturn(updateAttributes);
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

            assertTrue(exception.getMessage().contains("Please choose ONE authentication method"));
            assertTrue(exception.getMessage().contains("For OAuth 2.0: Provide Client Id and Client Secret"));
            assertTrue(exception.getMessage().contains("For Basic Auth: Provide Admin Auth Token, Admin User Name, and Host Url"));
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

        @Test
        @DisplayName("Should throw exception when both OAuth and Basic Auth credentials provided")
        void testValidateAttributes_MixedCredentials_AllFields() {
            Map<String, Object> attributes = new HashMap<>();
            // OAuth credentials
            attributes.put(JiraAttributes.CLIENT_ID, "test_client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));
            // Basic Auth credentials
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertTrue(exception.getMessage().contains("Please choose ONE authentication method"));
            assertTrue(exception.getMessage().contains("Do not mix credentials from both authentication methods"));
        }

        @Test
        @DisplayName("Should throw exception when OAuth credentials mixed with partial Basic Auth")
        void testValidateAttributes_MixedCredentials_OAuthPlusPartialBasic() {
            Map<String, Object> attributes = new HashMap<>();
            // Complete OAuth credentials
            attributes.put(JiraAttributes.CLIENT_ID, "test_client_id");
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));
            // Partial Basic Auth (only Auth Token)
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertTrue(exception.getMessage().contains("Please choose ONE authentication method"));
            assertTrue(exception.getMessage().contains("remove Admin Auth Token, Admin User Name, Host Url"));
        }

        @Test
        @DisplayName("Should throw exception when Basic Auth credentials mixed with partial OAuth")
        void testValidateAttributes_MixedCredentials_BasicPlusPartialOAuth() {
            Map<String, Object> attributes = new HashMap<>();
            // Complete Basic Auth credentials
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");
            attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            // Partial OAuth (only Client ID)
            attributes.put(JiraAttributes.CLIENT_ID, "test_client_id");

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertTrue(exception.getMessage().contains("Please choose ONE authentication method"));
            assertTrue(exception.getMessage().contains("remove Client Id, Client Secret"));
        }

        @Test
        @DisplayName("Should throw exception when only Client ID and Auth Token provided")
        void testValidateAttributes_MixedCredentials_ClientIdAndAuthToken() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_ID, "test_client_id");
            attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertTrue(exception.getMessage().contains("Please choose ONE authentication method"));
        }

        @Test
        @DisplayName("Should throw exception when only Client Secret and User Name provided")
        void testValidateAttributes_MixedCredentials_ClientSecretAndUserName() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(JiraAttributes.CLIENT_SECRET, "a".repeat(64));
            attributes.put(JiraAttributes.USER_NAME, "test@example.com");

            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jiraAttributes.validateAttributes(attributes)
            );

            assertTrue(exception.getMessage().contains("Please choose ONE authentication method"));
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

            assertTrue(exception.getMessage().contains("Incomplete OAuth 2.0 credentials detected"));
            assertTrue(exception.getMessage().contains("Please provide BOTH Client ID and Client Secret"));
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

            assertTrue(exception.getMessage().contains("Incomplete OAuth 2.0 credentials detected"));
            assertTrue(exception.getMessage().contains("Please provide BOTH Client ID and Client Secret"));
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

            assertTrue(exception.getMessage().contains("Incomplete Basic Auth credentials detected"));
            assertTrue(exception.getMessage().contains("Please provide ALL three fields"));
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

            assertTrue(exception.getMessage().contains("Incomplete Basic Auth credentials detected"));
            assertTrue(exception.getMessage().contains("Please provide ALL three fields"));
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

            assertTrue(exception.getMessage().contains("Incomplete Basic Auth credentials detected"));
            assertTrue(exception.getMessage().contains("Please provide ALL three fields"));
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

            assertTrue(exception.getMessage().contains("Incomplete Basic Auth credentials detected"));
            assertTrue(exception.getMessage().contains("Please provide ALL three fields"));
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

            assertTrue(exception.getMessage().contains("Incomplete Basic Auth credentials detected"));
            assertTrue(exception.getMessage().contains("Please provide ALL three fields"));
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

            assertTrue(exception.getMessage().contains("Incomplete Basic Auth credentials detected"));
            assertTrue(exception.getMessage().contains("Please provide ALL three fields"));
        }

        @Test
        @DisplayName("Should take OAuth path when isManagedCredentialsEnabled is true with no own OAuth creds")
        void testConnection_ManagedCredentials_TakesOAuthPath() {
            // Set up routing so JiraService constructor does not NPE
            RoutingInfo routingInfo = mock(RoutingInfo.class);
            lenient().when(invoker.getRoutingInfo()).thenReturn(routingInfo);
            lenient().when(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                    .thenReturn("http://localhost:8080");

            // Empty attributes — no own OAuth credentials, no Basic Auth
            Map<String, Object> attributes = new HashMap<>();

            // Spy so we can stub isManagedCredentialsEnabled() without changing real state
            JiraAttributes spy = spy(jiraAttributes);
            doReturn(true).when(spy).isManagedCredentialsEnabled();

            // The OAuth path is entered; the REST client fails in the test environment
            // (no Jersey provider on test classpath) — the important assertion is that
            // neither the "Incomplete OAuth" nor "Incomplete Basic Auth" guard is triggered.
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> spy.testConnection(attributes));
            assertFalse(ex.getMessage().contains("Incomplete OAuth 2.0 credentials detected"),
                    "Should not reject as partial OAuth — managed path was entered");
            assertFalse(ex.getMessage().contains("Incomplete Basic Auth credentials detected"),
                    "Should not reject as partial Basic Auth — managed path was entered");
        }
    }

    // ── NEW: credential snapshot fallback tests ────────────────────────────

    @Nested
    @DisplayName("Credential Snapshot Fallback Tests")
    class CredentialSnapshotFallbackTests {

        private static final String INVOKER_ID = "test-invoker-id";
        private static final String SNAP_HOST   = "cred." + INVOKER_ID + ".hostUrl";
        private static final String SNAP_USER   = "cred." + INVOKER_ID + ".userName";
        private static final String SNAP_TOKEN  = "cred." + INVOKER_ID + ".authToken";
        private static final String SNAP_CID    = "cred." + INVOKER_ID + ".clientId";
        private static final String SNAP_CSEC   = "cred." + INVOKER_ID + ".clientSecret";

        @BeforeEach
        void setUpSnap() {
            // lenient: some tests return the super-value before credSnap is ever consulted
            lenient().when(requestContext.getInvokerId()).thenReturn(INVOKER_ID);
        }

        // ── getHostUrl ──

        @Test
        @DisplayName("getHostUrl returns invoker-attribute value when snapshot is absent")
        void testGetHostUrl_ReturnsInvokerValue_WhenSnapshotAbsent() {
            // Credential snapshot is checked first but returns null (not stubbed),
            // so invoker's value (set in outer setUp) is used as the fallback.
            assertEquals("https://test.atlassian.net", jiraAttributes.getHostUrl());
        }

        @Test
        @DisplayName("getHostUrl falls back to snapshot when invoker attribute is null")
        void testGetHostUrl_FallsBackToSnapshot_WhenNull() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.HOST_URL, null);
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_HOST)).thenReturn("https://snap.atlassian.net");

            assertEquals("https://snap.atlassian.net", jiraAttributes.getHostUrl());
            verify(sessionManager).get(SNAP_HOST);
        }

        @Test
        @DisplayName("getHostUrl falls back to snapshot when invoker attribute is blank")
        void testGetHostUrl_FallsBackToSnapshot_WhenBlank() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.HOST_URL, "   ");
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_HOST)).thenReturn("https://snap.atlassian.net");

            assertEquals("https://snap.atlassian.net", jiraAttributes.getHostUrl());
        }

        @Test
        @DisplayName("getHostUrl returns null when both invoker attribute and snapshot are absent")
        void testGetHostUrl_ReturnsNull_WhenBothAbsent() {
            Map<String, Object> attrs = new HashMap<>();
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_HOST)).thenReturn(null);

            assertNull(jiraAttributes.getHostUrl());
        }

        // ── getUserName ──

        @Test
        @DisplayName("getUserName falls back to snapshot when invoker attribute is null")
        void testGetUserName_FallsBackToSnapshot_WhenNull() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.USER_NAME, null);
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_USER)).thenReturn("snap@example.com");

            assertEquals("snap@example.com", jiraAttributes.getUserName());
        }

        @Test
        @DisplayName("getUserName falls back to snapshot when invoker attribute is blank")
        void testGetUserName_FallsBackToSnapshot_WhenBlank() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.USER_NAME, "");
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_USER)).thenReturn("snap@example.com");

            assertEquals("snap@example.com", jiraAttributes.getUserName());
        }

        // ── getAuthToken ──

        @Test
        @DisplayName("getAuthToken falls back to snapshot when invoker attribute is null")
        void testGetAuthToken_FallsBackToSnapshot_WhenNull() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.AUTH_TOKEN, null);
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_TOKEN)).thenReturn("snap_token_abc");

            assertEquals("snap_token_abc", jiraAttributes.getAuthToken());
        }

        @Test
        @DisplayName("getAuthToken falls back to snapshot when invoker attribute is blank")
        void testGetAuthToken_FallsBackToSnapshot_WhenBlank() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.AUTH_TOKEN, "  ");
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_TOKEN)).thenReturn("snap_token_abc");

            assertEquals("snap_token_abc", jiraAttributes.getAuthToken());
        }

        // ── getClientId ──

        @Test
        @DisplayName("getClientId falls back to snapshot when invoker attribute is null")
        void testGetClientId_FallsBackToSnapshot_WhenNull() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.CLIENT_ID, null);
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_CID)).thenReturn("snap-client-id");

            assertEquals("snap-client-id", jiraAttributes.getClientId());
        }

        @Test
        @DisplayName("getClientId falls back to snapshot when invoker attribute is blank")
        void testGetClientId_FallsBackToSnapshot_WhenBlank() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.CLIENT_ID, "");
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_CID)).thenReturn("snap-client-id");

            assertEquals("snap-client-id", jiraAttributes.getClientId());
        }

        @Test
        @DisplayName("getClientId returns null when both invoker attribute and snapshot are absent")
        void testGetClientId_ReturnsNull_WhenBothAbsent() {
            Map<String, Object> attrs = new HashMap<>();
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_CID)).thenReturn(null);

            assertNull(jiraAttributes.getClientId());
        }

        // ── getClientSecret ──

        @Test
        @DisplayName("getClientSecret falls back to snapshot when invoker attribute is null")
        void testGetClientSecret_FallsBackToSnapshot_WhenNull() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.CLIENT_SECRET, null);
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_CSEC)).thenReturn("snap-secret-abcdefghij");

            assertEquals("snap-secret-abcdefghij", jiraAttributes.getClientSecret());
        }

        @Test
        @DisplayName("getClientSecret falls back to snapshot when invoker attribute is blank")
        void testGetClientSecret_FallsBackToSnapshot_WhenBlank() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.CLIENT_SECRET, "   ");
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_CSEC)).thenReturn("snap-secret-abcdefghij");

            assertEquals("snap-secret-abcdefghij", jiraAttributes.getClientSecret());
        }

        @Test
        @DisplayName("getClientId prefers snapshot over non-blank invoker attribute")
        void testGetClientId_PrefersSnapshot_OverInvokerAttribute() {
            // Invoker has a stale clientId; snapshot has the correct one
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.CLIENT_ID, "old-stale-client-id");
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_CID)).thenReturn("new-correct-client-id");

            // Snapshot should win over invoker attribute
            assertEquals("new-correct-client-id", jiraAttributes.getClientId());
        }

        @Test
        @DisplayName("getClientSecret prefers snapshot over non-blank invoker attribute")
        void testGetClientSecret_PrefersSnapshot_OverInvokerAttribute() {
            Map<String, Object> attrs = new HashMap<>();
            attrs.put(JiraAttributes.CLIENT_SECRET, "old-stale-secret");
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            when(sessionManager.get(SNAP_CSEC)).thenReturn("new-correct-secret");

            assertEquals("new-correct-secret", jiraAttributes.getClientSecret());
        }

        @Test
        @DisplayName("credSnap returns null when sessionManager is null (no NPE, falls back gracefully)")
        void testCredSnap_ReturnsNull_WhenSessionManagerNull() {
            // Invoker has no credentials so super returns null; sessionManager=null → credSnap returns null
            Map<String, Object> emptyAttrs = new HashMap<>();
            when(invoker.getAttributes()).thenReturn(emptyAttrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, null, authorizationContext);

            assertNull(jiraAttributes.getHostUrl());
            assertNull(jiraAttributes.getClientId());
        }

        @Test
        @DisplayName("credSnap returns null when requestContext is null (no NPE, falls back gracefully)")
        void testCredSnap_ReturnsNull_WhenRequestContextNull() {
            // Invoker has no credentials so super returns null; requestContext=null → credSnap returns null
            Map<String, Object> emptyAttrs = new HashMap<>();
            when(invoker.getAttributes()).thenReturn(emptyAttrs);
            jiraAttributes = new JiraAttributes(invoker, null, sessionManager, authorizationContext);

            assertNull(jiraAttributes.getHostUrl());
            assertNull(jiraAttributes.getUserName());
            assertNull(jiraAttributes.getAuthToken());
            assertNull(jiraAttributes.getClientId());
            assertNull(jiraAttributes.getClientSecret());
        }
    }

    // ── NEW: isManagedCredentialsEnabled override tests ──────────────────

    @Nested
    @DisplayName("isManagedCredentialsEnabled Override Tests")
    class IsManagedCredentialsEnabledTests {

        private static final String INVOKER_ID  = "test-invoker-id";
        private static final String SNAP_MODE   = "cred." + INVOKER_ID + ".credentialsMode";

        @BeforeEach
        void setUpSnap() {
            when(requestContext.getInvokerId()).thenReturn(INVOKER_ID);
        }

        @Test
        @DisplayName("Returns true via super when clientId and clientSecret are absent (ownOAuth=false)")
        void testIsManagedCredentialsEnabled_SuperReturnsTrue_WhenNoCreds() {
            // Invoker has no clientId or clientSecret → super sees ownOAuthPresent=false → returns true
            Map<String, Object> attrs = new HashMap<>();
            when(invoker.getAttributes()).thenReturn(attrs);
            jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);

            assertTrue(jiraAttributes.isManagedCredentialsEnabled());
        }

        @Test
        @DisplayName("Returns false when super returns false and credSnap credentialsMode is null")
        void testIsManagedCredentialsEnabled_ReturnsFalse_WhenSnapNull() {
            // Invoker has own clientId+clientSecret so ownOAuthPresent=true → super returns false
            // credSnap returns null
            when(sessionManager.get(SNAP_MODE)).thenReturn(null);

            assertFalse(jiraAttributes.isManagedCredentialsEnabled());
        }

        @Test
        @DisplayName("Returns false when credSnap credentialsMode is 'own'")
        void testIsManagedCredentialsEnabled_ReturnsFalse_WhenSnapOwn() {
            when(sessionManager.get(SNAP_MODE)).thenReturn("own");

            assertFalse(jiraAttributes.isManagedCredentialsEnabled());
        }

        @Test
        @DisplayName("Returns true when credSnap credentialsMode is 'managed' (snap takes precedence over store)")
        void testIsManagedCredentialsEnabled_ReturnsFalse_WhenManagedButStoreNotConfigured() {
            // The JSM override returns true immediately when snap says "managed"
            // without consulting ManagedCredentialStore — the snap is authoritative.
            when(sessionManager.get(SNAP_MODE)).thenReturn("managed");

            assertTrue(jiraAttributes.isManagedCredentialsEnabled());
        }

        @Test
        @DisplayName("Returns true when credSnap credentialsMode is 'managed'")
        void testIsManagedCredentialsEnabled_ReturnsTrue_WhenManagedAndStoreConfigured() {
            when(sessionManager.get(SNAP_MODE)).thenReturn("managed");

            assertTrue(jiraAttributes.isManagedCredentialsEnabled());
        }

        @Test
        @DisplayName("Returns true when credSnap credentialsMode is 'MANAGED' (case-insensitive)")
        void testIsManagedCredentialsEnabled_CaseInsensitive() {
            when(sessionManager.get(SNAP_MODE)).thenReturn("MANAGED");

            assertTrue(jiraAttributes.isManagedCredentialsEnabled());
        }
    }

    // ── Overridden protected method tests ──────────────────────────────────────

    @Nested
    @DisplayName("Overridden Protected Method Tests")
    class OverriddenMethodTests {

        @Test
        @DisplayName("getCredentialsModeFieldName should return CREDENTIALS_MODE constant")
        void testGetCredentialsModeFieldName() throws Exception {
            Method method = JiraAttributes.class.getDeclaredMethod("getCredentialsModeFieldName");
            method.setAccessible(true);
            assertEquals(JiraAttributes.CREDENTIALS_MODE, method.invoke(jiraAttributes));
        }

        @Test
        @DisplayName("getExtensionType should return 'jira-service-management'")
        void testGetExtensionType() throws Exception {
            Method method = JiraAttributes.class.getDeclaredMethod("getExtensionType");
            method.setAccessible(true);
            assertEquals("jira-service-management", method.invoke(jiraAttributes));
        }

        @Test
        @DisplayName("getServiceAccountFieldName should return SERVICE_ACCOUNT constant")
        void testGetServiceAccountFieldName() throws Exception {
            Method method = JiraAttributes.class.getDeclaredMethod("getServiceAccountFieldName");
            method.setAccessible(true);
            assertEquals(JiraAttributes.SERVICE_ACCOUNT, method.invoke(jiraAttributes));
        }
    }

    // ── testConnectionWithRestClient branch coverage (via reflection) ──────────

    @Nested
    @DisplayName("testConnectionWithRestClient Branch Tests")
    class TestConnectionWithRestClientTests {

        private RestClient mockRestClient;

        @BeforeEach
        void setUpRestClient() {
            mockRestClient = mock(RestClient.class);
        }

        /** Calls the private method via reflection; propagates InvocationTargetException. */
        private void invokePrivate(RestClient client) throws Exception {
            Method method = JiraAttributes.class.getDeclaredMethod(
                    "testConnectionWithRestClient", Map.class, RestClient.class);
            method.setAccessible(true);
            method.invoke(jiraAttributes, new HashMap<>(), client);
        }

        @Test
        @DisplayName("Should succeed (no exception) when response JSON contains 'self' field")
        void shouldSucceedWhenResponseHasSelf() {
            when(mockRestClient.fetch(eq("myself"), any(), eq(String.class)))
                    .thenReturn("{\"self\":\"https://api.atlassian.net/rest/api/3/user?accountId=abc123\"}");

            assertDoesNotThrow(() -> invokePrivate(mockRestClient));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException with 'incorrect credentials' message when 'self' absent and 'message' present")
        void shouldThrowIncorrectCredentialsWhenNoSelfButMessagePresent() throws Exception {
            when(mockRestClient.fetch(eq("myself"), any(), eq(String.class)))
                    .thenReturn("{\"message\":\"Unauthorized\"}");

            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> invokePrivate(mockRestClient));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
            assertTrue(ex.getCause().getMessage().contains(
                    "The credentials provided are incorrect"));
        }

        @Test
        @DisplayName("Should throw 'Wrong Credentials!' when response has no 'self' and no 'message'")
        void shouldThrowWrongCredentialsWhenNoSelfAndNoMessage() throws Exception {
            when(mockRestClient.fetch(eq("myself"), any(), eq(String.class)))
                    .thenReturn("{}");

            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> invokePrivate(mockRestClient));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
            // "Wrong Credentials!" is thrown inside the try block and is re-caught by
            // catch(Exception), which wraps it. The original message is therefore one
            // level deeper in the cause chain.
            assertInstanceOf(IllegalArgumentException.class, ex.getCause().getCause());
            assertTrue(ex.getCause().getCause().getMessage().contains("Wrong Credentials!"));
        }

        @Test
        @DisplayName("Should rethrow MustAuthorizeException directly without wrapping it")
        void shouldRethrowMustAuthorizeException() throws Exception {
            MustAuthorizeException mustAuth = mock(MustAuthorizeException.class);
            when(mockRestClient.fetch(eq("myself"), any(), eq(String.class)))
                    .thenThrow(mustAuth);

            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> invokePrivate(mockRestClient));
            assertSame(mustAuth, ex.getCause(),
                    "MustAuthorizeException should be rethrown as-is, not wrapped");
        }

        @Test
        @DisplayName("Should wrap generic Exception in IllegalArgumentException with 'incorrect credentials' message")
        void shouldWrapGenericExceptionInIllegalArgumentException() throws Exception {
            RuntimeException networkError = new RuntimeException("Connection refused");
            when(mockRestClient.fetch(eq("myself"), any(), eq(String.class)))
                    .thenThrow(networkError);

            InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                    () -> invokePrivate(mockRestClient));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
            assertTrue(ex.getCause().getMessage().contains(
                    "The credentials provided are incorrect"));
            assertSame(networkError, ex.getCause().getCause());
        }
    }

    // ── testConnection basic-auth path ────────────────────────────────────────

    @Nested
    @DisplayName("testConnection Basic Auth Path Tests")
    class TestConnectionBasicAuthPathTests {

        private final Map<String, Object> basicAttributes = new HashMap<>();

        @BeforeEach
        void setUpBasicAttributes() {
            basicAttributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
            basicAttributes.put(JiraAttributes.USER_NAME, "user@example.com");
            basicAttributes.put(JiraAttributes.AUTH_TOKEN, "auth_token_abc");
        }

        @Test
        @DisplayName("Should enter basic auth path and succeed when AuthRestClient returns valid 'self' JSON")
        void testConnection_BasicAuth_Success() {
            try (MockedConstruction<AuthRestClient> mocked = mockConstruction(AuthRestClient.class,
                    (mock, ctx) -> when(mock.fetch(eq("myself"), any(), eq(String.class)))
                            .thenReturn("{\"self\":\"https://api.atlassian.net/user?accountId=xyz\"}"))) {

                assertDoesNotThrow(() -> jiraAttributes.testConnection(basicAttributes));
                assertEquals(1, mocked.constructed().size(),
                        "Exactly one AuthRestClient should have been constructed");
            }
        }

        @Test
        @DisplayName("Should enter basic auth path and throw when AuthRestClient response lacks 'self'")
        void testConnection_BasicAuth_InvalidCredentials() {
            try (MockedConstruction<AuthRestClient> mocked = mockConstruction(AuthRestClient.class,
                    (mock, ctx) -> when(mock.fetch(eq("myself"), any(), eq(String.class)))
                            .thenReturn("{\"message\":\"Bad credentials\"}"))) {

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> jiraAttributes.testConnection(basicAttributes));
                assertTrue(ex.getMessage().contains("The credentials provided are incorrect"));
            }
        }

        @Test
        @DisplayName("Should enter basic auth path and throw 'Wrong Credentials!' when response is empty object")
        void testConnection_BasicAuth_WrongCredentials() {
            try (MockedConstruction<AuthRestClient> mocked = mockConstruction(AuthRestClient.class,
                    (mock, ctx) -> when(mock.fetch(eq("myself"), any(), eq(String.class)))
                            .thenReturn("{}"))) {

                IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                        () -> jiraAttributes.testConnection(basicAttributes));
                // "Wrong Credentials!" is thrown inside testConnectionWithRestClient's try
                // block and is re-caught by catch(Exception), which wraps it. The original
                // message is therefore in the cause, not in the outer exception message.
                assertTrue(ex.getCause().getMessage().contains("Wrong Credentials!"));
            }
        }

        @Test
        @DisplayName("Should enter basic auth path and rethrow MustAuthorizeException")
        void testConnection_BasicAuth_MustAuthorizeException() {
            MustAuthorizeException mustAuth = mock(MustAuthorizeException.class);
            try (MockedConstruction<AuthRestClient> mocked = mockConstruction(AuthRestClient.class,
                    (mock, ctx) -> when(mock.fetch(eq("myself"), any(), eq(String.class)))
                            .thenThrow(mustAuth))) {

                MustAuthorizeException thrown = assertThrows(MustAuthorizeException.class,
                        () -> jiraAttributes.testConnection(basicAttributes));
                assertSame(mustAuth, thrown);
            }
        }
    }
}

