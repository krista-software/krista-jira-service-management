# Get All Comments

## Overview

Retrieve all comments from a Jira issue, including comment body, author information, and creation date for comprehensive
issue discussion tracking.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description                                       | Example    |
|----------------|------|----------|---------------------------------------------------|------------|
| Issue Key      | Text | Yes      | Unique key of the issue to retrieve comments from | `DEMO-123` |

### Parameter Details

**Issue Key**

- Must be a valid issue key in the format PROJECT-NUMBER
- Issue must exist and be accessible to the user
- Case-sensitive (typically uppercase)
- User must have permission to view the issue and its comments

## Output Parameters

| Parameter Name | Type            | Description                           |
|----------------|-----------------|---------------------------------------|
| Comments       | List of Objects | Array of comment objects with details |

### Comment Object Structure

Each comment in the list contains:

| Field Name   | Type | Description                            | Example                                     |
|--------------|------|----------------------------------------|---------------------------------------------|
| Comment Body | Text | The text content of the comment        | `This issue has been reviewed and approved` |
| Author ID    | Text | Account ID of the comment author       | `5d7c8a9b1234567890abcdef`                  |
| Author Name  | Text | Display name of the comment author     | `John Doe`                                  |
| Date Created | Date | Timestamp when the comment was created | `2025-01-15T10:30:00.000Z`                  |

## Usage Examples

### Example 1: Get All Comments from Issue

**Input:**

- Issue Key: `DEMO-123`

**Expected Output:**

```json
{
  "Comments": [
    {
      "Comment Body": "Initial analysis completed. Root cause identified.",
      "Author ID": "5d7c8a9b1234567890abcdef",
      "Author Name": "Jane Smith",
      "Date Created": "2025-01-15T09:00:00.000Z"
    },
    {
      "Comment Body": "Fix has been implemented and deployed to staging.",
      "Author ID": "5d7c8a9b1234567890fedcba",
      "Author Name": "John Doe",
      "Date Created": "2025-01-15T14:30:00.000Z"
    },
    {
      "Comment Body": "Verified in staging. Ready for production deployment.",
      "Author ID": "5d7c8a9b1234567890abcdef",
      "Author Name": "Jane Smith",
      "Date Created": "2025-01-15T16:45:00.000Z"
    }
  ]
}
```

### Example 2: Issue with No Comments

**Input:**

- Issue Key: `DEMO-456`

**Expected Output:**

```json
{
  "Comments": []
}
```

**Result**: Empty array when issue has no comments

### Example 3: Issue with Single Comment

**Input:**

- Issue Key: `DEMO-789`

**Expected Output:**

```json
{
  "Comments": [
    {
      "Comment Body": "Assigned to development team for investigation.",
      "Author ID": "5d7c8a9b1234567890abcdef",
      "Author Name": "Project Manager",
      "Date Created": "2025-01-14T11:20:00.000Z"
    }
  ]
}
```

## Common Use Cases

### 1. Issue Discussion Tracking

- **Scenario**: Track all discussions and updates on an issue
- **Implementation**: Retrieve all comments to review conversation history
- **Benefit**: Complete visibility into issue communication

### 2. Audit Trail Analysis

- **Scenario**: Review who commented and when for compliance
- **Implementation**: Extract comment authors and timestamps
- **Benefit**: Maintain audit trail for regulatory requirements

### 3. Collaboration Monitoring

- **Scenario**: Monitor team collaboration on critical issues
- **Implementation**: Retrieve comments to assess team engagement
- **Benefit**: Identify communication gaps and improve collaboration

### 4. Status Update Extraction

- **Scenario**: Extract progress updates from issue comments
- **Implementation**: Parse comment bodies for status keywords
- **Benefit**: Automated status reporting and tracking

### 5. Knowledge Base Creation

- **Scenario**: Build knowledge base from issue resolutions
- **Implementation**: Collect comments containing solution details
- **Benefit**: Reusable knowledge for similar issues

## Validation Rules

### Issue Key Validation

- Must not be empty or null
- Must follow PROJECT-NUMBER format
- Issue must exist in Jira
- User must have view permission

## Error Handling

### Common Errors

**Issue Not Found**

- **Error**: "Issue with key 'DEMO-999' does not exist"
- **Cause**: Invalid or non-existent issue key
- **Solution**: Verify issue key exists in Jira

**Authentication Required**

- **Error**: "Please authenticate yourself"
- **Cause**: OAuth token expired or invalid credentials
- **Solution**: Re-authenticate using OAuth 2.0 or API token

## Related Catalog Requests

- [Add a Comment](pages/AddAComment.md) - Add new comments to issues
- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve complete issue details
- [Get Issues](pages/GetIssues.md) - Search for issues to retrieve comments from
- [Update an Issue](pages/UpdateAnIssue.md) - Update issues based on comment analysis

## Technical Implementation

- **Helper Class**: `app.krista.extensions.devsecops.development.jira.impl.IssueDao`
- **Validation Flow**: Issue key validation → Permission check → Comment retrieval → Response formatting
- **Service Delegation**: Delegates to Jira REST API v3 comment endpoints
- **Telemetry Metrics**: Tracks comment retrieval success rates, comment counts, and access patterns

## Troubleshooting

### Issue: Comments Not Appearing

**Symptoms**: Request succeeds but returns empty array despite comments existing

**Possible Causes**:

1. User lacks permission to view comments
2. Comments are restricted to specific roles
3. Internal comments not visible to current user

**Solutions**:

1. Verify user has appropriate permissions in Jira
2. Check comment visibility settings in Jira
3. Use admin credentials to view all comments
4. Contact Jira administrator for permission adjustments

### Issue: Incomplete Comment Data

**Symptoms**: Some comment fields are missing or null

**Possible Causes**:

1. Comment author account deleted
2. Comment created before certain fields existed
3. API version compatibility issues

**Solutions**:

1. Handle null values gracefully in your application
2. Check Jira audit logs for deleted users
3. Verify API version compatibility

## Performance Considerations

- **Large Comment Volumes**: Issues with hundreds of comments may take longer to retrieve
- **Pagination**: Consider implementing pagination for issues with many comments
- **Caching**: Cache comment data when appropriate to reduce API calls
- **Rate Limiting**: Be aware of Jira API rate limits when retrieving comments for multiple issues

## See Also

- [Add a Comment](pages/AddAComment.md) - Add comments to issues
- [Get Issue By Key](pages/GetIssueByKey.md) - Retrieve issue details
- [Jira Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions

