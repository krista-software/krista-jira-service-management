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

import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.oauth.AbstractAtlassianOAuthService;
import com.github.scribejava.core.model.OAuth2AccessToken;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * Jira Service Management OAuth 2.0 (3LO) service.
 *
 * <p>The token lifecycle (ScribeJava service construction, refresh-token
 * handling, accessible-resource resolution) lives in the shared Atlassian SDK
 * ({@link AbstractAtlassianOAuthService}); this class contributes only the
 * JSM-specific scopes (Jira scopes plus the Service Desk scopes), JAX-RS id
 * and authorization prompt, and keeps the legacy Jira-flavoured method names
 * as delegating wrappers for existing call sites.</p>
 */
@Service
public final class JiraService extends AbstractAtlassianOAuthService {

    public static final List<String> scopes = List.of("read:jira-user", "read:jira-work", "write:jira-work",
            "manage:jira-data-provider", "manage:jira-project",
            "read:servicedesk-request", "write:servicedesk-request", "manage:servicedesk-customer");

    @Inject
    public JiraService(RequestContext requestContext, SessionManager sessionManager, Invoker invoker,
            AuthorizationContext authorizationContext, JiraAttributes jiraAttributes) {
        super(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
    }

    @Override
    protected List<String> getScopes() {
        return scopes;
    }

    @Override
    protected String getJaxrsId() {
        return JiraConstants.JAXRS_ID;
    }

    @Override
    protected String getAuthorizationPrompt() {
        return JiraConstants.AUTHORIZATION_PROMPT;
    }

    // ── Legacy delegating methods kept for source compatibility ──

    public OAuth2AccessToken getJiraAccessToken(String accountId, String invokerId) {
        return getAccessToken(accountId, invokerId);
    }

    public OAuth2AccessToken getJiraAccessToken() {
        return getAccessToken();
    }

    public String getJiraToken() {
        return getToken();
    }

    public String getJiraTokenForIssues(String accountId, String invokerId) {
        return getToken(accountId, invokerId);
    }
}
