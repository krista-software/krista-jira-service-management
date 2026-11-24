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

package app.krista.extensions.devsecops.development.jira.authentication;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.oauth.ClientCredentialsTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("RestClientFactory Tests")
class RestClientFactoryTest {

    private RestClientFactory restClientFactory;
    private JiraAttributes jiraAttributes;
    private OAuth2RestClient oAuth2RestClient;
    private RequestContext requestContext;
    private Invoker invoker;

    @BeforeEach
    void setUp() {
        jiraAttributes = mock(JiraAttributes.class);
        oAuth2RestClient = mock(OAuth2RestClient.class);
        requestContext = mock(RequestContext.class);
        invoker = mock(Invoker.class);

        restClientFactory = new RestClientFactory(jiraAttributes, oAuth2RestClient, requestContext, invoker);
    }

    // ── Annotations & structure ─────────────────────────────────────────

    @Nested
    @DisplayName("Class structure")
    class ClassStructureTests {

        @Test
        @DisplayName("Has @Service annotation")
        void hasServiceAnnotation() {
            assertTrue(RestClientFactory.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
        }

        @Test
        @DisplayName("Has @Inject annotation on constructor")
        void hasInjectAnnotation() throws NoSuchMethodException {
            var constructor = RestClientFactory.class.getConstructor(
                    JiraAttributes.class, OAuth2RestClient.class, RequestContext.class, Invoker.class);
            assertTrue(constructor.isAnnotationPresent(javax.inject.Inject.class));
        }
    }

    // ── invokeAsUser() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("invokeAsUser")
    class InvokeAsUserTests {

        @Test
        @DisplayName("Delegates to RequestContext and returns true")
        void returnsTrue() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            assertTrue(restClientFactory.invokeAsUser());
            verify(requestContext).invokeAsUser();
        }

        @Test
        @DisplayName("Delegates to RequestContext and returns false")
        void returnsFalse() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            assertFalse(restClientFactory.invokeAsUser());
            verify(requestContext).invokeAsUser();
        }
    }

    // ── getInstance(boolean) — early OAuth return ───────────────────────

    @Nested
    @DisplayName("getInstance — invokeAsUser + OAuth early return")
    class GetInstanceOAuthEarlyReturnTests {

        @Test
        @DisplayName("Returns oAuth2RestClient when invokeAsUser=true AND OAuth present")
        void invokeAsUserTrue_oauthPresent_returnsOAuth() {
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);

            RestClient result = restClientFactory.getInstance(true);

            assertSame(oAuth2RestClient, result);
        }

        @Test
        @DisplayName("Falls through when invokeAsUser=true BUT OAuth NOT present")
        void invokeAsUserTrue_oauthAbsent_fallsThrough() {
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
            when(invoker.getAttributes()).thenReturn(new HashMap<>());

            RestClient result = restClientFactory.getInstance(true);

            assertInstanceOf(AuthRestClient.class, result);
        }

        @Test
        @DisplayName("Falls through when invokeAsUser=false regardless of OAuth")
        void invokeAsUserFalse_fallsThrough() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
            when(invoker.getAttributes()).thenReturn(new HashMap<>());

            RestClient result = restClientFactory.getInstance(false);

            assertInstanceOf(AuthRestClient.class, result);
        }
    }

    // ── getInstance(boolean) — RestClientSelector paths ─────────────────

    @Nested
    @DisplayName("getInstance — RestClientSelector paths")
    class GetInstanceSelectorTests {

        @Test
        @DisplayName("Basic credentials present → AuthRestClient")
        void basicCredentials_returnsAuthClient() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
            Map<String, Object> attrs = new HashMap<>();
            attrs.put("userName", "testUser");
            attrs.put("authToken", "testToken");
            attrs.put("hostUrl", "https://test.atlassian.net");
            when(invoker.getAttributes()).thenReturn(attrs);

            RestClient result = restClientFactory.getInstance(false);

            assertInstanceOf(AuthRestClient.class, result);
            verify(invoker, atLeastOnce()).getAttributes();
        }

        @Test
        @DisplayName("No basic creds + OAuth present → oAuth2RestClient")
        void noBasicCreds_oauthPresent_returnsOAuth() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);

            RestClient result = restClientFactory.getInstance(false);

            assertSame(oAuth2RestClient, result);
        }

        @Test
        @DisplayName("No basic creds + no OAuth → throws IllegalStateException")
        void noCredentials_throws() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);

            assertThrows(IllegalStateException.class, () ->
                    restClientFactory.getInstance(false));
        }

        @Test
        @DisplayName("Creates new AuthRestClient on each call")
        void createsNewAuthClientEachTime() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
            when(invoker.getAttributes()).thenReturn(new HashMap<>());

            RestClient r1 = restClientFactory.getInstance(false);
            RestClient r2 = restClientFactory.getInstance(false);

            assertNotSame(r1, r2);
        }

        @Test
        @DisplayName("Returns same oAuth2RestClient each time")
        void returnsSameOAuthInstance() {
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);

            RestClient r1 = restClientFactory.getInstance(false);
            RestClient r2 = restClientFactory.getInstance(false);

            assertSame(r1, r2);
            assertSame(oAuth2RestClient, r1);
        }
    }

    // ── getInstance(boolean) — service account mode ─────────────────────

    @Nested
    @DisplayName("getInstance — service account mode")
    class ServiceAccountModeTests {

        @Test
        @DisplayName("Service account mode creates OAuth2RestClient via ClientCredentialsTokenProvider")
        void serviceAccountMode_createsClient() {
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
            when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(true);

            try (MockedConstruction<ClientCredentialsTokenProvider> providerMock =
                         mockConstruction(ClientCredentialsTokenProvider.class)) {
                RestClient result = restClientFactory.getInstance(false);

                assertNotNull(result);
                assertInstanceOf(OAuth2RestClient.class, result);
                // Verify ClientCredentialsTokenProvider was constructed with jiraAttributes
                assertEquals(1, providerMock.constructed().size());
            }
        }

        @Test
        @DisplayName("Service account client is cached on subsequent calls")
        void serviceAccountClient_cachedOnSecondCall() {
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
            when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(true);

            try (MockedConstruction<ClientCredentialsTokenProvider> providerMock =
                         mockConstruction(ClientCredentialsTokenProvider.class)) {
                RestClient r1 = restClientFactory.getInstance(false);
                RestClient r2 = restClientFactory.getInstance(false);

                assertSame(r1, r2);
                // ClientCredentialsTokenProvider should only be constructed once
                assertEquals(1, providerMock.constructed().size());
            }
        }

        @Test
        @DisplayName("Double-checked locking outer null check — pre-set field skips creation")
        void outerNullCheck_fieldAlreadySet() throws Exception {
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
            when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(true);

            // Pre-set the volatile serviceAccountClient field via reflection
            OAuth2RestClient preExisting = mock(OAuth2RestClient.class);
            Field field = RestClientFactory.class.getDeclaredField("serviceAccountClient");
            field.setAccessible(true);
            field.set(restClientFactory, preExisting);

            RestClient result = restClientFactory.getInstance(false);

            // Should return the pre-set client without constructing a new one
            assertSame(preExisting, result);
        }

        @Test
        @DisplayName("Double-checked locking inner null check — concurrent threads exercise race condition")
        void innerNullCheck_concurrentAccess() throws Exception {
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
            when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(true);

            int threadCount = 50;
            CountDownLatch startGate = new CountDownLatch(1);
            CountDownLatch finishGate = new CountDownLatch(threadCount);
            ConcurrentLinkedQueue<RestClient> results = new ConcurrentLinkedQueue<>();

            try (MockedConstruction<ClientCredentialsTokenProvider> providerMock = mockConstruction(
                    ClientCredentialsTokenProvider.class, (mock, context) -> {
                        // Slow down construction to widen the race window so multiple
                        // threads pass the outer null check before the first completes
                        Thread.sleep(50);
                    })) {
                for (int i = 0; i < threadCount; i++) {
                    new Thread(() -> {
                        try {
                            startGate.await();
                            results.add(restClientFactory.getInstance(false));
                        } catch (Exception e) {
                            // ignore
                        } finally {
                            finishGate.countDown();
                        }
                    }).start();
                }

                startGate.countDown();
                assertTrue(finishGate.await(30, TimeUnit.SECONDS), "All threads should complete");
            }

            // Double-checked locking guarantees all threads get the same instance
            RestClient first = results.peek();
            assertNotNull(first);
            for (RestClient r : results) {
                assertSame(first, r);
            }
        }
    }

    // ── getInstance() — no-arg overload ─────────────────────────────────

    @Nested
    @DisplayName("getInstance — no-arg overload")
    class GetInstanceNoArgTests {

        @Test
        @DisplayName("Delegates to getInstance(boolean) using requestContext.invokeAsUser()")
        void delegatesToBooleanOverload() {
            when(requestContext.invokeAsUser()).thenReturn(false);
            when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);

            RestClient result = restClientFactory.getInstance();

            assertSame(oAuth2RestClient, result);
            verify(requestContext).invokeAsUser();
        }

        @Test
        @DisplayName("Passes invokeAsUser=true from RequestContext")
        void passesInvokeAsUserTrue() {
            when(requestContext.invokeAsUser()).thenReturn(true);
            when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);

            RestClient result = restClientFactory.getInstance();

            // invokeAsUser=true + OAuth present → early return of oAuth2RestClient
            assertSame(oAuth2RestClient, result);
            verify(requestContext).invokeAsUser();
        }
    }
}
