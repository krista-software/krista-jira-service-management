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

package app.krista.extensions.devsecops.development.jira.util;

import app.krista.extensions.util.KeyValueStore;
import app.krista.sdk.atlassian.oauth.KeyValueRefreshTokenStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

/**
 * HK2 service wrapper around the SDK's {@link KeyValueRefreshTokenStore}.
 * Existing injection points and the legacy {@code storeRefToken}/{@code getRefToken}
 * methods keep working through the inherited aliases.
 */
@Service
public final class SessionManager extends KeyValueRefreshTokenStore {

    @Inject
    public SessionManager(KeyValueStore keyValueStore) {
        super(keyValueStore);
    }
}
