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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JiraApplication Integration Tests")
class JiraApplicationIntegrationTest {

    private JiraApplication jiraApplication;

    @BeforeEach
    void setUp() {
        jiraApplication = new JiraApplication();
    }

    @Test
    @DisplayName("Should return correct set of resource classes")
    void testGetClasses() {
        // Act
        Set<Class<?>> classes = jiraApplication.getClasses();

        // Assert
        assertNotNull(classes);
        assertEquals(2, classes.size());
        assertTrue(classes.contains(AuthenticationResource.class));
        assertTrue(classes.contains(ListenerResource.class));
    }

    @Test
    @DisplayName("Should return immutable set of classes")
    void testGetClasses_Immutable() {
        // Act
        Set<Class<?>> classes = jiraApplication.getClasses();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> 
            classes.add(Object.class)
        );
    }

    @Test
    @DisplayName("Should return same classes on multiple calls")
    void testGetClasses_Consistency() {
        // Act
        Set<Class<?>> classes1 = jiraApplication.getClasses();
        Set<Class<?>> classes2 = jiraApplication.getClasses();

        // Assert
        assertEquals(classes1, classes2);
    }

    @Test
    @DisplayName("Should not return null classes")
    void testGetClasses_NotNull() {
        // Act
        Set<Class<?>> classes = jiraApplication.getClasses();

        // Assert
        assertNotNull(classes);
        assertFalse(classes.isEmpty());
    }

    @Test
    @DisplayName("Should only contain JAX-RS resource classes")
    void testGetClasses_OnlyResourceClasses() {
        // Act
        Set<Class<?>> classes = jiraApplication.getClasses();

        // Assert
        for (Class<?> clazz : classes) {
            assertTrue(
                clazz.isAnnotationPresent(javax.ws.rs.Path.class),
                "Class " + clazz.getName() + " should have @Path annotation"
            );
        }
    }

    @Test
    @DisplayName("Should be instance of Application")
    void testInstanceOfApplication() {
        // Assert
        assertTrue(jiraApplication instanceof javax.ws.rs.core.Application);
    }

    @Test
    @DisplayName("Should have ApplicationPath annotation")
    void testApplicationPathAnnotation() {
        // Assert
        assertTrue(jiraApplication.getClass().isAnnotationPresent(javax.ws.rs.ApplicationPath.class));
        javax.ws.rs.ApplicationPath annotation = jiraApplication.getClass().getAnnotation(javax.ws.rs.ApplicationPath.class);
        assertEquals("/", annotation.value());
    }

    @Test
    @DisplayName("Should have Service annotation")
    void testServiceAnnotation() {
        // Assert
        assertTrue(jiraApplication.getClass().isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }

    @Test
    @DisplayName("Should have ContractsProvided annotation")
    void testContractsProvidedAnnotation() {
        // Assert
        assertTrue(jiraApplication.getClass().isAnnotationPresent(org.jvnet.hk2.annotations.ContractsProvided.class));
        org.jvnet.hk2.annotations.ContractsProvided annotation = 
            jiraApplication.getClass().getAnnotation(org.jvnet.hk2.annotations.ContractsProvided.class);
        assertEquals(1, annotation.value().length);
        assertEquals(javax.ws.rs.core.Application.class, annotation.value()[0]);
    }

    @Test
    @DisplayName("Should be final class")
    void testFinalClass() {
        // Assert
        assertTrue(java.lang.reflect.Modifier.isFinal(jiraApplication.getClass().getModifiers()));
    }

    @Test
    @DisplayName("Should extend Application class")
    void testExtendsApplication() {
        // Assert
        assertEquals(javax.ws.rs.core.Application.class, jiraApplication.getClass().getSuperclass());
    }
}