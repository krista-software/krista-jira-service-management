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
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.devsecops.development.jira.api.JsmAuditService;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.Validator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.ReleaseDao;
import app.krista.extensions.devsecops.development.jira.services.ReleaseSummaryResponse;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import app.krista.sdk.atlassian.extresp.ExtensionResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "3ef3d7f7-b7ce-42e9-bdc4-c1b07fc33436")
public final class ReleaseManagementArea {

    private final ReleaseDao releaseDao;
    private final IssueDao issueDao;
    private final TelemetryMetrics telemetryMetrics;
    private final ValidationOrchestrator validationOrchestrator;
    private final ErrorHandlingStateManager internalStateManager;
    private final ExtensionResponseGenerator responseGenerator;
    private final RequestContext requestContext;
    private final JsmAuditService jsmAuditService;
    private final Logger logger = LoggerFactory.getLogger(ReleaseManagementArea.class);

    @Inject
    public ReleaseManagementArea(ReleaseDao releaseDao, IssueDao issueDao, TelemetryMetrics telemetryMetrics,
                                 ValidationOrchestrator validationOrchestrator,
                                 ErrorHandlingStateManager internalStateManager,
                                 ExtensionResponseGenerator responseGenerator,
                                 RequestContext requestContext, JsmAuditService jsmAuditService) {
        this.releaseDao = releaseDao;
        this.issueDao = issueDao;
        this.telemetryMetrics = telemetryMetrics;
        this.validationOrchestrator = validationOrchestrator;
        this.internalStateManager = internalStateManager;
        this.responseGenerator = responseGenerator;
        this.requestContext = requestContext;
        this.jsmAuditService = jsmAuditService;
    }

    @CatalogRequest(
            id = "localDomainRequest_dc6e0837-e404-4ef3-8123-172e07c4df75",
            name = "Get release summary",
            description = "Get release summary",
            area = "Release Management",
            type = CatalogRequest.Type.QUERY_SYSTEM,
            tool = true)
    @Field(name = "Total user stories", type = "Number")
    @Field(name = "Open user stories", type = "Number")
    @Field(name = "Total epics", type = "Number")
    @Field(name = "Total Tasks", type = "Number")
    @Field(name = "Open Tasks", type = "Number")
    @Field(name = "Total Bugs", type = "Number")
    @Field(name = "Open Bugs", type = "Number")
    @Field(name = "Release day", type = "Date")
    @Field(name = "Days remaining", type = "Number")
    @Field.Desc(name = "User stories", type = "[ Entity(Issue) ]")
    @Field.Desc(name = "Epics", type = "[ Entity(Issue) ]")
    @Field.Desc(name = "Bugs", type = "[ Entity(Issue) ]")
    @Field.Desc(name = "Tasks", type = "[ Entity(Issue) ]")
    public ExtensionResponse getReleaseSummary(
            @Field(name = "Status", type = "Text") String status,
            @Field(name = "Assignee", type = "Text") String assignee,
            @Field(name = "Priority", type = "Text") String priority,
            @Field(name = "Issue type", type = "Text") String issueType,
            @Field(name = "Page Size", type = "Number") Double pageSize,
            @Field(name = "Page Number", type = "Number") Double pageNumber,
            @Field(name = "Fix version", type = "Text") String fixVersion) {
        String invokerId = requestContext.getInvokerId();
        jsmAuditService.triggered(invokerId, "Get release summary");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            Map<Validator.ValidationResource, String> validationMap = new HashMap<>();
            if (status != null && !status.isEmpty()) {
                validationMap.put(Validator.ValidationResource.STATUS_NAME, status);
            }
            if (assignee != null && !assignee.isEmpty()) {
                validationMap.put(Validator.ValidationResource.USER_ID, assignee);
            }
            if (issueType != null && !issueType.isEmpty()) {
                validationMap.put(Validator.ValidationResource.ISSUE_TYPE, issueType);
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(validationMap);

            if (validationResults.isEmpty()) {
                long pSize = (pageSize != null && pageSize > 0) ? pageSize.longValue() : 20;
                long pNumber = (pageNumber != null && pageNumber > 0) ? pageNumber.longValue() : 1;
                ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(status, assignee, priority, issueType, pSize, pNumber, fixVersion);
                telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(response);
            } else {
                telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_RETRY_ATTEMPT, 1);
                String stateId = UUID.randomUUID().toString();
                Map<String, Object> stateData = new HashMap<>();
                stateData.put(JiraConstants.VALIDATION_RESULT, validationResults);
                stateData.put("Status", status);
                stateData.put("Assignee", assignee);
                stateData.put("Priority", priority);
                stateData.put("Issue type", issueType);
                stateData.put("Page Size", pageSize);
                stateData.put("Page Number", pageNumber);
                stateData.put("Fix version", fixVersion);
                internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(stateData));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterForGetReleaseSummary", Map.of(JiraConstants.STATE_ID, stateId,
                                "Status", status != null ? status : "",
                                "Assignee", assignee != null ? assignee : "",
                                "Priority", priority != null ? priority : "",
                                "Issue type", issueType != null ? issueType : "",
                                "Page Size", pageSize != null ? pageSize : 20.0,
                                "Page Number", pageNumber != null ? pageNumber : 1.0,
                                "Fix version", fixVersion != null ? fixVersion : ""));
                telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jsmAuditService.authFailure(invokerId, "Get release summary", cause);
            logger.error("Authorization failed for getReleaseSummary: {}", cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_FAILURE, 1);
            throw cause;
        } catch (Exception cause) {
            auditRecorded = true;
            jsmAuditService.failure(invokerId, "Get release summary", cause);
            logger.error("Error occurred while getting release summary :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while getting release summary.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        } finally {
            if (!auditRecorded) {
                jsmAuditService.success(invokerId, "Get release summary");
            }
        }
    }

}
