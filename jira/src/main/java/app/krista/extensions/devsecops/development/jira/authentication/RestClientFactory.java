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
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

@Service
public class RestClientFactory {

    private final JiraAttributes jiraAttributes;
    private final OAuth2RestClient oAuth2RestClient;
    private final RequestContext requestContext;
    private final Invoker invoker;

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
        RestClient restClient;
        if (jiraAttributes.areBasicCredentialsPresent() && !invokeAsUser) {
            System.out.println("Auth Rest Client created");
            restClient = new AuthRestClient(invoker.getAttributes(), false, false);
        } else {
            System.out.println("OAuth Rest Client created");
            restClient = oAuth2RestClient;
        }
        return restClient;
    }

    public RestClient getInstance() {
        return getInstance(requestContext.invokeAsUser());
    }


}
