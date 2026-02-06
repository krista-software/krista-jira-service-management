# Get Notification

## Overview

Receives constant updates from Jira via webhook events. This is a WAIT_FOR_EVENT type request that listens for Jira
webhook notifications.

## Request Details

- **Area**: Rituals
- **Type**: WAIT_FOR_EVENT
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type     | Required | Description                                    | Example                |
|----------------|----------|----------|------------------------------------------------|------------------------|
| eventName      | Text     | Yes      | Event name (automatically provided by webhook) | `"jira:issue_updated"` |
| eventData      | FreeForm | Yes      | Event data (automatically provided by webhook) | (system provided)      |

## Output Parameters

| Parameter Name | Type          | Description                                   |
|----------------|---------------|-----------------------------------------------|
| Update         | [ Composite ] | Array of update information from the webhook  |
| Issue Key      | Text          | The key of the issue that triggered the event |

## Error Handling

### System Errors

**Cause**: Webhook processing failures

**Resolution**:

1. Verify webhook is configured correctly
2. Check webhook URL is accessible
3. Review webhook logs for errors
4. Ensure Jira can reach the extension

## Usage Examples

### Example 1: Issue Updated Event

**Input** (from webhook):

```
eventName: "jira:issue_updated"
eventData: { issue: { key: "PROJ-123", ... } }
```

**Output**:

```json
{
  "Update": [
    {
      "eventType": "issue_updated",
      "timestamp": "2024-01-15T10:30:00.000Z",
      "user": "john.doe@company.com",
      "changes": [
        "status",
        "assignee"
      ]
    }
  ],
  "Issue Key": "PROJ-123"
}
```

### Example 2: Issue Created Event

**Input** (from webhook):

```
eventName: "jira:issue_created"
eventData: { issue: { key: "PROJ-456", ... } }
```

**Output**:

```json
{
  "Update": [
    {
      "eventType": "issue_created",
      "timestamp": "2024-01-15T11:00:00.000Z",
      "user": "jane.smith@company.com"
    }
  ],
  "Issue Key": "PROJ-456"
}
```

## Business Rules

1. **Event-Driven**: Triggered by Jira webhook events
2. **Real-Time**: Receives updates as they happen in Jira
3. **Webhook Required**: Requires webhook configuration in Jira
4. **Automatic**: No manual invocation required

## Webhook Events

Common Jira webhook events:

- `jira:issue_created` - Issue created
- `jira:issue_updated` - Issue updated
- `jira:issue_deleted` - Issue deleted
- `comment_created` - Comment added
- `comment_updated` - Comment updated
- `worklog_created` - Worklog added

## Limitations

1. **Webhook Dependency**: Requires webhook configuration
2. **Network Dependency**: Requires network connectivity from Jira
3. **No Filtering**: Receives all configured webhook events
4. **No Retry**: Missed events cannot be retrieved

## Best Practices

### 1. Configure Webhooks Properly

Ensure webhooks are configured correctly in Jira settings.

### 2. Handle All Event Types

Implement handlers for all expected event types.

### 3. Validate Event Data

Always validate event data before processing.

### 4. Log Events

Log all received events for debugging and auditing.

## Common Use Cases

### 1. Real-Time Notifications

```
Scenario: Notify users of issue updates
Action: Listen for webhook events
Result: Users notified in real-time
```

### 2. Automation Triggers

```
Scenario: Trigger automation on issue changes
Action: Process webhook events
Result: Automated actions executed
```

### 3. Integration Sync

```
Scenario: Sync Jira changes to external system
Action: Receive webhook events and sync
Result: External system stays in sync
```

## Related Catalog Requests

- [Get Issue By Key](/pages/GetIssueByKey.md) - Get full issue details after notification
- [Update an Issue](/pages/UpdateAnIssue.md) - Update issue in response to event

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: Event-driven, processes webhook payloads

### Webhook Configuration

Webhooks must be configured in Jira:

1. Go to Jira Settings > System > Webhooks
2. Create webhook pointing to extension URL
3. Select events to monitor
4. Save configuration

## See Also

- [Webhook Settings](/pages/WebhookSettings.md)
- [Extension Configuration](/pages/ExtensionConfiguration.md)
- [Jira Webhooks Documentation](https://developer.atlassian.com/cloud/jira/platform/webhooks/)

