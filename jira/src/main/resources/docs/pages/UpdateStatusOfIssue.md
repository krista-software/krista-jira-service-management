# Update Status of Issue

## Overview

Updates the status of a single Jira issue by transitioning it to a new status.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type | Required | Description                    | Example                   |
|----------------|------|----------|--------------------------------|---------------------------|
| Issue Key      | Text | Yes      | The key of the issue to update | `"PROJ-123"`              |
| Status Name    | Text | Yes      | The target status name         | `"In Progress"`, `"Done"` |

## Output Parameters

| Parameter Name | Type          | Description                           |
|----------------|---------------|---------------------------------------|
| Updated Issue  | Entity(Issue) | The updated issue with its new status |

## Error Handling

### Authorization Errors

**Cause**: Authentication required or insufficient permissions

**Resolution**: Ensure user is authenticated and has permission to transition the issue

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while updating status of issue"`

**Resolution**:

1. Verify issue key is valid
2. Check status name is valid for the issue
3. Ensure status transition is allowed by workflow
4. Verify user has transition permission

## Usage Examples

### Example 1: Move Issue to In Progress

**Input**:

```
Issue Key: "PROJ-123"
Status Name: "In Progress"
```

**Output**:

```json
{
  "Updated Issue": {
    "key": "PROJ-123",
    "summary": "Implement login feature",
    "status": "In Progress",
    "priority": "High"
  }
}
```

### Example 2: Close Issue

**Input**:

```
Issue Key: "BUG-456"
Status Name: "Done"
```

**Output**:

```json
{
  "Updated Issue": {
    "key": "BUG-456",
    "summary": "Fix database connection",
    "status": "Done",
    "issueType": "Bug"
  }
}
```

## Business Rules

1. **Workflow Validation**: Status transition must be allowed by workflow
2. **Permissions**: User must have transition permission
3. **Status Availability**: Status must be available in issue's workflow
4. **Transition Rules**: Must follow workflow transition rules and conditions

## Limitations

1. **No Custom Fields**: Cannot update custom fields during transition
2. **No Comments**: Cannot add transition comments
3. **Workflow Constraints**: Must follow workflow transition rules
4. **Single Issue**: Can only update one issue at a time

## Best Practices

### 1. Verify Status Name

Ensure status name matches exactly (case-sensitive).

### 2. Check Workflow

Verify the status transition is valid before updating.

### 3. For Bulk Updates

Use [Update Status of Multiple Issues](/pages/UpdateStatusOfMultipleIssues.md) for bulk operations.

### 4. Add Comments

Use [Add a Comment](/pages/AddAComment.md) to document why status was changed.

## Common Use Cases

### 1. Start Work

```
Scenario: Developer starts working on issue
Action: Update status to "In Progress"
Result: Issue marked as being worked on
```

### 2. Complete Task

```
Scenario: Work completed on issue
Action: Update status to "Done"
Result: Issue marked as complete
```

### 3. Send for Review

```
Scenario: Code ready for review
Action: Update status to "In Review"
Result: Issue moved to review queue
```

## Related Catalog Requests

- [Update Status of Multiple Issues](/pages/UpdateStatusOfMultipleIssues.md) - Bulk status updates
- [Update an Issue](/pages/UpdateAnIssue.md) - Update multiple fields
- [Add a Comment](/pages/AddAComment.md) - Add comment to issue
- [Get Issue By Key](/pages/GetIssueByKey.md) - Get issue details

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.updateStatusOfIssue()

## See Also

- [Update Status of Multiple Issues](/pages/UpdateStatusOfMultipleIssues.md)
- [Update an Issue](/pages/UpdateAnIssue.md)
- [Jira Transitions API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/#api-rest-api-3-issue-issueidorkey-transitions-post)

