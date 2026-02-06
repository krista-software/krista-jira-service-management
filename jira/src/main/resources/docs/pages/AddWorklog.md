# Add Worklog

## Overview

Adds a worklog entry to a Jira issue to track time spent working on it.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name  | Type | Required | Description                                | Example                     |
|-----------------|------|----------|--------------------------------------------|-----------------------------|
| Issue Key       | Text | Yes      | The key of the issue                       | `"PROJ-123"`                |
| Start Date time | Date | Yes      | When the work started (epoch milliseconds) | `1705320000000`             |
| Time Spent      | Text | Yes      | Time spent in Jira format                  | `"2h 30m"`, `"1d"`, `"45m"` |

## Output Parameters

| Parameter Name | Type   | Description                                          |
|----------------|--------|------------------------------------------------------|
| Is successful  | Switch | Boolean indicating if worklog was added successfully |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while adding worklog"`

**Resolution**:

1. Verify issue key is valid
2. Check time format is correct
3. Ensure start date is valid
4. Verify user has work logging permission

## Usage Examples

### Example 1: Log 2 Hours of Work

**Input**:

```
Issue Key: "PROJ-123"
Start Date time: 1705320000000
Time Spent: "2h"
```

**Output**:

```json
{
  "Is successful": true
}
```

## Business Rules

1. **Time Format**: Supports Jira time format (e.g., "1d", "2h", "30m", "1d 2h 30m")
2. **Author**: Worklog author is the authenticated user
3. **Time Tracking**: Updates issue's time tracking fields

## Time Format Reference

| Format   | Description    | Example                    |
|----------|----------------|----------------------------|
| `m`      | Minutes        | `"30m"` = 30 minutes       |
| `h`      | Hours          | `"2h"` = 2 hours           |
| `d`      | Days           | `"1d"` = 1 day (8 hours)   |
| Combined | Multiple units | `"1d 2h 30m"` = 10.5 hours |

## Best Practices

### 1. Log Work Regularly

Log work daily or as it's completed for accurate tracking.

### 2. Use Accurate Start Times

Provide accurate start times for better time tracking reports.

## Common Use Cases

### 1. Daily Time Logging

```
Scenario: Developer logs daily work
Action: Add worklog with hours spent
Result: Time tracked for reporting
```

## Related Catalog Requests

- [Get Issue By Key](/pages/GetIssueByKey.md) - Get issue details including time tracking
- [Update an Issue](/pages/UpdateAnIssue.md) - Update issue fields

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.addWorklog()

## See Also

- [Jira Worklog API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issue-worklogs/)

