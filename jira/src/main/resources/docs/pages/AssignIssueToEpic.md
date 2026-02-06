# Assign Issue to Epic

## Overview

Assigns an issue to an epic in Jira. This creates a relationship between the issue and the epic.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type | Required | Description                    | Example      |
|----------------|------|----------|--------------------------------|--------------|
| Issue Key      | Text | Yes      | The key of the issue to assign | `"PROJ-123"` |
| Epic Key       | Text | Yes      | The key of the epic            | `"PROJ-100"` |

## Output Parameters

| Parameter Name | Type   | Description                                     |
|----------------|--------|-------------------------------------------------|
| Is successful  | Switch | Boolean indicating if assignment was successful |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while assigning issue to epic"`

**Resolution**:

1. Verify both issue keys are valid
2. Check epic issue is of type "Epic"
3. Ensure user has permission to link issues
4. Verify issues are in compatible projects

## Usage Examples

### Example 1: Assign Story to Epic

**Input**:

```
Issue Key: "PROJ-123"
Epic Key: "PROJ-100"
```

**Output**:

```json
{
  "Is successful": true
}
```

### Example 2: Assign Bug to Epic

**Input**:

```
Issue Key: "BUG-456"
Epic Key: "PROJ-100"
```

**Output**:

```json
{
  "Is successful": true
}
```

## Business Rules

1. **Epic Type**: Epic issue must be of type "Epic"
2. **Relationship**: Creates epic-issue relationship
3. **Single Epic**: Issue can only be assigned to one epic
4. **Overwrite**: Replaces existing epic if issue already assigned

## Limitations

1. **Epic Only**: Target must be an Epic type issue
2. **No Validation**: No validation of issue types before execution
3. **Single Operation**: Can only assign one issue at a time

## Best Practices

### 1. Verify Epic Type

Ensure target issue is actually an Epic before assigning.

### 2. Use for Work Organization

Use to organize related work items under epics.

### 3. Check Existing Assignment

Check if issue already assigned to avoid unintended changes.

## Common Use Cases

### 1. Feature Organization

```
Scenario: Group feature work under epic
Action: Assign stories and tasks to epic
Result: Epic shows all related work
```

### 2. Release Planning

```
Scenario: Organize release work
Action: Assign issues to release epic
Result: Track all release work under epic
```

## Related Catalog Requests

- [Assign Child Issue to Epic](/pages/AssignChildIssueToEpic.md) - Alternative method for epic assignment
- [Get Issue By Key](/pages/GetIssueByKey.md) - Get issue details
- [Create an Issue](/pages/CreateAnIssue.md) - Create issues

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.assignIssueToEpic()

## See Also

- [Assign Child Issue to Epic](/pages/AssignChildIssueToEpic.md)
- [Jira Issues API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/)

