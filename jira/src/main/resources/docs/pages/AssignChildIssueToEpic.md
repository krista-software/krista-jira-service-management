# Assign Child Issue to Epic

## Overview

Assigns a child issue to a parent epic in Jira. This creates a parent-child relationship between the issues.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name  | Type | Required | Description                | Example      |
|-----------------|------|----------|----------------------------|--------------|
| Child Issue Key | Text | Yes      | The key of the child issue | `"PROJ-123"` |
| Parent Epic Key | Text | Yes      | The key of the parent epic | `"PROJ-100"` |

## Output Parameters

| Parameter Name | Type   | Description                                     |
|----------------|--------|-------------------------------------------------|
| Is successful  | Switch | Boolean indicating if assignment was successful |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while assigning child issue to epic"`

**Resolution**:

1. Verify both issue keys are valid
2. Check parent issue is an Epic
3. Ensure user has permission to link issues
4. Verify issues are in compatible projects

## Usage Examples

### Example 1: Assign Story to Epic

**Input**:

```
Child Issue Key: "PROJ-123"
Parent Epic Key: "PROJ-100"
```

**Output**:

```json
{
  "Is successful": true
}
```

### Example 2: Assign Task to Epic

**Input**:

```
Child Issue Key: "PROJ-456"
Parent Epic Key: "PROJ-100"
```

**Output**:

```json
{
  "Is successful": true
}
```

## Business Rules

1. **Parent Must Be Epic**: Parent issue must be of type "Epic"
2. **Hierarchy**: Creates parent-child relationship
3. **Single Parent**: Child can only have one parent epic
4. **Overwrite**: Replaces existing parent if child already has one

## Limitations

1. **Epic Only**: Parent must be an Epic type issue
2. **No Validation**: No validation of issue types before execution
3. **Single Operation**: Can only assign one child at a time

## Best Practices

### 1. Verify Epic Type

Ensure parent issue is actually an Epic before assigning.

### 2. Use for Story/Task Organization

Use to organize stories and tasks under epics.

### 3. Check Existing Parent

Check if child already has a parent to avoid unintended changes.

## Common Use Cases

### 1. Epic Organization

```
Scenario: Organize user stories under epic
Action: Assign story to epic
Result: Story appears under epic in hierarchy
```

### 2. Sprint Planning

```
Scenario: Group sprint items under epic
Action: Assign tasks to epic
Result: Epic shows all related work
```

## Related Catalog Requests

- [Assign Issue to Epic](/pages/AssignIssueToEpic.md) - Alternative method for epic assignment
- [Get Issue By Key](/pages/GetIssueByKey.md) - Get issue details
- [Create an Issue](/pages/CreateAnIssue.md) - Create issues

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.assignChildIssueToEpic()

## See Also

- [Assign Issue to Epic](/pages/AssignIssueToEpic.md)
- [Jira Issues API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/)

