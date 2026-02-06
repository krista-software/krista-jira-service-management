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

import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketStatus;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.List;
import java.util.Map;

import static app.krista.extensions.devsecops.development.jira.impl.JQLBuilder.removeEndsWith;

public abstract class EntityImplementationService {

    private IssueDao issueDao;
    private ProjectDao projectDao;
    private UserDao userDao;
    private TicketInfoDao ticketInfoDao;

    public EntityImplementationService(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    public EntityImplementationService(ProjectDao projectDao) {
        this.projectDao = projectDao;
    }

    public EntityImplementationService(UserDao userDao) {
        this.userDao = userDao;
    }

    public EntityImplementationService(TicketInfoDao ticketInfoDao) {
        this.ticketInfoDao = ticketInfoDao;
    }

    public IssueDao getIssueService() {
        return issueDao;
    }

    public ProjectDao getProjectService() {
        return projectDao;
    }

    public UserDao getUserService() {
        return userDao;
    }

    public TicketInfoDao getTicketInfoService() {
        return ticketInfoDao;
    }

    abstract public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField);

    abstract public EntityValue update(EntityType entityType, EntityValue entity);

    public abstract List<EntityValue> search(EntityType entityType, SearchQuery conditions, Long pageIndex, Integer pageSize);

    public abstract EntityValue get(EntityType entityType, String primaryKey);

    public List<Issue> searchBySummaryIssueTypeProjectKey(String projectKey, String text, String issueType, long size, long page) {
        StringBuilder q = new StringBuilder();
        q.append(getProjectKey(projectKey));
        q.append(q.length() == 0 ? "" : JiraConstants.AND);
        q.append(getIssueType(issueType));
        q.append(q.length() == 0 ? "" : q.toString().endsWith(JiraConstants.AND) ? "" : JiraConstants.AND);
        q.append(getSummary(text));
        String query = removeEndsWith(q.toString(), JiraConstants.AND);
        return issueDao.searchByJQL(query + "  ORDER BY created DESC", size, page);
    }

    public List<Issue> searchByTextIssueTypeProjectKey(String projectKey, String text, String issueType, long size, long page) {
        StringBuilder q = new StringBuilder();
        q.append(getProjectKey(projectKey));
        q.append(q.length() == 0 ? "" : JiraConstants.AND);
        q.append(getIssueType(issueType));
        q.append(q.length() == 0 ? "" : q.toString().endsWith(JiraConstants.AND) ? "" : JiraConstants.AND);
        q.append(getSearchText(text));
        String query = removeEndsWith(q.toString(), JiraConstants.AND);
        return issueDao.searchByJQL(query + "  ORDER BY created DESC", size, page);
    }

    private String getSummary(String text) {
        return text == null || text.isEmpty() ? "" : " summary ~ \"" + text + "\"";
    }

    private String getIssueType(String issueType) {
        return issueType == null || issueType.isEmpty() ? "" : "issuetype = " + issueType;
    }

    private String getProjectKey(String projectKey) {
        return projectKey == null || projectKey.isEmpty() ? "" : "project = " + projectKey;
    }

    private String getSearchText(String text) {
        return text == null || text.isEmpty() ? "" : " text ~ \"" + text + "\"";
    }

    public List<Issue> searchBySummary(String text, long size, long page) {
        if (text == null || text.isBlank())
            return issueDao.searchByJQL("order by priority DESC, updated DESC", size, page);
        return issueDao.searchByJQL("summary ~ \"" + text + "\" ORDER BY created DESC", size, page);
    }

    public List<Issue> searchBySummaryIssueType(String text, String issueType, long size, long page) {
        if (issueType == null || issueType.isEmpty()) {
            return searchBySummary(text, size, page);
        } else {
            if (text == null || text.isEmpty())
                return issueDao.searchByJQL("issuetype = " + issueType + "  ORDER BY created DESC", size, page);
            return issueDao.searchByJQL("summary ~ \"" + text + "\" AND issuetype = " + issueType + "  ORDER BY created DESC", size, page);

        }
    }

    public List<Issue> getIssuesByStatus(String statusName) {
        TicketStatus status = ticketInfoDao.getStatuses().stream()
                .filter(tstatus -> tstatus.name.toLowerCase().contains(statusName.toLowerCase()))
                .findFirst().orElseThrow(() -> new RuntimeException("Status not found for " + statusName));
        return getIssueService().searchByJQL("status = \"" + status.name + "\"   ORDER BY created DESC", 30, 1);
    }

    public List<Issue> searchByTextIssueType(String text, String issueType, long size, long page) {
        return issueDao.searchByJQL("text ~ \"" + text + "\" AND issuetype = " + issueType + " ORDER BY created DESC", size, page);
    }

    public String getConditionValue(List<SearchCondition> conditions, String fieldName) {
        for (SearchCondition con : conditions) {
            if (fieldName.equalsIgnoreCase(con.getFieldName())) {
                return con.getOperand() != null ? String.valueOf(con.getOperand()).trim() : "";
            }
        }
        return "";
    }

    public void validateInputs(Map<String, Object> entityAttributeField, List<String> keys) {
        for (String key : keys) {
            if (!entityAttributeField.containsKey(key)) {
                throw new IllegalArgumentException("Please provide " + key);
            }
        }
    }

}
