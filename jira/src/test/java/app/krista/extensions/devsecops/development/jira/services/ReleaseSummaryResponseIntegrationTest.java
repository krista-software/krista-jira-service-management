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

package app.krista.extensions.devsecops.development.jira.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReleaseSummaryResponse Integration Tests")
class ReleaseSummaryResponseIntegrationTest {

    private ReleaseSummaryResponse response;

    @BeforeEach
    void setUp() {
        response = new ReleaseSummaryResponse();
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create empty ReleaseSummaryResponse")
    void testConstructor_Empty() {
        ReleaseSummaryResponse newResponse = new ReleaseSummaryResponse();
        
        assertNotNull(newResponse);
        assertTrue(newResponse.isEmpty());
        assertEquals(0, newResponse.size());
    }

    @Test
    @DisplayName("Should verify ReleaseSummaryResponse extends HashMap")
    void testExtendsHashMap() {
        assertTrue(response instanceof java.util.HashMap);
    }

    @Test
    @DisplayName("Should verify ReleaseSummaryResponse is final class")
    void testReleaseSummaryResponseIsFinal() {
        assertTrue(java.lang.reflect.Modifier.isFinal(ReleaseSummaryResponse.class.getModifiers()));
    }

    // ==================== Constant Tests ====================

    @Test
    @DisplayName("Should verify all constant keys are correctly defined")
    void testConstants() {
        assertEquals("Total user stories", ReleaseSummaryResponse.totalUserStoriesKey);
        assertEquals("Open user stories", ReleaseSummaryResponse.openUserStoriesKey);
        assertEquals("Total epics", ReleaseSummaryResponse.totalEpicsKey);
        assertEquals("Total Tasks", ReleaseSummaryResponse.totalTasksKey);
        assertEquals("Open Tasks", ReleaseSummaryResponse.openTasksKey);
        assertEquals("Total Bugs", ReleaseSummaryResponse.totalBugsKey);
        assertEquals("Open Bugs", ReleaseSummaryResponse.openBugsKey);
        assertEquals("Release day", ReleaseSummaryResponse.releaseDayKey);
        assertEquals("Days remaining", ReleaseSummaryResponse.daysRemainingKey);
        assertEquals("User stories", ReleaseSummaryResponse.userStoriesKey);
        assertEquals("Epics", ReleaseSummaryResponse.epicsKey);
        assertEquals("Bugs", ReleaseSummaryResponse.bugsKey);
        assertEquals("Tasks", ReleaseSummaryResponse.tasksKey);
    }

    // ==================== Total User Stories Tests ====================

    @Test
    @DisplayName("Should set and get total user stories")
    void testTotalUserStories_SetAndGet() {
        response.setTotalUserStories(10.0);
        
        assertEquals(10.0, response.getTotalUserStories());
        assertEquals(10.0, response.get(ReleaseSummaryResponse.totalUserStoriesKey));
    }

    @Test
    @DisplayName("Should return null when total user stories not set")
    void testTotalUserStories_NotSet() {
        assertNull(response.getTotalUserStories());
    }

    @Test
    @DisplayName("Should set total user stories to zero")
    void testTotalUserStories_Zero() {
        response.setTotalUserStories(0.0);
        
        assertEquals(0.0, response.getTotalUserStories());
    }

    @Test
    @DisplayName("Should set total user stories to large value")
    void testTotalUserStories_LargeValue() {
        response.setTotalUserStories(999999.0);
        
        assertEquals(999999.0, response.getTotalUserStories());
    }

    @Test
    @DisplayName("Should set total user stories to decimal value")
    void testTotalUserStories_DecimalValue() {
        response.setTotalUserStories(10.5);
        
        assertEquals(10.5, response.getTotalUserStories());
    }

    @Test
    @DisplayName("Should set total user stories to null")
    void testTotalUserStories_Null() {
        response.setTotalUserStories(10.0);
        response.setTotalUserStories(null);
        
        assertNull(response.getTotalUserStories());
    }

    // ==================== Open User Stories Tests ====================

    @Test
    @DisplayName("Should set and get open user stories")
    void testOpenUserStories_SetAndGet() {
        response.setOpenUserStories(5.0);
        
        assertEquals(5.0, response.getOpenUserStories());
        assertEquals(5.0, response.get(ReleaseSummaryResponse.openUserStoriesKey));
    }

    @Test
    @DisplayName("Should return null when open user stories not set")
    void testOpenUserStories_NotSet() {
        assertNull(response.getOpenUserStories());
    }

    @Test
    @DisplayName("Should set open user stories to zero")
    void testOpenUserStories_Zero() {
        response.setOpenUserStories(0.0);
        
        assertEquals(0.0, response.getOpenUserStories());
    }

    @Test
    @DisplayName("Should set open user stories to null")
    void testOpenUserStories_Null() {
        response.setOpenUserStories(5.0);
        response.setOpenUserStories(null);
        
        assertNull(response.getOpenUserStories());
    }

    // ==================== Total Epics Tests ====================

    @Test
    @DisplayName("Should set and get total epics")
    void testTotalEpics_SetAndGet() {
        response.setTotalEpics(3.0);
        
        assertEquals(3.0, response.getTotalEpics());
        assertEquals(3.0, response.get(ReleaseSummaryResponse.totalEpicsKey));
    }

    @Test
    @DisplayName("Should return null when total epics not set")
    void testTotalEpics_NotSet() {
        assertNull(response.getTotalEpics());
    }

    @Test
    @DisplayName("Should set total epics to null")
    void testTotalEpics_Null() {
        response.setTotalEpics(3.0);
        response.setTotalEpics(null);

        assertNull(response.getTotalEpics());
    }

    // ==================== Total Tasks Tests ====================

    @Test
    @DisplayName("Should set and get total tasks")
    void testTotalTasks_SetAndGet() {
        response.setTotalTasks(20.0);

        assertEquals(20.0, response.getTotalTasks());
        assertEquals(20.0, response.get(ReleaseSummaryResponse.totalTasksKey));
    }

    @Test
    @DisplayName("Should return null when total tasks not set")
    void testTotalTasks_NotSet() {
        assertNull(response.getTotalTasks());
    }

    @Test
    @DisplayName("Should set total tasks to null")
    void testTotalTasks_Null() {
        response.setTotalTasks(20.0);
        response.setTotalTasks(null);

        assertNull(response.getTotalTasks());
    }

    // ==================== Open Tasks Tests ====================

    @Test
    @DisplayName("Should set and get open tasks")
    void testOpenTasks_SetAndGet() {
        response.setOpenTasks(8.0);

        assertEquals(8.0, response.getOpenTasks());
        assertEquals(8.0, response.get(ReleaseSummaryResponse.openTasksKey));
    }

    @Test
    @DisplayName("Should return null when open tasks not set")
    void testOpenTasks_NotSet() {
        assertNull(response.getOpenTasks());
    }

    @Test
    @DisplayName("Should set open tasks to null")
    void testOpenTasks_Null() {
        response.setOpenTasks(8.0);
        response.setOpenTasks(null);

        assertNull(response.getOpenTasks());
    }

    // ==================== Total Bugs Tests ====================

    @Test
    @DisplayName("Should set and get total bugs")
    void testTotalBugs_SetAndGet() {
        response.setTotalBugs(15.0);

        assertEquals(15.0, response.getTotalBugs());
        assertEquals(15.0, response.get(ReleaseSummaryResponse.totalBugsKey));
    }

    @Test
    @DisplayName("Should return null when total bugs not set")
    void testTotalBugs_NotSet() {
        assertNull(response.getTotalBugs());
    }

    @Test
    @DisplayName("Should set total bugs to null")
    void testTotalBugs_Null() {
        response.setTotalBugs(15.0);
        response.setTotalBugs(null);

        assertNull(response.getTotalBugs());
    }

    // ==================== Open Bugs Tests ====================

    @Test
    @DisplayName("Should set and get open bugs")
    void testOpenBugs_SetAndGet() {
        response.setOpenBugs(7.0);

        assertEquals(7.0, response.getOpenBugs());
        assertEquals(7.0, response.get(ReleaseSummaryResponse.openBugsKey));
    }

    @Test
    @DisplayName("Should return null when open bugs not set")
    void testOpenBugs_NotSet() {
        assertNull(response.getOpenBugs());
    }

    @Test
    @DisplayName("Should set open bugs to null")
    void testOpenBugs_Null() {
        response.setOpenBugs(7.0);
        response.setOpenBugs(null);

        assertNull(response.getOpenBugs());
    }

    // ==================== Release Day Tests ====================

    @Test
    @DisplayName("Should set and get release day")
    void testReleaseDay_SetAndGet() {
        Long releaseDay = 1640995200000L; // Unix timestamp
        response.setReleaseDay(releaseDay);

        assertEquals(releaseDay, response.getReleaseDay());
        assertEquals(releaseDay, response.get(ReleaseSummaryResponse.releaseDayKey));
    }

    @Test
    @DisplayName("Should return null when release day not set")
    void testReleaseDay_NotSet() {
        assertNull(response.getReleaseDay());
    }

    @Test
    @DisplayName("Should set release day to zero")
    void testReleaseDay_Zero() {
        response.setReleaseDay(0L);

        assertEquals(0L, response.getReleaseDay());
    }

    @Test
    @DisplayName("Should set release day to null")
    void testReleaseDay_Null() {
        response.setReleaseDay(1640995200000L);
        response.setReleaseDay(null);

        assertNull(response.getReleaseDay());
    }

    @Test
    @DisplayName("Should set release day to negative value")
    void testReleaseDay_Negative() {
        response.setReleaseDay(-1000L);

        assertEquals(-1000L, response.getReleaseDay());
    }

    // ==================== Days Remaining Tests ====================

    @Test
    @DisplayName("Should set and get days remaining")
    void testDaysRemaining_SetAndGet() {
        response.setDaysRemaining(30.0);

        assertEquals(30.0, response.getDaysRemaining());
        assertEquals(30.0, response.get(ReleaseSummaryResponse.daysRemainingKey));
    }

    @Test
    @DisplayName("Should return null when days remaining not set")
    void testDaysRemaining_NotSet() {
        assertNull(response.getDaysRemaining());
    }

    @Test
    @DisplayName("Should set days remaining to zero")
    void testDaysRemaining_Zero() {
        response.setDaysRemaining(0.0);

        assertEquals(0.0, response.getDaysRemaining());
    }

    @Test
    @DisplayName("Should set days remaining to decimal value")
    void testDaysRemaining_DecimalValue() {
        response.setDaysRemaining(15.5);

        assertEquals(15.5, response.getDaysRemaining());
    }

    @Test
    @DisplayName("Should set days remaining to negative value")
    void testDaysRemaining_Negative() {
        response.setDaysRemaining(-5.0);

        assertEquals(-5.0, response.getDaysRemaining());
    }

    @Test
    @DisplayName("Should set days remaining to null")
    void testDaysRemaining_Null() {
        response.setDaysRemaining(30.0);
        response.setDaysRemaining(null);

        assertNull(response.getDaysRemaining());
    }

    // ==================== User Stories List Tests ====================

    @Test
    @DisplayName("Should set and get user stories list")
    void testUserStories_SetAndGet() {
        List<String> userStories = Arrays.asList("STORY-1", "STORY-2", "STORY-3");
        response.setUserStories(userStories);

        assertEquals(userStories, response.getUserStories());
        assertEquals(userStories, response.get(ReleaseSummaryResponse.userStoriesKey));
        assertEquals(3, response.getUserStories().size());
    }

    @Test
    @DisplayName("Should return null when user stories not set")
    void testUserStories_NotSet() {
        assertNull(response.getUserStories());
    }

    @Test
    @DisplayName("Should set user stories to empty list")
    void testUserStories_EmptyList() {
        List<String> emptyList = Collections.emptyList();
        response.setUserStories(emptyList);

        assertEquals(emptyList, response.getUserStories());
        assertEquals(0, response.getUserStories().size());
    }

    @Test
    @DisplayName("Should set user stories to null")
    void testUserStories_Null() {
        response.setUserStories(Arrays.asList("STORY-1"));
        response.setUserStories(null);

        assertNull(response.getUserStories());
    }

    @Test
    @DisplayName("Should set user stories with single item")
    void testUserStories_SingleItem() {
        List<String> singleItem = Collections.singletonList("STORY-1");
        response.setUserStories(singleItem);

        assertEquals(1, response.getUserStories().size());
        assertEquals("STORY-1", response.getUserStories().get(0));
    }

    @Test
    @DisplayName("Should set user stories with special characters")
    void testUserStories_SpecialCharacters() {
        List<String> stories = Arrays.asList("STORY-1: User's Story", "STORY-2 & 3");
        response.setUserStories(stories);

        assertEquals(stories, response.getUserStories());
    }

    // ==================== Epics List Tests ====================

    @Test
    @DisplayName("Should set and get epics list")
    void testEpics_SetAndGet() {
        List<String> epics = Arrays.asList("EPIC-1", "EPIC-2");
        response.setEpics(epics);

        assertEquals(epics, response.getEpics());
        assertEquals(epics, response.get(ReleaseSummaryResponse.epicsKey));
        assertEquals(2, response.getEpics().size());
    }

    @Test
    @DisplayName("Should return null when epics not set")
    void testEpics_NotSet() {
        assertNull(response.getEpics());
    }

    @Test
    @DisplayName("Should set epics to empty list")
    void testEpics_EmptyList() {
        List<String> emptyList = Collections.emptyList();
        response.setEpics(emptyList);

        assertEquals(emptyList, response.getEpics());
        assertEquals(0, response.getEpics().size());
    }

    @Test
    @DisplayName("Should set epics to null")
    void testEpics_Null() {
        response.setEpics(Arrays.asList("EPIC-1"));
        response.setEpics(null);

        assertNull(response.getEpics());
    }

    @Test
    @DisplayName("Should set epics with single item")
    void testEpics_SingleItem() {
        List<String> singleItem = Collections.singletonList("EPIC-1");
        response.setEpics(singleItem);

        assertEquals(1, response.getEpics().size());
        assertEquals("EPIC-1", response.getEpics().get(0));
    }

    // ==================== Bugs List Tests ====================

    @Test
    @DisplayName("Should set and get bugs list")
    void testBugs_SetAndGet() {
        List<String> bugs = Arrays.asList("BUG-1", "BUG-2", "BUG-3", "BUG-4");
        response.setBugs(bugs);

        assertEquals(bugs, response.getBugs());
        assertEquals(bugs, response.get(ReleaseSummaryResponse.bugsKey));
        assertEquals(4, response.getBugs().size());
    }

    @Test
    @DisplayName("Should return null when bugs not set")
    void testBugs_NotSet() {
        assertNull(response.getBugs());
    }

    @Test
    @DisplayName("Should set bugs to empty list")
    void testBugs_EmptyList() {
        List<String> emptyList = Collections.emptyList();
        response.setBugs(emptyList);

        assertEquals(emptyList, response.getBugs());
        assertEquals(0, response.getBugs().size());
    }

    @Test
    @DisplayName("Should set bugs to null")
    void testBugs_Null() {
        response.setBugs(Arrays.asList("BUG-1"));
        response.setBugs(null);

        assertNull(response.getBugs());
    }

    @Test
    @DisplayName("Should set bugs with single item")
    void testBugs_SingleItem() {
        List<String> singleItem = Collections.singletonList("BUG-1");
        response.setBugs(singleItem);

        assertEquals(1, response.getBugs().size());
        assertEquals("BUG-1", response.getBugs().get(0));
    }

    // ==================== Tasks List Tests ====================

    @Test
    @DisplayName("Should set and get tasks list")
    void testTasks_SetAndGet() {
        List<String> tasks = Arrays.asList("TASK-1", "TASK-2", "TASK-3");
        response.setTasks(tasks);

        assertEquals(tasks, response.getTasks());
        assertEquals(tasks, response.get(ReleaseSummaryResponse.tasksKey));
        assertEquals(3, response.getTasks().size());
    }

    @Test
    @DisplayName("Should return null when tasks not set")
    void testTasks_NotSet() {
        assertNull(response.getTasks());
    }

    @Test
    @DisplayName("Should set tasks to empty list")
    void testTasks_EmptyList() {
        List<String> emptyList = Collections.emptyList();
        response.setTasks(emptyList);

        assertEquals(emptyList, response.getTasks());
        assertEquals(0, response.getTasks().size());
    }

    @Test
    @DisplayName("Should set tasks to null")
    void testTasks_Null() {
        response.setTasks(Arrays.asList("TASK-1"));
        response.setTasks(null);

        assertNull(response.getTasks());
    }

    @Test
    @DisplayName("Should set tasks with single item")
    void testTasks_SingleItem() {
        List<String> singleItem = Collections.singletonList("TASK-1");
        response.setTasks(singleItem);

        assertEquals(1, response.getTasks().size());
        assertEquals("TASK-1", response.getTasks().get(0));
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Should set and get all fields together")
    void testAllFields_Integration() {
        // Set all numeric fields
        response.setTotalUserStories(50.0);
        response.setOpenUserStories(10.0);
        response.setTotalEpics(5.0);
        response.setTotalTasks(30.0);
        response.setOpenTasks(8.0);
        response.setTotalBugs(20.0);
        response.setOpenBugs(5.0);
        response.setReleaseDay(1640995200000L);
        response.setDaysRemaining(15.0);

        // Set all list fields
        response.setUserStories(Arrays.asList("STORY-1", "STORY-2"));
        response.setEpics(Arrays.asList("EPIC-1"));
        response.setBugs(Arrays.asList("BUG-1", "BUG-2", "BUG-3"));
        response.setTasks(Arrays.asList("TASK-1", "TASK-2"));

        // Verify all numeric fields
        assertEquals(50.0, response.getTotalUserStories());
        assertEquals(10.0, response.getOpenUserStories());
        assertEquals(5.0, response.getTotalEpics());
        assertEquals(30.0, response.getTotalTasks());
        assertEquals(8.0, response.getOpenTasks());
        assertEquals(20.0, response.getTotalBugs());
        assertEquals(5.0, response.getOpenBugs());
        assertEquals(1640995200000L, response.getReleaseDay());
        assertEquals(15.0, response.getDaysRemaining());

        // Verify all list fields
        assertEquals(2, response.getUserStories().size());
        assertEquals(1, response.getEpics().size());
        assertEquals(3, response.getBugs().size());
        assertEquals(2, response.getTasks().size());

        // Verify HashMap size
        assertEquals(13, response.size());
    }

    @Test
    @DisplayName("Should update existing values")
    void testUpdateExistingValues() {
        response.setTotalUserStories(10.0);
        assertEquals(10.0, response.getTotalUserStories());

        response.setTotalUserStories(20.0);
        assertEquals(20.0, response.getTotalUserStories());

        response.setUserStories(Arrays.asList("STORY-1"));
        assertEquals(1, response.getUserStories().size());

        response.setUserStories(Arrays.asList("STORY-1", "STORY-2", "STORY-3"));
        assertEquals(3, response.getUserStories().size());
    }

    @Test
    @DisplayName("Should handle HashMap operations")
    void testHashMapOperations() {
        response.setTotalUserStories(10.0);
        response.setOpenUserStories(5.0);

        // Test size
        assertEquals(2, response.size());

        // Test containsKey
        assertTrue(response.containsKey(ReleaseSummaryResponse.totalUserStoriesKey));
        assertTrue(response.containsKey(ReleaseSummaryResponse.openUserStoriesKey));
        assertFalse(response.containsKey("NonExistentKey"));

        // Test containsValue
        assertTrue(response.containsValue(10.0));
        assertTrue(response.containsValue(5.0));

        // Test isEmpty
        assertFalse(response.isEmpty());

        // Test clear
        response.clear();
        assertTrue(response.isEmpty());
        assertEquals(0, response.size());
        assertNull(response.getTotalUserStories());
    }

    @Test
    @DisplayName("Should handle remove operations")
    void testRemoveOperations() {
        response.setTotalUserStories(10.0);
        response.setOpenUserStories(5.0);

        assertEquals(2, response.size());

        response.remove(ReleaseSummaryResponse.totalUserStoriesKey);

        assertNull(response.getTotalUserStories());
        assertEquals(5.0, response.getOpenUserStories());
        assertEquals(1, response.size());
    }

    @Test
    @DisplayName("Should handle putAll operations")
    void testPutAllOperations() {
        ReleaseSummaryResponse otherResponse = new ReleaseSummaryResponse();
        otherResponse.setTotalUserStories(100.0);
        otherResponse.setOpenUserStories(50.0);
        otherResponse.setTotalEpics(10.0);

        response.putAll(otherResponse);

        assertEquals(100.0, response.getTotalUserStories());
        assertEquals(50.0, response.getOpenUserStories());
        assertEquals(10.0, response.getTotalEpics());
        assertEquals(3, response.size());
    }

    @Test
    @DisplayName("Should handle keySet operations")
    void testKeySetOperations() {
        response.setTotalUserStories(10.0);
        response.setOpenUserStories(5.0);
        response.setTotalEpics(3.0);

        var keySet = response.keySet();

        assertEquals(3, keySet.size());
        assertTrue(keySet.contains(ReleaseSummaryResponse.totalUserStoriesKey));
        assertTrue(keySet.contains(ReleaseSummaryResponse.openUserStoriesKey));
        assertTrue(keySet.contains(ReleaseSummaryResponse.totalEpicsKey));
    }

    @Test
    @DisplayName("Should handle values operations")
    void testValuesOperations() {
        response.setTotalUserStories(10.0);
        response.setOpenUserStories(5.0);
        response.setTotalEpics(3.0);

        var values = response.values();

        assertEquals(3, values.size());
        assertTrue(values.contains(10.0));
        assertTrue(values.contains(5.0));
        assertTrue(values.contains(3.0));
    }

    @Test
    @DisplayName("Should handle entrySet operations")
    void testEntrySetOperations() {
        response.setTotalUserStories(10.0);
        response.setOpenUserStories(5.0);

        var entrySet = response.entrySet();

        assertEquals(2, entrySet.size());

        boolean foundTotalUserStories = false;
        boolean foundOpenUserStories = false;

        for (var entry : entrySet) {
            if (entry.getKey().equals(ReleaseSummaryResponse.totalUserStoriesKey)) {
                assertEquals(10.0, entry.getValue());
                foundTotalUserStories = true;
            }
            if (entry.getKey().equals(ReleaseSummaryResponse.openUserStoriesKey)) {
                assertEquals(5.0, entry.getValue());
                foundOpenUserStories = true;
            }
        }

        assertTrue(foundTotalUserStories);
        assertTrue(foundOpenUserStories);
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle very large numbers")
    void testVeryLargeNumbers() {
        response.setTotalUserStories(Double.MAX_VALUE);
        response.setReleaseDay(Long.MAX_VALUE);

        assertEquals(Double.MAX_VALUE, response.getTotalUserStories());
        assertEquals(Long.MAX_VALUE, response.getReleaseDay());
    }

    @Test
    @DisplayName("Should handle very small numbers")
    void testVerySmallNumbers() {
        response.setTotalUserStories(Double.MIN_VALUE);
        response.setReleaseDay(Long.MIN_VALUE);

        assertEquals(Double.MIN_VALUE, response.getTotalUserStories());
        assertEquals(Long.MIN_VALUE, response.getReleaseDay());
    }

    @Test
    @DisplayName("Should handle negative numbers")
    void testNegativeNumbers() {
        response.setTotalUserStories(-10.0);
        response.setOpenUserStories(-5.0);
        response.setDaysRemaining(-15.0);
        response.setReleaseDay(-1000L);

        assertEquals(-10.0, response.getTotalUserStories());
        assertEquals(-5.0, response.getOpenUserStories());
        assertEquals(-15.0, response.getDaysRemaining());
        assertEquals(-1000L, response.getReleaseDay());
    }

    @Test
    @DisplayName("Should handle lists with duplicate values")
    void testListsWithDuplicates() {
        List<String> duplicates = Arrays.asList("STORY-1", "STORY-1", "STORY-2", "STORY-1");
        response.setUserStories(duplicates);

        assertEquals(4, response.getUserStories().size());
        assertEquals(duplicates, response.getUserStories());
    }

    @Test
    @DisplayName("Should handle lists with null elements")
    void testListsWithNullElements() {
        List<String> withNulls = Arrays.asList("STORY-1", null, "STORY-2");
        response.setUserStories(withNulls);

        assertEquals(3, response.getUserStories().size());
        assertNull(response.getUserStories().get(1));
    }

    @Test
    @DisplayName("Should handle lists with empty strings")
    void testListsWithEmptyStrings() {
        List<String> withEmpty = Arrays.asList("STORY-1", "", "STORY-2");
        response.setUserStories(withEmpty);

        assertEquals(3, response.getUserStories().size());
        assertEquals("", response.getUserStories().get(1));
    }

    @Test
    @DisplayName("Should handle very long lists")
    void testVeryLongLists() {
        List<String> longList = new java.util.ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            longList.add("STORY-" + i);
        }

        response.setUserStories(longList);

        assertEquals(1000, response.getUserStories().size());
        assertEquals("STORY-0", response.getUserStories().get(0));
        assertEquals("STORY-999", response.getUserStories().get(999));
    }

    @Test
    @DisplayName("Should handle infinity values")
    void testInfinityValues() {
        response.setTotalUserStories(Double.POSITIVE_INFINITY);
        response.setOpenUserStories(Double.NEGATIVE_INFINITY);

        assertEquals(Double.POSITIVE_INFINITY, response.getTotalUserStories());
        assertEquals(Double.NEGATIVE_INFINITY, response.getOpenUserStories());
    }

    @Test
    @DisplayName("Should handle NaN values")
    void testNaNValues() {
        response.setTotalUserStories(Double.NaN);

        assertTrue(Double.isNaN(response.getTotalUserStories()));
    }

    @Test
    @DisplayName("Should verify all constants are public static final")
    void testConstantsArePublicStaticFinal() throws NoSuchFieldException {
        var field = ReleaseSummaryResponse.class.getDeclaredField("totalUserStoriesKey");
        int modifiers = field.getModifiers();

        assertTrue(java.lang.reflect.Modifier.isPublic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isStatic(modifiers));
        assertTrue(java.lang.reflect.Modifier.isFinal(modifiers));
    }
}




