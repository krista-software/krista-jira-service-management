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

package app.krista.extensions.devsecops.development.jira.catalog.entities;

import app.krista.extension.impl.anno.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Searchable
@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "7339e9c9-76fd-4b24-bf95-4c5fdc3cef85")
@Entity(name = "Task", id = "localDomainEntity_1b51d92a-7a4b-412b-b7bd-29cd074b206f", primaryKey = "Task Key", supportStore = true)
public class Task {

    @Searchable
    @ToString
    @Field.Desc(name = "Comments", type = "[ Text ]", required = false)
    public List<String> comments;

    @Searchable
    @ToString
    @Field(name = "Summary", type = "Text", required = false)
    public String summary;

    @Searchable
    @ToString
    @Field(name = "Reporter Name", type = "Text", required = false)
    public String reporterName;

    @Searchable
    @ToString
    @Field(name = "Assignee Name", type = "Text", required = false)
    public String assigneeName;

    @Searchable
    @ToString
    @Field(name = "Task Type", type = "Text", required = false)
    public String taskType;

    @Searchable
    @ToString
    @Field(name = "Task Key", type = "Text", required = false)
    public String taskKey;

    @Field(name = "Id", type = "Text", required = false)
    public String id;

    @Field(name = "Project Key", type = "Text")
    public String projectKey;

    @Searchable
    @Field(name = "Priority", type = "Text", required = false)
    public String priority;

    @Searchable
    @ToString
    @Field(name = "Status", type = "Text", required = false)
    public String status;

    @Searchable
    @Field(name = "Description", type = "RichText")
    public String description;

    public Map<String, Object> toFields() {
        Task task = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Comments", task.comments);
        fieldMap.put("Summary", task.summary);
        fieldMap.put("Reporter Name", task.reporterName);
        fieldMap.put("Assignee Name", task.assigneeName);
        fieldMap.put("Task Type", task.taskType);
        fieldMap.put("Task Key", task.taskKey);
        fieldMap.put("Id", task.id);
        fieldMap.put("Project Key", task.projectKey);
        fieldMap.put("Priority", task.priority);
        fieldMap.put("Status", task.status);
        fieldMap.put("Description", task.description);
        return fieldMap;
    }

}