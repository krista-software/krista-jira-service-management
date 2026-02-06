# Get My Issues by Given Statuses

## Overview

Retrieves issues assigned to the current authenticated user filtered by one or more status values.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type     | Required | Description                       | Example                    |
|----------------|----------|----------|-----------------------------------|----------------------------|
| Status Names   | [ Text ] | Yes      | List of status names to filter by | `["To Do", "In Progress"]` |

## Output Parameters

| Parameter Name | Type              | Description                                                |
|----------------|-------------------|------------------------------------------------------------|
| Issues         | [ Entity(Issue) ] | List of current user's issues matching the status criteria |

## Error Handling

### Authorization Errors

**Cause**: Authentication required

**Resolution**: Ensure user is authenticated

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while getting my issues by given statuses"`

**Resolution**:

1. Verify status names are valid
2. Check authentication is working
3. Ensure user has access to view issues

## Usage Examples

### Example 1: Get My Active Work

**Input**:

```
Status Names: ["To Do", "In Progress"]
```

**Output**:

```json
{
  "Issues": [
    {
      "key": "PROJ-123",
      "summary": "Implement login feature",
      "status": "In Progress",
      "priority": "High"
    },
    {
      "key": "PROJ-456",
      "summary": "Update documentation",
      "status": "To Do",
      "priority": "Medium"
    }
  ]
}
```

### Example 2: Get My Completed Work

**Input**:

```
Status Names: ["Done", "Closed"]
```

**Output**:

```json
{
  "Issues": [
    {
      "key": "PROJ-789",
      "summary": "Fix database bug",
      "status": "Done",
      "priority": "High"
    }
  ]
}
```

## Business Rules

1. **Current User**: Only returns issues assigned to authenticated user
2. **Multiple Statuses**: Matches issues with ANY of the specified statuses (OR logic)
3. **All Projects**: Searches across all projects user has access to
4. **Sorted**: Results sorted by priority DESC, due date DESC
5. **Limit**: Maximum 30 issues returned

## Limitations

1. **Result Limit**: Maximum 30 issues returned
2. **Current User Only**: Cannot query other users' issues
3. **No Pagination**: Cannot retrieve results beyond first 30
4. **Status Filter Only**: Cannot filter by other criteria

## Best Practices

### 1. Use for Personal Dashboards

Ideal for showing user their current work items.

### 2. Group Related Statuses

Group related statuses together (e.g., all "in progress" statuses).

### 3. For Other Users

Use [Get User Issues](/pages/GetUserIssues.md) to query other users' issues.

## Common Use Cases

### 1. My Active Work Dashboard

```
Scenario: Show user their current work
Action: Get issues with statuses ["To Do", "In Progress", "In Review"]
Result: Display user's active tasks
```

### 2. My Completed Work

```
Scenario: Show user's completed work for reporting
Action: Get issues with status ["Done"]
Result: List of completed items
```

## Related Catalog Requests

- [Get User Issues](/pages/GetUserIssues.md) - Get all issues for a specific user
- [Get Issues](/pages/GetIssues.md) - Query issues with multiple filters
- [Fetch by JQL Query](/pages/FetchByJQLQuery.md) - Advanced querying

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.searchByJQL()

### JQL Query

Executes: `assignee = currentUser() AND status IN ({statusNames}) ORDER BY priority DESC, duedate DESC`

## See Also

- [Get User Issues](/pages/GetUserIssues.md)
- [Get Issues](/pages/GetIssues.md)
- [Fetch by JQL Query](/pages/FetchByJQLQuery.md)

