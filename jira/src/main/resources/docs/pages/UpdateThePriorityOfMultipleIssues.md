# Update the Priority of Multiple Issues

## Overview

Updates the priority of multiple Jira issues in a single operation.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type     | Required | Description                  | Example                       |
|----------------|----------|----------|------------------------------|-------------------------------|
| Issue Keys     | [ Text ] | Yes      | List of issue keys to update | `["PROJ-123", "PROJ-456"]`    |
| New Priority   | Text     | Yes      | The new priority name        | `"High"`, `"Medium"`, `"Low"` |

## Output Parameters

| Parameter Name | Type              | Description                              |
|----------------|-------------------|------------------------------------------|
| List of Issues | [ Entity(Issue) ] | List of updated issues with new priority |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while updating the priority of multiple issues"`

**Resolution**:

1. Verify all issue keys are valid
2. Check priority name is valid
3. Ensure user has edit permission for all issues

## Usage Examples

### Example 1: Escalate Multiple Issues

**Input**:

```
Issue Keys: ["PROJ-123", "PROJ-456", "PROJ-789"]
New Priority: "High"
```

**Output**:

```json
{
  "List of Issues": [
    {
      "key": "PROJ-123",
      "priority": "High"
    },
    {
      "key": "PROJ-456",
      "priority": "High"
    },
    {
      "key": "PROJ-789",
      "priority": "High"
    }
  ]
}
```

## Business Rules

1. **Same Priority**: All issues updated to the same priority
2. **Partial Success**: If some issues fail, others may still succeed
3. **Permissions**: User must have edit permission for each issue

## Common Use Cases

### 1. Sprint Planning

```
Scenario: Prioritize all sprint items
Action: Update priority of all sprint issues to "High"
Result: Sprint items prioritized
```

## Related Catalog Requests

- [Update Priority of Issue](/pages/UpdatePriorityOfIssue.md) - Update single issue priority
- [Update an Issue](/pages/UpdateAnIssue.md) - Update multiple fields

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.updateThePriorityOfMultipleIssues()

## See Also

- [Update Priority of Issue](/pages/UpdatePriorityOfIssue.md)
- [Jira Issues API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/)

