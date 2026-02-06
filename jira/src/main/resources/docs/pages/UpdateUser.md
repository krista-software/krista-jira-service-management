# Update User

## Overview

Adds a user to one or more Jira groups. This is used to grant users access to projects and assign permissions.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type     | Required | Description                            | Example                        |
|----------------|----------|----------|----------------------------------------|--------------------------------|
| Account Id     | Text     | Yes      | The Jira account ID of the user        | `"5d1234567890abcdef"`         |
| Group Names    | [ Text ] | Yes      | List of group names to add the user to | `["developers", "jira-users"]` |

## Output Parameters

| Parameter Name | Type   | Description                                                 |
|----------------|--------|-------------------------------------------------------------|
| User Added     | Switch | Boolean indicating if user was successfully added to groups |

## Error Handling

### System Errors (SYSTEM_ERROR)

**Error Message**: `"Error occurred while updating user using Account Id."`

**Resolution**:

1. Verify account ID is valid
2. Check group names exist
3. Ensure you have group management permissions
4. Verify user exists in Jira

## Usage Examples

### Example 1: Add User to Developer Group

**Input**:

```
Account Id: "5d1234567890abcdef"
Group Names: ["developers"]
```

**Output**:

```json
{
  "User Added": true
}
```

### Example 2: Add User to Multiple Groups

**Input**:

```
Account Id: "5d9876543210fedcba"
Group Names: ["developers", "jira-administrators", "confluence-users"]
```

**Output**:

```json
{
  "User Added": true
}
```

## Business Rules

1. **Group Existence**: All groups must exist before adding user
2. **Additive Operation**: Only adds user to groups, doesn't remove from existing groups
3. **Duplicate Safe**: Adding user to group they're already in has no effect
4. **Permissions**: Requires group management permissions

## Limitations

1. **No Group Removal**: Cannot remove user from groups (only add)
2. **No Group Creation**: Groups must already exist
3. **No Validation**: No validation of account ID or group names before execution

## Best Practices

### 1. Verify Account ID

Use [Get Users Account Id](/pages/GetUsersAccountId.md) to get the correct account ID before updating.

### 2. Check Group Names

Verify group names are spelled correctly and exist in Jira.

### 3. Use Standard Groups

Use standard Jira groups for consistent permissions:

- `jira-users` - Basic Jira access
- `jira-administrators` - Admin access
- `jira-developers` - Developer access

## Common Use Cases

### 1. Grant Project Access

```
Scenario: New developer needs access to development projects
Action: Add user to "developers" group
Result: User can access development projects
```

### 2. Promote to Administrator

```
Scenario: User needs admin privileges
Action: Add user to "jira-administrators" group
Result: User has admin access
```

## Related Catalog Requests

- [Create User](/pages/CreateUser.md) - Create user before updating
- [Delete User](/pages/DeleteUser.md) - Remove user from Jira
- [Get Users Account Id](/pages/GetUsersAccountId.md) - Look up account ID

## Technical Implementation

### Helper Class

- **Class**: OrganizationArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Validation**: ValidationOrchestrator validates account ID
- **Service**: IssueDao.updateUser()

## See Also

- [Create User](/pages/CreateUser.md)
- [Delete User](/pages/DeleteUser.md)
- [Jira Group Management API](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-groups/)

