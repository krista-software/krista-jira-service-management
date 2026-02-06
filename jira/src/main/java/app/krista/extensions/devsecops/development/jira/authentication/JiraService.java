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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.AUTHORIZATION_PROMPT;
import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

@Service
public final class JiraService {

    public static final List<String> scopes = List.of("read:jira-user", "read:jira-work", "write:jira-work",
            "manage:jira-data-provider",
            "manage:jira-project",
            "read:servicedesk-request", "write:servicedesk-request", "manage:servicedesk-customer");
    private final SessionManager sessionManager;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final JiraAttributes jiraAttributes;
    private final String callBackUri;
    private OAuth20Service oAuthService;
    public static final String RESOURCE_URL = "https://api.atlassian.com/oauth/token/accessible-resources";
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraService.class);

    @Inject
    public JiraService(RequestContext requestContext, SessionManager sessionManager, Invoker invoker,
                       AuthorizationContext authorizationContext, JiraAttributes jiraAttributes) {
        this.sessionManager = sessionManager;
        this.requestContext = requestContext;
        callBackUri = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE) + "/" +
                JiraConstants.JAXRS_ID + "/callback";
        this.authorizationContext = authorizationContext;
        this.jiraAttributes = jiraAttributes;
        oAuthService = getOauthService();
    }

    public OAuth20Service getOauthService() {
        if (oAuthService == null && jiraAttributes.areOAuthCredentialsPresent() || (oAuthService != null && !oAuthService.getApiKey().equals(jiraAttributes.getClientId()) && !oAuthService.getApiSecret().equals(jiraAttributes.getClientSecret()))) {
            oAuthService = new ServiceBuilder(jiraAttributes.getClientId())
                    .apiSecret(jiraAttributes.getClientSecret())
                    .callback(callBackUri)
                    .defaultScope(String.join(" ", scopes) + " offline_access")
                    .build(JiraApi20.instance());
        }
        return oAuthService;
    }

    public OAuth2AccessToken getJiraAccessToken(String accountId, String invokerId) {
        try {
            if (accountId == null) {
                accountId = authorizationContext.getAuthorizedAccount().getAccountId();
            }
            if (invokerId == null) {
                invokerId = requestContext.getInvokerId();
            }
            String refreshToken = sessionManager.getRefToken(accountId + "#" + invokerId);
            if (refreshToken == null) {
                throw new MustAuthorizeException(AUTHORIZATION_PROMPT);
            }
            OAuth2AccessToken oAuth2AccessToken = getOauthService().refreshAccessToken(refreshToken);
            sessionManager.storeRefToken(accountId + "#" + invokerId, oAuth2AccessToken.getRefreshToken());
            return oAuth2AccessToken;
        } catch (ExecutionException cause) {
            System.err.println("Authorization error: :" + cause.getMessage());
            LOGGER.debug(cause.getMessage(), cause);
            return null;
        } catch (IOException | InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(cause);
        }
    }

    public OAuth2AccessToken getJiraAccessToken() {
        try {
            String accountId = authorizationContext.getAuthorizedAccount().getAccountId();
            String invokerId = requestContext.getInvokerId();
            String refreshToken = sessionManager.getRefToken(accountId + "#" + invokerId);
            if (refreshToken == null) {
                throw new MustAuthorizeException(AUTHORIZATION_PROMPT);
            }
            OAuth2AccessToken oAuth2AccessToken = getOauthService().refreshAccessToken(refreshToken);
            sessionManager.storeRefToken(accountId + "#" + invokerId, oAuth2AccessToken.getRefreshToken());
            return oAuth2AccessToken;
        } catch (ExecutionException | RuntimeException cause) {
            System.err.println("Authorization error: :" + cause.getMessage());
            LOGGER.debug(cause.getMessage());
            if (jiraAttributes.getClientId() == null || jiraAttributes.getClientSecret() == null) {
                throw new IllegalCallerException("Client ID and Client Secret are missing. Please provide them to authenticate using OAuth 2.0.");
            }
            throw new MustAuthorizeException(cause.getMessage());
        } catch (IOException | InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(cause);
        }
    }

    public String getJiraToken() {
        return Objects.requireNonNull(getJiraAccessToken()).getAccessToken();
    }

    public String getJiraTokenForIssues(String accountId, String invokerId) {
        return Objects.requireNonNull(getJiraAccessToken(accountId, invokerId)).getAccessToken();
    }

    public AccessibleResources getAccessibleResources() {
        OAuth2AccessToken jiraToken = Objects.requireNonNull(getJiraAccessToken());
        final OAuthRequest request = getOAuthRequest(jiraToken);
        getOauthService().signRequest(jiraToken, request);
        try (Response response = getOauthService().execute(request)) {
            if (response.getCode() == 400) {
                throw new IllegalStateException(
                        "Bad Request, Please enter correct required data. The response is " + response.getBody());
            }
            return getResources(response);
        } catch (MustAuthorizeException cause) {
            throw new MustAuthorizeException("Exception : " + cause.getMessage());
        } catch (Exception cause) {
            throw new IllegalStateException(cause.getMessage(), cause);
        }
    }

    public AccessibleResources getAccessibleResources(String accountId, String invokerId) {
        OAuth2AccessToken jiraToken = Objects.requireNonNull(getJiraAccessToken(accountId, invokerId));
        final OAuthRequest request = getOAuthRequest(jiraToken);
        getOauthService().signRequest(jiraToken, request);
        try (Response response = getOauthService().execute(request)) {
            if (response.getCode() == 400) {
                throw new IllegalStateException(
                        "Bad Request, Please enter correct required data. The response is " + response.getBody());
            }
            return getResources(response);
        } catch (MustAuthorizeException cause) {
            throw new MustAuthorizeException("Exception : " + cause.getMessage());
        } catch (Exception cause) {
            throw new IllegalStateException(cause.getMessage(), cause);
        }
    }

    private OAuthRequest getOAuthRequest(OAuth2AccessToken jiraToken) {
        final OAuthRequest request = new OAuthRequest(Verb.GET, RESOURCE_URL);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Authorization", JiraConstants.BEARER + jiraToken.getAccessToken());
        return request;
    }

    private AccessibleResources getResources(Response response) throws IOException {
        AccessibleResources[] accessibleResources = GSON.fromJson(response.getBody(), AccessibleResources[].class);
        if (accessibleResources.length == 0) {
            throw new IllegalStateException("We didn't found any resources to fulfill your requests.");
        } else {
            return getMultipleResources(accessibleResources);
        }
    }

    private AccessibleResources getMultipleResources(AccessibleResources[] accessibleResources) {
        if (accessibleResources.length == 1) {
            return accessibleResources[0];
        }
        for (AccessibleResources accessibleResource : accessibleResources) {
            boolean isScopeFulfilled = true;
            for (String scope : scopes) {
                if (!accessibleResource.scopes.contains(scope)) {
                    isScopeFulfilled = false;
                    break;
                }
            }
            if (isScopeFulfilled) {
                return accessibleResource;
            }
        }
        throw new IllegalStateException("None of the accessible resources has all the scopes required by this app.");
    }

    public static class AccessibleResources {

        private final String id;
        private final List<String> scopes;

        public AccessibleResources(String id, List<String> scopes) {
            this.id = id;
            this.scopes = scopes;
        }

        public String getId() {
            return id;
        }

    }

}
