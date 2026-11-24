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
        ecosystemVersion = "e69f2365-1bc2-47c8-b966-57c3b3604231")
@Entity(name = "Issue", id = "localDomainEntity_4735e2d8-bd37-4a7d-892f-dfd899ff2336", primaryKey = "Key", supportStore = true)
public class Issue {

    @Searchable
    @ToString
    @Field(name = "Summary", type = "Text")
    public String summary;

    @Field.Desc(name = "Comments", type = "[ Text ]", required = false)
    public List<String> comments;

    @Field(name = "Reporter Name", type = "Text", required = false)
    public String reporterName;

    @ToString
    @Field(name = "Assignee Name", type = "Text", required = false)
    public String assigneeName;

    @Searchable
    @ToString
    @Field(name = "Key", type = "Text", required = false)
    public String key;

    @Field(name = "Id", type = "Text", required = false)
    public String id;

    @Field(name = "Parent Key", type = "Text", required = false)
    public String parentKey;

    @Field(name = "Project Key", type = "Text")
    public String projectKey;

    @Searchable
    @Field(name = "Description", type = "RichText", required = false)
    public String description;

    @Searchable
    @Field(name = "Issue Type", type = "Text", required = false)
    public String issueType;

    @Field(name = "Priority", type = "Text", required = false)
    public String priority;

    @Searchable
    @ToString
    @Field(name = "Status", type = "Text", required = false)
    public String status;

    @Searchable
    @Field.Desc(name = "Labels", type = "[ Text ]", required = false)
    public List<String> labels;

    @Searchable
    @Field.Desc(name = "Components", type = "[ Text ]", required = false)
    public List<String> components;

    @Searchable
    @Field(name = "Request Type", type = "Text", required = false)
    public String requestType;

    @Field.Date(name = "Last Modified Date", required = false, includeTimeOfDay = true, allowPast = true, allowToday = true, allowFuture = true, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "dateFormat", value = "'MMM DD, yyyy h:mm:ss a'")}, options = {})
    public Long lastModifiedDate;


    public Map<String, Object> toFields() {
        Issue issue = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Summary", issue.summary);
        fieldMap.put("Comments", issue.comments);
        fieldMap.put("Reporter Name", issue.reporterName);
        fieldMap.put("Assignee Name", issue.assigneeName);
        fieldMap.put("Key", issue.key);
        fieldMap.put("Id", issue.id);
        fieldMap.put("Parent Key", issue.parentKey);
        fieldMap.put("Project Key", issue.projectKey);
        fieldMap.put("Description", issue.description);
        fieldMap.put("Issue Type", issue.issueType);
        fieldMap.put("Priority", issue.priority);
        fieldMap.put("Status", issue.status);
        fieldMap.put("Labels", issue.labels);
        fieldMap.put("Components", issue.components);
        fieldMap.put("Request Type", issue.requestType);
        fieldMap.put("Last Modified Date", issue.lastModifiedDate);
        return fieldMap;
    }

}
