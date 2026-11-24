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

package app.krista.extensions.devsecops.development.jira.services;

import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JiraUser Integration Tests")
class JiraUserIntegrationTest {

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create JiraUser with all fields")
    void testConstructor_AllFields() {
        JiraUser jiraUser = new JiraUser(
            "account123",
            "John Doe",
            "atlassian",
            "john.doe@example.com"
        );
        
        assertEquals("account123", jiraUser.accountId);
        assertEquals("John Doe", jiraUser.displayName);
        assertEquals("atlassian", jiraUser.accountType);
        assertEquals("john.doe@example.com", jiraUser.emailId);
    }

    @Test
    @DisplayName("Should create JiraUser with null values")
    void testConstructor_NullValues() {
        JiraUser jiraUser = new JiraUser(null, null, null, null);
        
        assertNull(jiraUser.accountId);
        assertNull(jiraUser.displayName);
        assertNull(jiraUser.accountType);
        assertNull(jiraUser.emailId);
    }

    @Test
    @DisplayName("Should create JiraUser with empty strings")
    void testConstructor_EmptyStrings() {
        JiraUser jiraUser = new JiraUser("", "", "", "");
        
        assertEquals("", jiraUser.accountId);
        assertEquals("", jiraUser.displayName);
        assertEquals("", jiraUser.accountType);
        assertEquals("", jiraUser.emailId);
    }

    @Test
    @DisplayName("Should create JiraUser with mixed null and valid values")
    void testConstructor_MixedValues() {
        JiraUser jiraUser = new JiraUser(
            "account456",
            null,
            "customer",
            ""
        );
        
        assertEquals("account456", jiraUser.accountId);
        assertNull(jiraUser.displayName);
        assertEquals("customer", jiraUser.accountType);
        assertEquals("", jiraUser.emailId);
    }

    // ==================== create() Method Tests ====================

    @Test
    @DisplayName("Should create JiraUser from JsonObject with all fields")
    void testCreate_AllFields() {
        JsonObject userObject = new JsonObject();
        userObject.addProperty("accountId", "acc789");
        userObject.addProperty("displayName", "Jane Smith");
        userObject.addProperty("accountType", "atlassian");
        userObject.addProperty("emailAddress", "jane.smith@example.com");
        
        JiraUser jiraUser = JiraUser.create(userObject);
        
        assertEquals("acc789", jiraUser.accountId);
        assertEquals("Jane Smith", jiraUser.displayName);
        assertEquals("atlassian", jiraUser.accountType);
        assertEquals("jane.smith@example.com", jiraUser.emailId);
    }

    @Test
    @DisplayName("Should create JiraUser from JsonObject without emailAddress")
    void testCreate_WithoutEmailAddress() {
        JsonObject userObject = new JsonObject();
        userObject.addProperty("accountId", "acc999");
        userObject.addProperty("displayName", "Bob Johnson");
        userObject.addProperty("accountType", "customer");
        
        JiraUser jiraUser = JiraUser.create(userObject);
        
        assertEquals("acc999", jiraUser.accountId);
        assertEquals("Bob Johnson", jiraUser.displayName);
        assertEquals("customer", jiraUser.accountType);
        assertEquals("", jiraUser.emailId);
    }

    @Test
    @DisplayName("Should create JiraUser from JsonObject with empty emailAddress")
    void testCreate_EmptyEmailAddress() {
        JsonObject userObject = new JsonObject();
        userObject.addProperty("accountId", "acc111");
        userObject.addProperty("displayName", "Alice Brown");
        userObject.addProperty("accountType", "app");
        userObject.addProperty("emailAddress", "");
        
        JiraUser jiraUser = JiraUser.create(userObject);
        
        assertEquals("acc111", jiraUser.accountId);
        assertEquals("Alice Brown", jiraUser.displayName);
        assertEquals("app", jiraUser.accountType);
        assertEquals("", jiraUser.emailId);
    }

    @Test
    @DisplayName("Should create JiraUser from JsonObject with special characters")
    void testCreate_SpecialCharacters() {
        JsonObject userObject = new JsonObject();
        userObject.addProperty("accountId", "acc-special-123");
        userObject.addProperty("displayName", "User O'Brien");
        userObject.addProperty("accountType", "atlassian");
        userObject.addProperty("emailAddress", "user+test@example.com");
        
        JiraUser jiraUser = JiraUser.create(userObject);
        
        assertEquals("acc-special-123", jiraUser.accountId);
        assertEquals("User O'Brien", jiraUser.displayName);
        assertEquals("atlassian", jiraUser.accountType);
        assertEquals("user+test@example.com", jiraUser.emailId);
    }

    @Test
    @DisplayName("Should create JiraUser from JsonObject with unicode characters")
    void testCreate_UnicodeCharacters() {
        JsonObject userObject = new JsonObject();
        userObject.addProperty("accountId", "acc-unicode");
        userObject.addProperty("displayName", "用户名");
        userObject.addProperty("accountType", "atlassian");
        userObject.addProperty("emailAddress", "user@例え.com");
        
        JiraUser jiraUser = JiraUser.create(userObject);
        
        assertEquals("acc-unicode", jiraUser.accountId);
        assertEquals("用户名", jiraUser.displayName);
        assertEquals("atlassian", jiraUser.accountType);
        assertEquals("user@例え.com", jiraUser.emailId);
    }

    // ==================== summary() Method Tests ====================

    @Test
    @DisplayName("Should return displayName from summary()")
    void testSummary_ValidDisplayName() {
        JiraUser jiraUser = new JiraUser(
            "acc123",
            "Test User",
            "atlassian",
            "test@example.com"
        );

        String summary = jiraUser.summary();

        assertEquals("Test User", summary);
    }

    @Test
    @DisplayName("Should return null from summary() when displayName is null")
    void testSummary_NullDisplayName() {
        JiraUser jiraUser = new JiraUser(
            "acc123",
            null,
            "atlassian",
            "test@example.com"
        );

        String summary = jiraUser.summary();

        assertNull(summary);
    }

    @Test
    @DisplayName("Should return empty string from summary() when displayName is empty")
    void testSummary_EmptyDisplayName() {
        JiraUser jiraUser = new JiraUser(
            "acc123",
            "",
            "atlassian",
            "test@example.com"
        );

        String summary = jiraUser.summary();

        assertEquals("", summary);
    }

    @Test
    @DisplayName("Should return displayName with special characters from summary()")
    void testSummary_SpecialCharacters() {
        JiraUser jiraUser = new JiraUser(
            "acc123",
            "User's Name & Co.",
            "atlassian",
            "test@example.com"
        );

        String summary = jiraUser.summary();

        assertEquals("User's Name & Co.", summary);
    }

    // ==================== getUser() Method Tests ====================

    @Test
    @DisplayName("Should convert JiraUser to User entity with all fields")
    void testGetUser_AllFields() {
        JiraUser jiraUser = new JiraUser(
            "account-id-123",
            "John Doe",
            "atlassian",
            "john.doe@example.com"
        );

        User user = jiraUser.getUser();

        assertNotNull(user);
        assertEquals("account-id-123", user.accountId);
        assertEquals("John Doe", user.name);
        assertEquals("atlassian", user.accountType);
        assertEquals("john.doe@example.com", user.email);
    }

    @Test
    @DisplayName("Should convert JiraUser to User entity with null values")
    void testGetUser_NullValues() {
        JiraUser jiraUser = new JiraUser(null, null, null, null);

        User user = jiraUser.getUser();

        assertNotNull(user);
        assertNull(user.accountId);
        assertNull(user.name);
        assertNull(user.accountType);
        assertNull(user.email);
    }

    @Test
    @DisplayName("Should convert JiraUser to User entity with empty strings")
    void testGetUser_EmptyStrings() {
        JiraUser jiraUser = new JiraUser("", "", "", "");

        User user = jiraUser.getUser();

        assertNotNull(user);
        assertEquals("", user.accountId);
        assertEquals("", user.name);
        assertEquals("", user.accountType);
        assertEquals("", user.email);
    }

    @Test
    @DisplayName("Should convert JiraUser to User entity with mixed values")
    void testGetUser_MixedValues() {
        JiraUser jiraUser = new JiraUser(
            "acc456",
            null,
            "customer",
            ""
        );

        User user = jiraUser.getUser();

        assertNotNull(user);
        assertEquals("acc456", user.accountId);
        assertNull(user.name);
        assertEquals("customer", user.accountType);
        assertEquals("", user.email);
    }

    @Test
    @DisplayName("Should convert JiraUser to User entity with long values")
    void testGetUser_LongValues() {
        String longAccountId = "a".repeat(100);
        String longDisplayName = "b".repeat(100);
        String longAccountType = "c".repeat(100);
        String longEmail = "d".repeat(50) + "@example.com";

        JiraUser jiraUser = new JiraUser(
            longAccountId,
            longDisplayName,
            longAccountType,
            longEmail
        );

        User user = jiraUser.getUser();

        assertNotNull(user);
        assertEquals(longAccountId, user.accountId);
        assertEquals(longDisplayName, user.name);
        assertEquals(longAccountType, user.accountType);
        assertEquals(longEmail, user.email);
    }

    @Test
    @DisplayName("Should create multiple User entities from same JiraUser")
    void testGetUser_MultipleInvocations() {
        JiraUser jiraUser = new JiraUser(
            "acc789",
            "Jane Smith",
            "atlassian",
            "jane@example.com"
        );

        User user1 = jiraUser.getUser();
        User user2 = jiraUser.getUser();

        assertNotNull(user1);
        assertNotNull(user2);
        assertNotSame(user1, user2); // Should be different instances
        assertEquals(user1.accountId, user2.accountId);
        assertEquals(user1.name, user2.name);
        assertEquals(user1.accountType, user2.accountType);
        assertEquals(user1.email, user2.email);
    }

    // ==================== Field Immutability Tests ====================

    @Test
    @DisplayName("Should have final fields that cannot be reassigned")
    void testFieldsAreFinal() throws NoSuchFieldException {
        assertTrue(java.lang.reflect.Modifier.isFinal(
            JiraUser.class.getDeclaredField("accountId").getModifiers()
        ));
        assertTrue(java.lang.reflect.Modifier.isFinal(
            JiraUser.class.getDeclaredField("displayName").getModifiers()
        ));
        assertTrue(java.lang.reflect.Modifier.isFinal(
            JiraUser.class.getDeclaredField("accountType").getModifiers()
        ));
        assertTrue(java.lang.reflect.Modifier.isFinal(
            JiraUser.class.getDeclaredField("emailId").getModifiers()
        ));
    }

    @Test
    @DisplayName("Should verify JiraUser is final class")
    void testJiraUserIsFinal() {
        assertTrue(java.lang.reflect.Modifier.isFinal(JiraUser.class.getModifiers()));
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle whitespace in fields")
    void testCreate_WhitespaceValues() {
        JsonObject userObject = new JsonObject();
        userObject.addProperty("accountId", "  acc-whitespace  ");
        userObject.addProperty("displayName", "  User Name  ");
        userObject.addProperty("accountType", "  atlassian  ");
        userObject.addProperty("emailAddress", "  user@example.com  ");

        JiraUser jiraUser = JiraUser.create(userObject);

        assertEquals("  acc-whitespace  ", jiraUser.accountId);
        assertEquals("  User Name  ", jiraUser.displayName);
        assertEquals("  atlassian  ", jiraUser.accountType);
        assertEquals("  user@example.com  ", jiraUser.emailId);
    }

    @Test
    @DisplayName("Should handle very long email addresses")
    void testCreate_VeryLongEmail() {
        JsonObject userObject = new JsonObject();
        userObject.addProperty("accountId", "acc-long-email");
        userObject.addProperty("displayName", "User");
        userObject.addProperty("accountType", "atlassian");
        String longEmail = "very.long.email.address.with.many.dots.and.characters@subdomain.example.com";
        userObject.addProperty("emailAddress", longEmail);

        JiraUser jiraUser = JiraUser.create(userObject);

        assertEquals(longEmail, jiraUser.emailId);
    }

    @Test
    @DisplayName("Should handle account types with different values")
    void testCreate_DifferentAccountTypes() {
        String[] accountTypes = {"atlassian", "customer", "app", "unknown"};

        for (String accountType : accountTypes) {
            JsonObject userObject = new JsonObject();
            userObject.addProperty("accountId", "acc-" + accountType);
            userObject.addProperty("displayName", "User");
            userObject.addProperty("accountType", accountType);

            JiraUser jiraUser = JiraUser.create(userObject);

            assertEquals(accountType, jiraUser.accountType);
        }
    }

    @Test
    @DisplayName("Should handle displayName with newlines and tabs")
    void testCreate_DisplayNameWithControlCharacters() {
        JsonObject userObject = new JsonObject();
        userObject.addProperty("accountId", "acc-control");
        userObject.addProperty("displayName", "User\nWith\tControl\rChars");
        userObject.addProperty("accountType", "atlassian");

        JiraUser jiraUser = JiraUser.create(userObject);

        assertEquals("User\nWith\tControl\rChars", jiraUser.displayName);
    }

    @Test
    @DisplayName("Should handle numeric-like strings in fields")
    void testCreate_NumericStrings() {
        JsonObject userObject = new JsonObject();
        userObject.addProperty("accountId", "12345");
        userObject.addProperty("displayName", "67890");
        userObject.addProperty("accountType", "99999");
        userObject.addProperty("emailAddress", "123@456.789");

        JiraUser jiraUser = JiraUser.create(userObject);

        assertEquals("12345", jiraUser.accountId);
        assertEquals("67890", jiraUser.displayName);
        assertEquals("99999", jiraUser.accountType);
        assertEquals("123@456.789", jiraUser.emailId);
    }
}


