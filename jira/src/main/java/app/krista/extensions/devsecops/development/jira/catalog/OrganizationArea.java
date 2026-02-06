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
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.Validator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.KristaMediaClient;
import app.krista.extensions.devsecops.development.jira.util.FieldValidator;
import app.krista.extensions.devsecops.development.jira.util.IssueMapper;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
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
    private final KristaMediaClient kristaMediaClient;
    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;
    private final ValidationOrchestrator validationOrchestrator;
    private final Logger logger = LoggerFactory.getLogger(OrganizationArea.class);

    @Inject
    public OrganizationArea(Invoker invoker, IssueDao issueDao, KristaMediaClient kristaMediaClient,
                            ExtensionResponseGenerator responseGenerator,
                            ErrorHandlingStateManager internalStateManager, ValidationOrchestrator validationOrchestrator) {
        this.invoker = invoker;
        this.issueDao = issueDao;
        this.kristaMediaClient = kristaMediaClient;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
        this.validationOrchestrator = validationOrchestrator;
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
        try {
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
            if (validationResults.isEmpty())
                return ExtensionResponseFactory.create(Map.of("Upload Status",
                        issueDao.attachFileToTicket(ticketFiles, ticketId)));
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraResources.TICKET_ID, ticketId,
                            "File", file,
                            JiraConstants.VALIDATION_RESULT, validationResults)));
            return responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterTicketId", Map.of(JiraConstants.STATE_ID, stateId));
        } catch (IllegalCallerException cause) {
            logger.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (MustAuthorizeException cause) {
            logger.error(cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            logger.error("Error occurred while attaching file to ticket :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while attaching file to ticket",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_f9dd5785-388c-4e41-acd6-155405c6aa9a",
            name = "Get Request Type Field Value",
            description = "Accepts any Issue Key to get Request Type Field Value, gathers portal key and key of Request Type",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Field Value", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {})
    public ExtensionResponse getRequestTypeFieldValue(
            @Field.Text(name = "Issue Key", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S")}, options = {}) String issueKey,
            @Field.Text(name = "Request Type Name", required = true, attributes = {
                    @Attribute(name = "visualWidth", value = "S"),
                    @Attribute(name = "toolTip", value = "'Note the Request Type Name is Case Sensitive'")}, options = {}) String requestTypeName) {
        logger.info("Get Request Type Field Value initialised : Issue Key : {} \n Request Type Name : {}", issueKey,
                requestTypeName);
        String fieldValue = issueDao.getRequestTypeFieldValue(issueKey, requestTypeName);

        return ExtensionResponseFactory
                .create(Map.of("Field Value", fieldValue != null ? fieldValue : "Invalid Issue key or Request Type"));
    }

    @CatalogRequest(
            id = "localDomainRequest_7f825af5-5dbc-4803-96f1-d221e3838f7c",
            name = "Create an issue",
            description = "Create an issue with Summary text and issue type",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
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
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.PROJECT_KEY, projectKey,
                            Validator.ValidationResource.ISSUE_TYPE, issueType, Validator.ValidationResource.SUMMARY,
                            summary));
            if (validationResults.isEmpty()) {
                return ExtensionResponseFactory.create(Map.of("New Issue",
                        issueDao.createIssueWithRequestTypeName(projectKey, summary, description, null, issueType,
                                requestType)));
            }
            String stateId = UUID.randomUUID().toString();
            Map<String, Object> filedMap = new LinkedHashMap<>();
            filedMap.put(JiraConstants.VALIDATION_RESULT, validationResults);
            filedMap.put("Request Type", requestType);

            internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(filedMap));
            return responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForCreateIssue",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.PROJECT_KEY, projectKey,
                            JiraResources.DESCRIPTION, description, JiraResources.ISSUE_TYPE, issueType,
                            JiraResources.SUMMARY, summary));
        } catch (IllegalCallerException cause) {
            logger.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (MustAuthorizeException cause) {
            logger.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalArgumentException cause) {
            // Propagate input validation errors (like "Request Type not found") directly to
            // user
            logger.error("Validation error: {}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR);
        } catch (Exception cause) {
            // Catch-all: Ensure we propagate the actual error message, not a generic one
            String errorMsg = cause.getMessage();
            if (errorMsg == null || errorMsg.trim().isEmpty()) {
                errorMsg = "Unknown error occurred: " + cause.toString();
            }
            logger.error("System error while creating issue: {}", errorMsg, cause);
            return ExtensionResponseFactory.create(cause, errorMsg, ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_23c5c35c-2c99-4e38-b34a-f911c589f02f",
            name = "Report a bug",
            description = "Report a bug for a given project with summary text",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = "New Bug", type = "Entity(Bug)")
    public ExtensionResponse reportABug(
            @Field(name = "Project key", type = "Text") String projectKey,
            @Field(name = "Summary", type = "Text") String summary) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.PROJECT_KEY, projectKey, Validator.ValidationResource.SUMMARY,
                            summary));
            if (validationResults.isEmpty())
                return ExtensionResponseFactory.create(
                        Map.of("New Bug", IssueMapper.getBug(issueDao.createIssue(projectKey, summary, summary,
                                null, JiraConstants.BUG))));
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            return responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForReportABug",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.PROJECT_KEY, projectKey,
                            JiraResources.SUMMARY, summary));
        } catch (MustAuthorizeException cause) {
            logger.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            logger.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            logger.error("Error occurred while reporting bug: {}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while reporting bug",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_10d947e5-ccbe-457c-99f2-d50dd5ede35f",
            name = "Create User",
            description = "This request sends an invite to the user.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Self", type = "Text", required = false, attributes = {}, options = {})
    @Field(name = "Account Id", type = "Text", required = false, attributes = {}, options = {})
    @Field(name = "Email Address", type = "Text", required = false, attributes = {}, options = {})
    @Field(name = "Display Name", type = "Text", required = false, attributes = {}, options = {})
    public ExtensionResponse createUser(
            @Field(name = "Email", type = "Text") String email) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.EMAIL, email));
            if (validationResults.isEmpty()) {
                Map<String, Object> response = issueDao.createUser(email);
                return ExtensionResponseFactory.create(Map.of(
                        "Self", response.get("Self"), "Account Id", response.get("Account Id"), "Email Address",
                        response.get("Email Address"), "Display Name", response.get("Display Name")));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId,
                        JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterCreateUser",
                        Map.of(JiraConstants.STATE_ID, stateId, JiraResources.EMAIL, email));
            }
        } catch (MustAuthorizeException cause) {
            logger.error(cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            logger.error("Error occurred while creating user using Account Id :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while creating user using Account Id.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
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
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.USER_ID, accountId));
            if (validationResults.isEmpty())
                return ExtensionResponseFactory.create(
                        Map.of("User Added", issueDao.updateUser(accountId, groupNames)));
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            return responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForUpdateUser",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.ACCOUNT_ID, accountId, "Group Names",
                            groupNames));
        } catch (MustAuthorizeException cause) {
            logger.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            logger.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            logger.error("Error occurred while updating user using Account Id :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while updating user using Account Id.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
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
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator
                    .validate(Map.of(Validator.ValidationResource.USER_ID, accountId));
            if (validationResults.isEmpty())
                return ExtensionResponseFactory.create(
                        Map.of("Deleted", issueDao.deleteUser(accountId)));
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId,
                    JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            return responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForDeleteUser",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.ACCOUNT_ID, accountId));
        } catch (MustAuthorizeException cause) {
            logger.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            logger.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            logger.error("Error occurred while deleting user using Account Id :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while deleting user using Account Id.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_98edc1eb-776c-45dd-9084-3015047d880e",
            name = "Get Users Account Id",
            description = "With this request you can fetch the Users account Id by Email",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Account Id", type = "Text", required = false, attributes = {}, options = {})
    public ExtensionResponse getUsersAccountId(
            @Field(name = "Email", type = "Text", required = false, attributes = {}, options = {}) String email) {
        try {
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.EMAIL, FieldValidator.getNullOrEmptyDefault(email)));
            if (validationResults.isEmpty()) {
                return ExtensionResponseFactory.create(
                        Map.of(JiraResources.ACCOUNT_ID, issueDao.getAccountIdByEmail(email)));
            } else {
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId,
                        JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterGetUsersAccountId",
                        Map.of(JiraConstants.STATE_ID, stateId, JiraResources.EMAIL, email));
            }
        } catch (IllegalCallerException cause) {
            logger.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (MustAuthorizeException cause) {
            logger.error(cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            logger.error("Error occurred while getting Users Account Id using Email :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Error occurred while getting Users Account Id using Email.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        }
    }

}
