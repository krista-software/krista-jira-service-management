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
import app.krista.extensions.devsecops.development.jira.api.JsmAuditService;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.ReleaseDao;
import app.krista.extensions.devsecops.development.jira.services.ReleaseSummaryResponse;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReleaseManagementArea Tests")
class ReleaseManagementAreaTest {

    @Mock private ReleaseDao releaseDao;
    @Mock private IssueDao issueDao;
    @Mock private TelemetryMetrics telemetryMetrics;
    @Mock private ValidationOrchestrator validationOrchestrator;
    @Mock private ErrorHandlingStateManager internalStateManager;
    @Mock private ExtensionResponseGenerator responseGenerator;
    @Mock private RequestContext requestContext;
    @Mock private JsmAuditService jsmAuditService;

    private ReleaseManagementArea releaseManagementArea;

    private static final String INVOKER_ID = "test-invoker-id";
    private static final String CATALOG_NAME = "Get release summary";

    @BeforeEach
    void setUp() {
        releaseManagementArea = new ReleaseManagementArea(
                releaseDao, issueDao, telemetryMetrics,
                validationOrchestrator, internalStateManager,
                responseGenerator, requestContext, jsmAuditService);
        when(requestContext.getInvokerId()).thenReturn(INVOKER_ID);
    }

    // ========================= Helpers =========================

    private List<ValidationOrchestrator.ValidationResult> noValidationErrors() {
        return Collections.emptyList();
    }

    private List<ValidationOrchestrator.ValidationResult> oneValidationError() {
        return List.of(new ValidationOrchestrator.ValidationResult(
                "Please re-enter value", "FieldName", "Enter value", "Value is invalid"));
    }

    private ExtensionResponse mockResponse() {
        return mock(ExtensionResponse.class);
    }

    // ========================= getReleaseSummary – success paths =========================

    @Nested
    @DisplayName("getReleaseSummary - success")
    class GetReleaseSummarySuccessTests {

        @Test
        @DisplayName("success - all null filters, default page size and number")
        void success_allNullFilters_defaultPaging() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    isNull(), isNull(), isNull(), isNull(), eq(20L), eq(1L), isNull()))
                    .thenReturn(new ReleaseSummaryResponse());

            ExtensionResponse response = releaseManagementArea.getReleaseSummary(
                    null, null, null, null, null, null, null);

            assertNotNull(response);
            verify(releaseDao).getGetReleaseSummaryResponse(null, null, null, null, 20L, 1L, null);
            verify(issueDao, never()).validateCredentials();
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_COUNT), eq(1L));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).triggered(INVOKER_ID, CATALOG_NAME);
            verify(jsmAuditService).success(INVOKER_ID, CATALOG_NAME);
            verify(jsmAuditService, never()).failure(any(), any(), any());
            verify(jsmAuditService, never()).authFailure(any(), any(), any());
        }

        @Test
        @DisplayName("success - with invokeAsUser=true validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            ExtensionResponse response = releaseManagementArea.getReleaseSummary(
                    null, null, null, null, null, null, null);

            assertNotNull(response);
            verify(issueDao).validateCredentials();
            verify(jsmAuditService).success(INVOKER_ID, CATALOG_NAME);
        }

        @Test
        @DisplayName("success - with custom pageSize and pageNumber")
        void success_withCustomPageSizeAndNumber() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), eq(50L), eq(3L), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary(null, null, null, null, 50.0, 3.0, null);

            verify(releaseDao).getGetReleaseSummaryResponse(null, null, null, null, 50L, 3L, null);
        }

        @Test
        @DisplayName("success - zero pageSize and pageNumber use defaults")
        void success_zeroPageSizeAndNumber_useDefaults() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), eq(20L), eq(1L), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary(null, null, null, null, 0.0, 0.0, null);

            verify(releaseDao).getGetReleaseSummaryResponse(null, null, null, null, 20L, 1L, null);
        }

        @Test
        @DisplayName("success - negative pageSize and pageNumber use defaults")
        void success_negativePageSizeAndNumber_useDefaults() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), eq(20L), eq(1L), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary(null, null, null, null, -5.0, -1.0, null);

            verify(releaseDao).getGetReleaseSummaryResponse(null, null, null, null, 20L, 1L, null);
        }

        @Test
        @DisplayName("success - with all filters populated adds validation entries")
        void success_allFiltersPopulated() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    eq("Open"), eq("user1"), eq("High"), eq("Bug"), eq(10L), eq(2L), eq("v1.0")))
                    .thenReturn(new ReleaseSummaryResponse());

            ExtensionResponse response = releaseManagementArea.getReleaseSummary(
                    "Open", "user1", "High", "Bug", 10.0, 2.0, "v1.0");

            assertNotNull(response);
            verify(validationOrchestrator).validate(anyMap());
            verify(jsmAuditService).success(INVOKER_ID, CATALOG_NAME);
        }

        @Test
        @DisplayName("success - empty string filters are not added to validation map")
        void success_emptyStringFilters_notAddedToValidation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary("", "", "High", "", null, null, "v1.0");

            verify(validationOrchestrator).validate(argThat(map -> map.isEmpty()));
        }

        @Test
        @DisplayName("success - fixVersion is passed through without validation")
        void success_fixVersionPassedThrough() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    isNull(), isNull(), isNull(), isNull(), eq(20L), eq(1L), eq("Release-2.0")))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary(null, null, null, null, null, null, "Release-2.0");

            verify(releaseDao).getGetReleaseSummaryResponse(null, null, null, null, 20L, 1L, "Release-2.0");
        }
    }

    // ========================= getReleaseSummary – validation failure =========================

    @Nested
    @DisplayName("getReleaseSummary - validation failure")
    class GetReleaseSummaryValidationFailureTests {

        @Test
        @DisplayName("validation failure with all null params - returns confirmation with defaults")
        void validationFailure_allNullParams_returnsConfirmationWithDefaults() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = releaseManagementArea.getReleaseSummary(
                    null, null, null, null, null, null, null);

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
            verify(responseGenerator).generateConfirmationResponse(
                    eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                    anyList(),
                    eq("confirmReenterForGetReleaseSummary"),
                    argThat(map ->
                            "".equals(map.get("Status"))
                                    && "".equals(map.get("Assignee"))
                                    && "".equals(map.get("Priority"))
                                    && "".equals(map.get("Issue type"))
                                    && Double.valueOf(20.0).equals(map.get("Page Size"))
                                    && Double.valueOf(1.0).equals(map.get("Page Number"))
                                    && "".equals(map.get("Fix version"))
                    ));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_RETRY_ATTEMPT), eq(1L));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_SUCCESS), eq(1L));
            verify(releaseDao, never()).getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any());
            verify(jsmAuditService).success(INVOKER_ID, CATALOG_NAME);
        }

        @Test
        @DisplayName("validation failure with all non-null params - passes actual values in state")
        void validationFailure_allNonNullParams_passesActualValues() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = releaseManagementArea.getReleaseSummary(
                    "Open", "user1", "High", "Bug", 25.0, 5.0, "v3.0");

            assertSame(confirmation, response);
            verify(responseGenerator).generateConfirmationResponse(
                    eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                    anyList(),
                    eq("confirmReenterForGetReleaseSummary"),
                    argThat(map ->
                            "Open".equals(map.get("Status"))
                                    && "user1".equals(map.get("Assignee"))
                                    && "High".equals(map.get("Priority"))
                                    && "Bug".equals(map.get("Issue type"))
                                    && Double.valueOf(25.0).equals(map.get("Page Size"))
                                    && Double.valueOf(5.0).equals(map.get("Page Number"))
                                    && "v3.0".equals(map.get("Fix version"))
                    ));
            verify(jsmAuditService).success(INVOKER_ID, CATALOG_NAME);
        }
    }

    // ========================= getReleaseSummary – validation map construction =========================

    @Nested
    @DisplayName("getReleaseSummary - validation map construction")
    class GetReleaseSummaryValidationMapTests {

        @Test
        @DisplayName("only status non-null and non-empty - validation map contains STATUS_NAME")
        void onlyStatusNonEmpty_validationMapContainsStatusName() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary("Open", null, null, null, null, null, null);

            verify(validationOrchestrator).validate(argThat(map ->
                    map.size() == 1 && map.containsKey(
                            app.krista.extensions.devsecops.development.jira.catalog.validators.Validator.ValidationResource.STATUS_NAME)));
        }

        @Test
        @DisplayName("only assignee non-null and non-empty - validation map contains USER_ID")
        void onlyAssigneeNonEmpty_validationMapContainsUserId() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary(null, "user1", null, null, null, null, null);

            verify(validationOrchestrator).validate(argThat(map ->
                    map.size() == 1 && map.containsKey(
                            app.krista.extensions.devsecops.development.jira.catalog.validators.Validator.ValidationResource.USER_ID)));
        }

        @Test
        @DisplayName("only issueType non-null and non-empty - validation map contains ISSUE_TYPE")
        void onlyIssueTypeNonEmpty_validationMapContainsIssueType() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary(null, null, null, "Bug", null, null, null);

            verify(validationOrchestrator).validate(argThat(map ->
                    map.size() == 1 && map.containsKey(
                            app.krista.extensions.devsecops.development.jira.catalog.validators.Validator.ValidationResource.ISSUE_TYPE)));
        }

        @Test
        @DisplayName("all three filters non-null and non-empty - validation map contains all three")
        void allThreeFiltersNonEmpty_validationMapContainsAll() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary("Done", "user2", "Low", "Story", null, null, null);

            verify(validationOrchestrator).validate(argThat(map -> map.size() == 3));
        }

        @Test
        @DisplayName("null status is not added to validation map")
        void nullStatus_notAddedToValidationMap() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary(null, null, null, null, null, null, null);

            verify(validationOrchestrator).validate(argThat(Map::isEmpty));
        }

        @Test
        @DisplayName("empty status is not added to validation map")
        void emptyStatus_notAddedToValidationMap() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary("", null, null, null, null, null, null);

            verify(validationOrchestrator).validate(argThat(Map::isEmpty));
        }

        @Test
        @DisplayName("empty assignee is not added to validation map")
        void emptyAssignee_notAddedToValidationMap() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary(null, "", null, null, null, null, null);

            verify(validationOrchestrator).validate(argThat(Map::isEmpty));
        }

        @Test
        @DisplayName("empty issueType is not added to validation map")
        void emptyIssueType_notAddedToValidationMap() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenReturn(new ReleaseSummaryResponse());

            releaseManagementArea.getReleaseSummary(null, null, null, "", null, null, null);

            verify(validationOrchestrator).validate(argThat(Map::isEmpty));
        }
    }

    // ========================= getReleaseSummary – exception handling =========================

    @Nested
    @DisplayName("getReleaseSummary - exception handling")
    class GetReleaseSummaryExceptionTests {

        @Test
        @DisplayName("MustAuthorizeException from validateCredentials is re-thrown")
        void mustAuthorizeException_fromValidateCredentials_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> releaseManagementArea.getReleaseSummary(
                            null, null, null, null, null, null, null));

            verify(jsmAuditService).authFailure(INVOKER_ID, CATALOG_NAME, authEx);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_FAILURE), eq(1L));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("MustAuthorizeException from releaseDao is re-thrown")
        void mustAuthorizeException_fromReleaseDao_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenThrow(authEx);

            assertThrows(MustAuthorizeException.class,
                    () -> releaseManagementArea.getReleaseSummary(
                            null, null, null, null, null, null, null));

            verify(jsmAuditService).authFailure(INVOKER_ID, CATALOG_NAME, authEx);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_FAILURE), eq(1L));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception from releaseDao returns failure ExtensionResponse")
        void genericException_fromReleaseDao_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(releaseDao.getGetReleaseSummaryResponse(
                    any(), any(), any(), any(), anyLong(), anyLong(), any()))
                    .thenThrow(new RuntimeException("Server error"));

            ExtensionResponse response = releaseManagementArea.getReleaseSummary(
                    null, null, null, null, null, null, null);

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq(CATALOG_NAME), any(RuntimeException.class));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_FAILURE), eq(1L));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception from validationOrchestrator returns failure response")
        void genericException_fromValidationOrchestrator_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap()))
                    .thenThrow(new RuntimeException("Validation error"));

            ExtensionResponse response = releaseManagementArea.getReleaseSummary(
                    null, null, null, null, null, null, null);

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq(CATALOG_NAME), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }
}
