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
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.base.FreeForm;
import app.krista.sdk.atlassian.extension.authentication.AbstractAtlassianAuthResource;
import app.krista.sdk.atlassian.oauth.OAuthStateManager;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.JIRA_UPDATES;
import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.UPDATES_ON_JIRA;

@Path("/")
public final class AuthenticationResource {
    private final SessionManager sessionManager;
    private final JiraService jiraService;
    private final RequestContext requestContext;
    private final AuthorizationContext context;
    private final AuthorizationListener authorizationListener;
    private final EventHandler eventHandler;
    private final JiraAttributes jiraAttributes;
    private static final String HASH = "#";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);

    @Inject
    public AuthenticationResource(SessionManager sessionManager, JiraService jiraService,
                                  RequestContext requestContext, AuthorizationContext context,
                                  AuthorizationListener authorizationListener, EventHandler eventHandler,
                                  JiraAttributes jiraAttributes) {
        this.sessionManager = sessionManager;
        this.jiraService = jiraService;
        this.requestContext = requestContext;
        this.context = context;
        this.authorizationListener = authorizationListener;
        this.eventHandler = eventHandler;
        this.jiraAttributes = jiraAttributes;
    }

    /**
     * OAuth 2.0 (3LO) callback endpoint for own-credentials mode.
     *
     * <p>Delegates to the same token-exchange and storage logic used by the SDK
     * auth tab callback ({@link AbstractAtlassianAuthResource#oauthCallback}),
     * including CSRF state validation via {@link OAuthStateManager} and proper
     * per-user vs app-wide token key separation.</p>
     */
    @GET
    @Path("/callback")
    @Produces(MediaType.TEXT_HTML)
    public String getCallBack(@QueryParam(JiraConstants.CODE) String code,
                              @QueryParam(JiraConstants.STATE) String state) {
        LOGGER.info("Legacy OAuth callback reached: code={} state='{}'",
                (code != null && !code.isBlank()) ? "present(" + code.length() + " chars)" : "MISSING",
                state);

        if (code == null || code.isBlank()) {
            LOGGER.warn("OAuth callback: no authorization code received — user may have cancelled");
            return AbstractAtlassianAuthResource.callbackErrorHtml(
                    "No authorization code received — the user may have cancelled.");
        }

        // Extract accountId from state. The state may be CSRF-protected
        // ("accountId.nonce") or plain ("accountId") from legacy flows.
        // Try CSRF validation first; fall back to using raw state as accountId.
        String accountId = null;
        if (state != null && !state.isBlank()) {
            OAuthStateManager stateManager = new OAuthStateManager(sessionManager);
            accountId = stateManager.validateAndConsume(state);
            if (accountId == null) {
                // CSRF nonce not found — likely a legacy flow where state is just the accountId.
                // Use the raw state as the accountId to remain backward-compatible.
                LOGGER.info("OAuth callback: CSRF nonce not found for state — using raw state as accountId");
                accountId = state;
            } else {
                LOGGER.info("OAuth callback CSRF check passed: accountId='{}'", accountId);
            }
        }

        try {
            // This legacy callback endpoint is exclusively for own-credentials OAuth.
            // Force "own" mode so that isManagedCredentialsEnabled() resolves correctly
            // even when the user's credentials haven't been persisted yet (e.g. during
            // Test Connection before Save). Without this, the fallback in
            // isManagedCredentialsEnabled() can pick up managed credentials, causing
            // "authorization_code has mismatched aud" on token exchange.
            jiraService.forceCredentialsMode("own");

            OAuth20Service oauthService = jiraService.getOauthService();
            if (oauthService == null) {
                LOGGER.error("OAuth service unavailable during callback — credentials may not be configured. "
                        + "managed={} clientId={} invokerId='{}'",
                        jiraAttributes.isManagedCredentialsEnabled(),
                        jiraAttributes.getEffectiveClientId() != null ? "present" : "NULL",
                        requestContext.getInvokerId());
                return AbstractAtlassianAuthResource.callbackErrorHtml(
                        "OAuth service is not configured. Please ensure credentials are saved in the Authentication tab.");
            }

            LOGGER.info("OAuth callback: exchanging authorization code for tokens — clientId='{}'",
                    oauthService.getApiKey() != null ? oauthService.getApiKey().substring(0, Math.min(6, oauthService.getApiKey().length())) + "..." : "null");
            OAuth2AccessToken accessToken = oauthService.getAccessToken(code);
            String refreshToken = accessToken.getRefreshToken();
            String invokerId = requestContext.getInvokerId();
            LOGGER.info("OAuth callback: token exchange successful — refreshToken={}",
                    refreshToken != null ? "present" : "MISSING");

            String appKey = oauthService.getApiKey() + HASH
                    + oauthService.getApiSecret() + HASH + invokerId;

            if (accountId != null && !accountId.isBlank()) {
                // Per-user (invokeAsUser) flow: store ONLY under user-specific key
                String userKey = accountId + HASH + invokerId;
                sessionManager.store(userKey, refreshToken);
                LOGGER.info("OAuth refresh token stored under user-specific key for invoker {}", invokerId);
            } else {
                // Admin / app-wide flow: store under app-wide key
                sessionManager.store(appKey, refreshToken);
                LOGGER.info("OAuth refresh token stored under app-wide key for invoker {}", invokerId);
            }

            if (refreshToken != null && !refreshToken.isBlank()) {
                // Notify the platform — failures here must NOT prevent the success
                // response because tokens have already been persisted.
                try {
                    authorizationListener.authorized();
                } catch (Exception listenerFailure) {
                    LOGGER.warn("authorizationListener.authorized() failed (tokens already stored, ignoring): {} ({})",
                            listenerFailure.getMessage(), listenerFailure.getClass().getSimpleName());
                }
                return AbstractAtlassianAuthResource.CALLBACK_SUCCESS_HTML;
            } else {
                return AbstractAtlassianAuthResource.callbackErrorHtml(
                        "Authentication unsuccessful — no refresh token received.");
            }
        } catch (Exception cause) {
            LOGGER.error("OAuth callback failed: {} ({})", cause.getMessage(),
                    cause.getClass().getSimpleName(), cause);
            String msg = cause.getMessage();
            if (cause.getCause() != null) msg += " | " + cause.getCause().getMessage();
            return AbstractAtlassianAuthResource.callbackErrorHtml("Authentication failed: " + msg);
        }
    }

    @POST
    @Path("/jirawebhook")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getJiraWebhookResponse(String webhookData) {
        if (!webhookData.isEmpty()) {
            FreeForm freeForm = new FreeForm();
            freeForm.put(UPDATES_ON_JIRA, "Text", webhookData);
            eventHandler.handleEvent(JIRA_UPDATES, freeForm);
        }
        return Response.status(Response.Status.OK).entity("Webhook received and processed").build();
    }

}
