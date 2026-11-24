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
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketPriority;
import app.krista.extensions.devsecops.development.jira.catalog.entityrequests.EntityImplementationService;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TicketPriorityImpl extends EntityImplementationService {

    public TicketPriorityImpl(TicketInfoDao ticketInfoDao) {
        super(ticketInfoDao);
    }

    @Override
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField) {
        throw new IllegalArgumentException("Creation of new Priority not supported");
    }

    @Override
    public EntityValue update(EntityType entityType, EntityValue entity) {
        throw new IllegalArgumentException("Updation of Priority not supported");
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
                list = getPrioritiesByFilter(matchPattern, skipSize, pageSize, entityType.getEntityName());
            }
        }
        if (conditions.size() == 1 && list.isEmpty()) {
            list = getAllPriorities(skipSize, pageSize, entityType.getEntityName());
        }
        return list;
    }

    private List<EntityValue> getPrioritiesByFilter(String matchPattern, long skipSize, Integer pageSize, String entityName) {
        return getTicketInfoService().getPriorities().stream()
                .filter(status -> status.name.toLowerCase().contains(matchPattern.toLowerCase()))
                .skip(skipSize).limit(pageSize)
                .map(ticketPriority -> new EntityValue(entityName, ticketPriority.toFields()))
                .collect(Collectors.toList());
    }


    private List<EntityValue> getAllPriorities(long skipSize, Integer pageSize, String entityName) {
        return getTicketInfoService().getPriorities().stream()
                .skip(skipSize).limit(pageSize)
                .map(ticketPriority -> new EntityValue(entityName, ticketPriority.toFields()))
                .collect(Collectors.toList());
    }


    @Override
    public EntityValue get(EntityType entityType, String primaryKey) {
        TicketPriority ticketPriority = getTicketInfoService().getPriorities().stream().filter(status -> status.id.equalsIgnoreCase(primaryKey))
                .findFirst().orElseThrow(() -> new RuntimeException("Priority not found for " + primaryKey));
        return new EntityValue(entityType.getEntityName(), ticketPriority.toFields());
    }
}
