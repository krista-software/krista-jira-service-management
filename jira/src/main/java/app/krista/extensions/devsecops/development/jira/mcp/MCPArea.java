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
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.sdk.atlassian.mcp.AbstractMcpArea;

import javax.inject.Inject;

/**
 * JSM catalog entry point for the MCP "Ask Agent" natural language query.
 */
@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "e043a8e4-1828-4a85-9b42-781835e61e80")
public class MCPArea extends AbstractMcpArea {

    @Inject
    public MCPArea(JsmMcpQueryService mcpQueryService) {
        super(mcpQueryService);
    }

    @Override
    @CatalogRequest(
            id = CATALOG_REQUEST_ID,
            name = CATALOG_REQUEST_NAME,
            description = CATALOG_REQUEST_DESCRIPTION,
            area = CATALOG_REQUEST_AREA,
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field.Desc(name = "Response", type = "[ FreeForm ]", required = false,
            description = "Results matching the query")
    public ExtensionResponse askAgent(
            @Field(name = "Query", type = "Paragraph", required = true, description = "",
                    attributes = {@Attribute(name = "visualWidth", value = "L")},
                    options = {}) String query) {
        return super.askAgent(query);
    }
}
