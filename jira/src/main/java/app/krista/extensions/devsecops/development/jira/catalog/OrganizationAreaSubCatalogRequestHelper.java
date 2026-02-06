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
import app.krista.extensions.devsecops.development.jira.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.RemediationActionFactory;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.KristaMediaClient;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

@Service
public class OrganizationAreaSubCatalogRequestHelper {

    private final IssueDao issueDao;
    private final ProjectDao projectDao;
    private final UserDao userDao;
    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;
    private final KristaMediaClient kristaMediaClient;
    private static final Logger logger = LoggerFactory.getLogger(OrganizationAreaSubCatalogRequests.class);

    @Inject
    public OrganizationAreaSubCatalogRequestHelper(IssueDao issueDao, ProjectDao projectDao, UserDao userDao, ExtensionResponseGenerator responseGenerator, ErrorHandlingStateManager internalStateManager, KristaMediaClient kristaMediaClient) {
        this.issueDao = issueDao;
        this.projectDao = projectDao;
        this.userDao = userDao;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
        this.kristaMediaClient = kristaMediaClient;
    }

    @SubCatalogRequest(
            name = "confirmReenterGetUsersAccountId",
            description = "Checks if user wants to re enter details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterGetUsersAccountId(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Email: Text }",
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
                validationResults, "handleReenterGetUsersAccountId",
                Map.of(JiraConstants.STATE_ID, stateId, JiraResources.EMAIL, map.get(JiraResources.EMAIL)));
    }

    @SubCatalogRequest(
            name = "handleReenterGetUsersAccountId",
            description = "With this request you can fetch the Users account Id by Email",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field(name = "Account Id", type = "Text", required = false, attributes = {}, options = {})
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterGetUsersAccountId(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Email: Text }",
            required = true) Map<String, Object> map) {
        try {
            String email = (String) map.get(JiraResources.EMAIL);
            ExtensionResponse emailErrorResponse = emailValidator(email);
            if (emailErrorResponse != null) return emailErrorResponse;
            return ExtensionResponseFactory.create(
                    Map.of(JiraResources.ACCOUNT_ID, issueDao.getAccountIdByEmail(email)));
        } catch (Exception cause) {
            logger.error("Failed to get user by Account Id :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Failed to get user by Account Id.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    private static ExtensionResponse emailValidator(String email) {
        org.apache.commons.validator.routines.EmailValidator emailValidator = org.apache.commons.validator.routines.EmailValidator.getInstance();
        if (!emailValidator.isValid(email)) {
            return ExtensionResponseFactory.create("Provided email is not valid : " + email, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided email is not valid : " + email, List.of())),
                    null, Map.of());
        }
        return null;
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

    @SubCatalogRequest(
            name = "confirmReenterCreateUser",
            description = "Checks if user wants to re enter details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterCreateUser(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Email: Text }",
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
                validationResults, "handleReenterCreateUser",
                Map.of(JiraConstants.STATE_ID, stateId, JiraResources.EMAIL, map.get(JiraResources.EMAIL)));
    }

    @SubCatalogRequest(
            name = "handleReenterCreateUser",
            description = "Use the new details to delete user",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field(name = "Self", type = "Text", required = false, attributes = {}, options = {})
    @Field(name = "Account Id", type = "Text", required = false, attributes = {}, options = {})
    @Field(name = "Email Address", type = "Text", required = false, attributes = {}, options = {})
    @Field(name = "Display Name", type = "Text", required = false, attributes = {}, options = {})
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterCreateUser(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Email: Text }",
            required = true) Map<String, Object> map) {
        try {
            String email = (String) map.get(JiraResources.EMAIL);
            ExtensionResponse emailErrorResponse = emailValidator(email);
            if (emailErrorResponse != null) return emailErrorResponse;
            Map<String, Object> response = issueDao.createUser(email);
            return ExtensionResponseFactory.create(Map.of(
                    "Self", response.get("Self")
                    , JiraResources.ACCOUNT_ID, response.get(JiraResources.ACCOUNT_ID)
                    , "Email Address", response.get("Email Address")
                    , "Display Name", response.get("Display Name")));
        } catch (Exception cause) {
            logger.error("Failed to create the user :{}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Failed to create the user.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

}
