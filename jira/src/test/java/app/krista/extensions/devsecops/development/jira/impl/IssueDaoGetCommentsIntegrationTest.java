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

package app.krista.extensions.devsecops.development.jira.impl;

import app.krista.extensions.devsecops.development.jira.BaseIntegrationTest;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for IssueDao.getComments() method.
 * Tests comment retrieval with various scenarios including ADF body parsing,
 * author information extraction, and date-time formatting.
 */
@DisplayName("IssueDao.getComments Integration Tests")
class IssueDaoGetCommentsIntegrationTest extends BaseIntegrationTest {

    @Mock
    private RestClient mockRestClient;

    private IssueDao issueDao;

    @BeforeEach
    void setUp() {
        mockRestClient = mock(RestClient.class);
        when(restClientFactory.getInstance()).thenReturn(mockRestClient);
        when(restClientFactory.invokeAsUser()).thenReturn(false);
        issueDao = new IssueDao(restClientFactory, mockRestClient);
    }

    @Test
    @DisplayName("Should get comments with all fields successfully")
    void testGetCommentsWithAllFields() {
        JsonObject response = createCommentResponse(
                createComment("10001", "This is a test comment", "2026-01-22T16:06:53.126+0530",
                        "712020:8af38a41-3323-45a7-af18-d32c96ae692a", "Test User")
        );

        when(mockRestClient.fetch(eq("issue/TEST-123/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-123");

        assertNotNull(comments);
        assertEquals(1, comments.size());

        Map<String, Object> comment = comments.get(0);
        assertTrue(comment.get("Comment Body").toString().trim().equals("This is a test comment"));
        assertEquals("712020:8af38a41-3323-45a7-af18-d32c96ae692a", comment.get("Author ID"));
        assertEquals("Test User", comment.get("Author Name"));
        assertNotNull(comment.get("Date Created"));
        assertTrue(comment.get("Date Created").toString().contains("Jan 22, 2026"));
        assertTrue(comment.get("Date Created").toString().contains("PM"));

        // Verify orderBy=-created is passed for descending sort (latest first)
        verify(mockRestClient).fetch(eq("issue/TEST-123/comment"),
                argThat(map -> "-created".equals(map.get("orderBy"))), eq(String.class));
    }

    @Test
    @DisplayName("Should get multiple comments successfully")
    void testGetMultipleComments() {
        JsonObject response = createCommentResponse(
                createComment("10001", "First comment", "2026-01-22T10:30:00.000+0000",
                        "user1", "User One"),
                createComment("10002", "Second comment", "2026-01-22T14:45:00.000+0000",
                        "user2", "User Two"),
                createComment("10003", "Third comment", "2026-01-22T18:20:00.000+0000",
                        "user3", "User Three")
        );

        when(mockRestClient.fetch(eq("issue/TEST-456/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-456");

        assertNotNull(comments);
        assertEquals(3, comments.size());
        assertTrue(comments.get(0).get("Comment Body").toString().trim().equals("First comment"));
        assertTrue(comments.get(1).get("Comment Body").toString().trim().equals("Second comment"));
        assertTrue(comments.get(2).get("Comment Body").toString().trim().equals("Third comment"));
        assertEquals("User One", comments.get(0).get("Author Name"));
        assertEquals("User Two", comments.get(1).get("Author Name"));
        assertEquals("User Three", comments.get(2).get("Author Name"));
    }

    @Test
    @DisplayName("Should return empty list when no comments exist")
    void testGetCommentsWhenNoCommentsExist() {
        JsonObject response = createEmptyCommentResponse();

        when(mockRestClient.fetch(eq("issue/TEST-789/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-789");

        assertNotNull(comments);
        assertTrue(comments.isEmpty());
    }

    @Test
    @DisplayName("Should handle comment with ADF body format")
    void testGetCommentsWithADFBody() {
        JsonObject adfBody = createADFBody("This is ADF formatted text");
        JsonObject comment = createCommentWithADFBody("10001", adfBody, "2026-01-22T12:00:00.000+0000",
                "user1", "Test User");
        JsonObject response = createCommentResponse(comment);

        when(mockRestClient.fetch(eq("issue/TEST-100/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-100");

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertTrue(comments.get(0).get("Comment Body").toString().trim().equals("This is ADF formatted text"));
    }

    @Test
    @DisplayName("Should handle comment with primitive body format")
    void testGetCommentsWithPrimitiveBody() {
        JsonObject comment = createCommentWithPrimitiveBody("10001", "Simple text comment",
                "2026-01-22T12:00:00.000+0000", "user1", "Test User");
        JsonObject response = createCommentResponse(comment);

        when(mockRestClient.fetch(eq("issue/TEST-200/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-200");

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("Simple text comment", comments.get(0).get("Comment Body"));
    }

    @Test
    @DisplayName("Should handle comment without body field")
    void testGetCommentsWithoutBody() {
        JsonObject comment = new JsonObject();
        comment.addProperty("id", "10001");
        comment.addProperty("created", "2026-01-22T12:00:00.000+0000");
        comment.add("author", createAuthor("user1", "Test User"));

        JsonObject response = createCommentResponse(comment);

        when(mockRestClient.fetch(eq("issue/TEST-300/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-300");

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertNull(comments.get(0).get("Comment Body"));
    }

    @Test
    @DisplayName("Should handle comment without author field")
    void testGetCommentsWithoutAuthor() {
        JsonObject comment = new JsonObject();
        comment.addProperty("id", "10001");
        comment.addProperty("body", "Comment without author");
        comment.addProperty("created", "2026-01-22T12:00:00.000+0000");

        JsonObject response = createCommentResponse(comment);

        when(mockRestClient.fetch(eq("issue/TEST-400/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-400");

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertNull(comments.get(0).get("Author ID"));
        assertNull(comments.get(0).get("Author Name"));
    }

    @Test
    @DisplayName("Should handle comment without created date field")
    void testGetCommentsWithoutCreatedDate() {
        JsonObject comment = new JsonObject();
        comment.addProperty("id", "10001");
        comment.add("body", createADFBody("Comment without date"));
        comment.add("author", createAuthor("user1", "Test User"));

        JsonObject response = createCommentResponse(comment);

        when(mockRestClient.fetch(eq("issue/TEST-500/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-500");

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertNull(comments.get(0).get("Date Created"));
    }

    @Test
    @DisplayName("Should handle comment with invalid date format")
    void testGetCommentsWithInvalidDateFormat() {
        JsonObject comment = createComment("10001", "Comment with bad date",
                "invalid-date-format", "user1", "Test User");
        JsonObject response = createCommentResponse(comment);

        when(mockRestClient.fetch(eq("issue/TEST-600/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-600");

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertNull(comments.get(0).get("Date Created"));
    }

    @Test
    @DisplayName("Should handle comment with author missing accountId")
    void testGetCommentsWithAuthorMissingAccountId() {
        JsonObject author = new JsonObject();
        author.addProperty("displayName", "Test User");

        JsonObject comment = new JsonObject();
        comment.addProperty("id", "10001");
        comment.add("body", createADFBody("Test comment"));
        comment.addProperty("created", "2026-01-22T12:00:00.000+0000");
        comment.add("author", author);

        JsonObject response = createCommentResponse(comment);

        when(mockRestClient.fetch(eq("issue/TEST-700/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-700");

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertNull(comments.get(0).get("Author ID"));
        assertEquals("Test User", comments.get(0).get("Author Name"));
    }

    @Test
    @DisplayName("Should handle comment with author missing displayName")
    void testGetCommentsWithAuthorMissingDisplayName() {
        JsonObject author = new JsonObject();
        author.addProperty("accountId", "user1");

        JsonObject comment = new JsonObject();
        comment.addProperty("id", "10001");
        comment.add("body", createADFBody("Test comment"));
        comment.addProperty("created", "2026-01-22T12:00:00.000+0000");
        comment.add("author", author);

        JsonObject response = createCommentResponse(comment);

        when(mockRestClient.fetch(eq("issue/TEST-800/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-800");

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals("user1", comments.get(0).get("Author ID"));
        assertNull(comments.get(0).get("Author Name"));
    }

    @Test
    @DisplayName("Should handle different timezone formats in created date")
    void testGetCommentsWithDifferentTimezones() {
        JsonObject response = createCommentResponse(
                createComment("10001", "UTC timezone", "2026-01-22T12:00:00.000+0000",
                        "user1", "User One"),
                createComment("10002", "IST timezone", "2026-01-22T17:30:00.000+0530",
                        "user2", "User Two"),
                createComment("10003", "PST timezone", "2026-01-22T04:00:00.000-0800",
                        "user3", "User Three")
        );

        when(mockRestClient.fetch(eq("issue/TEST-900/comment"), any(HashMap.class), eq(String.class)))
                .thenReturn(response.toString());

        List<Map<String, Object>> comments = issueDao.getComments("TEST-900");

        assertNotNull(comments);
        assertEquals(3, comments.size());

        // All should have valid date-time strings
        for (Map<String, Object> comment : comments) {
            assertNotNull(comment.get("Date Created"));
            String dateCreated = comment.get("Date Created").toString();
            assertTrue(dateCreated.contains("2026"));
            assertTrue(dateCreated.matches(".*\\d{1,2}:\\d{2} [AP]M.*"));
        }
    }

    // Helper methods to create test data
    private JsonObject createCommentResponse(JsonObject... comments) {
        JsonObject response = new JsonObject();
        response.addProperty("startAt", 0);
        response.addProperty("maxResults", 100);
        response.addProperty("total", comments.length);

        JsonArray commentsArray = new JsonArray();
        for (JsonObject comment : comments) {
            commentsArray.add(comment);
        }
        response.add("comments", commentsArray);

        return response;
    }



    private JsonObject createEmptyCommentResponse() {
        JsonObject response = new JsonObject();
        response.addProperty("startAt", 0);
        response.addProperty("maxResults", 100);
        response.addProperty("total", 0);
        response.add("comments", new JsonArray());
        return response;
    }

    private JsonObject createComment(String id, String bodyText, String created, String accountId, String displayName) {
        JsonObject comment = new JsonObject();
        comment.addProperty("id", id);
        comment.addProperty("self", "https://test.atlassian.net/rest/api/3/issue/10000/comment/" + id);
        comment.add("body", createADFBody(bodyText));
        comment.addProperty("created", created);

        if (accountId != null && displayName != null) {
            comment.add("author", createAuthor(accountId, displayName));
        }

        return comment;
    }

    private JsonObject createCommentWithADFBody(String id, JsonObject adfBody, String created,
                                                 String accountId, String displayName) {
        JsonObject comment = new JsonObject();
        comment.addProperty("id", id);
        comment.addProperty("self", "https://test.atlassian.net/rest/api/3/issue/10000/comment/" + id);
        comment.add("body", adfBody);
        comment.addProperty("created", created);

        if (accountId != null && displayName != null) {
            comment.add("author", createAuthor(accountId, displayName));
        }

        return comment;
    }

    private JsonObject createCommentWithPrimitiveBody(String id, String bodyText, String created,
                                                       String accountId, String displayName) {
        JsonObject comment = new JsonObject();
        comment.addProperty("id", id);
        comment.addProperty("self", "https://test.atlassian.net/rest/api/3/issue/10000/comment/" + id);
        comment.addProperty("body", bodyText);
        comment.addProperty("created", created);

        if (accountId != null && displayName != null) {
            comment.add("author", createAuthor(accountId, displayName));
        }

        return comment;
    }

    private JsonObject createADFBody(String text) {
        JsonObject adfBody = new JsonObject();
        adfBody.addProperty("version", 1);
        adfBody.addProperty("type", "doc");

        JsonArray content = new JsonArray();
        JsonObject paragraph = new JsonObject();
        paragraph.addProperty("type", "paragraph");

        JsonArray paragraphContent = new JsonArray();
        JsonObject textNode = new JsonObject();
        textNode.addProperty("type", "text");
        textNode.addProperty("text", text);
        paragraphContent.add(textNode);

        paragraph.add("content", paragraphContent);
        content.add(paragraph);
        adfBody.add("content", content);

        return adfBody;
    }

    private JsonObject createAuthor(String accountId, String displayName) {
        JsonObject author = new JsonObject();
        author.addProperty("accountId", accountId);
        author.addProperty("displayName", displayName);
        author.addProperty("active", true);
        author.addProperty("self", "https://test.atlassian.net/rest/api/3/user?accountId=" + accountId);
        return author;
    }
}