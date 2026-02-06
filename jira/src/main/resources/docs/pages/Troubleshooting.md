# Troubleshooting Guide

## Overview

This comprehensive troubleshooting guide helps resolve common issues encountered when using the JIRA Service Management
Extension. Issues are organized by category with specific solutions and prevention strategies.

*Systematic approach to troubleshooting JIRA extension issues*

## Authentication Issues

### OAuth 2.0 Authentication Problems

#### Problem: "Authentication is required to access JIRA"

**Symptoms:**

- Extension requests fail with authentication errors
- "Authorize" button appears repeatedly
- Token-related error messages

**Solutions:**

1. **Re-authorize Connection**
    - Click the "Authorize" button in extension configuration
    - Complete the OAuth flow in the popup window
    - Ensure you grant all requested permissions

2. **Check OAuth Configuration**
    - Verify Client ID and Client Secret are correct
    - Confirm callback URL matches JIRA app configuration
    - Ensure JIRA app is not disabled or expired

3. **Clear Browser Cache**
    - Clear browser cookies and cache
    - Disable browser extensions that might interfere
    - Try using an incognito/private browsing window

**Prevention:**

- Regularly monitor token expiration
- Keep OAuth credentials secure and up-to-date
- Document OAuth app configuration for team reference

#### Problem: "Authorization failed" or "Invalid credentials"

**Symptoms:**

- OAuth flow completes but authentication still fails
- Intermittent authentication issues
- Permission-related errors

**Solutions:**

1. **Verify JIRA App Permissions**
    - Check JIRA app has required scopes (read, write, admin)
    - Ensure app is approved by JIRA administrator
    - Verify user account has necessary JIRA permissions

2. **Update OAuth Configuration**
    - Regenerate Client Secret in JIRA app settings
    - Update extension configuration with new credentials
    - Test connection after updating

3. **Check User Account Status**
    - Ensure JIRA user account is active
    - Verify user has access to target projects
    - Check if account has been suspended or restricted

### API Token Authentication Problems

#### Problem: "Invalid API token" or "Token expired"

**Symptoms:**

- Basic authentication fails
- API token not recognized
- Sudden authentication failures

**Solutions:**

1. **Regenerate API Token**
    - Go to JIRA Account Settings → Security → API tokens
    - Create new API token
    - Update extension configuration with new token

2. **Verify Token Format**
    - Ensure token is copied completely without spaces
    - Check for special characters or encoding issues
    - Verify username/email is correct

3. **Test Token Manually**
    - Use curl or Postman to test token directly
    - Verify JIRA instance URL is correct
    - Check network connectivity to JIRA

**Prevention:**

- Set calendar reminders for token renewal
- Use secure password managers for token storage
- Document token management procedures

## Connection Issues

### Network Connectivity Problems

#### Problem: "Unable to connect to JIRA"

**Symptoms:**

- Timeout errors
- Network connection failures
- Intermittent connectivity issues

**Solutions:**

1. **Check Network Configuration**
    - Verify internet connectivity
    - Test JIRA URL in web browser
    - Check firewall and proxy settings

2. **Validate JIRA URL**
    - Ensure URL format is correct (https://your-domain.atlassian.net/rest/api/3)
    - Remove trailing slashes from URL
    - Verify domain spelling and accessibility

3. **Corporate Network Issues**
    - Contact IT administrator about firewall rules
    - Check if JIRA domain is whitelisted
    - Verify proxy configuration if applicable

#### Problem: "Request timeout" or slow performance

**Symptoms:**

- Operations take too long to complete
- Frequent timeout errors
- Poor response times

**Solutions:**

1. **Optimize Query Complexity**
    - Simplify JQL queries
    - Add more specific filters
    - Reduce result set sizes

2. **Check JIRA Server Performance**
    - Monitor JIRA server status
    - Contact JIRA administrator about performance
    - Check for maintenance windows or upgrades

3. **Network Optimization**
    - Test from different network locations
    - Check bandwidth and latency
    - Consider using wired connection instead of WiFi

## Permission Issues

### Access Denied Problems

#### Problem: "You don't have sufficient permissions"

**Symptoms:**

- Permission denied errors
- Cannot access specific projects
- Limited functionality available

**Solutions:**

1. **Verify Project Permissions**
    - Check user is assigned to target projects
    - Verify project roles and permissions
    - Contact JIRA administrator for access

2. **Review Permission Scheme**
    - Understand project permission scheme
    - Check if permissions changed recently
    - Verify group memberships

3. **Test with Different User**
    - Try with JIRA administrator account
    - Test with known working user account
    - Isolate permission vs. configuration issues

#### Problem: "Project not found" or "Access denied to project"

**Symptoms:**

- Cannot see expected projects
- Project-specific operations fail
- Inconsistent project visibility

**Solutions:**

1. **Check Project Visibility**
    - Verify project exists and is not archived
    - Check project permission scheme
    - Ensure user has "Browse Projects" permission

2. **Review User Groups**
    - Verify user is in correct groups
    - Check group permissions for projects
    - Contact administrator for group assignment

## Data and Query Issues

### JQL Query Problems

#### Problem: "Invalid JQL syntax" or query errors

**Symptoms:**

- JQL validation failures
- Syntax error messages
- Query returns unexpected results

**Solutions:**

1. **Validate JQL Syntax**
    - Test query in JIRA issue navigator
    - Check field names and operators
    - Verify parentheses are balanced

2. **Common Syntax Fixes**
    - Quote string values: `project = "DEMO"`
    - Use correct operators: `=`, `!=`, `IN`, `~`
    - Check field name spelling and case

3. **Simplify Complex Queries**
    - Break complex queries into parts
    - Test each condition separately
    - Use JIRA's query builder for validation

**Reference:** See [JQL Query Guide](pages/JQLQueryGuide.md) for detailed syntax help

#### Problem: "No results found" when results expected

**Symptoms:**

- Empty result sets
- Missing expected issues
- Inconsistent query results

**Solutions:**

1. **Verify Filter Criteria**
    - Check if issues actually exist matching criteria
    - Test with broader filters
    - Verify date ranges and status values

2. **Check Permissions**
    - Ensure user can view target issues
    - Verify project access permissions
    - Test with simpler queries

3. **Data Validation**
    - Check if issues were moved or deleted
    - Verify field values match exactly
    - Consider case sensitivity in filters

### Issue Management Problems

#### Problem: "Issue not found" or "Invalid issue key"

**Symptoms:**

- Cannot retrieve specific issues
- Issue key format errors
- Inconsistent issue access

**Solutions:**

1. **Verify Issue Key Format**
    - Use correct format: PROJECT-NUMBER
    - Check spelling and case sensitivity
    - Ensure issue exists and is accessible

2. **Check Issue Status**
    - Verify issue hasn't been deleted
    - Check if issue was moved to different project
    - Ensure issue is visible to current user

3. **Test Issue Access**
    - Try accessing issue directly in JIRA
    - Verify user permissions on the issue
    - Check project access rights

#### Problem: "Cannot update issue" or validation errors

**Symptoms:**

- Issue update operations fail
- Field validation errors
- Workflow transition problems

**Solutions:**

1. **Check Workflow Rules**
    - Verify status transition is allowed
    - Check required fields for transitions
    - Understand workflow configuration

2. **Validate Field Values**
    - Ensure field values match requirements
    - Check custom field configurations
    - Verify user permissions for field updates

3. **Test Incremental Updates**
    - Update one field at a time
    - Identify which field causes validation failure
    - Check field-specific permissions

## Performance Issues

### Slow Response Times

#### Problem: Operations take too long to complete

**Symptoms:**

- Slow query execution
- Timeout errors
- Poor user experience

**Solutions:**

1. **Query Optimization**
    - Use more specific filters
    - Limit result set sizes
    - Add date ranges to queries

2. **Pagination Strategy**
    - Use appropriate page sizes (20-50)
    - Implement progressive loading
    - Avoid retrieving all results at once

3. **System Resources**
    - Check JIRA server performance
    - Monitor network latency
    - Consider peak usage times

### Memory and Resource Issues

#### Problem: "Out of memory" or resource exhaustion

**Symptoms:**

- Application crashes
- Memory-related errors
- System slowdown

**Solutions:**

1. **Reduce Data Volume**
    - Limit query result sizes
    - Use pagination effectively
    - Process data in smaller chunks

2. **Optimize Processing**
    - Avoid storing large datasets in memory
    - Use streaming for large operations
    - Implement proper cleanup procedures

## Configuration Issues

### Extension Setup Problems

#### Problem: Extension configuration fails

**Symptoms:**

- Cannot save configuration
- Validation errors during setup
- Extension not working after configuration

**Solutions:**

1. **Verify Required Fields**
    - Ensure all mandatory fields are completed
    - Check field format requirements
    - Validate URLs and credentials

2. **Test Connection**
    - Use "Test Connection" feature if available
    - Verify credentials work in JIRA directly
    - Check network connectivity

3. **Review Configuration Steps**
    - Follow setup guide step-by-step
    - Verify OAuth app configuration
    - Check callback URL configuration

**Reference:** See [Extension Configuration](pages/ExtensionConfiguration.md) for detailed setup instructions

## Error Message Reference

### Common Error Messages and Solutions

| Error Message                               | Cause                            | Solution                                    |
|---------------------------------------------|----------------------------------|---------------------------------------------|
| "Authentication is required to access JIRA" | Token expired or invalid         | Re-authorize OAuth or regenerate API token  |
| "You don't have sufficient permissions"     | Insufficient JIRA permissions    | Contact administrator for permission review |
| "The specified issue could not be found"    | Invalid issue key or no access   | Verify issue key and check permissions      |
| "Unable to connect to JIRA"                 | Network or server issues         | Check connectivity and JIRA server status   |
| "Invalid JQL syntax"                        | Query syntax errors              | Review JQL syntax and test in JIRA          |
| "Request timeout"                           | Query too complex or server slow | Simplify query or check server performance  |
| "The field value is invalid"                | Field validation failure         | Check field format and requirements         |
| "This status transition is not allowed"     | Workflow restriction             | Check workflow rules and required fields    |

## Getting Help

### Self-Service Resources

1. **Documentation Review**
    - Check relevant catalog request documentation
    - Review configuration guides
    - Consult JQL syntax reference

2. **Testing and Validation**
    - Test operations in JIRA directly
    - Use simpler queries to isolate issues
    - Verify configuration step-by-step

3. **Community Resources**
    - JIRA community forums
    - Atlassian documentation
    - Stack Overflow for technical issues

### Contacting Support

When contacting support, provide:

- **Error Messages**: Exact error text and screenshots
- **Steps to Reproduce**: Detailed steps that cause the issue
- **Configuration Details**: Extension version, JIRA version, authentication method
- **Environment Information**: Browser, network setup, timing of issues
- **Attempted Solutions**: What troubleshooting steps you've already tried

### Escalation Process

1. **Level 1**: Self-service troubleshooting using this guide
2. **Level 2**: Contact Krista support with detailed information
3. **Level 3**: Escalation to JIRA administrator or Atlassian support
4. **Level 4**: Engineering team involvement for complex technical issues

## Prevention Strategies

### Proactive Monitoring

- Set up alerts for authentication token expiration
- Monitor JIRA server performance and availability
- Regularly review and update permissions
- Keep extension and JIRA versions current

### Best Practices

- Document configuration and setup procedures
- Train team members on proper usage
- Implement proper error handling in workflows
- Regular testing of critical functionality

### Maintenance Schedule

- Monthly: Review permissions and access
- Quarterly: Update authentication credentials
- Annually: Review and update configuration
- As needed: Apply updates and patches

## See Also

- [JIRA Extension Configuration](pages/ExtensionConfiguration.md) - Setup and configuration
- [Authentication Guide](pages/Authentication.md) - OAuth 2.0 and API token setup
- [JQL Query Guide](pages/JQLQueryGuide.md) - Query syntax and examples
- Individual catalog request documentation for specific operation issues
