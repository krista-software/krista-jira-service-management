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
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketProject;
import app.krista.extensions.devsecops.development.jira.catalog.entityrequests.EntityImplementationService;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public final class TicketProjectImpl extends EntityImplementationService {

    public TicketProjectImpl(ProjectDao projectDao) {
        super(projectDao);
    }

    @Override
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField) {
        throw new IllegalArgumentException("Creation of new Project not supported");
    }

    @Override
    public EntityValue update(EntityType entityType, EntityValue entity) {
        throw new IllegalArgumentException("Updation of Project not supported");
    }

    @Override
    public List<EntityValue> search(EntityType entityType, SearchQuery searchQuery, Long pageIndex, Integer pageSize) {
        List<SearchCondition> conditions = searchQuery.getSearchConditions();
        final long skipSize = Math.max((pageIndex - 1) * pageSize.longValue(), 0);
        List<EntityValue> list = new ArrayList<>();
        for (SearchCondition condition : conditions) {
            String selector = condition.getFieldName();
            String matchPattern = condition.getOperand() != null ? String.valueOf(condition.getOperand()).trim() : "";
            switch (selector.toLowerCase(Locale.ROOT)) {
                case "name":
                    list = getProjectByName(matchPattern, skipSize, pageSize, entityType.getEntityName());
                    break;
                case "key":
                    list = getProjectByKey(matchPattern, skipSize, pageSize, entityType.getEntityName());
            }
        }
        if (conditions.size() == 1 && list.isEmpty()) {
            list = getAllProjects(skipSize, pageSize, entityType.getEntityName());
        }
        return list;
    }

    private List<EntityValue> getProjectByKey(String matchPattern, long skipSize, Integer pageSize, String entityName) {
        return getProjectService().getAllProjects().stream()
                .filter(project -> project.key.toLowerCase().contains(matchPattern.trim().toLowerCase()))
                .skip(skipSize).limit(pageSize)
                .map(project -> new EntityValue(entityName, project.toFields()))
                .collect(Collectors.toList());
    }

    private List<EntityValue> getProjectByName(String matchPattern, long skipSize, Integer pageSize, String entityName) {
        return getProjectService().getAllProjects().stream()
                .filter(project -> project.name.toLowerCase().contains(matchPattern.trim().toLowerCase()))
                .skip(skipSize).limit(pageSize)
                .map(project -> new EntityValue(entityName, project.toFields()))
                .collect(Collectors.toList());
    }

    private List<EntityValue> getAllProjects(long skipSize, Integer pageSize, String entityName) {
        return getProjectService().getAllProjects().stream()
                .skip(skipSize).limit(pageSize)
                .map(project -> new EntityValue(entityName, project.toFields()))
                .collect(Collectors.toList());
    }

    @Override
    public EntityValue get(EntityType entityType, String primaryKey) {
        TicketProject ticketProject = getProjectService().getProjectByIdOrKey(primaryKey);
        return new EntityValue(entityType.getEntityName(), ticketProject.toFields());
    }
}
