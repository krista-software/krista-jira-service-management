# Get User Issues

## Overview

Retrieves all issues assigned to a specific user by their account ID.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name  | Type | Required | Description                     | Example                |
|-----------------|------|----------|---------------------------------|------------------------|
| User Account Id | Text | Yes      | The Jira account ID of the user | `"5d1234567890abcdef"` |

## Output Parameters

| Parameter Name | Type              | Description                         |
|----------------|-------------------|-------------------------------------|
| User Issues    | [ Entity(Issue) ] | List of issues assigned to the user |

## Error Handling

### Authorization Errors

**Cause**: Authentication required or insufficient permissions

**Resolution**: Ensure user is authenticated and has permission to view issues

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while getting user issues"`

**Resolution**:

1. Verify account ID is valid
2. Check user exists in Jira
3. Ensure you have permission to view user's issues

## Usage Examples

### Example 1: Get User's Issues

**Input**:

```
User Account Id: "5d1234567890abcdef"
```

**Output**:

```json
{
  "User Issues": [
    {
      "key": "PROJ-123",
      "summary": "Implement login feature",
      "status": "In Progress",
      "priority": "High",
      "assignee": "5d1234567890abcdef"
    },
    {
      "key": "PROJ-456",
      "summary": "Fix database connection",
      "status": "To Do",
      "priority": "Medium",
      "assignee": "5d1234567890abcdef"
    }
  ]
}
```

## Business Rules

1. **Assigned Issues Only**: Returns only issues assigned to the specified user
2. **All Statuses**: Includes issues in all statuses (open, in progress, done, etc.)
3. **All Projects**: Searches across all projects user has access to
4. **Sorted**: Results sorted by priority DESC, due date DESC
5. **Limit**: Maximum 30 issues returned

## Limitations

1. **Result Limit**: Maximum 30 issues returned
2. **No Filtering**: Cannot filter by status, project, or other criteria
3. **No Pagination**: Cannot retrieve results beyond first 30

## Best Practices

### 1. Use Account ID

Always use the account ID, not username or email. Use [Get Users Account Id](/pages/GetUsersAccountId.md) to look up the
ID.

### 2. For Advanced Filtering

Use [Fetch by JQL Query](/pages/FetchByJQLQuery.md) for more complex queries with filtering.

### 3. Cache Results

Cache results if querying frequently to reduce API calls.

## Common Use Cases

### 1. User Dashboard

```
Scenario: Show user their assigned work
Action: Get user issues by account ID
Result: Display user's task list
```

### 2. Workload Analysis

```
Scenario: Check team member's workload
Action: Get user issues for each team member
Result: Understand team capacity
```

### 3. Status Report

```
Scenario: Generate user's weekly status report
Action: Get user issues and filter by status
Result: Report of user's active work
```

## Related Catalog Requests

- [Get User Issue by Issue Type](/pages/GetUserIssueByIssueType.md) - Filter by issue type
- [Get My Issues by Given Statuses](/pages/GetMyIssuesByGivenStatuses.md) - Get current user's issues by status
- [Get Users Account Id](/pages/GetUsersAccountId.md) - Look up account ID by email
- [Fetch by JQL Query](/pages/FetchByJQLQuery.md) - Advanced querying

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Validation**: ValidationOrchestrator validates account ID
- **Service**: IssueDao.searchByJQL()

### JQL Query

Executes: `assignee = "{accountId}" ORDER BY priority DESC, duedate DESC`

## See Also

- [Get User Issue by Issue Type](/pages/GetUserIssueByIssueType.md)
- [Get My Issues by Given Statuses](/pages/GetMyIssuesByGivenStatuses.md)
- [Fetch by JQL Query](/pages/FetchByJQLQuery.md)

