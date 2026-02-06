/*
 * JIRA SERVICE MANAGEMENT Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.devsecops.development.jira;

import app.krista.extension.executor.Invoker;
import app.krista.extension.util.InvokerAttributes;
import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

/**
 * Manages Jira authentication credentials and validates connections.
 *
 * <p>This service supports two authentication methods:</p>
 * <ul>
 *   <li><b>Basic Authentication</b>: Using Admin Auth Token, User Name, and Host URL</li>
 *   <li><b>OAuth 2.0</b>: Using Client ID and Client Secret (Authorization Code Grant flow)</li>
 * </ul>
 *
 * <p>The class validates credential formats, checks for partial credentials,
 * and tests connections before allowing usage. It ensures proper propagation
 * of {@link app.krista.extension.authorization.MustAuthorizeException} to trigger
 * OAuth authorization flows.</p>
 *
 * @author Simran Sethi
 * @since 1.0.1
 * @see JiraService
 * @see AuthRestClient
 * @see OAuth2RestClient
 */
@Service
public class JiraAttributes {

    public static final String HOST_URL = "Host Url";
    public static final String USER_NAME = "Admin User Name";
    public static final String AUTH_TOKEN = "Admin Auth Token";
    public static final String CLIENT_ID = "Client Id";
    public static final String CLIENT_SECRET = "Client Secret";
    private String hostUrl;
    private String userName;
    private String authToken;
    private String clientId;
    private String clientSecret;
    private final Invoker invoker;
    private final RequestContext requestContext;
    private final SessionManager sessionManager;
    private final AuthorizationContext authorizationContext;

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraAttributes.class);

    /** Compiled regex pattern for email validation (performance optimization) */
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    /** Minimum required length for OAuth client secret */
    private static final int MIN_CLIENT_SECRET_LENGTH = 32;

    /**
     * Constructs a new JiraAttributes instance with dependency injection.
     * Initializes credentials from the invoker's attributes.
     *
     * @param invoker the invoker containing extension attributes
     * @param requestContext the request context for the current operation
     * @param sessionManager manages OAuth session state
     * @param authorizationContext provides authorization context
     */
    @Inject
    public JiraAttributes(Invoker invoker, RequestContext requestContext, SessionManager sessionManager,
                          AuthorizationContext authorizationContext) {
        this.hostUrl = (String) invoker.getAttributes().get(HOST_URL);
        this.userName = (String) invoker.getAttributes().get(USER_NAME);
        this.authToken = (String) invoker.getAttributes().get(AUTH_TOKEN);
        this.clientId = (String) invoker.getAttributes().get(CLIENT_ID);
        this.clientSecret = (String) invoker.getAttributes().get(CLIENT_SECRET);
        this.invoker = invoker;
        this.requestContext = requestContext;
        this.sessionManager = sessionManager;
        this.authorizationContext = authorizationContext;
    }

    /**
     * Sets the authentication attributes from the provided map.
     * Updates all credential fields (both basic and OAuth).
     *
     * @param attributes map containing credential attributes
     */
    public void setAttributes(Map<String, Object> attributes) {
        hostUrl = (String) attributes.get(HOST_URL);
        userName = (String) attributes.get(USER_NAME);
        authToken = (String) attributes.get(AUTH_TOKEN);
        clientId = (String) attributes.get(CLIENT_ID);
        clientSecret = (String) attributes.get(CLIENT_SECRET);
    }

    /**
     * Gets the Jira host URL.
     *
     * @return the host URL, or null if not set
     */
    public String getHostUrl() {
        return hostUrl;
    }

    /**
     * Gets the admin user name (email address).
     *
     * @return the user name, or null if not set
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Gets the admin authentication token.
     *
     * @return the auth token, or null if not set
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Gets the OAuth 2.0 client ID.
     *
     * @return the client ID, or null if not set
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the OAuth 2.0 client secret.
     *
     * @return the client secret, or null if not set
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Validates if the provided string is a valid email address.
     * Uses a compiled regex pattern for performance.
     *
     * @param userName the email address to validate
     * @return true if the string is a valid email format, false otherwise
     */
    private boolean isValidEmailAddress(String userName) {
        return EMAIL_PATTERN.matcher(userName).matches();
    }

    /**
     * Checks if an attribute is present and non-blank in the attributes map.
     * Helper method to eliminate code duplication in credential checking.
     *
     * @param attributes the attributes map to check
     * @param key the attribute key to look up
     * @return true if the attribute exists and is not blank, false otherwise
     */
    private boolean isAttributeNonBlank(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value != null && !value.toString().isBlank();
    }

    /**
     * Checks if all basic authentication credentials are present in the attributes map.
     * Basic credentials include: Auth Token, User Name, and Host URL.
     *
     * @param attributes the attributes map to check
     * @return true if all three basic credentials are present and non-blank, false otherwise
     */
    public boolean basicCredentialsPresentInAttributes(Map<String, Object> attributes) {
        return isAttributeNonBlank(attributes, AUTH_TOKEN) &&
               isAttributeNonBlank(attributes, USER_NAME) &&
               isAttributeNonBlank(attributes, HOST_URL);
    }

    /**
     * Checks if all OAuth 2.0 credentials are present in the attributes map.
     * OAuth credentials include: Client ID and Client Secret.
     *
     * @param attributes the attributes map to check
     * @return true if both OAuth credentials are present and non-blank, false otherwise
     */
    public boolean oAuthCredentialsPresentInAttributes(Map<String, Object> attributes) {
        return isAttributeNonBlank(attributes, CLIENT_ID) &&
               isAttributeNonBlank(attributes, CLIENT_SECRET);
    }

    /**
     * Validates the provided authentication attributes and tests the connection.
     *
     * <p>This method performs the following validations:</p>
     * <ol>
     *   <li>Ensures either basic or OAuth credentials are provided</li>
     *   <li>Validates email format for user name (if basic auth)</li>
     *   <li>Validates OAuth credential format (if OAuth)</li>
     *   <li>Tests the connection with the provided credentials</li>
     * </ol>
     *
     * @param attributes map containing authentication credentials
     * @throws IllegalArgumentException if credentials are missing, invalid, or connection fails
     * @throws app.krista.extension.authorization.MustAuthorizeException if OAuth authorization is required
     */
    public void validateAttributes(Map<String, Object> attributes) {
        if (!basicCredentialsPresentInAttributes(attributes) && (!oAuthCredentialsPresentInAttributes(attributes)))
            throw new IllegalArgumentException("Either (Auth token, user name, host url) or (Client ID, Client secret) must be provided for authentication");

        // Validate email format for basic auth credentials
        if (basicCredentialsPresentInAttributes(attributes)) {
            String userNameFromAttributes = (String) attributes.get(USER_NAME);
            if (userNameFromAttributes != null && !isValidEmailAddress(userNameFromAttributes)) {
                throw new IllegalArgumentException("User name is not a valid email address");
            }
        }

        // Validate OAuth credential format
        if (oAuthCredentialsPresentInAttributes(attributes)) {
            validateOAuthCredentialsFormat(attributes);
        }

        testConnection(attributes);
    }

    /**
     * Validates the format of OAuth 2.0 credentials.
     *
     * <p>Validation rules:</p>
     * <ul>
     *   <li>Client ID must contain only alphanumeric characters, hyphens, and underscores</li>
     *   <li>Client Secret should be at least 32 characters (warning if shorter)</li>
     * </ul>
     *
     * @param attributes map containing OAuth credentials
     * @throws IllegalArgumentException if Client ID format is invalid
     */
    private void validateOAuthCredentialsFormat(Map<String, Object> attributes) {
        String testClientId = (String) attributes.get(CLIENT_ID);
        String testClientSecret = (String) attributes.get(CLIENT_SECRET);

        // Validate Client ID format
        if (!testClientId.matches("^[a-zA-Z0-9_-]+$")) {
            LOGGER.error("Validation FAILED: Client ID contains invalid characters");
            throw new IllegalArgumentException("Client ID format is invalid. It should contain only alphanumeric characters, hyphens, and underscores.");
        }

        // Validate Client Secret length
        if (testClientSecret.length() < MIN_CLIENT_SECRET_LENGTH) {
            LOGGER.debug("Please verify you copied the complete Client Secret from Atlassian Developer Console.");
        }
        LOGGER.info("OAuth credentials format validation passed");
    }

    /**
     * Tests the connection to Jira using the provided credentials.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Validates that credentials are not partially provided</li>
     *   <li>Creates the appropriate REST client (Basic Auth or OAuth)</li>
     *   <li>Tests the connection by fetching the current user's profile</li>
     * </ol>
     *
     * @param attributes map containing authentication credentials
     * @throws IllegalArgumentException if credentials are partial or connection fails
     * @throws app.krista.extension.authorization.MustAuthorizeException if OAuth authorization is required
     */
    void testConnection(Map<String, Object> attributes) {
        Map<String, Object> qparams = new HashMap<>();
        qparams.put("jql", "ORDER BY priority DESC, updated desc");
        qparams.put("fields", "*all");
        qparams.put("startAt", 0L);
        qparams.put("maxResults", 1L);
        RestClient restClient;

        // Check for partial credentials to provide helpful error messages
        if (isPartialOAuthCredentialPresent(attributes)) {
            throw new IllegalArgumentException("Please provide both Client Id and Client Secret");
        }
        if (isPartialBasicCredentialPresent(attributes)) {
            throw new IllegalArgumentException("Please provide Admin Auth Token, Admin User Name, and Host Url");
        }

        // Test connection with appropriate authentication method
        if (basicCredentialsPresentInAttributes(attributes)) {
            restClient = new AuthRestClient(attributes, requestContext.invokeAsUser(), true);
            testConnectionWithRestClient(qparams, restClient);
        }  if (oAuthCredentialsPresentInAttributes(attributes)) {
            JiraService jiraService = new JiraService(requestContext, sessionManager, invoker, authorizationContext, this);
            restClient = new OAuth2RestClient(jiraService);
            testConnectionWithRestClient(qparams, restClient);
        }
    }

    /**
     * Checks if basic credentials are partially present (1 or 2 out of 3 required fields).
     *
     * <p>Basic credentials require all three fields: Auth Token, User Name, and Host URL.
     * This method returns true if at least one is present but not all three.</p>
     *
     * @param attributes the attributes map to check
     * @return true if some but not all basic credentials are present, false otherwise
     */
    public boolean isPartialBasicCredentialPresent(Map<String, Object> attributes) {
        boolean hasAuthToken = isAttributePresent(attributes, AUTH_TOKEN);
        boolean hasHostUrl = isAttributePresent(attributes, HOST_URL);
        boolean hasUserName = isAttributePresent(attributes, USER_NAME);

        int presentCount = (hasAuthToken ? 1 : 0) + (hasHostUrl ? 1 : 0) + (hasUserName ? 1 : 0);

        // Partial means at least one present but not all three
        return presentCount > 0 && presentCount < 3;
    }

    /**
     * Checks if OAuth credentials are partially present (only one of the two required fields).
     *
     * <p>OAuth credentials require both Client ID and Client Secret.
     * This method returns true if only one is present.</p>
     *
     * @param attributes the attributes map to check
     * @return true if only one OAuth credential is present, false otherwise
     */
    private boolean isPartialOAuthCredentialPresent(Map<String, Object> attributes) {
        if ((isAttributePresent(attributes, CLIENT_ID) && !isAttributePresent(attributes, CLIENT_SECRET)) ||
                (isAttributePresent(attributes, CLIENT_SECRET) && !isAttributePresent(attributes, CLIENT_ID))) {
            return true;
        }
        return false;
    }

    /**
     * Checks if an attribute is present and non-blank in the attributes map.
     *
     * @param attributes the attributes map to check
     * @param attributeName the attribute key to look up
     * @return true if the attribute exists and is not blank, false otherwise
     */
    private boolean isAttributePresent(Map<String, Object> attributes, String attributeName) {
        return attributes.get(attributeName) != null && !attributes.get(attributeName).toString().isBlank();
    }

    /**
     * Tests the connection to Jira by fetching the current user's profile.
     *
     * <p>This method makes a REST API call to the "myself" endpoint to verify
     * that the credentials are valid and the connection is working.</p>
     *
     * @param qparams query parameters for the test request
     * @param restClient the REST client to use (Basic Auth or OAuth)
     * @throws IllegalArgumentException if credentials are invalid or connection fails
     * @throws app.krista.extension.authorization.MustAuthorizeException if OAuth authorization is required
     */
    private void testConnectionWithRestClient(Map<String, Object> qparams, RestClient restClient) {
        try {
            String out = restClient.fetch("myself", new HashMap<>(), String.class);
            LOGGER.debug("Test Connection Response Details : {}", out);
            JsonObject jsonObject = GSON.fromJson(out, JsonObject.class);
            if (!jsonObject.has("self")) {
                setAttributes(invoker.getAttributes());
                throw new IllegalArgumentException(jsonObject.has("message") ? "The credentials provided are incorrect. Please ensure the Host Url, User Name and Admin Auth Token are correct." : "Wrong Credentials!");
            }
            LOGGER.info("Connection test successful!");
        } catch (app.krista.extension.authorization.MustAuthorizeException cause) {
            // Propagate to trigger OAuth popup
            LOGGER.info("OAuth authorization required - redirecting to Atlassian login...");
            throw cause;
        } catch (Exception cause) {
            setAttributes(invoker.getAttributes());
            LOGGER.error("Connection test failed: {}", cause.getMessage());
            throw new IllegalArgumentException("The credentials provided are incorrect. Please ensure the Host Url, User Name and Admin Auth Token are correct.", cause);
        }
    }

    /**
     * Checks if all basic authentication credentials are present in the instance fields.
     *
     * @return true if all three basic credentials (userName, authToken, hostUrl) are present and non-blank
     */
    public boolean areBasicCredentialsPresent() {
        return !((userName == null || userName.isBlank()) ||
                (authToken == null || authToken.isBlank()) ||
                (hostUrl == null || hostUrl.isBlank()));
    }

    /**
     * Checks if all OAuth 2.0 credentials are present in the instance fields.
     *
     * @return true if both OAuth credentials (clientId, clientSecret) are present and non-blank
     */
    public boolean areOAuthCredentialsPresent() {
        return !((clientId == null || clientId.isBlank()) ||
                (clientSecret == null || clientSecret.isBlank()));
    }

    public void update(Map<String, Object> attributes) {
        clientId = InvokerAttributes.getStringOrNull(attributes, CLIENT_ID);
        clientSecret = InvokerAttributes.getStringOrNull(attributes, CLIENT_SECRET);
        userName = InvokerAttributes.getStringOrNull(attributes, USER_NAME);
        hostUrl = InvokerAttributes.getStringOrNull(attributes, HOST_URL);
        authToken = InvokerAttributes.getStringOrNull(attributes, AUTH_TOKEN);
    }

}
