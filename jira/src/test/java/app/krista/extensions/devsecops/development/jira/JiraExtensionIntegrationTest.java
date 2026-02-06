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

package app.krista.extensions.devsecops.development.jira;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.authentication.JiraRequestAuthenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for JiraExtension.
 * Tests main extension functionality and lifecycle methods.
 */
@DisplayName("JiraExtension Integration Tests")
class JiraExtensionIntegrationTest {

    @Mock
    private JiraRequestAuthenticator mockAuthenticator;

    @Mock
    private Invoker mockInvoker;

    @Mock
    private JiraAttributes mockJiraAttributes;

    private JiraExtension extension;
    private Map<String, Object> testAttributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testAttributes = new HashMap<>();
        testAttributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        testAttributes.put(JiraAttributes.USER_NAME, "test@example.com");
        testAttributes.put(JiraAttributes.AUTH_TOKEN, "test-token");

        when(mockInvoker.getAttributes()).thenReturn(testAttributes);

        extension = new JiraExtension(mockAuthenticator, mockInvoker, mockJiraAttributes);
    }

    @Test
    @DisplayName("Should return custom tabs configuration")
    void testCustomTabs() {
        Map<String, String> tabs = extension.customTabs();

        assertNotNull(tabs);
        assertFalse(tabs.isEmpty());
        assertTrue(tabs.containsKey("Documentation"));
        assertEquals("static/docs", tabs.get("Documentation"));
    }

    @Test
    @DisplayName("Should test connection successfully")
    void testTestConnection() {
        doNothing().when(mockJiraAttributes).testConnection(any());

        assertDoesNotThrow(() -> extension.testConnection());

        verify(mockJiraAttributes).testConnection(testAttributes);
    }

    @Test
    @DisplayName("Should validate attributes")
    void testValidateAttributes() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");

        doNothing().when(mockJiraAttributes).setAttributes(any());
        doNothing().when(mockJiraAttributes).validateAttributes(any());

        assertDoesNotThrow(() -> extension.validateAttributes(attributes));

        verify(mockJiraAttributes).setAttributes(attributes);
        verify(mockJiraAttributes).validateAttributes(attributes);
    }

    @Test
    @DisplayName("Should return request authenticator")
    void testGetRequestAuthenticator() {
        RequestAuthenticator authenticator = extension.getRequestAuthenticator();

        assertNotNull(authenticator);
        assertSame(mockAuthenticator, authenticator);
    }

    @Test
    @DisplayName("Should handle attributes updated")
    void testAttributesUpdated() {
        Map<String, Object> oldAttributes = new HashMap<>();
        oldAttributes.put(JiraAttributes.HOST_URL, "https://old.atlassian.net");

        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(JiraAttributes.HOST_URL, "https://new.atlassian.net");

        doNothing().when(mockJiraAttributes).update(any());

        assertDoesNotThrow(() -> extension.attributesUpdated(oldAttributes, newAttributes));

        verify(mockJiraAttributes).update(newAttributes);
    }

    @Test
    @DisplayName("Should handle null old attributes in update")
    void testAttributesUpdatedWithNullOld() {
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(JiraAttributes.HOST_URL, "https://new.atlassian.net");

        doNothing().when(mockJiraAttributes).update(any());

        assertDoesNotThrow(() -> extension.attributesUpdated(null, newAttributes));

        verify(mockJiraAttributes).update(newAttributes);
    }

    @Test
    @DisplayName("Should handle empty attributes in validation")
    void testValidateEmptyAttributes() {
        Map<String, Object> emptyAttributes = new HashMap<>();

        doNothing().when(mockJiraAttributes).setAttributes(any());
        doNothing().when(mockJiraAttributes).validateAttributes(any());

        assertDoesNotThrow(() -> extension.validateAttributes(emptyAttributes));

        verify(mockJiraAttributes).setAttributes(emptyAttributes);
        verify(mockJiraAttributes).validateAttributes(emptyAttributes);
    }
}