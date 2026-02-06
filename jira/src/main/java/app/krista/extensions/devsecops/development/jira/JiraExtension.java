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

package app.krista.extensions.devsecops.development.jira;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.*;
import app.krista.extensions.devsecops.development.jira.authentication.JiraRequestAuthenticator;

import javax.inject.Inject;
import java.util.Map;

@Java(version = Java.Version.JAVA_21)
@Field.Text(value = JiraAttributes.AUTH_TOKEN, isSecured = true, required = false)
@Field.Text(value = JiraAttributes.USER_NAME, required = false)
@Field.Text(value = JiraAttributes.HOST_URL, required = false)
@Field.Text(value = JiraAttributes.CLIENT_ID, required = false)
@Field.Text(value = JiraAttributes.CLIENT_SECRET, isSecured = true, required = false)
@Extension(name = "Jira Service Management", version = "1.0.1", jaxrsId = "jiraextn")
@StaticResource(path = "docs", file = "docs")
//Description = This extension integrates Krista with Jira and performs various operations related to Jira workflows
public final class JiraExtension {

    private final JiraRequestAuthenticator jiraRequestAuthenticator;
    private final Invoker invoker;
    private final JiraAttributes jiraAttributes;

    @Inject
    public JiraExtension(JiraRequestAuthenticator jiraRequestAuthenticator, Invoker invoker,
                         JiraAttributes jiraAttributes) {
        this.jiraRequestAuthenticator = jiraRequestAuthenticator;
        this.invoker = invoker;
        this.jiraAttributes = jiraAttributes;
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        jiraAttributes.testConnection(invoker.getAttributes());
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, Object> attributes) {
        jiraAttributes.setAttributes(attributes);
        jiraAttributes.validateAttributes(attributes);
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return jiraRequestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
    public void attributesUpdated(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {
        jiraAttributes.update(newAttributes);
    }
}
