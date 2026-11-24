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
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.devsecops.development.jira.api.JsmAuditService;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.sdk.atlassian.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.Validator;
import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.KristaMediaClient;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.FieldValidator;
import app.krista.extensions.devsecops.development.jira.util.IssueMapper;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import app.krista.model.base.File;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "87b4a4a9-1870-4c82-abea-bc71b22d3081")
public final class OrganizationArea {
    private final Invoker invoker;
    private final IssueDao issueDao;
    private final UserDao userDao;
    private final KristaMediaClient kristaMediaClient;
    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;
    private final ValidationOrchestrator validationOrchestrator;
    private final TelemetryMetrics telemetryMetrics;
    private final RequestContext requestContext;
    private final JsmAuditService jsmAuditService;
    private final Logger logger = LoggerFactory.getLogger(OrganizationArea.class);

    @Inject
    public OrganizationArea(Invoker invoker, IssueDao issueDao, UserDao userDao, KristaMediaClient kristaMediaClient,
                            ExtensionResponseGenerator responseGenerator,
                            ErrorHandlingStateManager internalStateManager, ValidationOrchestrator validationOrchestrator,
                            TelemetryMetrics telemetryMetrics, RequestContext requestContext,
                            JsmAuditService jsmAuditService) {
        this.invoker = invoker;
        this.issueDao = issueDao;
        this.userDao = userDao;
        this.kristaMediaClient = kristaMediaClient;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
        this.validationOrchestrator = validationOrchestrator;
        this.telemetryMetrics = telemetryMetrics;
        this.requestContext = requestContext;
        this.jsmAuditService = jsmAuditService;
    }

    @CatalogRequest(
            id = "localDomainRequest_6f69c3d3-601b-4ca6-ace2-b0cd242ab26e",
            name = "Attach File to Ticket",
            description = "Attach the file to jira ticket",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Upload Status", type = "Switch")
    public ExtensionResponse attachFileToTicket(
            @Field(name = "Ticket Id", type = "Identifier") String ticketId,
            @Field(name = "File", type = "File") List<File> file) {
        String invokerId = requestContext.getInvokerId();
        jsmAuditService.triggered(invokerId, "Attach File to Ticket");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
            List<java.io.File> ticketFiles = new ArrayList<>();
            Type fileListType = new TypeToken<ArrayList<File>>() {
            }.getType();
            file = GSON.fromJson(GSON.toJson(file), fileListType);
            for (File currentFile : file) {
                try {
                    ticketFiles.add(kristaMediaClient.toJavaFile(currentFile));
                } catch (IOException cause) {
                    logger.debug("Unable to process the file {}", currentFile.getFileName());
                }
            }
            if (validationResults.isEmpty()) {
                boolean uploadStatus = issueDao.attachFileToTicket(ticketFiles, ticketId);
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("Upload Status", uploadStatus));
            }
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_RETRY_ATTEMPT, 1);
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraResources.TICKET_ID, ticketId,
                            "File", file,
                            JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterTicketId", Map.of(JiraConstants.STATE_ID, stateId));
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jsmAuditService.authFailure(invokerId, "Attach File to Ticket", cause);
            logger.error("Authorization failed for attachFileToTicket: {}", cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Attach File to Ticket", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Attach File to Ticket", cause);
            logger.error("Error occurred while attaching file to ticket :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while attaching file to ticket",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jsmAuditService.success(invokerId, "Attach File to Ticket");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_f9dd5785-388c-4e41-acd6-155405c6aa9a",
            name = "Get Request Type Field Value",
            description = "Accepts any Issue Key to get Request Type Field Value, gathers portal key and key of Request Type",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Text(name = "Field Value", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse getRequestTypeFieldValue(
            @Field.Text(name = "Issue Key", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String issueKey,
            @Field.Text(name = "Request Type Name", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S"),
                    @Attribute(name = "toolTip", value = "'Note the Request Type Name is Case Sensitive'")}, options = {}) String requestTypeName) {
        String invokerId = requestContext.getInvokerId();
        jsmAuditService.triggered(invokerId, "Get Request Type Field Value");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_REQUEST_TYPE_FIELD_VALUE_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            logger.info("Get Request Type Field Value initialised : Issue Key : {} \n Request Type Name : {}", issueKey,
                    requestTypeName);
            String fieldValue = issueDao.getRequestTypeFieldValue(issueKey, requestTypeName);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_REQUEST_TYPE_FIELD_VALUE_REQUEST_SUCCESS, 1);
            return ExtensionResponseFactory
                    .create(Map.of("Field Value", fieldValue != null ? fieldValue : "Invalid Issue key or Request Type"));
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jsmAuditService.authFailure(invokerId, "Get Request Type Field Value", cause);
            logger.error("Authorization failed for getRequestTypeFieldValue: {}", cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_REQUEST_TYPE_FIELD_VALUE_REQUEST_FAILURE, 1);
            throw cause;
        } catch (Exception cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Get Request Type Field Value", cause);
            logger.error("Error occurred while getting request type field value: {}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_REQUEST_TYPE_FIELD_VALUE_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while getting request type field value",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jsmAuditService.success(invokerId, "Get Request Type Field Value");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f825af5-5dbc-4803-96f1-d221e3838f7c",
            name = "Create an issue",
            description = "Create an issue with Summary text and issue type",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "New Issue", type = "Entity(Issue)", required = false)
    public ExtensionResponse createAnIssue(
            @Field.Text(name = "Project Key", required = true, attributes = {}, options = {}) String projectKey,
            @Field.Text(name = "Summary", required = true, attributes = {}, options = {}) String summary,
            @Field.Text(name = "Issue Type", required = true, attributes = {}, options = {}) String issueType,
            @Field.Text(name = "Description", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String description,
            @Field.Text(name = "Request Type", required = false, attributes = {
                    @Attribute(name = "visualWidth", value = "S"),
                    @Attribute(name = "toolTip", value = "'Enter the Request Type name (e.g., \\'Get IT help\\'). Leave empty if not using Service Management.'")}, options = {}) String requestType) {
        String invokerId = requestContext.getInvokerId();
        jsmAuditService.triggered(invokerId, "Create an issue");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.PROJECT_KEY, projectKey,
                            Validator.ValidationResource.ISSUE_TYPE, issueType, Validator.ValidationResource.SUMMARY,
                            summary));
            if (validationResults.isEmpty()) {
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("New Issue",
                        issueDao.createIssueWithRequestTypeName(projectKey, summary, description, null, issueType,
                                requestType)));
            }
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_RETRY_ATTEMPT, 1);
            String stateId = UUID.randomUUID().toString();
            Map<String, Object> filedMap = new LinkedHashMap<>();
            filedMap.put(JiraConstants.VALIDATION_RESULT, validationResults);
            filedMap.put("Request Type", requestType);

            internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(filedMap));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForCreateIssue",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.PROJECT_KEY, projectKey,
                            JiraResources.DESCRIPTION, description, JiraResources.ISSUE_TYPE, issueType,
                            JiraResources.SUMMARY, summary));
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jsmAuditService.authFailure(invokerId, "Create an issue", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Create an issue", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (IllegalArgumentException cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Create an issue", cause);
            logger.error("Validation error: {}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR);
        } catch (Exception cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Create an issue", cause);
            String errorMsg = cause.getMessage();
            if (errorMsg == null || errorMsg.trim().isEmpty()) {
                errorMsg = "Unknown error occurred: " + cause.toString();
            }
            logger.error("System error while creating issue: {}", errorMsg, cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, errorMsg, ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jsmAuditService.success(invokerId, "Create an issue");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_23c5c35c-2c99-4e38-b34a-f911c589f02f",
            name = "Report a bug",
            description = "Report a bug for a given project with summary text",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field.Desc(name = "New Bug", type = "Entity(Bug)")
    public ExtensionResponse reportABug(
            @Field(name = "Project key", type = "Text") String projectKey,
            @Field(name = "Summary", type = "Text") String summary) {
        String invokerId = requestContext.getInvokerId();
        jsmAuditService.triggered(invokerId, "Report a bug");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.PROJECT_KEY, projectKey, Validator.ValidationResource.SUMMARY,
                            summary));
            if (validationResults.isEmpty()) {
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(
                        Map.of("New Bug", IssueMapper.getBug(issueDao.createIssue(projectKey, summary, summary,
                                null, JiraConstants.BUG))));
            }
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_RETRY_ATTEMPT, 1);
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForReportABug",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.PROJECT_KEY, projectKey,
                            JiraResources.SUMMARY, summary));
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jsmAuditService.authFailure(invokerId, "Report a bug", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Report a bug", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Report a bug", cause);
            logger.error("Error occurred while reporting bug: {}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while reporting bug",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jsmAuditService.success(invokerId, "Report a bug");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_10d947e5-ccbe-457c-99f2-d50dd5ede35f",
            name = "Create User",
            description = "This request sends an invite to the user.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field(name = "Self", type = "Text", required = false, attributes = {}, options = {})
    @Field(name = "Account Id", type = "Text", required = false, attributes = {}, options = {})
    @Field(name = "Email Address", type = "Text", required = false, attributes = {}, options = {})
    @Field(name = "Display Name", type = "Text", required = false, attributes = {}, options = {})
    public ExtensionResponse createUser(
            @Field(name = "Email", type = "Text") String email) {
        String invokerId = requestContext.getInvokerId();
        jsmAuditService.triggered(invokerId, "Create User");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.EMAIL, email));
            if (validationResults.isEmpty()) {
                Map<String, Object> response = issueDao.createUser(email);
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of(
                        "Self", response.get("Self"), "Account Id", response.get("Account Id"), "Email Address",
                        response.get("Email Address"), "Display Name", response.get("Display Name")));
            } else {
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_RETRY_ATTEMPT, 1);
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId,
                        JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterCreateUser",
                        Map.of(JiraConstants.STATE_ID, stateId, JiraResources.EMAIL, email));
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jsmAuditService.authFailure(invokerId, "Create User", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_FAILURE, 1);
            throw cause;
        } catch (Exception cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Create User", cause);
            logger.error("Error occurred while creating user using Account Id :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while creating user using Account Id.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jsmAuditService.success(invokerId, "Create User");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_5c2d6fe7-2e9e-4947-bc9a-e9f41536810a",
            name = "Update User",
            description = "This request will add the user in groups.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "User Added", type = "Switch", required = false, attributes = {}, options = {})
    public ExtensionResponse updateUser(
            @Field(name = "Account Id", type = "Text") String accountId,
            @Field.Desc(name = "Group Names", type = "[ Text ]") List<String> groupNames) {
        String invokerId = requestContext.getInvokerId();
        jsmAuditService.triggered(invokerId, "Update User");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.USER_ID, accountId));
            if (validationResults.isEmpty()) {
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(
                        Map.of("User Added", issueDao.updateUser(accountId, groupNames)));
            }
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_RETRY_ATTEMPT, 1);
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForUpdateUser",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.ACCOUNT_ID, accountId, "Group Names",
                            groupNames));
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jsmAuditService.authFailure(invokerId, "Update User", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Update User", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Update User", cause);
            logger.error("Error occurred while updating user using Account Id :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while updating user using Account Id.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jsmAuditService.success(invokerId, "Update User");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_8431e961-69db-4d9c-a380-2d34b89a1fda",
            name = "Delete User",
            description = "This request removes the user.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Deleted", type = "Switch", required = false, attributes = {}, options = {})
    public ExtensionResponse deleteUser(
            @Field(name = "Account Id", type = "Text", required = true, attributes = {}, options = {}) String accountId) {
        String invokerId = requestContext.getInvokerId();
        jsmAuditService.triggered(invokerId, "Delete User");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.USER_ID, accountId));
            if (validationResults.isEmpty()) {
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(
                        Map.of("Deleted", issueDao.deleteUser(accountId)));
            }
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_RETRY_ATTEMPT, 1);
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForDeleteUser",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.ACCOUNT_ID, accountId));
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jsmAuditService.authFailure(invokerId, "Delete User", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Delete User", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Delete User", cause);
            logger.error("Error occurred while deleting user using Account Id :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while deleting user using Account Id.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jsmAuditService.success(invokerId, "Delete User");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_98edc1eb-776c-45dd-9084-3015047d880e",
            name = "Get Users Account Id",
            description = "With this request you can fetch the Users account Id by Email",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM,
            tool = true)
    @Field(name = "Account Id", type = "Text", required = false, attributes = {}, options = {})
    public ExtensionResponse getUsersAccountId(
            @Field(name = "Email", type = "Text", required = false, attributes = {}, options = {}) String email) {
        String invokerId = requestContext.getInvokerId();
        jsmAuditService.triggered(invokerId, "Get Users Account Id");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.EMAIL, FieldValidator.getNullOrEmptyDefault(email)));
            if (validationResults.isEmpty()) {
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(
                        Map.of(JiraResources.ACCOUNT_ID, issueDao.getAccountIdByEmail(email)));
            } else {
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_RETRY_ATTEMPT, 1);
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId,
                        JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterGetUsersAccountId",
                        Map.of(JiraConstants.STATE_ID, stateId, JiraResources.EMAIL, email));
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jsmAuditService.authFailure(invokerId, "Get Users Account Id", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Get Users Account Id", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Get Users Account Id", cause);
            logger.error("Error occurred while getting Users Account Id using Email :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while getting Users Account Id using Email.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jsmAuditService.success(invokerId, "Get Users Account Id");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
            name = "Get User Account Information",
            description = "Retrieve detailed account information for a Jira user by searching with their email address or name. Returns all user fields if a match is found, or empty if no match exists.",
            area = "Organization",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field.Desc(name = "User", type = "Entity(User)", required = false)
    public ExtensionResponse getUserAccountInformation(
            @Field(name = "Search Query", type = "Text", required = false, attributes = {}, options = {}) String searchQuery) {
        String invokerId = requestContext.getInvokerId();
        jsmAuditService.triggered(invokerId, "Get User Account Information");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("User", new User()));
            }
            User user = userDao.getUserByEmailOrName(searchQuery);

            if (user == null) {
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("User", new User()));
            }

            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_SUCCESS, 1);
            return ExtensionResponseFactory.create(Map.of("User", user));

        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jsmAuditService.authFailure(invokerId, "Get User Account Information", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Get User Account Information", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Get User Account Information", cause);
            logger.error("Error occurred while getting user account information: {}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USER_ACCOUNT_INFORMATION_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while getting user account information.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jsmAuditService.success(invokerId, "Get User Account Information");
            }
        }
    }

}
