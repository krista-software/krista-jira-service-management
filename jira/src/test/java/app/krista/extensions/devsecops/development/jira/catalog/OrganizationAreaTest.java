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
import app.krista.extensions.devsecops.development.jira.api.JsmAuditService;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.KristaMediaClient;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import app.krista.model.base.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrganizationArea Tests")
class OrganizationAreaTest {

    @Mock private Invoker invoker;
    @Mock private IssueDao issueDao;
    @Mock private UserDao userDao;
    @Mock private KristaMediaClient kristaMediaClient;
    @Mock private ExtensionResponseGenerator responseGenerator;
    @Mock private ErrorHandlingStateManager internalStateManager;
    @Mock private ValidationOrchestrator validationOrchestrator;
    @Mock private TelemetryMetrics telemetryMetrics;
    @Mock private RequestContext requestContext;
    @Mock private JsmAuditService jsmAuditService;

    private OrganizationArea organizationArea;

    private static final String INVOKER_ID = "test-invoker-id";
    private static final String TICKET_ID = "TICKET-123";
    private static final String ISSUE_KEY = "PROJ-123";
    private static final String PROJECT_KEY = "PROJ";
    private static final String SUMMARY = "Test Summary";
    private static final String ISSUE_TYPE = "Task";
    private static final String DESCRIPTION = "Test Description";
    private static final String REQUEST_TYPE = "Get IT help";
    private static final String EMAIL = "test@example.com";
    private static final String ACCOUNT_ID = "account123";

    @BeforeEach
    void setUp() {
        organizationArea = new OrganizationArea(
                invoker, issueDao, userDao, kristaMediaClient,
                responseGenerator, internalStateManager, validationOrchestrator,
                telemetryMetrics, requestContext, jsmAuditService);
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

    private Map<String, Object> userResponseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("Self", "https://api.atlassian.com/user/abc");
        map.put("Account Id", ACCOUNT_ID);
        map.put("Email Address", EMAIL);
        map.put("Display Name", "Test User");
        return map;
    }

    // ========================= attachFileToTicket =========================

    @Test
    @DisplayName("attachFileToTicket - success, no invokeAsUser, empty file list")
    void attachFileToTicket_success_noInvokeAsUser() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.attachFileToTicket(anyList(), eq(TICKET_ID))).thenReturn(true);

        ExtensionResponse response = organizationArea.attachFileToTicket(TICKET_ID, Collections.emptyList());

        assertNotNull(response);
        verify(issueDao, never()).validateCredentials();
        verify(issueDao).attachFileToTicket(anyList(), eq(TICKET_ID));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.attachFileToTicket.count"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.attachFileToTicket.success"), eq(1L));
        verify(jsmAuditService).triggered(INVOKER_ID, "Attach File to Ticket");
        verify(jsmAuditService).success(INVOKER_ID, "Attach File to Ticket");
        verify(jsmAuditService, never()).failure(any(), any(), any());
        verify(jsmAuditService, never()).authFailure(any(), any(), any());
    }

    @Test
    @DisplayName("attachFileToTicket - success, invokeAsUser=true validates credentials")
    void attachFileToTicket_success_withInvokeAsUser() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.attachFileToTicket(anyList(), eq(TICKET_ID))).thenReturn(false);

        ExtensionResponse response = organizationArea.attachFileToTicket(TICKET_ID, Collections.emptyList());

        assertNotNull(response);
        verify(issueDao).validateCredentials();
        verify(jsmAuditService).success(INVOKER_ID, "Attach File to Ticket");
    }

    @Test
    @DisplayName("attachFileToTicket - success with non-empty file list, toJavaFile succeeds")
    void attachFileToTicket_success_withFileList_toJavaFileSucceeds() throws IOException {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(kristaMediaClient.toJavaFile(any())).thenReturn(new java.io.File("test.pdf"));
        when(issueDao.attachFileToTicket(anyList(), eq(TICKET_ID))).thenReturn(true);

        File mockFile = mock(File.class);
        ExtensionResponse response = organizationArea.attachFileToTicket(TICKET_ID, List.of(mockFile));

        assertNotNull(response);
        verify(kristaMediaClient).toJavaFile(any());
        verify(issueDao).attachFileToTicket(anyList(), eq(TICKET_ID));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.attachFileToTicket.success"), eq(1L));
        verify(jsmAuditService).success(INVOKER_ID, "Attach File to Ticket");
    }

    @Test
    @DisplayName("attachFileToTicket - IOException from toJavaFile is swallowed, method still completes")
    void attachFileToTicket_ioExceptionFromToJavaFile_swallowed() throws IOException {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.attachFileToTicket(anyList(), eq(TICKET_ID))).thenReturn(true);
        when(kristaMediaClient.toJavaFile(any())).thenThrow(new IOException("IO failure"));

        // File mock: GSON will serialize it to {} and deserialize to a new File; the
        // mock itself is never called after deserialization, so no stubs on it.
        File mockFile = mock(File.class);

        ExtensionResponse response = organizationArea.attachFileToTicket(TICKET_ID, List.of(mockFile));

        assertNotNull(response);
        // toJavaFile threw IOException → ticketFiles is empty; attachFileToTicket still called
        verify(issueDao).attachFileToTicket(anyList(), eq(TICKET_ID));
        verify(jsmAuditService).success(INVOKER_ID, "Attach File to Ticket");
    }

    @Test
    @DisplayName("attachFileToTicket - validation failure returns confirmation response")
    void attachFileToTicket_validationFailure_returnsConfirmationResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
        ExtensionResponse confirmation = mockResponse();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(confirmation);

        ExtensionResponse response = organizationArea.attachFileToTicket(TICKET_ID, Collections.emptyList());

        assertSame(confirmation, response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.attachFileToTicket.retry.attempt"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.attachFileToTicket.success"), eq(1L));
        verify(internalStateManager).put(anyString(), anyString());
        verify(jsmAuditService).success(INVOKER_ID, "Attach File to Ticket");
        verify(issueDao, never()).attachFileToTicket(anyList(), anyString());
    }

    @Test
    @DisplayName("attachFileToTicket - MustAuthorizeException is re-thrown and audit recorded")
    void attachFileToTicket_mustAuthorizeException_isRethrown() {
        MustAuthorizeException authEx = mock(MustAuthorizeException.class);
        when(requestContext.invokeAsUser()).thenReturn(true);
        doThrow(authEx).when(issueDao).validateCredentials();

        assertThrows(MustAuthorizeException.class,
                () -> organizationArea.attachFileToTicket(TICKET_ID, Collections.emptyList()));

        verify(jsmAuditService).authFailure(INVOKER_ID, "Attach File to Ticket", authEx);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.attachFileToTicket.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("attachFileToTicket - IllegalCallerException is wrapped in RuntimeException")
    void attachFileToTicket_illegalCallerException_wrappedInRuntimeException() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.attachFileToTicket(anyList(), eq(TICKET_ID)))
                .thenThrow(new IllegalCallerException("Unauthorized caller"));

        assertThrows(RuntimeException.class,
                () -> organizationArea.attachFileToTicket(TICKET_ID, Collections.emptyList()));

        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Attach File to Ticket"), any(IllegalCallerException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.attachFileToTicket.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("attachFileToTicket - generic Exception returns failure ExtensionResponse")
    void attachFileToTicket_genericException_returnsFailureResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.attachFileToTicket(anyList(), eq(TICKET_ID)))
                .thenThrow(new RuntimeException("Unexpected failure"));

        ExtensionResponse response = organizationArea.attachFileToTicket(TICKET_ID, Collections.emptyList());

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Attach File to Ticket"), any(RuntimeException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.attachFileToTicket.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    // ========================= getRequestTypeFieldValue =========================

    @Test
    @DisplayName("getRequestTypeFieldValue - success with non-null field value")
    void getRequestTypeFieldValue_success_nonNullFieldValue() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(issueDao.getRequestTypeFieldValue(ISSUE_KEY, REQUEST_TYPE)).thenReturn("portal/key1");

        ExtensionResponse response = organizationArea.getRequestTypeFieldValue(ISSUE_KEY, REQUEST_TYPE);

        assertNotNull(response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getRequestTypeFieldValue.count"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getRequestTypeFieldValue.success"), eq(1L));
        verify(jsmAuditService).success(INVOKER_ID, "Get Request Type Field Value");
        verify(jsmAuditService, never()).failure(any(), any(), any());
    }

    @Test
    @DisplayName("getRequestTypeFieldValue - success with null field value returns fallback message")
    void getRequestTypeFieldValue_success_nullFieldValue_returnsFallback() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(issueDao.getRequestTypeFieldValue(ISSUE_KEY, "Unknown Type")).thenReturn(null);

        ExtensionResponse response = organizationArea.getRequestTypeFieldValue(ISSUE_KEY, "Unknown Type");

        assertNotNull(response);
        verify(jsmAuditService).success(INVOKER_ID, "Get Request Type Field Value");
    }

    @Test
    @DisplayName("getRequestTypeFieldValue - success with invokeAsUser=true")
    void getRequestTypeFieldValue_success_withInvokeAsUser() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(issueDao.getRequestTypeFieldValue(ISSUE_KEY, REQUEST_TYPE)).thenReturn("some/value");

        ExtensionResponse response = organizationArea.getRequestTypeFieldValue(ISSUE_KEY, REQUEST_TYPE);

        assertNotNull(response);
        verify(issueDao).validateCredentials();
        verify(jsmAuditService).success(INVOKER_ID, "Get Request Type Field Value");
    }

    @Test
    @DisplayName("getRequestTypeFieldValue - MustAuthorizeException is re-thrown and audit recorded")
    void getRequestTypeFieldValue_mustAuthorizeException_isRethrown() {
        MustAuthorizeException authEx = mock(MustAuthorizeException.class);
        when(requestContext.invokeAsUser()).thenReturn(true);
        doThrow(authEx).when(issueDao).validateCredentials();

        assertThrows(MustAuthorizeException.class,
                () -> organizationArea.getRequestTypeFieldValue(ISSUE_KEY, REQUEST_TYPE));

        verify(jsmAuditService).authFailure(INVOKER_ID, "Get Request Type Field Value", authEx);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getRequestTypeFieldValue.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("getRequestTypeFieldValue - generic Exception returns failure ExtensionResponse")
    void getRequestTypeFieldValue_genericException_returnsFailureResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(issueDao.getRequestTypeFieldValue(anyString(), anyString()))
                .thenThrow(new RuntimeException("Jira unavailable"));

        ExtensionResponse response = organizationArea.getRequestTypeFieldValue(ISSUE_KEY, REQUEST_TYPE);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get Request Type Field Value"), any(RuntimeException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getRequestTypeFieldValue.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    // ========================= createAnIssue =========================

    @Test
    @DisplayName("createAnIssue - success with null request type")
    void createAnIssue_success_nullRequestType() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssueWithRequestTypeName(eq(PROJECT_KEY), eq(SUMMARY), eq(DESCRIPTION),
                isNull(), eq(ISSUE_TYPE), isNull())).thenReturn(new Issue());

        ExtensionResponse response = organizationArea.createAnIssue(PROJECT_KEY, SUMMARY, ISSUE_TYPE, DESCRIPTION, null);

        assertNotNull(response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createAnIssue.count"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createAnIssue.success"), eq(1L));
        verify(jsmAuditService).success(INVOKER_ID, "Create an issue");
    }

    @Test
    @DisplayName("createAnIssue - success with non-null request type")
    void createAnIssue_success_withRequestType() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssueWithRequestTypeName(eq(PROJECT_KEY), eq(SUMMARY), eq(DESCRIPTION),
                isNull(), eq(ISSUE_TYPE), eq(REQUEST_TYPE))).thenReturn(new Issue());

        ExtensionResponse response = organizationArea.createAnIssue(PROJECT_KEY, SUMMARY, ISSUE_TYPE, DESCRIPTION, REQUEST_TYPE);

        assertNotNull(response);
        verify(jsmAuditService).success(INVOKER_ID, "Create an issue");
    }

    @Test
    @DisplayName("createAnIssue - success with invokeAsUser=true")
    void createAnIssue_success_withInvokeAsUser() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssueWithRequestTypeName(anyString(), anyString(), anyString(),
                isNull(), anyString(), anyString())).thenReturn(new Issue());

        ExtensionResponse response = organizationArea.createAnIssue(PROJECT_KEY, SUMMARY, ISSUE_TYPE, DESCRIPTION, REQUEST_TYPE);

        assertNotNull(response);
        verify(issueDao).validateCredentials();
        verify(jsmAuditService).success(INVOKER_ID, "Create an issue");
    }

    @Test
    @DisplayName("createAnIssue - validation failure returns confirmation response")
    void createAnIssue_validationFailure_returnsConfirmationResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
        ExtensionResponse confirmation = mockResponse();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(confirmation);

        ExtensionResponse response = organizationArea.createAnIssue(PROJECT_KEY, SUMMARY, ISSUE_TYPE, DESCRIPTION, REQUEST_TYPE);

        assertSame(confirmation, response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createAnIssue.retry.attempt"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createAnIssue.success"), eq(1L));
        verify(internalStateManager).put(anyString(), anyString());
        verify(jsmAuditService).success(INVOKER_ID, "Create an issue");
    }

    @Test
    @DisplayName("createAnIssue - MustAuthorizeException is re-thrown and audit recorded")
    void createAnIssue_mustAuthorizeException_isRethrown() {
        MustAuthorizeException authEx = mock(MustAuthorizeException.class);
        when(requestContext.invokeAsUser()).thenReturn(true);
        doThrow(authEx).when(issueDao).validateCredentials();

        assertThrows(MustAuthorizeException.class,
                () -> organizationArea.createAnIssue(PROJECT_KEY, SUMMARY, ISSUE_TYPE, DESCRIPTION, REQUEST_TYPE));

        verify(jsmAuditService).authFailure(INVOKER_ID, "Create an issue", authEx);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createAnIssue.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("createAnIssue - IllegalCallerException is wrapped in RuntimeException")
    void createAnIssue_illegalCallerException_wrappedInRuntimeException() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssueWithRequestTypeName(anyString(), anyString(), anyString(),
                isNull(), anyString(), anyString()))
                .thenThrow(new IllegalCallerException("Unauthorized caller"));

        assertThrows(RuntimeException.class,
                () -> organizationArea.createAnIssue(PROJECT_KEY, SUMMARY, ISSUE_TYPE, DESCRIPTION, REQUEST_TYPE));

        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Create an issue"), any(IllegalCallerException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createAnIssue.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("createAnIssue - IllegalArgumentException returns INPUT_ERROR response")
    void createAnIssue_illegalArgumentException_returnsInputErrorResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssueWithRequestTypeName(anyString(), anyString(), anyString(),
                isNull(), anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid issue type provided"));

        ExtensionResponse response = organizationArea.createAnIssue(PROJECT_KEY, SUMMARY, ISSUE_TYPE, DESCRIPTION, REQUEST_TYPE);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Create an issue"), any(IllegalArgumentException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createAnIssue.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("createAnIssue - generic Exception with non-empty message uses that message")
    void createAnIssue_genericException_withMessage_returnsSystemErrorResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssueWithRequestTypeName(anyString(), anyString(), anyString(),
                isNull(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Jira API error"));

        ExtensionResponse response = organizationArea.createAnIssue(PROJECT_KEY, SUMMARY, ISSUE_TYPE, DESCRIPTION, REQUEST_TYPE);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Create an issue"), any(RuntimeException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createAnIssue.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("createAnIssue - generic Exception with null message falls back to toString")
    void createAnIssue_genericException_nullMessage_fallsBackToToString() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        RuntimeException exNullMsg = new RuntimeException((String) null);
        when(issueDao.createIssueWithRequestTypeName(anyString(), anyString(), anyString(),
                isNull(), anyString(), anyString()))
                .thenThrow(exNullMsg);

        ExtensionResponse response = organizationArea.createAnIssue(PROJECT_KEY, SUMMARY, ISSUE_TYPE, DESCRIPTION, REQUEST_TYPE);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Create an issue"), eq(exNullMsg));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createAnIssue.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("createAnIssue - generic Exception with blank message falls back to toString")
    void createAnIssue_genericException_blankMessage_fallsBackToToString() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssueWithRequestTypeName(anyString(), anyString(), anyString(),
                isNull(), anyString(), anyString()))
                .thenThrow(new RuntimeException("   "));

        ExtensionResponse response = organizationArea.createAnIssue(PROJECT_KEY, SUMMARY, ISSUE_TYPE, DESCRIPTION, REQUEST_TYPE);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Create an issue"), any(RuntimeException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createAnIssue.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    // ========================= reportABug =========================

    @Test
    @DisplayName("reportABug - success, validation passes")
    void reportABug_success() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssue(eq(PROJECT_KEY), eq(SUMMARY), eq(SUMMARY), isNull(), anyString()))
                .thenReturn(new Issue());

        ExtensionResponse response = organizationArea.reportABug(PROJECT_KEY, SUMMARY);

        assertNotNull(response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.reportABug.count"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.reportABug.success"), eq(1L));
        verify(jsmAuditService).success(INVOKER_ID, "Report a bug");
        verify(jsmAuditService, never()).failure(any(), any(), any());
    }

    @Test
    @DisplayName("reportABug - success with invokeAsUser=true")
    void reportABug_success_withInvokeAsUser() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssue(anyString(), anyString(), anyString(), isNull(), anyString()))
                .thenReturn(new Issue());

        ExtensionResponse response = organizationArea.reportABug(PROJECT_KEY, SUMMARY);

        assertNotNull(response);
        verify(issueDao).validateCredentials();
        verify(jsmAuditService).success(INVOKER_ID, "Report a bug");
    }

    @Test
    @DisplayName("reportABug - validation failure returns confirmation response")
    void reportABug_validationFailure_returnsConfirmationResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
        ExtensionResponse confirmation = mockResponse();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(confirmation);

        ExtensionResponse response = organizationArea.reportABug(PROJECT_KEY, SUMMARY);

        assertSame(confirmation, response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.reportABug.retry.attempt"), eq(1L));
        verify(internalStateManager).put(anyString(), anyString());
        verify(jsmAuditService).success(INVOKER_ID, "Report a bug");
        verify(issueDao, never()).createIssue(anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    @DisplayName("reportABug - MustAuthorizeException is re-thrown and audit recorded")
    void reportABug_mustAuthorizeException_isRethrown() {
        MustAuthorizeException authEx = mock(MustAuthorizeException.class);
        when(requestContext.invokeAsUser()).thenReturn(true);
        doThrow(authEx).when(issueDao).validateCredentials();

        assertThrows(MustAuthorizeException.class,
                () -> organizationArea.reportABug(PROJECT_KEY, SUMMARY));

        verify(jsmAuditService).authFailure(INVOKER_ID, "Report a bug", authEx);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.reportABug.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("reportABug - IllegalCallerException is wrapped in RuntimeException")
    void reportABug_illegalCallerException_wrappedInRuntimeException() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssue(anyString(), anyString(), anyString(), isNull(), anyString()))
                .thenThrow(new IllegalCallerException("Unauthorized caller"));

        assertThrows(RuntimeException.class,
                () -> organizationArea.reportABug(PROJECT_KEY, SUMMARY));

        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Report a bug"), any(IllegalCallerException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.reportABug.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("reportABug - generic Exception returns failure ExtensionResponse")
    void reportABug_genericException_returnsFailureResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createIssue(anyString(), anyString(), anyString(), isNull(), anyString()))
                .thenThrow(new RuntimeException("Network error"));

        ExtensionResponse response = organizationArea.reportABug(PROJECT_KEY, SUMMARY);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Report a bug"), any(RuntimeException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.reportABug.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    // ========================= createUser =========================

    @Test
    @DisplayName("createUser - success with valid email")
    void createUser_success_validEmail() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createUser(EMAIL)).thenReturn(userResponseMap());

        ExtensionResponse response = organizationArea.createUser(EMAIL);

        assertNotNull(response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createUser.count"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createUser.success"), eq(1L));
        verify(jsmAuditService).success(INVOKER_ID, "Create User");
        verify(jsmAuditService, never()).failure(any(), any(), any());
    }

    @Test
    @DisplayName("createUser - success with invokeAsUser=true")
    void createUser_success_withInvokeAsUser() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createUser(anyString())).thenReturn(userResponseMap());

        ExtensionResponse response = organizationArea.createUser(EMAIL);

        assertNotNull(response);
        verify(issueDao).validateCredentials();
        verify(jsmAuditService).success(INVOKER_ID, "Create User");
    }

    @Test
    @DisplayName("createUser - validation failure returns confirmation response")
    void createUser_validationFailure_returnsConfirmationResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
        ExtensionResponse confirmation = mockResponse();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(confirmation);

        ExtensionResponse response = organizationArea.createUser(EMAIL);

        assertSame(confirmation, response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createUser.retry.attempt"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createUser.success"), eq(1L));
        verify(internalStateManager).put(anyString(), anyString());
        verify(jsmAuditService).success(INVOKER_ID, "Create User");
        verify(issueDao, never()).createUser(anyString());
    }

    @Test
    @DisplayName("createUser - MustAuthorizeException is re-thrown and audit recorded")
    void createUser_mustAuthorizeException_isRethrown() {
        MustAuthorizeException authEx = mock(MustAuthorizeException.class);
        when(requestContext.invokeAsUser()).thenReturn(true);
        doThrow(authEx).when(issueDao).validateCredentials();

        assertThrows(MustAuthorizeException.class,
                () -> organizationArea.createUser(EMAIL));

        verify(jsmAuditService).authFailure(INVOKER_ID, "Create User", authEx);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createUser.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("createUser - generic Exception returns failure ExtensionResponse")
    void createUser_genericException_returnsFailureResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.createUser(anyString())).thenThrow(new RuntimeException("Server error"));

        ExtensionResponse response = organizationArea.createUser(EMAIL);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Create User"), any(RuntimeException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.createUser.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    // ========================= updateUser =========================

    @Test
    @DisplayName("updateUser - success, user added to groups")
    void updateUser_success() {
        List<String> groupNames = List.of("group-a", "group-b");
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.updateUser(ACCOUNT_ID, groupNames)).thenReturn(true);

        ExtensionResponse response = organizationArea.updateUser(ACCOUNT_ID, groupNames);

        assertNotNull(response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.updateUser.count"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.updateUser.success"), eq(1L));
        verify(jsmAuditService).success(INVOKER_ID, "Update User");
        verify(jsmAuditService, never()).failure(any(), any(), any());
    }

    @Test
    @DisplayName("updateUser - success with invokeAsUser=true")
    void updateUser_success_withInvokeAsUser() {
        List<String> groupNames = List.of("group-a");
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.updateUser(anyString(), anyList())).thenReturn(false);

        ExtensionResponse response = organizationArea.updateUser(ACCOUNT_ID, groupNames);

        assertNotNull(response);
        verify(issueDao).validateCredentials();
        verify(jsmAuditService).success(INVOKER_ID, "Update User");
    }

    @Test
    @DisplayName("updateUser - validation failure returns confirmation response")
    void updateUser_validationFailure_returnsConfirmationResponse() {
        List<String> groupNames = List.of("group-a");
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
        ExtensionResponse confirmation = mockResponse();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(confirmation);

        ExtensionResponse response = organizationArea.updateUser(ACCOUNT_ID, groupNames);

        assertSame(confirmation, response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.updateUser.retry.attempt"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.updateUser.success"), eq(1L));
        verify(internalStateManager).put(anyString(), anyString());
        verify(jsmAuditService).success(INVOKER_ID, "Update User");
        verify(issueDao, never()).updateUser(anyString(), anyList());
    }

    @Test
    @DisplayName("updateUser - MustAuthorizeException is re-thrown and audit recorded")
    void updateUser_mustAuthorizeException_isRethrown() {
        MustAuthorizeException authEx = mock(MustAuthorizeException.class);
        when(requestContext.invokeAsUser()).thenReturn(true);
        doThrow(authEx).when(issueDao).validateCredentials();

        assertThrows(MustAuthorizeException.class,
                () -> organizationArea.updateUser(ACCOUNT_ID, List.of("group-a")));

        verify(jsmAuditService).authFailure(INVOKER_ID, "Update User", authEx);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.updateUser.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("updateUser - IllegalCallerException is wrapped in RuntimeException")
    void updateUser_illegalCallerException_wrappedInRuntimeException() {
        List<String> groupNames = List.of("group-a");
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.updateUser(anyString(), anyList()))
                .thenThrow(new IllegalCallerException("Unauthorized caller"));

        assertThrows(RuntimeException.class,
                () -> organizationArea.updateUser(ACCOUNT_ID, groupNames));

        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Update User"), any(IllegalCallerException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.updateUser.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("updateUser - generic Exception returns failure ExtensionResponse")
    void updateUser_genericException_returnsFailureResponse() {
        List<String> groupNames = List.of("group-a");
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.updateUser(anyString(), anyList()))
                .thenThrow(new RuntimeException("Network error"));

        ExtensionResponse response = organizationArea.updateUser(ACCOUNT_ID, groupNames);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Update User"), any(RuntimeException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.updateUser.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    // ========================= deleteUser =========================

    @Test
    @DisplayName("deleteUser - success, user deleted")
    void deleteUser_success() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.deleteUser(ACCOUNT_ID)).thenReturn(true);

        ExtensionResponse response = organizationArea.deleteUser(ACCOUNT_ID);

        assertNotNull(response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.deleteUser.count"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.deleteUser.success"), eq(1L));
        verify(jsmAuditService).success(INVOKER_ID, "Delete User");
        verify(jsmAuditService, never()).failure(any(), any(), any());
    }

    @Test
    @DisplayName("deleteUser - success with invokeAsUser=true")
    void deleteUser_success_withInvokeAsUser() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.deleteUser(anyString())).thenReturn(false);

        ExtensionResponse response = organizationArea.deleteUser(ACCOUNT_ID);

        assertNotNull(response);
        verify(issueDao).validateCredentials();
        verify(jsmAuditService).success(INVOKER_ID, "Delete User");
    }

    @Test
    @DisplayName("deleteUser - validation failure returns confirmation response")
    void deleteUser_validationFailure_returnsConfirmationResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
        ExtensionResponse confirmation = mockResponse();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(confirmation);

        ExtensionResponse response = organizationArea.deleteUser(ACCOUNT_ID);

        assertSame(confirmation, response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.deleteUser.retry.attempt"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.deleteUser.success"), eq(1L));
        verify(internalStateManager).put(anyString(), anyString());
        verify(jsmAuditService).success(INVOKER_ID, "Delete User");
        verify(issueDao, never()).deleteUser(anyString());
    }

    @Test
    @DisplayName("deleteUser - MustAuthorizeException is re-thrown and audit recorded")
    void deleteUser_mustAuthorizeException_isRethrown() {
        MustAuthorizeException authEx = mock(MustAuthorizeException.class);
        when(requestContext.invokeAsUser()).thenReturn(true);
        doThrow(authEx).when(issueDao).validateCredentials();

        assertThrows(MustAuthorizeException.class,
                () -> organizationArea.deleteUser(ACCOUNT_ID));

        verify(jsmAuditService).authFailure(INVOKER_ID, "Delete User", authEx);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.deleteUser.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("deleteUser - IllegalCallerException is wrapped in RuntimeException")
    void deleteUser_illegalCallerException_wrappedInRuntimeException() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.deleteUser(anyString()))
                .thenThrow(new IllegalCallerException("Unauthorized caller"));

        assertThrows(RuntimeException.class,
                () -> organizationArea.deleteUser(ACCOUNT_ID));

        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Delete User"), any(IllegalCallerException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.deleteUser.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("deleteUser - generic Exception returns failure ExtensionResponse")
    void deleteUser_genericException_returnsFailureResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.deleteUser(anyString()))
                .thenThrow(new RuntimeException("Server error"));

        ExtensionResponse response = organizationArea.deleteUser(ACCOUNT_ID);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Delete User"), any(RuntimeException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.deleteUser.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    // ========================= getUsersAccountId =========================

    @Test
    @DisplayName("getUsersAccountId - success with valid email")
    void getUsersAccountId_success_validEmail() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.getAccountIdByEmail(EMAIL)).thenReturn(ACCOUNT_ID);

        ExtensionResponse response = organizationArea.getUsersAccountId(EMAIL);

        assertNotNull(response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUsersAccountId.count"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUsersAccountId.success"), eq(1L));
        verify(jsmAuditService).success(INVOKER_ID, "Get Users Account Id");
        verify(jsmAuditService, never()).failure(any(), any(), any());
    }

    @Test
    @DisplayName("getUsersAccountId - success with invokeAsUser=true")
    void getUsersAccountId_success_withInvokeAsUser() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.getAccountIdByEmail(anyString())).thenReturn(ACCOUNT_ID);

        ExtensionResponse response = organizationArea.getUsersAccountId(EMAIL);

        assertNotNull(response);
        verify(issueDao).validateCredentials();
        verify(jsmAuditService).success(INVOKER_ID, "Get Users Account Id");
    }

    @Test
    @DisplayName("getUsersAccountId - validation failure returns confirmation response")
    void getUsersAccountId_validationFailure_returnsConfirmationResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(oneValidationError());
        ExtensionResponse confirmation = mockResponse();
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(confirmation);

        ExtensionResponse response = organizationArea.getUsersAccountId(EMAIL);

        assertSame(confirmation, response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUsersAccountId.retry.attempt"), eq(1L));
        verify(internalStateManager).put(anyString(), anyString());
        verify(jsmAuditService).success(INVOKER_ID, "Get Users Account Id");
        verify(issueDao, never()).getAccountIdByEmail(anyString());
    }

    @Test
    @DisplayName("getUsersAccountId - MustAuthorizeException is re-thrown and audit recorded")
    void getUsersAccountId_mustAuthorizeException_isRethrown() {
        MustAuthorizeException authEx = mock(MustAuthorizeException.class);
        when(requestContext.invokeAsUser()).thenReturn(true);
        doThrow(authEx).when(issueDao).validateCredentials();

        assertThrows(MustAuthorizeException.class,
                () -> organizationArea.getUsersAccountId(EMAIL));

        verify(jsmAuditService).authFailure(INVOKER_ID, "Get Users Account Id", authEx);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUsersAccountId.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("getUsersAccountId - IllegalCallerException is wrapped in RuntimeException")
    void getUsersAccountId_illegalCallerException_wrappedInRuntimeException() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.getAccountIdByEmail(anyString()))
                .thenThrow(new IllegalCallerException("Unauthorized caller"));

        assertThrows(RuntimeException.class,
                () -> organizationArea.getUsersAccountId(EMAIL));

        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get Users Account Id"), any(IllegalCallerException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUsersAccountId.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("getUsersAccountId - generic Exception returns failure ExtensionResponse")
    void getUsersAccountId_genericException_returnsFailureResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(validationOrchestrator.validate(anyMap())).thenReturn(noValidationErrors());
        when(issueDao.getAccountIdByEmail(anyString()))
                .thenThrow(new RuntimeException("Server error"));

        ExtensionResponse response = organizationArea.getUsersAccountId(EMAIL);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get Users Account Id"), any(RuntimeException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUsersAccountId.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    // ========================= getUserAccountInformation =========================

    @Test
    @DisplayName("getUserAccountInformation - returns empty User when searchQuery is null")
    void getUserAccountInformation_nullSearchQuery_returnsEmptyUser() {
        when(requestContext.invokeAsUser()).thenReturn(false);

        ExtensionResponse response = organizationArea.getUserAccountInformation(null);

        assertNotNull(response);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUserAccountInformation.count"), eq(1L));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUserAccountInformation.success"), eq(1L));
        verify(userDao, never()).getUserByEmailOrName(anyString());
        verify(jsmAuditService).success(INVOKER_ID, "Get User Account Information");
        verify(jsmAuditService, never()).failure(any(), any(), any());
    }

    @Test
    @DisplayName("getUserAccountInformation - returns empty User when searchQuery is empty string")
    void getUserAccountInformation_emptySearchQuery_returnsEmptyUser() {
        when(requestContext.invokeAsUser()).thenReturn(false);

        ExtensionResponse response = organizationArea.getUserAccountInformation("");

        assertNotNull(response);
        verify(userDao, never()).getUserByEmailOrName(anyString());
        verify(jsmAuditService).success(INVOKER_ID, "Get User Account Information");
    }

    @Test
    @DisplayName("getUserAccountInformation - returns empty User when searchQuery is blank (whitespace only)")
    void getUserAccountInformation_blankSearchQuery_returnsEmptyUser() {
        when(requestContext.invokeAsUser()).thenReturn(false);

        ExtensionResponse response = organizationArea.getUserAccountInformation("   ");

        assertNotNull(response);
        verify(userDao, never()).getUserByEmailOrName(anyString());
        verify(jsmAuditService).success(INVOKER_ID, "Get User Account Information");
    }

    @Test
    @DisplayName("getUserAccountInformation - returns User when found by email/name")
    void getUserAccountInformation_userFound_returnsUser() {
        User user = new User();
        user.accountId = ACCOUNT_ID;
        user.email = EMAIL;
        user.name = "Alice Smith";
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(userDao.getUserByEmailOrName(EMAIL)).thenReturn(user);

        ExtensionResponse response = organizationArea.getUserAccountInformation(EMAIL);

        assertNotNull(response);
        verify(userDao).getUserByEmailOrName(EMAIL);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUserAccountInformation.success"), eq(1L));
        verify(jsmAuditService).success(INVOKER_ID, "Get User Account Information");
    }

    @Test
    @DisplayName("getUserAccountInformation - returns empty User when userDao returns null")
    void getUserAccountInformation_userNotFound_returnsEmptyUser() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(userDao.getUserByEmailOrName("nobody@example.com")).thenReturn(null);

        ExtensionResponse response = organizationArea.getUserAccountInformation("nobody@example.com");

        assertNotNull(response);
        verify(userDao).getUserByEmailOrName("nobody@example.com");
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUserAccountInformation.success"), eq(1L));
        verify(jsmAuditService).success(INVOKER_ID, "Get User Account Information");
    }

    @Test
    @DisplayName("getUserAccountInformation - success with invokeAsUser=true validates credentials")
    void getUserAccountInformation_success_withInvokeAsUser() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(userDao.getUserByEmailOrName(anyString())).thenReturn(new User());

        ExtensionResponse response = organizationArea.getUserAccountInformation(EMAIL);

        assertNotNull(response);
        verify(issueDao).validateCredentials();
        verify(jsmAuditService).success(INVOKER_ID, "Get User Account Information");
    }

    @Test
    @DisplayName("getUserAccountInformation - MustAuthorizeException is re-thrown and audit recorded")
    void getUserAccountInformation_mustAuthorizeException_isRethrown() {
        MustAuthorizeException authEx = mock(MustAuthorizeException.class);
        when(requestContext.invokeAsUser()).thenReturn(true);
        doThrow(authEx).when(issueDao).validateCredentials();

        assertThrows(MustAuthorizeException.class,
                () -> organizationArea.getUserAccountInformation(EMAIL));

        verify(jsmAuditService).authFailure(INVOKER_ID, "Get User Account Information", authEx);
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUserAccountInformation.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("getUserAccountInformation - IllegalCallerException is wrapped in RuntimeException")
    void getUserAccountInformation_illegalCallerException_wrappedInRuntimeException() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(userDao.getUserByEmailOrName(anyString()))
                .thenThrow(new IllegalCallerException("Unauthorized caller"));

        assertThrows(RuntimeException.class,
                () -> organizationArea.getUserAccountInformation(EMAIL));

        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get User Account Information"), any(IllegalCallerException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUserAccountInformation.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }

    @Test
    @DisplayName("getUserAccountInformation - generic Exception returns failure ExtensionResponse")
    void getUserAccountInformation_genericException_returnsFailureResponse() {
        when(requestContext.invokeAsUser()).thenReturn(false);
        when(userDao.getUserByEmailOrName(anyString()))
                .thenThrow(new RuntimeException("Service unavailable"));

        ExtensionResponse response = organizationArea.getUserAccountInformation(EMAIL);

        assertNotNull(response);
        verify(jsmAuditService).failure(eq(INVOKER_ID), eq("Get User Account Information"), any(RuntimeException.class));
        verify(telemetryMetrics).incrementCounter(
                eq("Jsm.OrganizationArea.getUserAccountInformation.failure"), eq(1L));
        verify(jsmAuditService, never()).success(any(), any());
    }
}
