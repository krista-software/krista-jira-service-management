[comment]: <> (Home page)

# JIRA Service Management Extension

## Overview

The JIRA Service Management Extension provides comprehensive integration between Krista and Atlassian Jira, enabling
seamless project management, issue tracking, and team collaboration workflows. This extension supports advanced features
including JQL queries, real-time webhooks, bulk operations, and sophisticated user management capabilities.

## What's New in Version 1.0.0

### New Features

- **Get All Comments** - Retrieve all comments from a Jira issue with comprehensive details
    - Fetch comment body, author information, and creation timestamps
    - Returns complete comment history for audit and tracking
    - Support for both public and internal comments
    - Perfect for issue analysis, collaboration tracking, and compliance reporting

### Code Quality & Testing

- **JaCoCo Integration** - Automatic code coverage reporting (v0.8.11)
    - XML and HTML coverage reports generated after each test run
    - Reports available at `build/reports/jacoco/test/`
- **SonarQube Integration** - Static code analysis support (v6.3.1.5724)
    - Pre-configured for Jira extension analysis
    - Ready for CI/CD pipeline integration
- **Comprehensive Test Suite** - 14 new integration tests with 100% pass rate
    - UserServiceTest: 6 tests covering service layer
    - OrganizationAreaTest: 8 tests covering catalog layer

### Documentation

- Complete documentation for Get All Comments request
- Updated release notes with version 1.0.0 details
- Enhanced sidebar navigation

## Key Features

- **Complete Issue Lifecycle Management**: Create, read, update, and track issues throughout their lifecycle
- **Advanced Search & Filtering**: Powerful JQL (Jira Query Language) support for complex queries
- **User & Permission Management**: Comprehensive user administration and group management
- **Real-time Notifications**: Webhook-based event notifications for instant updates
- **File & Attachment Management**: Upload, download, and manage file attachments
- **Bulk Operations**: Efficient batch processing for multiple issues
- **Release Management**: Track and analyze release progress with detailed summaries
- **Flexible Authentication**: Support for OAuth 2.0 and API token authentication
- **Retry Mechanism**: Built-in error handling with user-friendly retry workflows

## Quick Start Guide

1. **Prerequisites**: Ensure you have Jira administrator access and Krista workspace permissions
2. **Authentication Setup**: Configure OAuth 2.0 credentials or API tokens
3. **Extension Configuration**: Set up the extension with your Jira instance details
4. **Connection Validation**: Test and validate your Jira connection
5. **Start Managing**: Begin creating and managing Jira issues through Krista

## Documentation Structure

### Getting Started

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Complete setup and configuration guide
- [Authentication](/pages/Authentication.md) - OAuth 2.0 and API token authentication
- [Creating Jira App](/pages/GettingOAuth2Credentials.md) - Jira application setup for OAuth

### Organization Management

- [Attach File to Ticket](/pages/AttachFileToTicket.md) - Upload files to Jira tickets
- [Create an Issue](/pages/CreateAnIssue.md) - Create new Jira issues
- [Report a Bug](/pages/ReportABug.md) - Specialized bug reporting workflow
- [Create User](/pages/CreateUser.md) - Add new users to Jira workspace
- [Update User](/pages/UpdateUser.md) - Manage user groups and permissions
- [Delete User](/pages/DeleteUser.md) - Remove users from Jira workspace
- [Get Users Account Id](/pages/GetUsersAccountId.md) - Retrieve user account identifiers
- [Get User Account Information](/pages/GetUserAccountInformation.md) - Retrieve detailed user account information by
  email or username

### Issue Management & Workflows

- [Update Status of Multiple Issues](/pages/UpdateStatusOfMultipleIssues.md) - Bulk status updates
- [Get User Issues](/pages/GetUserIssues.md) - Retrieve user-assigned issues
- [Fetch by JQL Query](/pages/FetchByJQLQuery.md) - Advanced JQL-based searches
- [Get Results for Fetch Issues by JQL Query](/pages/GetResultsForFetchIssuesByJQLQuery.md) - Retrieve JQL query results
- [Get My Issues by Given Statuses](/pages/GetMyIssuesByGivenStatuses.md) - Personal issue tracking
- [Get Issues](/pages/GetIssues.md) - Advanced issue filtering and retrieval
- [Get User Issue by Issue Type](/pages/GetUserIssueByIssueType.md) - Type-based issue analysis
- [Update an Issue](/pages/UpdateAnIssue.md) - Comprehensive issue updates
- [Assign Issue to Epic](/pages/AssignIssueToEpic.md) - Epic-issue relationship management
- [Get Issue By Key](/pages/GetIssueByKey.md) - Retrieve specific issue details
- [Add a Comment](/pages/AddAComment.md) - Add comments to issues
- [Get All Comments](/pages/GetAllComments.md) - Retrieve all comments from an issue
- [Get Notification](/pages/GetNotification.md) - Real-time webhook notifications

### Release Management

- [Get Release Summary](/pages/GetReleaseSummary.md) - Comprehensive release analytics and reporting

### Advanced Topics

- [Troubleshooting](/pages/Troubleshooting.md) - Common issues and solutions
- [Webhook Configuration](/pages/WebhookSettings.md) - Real-time notification setup
- [JQL Query Guide](/pages/JQLQueryGuide.md) - Jira Query Language reference

## Support & Resources

- **Extension Version**: 1.0.0
- **Jira API Version**: REST API v2/v3
- **Java Version**: 21
- **Gradle Version**: 8.5
- **Authentication Methods**: OAuth 2.0, API Token
- **Webhook Support**: ✅ Yes
- **Retry Support**: ✅ Yes (with user-friendly error handling)
- **Real-time Updates**: ✅ Yes (via webhooks)
- **Code Coverage**: ✅ Yes (JaCoCo integration)
- **Code Quality**: ✅ Yes (SonarQube integration)

## Technical Specifications

- **Supported Jira Versions**: Jira Cloud, Jira Server 8.0+
- **Required Permissions**: Project administration, User management
- **Rate Limits**: Follows Jira API rate limiting guidelines
- **Security**: OAuth 2.0 with PKCE, API token encryption
- **Monitoring**: Comprehensive telemetry and performance tracking
- **Testing**: JUnit 4/5 with Mockito, 100% test pass rate
- **Code Coverage**: JaCoCo v0.8.11 with automatic report generation
- **Code Quality**: SonarQube v6.3.1.5724 integration

## Testing & Quality Assurance

### Running Tests

```bash
# Run all tests with coverage
./gradlew test

# Generate coverage report
./gradlew jacocoTestReport

# View HTML coverage report
open build/reports/jacoco/test/html/index.html

# Run SonarQube analysis
./gradlew sonar -Dsonar.host.url=<URL> -Dsonar.login=<TOKEN>
```

### Test Coverage

- **Total Tests**: 14 integration tests for user account operations
- **Pass Rate**: 100% (14/14 passing)
- **Coverage Reports**: XML (SonarQube) and HTML (local viewing)
- **Test Quality**: AAA pattern, proper mocking, comprehensive assertions

## Release Notes

For detailed information about the latest changes, improvements, and bug fixes, see
the [Release Notes](/pages/releaseNotes.md).

**Latest Release**: Version 2.0.23 - Feature Enhancement & Testing (January 30, 2026)

---

For technical support, feature requests, or questions about this extension, please contact the Krista Development Team.

