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
        ecosystemVersion = "f376e5a4-a15d-4a3f-b274-cafb36d98bc0")
@Entity(name = "Subtask", id = "localDomainEntity_a021b0e2-d62c-41c6-83f7-3bdc278fae9c", primaryKey = "Key", supportStore = true)
public class Subtask {

    @Field.Desc(name = "Comments", type = "[ Text ]", required = false)
    public List<String> comments;

    @Searchable
    @ToString
    @Field(name = "Summary", type = "Text")
    public String summary;

    @Field(name = "Reporter Name", type = "Text", required = false)
    public String reporterName;

    @Field(name = "Assignee Name", type = "Text", required = false)
    public String assigneeName;

    @Field(name = "Task Type", type = "Text", required = false)
    public String taskType;

    @Searchable
    @ToString
    @Field(name = "Key", type = "Text", required = false)
    public String key;

    @Field(name = "Id", type = "Text", required = false)
    public String id;

    @Field(name = "Parent Key", type = "Text")
    public String parentKey;

    @Field(name = "Project Key", type = "Text")
    public String projectKey;

    @Field(name = "Priority", type = "Text", required = false)
    public String priority;

    @ToString
    @Field(name = "Status", type = "Text", required = false)
    public String status;

    @Searchable
    @Field(name = "Description", type = "RichText")
    public String description;

    public Map<String, Object> toFields() {
        Subtask subtask = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Comments", subtask.comments);
        fieldMap.put("Summary", subtask.summary);
        fieldMap.put("Reporter Name", subtask.reporterName);
        fieldMap.put("Assignee Name", subtask.assigneeName);
        fieldMap.put("Task Type", subtask.taskType);
        fieldMap.put("Key", subtask.key);
        fieldMap.put("Id", subtask.id);
        fieldMap.put("Parent Key", subtask.parentKey);
        fieldMap.put("Project Key", subtask.projectKey);
        fieldMap.put("Priority", subtask.priority);
        fieldMap.put("Status", subtask.status);
        fieldMap.put("Description", subtask.description);
        return fieldMap;
    }
}
