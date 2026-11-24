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
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Shared singleton audit service for the Jira Service Management extension.
 *
 * <p>Wraps the SDK's {@link KeyValueAuditStore} and provides safe convenience
 * methods that never throw — audit must never affect catalog execution.
 * Inject this service into catalog areas to record TRIGGERED / SUCCESS /
 * FAILURE events for the audit dashboard.</p>
 */
@Service
public final class JsmAuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsmAuditService.class);

    private final KeyValueAuditStore auditStore;

    @Inject
    public JsmAuditService(KeyValueStore keyValueStore) {
        this.auditStore = new KeyValueAuditStore(keyValueStore, "jsm");
    }

    /** Returns the underlying store (used by {@code JsmAuditResource}). */
    public KeyValueAuditStore getAuditStore() {
        return auditStore;
    }

    /** Records CATALOG_REQUEST_TRIGGERED (stamps request start time for duration). */
    public void triggered(String invokerId, String catalogName) {
        try {
            auditStore.record(invokerId, "CATALOG_REQUEST_TRIGGERED", "triggered",
                    catalogName, null, null, null);
        } catch (Exception e) {
            LOGGER.debug("Audit triggered failed for '{}': {}", catalogName, e.getMessage());
        }
    }

    /** Records CATALOG_REQUEST_SUCCESS (pairs with previous TRIGGERED for duration). */
    public void success(String invokerId, String catalogName) {
        try {
            auditStore.record(invokerId, "CATALOG_REQUEST_SUCCESS", "success",
                    catalogName, null, null, null);
        } catch (Exception e) {
            LOGGER.debug("Audit success failed for '{}': {}", catalogName, e.getMessage());
        }
    }

    /** Records CATALOG_REQUEST_FAILURE with reason and exception class. */
    public void failure(String invokerId, String catalogName, Throwable cause) {
        try {
            auditStore.record(invokerId, "CATALOG_REQUEST_FAILURE", "failure",
                    catalogName,
                    cause != null ? cause.getMessage() : null,
                    cause != null ? cause.getClass().getSimpleName() : null,
                    null);
        } catch (Exception e) {
            LOGGER.debug("Audit failure failed for '{}': {}", catalogName, e.getMessage());
        }
    }

    /** Records CATALOG_REQUEST_FAILURE + classified auth error event. */
    public void authFailure(String invokerId, String catalogName, Throwable cause) {
        try {
            auditStore.recordAuthError(invokerId, catalogName, cause);
        } catch (Exception e) {
            LOGGER.debug("Audit authFailure failed for '{}': {}", catalogName, e.getMessage());
        }
    }
}
