# AI Assistant Tab

## Overview

The **AI Assistant** tab enables Krista's AI agent to interact with your Jira Service Management instance using natural language. It is powered by the Krista MCP (Model Context Protocol) server and exposes all 29 JSM catalog requests as tools the AI can invoke on your behalf.

The tab has three main sections:

1. **Setup** — Shows connection status and lets you configure an AI provider (Claude, OpenAI, or Gemini) for the natural language query pipeline.
2. **Query & AI Settings** — Provides a query playground for testing JQL queries directly and a natural language interface where you type a question in plain English; the AI translates it into a JQL query, fetches results from Jira, and returns a formatted table or a direct answer.
3. **Help & Troubleshooting** — Shows MCP server configuration snippets for popular AI assistants (Claude Desktop, Cursor, VS Code, Windsurf) and troubleshooting tips.

---

## Prerequisites

Before using the AI Assistant tab, ensure:

- The extension is installed in your Krista workspace
- Authentication is configured and **Test Connection** passes in the **Authentication** tab
- Your authentication mode supports per-user MCP (see [Supported Auth Modes](#supported-auth-modes))

---

## Setup Panel

### Connection Status

The Setup panel displays the current connection status based on the credentials saved in the Authentication tab:

| Status | Meaning |
|--------|---------|
| **Connected** | Credentials are valid and the extension can query Jira Service Management |
| **Not Connected** | No credentials configured or credentials are invalid |

The panel also shows:

- **Authentication method** — Quick Setup, OAuth 2.0 (User Auth), OAuth 2.0 (Server to Server), or Basic Auth
- **Site URL** — The Jira instance being connected to
- **User email** — The email address of the authenticated user (for OAuth modes)

### AI Provider Configuration

To use the **natural language query** feature (Ask Agent), you must configure an AI provider. The AI provider is used to translate natural language questions into JQL queries and to extract answers from search results.

| Provider | Model | API Key Source |
|----------|-------|----------------|
| **Claude** (Anthropic) | claude-sonnet-4-20250514 | [console.anthropic.com](https://console.anthropic.com/) |
| **OpenAI** | gpt-4o | [platform.openai.com](https://platform.openai.com/) |
| **Gemini** (Google) | gemini-2.0-flash | [aistudio.google.com](https://aistudio.google.com/) |

To configure:

1. Select your preferred AI provider from the dropdown
2. Enter your API key
3. Click **Validate** to verify the key works
4. The key is stored securely per user — each user can configure their own provider

> **Note**: The AI provider is only needed for the natural language query pipeline. Direct JQL queries in the playground do not require an AI provider.

---

## Query Playground

The query playground lets you test JQL queries directly against your Jira Service Management instance:

1. Enter a JQL query in the text field (e.g., `issuetype = Incident AND status != "Done"`)
2. Click **Search** to execute the query
3. Results are displayed in a table with the following columns:

| Column   | Display Type | Description                        |
|----------|--------------|------------------------------------|
| Key      | Link         | Issue key, links to the JSM issue  |
| Type     | Pill         | Issue type (Incident, Bug, Task…)  |
| Summary  | Text         | Issue summary / title              |
| Status   | Pill         | Current workflow status            |
| Priority | Pill         | Priority level (High, Medium…)     |
| Assignee | Text         | Assigned user's display name       |

### Example JQL Queries

| Query | Description |
|-------|-------------|
| `issuetype = Incident AND status != "Done"` | All open incidents |
| `assignee = currentUser()` | Issues assigned to you |
| `project = KE AND issuetype = Bug` | Bugs in project KE |
| `priority = High AND status = "In Progress"` | High-priority in-progress issues |
| `created >= -7d AND issuetype = "Service Request"` | Service requests from the last 7 days |
| `reporter = currentUser() ORDER BY created DESC` | Your reported issues, newest first |

---

## Natural Language Queries (Ask Agent)

When an AI provider is configured, you can type questions in plain English:

```
User types a natural language question
  → AI generates a JQL query from the question
  → Extension executes JQL search against Jira Service Management
  → Results are returned as a formatted table or a direct answer
```

For **list questions** (e.g., "Show all open incidents"), the AI returns a results table.

For **specific questions** (e.g., "What is the priority of JSM-100?"), the AI returns a single concise answer.

---

## MCP Server Configuration

The Help panel provides ready-to-use configuration snippets for connecting popular AI assistants to your Jira Service Management instance via the Krista MCP server:

### Supported AI Assistants

| Assistant | Configuration File |
|-----------|-------------------|
| **Claude Desktop** | `claude_desktop_config.json` |
| **Cursor** | `.cursor/mcp.json` |
| **VS Code (Copilot)** | `.vscode/mcp.json` |
| **Windsurf** | `~/.windsurf/mcp.json` |

Each snippet includes the MCP server URL derived from your extension's base URL and JAXRS path. Copy the snippet into the corresponding configuration file to enable your AI assistant to search JSM data.

---

## Per-User Authentication (MCP OAuth Flow)

Each user who interacts with the AI Assistant authenticates independently with their own Jira account. This ensures that:

- The AI acts on behalf of the individual user, not a shared service account
- Permissions are correctly enforced per user
- The audit trail in the **JSM Audit** tab reflects the correct user for every AI-triggered request

### First-Time Authorization

When a user sends their first question through the AI Assistant:

1. The extension detects no existing token for that user
2. Krista presents an **authorization link** in the AI chat
3. The user clicks the link — a Jira sign-in popup opens
4. The user signs in and grants consent to the OAuth app
5. After the popup closes, the token is stored under the user's Krista contact ID
6. The AI retries the request automatically — this time the token is found and the query succeeds

Subsequent questions from the same user proceed without interruption; tokens are refreshed automatically.

### Supported Auth Modes

| Auth Mode | MCP Per-User Support |
|-----------|----------------------|
| Quick Setup | Yes — per-user OAuth popup |
| OAuth 2.0 — User Authentication | Yes — per-user OAuth popup |
| OAuth 2.0 — Server to Server | No — single shared service account |
| Basic Auth | No — single shared API token |

> **Recommendation**: Use **Quick Setup** or **OAuth 2.0 — User Authentication** to enable full per-user AI assistant access. Server to Server and Basic Auth modes will use a single shared credential for all AI queries.

---

## Example Queries

The following examples show the kinds of questions the AI Assistant understands:

| Question | What the AI does |
|----------|-----------------|
| `Show all open incidents` | JQL: `issuetype = Incident AND status != "Done"` → returns list |
| `What tickets are assigned to me?` | JQL: `assignee = currentUser()` → returns list |
| `What is the priority of JSM-100?` | JQL: `key = JSM-100` → returns `priority` field value |
| `Who is assigned to JSM-200?` | JQL: `key = JSM-200` → returns `assignee` field value |
| `Show all bugs in project KE` | JQL: `issuetype = Bug AND project = KE` → returns list |
| `What is the status of the login issue?` | JQL derived from question context → returns `status` |

> **Current user context**: When your question mentions "me" or "my", the AI uses `currentUser()` in JQL — no email or name is required.

---

## Ask Agent Catalog Request

The AI Assistant tab exposes an **Ask Agent** catalog request that can also be invoked directly from Krista workflows:

| Property | Value |
|----------|-------|
| **Name** | Ask Agent |
| **Type** | Query System |
| **MCP Exposed** | Yes (`tool=true`) |
| **Input** | `Query` (Paragraph, required) — your natural language question |
| **Output** | `Response` (FreeForm array) — matching JSM issues |

This catalog request is available in the **Development** domain under the **Dev Sec Ops** ecosystem, alongside all other JSM catalog requests.

---

## Audit Visibility

All AI-triggered catalog requests are automatically recorded in the **JSM Audit** tab:

| Event | When |
|-------|------|
| `CATALOG_REQUEST_TRIGGERED` | AI sends a query to Jira |
| `CATALOG_REQUEST_SUCCESS` | Query returns results successfully |
| `CATALOG_REQUEST_FAILURE` | Query fails (permissions, network, etc.) |

To review AI activity, open the **JSM Audit** tab and filter for `Ask Agent` events.

---

## Troubleshooting

### Authorization Popup Appears on Every Question

**Cause**: The per-user OAuth token was not saved, or the token has expired.

**Fix**:
1. Complete the authorization popup when prompted — wait for the "Authentication successful" message before closing it
2. Verify the callback URL in your Atlassian OAuth app matches your Extension Base URL + `/jiraextn/callback`
3. If the popup keeps appearing, check the **JSM Audit** tab for `AUTH_FAILURE` events

### AI Returns No Results

**Cause**: The JQL query generated from the question returned no matching issues.

**Fix**:
1. Rephrase the question to be more specific (e.g., include the project key or issue type)
2. Verify that the authenticated user has permission to view the issues
3. Check the **JSM Audit** tab for `CATALOG_REQUEST_FAILURE` events

### AI Provider Key Validation Fails

**Cause**: The API key is invalid, expired, or has insufficient quota.

**Fix**:
1. Verify the API key is correct — re-copy it from your provider's dashboard
2. Check that the API key has available quota/credits
3. Ensure the key is for the correct provider (e.g., an Anthropic key for the Claude provider)

### AI Returns "Not authorized" or Permission Error

**Cause**: The authenticated user does not have the required Jira permissions, or the OAuth scopes are insufficient.

**Fix**:
1. Ensure the OAuth app has the `read:jira-work` scope (required for all issue queries)
2. Verify the user account has at least **Browse Projects** permission in Jira
3. For service desk queries, ensure `read:servicedesk-request` is also granted

### MCP Not Available (Server to Server or Basic Auth)

Per-user AI queries are not supported in Server to Server or Basic Auth modes. All AI queries will run as the single configured service account or API token user. To enable per-user AI access, switch to **Quick Setup** or **OAuth 2.0 — User Authentication** in the Authentication tab.

---

## See Also

- [Authentication](/pages/Authentication.md) — Configure credentials before using the AI Assistant
- [MCP Authentication (Per-User OAuth)](/pages/Authentication.md#mcp-authentication-per-user-oauth) — Detailed per-user OAuth flow
- [JSM Audit Dashboard](/pages/Authentication.md#audit-dashboard) — Monitor AI-triggered activity
- [JQL Query Guide](/pages/JQLQueryGuide.md) — Reference for the query language the AI uses
