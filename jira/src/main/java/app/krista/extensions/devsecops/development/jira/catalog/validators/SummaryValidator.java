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

package app.krista.extensions.devsecops.development.jira.catalog.validators;

import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;

import java.util.Map;

public class SummaryValidator implements Validator {
    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        return context.get(ValidationResource.SUMMARY).length() <= 255;
    }

    @Override
    public String getFetchFieldName() {
        return JiraResources.SUMMARY;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter summary up to 255 characters.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided summary: %s more than 255 characters", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Provided summary is more than 255 characters: %s", resourceId);
    }
}
