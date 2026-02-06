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

import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import com.google.gson.JsonObject;

public final class JiraUser {

    public final String accountId;
    public final String displayName;
    public final String accountType;
    public final String emailId;

    public JiraUser(String accountId, String displayName, String accountType, String emailId) {
        this.accountId = accountId;
        this.displayName = displayName;
        this.accountType = accountType;
        this.emailId = emailId;
    }

    public static JiraUser create(JsonObject projectObject) {
        return new JiraUser(
                projectObject.get("accountId").getAsString(),
                projectObject.get("displayName").getAsString(),
                projectObject.get("accountType").getAsString(),
                projectObject.has("emailAddress") ? projectObject.get("emailAddress").getAsString() : ""
        );
    }

    public String summary() {
        return displayName;
    }

    public User getUser() {
        User userFinal = new User();
        userFinal.accountId = this.accountId;
        userFinal.accountType = this.accountType;
        userFinal.email = this.emailId;
        userFinal.name = this.displayName;
        return userFinal;
    }

}
