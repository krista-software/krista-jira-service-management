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

package app.krista.extensions.devsecops.development.jira.util;

import app.krista.extensions.devsecops.development.jira.catalog.entities.*;

public final class IssueMapper {

    public static Epic getEpic(Issue issue) {
        Epic epic = new Epic();
        epic.assigneeName = issue.assigneeName;
        epic.comments = issue.comments;
        epic.description = issue.description;
        epic.epicKey = issue.key;
        epic.priority = issue.priority;
        epic.projectKey = issue.projectKey;
        epic.reporterName = issue.reporterName;
        epic.status = issue.status;
        epic.type = issue.issueType;
        epic.summary = issue.summary;
        epic.id = issue.id;
        return epic;
    }

    public static Task getTask(Issue issue) {
        Task task = new Task();
        task.assigneeName = issue.assigneeName;
        task.comments = issue.comments;
        task.description = issue.description;
        task.taskKey = issue.key;
        task.priority = issue.priority;
        task.projectKey = issue.projectKey;
        task.reporterName = issue.reporterName;
        task.status = issue.status;
        task.taskType = issue.issueType;
        task.summary = issue.summary;
        task.id = issue.id;
        return task;
    }

    public static Bug getBug(Issue issue) {
        Bug bug = new Bug();
        bug.assigneeName = issue.assigneeName;
        bug.comments = issue.comments;
        bug.description = issue.description;
        bug.bugKey = issue.key;
        bug.priority = issue.priority;
        bug.projectKey = issue.projectKey;
        bug.reporterName = issue.reporterName;
        bug.status = issue.status;
        bug.type = issue.issueType;
        bug.summary = issue.summary;
        bug.id = issue.id;
        return bug;
    }

    public static Story getStory(Issue issue) {
        Story story = new Story();
        story.assigneeName = issue.assigneeName;
        story.comments = issue.comments;
        story.description = issue.description;
        story.storyKey = issue.key;
        story.priority = issue.priority;
        story.projectKey = issue.projectKey;
        story.reporterName = issue.reporterName;
        story.status = issue.status;
        story.storyType = issue.issueType;
        story.summary = issue.summary;
        story.id = issue.id;
        return story;
    }

    public static Subtask getSubtask(Issue issue) {
        Subtask subtask = new Subtask();
        subtask.assigneeName = issue.assigneeName;
        subtask.comments = issue.comments;
        subtask.description = issue.description;
        subtask.key = issue.key;
        subtask.priority = issue.priority;
        subtask.projectKey = issue.projectKey;
        subtask.reporterName = issue.reporterName;
        subtask.status = issue.status;
        subtask.taskType = issue.issueType;
        subtask.summary = issue.summary;
        subtask.id = issue.id;
        subtask.parentKey = issue.parentKey;
        return subtask;
    }

    public static Issue getIssue(Subtask subtask) {
        Issue issue = new Issue();
        issue.assigneeName = subtask.assigneeName;
        issue.description = subtask.description;
        issue.key = subtask.key;
        issue.priority = subtask.priority;
        issue.projectKey = subtask.projectKey;
        issue.reporterName = subtask.reporterName;
        issue.status = subtask.status;
        issue.issueType = subtask.taskType;
        issue.summary = subtask.summary;
        issue.id = subtask.id;
        issue.parentKey = subtask.parentKey;
        issue.comments = subtask.comments;
        return issue;
    }

    public static Issue getIssue(Epic epic) {
        Issue issue = new Issue();
        issue.assigneeName = epic.assigneeName;
        issue.description = epic.description;
        issue.key = epic.epicKey;
        issue.priority = epic.priority;
        issue.projectKey = epic.projectKey;
        issue.reporterName = epic.reporterName;
        issue.status = epic.status;
        issue.issueType = epic.type;
        issue.summary = epic.summary;
        issue.id = epic.id;
        issue.comments = epic.comments;
        return issue;
    }

    public static Issue getIssue(Bug bug) {
        Issue issue = new Issue();
        issue.assigneeName = bug.assigneeName;
        issue.comments = bug.comments;
        issue.description = bug.description;
        issue.key = bug.bugKey;
        issue.priority = bug.priority;
        issue.projectKey = bug.projectKey;
        issue.reporterName = bug.reporterName;
        issue.status = bug.status;
        issue.issueType = bug.type;
        issue.summary = bug.summary;
        issue.id = bug.id;
        return issue;
    }

    public static Issue getIssue(Story story) {
        Issue issue = new Issue();
        issue.assigneeName = story.assigneeName;
        issue.comments = story.comments;
        issue.description = story.description;
        issue.key = story.storyKey;
        issue.priority = story.priority;
        issue.projectKey = story.projectKey;
        issue.reporterName = story.reporterName;
        issue.status = story.status;
        issue.issueType = story.storyType;
        issue.summary = story.summary;
        issue.id = story.id;
        return issue;
    }

    public static Issue getIssue(Task task) {
        Issue issue = new Issue();
        issue.assigneeName = task.assigneeName;
        issue.comments = task.comments;
        issue.description = task.description;
        issue.key = task.taskKey;
        issue.priority = task.priority;
        issue.projectKey = task.projectKey;
        issue.reporterName = task.reporterName;
        issue.status = task.status;
        issue.issueType = task.taskType;
        issue.summary = task.summary;
        issue.id = task.id;
        return issue;
    }
}