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

package app.krista.extensions.devsecops.development.jira.mcp;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.sdk.atlassian.mcp.AbstractMcpSetupResource;
import app.krista.sdk.atlassian.mcp.DefaultMcpAuthBridge;
import app.krista.sdk.atlassian.mcp.McpProductConfig;

import javax.inject.Inject;
import javax.ws.rs.Path;

/**
 * Jira Service Management MCP setup resource — thin subclass of the SDK's
 * {@link AbstractMcpSetupResource} that plugs in JSM product config and
 * bridges to the Authentication tab's credentials via {@link DefaultMcpAuthBridge}.
 */
@Path("/mcp-setup")
public class JsmMcpSetupResource extends AbstractMcpSetupResource {

    @Inject
    public JsmMcpSetupResource(SessionManager sessionManager, Invoker invoker,
                               JiraAttributes jiraAttributes, JiraService jiraService) {
        super(sessionManager, invoker,
              new DefaultMcpAuthBridge(jiraAttributes, jiraService,
                                      sessionManager, invoker.getInvokerId()));
    }

    @Override
    protected McpProductConfig getProductConfig() {
        return new JsmMcpProductConfig();
    }

    @Override
    protected String getJaxrsId() {
        return JiraConstants.JAXRS_ID;
    }
}
