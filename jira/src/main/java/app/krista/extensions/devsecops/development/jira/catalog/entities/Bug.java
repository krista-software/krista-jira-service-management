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
        ecosystemVersion = "12c57bb9-5b53-4ff7-b51f-aecbbfc7173c")
@Entity(name = "Bug", id = "localDomainEntity_65413b5b-acbb-49d3-8a6b-5de7dacce1ef", primaryKey = "Bug Key", supportStore = true)
public class Bug {

    @Field.Desc(name = "Comments", type = "[ Text ]", required = false)
    public List<String> comments;

    @ToString
    @Searchable
    @Field(name = "Summary", type = "Text")
    public String summary;

    @Field(name = "Reporter Name", type = "Text", required = false)
    public String reporterName;

    @Field(name = "Assignee Name", type = "Text", required = false)
    public String assigneeName;

    @Field(name = "Type", type = "Text", required = false)
    public String type;
    @Searchable
    @ToString
    @Field(name = "Bug Key", type = "Text", required = false)
    public String bugKey;

    @Searchable
    @Field(name = "Id", type = "Text", required = false)
    public String id;

    @Field(name = "Project Key", type = "Text")
    public String projectKey;

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
        Bug bug = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Summary", bug.summary);
        fieldMap.put("Comments", bug.comments);
        fieldMap.put("Reporter Name", bug.reporterName);
        fieldMap.put("Assignee Name", bug.assigneeName);
        fieldMap.put("Bug Key", bug.bugKey);
        fieldMap.put("Id", bug.id);
        fieldMap.put("Project Key", bug.projectKey);
        fieldMap.put("Description", bug.description);
        fieldMap.put("Type", bug.type);
        fieldMap.put("Priority", bug.priority);
        fieldMap.put("Status", bug.status);
        return fieldMap;
    }
}