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

public class TelemetryConstants {

    public static final class OrganizationArea {
        // Attach File to Ticket
        public static final String ATTACH_FILE_TO_TICKET_REQUEST_COUNT = "Jsm.OrganizationArea.attachFileToTicket.count";
        public static final String ATTACH_FILE_TO_TICKET_REQUEST_FAILURE = "Jsm.OrganizationArea.attachFileToTicket.failure";
        public static final String ATTACH_FILE_TO_TICKET_REQUEST_SUCCESS = "Jsm.OrganizationArea.attachFileToTicket.success";
        public static final String ATTACH_FILE_TO_TICKET_RETRY_ATTEMPT = "Jsm.OrganizationArea.attachFileToTicket.retry.attempt";

        // Get Request Type Field Value
        public static final String GET_REQUEST_TYPE_FIELD_VALUE_REQUEST_COUNT = "Jsm.OrganizationArea.getRequestTypeFieldValue.count";
        public static final String GET_REQUEST_TYPE_FIELD_VALUE_REQUEST_FAILURE = "Jsm.OrganizationArea.getRequestTypeFieldValue.failure";
        public static final String GET_REQUEST_TYPE_FIELD_VALUE_REQUEST_SUCCESS = "Jsm.OrganizationArea.getRequestTypeFieldValue.success";

        // Create an Issue
        public static final String CREATE_AN_ISSUE_REQUEST_COUNT = "Jsm.OrganizationArea.createAnIssue.count";
        public static final String CREATE_AN_ISSUE_REQUEST_FAILURE = "Jsm.OrganizationArea.createAnIssue.failure";
        public static final String CREATE_AN_ISSUE_REQUEST_SUCCESS = "Jsm.OrganizationArea.createAnIssue.success";
        public static final String CREATE_AN_ISSUE_RETRY_ATTEMPT = "Jsm.OrganizationArea.createAnIssue.retry.attempt";

        // Report a Bug
        public static final String REPORT_A_BUG_REQUEST_COUNT = "Jsm.OrganizationArea.reportABug.count";
        public static final String REPORT_A_BUG_REQUEST_FAILURE = "Jsm.OrganizationArea.reportABug.failure";
        public static final String REPORT_A_BUG_REQUEST_SUCCESS = "Jsm.OrganizationArea.reportABug.success";
        public static final String REPORT_A_BUG_RETRY_ATTEMPT = "Jsm.OrganizationArea.reportABug.retry.attempt";

        // Create User
        public static final String CREATE_USER_REQUEST_COUNT = "Jsm.OrganizationArea.createUser.count";
        public static final String CREATE_USER_REQUEST_FAILURE = "Jsm.OrganizationArea.createUser.failure";
        public static final String CREATE_USER_REQUEST_SUCCESS = "Jsm.OrganizationArea.createUser.success";
        public static final String CREATE_USER_RETRY_ATTEMPT = "Jsm.OrganizationArea.createUser.retry.attempt";

        // Update User
        public static final String UPDATE_USER_REQUEST_COUNT = "Jsm.OrganizationArea.updateUser.count";
        public static final String UPDATE_USER_REQUEST_FAILURE = "Jsm.OrganizationArea.updateUser.failure";
        public static final String UPDATE_USER_REQUEST_SUCCESS = "Jsm.OrganizationArea.updateUser.success";
        public static final String UPDATE_USER_RETRY_ATTEMPT = "Jsm.OrganizationArea.updateUser.retry.attempt";

        // Delete User
        public static final String DELETE_USER_REQUEST_COUNT = "Jsm.OrganizationArea.deleteUser.count";
        public static final String DELETE_USER_REQUEST_FAILURE = "Jsm.OrganizationArea.deleteUser.failure";
        public static final String DELETE_USER_REQUEST_SUCCESS = "Jsm.OrganizationArea.deleteUser.success";
        public static final String DELETE_USER_RETRY_ATTEMPT = "Jsm.OrganizationArea.deleteUser.retry.attempt";

        // Get Users Account Id
        public static final String GET_USERS_ACCOUNT_ID_REQUEST_COUNT = "Jsm.OrganizationArea.getUsersAccountId.count";
        public static final String GET_USERS_ACCOUNT_ID_REQUEST_FAILURE = "Jsm.OrganizationArea.getUsersAccountId.failure";
        public static final String GET_USERS_ACCOUNT_ID_REQUEST_SUCCESS = "Jsm.OrganizationArea.getUsersAccountId.success";
        public static final String GET_USERS_ACCOUNT_ID_RETRY_ATTEMPT = "Jsm.OrganizationArea.getUsersAccountId.retry.attempt";

        // Get User Account Information
        public static final String GET_USER_ACCOUNT_INFORMATION_REQUEST_COUNT = "Jsm.OrganizationArea.getUserAccountInformation.count";
        public static final String GET_USER_ACCOUNT_INFORMATION_REQUEST_FAILURE = "Jsm.OrganizationArea.getUserAccountInformation.failure";
        public static final String GET_USER_ACCOUNT_INFORMATION_REQUEST_SUCCESS = "Jsm.OrganizationArea.getUserAccountInformation.success";
    }

    public static final class RitualsArea {
        // Update Status of Multiple Issues
        public static final String UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_COUNT = "Jsm.RitualsArea.updateStatusOfMultipleIssues.count";
        public static final String UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_FAILURE = "Jsm.RitualsArea.updateStatusOfMultipleIssues.failure";
        public static final String UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_SUCCESS = "Jsm.RitualsArea.updateStatusOfMultipleIssues.success";
        public static final String UPDATE_STATUS_OF_MULTIPLE_ISSUES_RETRY_ATTEMPT = "Jsm.RitualsArea.updateStatusOfMultipleIssues.retry.attempt";

        // Add Comment
        public static final String ADD_COMMENT_REQUEST_COUNT = "Jsm.RitualsArea.addComment.count";
        public static final String ADD_COMMENT_REQUEST_FAILURE = "Jsm.RitualsArea.addComment.failure";
        public static final String ADD_COMMENT_REQUEST_SUCCESS = "Jsm.RitualsArea.addComment.success";
        public static final String ADD_COMMENT_RETRY_ATTEMPT = "Jsm.RitualsArea.addComment.retry.attempt";

        // Get User Issues
        public static final String GET_USER_ISSUES_REQUEST_COUNT = "Jsm.RitualsArea.getUserIssues.count";
        public static final String GET_USER_ISSUES_REQUEST_FAILURE = "Jsm.RitualsArea.getUserIssues.failure";
        public static final String GET_USER_ISSUES_REQUEST_SUCCESS = "Jsm.RitualsArea.getUserIssues.success";
        public static final String GET_USER_ISSUES_RETRY_ATTEMPT = "Jsm.RitualsArea.getUserIssues.retry.attempt";

        // Update Status of Issue
        public static final String UPDATE_STATUS_OF_ISSUE_REQUEST_COUNT = "Jsm.RitualsArea.updateStatusOfIssue.count";
        public static final String UPDATE_STATUS_OF_ISSUE_REQUEST_FAILURE = "Jsm.RitualsArea.updateStatusOfIssue.failure";
        public static final String UPDATE_STATUS_OF_ISSUE_REQUEST_SUCCESS = "Jsm.RitualsArea.updateStatusOfIssue.success";
        public static final String UPDATE_STATUS_OF_ISSUE_RETRY_ATTEMPT = "Jsm.RitualsArea.updateStatusOfIssue.retry.attempt";

        // Assign Task to User
        public static final String ASSIGN_TASK_TO_USER_REQUEST_COUNT = "Jsm.RitualsArea.assignTaskToUser.count";
        public static final String ASSIGN_TASK_TO_USER_REQUEST_FAILURE = "Jsm.RitualsArea.assignTaskToUser.failure";
        public static final String ASSIGN_TASK_TO_USER_REQUEST_SUCCESS = "Jsm.RitualsArea.assignTaskToUser.success";
        public static final String ASSIGN_TASK_TO_USER_RETRY_ATTEMPT = "Jsm.RitualsArea.assignTaskToUser.retry.attempt";

        // Fetch by JQL Query
        public static final String FETCH_BY_JQL_QUERY_REQUEST_COUNT = "Jsm.RitualsArea.fetchByJQLQuery.count";
        public static final String FETCH_BY_JQL_QUERY_REQUEST_FAILURE = "Jsm.RitualsArea.fetchByJQLQuery.failure";
        public static final String FETCH_BY_JQL_QUERY_REQUEST_SUCCESS = "Jsm.RitualsArea.fetchByJQLQuery.success";
        public static final String FETCH_BY_JQL_QUERY_RETRY_ATTEMPT = "Jsm.RitualsArea.fetchByJQLQuery.retry.attempt";

        // Get Results for Fetch Issues by JQL Query
        public static final String GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_COUNT = "Jsm.RitualsArea.getResultsForFetchIssuesByJQLQuery.count";
        public static final String GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_FAILURE = "Jsm.RitualsArea.getResultsForFetchIssuesByJQLQuery.failure";
        public static final String GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_SUCCESS = "Jsm.RitualsArea.getResultsForFetchIssuesByJQLQuery.success";

        // Get My Issues by Given Statuses
        public static final String GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_COUNT = "Jsm.RitualsArea.getMyIssuesByGivenStatuses.count";
        public static final String GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_FAILURE = "Jsm.RitualsArea.getMyIssuesByGivenStatuses.failure";
        public static final String GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_SUCCESS = "Jsm.RitualsArea.getMyIssuesByGivenStatuses.success";

        // Get Issues
        public static final String GET_ISSUES_REQUEST_COUNT = "Jsm.RitualsArea.getIssues.count";
        public static final String GET_ISSUES_REQUEST_FAILURE = "Jsm.RitualsArea.getIssues.failure";
        public static final String GET_ISSUES_REQUEST_SUCCESS = "Jsm.RitualsArea.getIssues.success";
        public static final String GET_ISSUES_RETRY_ATTEMPT = "Jsm.RitualsArea.getIssues.retry.attempt";

        // Get User Issue by Issue Type
        public static final String GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_COUNT = "Jsm.RitualsArea.getUserIssueByIssueType.count";
        public static final String GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_FAILURE = "Jsm.RitualsArea.getUserIssueByIssueType.failure";
        public static final String GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_SUCCESS = "Jsm.RitualsArea.getUserIssueByIssueType.success";

        // Add Request Participants to Issue
        public static final String ADD_REQUEST_PARTICIPANTS_REQUEST_COUNT = "Jsm.RitualsArea.addRequestParticipantsToIssue.count";
        public static final String ADD_REQUEST_PARTICIPANTS_REQUEST_FAILURE = "Jsm.RitualsArea.addRequestParticipantsToIssue.failure";
        public static final String ADD_REQUEST_PARTICIPANTS_REQUEST_SUCCESS = "Jsm.RitualsArea.addRequestParticipantsToIssue.success";

        // Update an Issue
        public static final String UPDATE_AN_ISSUE_REQUEST_COUNT = "Jsm.RitualsArea.updateAnIssue.count";
        public static final String UPDATE_AN_ISSUE_REQUEST_FAILURE = "Jsm.RitualsArea.updateAnIssue.failure";
        public static final String UPDATE_AN_ISSUE_REQUEST_SUCCESS = "Jsm.RitualsArea.updateAnIssue.success";
        public static final String UPDATE_AN_ISSUE_RETRY_ATTEMPT = "Jsm.RitualsArea.updateAnIssue.retry.attempt";

        // Update Priority of Issue
        public static final String UPDATE_PRIORITY_OF_ISSUE_REQUEST_COUNT = "Jsm.RitualsArea.updatePriorityOfIssue.count";
        public static final String UPDATE_PRIORITY_OF_ISSUE_REQUEST_FAILURE = "Jsm.RitualsArea.updatePriorityOfIssue.failure";
        public static final String UPDATE_PRIORITY_OF_ISSUE_REQUEST_SUCCESS = "Jsm.RitualsArea.updatePriorityOfIssue.success";
        public static final String UPDATE_PRIORITY_OF_ISSUE_RETRY_ATTEMPT = "Jsm.RitualsArea.updatePriorityOfIssue.retry.attempt";

        // Update Priority of Multiple Issues
        public static final String UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_COUNT = "Jsm.RitualsArea.updateThePriorityOfMultipleIssues.count";
        public static final String UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_FAILURE = "Jsm.RitualsArea.updateThePriorityOfMultipleIssues.failure";
        public static final String UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_SUCCESS = "Jsm.RitualsArea.updateThePriorityOfMultipleIssues.success";

        // Add Worklog
        public static final String ADD_WORK_LOG_REQUEST_COUNT = "Jsm.RitualsArea.addWorkLog.count";
        public static final String ADD_WORK_LOG_REQUEST_FAILURE = "Jsm.RitualsArea.addWorkLog.failure";
        public static final String ADD_WORK_LOG_REQUEST_SUCCESS = "Jsm.RitualsArea.addWorkLog.success";
        public static final String ADD_WORK_LOG_RETRY_ATTEMPT = "Jsm.RitualsArea.addWorkLog.retry.attempt";

        // Get Issue by Key
        public static final String GET_ISSUE_BY_KEY_REQUEST_COUNT = "Jsm.RitualsArea.getIssueByKey.count";
        public static final String GET_ISSUE_BY_KEY_REQUEST_FAILURE = "Jsm.RitualsArea.getIssueByKey.failure";
        public static final String GET_ISSUE_BY_KEY_REQUEST_SUCCESS = "Jsm.RitualsArea.getIssueByKey.success";
        public static final String GET_ISSUE_BY_KEY_RETRY_ATTEMPT = "Jsm.RitualsArea.getIssueByKey.retry.attempt";

        // Assign Child Issue to Epic
        public static final String ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_COUNT = "Jsm.RitualsArea.assignChildIssueToEpic.count";
        public static final String ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_FAILURE = "Jsm.RitualsArea.assignChildIssueToEpic.failure";
        public static final String ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_SUCCESS = "Jsm.RitualsArea.assignChildIssueToEpic.success";
        public static final String ASSIGN_CHILD_ISSUE_TO_EPIC_RETRY_ATTEMPT = "Jsm.RitualsArea.assignChildIssueToEpic.retry.attempt";

        // Assign Issue to Epic
        public static final String ASSIGN_ISSUE_TO_EPIC_REQUEST_COUNT = "Jsm.RitualsArea.assignIssueToEpic.count";
        public static final String ASSIGN_ISSUE_TO_EPIC_REQUEST_FAILURE = "Jsm.RitualsArea.assignIssueToEpic.failure";
        public static final String ASSIGN_ISSUE_TO_EPIC_REQUEST_SUCCESS = "Jsm.RitualsArea.assignIssueToEpic.success";
        public static final String ASSIGN_ISSUE_TO_EPIC_RETRY_ATTEMPT = "Jsm.RitualsArea.assignIssueToEpic.retry.attempt";

        // Get All Comments
        public static final String GET_ALL_COMMENTS_REQUEST_COUNT = "Jsm.RitualsArea.getAllComments.count";
        public static final String GET_ALL_COMMENTS_REQUEST_FAILURE = "Jsm.RitualsArea.getAllComments.failure";
        public static final String GET_ALL_COMMENTS_REQUEST_SUCCESS = "Jsm.RitualsArea.getAllComments.success";
        public static final String GET_ALL_COMMENTS_RETRY_ATTEMPT = "Jsm.RitualsArea.getAllComments.retry.attempt";

        // Get Notification
        public static final String GET_NOTIFICATION_REQUEST_COUNT = "Jsm.RitualsArea.getNotification.count";
        public static final String GET_NOTIFICATION_REQUEST_SUCCESS = "Jsm.RitualsArea.getNotification.success";
        public static final String GET_NOTIFICATION_REQUEST_FAILURE = "Jsm.RitualsArea.getNotification.failure";

        // Get Custom Field Value
        public static final String GET_CUSTOM_FIELD_VALUE_REQUEST_COUNT = "Jsm.RitualsArea.getCustomFieldValue.count";
        public static final String GET_CUSTOM_FIELD_VALUE_REQUEST_FAILURE = "Jsm.RitualsArea.getCustomFieldValue.failure";
        public static final String GET_CUSTOM_FIELD_VALUE_REQUEST_SUCCESS = "Jsm.RitualsArea.getCustomFieldValue.success";
    }

    public static final class ReleaseManagementArea {
        // Get Release Summary
        public static final String GET_RELEASE_SUMMARY_REQUEST_COUNT = "Jsm.ReleaseManagementArea.getReleaseSummary.count";
        public static final String GET_RELEASE_SUMMARY_REQUEST_FAILURE = "Jsm.ReleaseManagementArea.getReleaseSummary.failure";
        public static final String GET_RELEASE_SUMMARY_REQUEST_SUCCESS = "Jsm.ReleaseManagementArea.getReleaseSummary.success";
        public static final String GET_RELEASE_SUMMARY_RETRY_ATTEMPT = "Jsm.ReleaseManagementArea.getReleaseSummary.retry.attempt";
    }
}
