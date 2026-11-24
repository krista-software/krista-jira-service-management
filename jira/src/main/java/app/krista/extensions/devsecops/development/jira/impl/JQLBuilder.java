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

package app.krista.extensions.devsecops.development.jira.impl;

public final class JQLBuilder {

    public static final String AND = " and ";
    private StringBuilder query;

    public JQLBuilder() {
        query = new StringBuilder();
    }

    public JQLBuilder issueType(String value) {
        if (isValid(value))
            query.append(" issuetype = \"").append(value).append("\" ").append(AND);
        return this;
    }

    public JQLBuilder status(String value) {
        if (isValid(value)) {
            query.append(" status = \"").append(value).append("\" ").append(AND);
        }
        return this;
    }

    public JQLBuilder priority(String value) {
        if (isValid(value))
            query.append(" priority = \"").append(value).append("\" ").append(AND);
        return this;
    }

    public JQLBuilder projectKey(String value) {
        if (isValid(value))
            query.append(" project = \"").append(value).append("\" ").append(AND);
        return this;
    }

    public JQLBuilder summary(String value) {
        if (isValid(value))
            query.append(" summary ~ \"").append(value).append("\" ").append(AND);
        return this;
    }

    public JQLBuilder text(String value) {
        if (isValid(value))
            query.append(" text ~ \"").append(value).append("\" ").append(AND);
        return this;
    }

    private boolean isValid(String value) {
        return value != null && !value.isBlank();
    }

    public JQLBuilder assignee(String value) {
        if (isValid(value))
            query.append(" assignee = \"").append(value).append("\" ").append(AND);
        return this;
    }

    public JQLBuilder fixVersion(String value) {
        if (isValid(value))
            query.append(" fixversion = \"").append(value).append("\" ").append(AND);
        return this;
    }

    public JQLBuilder orderBy(String value, String direction) {
        String q = removeEndsWith(query.toString(), AND);
        query = new StringBuilder(q);
        if (isValid(value) && (direction.equalsIgnoreCase("asc") || direction.equalsIgnoreCase("desc")))
            query.append(" ORDER BY ").append(value).append(" ").append(direction);
        return this;
    }

    public String build() {
        return removeEndsWith(query.toString(), AND);
    }

    public static String removeEndsWith(String s, String val) {
        if (s.endsWith(val)) {
            int index = s.lastIndexOf(val);
            if (index > 0) {
                return s.substring(0, index);
            }
        }
        return s;
    }

}