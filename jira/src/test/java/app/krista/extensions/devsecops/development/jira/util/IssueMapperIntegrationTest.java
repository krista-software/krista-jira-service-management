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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IssueMapper Integration Tests")
class IssueMapperIntegrationTest {

    // ==================== Helper Methods ====================

    private Issue createSampleIssue() {
        Issue issue = new Issue();
        issue.id = "12345";
        issue.key = "TEST-123";
        issue.summary = "Test Summary";
        issue.description = "Test Description";
        issue.assigneeName = "John Doe";
        issue.reporterName = "Jane Smith";
        issue.status = "IN PROGRESS";
        issue.priority = "High";
        issue.projectKey = "TEST";
        issue.issueType = "Story";
        issue.parentKey = "TEST-100";
        issue.comments = Arrays.asList("Comment 1", "Comment 2");
        issue.labels = Arrays.asList("label1", "label2");
        issue.components = Arrays.asList("component1", "component2");
        return issue;
    }

    // ==================== Class Structure Tests ====================

    @Test
    @DisplayName("Should verify IssueMapper is final class")
    void testIssueMapperIsFinal() {
        assertTrue(java.lang.reflect.Modifier.isFinal(IssueMapper.class.getModifiers()));
    }

    @Test
    @DisplayName("Should verify all methods are static")
    void testAllMethodsAreStatic() {
        var methods = IssueMapper.class.getDeclaredMethods();
        for (var method : methods) {
            assertTrue(java.lang.reflect.Modifier.isStatic(method.getModifiers()),
                "Method " + method.getName() + " should be static");
        }
    }

    // ==================== getEpic() Tests ====================

    @Test
    @DisplayName("Should convert Issue to Epic with all fields")
    void testGetEpic_AllFields() {
        Issue issue = createSampleIssue();
        
        Epic epic = IssueMapper.getEpic(issue);
        
        assertNotNull(epic);
        assertEquals(issue.id, epic.id);
        assertEquals(issue.key, epic.epicKey);
        assertEquals(issue.summary, epic.summary);
        assertEquals(issue.description, epic.description);
        assertEquals(issue.assigneeName, epic.assigneeName);
        assertEquals(issue.reporterName, epic.reporterName);
        assertEquals(issue.status, epic.status);
        assertEquals(issue.priority, epic.priority);
        assertEquals(issue.projectKey, epic.projectKey);
        assertEquals(issue.issueType, epic.type);
        assertEquals(issue.comments, epic.comments);
    }

    @Test
    @DisplayName("Should convert Issue to Epic with null fields")
    void testGetEpic_NullFields() {
        Issue issue = new Issue();
        
        Epic epic = IssueMapper.getEpic(issue);
        
        assertNotNull(epic);
        assertNull(epic.id);
        assertNull(epic.epicKey);
        assertNull(epic.summary);
        assertNull(epic.description);
        assertNull(epic.assigneeName);
        assertNull(epic.reporterName);
        assertNull(epic.status);
        assertNull(epic.priority);
        assertNull(epic.projectKey);
        assertNull(epic.type);
        assertNull(epic.comments);
    }

    @Test
    @DisplayName("Should convert Issue to Epic with empty strings")
    void testGetEpic_EmptyStrings() {
        Issue issue = new Issue();
        issue.id = "";
        issue.key = "";
        issue.summary = "";
        issue.description = "";
        issue.assigneeName = "";
        issue.reporterName = "";
        issue.status = "";
        issue.priority = "";
        issue.projectKey = "";
        issue.issueType = "";
        issue.comments = Collections.emptyList();
        
        Epic epic = IssueMapper.getEpic(issue);
        
        assertNotNull(epic);
        assertEquals("", epic.id);
        assertEquals("", epic.epicKey);
        assertEquals("", epic.summary);
        assertEquals("", epic.description);
        assertEquals("", epic.assigneeName);
        assertEquals("", epic.reporterName);
        assertEquals("", epic.status);
        assertEquals("", epic.priority);
        assertEquals("", epic.projectKey);
        assertEquals("", epic.type);
        assertEquals(0, epic.comments.size());
    }

    @Test
    @DisplayName("Should convert Issue to Epic with special characters")
    void testGetEpic_SpecialCharacters() {
        Issue issue = new Issue();
        issue.summary = "Test's Summary & Co.";
        issue.description = "Description with <html> tags";
        issue.assigneeName = "O'Brien";
        
        Epic epic = IssueMapper.getEpic(issue);
        
        assertEquals("Test's Summary & Co.", epic.summary);
        assertEquals("Description with <html> tags", epic.description);
        assertEquals("O'Brien", epic.assigneeName);
    }

    // ==================== getTask() Tests ====================

    @Test
    @DisplayName("Should convert Issue to Task with all fields")
    void testGetTask_AllFields() {
        Issue issue = createSampleIssue();

        Task task = IssueMapper.getTask(issue);

        assertNotNull(task);
        assertEquals(issue.id, task.id);
        assertEquals(issue.key, task.taskKey);
        assertEquals(issue.summary, task.summary);
        assertEquals(issue.description, task.description);
        assertEquals(issue.assigneeName, task.assigneeName);
        assertEquals(issue.reporterName, task.reporterName);
        assertEquals(issue.status, task.status);
        assertEquals(issue.priority, task.priority);
        assertEquals(issue.projectKey, task.projectKey);
        assertEquals(issue.issueType, task.taskType);
        assertEquals(issue.comments, task.comments);
    }

    @Test
    @DisplayName("Should convert Issue to Task with null fields")
    void testGetTask_NullFields() {
        Issue issue = new Issue();

        Task task = IssueMapper.getTask(issue);

        assertNotNull(task);
        assertNull(task.id);
        assertNull(task.taskKey);
        assertNull(task.summary);
        assertNull(task.description);
        assertNull(task.assigneeName);
        assertNull(task.reporterName);
        assertNull(task.status);
        assertNull(task.priority);
        assertNull(task.projectKey);
        assertNull(task.taskType);
        assertNull(task.comments);
    }

    // ==================== getBug() Tests ====================

    @Test
    @DisplayName("Should convert Issue to Bug with all fields")
    void testGetBug_AllFields() {
        Issue issue = createSampleIssue();

        Bug bug = IssueMapper.getBug(issue);

        assertNotNull(bug);
        assertEquals(issue.id, bug.id);
        assertEquals(issue.key, bug.bugKey);
        assertEquals(issue.summary, bug.summary);
        assertEquals(issue.description, bug.description);
        assertEquals(issue.assigneeName, bug.assigneeName);
        assertEquals(issue.reporterName, bug.reporterName);
        assertEquals(issue.status, bug.status);
        assertEquals(issue.priority, bug.priority);
        assertEquals(issue.projectKey, bug.projectKey);
        assertEquals(issue.issueType, bug.type);
        assertEquals(issue.comments, bug.comments);
    }

    @Test
    @DisplayName("Should convert Issue to Bug with null fields")
    void testGetBug_NullFields() {
        Issue issue = new Issue();

        Bug bug = IssueMapper.getBug(issue);

        assertNotNull(bug);
        assertNull(bug.id);
        assertNull(bug.bugKey);
        assertNull(bug.summary);
        assertNull(bug.description);
        assertNull(bug.assigneeName);
        assertNull(bug.reporterName);
        assertNull(bug.status);
        assertNull(bug.priority);
        assertNull(bug.projectKey);
        assertNull(bug.type);
        assertNull(bug.comments);
    }

    // ==================== getStory() Tests ====================

    @Test
    @DisplayName("Should convert Issue to Story with all fields")
    void testGetStory_AllFields() {
        Issue issue = createSampleIssue();

        Story story = IssueMapper.getStory(issue);

        assertNotNull(story);
        assertEquals(issue.id, story.id);
        assertEquals(issue.key, story.storyKey);
        assertEquals(issue.summary, story.summary);
        assertEquals(issue.description, story.description);
        assertEquals(issue.assigneeName, story.assigneeName);
        assertEquals(issue.reporterName, story.reporterName);
        assertEquals(issue.status, story.status);
        assertEquals(issue.priority, story.priority);
        assertEquals(issue.projectKey, story.projectKey);
        assertEquals(issue.issueType, story.storyType);
        assertEquals(issue.comments, story.comments);
    }

    @Test
    @DisplayName("Should convert Issue to Story with null fields")
    void testGetStory_NullFields() {
        Issue issue = new Issue();

        Story story = IssueMapper.getStory(issue);

        assertNotNull(story);
        assertNull(story.id);
        assertNull(story.storyKey);
        assertNull(story.summary);
        assertNull(story.description);
        assertNull(story.assigneeName);
        assertNull(story.reporterName);
        assertNull(story.status);
        assertNull(story.priority);
        assertNull(story.projectKey);
        assertNull(story.storyType);
        assertNull(story.comments);
    }

    // ==================== getSubtask() Tests ====================

    @Test
    @DisplayName("Should convert Issue to Subtask with all fields")
    void testGetSubtask_AllFields() {
        Issue issue = createSampleIssue();

        Subtask subtask = IssueMapper.getSubtask(issue);

        assertNotNull(subtask);
        assertEquals(issue.id, subtask.id);
        assertEquals(issue.key, subtask.key);
        assertEquals(issue.summary, subtask.summary);
        assertEquals(issue.description, subtask.description);
        assertEquals(issue.assigneeName, subtask.assigneeName);
        assertEquals(issue.reporterName, subtask.reporterName);
        assertEquals(issue.status, subtask.status);
        assertEquals(issue.priority, subtask.priority);
        assertEquals(issue.projectKey, subtask.projectKey);
        assertEquals(issue.issueType, subtask.taskType);
        assertEquals(issue.parentKey, subtask.parentKey);
        assertEquals(issue.comments, subtask.comments);
    }

    @Test
    @DisplayName("Should convert Issue to Subtask with null fields")
    void testGetSubtask_NullFields() {
        Issue issue = new Issue();

        Subtask subtask = IssueMapper.getSubtask(issue);

        assertNotNull(subtask);
        assertNull(subtask.id);
        assertNull(subtask.key);
        assertNull(subtask.summary);
        assertNull(subtask.description);
        assertNull(subtask.assigneeName);
        assertNull(subtask.reporterName);
        assertNull(subtask.status);
        assertNull(subtask.priority);
        assertNull(subtask.projectKey);
        assertNull(subtask.taskType);
        assertNull(subtask.parentKey);
        assertNull(subtask.comments);
    }

    @Test
    @DisplayName("Should convert Issue to Subtask with parentKey")
    void testGetSubtask_WithParentKey() {
        Issue issue = new Issue();
        issue.parentKey = "PARENT-123";
        issue.key = "SUBTASK-456";

        Subtask subtask = IssueMapper.getSubtask(issue);

        assertEquals("PARENT-123", subtask.parentKey);
        assertEquals("SUBTASK-456", subtask.key);
    }

    // ==================== getIssue(Subtask) Tests ====================

    @Test
    @DisplayName("Should convert Subtask to Issue with all fields")
    void testGetIssueFromSubtask_AllFields() {
        Subtask subtask = new Subtask();
        subtask.id = "12345";
        subtask.key = "SUB-123";
        subtask.summary = "Subtask Summary";
        subtask.description = "Subtask Description";
        subtask.assigneeName = "John Doe";
        subtask.reporterName = "Jane Smith";
        subtask.status = "DONE";
        subtask.priority = "Medium";
        subtask.projectKey = "PROJ";
        subtask.taskType = "Sub-task";
        subtask.parentKey = "PARENT-100";
        subtask.comments = Arrays.asList("Comment A", "Comment B");

        Issue issue = IssueMapper.getIssue(subtask);

        assertNotNull(issue);
        assertEquals(subtask.id, issue.id);
        assertEquals(subtask.key, issue.key);
        assertEquals(subtask.summary, issue.summary);
        assertEquals(subtask.description, issue.description);
        assertEquals(subtask.assigneeName, issue.assigneeName);
        assertEquals(subtask.reporterName, issue.reporterName);
        assertEquals(subtask.status, issue.status);
        assertEquals(subtask.priority, issue.priority);
        assertEquals(subtask.projectKey, issue.projectKey);
        assertEquals(subtask.taskType, issue.issueType);
        assertEquals(subtask.parentKey, issue.parentKey);
        assertEquals(subtask.comments, issue.comments);
    }

    @Test
    @DisplayName("Should convert Subtask to Issue with null fields")
    void testGetIssueFromSubtask_NullFields() {
        Subtask subtask = new Subtask();

        Issue issue = IssueMapper.getIssue(subtask);

        assertNotNull(issue);
        assertNull(issue.id);
        assertNull(issue.key);
        assertNull(issue.summary);
        assertNull(issue.description);
        assertNull(issue.assigneeName);
        assertNull(issue.reporterName);
        assertNull(issue.status);
        assertNull(issue.priority);
        assertNull(issue.projectKey);
        assertNull(issue.issueType);
        assertNull(issue.parentKey);
        assertNull(issue.comments);
    }

    // ==================== getIssue(Epic) Tests ====================

    @Test
    @DisplayName("Should convert Epic to Issue with all fields")
    void testGetIssueFromEpic_AllFields() {
        Epic epic = new Epic();
        epic.id = "67890";
        epic.epicKey = "EPIC-456";
        epic.summary = "Epic Summary";
        epic.description = "Epic Description";
        epic.assigneeName = "Alice";
        epic.reporterName = "Bob";
        epic.status = "IN PROGRESS";
        epic.priority = "High";
        epic.projectKey = "PROJ";
        epic.type = "Epic";
        epic.comments = Arrays.asList("Epic Comment 1");

        Issue issue = IssueMapper.getIssue(epic);

        assertNotNull(issue);
        assertEquals(epic.id, issue.id);
        assertEquals(epic.epicKey, issue.key);
        assertEquals(epic.summary, issue.summary);
        assertEquals(epic.description, issue.description);
        assertEquals(epic.assigneeName, issue.assigneeName);
        assertEquals(epic.reporterName, issue.reporterName);
        assertEquals(epic.status, issue.status);
        assertEquals(epic.priority, issue.priority);
        assertEquals(epic.projectKey, issue.projectKey);
        assertEquals(epic.type, issue.issueType);
        assertEquals(epic.comments, issue.comments);
    }

    @Test
    @DisplayName("Should convert Epic to Issue with null fields")
    void testGetIssueFromEpic_NullFields() {
        Epic epic = new Epic();

        Issue issue = IssueMapper.getIssue(epic);

        assertNotNull(issue);
        assertNull(issue.id);
        assertNull(issue.key);
        assertNull(issue.summary);
        assertNull(issue.description);
        assertNull(issue.assigneeName);
        assertNull(issue.reporterName);
        assertNull(issue.status);
        assertNull(issue.priority);
        assertNull(issue.projectKey);
        assertNull(issue.issueType);
        assertNull(issue.comments);
    }

    // ==================== getIssue(Bug) Tests ====================

    @Test
    @DisplayName("Should convert Bug to Issue with all fields")
    void testGetIssueFromBug_AllFields() {
        Bug bug = new Bug();
        bug.id = "11111";
        bug.bugKey = "BUG-789";
        bug.summary = "Bug Summary";
        bug.description = "Bug Description";
        bug.assigneeName = "Charlie";
        bug.reporterName = "Diana";
        bug.status = "OPEN";
        bug.priority = "Critical";
        bug.projectKey = "PROJ";
        bug.type = "Bug";
        bug.comments = Arrays.asList("Bug Comment 1", "Bug Comment 2");

        Issue issue = IssueMapper.getIssue(bug);

        assertNotNull(issue);
        assertEquals(bug.id, issue.id);
        assertEquals(bug.bugKey, issue.key);
        assertEquals(bug.summary, issue.summary);
        assertEquals(bug.description, issue.description);
        assertEquals(bug.assigneeName, issue.assigneeName);
        assertEquals(bug.reporterName, issue.reporterName);
        assertEquals(bug.status, issue.status);
        assertEquals(bug.priority, issue.priority);
        assertEquals(bug.projectKey, issue.projectKey);
        assertEquals(bug.type, issue.issueType);
        assertEquals(bug.comments, issue.comments);
    }

    @Test
    @DisplayName("Should convert Bug to Issue with null fields")
    void testGetIssueFromBug_NullFields() {
        Bug bug = new Bug();

        Issue issue = IssueMapper.getIssue(bug);

        assertNotNull(issue);
        assertNull(issue.id);
        assertNull(issue.key);
        assertNull(issue.summary);
        assertNull(issue.description);
        assertNull(issue.assigneeName);
        assertNull(issue.reporterName);
        assertNull(issue.status);
        assertNull(issue.priority);
        assertNull(issue.projectKey);
        assertNull(issue.issueType);
        assertNull(issue.comments);
    }

    // ==================== getIssue(Story) Tests ====================

    @Test
    @DisplayName("Should convert Story to Issue with all fields")
    void testGetIssueFromStory_AllFields() {
        Story story = new Story();
        story.id = "22222";
        story.storyKey = "STORY-999";
        story.summary = "Story Summary";
        story.description = "Story Description";
        story.assigneeName = "Eve";
        story.reporterName = "Frank";
        story.status = "TO DO";
        story.priority = "Low";
        story.projectKey = "PROJ";
        story.storyType = "Story";
        story.comments = Arrays.asList("Story Comment");

        Issue issue = IssueMapper.getIssue(story);

        assertNotNull(issue);
        assertEquals(story.id, issue.id);
        assertEquals(story.storyKey, issue.key);
        assertEquals(story.summary, issue.summary);
        assertEquals(story.description, issue.description);
        assertEquals(story.assigneeName, issue.assigneeName);
        assertEquals(story.reporterName, issue.reporterName);
        assertEquals(story.status, issue.status);
        assertEquals(story.priority, issue.priority);
        assertEquals(story.projectKey, issue.projectKey);
        assertEquals(story.storyType, issue.issueType);
        assertEquals(story.comments, issue.comments);
    }

    @Test
    @DisplayName("Should convert Story to Issue with null fields")
    void testGetIssueFromStory_NullFields() {
        Story story = new Story();

        Issue issue = IssueMapper.getIssue(story);

        assertNotNull(issue);
        assertNull(issue.id);
        assertNull(issue.key);
        assertNull(issue.summary);
        assertNull(issue.description);
        assertNull(issue.assigneeName);
        assertNull(issue.reporterName);
        assertNull(issue.status);
        assertNull(issue.priority);
        assertNull(issue.projectKey);
        assertNull(issue.issueType);
        assertNull(issue.comments);
    }

    // ==================== getIssue(Task) Tests ====================

    @Test
    @DisplayName("Should convert Task to Issue with all fields")
    void testGetIssueFromTask_AllFields() {
        Task task = new Task();
        task.id = "33333";
        task.taskKey = "TASK-555";
        task.summary = "Task Summary";
        task.description = "Task Description";
        task.assigneeName = "Grace";
        task.reporterName = "Henry";
        task.status = "IN REVIEW";
        task.priority = "Medium";
        task.projectKey = "PROJ";
        task.taskType = "Task";
        task.comments = Arrays.asList("Task Comment 1", "Task Comment 2", "Task Comment 3");

        Issue issue = IssueMapper.getIssue(task);

        assertNotNull(issue);
        assertEquals(task.id, issue.id);
        assertEquals(task.taskKey, issue.key);
        assertEquals(task.summary, issue.summary);
        assertEquals(task.description, issue.description);
        assertEquals(task.assigneeName, issue.assigneeName);
        assertEquals(task.reporterName, issue.reporterName);
        assertEquals(task.status, issue.status);
        assertEquals(task.priority, issue.priority);
        assertEquals(task.projectKey, issue.projectKey);
        assertEquals(task.taskType, issue.issueType);
        assertEquals(task.comments, issue.comments);
    }

    @Test
    @DisplayName("Should convert Task to Issue with null fields")
    void testGetIssueFromTask_NullFields() {
        Task task = new Task();

        Issue issue = IssueMapper.getIssue(task);

        assertNotNull(issue);
        assertNull(issue.id);
        assertNull(issue.key);
        assertNull(issue.summary);
        assertNull(issue.description);
        assertNull(issue.assigneeName);
        assertNull(issue.reporterName);
        assertNull(issue.status);
        assertNull(issue.priority);
        assertNull(issue.projectKey);
        assertNull(issue.issueType);
        assertNull(issue.comments);
    }

    // ==================== Round-trip Conversion Tests ====================

    @Test
    @DisplayName("Should maintain data integrity in round-trip Issue to Epic to Issue")
    void testRoundTrip_IssueToEpicToIssue() {
        Issue original = createSampleIssue();

        Epic epic = IssueMapper.getEpic(original);
        Issue converted = IssueMapper.getIssue(epic);

        assertEquals(original.id, converted.id);
        assertEquals(original.key, converted.key);
        assertEquals(original.summary, converted.summary);
        assertEquals(original.description, converted.description);
        assertEquals(original.assigneeName, converted.assigneeName);
        assertEquals(original.reporterName, converted.reporterName);
        assertEquals(original.status, converted.status);
        assertEquals(original.priority, converted.priority);
        assertEquals(original.projectKey, converted.projectKey);
        assertEquals(original.issueType, converted.issueType);
        assertEquals(original.comments, converted.comments);
    }

    @Test
    @DisplayName("Should maintain data integrity in round-trip Issue to Bug to Issue")
    void testRoundTrip_IssueToBugToIssue() {
        Issue original = createSampleIssue();

        Bug bug = IssueMapper.getBug(original);
        Issue converted = IssueMapper.getIssue(bug);

        assertEquals(original.id, converted.id);
        assertEquals(original.key, converted.key);
        assertEquals(original.summary, converted.summary);
        assertEquals(original.description, converted.description);
        assertEquals(original.assigneeName, converted.assigneeName);
        assertEquals(original.reporterName, converted.reporterName);
        assertEquals(original.status, converted.status);
        assertEquals(original.priority, converted.priority);
        assertEquals(original.projectKey, converted.projectKey);
        assertEquals(original.issueType, converted.issueType);
        assertEquals(original.comments, converted.comments);
    }

    @Test
    @DisplayName("Should maintain data integrity in round-trip Issue to Story to Issue")
    void testRoundTrip_IssueToStoryToIssue() {
        Issue original = createSampleIssue();

        Story story = IssueMapper.getStory(original);
        Issue converted = IssueMapper.getIssue(story);

        assertEquals(original.id, converted.id);
        assertEquals(original.key, converted.key);
        assertEquals(original.summary, converted.summary);
        assertEquals(original.description, converted.description);
        assertEquals(original.assigneeName, converted.assigneeName);
        assertEquals(original.reporterName, converted.reporterName);
        assertEquals(original.status, converted.status);
        assertEquals(original.priority, converted.priority);
        assertEquals(original.projectKey, converted.projectKey);
        assertEquals(original.issueType, converted.issueType);
        assertEquals(original.comments, converted.comments);
    }

    @Test
    @DisplayName("Should maintain data integrity in round-trip Issue to Task to Issue")
    void testRoundTrip_IssueToTaskToIssue() {
        Issue original = createSampleIssue();

        Task task = IssueMapper.getTask(original);
        Issue converted = IssueMapper.getIssue(task);

        assertEquals(original.id, converted.id);
        assertEquals(original.key, converted.key);
        assertEquals(original.summary, converted.summary);
        assertEquals(original.description, converted.description);
        assertEquals(original.assigneeName, converted.assigneeName);
        assertEquals(original.reporterName, converted.reporterName);
        assertEquals(original.status, converted.status);
        assertEquals(original.priority, converted.priority);
        assertEquals(original.projectKey, converted.projectKey);
        assertEquals(original.issueType, converted.issueType);
        assertEquals(original.comments, converted.comments);
    }

    @Test
    @DisplayName("Should maintain data integrity in round-trip Issue to Subtask to Issue")
    void testRoundTrip_IssueToSubtaskToIssue() {
        Issue original = createSampleIssue();

        Subtask subtask = IssueMapper.getSubtask(original);
        Issue converted = IssueMapper.getIssue(subtask);

        assertEquals(original.id, converted.id);
        assertEquals(original.key, converted.key);
        assertEquals(original.summary, converted.summary);
        assertEquals(original.description, converted.description);
        assertEquals(original.assigneeName, converted.assigneeName);
        assertEquals(original.reporterName, converted.reporterName);
        assertEquals(original.status, converted.status);
        assertEquals(original.priority, converted.priority);
        assertEquals(original.projectKey, converted.projectKey);
        assertEquals(original.issueType, converted.issueType);
        assertEquals(original.parentKey, converted.parentKey);
        assertEquals(original.comments, converted.comments);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle empty comments list")
    void testEmptyCommentsList() {
        Issue issue = new Issue();
        issue.comments = Collections.emptyList();

        Epic epic = IssueMapper.getEpic(issue);
        Bug bug = IssueMapper.getBug(issue);
        Story story = IssueMapper.getStory(issue);
        Task task = IssueMapper.getTask(issue);
        Subtask subtask = IssueMapper.getSubtask(issue);

        assertEquals(0, epic.comments.size());
        assertEquals(0, bug.comments.size());
        assertEquals(0, story.comments.size());
        assertEquals(0, task.comments.size());
        assertEquals(0, subtask.comments.size());
    }

    @Test
    @DisplayName("Should handle large comments list")
    void testLargeCommentsList() {
        Issue issue = new Issue();
        List<String> largeCommentsList = new java.util.ArrayList<>();
        for (int i = 0; i < 100; i++) {
            largeCommentsList.add("Comment " + i);
        }
        issue.comments = largeCommentsList;

        Epic epic = IssueMapper.getEpic(issue);

        assertEquals(100, epic.comments.size());
        assertEquals("Comment 0", epic.comments.get(0));
        assertEquals("Comment 99", epic.comments.get(99));
    }

    @Test
    @DisplayName("Should handle very long strings")
    void testVeryLongStrings() {
        Issue issue = new Issue();
        String longString = "A".repeat(10000);
        issue.summary = longString;
        issue.description = longString;

        Epic epic = IssueMapper.getEpic(issue);

        assertEquals(10000, epic.summary.length());
        assertEquals(10000, epic.description.length());
    }

    @Test
    @DisplayName("Should handle special characters in all fields")
    void testSpecialCharactersInAllFields() {
        Issue issue = new Issue();
        issue.summary = "Test's \"Summary\" & <html>";
        issue.description = "Description\nwith\ttabs\rand\nnewlines";
        issue.assigneeName = "O'Brien & Co.";
        issue.reporterName = "Smith, Jr.";
        issue.key = "PROJ-123";

        Bug bug = IssueMapper.getBug(issue);

        assertEquals("Test's \"Summary\" & <html>", bug.summary);
        assertEquals("Description\nwith\ttabs\rand\nnewlines", bug.description);
        assertEquals("O'Brien & Co.", bug.assigneeName);
        assertEquals("Smith, Jr.", bug.reporterName);
    }

    @Test
    @DisplayName("Should handle unicode characters")
    void testUnicodeCharacters() {
        Issue issue = new Issue();
        issue.summary = "测试摘要 🚀";
        issue.description = "説明 テスト";
        issue.assigneeName = "用户名";

        Story story = IssueMapper.getStory(issue);

        assertEquals("测试摘要 🚀", story.summary);
        assertEquals("説明 テスト", story.description);
        assertEquals("用户名", story.assigneeName);
    }

    @Test
    @DisplayName("Should create independent objects")
    void testIndependentObjects() {
        Issue issue = createSampleIssue();

        Epic epic1 = IssueMapper.getEpic(issue);
        Epic epic2 = IssueMapper.getEpic(issue);

        assertNotSame(epic1, epic2);

        epic1.summary = "Modified Summary";

        assertNotEquals(epic1.summary, epic2.summary);
        assertEquals("Test Summary", epic2.summary);
    }

    @Test
    @DisplayName("Should handle all entity types from same Issue")
    void testAllEntityTypesFromSameIssue() {
        Issue issue = createSampleIssue();

        Epic epic = IssueMapper.getEpic(issue);
        Bug bug = IssueMapper.getBug(issue);
        Story story = IssueMapper.getStory(issue);
        Task task = IssueMapper.getTask(issue);
        Subtask subtask = IssueMapper.getSubtask(issue);

        // All should have same core data
        assertEquals(issue.id, epic.id);
        assertEquals(issue.id, bug.id);
        assertEquals(issue.id, story.id);
        assertEquals(issue.id, task.id);
        assertEquals(issue.id, subtask.id);

        assertEquals(issue.summary, epic.summary);
        assertEquals(issue.summary, bug.summary);
        assertEquals(issue.summary, story.summary);
        assertEquals(issue.summary, task.summary);
        assertEquals(issue.summary, subtask.summary);
    }
}