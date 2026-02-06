# Creating a Jira OAuth App

## Overview

This guide walks you through creating an OAuth 2.0 app in Jira Cloud to enable secure authentication for the Krista Jira
Extension.

## Prerequisites

- Jira Cloud instance (OAuth 2.0 apps not available for Jira Server/Data Center)
- Jira Administrator permissions
- Krista instance URL for redirect configuration

## Step-by-Step Guide

### Step 1: Access Atlassian Developer Console

1. Navigate to [Atlassian Developer Console](https://developer.atlassian.com/console/myapps/)
2. Log in with your Atlassian account (must have admin access)
3. You'll see the "Your apps" dashboard

![Developer Console](../images/CreatingJiraApp_console.png)

### Step 2: Create a New App

1. Click **Create** button (top right)
2. Select **OAuth 2.0 integration**
3. Enter app details:
    - **App name**: `Krista Jira Integration` (or your preferred name)
    - **Description**: `OAuth app for Krista workflow automation`
4. Click **Create**

![Create App](../images/CreatingJiraApp_create.png)

### Step 3: Configure OAuth 2.0 Settings

After creating the app, you'll be taken to the app settings page.

#### 3.1 Set Redirect URI

1. In the left sidebar, click **Authorization**
2. Under **OAuth 2.0 (3LO)**, click **Add** next to "Callback URL"
3. Enter your Krista redirect URI:
   ```
   https://your-krista-instance.com/oauth/callback
   ```
4. Click **Save changes**

> **Important**: The redirect URI must match exactly (including protocol and trailing slash)

![Redirect URI](../images/CreatingJiraApp_redirect.png)

#### 3.2 Configure Permissions (Scopes)

1. In the left sidebar, click **Permissions**
2. Click **Add** under "Jira API"
3. Select the following scopes:

**Required Scopes:**

- `read:jira-work` - Read issues, projects, and work data
- `write:jira-work` - Create and update issues
- `read:jira-user` - Read user information
- `write:jira-user` - Create and update users

**Optional Scopes (for Jira Service Management):**

- `read:servicedesk-request` - Read service desk requests
- `write:servicedesk-request` - Create and update service desk requests

4. Click **Save changes**

![Permissions](../images/CreatingJiraApp_permissions.png)

### Step 4: Get Client Credentials

1. In the left sidebar, click **Settings**
2. Under **Authentication details**, you'll find:
    - **Client ID**: Copy this value
    - **Secret**: Click **Generate secret**, then copy the value

> **Important**: The secret is shown only once. Store it securely!

![Client Credentials](../images/CreatingJiraApp_credentials.png)

**Example values:**

```
Client ID: ari:cloud:jira::app/12345678-1234-1234-1234-123456789abc
Client Secret: ABC123xyz789secretkeyhere
```

### Step 5: Enable the App

1. In the left sidebar, click **Distribution**
2. Under **Sharing**, select one of:
    - **This app is private** (recommended for internal use)
    - **Share with specific sites** (if you have multiple Jira instances)
    - **Make this app public** (only if you're distributing to others)
3. Click **Save changes**

### Step 6: Install the App to Your Jira Site

1. In the left sidebar, click **Distribution**
2. Under **Install app**, click **Install app**
3. Select your Jira site from the dropdown
4. Click **Install**
5. Approve the permissions when prompted

![Install App](../images/CreatingJiraApp_install.png)

### Step 7: Configure in Krista

Now that your OAuth app is created, configure it in Krista:

1. Go to Krista → **Extensions** → **Jira Extension** → **Configure**
2. Enter the following:
    - **Authentication Type**: `OAuth2`
    - **Client ID**: (paste from Step 4)
    - **Client Secret**: (paste from Step 4)
    - **Redirect URI**: (same as Step 3.1)
    - **Jira Site URL**: `https://yourcompany.atlassian.net`
3. Click **Save**
4. Click **Test Connection** to verify

## OAuth Flow Testing

### Test the Authorization Flow

1. In Krista, trigger a workflow that uses Jira
2. You should be redirected to Jira authorization page
3. Review the requested permissions
4. Click **Accept** to grant access
5. You'll be redirected back to Krista
6. The workflow should now execute successfully

### What Users See

When a user first uses the integration:

1. **Authorization Request**: "Krista Jira Integration wants to access your Jira account"
2. **Permissions List**: Shows all requested scopes
3. **Accept/Deny**: User chooses to grant or deny access
4. **Redirect**: User is sent back to Krista with authorization code

## Troubleshooting

### "Redirect URI mismatch" Error

**Problem**: After clicking "Accept", you see an error about redirect URI.

**Solutions:**

- ✅ Verify redirect URI in Jira app matches Krista exactly
- ✅ Check for HTTP vs HTTPS mismatch
- ✅ Ensure no extra trailing slashes
- ✅ Verify domain name is correct

### "Invalid client credentials" Error

**Problem**: Krista can't authenticate with Jira.

**Solutions:**

- ✅ Verify Client ID is copied correctly (no extra spaces)
- ✅ Regenerate Client Secret and update in Krista
- ✅ Ensure OAuth app is installed to your Jira site
- ✅ Check that app is not disabled

### "Insufficient permissions" Error

**Problem**: Some operations fail with permission errors.

**Solutions:**

- ✅ Verify all required scopes are added in Jira app
- ✅ Re-install the app to Jira site
- ✅ Have users re-authorize to get new scopes
- ✅ Check Jira user permissions (separate from OAuth scopes)

### App Not Appearing in Jira

**Problem**: Can't find the app in Jira settings.

**Solutions:**

- ✅ Ensure app is installed to the correct Jira site
- ✅ Check app distribution settings (must be enabled)
- ✅ Verify you have admin access to Jira
- ✅ Try reinstalling the app

## Security Best Practices

### Protect Your Client Secret

- ✅ Store securely (encrypted)
- ✅ Never commit to version control
- ✅ Rotate periodically (every 90 days)
- ✅ Limit access to authorized personnel only

### Scope Management

- ✅ Request only necessary scopes
- ✅ Review scopes periodically
- ✅ Remove unused scopes
- ✅ Document why each scope is needed

### App Distribution

- ✅ Keep app private for internal use
- ✅ Don't make public unless necessary
- ✅ Review installed sites regularly
- ✅ Revoke access for unused sites

## Managing the OAuth App

### Updating Scopes

If you need to add or remove scopes:

1. Go to Developer Console → Your App → **Permissions**
2. Add or remove scopes
3. Click **Save changes**
4. Users must re-authorize to get new permissions

### Rotating Client Secret

To rotate the secret:

1. Go to Developer Console → Your App → **Settings**
2. Click **Generate secret**
3. Copy the new secret
4. Update in Krista configuration
5. Old secret remains valid for 24 hours

### Monitoring Usage

1. Go to Developer Console → Your App → **Analytics**
2. View:
    - Number of active users
    - API call volume
    - Error rates
    - Token refresh patterns

### Revoking Access

To revoke all access:

1. Go to Developer Console → Your App → **Distribution**
2. Click **Uninstall** for each Jira site
3. Or delete the app entirely

## Next Steps

After creating your OAuth app:

1. ✅ Configure the extension in Krista ([Extension Configuration](/pages/ExtensionConfiguration.md))
2. ✅ Test the OAuth flow with a simple workflow
3. ✅ Review [Authentication](/pages/Authentication.md) for advanced scenarios
4. ✅ Set up [Webhook Settings](/pages/WebhookSettings.md) for real-time updates

## Additional Resources

- [Atlassian OAuth 2.0 Documentation](https://developer.atlassian.com/cloud/jira/platform/oauth-2-3lo-apps/)
- [Jira REST API Scopes](https://developer.atlassian.com/cloud/jira/platform/scopes-for-oauth-2-3LO-and-forge-apps/)
- [Authentication Guide](/pages/Authentication.md)
- [Extension Configuration](/pages/ExtensionConfiguration.md)

