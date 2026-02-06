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

package app.krista.extensions.devsecops.development.jira.catalog.entityrequests;

import app.krista.extension.executor.SearchQuery;
import app.krista.extension.impl.anno.EntityRequest;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public final class EntityRequests {

    private final ServiceRegistry serviceRegistry;

    @Inject
    public EntityRequests(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @EntityRequest(EntityRequest.Type.CREATE)
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField) {
        EntityImplementationService entityService = this.serviceRegistry.getServiceMap().get(entityType.getEntityName());
        return entityService.create(entityType, entityAttributeField);
    }

    @EntityRequest(EntityRequest.Type.UPDATE)
    public EntityValue update(EntityType entityType, EntityValue entity) {
        EntityImplementationService entityService = this.serviceRegistry.getServiceMap().get(entityType.getEntityName());
        return entityService.update(entityType, entity);
    }

    @EntityRequest(EntityRequest.Type.SEARCH)
    public List<EntityValue> search(EntityType entityType, SearchQuery conditions, Long pageIndex, Integer pageSize) {
        EntityImplementationService entityService = this.serviceRegistry.getServiceMap().get(entityType.getEntityName());
        return entityService.search(entityType, conditions, pageIndex, pageSize);
    }

    @EntityRequest(EntityRequest.Type.GET)
    public EntityValue get(EntityType entityType, String primaryKey) {
        EntityImplementationService entityService = this.serviceRegistry.getServiceMap().get(entityType.getEntityName());
        return entityService.get(entityType, primaryKey);
    }

}