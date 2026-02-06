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

import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.base.FreeForm;
import com.github.scribejava.core.model.OAuth2AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);

    @Inject
    public AuthenticationResource(SessionManager sessionManager, JiraService jiraService,
                                  RequestContext requestContext, AuthorizationContext context, AuthorizationListener authorizationListener, EventHandler eventHandler) {
        this.sessionManager = sessionManager;
        this.jiraService = jiraService;
        this.requestContext = requestContext;
        this.context = context;
        this.authorizationListener = authorizationListener;
        this.eventHandler = eventHandler;
    }

    @GET
    @Path("/callback")
    public String getCallBack(@QueryParam(JiraConstants.CODE) String code,
                              @QueryParam(JiraConstants.STATE) String state) {
        try {
            Map<String, Object> authTokenResponse = getAuthTokenResponse(code);
            String invokerId = requestContext.getInvokerId();
            sessionManager.storeRefToken(state + "#" + invokerId,
                    (String) authTokenResponse.get(JiraConstants.REFRESH_TOKEN));
            if (context.isAuthenticated()) {
                authorizationListener.authorized();
                return "Authentication successful. Please close the window and proceed ahead.";
            } else {
                return "Authentication is unsuccessful";
            }
        } catch (Exception cause) {
            throw new IllegalArgumentException("Error occurred during callback", cause);
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

    public Map<String, Object> getAuthTokenResponse(String code) {
        Map<String, Object> response = new HashMap<>();
        try {
            Objects.requireNonNull(code);
            OAuth2AccessToken accessToken = jiraService.getOauthService().getAccessToken(code);
            response.put(JiraConstants.REFRESH_TOKEN, accessToken.getRefreshToken());
        } catch (IOException | ExecutionException cause) {
            LOGGER.debug(cause.getMessage());
        } catch (InterruptedException cause) {
            LOGGER.debug(cause.getMessage());
            Thread.currentThread().interrupt();
        }
        return response;
    }

}
