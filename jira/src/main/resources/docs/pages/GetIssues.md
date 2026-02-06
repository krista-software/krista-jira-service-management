# Get Issues

## Overview

Retrieves a filtered list of Jira issues based on multiple criteria including summary, priority, issue type, status,
assignee, reporter, labels, and project key.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type | Required | Description                                    | Example                              |
|----------------|------|----------|------------------------------------------------|--------------------------------------|
| Summary        | Text | No       | Filter by issue summary (partial match)        | `"login"`                            |
| Priority       | Text | No       | Filter by priority name                        | `"High"`, `"Medium"`, `"Low"`        |
| Issue Type     | Text | No       | Filter by issue type                           | `"Bug"`, `"Task"`, `"Story"`         |
| Status         | Text | No       | Filter by status name                          | `"To Do"`, `"In Progress"`, `"Done"` |
| Assignee       | Text | No       | Filter by assignee account ID                  | `"5d1234567890abcdef"`               |
| Reporter       | Text | No       | Filter by reporter account ID                  | `"5d1234567890abcdef"`               |
| Labels         | Text | No       | Filter by label (comma-separated for multiple) | `"backend"`, `"frontend,urgent"`     |
| Project Key    | Text | No       | Filter by project key                          | `"PROJ"`, `"SUP"`                    |

### Parameter Details

All parameters are optional. If no parameters are provided, returns recent issues. Multiple filters are combined with
AND logic.

#### Summary

- Performs partial text match (case-insensitive)
- Searches within the issue summary field
- Example: "login" matches "Login button not working"

#### Priority

- Must match exact priority name
- Common values: "Highest", "High", "Medium", "Low", "Lowest"
- Case-sensitive

#### Issue Type

- Must match exact issue type name
- Common values: "Bug", "Task", "Story", "Epic", "Service Request"
- Case-sensitive

#### Status

- Must match exact status name
- Common values: "To Do", "In Progress", "Done", "Closed"
- Case-sensitive

#### Assignee / Reporter

- Must be valid Jira account ID
- Format: Alphanumeric string (e.g., "5d1234567890abcdef")
- Use "Unassigned" for issues with no assignee

#### Labels

- Comma-separated for multiple labels
- Matches issues with ANY of the specified labels (OR logic)
- Case-sensitive

#### Project Key

- Must be valid project key
- Case-sensitive
- Example: "PROJ", "SUP", "TEAM"

## Output Parameters

| Parameter Name          | Type              | Description                                  |
|-------------------------|-------------------|----------------------------------------------|
| List of Filtered Issues | [ Entity(Issue) ] | Array of issues matching the filter criteria |

### Issue Entity Structure

Each issue contains:

- **Key**: Issue key
- **Summary**: Issue summary
- **Description**: Issue description
- **Status**: Current status
- **Issue Type**: Type of issue
- **Priority**: Issue priority
- **Assignee**: Assigned user
- **Reporter**: Reporter user
- **Created**: Creation date
- **Updated**: Last update date
- **Labels**: Array of labels
- **Project Key**: Project key

## Error Handling

### Authorization Errors

**Cause**: Authentication required or insufficient permissions

**Resolution**: Ensure user is authenticated and has access to query issues

## Usage Examples

### Example 1: Get All High Priority Bugs

**Input**:

```
Summary: (empty)
Priority: "High"
Issue Type: "Bug"
Status: (empty)
Assignee: (empty)
Reporter: (empty)
Labels: (empty)
Project Key: (empty)
```

**Output**:

```json
{
  "List of Filtered Issues": [
    {
      "key": "PROJ-123",
      "summary": "Critical login failure",
      "priority": "High",
      "issueType": "Bug",
      "status": "In Progress"
    },
    {
      "key": "PROJ-456",
      "summary": "Database connection timeout",
      "priority": "High",
      "issueType": "Bug",
      "status": "To Do"
    }
  ]
}
```

### Example 2: Get Issues by Project and Status

**Input**:

```
Project Key: "SUP"
Status: "Waiting for support"
(all other fields empty)
```

**Output**: Returns all issues in SUP project with status "Waiting for support"

### Example 3: Get Issues by Multiple Labels

**Input**:

```
Labels: "backend,urgent"
(all other fields empty)
```

**Output**: Returns issues tagged with either "backend" OR "urgent" label

### Example 4: Get Unassigned Issues

**Input**:

```
Assignee: "Unassigned"
Status: "To Do"
(all other fields empty)
```

**Output**: Returns all unassigned issues in "To Do" status

## Business Rules

1. **Filter Combination**: Multiple filters are combined with AND logic
2. **Label Matching**: Multiple labels use OR logic (matches ANY label)
3. **Partial Summary Match**: Summary filter performs partial text search
4. **Default Limit**: Returns maximum 30 issues by default
5. **Sorting**: Results sorted by priority DESC, due date DESC
6. **Access Control**: Only returns issues user has permission to view

## Limitations

1. **Result Limit**: Maximum 30 issues returned per request
2. **No Pagination**: Cannot retrieve results beyond the first 30
3. **No Custom Fields**: Cannot filter by custom fields
4. **No Date Filters**: Cannot filter by created/updated dates
5. **No Advanced JQL**: For complex queries, use [Fetch by JQL Query](/pages/FetchByJQLQuery.md)

## Best Practices

### 1. Use Specific Filters

Combine multiple filters to narrow results and get more relevant issues.

### 2. Use JQL for Complex Queries

For advanced filtering, use the [Fetch by JQL Query](/pages/FetchByJQLQuery.md) catalog request.

### 3. Filter by Project First

When querying large Jira instances, filter by project key first to improve performance.

### 4. Use Account IDs for Users

Always use account IDs (not usernames or emails) for assignee and reporter filters.

## Common Use Cases

### 1. Dashboard - My Open Issues

```
Scenario: Show user's open issues
Filters: Assignee = current user, Status != "Done"
Result: List of user's active work items
```

### 2. Bug Triage

```
Scenario: Find unassigned high-priority bugs
Filters: Issue Type = "Bug", Priority = "High", Assignee = "Unassigned"
Result: Bugs needing assignment
```

### 3. Project Status

```
Scenario: Check project progress
Filters: Project Key = "PROJ", Status = "In Progress"
Result: Currently active work in project
```

## Related Catalog Requests

- [Fetch by JQL Query](/pages/FetchByJQLQuery.md) - Advanced querying with JQL
- [Get User Issues](/pages/GetUserIssues.md) - Get issues for specific user
- [Get User Issue by Issue Type](/pages/GetUserIssueByIssueType.md) - Filter by user and type

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: IssueDao.getIssues()

### JQL Query Construction

The catalog request builds a JQL query from the provided filters:

```
project = "{projectKey}" AND 
summary ~ "{summary}" AND 
priority = "{priority}" AND 
issuetype = "{issueType}" AND 
status = "{status}" AND 
assignee = "{assignee}" AND 
reporter = "{reporter}" AND 
labels IN ({labels})
ORDER BY priority DESC, duedate DESC
```

## Troubleshooting

### Issue: No results returned

**Cause**: Filters too restrictive or no matching issues
**Solution**: Remove some filters or verify filter values are correct

### Issue: Wrong issues returned

**Cause**: Filter values don't match exactly (case-sensitive)
**Solution**: Verify exact spelling and case of status, priority, issue type

## See Also

- [Fetch by JQL Query](/pages/FetchByJQLQuery.md)
- [Get User Issues](/pages/GetUserIssues.md)
- [Extension Configuration](/pages/ExtensionConfiguration.md)

