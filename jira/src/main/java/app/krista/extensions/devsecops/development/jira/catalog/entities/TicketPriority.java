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
import java.util.Map;

@Searchable
@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "7339e9c9-76fd-4b24-bf95-4c5fdc3cef85")
@Entity(name = "Ticket Priority", id = "localDomainEntity_ac591738-5e26-450f-9968-01f6e1e0739f", primaryKey = "Id", supportStore = true)
public class TicketPriority {

    @Field(name = "Id", type = "Text", required = false)
    public String id;

    @Searchable
    @ToString
    @Field(name = "Name", type = "Text")
    public String name;

    @ToString
    @Field(name = "Description", type = "RichText", required = false)
    public String description;

    public TicketPriority() {
    }

    public TicketPriority(String name) {
        this.name = name;
    }

    public Map<String, Object> toFields() {
        TicketPriority ticketPriority = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Id", ticketPriority.id);
        fieldMap.put("Name", ticketPriority.name);
        fieldMap.put("Description", ticketPriority.description);
        return fieldMap;
    }

}