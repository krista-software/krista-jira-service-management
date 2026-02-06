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

package app.krista.extensions.devsecops.development.jira.impl;

import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.services.ReleaseSummaryResponse;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public final class ReleaseDao {

    private final IssueDao issueDao;

    @Inject
    public ReleaseDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    public ReleaseSummaryResponse getGetReleaseSummaryResponse(String status, String assignee, String priority, String issueType, long pageSize, long pageNumber, String release) {
        ReleaseSummaryResponse res = new ReleaseSummaryResponse();

        //Stories
        if (issueType == null || "Story".equalsIgnoreCase(issueType)) {
            updateStories(status, assignee, priority, pageSize, pageNumber, release, res);
        }

        //Tasks
        if (issueType == null || "Task".equalsIgnoreCase(issueType)) {
            updateTasks(status, assignee, priority, pageSize, pageNumber, release, res);
        }

        //Bugs
        if (issueType == null || "Bug".equalsIgnoreCase(issueType)) {
            updateBugs(status, assignee, priority, pageSize, pageNumber, release, res);
        }

        //Epics
        if (issueType == null || "Epic".equalsIgnoreCase(issueType)) {
            updateEpics(status, assignee, priority, pageSize, pageNumber, release, res);
        }

        res.put(ReleaseSummaryResponse.releaseDayKey, null);
        res.put(ReleaseSummaryResponse.daysRemainingKey, null);

        return res;
    }

    private void updateStories(String status, String assignee, String priority, long pageSize, long pageNumber, String release, ReleaseSummaryResponse res) {
        String jql = getJqlQuery(status, assignee, priority, "Story", release);
        List<Issue> list = issueDao.searchByJQL(jql, pageSize, pageNumber);
        res.put(ReleaseSummaryResponse.totalUserStoriesKey, list.size());
        res.put(ReleaseSummaryResponse.openUserStoriesKey, issueDao.searchByJQL(jql, pageSize, pageNumber).size());
        res.put(ReleaseSummaryResponse.userStoriesKey, list);
    }

    private void updateTasks(String status, String assignee, String priority, long pageSize, long pageNumber, String release, ReleaseSummaryResponse res) {
        String jql = getJqlQuery(status, assignee, priority, "Task", release);
        List<Issue> list = issueDao.searchByJQL(jql, pageSize, pageNumber);
        res.put(ReleaseSummaryResponse.totalTasksKey, list.size());
        res.put(ReleaseSummaryResponse.openTasksKey, issueDao.searchByJQL(jql, pageSize, pageNumber).size());
        res.put(ReleaseSummaryResponse.tasksKey, list);
    }

    private void updateBugs(String status, String assignee, String priority, long pageSize, long pageNumber, String release, ReleaseSummaryResponse res) {
        String jql = getJqlQuery(status, assignee, priority, "Bug", release);
        List<Issue> list = issueDao.searchByJQL(jql, pageSize, pageNumber);
        res.put(ReleaseSummaryResponse.totalBugsKey, list.size());
        res.put(ReleaseSummaryResponse.openBugsKey, issueDao.searchByJQL(jql, pageSize, pageNumber).size());
        res.put(ReleaseSummaryResponse.bugsKey, list);
    }

    private void updateEpics(String status, String assignee, String priority, long pageSize, long pageNumber, String release, ReleaseSummaryResponse res) {
        String jql = getJqlQuery(status, assignee, priority, "Epic", release);
        List<Issue> list = issueDao.searchByJQL(jql, pageSize, pageNumber);
        res.put(ReleaseSummaryResponse.totalEpicsKey, list.size());
        res.put(ReleaseSummaryResponse.epicsKey, list);
    }

    private String getJqlQuery(String status, String assignee, String priority, String issueType, String release) {
        return new JQLBuilder().status(status)
                .assignee(assignee)
                .priority(priority)
                .issueType(issueType)
                .fixVersion(release)
                .orderBy("priority DESC, updated", "desc")
                .build();
    }
}
