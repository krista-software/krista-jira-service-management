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

import app.krista.extensions.devsecops.development.jira.catalog.entityrequests.impl.*;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@Service
public final class ServiceRegistry {
    private final Map<String, EntityImplementationService> serviceMap = new HashMap<>();

    @Inject
    public ServiceRegistry(IssueDao issueDao, ProjectDao projectDao, UserDao userDao, TicketInfoDao ticketInfoDao) {
        serviceMap.put(JiraConstants.BUG, new BugImpl(issueDao));
        serviceMap.put(JiraConstants.EPIC, new EpicImpl(issueDao));
        serviceMap.put(JiraConstants.ISSUE, new IssueImpl(issueDao));
        serviceMap.put(JiraConstants.STORY, new StoryImpl(issueDao));
        serviceMap.put(JiraConstants.TASK, new TaskImpl(issueDao));
        serviceMap.put(JiraConstants.SUBTASK_ENTITY, new SubtaskImpl(issueDao));
        serviceMap.put(JiraConstants.ISSUE_TYPE, new IssueTypeImpl(ticketInfoDao));
        serviceMap.put(JiraConstants.TICKET_PRIORITY, new TicketPriorityImpl(ticketInfoDao));
        serviceMap.put(JiraConstants.TICKET_STATUS, new TicketStatusImpl(ticketInfoDao));
        serviceMap.put(JiraConstants.TICKET_PROJECT, new TicketProjectImpl(projectDao));
        serviceMap.put(JiraConstants.USER, new UserImpl(userDao));
        serviceMap.put(JiraConstants.VERSION, new VersionImpl(projectDao));
        serviceMap.put(JiraConstants.COMPONENT, new ComponentImpl(issueDao));
        serviceMap.put(JiraConstants.LABEL, new LabelImpl(issueDao));
    }

    public Map<String, EntityImplementationService> getServiceMap() {
        return serviceMap;
    }
}
