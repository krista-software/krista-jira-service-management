# Create User

## Overview

Sends an invitation to a user to join the Jira instance. Creates a new user account and sends them an email invitation.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type | Required | Description                         | Example                  |
|----------------|------|----------|-------------------------------------|--------------------------|
| Email          | Text | Yes      | Email address of the user to invite | `"john.doe@company.com"` |

## Output Parameters

| Parameter Name | Type | Description                         |
|----------------|------|-------------------------------------|
| Self           | Text | URL to the user's profile           |
| Account Id     | Text | Unique Jira account ID for the user |
| Email Address  | Text | Email address of the created user   |
| Display Name   | Text | Display name of the user            |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while creating user using Account Id."`

**Resolution**:

1. Verify email address is valid and not already in use
2. Check you have user management permissions
3. Ensure email server is configured correctly
4. Verify Jira license has available user seats

## Usage Examples

### Example 1: Invite New User

**Input**:

```
Email: "jane.smith@company.com"
```

**Output**:

```json
{
  "Self": "https://your-domain.atlassian.net/rest/api/3/user?accountId=5d9876543210fedcba",
  "Account Id": "5d9876543210fedcba",
  "Email Address": "jane.smith@company.com",
  "Display Name": "Jane Smith"
}
```

## Business Rules

1. **Email Uniqueness**: Email must be unique across the Jira instance
2. **Invitation Email**: Invitation email sent automatically upon creation
3. **License Consumption**: Creates a billable user (consumes license seat)
4. **Default Permissions**: User created with default permissions

## Limitations

1. **No Group Assignment**: Cannot add user to groups during creation (use [Update User](UpdateUser.md))
2. **No Custom Display Name**: Cannot specify custom display name
3. **Email Required**: Must have valid email address
4. **License Limit**: Limited by available Jira license seats

## Best Practices

### 1. Verify Email Before Creating

Ensure email address is correct before creating user to avoid wasted invitations.

### 2. Follow Up with Group Assignment

After creating user, use [Update User](/pages/UpdateUser.md) to add them to appropriate groups.

### 3. Track Account IDs

Store the returned Account ID for future operations.

## Common Use Cases

### 1. Onboarding New Employees

```
Scenario: New employee joins company
Action: Create Jira user with employee email
Result: User invited and can access Jira after accepting invitation
```

### 2. Automated User Provisioning

```
Scenario: HR system triggers user creation
Action: Automatically create Jira user when employee added to HR system
Result: Jira account ready when employee starts
```

## Related Catalog Requests

- [Update User](/pages/UpdateUser.md) - Add user to groups after creation
- [Delete User](/pages/DeleteUser.md) - Remove user from Jira
- [Get Users Account Id](/pages/GetUsersAccountId.md) - Look up user by email

## Technical Implementation

### Helper Class

- **Class**: OrganizationArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.createUser()

## See Also

- [Update User](/pages/UpdateUser.md)
- [Delete User](/pages/DeleteUser.md)
- [Jira User Management API](/pages/https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-users/#api-rest-api-3-user-post)

