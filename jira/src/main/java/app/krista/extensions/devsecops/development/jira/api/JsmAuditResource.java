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
import app.krista.sdk.atlassian.audit.AbstractAuditResource;
import app.krista.sdk.atlassian.audit.AuditData;
import app.krista.sdk.atlassian.audit.KeyValueAuditStore;
import app.krista.sdk.atlassian.config.AuthMethod;

import javax.inject.Inject;
import javax.ws.rs.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * SDK-driven audit dashboard for the Jira Service Management extension.
 *
 * <p>The dashboard UI and JSON endpoints live in
 * {@link AbstractAuditResource}; persistence uses the shared
 * {@link JsmAuditService}'s {@link KeyValueAuditStore} (bounded, KV-backed),
 * so this class only contributes JSM's credential health summary.</p>
 */
@Path("/sdk/audit")
public final class JsmAuditResource extends AbstractAuditResource {

    private final KeyValueAuditStore auditStore;
    private final JiraAttributes jiraAttributes;
    private final SessionManager sessionManager;

    @Inject
    public JsmAuditResource(JsmAuditService jsmAuditService, RequestContext requestContext,
                            JiraAttributes jiraAttributes, SessionManager sessionManager) {
        super(requestContext.getInvokerId());
        this.auditStore = jsmAuditService.getAuditStore();
        this.jiraAttributes = jiraAttributes;
        this.sessionManager = sessionManager;
    }

    @Override
    protected AuditData getAuditData(String invokerId, Long from, Long to, String type, String status) {
        return auditStore.getData(invokerId, from, to, type, status);
    }

    @Override
    protected void clearAuditRecords(String invokerId, Long from, Long to) {
        auditStore.clearRecords(invokerId, from, to);
    }

    @Override
    protected Map<String, Object> getGlobalCounter() {
        return auditStore.getGlobalCounter();
    }

    @Override
    protected Map<String, Object> buildHealthResponse() {
        Map<String, Object> health = new HashMap<>();
        try {
            if (jiraAttributes.areBasicCredentialsPresent()) {
                health.put("authMethod", AuthMethod.BASIC.name());
                health.put("hostUrl", jiraAttributes.getHostUrl());
                health.put("userName", jiraAttributes.getUserName());
                health.put("hasRefreshToken", "N/A"); // basic auth uses no tokens
            } else if (jiraAttributes.areOAuthCredentialsPresent()) {
                boolean serviceAccount = jiraAttributes.isServiceAccountModeEnabled();
                health.put("authMethod", serviceAccount
                        ? AuthMethod.OAUTH2_CLIENT_CREDENTIALS.name()
                        : AuthMethod.OAUTH2.name());
                health.put("clientId", mask(jiraAttributes.getClientId()));
                if (serviceAccount) {
                    health.put("hasRefreshToken", "N/A"); // 2LO uses no refresh tokens
                } else {
                    String appKey = jiraAttributes.getClientId() + "#"
                            + jiraAttributes.getClientSecret() + "#" + getInvokerId();
                    String refreshToken = sessionManager.get(appKey);
                    health.put("hasRefreshToken", refreshToken != null && !refreshToken.isEmpty());
                }
            } else {
                health.put("authMethod", "not configured");
                health.put("hasRefreshToken", false);
            }
        } catch (Exception e) {
            health.put("authMethod", "error");
            health.put("hasRefreshToken", false);
        }
        return health;
    }
}
