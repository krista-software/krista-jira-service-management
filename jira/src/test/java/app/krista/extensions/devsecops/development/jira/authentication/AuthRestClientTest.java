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

package app.krista.extensions.devsecops.development.jira.authentication;

import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.sdk.atlassian.error.HttpErrorMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthRestClient Tests")
class AuthRestClientTest {

    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        attributes = new HashMap<>();
        attributes.put(JiraAttributes.USER_NAME, "test-user");
        attributes.put(JiraAttributes.AUTH_TOKEN, "test-token");
        attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net/rest/api/3");
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private Client mockClient;
    private WebTarget mockBaseTarget;
    private WebTarget mockPathTarget;
    private Invocation.Builder mockBuilder;
    private Response mockResponse;

    /**
     * Sets up a full Jersey mock chain:
     * ClientBuilder.newClient() → client.register() → client.target() → target.path() → ...
     */
    private MockedStatic<ClientBuilder> setupJerseyMocks() {
        MockedStatic<ClientBuilder> cbMock = mockStatic(ClientBuilder.class);
        mockClient = mock(Client.class);
        mockBaseTarget = mock(WebTarget.class);
        mockPathTarget = mock(WebTarget.class);
        mockBuilder = mock(Invocation.Builder.class);
        mockResponse = mock(Response.class);

        cbMock.when(ClientBuilder::newClient).thenReturn(mockClient);
        when(mockClient.register(any())).thenReturn(mockClient);
        when(mockClient.target(anyString())).thenReturn(mockBaseTarget);
        when(mockBaseTarget.path(anyString())).thenReturn(mockPathTarget);
        when(mockPathTarget.path(anyString())).thenReturn(mockPathTarget);
        when(mockPathTarget.queryParam(anyString(), any())).thenReturn(mockPathTarget);
        when(mockPathTarget.request(any(MediaType.class))).thenReturn(mockBuilder);
        when(mockPathTarget.request(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.header(anyString(), anyString())).thenReturn(mockBuilder);
        when(mockPathTarget.getUri()).thenReturn(java.net.URI.create("https://test.atlassian.net/rest/servicedeskapi"));

        return cbMock;
    }

    private void mockSuccessResponse(String body) {
        Response.StatusType statusType = mock(Response.StatusType.class);
        when(mockResponse.getStatusInfo()).thenReturn(statusType);
        when(statusType.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);
        when(mockResponse.readEntity(String.class)).thenReturn(body);
    }

    private void mockErrorResponse(int status, String body) {
        Response.StatusType statusType = mock(Response.StatusType.class);
        when(mockResponse.getStatusInfo()).thenReturn(statusType);
        when(statusType.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
        when(mockResponse.getStatus()).thenReturn(status);
        when(mockResponse.readEntity(String.class)).thenReturn(body);
    }

    // ── Constructor & class structure ────────────────────────────────────

    @Nested
    @DisplayName("Constructor and class structure")
    class ConstructorTests {

        @Test
        @DisplayName("Should create AuthRestClient with valid attributes")
        void constructor_success() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            assertNotNull(client);
        }

        @Test
        @DisplayName("Should be final class")
        void isFinalClass() {
            assertTrue(java.lang.reflect.Modifier.isFinal(AuthRestClient.class.getModifiers()));
        }

        @Test
        @DisplayName("Should implement RestClient interface")
        void implementsRestClient() {
            assertTrue(RestClient.class.isAssignableFrom(AuthRestClient.class));
        }

        @Test
        @DisplayName("Should allow invokeAsUser when callFromValidateAttributes is true")
        void allowsInvokeAsUser_whenFromValidateAttributes() {
            AuthRestClient client = new AuthRestClient(attributes, true, true);
            assertNotNull(client);
        }

        @Test
        @DisplayName("getJiraAccessToken returns null")
        void getJiraAccessToken_returnsNull() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            assertNull(client.getJiraAccessToken());
        }
    }

    // ── getBaseUrl (private) ────────────────────────────────────────────

    @Nested
    @DisplayName("getBaseUrl")
    class GetBaseUrlTests {

        private String invokeGetBaseUrl(String hostUrl) throws Exception {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            Method method = AuthRestClient.class.getDeclaredMethod("getBaseUrl", String.class);
            method.setAccessible(true);
            return (String) method.invoke(client, hostUrl);
        }

        @Test
        @DisplayName("Extracts base URL from hostUrl with /rest/ path")
        void withRestPath() throws Exception {
            assertEquals("https://test.atlassian.net", invokeGetBaseUrl("https://test.atlassian.net/rest/api/3"));
        }

        @Test
        @DisplayName("Extracts base URL from hostUrl ending with /rest")
        void endingWithRest() throws Exception {
            assertEquals("https://test.atlassian.net", invokeGetBaseUrl("https://test.atlassian.net/rest"));
        }

        @Test
        @DisplayName("Returns same URL when no /rest in hostUrl")
        void noRestPath() throws Exception {
            assertEquals("https://test.atlassian.net", invokeGetBaseUrl("https://test.atlassian.net"));
        }

        @Test
        @DisplayName("Returns empty string when hostUrl is null")
        void nullHostUrl() throws Exception {
            assertEquals("", invokeGetBaseUrl(null));
        }

        @Test
        @DisplayName("Returns empty string when hostUrl is empty")
        void emptyHostUrl() throws Exception {
            assertEquals("", invokeGetBaseUrl(""));
        }
    }

    // ── checkInvokeAsUser ───────────────────────────────────────────────

    @Nested
    @DisplayName("checkInvokeAsUser")
    class CheckInvokeAsUserTests {

        @Test
        @DisplayName("Throws when invokeAsUser=true and callFromValidateAttributes=false")
        void throwsWhenInvokeAsUserTrue() {
            AuthRestClient client = new AuthRestClient(attributes, true, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                assertThrows(IllegalArgumentException.class, () ->
                        client.getJiraServiceResourceInvoker("test", Map.of()));
            }
        }

        @Test
        @DisplayName("Does not throw when callFromValidateAttributes=true")
        void allowsWhenCallFromValidateAttributes() {
            AuthRestClient client = new AuthRestClient(attributes, true, true);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                Invocation.Builder result = client.getJiraServiceResourceInvoker("test", Map.of());
                assertNotNull(result);
            }
        }

        @Test
        @DisplayName("Uses field invokeAsUser when override is null")
        void usesFieldWhenOverrideNull() {
            // invokeAsUser=true, callFromValidateAttributes=false, override=null → throws
            AuthRestClient client = new AuthRestClient(attributes, true, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                // getJiraServiceResourceInvoker passes null as override
                assertThrows(IllegalArgumentException.class, () ->
                        client.getJiraServiceResourceInvoker("test", Map.of()));
            }
        }

        @Test
        @DisplayName("Uses override=false when field invokeAsUser=true → no throw")
        void overrideFalse_fieldTrue_noThrow() {
            AuthRestClient client = new AuthRestClient(attributes, true, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks();
                 MockedStatic<Entity> entityMock = mockStatic(Entity.class)) {
                Entity<?> mockEntity = mock(Entity.class);
                entityMock.when(() -> Entity.entity(anyString(), eq(MediaType.APPLICATION_JSON)))
                        .thenReturn(mockEntity);
                when(mockBuilder.post(any(Entity.class))).thenReturn(mockResponse);
                Response.StatusType statusType = mock(Response.StatusType.class);
                when(mockResponse.getStatusInfo()).thenReturn(statusType);
                when(statusType.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);
                when(mockResponse.readEntity(String.class)).thenReturn("{}");
                // parseAndValidateJQL passes explicit invokeAsUser=false
                String result = client.parseAndValidateJQL("project=TEST", false, "acc", "inv");
                assertNotNull(result);
            }
        }

        @Test
        @DisplayName("Override=true throws when callFromValidateAttributes=false via reflection")
        void overrideTrue_callFromValidateFalse_throws() throws Exception {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            Method m = AuthRestClient.class.getDeclaredMethod("checkInvokeAsUser", Boolean.class);
            m.setAccessible(true);
            var ex = assertThrows(java.lang.reflect.InvocationTargetException.class, () ->
                    m.invoke(client, Boolean.TRUE));
            assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        }

        @Test
        @DisplayName("Override=true does not throw when callFromValidateAttributes=true via reflection")
        void overrideTrue_callFromValidateTrue_noThrow() throws Exception {
            AuthRestClient client = new AuthRestClient(attributes, false, true);
            Method m = AuthRestClient.class.getDeclaredMethod("checkInvokeAsUser", Boolean.class);
            m.setAccessible(true);
            m.invoke(client, Boolean.TRUE); // should not throw
        }

        @Test
        @DisplayName("Override=false does not throw regardless via reflection")
        void overrideFalse_noThrow() throws Exception {
            AuthRestClient client = new AuthRestClient(attributes, true, false);
            Method m = AuthRestClient.class.getDeclaredMethod("checkInvokeAsUser", Boolean.class);
            m.setAccessible(true);
            m.invoke(client, Boolean.FALSE); // override=false means condition is false
        }

        @Test
        @DisplayName("Override=null with field invokeAsUser=false does not throw via reflection")
        void overrideNull_fieldFalse_noThrow() throws Exception {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            Method m = AuthRestClient.class.getDeclaredMethod("checkInvokeAsUser", Boolean.class);
            m.setAccessible(true);
            m.invoke(client, (Boolean) null); // falls back to this.invokeAsUser=false
        }
    }

    // ── getJiraServiceResourceInvoker ────────────────────────────────────

    @Nested
    @DisplayName("getJiraServiceResourceInvoker")
    class GetJiraServiceResourceInvokerTests {

        @Test
        @DisplayName("Returns Invocation.Builder on success")
        void success() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                Invocation.Builder result = client.getJiraServiceResourceInvoker("servicedesk", Map.of());
                assertNotNull(result);
            }
        }

        @Test
        @DisplayName("Applies query parameters")
        void withQueryParams() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            Map<String, Object> params = Map.of("limit", 10);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                Invocation.Builder result = client.getJiraServiceResourceInvoker("servicedesk", params);
                assertNotNull(result);
                verify(mockPathTarget).queryParam("limit", 10);
            }
        }

        @Test
        @DisplayName("Throws when invokeAsUser is true")
        void throwsWhenInvokeAsUserTrue() {
            AuthRestClient client = new AuthRestClient(attributes, true, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.getJiraServiceResourceInvoker("servicedesk", Map.of()));
        }

        @Test
        @DisplayName("Caches webTargetServiceManage on second call")
        void cachesWebTarget() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                client.getJiraServiceResourceInvoker("a", Map.of());
                client.getJiraServiceResourceInvoker("b", Map.of());
                // createTarget → client.target() should only be called once for the service manage target
                verify(mockClient, times(1)).target(anyString());
            }
        }
    }

    // ── fetchMiscellaneousService ────────────────────────────────────────

    @Nested
    @DisplayName("fetchMiscellaneousService")
    class FetchMiscellaneousServiceTests {

        @Test
        @DisplayName("Returns entity on successful response")
        void success() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockSuccessResponse("{\"id\":1}");

                String result = client.fetchMiscellaneousService("queues", Map.of(), String.class);
                assertEquals("{\"id\":1}", result);
            }
        }

        @Test
        @DisplayName("Throws when resource is null")
        void throwsWhenResourceNull() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.fetchMiscellaneousService(null, Map.of(), String.class));
        }

        @Test
        @DisplayName("Throws exception on error response with String entity")
        void errorResponse_stringEntity() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockErrorResponse(404, "Not Found");

                assertThrows(Exception.class, () ->
                        client.fetchMiscellaneousService("queues", Map.of(), String.class));
            }
        }

        @Test
        @DisplayName("Throws exception on error response with non-String entity")
        void errorResponse_nonStringEntity() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                Response.StatusType statusType = mock(Response.StatusType.class);
                when(mockResponse.getStatusInfo()).thenReturn(statusType);
                when(statusType.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
                when(mockResponse.getStatus()).thenReturn(400);
                when(mockResponse.readEntity(Object.class)).thenReturn(12345);

                assertThrows(Exception.class, () ->
                        client.fetchMiscellaneousService("queues", Map.of(), Object.class));
            }
        }

        @Test
        @DisplayName("Throws when invokeAsUser is true")
        void throwsWhenInvokeAsUserTrue() {
            AuthRestClient client = new AuthRestClient(attributes, true, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.fetchMiscellaneousService("queues", Map.of(), String.class));
        }

        @Test
        @DisplayName("Caches webTargetServiceDesk on second call")
        void cachesWebTarget() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockSuccessResponse("ok");

                client.fetchMiscellaneousService("a", Map.of(), String.class);
                client.fetchMiscellaneousService("b", Map.of(), String.class);
                verify(mockClient, times(1)).target(anyString());
            }
        }
    }

    // ── fetchJiraService ────────────────────────────────────────────────

    @Nested
    @DisplayName("fetchJiraService")
    class FetchJiraServiceTests {

        @Test
        @DisplayName("Returns entity on successful response")
        void success() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockSuccessResponse("{\"data\":true}");

                String result = client.fetchJiraService("request", Map.of(), String.class);
                assertEquals("{\"data\":true}", result);
            }
        }

        @Test
        @DisplayName("Throws when resource is null")
        void throwsWhenResourceNull() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.fetchJiraService(null, Map.of(), String.class));
        }

        @Test
        @DisplayName("Throws exception on error response with String entity")
        void errorResponse_stringEntity() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockErrorResponse(401, "Unauthorized");

                assertThrows(Exception.class, () ->
                        client.fetchJiraService("request", Map.of(), String.class));
            }
        }

        @Test
        @DisplayName("Throws exception on error response with non-String entity")
        void errorResponse_nonStringEntity() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                Response.StatusType statusType = mock(Response.StatusType.class);
                when(mockResponse.getStatusInfo()).thenReturn(statusType);
                when(statusType.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
                when(mockResponse.getStatus()).thenReturn(403);
                when(mockResponse.readEntity(Object.class)).thenReturn(List.of("error"));

                assertThrows(Exception.class, () ->
                        client.fetchJiraService("request", Map.of(), Object.class));
            }
        }

        @Test
        @DisplayName("Throws when invokeAsUser is true")
        void throwsWhenInvokeAsUserTrue() {
            AuthRestClient client = new AuthRestClient(attributes, true, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.fetchJiraService("request", Map.of(), String.class));
        }
    }

    // ── parseAndValidateJQL ─────────────────────────────────────────────

    @Nested
    @DisplayName("parseAndValidateJQL")
    class ParseAndValidateJQLTests {

        private MockedStatic<Entity> setupEntityMock() {
            MockedStatic<Entity> entityMock = mockStatic(Entity.class);
            Entity<?> mockEntity = mock(Entity.class);
            entityMock.when(() -> Entity.entity(anyString(), eq(MediaType.APPLICATION_JSON)))
                    .thenReturn(mockEntity);
            return entityMock;
        }

        @Test
        @DisplayName("Returns JSON on successful response")
        void success() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks();
                 MockedStatic<Entity> eMock = setupEntityMock()) {
                when(mockBuilder.post(any())).thenReturn(mockResponse);
                Response.StatusType statusType = mock(Response.StatusType.class);
                when(mockResponse.getStatusInfo()).thenReturn(statusType);
                when(statusType.getFamily()).thenReturn(Response.Status.Family.SUCCESSFUL);
                when(mockResponse.readEntity(String.class)).thenReturn("{\"queries\":[]}");

                String result = client.parseAndValidateJQL("project = TEST", false, "acc", "inv");
                assertEquals("{\"queries\":[]}", result);
            }
        }

        @Test
        @DisplayName("Throws IllegalStateException on 404")
        void throws404() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks();
                 MockedStatic<Entity> eMock = setupEntityMock()) {
                when(mockBuilder.post(any())).thenReturn(mockResponse);
                Response.StatusType statusType = mock(Response.StatusType.class);
                when(mockResponse.getStatusInfo()).thenReturn(statusType);
                when(statusType.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
                when(mockResponse.getStatus()).thenReturn(404);
                when(mockResponse.readEntity(String.class)).thenReturn("Not Found");

                IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                        client.parseAndValidateJQL("project = TEST", false, "acc", "inv"));
                assertEquals("Please Provide Valid Host URL", ex.getMessage());
            }
        }

        @Test
        @DisplayName("Throws IllegalStateException on 400")
        void throws400() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks();
                 MockedStatic<Entity> eMock = setupEntityMock()) {
                when(mockBuilder.post(any())).thenReturn(mockResponse);
                Response.StatusType statusType = mock(Response.StatusType.class);
                when(mockResponse.getStatusInfo()).thenReturn(statusType);
                when(statusType.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
                when(mockResponse.getStatus()).thenReturn(400);
                when(mockResponse.readEntity(String.class)).thenReturn("Bad Request");

                IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                        client.parseAndValidateJQL("project = TEST", false, "acc", "inv"));
                assertEquals("Please Provide Valid Auth Token OR User Name", ex.getMessage());
            }
        }

        @Test
        @DisplayName("Returns empty string on other error status codes")
        void returnsEmptyOnOtherErrors() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks();
                 MockedStatic<Entity> eMock = setupEntityMock()) {
                when(mockBuilder.post(any())).thenReturn(mockResponse);
                Response.StatusType statusType = mock(Response.StatusType.class);
                when(mockResponse.getStatusInfo()).thenReturn(statusType);
                when(statusType.getFamily()).thenReturn(Response.Status.Family.SERVER_ERROR);
                when(mockResponse.getStatus()).thenReturn(500);
                when(mockResponse.readEntity(String.class)).thenReturn("Server Error");

                String result = client.parseAndValidateJQL("project = TEST", false, "acc", "inv");
                assertEquals("", result);
            }
        }

        @Test
        @DisplayName("Throws when invokeAsUser is true")
        void throwsWhenInvokeAsUserTrue() {
            AuthRestClient client = new AuthRestClient(attributes, true, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.parseAndValidateJQL("project = TEST", true, "acc", "inv"));
        }

        @Test
        @DisplayName("Try-with-resources handles null response from post")
        void nullResponseFromPost() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks();
                 MockedStatic<Entity> eMock = setupEntityMock()) {
                when(mockBuilder.post(any())).thenReturn(null);

                assertThrows(NullPointerException.class, () ->
                        client.parseAndValidateJQL("project = TEST", false, "acc", "inv"));
            }
        }

        @Test
        @DisplayName("Try-with-resources suppresses close exception when body throws")
        void closeThrowsDuringExceptionPath() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks();
                 MockedStatic<Entity> eMock = setupEntityMock()) {
                Response spyResponse = mock(Response.class);
                when(mockBuilder.post(any())).thenReturn(spyResponse);
                when(spyResponse.readEntity(String.class)).thenThrow(new RuntimeException("body error"));
                doThrow(new RuntimeException("close error")).when(spyResponse).close();

                RuntimeException ex = assertThrows(RuntimeException.class, () ->
                        client.parseAndValidateJQL("project = TEST", false, "acc", "inv"));
                assertEquals("body error", ex.getMessage());
                assertTrue(ex.getSuppressed().length > 0);
                assertEquals("close error", ex.getSuppressed()[0].getMessage());
            }
        }
    }

    // ── fetchIssues (with invokeAsUser param) ───────────────────────────

    @Nested
    @DisplayName("fetchIssues with invokeAsUser")
    class FetchIssuesWithInvokeAsUserTests {

        @Test
        @DisplayName("Returns entity on successful response")
        void success() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockSuccessResponse("{\"issues\":[]}");

                String result = client.fetchIssues(false, "acc", "inv", "search", Map.of(), String.class);
                assertEquals("{\"issues\":[]}", result);
            }
        }

        @Test
        @DisplayName("Throws IllegalStateException on error response")
        void errorResponse() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockErrorResponse(500, "Internal Server Error");

                IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                        client.fetchIssues(false, "acc", "inv", "search", Map.of(), String.class));
                assertEquals("Internal Server Error", ex.getMessage());
            }
        }

        @Test
        @DisplayName("Throws when invokeAsUser override is true")
        void throwsWhenInvokeAsUserTrue() {
            AuthRestClient client = new AuthRestClient(attributes, true, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.fetchIssues(true, "acc", "inv", "search", Map.of(), String.class));
        }

        @Test
        @DisplayName("Applies query parameters")
        void withQueryParams() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            Map<String, Object> params = new HashMap<>();
            params.put("jql", "project=TEST");
            params.put("maxResults", 50);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockSuccessResponse("{}");

                client.fetchIssues(false, "acc", "inv", "search", params, String.class);
                verify(mockPathTarget).queryParam(eq("jql"), eq("project=TEST"));
                verify(mockPathTarget).queryParam(eq("maxResults"), eq(50));
            }
        }
    }

    // ── fetchIssues (without invokeAsUser param) ────────────────────────

    @Nested
    @DisplayName("fetchIssues without invokeAsUser")
    class FetchIssuesWithoutInvokeAsUserTests {

        @Test
        @DisplayName("Returns entity on successful response")
        void success() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockSuccessResponse("{\"issues\":[]}");

                String result = client.fetchIssues("acc", "inv", "search", Map.of(), String.class);
                assertEquals("{\"issues\":[]}", result);
            }
        }

        @Test
        @DisplayName("Throws IllegalStateException on error response")
        void errorResponse() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockErrorResponse(401, "Unauthorized");

                IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                        client.fetchIssues("acc", "inv", "search", Map.of(), String.class));
                assertEquals("Unauthorized", ex.getMessage());
            }
        }

        @Test
        @DisplayName("Uses null invokeAsUser override (delegates to getWebTarget(null))")
        void usesNullOverride() {
            // invokeAsUser=true on field, override=null → falls back to this.invokeAsUser=true → throws
            AuthRestClient client = new AuthRestClient(attributes, true, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.fetchIssues("acc", "inv", "search", Map.of(), String.class));
        }
    }

    // ── attachFile ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("attachFile")
    class AttachFileTests {

        @Test
        @DisplayName("Throws when resource is null")
        void throwsWhenResourceNull() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.attachFile(null, List.of(new File("test.txt"))));
        }

        @Test
        @DisplayName("Throws when resource is blank")
        void throwsWhenResourceBlank() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.attachFile("   ", List.of(new File("test.txt"))));
        }

        @Test
        @DisplayName("Throws when files is null")
        void throwsWhenFilesNull() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.attachFile("TEST-123", null));
        }

        @Test
        @DisplayName("Throws when files is empty")
        void throwsWhenFilesEmpty() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            assertThrows(IllegalArgumentException.class, () ->
                    client.attachFile("TEST-123", List.of()));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("Constructs correct URL and delegates to uploadMultipart")
        void success_constructsUrlAndDelegates() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            File testFile = new File("test.txt");

            try (MockedStatic<Unirest> unirestMock = mockStatic(Unirest.class)) {
                HttpRequestWithBody mockReqBody = mock(HttpRequestWithBody.class);
                MultipartBody mockMultipart = mock(MultipartBody.class);
                HttpResponse<JsonNode> mockHttpResponse = mock(HttpResponse.class);

                unirestMock.when(() -> Unirest.post(anyString())).thenReturn(mockReqBody);
                when(mockReqBody.basicAuth(anyString(), anyString())).thenReturn(mockReqBody);
                when(mockReqBody.header(anyString(), anyString())).thenReturn(mockReqBody);
                when(mockReqBody.field(eq("file"), anyList())).thenReturn(mockMultipart);
                when(mockMultipart.asJson()).thenReturn(mockHttpResponse);

                HttpResponse<JsonNode> result = client.attachFile("TEST-123", List.of(testFile));
                assertNotNull(result);
            } catch (Exception e) {
                fail("Should not throw: " + e.getMessage());
            }
        }
    }

    // ── WebTarget caching ───────────────────────────────────────────────

    @Nested
    @DisplayName("WebTarget caching")
    class WebTargetCachingTests {

        @Test
        @DisplayName("getJiraServiceAPIWebTarget caches after first call")
        void serviceAPITargetCached() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockSuccessResponse("ok");

                // Two calls to fetchMiscellaneousService use getJiraServiceAPIWebTarget
                client.fetchMiscellaneousService("a", Map.of(), String.class);
                client.fetchMiscellaneousService("b", Map.of(), String.class);

                // target() should only be called once (cached)
                verify(mockClient, times(1)).target(anyString());
            }
        }

        @Test
        @DisplayName("getJiraServiceManageWebTarget caches after first call")
        void serviceManageTargetCached() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                // Two calls to getJiraServiceResourceInvoker use getJiraServiceManageWebTarget
                client.getJiraServiceResourceInvoker("a", Map.of());
                client.getJiraServiceResourceInvoker("b", Map.of());

                verify(mockClient, times(1)).target(anyString());
            }
        }
    }

    // ── entity instanceof String pattern ────────────────────────────────

    @Nested
    @DisplayName("entity instanceof String pattern match")
    class EntityInstanceOfStringTests {

        @Test
        @DisplayName("fetchMiscellaneousService passes String body to error mapper")
        void fetchMiscellaneous_stringBody() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockErrorResponse(404, "Resource not found");

                Exception ex = assertThrows(Exception.class, () ->
                        client.fetchMiscellaneousService("missing", Map.of(), String.class));
                assertNotNull(ex);
            }
        }

        @Test
        @DisplayName("fetchJiraService passes String body to error mapper")
        void fetchJiraService_stringBody() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                mockErrorResponse(401, "Invalid token");

                Exception ex = assertThrows(Exception.class, () ->
                        client.fetchJiraService("missing", Map.of(), String.class));
                assertNotNull(ex);
            }
        }

        @Test
        @DisplayName("fetchMiscellaneousService passes null body when entity is not String")
        void fetchMiscellaneous_nonStringBody() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                Response.StatusType statusType = mock(Response.StatusType.class);
                when(mockResponse.getStatusInfo()).thenReturn(statusType);
                when(statusType.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
                when(mockResponse.getStatus()).thenReturn(400);
                // Return Integer, which is not instanceof String
                when(mockResponse.readEntity(Integer.class)).thenReturn(42);

                assertThrows(Exception.class, () ->
                        client.fetchMiscellaneousService("x", Map.of(), Integer.class));
            }
        }

        @Test
        @DisplayName("fetchJiraService passes null body when entity is not String")
        void fetchJiraService_nonStringBody() {
            AuthRestClient client = new AuthRestClient(attributes, false, false);
            try (MockedStatic<ClientBuilder> cbMock = setupJerseyMocks()) {
                when(mockBuilder.get()).thenReturn(mockResponse);
                Response.StatusType statusType = mock(Response.StatusType.class);
                when(mockResponse.getStatusInfo()).thenReturn(statusType);
                when(statusType.getFamily()).thenReturn(Response.Status.Family.CLIENT_ERROR);
                when(mockResponse.getStatus()).thenReturn(403);
                when(mockResponse.readEntity(Integer.class)).thenReturn(99);

                assertThrows(Exception.class, () ->
                        client.fetchJiraService("x", Map.of(), Integer.class));
            }
        }
    }
}
