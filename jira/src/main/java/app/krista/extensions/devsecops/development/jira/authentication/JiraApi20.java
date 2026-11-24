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

import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;

public final class JiraApi20 extends DefaultApi20 {

    private static class InstanceHolder {
        private static final JiraApi20 INSTANCE = new JiraApi20();
    }

    public static JiraApi20 instance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String getAccessTokenEndpoint() {
        return "https://auth.atlassian.com/oauth/token";
    }

    @Override
    protected String getAuthorizationBaseUrl() {
        return "https://auth.atlassian.com/authorize";
    }

    @Override
    public OAuth2AccessTokenJsonExtractor getAccessTokenExtractor() {
        return OAuth2AccessTokenJsonExtractor.instance();
    }
}
