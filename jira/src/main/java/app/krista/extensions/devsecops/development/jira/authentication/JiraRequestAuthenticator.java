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

import app.krista.ksdk.context.AuthorizationContext;
import app.krista.sdk.atlassian.authn.AbstractAtlassianRequestAuthenticator;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

/**
 * Request authenticator for the JSM extension. The OAuth callback plumbing
 * (state extraction, must-authorize redirect with audience/consent suffix)
 * lives in the shared {@link AbstractAtlassianRequestAuthenticator}.
 *
 * <p>Uses the SDK's {@code buildSecuredAuthorizationUrl()} so that user-level
 * authentication redirects produce properly secured OAuth state (CSRF nonce,
 * invokerId and forwardPath for managed mode) — identical to the auth-tab flow.</p>
 */
@Service
public final class JiraRequestAuthenticator extends AbstractAtlassianRequestAuthenticator {

    private final JiraService jiraService;

    @Inject
    public JiraRequestAuthenticator(JiraService jiraService,
                                    AuthorizationContext authorizationContext) {
        super(authorizationContext);
        this.jiraService = jiraService;
    }

    @Override
    protected String buildAuthorizationUrl(String accountId) {
        return jiraService.buildSecuredAuthorizationUrl(accountId);
    }
}
