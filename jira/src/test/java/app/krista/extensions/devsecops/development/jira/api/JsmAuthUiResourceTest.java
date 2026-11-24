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
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.audit.KeyValueAuditStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JsmAuthUiResource}.
 *
 * <p>Covers all four hook methods overridden from
 * {@code AbstractAtlassianAuthResource}:
 * <ul>
 *   <li>{@code loadAttributes} – always returns the injected {@link JiraAttributes}</li>
 *   <li>{@code saveAttributes} – always returns {@code true}</li>
 *   <li>{@code onOAuthCallbackSuccess} – notifies {@link AuthorizationListener}</li>
 *   <li>{@code removeTokens} – conditionally removes the app-wide token from
 *       {@link KeyValueStore} based on OAuth / managed-credentials presence</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JsmAuthUiResource Tests")
class JsmAuthUiResourceTest {

    @Mock private JiraAttributes jiraAttributes;
    @Mock private JiraService jiraService;
    @Mock private SessionManager sessionManager;
    @Mock private RequestContext requestContext;
    @Mock private KeyValueStore keyValueStore;
    @Mock private AuthorizationListener authorizationListener;
    @Mock private JsmAuditService jsmAuditService;
    @Mock private KeyValueAuditStore auditStore;

    private JsmAuthUiResource resource;

    private static final String INVOKER_ID = "test-invoker-id";

    @BeforeEach
    void setUp() {
        when(requestContext.getInvokerId()).thenReturn(INVOKER_ID);
        when(jsmAuditService.getAuditStore()).thenReturn(auditStore);
        resource = new JsmAuthUiResource(
                jiraAttributes, jiraService, sessionManager, requestContext,
                keyValueStore, authorizationListener, jsmAuditService);
    }

    // ── loadAttributes ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("loadAttributes")
    class LoadAttributesTests {

        @Test
        @DisplayName("Returns the injected JiraAttributes regardless of key value")
        void loadAttributes_returnsInjectedJiraAttributes() {
            assertSame(jiraAttributes, resource.loadAttributes("invoker-abc"));
        }

        @Test
        @DisplayName("Returns the injected JiraAttributes when key is null")
        void loadAttributes_nullKey_stillReturnsJiraAttributes() {
            assertSame(jiraAttributes, resource.loadAttributes(null));
        }
    }

    // ── saveAttributes ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("saveAttributes")
    class SaveAttributesTests {

        @Test
        @DisplayName("Always returns true — Krista attribute store is the system of record")
        void saveAttributes_alwaysReturnsTrue() {
            assertTrue(resource.saveAttributes(jiraAttributes, "invoker-abc"));
        }

        @Test
        @DisplayName("Returns true even when attributes argument is null")
        void saveAttributes_nullAttributes_returnsTrue() {
            assertTrue(resource.saveAttributes(null, "invoker-abc"));
        }
    }

    // ── onOAuthCallbackSuccess ─────────────────────────────────────────────

    @Nested
    @DisplayName("onOAuthCallbackSuccess")
    class OAuthCallbackSuccessTests {

        @Test
        @DisplayName("Notifies AuthorizationListener on a successful OAuth callback")
        void onOAuthCallbackSuccess_notifiesAuthorizationListener() {
            resource.onOAuthCallbackSuccess("refresh-token-key", "oauth-state-abc");

            verify(authorizationListener).authorized();
        }

        @Test
        @DisplayName("Notifies AuthorizationListener even when refreshTokenKey and state are null")
        void onOAuthCallbackSuccess_nullArgs_stillNotifiesAuthorizationListener() {
            resource.onOAuthCallbackSuccess(null, null);

            verify(authorizationListener).authorized();
        }
    }

    // ── removeTokens ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("removeTokens")
    class RemoveTokensTests {

        @Test
        @DisplayName("Removes app-wide token when OAuth credentials are present")
        void removeTokens_oAuthCredentialsPresent_removesToken() {
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);

            resource.removeTokens(jiraAttributes);

            verify(keyValueStore).remove(anyString());
        }

        @Test
        @DisplayName("Removes app-wide token when managed credentials are enabled (and OAuth not present)")
        void removeTokens_managedCredentialsEnabled_removesToken() {
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.isManagedCredentialsEnabled()).thenReturn(true);

            resource.removeTokens(jiraAttributes);

            verify(keyValueStore).remove(anyString());
        }

        @Test
        @DisplayName("Does not remove token when neither OAuth nor managed credentials are active")
        void removeTokens_noCredentials_doesNotRemoveToken() {
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.isManagedCredentialsEnabled()).thenReturn(false);

            resource.removeTokens(jiraAttributes);

            verify(keyValueStore, never()).remove(anyString());
        }
    }
}
