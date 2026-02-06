# Create an Issue

## Overview

Creates a new Jira issue with the specified summary, description, issue type, and optional request type for Service
Management projects.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  Yes - If validation fails, you can re-enter invalid parameters

## Input Parameters

| Parameter Name | Type | Required | Description                                                           | Example                                   |
|----------------|------|----------|-----------------------------------------------------------------------|-------------------------------------------|
| Project Key    | Text | Yes      | The key of the Jira project where the issue will be created           | `"SUP"`, `"PROJ"`                         |
| Summary        | Text | Yes      | Brief title/summary of the issue                                      | `"Laptop not connecting to WiFi"`         |
| Issue Type     | Text | Yes      | Type of issue to create                                               | `"Bug"`, `"Task"`, `"Service Request"`    |
| Description    | Text | Yes      | Detailed description of the issue                                     | `"User reports unable to connect..."`     |
| Request Type   | Text | No       | Service Desk request type name (for Service Management projects only) | `"Get IT help"`, `"Request new software"` |

### Parameter Details

#### Project Key

- Must be a valid, existing project key in your Jira instance
- Case-sensitive
- Typically 2-10 uppercase characters
- Validation: Checks if project exists before creating issue

#### Summary

- Maximum length: 255 characters
- Should be concise and descriptive
- Required for all issue types
- Validation: Cannot be empty or whitespace only

#### Issue Type

- Must be a valid issue type for the specified project
- Common types: Bug, Task, Story, Epic, Service Request, Incident
- Case-sensitive
- Validation: Checks if issue type exists in the project

#### Description

- Supports plain text
- Can include detailed information, steps to reproduce, etc.
- If not provided, defaults to the summary value

#### Request Type

- **Only for Jira Service Management projects**
- Must match the exact name of a request type in the service desk
- Case-insensitive matching
- Examples: "Get IT help", "Request new software", "Report a system problem"
- Leave empty for non-Service Management projects
- The extension automatically resolves the request type name to the correct field value

> **💡 Tip**: To find available request types, use the [Get Request Type Field Value](/pages/GetRequestTypeFieldValue.md)
> catalog request.

## Output Parameters

| Parameter Name | Type          | Description                                  |
|----------------|---------------|----------------------------------------------|
| New Issue      | Entity(Issue) | The newly created issue with all its details |

### Issue Entity Structure

The returned Issue entity contains:

- **Key**: Issue key (e.g., "SUP-123")
- **ID**: Internal Jira issue ID
- **Summary**: Issue summary
- **Description**: Issue description
- **Status**: Current status (e.g., "Open", "To Do")
- **Issue Type**: Type of issue
- **Priority**: Issue priority
- **Assignee**: Assigned user (if any)
- **Reporter**: User who created the issue
- **Created**: Creation timestamp
- **Updated**: Last update timestamp
- **Request Type**: Request type name (for Service Management issues)
- **Project Key**: Project key
- **Project Name**: Project name

## Validation Rules

| Validation                        | Error Message                                                                          | Resolution                                                                |
|-----------------------------------|----------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| Project Key exists                | "Provided project key is not valid: {projectKey}"                                      | Verify the project key exists in Jira and you have access to it           |
| Issue Type valid for project      | "Provided issue type: {issueType} is not valid for provided project key: {projectKey}" | Check that the issue type is available in the project's issue type scheme |
| Summary not empty                 | "Summary cannot be empty"                                                              | Provide a non-empty summary                                               |
| Request Type exists (if provided) | "Request type '{requestType}' not found"                                               | Verify the request type name matches exactly (case-insensitive)           |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing required parameters

**Common Scenarios**:

- Project key doesn't exist
- Issue type not available in the project
- Summary is empty or whitespace only
- Request type name doesn't match any available request types

**Error Messages**:

- `"Provided project key is not valid: {projectKey}"`
- `"Provided issue type: {issueType} is not valid for provided project key: {projectKey}"`
- `"Summary cannot be empty"`
- `"Request type '{requestType}' not found in the {count} available request types"`

**Resolution**:

1. Verify all required parameters are provided
2. Check project key spelling and case
3. Confirm issue type is available in the project
4. Ensure summary is not empty
5. For request types, check the exact name in Jira Service Management

### Logic Errors (LOGIC_ERROR)

**Cause**: Business logic failures during issue creation

**Common Scenarios**:

- Insufficient permissions to create issues in the project
- Required custom fields not provided
- Field validation failures (e.g., invalid field values)

**Error Message**: `"Failed to create ticket: {error details}"`

**Resolution**:

1. Verify you have "Create Issues" permission in the project
2. Check if the project requires additional custom fields
3. Review Jira project configuration for mandatory fields
4. Ensure all field values meet Jira's validation rules

### System Errors (SYSTEM_ERROR)

**Cause**: System-level failures

**Common Scenarios**:

- Network connectivity issues
- Jira API unavailable
- Authentication token expired
- Internal server errors

**Error Message**: `"Error occurred while creating issue"`

**Resolution**:

1. Check network connectivity to Jira
2. Verify Jira instance is accessible
3. Re-authenticate if using OAuth 2.0
4. Check Jira system status
5. Review extension logs for detailed error information

## Usage Examples

### Example 1: Create a Simple Bug

**Input**:

```
Project Key: "PROJ"
Summary: "Login button not working"
Issue Type: "Bug"
Description: "When users click the login button, nothing happens. Browser console shows no errors."
Request Type: (empty)
```

**Output**:

```json
{
  "New Issue": {
    "key": "PROJ-456",
    "id": "10234",
    "summary": "Login button not working",
    "description": "When users click the login button, nothing happens...",
    "status": "To Do",
    "issueType": "Bug",
    "priority": "Medium",
    "reporter": "john.doe@company.com",
    "created": "2024-01-15T10:30:00.000Z",
    "projectKey": "PROJ"
  }
}
```

**Result**: Bug created successfully in project PROJ

### Example 2: Create a Service Request with Request Type

**Input**:

```
Project Key: "SUP"
Summary: "Need new laptop for remote work"
Issue Type: "Service Request"
Description: "Current laptop is 5 years old and running slowly. Need a new laptop for remote work."
Request Type: "Request new hardware"
```

**Output**:

```json
{
  "New Issue": {
    "key": "SUP-789",
    "id": "10567",
    "summary": "Need new laptop for remote work",
    "description": "Current laptop is 5 years old...",
    "status": "Waiting for support",
    "issueType": "Service Request",
    "requestType": "Request new hardware",
    "priority": "Medium",
    "reporter": "jane.smith@company.com",
    "created": "2024-01-15T14:20:00.000Z",
    "projectKey": "SUP"
  }
}
```

**Result**: Service request created with request type "Request new hardware"

### Example 3: Create a Task

**Input**:

```
Project Key: "TEAM"
Summary: "Update documentation for API v2"
Issue Type: "Task"
Description: "Update all API documentation to reflect changes in version 2.0"
Request Type: (empty)
```

**Output**:

```json
{
  "New Issue": {
    "key": "TEAM-123",
    "id": "10890",
    "summary": "Update documentation for API v2",
    "description": "Update all API documentation to reflect changes in version 2.0",
    "status": "To Do",
    "issueType": "Task",
    "priority": "Medium",
    "projectKey": "TEAM"
  }
}
```

**Result**: Task created successfully

### Example 4: Retry Scenario - Invalid Project Key

**Initial Input**:

```
Project Key: "INVALID"
Summary: "Test issue"
Issue Type: "Bug"
Description: "Testing issue creation"
```

**Error Response**:

```
Error: "Provided project key is not valid: INVALID"
Prompt: "Would you like to re-enter the project key?"
```

**Corrected Input**:

```
Project Key: "PROJ"
(other fields remain the same)
```

**Result**: Issue created successfully after correction

## Business Rules

1. **Project Access**: User must have "Create Issues" permission in the target project
2. **Issue Type Availability**: Issue type must be enabled in the project's issue type scheme
3. **Request Type Resolution**: Request type names are automatically resolved to field values for Service Management
   projects
4. **Default Values**: If description is not provided, it defaults to the summary value
5. **Reporter Assignment**: The issue reporter is automatically set to the authenticated user
6. **Status Assignment**: New issues are created with the default status for the issue type (typically "To Do" or "
   Open")
7. **Service Desk Detection**: The extension automatically detects if a project is a Service Desk project and handles
   request types accordingly

## Limitations

1. **Custom Fields**: This catalog request does not support setting custom fields beyond Request Type
2. **Attachments**: Files cannot be attached during issue creation (use [Attach File to Ticket](AttachFileToTicket.md)
   after creation)
3. **Assignee**: Cannot specify assignee during creation (issue is created unassigned)
4. **Labels**: Cannot add labels during creation
5. **Components**: Cannot set components during creation
6. **Fix Versions**: Cannot set fix versions during creation
7. **Request Type Limit**: Only one request type can be specified per issue
8. **Description Format**: Only plain text is supported (no rich text or Atlassian Document Format)

## Best Practices

### 1. Use Descriptive Summaries

Write clear, concise summaries that describe the issue at a glance. Avoid vague titles like "Problem" or "Issue".

**Good**: "Login button returns 500 error on mobile devices"
**Bad**: "Login problem"

### 2. Provide Detailed Descriptions

Include all relevant information in the description:

- What happened
- Expected behavior
- Steps to reproduce
- Environment details
- Impact on users

### 3. Choose the Correct Issue Type

Select the issue type that best matches the nature of the work:

- **Bug**: Defects or errors in existing functionality
- **Task**: Work items that need to be completed
- **Story**: User stories or features
- **Service Request**: Requests for service or support (Service Management)
- **Incident**: Service disruptions or outages (Service Management)

### 4. Verify Request Type Names

For Service Management projects, verify the exact request type name before creating issues.
Use [Get Request Type Field Value](GetRequestTypeFieldValue.md) to list available request types.

### 5. Handle Validation Errors Gracefully

When validation fails, use the retry mechanism to correct invalid inputs rather than creating a new request.

### 6. Use Service Accounts for Automation

For automated issue creation, use a dedicated service account with appropriate permissions rather than personal
accounts.

## Common Use Cases

### 1. Automated Bug Reporting

```
Scenario: Application detects an error and automatically creates a bug report
Action: Call Create an Issue with error details
Result: Bug ticket created with stack trace and error information
```

### 2. Service Desk Request Automation

```
Scenario: Employee submits a request through a portal
Action: Create Service Request with appropriate request type
Result: Service desk ticket created and routed to correct team
```

### 3. Task Creation from Workflow

```
Scenario: Approval workflow creates follow-up tasks
Action: Create Task issues for each required action
Result: Tasks created and assigned to workflow participants
```

### 4. Incident Management

```
Scenario: Monitoring system detects service outage
Action: Create Incident with severity and impact details
Result: Incident ticket created for immediate response
```

## Related Catalog Requests

- [Attach File to Ticket](AttachFileToTicket.md) - Add files to the created issue
- [Update Status of Multiple Issues](UpdateStatusOfMultipleIssues.md) - Change issue status
- [Get Request Type Field Value](GetRequestTypeFieldValue.md) - Find available request types
- [Get Issues](GetIssues.md) - Query created issues

## Technical Implementation

### Helper Class

- **Class**: OrganizationAreaSubCatalogRequests
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Validation**: ValidationOrchestrator validates project key, issue type, and summary
- **Service**: IssueDao.createIssueWithRequestTypeName()

### Request Type Resolution Flow

1. Extension receives request type name (e.g., "Get IT help")
2. Calls `getServiceDeskIdForProject()` to find service desk ID
3. Calls `getRequestTypeFieldValueByName()` to resolve name to field value
4. Retrieves all request types from Service Desk API
5. Matches request type name (case-insensitive)
6. Returns field value in format "serviceDeskId/requestTypeId"
7. Adds field value to issue creation payload

### Validation Flow

1. Validates project key exists
2. Validates issue type is available in project
3. Validates summary is not empty
4. If validation fails, prompts user to re-enter invalid fields
5. Re-validates corrected inputs
6. Creates issue if all validations pass

### Telemetry Metrics

- `jira.issue.created` - Incremented when issue is created successfully
- `jira.issue.creation.failed` - Incremented when creation fails
- `jira.validation.failed` - Incremented when validation fails

## Troubleshooting

### Issue: "Provided project key is not valid"

**Cause**: Project doesn't exist or user doesn't have access
**Solution**:

1. Verify project key spelling (case-sensitive)
2. Check user has access to the project
3. Confirm project exists in Jira

### Issue: "Provided issue type is not valid for provided project key"

**Cause**: Issue type not available in project's issue type scheme
**Solution**:

1. Check project's issue type scheme in Jira
2. Verify issue type name spelling (case-sensitive)
3. Use a different issue type available in the project

### Issue: "Request type not found"

**Cause**: Request type name doesn't match any available request types
**Solution**:

1. Use [Get Request Type Field Value](/pages/GetRequestTypeFieldValue.md) to list available types
2. Verify exact spelling (case-insensitive but must match)
3. Ensure project is a Service Desk project

### Issue: "Failed to create ticket: Required fields are missing"

**Cause**: Project has required custom fields not provided
**Solution**:

1. Check project configuration for required fields
2. This catalog request only supports standard fields
3. Create issue manually or use Jira API directly for custom fields

### Issue: "403 Forbidden"

**Cause**: User doesn't have "Create Issues" permission
**Solution**:

1. Verify user has "Create Issues" permission in project
2. Check project permission scheme
3. For Service Management, verify Service Desk access

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md)
- [Authentication](/pages/Authentication.md)
- [Attach File to Ticket](/pages/AttachFileToTicket.md)
- [Get Request Type Field Value](/pages/GetRequestTypeFieldValue.md)
- [Jira REST API - Create Issue](/pages/https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/#api-rest-api-3-issue-post)

**Common Scenarios**:

- OAuth token expired
- API token revoked
- User doesn't have project access
- Missing required permissions

**Resolution**:

1. Re-authenticate with Jira
2. Verify user has access to the project
3. Check "Create Issues" permission in project
4. For Service Management, verify Service Desk access

