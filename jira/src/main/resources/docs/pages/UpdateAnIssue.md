# Update an Issue

## Overview

Updates multiple fields of a Jira issue including status, priority, assignee, summary, and description.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name          | Type | Required | Description                    | Example                 |
|-------------------------|------|----------|--------------------------------|-------------------------|
| Issue Key               | Text | Yes      | The key of the issue to update | `"PROJ-123"`            |
| New Status              | Text | No       | New status name                | `"In Progress"`         |
| New priority            | Text | No       | New priority name              | `"High"`                |
| New assignee account id | Text | No       | Account ID of new assignee     | `"5d1234567890abcdef"`  |
| New summary             | Text | No       | New summary text               | `"Updated summary"`     |
| New description         | Text | No       | New description text           | `"Updated description"` |

## Output Parameters

| Parameter Name | Type          | Description                                |
|----------------|---------------|--------------------------------------------|
| Updated Issue  | Entity(Issue) | The updated issue with all changes applied |

## Error Handling

### Authorization Errors

**Cause**: Authentication required or insufficient permissions

**Resolution**: Ensure user is authenticated and has permission to edit the issue

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while updating issue"`

**Resolution**:

1. Verify issue key is valid
2. Check all field values are valid
3. Ensure status transition is allowed by workflow
4. Verify user has edit permission

## Usage Examples

### Example 1: Update Status and Priority

**Input**:

```
Issue Key: "PROJ-123"
New Status: "In Progress"
New priority: "High"
New assignee account id: (empty)
New summary: (empty)
New description: (empty)
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

### Example 2: Reassign and Update Summary

**Input**:

```
Issue Key: "PROJ-456"
New Status: (empty)
New priority: (empty)
New assignee account id: "5d9876543210fedcba"
New summary: "Fix critical database connection issue"
New description: "Database connections timing out under load"
```

**Output**:

```json
{
  "Updated Issue": {
    "key": "PROJ-456",
    "summary": "Fix critical database connection issue",
    "description": "Database connections timing out under load",
    "assignee": {
      "accountId": "5d9876543210fedcba",
      "displayName": "Jane Smith"
    }
  }
}
```

### Example 3: Update All Fields

**Input**:

```
Issue Key: "BUG-789"
New Status: "In Review"
New priority: "Highest"
New assignee account id: "5d1234567890abcdef"
New summary: "Critical security vulnerability in authentication"
New description: "SQL injection vulnerability discovered in login form"
```

**Output**:

```json
{
  "Updated Issue": {
    "key": "BUG-789",
    "summary": "Critical security vulnerability in authentication",
    "description": "SQL injection vulnerability discovered in login form",
    "status": "In Review",
    "priority": "Highest",
    "assignee": {
      "accountId": "5d1234567890abcdef"
    }
  }
}
```

## Business Rules

1. **Partial Updates**: Only specified fields are updated
2. **Workflow Validation**: Status changes must follow workflow rules
3. **Permissions**: User must have edit permission for each field
4. **Null Values**: Empty parameters are ignored (field not updated)

## Limitations

1. **No Custom Fields**: Cannot update custom fields
2. **No Labels**: Cannot update labels
3. **No Components**: Cannot update components
4. **Workflow Constraints**: Status changes must follow workflow

## Best Practices

### 1. Update Only Necessary Fields

Only specify fields that need to be changed.

### 2. Verify Status Transitions

Ensure status transition is valid before updating.

### 3. Use Account IDs

Always use account IDs for assignee, not emails or usernames.

### 4. Add Comments

Use [Add a Comment](/pages/AddAComment.md) to document why changes were made.

## Common Use Cases

### 1. Bulk Field Update

```
Scenario: Update multiple fields at once
Action: Specify all fields to update
Result: Issue updated with all changes
```

### 2. Escalation

```
Scenario: Escalate issue to high priority and reassign
Action: Update priority to "Highest" and assign to senior developer
Result: Issue escalated and reassigned
```

## Related Catalog Requests

- [Update Status of Issue](/pages/UpdateStatusOfIssue.md) - Update status only
- [Update Priority of Issue](/pages/UpdatePriorityOfIssue.md) - Update priority only
- [Assign a Task to User](/pages/AssignATaskToUser.md) - Assign issue only
- [Add a Comment](/pages/AddAComment.md) - Add comment to issue

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.updateAnIssue()

## See Also

- [Update Status of Issue](/pages/UpdateStatusOfIssue.md)
- [Update Priority of Issue](/pages/UpdatePriorityOfIssue.md)
- [Jira Issues API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/#api-rest-api-3-issue-issueidorkey-put)

