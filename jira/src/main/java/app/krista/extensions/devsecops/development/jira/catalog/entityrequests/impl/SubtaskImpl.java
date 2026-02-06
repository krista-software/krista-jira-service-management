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
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Subtask;
import app.krista.extensions.devsecops.development.jira.catalog.entityrequests.EntityImplementationService;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.util.IssueMapper;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public final class SubtaskImpl extends EntityImplementationService {

    public SubtaskImpl(IssueDao issueDao) {
        super(issueDao);
    }

    @Override
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField) {
        String projectKey, summary, description, parentKey;
        validateInputs(entityAttributeField, List.of(JiraConstants.PROJECT_KEY, JiraConstants.SUMMARY, JiraConstants.PARENT_KEY));
        projectKey = entityAttributeField.get(JiraConstants.PROJECT_KEY).toString();
        summary = entityAttributeField.get(JiraConstants.SUMMARY).toString();
        parentKey = entityAttributeField.get(JiraConstants.PARENT_KEY).toString();
        description = entityAttributeField.containsKey(JiraConstants.DESCRIPTION) ? entityAttributeField.get(JiraConstants.DESCRIPTION).toString() : summary;
        Issue issue = getIssueService().createIssue(projectKey, summary, description, parentKey, "Sub-task");
        if (entityAttributeField.get(JiraConstants.COMMENTS) != null) {
            issue.comments = (List<String>) entityAttributeField.get(JiraConstants.COMMENTS);
            String commentVisibility = entityAttributeField.get(JiraConstants.COMMENT_VISIBILITY) != null ?
                    entityAttributeField.get(JiraConstants.COMMENT_VISIBILITY).toString() : null;
            issue.comments.forEach(comment -> getIssueService().addComment(issue.key, comment, commentVisibility));
        }
        if (entityAttributeField.get(JiraConstants.ASSIGNEE_NAME) != null) {
            issue.assigneeName = entityAttributeField.get(JiraConstants.ASSIGNEE_NAME).toString();
        }
        if (entityAttributeField.get(JiraConstants.PRIORITY) != null) {
            issue.priority = entityAttributeField.get(JiraConstants.PRIORITY).toString();
        }
        if (entityAttributeField.get("Status") != null) {
            issue.status = entityAttributeField.get("Status").toString();
        }
        getIssueService().updateIssue(issue);
        return new EntityValue(entityType.getEntityName(), IssueMapper.getSubtask(getIssueService().getIssueFinal(issue.key)).toFields());
    }

    @Override
    public EntityValue update(EntityType entityType, EntityValue entity) {
        if (getIssueService().getIssueFinal(entity.getFields().get("Key").toString()) == null) {
            throw new NoSuchElementException();
        }
        Subtask subtask = getSubtaskEntity(entity.getFields());
        getIssueService().updateIssue(IssueMapper.getIssue(subtask));
        return new EntityValue(entityType.getEntityName(), subtask.toFields());
    }

    private Subtask getSubtaskEntity(Map<String, Object> attributeFields) {
        Subtask subtask = new Subtask();
        subtask.summary = attributeFields.get(JiraConstants.SUMMARY).toString();
        subtask.comments = attributeFields.containsKey(JiraConstants.COMMENTS) ? Collections.singletonList(attributeFields.get(JiraConstants.COMMENTS).toString()) : Collections.emptyList();
        subtask.reporterName = attributeFields.get(JiraConstants.REPORTER_NAME).toString();
        subtask.assigneeName = attributeFields.get(JiraConstants.ASSIGNEE_NAME).toString();
        subtask.key = attributeFields.get("Key").toString();
        subtask.id = attributeFields.get(JiraConstants.ID).toString();
        subtask.parentKey = attributeFields.get(JiraConstants.PARENT_KEY).toString();
        subtask.projectKey = attributeFields.get(JiraConstants.PROJECT_KEY).toString();
        subtask.description = attributeFields.get(JiraConstants.DESCRIPTION).toString();
        subtask.taskType = attributeFields.get("Task Type").toString();
        subtask.priority = attributeFields.get(JiraConstants.PRIORITY).toString();
        subtask.status = attributeFields.get(JiraConstants.STATUS).toString();
        return subtask;
    }

    @Override
    public List<EntityValue> search(EntityType entityType, SearchQuery searchQuery, Long pageIndex, Integer pageSize) {
        List<SearchCondition> conditions = searchQuery.getSearchConditions();
        final long skipSize = Math.max((pageIndex - 1) * pageSize.longValue(), 0);
        List<EntityValue> list = new ArrayList<>();
        List<Issue> issues;
        for (SearchCondition condition : conditions) {
            String selector = condition.getFieldName();
            String matchPattern = condition.getOperand() != null ? String.valueOf(condition.getOperand()).trim() : "";
            switch (selector.toLowerCase(Locale.ROOT)) {
                case "summary":
                    issues = searchBySummaryIssueType(matchPattern, JiraConstants.SUBTASK, pageSize, pageIndex);
                    list = getEntityList(issues, skipSize, pageSize, entityType.getEntityName());
                    break;
                case "description":
                    issues = searchByTextIssueType(matchPattern, JiraConstants.SUBTASK, pageSize, pageIndex);
                    list = getEntityList(issues, skipSize, pageSize, entityType.getEntityName());
                    break;
                case "key":
                    Issue issue = getIssueService().getIssueFinal(matchPattern);
                    if (JiraConstants.SUBTASK.equalsIgnoreCase(issue.issueType))
                        list.add(new EntityValue(entityType.getEntityName(), issue.toFields()));
            }
        }
        if (conditions.size() == 1 && list.isEmpty()) {
            issues = searchBySummaryIssueType("", JiraConstants.SUBTASK, pageSize, pageIndex);
            list = getEntityList(issues, skipSize, pageSize, entityType.getEntityName());
        }
        return list;
    }

    private List<EntityValue> getEntityList(List<Issue> issues, Long skipSize, Integer pageSize, String entityName) {
        return issues.stream()
                .filter(issue -> issue.issueType.equalsIgnoreCase(JiraConstants.SUBTASK))
                .skip(skipSize).limit(pageSize)
                .map(IssueMapper::getSubtask)
                .map(subtask -> new EntityValue(entityName, subtask.toFields()))
                .collect(Collectors.toList());
    }

    @Override
    public EntityValue get(EntityType entityType, String primaryKey) {
        Subtask subtask = IssueMapper.getSubtask(getIssueService().getIssueFinal(primaryKey));
        return new EntityValue(entityType.getEntityName(), subtask.toFields());
    }
}
