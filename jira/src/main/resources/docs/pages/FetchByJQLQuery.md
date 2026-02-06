# Fetch by JQL Query

## Overview

Executes a custom JQL (Jira Query Language) query to retrieve issues. This is an asynchronous operation that supports complex queries and large result sets.

## Request Details

- **Area**: Rituals
- **Type**: QUERY_SYSTEM
- **Retry Support**:  Yes - If JQL query validation fails, you can re-enter the query

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| JQL Query | Paragraph | Yes | The JQL query to execute | `"project = PROJ AND status = 'In Progress'"` |

### Parameter Details

#### JQL Query
- Full JQL (Jira Query Language) query string
- Supports all standard JQL operators and functions
- Can include complex conditions, sorting, and filtering
- Validated before execution
- Maximum recommended result size: 1000 issues

**Common JQL Operators**:
- `=`, `!=` - Equals, not equals
- `>`, `<`, `>=`, `<=` - Comparison
- `IN`, `NOT IN` - List membership
- `~` - Contains (text search)
- `IS`, `IS NOT` - Null checks
- `AND`, `OR` - Logical operators

**Common JQL Functions**:
- `currentUser()` - Current logged-in user
- `now()` - Current date/time
- `startOfDay()`, `endOfDay()` - Date boundaries
- `membersOf()` - Group members

**Example Queries**:
```
project = PROJ AND status = "In Progress"
assignee = currentUser() AND status != Done
priority IN (High, Highest) AND created >= -7d
project = SUP AND labels = urgent ORDER BY created DESC
```

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Task ID | Text | Unique identifier for the async task (used to retrieve results) |

### Asynchronous Operation

This catalog request initiates an asynchronous query and returns a Task ID. To get the results, use the companion catalog request **"Get Results for Fetch Issues by JQL Query"** with the Task ID.

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| JQL Syntax Valid | "JQL query validation failed: {error details}" | Fix JQL syntax errors |
| Field Names Valid | "Field '{fieldName}' does not exist" | Use valid Jira field names |
| Values Valid | "Invalid value '{value}' for field '{field}'" | Use valid field values |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid JQL syntax or query validation failure

**Common Scenarios**:
- Syntax errors in JQL query
- Invalid field names
- Invalid field values
- Unsupported operators

**Error Messages**:
- `"JQL query validation failed: {error details}"`
- `"Field '{fieldName}' does not exist"`
- `"Invalid value '{value}' for field '{field}'"`

**Resolution**:
1. Verify JQL syntax is correct
2. Check field names are valid
3. Ensure field values are properly quoted
4. Test query in Jira's issue search first

### Logic Errors (LOGIC_ERROR)

**Cause**: Query execution failures

**Error Message**: `"Error occurred while Validating JQL Query"`

**Resolution**:
1. Simplify the query
2. Check for permission issues
3. Verify all referenced projects/fields are accessible

## Usage Examples

### Example 1: Get Issues by Project and Status

**Input**:
```
JQL Query: "project = PROJ AND status = 'In Progress'"
```

**Output**:
```json
{
  "Task ID": "fetch-jql-12345-67890"
}
```

**Next Step**: Use Task ID with "Get Results for Fetch Issues by JQL Query" to retrieve results

### Example 2: Get My Open Issues

**Input**:
```
JQL Query: "assignee = currentUser() AND status != Done ORDER BY priority DESC"
```

**Output**:
```json
{
  "Task ID": "fetch-jql-23456-78901"
}
```

### Example 3: Get Recent High Priority Bugs

**Input**:
```
JQL Query: "project = PROJ AND issuetype = Bug AND priority IN (High, Highest) AND created >= -7d"
```

**Output**:
```json
{
  "Task ID": "fetch-jql-34567-89012"
}
```

### Example 4: Complex Query with Multiple Conditions

**Input**:
```
JQL Query: "project IN (PROJ, SUP) AND (status = 'In Progress' OR status = 'To Do') AND assignee IN membersOf('developers') ORDER BY created DESC"
```

**Output**:
```json
{
  "Task ID": "fetch-jql-45678-90123"
}
```

### Example 5: Retry Scenario - Invalid JQL

**Initial Input**:
```
JQL Query: "project = PROJ AND status = In Progress"  (missing quotes)
```

**Error Response**:
```
Error: "JQL query validation failed: Expecting closing quote"
Prompt: "Would you like to re-enter the JQL query?"
```

**Corrected Input**:
```
JQL Query: "project = PROJ AND status = 'In Progress'"
```

**Result**: Query validated and Task ID returned

## Business Rules

1. **Asynchronous Execution**: Query runs in background, results retrieved separately
2. **Query Validation**: JQL is validated before execution
3. **Permission Filtering**: Results only include issues user can access
4. **Result Limit**: Maximum 1000 issues recommended
5. **Timeout**: Long-running queries may timeout (typically 5 minutes)

## Limitations

1. **Asynchronous Only**: Cannot get immediate results
2. **Result Size**: Very large result sets (>1000 issues) may cause performance issues
3. **Timeout**: Complex queries on large datasets may timeout
4. **No Streaming**: All results loaded into memory at once
5. **JQL Version**: Supports JQL features available in your Jira version

## Best Practices

### 1. Test Queries in Jira First
Always test your JQL query in Jira's issue search before using it in the catalog request.

### 2. Use Specific Filters
Add project filters to limit scope and improve performance:
```
Good: project = PROJ AND status = 'In Progress'
Bad: status = 'In Progress'  (searches all projects)
```

### 3. Limit Result Size
Use date filters or other constraints to limit results:
```
created >= -30d  (last 30 days)
updated >= -7d   (updated in last week)
```

### 4. Use ORDER BY for Consistent Results
Always include ORDER BY clause for predictable result ordering:
```
ORDER BY created DESC
ORDER BY priority DESC, created DESC
```

### 5. Quote String Values
Always quote string values in JQL:
```
Correct: status = 'In Progress'
Incorrect: status = In Progress
```

### 6. Use Functions for Dynamic Queries
Use JQL functions for dynamic, user-specific queries:
```
assignee = currentUser()
created >= startOfDay(-7d)
```

## Common Use Cases

### 1. Sprint Report
```
Scenario: Get all issues in current sprint
JQL: "project = PROJ AND sprint in openSprints() ORDER BY rank"
Result: Issues in active sprint
```

### 2. Overdue Issues
```
Scenario: Find overdue issues
JQL: "duedate < now() AND status != Done ORDER BY duedate ASC"
Result: Issues past their due date
```

### 3. Team Workload
```
Scenario: Check team's current workload
JQL: "assignee IN membersOf('team-developers') AND status IN ('To Do', 'In Progress')"
Result: Team's active work items
```

### 4. Release Planning
```
Scenario: Issues for next release
JQL: "project = PROJ AND fixVersion = '2.0' ORDER BY priority DESC"
Result: Issues targeted for version 2.0
```

## Related Catalog Requests

- **Get Results for Fetch Issues by JQL Query** - Retrieve results using Task ID
- [Get Issues](/pages/GetIssues.md) - Simple filtering without JQL
- [Get User Issues](/pages/GetUserIssues.md) - Get issues for specific user

## Technical Implementation

### Helper Class
- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Validation**: IssueDao.fetchErrorsInJQLQuery()
- **Service**: IssueDao.searchByJQL() (async)

### Execution Flow
1. Receives JQL query
2. Validates query syntax via Jira API
3. If validation fails, prompts for retry
4. If valid, initiates async query execution
5. Returns Task ID for result retrieval
6. Query executes in background
7. Results stored with Task ID
8. User retrieves results using Task ID

### Event-Driven Architecture
- Uses EventHandler to trigger async execution
- Results published as events
- Companion catalog request listens for completion events

## Troubleshooting

### Issue: "JQL query validation failed"
**Cause**: Syntax error in JQL query
**Solution**: Test query in Jira's issue search, fix syntax errors

### Issue: "Field does not exist"
**Cause**: Invalid field name in query
**Solution**: Use valid Jira field names, check custom field names

### Issue: Task ID never returns results
**Cause**: Query timeout or execution failure
**Solution**: Simplify query, reduce result set size, check logs

### Issue: Results incomplete
**Cause**: Result set too large
**Solution**: Add filters to reduce result size

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md)
- [Get Issues](/pages/GetIssues.md)
- [Jira JQL Documentation](https://support.atlassian.com/jira-software-cloud/docs/use-advanced-search-with-jira-query-language-jql/)
- [JQL Functions Reference](https://support.atlassian.com/jira-software-cloud/docs/jql-functions/)

