# Update Status of Multiple Issues

## Overview

Updates the status of multiple Jira issues in a single operation. This is useful for bulk status transitions.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type     | Required | Description                  | Example                    |
|----------------|----------|----------|------------------------------|----------------------------|
| Status Name    | Text     | Yes      | The target status name       | `"In Progress"`, `"Done"`  |
| Issue Key      | [ Text ] | Yes      | List of issue keys to update | `["PROJ-123", "PROJ-456"]` |

## Output Parameters

| Parameter Name | Type              | Description                                  |
|----------------|-------------------|----------------------------------------------|
| Updated issues | [ Entity(Issue) ] | List of updated issues with their new status |

## Error Handling

### Authorization Errors

**Cause**: Authentication required or insufficient permissions

**Resolution**: Ensure user is authenticated and has permission to transition issues

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while updating status of multiple issues"`

**Resolution**:

1. Verify all issue keys are valid
2. Check status name is valid for all issues
3. Ensure status transition is allowed by workflow

## Usage Examples

### Example 1: Move Multiple Issues to In Progress

**Input**:

```
Status Name: "In Progress"
Issue Key: ["PROJ-123", "PROJ-456", "PROJ-789"]
```

**Output**:

```json
{
  "Updated issues": [
    {
      "key": "PROJ-123",
      "status": "In Progress",
      "summary": "Implement login feature"
    },
    {
      "key": "PROJ-456",
      "status": "In Progress",
      "summary": "Fix database connection"
    },
    {
      "key": "PROJ-789",
      "status": "In Progress",
      "summary": "Update documentation"
    }
  ]
}
```

### Example 2: Close Multiple Bugs

**Input**:

```
Status Name: "Done"
Issue Key: ["BUG-101", "BUG-102"]
```

**Output**:

```json
{
  "Updated issues": [
    {
      "key": "BUG-101",
      "status": "Done"
    },
    {
      "key": "BUG-102",
      "status": "Done"
    }
  ]
}
```

## Business Rules

1. **Workflow Validation**: Status transition must be allowed by workflow
2. **Partial Success**: If some issues fail, others may still succeed
3. **Permissions**: User must have transition permission for each issue
4. **Status Availability**: Status must be available in each issue's workflow

## Limitations

1. **Same Status**: All issues updated to the same status
2. **No Custom Fields**: Cannot update custom fields during transition
3. **No Comments**: Cannot add transition comments
4. **Workflow Constraints**: Must follow workflow transition rules

## Best Practices

### 1. Verify Status Name

Ensure status name matches exactly (case-sensitive) for all issues.

### 2. Group Similar Issues

Update issues from the same project/workflow together for consistency.

### 3. Check Workflow

Verify the status transition is valid for all issues before updating.

### 4. Handle Failures

Check the response to identify which issues failed to update.

## Common Use Cases

### 1. Sprint Completion

```
Scenario: Move all completed sprint items to Done
Action: Update status of all sprint issues to "Done"
Result: Sprint items marked as complete
```

### 2. Bulk Triage

```
Scenario: Move multiple bugs to In Progress for investigation
Action: Update status of bug list to "In Progress"
Result: Bugs marked as being worked on
```

### 3. Release Preparation

```
Scenario: Move all release items to Testing
Action: Update status of release issues to "Testing"
Result: Issues ready for QA
```

## Related Catalog Requests

- [Update Status of Issue](/pages/UpdateStatusOfIssue.md) - Update single issue status
- [Update an Issue](/pages/UpdateAnIssue.md) - Update multiple fields of an issue
- [Get Issues](/pages/GetIssues.md) - Query issues to get keys for update

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.updateStatusOfMultipleIssues()

## See Also

- [Update Status of Issue](/pages/UpdateStatusOfIssue.md)
- [Update an Issue](/pages/UpdateAnIssue.md)
- [Jira Transitions API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issues/#api-rest-api-3-issue-issueidorkey-transitions-post)

