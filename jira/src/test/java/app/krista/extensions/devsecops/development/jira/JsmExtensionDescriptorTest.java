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

import app.krista.sdk.atlassian.registry.AtlassianExtensionDescriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JsmExtensionDescriptor}.
 */
@DisplayName("JsmExtensionDescriptor Tests")
class JsmExtensionDescriptorTest {

    private final JsmExtensionDescriptor descriptor = new JsmExtensionDescriptor();

    @Test
    @DisplayName("getConfigClass returns JsmExtensionConfig.class")
    void getConfigClass_returnsJsmExtensionConfigClass() {
        assertEquals(JsmExtensionConfig.class, descriptor.getConfigClass());
    }

    @Test
    @DisplayName("JsmExtensionDescriptor implements AtlassianExtensionDescriptor")
    void descriptor_implementsAtlassianExtensionDescriptor() {
        assertInstanceOf(AtlassianExtensionDescriptor.class, descriptor);
    }
}
