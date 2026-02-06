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
        ecosystemVersion = "33aa9347-d825-447c-be08-a2b02f8af859")
@Entity(name = "Story", id = "localDomainEntity_5faf6082-c1d7-4cfa-b23b-d23ce2a22877", primaryKey = "Story Key", supportStore = true)
public class Story {

    @Searchable
    @ToString
    @Field(name = "Summary", type = "Text", required = false)
    public String summary;

    @Field.Desc(name = "Comments", type = "[ Text ]", required = false)
    public List<String> comments;

    @Field(name = "Reporter Name", type = "Text", required = false)
    public String reporterName;

    @Field(name = "Assignee Name", type = "Text", required = false)
    public String assigneeName;

    @Field(name = "Story Type", type = "Text", required = false)
    public String storyType;

    @Searchable
    @ToString
    @Field(name = "Story Key", type = "Text", required = false)
    public String storyKey;

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
        Story story = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Comments", story.comments);
        fieldMap.put("Summary", story.summary);
        fieldMap.put("Reporter Name", story.reporterName);
        fieldMap.put("Assignee Name", story.assigneeName);
        fieldMap.put("Story Type", story.storyType);
        fieldMap.put("Story Key", story.storyKey);
        fieldMap.put("Id", story.id);
        fieldMap.put("Project Key", story.projectKey);
        fieldMap.put("Priority", story.priority);
        fieldMap.put("Status", story.status);
        fieldMap.put("Description", story.description);
        return fieldMap;
    }

}
