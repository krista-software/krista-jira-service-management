# Update Priority of Issue

## Overview

Updates the priority of a single Jira issue.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type | Required | Description                    | Example                       |
|----------------|------|----------|--------------------------------|-------------------------------|
| Issue Key      | Text | Yes      | The key of the issue to update | `"PROJ-123"`                  |
| Priority Name  | Text | Yes      | The new priority name          | `"High"`, `"Medium"`, `"Low"` |

## Output Parameters

| Parameter Name | Type          | Description                         |
|----------------|---------------|-------------------------------------|
| Updated Issue  | Entity(Issue) | The updated issue with new priority |

## Error Handling

### Authorization Errors

**Cause**: Authentication required or insufficient permissions

**Resolution**: Ensure user is authenticated and has permission to edit the issue

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while updating priority of issue"`

**Resolution**:

1. Verify issue key is valid
2. Check priority name is valid
3. Ensure user has edit permission

## Usage Examples

### Example 1: Increase Priority

**Input**:

```
Issue Key: "PROJ-123"
Priority Name: "High"
```

**Output**:

```json
{
  "Updated Issue": {
    "key": "PROJ-123",
    "summary": "Implement login feature",
    "priority": "High",
    "status": "In Progress"
  }
}
```

### Example 2: Decrease Priority

**Input**:

```
Issue Key: "BUG-456"
Priority Name: "Low"
```

**Output**:

```json
{
  "Updated Issue": {
    "key": "BUG-456",
    "summary": "Minor UI alignment issue",
    "priority": "Low",
    "issueType": "Bug"
  }
}
```

## Business Rules

1. **Valid Priority**: Priority must exist in Jira configuration
2. **Permissions**: User must have edit permission
3. **Case Sensitive**: Priority name is case-sensitive
4. **Common Priorities**: Highest, High, Medium, Low, Lowest

## Limitations

1. **Single Issue**: Can only update one issue at a time
2. **Priority Only**: Cannot update other fields
3. **No Custom Priorities**: Must use standard Jira priorities

## Best Practices

### 1. Verify Priority Name

Ensure priority name matches exactly (case-sensitive).

### 2. For Bulk Updates

Use [Update the Priority of Multiple Issues](/pages/UpdateThePriorityOfMultipleIssues.md) for bulk operations.

### 3. Document Changes

Use [Add a Comment](/pages/AddAComment.md) to explain why priority was changed.

## Common Use Cases

### 1. Escalation

```
Scenario: Issue becomes critical
Action: Update priority to "Highest"
Result: Issue prioritized for immediate attention
```

### 2. De-escalation

```
Scenario: Issue less urgent than initially thought
Action: Update priority to "Low"
Result: Issue deprioritized
```

## Related Catalog Requests

- [Update the Priority of Multiple Issues](/pages/UpdateThePriorityOfMultipleIssues.md) - Bulk priority updates
- [Update an Issue](/pages/UpdateAnIssue.md) - Update multiple fields
- [Add a Comment](/pages/AddAComment.md) - Add comment to issue

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.updatePriorityOfIssue()

## See Also

- [Update the Priority of Multiple Issues](/pages/UpdateThePriorityOfMultipleIssues.md)
- [Update an Issue](/pages/UpdateAnIssue.md)
- [Jira Issues API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/)

