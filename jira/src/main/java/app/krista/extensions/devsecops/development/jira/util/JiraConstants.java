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

package app.krista.extensions.devsecops.development.jira.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JiraConstants {
    public static final String STATE = "state";
    public static final String STRONG = "strong";
    public static final String EM = "em";
    public static final String UNDERLINE = "underline";
    public static final String GROUP_NAMES = "Group Names";
    public static final String COMMENT = "Comment";
    public static final String COMMENT_VISIBILITY = "Comment Visibility";
    public static final String VALIDATION_RESULT = "Validation Results";
    public static final String ASSIGNEE = "Assignee";

    private JiraConstants() {
    }

    public static final Gson GSON = new Gson();
    public static final Gson GSON_BUILDER = new GsonBuilder().setPrettyPrinting().create();
    public static final String NA = "NA";
    public static final String ACCEPT = "Accept";
    public static final String X_ATLASSIAN_TOKEN = "X-Atlassian-Token";
    public static final String NO_CHECK = "no-check";
    public static final String BUG = "Bug";
    public static final String EPIC = "Epic";
    public static final String ISSUE = "Issue";
    public static final String ISSUE_TYPE = "Issue Type";
    public static final String STORY = "Story";
    public static final String TASK = "Task";
    public static final String SUBTASK = "Sub-task";
    public static final String SUBTASK_ENTITY = "Subtask";
    public static final String TICKET_PRIORITY = "Ticket Priority";
    public static final String TICKET_PROJECT = "Ticket Project";
    public static final String TICKET_STATUS = "Ticket Status";
    public static final String USER = "User";
    public static final String VERSION = "Version";
    public static final String AND = " AND ";
    public static final String LOGICAL_NAME_FIELD = "logicalName";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String CODE = "code";
    public static final String JAXRS_ID = "jiraextn";
    public static final String BEARER = "Bearer ";
    public static final String PROJECT_KEY = "Project Key";
    public static final String SUMMARY = "Summary";
    public static final String DESCRIPTION = "Description";
    public static final String COMMENTS = "Comments";
    public static final String REPORTER_NAME = "Reporter Name";
    public static final String ASSIGNEE_NAME = "Assignee Name";
    public static final String ID = "Id";
    public static final String TYPE = "Type";
    public static final String _TYPE = "type";

    public static final String PRIORITY = "Priority";
    public static final String STATUS = "Status";
    public static final String PARENT_KEY = "Parent Key";

    public static final String LABELS = "Labels";

    public static final String COMPONENTS = "Components";
    public static final String LABEL = "Label";
    public static final String COMPONENT = "Component";
    public static final String REQUEST_TYPE = "Request Type";
    public static final String _DESCRIPTION = "description";
    public static final String _SUMMARY = "summary";
    public static final String STORY_KEY = "story key";
    public static final long DEFAULT_PAGE_SIZE = 50;
    public static final long PAGE_NUMBER = 1;

    public static final String UPDATES_ON_JIRA = "Updates On Jira";
    public static final String JIRA_UPDATES = "Jira Updates";

    public static final String ISSUE_ = "issue";
    public static final String UPDATE = "Update";
    public static final String ISSUE_KEY = "Issue Key";
    public static final String AUTHORIZATION_PROMPT = "Authorize yourself and click Validate Attributes before saving changes to proceed further";

    public static final String CONTENT = "content";
    public static final String TEXT = "text";
    public static final String ATTRS = "attrs";
    public static final String STATE_ID = "stateId";

    public static final String ERRORS = "errors";
    public static final String QUERIES = "queries";
    public static final String QUERY = "query";

    public static final String JQL_QUERY = "JQL Query";
    public static final String VALUE = "value";
    public static final String FIELDS = "fields";
}
