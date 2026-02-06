# Get Users Account Id

## Overview

Retrieves a user's Jira account ID by their email address. This is useful for operations that require the account ID.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type | Required | Description               | Example                  |
|----------------|------|----------|---------------------------|--------------------------|
| Email          | Text | No       | Email address of the user | `"john.doe@company.com"` |

## Output Parameters

| Parameter Name | Type | Description                      |
|----------------|------|----------------------------------|
| Account Id     | Text | The Jira account ID for the user |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while getting user account id."`

**Resolution**:

1. Verify email address is correct
2. Check user exists in Jira
3. Ensure you have permissions to view users

## Usage Examples

### Example 1: Get Account ID by Email

**Input**:

```
Email: "jane.smith@company.com"
```

**Output**:

```json
{
  "Account Id": "5d9876543210fedcba"
}
```

### Example 2: Look Up Developer Account

**Input**:

```
Email: "developer@company.com"
```

**Output**:

```json
{
  "Account Id": "5d1111222233334444"
}
```

## Business Rules

1. **Email Lookup**: Searches for user by email address
2. **Case Insensitive**: Email search is case-insensitive
3. **Exact Match**: Must match exact email address
4. **Active Users Only**: Only returns active user accounts

## Limitations

1. **Email Required**: Cannot search by name or username
2. **Single User**: Returns only one account ID
3. **No Partial Match**: Must provide complete email address

## Best Practices

### 1. Verify Email Format

Ensure email address is properly formatted before searching.

### 2. Cache Account IDs

Store account IDs for frequently accessed users to reduce API calls.

### 3. Use for Automation

Use this request to look up account IDs before assigning issues or updating users.

## Common Use Cases

### 1. Assign Issue to User

```
Scenario: Need to assign issue to user by email
Action: Get account ID, then use in assign request
Result: Issue assigned to correct user
```

### 2. Add User to Group

```
Scenario: Add user to group but only have email
Action: Get account ID, then update user
Result: User added to group
```

### 3. User Lookup

```
Scenario: Find user's account ID for reporting
Action: Search by email
Result: Account ID retrieved for use in reports
```

## Related Catalog Requests

- [Create User](/pages/CreateUser.md) - Create new user
- [Update User](/pages/UpdateUser.md) - Update user groups (requires account ID)
- [Delete User](/pages/DeleteUser.md) - Delete user (requires account ID)
- [Assign a Task to User](/pages/AssignATaskToUser.md) - Assign issues (requires account ID)

## Technical Implementation

### Helper Class

- **Class**: OrganizationArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Validation**: ValidationOrchestrator validates email
- **Service**: IssueDao.getUsersAccountId()

## See Also

- [Create User](/pages/CreateUser.md)
- [Update User](/pages/UpdateUser.md)
- [Jira User Search API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-user-search/)

