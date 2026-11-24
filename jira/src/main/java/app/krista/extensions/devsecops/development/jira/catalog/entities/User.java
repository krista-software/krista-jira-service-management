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
@Entity(name = "User", id = "localDomainEntity_c68856a4-72c6-4c49-9a3d-08fc863c72c4", primaryKey = "Account Id", supportStore = true)
public class User {

    @Searchable
    @ToString
    @Field(name = "Name", type = "Text")
    public String name;

    @Field(name = "Account Id", type = "Text")
    public String accountId;

    @Searchable
    @ToString
    @Field(name = "Email", type = "Text")
    public String email;

    @Field(name = "Account Type", type = "Text")
    public String accountType;

    @Field(name = "Self", type = "Text", required = false)
    public String self;

    @Field(name = "Display Name", type = "Text", required = false)
    public String displayName;

    @Field.Desc(name = "Application Keys", type = "[ Text ]", required = false)
    public List<String> applicationKeys;


    public Map<String, Object> toFields() {
        User user = this;
        Map<String, Object> fieldMap = new LinkedHashMap<>();
        fieldMap.put("Name", user.name);
        fieldMap.put("Account Id", user.accountId);
        fieldMap.put("Email", user.email);
        fieldMap.put("Account Type", user.accountType);
        return fieldMap;
    }

}
