# Report a Bug

## Overview

Creates a new bug issue in a specified Jira project with a summary. This is a simplified version of "Create an Issue"
specifically for bug reporting.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type | Required | Description                                          | Example                         |
|----------------|------|----------|------------------------------------------------------|---------------------------------|
| Project key    | Text | Yes      | The key of the project where the bug will be created | `"PROJ"`                        |
| Summary        | Text | Yes      | Brief description of the bug                         | `"Login button not responding"` |

## Output Parameters

| Parameter Name | Type        | Description                                |
|----------------|-------------|--------------------------------------------|
| New Bug        | Entity(Bug) | The created bug issue with all its details |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while reporting bug"`

**Resolution**:

1. Verify project key is valid
2. Check user has "Create Issues" permission
3. Ensure Jira instance is accessible

## Usage Examples

### Example 1: Report a Simple Bug

**Input**:

```
Project key: "PROJ"
Summary: "Login button not working on mobile"
```

**Output**:

```json
{
  "New Bug": {
    "key": "PROJ-789",
    "summary": "Login button not working on mobile",
    "status": "To Do",
    "issueType": "Bug",
    "priority": "Medium"
  }
}
```

## Business Rules

1. **Issue Type**: Always creates issues with type "Bug"
2. **Description**: Automatically set to summary value
3. **Reporter**: Set to authenticated user
4. **Default Status**: Created with project's default status for bugs

## Limitations

1. **Fixed Issue Type**: Can only create Bug type issues
2. **No Description**: Cannot provide custom description (uses summary)
3. **No Custom Fields**: Cannot set custom fields
4. **No Attachments**: Cannot attach files during creation

## Best Practices

### 1. Use Descriptive Summaries

Write clear summaries that describe the bug at a glance.

**Good**: "Login API returns 500 error when password contains special characters"
**Bad**: "Login broken"

### 2. For Complex Bugs, Use "Create an Issue"

For bugs requiring detailed descriptions or attachments, use [Create an Issue](/pages/CreateAnIssue.md) instead.

## Common Use Cases

### 1. Quick Bug Reporting

```
Scenario: User discovers a bug and wants to report it quickly
Action: Use Report a Bug with project key and summary
Result: Bug created immediately for triage
```

## Related Catalog Requests

- [Create an Issue](/pages/CreateAnIssue.md) - More flexible issue creation
- [Attach File to Ticket](/pages/AttachFileToTicket.md) - Add attachments after bug creation

## Technical Implementation

### Helper Class

- **Class**: OrganizationArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.createIssue()

## See Also

- [Create an Issue](/pages/CreateAnIssue.md)
- [Extension Configuration](/pages/ExtensionConfiguration.md)

