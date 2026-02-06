# Release Notes

## Krista Jira Extension 1.0.1 - OAuth Authentication Fix & Test Coverage Improvements

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2026.1.5
- **Java Version** : 21

### What's New

#### OAuth 2.0 Authentication Bug Fix

- **Fixed OAuth Popup Not Opening**
    - Resolved issue where Atlassian OAuth login popup was not appearing when entering Client ID and Client Secret
    - Modified `validateAttributes()` method to properly separate format validation from connection testing
    - Ensured `MustAuthorizeException` propagates correctly to trigger OAuth authorization flow
    - OAuth credentials now properly validated before attempting connection
    - **Impact**: Users can now successfully authenticate using OAuth 2.0 Authorization Code Grant flow

- **Enhanced OAuth Credentials Validation**
    - Added format validation for Client ID (alphanumeric, hyphens, underscores only)
    - Added length validation for Client Secret (warns if less than 32 characters)
    - Improved error messages for invalid OAuth credential formats
    - Better detection of partial OAuth credentials (missing Client ID or Client Secret)

#### Test Coverage Improvements

- **Comprehensive Unit Test Coverage**
    - Added 65 unit tests for `JiraAttributes` class
    - Achieved 85%+ code coverage for core functionality
    - All public methods now have comprehensive test coverage

- **Integration Test Coverage**
    - Added 43 integration tests for OAuth validation
    - Added 8 tests for connection validation logic
    - Total: 108 tests across unit and integration test suites

- **Test Documentation**
    - Created detailed test coverage documentation
    - Added fix documentation for coverage issues
    - Documented all test scenarios and expected behaviors

---

## Krista Jira Extension 1.0.0 - Critical Bug Fixes, JSM Support & Enhanced Error Handling

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2026.1.4
- **Java Version** : 21

### What's New

#### Jira Service Management (JSM) Support Documentation

- **Comprehensive JSM Feature Documentation**
    - Documented existing Request Type field support in Issue entity
    - Documented Internal/Public comment visibility functionality
    - Created detailed usage examples and API reference
    - Added troubleshooting guide for JSM-specific issues
    - **Documentation**: [Jira Service Management Support](/pages/JiraServiceManagementSupport.md)

- **Request Type Field**
    - Automatically populated from JSM API responses
    - Supports standard `requesttype` field and `customfield_10010`
    - Searchable in entity searches
    - Can be set when creating issues

- **Internal/Public Comment Visibility**
    - Dropdown selection in "Add a comment" catalog request
    - Uses JSM-specific `sd.public.comment` property
    - Internal comments visible only to agents
    - Public comments visible to customers
    - Default behavior: Public if not specified

---