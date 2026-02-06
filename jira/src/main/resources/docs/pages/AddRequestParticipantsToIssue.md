# Add Request Participants to Issue

## Overview

Adds one or more request participants to a Jira Service Management issue. Request participants can view and comment on
the issue.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name       | Type     | Required | Description                                | Example                    |
|----------------------|----------|----------|--------------------------------------------|----------------------------|
| Issue Key            | Text     | Yes      | The key of the Service Management issue    | `"SUP-123"`                |
| Request Participants | [ Text ] | Yes      | List of account IDs to add as participants | `["5d123...", "5d456..."]` |

## Output Parameters

| Parameter Name                    | Type    | Description                                       |
|-----------------------------------|---------|---------------------------------------------------|
| Request Participants Successfully | Boolean | Indicates if participants were added successfully |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while adding request participants to issue"`

**Resolution**:

1. Verify issue key is valid and is a Service Management issue
2. Check account IDs are valid
3. Ensure you have permission to modify the issue
4. Verify issue is in a Service Desk project

## Usage Examples

### Example 1: Add Single Participant

**Input**:

```
Issue Key: "SUP-123"
Request Participants: ["5d1234567890abcdef"]
```

**Output**:

```json
{
  "Request Participants Successfully": true
}
```

### Example 2: Add Multiple Participants

**Input**:

```
Issue Key: "SUP-456"
Request Participants: ["5d1234567890abcdef", "5d9876543210fedcba", "5d1111222233334444"]
```

**Output**:

```json
{
  "Request Participants Successfully": true
}
```

## Business Rules

1. **Service Desk Only**: Only works with Jira Service Management issues
2. **Participant Access**: Participants can view and comment on the issue
3. **Duplicate Safe**: Adding existing participant has no effect
4. **Notification**: Participants receive notification when added
5. **Permissions**: Requires permission to modify the issue

## Limitations

1. **Service Desk Only**: Does not work with regular Jira issues
2. **Account IDs Required**: Must use account IDs, not emails
3. **No Removal**: Cannot remove participants (only add)
4. **No Validation**: No validation of account IDs before execution

## Best Practices

### 1. Verify Account IDs

Use [Get Users Account Id](/pages/GetUsersAccountId.md) to look up account IDs by email.

### 2. Use for Collaboration

Add participants who need visibility into the issue.

### 3. Notify Participants

Consider adding a comment to explain why they were added.

## Common Use Cases

### 1. Add Stakeholders

```
Scenario: Add stakeholders to service request
Action: Add their account IDs as participants
Result: Stakeholders can track progress
```

### 2. Escalation

```
Scenario: Escalate issue to management
Action: Add manager as participant
Result: Manager has visibility into issue
```

### 3. Cross-Team Collaboration

```
Scenario: Issue requires input from multiple teams
Action: Add team representatives as participants
Result: All teams can collaborate on issue
```

## Related Catalog Requests

- [Get Users Account Id](/pages/GetUsersAccountId.md) - Look up account IDs
- [Add a Comment](/pages/AddAComment.md) - Add comment to issue
- [Get Issue By Key](/pages/GetIssueByKey.md) - Get issue details

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.addRequestParticipantsToIssue()

## See Also

- [Get Users Account Id](/pages/GetUsersAccountId.md)
- [Add a Comment](/pages/AddAComment.md)
- [Jira Service Desk API](https://developer.atlassian.com/cloud/jira/service-desk/rest/api-group-request/)

