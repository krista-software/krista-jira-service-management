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

package app.krista.extensions.devsecops.development.jira.mcp;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.sdk.atlassian.mcp.McpProductConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hk2.annotations.Service;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for {@link JsmMcpQueryService}.
 *
 * <p>Tests the constructor wiring, protected method overrides (via reflection),
 * and annotation presence ({@code @Service}).</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JsmMcpQueryService Tests")
class JsmMcpQueryServiceTest {

    @Mock
    private SessionManager sessionManager;

    @Mock
    private Invoker invoker;

    @Mock
    private JiraAttributes jiraAttributes;

    @Mock
    private JiraService jiraService;

    private JsmMcpQueryService service;

    @BeforeEach
    void setUp() {
        service = new JsmMcpQueryService(
                sessionManager,
                invoker,
                jiraAttributes,
                jiraService
        );
    }

    // ── Helper to invoke protected methods via reflection ──

    private Object invokeMethod(String methodName) throws Exception {
        Method method = JsmMcpQueryService.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method.invoke(service);
    }

    // ── Constructor tests ──

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Should construct successfully with valid dependencies")
        void shouldConstructSuccessfully() {
            assertNotNull(service);
        }

        @Test
        @DisplayName("Should be an instance of AbstractMcpQueryService")
        void shouldExtendAbstractMcpQueryService() {
            assertInstanceOf(
                    app.krista.sdk.atlassian.mcp.AbstractMcpQueryService.class,
                    service
            );
        }
    }

    // ── getProductConfig tests ──

    @Nested
    @DisplayName("getProductConfig()")
    class GetProductConfigTests {

        @Test
        @DisplayName("Should return a JsmMcpProductConfig instance")
        void shouldReturnJsmMcpProductConfig() throws Exception {
            McpProductConfig config = (McpProductConfig) invokeMethod("getProductConfig");

            assertNotNull(config);
            assertInstanceOf(JsmMcpProductConfig.class, config);
        }

        @Test
        @DisplayName("Should return a new instance each call")
        void shouldReturnNewInstanceEachCall() throws Exception {
            McpProductConfig config1 = (McpProductConfig) invokeMethod("getProductConfig");
            McpProductConfig config2 = (McpProductConfig) invokeMethod("getProductConfig");

            assertNotNull(config1);
            assertNotNull(config2);
            assertNotSame(config1, config2);
        }

        @Test
        @DisplayName("Returned config should have productName 'Jira Service Management'")
        void returnedConfigShouldHaveCorrectProductName() throws Exception {
            McpProductConfig config = (McpProductConfig) invokeMethod("getProductConfig");

            assertEquals("Jira Service Management", config.productName());
        }

        @Test
        @DisplayName("Returned config should have queryLanguage 'JQL'")
        void returnedConfigShouldHaveCorrectQueryLanguage() throws Exception {
            McpProductConfig config = (McpProductConfig) invokeMethod("getProductConfig");

            assertEquals("JQL", config.queryLanguage());
        }

        @Test
        @DisplayName("Returned config should have correct full query language name")
        void returnedConfigShouldHaveCorrectQueryLanguageFullName() throws Exception {
            McpProductConfig config = (McpProductConfig) invokeMethod("getProductConfig");

            assertEquals("JQL (Jira Query Language)", config.queryLanguageFullName());
        }

        @Test
        @DisplayName("Returned config should use POST search HTTP method")
        void returnedConfigShouldUsePostSearchMethod() throws Exception {
            McpProductConfig config = (McpProductConfig) invokeMethod("getProductConfig");

            assertEquals("POST", config.searchHttpMethod());
        }
    }

    // ── getJaxrsId tests ──

    @Nested
    @DisplayName("getJaxrsId()")
    class GetJaxrsIdTests {

        @Test
        @DisplayName("Should return JiraConstants.JAXRS_ID")
        void shouldReturnJaxrsIdConstant() throws Exception {
            String jaxrsId = (String) invokeMethod("getJaxrsId");

            assertEquals(JiraConstants.JAXRS_ID, jaxrsId);
        }

        @Test
        @DisplayName("Should return 'jiraextn'")
        void shouldReturnJiraExtn() throws Exception {
            String jaxrsId = (String) invokeMethod("getJaxrsId");

            assertEquals("jiraextn", jaxrsId);
        }
    }

    // ── Annotation tests ──

    @Nested
    @DisplayName("Annotations")
    class AnnotationTests {

        @Test
        @DisplayName("Should have @Service annotation")
        void shouldHaveServiceAnnotation() {
            Service serviceAnnotation = JsmMcpQueryService.class.getAnnotation(Service.class);

            assertNotNull(serviceAnnotation,
                    "JsmMcpQueryService should be annotated with @Service");
        }

        @Test
        @DisplayName("Constructor should have @Inject annotation")
        void constructorShouldHaveInjectAnnotation() throws Exception {
            var constructor = JsmMcpQueryService.class.getDeclaredConstructor(
                    SessionManager.class,
                    Invoker.class,
                    JiraAttributes.class,
                    JiraService.class
            );
            assertNotNull(constructor.getAnnotation(javax.inject.Inject.class));
        }

        @Test
        @DisplayName("Should NOT have @Path annotation (it is a service, not a resource)")
        void shouldNotHavePathAnnotation() {
            javax.ws.rs.Path pathAnnotation = JsmMcpQueryService.class
                    .getAnnotation(javax.ws.rs.Path.class);

            assertNull(pathAnnotation,
                    "JsmMcpQueryService should NOT have @Path annotation");
        }
    }

    // ── Consistency tests ──

    @Nested
    @DisplayName("Consistency with JsmMcpSetupResource")
    class ConsistencyTests {

        @Test
        @DisplayName("Should return same JAXRS_ID as JsmMcpSetupResource")
        void shouldReturnSameJaxrsIdAsSetupResource() throws Exception {
            JsmMcpSetupResource setupResource = new JsmMcpSetupResource(
                    sessionManager, invoker, jiraAttributes, jiraService);

            Method setupGetJaxrsId = JsmMcpSetupResource.class.getDeclaredMethod("getJaxrsId");
            setupGetJaxrsId.setAccessible(true);
            String setupJaxrsId = (String) setupGetJaxrsId.invoke(setupResource);

            String queryServiceJaxrsId = (String) invokeMethod("getJaxrsId");

            assertEquals(setupJaxrsId, queryServiceJaxrsId,
                    "Both MCP classes should use the same JAXRS_ID");
        }

        @Test
        @DisplayName("Should return same product config type as JsmMcpSetupResource")
        void shouldReturnSameProductConfigType() throws Exception {
            JsmMcpSetupResource setupResource = new JsmMcpSetupResource(
                    sessionManager, invoker, jiraAttributes, jiraService);

            Method setupGetConfig = JsmMcpSetupResource.class.getDeclaredMethod("getProductConfig");
            setupGetConfig.setAccessible(true);
            McpProductConfig setupConfig = (McpProductConfig) setupGetConfig.invoke(setupResource);

            McpProductConfig queryConfig = (McpProductConfig) invokeMethod("getProductConfig");

            assertEquals(setupConfig.getClass(), queryConfig.getClass(),
                    "Both MCP classes should use the same product config class");
        }
    }
}
