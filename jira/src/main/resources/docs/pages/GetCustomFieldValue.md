# Get Custom Field Value

## Overview

Retrieves the value of a custom field from a Jira issue.

## Request Details

- **Area**: Rituals
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Issue Key | Text | Yes | The key of the issue | `"PROJ-123"` |
| Custom Field | Text | Yes | The name or ID of the custom field | `"Story Points"`, `"customfield_10001"` |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Response | [ Text ] | Array of custom field values |

## Error Handling

### System Errors

**Cause**: Custom field retrieval failures

**Resolution**:
1. Verify issue key is valid
2. Check custom field name/ID is correct
3. Ensure custom field exists in the project
4. Verify user has permission to view the field

## Usage Examples

### Example 1: Get Story Points

**Input**:
```
Issue Key: "PROJ-123"
Custom Field: "Story Points"
```

**Output**:
```json
{
  "Response": ["5"]
}
```

### Example 2: Get Custom Field by ID

**Input**:
```
Issue Key: "PROJ-456"
Custom Field: "customfield_10001"
```

**Output**:
```json
{
  "Response": ["High", "Critical"]
}
```

### Example 3: Empty Custom Field

**Input**:
```
Issue Key: "PROJ-789"
Custom Field: "Sprint"
```

**Output**:
```json
{
  "Response": []
}
```

## Business Rules

1. **Field Existence**: Custom field must exist in the project
2. **Permissions**: User must have permission to view the field
3. **Multiple Values**: Returns array for multi-value fields
4. **Empty Values**: Returns empty array if field has no value

## Limitations

1. **Read Only**: Can only read custom field values, not update
2. **No Type Conversion**: Returns values as strings
3. **Field Name/ID**: Must know exact field name or ID

## Best Practices

### 1. Use Field ID for Reliability
Use custom field ID (e.g., "customfield_10001") instead of name for reliability.

### 2. Handle Empty Values
Always check for empty arrays in response.

### 3. Verify Field Exists
Verify custom field exists in the project before querying.

## Common Use Cases

### 1. Story Points Retrieval
```
Scenario: Get story points for reporting
Action: Get custom field "Story Points"
Result: Story points value retrieved
```

### 2. Sprint Information
```
Scenario: Get current sprint for issue
Action: Get custom field "Sprint"
Result: Sprint information retrieved
```

### 3. Custom Metadata
```
Scenario: Retrieve custom metadata
Action: Get custom field value
Result: Metadata retrieved for processing
```

## Related Catalog Requests

- [Get Issue By Key](/pages/GetIssueByKey.md) - Get all issue details including custom fields
- [Update an Issue](/pages/UpdateAnIssue.md) - Update issue fields

## Technical Implementation

### Helper Class
- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.getCustomIssue()

### Custom Field IDs
Custom fields in Jira have IDs like:
- `customfield_10001`
- `customfield_10002`
- etc.

To find custom field IDs:
1. Go to Jira Settings > Issues > Custom Fields
2. Click on the custom field
3. Note the ID in the URL

## See Also

- [Get Issue By Key](/pages/GetIssueByKey.md)
- [Jira Custom Fields API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issue-custom-field-options/)

