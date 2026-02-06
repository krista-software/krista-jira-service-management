# Get Issue By Key

## Overview

Retrieves detailed information about a specific Jira issue by its key.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type | Required | Description                      | Example      |
|----------------|------|----------|----------------------------------|--------------|
| Issue Key      | Text | Yes      | The key of the issue to retrieve | `"PROJ-123"` |

## Output Parameters

| Parameter Name | Type          | Description            |
|----------------|---------------|------------------------|
| Issue          | Entity(Issue) | Complete issue details |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while getting issue by key"`

**Resolution**:

1. Verify issue key is valid
2. Check user has permission to view the issue
3. Ensure issue exists

## Usage Examples

### Example 1: Get Issue Details

**Input**:

```
Issue Key: "PROJ-123"
```

**Output**:

```json
{
  "Issue": {
    "key": "PROJ-123",
    "summary": "Implement login feature",
    "description": "Add user authentication with OAuth 2.0",
    "status": "In Progress",
    "issueType": "Story",
    "priority": "High",
    "assignee": {
      "accountId": "5d1234567890abcdef",
      "displayName": "John Doe"
    },
    "created": "2024-01-10T09:00:00.000Z",
    "projectKey": "PROJ"
  }
}
```

## Business Rules

1. **Single Issue**: Returns details for one issue only
2. **Complete Data**: Returns all available issue fields
3. **Permissions**: Only returns data user has permission to view

## Best Practices

### 1. Use for Detailed Views

Use this request when you need complete issue information.

### 2. For Multiple Issues

Use [Get Issues](/pages/GetIssues.md) or [Fetch by JQL Query](/pages/FetchByJQLQuery.md) for multiple issues.

## Common Use Cases

### 1. Issue Detail Page

```
Scenario: Display full issue details to user
Action: Get issue by key
Result: Show complete issue information
```

## Related Catalog Requests

- [Get Issues](/pages/GetIssues.md) - Query multiple issues
- [Fetch by JQL Query](/pages/FetchByJQLQuery.md) - Advanced querying
- [Update an Issue](/pages/UpdateAnIssue.md) - Update issue fields

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.getIssueByKey()

## See Also

- [Jira Issues API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/#api-rest-api-3-issue-issueidorkey-get)

