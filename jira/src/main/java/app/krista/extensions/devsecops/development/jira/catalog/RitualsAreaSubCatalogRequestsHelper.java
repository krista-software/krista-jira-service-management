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

package app.krista.extensions.devsecops.development.jira.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.SubCatalogRequest;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.RemediationActionFactory;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.base.FreeForm;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.ERRORS;
import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.JQL_QUERY;

@Service
public class RitualsAreaSubCatalogRequestsHelper {

    private final IssueDao issueDao;
    private final RequestContext requestContext;
    private final UserDao userDao;
    private final AuthorizationContext authorizationContext;
    private final EventHandler eventHandler;
    private final ErrorHandlingStateManager internalStateManager;
    private final ExtensionResponseGenerator responseGenerator;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final RitualsArea ritualsArea;
    private final TicketInfoDao ticketInfoDao;
    private final ProjectDao projectDao;

    @Inject
    public RitualsAreaSubCatalogRequestsHelper(IssueDao issueDao, RequestContext requestContext, UserDao userDao, AuthorizationContext authorizationContext, EventHandler eventHandler, ErrorHandlingStateManager internalStateManager, ExtensionResponseGenerator responseGenerator, RitualsArea ritualsArea, TicketInfoDao ticketInfoDao, ProjectDao projectDao) {
        this.issueDao = issueDao;
        this.requestContext = requestContext;
        this.userDao = userDao;
        this.authorizationContext = authorizationContext;
        this.eventHandler = eventHandler;
        this.internalStateManager = internalStateManager;
        this.responseGenerator = responseGenerator;
        this.ritualsArea = ritualsArea;
        this.ticketInfoDao = ticketInfoDao;
        this.projectDao = projectDao;
    }

    @SubCatalogRequest(
            name = "confirmReenterJQLQuery",
            description = "Checks if user wants to re enter JQL Query and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterJQLQuery(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, JQL Query: Paragraph }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get("Reenter");
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterJQLQuery",
                Map.of("stateId",
                        stateId, JQL_QUERY, map.get(JQL_QUERY)));
    }

    @SubCatalogRequest(
            name = "handleReenterJQLQuery",
            description = "Use the new details to JQL Query",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @Field.Desc(name = "Task ID", type = "Text")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterJQLQuery(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, JQL Query: Paragraph }",
            required = true) Map<String, Object> map) {
        try {
            boolean invokeAsUser = requestContext.invokeAsUser();
            String invokerId = requestContext.getInvokerId();
            String stateId = (String) map.get("stateId");
            Map<String, Object> state = internalStateManager.get(stateId);
            String accountID = (String) state.get("AccountID");
            String taskId = (String) state.get("Task ID");
            String jQLQuery = (String) map.get(JQL_QUERY);

            try {
                Map<String, String> queryData = issueDao.fetchErrorsInJQLQuery(invokeAsUser, accountID, invokerId, jQLQuery);
                if (queryData.containsKey(ERRORS)) {
                    return ExtensionResponseFactory.create(queryData.get(ERRORS), ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                            List.of(RemediationActionFactory.createInformActionALLParticipants(queryData.get(ERRORS), List.of())),
                            null, Map.of());
                }
            } catch (Exception cause) {
                return ExtensionResponseFactory.create(cause, "Error occurred while Validating JQL Query",
                        ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
            }
            executorService.submit(() -> {
                List<Issue> issues;
                FreeForm freeForm = new FreeForm();
                try {
                    issues = issueDao.searchByJqlQuery(invokeAsUser, accountID, invokerId, jQLQuery, JiraConstants.DEFAULT_PAGE_SIZE, JiraConstants.PAGE_NUMBER);

                    freeForm.put(RitualsArea.IS_ERROR, "Switch", false);
                    freeForm.put("Data", "[ Entity(Issue) ]", issues);
                } catch (Exception cause) {
                    freeForm.put(RitualsArea.IS_ERROR, "Switch", true);
                    freeForm.put(RitualsArea.ERROR_MESSAGE, "Text", "Error occurred while fetching JQL - " + cause.getMessage());
                }

                eventHandler.handleEvent(taskId, freeForm);
            });
            return ExtensionResponseFactory.create(Map.of(
                    "Task ID", taskId));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to add comment",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @NotNull
    private static List<ValidationOrchestrator.ValidationResult> getValidationResults(Map<String, Object> state) {
        List<ValidationOrchestrator.ValidationResult> validationResults = new ArrayList<>();
        List<?> results = (List<?>) state.get(JiraConstants.VALIDATION_RESULT);
        for (Object item : results) {
            validationResults.add(JiraConstants.GSON.fromJson(JiraConstants.GSON.toJson(item), ValidationOrchestrator.ValidationResult.class));
        }
        return validationResults;
    }

    @SubCatalogRequest(
            name = "confirmReenterForUpdateAnIssue",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForUpdateAnIssue(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Ticket Id: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get("Reenter");
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = (Map<String, Object>) internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterForUpdateAnIssue",
                Map.of("stateId", stateId,
                        JiraResources.TICKET_ID, map.get(JiraResources.TICKET_ID)));
    }

    @SubCatalogRequest(
            name = "handleReenterForUpdateAnIssue",
            description = "Update an issue with given details",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "Updated Issue", type = "Entity(Issue)")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterForUpdateAnIssue(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Ticket Id: Text }",
            required = true) Map<String, Object> map) {
        try {
            String issueKey = (String) map.get(JiraResources.TICKET_ID);
            //  String newStatus = (String) map.get(JiraResources.STATUS_NAME);
            String stateId = (String) map.get("stateId");
            ExtensionResponse ticketIdResponse = getTicketIdResponse(map);
            if (ticketIdResponse != null) return ticketIdResponse;
            Map<String, Object> state = internalStateManager.get(stateId);
            Issue issue = new Issue();
            issue.key = issueKey;
            issue.status = (String) state.get("New Status");
            issue.priority = (String) state.get("New priority");
            issue.summary = (String) state.get("New Summary");
            issue.description = (String) map.get("New Description");
            issue.assigneeName = (String) map.get("New Assignee Name");
            issue.labels = (List<String>) map.get("Labels");
            issue.components = (List<String>) map.get("Components");
            String workLog = (String) map.get("Work log");
            FreeForm customFields = (FreeForm) map.get("Custom Fields");
            issueDao.updateIssue(issue, customFields);
            if (workLog != null && !workLog.isBlank()) {
                ritualsArea.addWorklog(issueKey, new Date().getTime(), workLog);
            }
            Issue updatedIssue = issueDao.getIssueFinal(issueKey);
            return ExtensionResponseFactory.create(Map.of("Updated Issue", updatedIssue));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to update an issue",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = "confirmReenterAssignChildIssueToEpic",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterAssignChildIssueToEpic(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Child Issue Key: Text , Parent Epic Key: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get("Reenter");
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = (Map<String, Object>) internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterAssignChildIssueToEpic",
                Map.of("stateId", stateId,
                        JiraResources.CHILD_ISSUE_KEY, map.get(JiraResources.CHILD_ISSUE_KEY), JiraResources.PARENT_EPIC_KEY, map.get(JiraResources.PARENT_EPIC_KEY)));
    }

    @SubCatalogRequest(
            name = "handleReenterAssignChildIssueToEpic",
            description = "Assign Child Issue to Epic",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field(name = "Is successful", type = "Switch", required = false)
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterAssignChildIssueToEpic(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Child Issue Key: Text, Parent Epic Key: Text }",
            required = true) Map<String, Object> map) {
        try {
            String childIssueKey = (String) map.get(JiraResources.CHILD_ISSUE_KEY);
            String parentEpicKey = (String) map.get(JiraResources.PARENT_EPIC_KEY);
            if (!issueDao.isIssueExists(childIssueKey) && !issueDao.isIssueExists(parentEpicKey)) {
                return ExtensionResponseFactory.create("Provided child issue key and parent epic key is not valid : " + "Child issue Key - " + childIssueKey + " And Parent epic key -" + parentEpicKey, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants("Provided child issue key and parent epic key is not valid : " + "Chile issue Key - " + childIssueKey + " Parent epic key -" + parentEpicKey, List.of())),
                        null, Map.of());
            }
            ExtensionResponse childKeyErrorResponse = getChildKeyResponse(map);
            if (childKeyErrorResponse != null) return childKeyErrorResponse;

            ExtensionResponse parentKeyErrorResponse = getParentKeyResponse(map);
            if (parentKeyErrorResponse != null) return parentKeyErrorResponse;

            issueDao.assignChildIssueToEpic(childIssueKey, parentEpicKey);
            return ExtensionResponseFactory.create(Map.of("Is successful", true));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to update priority of issue " + cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = "confirmReenterAddWorkLog",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterAddWorkLog(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Ticket Id: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get("Reenter");
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterAddWorkLog",
                Map.of("stateId", stateId,
                        JiraResources.TICKET_ID, map.get(JiraResources.TICKET_ID)));
    }

    @SubCatalogRequest(
            name = "handleReenterAddWorkLog",
            description = "Add worklog",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field(name = "Is successful", type = "Switch", required = false)
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterAddWorkLog(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Ticket Id: Text}",
            required = true) Map<String, Object> map) {
        try {
            ExtensionResponse ticketIdResponse = getTicketIdResponse(map);
            if (ticketIdResponse != null) return ticketIdResponse;
            String stateId = (String) map.get("stateId");
            Map<String, Object> state = internalStateManager.get(stateId);
            Double startDateTimeDouble = (Double) state.get("Start Date time");
            long startDateTimeMillis = startDateTimeDouble.longValue();
            String timeSpent = (String) state.get("Time Spent");
            issueDao.addWorklog(timeSpent, startDateTimeMillis, (String) map.get(JiraResources.TICKET_ID), "");
            return ExtensionResponseFactory.create(Map.of(
                    "Is successful", true));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to add work log of issue",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    private ExtensionResponse getTicketIdResponse(Map<String, Object> map) {
        String ticketId = (String) map.get(JiraResources.TICKET_ID);
        return validateTicketId(ticketId);
    }

    private ExtensionResponse validateTicketId(String ticketId) {
        if (!issueDao.isIssueExists(ticketId)) {
            return ExtensionResponseFactory.create("Provided ticket id is not valid : " + ticketId, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided ticket id is not valid : " + ticketId, List.of())),
                    null, Map.of());
        }
        return null;
    }

    private ExtensionResponse getChildKeyResponse(Map<String, Object> map) {
        String ticketId = (String) map.get(JiraResources.CHILD_ISSUE_KEY);
        ExtensionResponse ticketIdErrorResponse = validateTicketId(ticketId);
        if (ticketIdErrorResponse != null) return ticketIdErrorResponse;
        return null;
    }

    private ExtensionResponse getParentKeyResponse(Map<String, Object> map) {
        String ticketId = (String) map.get(JiraResources.PARENT_EPIC_KEY);
        ExtensionResponse ticketIdErrorResponse = validateTicketId(ticketId);
        if (ticketIdErrorResponse != null) return ticketIdErrorResponse;
        return null;
    }

}
