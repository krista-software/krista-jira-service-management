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

import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.sdk.atlassian.config.AtlassianProduct;
import app.krista.sdk.atlassian.registry.annotations.AtlassianExtension;
import app.krista.sdk.atlassian.registry.annotations.OAuthScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 100 % coverage tests for {@link JsmExtensionConfig}.
 *
 * <p>The class is a pure registry marker: it has no logic beyond a private
 * constructor and a set of compile-time (RUNTIME-retained) annotations.
 * Coverage therefore requires:
 * <ol>
 *   <li>Executing the private constructor via reflection.</li>
 *   <li>Reading every annotation element to prevent dead-code warnings and
 *       to lock the metadata against accidental changes.</li>
 * </ol>
 */
@DisplayName("JsmExtensionConfig")
class JsmExtensionConfigTest {

    // ── Class-structure tests ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Class structure")
    class ClassStructureTests {

        @Test
        @DisplayName("Should be declared final")
        void shouldBeFinal() {
            assertTrue(Modifier.isFinal(JsmExtensionConfig.class.getModifiers()),
                    "JsmExtensionConfig must be final to prevent subclassing");
        }

        @Test
        @DisplayName("Should declare exactly one constructor")
        void shouldDeclareExactlyOneConstructor() {
            assertEquals(1, JsmExtensionConfig.class.getDeclaredConstructors().length);
        }

        @Test
        @DisplayName("Constructor should be private")
        void constructorShouldBePrivate() throws NoSuchMethodException {
            Constructor<JsmExtensionConfig> ctor =
                    JsmExtensionConfig.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(ctor.getModifiers()),
                    "No-arg constructor must be private");
        }

        @Test
        @DisplayName("Private constructor should be invocable via reflection (line coverage)")
        void privateConstructorShouldBeReachableViaReflection() throws Exception {
            Constructor<JsmExtensionConfig> ctor =
                    JsmExtensionConfig.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            JsmExtensionConfig instance = ctor.newInstance();
            assertNotNull(instance,
                    "Reflection-instantiated object should not be null");
        }

        @Test
        @DisplayName("Should have @AtlassianExtension annotation present at runtime")
        void shouldHaveAtlassianExtensionAnnotation() {
            assertNotNull(JsmExtensionConfig.class.getAnnotation(AtlassianExtension.class));
        }

        @Test
        @DisplayName("Should have @OAuthScope annotations present at runtime")
        void shouldHaveOAuthScopeAnnotations() {
            OAuthScope[] scopes =
                    JsmExtensionConfig.class.getAnnotationsByType(OAuthScope.class);
            assertTrue(scopes.length > 0,
                    "At least one @OAuthScope annotation should be present");
        }
    }

    // ── @AtlassianExtension tests ─────────────────────────────────────────────

    @Nested
    @DisplayName("@AtlassianExtension annotation")
    class AtlassianExtensionAnnotationTests {

        private final AtlassianExtension ext =
                JsmExtensionConfig.class.getAnnotation(AtlassianExtension.class);

        @Test
        @DisplayName("extensionType should be 'jira-service-management'")
        void extensionTypeShouldBeJiraServiceManagement() {
            assertEquals("jira-service-management", ext.extensionType());
        }

        @Test
        @DisplayName("product should be AtlassianProduct.JIRA_SERVICE_MANAGEMENT")
        void productShouldBeJiraServiceManagement() {
            assertEquals(AtlassianProduct.JIRA_SERVICE_MANAGEMENT, ext.product());
        }

        @Test
        @DisplayName("jaxrsId should match JiraConstants.JAXRS_ID")
        void jaxrsIdShouldMatchJiraConstants() {
            assertEquals(JiraConstants.JAXRS_ID, ext.jaxrsId());
        }

        @Test
        @DisplayName("jaxrsId should be 'jiraextn'")
        void jaxrsIdShouldBeJiraExtn() {
            assertEquals("jiraextn", ext.jaxrsId());
        }

        @Test
        @DisplayName("displayName should be 'Jira Service Management'")
        void displayNameShouldBeJiraServiceManagement() {
            assertEquals("Jira Service Management", ext.displayName());
        }
    }

    // ── @OAuthScope tests ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("@OAuthScope annotations")
    class OAuthScopeAnnotationTests {

        /** Uses {@code getAnnotationsByType} to handle the repeatable container transparently. */
        private OAuthScope[] scopes() {
            return JsmExtensionConfig.class.getAnnotationsByType(OAuthScope.class);
        }

        private OAuthScope findScope(String value) {
            return Arrays.stream(scopes())
                    .filter(s -> value.equals(s.value()))
                    .findFirst()
                    .orElse(null);
        }

        // ── Count ────────────────────────────────────────────────────────────

        @Test
        @DisplayName("Should declare exactly 8 OAuth scopes")
        void shouldDeclareEightScopes() {
            assertEquals(8, scopes().length);
        }

        @Test
        @DisplayName("All scope values should be unique")
        void allScopeValuesShouldBeUnique() {
            OAuthScope[] all = scopes();
            long unique = Arrays.stream(all).map(OAuthScope::value).distinct().count();
            assertEquals(all.length, unique, "Duplicate @OAuthScope values detected");
        }

        @Test
        @DisplayName("Every scope should have a non-blank description")
        void everyScopeShouldHaveNonBlankDescription() {
            for (OAuthScope scope : scopes()) {
                assertFalse(scope.description().isBlank(),
                        "Scope '" + scope.value() + "' has a blank description");
            }
        }

        @Test
        @DisplayName("Every scope should declare at least one catalog request")
        void everyScopeShouldDeclareAtLeastOneCatalogRequest() {
            for (OAuthScope scope : scopes()) {
                assertTrue(scope.catalogRequests().length > 0,
                        "Scope '" + scope.value() + "' declares no catalog requests");
            }
        }

        // ── read:jira-user ───────────────────────────────────────────────────

        @Test
        @DisplayName("Should contain 'read:jira-user' scope")
        void shouldContainReadJiraUserScope() {
            assertNotNull(findScope("read:jira-user"));
        }

        @Test
        @DisplayName("'read:jira-user' description should be 'Read Jira user information'")
        void readJiraUserDescriptionShouldBeCorrect() {
            assertEquals("Read Jira user information",
                    findScope("read:jira-user").description());
        }

        @Test
        @DisplayName("'read:jira-user' should declare 2 catalog requests")
        void readJiraUserShouldDeclaretwoCatalogRequests() {
            assertEquals(2, findScope("read:jira-user").catalogRequests().length);
        }

        @Test
        @DisplayName("'read:jira-user' catalog requests should match exactly")
        void readJiraUserCatalogRequestsShouldMatchExactly() {
            assertArrayEquals(
                    new String[]{"Get Users Account Id", "Get User Account Information"},
                    findScope("read:jira-user").catalogRequests()
            );
        }

        // ── read:jira-work ───────────────────────────────────────────────────

        @Test
        @DisplayName("Should contain 'read:jira-work' scope")
        void shouldContainReadJiraWorkScope() {
            assertNotNull(findScope("read:jira-work"));
        }

        @Test
        @DisplayName("'read:jira-work' description should be 'Read Jira issues and work items'")
        void readJiraWorkDescriptionShouldBeCorrect() {
            assertEquals("Read Jira issues and work items",
                    findScope("read:jira-work").description());
        }

        @Test
        @DisplayName("'read:jira-work' should declare 9 catalog requests")
        void readJiraWorkShouldDeclareNineCatalogRequests() {
            assertEquals(9, findScope("read:jira-work").catalogRequests().length);
        }

        @Test
        @DisplayName("'read:jira-work' catalog requests should contain 'Get Issue By Key'")
        void readJiraWorkShouldContainGetIssueByKey() {
            List<String> requests = Arrays.asList(findScope("read:jira-work").catalogRequests());
            assertTrue(requests.contains("Get Issue By Key"));
        }

        @Test
        @DisplayName("'read:jira-work' catalog requests should contain 'Fetch by JQL query'")
        void readJiraWorkShouldContainFetchByJqlQuery() {
            List<String> requests = Arrays.asList(findScope("read:jira-work").catalogRequests());
            assertTrue(requests.contains("Fetch by JQL query"));
        }

        @Test
        @DisplayName("'read:jira-work' catalog requests should contain 'Get Custom Field Value'")
        void readJiraWorkShouldContainGetCustomFieldValue() {
            List<String> requests = Arrays.asList(findScope("read:jira-work").catalogRequests());
            assertTrue(requests.contains("Get Custom Field Value"));
        }

        @Test
        @DisplayName("'read:jira-work' catalog requests should match exactly")
        void readJiraWorkCatalogRequestsShouldMatchExactly() {
            assertArrayEquals(
                    new String[]{
                            "Get Issue By Key", "Get issues", "Get user issues",
                            "Get my issues by given statuses", "Get user issue by issue type",
                            "Fetch by JQL query", "Get Results for Fetch Issues by JQL Query",
                            "Get all comments", "Get Custom Field Value"
                    },
                    findScope("read:jira-work").catalogRequests()
            );
        }

        // ── write:jira-work ──────────────────────────────────────────────────

        @Test
        @DisplayName("Should contain 'write:jira-work' scope")
        void shouldContainWriteJiraWorkScope() {
            assertNotNull(findScope("write:jira-work"));
        }

        @Test
        @DisplayName("'write:jira-work' description should be 'Create and update Jira issues'")
        void writeJiraWorkDescriptionShouldBeCorrect() {
            assertEquals("Create and update Jira issues",
                    findScope("write:jira-work").description());
        }

        @Test
        @DisplayName("'write:jira-work' should declare 11 catalog requests")
        void writeJiraWorkShouldDeclareElevenCatalogRequests() {
            assertEquals(11, findScope("write:jira-work").catalogRequests().length);
        }

        @Test
        @DisplayName("'write:jira-work' catalog requests should contain 'Create an issue'")
        void writeJiraWorkShouldContainCreateAnIssue() {
            List<String> requests = Arrays.asList(findScope("write:jira-work").catalogRequests());
            assertTrue(requests.contains("Create an issue"));
        }

        @Test
        @DisplayName("'write:jira-work' catalog requests should contain 'Attach File to Ticket'")
        void writeJiraWorkShouldContainAttachFile() {
            List<String> requests = Arrays.asList(findScope("write:jira-work").catalogRequests());
            assertTrue(requests.contains("Attach File to Ticket"));
        }

        @Test
        @DisplayName("'write:jira-work' catalog requests should match exactly")
        void writeJiraWorkCatalogRequestsShouldMatchExactly() {
            assertArrayEquals(
                    new String[]{
                            "Create an issue", "Report a bug", "Update an issue",
                            "Update status of issue", "Update status of multiple issues",
                            "Update priority of issue", "Update the priority of multiple issues",
                            "Assign a task to user", "Add a comment",
                            "Add worklog", "Attach File to Ticket"
                    },
                    findScope("write:jira-work").catalogRequests()
            );
        }

        // ── manage:jira-data-provider ────────────────────────────────────────

        @Test
        @DisplayName("Should contain 'manage:jira-data-provider' scope")
        void shouldContainManageJiraDataProviderScope() {
            assertNotNull(findScope("manage:jira-data-provider"));
        }

        @Test
        @DisplayName("'manage:jira-data-provider' description should be 'Manage webhooks and data providers'")
        void manageJiraDataProviderDescriptionShouldBeCorrect() {
            assertEquals("Manage webhooks and data providers",
                    findScope("manage:jira-data-provider").description());
        }

        @Test
        @DisplayName("'manage:jira-data-provider' catalog requests should match exactly")
        void manageJiraDataProviderCatalogRequestsShouldMatchExactly() {
            assertArrayEquals(
                    new String[]{"Get Notification"},
                    findScope("manage:jira-data-provider").catalogRequests()
            );
        }

        // ── manage:jira-project ──────────────────────────────────────────────

        @Test
        @DisplayName("Should contain 'manage:jira-project' scope")
        void shouldContainManageJiraProjectScope() {
            assertNotNull(findScope("manage:jira-project"));
        }

        @Test
        @DisplayName("'manage:jira-project' description should be 'Manage Jira projects, versions and releases'")
        void manageJiraProjectDescriptionShouldBeCorrect() {
            assertEquals("Manage Jira projects, versions and releases",
                    findScope("manage:jira-project").description());
        }

        @Test
        @DisplayName("'manage:jira-project' should declare 3 catalog requests")
        void manageJiraProjectShouldDeclareThreeCatalogRequests() {
            assertEquals(3, findScope("manage:jira-project").catalogRequests().length);
        }

        @Test
        @DisplayName("'manage:jira-project' catalog requests should match exactly")
        void manageJiraProjectCatalogRequestsShouldMatchExactly() {
            assertArrayEquals(
                    new String[]{"Get release summary", "Assign Issue to Epic", "Assign Child Issue to Epic"},
                    findScope("manage:jira-project").catalogRequests()
            );
        }

        // ── read:servicedesk-request ─────────────────────────────────────────

        @Test
        @DisplayName("Should contain 'read:servicedesk-request' scope")
        void shouldContainReadServicedeskRequestScope() {
            assertNotNull(findScope("read:servicedesk-request"));
        }

        @Test
        @DisplayName("'read:servicedesk-request' description should be 'Read service desk requests'")
        void readServicedeskRequestDescriptionShouldBeCorrect() {
            assertEquals("Read service desk requests",
                    findScope("read:servicedesk-request").description());
        }

        @Test
        @DisplayName("'read:servicedesk-request' catalog requests should match exactly")
        void readServicedeskRequestCatalogRequestsShouldMatchExactly() {
            assertArrayEquals(
                    new String[]{"Get Request Type Field Value"},
                    findScope("read:servicedesk-request").catalogRequests()
            );
        }

        // ── write:servicedesk-request ────────────────────────────────────────

        @Test
        @DisplayName("Should contain 'write:servicedesk-request' scope")
        void shouldContainWriteServicedeskRequestScope() {
            assertNotNull(findScope("write:servicedesk-request"));
        }

        @Test
        @DisplayName("'write:servicedesk-request' description should be 'Create and update service desk requests'")
        void writeServicedeskRequestDescriptionShouldBeCorrect() {
            assertEquals("Create and update service desk requests",
                    findScope("write:servicedesk-request").description());
        }

        @Test
        @DisplayName("'write:servicedesk-request' catalog requests should match exactly")
        void writeServicedeskRequestCatalogRequestsShouldMatchExactly() {
            assertArrayEquals(
                    new String[]{"Add Request Participants to Issue"},
                    findScope("write:servicedesk-request").catalogRequests()
            );
        }

        // ── manage:servicedesk-customer ──────────────────────────────────────

        @Test
        @DisplayName("Should contain 'manage:servicedesk-customer' scope")
        void shouldContainManageServicedeskCustomerScope() {
            assertNotNull(findScope("manage:servicedesk-customer"));
        }

        @Test
        @DisplayName("'manage:servicedesk-customer' description should be 'Manage service desk customers'")
        void manageServicedeskCustomerDescriptionShouldBeCorrect() {
            assertEquals("Manage service desk customers",
                    findScope("manage:servicedesk-customer").description());
        }

        @Test
        @DisplayName("'manage:servicedesk-customer' should declare 3 catalog requests")
        void manageServicedeskCustomerShouldDeclareThreeCatalogRequests() {
            assertEquals(3, findScope("manage:servicedesk-customer").catalogRequests().length);
        }

        @Test
        @DisplayName("'manage:servicedesk-customer' catalog requests should match exactly")
        void manageServicedeskCustomerCatalogRequestsShouldMatchExactly() {
            assertArrayEquals(
                    new String[]{"Create User", "Update User", "Delete User"},
                    findScope("manage:servicedesk-customer").catalogRequests()
            );
        }
    }
}
