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

import app.krista.extensions.util.KeyValueStore;
import app.krista.sdk.atlassian.audit.KeyValueAuditStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JsmAuditService}.
 *
 * <p>Because {@code JsmAuditService} instantiates {@code new KeyValueAuditStore(...)} inside
 * its constructor, a mock {@link KeyValueAuditStore} is injected after construction via
 * reflection so that each audit method's happy path and exception-swallowing path can be
 * verified independently.</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JsmAuditService Tests")
class JsmAuditServiceTest {

    @Mock private KeyValueStore keyValueStore;
    @Mock private KeyValueAuditStore auditStore;

    private JsmAuditService service;

    private static final String INVOKER_ID = "test-invoker";
    private static final String CATALOG_NAME = "Create Ticket";

    @BeforeEach
    void setUp() throws Exception {
        service = new JsmAuditService(keyValueStore);
        // Replace the internally-created KeyValueAuditStore with a controllable mock
        Field field = JsmAuditService.class.getDeclaredField("auditStore");
        field.setAccessible(true);
        field.set(service, auditStore);
    }

    // ── getAuditStore ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getAuditStore returns the wrapped KeyValueAuditStore")
    void getAuditStore_returnsAuditStore() {
        assertSame(auditStore, service.getAuditStore());
    }

    // ── triggered ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("triggered")
    class TriggeredTests {

        @Test
        @DisplayName("Delegates to auditStore.record with CATALOG_REQUEST_TRIGGERED event type")
        void triggered_success_delegatesToAuditStore() {
            service.triggered(INVOKER_ID, CATALOG_NAME);

            verify(auditStore).record(INVOKER_ID, "CATALOG_REQUEST_TRIGGERED", "triggered",
                    CATALOG_NAME, null, null, null);
        }

        @Test
        @DisplayName("Silently swallows exception thrown by auditStore — never propagates")
        void triggered_exceptionFromAuditStore_doesNotPropagate() {
            doThrow(new RuntimeException("store failure"))
                    .when(auditStore).record(any(), any(), any(), any(), any(), any(), any());

            assertDoesNotThrow(() -> service.triggered(INVOKER_ID, CATALOG_NAME));
        }
    }

    // ── success ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("success")
    class SuccessTests {

        @Test
        @DisplayName("Delegates to auditStore.record with CATALOG_REQUEST_SUCCESS event type")
        void success_success_delegatesToAuditStore() {
            service.success(INVOKER_ID, CATALOG_NAME);

            verify(auditStore).record(INVOKER_ID, "CATALOG_REQUEST_SUCCESS", "success",
                    CATALOG_NAME, null, null, null);
        }

        @Test
        @DisplayName("Silently swallows exception thrown by auditStore — never propagates")
        void success_exceptionFromAuditStore_doesNotPropagate() {
            doThrow(new RuntimeException("store failure"))
                    .when(auditStore).record(any(), any(), any(), any(), any(), any(), any());

            assertDoesNotThrow(() -> service.success(INVOKER_ID, CATALOG_NAME));
        }
    }

    // ── failure ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("failure")
    class FailureTests {

        @Test
        @DisplayName("Passes cause message and simple class name when cause is non-null")
        void failure_withNonNullCause_passesMessageAndClassName() {
            RuntimeException cause = new RuntimeException("connection reset");

            service.failure(INVOKER_ID, CATALOG_NAME, cause);

            verify(auditStore).record(INVOKER_ID, "CATALOG_REQUEST_FAILURE", "failure",
                    CATALOG_NAME, "connection reset", "RuntimeException", null);
        }

        @Test
        @DisplayName("Passes null reason and null class name when cause is null")
        void failure_withNullCause_passesNullDetails() {
            service.failure(INVOKER_ID, CATALOG_NAME, null);

            verify(auditStore).record(INVOKER_ID, "CATALOG_REQUEST_FAILURE", "failure",
                    CATALOG_NAME, null, null, null);
        }

        @Test
        @DisplayName("Silently swallows exception thrown by auditStore — never propagates")
        void failure_exceptionFromAuditStore_doesNotPropagate() {
            doThrow(new RuntimeException("store failure"))
                    .when(auditStore).record(any(), any(), any(), any(), any(), any(), any());

            assertDoesNotThrow(() -> service.failure(INVOKER_ID, CATALOG_NAME, new RuntimeException("cause")));
        }
    }

    // ── authFailure ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("authFailure")
    class AuthFailureTests {

        @Test
        @DisplayName("Delegates to auditStore.recordAuthError with invokerId, catalogName, and cause")
        void authFailure_success_delegatesToRecordAuthError() {
            RuntimeException cause = new RuntimeException("OAuth token expired");

            service.authFailure(INVOKER_ID, CATALOG_NAME, cause);

            verify(auditStore).recordAuthError(INVOKER_ID, CATALOG_NAME, cause);
        }

        @Test
        @DisplayName("Silently swallows exception thrown by auditStore — never propagates")
        void authFailure_exceptionFromAuditStore_doesNotPropagate() {
            doThrow(new RuntimeException("store failure"))
                    .when(auditStore).recordAuthError(anyString(), anyString(), any());

            assertDoesNotThrow(() -> service.authFailure(INVOKER_ID, CATALOG_NAME, new RuntimeException("cause")));
        }
    }
}
