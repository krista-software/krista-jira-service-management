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
import app.krista.extensions.devsecops.development.jira.catalog.entityrequests.EntityImplementationService;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public final class IssueImpl extends EntityImplementationService {

    public IssueImpl(IssueDao issueDao) {
        super(issueDao);
    }

    @Override
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField) {
        String projectKey, summary, description, issueType;
        validateInputs(entityAttributeField, List.of(JiraConstants.PROJECT_KEY, JiraConstants.SUMMARY, JiraConstants.ISSUE_TYPE));
        projectKey = entityAttributeField.get(JiraConstants.PROJECT_KEY).toString();
        summary = entityAttributeField.get(JiraConstants.SUMMARY).toString();
        issueType = entityAttributeField.get(JiraConstants.ISSUE_TYPE).toString();
        description = entityAttributeField.containsKey(JiraConstants.DESCRIPTION) ? entityAttributeField.get(JiraConstants.DESCRIPTION).toString() : summary;
        Issue issue = getIssueService().createIssue(projectKey, summary, description, null, issueType);
        if (entityAttributeField.get(JiraConstants.COMMENTS) != null) {
            issue.comments = (List<String>) entityAttributeField.get(JiraConstants.COMMENTS);
            String commentVisibility = entityAttributeField.get(JiraConstants.COMMENT_VISIBILITY) != null ?
                    entityAttributeField.get(JiraConstants.COMMENT_VISIBILITY).toString() : null;
            issue.comments.forEach(comment -> getIssueService().addComment(issue.key, comment, commentVisibility));
        }
        if (entityAttributeField.get(JiraConstants.COMPONENTS) != null) {
            issue.components = (List<String>) entityAttributeField.get(JiraConstants.COMPONENTS);
            issue.components.forEach(component -> getIssueService().addComponent(issue.key, component));
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
        if (entityAttributeField.get(JiraConstants.LABELS) != null) {
            issue.labels = (List<String>) entityAttributeField.get(JiraConstants.LABELS);
            issue.labels.forEach(label -> getIssueService().addLabel(label, issue.key));
        }
        getIssueService().updateIssue(issue);
        Issue issueFinal = getIssueService().getIssueFinal(issue.key);
        return new EntityValue(entityType.getEntityName(), issueFinal.toFields());
    }

    @Override
    public EntityValue update(EntityType entityType, EntityValue entity) {
        if (getIssueService().getIssueFinal(entity.getFields().get("Key").toString()) == null) {
            throw new NoSuchElementException();
        }
        Issue issue = getIssueEntity(entity.getFields());
        issue.labels.forEach(label -> getIssueService().addLabel(label, issue.key));
        getIssueService().updateIssue(issue);
        return new EntityValue(entityType.getEntityName(), issue.toFields());
    }

    private Issue getIssueEntity(Map<String, Object> attributeFields) {
        Issue issue = new Issue();
        issue.summary = attributeFields.get(JiraConstants.SUMMARY).toString();
        issue.comments = attributeFields.containsKey("Comment") ? Collections.singletonList(attributeFields.get("Comment").toString()) : Collections.emptyList();
        issue.reporterName = attributeFields.get(JiraConstants.REPORTER_NAME).toString();
        issue.assigneeName = attributeFields.get(JiraConstants.ASSIGNEE_NAME).toString();
        issue.key = attributeFields.get("Key").toString();
        issue.id = attributeFields.get(JiraConstants.ID).toString();
        issue.parentKey = attributeFields.get(JiraConstants.PARENT_KEY).toString();
        issue.projectKey = attributeFields.get(JiraConstants.PROJECT_KEY).toString();
        issue.description = attributeFields.get(JiraConstants.DESCRIPTION).toString();
        issue.issueType = attributeFields.get(JiraConstants.ISSUE_TYPE).toString();
        issue.priority = attributeFields.get(JiraConstants.PRIORITY).toString();
        issue.status = attributeFields.get(JiraConstants.STATUS).toString();
        return issue;
    }

    @Override
    public List<EntityValue> search(EntityType entityType, SearchQuery searchQuery, Long pageIndex, Integer pageSize) {
        List<SearchCondition> conditions = searchQuery.getSearchConditions();
        String projectKey = getConditionValue(conditions, "Project key");
        List<EntityValue> list = new ArrayList<>();
        List<Issue> issues;
        for (SearchCondition condition : conditions) {
            String selector = condition.getFieldName();
            String matchPattern = condition.getOperand() != null ? String.valueOf(condition.getOperand()).trim() : "";
            switch (selector.toLowerCase(Locale.ROOT)) {
                case "summary":
                    issues = searchBySummaryIssueTypeProjectKey(projectKey, matchPattern, "", pageSize, pageIndex);
                    list = getEntityList(issues, pageSize, entityType.getEntityName());
                    break;
                case "description":
                    issues = searchByTextIssueTypeProjectKey(projectKey, matchPattern, "", pageSize, pageIndex);
                    list = getEntityList(issues, pageSize, entityType.getEntityName());
                    break;
                case "status":
                    issues = getIssuesByStatus(matchPattern);
                    list = getEntityList(issues, pageSize, entityType.getEntityName());
                    break;
                case "issue type":
                    issues = searchBySummaryIssueTypeProjectKey(projectKey, "", matchPattern, pageSize, pageIndex);
                    list = getEntityList(issues, pageSize, entityType.getEntityName());
                    break;
                case "key":
                    Issue issue = getIssueService().getIssueFinal(matchPattern);
                    list.add(new EntityValue(entityType.getEntityName(), issue.toFields()));
            }
        }
        if (conditions.size() == 1 && list.isEmpty()) {
            issues = searchBySummary("", pageSize, pageIndex);
            list = getEntityList(issues, pageSize, entityType.getEntityName());
        }
        return list;
    }

    private List<EntityValue> getEntityList(List<Issue> issues, Integer pageSize, String entityName) {
        return issues.stream()
                .limit(pageSize)
                .map(issue -> new EntityValue(entityName, issue.toFields()))
                .collect(Collectors.toList());
    }

    @Override
    public EntityValue get(EntityType entityType, String primaryKey) {
        Issue issue = getIssueService().getIssueFinal(primaryKey);
        return new EntityValue(entityType.getEntityName(), issue.toFields());
    }
}
