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

package app.krista.extensions.devsecops.development.jira.catalog.entityrequests.impl;

import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extensions.devsecops.development.jira.catalog.entities.IssueType;
import app.krista.extensions.devsecops.development.jira.catalog.entityrequests.EntityImplementationService;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class IssueTypeImpl extends EntityImplementationService {

    public IssueTypeImpl(TicketInfoDao ticketInfoDao) {
        super(ticketInfoDao);
    }

    @Override
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeFielde) {
        throw new IllegalArgumentException("Creation of new Issue Type not supported");
    }

    @Override
    public EntityValue update(EntityType entityType, EntityValue entity) {
        throw new IllegalArgumentException("Updation of Issue Type not supported");
    }

    @Override
    public List<EntityValue> search(EntityType entityType, SearchQuery searchQuery, Long pageIndex, Integer pageSize) {
        List<SearchCondition> conditions = searchQuery.getSearchConditions();
        final long skipSize = Math.max((pageIndex - 1) * pageSize.longValue(), 0);
        List<EntityValue> list = new ArrayList<>();
        for (SearchCondition condition : conditions) {
            String selector = condition.getFieldName();
            if ("name".equalsIgnoreCase(selector)) {
                String matchPattern = condition.getOperand() != null ? String.valueOf(condition.getOperand()).trim() : "";
                list = getIssueTypesByFilter(matchPattern, skipSize, pageSize, entityType.getEntityName());
            }
        }
        if (conditions.size() == 1 && list.isEmpty()) {
            list = getAllIssueTypes(skipSize, pageSize, entityType.getEntityName());
        }
        return list;
    }

    private List<EntityValue> getIssueTypesByFilter(String matchPattern, long skipSize, Integer pageSize, String entityName) {
        return getTicketInfoService().getIssueTypes().stream()
                .filter(status -> status.name.toLowerCase().contains(matchPattern.toLowerCase()))
                .skip(skipSize).limit(pageSize)
                .map(issueType -> new EntityValue(entityName, issueType.toFields()))
                .collect(Collectors.toList());
    }

    private List<EntityValue> getAllIssueTypes(Long skipSize, Integer pageSize, String entityName) {
        return getTicketInfoService().getIssueTypes().stream()
                .skip(skipSize).limit(pageSize)
                .map(issueType -> new EntityValue(entityName, issueType.toFields()))
                .collect(Collectors.toList());
    }

    @Override
    public EntityValue get(EntityType entityType, String primaryKey) {
        IssueType issueType = getTicketInfoService().getIssueTypes().stream().filter(status -> status.id.equalsIgnoreCase(primaryKey))
                .findFirst().orElseThrow(() -> new RuntimeException("Issue Type not found for " + primaryKey));
        return new EntityValue(entityType.getEntityName(), issueType.toFields());
    }
}
