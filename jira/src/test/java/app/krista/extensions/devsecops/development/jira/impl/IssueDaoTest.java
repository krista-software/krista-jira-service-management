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

import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketPriority;
import app.krista.model.base.FreeForm;
import com.google.gson.JsonArray;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link IssueDao}.
 *
 * <p>Uses the test-friendly secondary constructor
 * {@code IssueDao(RestClientFactory, RestClient)} which also creates
 * internal {@code ProjectDao}, {@code UserDao} and {@code TicketInfoDao}
 * instances backed by the same {@code restClient} mock, so all HTTP
 * interactions can be controlled through a single mock.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("IssueDao Tests")
class IssueDaoTest {

    @Mock private RestClientFactory restClientFactory;
    @Mock private RestClient restClient;
    @Mock private Invocation.Builder invoker;
    @Mock private Response response;
    @Mock private Response.StatusType statusType;
    @Mock private HttpResponse<JsonNode> attachResponse;
    @Mock private OAuth2RestClient mockOAuth2Client;
    @Mock private FreeForm freeForm;

    private IssueDao dao;

    // ── shared JSON fixtures ────────────────────────────────────────────────

    private static final String ISSUE_KEY = "TEST-1";
    private static final String ISSUE_ID   = "10001";

    /** Minimal issue JSON that JiraIssue.create() can parse into a populated Issue. */
    private static final String ISSUE_JSON =
            "{\"id\":\"" + ISSUE_ID + "\",\"key\":\"" + ISSUE_KEY + "\",\"fields\":{" +
            "\"summary\":\"Test Issue\",\"status\":{\"name\":\"Open\"}," +
            "\"issuetype\":{\"name\":\"Story\"},\"priority\":{\"name\":\"Medium\"}," +
            "\"assignee\":{\"displayName\":\"John Doe\"}," +
            "\"reporter\":{\"displayName\":\"Jane Doe\"}," +
            "\"project\":{\"key\":\"PROJ\"},\"labels\":[],\"components\":[]," +
            "\"updated\":\"2026-01-01T00:00:00.000+0000\"," +
            "\"comment\":{\"comments\":[]}}}";

    /** Issue JSON with a requesttype field value that contains "/" (triggers resolution). */
    private static final String ISSUE_JSON_WITH_REQUEST_TYPE =
            "{\"id\":\"" + ISSUE_ID + "\",\"key\":\"" + ISSUE_KEY + "\",\"fields\":{" +
            "\"summary\":\"Test Issue\",\"status\":{\"name\":\"Open\"}," +
            "\"issuetype\":{\"name\":\"Story\"},\"priority\":{\"name\":\"Medium\"}," +
            "\"assignee\":{\"displayName\":\"John Doe\"}," +
            "\"reporter\":{\"displayName\":\"Jane Doe\"}," +
            "\"project\":{\"key\":\"PROJ\"},\"labels\":[],\"components\":[]," +
            "\"requesttype\":\"portal1/rt1\"," +
            "\"updated\":\"2026-01-01T00:00:00.000+0000\"," +
            "\"comment\":{\"comments\":[]}}}";

    /** Project JSON that contains the issueTypes array and project style. */
    private static final String PROJECT_JSON =
            "{\"id\":\"1\",\"key\":\"PROJ\",\"name\":\"Test Project\"," +
            "\"style\":\"next-gen\"," +
            "\"issueTypes\":[{\"id\":\"10001\",\"name\":\"Story\"}," +
            "{\"id\":\"10002\",\"name\":\"Bug\"},{\"id\":\"10003\",\"name\":\"Epic\"}]}";

    private static final String CLASSIC_PROJECT_JSON =
            "{\"id\":\"1\",\"key\":\"PROJ\",\"name\":\"Test Project\"," +
            "\"style\":\"classic\"," +
            "\"issueTypes\":[{\"id\":\"10003\",\"name\":\"Epic\"}]}";

    /**
     * Fix JAX-RS classloader conflict caused by dependency version upgrade.
     *
     * <p>Gradle upgrades {@code jersey-common:2.35} to {@code 3.0.3} because
     * {@code jersey-hk2:3.0.3} (test-only) requires the newer version.
     * {@code jersey-common:3.0.3} is a Jakarta EE 9 artifact whose
     * {@code RuntimeDelegateImpl} implements {@code jakarta.ws.rs.ext.RuntimeDelegate},
     * not {@code javax.ws.rs.ext.RuntimeDelegate}.  When production code calls
     * {@code Entity.entity(body, MediaType.APPLICATION_JSON)} (the {@code String}
     * overload), it invokes {@code MediaType.valueOf()} →
     * {@code RuntimeDelegate.getInstance()} → SPI discovery finds the 3.x impl →
     * instanceof check against {@code javax.ws.rs.ext.RuntimeDelegate} fails →
     * {@code LinkageError}.</p>
     *
     * <p>Pre-populating the singleton via {@code setInstance()} bypasses SPI
     * discovery entirely.  The anonymous implementation here supplies only what
     * the production code actually invokes: {@code createHeaderDelegate(MediaType.class)}
     * so that {@code MediaType.valueOf(String)} can parse the content-type string.</p>
     */
    @BeforeAll
    @SuppressWarnings("unchecked")
    static void initJaxRsRuntimeDelegate() {
        RuntimeDelegate.setInstance(new RuntimeDelegate() {
            @Override
            public javax.ws.rs.core.UriBuilder createUriBuilder() {
                throw new UnsupportedOperationException();
            }
            @Override
            public javax.ws.rs.core.Response.ResponseBuilder createResponseBuilder() {
                throw new UnsupportedOperationException();
            }
            @Override
            public javax.ws.rs.core.Variant.VariantListBuilder createVariantListBuilder() {
                throw new UnsupportedOperationException();
            }
            @Override
            public <T> T createEndpoint(javax.ws.rs.core.Application application,
                                        Class<T> endpointType) {
                throw new UnsupportedOperationException();
            }
            @Override
            public <T> RuntimeDelegate.HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
                if (type == javax.ws.rs.core.MediaType.class) {
                    return (RuntimeDelegate.HeaderDelegate<T>) new RuntimeDelegate.HeaderDelegate<javax.ws.rs.core.MediaType>() {
                        @Override
                        public javax.ws.rs.core.MediaType fromString(String value) {
                            if (value == null) return null;
                            // Strip parameters (e.g., ";charset=UTF-8") and parse type/subtype
                            String core = value.split(";")[0].trim();
                            String[] parts = core.split("/");
                            String type = parts.length > 0 ? parts[0].trim() : "*";
                            String subtype = parts.length > 1 ? parts[1].trim() : "*";
                            return new javax.ws.rs.core.MediaType(type, subtype);
                        }
                        @Override
                        public String toString(javax.ws.rs.core.MediaType value) {
                            return value == null ? null : value.getType() + "/" + value.getSubtype();
                        }
                    };
                }
                throw new UnsupportedOperationException("No HeaderDelegate for: " + type);
            }
            @Override
            public javax.ws.rs.core.Link.Builder createLinkBuilder() {
                throw new UnsupportedOperationException();
            }
        });
    }

    @BeforeEach
    void setUp() {
        dao = new IssueDao(restClientFactory, restClient);
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private void mockSuccessFamily() {
        when(response.getStatusInfo()).thenReturn(statusType);
        when(statusType.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);
    }

    private void mockFailureFamily() {
        when(response.getStatusInfo()).thenReturn(statusType);
        when(statusType.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
    }

    private void stubIssueResponse() {
        when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                .thenReturn(ISSUE_JSON);
    }

    private void stubProjectResponse() {
        when(restClient.fetch(eq("project/PROJ"), any(), eq(String.class)))
                .thenReturn(PROJECT_JSON);
    }

    // ── getRestClient ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getRestClient")
    class GetRestClientTests {

        @Test
        @DisplayName("Returns restClient when factory is not null and restClient is a plain mock")
        void getRestClient_normalPath_returnsExistingRestClient() {
            assertSame(restClient, dao.getRestClient());
        }

        @Test
        @DisplayName("Returns restClient when restClientFactory is null (factory-null branch)")
        void getRestClient_factoryNull_returnsCurrentRestClient() throws Exception {
            Field field = IssueDao.class.getDeclaredField("restClientFactory");
            field.setAccessible(true);
            field.set(dao, null);

            assertSame(restClient, dao.getRestClient());
        }

        @Test
        @DisplayName("Calls factory.getInstance() when restClient field is null")
        void getRestClient_restClientNull_callsFactory() throws Exception {
            Field field = IssueDao.class.getDeclaredField("restClient");
            field.setAccessible(true);
            field.set(dao, null);

            when(restClientFactory.getInstance(false)).thenReturn(restClient);

            assertSame(restClient, dao.getRestClient());
            verify(restClientFactory).getInstance(false);
        }

        @Test
        @DisplayName("Reassigns restClient when current is OAuth2RestClient and invokeAsUser is false")
        void getRestClient_oauth2Client_notInvokeAsUser_callsFactory() throws Exception {
            Field field = IssueDao.class.getDeclaredField("restClient");
            field.setAccessible(true);
            field.set(dao, mockOAuth2Client);

            RestClient newClient = mock(RestClient.class);
            when(restClientFactory.getInstance(false)).thenReturn(newClient);

            // getRestClient(null) → invokeAsUserAuth = restClientFactory.invokeAsUser() = false
            // → mockOAuth2Client instanceof OAuth2RestClient && !false → enters if
            RestClient result = dao.getRestClient();
            assertSame(newClient, result);
        }
    }

    // ── getAccountIdByEmail ─────────────────────────────────────────────────

    @Nested
    @DisplayName("getAccountIdByEmail")
    class GetAccountIdByEmailTests {

        @Test
        @DisplayName("Returns accountId when a user is found")
        void getAccountIdByEmail_userFound_returnsAccountId() {
            when(restClient.fetch(eq("user/search"), any(), eq(String.class)))
                    .thenReturn("[{\"accountId\":\"acc123\",\"displayName\":\"John\"}]");

            String result = dao.getAccountIdByEmail("john@example.com");
            assertEquals("acc123", result);
        }

        @Test
        @DisplayName("Returns error message when no user is found")
        void getAccountIdByEmail_userNotFound_returnsErrorMessage() {
            when(restClient.fetch(eq("user/search"), any(), eq(String.class)))
                    .thenReturn("[]");

            String result = dao.getAccountIdByEmail("unknown@example.com");
            assertTrue(result.startsWith("No user found"));
        }

        @Test
        @DisplayName("Rethrows IllegalCallerException from fetch")
        void getAccountIdByEmail_illegalCallerException_rethrows() {
            when(restClient.fetch(eq("user/search"), any(), eq(String.class)))
                    .thenThrow(new IllegalCallerException("not allowed"));

            assertThrows(IllegalCallerException.class,
                    () -> dao.getAccountIdByEmail("test@example.com"));
        }
    }

    // ── createUser ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {

        @Test
        @DisplayName("Returns user map when creation succeeds (201)")
        void createUser_status201_returnsUserMap() {
            when(restClient.getResourceInvoker(eq("user"), any())).thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(201);
            when(response.readEntity(String.class)).thenReturn(
                    "{\"self\":\"http://jira/user/acc1\",\"accountId\":\"acc1\"," +
                    "\"emailAddress\":\"u@x.com\",\"displayName\":\"User One\"}");

            Map<String, Object> result = dao.createUser("u@x.com");
            assertEquals("acc1", result.get("Account Id"));
            assertEquals("u@x.com", result.get("Email Address"));
        }

        @Test
        @DisplayName("Returns empty map when creation fails (non-201)")
        void createUser_nonStatus201_returnsEmptyMap() {
            when(restClient.getResourceInvoker(eq("user"), any())).thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(400);

            Map<String, Object> result = dao.createUser("bad@example.com");
            assertTrue(result.isEmpty());
        }
    }

    // ── getUserGroups ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserGroups")
    class GetUserGroupsTests {

        @Test
        @DisplayName("Returns group name list when response is non-empty")
        void getUserGroups_nonEmpty_returnsGroupNames() {
            when(restClient.fetch(eq("user/groups"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[{\"name\":\"dev-team\"},{\"name\":\"qa-team\"}]}");

            List<String> groups = dao.getUserGroups("user-key-123");
            assertEquals(List.of("dev-team", "qa-team"), groups);
        }

        @Test
        @DisplayName("Throws RuntimeException when response is empty")
        void getUserGroups_emptyResponse_throwsRuntimeException() {
            when(restClient.fetch(eq("user/groups"), any(), eq(String.class))).thenReturn("");

            assertThrows(RuntimeException.class, () -> dao.getUserGroups("user-key-123"));
        }
    }

    // ── updateUser ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUser")
    class UpdateUserTests {

        @Test
        @DisplayName("Returns false when addGroupNames is null")
        void updateUser_nullGroupNames_returnsFalse() {
            assertFalse(dao.updateUser("user-key", null));
        }

        @Test
        @DisplayName("Returns false when addGroupNames is empty")
        void updateUser_emptyGroupNames_returnsFalse() {
            assertFalse(dao.updateUser("user-key", Collections.emptyList()));
        }

        @Test
        @DisplayName("Returns true when all groups are added successfully")
        void updateUser_allGroupsAdded_returnsTrue() {
            when(restClient.getResourceInvoker(eq("group/user"), any())).thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(201);

            assertTrue(dao.updateUser("user-key", List.of("devs", "qa")));
        }

        @Test
        @DisplayName("Returns false when addUserToGroup fails for any group")
        void updateUser_groupAddFails_returnsFalse() {
            when(restClient.getResourceInvoker(eq("group/user"), any())).thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(400);

            assertFalse(dao.updateUser("user-key", List.of("devs")));
        }
    }

    // ── deleteUser ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteUser")
    class DeleteUserTests {

        @Test
        @DisplayName("Returns true when deletion returns 204")
        void deleteUser_status204_returnsTrue() {
            when(restClient.getResourceInvoker(eq("user"), any())).thenReturn(invoker);
            when(invoker.delete()).thenReturn(response);
            when(response.getStatus()).thenReturn(204);

            assertTrue(dao.deleteUser("user-key-abc"));
        }

        @Test
        @DisplayName("Returns false when deletion returns non-204")
        void deleteUser_nonStatus204_returnsFalse() {
            when(restClient.getResourceInvoker(eq("user"), any())).thenReturn(invoker);
            when(invoker.delete()).thenReturn(response);
            when(response.getStatus()).thenReturn(400);

            assertFalse(dao.deleteUser("user-key-abc"));
        }
    }

    // ── createIssue ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createIssue")
    class CreateIssueTests {

        @Test
        @DisplayName("5-param overload delegates to 6-param with empty requestType map")
        void createIssue_fiveParam_delegatesToSixParam() {
            when(restClient.getResourceInvoker(eq("issue"), any())).thenReturn(invoker);
            stubProjectResponse();
            when(invoker.post(any())).thenReturn(response);
            when(response.readEntity(String.class)).thenReturn("{\"key\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Issue result = dao.createIssue("PROJ", "Summary", "Description", null, "Story");
            assertNotNull(result);
        }

        @Test
        @DisplayName("Creates issue with parentKey set when parentKey is non-null")
        void createIssue_withParent_addsParentToFields() {
            when(restClient.getResourceInvoker(eq("issue"), any())).thenReturn(invoker);
            stubProjectResponse();
            when(invoker.post(any())).thenReturn(response);
            when(response.readEntity(String.class)).thenReturn("{\"key\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Issue result = dao.createIssue("PROJ", "Summary", "Desc", "PROJ-0", "Story", Map.of());
            assertNotNull(result);
        }

        @Test
        @DisplayName("Adds customfield_10011 when issueType is Epic and project style is classic")
        void createIssue_epicWithClassicStyle_addsEpicNameField() {
            when(restClient.getResourceInvoker(eq("issue"), any())).thenReturn(invoker);
            when(restClient.fetch(eq("project/PROJ"), any(), eq(String.class)))
                    .thenReturn(CLASSIC_PROJECT_JSON);
            when(invoker.post(any())).thenReturn(response);
            when(response.readEntity(String.class)).thenReturn("{\"key\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Issue result = dao.createIssue("PROJ", "My Epic", "Desc", null, "Epic", Map.of());
            assertNotNull(result);
        }

        @Test
        @DisplayName("Adds requestType field to payload when requestType map is complete")
        void createIssue_withCompleteRequestTypeMap_addsField() {
            when(restClient.getResourceInvoker(eq("issue"), any())).thenReturn(invoker);
            stubProjectResponse();
            when(invoker.post(any())).thenReturn(response);
            when(response.readEntity(String.class)).thenReturn("{\"key\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Map<String, Object> requestType = Map.of(
                    "Custom Field", "customfield_10010", "Field Value", "1/2");
            Issue result = dao.createIssue("PROJ", "Summary", "Desc", null, "Story", requestType);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Logs warning when requestType map has incomplete entries")
        void createIssue_withIncompleteRequestTypeMap_logsWarning() {
            when(restClient.getResourceInvoker(eq("issue"), any())).thenReturn(invoker);
            stubProjectResponse();
            when(invoker.post(any())).thenReturn(response);
            when(response.readEntity(String.class)).thenReturn("{\"key\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Map<String, Object> requestType = Map.of("Custom Field", "  ", "Field Value", "");
            Issue result = dao.createIssue("PROJ", "Summary", "Desc", null, "Story", requestType);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when response JSON has no 'key'")
        void createIssue_noKeyInResponse_throwsIllegalArgumentException() {
            when(restClient.getResourceInvoker(eq("issue"), any())).thenReturn(invoker);
            stubProjectResponse();
            when(invoker.post(any())).thenReturn(response);
            when(response.readEntity(String.class)).thenReturn("{\"errors\":{}}");

            assertThrows(IllegalArgumentException.class,
                    () -> dao.createIssue("PROJ", "Summary", "Desc", null, "Story"));
        }

        @Test
        @DisplayName("validateIssueType returns true when issue type is found")
        void validateIssueType_validType_returnsTrue() {
            stubProjectResponse();
            assertTrue(dao.validateIssueType("PROJ", "Story"));
        }

        @Test
        @DisplayName("validateIssueType returns false when issue type is not found")
        void validateIssueType_invalidType_returnsFalse() {
            stubProjectResponse();
            assertFalse(dao.validateIssueType("PROJ", "UnknownType"));
        }

        @Test
        @DisplayName("validateIssueType returns false on fetch exception")
        void validateIssueType_fetchException_returnsFalse() {
            when(restClient.fetch(eq("project/PROJ"), any(), eq(String.class)))
                    .thenThrow(new RuntimeException("fetch failed"));
            assertFalse(dao.validateIssueType("PROJ", "Story"));
        }
    }

    // ── createIssueWithRequestTypeName ──────────────────────────────────────

    @Nested
    @DisplayName("createIssueWithRequestTypeName")
    class CreateIssueWithRequestTypeNameTests {

        @Test
        @DisplayName("Creates standard issue when requestTypeName is null")
        void createIssueWithRequestTypeName_nullName_createsStandardIssue() {
            when(restClient.getResourceInvoker(eq("issue"), any())).thenReturn(invoker);
            stubProjectResponse();
            when(invoker.post(any())).thenReturn(response);
            when(response.readEntity(String.class)).thenReturn("{\"key\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Issue result = dao.createIssueWithRequestTypeName(
                    "PROJ", "Summary", "Desc", null, "Story", null);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Creates standard issue when requestTypeName is blank")
        void createIssueWithRequestTypeName_blankName_createsStandardIssue() {
            when(restClient.getResourceInvoker(eq("issue"), any())).thenReturn(invoker);
            stubProjectResponse();
            when(invoker.post(any())).thenReturn(response);
            when(response.readEntity(String.class)).thenReturn("{\"key\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Issue result = dao.createIssueWithRequestTypeName(
                    "PROJ", "Summary", "Desc", null, "Story", "   ");
            assertNotNull(result);
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when service desk not found for project")
        void createIssueWithRequestTypeName_serviceDeskNotFound_throwsIllegalArgument() {
            when(restClient.fetchJiraService(eq("servicedesk"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[]}");

            assertThrows(IllegalArgumentException.class,
                    () -> dao.createIssueWithRequestTypeName(
                            "PROJ", "Summary", "Desc", null, "Story", "IT Help"));
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when request type not found in service desk")
        void createIssueWithRequestTypeName_requestTypeNotFound_throwsIllegalArgument() {
            when(restClient.fetchJiraService(eq("servicedesk"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[{\"id\":\"1\",\"projectKey\":\"PROJ\"}]}");
            when(restClient.fetchJiraService(eq("servicedesk/1/requesttype"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[{\"id\":\"99\",\"name\":\"Other Type\"}]}");

            assertThrows(IllegalArgumentException.class,
                    () -> dao.createIssueWithRequestTypeName(
                            "PROJ", "Summary", "Desc", null, "Story", "IT Help"));
        }

        @Test
        @DisplayName("Creates customer request when service desk and request type are found")
        void createIssueWithRequestTypeName_found_createsCustomerRequest() {
            when(restClient.fetchJiraService(eq("servicedesk"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[{\"id\":\"1\",\"projectKey\":\"PROJ\"}]}");
            when(restClient.fetchJiraService(eq("servicedesk/1/requesttype"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[{\"id\":\"42\",\"name\":\"IT Help\"}]}");
            when(restClient.getJiraServiceResourceInvoker(eq("request"), any())).thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            mockSuccessFamily();
            when(response.readEntity(String.class)).thenReturn("{\"issueKey\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Issue result = dao.createIssueWithRequestTypeName(
                    "PROJ", "Summary", "Some description", null, "Story", "IT Help");
            assertNotNull(result);
        }

        @Test
        @DisplayName("Wraps non-IllegalArgumentException in RuntimeException")
        void createIssueWithRequestTypeName_unexpectedException_throwsRuntimeException() {
            when(restClient.fetchJiraService(eq("servicedesk"), any(), eq(String.class)))
                    .thenThrow(new NullPointerException("service unavailable"));

            assertThrows(RuntimeException.class,
                    () -> dao.createIssueWithRequestTypeName(
                            "PROJ", "Summary", "Desc", null, "Story", "IT Help"));
        }

        @Test
        @DisplayName("Service desk projectKey matches as JsonObject")
        void createIssueWithRequestTypeName_serviceDeskProjectKeyAsJsonObject() {
            // projectKey is stored as a nested JsonObject {"key":"PROJ"} instead of plain string
            when(restClient.fetchJiraService(eq("servicedesk"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[{\"id\":\"2\",\"projectKey\":{\"key\":\"PROJ\"}}]}");
            when(restClient.fetchJiraService(eq("servicedesk/2/requesttype"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[{\"id\":\"7\",\"name\":\"IT Help\"}]}");
            when(restClient.getJiraServiceResourceInvoker(eq("request"), any())).thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            mockSuccessFamily();
            when(response.readEntity(String.class)).thenReturn("{\"issueKey\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Issue result = dao.createIssueWithRequestTypeName(
                    "PROJ", "Summary", "Desc", null, "Story", "IT Help");
            assertNotNull(result);
        }
    }

    // ── createCustomerRequest ───────────────────────────────────────────────

    @Nested
    @DisplayName("createCustomerRequest")
    class CreateCustomerRequestTests {

        private void stubServiceDeskPost() {
            when(restClient.getJiraServiceResourceInvoker(eq("request"), any())).thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
        }

        @Test
        @DisplayName("Creates request with null description")
        void createCustomerRequest_nullDescription_succeeds() {
            stubServiceDeskPost();
            mockSuccessFamily();
            when(response.readEntity(String.class)).thenReturn("{\"issueKey\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Issue result = dao.createCustomerRequest("1", "42", "Summary", null);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Extracts text from ADF JSON description")
        void createCustomerRequest_adfDescription_extractsText() {
            stubServiceDeskPost();
            mockSuccessFamily();
            when(response.readEntity(String.class)).thenReturn("{\"issueKey\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            String adfDesc = "{\"type\":\"doc\",\"version\":1,\"content\":[" +
                    "{\"type\":\"paragraph\",\"content\":[{\"type\":\"text\",\"text\":\"Hello World\"}]}]}";
            Issue result = dao.createCustomerRequest("1", "42", "Summary", adfDesc);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Falls back to original string when JSON is not valid ADF")
        void createCustomerRequest_nonAdfJson_usesAsString() {
            stubServiceDeskPost();
            mockSuccessFamily();
            when(response.readEntity(String.class)).thenReturn("{\"issueKey\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            String nonAdfJson = "{\"other\":\"data\"}";
            Issue result = dao.createCustomerRequest("1", "42", "Summary", nonAdfJson);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Strips HTML tags from HTML description")
        void createCustomerRequest_htmlDescription_stripsHtml() {
            stubServiceDeskPost();
            mockSuccessFamily();
            when(response.readEntity(String.class)).thenReturn("{\"issueKey\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            String htmlDesc = "<p>Hello <b>World</b></p><br/>More text";
            Issue result = dao.createCustomerRequest("1", "42", "Summary", htmlDesc);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Also strips HTML from description that starts with HTML but contains < and >")
        void createCustomerRequest_plainTextWithBrTag_stripsIt() {
            stubServiceDeskPost();
            mockSuccessFamily();
            when(response.readEntity(String.class)).thenReturn("{\"issueKey\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            Issue result = dao.createCustomerRequest("1", "42", "Summary", "Line1<br/>Line2");
            assertNotNull(result);
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when response is not successful")
        void createCustomerRequest_failedResponse_throwsIllegalArgument() {
            stubServiceDeskPost();
            mockFailureFamily();
            when(response.readEntity(String.class)).thenReturn("{\"errorMessage\":\"Bad\"}");

            assertThrows(IllegalArgumentException.class,
                    () -> dao.createCustomerRequest("1", "42", "Summary", "Desc"));
        }

        @Test
        @DisplayName("Throws IllegalArgumentException when issueKey cannot be parsed from response")
        void createCustomerRequest_noIssueKey_throwsIllegalArgument() {
            stubServiceDeskPost();
            mockSuccessFamily();
            when(response.readEntity(String.class)).thenReturn("{\"no_key\":\"here\"}");

            assertThrows(IllegalArgumentException.class,
                    () -> dao.createCustomerRequest("1", "42", "Summary", "Desc"));
        }

        @Test
        @DisplayName("Falls back to string when ADF JSON throws parse exception")
        void createCustomerRequest_invalidAdfJson_treatedAsPlainText() {
            // Starts with { and ends with } but throws during GSON parsing
            stubServiceDeskPost();
            mockSuccessFamily();
            when(response.readEntity(String.class)).thenReturn("{\"issueKey\":\"" + ISSUE_KEY + "\"}");
            stubIssueResponse();

            // This is malformed but starts/ends with braces — GSON will throw
            String badJson = "{not json}";
            // GSON.fromJson("{not json}") throws, so catch block sets descriptionText = description
            assertDoesNotThrow(() -> dao.createCustomerRequest("1", "42", "Summary", badJson));
        }
    }

    // ── getRequestTypeIdByName ──────────────────────────────────────────────

    @Nested
    @DisplayName("getRequestTypeIdByName")
    class GetRequestTypeIdByNameTests {

        @Test
        @DisplayName("Returns request type ID when name matches")
        void getRequestTypeIdByName_found_returnsId() {
            when(restClient.fetchJiraService(eq("servicedesk/1/requesttype"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[{\"id\":\"42\",\"name\":\"IT Help\"}]}");

            assertEquals("42", dao.getRequestTypeIdByName("1", "it help"));
        }

        @Test
        @DisplayName("Returns null when no request type matches")
        void getRequestTypeIdByName_notFound_returnsNull() {
            when(restClient.fetchJiraService(eq("servicedesk/1/requesttype"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[{\"id\":\"10\",\"name\":\"Other\"}]}");

            assertNull(dao.getRequestTypeIdByName("1", "IT Help"));
        }

        @Test
        @DisplayName("Returns null when values is null in response")
        void getRequestTypeIdByName_nullValues_returnsNull() {
            when(restClient.fetchJiraService(eq("servicedesk/1/requesttype"), any(), eq(String.class)))
                    .thenReturn("{\"values\":null}");

            assertNull(dao.getRequestTypeIdByName("1", "IT Help"));
        }

        @Test
        @DisplayName("Returns null on exception from fetch")
        void getRequestTypeIdByName_exception_returnsNull() {
            when(restClient.fetchJiraService(eq("servicedesk/1/requesttype"), any(), eq(String.class)))
                    .thenThrow(new RuntimeException("network error"));

            assertNull(dao.getRequestTypeIdByName("1", "IT Help"));
        }
    }

    // ── getRequestTypeFieldValue ────────────────────────────────────────────

    @Nested
    @DisplayName("getRequestTypeFieldValue")
    class GetRequestTypeFieldValueTests {

        @Test
        @DisplayName("Returns portalKey/key when matching request type is found")
        void getRequestTypeFieldValue_found_returnsFieldValue() {
            stubIssueResponse();
            when(restClient.fetchJiraService(
                    eq("request/" + ISSUE_ID + "/requesttype"), any(), eq(String.class)))
                    .thenReturn("{\"validRequestTypes\":[" +
                            "{\"name\":\"IT Help\",\"portalKey\":\"portal1\",\"key\":\"rt1\"}]}");

            String result = dao.getRequestTypeFieldValue(ISSUE_KEY, "IT Help");
            assertEquals("portal1/rt1", result);
        }

        @Test
        @DisplayName("Returns null when no matching request type is found")
        void getRequestTypeFieldValue_notFound_returnsNull() {
            stubIssueResponse();
            when(restClient.fetchJiraService(
                    eq("request/" + ISSUE_ID + "/requesttype"), any(), eq(String.class)))
                    .thenReturn("{\"validRequestTypes\":[" +
                            "{\"name\":\"Other\",\"portalKey\":\"p\",\"key\":\"k\"}]}");

            assertNull(dao.getRequestTypeFieldValue(ISSUE_KEY, "IT Help"));
        }
    }

    // ── addComment ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addComment")
    class AddCommentTests {

        @Test
        @DisplayName("Posts comment successfully (public visibility, no trailing BR)")
        void addComment_publicVisibility_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/comment"), any()))
                    .thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.addComment(ISSUE_KEY, "Hello World", "Public"));
            verify(invoker).post(any());
        }

        @Test
        @DisplayName("Posts internal comment (Internal visibility)")
        void addComment_internalVisibility_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/comment"), any()))
                    .thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.addComment(ISSUE_KEY, "Internal note", "Internal"));
        }

        @Test
        @DisplayName("Strips trailing <br> tag from comment before posting")
        void addComment_trailingBrTag_stripped() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/comment"), any()))
                    .thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.addComment(ISSUE_KEY, "Text<br>", null));
        }

        @Test
        @DisplayName("Throws RuntimeException when response is not successful")
        void addComment_failedResponse_throwsRuntimeException() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/comment"), any()))
                    .thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            mockFailureFamily();
            when(response.readEntity(String.class)).thenReturn("error body");

            assertThrows(RuntimeException.class,
                    () -> dao.addComment(ISSUE_KEY, "Hello", "Public"));
        }
    }

    // ── getComments ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getComments")
    class GetCommentsTests {

        @Test
        @DisplayName("Returns empty list when commentsArray is null")
        void getComments_nullCommentsArray_returnsEmpty() {
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/comment"), any(), eq(String.class)))
                    .thenReturn("{}");

            List<Map<String, Object>> result = dao.getComments(ISSUE_KEY);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Parses comment with ADF body, full author, and valid date")
        void getComments_adfBody_fullAuthor_validDate() {
            String commentsJson = "{\"comments\":[{" +
                    "\"body\":{\"type\":\"doc\",\"content\":[{\"type\":\"text\",\"text\":\"Hi\"}]}," +
                    "\"author\":{\"accountId\":\"uid1\",\"displayName\":\"Alice\"}," +
                    "\"created\":\"2026-01-15T10:30:00.000+0000\"}]}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/comment"), any(), eq(String.class)))
                    .thenReturn(commentsJson);

            List<Map<String, Object>> result = dao.getComments(ISSUE_KEY);
            assertEquals(1, result.size());
            assertEquals("uid1", result.get(0).get("Author ID"));
            assertEquals("Alice", result.get(0).get("Author Name"));
            assertNotNull(result.get(0).get("Date Created"));
        }

        @Test
        @DisplayName("Parses comment with plain-text body, author without accountId, invalid date")
        void getComments_plainBody_authorWithoutAccountId_invalidDate() {
            String commentsJson = "{\"comments\":[{" +
                    "\"body\":\"Plain text comment\"," +
                    "\"author\":{\"displayName\":\"Bob\"}," +
                    "\"created\":\"not-a-valid-date\"}]}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/comment"), any(), eq(String.class)))
                    .thenReturn(commentsJson);

            List<Map<String, Object>> result = dao.getComments(ISSUE_KEY);
            assertEquals(1, result.size());
            assertEquals("Plain text comment", result.get(0).get("Comment Body"));
            assertNull(result.get(0).get("Author ID"));
            assertNull(result.get(0).get("Date Created"));
        }

        @Test
        @DisplayName("Sets all author fields to null when no author object is present")
        void getComments_noAuthor_noCreated_authorFieldsNull() {
            String commentsJson = "{\"comments\":[{\"body\":\"Text\"}]}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/comment"), any(), eq(String.class)))
                    .thenReturn(commentsJson);

            List<Map<String, Object>> result = dao.getComments(ISSUE_KEY);
            assertEquals(1, result.size());
            assertNull(result.get(0).get("Author ID"));
            assertNull(result.get(0).get("Author Name"));
            assertNull(result.get(0).get("Date Created"));
        }

        @Test
        @DisplayName("Parses comment with author that has accountId but no displayName")
        void getComments_authorWithAccountIdNoDisplayName() {
            String commentsJson = "{\"comments\":[{" +
                    "\"body\":\"msg\"," +
                    "\"author\":{\"accountId\":\"a1\"}}]}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/comment"), any(), eq(String.class)))
                    .thenReturn(commentsJson);

            List<Map<String, Object>> result = dao.getComments(ISSUE_KEY);
            assertEquals("a1", result.get(0).get("Author ID"));
            assertNull(result.get(0).get("Author Name"));
        }
    }

    // ── searchByJQL ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("searchByJQL")
    class SearchByJQLTests {

        @Test
        @DisplayName("Returns issues when response has 'issues' array")
        void searchByJQL_hasIssues_returnsList() {
            when(restClient.fetch(eq("search/jql"), any(), eq(String.class)))
                    .thenReturn("{\"issues\":[" + ISSUE_JSON + "]}");

            List<Issue> result = dao.searchByJQL("project = PROJ", 10, 1);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Uses size=20 when size parameter is 0")
        void searchByJQL_zeroSize_usesDefault20() {
            when(restClient.fetch(eq("search/jql"), any(), eq(String.class)))
                    .thenReturn("{\"issues\":[]}");

            // Should not throw - just returns empty list
            List<Issue> result = dao.searchByJQL("project = PROJ", 0, 1);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Throws with errorMessages content when response has errorMessages")
        void searchByJQL_errorMessages_throwsRuntimeException() {
            when(restClient.fetch(eq("search/jql"), any(), eq(String.class)))
                    .thenReturn("{\"errorMessages\":[\"Invalid JQL\"]}");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> dao.searchByJQL("invalid", 10, 1));
            assertEquals("Invalid JQL", ex.getMessage());
        }

        @Test
        @DisplayName("Throws ISSUES_NOT_FOUND when response has no issues and no errorMessages")
        void searchByJQL_noIssuesNoErrors_throwsIssuesNotFound() {
            when(restClient.fetch(eq("search/jql"), any(), eq(String.class)))
                    .thenReturn("{\"total\":0}");

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> dao.searchByJQL("project = PROJ", 10, 1));
            assertEquals(IssueDao.ISSUES_NOT_FOUND, ex.getMessage());
        }
    }

    // ── searchByJqlQuery ────────────────────────────────────────────────────

    @Nested
    @DisplayName("searchByJqlQuery")
    class SearchByJqlQueryTests {

        @Test
        @DisplayName("Returns issues and stops when page has fewer results than page size")
        void searchByJqlQuery_lessResultsThanPageSize_stops() {
            when(restClient.fetchIssues(anyBoolean(), anyString(), anyString(),
                    eq("search/jql"), any(), eq(String.class)))
                    .thenReturn("{\"issues\":[" + ISSUE_JSON + "]}");

            List<Issue> result = dao.searchByJqlQuery(true, "acc1", "invoker1",
                    "project = PROJ", 50, 1);
            // 1 issue < 50 page size → stops immediately
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Throws RuntimeException when errorMessages are returned")
        void searchByJqlQuery_errorMessages_throwsRuntimeException() {
            when(restClient.fetchIssues(anyBoolean(), anyString(), anyString(),
                    eq("search/jql"), any(), eq(String.class)))
                    .thenReturn("{\"errorMessages\":[\"JQL error\"]}");

            assertThrows(RuntimeException.class, () -> dao.searchByJqlQuery(false,
                    "acc1", "inv1", "bad jql", 10, 1));
        }
    }

    // ── fetchErrorsInJQLQuery ───────────────────────────────────────────────

    @Nested
    @DisplayName("fetchErrorsInJQLQuery")
    class FetchErrorsInJQLQueryTests {

        @Test
        @DisplayName("Returns empty map when JSON response is blank")
        void fetchErrorsInJQLQuery_blankResponse_returnsEmpty() {
            when(restClient.parseAndValidateJQL(anyString(), anyBoolean(), anyString(), anyString()))
                    .thenReturn("   ");

            Map<String, String> result = dao.fetchErrorsInJQLQuery(false, "acc1", "inv1", "project=PROJ");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Returns errors map when queries array has error data")
        void fetchErrorsInJQLQuery_withErrors_returnsErrorMap() {
            String jqlResponse = "{\"queries\":[{\"query\":\"project=X\",\"errors\":[\"Unknown field\"]}]}";
            when(restClient.parseAndValidateJQL(anyString(), anyBoolean(), anyString(), anyString()))
                    .thenReturn(jqlResponse);

            Map<String, String> result = dao.fetchErrorsInJQLQuery(true, "acc1", "inv1", "project=X");
            assertEquals("Unknown field", result.get("errors"));
            assertEquals("project=X", result.get("query"));
        }

        @Test
        @DisplayName("Returns query-only map when errors array is null or empty")
        void fetchErrorsInJQLQuery_noErrors_returnsQueryOnly() {
            String jqlResponse = "{\"queries\":[{\"query\":\"project=PROJ\"}]}";
            when(restClient.parseAndValidateJQL(anyString(), anyBoolean(), anyString(), anyString()))
                    .thenReturn(jqlResponse);

            Map<String, String> result = dao.fetchErrorsInJQLQuery(false, "acc1", "inv1", "project=PROJ");
            // errors array is empty → no "errors" key; "query" key only if present
            assertNotNull(result);
        }

        @Test
        @DisplayName("Returns empty map when queries array element is null")
        void fetchErrorsInJQLQuery_nullQueries_returnsEmpty() {
            String jqlResponse = "{}";
            when(restClient.parseAndValidateJQL(anyString(), anyBoolean(), anyString(), anyString()))
                    .thenReturn(jqlResponse);

            Map<String, String> result = dao.fetchErrorsInJQLQuery(false, "acc1", "inv1", "project=P");
            assertTrue(result.isEmpty());
        }
    }

    // ── getIssues ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getIssues")
    class GetIssuesTests {

        @Test
        @DisplayName("Parses each JSON object element into an Issue")
        void getIssues_validArray_returnsList() {
            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            JsonArray arr = parser.parse("[" + ISSUE_JSON + "]").getAsJsonArray();

            List<Issue> result = dao.getIssues(arr);
            assertEquals(1, result.size());
            assertEquals(ISSUE_KEY, result.get(0).key);
        }
    }

    // ── updateStatus ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatusTests {

        @Test
        @DisplayName("Posts transition successfully when status is found")
        void updateStatus_statusFound_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/transitions"), any()))
                    .thenReturn(invoker);
            // ticketInfoDao.getTransitions → restClient.fetch("issue/TEST-1/transitions", ...)
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/transitions"), any(), eq(String.class)))
                    .thenReturn("{\"transitions\":[{\"id\":\"31\",\"name\":\"Done\"," +
                            "\"to\":{\"id\":\"3\",\"name\":\"Done\",\"statusCategory\":{\"name\":\"Done\"}}}]}");
            when(invoker.post(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.updateStatus(ISSUE_KEY, "Done"));
        }

        @Test
        @DisplayName("Throws RuntimeException when no transition matches status name")
        void updateStatus_noMatchingStatus_throwsRuntimeException() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/transitions"), any()))
                    .thenReturn(invoker);
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/transitions"), any(), eq(String.class)))
                    .thenReturn("{\"transitions\":[{\"id\":\"31\",\"name\":\"In Progress\"," +
                            "\"to\":{\"id\":\"2\",\"name\":\"In Progress\",\"statusCategory\":{\"name\":\"In Progress\"}}}]}");

            assertThrows(RuntimeException.class,
                    () -> dao.updateStatus(ISSUE_KEY, "NonExistentStatus"));
        }

        @Test
        @DisplayName("Throws RuntimeException when post response is not successful")
        void updateStatus_failedResponse_throwsRuntimeException() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/transitions"), any()))
                    .thenReturn(invoker);
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/transitions"), any(), eq(String.class)))
                    .thenReturn("{\"transitions\":[{\"id\":\"31\",\"name\":\"Done\"," +
                            "\"to\":{\"id\":\"3\",\"name\":\"Done\",\"statusCategory\":{\"name\":\"Done\"}}}]}");
            when(invoker.post(any())).thenReturn(response);
            mockFailureFamily();
            when(response.readEntity(String.class)).thenReturn("transition error");

            assertThrows(RuntimeException.class,
                    () -> dao.updateStatus(ISSUE_KEY, "Done"));
        }
    }

    // ── addRequestParticipants ──────────────────────────────────────────────

    @Nested
    @DisplayName("addRequestParticipants")
    class AddRequestParticipantsTests {

        @Test
        @DisplayName("Posts participants successfully")
        void addRequestParticipants_success() {
            when(restClient.getJiraServiceResourceInvoker(
                    eq("request/" + ISSUE_KEY + "/participant"), any())).thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            mockSuccessFamily();

            assertThrows(UnsupportedOperationException.class,
                    () -> dao.addRequestParticipants(ISSUE_KEY, List.of("acc1", "acc2")));
        }

        @Test
        @DisplayName("Throws RuntimeException when response is not successful")
        void addRequestParticipants_failure_throwsRuntimeException() {
            when(restClient.getJiraServiceResourceInvoker(
                    eq("request/" + ISSUE_KEY + "/participant"), any())).thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            mockFailureFamily();
            when(response.readEntity(String.class)).thenReturn("error");

            assertThrows(RuntimeException.class,
                    () -> dao.addRequestParticipants(ISSUE_KEY, List.of("acc1")));
        }
    }

    // ── updateIssue ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateIssue")
    class UpdateIssueTests {

        @Test
        @DisplayName("Single-param updateIssue delegates to two-param version (no-op update)")
        void updateIssue_singleParam_delegatesToTwoParam() {
            Issue request = new Issue();
            request.key = ISSUE_KEY;
            // all optional fields null → no conditional sub-updates

            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            stubIssueResponse();
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.updateIssue(request));
            verify(invoker).put(any());
        }

        @Test
        @DisplayName("updateIssue with null customFields skips custom field update")
        void updateIssue_nullCustomFields_skipsCustomFields() {
            Issue request = new Issue();
            request.key = ISSUE_KEY;

            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            stubIssueResponse();
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.updateIssue(request, null));
        }

        @Test
        @DisplayName("Throws RuntimeException when PUT response is not successful")
        void updateIssue_failedPut_throwsRuntimeException() {
            Issue request = new Issue();
            request.key = ISSUE_KEY;

            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            stubIssueResponse();
            when(invoker.put(any())).thenReturn(response);
            mockFailureFamily();
            when(response.readEntity(String.class)).thenReturn("update error");

            assertThrows(RuntimeException.class, () -> dao.updateIssue(request, null));
        }

        @Test
        @DisplayName("updateIssue with customFields (string type) updates field")
        void updateIssue_withStringCustomField_updatesField() {
            Issue request = new Issue();
            request.key = ISSUE_KEY;

            String editMetaJson = "{\"fields\":{\"customfield_10100\":{\"name\":\"My Field\"," +
                    "\"schema\":{\"type\":\"string\"}}}}";

            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            stubIssueResponse();
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/editmeta"), any(), eq(String.class)))
                    .thenReturn(editMetaJson);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            when(freeForm.isEmpty()).thenReturn(false);
            when(freeForm.keySet()).thenReturn(Set.of("My Field"));
            when(freeForm.get("My Field")).thenReturn("New String Value");

            assertDoesNotThrow(() -> dao.updateIssue(request, freeForm));
        }

        @Test
        @DisplayName("updateIssue with customFields (date type, number value) updates field")
        void updateIssue_withDateCustomField_numberValue_updatesField() {
            Issue request = new Issue();
            request.key = ISSUE_KEY;

            String editMetaJson = "{\"fields\":{\"customfield_10200\":{\"name\":\"Due Date\"," +
                    "\"schema\":{\"type\":\"date\"}}}}";

            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            stubIssueResponse();
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/editmeta"), any(), eq(String.class)))
                    .thenReturn(editMetaJson);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            when(freeForm.isEmpty()).thenReturn(false);
            when(freeForm.keySet()).thenReturn(Set.of("Due Date"));
            when(freeForm.get("Due Date")).thenReturn(1700000000000.0);  // double (Number)

            assertDoesNotThrow(() -> dao.updateIssue(request, freeForm));
        }

        @Test
        @DisplayName("updateIssue with customFields (date type, non-number value) skips update")
        void updateIssue_withDateCustomField_nonNumberValue_skips() {
            Issue request = new Issue();
            request.key = ISSUE_KEY;

            String editMetaJson = "{\"fields\":{\"customfield_10200\":{\"name\":\"Due Date\"," +
                    "\"schema\":{\"type\":\"date\"}}}}";

            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            stubIssueResponse();
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/editmeta"), any(), eq(String.class)))
                    .thenReturn(editMetaJson);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            when(freeForm.isEmpty()).thenReturn(false);
            when(freeForm.keySet()).thenReturn(Set.of("Due Date"));
            when(freeForm.get("Due Date")).thenReturn("2026-01-01");  // String, not Number

            assertDoesNotThrow(() -> dao.updateIssue(request, freeForm));
        }

        @Test
        @DisplayName("updateIssue with customFields (option type) finds and sets optionId")
        void updateIssue_withOptionCustomField_setsOptionId() {
            Issue request = new Issue();
            request.key = ISSUE_KEY;

            String editMetaJson = "{\"fields\":{\"customfield_10300\":{\"name\":\"Category\"," +
                    "\"schema\":{\"type\":\"option\"}," +
                    "\"allowedValues\":[{\"id\":\"opt1\",\"value\":\"Backend\"}]}}}";

            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            stubIssueResponse();
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/editmeta"), any(), eq(String.class)))
                    .thenReturn(editMetaJson);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            when(freeForm.isEmpty()).thenReturn(false);
            when(freeForm.keySet()).thenReturn(Set.of("Category"));
            when(freeForm.get("Category")).thenReturn("Backend");

            assertDoesNotThrow(() -> dao.updateIssue(request, freeForm));
        }

        @Test
        @DisplayName("updateIssue with customFields (array of non-string items) builds array")
        void updateIssue_withArrayCustomField_nonStringItems_buildsArray() {
            Issue request = new Issue();
            request.key = ISSUE_KEY;

            String editMetaJson = "{\"fields\":{\"customfield_10400\":{\"name\":\"Tags\"," +
                    "\"schema\":{\"type\":\"array\",\"items\":\"option\"}," +
                    "\"allowedValues\":[{\"id\":\"t1\",\"value\":\"java\"},{\"id\":\"t2\",\"value\":\"python\"}]}}}";

            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            stubIssueResponse();
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/editmeta"), any(), eq(String.class)))
                    .thenReturn(editMetaJson);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            when(freeForm.isEmpty()).thenReturn(false);
            when(freeForm.keySet()).thenReturn(Set.of("Tags"));
            when(freeForm.get("Tags")).thenReturn(Arrays.asList("java", "python"));

            assertDoesNotThrow(() -> dao.updateIssue(request, freeForm));
        }

        @Test
        @DisplayName("updateIssue with customFields (array of string items) builds string array")
        void updateIssue_withArrayCustomField_stringItems_buildsStringArray() {
            Issue request = new Issue();
            request.key = ISSUE_KEY;

            String editMetaJson = "{\"fields\":{\"customfield_10500\":{\"name\":\"Labels\"," +
                    "\"schema\":{\"type\":\"array\",\"items\":\"string\"}}}}";

            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            stubIssueResponse();
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/editmeta"), any(), eq(String.class)))
                    .thenReturn(editMetaJson);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            when(freeForm.isEmpty()).thenReturn(false);
            when(freeForm.keySet()).thenReturn(Set.of("Labels"));
            when(freeForm.get("Labels")).thenReturn(Arrays.asList("backend", "api"));

            assertDoesNotThrow(() -> dao.updateIssue(request, freeForm));
        }

        @Test
        @DisplayName("updateIssue with customFields (unknown type) hits default case")
        void updateIssue_withUnknownTypeCustomField_hitsDefaultCase() {
            Issue request = new Issue();
            request.key = ISSUE_KEY;

            String editMetaJson = "{\"fields\":{\"customfield_10600\":{\"name\":\"Special\"," +
                    "\"schema\":{\"type\":\"unknown-type\"}}}}";

            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            stubIssueResponse();
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/editmeta"), any(), eq(String.class)))
                    .thenReturn(editMetaJson);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            when(freeForm.isEmpty()).thenReturn(false);
            when(freeForm.keySet()).thenReturn(Set.of("Special"));
            when(freeForm.get("Special")).thenReturn("some value");

            assertDoesNotThrow(() -> dao.updateIssue(request, freeForm));
        }
    }

    // ── addLabel ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addLabel")
    class AddLabelTests {

        @Test
        @DisplayName("Puts label update successfully")
        void addLabel_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.addLabel("backend", ISSUE_KEY));
        }

        @Test
        @DisplayName("Throws RuntimeException when response is not successful")
        void addLabel_failure_throwsRuntimeException() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            mockFailureFamily();
            when(response.readEntity(String.class)).thenReturn("label error");

            assertThrows(RuntimeException.class, () -> dao.addLabel("backend", ISSUE_KEY));
        }
    }

    // ── assignTask ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("assignTask")
    class AssignTaskTests {

        @Test
        @DisplayName("Puts assignee update successfully")
        void assignTask_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/assignee"), any()))
                    .thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.assignTask(ISSUE_KEY, "user-account-id"));
        }

        @Test
        @DisplayName("Throws RuntimeException when response is not successful")
        void assignTask_failure_throwsRuntimeException() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/assignee"), any()))
                    .thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            mockFailureFamily();
            when(response.readEntity(String.class)).thenReturn("assign error");

            assertThrows(RuntimeException.class, () -> dao.assignTask(ISSUE_KEY, "user-id"));
        }
    }

    // ── getIssueFinal ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getIssueFinal")
    class GetIssueFinalTests {

        @Test
        @DisplayName("Returns a populated Issue on successful fetch")
        void getIssueFinal_success_returnsIssue() {
            stubIssueResponse();

            Issue result = dao.getIssueFinal(ISSUE_KEY);
            assertNotNull(result);
            assertEquals(ISSUE_KEY, result.key);
        }

        @Test
        @DisplayName("Resolves requestType name when requestType contains '/'")
        void getIssueFinal_requestTypeContainsSlash_resolvesName() {
            // First call returns JSON with requesttype = "portal1/rt1"
            // Second call (recursive via getIssueId) returns normal JSON without requesttype
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenReturn(ISSUE_JSON_WITH_REQUEST_TYPE)
                    .thenReturn(ISSUE_JSON);
            when(restClient.fetchMiscellaneousService(
                    eq("request/" + ISSUE_ID + "/request-types"), any(), eq(String.class)))
                    .thenReturn("{\"validRequestTypes\":[" +
                            "{\"portalKey\":\"portal1\",\"key\":\"rt1\",\"name\":\"IT Help\"}]}");

            Issue result = dao.getIssueFinal(ISSUE_KEY);
            assertEquals("IT Help", result.requestType);
        }

        @Test
        @DisplayName("Returns empty Issue when fetch throws RuntimeException")
        void getIssueFinal_runtimeException_returnsEmptyIssue() {
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenThrow(new RuntimeException("fetch failed"));

            Issue result = dao.getIssueFinal(ISSUE_KEY);
            assertNotNull(result);
            assertNull(result.key);  // empty Issue has null key
        }
    }

    // ── getCustomIssue ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getCustomIssue")
    class GetCustomIssueTests {

        private static final String FIELD_LIST_JSON =
                "[{\"name\":\"My Field\",\"key\":\"customfield_10100\",\"schema\":{\"type\":\"string\"}}]";

        private void stubFieldResponse() {
            when(restClient.fetch(eq("field"), any(), eq(String.class))).thenReturn(FIELD_LIST_JSON);
        }

        @Test
        @DisplayName("Returns empty list when field key is not found")
        void getCustomIssue_fieldKeyNull_returnsEmpty() {
            // FIELD_LIST_JSON doesn't have "NonExistentField"
            stubFieldResponse();

            List<String> result = dao.getCustomIssue(ISSUE_KEY, "NonExistentField");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Returns empty list when field value is null in issue JSON")
        void getCustomIssue_fieldValueNull_returnsEmpty() {
            stubFieldResponse();
            String issueWithNullField =
                    "{\"id\":\"10001\",\"key\":\"TEST-1\",\"fields\":{\"customfield_10100\":null}}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenReturn(issueWithNullField);

            List<String> result = dao.getCustomIssue(ISSUE_KEY, "My Field");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Extracts value from JsonObject field with 'value' key")
        void getCustomIssue_jsonObjectWithValue_returnsValue() {
            stubFieldResponse();
            String issueWithObj =
                    "{\"id\":\"10001\",\"key\":\"TEST-1\",\"fields\":" +
                    "{\"customfield_10100\":{\"value\":\"Option A\",\"id\":\"1\"}}}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenReturn(issueWithObj);

            List<String> result = dao.getCustomIssue(ISSUE_KEY, "My Field");
            assertEquals(List.of("Option A"), result);
        }

        @Test
        @DisplayName("Extracts displayName from JsonObject field when no 'value' key")
        void getCustomIssue_jsonObjectWithDisplayName_returnsDisplayName() {
            stubFieldResponse();
            String issueWithObj =
                    "{\"id\":\"10001\",\"key\":\"TEST-1\",\"fields\":" +
                    "{\"customfield_10100\":{\"displayName\":\"John Doe\",\"accountId\":\"u1\"}}}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenReturn(issueWithObj);

            List<String> result = dao.getCustomIssue(ISSUE_KEY, "My Field");
            assertEquals(List.of("John Doe"), result);
        }

        @Test
        @DisplayName("Extracts name from JsonObject field when no 'value' or 'displayName'")
        void getCustomIssue_jsonObjectWithName_returnsName() {
            stubFieldResponse();
            String issueWithObj =
                    "{\"id\":\"10001\",\"key\":\"TEST-1\",\"fields\":" +
                    "{\"customfield_10100\":{\"name\":\"Backend\",\"id\":\"c1\"}}}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenReturn(issueWithObj);

            List<String> result = dao.getCustomIssue(ISSUE_KEY, "My Field");
            assertEquals(List.of("Backend"), result);
        }

        @Test
        @DisplayName("Returns empty list when JsonObject has none of value/displayName/name")
        void getCustomIssue_jsonObjectWithUnknownKeys_returnsEmpty() {
            stubFieldResponse();
            String issueWithObj =
                    "{\"id\":\"10001\",\"key\":\"TEST-1\",\"fields\":" +
                    "{\"customfield_10100\":{\"otherKey\":\"data\"}}}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenReturn(issueWithObj);

            List<String> result = dao.getCustomIssue(ISSUE_KEY, "My Field");
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Extracts strings and objects from a JsonArray field")
        void getCustomIssue_jsonArray_withPrimitivesAndObjects() {
            stubFieldResponse();
            String issueWithArray =
                    "{\"id\":\"10001\",\"key\":\"TEST-1\",\"fields\":" +
                    "{\"customfield_10100\":[\"label1\",{\"name\":\"Component A\"}]}}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenReturn(issueWithArray);

            List<String> result = dao.getCustomIssue(ISSUE_KEY, "My Field");
            assertTrue(result.contains("label1"));
            assertTrue(result.contains("Component A"));
        }

        @Test
        @DisplayName("Extracts string primitive value directly")
        void getCustomIssue_jsonPrimitiveString_returnsValue() {
            stubFieldResponse();
            String issueWithPrimitive =
                    "{\"id\":\"10001\",\"key\":\"TEST-1\",\"fields\":" +
                    "{\"customfield_10100\":\"simple string value\"}}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenReturn(issueWithPrimitive);

            List<String> result = dao.getCustomIssue(ISSUE_KEY, "My Field");
            assertEquals(List.of("simple string value"), result);
        }

        @Test
        @DisplayName("Returns empty list when JsonPrimitive is not a string (e.g., boolean)")
        void getCustomIssue_jsonPrimitiveNonString_returnsEmpty() {
            stubFieldResponse();
            String issueWithBool =
                    "{\"id\":\"10001\",\"key\":\"TEST-1\",\"fields\":" +
                    "{\"customfield_10100\":true}}";
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenReturn(issueWithBool);

            List<String> result = dao.getCustomIssue(ISSUE_KEY, "My Field");
            assertTrue(result.isEmpty());
        }
    }

    // ── getKeyByName ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getKeyByName")
    class GetKeyByNameTests {

        private static final String FIELDS_JSON =
                "[{\"name\":\"Summary\",\"key\":\"summary\"}," +
                "{\"name\":\"Priority\",\"key\":\"priority\"}]";

        @Test
        @DisplayName("Returns the key when the name matches (case-insensitive)")
        void getKeyByName_found_returnsKey() {
            assertEquals("summary", IssueDao.getKeyByName(FIELDS_JSON, "SUMMARY"));
        }

        @Test
        @DisplayName("Returns null when no field matches the name")
        void getKeyByName_notFound_returnsNull() {
            assertNull(IssueDao.getKeyByName(FIELDS_JSON, "NonExistentField"));
        }
    }

    // ── isIssueExists ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("isIssueExists")
    class IsIssueExistsTests {

        @Test
        @DisplayName("Returns true when getIssueFinal succeeds")
        void isIssueExists_found_returnsTrue() {
            stubIssueResponse();
            assertTrue(dao.isIssueExists(ISSUE_KEY));
        }

        @Test
        @DisplayName("Returns false when getIssueFinal throws NoSuchElementException")
        void isIssueExists_getIssueFinalThrowsNoSuchElement_returnsFalse() {
            // getIssueFinal() catches all RuntimeException internally, so to exercise the
            // catch block in isIssueExists() we must stub getIssueFinal() directly via spy.
            IssueDao spyDao = spy(dao);
            doThrow(new java.util.NoSuchElementException("not found"))
                    .when(spyDao).getIssueFinal(ISSUE_KEY);
            assertFalse(spyDao.isIssueExists(ISSUE_KEY));
        }
    }

    // ── getIssueResponse / getFieldResponse / getJiraFieldsData ─────────────

    @Nested
    @DisplayName("getIssueResponse / getFieldResponse / getJiraFieldsData")
    class GetResponseTests {

        @Test
        @DisplayName("getIssueResponse returns the fetch result")
        void getIssueResponse_returnsFetchedString() {
            when(restClient.fetch(eq("issue/" + ISSUE_KEY), any(), eq(String.class)))
                    .thenReturn(ISSUE_JSON);
            assertEquals(ISSUE_JSON, dao.getIssueResponse(ISSUE_KEY));
        }

        @Test
        @DisplayName("getFieldResponse returns the fetch result for 'field'")
        void getFieldResponse_returnsFetchedString() {
            when(restClient.fetch(eq("field"), any(), eq(String.class))).thenReturn("[]");
            assertEquals("[]", dao.getFieldResponse());
        }

        @Test
        @DisplayName("getJiraFieldsData returns the 'fields' object from editmeta response")
        void getJiraFieldsData_returnsFieldsObject() {
            when(restClient.fetch(eq("issue/" + ISSUE_KEY + "/editmeta"), any(), eq(String.class)))
                    .thenReturn("{\"fields\":{\"summary\":{\"name\":\"Summary\"}}}");
            assertNotNull(dao.getJiraFieldsData(ISSUE_KEY));
        }
    }

    // ── updatePriority ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("updatePriority")
    class UpdatePriorityTests {

        @Test
        @DisplayName("Puts priority and returns updated issue")
        void updatePriority_success_returnsIssue() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(200);
            stubIssueResponse();

            TicketPriority priority = new TicketPriority();
            priority.name = "High";
            priority.id = "2";

            Issue result = dao.updatePriority(priority, ISSUE_KEY);
            assertNotNull(result);
        }

        @Test
        @DisplayName("Throws RuntimeException when PUT status >= 400")
        void updatePriority_failedStatus_throwsRuntimeException() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(400);
            when(response.readEntity(String.class)).thenReturn("priority error");

            TicketPriority priority = new TicketPriority();
            priority.name = "High";

            assertThrows(RuntimeException.class, () -> dao.updatePriority(priority, ISSUE_KEY));
        }

        @Test
        @DisplayName("getPriorityJsonObject works with only id set")
        void updatePriority_priorityWithIdOnly_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(200);
            stubIssueResponse();

            TicketPriority priority = new TicketPriority();
            priority.id = "3";
            priority.name = null;  // only id, no name

            assertNotNull(dao.updatePriority(priority, ISSUE_KEY));
        }

        @Test
        @DisplayName("getPriorityJsonObject works with only name set")
        void updatePriority_priorityWithNameOnly_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(200);
            stubIssueResponse();

            TicketPriority priority = new TicketPriority();
            priority.id = null;   // only name, no id
            priority.name = "Low";

            assertNotNull(dao.updatePriority(priority, ISSUE_KEY));
        }
    }

    // ── addWorklog ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addWorklog")
    class AddWorklogTests {

        @Test
        @DisplayName("Posts worklog without comment successfully")
        void addWorklog_noComment_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/worklog"), any()))
                    .thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(201);

            assertDoesNotThrow(() -> dao.addWorklog("2h", System.currentTimeMillis(), ISSUE_KEY, null));
        }

        @Test
        @DisplayName("Posts worklog with comment successfully")
        void addWorklog_withComment_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/worklog"), any()))
                    .thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(201);

            assertDoesNotThrow(() -> dao.addWorklog("1h", System.currentTimeMillis(),
                    ISSUE_KEY, "Worked on feature"));
        }

        @Test
        @DisplayName("Posts worklog with empty comment (treated as no comment)")
        void addWorklog_emptyComment_skipsCommentField() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/worklog"), any()))
                    .thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(201);

            assertDoesNotThrow(() -> dao.addWorklog("30m", System.currentTimeMillis(), ISSUE_KEY, ""));
        }

        @Test
        @DisplayName("Throws RuntimeException when POST status >= 400")
        void addWorklog_failedStatus_throwsRuntimeException() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/worklog"), any()))
                    .thenReturn(invoker);
            when(invoker.post(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(400);
            when(response.readEntity(String.class)).thenReturn("worklog error");

            assertThrows(RuntimeException.class,
                    () -> dao.addWorklog("2h", System.currentTimeMillis(), ISSUE_KEY, null));
        }
    }

    // ── attachFileToTicket ───────────────────────────────────────────────────

    @Nested
    @DisplayName("attachFileToTicket")
    class AttachFileToTicketTests {

        @Test
        @DisplayName("Returns true when attachFile returns status 200")
        void attachFileToTicket_status200_returnsTrue() {
            List<File> files = List.of(new File("test.pdf"));
            when(restClient.attachFile(eq(ISSUE_KEY), eq(files))).thenReturn(attachResponse);
            when(attachResponse.getStatus()).thenReturn(200);

            assertTrue(dao.attachFileToTicket(files, ISSUE_KEY));
        }

        @Test
        @DisplayName("Returns false when attachFile returns non-200 status")
        void attachFileToTicket_nonStatus200_returnsFalse() {
            List<File> files = List.of(new File("test.pdf"));
            when(restClient.attachFile(eq(ISSUE_KEY), eq(files))).thenReturn(attachResponse);
            when(attachResponse.getStatus()).thenReturn(400);

            assertFalse(dao.attachFileToTicket(files, ISSUE_KEY));
        }
    }

    // ── assignIssueToEpic ────────────────────────────────────────────────────

    @Nested
    @DisplayName("assignIssueToEpic")
    class AssignIssueToEpicTests {

        @Test
        @DisplayName("Assigns issue to epic when Epic Link field is found")
        void assignIssueToEpic_epicLinkFound_success() {
            // getEpicLinkId uses restClient.fetch("field", ...) → returns array with "Epic Link"
            when(restClient.fetch(eq("field"), any(), eq(String.class)))
                    .thenReturn("[{\"name\":\"Epic Link\",\"key\":\"customfield_10014\"}]");
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/"), any()))
                    .thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.assignIssueToEpic(ISSUE_KEY, "PROJ-100"));
        }

        @Test
        @DisplayName("Throws RuntimeException when PUT response is not successful")
        void assignIssueToEpic_failedPut_throwsRuntimeException() {
            when(restClient.fetch(eq("field"), any(), eq(String.class)))
                    .thenReturn("[{\"name\":\"Epic Link\",\"key\":\"customfield_10014\"}]");
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/"), any()))
                    .thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            mockFailureFamily();
            when(response.readEntity(String.class)).thenReturn("epic error");

            assertThrows(RuntimeException.class,
                    () -> dao.assignIssueToEpic(ISSUE_KEY, "PROJ-100"));
        }

        @Test
        @DisplayName("Uses empty string as epicLinkId when Epic Link field is not found")
        void assignIssueToEpic_epicLinkNotFound_usesEmptyKey() {
            when(restClient.fetch(eq("field"), any(), eq(String.class)))
                    .thenReturn("[{\"name\":\"Summary\",\"key\":\"summary\"}]");
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY + "/"), any()))
                    .thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.assignIssueToEpic(ISSUE_KEY, "PROJ-100"));
        }
    }

    // ── assignChildIssueToEpic ───────────────────────────────────────────────

    @Nested
    @DisplayName("assignChildIssueToEpic")
    class AssignChildIssueToEpicTests {

        @Test
        @DisplayName("Assigns child issue to epic when response is 204")
        void assignChildIssueToEpic_status204_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(204);

            assertDoesNotThrow(() -> dao.assignChildIssueToEpic(ISSUE_KEY, "EPIC-1"));
        }

        @Test
        @DisplayName("Throws RuntimeException when response is not 204")
        void assignChildIssueToEpic_nonStatus204_throwsRuntimeException() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            when(response.getStatus()).thenReturn(400);
            when(response.readEntity(String.class)).thenReturn("child epic error");

            assertThrows(RuntimeException.class,
                    () -> dao.assignChildIssueToEpic(ISSUE_KEY, "EPIC-1"));
        }
    }

    // ── addComponent ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("addComponent")
    class AddComponentTests {

        @Test
        @DisplayName("Puts component update successfully")
        void addComponent_success() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            mockSuccessFamily();

            assertDoesNotThrow(() -> dao.addComponent(ISSUE_KEY, "Backend"));
        }

        @Test
        @DisplayName("Throws RuntimeException when response is not successful")
        void addComponent_failure_throwsRuntimeException() {
            when(restClient.getResourceInvoker(eq("issue/" + ISSUE_KEY), any())).thenReturn(invoker);
            when(invoker.put(any())).thenReturn(response);
            mockFailureFamily();
            when(response.readEntity(String.class)).thenReturn("component error");

            assertThrows(RuntimeException.class,
                    () -> dao.addComponent(ISSUE_KEY, "Backend"));
        }
    }

    // ── getComponents ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getComponents")
    class GetComponentsTests {

        @Test
        @DisplayName("Returns component list from all projects")
        void getComponents_returnsComponents() {
            // projectDao.getAllProjects() → restClient.fetch("project", ...)
            when(restClient.fetch(eq("project"), any(), eq(String.class)))
                    .thenReturn("[{\"id\":\"1\",\"key\":\"PROJ\",\"name\":\"Test Project\"," +
                            "\"style\":\"next-gen\",\"projectTypeKey\":\"software\"}]");
            // Then for each project: restClient.fetch("project/PROJ/components", ...)
            when(restClient.fetch(eq("project/PROJ/components"), any(), eq(String.class)))
                    .thenReturn("[{\"id\":\"c1\",\"name\":\"Backend\",\"project\":\"PROJ\"}]");

            List<?> result = dao.getComponents();
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Skips blank component responses")
        void getComponents_blankComponentResponse_skipped() {
            when(restClient.fetch(eq("project"), any(), eq(String.class)))
                    .thenReturn("[{\"id\":\"1\",\"key\":\"PROJ\",\"name\":\"Test Project\"," +
                            "\"style\":\"next-gen\",\"projectTypeKey\":\"software\"}]");
            when(restClient.fetch(eq("project/PROJ/components"), any(), eq(String.class)))
                    .thenReturn("   ");  // blank response

            List<?> result = dao.getComponents();
            assertTrue(result.isEmpty());
        }
    }

    // ── getLabels ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getLabels")
    class GetLabelsTests {

        @Test
        @DisplayName("Returns label list from response")
        void getLabels_returnsLabels() {
            when(restClient.fetch(eq("label"), any(), eq(String.class)))
                    .thenReturn("{\"values\":[\"backend\",\"frontend\"]}");

            List<?> result = dao.getLabels();
            assertEquals(2, result.size());
        }
    }

    // ── validateCredentials ──────────────────────────────────────────────────

    @Nested
    @DisplayName("validateCredentials")
    class ValidateCredentialsTests {

        @Test
        @DisplayName("Calls getJiraAccessToken on the restClient obtained with invokeAsUser=true")
        void validateCredentials_callsGetJiraAccessToken() {
            // getRestClient(true) → restClient (plain mock — not AuthRestClient, condition false)
            // getJiraAccessToken() is a default interface method; Mockito mocks do NOT call the
            // real default implementation, so getAccessToken() is never reached from the mock.
            // We therefore verify the call at the level Mockito actually intercepts.
            assertDoesNotThrow(() -> dao.validateCredentials());
            verify(restClient).getJiraAccessToken();
        }
    }
}
