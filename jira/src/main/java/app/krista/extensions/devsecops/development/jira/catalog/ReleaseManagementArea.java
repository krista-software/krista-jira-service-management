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

package app.krista.extensions.devsecops.development.jira.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.impl.ReleaseDao;
import app.krista.extensions.devsecops.development.jira.services.ReleaseSummaryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "3ef3d7f7-b7ce-42e9-bdc4-c1b07fc33436")
public final class ReleaseManagementArea {

    private final ReleaseDao releaseDao;
    private final Logger logger = LoggerFactory.getLogger(ReleaseManagementArea.class);

    @Inject
    public ReleaseManagementArea(ReleaseDao releaseDao) {
        this.releaseDao = releaseDao;
    }

    @CatalogRequest(
            id = "localDomainRequest_dc6e0837-e404-4ef3-8123-172e07c4df75",
            name = "Get release summary",
            description = "Get release summary",
            area = "Release Management",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field(name = "Total user stories", type = "Number")
    @Field(name = "Open user stories", type = "Number")
    @Field(name = "Total epics", type = "Number")
    @Field(name = "Total Tasks", type = "Number")
    @Field(name = "Open Tasks", type = "Number")
    @Field(name = "Total Bugs", type = "Number")
    @Field(name = "Open Bugs", type = "Number")
    @Field(name = "Release day", type = "Date")
    @Field(name = "Days remaining", type = "Number")
    @Field.Desc(name = "User stories", type = "[ Entity(Issue) ]")
    @Field.Desc(name = "Epics", type = "[ Entity(Issue) ]")
    @Field.Desc(name = "Bugs", type = "[ Entity(Issue) ]")
    @Field.Desc(name = "Tasks", type = "[ Entity(Issue) ]")
    public ExtensionResponse getReleaseSummary(
            @Field(name = "Status", type = "Text") String status,
            @Field(name = "Assignee", type = "Text") String assignee,
            @Field(name = "Priority", type = "Text") String priority,
            @Field(name = "Issue type", type = "Text") String issueType,
            @Field(name = "Page Size", type = "Number") Double pageSize,
            @Field(name = "Page Number", type = "Number") Double pageNumber,
            @Field(name = "Fix version", type = "Text") String fixVersion) {
        try {
            long pSize = (pageSize != null && pageSize > 0) ? pageSize.longValue() : 20;
            long pNumber = (pageNumber != null && pageNumber > 0) ? pageNumber.longValue() : 1;
            ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(status, assignee, priority, issueType, pSize, pNumber, fixVersion);
            return ExtensionResponseFactory.create(response);
        } catch (Exception cause) {
            logger.error("Error occurred while getting release summary :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while getting release summary.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

}