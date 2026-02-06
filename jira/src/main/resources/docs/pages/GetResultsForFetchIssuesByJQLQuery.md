# Get Results for Fetch Issues by JQL Query

## Overview

Retrieves the results of an asynchronous JQL query initiated by "Fetch by JQL query". This is a WAIT_FOR_EVENT type
request that listens for query completion.

## Request Details

- **Area**: Rituals
- **Type**: WAIT_FOR_EVENT
- **Retry Support**:  No

## Input Parameters

| Parameter Name | Type     | Required | Description                                    | Example                   |
|----------------|----------|----------|------------------------------------------------|---------------------------|
| Task ID        | Text     | Yes      | The task ID returned from "Fetch by JQL query" | `"fetch-jql-12345-67890"` |
| eventName      | Text     | Yes      | Event name (automatically provided)            | `"fetch-jql-12345-67890"` |
| eventData      | FreeForm | Yes      | Event data (automatically provided)            | (system provided)         |

## Output Parameters

| Parameter Name | Type              | Description                            |
|----------------|-------------------|----------------------------------------|
| List of Issues | [ Entity(Issue) ] | Array of issues matching the JQL query |

## Error Handling

### System Errors

**Cause**: Query execution failures or timeout

**Resolution**:

1. Verify Task ID is correct
2. Check if query completed successfully
3. Review logs for query execution errors
4. Retry the original JQL query if needed

## Usage Examples

### Example 1: Get Query Results

**Input**:

```
Task ID: "fetch-jql-12345-67890"
```

**Output**:

```json
{
  "List of Issues": [
    {
      "key": "PROJ-123",
      "summary": "Implement login feature",
      "status": "In Progress",
      "priority": "High"
    },
    {
      "key": "PROJ-456",
      "summary": "Fix database connection",
      "status": "To Do",
      "priority": "Medium"
    }
  ]
}
```

### Example 2: Empty Results

**Input**:

```
Task ID: "fetch-jql-23456-78901"
```

**Output**:

```json
{
  "List of Issues": []
}
```

**Result**: Query completed but no issues matched the criteria

## Business Rules

1. **Asynchronous**: Waits for query completion event
2. **Event-Driven**: Triggered when query execution completes
3. **Task ID Matching**: Task ID must match the event name
4. **Single Use**: Each task ID can only be used once
5. **Timeout**: Request may timeout if query takes too long

## Limitations

1. **Requires Task ID**: Must have valid task ID from "Fetch by JQL query"
2. **No Retry**: Cannot retry if event missed
3. **Timeout**: Long-running queries may timeout
4. **Single Retrieval**: Results can only be retrieved once

## Best Practices

### 1. Use Immediately After Fetch

Call this request immediately after "Fetch by JQL query" to avoid missing the event.

### 2. Handle Timeouts

Implement timeout handling for long-running queries.

### 3. Validate Task ID

Ensure Task ID is correctly passed from the fetch request.

### 4. Check for Empty Results

Handle cases where query returns no results.

## Common Use Cases

### 1. Async Query Execution

```
Scenario: Execute complex JQL query asynchronously
Action: 
  1. Call "Fetch by JQL query" to get Task ID
  2. Call this request with Task ID to get results
Result: Query results retrieved when ready
```

### 2. Large Result Sets

```
Scenario: Query returns many issues
Action: Use async pattern to avoid timeout
Result: Results retrieved without blocking
```

### 3. Background Processing

```
Scenario: Run query in background while user continues work
Action: Initiate query, retrieve results later
Result: Non-blocking query execution
```

## Related Catalog Requests

- [Fetch by JQL Query](/pages/FetchByJQLQuery.md) - Initiate the async query (required first step)
- [Get Issues](/pages/GetIssues.md) - Simpler synchronous query
- [Get User Issues](/pages/GetUserIssues.md) - Get user-specific issues

## Technical Implementation

### Helper Class

- **Class**: RitualsArea
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Service**: Event-driven, listens for query completion events

### Event Flow

1. "Fetch by JQL query" initiates async query and returns Task ID
2. Query executes in background
3. On completion, event published with Task ID as event name
4. This request listens for event matching Task ID
5. Returns query results when event received

## See Also

- [Fetch by JQL Query](/pages/FetchByJQLQuery.md)
- [Get Issues](/pages/GetIssues.md)
- [Extension Configuration](/pages/ExtensionConfiguration.md)

