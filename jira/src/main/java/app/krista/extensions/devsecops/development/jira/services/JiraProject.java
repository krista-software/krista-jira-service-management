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

package app.krista.extensions.devsecops.development.jira.services;

import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketProject;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import com.google.gson.JsonObject;

public final class JiraProject {
    public final String name;
    public final String type;
    public final String id;
    public final String key;
    public final String style;
    public final String assigneeType;
    public final JsonObject lead;

    public JiraProject(String name,
                       String type,
                       String id,
                       String key,
                       String style,
                       String assigneeType,
                       JsonObject lead) {
        this.name = name;
        this.type = type;
        this.id = id;
        this.key = key;
        this.style = style;
        this.assigneeType = assigneeType;
        this.lead = lead;
    }

    public static JiraProject create(JsonObject projectObject) {
        return new JiraProject(
                projectObject.get("name").getAsString(),
                projectObject.get("projectTypeKey").getAsString(),
                projectObject.get("id").getAsString(),
                projectObject.get("key").getAsString(),
                projectObject.get("style").getAsString(),
                projectObject.has("assigneeType") ? projectObject.get("assigneeType").getAsString() : JiraConstants.NA,
                projectObject.getAsJsonObject("lead")
        );
    }

    public TicketProject getProject() {
        TicketProject project = new TicketProject();
        project.projectId = id;
        project.key = key;
        project.name = name;
        project.type = type;
        project.assigneeType = assigneeType;
        project.style = style;
        project.leadAccountId = getLeadId();
        project.leadName = getLeadName();
        return project;
    }

    private String getLeadName() {
        try {
            if (lead != null) {
                return lead.get("displayName").getAsString();
            }
        } catch (Exception ignored) {

        }
        return JiraConstants.NA;
    }

    private String getLeadId() {
        try {
            if (lead != null) {
                return lead.get("accountId").getAsString();
            }
        } catch (Exception ignored) {

        }
        return JiraConstants.NA;
    }
}
