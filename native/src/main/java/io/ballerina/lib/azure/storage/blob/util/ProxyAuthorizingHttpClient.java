/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.azure.storage.blob.util;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Adds the proxy credentials to every request that goes through the proxy, as a pre-emptive
 * {@code Proxy-Authorization} header. The JDK client's own route for proxy credentials, an
 * {@code Authenticator}, is not used: on Java 21 a configured authenticator makes the client
 * drop the request's {@code Authorization} header, which carries the storage account's shared
 * key signature or the Entra bearer token (fixed in later JDKs). A plain header reaches the
 * proxy on direct requests and on the CONNECT tunnel alike, and the JDK never sends it to a
 * host that bypasses the proxy.
 */
final class ProxyAuthorizingHttpClient implements HttpClient {

    private final HttpClient delegate;
    private final Pattern nonProxyHosts;
    private final String credentials;

    ProxyAuthorizingHttpClient(HttpClient delegate, String nonProxyHostsPattern, String username, String password) {
        this.delegate = delegate;
        this.nonProxyHosts = nonProxyHostsPattern == null
                ? null : Pattern.compile(nonProxyHostsPattern, Pattern.CASE_INSENSITIVE);
        this.credentials = "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return delegate.send(authorize(request));
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return delegate.send(authorize(request), context);
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        return delegate.sendSync(authorize(request), context);
    }

    private HttpRequest authorize(HttpRequest request) {
        String host = request.getUrl().getHost();
        if (nonProxyHosts == null || !nonProxyHosts.matcher(host).matches()) {
            request.getHeaders().set(HttpHeaderName.PROXY_AUTHORIZATION, credentials);
        }
        return request;
    }
}
