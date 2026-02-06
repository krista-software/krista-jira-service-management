# Get Request Type Field Value

## Overview

Retrieves the request type field value for a given issue key and request type name. This is useful for understanding how
request types are stored in Jira Service Management and for debugging request type issues.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name    | Type | Required | Description                                          | Example         |
|-------------------|------|----------|------------------------------------------------------|-----------------|
| Issue Key         | Text | Yes      | The key of an existing Jira Service Management issue | `"SUP-123"`     |
| Request Type Name | Text | Yes      | The name of the request type to look up              | `"Get IT help"` |

### Parameter Details

#### Issue Key

- Must be a valid, existing issue key from a Service Desk project
- Format: PROJECT_KEY-NUMBER
- Case-sensitive
- Used to identify the service desk and retrieve available request types

#### Request Type Name

- The display name of the request type as shown in Jira
- **Case-sensitive** - must match exactly
- Examples: "Get IT help", "Request new software", "Report a system problem"
- Used to find the corresponding field value

> ** Note**: The request type name is case-sensitive and must match exactly as it appears in Jira Service Management.

## Output Parameters

| Parameter Name | Type | Description                                                                           |
|----------------|------|---------------------------------------------------------------------------------------|
| Field Value    | Text | The request type field value in format "serviceDeskId/requestTypeId" or error message |

### Field Value Format

The field value is returned in the format: `"serviceDeskId/requestTypeId"`

Example: `"1/2"` where:

- `1` is the service desk (portal) ID
- `2` is the request type ID

If the request type is not found, returns: `"Invalid Issue key or Request Type"`

## Validation Rules

No automatic validation is performed. The catalog request attempts to retrieve the field value and returns an error
message if unsuccessful.

## Error Handling

### System Errors (SYSTEM_ERROR)

**Cause**: Unable to retrieve request type information

**Common Scenarios**:

- Issue key doesn't exist
- Issue is not in a Service Desk project
- Request type name doesn't match any available types
- Network or API errors

**Error Message**: Returns `"Invalid Issue key or Request Type"` in the Field Value output

**Resolution**:

1. Verify issue key exists and is from a Service Desk project
2. Check request type name spelling (case-sensitive)
3. Ensure you have access to the service desk
4. Verify the request type exists in the service desk

## Usage Examples

### Example 1: Get Field Value for Valid Request Type

**Input**:

```
Issue Key: "SUP-123"
Request Type Name: "Get IT help"
```

**Output**:

```json
{
  "Field Value": "1/2"
}
```

**Result**: Successfully retrieved field value "1/2" for request type "Get IT help"

### Example 2: Invalid Request Type Name

**Input**:

```
Issue Key: "SUP-123"
Request Type Name: "Invalid Type"
```

**Output**:

```json
{
  "Field Value": "Invalid Issue key or Request Type"
}
```

**Result**: Request type not found

### Example 3: Case-Sensitive Matching

**Input**:

```
Issue Key: "SUP-123"
Request Type Name: "get it help"  (lowercase)
```

**Output**:

```json
{
  "Field Value": "Invalid Issue key or Request Type"
}
```

**Result**: Failed because request type name is case-sensitive

**Corrected Input**:

```
Issue Key: "SUP-123"
Request Type Name: "Get IT help"  (correct case)
```

**Output**:

```json
{
  "Field Value": "1/2"
}
```

**Result**: Success with correct case

## Business Rules

1. **Service Desk Only**: Only works with Jira Service Management (Service Desk) projects
2. **Case-Sensitive Matching**: Request type name must match exactly, including case
3. **Issue Access**: User must have access to the specified issue
4. **Service Desk Access**: User must have access to the service desk

## Limitations

1. **Service Desk Projects Only**: Does not work with regular Jira projects
2. **Case-Sensitive**: Request type name matching is case-sensitive
3. **Single Request Type**: Can only query one request type at a time
4. **No List Function**: Does not list all available request types (returns value for specific type only)

## Best Practices

### 1. Verify Request Type Name Case

Always use the exact case as shown in Jira Service Management. Copy the name directly from Jira if possible.

### 2. Use for Debugging

Use this catalog request to debug issues with request type creation and to understand the field value format.

### 3. Test with Known Issue

Test with an existing issue that has a request type to verify the service desk is configured correctly.

### 4. Document Field Values

Document the field values for commonly used request types for reference in automation workflows.

## Common Use Cases

### 1. Debugging Request Type Issues

```
Scenario: Request type not appearing when creating issues
Action: Use this request to verify the field value format
Result: Understand how request types are stored and identify configuration issues
```

### 2. Mapping Request Types

```
Scenario: Need to map request type names to field values for automation
Action: Query each request type to get its field value
Result: Create a mapping table for use in automated workflows
```

### 3. Verifying Service Desk Configuration

```
Scenario: Confirm service desk is properly configured
Action: Test with known request types
Result: Verify request types are accessible and properly configured
```

## Related Catalog Requests

- [Create an Issue](/pages/CreateAnIssue.md) - Use request type names when creating Service Management issues

## Technical Implementation

### Helper Class

- **Class**: OrganizationArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.getRequestTypeFieldValue()

### Implementation Flow

1. Receives issue key and request type name
2. Calls `getIssueId()` to get internal issue ID
3. Calls Service Desk API: `/request/{issueId}/request-types`
4. Retrieves list of valid request types for the issue
5. Matches request type name (case-sensitive)
6. Returns field value in format "portalKey/requestTypeKey"
7. Returns error message if not found

## Troubleshooting

### Issue: Always returns "Invalid Issue key or Request Type"

**Cause**: Issue key doesn't exist, not a Service Desk project, or request type name doesn't match
**Solution**:

1. Verify issue key exists
2. Confirm issue is in a Service Desk project
3. Check request type name spelling and case
4. Verify you have access to the service desk

### Issue: Field value format unexpected

**Cause**: Different Jira versions may use different formats
**Solution**: The format should be "serviceDeskId/requestTypeId" - if different, check Jira version and API
documentation

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md)
- [Create an Issue](/pages/CreateAnIssue.md)
- [Jira Service Desk API Documentation](https://developer.atlassian.com/cloud/jira/service-desk/rest/api-group-request/)

