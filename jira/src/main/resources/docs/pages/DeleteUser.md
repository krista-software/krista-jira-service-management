# Delete User

## Overview

Removes a user from the Jira instance. This permanently deletes the user account.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Account Id | Text | Yes | The Jira account ID of the user to delete | `"5d1234567890abcdef"` |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Deleted | Switch | Boolean indicating if user was successfully deleted |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while deleting user using Account Id."`

**Resolution**:
1. Verify account ID is valid
2. Check you have user management permissions
3. Ensure user exists in Jira
4. Verify user is not the last administrator

## Usage Examples

### Example 1: Delete User

**Input**:
```
Account Id: "5d1234567890abcdef"
```

**Output**:
```json
{
  "Deleted": true
}
```

## Business Rules

1. **Permanent Deletion**: User account is permanently removed
2. **Issue Reassignment**: Issues assigned to user remain but show deleted user
3. **History Preserved**: User's activity history is preserved
4. **License Release**: Frees up a license seat
5. **Cannot Delete Last Admin**: Cannot delete the last administrator

## Limitations

1. **Permanent**: Cannot undo deletion
2. **No Bulk Delete**: Can only delete one user at a time
3. **No Deactivation**: Deletes user instead of deactivating

## Best Practices

### 1. Verify Before Deleting
Double-check the account ID before deletion as this is permanent.

### 2. Reassign Issues First
Reassign user's issues before deletion for better tracking.

### 3. Export User Data
Export any necessary user data before deletion.

### 4. Consider Deactivation
For temporary removal, consider deactivating instead of deleting.

## Common Use Cases

### 1. Employee Offboarding
```
Scenario: Employee leaves company
Action: Delete user account
Result: User removed, license freed
```

### 2. Clean Up Inactive Users
```
Scenario: Remove inactive contractor accounts
Action: Delete unused accounts
Result: License seats freed for new users
```

## Related Catalog Requests

- [Create User](/pages/CreateUser.md) - Create new user
- [Update User](/pages/UpdateUser.md) - Modify user groups
- [Get Users Account Id](/pages/GetUsersAccountId.md) - Look up account ID

## Technical Implementation

### Helper Class
- **Class**: OrganizationArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Validation**: ValidationOrchestrator validates account ID
- **Service**: IssueDao.deleteUser()

## See Also

- [Create User](/pages/CreateUser.md)
- [Update User](/pages/UpdateUser.md)
- [Jira User Management API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-users/#api-rest-api-3-user-delete)

