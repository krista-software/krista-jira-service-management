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

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedField;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public final class JiraRequestAuthenticator implements RequestAuthenticator {
    private static final String STATE = "state";
    private final JiraService jiraService;
    private final AuthorizationContext authorizationContext;
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraRequestAuthenticator.class);

    @Inject
    public JiraRequestAuthenticator(JiraService jiraService,
                                    AuthorizationContext authorizationContext) {
        this.jiraService = jiraService;
        this.authorizationContext = authorizationContext;
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return Collections.emptySet();
    }

    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        try {
            HttpRequest httpRequest = (HttpRequest) protoRequest;
            if (httpRequest.getUri().getPath().equals("/callback")) {
                httpRequest.bufferBody();
                MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
                if (queryParameters == null || queryParameters.get(STATE) == null || queryParameters.get(STATE).get(0) == null) {
                    return null;
                }
                return String.valueOf(queryParameters.get(STATE).get(0));
            } else {
                return null;
            }
        } catch (RuntimeException | IOException cause) {
            System.err.println("### IGNORING and returning null for getAuthenticatedAccountId :" + cause.getMessage());
            LOGGER.debug(cause.getMessage());
            return null;
        }
    }

    @Override
    public boolean setServiceAuthorization(String s) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return Map.of();
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException cause, ProtoRequest protoRequest) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException cause) {
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException cause, ProtoRequest protoRequest) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        String accountId = authorizationContext.getAuthorizedAccount().getAccountId();
        String location = jiraService.getOauthService().getAuthorizationUrl(accountId) + "&audience=api.atlassian.com&prompt=consent";
        return new AuthorizationResponse(location, List.of());
    }
}
