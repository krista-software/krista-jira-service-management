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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketPriority;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.RemediationActionFactory;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.Validator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.JQLBuilder;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.WebhookDao;
import app.krista.extensions.devsecops.development.jira.util.FieldValidator;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.base.FreeForm;
import com.kristasoft.common.holders.ThreadLocalProxy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.*;

@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812", name = "Development", ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417", ecosystemName = "Dev Sec Ops", ecosystemVersion = "3ef3d7f7-b7ce-42e9-bdc4-c1b07fc33436")
public final class RitualsArea {

    public static final String ERROR_MESSAGE = "ErrorMessage";
    public static final String IS_ERROR = "IsError";
    public static final String LIST_OF_ISSUES = "List of Issues";
    private final IssueDao issueDao;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final EventHandler eventHandler;
    private final ValidationOrchestrator validationOrchestrator;
    private final ErrorHandlingStateManager internalStateManager;
    private final ExtensionResponseGenerator responseGenerator;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = LoggerFactory.getLogger(RitualsArea.class);
    private final ProjectDao projectDao;

    @Inject
    public RitualsArea(IssueDao issueDao, RequestContext requestContext, AuthorizationContext authorizationContext,
                       EventHandler eventHandler, ValidationOrchestrator validationOrchestrator,
                       ErrorHandlingStateManager internalStateManager, ExtensionResponseGenerator responseGenerator,
                       ProjectDao projectDao) {
        this.issueDao = issueDao;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.eventHandler = eventHandler;
        this.validationOrchestrator = validationOrchestrator;
        this.internalStateManager = internalStateManager;
        this.responseGenerator = responseGenerator;
        this.projectDao = projectDao;
    }

    @CatalogRequest(id = "localDomainRequest_30916ab2-7bbf-4c1d-902f-ce12adeb2d09", name = "Update status of multiple issues", description = "Update status of issues", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Updated issues", type = "[ Entity(Issue) ]", required = false)
    public ExtensionResponse updateStatusOfMultipleIssues(
            @Field(name = "Status Name", type = "Text") String statusName,
            @Field.Desc(name = "Issue Key", type = "[ Text ]") List<String> issueKey) {
        try {
            List<Issue> updatedIssue = new ArrayList<>();
            issueKey.forEach(key -> {
                issueDao.updateStatus(key, statusName);
                updatedIssue.add(issueDao.getIssueFinal(key));
            });
            return ExtensionResponseFactory.create(Map.of("Updated issues", updatedIssue));
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while updating multiple issues :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while updating multiple issues",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_f39c66d5-0862-4b3f-a6bb-badadf5ce7d0", name = "Add a comment", description = "Add comment to issue", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is successful", type = "Switch", required = false)
    public ExtensionResponse addAComment(
            @Field(name = "Comment", type = "Paragraph", required = true, attributes = {}, options = {}) String comment,
            @Field.Text(name = "Issue Key", required = true, attributes = {}, options = {}) String issueKey,
            @Field.PickOne(name = "Comment Visibility", values = {"Public",
                    "Internal"}, required = false, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String commentVisibility) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) {
                try {
                    issueDao.addComment(issueKey, comment, commentVisibility);
                    return ExtensionResponseFactory.create(Map.of("Is successful", true));
                } catch (MustAuthorizeException cause) {
                    throw new MustAuthorizeException("Please authenticate yourself." + cause.getMessage());
                } catch (Exception cause) {
                    return ExtensionResponseFactory.create(cause, "Failed to add comment to the issue",
                            ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
                }
            }
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            return responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForAddAComment", Map.of(JiraConstants.STATE_ID, stateId,
                            JiraResources.TICKET_ID, issueKey,
                            JiraConstants.COMMENT, comment,
                            COMMENT_VISIBILITY, commentVisibility));
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error("Error occurred while adding comment in issue ;{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while adding comment in issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_21823408-60d2-4cec-b39a-b98f806c0638", name = "Get user issues", description = "Get issues by user account id", area = "Rituals", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "User Issues", type = "[ Entity(Issue) ]")
    public ExtensionResponse getUserIssues(
            @Field(name = "User Account Id", type = "Text") String userAccountId) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.USER_ID, userAccountId));
            if (validationResults.isEmpty())
                return ExtensionResponseFactory.create(Map.of(
                        "User Issues", issueDao.searchByJQL(
                                " assignee in (" + userAccountId + ") order by priority DESC, duedate DESC", 30, 1)));
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            return responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForGetUserIssues", Map.of(JiraConstants.STATE_ID, stateId,
                            JiraResources.ACCOUNT_ID, userAccountId));
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error("Error occurred while get issues by user account Id :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while get issues by user account Id",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_054845ea-7fc1-4ad6-b1f3-5eb96a784daa", name = "Update status of issue", description = "Update status of issue", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Updated Issue", type = "Entity(Issue)")
    public ExtensionResponse updateStatusOfIssue(
            @Field(name = "Issue Key", type = "Text") String issueKey,
            @Field(name = "Status Name", type = "Text") String statusName) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty())
                validationResults = validationOrchestrator.validate(
                        Map.of(Validator.ValidationResource.TICKET_ID, issueKey,
                                Validator.ValidationResource.STATUS_NAME, statusName));
            if (validationResults.isEmpty()) {
                issueDao.updateStatus(issueKey, statusName);
                return ExtensionResponseFactory.create(Map.of(
                        "Updated Issue", issueDao.getIssueFinal(issueKey)));
            }
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            return responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForUpdateStatusOfIssue", Map.of(JiraConstants.STATE_ID, stateId,
                            JiraResources.TICKET_ID, issueKey,
                            JiraResources.STATUS_NAME, statusName));
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error("Error occurred while updating status of issue :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while updating status of issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_33949891-0ba1-43dd-8d2f-170596a74a00", name = "Assign a task to user", description = "Assign a task to user", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Updated issue details", type = "Entity(Issue)")
    public ExtensionResponse assignATaskToUser(
            @Field(name = "To assign user account id", type = "Text") String toAssignUserAccountId,
            @Field(name = "Issue key", type = "Text") String issueKey) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey,
                            Validator.ValidationResource.USER_ID, toAssignUserAccountId));
            if (validationResults.isEmpty()) {
                issueDao.assignTask(issueKey, toAssignUserAccountId);
                return ExtensionResponseFactory.create(Map.of(
                        "Updated issue details", issueDao.getIssueFinal(issueKey)));
            }
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            return responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterAssignATaskToUser", Map.of(
                            JiraConstants.STATE_ID, stateId,
                            JiraResources.TICKET_ID, issueKey, JiraResources.ACCOUNT_ID, toAssignUserAccountId));
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error("Error occurred while assigning task to user :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while assigning task to user",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_ac7c50a8-f72f-4b71-870b-d3db381a7b05", name = "Fetch by JQL query", description = "Fetch list of issues by JQL query", area = "Rituals", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Task ID", type = "Text", required = false)
    public ExtensionResponse fetchByJQLQuery(@Field(name = "JQL Query", type = "Paragraph") String jQLQuery) {
        try {
            boolean invokeAsUser = requestContext.invokeAsUser();
            if (invokeAsUser) {
                issueDao.validateCredentials();
            }
            String invokerId = requestContext.getInvokerId();
            String accountID = authorizationContext.getAuthorizedAccount().getAccountId();
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.JQL_QUERY, jQLQuery));
            final String taskId = UUID.randomUUID().toString();
            if (validationResults.isEmpty()) {
                Map<Class<?>, Object> threadLocals = ThreadLocalProxy.getAll();
                executorService.submit(() -> {
                    ThreadLocalProxy.setAll(threadLocals);
                    List<Issue> issues;
                    boolean isError = false;
                    FreeForm freeForm = new FreeForm();
                    try {
                        issues = issueDao.searchByJqlQuery(invokeAsUser, accountID, invokerId, jQLQuery,
                                JiraConstants.DEFAULT_PAGE_SIZE, JiraConstants.PAGE_NUMBER);
                        LOGGER.info(" fetchByJQLQuery taskId : {} issues size : {}", taskId, issues.size());
                        freeForm.put(IS_ERROR, "Switch", false);
                        freeForm.put("Data", "[ Entity(Issue) ]", issues);
                    } catch (Exception cause) {
                        freeForm.put(IS_ERROR, "Switch", true);
                        freeForm.put(ERROR_MESSAGE, "Text",
                                "Error occurred while fetching JQL - " + cause.getMessage());
                    }

                    eventHandler.handleEvent(taskId, freeForm);
                });
                return ExtensionResponseFactory.create(Map.of(
                        "Task ID", taskId));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId,
                        JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults,
                                "Task ID", taskId, "AccountID", accountID)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterJQLQuery", Map.of(
                                JiraConstants.STATE_ID, stateId, JQL_QUERY, jQLQuery));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error(cause.getMessage(), cause);
            return ExtensionResponseFactory.create(cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(cause.getMessage(), List.of())),
                    null, Map.of());
        }
    }

    @CatalogRequest(id = "localDomainRequest_51d3362e-bce5-47b5-b2f0-c458522dcfcb", name = "Get Results for Fetch Issues by JQL Query", description = "Fetch Issues by JQL Query Async", area = "Rituals", type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "List of Issues", type = "[ Entity(Issue) ]", required = false)
    public ExtensionResponse getResultsForFetchIssuesByJQLQuery(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData,
            @Field(name = "Task ID", type = "Text", required = true, attributes = {}, options = {}) String taskID) {
        LOGGER.info(" Started getResultsForFetchIssuesByJQLQuery taskID : {} & eventName : {}", taskID, eventName);
        if (eventName.equals(taskID)) {
            if (Boolean.TRUE.equals(eventData.get(IS_ERROR))) {
                LOGGER.error("Invalid JQL query :{}", eventData.get(ERROR_MESSAGE));
                return ExtensionResponseFactory.create(eventData.get(ERROR_MESSAGE).toString(),
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory
                                .createInformActionALLParticipants(eventData.get(ERROR_MESSAGE).toString(), List.of())),
                        null, Map.of());
            } else {
                try {
                    List<Issue> issueList = (List<Issue>) eventData.get("Data");
                    LOGGER.info(" issueList size : {}", issueList.size());
                    if (issueList.isEmpty()) {
                        return ExtensionResponseFactory.create(Map.of(
                                LIST_OF_ISSUES, Collections.emptyList()));
                    }
                    return ExtensionResponseFactory.create(Map.of(LIST_OF_ISSUES, issueList));
                } catch (MustAuthorizeException cause) {
                    LOGGER.error(cause.getMessage(), cause);
                    throw cause;
                } catch (ClassCastException cause) {
                    LOGGER.debug("Unable to cast eventData to List<Issue> : {}", cause.getMessage(), cause);
                    return ExtensionResponseFactory.create(Map.of(
                            LIST_OF_ISSUES, Collections.emptyList()));
                }
            }
        }
        throw new IllegalStateException("Invalid task ID");
    }

    @CatalogRequest(id = "localDomainRequest_9f1e46ff-b952-454e-b19f-ea6c79c3c2e0", name = "Get my issues by given statuses", description = "Get issue list for the current user", area = "Rituals", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Issues", type = "[ Entity(Issue) ]")
    public ExtensionResponse getMyIssuesByGivenStatuses(
            @Field.Desc(name = "Status Names", type = "[ Text ]") List<String> statusNames) {
        try {
            String statusCommaSeparated = statusNames.stream()
                    .map(status -> "'" + status + "'")
                    .collect(Collectors.joining(","));
            List<Issue> issues = issueDao.searchByJQL("status in (" + statusCommaSeparated
                    + ") AND assignee in (currentUser()) ORDER BY priority DESC, updated DESC", 30, 1);
            return ExtensionResponseFactory.create(Map.of("Issues", issues));
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error("Invalid query provided, Please check :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Invalid query provided, Please check.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory
                            .createInformActionALLParticipants("Invalid query provided, Please check.", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(id = "localDomainRequest_554940f4-188c-47eb-b0b6-768fc99b1399", name = "Get issues", description = "Get issues by given filters", area = "Rituals", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "List of Filtered Issues", type = "[ Entity(Issue) ]")
    public ExtensionResponse getIssues(
            @Field(name = "Summary", type = "Text", required = false) String summary,
            @Field(name = "Priority", type = "Text", required = false) String priority,
            @Field(name = "Issue Type", type = "Text", required = false) String issueType,
            @Field(name = "Status", type = "Text", required = false) String status,
            @Field(name = "Assignee", type = "Text", required = false) String assignee,
            @Field(name = "Project Key", type = "Text", required = false) String projectKey,
            @Field(name = "Page Size", type = "Number", required = false) Double pageSize,
            @Field(name = "Page Number", type = "Number", required = false) Double pageNumber) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = new ArrayList<>();
            if (projectKey != null && !projectKey.isBlank()) {
                validationResults.addAll(validationOrchestrator.validate(
                        Map.of(Validator.ValidationResource.PROJECT_KEY,
                                FieldValidator.getNullOrEmptyDefault(projectKey))));
            }
            if (issueType != null && !issueType.isBlank() && (projectKey != null && !projectKey.isBlank()
                    && projectDao.isProjectExist(FieldValidator.getNullOrEmptyDefault(projectKey)))) {
                validationResults = validationOrchestrator.validate(
                        Map.of(Validator.ValidationResource.ISSUE_TYPE,
                                FieldValidator.getNullOrEmptyDefault(issueType)));
            }
            if (validationResults.isEmpty()) {
                String jql = new JQLBuilder().status(status)
                        .assignee(assignee)
                        .priority(priority)
                        .summary(summary)
                        .projectKey(projectKey)
                        .issueType(issueType)
                        .orderBy("priority DESC, updated", "desc")
                        .build();
                List<Issue> issues = issueDao.searchByJQL(jql,
                        (pageSize != null && pageSize > 0) ? pageSize.longValue() : 20,
                        (pageNumber != null && pageNumber > 0) ? pageNumber.longValue() : 1);
                return ExtensionResponseFactory.create(Map.of("List of Filtered Issues", issues));
            } else {
                String stateId = UUID.randomUUID().toString();
                Map<String, Object> filedMap = new LinkedHashMap<>();
                filedMap.put(JiraConstants.VALIDATION_RESULT, validationResults);
                filedMap.put(JiraConstants.STATUS, status);
                filedMap.put(JiraConstants.ASSIGNEE, assignee);
                filedMap.put(JiraConstants.PRIORITY, priority);
                filedMap.put(JiraConstants.SUMMARY, summary);
                filedMap.put("Page Size", pageSize);
                filedMap.put("Page Number", pageNumber);
                internalStateManager.put(stateId, GSON_BUILDER.toJson(filedMap));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterGetIssues",
                        Map.of(JiraConstants.STATE_ID, stateId, JiraResources.PROJECT_KEY, projectKey));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error("Error occurred while getting issues by using filters :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause,
                    "Error occurred while getting issues by using filters " + cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }

    }

    @CatalogRequest(id = "localDomainRequest_f3f1f7b6-9131-462d-bd1c-b80979b905cc", name = "Get user issue by issue type", description = "Get issue list by issue type and user account id", area = "Rituals", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Issues", type = "[ Entity(Issue) ]")
    public ExtensionResponse getUserIssueByIssueType(
            @Field(name = "User account Id", type = "Text") String userAccountId,
            @Field(name = "Issue Type", type = "Text") String issueType) {
        try {
            List<Issue> issues = issueDao.searchByJQL("issuetype = \"" + issueType + "\"  AND assignee in ("
                    + userAccountId + ") order by priority DESC, duedate DESC", 30, 1);
            return ExtensionResponseFactory.create(Map.of("Issues", issues));
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error("Invalid query provided, Please check :{}", cause.getMessage());
            return ExtensionResponseFactory.create("Invalid query provided, Please check.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory
                            .createInformActionALLParticipants("Invalid query provided, Please check.", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(id = "localDomainRequest_806b731e-19d5-4c65-8855-06a44cd14f17", name = "Add Request Participants to Issue", description = "Add Request Participants to Issue", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Request Participants Successfully", required = false, attributes = {
            @Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse addRequestParticipantsToIssue(
            @Field.Text(name = "Issue Key", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String issueKey,
            @Field.Desc(name = "Request Participants", type = "[ Text ]", required = true) List<String> requestParticipants) {
        LOGGER.info("Add Request Participants Initialised : Issue Key : {}\nRequest Participants : {}", issueKey,
                requestParticipants);
        issueDao.addRequestParticipants(issueKey, requestParticipants);
        return ExtensionResponseFactory.create(Map.of("Request Participants Successfully", true));
    }

    @CatalogRequest(id = "localDomainRequest_2087c0b0-dff9-4001-bbd8-cf9933aa06d5", name = "Update an issue", description = "Update an issue with given details", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Updated Issue", type = "Entity(Issue)")
    public ExtensionResponse updateAnIssue(
            @Field(name = "Issue Key", type = "Text") String issueKey,
            @Field(name = "New Status", type = "Text", required = false) String newStatus,
            @Field(name = "New priority", type = "Text", required = false) String newPriority,
            @Field(name = "New Summary", type = "Text", required = false) String newSummary,
            @Field(name = "New Description", type = "RichText", required = false) String newDescription,
            @Field(name = "New Assignee Name", required = false, type = "Text") String newAssigneeName,
            @Field(name = "Work log", type = "Text", required = false) String workLog,
            @Field.Desc(name = "Labels", type = "[ Text ]", required = false) List<String> labels,
            @Field.Desc(name = "Components", type = "[ Text ]", required = false) List<String> components,
            @Field(name = "Custom Fields", type = "FreeForm") FreeForm customFields) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) {
                Issue issue = new Issue();
                issue.key = issueKey;
                issue.status = newStatus;
                issue.priority = newPriority;
                issue.summary = newSummary;
                issue.description = newDescription;
                issue.assigneeName = newAssigneeName;
                issue.labels = labels;
                issue.components = components;
                issueDao.updateIssue(issue, customFields);
                if (workLog != null && !workLog.isBlank()) {
                    addWorklog(issueKey, new Date().getTime(), workLog);
                }
                Issue updatedIssue = issueDao.getIssueFinal(issueKey);
                return ExtensionResponseFactory.create(Map.of("Updated Issue", updatedIssue));
            } else {
                String stateId = UUID.randomUUID().toString();
                Map<String, Object> filedMap = new LinkedHashMap<>();
                filedMap.put("Validation Results", validationResults);
                filedMap.put("New priority", newPriority);
                filedMap.put("New Status", newStatus);
                filedMap.put("New Summary", newSummary);
                filedMap.put("New Description", newDescription);
                filedMap.put("New Assignee Name", newAssigneeName);
                filedMap.put("Work log", workLog);
                filedMap.put("Labels", labels);
                filedMap.put("Components", components);
                filedMap.put("Custom Fields", customFields);
                internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(filedMap));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterForUpdateAnIssue", Map.of(JiraConstants.STATE_ID, stateId,
                                JiraResources.TICKET_ID, issueKey));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error("Error occurred while updating issue :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while updating issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_3712bbb2-daaa-42d9-9a36-fe0e55738236", name = "Update priority of issue", description = "Update priority of the issue", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Updated Issue", type = "Entity(Issue)")
    public ExtensionResponse updatePriorityOfIssue(
            @Field(name = "Issue Key", type = "Text") String issueKey,
            @Field(name = "Priority Name", type = "Text") String priorityName) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) {
                TicketPriority priority = new TicketPriority();
                priority.name = priorityName;
                return ExtensionResponseFactory.create(Map.of(
                        "Updated Issue", issueDao.updatePriority(priority, issueKey)));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, GSON_BUILDER
                        .toJson(Map.of(VALIDATION_RESULT, validationResults, "Priority Name", priorityName)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterUpdatePriorityOfIssue", Map.of(
                                "stateId", stateId,
                                JiraResources.TICKET_ID, issueKey));
            }

        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error("Error occurred while updating priority of issue :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while updating priority of issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_ba64194f-f531-453e-b83d-597f646c923f", name = "Update the priority of multiple issues", description = "Update the priority of multiple issues", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = LIST_OF_ISSUES, type = "[ Entity(Issue) ]", required = false)
    public ExtensionResponse updateThePriorityOfMultipleIssues(
            @Field.Desc(name = "Issue Keys", type = "[ Text ]") List<String> issueKeys,
            @Field(name = "New Priority", type = "Text") String newPriority) {
        try {
            List<Issue> updatedIssue = new ArrayList<>();
            issueKeys.forEach(key -> {
                issueDao.updatePriority(new TicketPriority(newPriority), key);
                updatedIssue.add(issueDao.getIssueFinal(key));
            });
            return ExtensionResponseFactory.create(Map.of(LIST_OF_ISSUES, updatedIssue));
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Error occurred while updating priority of multiple issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }

    }

    @CatalogRequest(id = "localDomainRequest_1e62b36a-609b-4156-acba-8f6f8109c1d0", name = "Add worklog", description = "Add worklog", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is successful", type = "Switch", required = false)
    public ExtensionResponse addWorklog(
            @Field(name = "Issue Key", type = "Text") String issueKey,
            @Field(name = "Start Date time", type = "Date") Long startDateTime,
            @Field(name = "Time Spent", type = "Text") String timeSpent) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) {
                issueDao.addWorklog(timeSpent, startDateTime, issueKey, "");
                return ExtensionResponseFactory.create(Map.of(
                        "Is successful", true));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(VALIDATION_RESULT,
                        validationResults, "Start Date time", startDateTime, "Time Spent", timeSpent)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterAddWorkLog", Map.of(
                                JiraConstants.STATE_ID, stateId,
                                JiraResources.TICKET_ID, issueKey));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Error occurred in work log",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_3f532311-cd58-4a79-a727-5d12a9409319", name = "Get Issue By Key", description = "Get issue by issue key", area = "Rituals", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Issue", type = "Entity(Issue)")
    public ExtensionResponse getIssueByKey(
            @Field(name = "Issue Key", type = "Text") String issueKey) {
        try {
            LOGGER.info("Get Issue By Key stage 1 initialised : {}", issueKey);
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) {
                return ExtensionResponseFactory.create(Map.of(
                        "Issue", issueDao.getIssueFinal(issueKey)));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, GSON_BUILDER.toJson(Map.of(VALIDATION_RESULT, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterGetIssueByKey", Map.of(
                                JiraConstants.STATE_ID, stateId,
                                JiraResources.TICKET_ID, issueKey));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Error occurred while getting issue by issue key.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_42220d05-f6f1-447c-918a-b3efbf6e17e2", name = "Assign Child Issue to Epic", description = "Assign Child Issue to Epic", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is successful", type = "Switch", required = false)
    public ExtensionResponse assignChildIssueToEpic(
            @Field(name = "Child Issue Key", type = "Text") String childIssueKey,
            @Field(name = "Parent Epic Key", type = "Text") String parentEpicKey) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.CHILD_ISSUE_KEY, childIssueKey,
                            Validator.ValidationResource.PARENT_EPIC_KEY, parentEpicKey));
            if (validationResults.isEmpty()) {
                issueDao.assignChildIssueToEpic(childIssueKey, parentEpicKey);
                return ExtensionResponseFactory.create(Map.of("Is successful", true));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, GSON_BUILDER.toJson(Map.of(VALIDATION_RESULT, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterAssignChildIssueToEpic", Map.of(
                                JiraConstants.STATE_ID, stateId,
                                JiraResources.CHILD_ISSUE_KEY, childIssueKey, JiraResources.PARENT_EPIC_KEY,
                                parentEpicKey));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Error occurred while assigning child issue to parent epic.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_0e9b3e9b-3e15-43e9-8530-de879cf03e69", name = "Assign Issue to Epic", description = "Assign Issue to Epic", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is successful", type = "Switch", required = false)
    public ExtensionResponse assignIssueToEpic(
            @Field(name = "Issue Key", type = "Text") String issueKey,
            @Field(name = "Epic Key", type = "Text") String epicKey) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(
                    Validator.ValidationResource.TICKET_ID, issueKey, Validator.ValidationResource.EPIC_KEY, epicKey));
            if (validationResults.isEmpty()) {
                issueDao.assignIssueToEpic(issueKey, epicKey);
                return ExtensionResponseFactory.create(Map.of("Is successful", true));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, GSON_BUILDER.toJson(Map.of(VALIDATION_RESULT, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterAssignIssueToEpic", Map.of(
                                JiraConstants.STATE_ID, stateId,
                                JiraResources.TICKET_ID, issueKey, JiraResources.EPIC_KEY, epicKey));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            JSONObject jsonObject = new JSONObject(cause.getMessage());
            JSONArray errorMessages = (JSONArray) jsonObject.get("errorMessages");
            return ExtensionResponseFactory.create(cause,
                    "Error occurred while assigning issue to epic." + errorMessages.get(0).toString(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_ded0f2e0-8a1e-4b6a-9f7a-2c8a927e1f4b",
            name = "Get all comments",
            description = "Get all comments from an issue",
            area = "Rituals", type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Comments", type = "[ { Comment Body: Text, Author ID: Text, Author Name: Text, Date Created: Date } ]")
    public ExtensionResponse getAllComments(
            @Field(name = "Issue Key", type = "Text") String issueKey) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) {
                return ExtensionResponseFactory.create(Map.of(
                        "Comments", issueDao.getComments(issueKey)));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, GSON_BUILDER.toJson(Map.of(VALIDATION_RESULT, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterGetAllComments", Map.of(
                                JiraConstants.STATE_ID, stateId,
                                JiraResources.TICKET_ID, issueKey));
            }
        } catch (MustAuthorizeException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            LOGGER.error("Error occurred while getting all comments from issue :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while getting all comments from issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(id = "localDomainRequest_a6b3b600-16d6-4ddb-93ba-487d06d1baa8", name = "Get Notification", description = "Get Constant Updates using webhook", area = "Rituals", type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "Update", type = "[ Composite ]", required = false)
    @Field.Text(name = "Issue Key", required = false, attributes = {
            @Attribute(name = "visualWidth", value = "S")}, options = {})
    public Map<String, Object> getNotification(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData) {
        if (eventName.equals(JIRA_UPDATES)) {
            WebhookDao webhookDao = new WebhookDao();
            return webhookDao.parseWebhookResponse((String) eventData.get(UPDATES_ON_JIRA));
        }
        return Map.of();
    }

    @CatalogRequest(id = "localDomainRequest_0a5052cd-bd83-4a0a-82c3-861ed7090a19", name = "Get Custom Field Value", description = "Get data of custom field", area = "Rituals", type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "Response", type = "[ Text ]", required = false)
    public List<String> getCustomFieldValue(
            @Field.Text(name = "Issue Key", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String issueKey,
            @Field.Text(name = "Custom Field", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String customField) {
        return issueDao.getCustomIssue(issueKey, customField);

    }

}