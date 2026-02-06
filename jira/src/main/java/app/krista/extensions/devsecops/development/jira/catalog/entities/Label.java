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
        ecosystemVersion = "0fae7d39-6710-43ca-8f84-e0f20a150cc9")
@Entity(name = "Label", id = "localDomainEntity_40c80bb0-0d83-424f-8f2c-e941985a68fa", primaryKey = "Name", supportStore = true)
public class Label {

    @Searchable
    @ToString
    @Field(name = "Name", type = "Text", required = false)
    public final String name;

    public Map<String, Object> toFields() {
        Label label = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Name", label.name);
        return fieldMap;
    }

    public Label(String name) {
        this.name = name;
    }

}