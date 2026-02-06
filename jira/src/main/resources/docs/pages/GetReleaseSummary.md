# Get Release Summary

## Overview

Retrieves a comprehensive summary of a release including counts and lists of user stories, epics, tasks, and bugs
filtered by various criteria.

## Request Details

- **Area**: Release Management
- **Type**: QUERY_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type   | Required | Description                            | Example                   |
|----------------|--------|----------|----------------------------------------|---------------------------|
| Status         | Text   | No       | Filter by status                       | `"In Progress"`, `"Done"` |
| Assignee       | Text   | No       | Filter by assignee account ID          | `"5d1234567890abcdef"`    |
| Priority       | Text   | No       | Filter by priority                     | `"High"`, `"Medium"`      |
| Issue type     | Text   | No       | Filter by issue type                   | `"Story"`, `"Bug"`        |
| Page Size      | Number | No       | Number of items per page (default: 20) | `20`                      |
| Page Number    | Number | No       | Page number (default: 1)               | `1`                       |
| Fix version    | Text   | Yes      | The fix version/release name           | `"2.0"`, `"Sprint 1"`     |

## Output Parameters

| Parameter Name     | Type              | Description                            |
|--------------------|-------------------|----------------------------------------|
| Total user stories | Number            | Total count of user stories in release |
| Open user stories  | Number            | Count of open user stories             |
| Total epics        | Number            | Total count of epics in release        |
| Total Tasks        | Number            | Total count of tasks in release        |
| Open Tasks         | Number            | Count of open tasks                    |
| Total Bugs         | Number            | Total count of bugs in release         |
| Open Bugs          | Number            | Count of open bugs                     |
| Release day        | Date              | Release date (if set)                  |
| Days remaining     | Number            | Days until release                     |
| User stories       | [ Entity(Issue) ] | List of user story issues              |
| Epics              | [ Entity(Issue) ] | List of epic issues                    |
| Bugs               | [ Entity(Issue) ] | List of bug issues                     |
| Tasks              | [ Entity(Issue) ] | List of task issues                    |

## Error Handling

### Logic Errors (LOGIC_ERROR)

**Error Message**: `"Error occurred while getting release summary."`

**Resolution**:

1. Verify fix version name is correct
2. Check fix version exists in Jira
3. Ensure user has permission to view issues
4. Verify filters are valid

## Usage Examples

### Example 1: Get Release Summary

**Input**:

```
Status: (empty)
Assignee: (empty)
Priority: (empty)
Issue type: (empty)
Page Size: 20
Page Number: 1
Fix version: "2.0"
```

**Output**:

```json
{
  "Total user stories": 15,
  "Open user stories": 8,
  "Total epics": 3,
  "Total Tasks": 25,
  "Open Tasks": 12,
  "Total Bugs": 5,
  "Open Bugs": 2,
  "Release day": 1710374400000,
  "Days remaining": 45,
  "User stories": [
    {
      "key": "PROJ-123",
      "summary": "User login feature",
      "status": "In Progress",
      "priority": "High"
    }
  ],
  "Epics": [
    ...
  ],
  "Bugs": [
    ...
  ],
  "Tasks": [
    ...
  ]
}
```

### Example 2: Filter by Status

**Input**:

```
Status: "In Progress"
Fix version: "Sprint 1"
Page Size: 10
Page Number: 1
```

**Output**:

```json
{
  "Total user stories": 5,
  "Open user stories": 5,
  "Total Tasks": 8,
  "Open Tasks": 8,
  "User stories": [
    ...
  ],
  "Tasks": [
    ...
  ]
}
```

## Business Rules

1. **Fix Version Required**: Must specify fix version
2. **Pagination**: Supports pagination for large result sets
3. **Filtering**: All filters are optional except fix version
4. **Counts**: Provides both total and open counts
5. **Release Date**: Calculates days remaining if release date set

## Limitations

1. **Single Version**: Can only query one fix version at a time
2. **Pagination**: Results are paginated
3. **No Custom Fields**: Cannot filter by custom fields

## Best Practices

### 1. Use Pagination

Use pagination for releases with many issues.

### 2. Filter Appropriately

Use filters to narrow results to relevant issues.

### 3. Monitor Days Remaining

Use days remaining to track release progress.

## Common Use Cases

### 1. Release Dashboard

```
Scenario: Display release progress dashboard
Action: Get release summary for version
Result: Show counts and progress metrics
```

### 2. Sprint Review

```
Scenario: Review sprint completion
Action: Get release summary for sprint
Result: Show completed vs remaining work
```

### 3. Release Planning

```
Scenario: Plan upcoming release
Action: Get release summary with filters
Result: Understand scope and progress
```

## Related Catalog Requests

- [Get Issues](/pages/GetIssues.md) - Query issues with filters
- [Fetch by JQL Query](/pages/FetchByJQLQuery.md) - Advanced querying

## Technical Implementation

### Helper Class

- **Class**: ReleaseManagementArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: ReleaseDao.getGetReleaseSummaryResponse()

## See Also

- [Get Issues](/pages/GetIssues.md)
- [Fetch by JQL Query](/pages/FetchByJQLQuery.md)
- [Jira Versions API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-project-versions/)

