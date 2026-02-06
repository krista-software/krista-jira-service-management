# JQL Query Guide

## Overview

Jira Query Language (JQL) is a powerful query language that allows you to search for issues in Jira using structured
queries. This guide provides comprehensive syntax reference and examples for creating effective JQL queries.

## Basic Syntax

### Query Structure

```
field operator value
```

### Multiple Conditions

```
field1 operator1 value1 AND field2 operator2 value2
field1 operator1 value1 OR field2 operator2 value2
```

### Grouping with Parentheses

```
(field1 = value1 OR field1 = value2) AND field2 = value3
```

## Common Fields

| Field             | Description         | Example                          |
|-------------------|---------------------|----------------------------------|
| `project`         | Project key or name | `project = "DEMO"`               |
| `status`          | Issue status        | `status = "In Progress"`         |
| `assignee`        | Assigned user       | `assignee = "john.doe"`          |
| `reporter`        | Reporting user      | `reporter = currentUser()`       |
| `priority`        | Issue priority      | `priority = High`                |
| `type`            | Issue type          | `type = Bug`                     |
| `summary`         | Issue summary/title | `summary ~ "login"`              |
| `description`     | Issue description   | `description ~ "authentication"` |
| `created`         | Creation date       | `created >= -7d`                 |
| `updated`         | Last update date    | `updated >= "2025-01-01"`        |
| `resolved`        | Resolution date     | `resolved >= -30d`               |
| `labels`          | Issue labels        | `labels = urgent`                |
| `component`       | Project component   | `component = "Frontend"`         |
| `fixVersion`      | Fix version         | `fixVersion = "2.1.0"`           |
| `affectedVersion` | Affected version    | `affectedVersion = "2.0.0"`      |

## Operators

### Equality Operators

| Operator | Description                   | Example                 |
|----------|-------------------------------|-------------------------|
| `=`      | Equals                        | `status = "Done"`       |
| `!=`     | Not equals                    | `status != "Done"`      |
| `IS`     | Is (for empty values)         | `assignee IS EMPTY`     |
| `IS NOT` | Is not (for non-empty values) | `assignee IS NOT EMPTY` |

### Comparison Operators

| Operator | Description           | Example                  |
|----------|-----------------------|--------------------------|
| `>`      | Greater than          | `created > "2025-01-01"` |
| `>=`     | Greater than or equal | `priority >= High`       |
| `<`      | Less than             | `created < "2025-01-01"` |
| `<=`     | Less than or equal    | `priority <= Medium`     |

### Text Operators

| Operator | Description                 | Example             |
|----------|-----------------------------|---------------------|
| `~`      | Contains (case-insensitive) | `summary ~ "login"` |
| `!~`     | Does not contain            | `summary !~ "test"` |

### List Operators

| Operator | Description | Example                              |
|----------|-------------|--------------------------------------|
| `IN`     | In list     | `status IN ("To Do", "In Progress")` |
| `NOT IN` | Not in list | `priority NOT IN (Low, Trivial)`     |

### Logical Operators

| Operator | Description                   | Example                                  |
|----------|-------------------------------|------------------------------------------|
| `AND`    | Both conditions must be true  | `project = "DEMO" AND status = "Done"`   |
| `OR`     | Either condition must be true | `priority = High OR priority = Critical` |
| `NOT`    | Condition must be false       | `NOT status = "Done"`                    |

## Functions

### User Functions

| Function             | Description            | Example                               |
|----------------------|------------------------|---------------------------------------|
| `currentUser()`      | Current logged-in user | `assignee = currentUser()`            |
| `membersOf("group")` | Members of a group     | `assignee IN membersOf("developers")` |

### Date Functions

| Function         | Description            | Example                     |
|------------------|------------------------|-----------------------------|
| `now()`          | Current date/time      | `created <= now()`          |
| `startOfDay()`   | Start of current day   | `created >= startOfDay()`   |
| `startOfWeek()`  | Start of current week  | `created >= startOfWeek()`  |
| `startOfMonth()` | Start of current month | `created >= startOfMonth()` |
| `startOfYear()`  | Start of current year  | `created >= startOfYear()`  |
| `endOfDay()`     | End of current day     | `created <= endOfDay()`     |
| `endOfWeek()`    | End of current week    | `created <= endOfWeek()`    |
| `endOfMonth()`   | End of current month   | `created <= endOfMonth()`   |
| `endOfYear()`    | End of current year    | `created <= endOfYear()`    |

### Sprint Functions (for Agile projects)

| Function          | Description              | Example                     |
|-------------------|--------------------------|-----------------------------|
| `openSprints()`   | Issues in open sprints   | `sprint IN openSprints()`   |
| `closedSprints()` | Issues in closed sprints | `sprint IN closedSprints()` |

## Date Formats

### Absolute Dates

```
created = "2025-01-15"
created = "2025-01-15 14:30"
updated >= "2025/01/15"
```

### Relative Dates

```
created >= -7d        # Last 7 days
updated >= -2w        # Last 2 weeks
resolved >= -1M       # Last 1 month
created >= -1y        # Last 1 year
```

### Time Units

- `d` = days
- `w` = weeks
- `M` = months
- `y` = years
- `h` = hours
- `m` = minutes

## Common Query Examples

### Project-Based Queries

```sql
-- All issues in a project
project = "DEMO"

-- Issues in multiple projects
project IN ("DEMO", "TEST", "PROD")

-- Issues not in specific project
project != "ARCHIVE"
```

### Status-Based Queries

```sql
-- Open issues (not done)
status != "Done"

-- Issues in progress
status = "In Progress"

-- Issues in multiple statuses
status IN ("To Do", "In Progress", "Code Review")
```

### User-Based Queries

```sql
-- My assigned issues
assignee = currentUser()

-- Unassigned issues
assignee IS EMPTY

-- Issues reported by current user
reporter = currentUser()

-- Issues assigned to specific user
assignee = "john.doe@company.com"
```

### Priority and Type Queries

```sql
-- High priority bugs
priority = High AND type = Bug

-- Critical or blocker issues
priority IN (Critical, Blocker)

-- Stories and epics
type IN (Story, Epic)
```

### Date-Based Queries

```sql
-- Issues created in last 7 days
created >= -7d

-- Issues updated today
updated >= startOfDay()

-- Issues resolved this month
resolved >= startOfMonth() AND resolved <= endOfMonth()

-- Issues created in specific date range
created >= "2025-01-01" AND created <= "2025-01-31"
```

### Text Search Queries

```sql
-- Issues with "login" in summary
summary ~ "login"

-- Issues with "bug" in summary or description
summary ~ "bug" OR description ~ "bug"

-- Issues without "test" in summary
summary !~ "test"
```

### Complex Queries

```sql
-- Open high priority issues assigned to me
assignee = currentUser() AND priority = High AND status != "Done"

-- Recent bugs in specific projects
type = Bug AND created >= -14d AND project IN ("DEMO", "PROD")

-- Overdue issues (past due date)
duedate < now() AND status != "Done"

-- Issues in current sprint for my team
sprint IN openSprints() AND assignee IN membersOf("development-team")
```

## Advanced Techniques

### Subqueries with Functions

```sql
-- Issues assigned to project leads
assignee IN membersOf("project-leads")

-- Issues in projects I'm watching
project IN projectsWhereUserHasRole("Watchers")
```

### Custom Field Queries

```sql
-- Query custom fields (use field ID)
"Story Points" > 5
customfield_10001 = "High"
cf[10001] IN ("Option1", "Option2")
```

### Ordering Results

```sql
-- Order by priority (highest first)
project = "DEMO" ORDER BY priority DESC

-- Order by multiple fields
project = "DEMO" ORDER BY priority DESC, created ASC

-- Order by custom field
project = "DEMO" ORDER BY "Story Points" DESC
```

## Best Practices

### 1. Performance Optimization

- Use project filters to limit scope
- Add date ranges for time-sensitive queries
- Avoid overly broad text searches
- Use specific field filters when possible

### 2. Query Construction

- Start with broad filters and add specificity
- Use parentheses for complex logic
- Test queries in Jira's issue navigator first
- Keep queries readable with proper spacing

### 3. Field Usage

- Use exact field names (case-sensitive)
- Quote string values consistently
- Use appropriate operators for field types
- Verify custom field names and IDs

### 4. Error Prevention

- Balance parentheses in complex queries
- Quote multi-word values
- Use valid date formats
- Verify function syntax

## Common Errors and Solutions

### Syntax Errors

```sql
-- Wrong: Missing quotes
project = DEMO

-- Correct: Quoted project name
project = "DEMO"

-- Wrong: Unbalanced parentheses
(project = "DEMO" AND status = "Done"

-- Correct: Balanced parentheses
(project = "DEMO" AND status = "Done")
```

### Field Name Errors

```sql
-- Wrong: Incorrect field name
Project = "DEMO"

-- Correct: Proper field name
project = "DEMO"
```

### Date Format Errors

```sql
-- Wrong: Invalid date format
created = "01/15/2025"

-- Correct: Valid date format
created = "2025-01-15"
```

## Testing Your Queries

### 1. Jira Issue Navigator

- Use Jira's built-in issue navigator to test queries
- Switch between basic and advanced (JQL) modes
- Verify results match expectations

### 2. Query Validation

- Check syntax highlighting in Jira interface
- Review error messages for specific issues
- Test with small result sets first

### 3. Performance Testing

- Monitor query execution time
- Test with different date ranges
- Optimize slow-performing queries

## See Also

- [Fetch by JQL Query](pages/FetchByJQLQuery.md) - Execute JQL queries in Krista
- [Get Results for Fetch Issues by JQL Query](pages/GetResultsForFetchIssuesByJQLQuery.md) - Retrieve JQL results
- [Get Issues](pages/GetIssues.md) - Simpler issue filtering without JQL
- [Jira Extension Configuration](pages/ExtensionConfiguration.md) - Setup and authentication
