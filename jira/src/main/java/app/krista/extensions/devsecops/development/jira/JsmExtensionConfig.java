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

import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.sdk.atlassian.config.AtlassianProduct;
import app.krista.sdk.atlassian.registry.annotations.AtlassianExtension;
import app.krista.sdk.atlassian.registry.annotations.OAuthScope;

/**
 * Registry marker class carrying the Jira Service Management extension
 * metadata for the shared Atlassian SDK's extension registry.
 */
@AtlassianExtension(extensionType = "jira-service-management",
        product = AtlassianProduct.JIRA_SERVICE_MANAGEMENT,
        jaxrsId = JiraConstants.JAXRS_ID,
        displayName = "Jira Service Management")
@OAuthScope(value = "read:jira-user", description = "Read Jira user information",
        catalogRequests = {"Get Users Account Id", "Get User Account Information"})
@OAuthScope(value = "read:jira-work", description = "Read Jira issues and work items",
        catalogRequests = {"Get Issue By Key", "Get issues", "Get user issues",
                "Get my issues by given statuses", "Get user issue by issue type",
                "Fetch by JQL query", "Get Results for Fetch Issues by JQL Query",
                "Get all comments", "Get Custom Field Value"})
@OAuthScope(value = "write:jira-work", description = "Create and update Jira issues",
        catalogRequests = {"Create an issue", "Report a bug", "Update an issue",
                "Update status of issue", "Update status of multiple issues",
                "Update priority of issue", "Update the priority of multiple issues",
                "Assign a task to user", "Add a comment",
                "Add worklog", "Attach File to Ticket"})
@OAuthScope(value = "manage:jira-data-provider", description = "Manage webhooks and data providers",
        catalogRequests = {"Get Notification"})
@OAuthScope(value = "manage:jira-project", description = "Manage Jira projects, versions and releases",
        catalogRequests = {"Get release summary", "Assign Issue to Epic", "Assign Child Issue to Epic"})
@OAuthScope(value = "read:servicedesk-request", description = "Read service desk requests",
        catalogRequests = {"Get Request Type Field Value"})
@OAuthScope(value = "write:servicedesk-request", description = "Create and update service desk requests",
        catalogRequests = {"Add Request Participants to Issue"})
@OAuthScope(value = "manage:servicedesk-customer", description = "Manage service desk customers",
        catalogRequests = {"Create User", "Update User", "Delete User"})
public final class JsmExtensionConfig {

    private JsmExtensionConfig() {
    }
}
