# Get User Issue by Issue Type

## Overview

Retrieves issues assigned to a specific user filtered by issue type.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name  | Type | Required | Description                     | Example                      |
|-----------------|------|----------|---------------------------------|------------------------------|
| User account Id | Text | Yes      | The Jira account ID of the user | `"5d1234567890abcdef"`       |
| Issue Type      | Text | Yes      | The issue type to filter by     | `"Bug"`, `"Task"`, `"Story"` |

## Output Parameters

| Parameter Name | Type              | Description                          |
|----------------|-------------------|--------------------------------------|
| Issues         | [ Entity(Issue) ] | List of issues matching the criteria |

## Error Handling

### Authorization Errors

**Cause**: Authentication required or insufficient permissions

**Resolution**: Ensure user is authenticated and has permission to view issues

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while getting user issue by issue type"`

**Resolution**:

1. Verify account ID is valid
2. Check issue type name is correct
3. Ensure user has access to view issues

## Usage Examples

### Example 1: Get User's Bugs

**Input**:

```
User account Id: "5d1234567890abcdef"
Issue Type: "Bug"
```

**Output**:

```json
{
  "Issues": [
    {
      "key": "PROJ-123",
      "summary": "Login button not working",
      "status": "In Progress",
      "issueType": "Bug",
      "priority": "High",
      "assignee": "5d1234567890abcdef"
    },
    {
      "key": "PROJ-456",
      "summary": "Database connection timeout",
      "status": "To Do",
      "issueType": "Bug",
      "priority": "Medium",
      "assignee": "5d1234567890abcdef"
    }
  ]
}
```

### Example 2: Get User's Tasks

**Input**:

```
User account Id: "5d9876543210fedcba"
Issue Type: "Task"
```

**Output**:

```json
{
  "Issues": [
    {
      "key": "PROJ-789",
      "summary": "Update documentation",
      "status": "To Do",
      "issueType": "Task",
      "priority": "Low"
    }
  ]
}
```

## Business Rules

1. **User Filter**: Only returns issues assigned to specified user
2. **Type Filter**: Only returns issues of specified type
3. **All Statuses**: Includes issues in all statuses
4. **Sorted**: Results sorted by priority DESC, due date DESC
5. **Limit**: Maximum 30 issues returned

## Limitations

1. **Result Limit**: Maximum 30 issues returned
2. **Single Type**: Can only filter by one issue type at a time
3. **No Pagination**: Cannot retrieve results beyond first 30
4. **No Status Filter**: Cannot filter by status

## Best Practices

### 1. Use Account ID

Always use account ID. Use [Get Users Account Id](/pages/GetUsersAccountId.md) to look up by email.

### 2. Verify Issue Type Name

Ensure issue type name matches exactly (case-sensitive).

### 3. For Multiple Types

Use [Fetch by JQL Query](/pages/FetchByJQLQuery.md) for multiple issue types.

## Common Use Cases

### 1. Bug Assignment Report

```
Scenario: Show all bugs assigned to a developer
Action: Get user issues filtered by "Bug" type
Result: List of developer's bugs
```

### 2. Task Tracking

```
Scenario: Track user's tasks
Action: Get user issues filtered by "Task" type
Result: List of user's tasks
```

## Related Catalog Requests

- [Get User Issues](/pages/GetUserIssues.md) - Get all issues for a user
- [Get Issues](/pages/GetIssues.md) - Query with multiple filters
- [Get Users Account Id](/pages/GetUsersAccountId.md) - Look up account ID
- [Fetch by JQL Query](/pages/FetchByJQLQuery.md) - Advanced querying

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.searchByJQL()

### JQL Query

Executes: `issuetype = "{issueType}" AND assignee in ({userAccountId}) ORDER BY priority DESC, duedate DESC`

## See Also

- [Get User Issues](/pages/GetUserIssues.md)
- [Get Issues](/pages/GetIssues.md)
- [Fetch by JQL Query](/pages/FetchByJQLQuery.md)