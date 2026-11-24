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

package app.krista.extensions.devsecops.development.jira.api;

import app.krista.extension.executor.Invoker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ListenerResource Integration Tests")
class ListenerResourceIntegrationTest {

    private ListenerResource listenerResource;
    private Invoker invoker;

    @BeforeEach
    void setUp() {
        invoker = mock(Invoker.class);
        listenerResource = new ListenerResource(invoker);
    }

    @Test
    @DisplayName("Should have invoker field")
    void testHasInvokerField() throws NoSuchFieldException {
        // Assert
        var field = listenerResource.getClass().getDeclaredField("invoker");
        assertNotNull(field);
        assertEquals(Invoker.class, field.getType());
    }

    @Test
    @DisplayName("Should have correct Path annotation")
    void testPathAnnotation() {
        // Assert
        assertTrue(listenerResource.getClass().isAnnotationPresent(javax.ws.rs.Path.class));
        javax.ws.rs.Path annotation = listenerResource.getClass().getAnnotation(javax.ws.rs.Path.class);
        assertEquals("listeners", annotation.value());
    }

    @Test
    @DisplayName("Should have GET method annotation on getListeners")
    void testGetListenersAnnotation() throws NoSuchMethodException {
        // Assert
        assertTrue(listenerResource.getClass().getMethod("getListeners")
                .isAnnotationPresent(javax.ws.rs.GET.class));
        assertTrue(listenerResource.getClass().getMethod("getListeners")
                .isAnnotationPresent(javax.ws.rs.Produces.class));
    }

    @Test
    @DisplayName("Should have DELETE method annotation on clearListeners")
    void testClearListenersAnnotation() throws NoSuchMethodException {
        // Assert
        assertTrue(listenerResource.getClass().getMethod("clearListeners")
                .isAnnotationPresent(javax.ws.rs.DELETE.class));
        assertTrue(listenerResource.getClass().getMethod("clearListeners")
                .isAnnotationPresent(javax.ws.rs.Produces.class));
    }

    @Test
    @DisplayName("Should have correct constructor with Named annotation")
    void testConstructorAnnotation() throws NoSuchMethodException {
        // Assert
        var constructor = listenerResource.getClass().getConstructor(Invoker.class);
        assertTrue(constructor.isAnnotationPresent(javax.inject.Inject.class));
        var params = constructor.getParameters();
        assertEquals(1, params.length);
        assertTrue(params[0].isAnnotationPresent(javax.inject.Named.class));
        assertEquals("self", params[0].getAnnotation(javax.inject.Named.class).value());
    }
}