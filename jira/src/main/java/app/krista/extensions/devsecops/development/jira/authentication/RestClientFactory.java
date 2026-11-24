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
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.client.RestClientSelector;
import app.krista.sdk.atlassian.oauth.ClientCredentialsTokenProvider;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

/**
 * Selects the REST client implementation matching the configured credentials.
 *
 * <p>Selection is delegated to the SDK's {@link RestClientSelector}, which
 * encodes the corrected rule (basic auth when fully configured, OAuth when
 * its credentials are present, configuration error otherwise) instead of the
 * previous silent fall-through to OAuth.</p>
 */
@Service
public class RestClientFactory {

    private final JiraAttributes jiraAttributes;
    private final OAuth2RestClient oAuth2RestClient;
    private final RequestContext requestContext;
    private final Invoker invoker;

    /** Lazily created 2LO client — cached so the provider's token cache survives across requests. */
    private volatile OAuth2RestClient serviceAccountClient;

    @Inject
    public RestClientFactory(JiraAttributes jiraAttributes, OAuth2RestClient oAuth2RestClient,
                             RequestContext requestContext, Invoker invoker) {
        this.jiraAttributes = jiraAttributes;
        this.oAuth2RestClient = oAuth2RestClient;
        this.requestContext = requestContext;
        this.invoker = invoker;
    }

    public boolean invokeAsUser() {
        return requestContext.invokeAsUser();
    }

    public RestClient getInstance(boolean invokeAsUser) {
        // When per-user auth is required and OAuth credentials are available,
        // force OAuth 2.0 (3LO) so the user-specific token lookup can trigger
        // MustAuthorizeException when the MCP user hasn't authenticated yet.
        if (invokeAsUser && jiraAttributes.areOAuthCredentialsPresent()) {
            return oAuth2RestClient;
        }
        return RestClientSelector.select(jiraAttributes,
                jiraAttributes.isServiceAccountModeEnabled(),
                () -> new AuthRestClient(invoker.getAttributes(), invokeAsUser, false),
                this::serviceAccountClient,
                () -> oAuth2RestClient);
    }

    /** 2LO (service-account) client over the client-credentials token provider. */
    private OAuth2RestClient serviceAccountClient() {
        OAuth2RestClient client = serviceAccountClient;
        if (client == null) {
            synchronized (this) {
                if (serviceAccountClient == null) {
                    serviceAccountClient = new OAuth2RestClient(new ClientCredentialsTokenProvider(jiraAttributes));
                }
                client = serviceAccountClient;
            }
        }
        return client;
    }

    public RestClient getInstance() {
        return getInstance(requestContext.invokeAsUser());
    }
}
