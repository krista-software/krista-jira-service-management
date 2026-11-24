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
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.sdk.atlassian.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.sdk.atlassian.extresp.RemediationActionFactory;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.KristaMediaClient;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.IssueMapper;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import com.google.gson.reflect.TypeToken;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

@Service
public class OrganizationAreaSubCatalogRequests {

    private final IssueDao issueDao;
    private final ProjectDao projectDao;
    private final UserDao userDao;
    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;
    private final KristaMediaClient kristaMediaClient;
    private static final Logger logger = LoggerFactory.getLogger(OrganizationAreaSubCatalogRequests.class);

    @Inject
    public OrganizationAreaSubCatalogRequests(IssueDao issueDao, ProjectDao projectDao, UserDao userDao, ExtensionResponseGenerator responseGenerator, ErrorHandlingStateManager internalStateManager, KristaMediaClient kristaMediaClient) {
        this.projectDao = projectDao;
        this.userDao = userDao;
        this.issueDao = issueDao;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
        this.kristaMediaClient = kristaMediaClient;
    }

    @SubCatalogRequest(
            name = "confirmReenterTicketId",
            description = "Checks if user wants to re enter ticket Id and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterTicketId(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text  }", required = true) Map<String, Object> map) {
        Boolean reenterTicketId = (Boolean) map.get("Reenter");
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (!reenterTicketId) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null, Map.of(JiraConstants.STATE_ID, stateId));
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenteredTicketId", Map.of(JiraConstants.STATE_ID, stateId));
    }

    @SubCatalogRequest(
            name = "handleReenteredTicketId",
            description = "Use the new ticket Id to update already uploaded files in that",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field(name = "Upload Status", type = "Switch")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenteredTicketId(@Field.Desc(name = "inputMap",
            type = "{ Ticket Id: Text, stateId: Text }", required = true) Map<String, Object> map) {
        try {
            String ticketId = (String) map.get(JiraResources.TICKET_ID);
            ExtensionResponse ticketIdErrorResponse = ticketIdValidator(ticketId);
            if (ticketIdErrorResponse != null) return ticketIdErrorResponse;
            String stateId = (String) map.get("stateId");
            Map<String, Object> state = internalStateManager.get(stateId);
            List<app.krista.model.base.File> file = getFileList(state);
            List<java.io.File> ticketFiles = new ArrayList<>();
            Type fileListType = new TypeToken<ArrayList<app.krista.model.base.File>>() {
            }.getType();
            file = GSON.fromJson(GSON.toJson(file), fileListType);
            for (app.krista.model.base.File currentFile : file) {
                ticketFiles.add(kristaMediaClient.toJavaFile(currentFile));
            }
            return ExtensionResponseFactory.create(Map.of("Upload Status",
                    issueDao.attachFileToTicket(ticketFiles, ticketId)));
        } catch (Exception cause) {
            logger.error("Failed to update files in ticket : {}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Failed to update files in ticket : " + cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = "confirmReenterForCreateIssue",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForCreateIssue(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Project Key: Text, Summary: Text, Description: Text, Issue Type: Text }",
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
                validationResults, "handleReenteredForCreateIssue",
                Map.of(JiraConstants.STATE_ID, stateId, JiraResources.PROJECT_KEY, map.get(JiraResources.PROJECT_KEY),
                        JiraResources.SUMMARY, map.get(JiraResources.SUMMARY),
                        JiraResources.DESCRIPTION, map.get(JiraResources.DESCRIPTION),
                        JiraResources.ISSUE_TYPE, map.get(JiraResources.ISSUE_TYPE)));
    }

    @SubCatalogRequest(
            name = "handleReenteredForCreateIssue",
            description = "Use the new details to create the new ticket",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "New Issue", type = "Entity(Issue)")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenteredForCreateIssue(@Field.Desc(name = "inputMap",
            type = "{ Valid Project Key: Text, stateId: Text, Project Key: Text, Summary: Text, Description: Text, Issue Type: Text  }",
            required = true) Map<String, Object> map) {
        try {
            String projectKey = (String) map.get(JiraResources.PROJECT_KEY);
            String summary = (String) map.get(JiraResources.SUMMARY);
            String requestType = (String) map.get("Request Type");
            ExtensionResponse projectKeyErrorResponse = getProjectKeyResponse(map);
            if (projectKeyErrorResponse != null) return projectKeyErrorResponse;
            ExtensionResponse summaryErrorResponse = getSummaryErrorResponse(map);
            if (summaryErrorResponse != null) return summaryErrorResponse;
            return ExtensionResponseFactory.create(Map.of("New Issue",
                    issueDao.createIssueWithRequestTypeName(projectKey, summary,
                            (String) map.get(JiraResources.DESCRIPTION),
                            null, (String) map.get(JiraResources.ISSUE_TYPE),
                            requestType
                    )));
        } catch (Exception cause) {
            logger.error("Failed to create ticket : {}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Failed to create ticket : " + cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = "confirmReenterForReportABug",
            description = "Checks if user wants to re enter details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForReportABug(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Project Key: Text, Summary: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get("Reenter");
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenteredForReportABug",
                Map.of(JiraConstants.STATE_ID, stateId, JiraResources.PROJECT_KEY, map.get(JiraResources.PROJECT_KEY),
                        JiraResources.SUMMARY, map.get(JiraResources.SUMMARY)));
    }

    @SubCatalogRequest(
            name = "handleReenteredForReportABug",
            description = "Use the new data to create the new bug",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "New Bug", type = "Entity(Issue)")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenteredForReportABug(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Project Key: Text, Summary: Text }",
            required = true) Map<String, Object> map) {
        try {
            ExtensionResponse summaryAndProjectError = summaryAndProjectErrorResponse(map);
            if (summaryAndProjectError != null) return summaryAndProjectError;
            ExtensionResponse summaryErrorResponse = summaryValidator(map.get(JiraResources.SUMMARY).toString());
            if (summaryErrorResponse != null) return summaryErrorResponse;
            ExtensionResponse projectKeyErrorResponse = projectKeyValidator(map.get(JiraResources.PROJECT_KEY).toString());
            if (projectKeyErrorResponse != null) return projectKeyErrorResponse;

            return ExtensionResponseFactory.create(
                    Map.of("New Bug", IssueMapper.getBug(issueDao.createIssue((String) map.get(JiraResources.PROJECT_KEY),
                            (String) map.get(JiraResources.SUMMARY), (String) map.get(JiraResources.SUMMARY), null, JiraConstants.BUG))));
        } catch (Exception cause) {
            logger.error("Failed to report the bug :{} ", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Failed to report the bug. " + cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = "confirmReenterForUpdateUser",
            description = "Checks if user wants to re enter details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForUpdateUser(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Account Id: Text, Group Names: [Text] }",
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
                validationResults, "handleReenteredForUpdateUser",
                Map.of(JiraConstants.STATE_ID, stateId, JiraResources.ACCOUNT_ID, map.get(JiraResources.ACCOUNT_ID),
                        JiraConstants.GROUP_NAMES, map.get(JiraConstants.GROUP_NAMES)));
    }

    @SubCatalogRequest(
            name = "handleReenteredForUpdateUser",
            description = "Use the new details to update user",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "User Added", type = "Switch")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenteredForUpdateUser(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Account Id: Text, Group Names: [Text] }",
            required = true) Map<String, Object> map) {
        try {
            ExtensionResponse accountIdErrorResponse = accountIdValidator(map);
            if (accountIdErrorResponse != null) return accountIdErrorResponse;
            return ExtensionResponseFactory.create(
                    Map.of("User Added", issueDao.updateUser((String) map.get(JiraResources.ACCOUNT_ID),
                            (List<String>) map.get(JiraConstants.GROUP_NAMES))));
        } catch (Exception cause) {
            logger.error("Failed to update the user. : {}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Failed to update the user.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }

    }


    @SubCatalogRequest(
            name = "confirmReenterForDeleteUser",
            description = "Checks if user wants to re enter details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForDeleteUser(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Account Id: Text }",
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
                validationResults, "handleReenterForDeleteUser",
                Map.of(JiraConstants.STATE_ID, stateId, JiraResources.ACCOUNT_ID, map.get(JiraResources.ACCOUNT_ID)));
    }

    @SubCatalogRequest(
            name = "handleReenterForDeleteUser",
            description = "Use the new details to delete user",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "Deleted", type = "Switch")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterForDeleteUser(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Account Id: Text }",
            required = true) Map<String, Object> map) {
        try {
            ExtensionResponse accountIdErrorResponse = accountIdValidator(map);
            if (accountIdErrorResponse != null) return accountIdErrorResponse;
            return ExtensionResponseFactory.create(
                    Map.of("Deleted", issueDao.deleteUser((String) map.get(JiraResources.ACCOUNT_ID))));
        } catch (Exception cause) {
            logger.error("Failed to delete the user :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Failed to delete the user.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @NotNull
    private static List<ValidationOrchestrator.ValidationResult> getValidationResults(Map<String, Object> state) {
        List<ValidationOrchestrator.ValidationResult> validationResults = new ArrayList<>();
        List<?> results = (List<?>) state.get(JiraConstants.VALIDATION_RESULT);
        for (Object item : results) {
            validationResults.add(GSON.fromJson(GSON.toJson(item), ValidationOrchestrator.ValidationResult.class));
        }
        return validationResults;
    }

    @NotNull
    private static List<app.krista.model.base.File> getFileList(Map<String, Object> state) {
        List<app.krista.model.base.File> fileList = new ArrayList<>();
        if (state.containsKey("File")) {
            List<?> results = (List<?>) state.get("File");
            for (Object item : results) {
                fileList.add(JiraConstants.GSON_BUILDER.fromJson(JiraConstants.GSON_BUILDER.toJson(item), app.krista.model.base.File.class));
            }
        }
        return fileList;
    }

    private ExtensionResponse ticketIdValidator(String ticketId) {
        if (!issueDao.isIssueExists(ticketId)) {
            return ExtensionResponseFactory.create("Provided ticket id is not valid : " + ticketId, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided ticket id is not valid : " + ticketId, List.of())),
                    null, Map.of());
        }
        return null;
    }

    private ExtensionResponse projectKeyValidator(String projectKey) {
        if (!projectDao.isProjectExist(projectKey)) {
            return ExtensionResponseFactory.create("Provided project key is not valid : " + projectKey, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided project key is not valid : " + projectKey, List.of())),
                    null, Map.of());
        }
        return null;
    }

    private static ExtensionResponse summaryValidator(String summary) {
        if (summary.length() > 255) {
            return ExtensionResponseFactory.create("Provided Summary is more than 255 characters : " + summary, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided Summary is more than 255 characters : " + summary, List.of())),
                    null, Map.of());
        }
        return null;
    }

    private ExtensionResponse summaryAndProjectErrorResponse(Map<String, Object> map) {
        if (map.get(JiraResources.SUMMARY).toString().length() > 255 && !projectDao.isProjectExist(map.get(JiraResources.PROJECT_KEY).toString())) {
            return ExtensionResponseFactory.create("Provided project key is not valid : " + map.get(JiraResources.PROJECT_KEY).toString() + " And provided summary is more than 255 characters : " + map.get(JiraResources.SUMMARY).toString(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided project key is not valid : " + map.get(JiraResources.PROJECT_KEY).toString() + " And provided summary is more than 255 characters : " + map.get(JiraResources.SUMMARY).toString(), List.of())),
                    null, Map.of());
        }
        return null;
    }

    private ExtensionResponse accountIdValidator(Map<String, Object> map) {
        if (!userDao.isUserExist(map.get(JiraResources.ACCOUNT_ID).toString())) {
            return ExtensionResponseFactory.create("User is not exist for provided Account Id : " + map.get(JiraResources.ACCOUNT_ID).toString(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("User is not exist for provided Account Id : " + map.get(JiraResources.ACCOUNT_ID).toString(), List.of())),
                    null, Map.of());
        }
        return null;
    }


    ExtensionResponse getSummaryErrorResponse(Map<String, Object> map) {
        String summary = (String) map.get(JiraResources.SUMMARY);
        return summaryValidator(summary);
    }

    ExtensionResponse getProjectKeyResponse(Map<String, Object> map) {
        String projectKey = (String) map.get(JiraResources.PROJECT_KEY);
        return projectKeyValidator(projectKey);
    }

}
