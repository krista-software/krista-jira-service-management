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
import app.krista.extensions.devsecops.development.jira.authentication.AuthRestClient;
import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.authentication.OAuth2RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.attributes.BaseAtlassianAttributes;
import app.krista.sdk.atlassian.attributes.CredentialFieldNames;
import com.google.gson.JsonObject;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

/**
 * Manages Jira Service Management authentication credentials and validates connections.
 *
 * <p>Credential storage, lazy resolution, complete/partial/mixed detection and the
 * {@code validateAttributes} template flow live in the shared Atlassian SDK
 * ({@link BaseAtlassianAttributes}); this class contributes the JSM field labels and the
 * product-specific connection test against the Jira {@code /myself} endpoint.</p>
 *
 * <p>Supported authentication methods:</p>
 * <ul>
 *   <li><b>Basic Authentication</b>: Admin Auth Token, Admin User Name, and Host Url</li>
 *   <li><b>OAuth 2.0</b>: Client Id and Client Secret (Authorization Code Grant flow)</li>
 * </ul>
 *
 * @author Simran Sethi
 * @see JiraService
 * @see AuthRestClient
 * @see OAuth2RestClient
 * @since 1.0.1
 */
@Service
public class JiraAttributes extends BaseAtlassianAttributes {

    public static final String HOST_URL = "Host Url";
    public static final String USER_NAME = "Admin User Name";
    public static final String AUTH_TOKEN = "Admin Auth Token";
    public static final String CLIENT_ID = "Client Id";
    public static final String CLIENT_SECRET = "Client Secret";
    public static final String CREDENTIALS_MODE = "Credentials Mode";
    public static final String SERVICE_ACCOUNT = "Service Account";

    /** The JSM credential field labels, shared with the REST clients. */
    public static final CredentialFieldNames FIELD_NAMES =
            new CredentialFieldNames(HOST_URL, USER_NAME, AUTH_TOKEN, CLIENT_ID, CLIENT_SECRET);

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraAttributes.class);

    /**
     * Key prefix for credential snapshots persisted by {@code AbstractAtlassianAuthResource}
     * — mirrors the private constant there so per-request attribute reconstruction can
     * supplement missing fields from the Authentication tab's saved state.
     */
    private static final String CRED_PREFIX = "cred.";

    private final RequestContext requestContext;
    private final SessionManager sessionManager;
    private final AuthorizationContext authorizationContext;

    /**
     * Constructs a new JiraAttributes instance with dependency injection.
     * Initializes credentials from the invoker's attributes.
     *
     * @param invoker              the invoker containing extension attributes
     * @param requestContext       the request context for the current operation
     * @param sessionManager       manages OAuth session state
     * @param authorizationContext provides authorization context
     */
    @Inject
    public JiraAttributes(Invoker invoker, RequestContext requestContext, SessionManager sessionManager,
                          AuthorizationContext authorizationContext) {
        super(invoker, FIELD_NAMES);
        this.requestContext = requestContext;
        this.sessionManager = sessionManager;
        this.authorizationContext = authorizationContext;
    }

    /**
     * Exposes the "Krista Managed" credentials chooser: when the
     * {@value #CREDENTIALS_MODE} attribute is {@code "managed"}, the OAuth
     * client id/secret come from the SDK's bundled
     * {@code atlassian-managed-credentials.properties}.
     */
    @Override
    protected String getCredentialsModeFieldName() {
        return CREDENTIALS_MODE;
    }

    /** Extension type for per-extension managed-credential overrides. */
    @Override
    protected String getExtensionType() {
        return "jira-service-management";
    }

    /**
     * Exposes the server-to-server (2LO) toggle: when the
     * {@value #SERVICE_ACCOUNT} attribute is {@code true}, OAuth credentials
     * are used with the client-credentials grant instead of the 3LO
     * user-consent flow.
     */
    @Override
    protected String getServiceAccountFieldName() {
        return SERVICE_ACCOUNT;
    }

    // ── Credential getters: prefer Authentication-tab snapshot ─────────────
    //
    // JiraAttributes is recreated per-request from invoker.getAttributes() (the
    // Krista/Setup attribute store).  When credentials are saved through the
    // Authentication tab they are persisted to the KeyValueStore snapshot
    // (keys: "cred.<invokerId>.<field>") via persistCredentialSnapshot(), but
    // saveAttributes() is a no-op — so the invoker attribute store retains
    // STALE values from earlier saves.
    //
    // The credential snapshot is therefore the authoritative source and must be
    // checked FIRST.  Falling back to invoker attributes only when the snapshot
    // is absent preserves backward compatibility for extensions that pre-date
    // the Authentication tab.

    private String credSnap(String field) {
        if (sessionManager == null || requestContext == null) return null;
        return sessionManager.get(CRED_PREFIX + requestContext.getInvokerId() + "." + field);
    }

    @Override
    public String getHostUrl() {
        String snap = credSnap("hostUrl");
        if (snap != null && !snap.isBlank()) return snap;
        return super.getHostUrl();
    }

    @Override
    public String getUserName() {
        String snap = credSnap("userName");
        if (snap != null && !snap.isBlank()) return snap;
        return super.getUserName();
    }

    @Override
    public String getAuthToken() {
        String snap = credSnap("authToken");
        if (snap != null && !snap.isBlank()) return snap;
        return super.getAuthToken();
    }

    @Override
    public String getClientId() {
        String snap = credSnap("clientId");
        if (snap != null && !snap.isBlank()) return snap;
        return super.getClientId();
    }

    @Override
    public String getClientSecret() {
        String snap = credSnap("clientSecret");
        if (snap != null && !snap.isBlank()) return snap;
        return super.getClientSecret();
    }

    /**
     * Three-tier credentials mode resolution (mirrors Jira extension):
     *
     * <ol>
     *   <li><b>In-memory state</b> ({@code setCredentialsMode("managed"|"own")})
     *       — set during the same request that triggers the OAuth flow.</li>
     *   <li><b>Persistent credential snapshot</b>
     *       ({@code cred.<invokerId>.credentialsMode}) — written by
     *       {@code AbstractAtlassianAuthResource} when the user saves.
     *       Consulted when the in-memory state is null (fresh per-request
     *       attribute construction).</li>
     *   <li><b>Base-class auto-detection</b> — invoker attributes +
     *       {@code ManagedCredentialStore} fallback.</li>
     * </ol>
     */
    @Override
    public boolean isManagedCredentialsEnabled() {
        // 1. In-memory state (current request's intent — highest priority).
        String inMemoryMode = getCredentialsModeValue();
        if ("managed".equalsIgnoreCase(inMemoryMode)) {
            return true;
        }
        if ("own".equalsIgnoreCase(inMemoryMode)) {
            return false;
        }
        // 2. Persistent credential snapshot (previous save's intent).
        String storedMode = credSnap("credentialsMode");
        if ("managed".equalsIgnoreCase(storedMode)) {
            return true;
        }
        if ("own".equalsIgnoreCase(storedMode)) {
            return false;
        }
        // 3. Base-class auto-detection.
        return super.isManagedCredentialsEnabled();
    }

    /**
     * Tests the connection to Jira using the candidate credentials by fetching
     * the current user's profile from the {@code /myself} endpoint.
     *
     * <p>Structural validation (mixed/missing/partial credentials, email and
     * OAuth format) has already run in
     * {@link BaseAtlassianAttributes#validateAttributes(Map)} by the time this
     * hook is invoked.</p>
     *
     * @param attributes map containing authentication credentials
     * @throws IllegalArgumentException                                  if credentials are invalid or connection fails
     * @throws app.krista.extension.authorization.MustAuthorizeException if OAuth authorization is required
     */
    @Override
    protected void testConnection(Map<String, Object> attributes) {
        Map<String, Object> qparams = new HashMap<>();
        qparams.put("jql", "ORDER BY priority DESC, updated desc");
        qparams.put("fields", "*all");
        qparams.put("startAt", 0L);
        qparams.put("maxResults", 1L);
        RestClient restClient;

        // Re-checked here (in addition to the SDK's validateAttributes template) so
        // direct callers keep getting the historical JSM error messages.
        if (isPartialOAuthCredentialPresent(attributes)) {
            throw new IllegalArgumentException(
                    "Incomplete OAuth 2.0 credentials detected.\n"
                            + "Please provide BOTH Client ID and Client Secret for OAuth authentication.");
        }
        if (isPartialBasicCredentialPresent(attributes)) {
            throw new IllegalArgumentException(
                    "Incomplete Basic Auth credentials detected.\n"
                            + "Please provide ALL three fields: Auth Token, User Name, and Host URL for Basic authentication.");
        }

        if (basicCredentialsPresentInAttributes(attributes)) {
            restClient = new AuthRestClient(attributes, requestContext.invokeAsUser(), true);
            testConnectionWithRestClient(qparams, restClient);
        } else if (oAuthCredentialsPresentInAttributes(attributes) || isManagedCredentialsEnabled()) {
            JiraService jiraService = new JiraService(requestContext, sessionManager, getInvoker(),
                    authorizationContext, this);
            jiraService.setCalledFromSetup(true);
            restClient = new OAuth2RestClient(jiraService);
            testConnectionWithRestClient(qparams, restClient);
        }
    }

    /**
     * Tests the connection to Jira by fetching the current user's profile.
     *
     * @param qparams    pre-built query parameters (kept for legacy signature compatibility)
     * @param restClient the REST client to use (Basic Auth or OAuth)
     * @throws IllegalArgumentException                                  if credentials are invalid or connection fails
     * @throws app.krista.extension.authorization.MustAuthorizeException if OAuth authorization is required
     */
    private void testConnectionWithRestClient(Map<String, Object> qparams, RestClient restClient) {
        try {
            String out = restClient.fetch("myself", new HashMap<>(), String.class);
            LOGGER.debug("Test Connection Response Details : {}", out);
            JsonObject jsonObject = GSON.fromJson(out, JsonObject.class);
            if (!jsonObject.has("self")) {
                setAttributes(getInvoker().getAttributes());
                throw new IllegalArgumentException(jsonObject.has("message")
                        ? "The credentials provided are incorrect. Please ensure the Host Url, User Name and Admin Auth Token are correct."
                        : "Wrong Credentials!");
            }
            LOGGER.info("Connection test successful!");
        } catch (app.krista.extension.authorization.MustAuthorizeException cause) {
            // Propagate to trigger OAuth popup
            LOGGER.info("OAuth authorization required - redirecting to Atlassian login...");
            throw cause;
        } catch (Exception cause) {
            setAttributes(getInvoker().getAttributes());
            LOGGER.error("Connection test failed: {}", cause.getMessage());
            throw new IllegalArgumentException(
                    "The credentials provided are incorrect. Please ensure the Host Url, User Name and Admin Auth Token are correct.",
                    cause);
        }
    }
}
