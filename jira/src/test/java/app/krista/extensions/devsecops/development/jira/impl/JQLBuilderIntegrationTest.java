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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JQLBuilder.
 * Tests all JQL query building scenarios including edge cases.
 */
@DisplayName("JQLBuilder Integration Tests")
class JQLBuilderIntegrationTest {

    @Test
    @DisplayName("Should build simple project query")
    void testBuildSimpleProjectQuery() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .build();

        assertEquals(" project = \"TEST\" ", jql);
    }

    @Test
    @DisplayName("Should build query with status")
    void testBuildQueryWithStatus() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .status("In Progress")
                .build();

        assertTrue(jql.contains("project = \"TEST\""));
        assertTrue(jql.contains("status = \"In Progress\""));
        assertTrue(jql.contains(" and "));
    }

    @Test
    @DisplayName("Should build query with priority")
    void testBuildQueryWithPriority() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .priority("High")
                .build();

        assertTrue(jql.contains("project = \"TEST\""));
        assertTrue(jql.contains("priority = \"High\""));
    }

    @Test
    @DisplayName("Should build query with issue type")
    void testBuildQueryWithIssueType() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .issueType("Bug")
                .build();

        assertTrue(jql.contains("project = \"TEST\""));
        assertTrue(jql.contains("issuetype = \"Bug\""));
    }

    @Test
    @DisplayName("Should build query with assignee")
    void testBuildQueryWithAssignee() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .assignee("john.doe@example.com")
                .build();

        assertTrue(jql.contains("project = \"TEST\""));
        assertTrue(jql.contains("assignee = \"john.doe@example.com\""));
    }

    @Test
    @DisplayName("Should build query with fix version")
    void testBuildQueryWithFixVersion() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .fixVersion("1.0.0")
                .build();

        assertTrue(jql.contains("project = \"TEST\""));
        assertTrue(jql.contains("fixversion = \"1.0.0\""));
    }

    @Test
    @DisplayName("Should build query with summary search")
    void testBuildQueryWithSummary() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .summary("login bug")
                .build();

        assertTrue(jql.contains("project = \"TEST\""));
        assertTrue(jql.contains("summary ~ \"login bug\""));
    }

    @Test
    @DisplayName("Should build query with text search")
    void testBuildQueryWithText() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .text("authentication")
                .build();

        assertTrue(jql.contains("project = \"TEST\""));
        assertTrue(jql.contains("text ~ \"authentication\""));
    }

    @Test
    @DisplayName("Should build complex query with all fields")
    void testBuildComplexQuery() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .issueType("Bug")
                .status("Open")
                .priority("High")
                .assignee("test@example.com")
                .fixVersion("1.0.0")
                .build();

        assertTrue(jql.contains("project = \"TEST\""));
        assertTrue(jql.contains("issuetype = \"Bug\""));
        assertTrue(jql.contains("status = \"Open\""));
        assertTrue(jql.contains("priority = \"High\""));
        assertTrue(jql.contains("assignee = \"test@example.com\""));
        assertTrue(jql.contains("fixversion = \"1.0.0\""));
        assertFalse(jql.endsWith(" and "), "Query should not end with 'and'");
    }

    @Test
    @DisplayName("Should build query with ORDER BY ascending")
    void testBuildQueryWithOrderByAsc() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .status("Open")
                .orderBy("created", "asc")
                .build();

        assertTrue(jql.contains("project = \"TEST\""));
        assertTrue(jql.contains("status = \"Open\""));
        assertTrue(jql.contains("ORDER BY created asc"));
        assertFalse(jql.contains(" and ORDER BY"), "Should not have 'and' before ORDER BY");
    }

    @Test
    @DisplayName("Should build query with ORDER BY descending")
    void testBuildQueryWithOrderByDesc() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .priority("High")
                .orderBy("priority", "desc")
                .build();

        assertTrue(jql.contains("project = \"TEST\""));
        assertTrue(jql.contains("priority = \"High\""));
        assertTrue(jql.contains("ORDER BY priority desc"));
    }

    @Test
    @DisplayName("Should ignore null values")
    void testIgnoreNullValues() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .status(null)
                .priority(null)
                .assignee(null)
                .build();

        assertEquals(" project = \"TEST\" ", jql);
        assertFalse(jql.contains("status"));
        assertFalse(jql.contains("priority"));
        assertFalse(jql.contains("assignee"));
    }

    @Test
    @DisplayName("Should ignore empty strings")
    void testIgnoreEmptyStrings() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .status("")
                .priority("   ")
                .build();

        assertEquals(" project = \"TEST\" ", jql);
        assertFalse(jql.contains("status"));
        assertFalse(jql.contains("priority"));
    }

    @Test
    @DisplayName("Should handle empty query build")
    void testEmptyQueryBuild() {
        String jql = new JQLBuilder().build();
        assertEquals("", jql);
    }

    @Test
    @DisplayName("Should handle orderBy with invalid direction")
    void testOrderByWithInvalidDirection() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .orderBy("created", "invalid")
                .build();

        assertEquals(" project = \"TEST\" ", jql);
        assertFalse(jql.contains("ORDER BY"));
    }

    @Test
    @DisplayName("Should handle orderBy with null field")
    void testOrderByWithNullField() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .orderBy(null, "asc")
                .build();

        assertEquals(" project = \"TEST\" ", jql);
        assertFalse(jql.contains("ORDER BY"));
    }

    @Test
    @DisplayName("Should handle orderBy with empty field")
    void testOrderByWithEmptyField() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .orderBy("", "asc")
                .build();

        assertEquals(" project = \"TEST\" ", jql);
        assertFalse(jql.contains("ORDER BY"));
    }

    @Test
    @DisplayName("Should test removeEndsWith utility method")
    void testRemoveEndsWithUtility() {
        String result = JQLBuilder.removeEndsWith("test and ", " and ");
        assertEquals("test", result);
    }

    @Test
    @DisplayName("Should test removeEndsWith with no match")
    void testRemoveEndsWithNoMatch() {
        String result = JQLBuilder.removeEndsWith("test", " and ");
        assertEquals("test", result);
    }

    @Test
    @DisplayName("Should test removeEndsWith with empty string")
    void testRemoveEndsWithEmptyString() {
        String result = JQLBuilder.removeEndsWith("", " and ");
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should build query with all null values")
    void testBuildQueryWithAllNullValues() {
        String jql = new JQLBuilder()
                .projectKey(null)
                .status(null)
                .priority(null)
                .issueType(null)
                .assignee(null)
                .fixVersion(null)
                .summary(null)
                .text(null)
                .build();

        assertEquals("", jql);
    }

    @Test
    @DisplayName("Should chain multiple methods fluently")
    void testFluentChaining() {
        JQLBuilder builder = new JQLBuilder();
        JQLBuilder result = builder.projectKey("TEST")
                .status("Open")
                .priority("High");

        assertSame(builder, result, "Should return same instance for fluent chaining");
    }

    @Test
    @DisplayName("Should handle complex ORDER BY with multiple fields")
    void testComplexOrderBy() {
        String jql = new JQLBuilder()
                .projectKey("TEST")
                .status("Open")
                .orderBy("priority DESC, updated", "desc")
                .build();

        assertTrue(jql.contains("ORDER BY priority DESC, updated desc"));
    }
}