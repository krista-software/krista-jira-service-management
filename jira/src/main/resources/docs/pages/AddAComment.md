# Add a Comment

## Overview

Adds a comment to an existing Jira issue. Supports both public and internal comments.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name     | Type      | Required | Description                                       | Example                          |
|--------------------|-----------|----------|---------------------------------------------------|----------------------------------|
| Comment            | Paragraph | Yes      | The comment text to add                           | `"This issue has been resolved"` |
| Issue Key          | Text      | Yes      | The key of the issue to comment on                | `"PROJ-123"`                     |
| Comment Visibility | Pick One  | No       | Visibility of the comment: "Public" or "Internal" | `"Public"`                       |

## Output Parameters

| Parameter Name | Type   | Description                                          |
|----------------|--------|------------------------------------------------------|
| Is successful  | Switch | Boolean indicating if comment was added successfully |

## Error Handling

### Authorization Errors

**Cause**: Authentication required or insufficient permissions

**Resolution**: Ensure user is authenticated and has permission to comment on the issue

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while adding comment"`

**Resolution**:

1. Verify issue key is valid
2. Check user has comment permissions
3. Ensure comment text is not empty

## Usage Examples

### Example 1: Add Public Comment

**Input**:

```
Comment: "I've reviewed this issue and it looks good to merge."
Issue Key: "PROJ-123"
Comment Visibility: "Public"
```

**Output**:

```json
{
  "Is successful": true
}
```

### Example 2: Add Internal Comment

**Input**:

```
Comment: "Internal note: This requires database migration."
Issue Key: "PROJ-456"
Comment Visibility: "Internal"
```

**Output**:

```json
{
  "Is successful": true
}
```

### Example 3: Add Simple Comment

**Input**:

```
Comment: "Fixed in latest commit."
Issue Key: "BUG-789"
Comment Visibility: (not specified - defaults to Public)
```

**Output**:

```json
{
  "Is successful": true
}
```

## Business Rules

1. **Default Visibility**: If not specified, comment is public
2. **Internal Comments**: Only visible to project team members
3. **Public Comments**: Visible to all users with issue access
4. **Comment History**: All comments are preserved in issue history
5. **Author**: Comment author is the authenticated user

## Limitations

1. **No Formatting**: Plain text only, no rich text or markdown
2. **No Attachments**: Cannot attach files with comment
3. **No Editing**: Cannot edit comment after creation
4. **No Mentions**: Cannot @mention users

## Best Practices

### 1. Use Clear, Concise Comments

Write comments that add value and context to the issue.

### 2. Use Internal Comments for Sensitive Information

Use internal visibility for comments containing sensitive or internal-only information.

### 3. Include Context

Provide enough context so others can understand the comment without additional information.

### 4. Reference Related Work

Mention related commits, pull requests, or other issues when relevant.

## Common Use Cases

### 1. Status Update

```
Scenario: Provide progress update on issue
Action: Add comment with current status
Result: Team informed of progress
```

### 2. Resolution Note

```
Scenario: Document how issue was resolved
Action: Add comment explaining resolution
Result: Future reference for similar issues
```

### 3. Internal Discussion

```
Scenario: Discuss implementation details with team
Action: Add internal comment
Result: Discussion visible only to team
```

### 4. Customer Communication

```
Scenario: Respond to customer inquiry
Action: Add public comment
Result: Customer can see response
```

## Related Catalog Requests

- [Get Issue By Key](/pages/GetIssueByKey.md) - Get issue details before commenting
- [Update an Issue](/pages/UpdateAnIssue.md) - Update issue fields
- [Create an Issue](/pages/CreateAnIssue.md) - Create new issue

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.addComment()

### Implementation Details

- Converts comment text to Atlassian Document Format (ADF)
- Sets visibility based on Comment Visibility parameter
- Internal comments use role-based visibility restriction

## See Also

- [Get Issue By Key](/pages/GetIssueByKey.md)
- [Update an Issue](/pages/UpdateAnIssue.md)
- [Jira Comments API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issue-comments/)

