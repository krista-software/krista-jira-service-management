# Get User Account Information

## Overview

Retrieve detailed account information for a Jira user by searching with their email address or name. Returns
comprehensive user profile data including account details, contact information, and application access.

## Request Details

- **Area**: Organization
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (with validation and error handling)

## Input Parameters

| Parameter Name | Type | Required | Description                                              | Example                              |
|----------------|------|----------|----------------------------------------------------------|--------------------------------------|
| Search Query   | Text | No       | Email address or username to search for the user account | `john.doe@company.com` or `john.doe` |

### Parameter Details

**Search Query**

- Can be either an email address or Jira username
- Search is case-insensitive
- Partial matches are not supported - must be exact match
- If empty or null, returns an empty user object
- Whitespace is trimmed automatically

## Output Parameters

| Parameter Name | Type         | Description                              | Example                      |
|----------------|--------------|------------------------------------------|------------------------------|
| User           | Entity(User) | Complete user account information object | See User Entity Fields below |

### User Entity Fields

| Field Name       | Type       | Required | Description                                                      | Example                                                       |
|------------------|------------|----------|------------------------------------------------------------------|---------------------------------------------------------------|
| Name             | Text       | Yes      | Jira username (display name or account name)                     | `john.doe`                                                    |
| Account Id       | Text       | Yes      | Unique Atlassian account identifier                              | `712020:f540c563-d057-4be0-999a-4cbdb5fb8d84`                 |
| Email            | Text       | Yes      | User's email address (may be empty if privacy settings restrict) | `john.doe@company.com`                                        |
| Account Type     | Text       | Yes      | Type of Atlassian account                                        | `atlassian` or `customer`                                     |
| Self             | Text       | No       | REST API URL for the user resource                               | `https://example.atlassian.net/rest/api/2/user?accountId=123` |
| Display Name     | Text       | No       | User's full display name                                         | `John Doe`                                                    |
| Application Keys | List[Text] | No       | List of Atlassian applications the user has access to            | `["jira-software", "jira-servicedesk"]`                       |

### Output Details

**User Object**

- Returns complete user profile if match is found
- Returns empty user object (all fields null/empty) if no match found
- Email field may be empty string if user's privacy settings restrict email visibility
- Application Keys shows which Jira products the user can access

## Validation Rules

| Validation             | Error Message                                                                            | Resolution                                         |
|------------------------|------------------------------------------------------------------------------------------|----------------------------------------------------|
| Search query format    | The field value is invalid. Please check the format and requirements for this field.     | Use valid email format or username                 |
| User not found         | No user found matching the search query. Please verify the email or username is correct. | Verify the email/username spelling and try again   |
| Authentication failure | Authentication failed. Please verify your credentials and try again.                     | Re-authenticate with valid Jira credentials        |
| Permission denied      | You don't have sufficient permissions to view user information in Jira.                  | Request user browse permissions from administrator |

## Error Handling

### Input Errors (INPUT_ERROR)

- **Cause**: Invalid search query format
- **Error Message**: "The field value is invalid. Please check the format and requirements for this field."
- **Common Scenarios**: Malformed email addresses, special characters in username
- **Resolution**: Provide valid email address or username

### Logic Errors (LOGIC_ERROR)

- **Cause**: User not found or permission issues
- **Error Message**: "No user found matching the search query. Please verify the email or username is correct."
- **Common Scenarios**: Non-existent user, typo in email/username, insufficient permissions
- **Resolution**: Verify user exists in Jira and check permissions

### System Errors (SYSTEM_ERROR)

- **Cause**: Jira API unavailable or network issues
- **Error Message**: "Unable to connect to Jira. Please check your internet connection and try again."
- **Common Scenarios**: Network issues, Jira server downtime, API timeouts
- **Resolution**: Check connectivity and Jira server status

### Authorization Errors (MUST_AUTHORIZE)

- **Cause**: Authentication failure or expired credentials
- **Error Message**: "Authentication failed. Please verify your credentials and try again."
- **Common Scenarios**: Token expiration, invalid credentials, missing authentication
- **Resolution**: Re-authenticate with valid Jira credentials

## Usage Examples

### Example 1: Search by Email Address

**Input:**

- Search Query: `john.doe@company.com`

**Expected Output:**

```json
{
  "User": {
    "Name": "john.doe",
    "Account Id": "712020:f540c563-d057-4be0-999a-4cbdb5fb8d84",
    "Email": "john.doe@company.com",
    "Account Type": "atlassian",
    "Self": "https://company.atlassian.net/rest/api/2/user?accountId=712020:f540c563-d057-4be0-999a-4cbdb5fb8d84",
    "Display Name": "John Doe",
    "Application Keys": [
      "jira-software",
      "jira-servicedesk"
    ]
  }
}
```

**Result**: Complete user account information retrieved successfully

### Example 2: Search by Username

**Input:**

- Search Query: `jane.smith`

**Expected Output:**

```json
{
  "User": {
    "Name": "jane.smith",
    "Account Id": "557058:a4e1c89b-14f3-4657-9fe3-a9c6e8a38893",
    "Email": "jane.smith@company.com",
    "Account Type": "atlassian",
    "Self": "https://company.atlassian.net/rest/api/2/user?accountId=557058:a4e1c89b-14f3-4657-9fe3-a9c6e8a38893",
    "Display Name": "Jane Smith",
    "Application Keys": [
      "jira-software"
    ]
  }
}
```

**Result**: User found by username and full profile returned

### Example 3: User Not Found

**Input:**

- Search Query: `nonexistent@company.com`

**Expected Output:**

```json
{
  "User": {
    "Name": null,
    "Account Id": null,
    "Email": null,
    "Account Type": null,
    "Self": null,
    "Display Name": null,
    "Application Keys": null
  }
}
```

### Example 5: Empty Search Query

**Input:**

- Search Query: `` (empty or null)

**Expected Output:**

```json
{
  "User": {
    "Name": null,
    "Account Id": null,
    "Email": null,
    "Account Type": null,
    "Self": null,
    "Display Name": null,
    "Application Keys": null
  }
}
```

**Result**: Empty user object returned for null/empty search query

## Business Rules

1. **Exact Match Required**: Search query must exactly match email or username (case-insensitive)
2. **Privacy Compliance**: Email field respects user privacy settings and may be empty
3. **Account Types**: Supports both `atlassian` (internal) and `customer` (external) account types
4. **Application Access**: Application Keys field shows which Jira products user can access
5. **Unique Identifier**: Account Id is the primary unique identifier for users across Atlassian products
6. **Permission Based**: Results depend on requesting user's permissions to view user information

## Limitations

- **No Partial Matching**: Search requires exact email or username match
- **Single User Result**: Returns only one user (first match if multiple exist)
- **Privacy Restrictions**: Email may be hidden based on user privacy settings
- **Permission Dependent**: Requires user browse permissions in Jira
- **No Bulk Search**: Only one user can be searched per request
- **Active Users Only**: May not return deactivated or deleted users

## Best Practices

1. **Use Email for Accuracy**: Email addresses are more unique than usernames
2. **Handle Empty Results**: Always check if user fields are null before using
3. **Respect Privacy**: Don't assume email field will always be populated
4. **Cache Account IDs**: Store Account Id for future API calls (more stable than email)
5. **Error Handling**: Implement proper error handling for user not found scenarios
6. **Permission Verification**: Ensure requesting user has appropriate permissions

## Common Use Cases

### User Lookup and Verification

1. **Email Validation**:
    - Verify user exists before assigning issues
    - Validate email addresses in user input forms
    - Check user account status before granting access
    - Confirm user identity for audit purposes

2. **Account Information Retrieval**:
    - Get Account Id for API calls requiring user identification
    - Retrieve display name for user-friendly interfaces
    - Check application access for feature availability
    - Verify account type for permission logic

### Issue Assignment Preparation

1. **Pre-Assignment Validation**:
    - Verify assignee exists before creating/updating issues
    - Get Account Id for issue assignment API calls
    - Check user has access to required Jira products
    - Validate user permissions before assignment

2. **User Selection**:
    - Search for users in assignment workflows
    - Populate user selection dropdowns
    - Validate user input in automation scripts
    - Confirm user availability for task assignment

### User Management and Administration

1. **Account Auditing**:
    - Verify user account details for compliance
    - Check user application access for license management
    - Audit user account types for security reviews
    - Validate user information for reporting

2. **User Directory Integration**:
    - Sync user information with external systems
    - Validate user data consistency across platforms
    - Update user records in integrated applications
    - Maintain user directory accuracy

### Automation and Integration

1. **Workflow Automation**:
    - Validate users in automated issue creation
    - Get Account Ids for bulk operations
    - Verify user existence in scheduled tasks
    - Populate user fields in automated processes

2. **Third-Party Integration**:
    - Map users between Jira and external systems
    - Validate user credentials in SSO flows
    - Sync user information across platforms
    - Maintain user data consistency

## Related Catalog Requests

- [Get Users Account Id](pages/GetUsersAccountId.md) - Get Account Id by email (simpler alternative)
- [Create User](pages/CreateUser.md) - Create new Jira user accounts
- [Update User](pages/UpdateUser.md) - Update existing user information
- [Delete User](pages/DeleteUser.md) - Remove user accounts
- [Create an Issue](pages/CreateAnIssue.md) - Create issues with user assignment
- [Update an Issue](pages/UpdateAnIssue.md) - Update issue assignee using Account Id

## Technical Implementation

- **Service Class**: `app.krista.extensions.devsecops.development.jira.services.UserService`
- **DAO Class**: `app.krista.extensions.devsecops.development.jira.impl.UserDao`
- **Entity Class**: `app.krista.extensions.devsecops.development.jira.catalog.entities.User`
- **Validation Flow**: Input validation → User search → Field extraction → Response mapping
- **API Endpoint**: Jira REST API v2 `/user/search` endpoint
- **Telemetry Metrics**: Tracks user search success rates, query patterns, and usage analytics

## Troubleshooting

### User Not Found Errors

**Problem**: Search query returns empty user object
**Solution**:

1. Verify email address or username spelling is correct
2. Check if user account exists in Jira (may be deactivated)
3. Try searching with alternate identifier (email vs username)
4. Ensure user hasn't been deleted or merged
5. Verify you have permissions to view the user

### Empty Email Field

**Problem**: User found but email field is empty
**Solution**:

1. This is expected behavior when user has privacy settings enabled
2. Email visibility is controlled by user's privacy preferences
3. Use Account Id or Display Name as alternative identifiers
4. Contact Jira administrator if email is required for business process
5. Consider using username instead of email for lookups

### Permission Denied Errors

**Problem**: Cannot retrieve user information due to permissions
**Solution**:

1. Contact Jira administrator to grant "Browse Users" permission
2. Verify your account has appropriate global permissions
3. Check if user directory browsing is restricted in Jira settings
4. Ensure you're authenticated with correct credentials
5. Verify API token or OAuth credentials have required scopes

### Authentication Failures

**Problem**: Request fails with authentication error
**Solution**:

1. Verify Jira credentials are valid and not expired
2. Re-authenticate using OAuth 2.0 or API token
3. Check if API token has been revoked
4. Ensure authentication method matches Jira configuration
5. Verify network connectivity to Jira instance

### Multiple Users with Same Name

**Problem**: Uncertain which user is returned when multiple matches exist
**Solution**:

1. Use email address instead of username for more specific search
2. Email addresses are unique and provide better match accuracy
3. If using username, verify the returned Account Id matches expected user
4. Consider implementing additional validation using Display Name
5. Use Account Id for subsequent operations to ensure consistency

## Performance Considerations

- **Response Time**: Typically 200-500ms depending on Jira instance load
- **Rate Limiting**: Subject to Jira API rate limits (check Jira Cloud/Server limits)
- **Caching**: Consider caching Account Ids to reduce API calls
- **Batch Operations**: For multiple users, consider implementing batch processing
- **Network Latency**: Response time varies based on network conditions

## Security Considerations

- **Privacy Compliance**: Respects user privacy settings for email visibility
- **Permission Enforcement**: Only returns data user has permission to view
- **Audit Logging**: User searches may be logged for security auditing
- **Data Sensitivity**: User information should be handled according to privacy policies
- **Access Control**: Implement appropriate access controls in consuming applications

## See Also

- [Get Users Account Id](pages/GetUsersAccountId.md) - Simpler request to get just Account Id
- [Create User](pages/CreateUser.md) - Create new user accounts
- [Update User](pages/UpdateUser.md) - Update user information
- [Delete User](pages/DeleteUser.md) - Delete user accounts
- [Create an Issue](pages/CreateAnIssue.md) - Create issues with user assignment
- [Update an Issue](pages/UpdateAnIssue.md) - Update issue assignee
- [Jira Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [Troubleshooting Guide](pages/Troubleshooting.md) - Common issues and solutions
