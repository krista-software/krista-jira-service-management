# Assign a Task to User

## Overview

Assigns a Jira issue to a specific user by their account ID.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name            | Type | Required | Description                                       | Example                |
|---------------------------|------|----------|---------------------------------------------------|------------------------|
| To assign user account id | Text | Yes      | The account ID of the user to assign the issue to | `"5d1234567890abcdef"` |
| Issue key                 | Text | Yes      | The key of the issue to assign                    | `"PROJ-123"`           |

## Output Parameters

| Parameter Name        | Type          | Description                         |
|-----------------------|---------------|-------------------------------------|
| Updated issue details | Entity(Issue) | The updated issue with new assignee |

## Error Handling

### Authorization Errors

**Cause**: Authentication required or insufficient permissions

**Resolution**: Ensure user is authenticated and has permission to assign issues

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while assigning task to user"`

**Resolution**:

1. Verify issue key is valid
2. Check account ID is valid
3. Ensure user has permission to be assigned issues
4. Verify assignee has access to the project

## Usage Examples

### Example 1: Assign Issue to Developer

**Input**:

```
To assign user account id: "5d1234567890abcdef"
Issue key: "PROJ-123"
```

**Output**:

```json
{
  "Updated issue details": {
    "key": "PROJ-123",
    "summary": "Implement login feature",
    "assignee": {
      "accountId": "5d1234567890abcdef",
      "displayName": "John Doe"
    },
    "status": "To Do"
  }
}
```

### Example 2: Reassign Bug

**Input**:

```
To assign user account id: "5d9876543210fedcba"
Issue key: "BUG-456"
```

**Output**:

```json
{
  "Updated issue details": {
    "key": "BUG-456",
    "summary": "Fix database connection",
    "assignee": {
      "accountId": "5d9876543210fedcba",
      "displayName": "Jane Smith"
    },
    "issueType": "Bug"
  }
}
```

## Business Rules

1. **User Access**: Assignee must have access to the project
2. **Permissions**: User must have "Assign Issues" permission
3. **Overwrite**: Replaces current assignee if issue already assigned
4. **Notification**: Assignee receives notification of assignment

## Limitations

1. **Single Assignee**: Can only assign to one user at a time
2. **No Unassign**: Cannot unassign (set to null) - use Update an Issue instead
3. **Account ID Required**: Must use account ID, not email or username

## Best Practices

### 1. Verify Account ID

Use [Get Users Account Id](/pages/GetUsersAccountId.md) to look up account ID by email.

### 2. Check User Access

Ensure user has access to the project before assigning.

### 3. Add Comment

Use [Add a Comment](/pages/AddAComment.md) to explain why issue was reassigned.

### 4. Notify User

Consider adding a comment to notify the assignee of important context.

## Common Use Cases

### 1. Assign New Work

```
Scenario: Assign new issue to team member
Action: Assign issue to user's account ID
Result: User notified and issue appears in their queue
```

### 2. Reassign for Expertise

```
Scenario: Issue requires different expertise
Action: Reassign to specialist
Result: Issue moved to appropriate person
```

### 3. Load Balancing

```
Scenario: Distribute work evenly across team
Action: Assign issues to team members
Result: Balanced workload
```

## Related Catalog Requests

- [Get Users Account Id](/pages/GetUsersAccountId.md) - Look up account ID by email
- [Get User Issues](/pages/GetUserIssues.md) - View user's assigned issues
- [Update an Issue](/pages/UpdateAnIssue.md) - Update multiple fields including assignee

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.assignATaskToUser()

## See Also

- [Get Users Account Id](/pages/GetUsersAccountId.md)
- [Get User Issues](/pages/GetUserIssues.md)
- [Update an Issue](/pages/UpdateAnIssue.md)
- [Jira Issues API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/#api-rest-api-3-issue-issueidorkey-put)

