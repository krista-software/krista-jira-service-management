# Release Notes

## Krista Jira Service Management Extension 4.0.1 - Get Issue by Key Full Description Fix

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.125-sp1
- **Global Catalog Version** : GC-2026.08.5
- **Java Version** : 21
- **Release Date** : August 2026

### BugFix
[KE-3529]

- **Get Issue by Key — Full & Faithful Description Extraction**: Fixed the Description field being cut off and losing its formatting when a Jira issue description contained bullet lists, headings, or inline styling.
  - **Root Cause**: `JiraIssue.getDescription()` reconstructed the description by hand-parsing the raw Atlassian Document Format (ADF) JSON. It handled `orderedList` but not `bulletList` and did not handle `heading` blocks, so any unhandled block was silently dropped and everything after the first bullet list or heading was omitted. Inline nodes were also emitted one-per-line, breaking the layout.
  - **Fix**: The `Description` entity field is a `RichText` (HTML) field, so the description is now taken from Jira's own rendered HTML (`renderedFields.description`, already requested via `expand=renderedFields`) and returned as sanitized HTML (via jsoup). Because Jira renders the ADF itself, the original layout — paragraphs, bullet/ordered lists, bold, links, headings, tables — is preserved faithfully and renders natively in the RichText field instead of appearing as literal markup on one collapsed line. The hand-rolled ADF parser is retained as a fallback for responses without rendered HTML (and now also handles `bulletList`, `heading`, inline joining, and list markers).
  - **Impact**: "Get Issue by Key" now returns the complete, correctly-formatted description (e.g. CS-8821's intro paragraph followed by its bulleted team-member list) for all Issue entities (Issue, Bug, Epic, Story, Task, Subtask).

---

## Krista Jira Service Management Extension 4.0.0 - Atlassian Authentication SDK & MCP Support

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.125-sp1
- **Global Catalog Version** : GC-2026.05.3
- **Java Version** : 21
- **Release Date** : June 2026

### What's New

#### Authentication Tab — Atlassian SDK Integration
- Replaced the legacy attribute-screen OAuth flow with the **Atlassian Authentication SDK** (`atlassian-sdk:1.0.25`)
- New `JsmAuthUiResource` (`/sdk`) serves an embedded HTML authentication tab with three modes:
  - **Quick Setup (Krista Managed)** — pre-filled credentials; scopes shown as read-only chips
  - **OAuth 2.0 — Your Own Credentials** — client ID/secret entry with a toggleable scope panel (clickable chips)
  - **Basic Auth** — username, API token, and host URL
- Test Connection and Save Changes are handled by the SDK's `AbstractAtlassianAuthResource`; all credential events are audited automatically

#### MCP Support
- All 29 catalog requests marked with `tool=true` (MCP-exposed)
- `RestClientFactory.getInstance(invokeAsUser)` forces OAuth 2.0 (3LO) when `invokeAsUser=true` and OAuth credentials are present, triggering `MustAuthorizeException` for unauthenticated MCP users
- `onOAuthCallbackSuccess` in `JsmAuthUiResource` calls `authorizationListener.authorized()` — notifies the Krista runtime when a user completes the MCP per-user OAuth flow
- `MustAuthorizeException` is always re-thrown (never swallowed) across all 29 catalog request methods, including `getResultsForFetchIssuesByJQLQuery`, `getNotification`, and `getCustomFieldValue`

#### Full Audit Coverage
- All 29 catalog requests now emit `CATALOG_REQUEST_TRIGGERED`, `CATALOG_REQUEST_SUCCESS`, `CATALOG_REQUEST_FAILURE`, and auth-specific events via `JsmAuditService`
- `JsmAuthUiResource` passes the shared `JsmAuditService` audit store to the SDK parent — credential events (`CREDENTIALS_TEST_*`, `CREDENTIALS_SAVE_*`) appear on the same Audit Dashboard as catalog request events
- `JsmAuditResource` (`/sdk/audit`) serves the SDK-driven audit dashboard with credential health summary

#### Scope Documentation — 8 OAuth Scopes
- `JsmExtensionConfig` declares 8 `@OAuthScope` annotations mapping all 29 catalog requests to their required Jira/JSM API scopes:
  - `read:jira-user` → Get Users Account Id, Get User Account Information
  - `read:jira-work` → 9 read catalog requests
  - `write:jira-work` → 11 write catalog requests (including Update the priority of multiple issues)
  - `manage:jira-data-provider` → Get Notification
  - `manage:jira-project` → Get release summary, Assign Issue to Epic, Assign Child Issue to Epic
  - `read:servicedesk-request` → Get Request Type Field Value
  - `write:servicedesk-request` → Add Request Participants to Issue
  - `manage:servicedesk-customer` → Create User, Update User, Delete User

#### Documentation Updates
- Completely rewritten **Authentication** guide following the Outlook 4.0 format
- New sections: Krista Managed (Quick Setup), OAuth 2.0 scope panel with all 8 JSM scopes, MCP per-user auth flow, Token Management, Audit Dashboard, Security Best Practices

---

## Krista Jira Extension 1.0.6 - Documentation Improvements

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2026.2.4
- **Java Version** : 21
- 
### BugFix
[KE-3024]
- Store and retrieve OAuth refresh tokens using a client-level key, shared across users.


## Krista Jira Extension 1.0.5 - Documentation Improvements

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2026.2.3
- **Java Version** : 21

### Improvement
[KE-3013]
- **Documentation Enhancement**: Improved webhook authentication compatibility note with clearer explanation of Basic Auth and OAuth 2.0 support

---

## Krista Jira Extension 1.0.4 - Enhanced Last Modified Date Display

- **Developer Name** : Ramgopal Patidar
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2026.2.2
- **Java Version** : 21

### What's New

Catalog Request: Get Issue by Key
- Enhancement: Added seconds precision to Last Modified Date field display
- Previous Format: `Feb 12, 2026 7:38 PM`
- New Format: `Feb 12, 2026 7:38:51 PM`
- Impact: Provides more accurate timestamp information for audit and tracking purposes
- **Technical Details:**
 - Updated display format from `MMM d, yyyy h:mm a to MMM d, yyyy h:mm:ss a`
  - Backend already stored full precision (epoch milliseconds)

## Krista Jira Extension 1.0.3 - Get User Account Information & Get All Comments Enhancement

- **Developer Name** : Ramgopal Patidar
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2026.2.2
- **Java Version** : 21

### What's New

#### Last Modified Date Field - Get Issue by Key Enhancement (KE-2955)

- **New Field Added to Issue Entities**
    - Added "Last Modified Date" field to Issue, Bug, Epic, Story, Task, and Subtask entities
    - Displays the last time an issue was updated in Jira with date and time
    - Automatically populated from Jira API's `updated` field
    - Supports multiple timezone formats (ISO 8601, UTC/Zulu)
    - **Location**: All "Get Issue by Key" responses

#### Get User Account Information - New Catalog Request (KE-2962)

- **New Organization Catalog Request**
    - Added "Get User Account Information" catalog request to retrieve detailed user profile data
    - Search users by email address or username with exact match
    - Returns comprehensive user information including Account Id, Display Name, Email, and Application Keys
    - **Location**: Organization Area → Get User Account Information
    - **Documentation**: [Get User Account Information](/pages/GetUserAccountInformation.md)

- **Key Features**
    - **Flexible Search**: Search by email address or Jira username
    - **Complete User Profile**: Returns all user fields (Account Id, Email, Display Name, Account Type, etc.)
    - **Empty Result Handling**: Returns empty user object when no match found
    - **Privacy Compliant**: Respects user privacy settings for email visibility
    - **GDPR/CCPA Compliant**: No PII logging in application logs

- **Use Cases**
    - Validate user existence before issue assignment
    - Get Account Id for API calls requiring user identification
    - Verify user email addresses in automation workflows
    - Retrieve user information for integration with external systems
    - User lookup and verification in custom applications

- **Technical Implementation**
    - Added `getUserAccountInformation()` method to OrganizationArea
    - Added `getUserByEmailOrName()` method to UserDao
    - Direct DAO integration following jira-service-management patterns
    - Comprehensive error handling for authentication and permission issues
    - 10 integration tests with 100% pass rate

- **Input Parameters**
    - **Search Query** (Text, Optional): Email address or username to search

- **Output Parameters**
    - **User** (Entity): Complete user account information object with all fields

- **Error Handling**
    - Handles authentication failures with MustAuthorizeException
    - Returns empty user object for null/empty queries
    - Proper exception handling for system errors
    - No sensitive data logged (GDPR/CCPA compliant)

---

## Krista Jira Extension 1.0.2 - Get All Comments Date-Time Format Enhancement

- **Developer Name** : Simran Sethi
- **Krista Service APIs Java** : 1.0.118
- **Global Catalog Version** : GC-2026.1.6
- **Java Version** : 21

### What's New

#### Get All Comments - Enhanced Date-Time Display

- **Improved Date Created Format**
    - Changed "Date Created" field format from date-only to include time
    - New format: "Feb 6, 2026 12:57 PM" (Month Day, Year Hour:Minute AM/PM)
    - Uses Text field type to ensure both date and time are displayed in array structures
    - **Impact**: Users can now see the exact time when comments were created, not just the date

---

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