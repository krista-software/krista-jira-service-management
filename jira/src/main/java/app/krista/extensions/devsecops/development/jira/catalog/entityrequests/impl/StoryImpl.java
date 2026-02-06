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
import app.krista.extensions.devsecops.development.jira.catalog.entities.Story;
import app.krista.extensions.devsecops.development.jira.catalog.entityrequests.EntityImplementationService;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.util.IssueMapper;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public final class StoryImpl extends EntityImplementationService {

    public StoryImpl(IssueDao issueDao) {
        super(issueDao);
    }

    @Override
    public EntityValue create(EntityType entityType, Map<String, Object> entityAttributeField) {
        String projectKey, summary, description;
        validateInputs(entityAttributeField, List.of(JiraConstants.PROJECT_KEY, JiraConstants.SUMMARY));
        projectKey = entityAttributeField.get(JiraConstants.PROJECT_KEY).toString();
        summary = entityAttributeField.get(JiraConstants.SUMMARY).toString();
        description = entityAttributeField.containsKey(JiraConstants.DESCRIPTION) ? entityAttributeField.get(JiraConstants.DESCRIPTION).toString() : summary;
        Issue issue = getIssueService().createIssue(projectKey, summary, description, null, JiraConstants.STORY);
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
        return new EntityValue(entityType.getEntityName(), IssueMapper.getStory(getIssueService().getIssueFinal(issue.key)).toFields());
    }

    @Override
    public EntityValue update(EntityType entityType, EntityValue entity) {
        if (getIssueService().getIssueFinal(entity.getFields().get("Story Key").toString()) == null) {
            throw new NoSuchElementException();
        }
        Story story = getStoryEntity(entity.getFields());
        getIssueService().updateIssue(IssueMapper.getIssue(story));
        return new EntityValue(entityType.getEntityName(), story.toFields());
    }

    private Story getStoryEntity(Map<String, Object> attributeFields) {
        Story story = new Story();
        story.summary = attributeFields.get(JiraConstants.SUMMARY).toString();
        story.comments = attributeFields.containsKey(JiraConstants.COMMENTS) ? Collections.singletonList(attributeFields.get(JiraConstants.COMMENTS).toString()) : Collections.emptyList();
        story.reporterName = attributeFields.get(JiraConstants.REPORTER_NAME).toString();
        story.assigneeName = attributeFields.get(JiraConstants.ASSIGNEE_NAME).toString();
        story.storyKey = attributeFields.get("Story Key").toString();
        story.id = attributeFields.get(JiraConstants.ID).toString();
        story.projectKey = attributeFields.get(JiraConstants.PROJECT_KEY).toString();
        story.description = attributeFields.get(JiraConstants.DESCRIPTION).toString();
        story.storyType = attributeFields.get("Story Type").toString();
        story.priority = attributeFields.get(JiraConstants.PRIORITY).toString();
        story.status = attributeFields.get(JiraConstants.STATUS).toString();
        return story;
    }

    @Override
    public List<EntityValue> search(EntityType entityType, SearchQuery searchQuery, Long pageIndex, Integer pageSize) {
        List<SearchCondition> conditions = searchQuery.getSearchConditions();
        List<EntityValue> list = new ArrayList<>();
        List<Issue> issues;
        for (SearchCondition condition : conditions) {
            String selector = condition.getFieldName();
            String matchPattern = condition.getOperand() != null ? String.valueOf(condition.getOperand()).trim() : "";
            switch (selector.toLowerCase(Locale.ROOT)) {
                case JiraConstants._SUMMARY:
                    issues = searchBySummaryIssueType(matchPattern, JiraConstants.STORY, pageSize, pageIndex);
                    list = issues != null ? getEntityList(issues, pageSize, entityType.getEntityName()) : List.of();
                    break;
                case JiraConstants._DESCRIPTION:
                    issues = searchByTextIssueType(matchPattern, JiraConstants.STORY, pageSize, pageIndex);
                    list = issues != null ? getEntityList(issues, pageSize, entityType.getEntityName()) : List.of();
                    break;
                case JiraConstants.STORY_KEY:
                    Issue issue = getIssueService().getIssueFinal(matchPattern);
                    list = issue != null ? getEntityList(List.of(issue), pageSize, entityType.getEntityName()) : List.of();
            }
        }
        if (conditions.size() == 1 && list.isEmpty()) {
            issues = searchBySummaryIssueType("", JiraConstants.STORY, pageSize, pageIndex);
            list = getEntityList(issues, pageSize, entityType.getEntityName());
        }
        return list;
    }

    private List<EntityValue> getEntityList(List<Issue> issues, Integer pageSize, String entityName) {
        return issues.stream()
                .limit(pageSize)
                .filter(issue -> issue.issueType.equalsIgnoreCase(JiraConstants.STORY))
                .map(IssueMapper::getStory)
                .map(story -> new EntityValue(entityName, story.toFields()))
                .collect(Collectors.toList());
    }

    @Override
    public EntityValue get(EntityType entityType, String primaryKey) {
        Story story = IssueMapper.getStory(getIssueService().getIssueFinal(primaryKey));
        return new EntityValue(entityType.getEntityName(), story.toFields());
    }
}
