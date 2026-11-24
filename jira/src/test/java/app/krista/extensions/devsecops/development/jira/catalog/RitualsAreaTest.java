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
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import app.krista.model.base.FreeForm;
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
import java.util.concurrent.ExecutorService;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RitualsArea Tests")
class RitualsAreaTest {

    @Mock private IssueDao issueDao;
    @Mock private RequestContext requestContext;
    @Mock private AuthorizationContext authorizationContext;
    @Mock private EventHandler eventHandler;
    @Mock private ValidationOrchestrator validationOrchestrator;
    @Mock private ErrorHandlingStateManager internalStateManager;
    @Mock private ExtensionResponseGenerator responseGenerator;
    @Mock private ProjectDao projectDao;
    @Mock private TelemetryMetrics telemetryMetrics;
    @Mock private JsmAuditService jsmAuditService;
    @Mock private ExecutorService executorService;
    @Mock private Account account;

    private RitualsArea ritualsArea;

    private static final String INVOKER_ID = "test-invoker-id";
    private static final String ISSUE_KEY = "JSM-123";
    private static final String STATUS_NAME = "In Progress";
    private static final String COMMENT = "Test comment";
    private static final String ACCOUNT_ID = "account123";
    private static final String PRIORITY_NAME = "High";
    private static final String PROJECT_KEY = "PROJ";

    @BeforeEach
    void setUp() {
        ritualsArea = new RitualsArea(
                issueDao, requestContext, authorizationContext, eventHandler,
                validationOrchestrator, internalStateManager, responseGenerator,
                projectDao, telemetryMetrics, jsmAuditService);
        ritualsArea.executorService = executorService;
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

    // ========================= updateStatusOfMultipleIssues =========================

    @Nested
    @DisplayName("updateStatusOfMultipleIssues")
    class UpdateStatusOfMultipleIssuesTests {

        @Test
        @DisplayName("success - no invokeAsUser")
        void success_noInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.getIssueFinal(anyString())).thenReturn(new Issue());

            ExtensionResponse response = ritualsArea.updateStatusOfMultipleIssues(STATUS_NAME, List.of(ISSUE_KEY));

            assertNotNull(response);
            verify(issueDao).updateStatus(ISSUE_KEY, STATUS_NAME);
            verify(issueDao).getIssueFinal(ISSUE_KEY);
            verify(issueDao, never()).validateCredentials();
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_COUNT), eq(1L));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).triggered(INVOKER_ID, "Update Status of Multiple Issues");
            verify(jsmAuditService).success(INVOKER_ID, "Update Status of Multiple Issues");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(issueDao.getIssueFinal(anyString())).thenReturn(new Issue());

            ExtensionResponse response = ritualsArea.updateStatusOfMultipleIssues(STATUS_NAME, List.of(ISSUE_KEY));

            assertNotNull(response);
            verify(issueDao).validateCredentials();
            verify(jsmAuditService).success(INVOKER_ID, "Update Status of Multiple Issues");
        }

        @Test
        @DisplayName("success - multiple keys processes all")
        void success_multipleKeys() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.getIssueFinal(anyString())).thenReturn(new Issue());

            ritualsArea.updateStatusOfMultipleIssues(STATUS_NAME, List.of("JSM-1", "JSM-2", "JSM-3"));

            verify(issueDao, times(3)).updateStatus(anyString(), eq(STATUS_NAME));
            verify(issueDao, times(3)).getIssueFinal(anyString());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            doThrow(new IllegalCallerException("Unauthorized")).when(issueDao).updateStatus(anyString(), anyString());

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.updateStatusOfMultipleIssues(STATUS_NAME, List.of(ISSUE_KEY)));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Update Status of Multiple Issues"), any(IllegalCallerException.class));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_FAILURE), eq(1L));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.updateStatusOfMultipleIssues(STATUS_NAME, List.of(ISSUE_KEY)));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Update Status of Multiple Issues", authEx);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_FAILURE), eq(1L));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            doThrow(new RuntimeException("Server error")).when(issueDao).updateStatus(anyString(), anyString());

            ExtensionResponse response = ritualsArea.updateStatusOfMultipleIssues(STATUS_NAME, List.of(ISSUE_KEY));

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Update Status of Multiple Issues"), any(RuntimeException.class));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_FAILURE), eq(1L));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= addAComment =========================

    @Nested
    @DisplayName("addAComment")
    class AddACommentTests {

        @Test
        @DisplayName("success - no validation errors, no invokeAsUser")
        void success_noValidationErrors() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());

            ExtensionResponse response = ritualsArea.addAComment(COMMENT, ISSUE_KEY, "Public");

            assertNotNull(response);
            verify(issueDao).addComment(ISSUE_KEY, COMMENT, "Public");
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_COUNT), eq(1L));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Add a Comment");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());

            ritualsArea.addAComment(COMMENT, ISSUE_KEY, "Internal");

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.addAComment(COMMENT, ISSUE_KEY, "Public");

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
            verify(issueDao, never()).addComment(anyString(), anyString(), anyString());
            verify(jsmAuditService).success(INVOKER_ID, "Add a Comment");
        }

        @Test
        @DisplayName("inner MustAuthorizeException from addComment is re-wrapped and re-thrown")
        void innerMustAuthorizeException_fromAddComment() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new MustAuthorizeException("Auth needed")).when(issueDao).addComment(anyString(), anyString(), anyString());

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.addAComment(COMMENT, ISSUE_KEY, "Public"));

            verify(jsmAuditService).authFailure(eq(INVOKER_ID), eq("Add a Comment"), any(MustAuthorizeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("inner generic Exception from addComment returns failure response")
        void innerGenericException_fromAddComment() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new RuntimeException("Comment failed")).when(issueDao).addComment(anyString(), anyString(), anyString());

            ExtensionResponse response = ritualsArea.addAComment(COMMENT, ISSUE_KEY, "Public");

            assertNotNull(response);
            // Inner exception returns failure, and the outer finally runs with auditRecorded=false
            verify(jsmAuditService).success(INVOKER_ID, "Add a Comment");
        }

        @Test
        @DisplayName("outer MustAuthorizeException from validateCredentials is re-thrown")
        void outerMustAuthorizeException_fromValidateCredentials() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.addAComment(COMMENT, ISSUE_KEY, "Public"));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Add a Comment", authEx);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_FAILURE), eq(1L));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("outer IllegalCallerException is wrapped in RuntimeException")
        void outerIllegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenThrow(new IllegalCallerException("Unauthorized"));

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.addAComment(COMMENT, ISSUE_KEY, "Public"));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Add a Comment"), any(IllegalCallerException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("outer generic Exception returns failure response")
        void outerGenericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Network error"));

            ExtensionResponse response = ritualsArea.addAComment(COMMENT, ISSUE_KEY, "Public");

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Add a Comment"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= getUserIssues =========================

    @Nested
    @DisplayName("getUserIssues")
    class GetUserIssuesTests {

        @Test
        @DisplayName("success - no validation errors")
        void success_noValidationErrors() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of(new Issue()));

            ExtensionResponse response = ritualsArea.getUserIssues(ACCOUNT_ID);

            assertNotNull(response);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_USER_ISSUES_REQUEST_COUNT), eq(1L));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_USER_ISSUES_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Get User Issues");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getUserIssues(ACCOUNT_ID);

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.getUserIssues(ACCOUNT_ID);

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
            verify(jsmAuditService).success(INVOKER_ID, "Get User Issues");
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class, () -> ritualsArea.getUserIssues(ACCOUNT_ID));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Get User Issues", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                    .thenThrow(new IllegalCallerException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> ritualsArea.getUserIssues(ACCOUNT_ID));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get User Issues"), any(IllegalCallerException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("Network error"));

            ExtensionResponse response = ritualsArea.getUserIssues(ACCOUNT_ID);

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get User Issues"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= updateStatusOfIssue =========================

    @Nested
    @DisplayName("updateStatusOfIssue")
    class UpdateStatusOfIssueTests {

        @Test
        @DisplayName("success - both validations pass")
        void success_bothValidationsPass() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(ISSUE_KEY)).thenReturn(new Issue());

            ExtensionResponse response = ritualsArea.updateStatusOfIssue(ISSUE_KEY, STATUS_NAME);

            assertNotNull(response);
            verify(issueDao).updateStatus(ISSUE_KEY, STATUS_NAME);
            verify(validationOrchestrator, times(2)).validate(anyMap());
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_ISSUE_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Update Status of Issue");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(anyString())).thenReturn(new Issue());

            ritualsArea.updateStatusOfIssue(ISSUE_KEY, STATUS_NAME);

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("first validation fails - returns confirmation response")
        void firstValidationFails_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.updateStatusOfIssue(ISSUE_KEY, STATUS_NAME);

            assertSame(confirmation, response);
            verify(validationOrchestrator, times(1)).validate(anyMap());
            verify(issueDao, never()).updateStatus(anyString(), anyString());
            verify(jsmAuditService).success(INVOKER_ID, "Update Status of Issue");
        }

        @Test
        @DisplayName("first validation passes, second fails - returns confirmation response")
        void secondValidationFails_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap()))
                    .thenReturn(noValidationErrors())
                    .thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.updateStatusOfIssue(ISSUE_KEY, STATUS_NAME);

            assertSame(confirmation, response);
            verify(validationOrchestrator, times(2)).validate(anyMap());
            verify(issueDao, never()).updateStatus(anyString(), anyString());
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.updateStatusOfIssue(ISSUE_KEY, STATUS_NAME));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Update Status of Issue", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new IllegalCallerException("Unauthorized")).when(issueDao).updateStatus(anyString(), anyString());

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.updateStatusOfIssue(ISSUE_KEY, STATUS_NAME));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Update Status of Issue"), any(IllegalCallerException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new RuntimeException("Server error")).when(issueDao).updateStatus(anyString(), anyString());

            ExtensionResponse response = ritualsArea.updateStatusOfIssue(ISSUE_KEY, STATUS_NAME);

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Update Status of Issue"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= assignATaskToUser =========================

    @Nested
    @DisplayName("assignATaskToUser")
    class AssignATaskToUserTests {

        @Test
        @DisplayName("success - validation passes")
        void success_validationPasses() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(ISSUE_KEY)).thenReturn(new Issue());

            ExtensionResponse response = ritualsArea.assignATaskToUser(ACCOUNT_ID, ISSUE_KEY);

            assertNotNull(response);
            verify(issueDao).assignTask(ISSUE_KEY, ACCOUNT_ID);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.ASSIGN_TASK_TO_USER_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Assign a Task to User");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(anyString())).thenReturn(new Issue());

            ritualsArea.assignATaskToUser(ACCOUNT_ID, ISSUE_KEY);

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.assignATaskToUser(ACCOUNT_ID, ISSUE_KEY);

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
            verify(jsmAuditService).success(INVOKER_ID, "Assign a Task to User");
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.assignATaskToUser(ACCOUNT_ID, ISSUE_KEY));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Assign a Task to User", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new IllegalCallerException("Unauthorized")).when(issueDao).assignTask(anyString(), anyString());

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.assignATaskToUser(ACCOUNT_ID, ISSUE_KEY));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Assign a Task to User"), any(IllegalCallerException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new RuntimeException("Error")).when(issueDao).assignTask(anyString(), anyString());

            ExtensionResponse response = ritualsArea.assignATaskToUser(ACCOUNT_ID, ISSUE_KEY);

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Assign a Task to User"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= fetchByJQLQuery =========================

    @Nested
    @DisplayName("fetchByJQLQuery")
    class FetchByJQLQueryTests {

        @Test
        @DisplayName("success - validation passes, returns task ID")
        void success_validationPasses() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
            when(account.getAccountId()).thenReturn(ACCOUNT_ID);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());

            ExtensionResponse response = ritualsArea.fetchByJQLQuery("status = Open");

            assertNotNull(response);
            verify(executorService).submit(any(Runnable.class));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.FETCH_BY_JQL_QUERY_REQUEST_COUNT), eq(1L));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.FETCH_BY_JQL_QUERY_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Fetch by JQL Query");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
            when(account.getAccountId()).thenReturn(ACCOUNT_ID);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());

            ritualsArea.fetchByJQLQuery("status = Open");

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("success - async lambda executes successfully")
        void success_asyncLambdaExecutes() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
            when(account.getAccountId()).thenReturn(ACCOUNT_ID);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.searchByJqlQuery(anyBoolean(), anyString(), anyString(), anyString(), anyLong(), anyLong()))
                    .thenReturn(List.of(new Issue()));

            doAnswer(invocation -> {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }).when(executorService).submit(any(Runnable.class));

            ritualsArea.fetchByJQLQuery("status = Open");

            verify(eventHandler).handleEvent(anyString(), any(FreeForm.class));
        }

        @Test
        @DisplayName("async lambda handles exception from searchByJqlQuery")
        void asyncLambda_handlesException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
            when(account.getAccountId()).thenReturn(ACCOUNT_ID);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.searchByJqlQuery(anyBoolean(), anyString(), anyString(), anyString(), anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("JQL error"));

            doAnswer(invocation -> {
                ((Runnable) invocation.getArgument(0)).run();
                return null;
            }).when(executorService).submit(any(Runnable.class));

            ritualsArea.fetchByJQLQuery("bad query");

            verify(eventHandler).handleEvent(anyString(), any(FreeForm.class));
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
            when(account.getAccountId()).thenReturn(ACCOUNT_ID);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.fetchByJQLQuery("bad query");

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
            verify(executorService, never()).submit(any(Runnable.class));
            verify(jsmAuditService).success(INVOKER_ID, "Fetch by JQL Query");
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.fetchByJQLQuery("status = Open"));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Fetch by JQL Query", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
            when(account.getAccountId()).thenReturn(ACCOUNT_ID);
            when(validationOrchestrator.validate(anyMap())).thenThrow(new IllegalCallerException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> ritualsArea.fetchByJQLQuery("status = Open"));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Fetch by JQL Query"), any(IllegalCallerException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(authorizationContext.getAuthorizedAccount()).thenReturn(account);
            when(account.getAccountId()).thenReturn(ACCOUNT_ID);
            when(validationOrchestrator.validate(anyMap())).thenThrow(new RuntimeException("Network error"));

            ExtensionResponse response = ritualsArea.fetchByJQLQuery("status = Open");

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Fetch by JQL Query"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= getResultsForFetchIssuesByJQLQuery =========================

    @Nested
    @DisplayName("getResultsForFetchIssuesByJQLQuery")
    class GetResultsForFetchIssuesByJQLQueryTests {

        @Test
        @DisplayName("success - event matches, no error, non-empty issue list")
        void success_eventMatches_nonEmptyIssueList() {
            FreeForm eventData = new FreeForm();
            eventData.put(RitualsArea.IS_ERROR, "Switch", false);
            eventData.put("Data", "[ Entity(Issue) ]", List.of(new Issue()));

            ExtensionResponse response = ritualsArea.getResultsForFetchIssuesByJQLQuery("task-1", eventData, "task-1");

            assertNotNull(response);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Get Results for Fetch Issues by JQL Query");
        }

        @Test
        @DisplayName("success - event matches, no error, empty issue list")
        void success_eventMatches_emptyIssueList() {
            FreeForm eventData = new FreeForm();
            eventData.put(RitualsArea.IS_ERROR, "Switch", false);
            eventData.put("Data", "[ Entity(Issue) ]", List.of());

            ExtensionResponse response = ritualsArea.getResultsForFetchIssuesByJQLQuery("task-1", eventData, "task-1");

            assertNotNull(response);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Get Results for Fetch Issues by JQL Query");
        }

        @Test
        @DisplayName("event matches with error - returns error response")
        void eventMatches_withError_returnsErrorResponse() {
            FreeForm eventData = new FreeForm();
            eventData.put(RitualsArea.IS_ERROR, "Switch", true);
            eventData.put(RitualsArea.ERROR_MESSAGE, "Text", "Invalid JQL");

            ExtensionResponse response = ritualsArea.getResultsForFetchIssuesByJQLQuery("task-1", eventData, "task-1");

            assertNotNull(response);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_FAILURE), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Get Results for Fetch Issues by JQL Query");
        }

        @Test
        @DisplayName("event matches - ClassCastException returns empty list")
        void eventMatches_classCastException_returnsEmptyList() {
            FreeForm eventData = new FreeForm();
            eventData.put(RitualsArea.IS_ERROR, "Switch", false);
            eventData.put("Data", "Text", "not a list");

            ExtensionResponse response = ritualsArea.getResultsForFetchIssuesByJQLQuery("task-1", eventData, "task-1");

            assertNotNull(response);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_SUCCESS), eq(1L));
        }

        @Test
        @DisplayName("event does not match - throws IllegalStateException")
        void eventDoesNotMatch_throwsIllegalStateException() {
            FreeForm eventData = new FreeForm();

            assertThrows(IllegalStateException.class,
                    () -> ritualsArea.getResultsForFetchIssuesByJQLQuery("different-event", eventData, "task-1"));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get Results for Fetch Issues by JQL Query"),
                    any(IllegalStateException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("MustAuthorizeException from inner catch is re-thrown")
        void mustAuthorizeException_fromInnerCatch_isRethrown() {
            FreeForm eventData = mock(FreeForm.class);
            when(eventData.get(RitualsArea.IS_ERROR)).thenReturn(false);
            when(eventData.get("Data")).thenThrow(new MustAuthorizeException("Auth needed"));

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.getResultsForFetchIssuesByJQLQuery("task-1", eventData, "task-1"));

            verify(jsmAuditService).authFailure(eq(INVOKER_ID), eq("Get Results for Fetch Issues by JQL Query"),
                    any(MustAuthorizeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= getMyIssuesByGivenStatuses =========================

    @Nested
    @DisplayName("getMyIssuesByGivenStatuses")
    class GetMyIssuesByGivenStatusesTests {

        @Test
        @DisplayName("success - no invokeAsUser")
        void success_noInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of(new Issue()));

            ExtensionResponse response = ritualsArea.getMyIssuesByGivenStatuses(List.of("Open", "In Progress"));

            assertNotNull(response);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Get My Issues by Given Statuses");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getMyIssuesByGivenStatuses(List.of("Done"));

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.getMyIssuesByGivenStatuses(List.of("Open")));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Get My Issues by Given Statuses", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                    .thenThrow(new IllegalCallerException("Unauthorized"));

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.getMyIssuesByGivenStatuses(List.of("Open")));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get My Issues by Given Statuses"),
                    any(IllegalCallerException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("Network error"));

            ExtensionResponse response = ritualsArea.getMyIssuesByGivenStatuses(List.of("Open"));

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get My Issues by Given Statuses"),
                    any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= getIssues =========================

    @Nested
    @DisplayName("getIssues")
    class GetIssuesTests {

        @Test
        @DisplayName("success - all null filters, default page size and number")
        void success_allNullFilters() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ExtensionResponse response = ritualsArea.getIssues(null, null, null, null, null, null, null, null);

            assertNotNull(response);
            verify(issueDao).searchByJQL(anyString(), eq(20L), eq(1L));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_ISSUES_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Get Issues");
        }

        @Test
        @DisplayName("success - with custom page size and page number")
        void success_withCustomPageSizeAndNumber() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, null, null, null, null, 50.0, 3.0);

            verify(issueDao).searchByJQL(anyString(), eq(50L), eq(3L));
        }

        @Test
        @DisplayName("success - with zero page size and number uses defaults")
        void success_withZeroPageSizeAndNumber() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, null, null, null, null, 0.0, 0.0);

            verify(issueDao).searchByJQL(anyString(), eq(20L), eq(1L));
        }

        @Test
        @DisplayName("success - with negative page size and number uses defaults")
        void success_withNegativePageSizeAndNumber() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, null, null, null, null, -1.0, -1.0);

            verify(issueDao).searchByJQL(anyString(), eq(20L), eq(1L));
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, null, null, null, null, null, null);

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("projectKey present - validates project key")
        void projectKeyPresent_validatesProjectKey() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, null, null, null, PROJECT_KEY, null, null);

            verify(validationOrchestrator).validate(anyMap());
        }

        @Test
        @DisplayName("projectKey blank - skips project validation")
        void projectKeyBlank_skipsValidation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, null, null, null, "  ", null, null);

            verify(validationOrchestrator, never()).validate(anyMap());
        }

        @Test
        @DisplayName("issueType present with existing project - validates issue type")
        void issueTypePresent_withExistingProject_validatesIssueType() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(projectDao.isProjectExist(PROJECT_KEY)).thenReturn(true);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, "Bug", null, null, PROJECT_KEY, null, null);

            verify(validationOrchestrator, times(2)).validate(anyMap());
            verify(projectDao).isProjectExist(PROJECT_KEY);
        }

        @Test
        @DisplayName("issueType present with non-existing project - skips issue type validation")
        void issueTypePresent_withNonExistingProject_skipsIssueTypeValidation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(projectDao.isProjectExist(PROJECT_KEY)).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, "Bug", null, null, PROJECT_KEY, null, null);

            verify(validationOrchestrator, times(1)).validate(anyMap());
        }

        @Test
        @DisplayName("issueType present but projectKey null - skips issue type validation")
        void issueTypePresent_projectKeyNull_skipsIssueTypeValidation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, "Bug", null, null, null, null, null);

            verify(projectDao, never()).isProjectExist(anyString());
        }

        @Test
        @DisplayName("issueType present but projectKey blank - skips issue type validation (inner blank check)")
        void issueTypePresent_projectKeyBlank_skipsIssueTypeValidation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, "Bug", null, null, "  ", null, null);

            verify(projectDao, never()).isProjectExist(anyString());
            verify(validationOrchestrator, never()).validate(anyMap());
        }

        @Test
        @DisplayName("issueType blank - skips issue type validation")
        void issueTypeBlank_skipsIssueTypeValidation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getIssues(null, null, "  ", null, null, PROJECT_KEY, null, null);

            verify(projectDao, never()).isProjectExist(anyString());
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.getIssues(null, null, null, null, null, PROJECT_KEY, null, null);

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
            verify(issueDao, never()).searchByJQL(anyString(), anyLong(), anyLong());
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.getIssues(null, null, null, null, null, null, null, null));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Get Issues", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                    .thenThrow(new IllegalCallerException("Unauthorized"));

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.getIssues(null, null, null, null, null, null, null, null));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get Issues"), any(IllegalCallerException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("Network error"));

            ExtensionResponse response = ritualsArea.getIssues(null, null, null, null, null, null, null, null);

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get Issues"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= getUserIssueByIssueType =========================

    @Nested
    @DisplayName("getUserIssueByIssueType")
    class GetUserIssueByIssueTypeTests {

        @Test
        @DisplayName("success - no invokeAsUser")
        void success_noInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ExtensionResponse response = ritualsArea.getUserIssueByIssueType(ACCOUNT_ID, "Bug");

            assertNotNull(response);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Get User Issue by Issue Type");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());

            ritualsArea.getUserIssueByIssueType(ACCOUNT_ID, "Task");

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.getUserIssueByIssueType(ACCOUNT_ID, "Bug"));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Get User Issue by Issue Type", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                    .thenThrow(new IllegalCallerException("Unauthorized"));

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.getUserIssueByIssueType(ACCOUNT_ID, "Bug"));

            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                    .thenThrow(new RuntimeException("Error"));

            ExtensionResponse response = ritualsArea.getUserIssueByIssueType(ACCOUNT_ID, "Bug");

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get User Issue by Issue Type"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= addRequestParticipantsToIssue =========================

    @Nested
    @DisplayName("addRequestParticipantsToIssue")
    class AddRequestParticipantsToIssueTests {

        @Test
        @DisplayName("success - no invokeAsUser")
        void success_noInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(false);

            ExtensionResponse response = ritualsArea.addRequestParticipantsToIssue(ISSUE_KEY, List.of("user1", "user2"));

            assertNotNull(response);
            verify(issueDao).addRequestParticipants(ISSUE_KEY, List.of("user1", "user2"));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.ADD_REQUEST_PARTICIPANTS_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Add Request Participants to Issue");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);

            ritualsArea.addRequestParticipantsToIssue(ISSUE_KEY, List.of("user1"));

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.addRequestParticipantsToIssue(ISSUE_KEY, List.of("user1")));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Add Request Participants to Issue", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            doThrow(new RuntimeException("Error")).when(issueDao).addRequestParticipants(anyString(), anyList());

            ExtensionResponse response = ritualsArea.addRequestParticipantsToIssue(ISSUE_KEY, List.of("user1"));

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Add Request Participants to Issue"),
                    any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= updateAnIssue =========================

    @Nested
    @DisplayName("updateAnIssue")
    class UpdateAnIssueTests {

        @Test
        @DisplayName("success - validation passes, no worklog")
        void success_noWorklog() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(ISSUE_KEY)).thenReturn(new Issue());

            ExtensionResponse response = ritualsArea.updateAnIssue(ISSUE_KEY, "Done", "High",
                    "New Summary", "New Desc", "Assignee", null, List.of("label1"), List.of("comp1"), new FreeForm());

            assertNotNull(response);
            verify(issueDao).updateIssue(any(Issue.class), any(FreeForm.class));
            verify(issueDao).getIssueFinal(ISSUE_KEY);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.UPDATE_AN_ISSUE_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Update an Issue");
        }

        @Test
        @DisplayName("success - with non-blank worklog calls addWorklog")
        void success_withWorklog() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(ISSUE_KEY)).thenReturn(new Issue());

            ritualsArea.updateAnIssue(ISSUE_KEY, null, null, null, null, null, "2h", null, null, new FreeForm());

            verify(issueDao).updateIssue(any(Issue.class), any(FreeForm.class));
            verify(issueDao).addWorklog(eq("2h"), anyLong(), eq(ISSUE_KEY), eq(""));
        }

        @Test
        @DisplayName("success - blank worklog skips addWorklog")
        void success_blankWorklog_skipsAddWorklog() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(ISSUE_KEY)).thenReturn(new Issue());

            ritualsArea.updateAnIssue(ISSUE_KEY, null, null, null, null, null, "  ", null, null, new FreeForm());

            verify(issueDao, never()).addWorklog(anyString(), anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(anyString())).thenReturn(new Issue());

            ritualsArea.updateAnIssue(ISSUE_KEY, null, null, null, null, null, null, null, null, new FreeForm());

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.updateAnIssue(ISSUE_KEY, null, null, null, null, null,
                    null, null, null, new FreeForm());

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
            verify(issueDao, never()).updateIssue(any(), any());
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.updateAnIssue(ISSUE_KEY, null, null, null, null, null,
                            null, null, null, new FreeForm()));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Update an Issue", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new IllegalCallerException("Unauthorized")).when(issueDao).updateIssue(any(), any());

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.updateAnIssue(ISSUE_KEY, null, null, null, null, null,
                            null, null, null, new FreeForm()));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Update an Issue"), any(IllegalCallerException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new RuntimeException("Error")).when(issueDao).updateIssue(any(), any());

            ExtensionResponse response = ritualsArea.updateAnIssue(ISSUE_KEY, null, null, null, null, null,
                    null, null, null, new FreeForm());

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Update an Issue"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= updatePriorityOfIssue =========================

    @Nested
    @DisplayName("updatePriorityOfIssue")
    class UpdatePriorityOfIssueTests {

        @Test
        @DisplayName("success - validation passes")
        void success_validationPasses() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.updatePriority(any(), eq(ISSUE_KEY))).thenReturn(new Issue());

            ExtensionResponse response = ritualsArea.updatePriorityOfIssue(ISSUE_KEY, PRIORITY_NAME);

            assertNotNull(response);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_ISSUE_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Update Priority of Issue");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.updatePriority(any(), anyString())).thenReturn(new Issue());

            ritualsArea.updatePriorityOfIssue(ISSUE_KEY, PRIORITY_NAME);

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.updatePriorityOfIssue(ISSUE_KEY, PRIORITY_NAME);

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.updatePriorityOfIssue(ISSUE_KEY, PRIORITY_NAME));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Update Priority of Issue", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.updatePriority(any(), anyString()))
                    .thenThrow(new IllegalCallerException("Unauthorized"));

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.updatePriorityOfIssue(ISSUE_KEY, PRIORITY_NAME));

            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.updatePriority(any(), anyString()))
                    .thenThrow(new RuntimeException("Error"));

            ExtensionResponse response = ritualsArea.updatePriorityOfIssue(ISSUE_KEY, PRIORITY_NAME);

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Update Priority of Issue"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= updateThePriorityOfMultipleIssues =========================

    @Nested
    @DisplayName("updateThePriorityOfMultipleIssues")
    class UpdateThePriorityOfMultipleIssuesTests {

        @Test
        @DisplayName("success - no invokeAsUser")
        void success_noInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(issueDao.getIssueFinal(anyString())).thenReturn(new Issue());

            ExtensionResponse response = ritualsArea.updateThePriorityOfMultipleIssues(
                    List.of("JSM-1", "JSM-2"), PRIORITY_NAME);

            assertNotNull(response);
            verify(issueDao, times(2)).updatePriority(any(), anyString());
            verify(issueDao, times(2)).getIssueFinal(anyString());
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Update the Priority of Multiple Issues");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(issueDao.getIssueFinal(anyString())).thenReturn(new Issue());

            ritualsArea.updateThePriorityOfMultipleIssues(List.of(ISSUE_KEY), PRIORITY_NAME);

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.updateThePriorityOfMultipleIssues(List.of(ISSUE_KEY), PRIORITY_NAME));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Update the Priority of Multiple Issues", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            doThrow(new IllegalCallerException("Unauthorized")).when(issueDao).updatePriority(any(), anyString());

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.updateThePriorityOfMultipleIssues(List.of(ISSUE_KEY), PRIORITY_NAME));

            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            doThrow(new RuntimeException("Error")).when(issueDao).updatePriority(any(), anyString());

            ExtensionResponse response = ritualsArea.updateThePriorityOfMultipleIssues(
                    List.of(ISSUE_KEY), PRIORITY_NAME);

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Update the Priority of Multiple Issues"),
                    any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= addWorklog =========================

    @Nested
    @DisplayName("addWorklog")
    class AddWorklogTests {

        @Test
        @DisplayName("success - validation passes")
        void success_validationPasses() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());

            ExtensionResponse response = ritualsArea.addWorklog(ISSUE_KEY, 1000L, "2h");

            assertNotNull(response);
            verify(issueDao).addWorklog("2h", 1000L, ISSUE_KEY, "");
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.ADD_WORK_LOG_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Add Worklog");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());

            ritualsArea.addWorklog(ISSUE_KEY, 1000L, "1h");

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.addWorklog(ISSUE_KEY, 1000L, "2h");

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
            verify(issueDao, never()).addWorklog(anyString(), anyLong(), anyString(), anyString());
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.addWorklog(ISSUE_KEY, 1000L, "2h"));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Add Worklog", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new IllegalCallerException("Unauthorized")).when(issueDao)
                    .addWorklog(anyString(), anyLong(), anyString(), anyString());

            assertThrows(RuntimeException.class, () -> ritualsArea.addWorklog(ISSUE_KEY, 1000L, "2h"));

            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new RuntimeException("Error")).when(issueDao)
                    .addWorklog(anyString(), anyLong(), anyString(), anyString());

            ExtensionResponse response = ritualsArea.addWorklog(ISSUE_KEY, 1000L, "2h");

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Add Worklog"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= getIssueByKey =========================

    @Nested
    @DisplayName("getIssueByKey")
    class GetIssueByKeyTests {

        @Test
        @DisplayName("success - validation passes")
        void success_validationPasses() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(ISSUE_KEY)).thenReturn(new Issue());

            ExtensionResponse response = ritualsArea.getIssueByKey(ISSUE_KEY);

            assertNotNull(response);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_ISSUE_BY_KEY_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Get Issue by Key");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(anyString())).thenReturn(new Issue());

            ritualsArea.getIssueByKey(ISSUE_KEY);

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.getIssueByKey(ISSUE_KEY);

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class, () -> ritualsArea.getIssueByKey(ISSUE_KEY));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Get Issue by Key", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(anyString())).thenThrow(new IllegalCallerException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> ritualsArea.getIssueByKey(ISSUE_KEY));

            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getIssueFinal(anyString())).thenThrow(new RuntimeException("Error"));

            ExtensionResponse response = ritualsArea.getIssueByKey(ISSUE_KEY);

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get Issue by Key"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= assignChildIssueToEpic =========================

    @Nested
    @DisplayName("assignChildIssueToEpic")
    class AssignChildIssueToEpicTests {

        @Test
        @DisplayName("success - validation passes")
        void success_validationPasses() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());

            ExtensionResponse response = ritualsArea.assignChildIssueToEpic("CHILD-1", "EPIC-1");

            assertNotNull(response);
            verify(issueDao).assignChildIssueToEpic("CHILD-1", "EPIC-1");
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Assign Child Issue to Epic");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());

            ritualsArea.assignChildIssueToEpic("CHILD-1", "EPIC-1");

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.assignChildIssueToEpic("CHILD-1", "EPIC-1");

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.assignChildIssueToEpic("CHILD-1", "EPIC-1"));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Assign Child Issue to Epic", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new IllegalCallerException("Unauthorized")).when(issueDao)
                    .assignChildIssueToEpic(anyString(), anyString());

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.assignChildIssueToEpic("CHILD-1", "EPIC-1"));

            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new RuntimeException("Error")).when(issueDao)
                    .assignChildIssueToEpic(anyString(), anyString());

            ExtensionResponse response = ritualsArea.assignChildIssueToEpic("CHILD-1", "EPIC-1");

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Assign Child Issue to Epic"),
                    any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= assignIssueToEpic =========================

    @Nested
    @DisplayName("assignIssueToEpic")
    class AssignIssueToEpicTests {

        @Test
        @DisplayName("success - validation passes")
        void success_validationPasses() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());

            ExtensionResponse response = ritualsArea.assignIssueToEpic(ISSUE_KEY, "EPIC-1");

            assertNotNull(response);
            verify(issueDao).assignIssueToEpic(ISSUE_KEY, "EPIC-1");
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.ASSIGN_ISSUE_TO_EPIC_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Assign Issue to Epic");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());

            ritualsArea.assignIssueToEpic(ISSUE_KEY, "EPIC-1");

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.assignIssueToEpic(ISSUE_KEY, "EPIC-1");

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.assignIssueToEpic(ISSUE_KEY, "EPIC-1"));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Assign Issue to Epic", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            doThrow(new IllegalCallerException("Unauthorized")).when(issueDao)
                    .assignIssueToEpic(anyString(), anyString());

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.assignIssueToEpic(ISSUE_KEY, "EPIC-1"));

            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception with JSON errorMessages returns failure response")
        void genericException_withJsonErrorMessages_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            String errorJson = "{\"errorMessages\": [\"Epic does not exist\"]}";
            doThrow(new RuntimeException(errorJson)).when(issueDao)
                    .assignIssueToEpic(anyString(), anyString());

            ExtensionResponse response = ritualsArea.assignIssueToEpic(ISSUE_KEY, "EPIC-1");

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Assign Issue to Epic"),
                    any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= getAllComments =========================

    @Nested
    @DisplayName("getAllComments")
    class GetAllCommentsTests {

        @Test
        @DisplayName("success - validation passes")
        void success_validationPasses() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getComments(ISSUE_KEY)).thenReturn(List.of());

            ExtensionResponse response = ritualsArea.getAllComments(ISSUE_KEY);

            assertNotNull(response);
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_ALL_COMMENTS_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Get All Comments");
        }

        @Test
        @DisplayName("success - with invokeAsUser validates credentials")
        void success_withInvokeAsUser() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getComments(anyString())).thenReturn(List.of());

            ritualsArea.getAllComments(ISSUE_KEY);

            verify(issueDao).validateCredentials();
        }

        @Test
        @DisplayName("validation failure returns confirmation response")
        void validationFailure_returnsConfirmation() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
            ExtensionResponse confirmation = mockResponse();
            when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                    .thenReturn(confirmation);

            ExtensionResponse response = ritualsArea.getAllComments(ISSUE_KEY);

            assertSame(confirmation, response);
            verify(internalStateManager).put(anyString(), anyString());
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            MustAuthorizeException authEx = mock(MustAuthorizeException.class);
            when(requestContext.invokeAsUser()).thenReturn(true);
            doThrow(authEx).when(issueDao).validateCredentials();

            assertThrows(MustAuthorizeException.class, () -> ritualsArea.getAllComments(ISSUE_KEY));

            verify(jsmAuditService).authFailure(INVOKER_ID, "Get All Comments", authEx);
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("IllegalCallerException is wrapped in RuntimeException")
        void illegalCallerException_wrappedInRuntimeException() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getComments(anyString())).thenThrow(new IllegalCallerException("Unauthorized"));

            assertThrows(RuntimeException.class, () -> ritualsArea.getAllComments(ISSUE_KEY));

            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception returns failure response")
        void genericException_returnsFailureResponse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
            when(issueDao.getComments(anyString())).thenThrow(new RuntimeException("Error"));

            ExtensionResponse response = ritualsArea.getAllComments(ISSUE_KEY);

            assertNotNull(response);
            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get All Comments"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= getNotification =========================

    @Nested
    @DisplayName("getNotification")
    class GetNotificationTests {

        @Test
        @DisplayName("success - event name matches JIRA_UPDATES")
        void success_eventNameMatchesJiraUpdates() {
            FreeForm eventData = new FreeForm();
            eventData.put(UPDATES_ON_JIRA, "Text", "{}");

            Map<String, Object> result = ritualsArea.getNotification(JIRA_UPDATES, eventData);

            assertNotNull(result);
            verify(jsmAuditService).success(INVOKER_ID, "Get Notification");
        }

        @Test
        @DisplayName("event name does not match - returns empty map")
        void eventNameDoesNotMatch_returnsEmptyMap() {
            FreeForm eventData = new FreeForm();

            Map<String, Object> result = ritualsArea.getNotification("Other Event", eventData);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(jsmAuditService).success(INVOKER_ID, "Get Notification");
        }

        @Test
        @DisplayName("exception is re-thrown and audit failure recorded")
        void exception_isRethrown() {
            FreeForm eventData = mock(FreeForm.class);
            when(eventData.get(UPDATES_ON_JIRA)).thenThrow(new RuntimeException("Parse error"));

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.getNotification(JIRA_UPDATES, eventData));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get Notification"), any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }

    // ========================= getCustomFieldValue =========================

    @Nested
    @DisplayName("getCustomFieldValue")
    class GetCustomFieldValueTests {

        @Test
        @DisplayName("success - returns custom field values")
        void success_returnsCustomFieldValues() {
            when(issueDao.getCustomIssue(ISSUE_KEY, "customfield_10001")).thenReturn(List.of("value1", "value2"));

            List<String> result = ritualsArea.getCustomFieldValue(ISSUE_KEY, "customfield_10001");

            assertNotNull(result);
            assertEquals(2, result.size());
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_CUSTOM_FIELD_VALUE_REQUEST_COUNT), eq(1L));
            verify(telemetryMetrics).incrementCounter(
                    eq(TelemetryConstants.RitualsArea.GET_CUSTOM_FIELD_VALUE_REQUEST_SUCCESS), eq(1L));
            verify(jsmAuditService).success(INVOKER_ID, "Get Custom Field Value");
        }

        @Test
        @DisplayName("MustAuthorizeException is re-thrown")
        void mustAuthorizeException_isRethrown() {
            when(issueDao.getCustomIssue(anyString(), anyString()))
                    .thenThrow(new MustAuthorizeException("Auth needed"));

            assertThrows(MustAuthorizeException.class,
                    () -> ritualsArea.getCustomFieldValue(ISSUE_KEY, "customfield_10001"));

            verify(jsmAuditService).authFailure(eq(INVOKER_ID), eq("Get Custom Field Value"),
                    any(MustAuthorizeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }

        @Test
        @DisplayName("generic Exception is re-thrown")
        void genericException_isRethrown() {
            when(issueDao.getCustomIssue(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Error"));

            assertThrows(RuntimeException.class,
                    () -> ritualsArea.getCustomFieldValue(ISSUE_KEY, "customfield_10001"));

            verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get Custom Field Value"),
                    any(RuntimeException.class));
            verify(jsmAuditService, never()).success(any(), any());
        }
    }
}
