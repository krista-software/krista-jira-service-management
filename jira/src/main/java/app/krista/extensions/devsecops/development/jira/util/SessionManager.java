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
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

@Service
public final class SessionManager {

    private final KeyValueStore keyValueStore;

    @Inject
    public SessionManager(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    public void storeRefToken(String key, String refToken) {
        keyValueStore.put(key, refToken);
    }

    public String getRefToken(String refTokenKeyWord) {
        return keyValueStore.get(refTokenKeyWord, String.class);
    }

}
