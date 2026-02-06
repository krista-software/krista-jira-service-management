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
        ecosystemVersion = "3a0013af-cb88-411e-83f4-f02ad1ea5aa2")
@Entity(name = "Epic", id = "localDomainEntity_1d6d49bd-74d1-4c71-97e9-48cdc85034f4", primaryKey = "Epic Key", supportStore = true)
public class Epic {

    @Searchable
    @ToString
    @Field(name = "Summary", type = "Text")
    public String summary;

    @Field.Desc(name = "Comments", type = "[ Text ]", required = false)
    public List<String> comments;

    @Field(name = "Reporter Name", type = "Text", required = false)
    public String reporterName;

    @Field(name = "Assignee Name", type = "Text", required = false)
    public String assigneeName;

    @Field(name = "Type", type = "Text", required = false)
    public String type;
    @Searchable
    @ToString
    @Field(name = "Epic Key", type = "Text", required = false)
    public String epicKey;

    @Field(name = "Id", type = "Text", required = false)
    public String id;

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
        Epic epic = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Summary", epic.summary);
        fieldMap.put("Comments", epic.comments);
        fieldMap.put("Reporter Name", epic.reporterName);
        fieldMap.put("Assignee Name", epic.assigneeName);
        fieldMap.put("Type", epic.type);
        fieldMap.put("Epic Key", epic.epicKey);
        fieldMap.put("Id", epic.id);
        fieldMap.put("Project Key", epic.projectKey);
        fieldMap.put("Priority", epic.priority);
        fieldMap.put("Status", epic.status);
        fieldMap.put("Description", epic.description);
        return fieldMap;
    }

}