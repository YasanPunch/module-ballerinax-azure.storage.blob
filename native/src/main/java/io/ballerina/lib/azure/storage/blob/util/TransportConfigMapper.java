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

import com.azure.core.http.ProxyOptions;
import com.azure.core.http.jdk.httpclient.JdkHttpClientBuilder;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertPathBuilder;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.X509CertSelector;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.CertPathTrustManagerParameters;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

/**
 * Maps the connector's {@code RetryConfig} and {@code TransportConfig} onto the SDK's retry
 * policy and its JDK HttpClient transport. TLS settings ({@code SecureSocket}) are mapped onto
 * the JDK's {@link SSLContext} and {@link SSLParameters}: trust material (a {@code CertStore} or
 * a PEM file), client key material (a store or PKCS#8 cert-and-key files), TLS versions, cipher
 * suites, SNI override, session timeout, and certificate revocation checking through the JDK's
 * PKIX checker. The JDK client always verifies the server's host name against its certificate.
 */
public final class TransportConfigMapper {

    // Ballerina RetryPolicyType enum value matched against the config.
    private static final String RETRY_POLICY_FIXED = "fixed";

    // Field names of the retry, connection-pool, proxy, and TLS records (read only here).
    private static final BString RETRY_POLICY_TYPE = StringUtils.fromString("retryPolicyType");
    private static final BString MAX_TRIES = StringUtils.fromString("maxTries");
    private static final BString TRY_TIMEOUT_SECONDS = StringUtils.fromString("tryTimeoutSeconds");
    private static final BString RETRY_DELAY_SECONDS = StringUtils.fromString("retryDelaySeconds");
    private static final BString MAX_RETRY_DELAY_SECONDS = StringUtils.fromString("maxRetryDelaySeconds");
    private static final BString SECONDARY_HOST_URL = StringUtils.fromString("secondaryHostUrl");
    private static final BString CONNECTION_POOL = StringUtils.fromString("connectionPool");
    private static final BString CONNECT_TIMEOUT_SECONDS = StringUtils.fromString("connectTimeoutSeconds");
    private static final BString READ_TIMEOUT_SECONDS = StringUtils.fromString("readTimeoutSeconds");
    private static final BString SECURE_SOCKET = StringUtils.fromString("secureSocket");
    private static final BString PROXY = StringUtils.fromString("proxy");
    private static final BString HOST = StringUtils.fromString("host");
    private static final BString PORT = StringUtils.fromString("port");
    private static final BString USERNAME = StringUtils.fromString("username");
    private static final BString PASSWORD = StringUtils.fromString("password");
    private static final BString NON_PROXY_HOSTS = StringUtils.fromString("nonProxyHosts");
    private static final BString CERT = StringUtils.fromString("cert");
    private static final BString KEY = StringUtils.fromString("key");
    private static final BString PROTOCOL_VERSIONS = StringUtils.fromString("protocolVersions");
    private static final BString CIPHERS = StringUtils.fromString("ciphers");
    private static final BString VALIDATE_REVOCATION = StringUtils.fromString("validateRevocation");
    private static final BString SESSION_TIMEOUT_SECONDS = StringUtils.fromString("sessionTimeoutSeconds");
    private static final BString SNI_HOST_NAME = StringUtils.fromString("sniHostName");
    // CertStore and CertKey fields.
    private static final BString STORE_PATH = StringUtils.fromString("path");
    private static final BString CERT_FILE = StringUtils.fromString("certFile");
    private static final BString KEY_FILE = StringUtils.fromString("keyFile");
    private static final BString KEY_PASSWORD = StringUtils.fromString("keyPassword");

    // PEM armour of the two accepted private-key encodings.
    private static final String PKCS8_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PKCS8_ENCRYPTED_HEADER = "-----BEGIN ENCRYPTED PRIVATE KEY-----";
    private static final String[] KEY_ALGORITHMS = {"RSA", "EC"};
    private static final String JAVA_HOME_PROPERTY = "java.home";
    private static final char[] IN_MEMORY_STORE_PASSWORD = new char[0];

    // The transport drops the SDK's Content-Length header because the JDK client derives its own
    // from the body, and warns about it on every such request; the value on the wire is the same,
    // so that one message is filtered out of the transport's logger (the SDK logs through the
    // runtime's JUL binding). The field keeps the logger alive: the log manager holds it weakly.
    private static final String RESTRICTED_HEADER_MESSAGE = "The header is restricted by 'java.net.http.HttpClient'";
    private static final Logger TRANSPORT_LOGGER = Logger.getLogger("com.azure.core.http.jdk.httpclient.JdkHttpClient");

    static {
        TRANSPORT_LOGGER.setFilter(record -> record.getMessage() == null
                || !record.getMessage().contains(RESTRICTED_HEADER_MESSAGE));
    }

    private TransportConfigMapper() {
    }

    public static RequestRetryOptions retryOptions(BMap<BString, Object> retry) {
        String policy = retry.getStringValue(RETRY_POLICY_TYPE).getValue();
        return new RequestRetryOptions(
                RETRY_POLICY_FIXED.equals(policy) ? RetryPolicyType.FIXED : RetryPolicyType.EXPONENTIAL,
                Math.toIntExact((Long) retry.get(MAX_TRIES)),
                seconds(retry.get(TRY_TIMEOUT_SECONDS)),
                seconds(retry.get(RETRY_DELAY_SECONDS)),
                seconds(retry.get(MAX_RETRY_DELAY_SECONDS)),
                ValueUtils.optString(retry, SECONDARY_HOST_URL));
    }

    @SuppressWarnings("unchecked")
    public static com.azure.core.http.HttpClient httpClient(BMap<BString, Object> transport) {
        ensureJavaHome();
        HttpClient.Builder jdkBuilder = HttpClient.newBuilder();
        BMap<BString, Object> secureSocket = (BMap<BString, Object>) transport.get(SECURE_SOCKET);
        if (secureSocket != null) {
            applyTls(jdkBuilder, secureSocket);
        }

        // The read timeout bounds the wait for the response headers and every body read alike,
        // which is what the record documents as the timeout for reading a response.
        BMap<BString, Object> pool = (BMap<BString, Object>) transport.get(CONNECTION_POOL);
        Duration readTimeout = seconds(pool.get(READ_TIMEOUT_SECONDS));
        JdkHttpClientBuilder builder = new JdkHttpClientBuilder(jdkBuilder)
                .connectionTimeout(seconds(pool.get(CONNECT_TIMEOUT_SECONDS)))
                .responseTimeout(readTimeout)
                .readTimeout(readTimeout);

        BMap<BString, Object> proxy = (BMap<BString, Object>) transport.get(PROXY);
        if (proxy == null) {
            return builder.build();
        }
        // The proxy options carry no credentials: the transport would turn them into a JDK
        // Authenticator, which on Java 21 costs the request its Authorization header. The
        // credentials go on the wire as a header instead (ProxyAuthorizingHttpClient).
        ProxyOptions options = proxyOptions(proxy);
        com.azure.core.http.HttpClient client = builder.proxy(options).build();
        String username = ValueUtils.optString(proxy, USERNAME);
        String password = ValueUtils.optString(proxy, PASSWORD);
        if (username != null && password != null) {
            return new ProxyAuthorizingHttpClient(client, options.getNonProxyHosts(), username, password);
        }
        return client;
    }

    /*
     * The transport builder reads `java.home/conf/net.properties` through a path lookup that
     * refuses a null root, and a native image has no `java.home`. The property is then pointed
     * at a scratch directory holding an empty file, so the builder sees the JDK defaults. Runs
     * before the builder class is first used; a JVM run already has the property and skips it.
     */
    private static synchronized void ensureJavaHome() {
        if (System.getProperty(JAVA_HOME_PROPERTY) != null) {
            return;
        }
        try {
            Path home = Files.createTempDirectory("azure-storage-blob-java-home");
            Path conf = Files.createDirectories(home.resolve("conf"));
            Path netProperties = Files.createFile(conf.resolve("net.properties"));
            for (Path path : new Path[] {netProperties, conf, home}) {
                path.toFile().deleteOnExit();
            }
            System.setProperty(JAVA_HOME_PROPERTY, home.toString());
        } catch (IOException e) {
            // The builder then reports the unreadable file itself and falls back to its defaults.
        }
    }

    private static ProxyOptions proxyOptions(BMap<BString, Object> proxy) {
        ProxyOptions options = new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress(
                proxy.getStringValue(HOST).getValue(),
                Math.toIntExact((Long) proxy.get(PORT))));
        BArray nonProxyHosts = (BArray) proxy.get(NON_PROXY_HOSTS);
        if (nonProxyHosts != null && nonProxyHosts.size() > 0) {
            options.setNonProxyHosts(String.join("|", nonProxyHosts.getStringArray()));
        }
        return options;
    }

    private static void applyTls(HttpClient.Builder jdkBuilder, BMap<BString, Object> secureSocket) {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagers = keyManagerFactory(secureSocket);
            TrustManagerFactory trustManagers = trustManagerFactory(secureSocket);
            context.init(keyManagers == null ? null : keyManagers.getKeyManagers(),
                    trustManagers == null ? null : trustManagers.getTrustManagers(), null);
            Object sessionTimeout = secureSocket.get(SESSION_TIMEOUT_SECONDS);
            if (sessionTimeout != null) {
                context.getClientSessionContext()
                        .setSessionTimeout(Math.toIntExact(seconds(sessionTimeout).toSeconds()));
            }
            jdkBuilder.sslContext(context);
            SSLParameters parameters = sslParameters(context, secureSocket);
            if (parameters != null) {
                jdkBuilder.sslParameters(parameters);
            }
        } catch (GeneralSecurityException | IOException e) {
            throw BlobErrorCreator.clientError(
                    "invalid secureSocket configuration: " + BallerinaAzureClient.describe(e), e);
        }
    }

    /*
     * The handshake parameters are only overridden when the config sets one of them; otherwise
     * the JDK client's own defaults apply. The client sends the endpoint's host name as SNI
     * itself, so the configured sniHostName takes effect when the endpoint is an IP address.
     */
    private static SSLParameters sslParameters(SSLContext context, BMap<BString, Object> secureSocket) {
        BArray protocolVersions = (BArray) secureSocket.get(PROTOCOL_VERSIONS);
        BArray ciphers = (BArray) secureSocket.get(CIPHERS);
        String sniHostName = ValueUtils.optString(secureSocket, SNI_HOST_NAME);
        boolean hasProtocols = protocolVersions != null && protocolVersions.size() > 0;
        boolean hasCiphers = ciphers != null && ciphers.size() > 0;
        if (!hasProtocols && !hasCiphers && sniHostName == null) {
            return null;
        }
        SSLParameters parameters = context.getDefaultSSLParameters();
        if (hasProtocols) {
            parameters.setProtocols(protocolVersions.getStringArray());
        }
        if (hasCiphers) {
            parameters.setCipherSuites(ciphers.getStringArray());
        }
        if (sniHostName != null) {
            parameters.setServerNames(List.of(new SNIHostName(sniHostName)));
        }
        return parameters;
    }

    /*
     * Builds the trust side: a CertStore or a PEM certificate file, optionally with revocation
     * checking. Revocation runs through the JDK PKIX checker, which consumes stapled OCSP
     * responses when the server sends them and falls back to OCSP/CRL fetching. Without trust
     * material the factory is null and the JDK's default trust store applies.
     */
    private static TrustManagerFactory trustManagerFactory(BMap<BString, Object> secureSocket)
            throws GeneralSecurityException, IOException {
        Object cert = secureSocket.get(CERT);
        boolean validateRevocation = Boolean.TRUE.equals(secureSocket.get(VALIDATE_REVOCATION));
        if (cert == null && validateRevocation) {
            throw BlobErrorCreator.clientError(
                    "validateRevocation requires trust material (`cert`) to validate against", null);
        }
        if (cert == null) {
            return null;
        }
        KeyStore trustStore;
        if (cert instanceof BString pemPath) {
            requireFile(pemPath.getValue(), "cert");
            trustStore = pemToKeyStore(pemPath.getValue());
        } else {
            @SuppressWarnings("unchecked")
            BMap<BString, Object> store = (BMap<BString, Object>) cert;
            trustStore = loadKeyStore(store.getStringValue(STORE_PATH).getValue(),
                    store.getStringValue(PASSWORD).getValue());
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("PKIX");
        if (validateRevocation) {
            CertPathBuilder certPathBuilder = CertPathBuilder.getInstance("PKIX");
            PKIXRevocationChecker revocationChecker = (PKIXRevocationChecker) certPathBuilder.getRevocationChecker();
            PKIXBuilderParameters pkixParameters = new PKIXBuilderParameters(trustStore, new X509CertSelector());
            pkixParameters.addCertPathChecker(revocationChecker);
            trustManagerFactory.init(new CertPathTrustManagerParameters(pkixParameters));
        } else {
            trustManagerFactory.init(trustStore);
        }
        return trustManagerFactory;
    }

    private static KeyManagerFactory keyManagerFactory(BMap<BString, Object> secureSocket)
            throws GeneralSecurityException, IOException {
        Object key = secureSocket.get(KEY);
        if (key == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        BMap<BString, Object> keyRecord = (BMap<BString, Object>) key;
        KeyStore keyStore;
        char[] password;
        if (keyRecord.containsKey(CERT_FILE)) {
            String certFile = keyRecord.getStringValue(CERT_FILE).getValue();
            String keyFile = keyRecord.getStringValue(KEY_FILE).getValue();
            requireFile(certFile, "key.certFile");
            requireFile(keyFile, "key.keyFile");
            keyStore = certKeyToKeyStore(certFile, keyFile, ValueUtils.optString(keyRecord, KEY_PASSWORD));
            password = IN_MEMORY_STORE_PASSWORD;
        } else {
            String storePassword = keyRecord.getStringValue(PASSWORD).getValue();
            keyStore = loadKeyStore(keyRecord.getStringValue(STORE_PATH).getValue(), storePassword);
            password = storePassword.toCharArray();
        }
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        return keyManagerFactory;
    }

    private static KeyStore loadKeyStore(String path, String password) throws GeneralSecurityException, IOException {
        requireFile(path, "store path");
        for (String type : new String[] {"PKCS12", "JKS"}) {
            KeyStore store = KeyStore.getInstance(type);
            try (InputStream input = new FileInputStream(path)) {
                store.load(input, password.toCharArray());
                return store;
            } catch (IOException e) {
                // Wrong store format (or password); try the next type before giving up.
            }
        }
        throw BlobErrorCreator.clientError(
                "cannot load the certificate store at " + path + " as PKCS12 or JKS (check the password)", null);
    }

    private static KeyStore pemToKeyStore(String pemPath) throws GeneralSecurityException, IOException {
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, null);
        int index = 0;
        for (Certificate certificate : readCertificates(pemPath)) {
            store.setCertificateEntry("cert-" + index++, certificate);
        }
        return store;
    }

    private static KeyStore certKeyToKeyStore(String certFile, String keyFile, String keyPassword)
            throws GeneralSecurityException, IOException {
        Certificate[] chain = readCertificates(certFile).toArray(new Certificate[0]);
        PrivateKey privateKey = readPrivateKey(keyFile, keyPassword);
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, null);
        store.setKeyEntry("key", privateKey, IN_MEMORY_STORE_PASSWORD, chain);
        return store;
    }

    private static Collection<? extends Certificate> readCertificates(String pemPath)
            throws GeneralSecurityException, IOException {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        try (InputStream input = new FileInputStream(pemPath)) {
            Collection<? extends Certificate> certificates = factory.generateCertificates(input);
            if (certificates.isEmpty()) {
                throw BlobErrorCreator.clientError("no certificates found in " + pemPath, null);
            }
            return certificates;
        }
    }

    /*
     * Reads a PEM private key in PKCS#8 form, plain or password-encrypted. The traditional
     * OpenSSL forms (`BEGIN RSA PRIVATE KEY`, `BEGIN EC PRIVATE KEY`) are refused with a
     * pointer to the accepted form rather than parsed by hand.
     */
    private static PrivateKey readPrivateKey(String keyFile, String keyPassword)
            throws GeneralSecurityException, IOException {
        String pem = Files.readString(Path.of(keyFile), StandardCharsets.ISO_8859_1);
        boolean encrypted = pem.contains(PKCS8_ENCRYPTED_HEADER);
        if (!encrypted && !pem.contains(PKCS8_HEADER)) {
            throw BlobErrorCreator.clientError("key.keyFile must hold a PKCS#8 private key"
                    + " (`BEGIN PRIVATE KEY` or `BEGIN ENCRYPTED PRIVATE KEY`): " + keyFile, null);
        }
        byte[] encoded = Base64.getMimeDecoder().decode(pemBody(pem));
        PKCS8EncodedKeySpec keySpec;
        if (encrypted) {
            if (keyPassword == null) {
                throw BlobErrorCreator.clientError(
                        "key.keyPassword is required to read the encrypted private key " + keyFile, null);
            }
            EncryptedPrivateKeyInfo keyInfo = new EncryptedPrivateKeyInfo(encoded);
            SecretKey secret = SecretKeyFactory.getInstance(keyInfo.getAlgName())
                    .generateSecret(new PBEKeySpec(keyPassword.toCharArray()));
            Cipher cipher = Cipher.getInstance(keyInfo.getAlgName());
            cipher.init(Cipher.DECRYPT_MODE, secret, keyInfo.getAlgParameters());
            keySpec = keyInfo.getKeySpec(cipher);
        } else {
            keySpec = new PKCS8EncodedKeySpec(encoded);
        }
        for (String algorithm : KEY_ALGORITHMS) {
            try {
                return KeyFactory.getInstance(algorithm).generatePrivate(keySpec);
            } catch (InvalidKeySpecException e) {
                // Not this algorithm; try the next.
            }
        }
        throw BlobErrorCreator.clientError("cannot read the private key in " + keyFile + " as RSA or EC", null);
    }

    private static String pemBody(String pem) {
        StringBuilder body = new StringBuilder();
        for (String line : pem.split("\\R")) {
            if (!line.startsWith("-----")) {
                body.append(line.strip());
            }
        }
        return body.toString();
    }

    private static void requireFile(String path, String fieldName) {
        if (!new File(path).isFile()) {
            throw BlobErrorCreator.clientError(fieldName + " does not point to a readable file: " + path, null);
        }
    }

    private static Duration seconds(Object decimalValue) {
        return Duration.ofMillis((long) (((BDecimal) decimalValue).floatValue() * 1000));
    }
}
