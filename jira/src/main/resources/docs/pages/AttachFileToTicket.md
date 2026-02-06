# Attach File to Ticket

## Overview

Attaches one or more files to an existing Jira ticket/issue.

## Request Details

- **Area**: Organization
- **Type**: CHANGE_SYSTEM
- **Retry Support**:  Yes - If ticket ID validation fails, you can re-enter the ticket ID

## Input Parameters

| Parameter Name | Type       | Required | Description                     | Example                      |
|----------------|------------|----------|---------------------------------|------------------------------|
| Ticket Id      | Identifier | Yes      | The key or ID of the Jira issue | `"PROJ-123"`, `"SUP-456"`    |
| File           | File       | Yes      | One or more files to attach     | PDF, images, documents, etc. |

### Parameter Details

#### Ticket Id

- Must be a valid, existing issue key
- Format: PROJECT_KEY-NUMBER (e.g., "PROJ-123")
- Case-sensitive
- Validation: Checks if issue exists before attaching files

#### File

- Supports multiple files in a single request
- Maximum file size depends on Jira configuration (typically 10-100 MB)
- Supported formats: All file types supported by Jira
- Files are uploaded to Krista Media Client first, then attached to the issue

## Output Parameters

| Parameter Name | Type   | Description                                            |
|----------------|--------|--------------------------------------------------------|
| Upload Status  | Switch | Boolean indicating if files were successfully attached |

## Validation Rules

| Validation       | Error Message                                 | Resolution                                               |
|------------------|-----------------------------------------------|----------------------------------------------------------|
| Ticket ID exists | "Provided ticket id is not valid: {ticketId}" | Verify the ticket ID exists and you have access to it    |
| File processing  | "Unable to process the file {fileName}"       | Check file format and size, ensure file is not corrupted |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid ticket ID or file processing issues

**Common Scenarios**:

- Ticket ID doesn't exist
- Ticket ID format is incorrect
- File cannot be processed

**Error Messages**:

- `"Provided ticket id is not valid: {ticketId}"`
- `"Unable to process the file {fileName}"`

**Resolution**:

1. Verify ticket ID is correct and exists
2. Check file is not corrupted
3. Ensure file size is within limits
4. Verify file format is supported

### System Errors (SYSTEM_ERROR)

**Cause**: System-level failures during file upload

**Error Message**: `"Error occurred while attaching file to ticket"`

**Resolution**:

1. Check network connectivity
2. Verify Jira instance is accessible
3. Check Jira attachment settings
4. Review file size limits in Jira configuration

## Usage Examples

### Example 1: Attach Single File

**Input**:

```
Ticket Id: "PROJ-123"
File: screenshot.png
```

**Output**:

```json
{
  "Upload Status": true
}
```

**Result**: File attached successfully to PROJ-123

### Example 2: Attach Multiple Files

**Input**:

```
Ticket Id: "SUP-456"
File: [error_log.txt, screenshot1.png, screenshot2.png]
```

**Output**:

```json
{
  "Upload Status": true
}
```

**Result**: All three files attached successfully

### Example 3: Retry Scenario - Invalid Ticket ID

**Initial Input**:

```
Ticket Id: "INVALID-999"
File: document.pdf
```

**Error Response**:

```
Error: "Provided ticket id is not valid: INVALID-999"
Prompt: "Would you like to re-enter the ticket ID?"
```

**Corrected Input**:

```
Ticket Id: "PROJ-123"
(file remains the same)
```

**Result**: File attached successfully after correction

## Business Rules

1. **Attachment Permissions**: User must have "Create Attachments" permission on the issue
2. **File Size Limits**: Files must be within Jira's configured attachment size limit
3. **Multiple Files**: Multiple files can be attached in a single request
4. **File Processing**: Files are processed through Krista Media Client before upload
5. **Attachment History**: All attachments are logged in the issue's activity history

## Limitations

1. **File Size**: Limited by Jira's attachment size configuration (typically 10-100 MB per file)
2. **Total Attachments**: Limited by Jira's total attachment size per issue
3. **File Types**: Some file types may be blocked by Jira security settings
4. **Processing**: Large files may take longer to process and upload

## Best Practices

### 1. Verify Ticket ID Before Attaching

Always verify the ticket ID is correct before attaching files to avoid uploading to the wrong issue.

### 2. Use Descriptive File Names

Use clear, descriptive file names that indicate the content (e.g., "error_log_2024-01-15.txt" instead of "log.txt").

### 3. Compress Large Files

For large files, consider compressing them to reduce upload time and storage space.

### 4. Batch Multiple Files

When attaching multiple related files, attach them in a single request rather than multiple separate requests.

### 5. Check File Size Limits

Verify file sizes are within Jira's limits before attempting to attach.

## Common Use Cases

### 1. Bug Report with Screenshots

```
Scenario: User reports a bug and provides screenshots
Action: Attach screenshots to the bug ticket
Result: Screenshots available for developers to review
```

### 2. Log File Attachment

```
Scenario: Support team needs error logs for troubleshooting
Action: Attach log files to support ticket
Result: Logs available for analysis
```

### 3. Documentation Upload

```
Scenario: Attach requirements document to user story
Action: Upload PDF or Word document
Result: Document linked to story for reference
```

## Related Catalog Requests

- [Create an Issue](/pages/CreateAnIssue.md) - Create the issue first, then attach files
- [Get Issues](/pages/GetIssues.md) - Query issues to find the ticket ID

## Technical Implementation

### Helper Class

- **Class**: OrganizationAreaSubCatalogRequests
- **Package**: app.krista.extensions.devsecops.development.jira.catalog
- **Validation**: ValidationOrchestrator validates ticket ID
- **Service**: IssueDao.attachFileToTicket()

### File Processing Flow

1. Receives file(s) from Krista
2. Converts Krista File objects to Java File objects via KristaMediaClient
3. Validates ticket ID exists
4. Uploads each file to Jira via REST API
5. Returns upload status

## Troubleshooting

### Issue: "Provided ticket id is not valid"

**Cause**: Ticket doesn't exist or user doesn't have access
**Solution**: Verify ticket ID and user permissions

### Issue: "Unable to process the file"

**Cause**: File is corrupted or unsupported format
**Solution**: Check file integrity and format

### Issue: File upload fails silently

**Cause**: File size exceeds Jira limits
**Solution**: Check Jira attachment size limits and compress file if needed

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md)
- [Create an Issue](/pages/CreateAnIssue.md)
- [Jira REST API - Add Attachment](https://developer.atlassian.com/cloud/jira/platform/rest/v3/api-group-issue-attachments/#api-rest-api-3-issue-issueidorkey-attachments-post)

