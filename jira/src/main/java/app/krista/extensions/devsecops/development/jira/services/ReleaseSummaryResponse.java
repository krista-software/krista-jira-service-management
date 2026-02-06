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

import java.util.HashMap;
import java.util.List;

public final class ReleaseSummaryResponse extends HashMap<String, Object> {

    public static final String totalUserStoriesKey = "Total user stories";

    public static final String openUserStoriesKey = "Open user stories";

    public static final String totalEpicsKey = "Total epics";

    public static final String totalTasksKey = "Total Tasks";

    public static final String openTasksKey = "Open Tasks";

    public static final String totalBugsKey = "Total Bugs";

    public static final String openBugsKey = "Open Bugs";

    public static final String releaseDayKey = "Release day";

    public static final String daysRemainingKey = "Days remaining";

    public static final String userStoriesKey = "User stories";

    public static final String epicsKey = "Epics";

    public static final String bugsKey = "Bugs";

    public static final String tasksKey = "Tasks";

    public Double getTotalUserStories() {
        return (Double) this.get(totalUserStoriesKey);
    }

    public void setTotalUserStories(Double totalUserStories) {
        this.put(totalUserStoriesKey, totalUserStories);
    }

    public Double getOpenUserStories() {
        return (Double) this.get(openUserStoriesKey);
    }

    public void setOpenUserStories(Double openUserStories) {
        this.put(openUserStoriesKey, openUserStories);
    }

    public Double getTotalEpics() {
        return (Double) this.get(totalEpicsKey);
    }

    public void setTotalEpics(Double totalEpics) {
        this.put(totalEpicsKey, totalEpics);
    }

    public Double getTotalTasks() {
        return (Double) this.get(totalTasksKey);
    }

    public void setTotalTasks(Double totalTasks) {
        this.put(totalTasksKey, totalTasks);
    }

    public Double getOpenTasks() {
        return (Double) this.get(openTasksKey);
    }

    public void setOpenTasks(Double openTasks) {
        this.put(openTasksKey, openTasks);
    }

    public Double getTotalBugs() {
        return (Double) this.get(totalBugsKey);
    }

    public void setTotalBugs(Double totalBugs) {
        this.put(totalBugsKey, totalBugs);
    }

    public Double getOpenBugs() {
        return (Double) this.get(openBugsKey);
    }

    public void setOpenBugs(Double openBugs) {
        this.put(openBugsKey, openBugs);
    }

    public Long getReleaseDay() {
        return (Long) this.get(releaseDayKey);
    }

    public void setReleaseDay(Long releaseDay) {
        this.put(releaseDayKey, releaseDay);
    }

    public Double getDaysRemaining() {
        return (Double) this.get(daysRemainingKey);
    }

    public void setDaysRemaining(Double daysRemaining) {
        this.put(daysRemainingKey, daysRemaining);
    }

    public List<String> getUserStories() {
        return (List<String>) this.get(userStoriesKey);
    }

    public void setUserStories(List<String> userStories) {
        this.put(userStoriesKey, userStories);
    }

    public List<String> getEpics() {
        return (List<String>) this.get(epicsKey);
    }

    public void setEpics(List<String> epics) {
        this.put(epicsKey, epics);
    }

    public List<String> getBugs() {
        return (List<String>) this.get(bugsKey);
    }

    public void setBugs(List<String> bugs) {
        this.put(bugsKey, bugs);
    }

    public List<String> getTasks() {
        return (List<String>) this.get(tasksKey);
    }

    public void setTasks(List<String> tasks) {
        this.put(tasksKey, tasks);
    }
}
