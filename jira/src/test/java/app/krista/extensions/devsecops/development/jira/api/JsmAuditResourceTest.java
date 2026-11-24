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
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.audit.AuditData;
import app.krista.sdk.atlassian.audit.KeyValueAuditStore;
import app.krista.sdk.atlassian.config.AuthMethod;
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
 * Unit tests for JsmAuditResource.
 *
 * <p>Key assertions:
 * <ul>
 *   <li>Constructor uses the shared {@link KeyValueAuditStore} from
 *       {@link JsmAuditService#getAuditStore()} — not a newly created instance.</li>
 *   <li>Delegation to the store is correct for all audit operations.</li>
 *   <li>{@code buildHealthResponse()} reports the correct auth method.</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JsmAuditResource Tests")
class JsmAuditResourceTest {

    @Mock
    private JsmAuditService jsmAuditService;

    @Mock
    private RequestContext requestContext;

    @Mock
    private JiraAttributes jiraAttributes;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private KeyValueAuditStore auditStore;

    private JsmAuditResource jsmAuditResource;

    @BeforeEach
    void setUp() {
        when(requestContext.getInvokerId()).thenReturn("test-invoker-id");
        when(jsmAuditService.getAuditStore()).thenReturn(auditStore);

        jsmAuditResource = new JsmAuditResource(jsmAuditService, requestContext,
                jiraAttributes, sessionManager);
    }

    // ── Shared store wiring ────────────────────────────────────────────────

    @Nested
    @DisplayName("Shared Audit Store Tests")
    class SharedAuditStoreTests {

        @Test
        @DisplayName("Constructor calls jsmAuditService.getAuditStore() to obtain the shared store")
        void testConstructor_CallsGetAuditStore() {
            verify(jsmAuditService).getAuditStore();
        }

        @Test
        @DisplayName("getAuditData delegates to the shared store, not a new instance")
        void testGetAuditData_DelegatesToSharedStore() {
            AuditData mockData = mock(AuditData.class);
            when(auditStore.getData("test-invoker-id", 0L, Long.MAX_VALUE, null, null))
                    .thenReturn(mockData);

            AuditData result = jsmAuditResource.getAuditData(
                    "test-invoker-id", 0L, Long.MAX_VALUE, null, null);

            assertSame(mockData, result);
            verify(auditStore).getData("test-invoker-id", 0L, Long.MAX_VALUE, null, null);
        }

        @Test
        @DisplayName("clearAuditRecords delegates to the shared store")
        void testClearAuditRecords_DelegatesToSharedStore() {
            jsmAuditResource.clearAuditRecords("test-invoker-id", 0L, Long.MAX_VALUE);

            verify(auditStore).clearRecords("test-invoker-id", 0L, Long.MAX_VALUE);
        }

        @Test
        @DisplayName("getGlobalCounter delegates to the shared store")
        void testGetGlobalCounter_DelegatesToSharedStore() {
            Map<String, Object> mockCounter = new HashMap<>();
            mockCounter.put("total", 42L);
            when(auditStore.getGlobalCounter()).thenReturn(mockCounter);

            Map<String, Object> result = jsmAuditResource.getGlobalCounter();

            assertSame(mockCounter, result);
            verify(auditStore).getGlobalCounter();
        }

        @Test
        @DisplayName("Two JsmAuditResource instances using the same service share one store")
        void testTwoInstances_ShareSameStoreInstance() {
            // Simulate a second injection — getAuditStore() always returns the same instance
            JsmAuditResource second = new JsmAuditResource(jsmAuditService, requestContext,
                    jiraAttributes, sessionManager);

            // Both were constructed from the same service; the store reference is the same object
            second.getAuditData("test-invoker-id", 0L, Long.MAX_VALUE, null, null);
            jsmAuditResource.getAuditData("test-invoker-id", 0L, Long.MAX_VALUE, null, null);

            // Both calls route to the same mock store
            verify(auditStore, times(2))
                    .getData("test-invoker-id", 0L, Long.MAX_VALUE, null, null);
        }
    }

    // ── buildHealthResponse ────────────────────────────────────────────────

    @Nested
    @DisplayName("buildHealthResponse Tests")
    class BuildHealthResponseTests {

        @Test
        @DisplayName("Reports BASIC auth method when basic credentials present")
        void testBuildHealthResponse_BasicAuth() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
            when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
            when(jiraAttributes.getUserName()).thenReturn("test@example.com");

            Map<String, Object> health = jsmAuditResource.buildHealthResponse();

            assertEquals(AuthMethod.BASIC.name(), health.get("authMethod"));
            assertEquals("https://test.atlassian.net", health.get("hostUrl"));
            assertEquals("test@example.com", health.get("userName"));
            assertEquals("N/A", health.get("hasRefreshToken"));
        }

        @Test
        @DisplayName("Reports OAUTH2 auth method for 3LO OAuth credentials")
        void testBuildHealthResponse_OAuth2_ThreeLO() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
            when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(false);
            when(jiraAttributes.getClientId()).thenReturn("test-client-id");
            when(jiraAttributes.getClientSecret()).thenReturn("test-client-secret");
            // invokerId comes from requestContext.getInvokerId() set in setUp → "test-invoker-id"

            String appKey = "test-client-id#test-client-secret#test-invoker-id";
            when(sessionManager.get(appKey)).thenReturn(null);

            Map<String, Object> health = jsmAuditResource.buildHealthResponse();

            assertEquals(AuthMethod.OAUTH2.name(), health.get("authMethod"));
            assertFalse((Boolean) health.get("hasRefreshToken"));
        }

        @Test
        @DisplayName("Reports OAUTH2 with hasRefreshToken=true when refresh token exists")
        void testBuildHealthResponse_OAuth2_WithRefreshToken() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
            when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(false);
            when(jiraAttributes.getClientId()).thenReturn("cid");
            when(jiraAttributes.getClientSecret()).thenReturn("csec");
            // invokerId comes from requestContext.getInvokerId() set in setUp → "test-invoker-id"

            when(sessionManager.get("cid#csec#test-invoker-id")).thenReturn("some-refresh-token");

            Map<String, Object> health = jsmAuditResource.buildHealthResponse();

            assertEquals(AuthMethod.OAUTH2.name(), health.get("authMethod"));
            assertTrue((Boolean) health.get("hasRefreshToken"));
        }

        @Test
        @DisplayName("Reports OAUTH2_CLIENT_CREDENTIALS for service-account (2LO) mode")
        void testBuildHealthResponse_OAuth2_ServiceAccount() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
            when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(true);
            when(jiraAttributes.getClientId()).thenReturn("sa-client-id");

            Map<String, Object> health = jsmAuditResource.buildHealthResponse();

            assertEquals(AuthMethod.OAUTH2_CLIENT_CREDENTIALS.name(), health.get("authMethod"));
            assertEquals("N/A", health.get("hasRefreshToken"));
        }

        @Test
        @DisplayName("Reports 'not configured' when neither basic nor OAuth credentials present")
        void testBuildHealthResponse_NotConfigured() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);

            Map<String, Object> health = jsmAuditResource.buildHealthResponse();

            assertEquals("not configured", health.get("authMethod"));
            assertFalse((Boolean) health.get("hasRefreshToken"));
        }

        @Test
        @DisplayName("Returns error health map when jiraAttributes throws")
        void testBuildHealthResponse_HandlesException() {
            when(jiraAttributes.areBasicCredentialsPresent())
                    .thenThrow(new RuntimeException("simulated failure"));

            Map<String, Object> health = jsmAuditResource.buildHealthResponse();

            assertEquals("error", health.get("authMethod"));
            assertFalse((Boolean) health.get("hasRefreshToken"));
        }
    }
}
