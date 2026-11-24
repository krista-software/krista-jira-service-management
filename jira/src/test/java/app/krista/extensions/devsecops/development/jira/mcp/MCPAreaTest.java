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

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.Field;
import app.krista.sdk.atlassian.mcp.AbstractMcpArea;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MCPArea")
class MCPAreaTest {

    @Mock
    private JsmMcpQueryService mcpQueryService;

    private MCPArea mcpArea;

    @BeforeEach
    void setUp() {
        mcpArea = new MCPArea(mcpQueryService);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for class hierarchy and structure
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("class hierarchy")
    class ClassHierarchyTests {

        @Test
        @DisplayName("should extend AbstractMcpArea")
        void shouldExtendAbstractMcpArea() {
            assertTrue(AbstractMcpArea.class.isAssignableFrom(MCPArea.class),
                    "MCPArea should extend AbstractMcpArea");
        }

        @Test
        @DisplayName("should be an instance of AbstractMcpArea")
        void shouldBeInstanceOfAbstractMcpArea() {
            assertTrue(mcpArea instanceof AbstractMcpArea,
                    "MCPArea instance should be an instance of AbstractMcpArea");
        }

        @Test
        @DisplayName("should have a public askAgent method accepting String")
        void shouldHaveAskAgentMethod() throws NoSuchMethodException {
            Method method = MCPArea.class.getMethod("askAgent", String.class);

            assertNotNull(method);
            assertEquals(ExtensionResponse.class, method.getReturnType());
        }

        @Test
        @DisplayName("should have a single-parameter constructor accepting JsmMcpQueryService")
        void shouldHaveCorrectConstructor() throws NoSuchMethodException {
            var constructor = MCPArea.class.getConstructor(JsmMcpQueryService.class);

            assertNotNull(constructor);
            assertEquals(1, constructor.getParameterCount());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for askAgent method-level annotations (runtime-retained)
    // Note: @Domain and @CatalogRequest have SOURCE retention and are NOT
    // available at runtime via reflection. Only annotations with RUNTIME
    // retention (@Field, @Field.Desc, @Attribute, @Inject) can be tested here.
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("askAgent method annotations (runtime-retained)")
    class AskAgentMethodAnnotationTests {

        private Method getAskAgentMethod() throws NoSuchMethodException {
            return MCPArea.class.getMethod("askAgent", String.class);
        }

        @Test
        @DisplayName("askAgent should have @Field.Desc annotation for response")
        void shouldHaveFieldDescAnnotation() throws NoSuchMethodException {
            Method askAgentMethod = getAskAgentMethod();
            Field.Desc fieldDesc = askAgentMethod.getAnnotation(Field.Desc.class);

            assertNotNull(fieldDesc, "askAgent should have @Field.Desc annotation");
        }

        @Test
        @DisplayName("@Field.Desc should have name 'Response'")
        void shouldHaveResponseFieldDescName() throws NoSuchMethodException {
            Method askAgentMethod = getAskAgentMethod();
            Field.Desc fieldDesc = askAgentMethod.getAnnotation(Field.Desc.class);

            assertEquals("Response", fieldDesc.name());
        }

        @Test
        @DisplayName("@Field.Desc should have type '[ FreeForm ]'")
        void shouldHaveFreeFormFieldDescType() throws NoSuchMethodException {
            Method askAgentMethod = getAskAgentMethod();
            Field.Desc fieldDesc = askAgentMethod.getAnnotation(Field.Desc.class);

            assertEquals("[ FreeForm ]", fieldDesc.type());
        }

        @Test
        @DisplayName("@Field.Desc should not be required")
        void shouldNotBeRequiredFieldDesc() throws NoSuchMethodException {
            Method askAgentMethod = getAskAgentMethod();
            Field.Desc fieldDesc = askAgentMethod.getAnnotation(Field.Desc.class);

            assertFalse(fieldDesc.required(), "Response field should not be required");
        }

        @Test
        @DisplayName("@Field.Desc should have a description")
        void shouldHaveFieldDescDescription() throws NoSuchMethodException {
            Method askAgentMethod = getAskAgentMethod();
            Field.Desc fieldDesc = askAgentMethod.getAnnotation(Field.Desc.class);

            assertNotNull(fieldDesc.description());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for askAgent parameter annotations (runtime-retained)
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("askAgent parameter annotations")
    class AskAgentParameterAnnotationTests {

        private Parameter getQueryParameter() throws NoSuchMethodException {
            Method askAgentMethod = MCPArea.class.getMethod("askAgent", String.class);
            return askAgentMethod.getParameters()[0];
        }

        @Test
        @DisplayName("query parameter should have @Field annotation")
        void shouldHaveFieldAnnotationOnQueryParam() throws NoSuchMethodException {
            Parameter queryParam = getQueryParameter();
            Field fieldAnnotation = queryParam.getAnnotation(Field.class);

            assertNotNull(fieldAnnotation, "Query parameter should have @Field annotation");
        }

        @Test
        @DisplayName("query @Field should have name 'Query'")
        void shouldHaveCorrectFieldName() throws NoSuchMethodException {
            Parameter queryParam = getQueryParameter();
            Field fieldAnnotation = queryParam.getAnnotation(Field.class);

            assertEquals("Query", fieldAnnotation.name());
        }

        @Test
        @DisplayName("query @Field should have type 'Paragraph'")
        void shouldHaveCorrectFieldType() throws NoSuchMethodException {
            Parameter queryParam = getQueryParameter();
            Field fieldAnnotation = queryParam.getAnnotation(Field.class);

            assertEquals("Paragraph", fieldAnnotation.type());
        }

        @Test
        @DisplayName("query @Field should be required")
        void shouldBeRequired() throws NoSuchMethodException {
            Parameter queryParam = getQueryParameter();
            Field fieldAnnotation = queryParam.getAnnotation(Field.class);

            assertTrue(fieldAnnotation.required());
        }

        @Test
        @DisplayName("query @Field should have empty description")
        void shouldHaveEmptyDescription() throws NoSuchMethodException {
            Parameter queryParam = getQueryParameter();
            Field fieldAnnotation = queryParam.getAnnotation(Field.class);

            assertEquals("", fieldAnnotation.description());
        }

        @Test
        @DisplayName("query @Field should have visualWidth attribute set to 'L'")
        void shouldHaveVisualWidthAttribute() throws NoSuchMethodException {
            Parameter queryParam = getQueryParameter();
            Field fieldAnnotation = queryParam.getAnnotation(Field.class);

            Attribute[] attributes = fieldAnnotation.attributes();
            assertNotNull(attributes);
            assertTrue(attributes.length > 0, "Field should have at least one attribute");

            boolean hasVisualWidth = false;
            for (Attribute attr : attributes) {
                if ("visualWidth".equals(attr.name())) {
                    assertEquals("L", attr.value());
                    hasVisualWidth = true;
                }
            }
            assertTrue(hasVisualWidth, "Field should have a visualWidth attribute");
        }

        @Test
        @DisplayName("query @Field should have empty options array")
        void shouldHaveEmptyOptionsArray() throws NoSuchMethodException {
            Parameter queryParam = getQueryParameter();
            Field fieldAnnotation = queryParam.getAnnotation(Field.class);

            assertNotNull(fieldAnnotation.options());
            assertEquals(0, fieldAnnotation.options().length,
                    "Query field should have no options");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for askAgent method behavior
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("askAgent behavior")
    class AskAgentBehaviorTests {

        @Test
        @DisplayName("should delegate to mcpQueryService via super.askAgent")
        void shouldDelegateToMcpQueryService() {
            ExtensionResponse mockResponse = mock(ExtensionResponse.class);
            when(mcpQueryService.query("test query")).thenReturn(mockResponse);

            ExtensionResponse result = mcpArea.askAgent("test query");

            assertSame(mockResponse, result);
            verify(mcpQueryService).query("test query");
        }

        @Test
        @DisplayName("should pass query string to mcpQueryService")
        void shouldPassQueryToService() {
            String query = "Find all open incidents in JSM";
            when(mcpQueryService.query(query)).thenReturn(mock(ExtensionResponse.class));

            mcpArea.askAgent(query);

            verify(mcpQueryService).query(query);
        }

        @Test
        @DisplayName("should call mcpQueryService exactly once")
        void shouldCallServiceExactlyOnce() {
            when(mcpQueryService.query(anyString())).thenReturn(mock(ExtensionResponse.class));

            mcpArea.askAgent("any query");

            verify(mcpQueryService, times(1)).query(anyString());
        }

        @Test
        @DisplayName("should return the response from mcpQueryService")
        void shouldReturnServiceResponse() {
            ExtensionResponse expected = mock(ExtensionResponse.class);
            when(mcpQueryService.query("query")).thenReturn(expected);

            ExtensionResponse actual = mcpArea.askAgent("query");

            assertSame(expected, actual);
        }

        @Test
        @DisplayName("should handle empty query string")
        void shouldHandleEmptyQuery() {
            ExtensionResponse mockResponse = mock(ExtensionResponse.class);
            when(mcpQueryService.query("")).thenReturn(mockResponse);

            ExtensionResponse result = mcpArea.askAgent("");

            assertNotNull(result);
            verify(mcpQueryService).query("");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Tests for constructor
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("constructor")
    class ConstructorTests {

        @Test
        @DisplayName("should create instance with injected JsmMcpQueryService")
        void shouldCreateInstanceWithService() {
            MCPArea area = new MCPArea(mcpQueryService);

            assertNotNull(area);
        }

        @Test
        @DisplayName("constructor should have @Inject annotation")
        void shouldHaveInjectAnnotation() throws NoSuchMethodException {
            var constructor = MCPArea.class.getConstructor(JsmMcpQueryService.class);
            javax.inject.Inject inject = constructor.getAnnotation(javax.inject.Inject.class);

            assertNotNull(inject, "Constructor should be annotated with @Inject");
        }

        @Test
        @DisplayName("constructor should accept exactly one parameter")
        void shouldAcceptOneParameter() throws NoSuchMethodException {
            var constructor = MCPArea.class.getConstructor(JsmMcpQueryService.class);

            assertEquals(1, constructor.getParameterCount());
        }

        @Test
        @DisplayName("constructor parameter should be JsmMcpQueryService type")
        void shouldAcceptCorrectParameterType() throws NoSuchMethodException {
            var constructor = MCPArea.class.getConstructor(JsmMcpQueryService.class);

            assertEquals(JsmMcpQueryService.class, constructor.getParameterTypes()[0]);
        }
    }
}
