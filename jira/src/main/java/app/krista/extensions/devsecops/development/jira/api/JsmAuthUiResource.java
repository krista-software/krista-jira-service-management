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
import app.krista.sdk.atlassian.extension.authentication.AbstractAtlassianAuthResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.Path;

/**
 * SDK-driven authentication tab for the Jira Service Management extension.
 *
 * <p>All endpoint logic (serving the auth HTML, get/save/test credentials,
 * 3LO authorize URL + callback, token removal) lives in
 * {@link AbstractAtlassianAuthResource}; this class wires JSM's attribute
 * service and {@link SessionManager} (refresh-token store) into the hooks.
 * Mounted under {@code /sdk} so the legacy {@code /callback} endpoint in
 * {@link AuthenticationResource} keeps working unchanged.</p>
 *
 * <p>Both managed (Quick Setup) and own-credentials OAuth callbacks are
 * handled by the SDK's {@code oauthCallback} at {@code /sdk/auth/callback}.
 * On success, {@link #onOAuthCallbackSuccess} notifies the
 * {@link AuthorizationListener} so the Krista runtime (including MCP
 * per-user flows) knows the user has completed authorization.</p>
 */
@Path("/sdk")
public final class JsmAuthUiResource extends AbstractAtlassianAuthResource<JiraAttributes> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsmAuthUiResource.class);

    private final JiraAttributes jiraAttributes;
    private final KeyValueStore keyValueStore;
    private final AuthorizationListener authorizationListener;

    @Inject
    public JsmAuthUiResource(JiraAttributes jiraAttributes, JiraService jiraService,
                             SessionManager sessionManager, RequestContext requestContext,
                             KeyValueStore keyValueStore,
                             AuthorizationListener authorizationListener,
                             JsmAuditService jsmAuditService) {
        super("jira-service-management", requestContext.getInvokerId(), sessionManager, jiraService,
                jsmAuditService.getAuditStore());
        this.jiraAttributes = jiraAttributes;
        this.keyValueStore = keyValueStore;
        this.authorizationListener = authorizationListener;
    }

    @Override
    protected JiraAttributes loadAttributes(String key) {
        return jiraAttributes;
    }

    @Override
    protected boolean saveAttributes(JiraAttributes attributes, String key) {
        // The validated candidate has already been applied in-memory via
        // BaseAtlassianAttributes#update; the Krista attribute screen remains
        // the system of record for invoker attributes.
        return true;
    }

    @Override
    protected void onOAuthCallbackSuccess(String refreshTokenKey, String state) {
        LOGGER.info("OAuth callback success — notifying AuthorizationListener (key='{}')", refreshTokenKey);
        authorizationListener.authorized();
    }

    @Override
    protected void removeTokens(JiraAttributes attributes) {
        restoreCredentialsModeFromSnapshot(attributes);
        if (attributes.areOAuthCredentialsPresent() || attributes.isManagedCredentialsEnabled()) {
            keyValueStore.remove(appWideTokenKey(attributes));
        }
    }
}
