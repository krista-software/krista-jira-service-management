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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DatePlus utility class.
 * Tests ISO date formatting functionality.
 */
@DisplayName("DatePlus Integration Tests")
class DatePlusIntegrationTest {

    @Test
    @DisplayName("Should format date to ISO format with UTC timezone")
    void testGetISODate() throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date testDate = inputFormat.parse("2024-01-15 10:30:45");

        String isoDate = DatePlus.getISODate(testDate);

        assertNotNull(isoDate);
        assertTrue(isoDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{4}"));
        assertTrue(isoDate.contains("2024-01-15"));
        assertTrue(isoDate.contains("T"));
    }

    @Test
    @DisplayName("Should format current date to ISO format")
    void testGetISODateWithCurrentDate() {
        Date now = new Date();
        String isoDate = DatePlus.getISODate(now);

        assertNotNull(isoDate);
        assertTrue(isoDate.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{4}"));
    }

    @Test
    @DisplayName("Should format epoch date to ISO format")
    void testGetISODateWithEpoch() {
        Date epoch = new Date(0);
        String isoDate = DatePlus.getISODate(epoch);

        assertNotNull(isoDate);
        assertTrue(isoDate.contains("1970-01-01"));
    }

    @Test
    @DisplayName("Should format future date to ISO format")
    void testGetISODateWithFutureDate() throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date futureDate = inputFormat.parse("2030-12-31 23:59:59");

        String isoDate = DatePlus.getISODate(futureDate);

        assertNotNull(isoDate);
        assertTrue(isoDate.contains("2030-12-31"));
    }

    @Test
    @DisplayName("Should include milliseconds in ISO format")
    void testGetISODateIncludesMilliseconds() {
        Date now = new Date();
        String isoDate = DatePlus.getISODate(now);

        assertTrue(isoDate.contains("."));
        String[] parts = isoDate.split("\\.");
        assertTrue(parts.length >= 2);
    }

    @Test
    @DisplayName("Should include timezone offset in ISO format")
    void testGetISODateIncludesTimezone() {
        Date now = new Date();
        String isoDate = DatePlus.getISODate(now);

        assertTrue(isoDate.matches(".*[+-]\\d{4}$"));
    }
}