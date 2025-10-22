package fr.becpg.repo.helper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Helper class for RestTemplate configuration with optimized connection pooling,
 * DNS cache management, and DNS resolution logging.
 *
 * @author matthieu
 */
public final class RestTemplateHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(RestTemplateHelper.class);
    
    // Connection pool configuration
    private static final int MAX_TOTAL_CONNECTIONS = 50;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;
    private static final int CONNECTION_TTL_SECONDS = 5;
    
    // Timeout configuration
    private static final Timeout CONNECTION_TIMEOUT = Timeout.of(5, TimeUnit.SECONDS);
    private static final Timeout DEFAULT_SOCKET_TIMEOUT = Timeout.of(30, TimeUnit.SECONDS);
    private static final Timeout LONG_SOCKET_TIMEOUT = Timeout.of(3, TimeUnit.MINUTES);
    
    // Pre-configured RestTemplate instances
    private static final RestTemplate defaultRestTemplate;
    private static final RestTemplate longTimeoutRestTemplate;

    static {
        defaultRestTemplate = createRestTemplate(DEFAULT_SOCKET_TIMEOUT);
        longTimeoutRestTemplate = createRestTemplate(LONG_SOCKET_TIMEOUT);
    }

    private RestTemplateHelper() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Gets the default RestTemplate instance with standard timeout settings.
     *
     * @return configured RestTemplate instance
     */
    public static RestTemplate getRestTemplate() {
        return defaultRestTemplate;
    }
    
    /**
     * Gets the RestTemplate instance with extended timeout settings for long-running operations.
     *
     * @return configured RestTemplate instance with long timeout
     */
    public static RestTemplate getRestTemplateLongTimeout() {
        return longTimeoutRestTemplate;
    }

    /**
     * Creates a configured RestTemplate with the specified socket timeout.
     *
     * @param socketTimeout the socket timeout to apply
     * @return configured RestTemplate instance
     */
    private static RestTemplate createRestTemplate(Timeout socketTimeout) {
        CloseableHttpClient httpClient = createHttpClient(socketTimeout);
        ClientHttpRequestFactory requestFactory = new DnsLoggingRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    /**
     * Creates a configured HTTP client with connection pooling and timeout settings.
     *
     * @param socketTimeout the socket timeout to apply
     * @return configured CloseableHttpClient
     */
    private static CloseableHttpClient createHttpClient(Timeout socketTimeout) {
        ConnectionConfig connectionConfig = buildConnectionConfig(socketTimeout);
        
        PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = 
            PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
                .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                .setDefaultConnectionConfig(connectionConfig);

        return HttpClients.custom()
            .setConnectionManager(connectionManagerBuilder.build())
            .setConnectionManagerShared(true)
            .evictExpiredConnections()
            .evictIdleConnections(Timeout.of(CONNECTION_TTL_SECONDS, TimeUnit.SECONDS))
            .setConnectionReuseStrategy((request, response, context) -> true)
            .build();
    }

    /**
     * Builds connection configuration with specified timeouts.
     *
     * @param socketTimeout the socket timeout to apply
     * @return configured ConnectionConfig
     */
    private static ConnectionConfig buildConnectionConfig(Timeout socketTimeout) {
        return ConnectionConfig.custom()
            .setConnectTimeout(CONNECTION_TIMEOUT)
            .setSocketTimeout(socketTimeout)
            .setTimeToLive(CONNECTION_TTL_SECONDS, TimeUnit.SECONDS)
            .build();
    }

    /**
     * Performs manual DNS resolution check for a hostname.
     *
     * @param hostname the hostname to resolve
     * @return array of resolved IP addresses, or empty array if resolution fails
     */
    public static String[] checkDnsResolution(String hostname) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(hostname);
            String[] ips = Arrays.stream(addresses)
                .map(InetAddress::getHostAddress)
                .toArray(String[]::new);
            
            if (logger.isInfoEnabled()) {
                logger.info("Manual DNS check for {}: {}", hostname, Arrays.toString(ips));
            }
            
            return ips;
        } catch (UnknownHostException e) {
            logger.error("Manual DNS check failed for {}: {}", hostname, e.getMessage());
            return new String[0];
        }
    }

    /**
     * Custom request factory that logs DNS resolution and adds standard headers.
     */
    private static class DnsLoggingRequestFactory extends HttpComponentsClientHttpRequestFactory {
        
        DnsLoggingRequestFactory(CloseableHttpClient httpClient) {
            super(httpClient);
            setConnectTimeout((int) CONNECTION_TIMEOUT.toMilliseconds());
            setConnectionRequestTimeout((int) CONNECTION_TIMEOUT.toMilliseconds());
        }

        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
            if (logger.isDebugEnabled()) {
                logDnsResolution(uri);
            }
            
            ClientHttpRequest request = super.createRequest(uri, httpMethod);
            addStandardHeaders(request);
            return request;
        }
        
        /**
         * Logs DNS resolution information for the given URI.
         *
         * @param uri the URI to resolve
         */
        private void logDnsResolution(URI uri) {
            String host = uri.getHost();
            if (host == null) {
                return;
            }
            
            try {
                InetAddress[] addresses = InetAddress.getAllByName(host);
                String[] ips = Arrays.stream(addresses)
                    .map(InetAddress::getHostAddress)
                    .toArray(String[]::new);
                if (logger.isInfoEnabled()) {
                	logger.info("DNS Resolution for host {}: {}", host, Arrays.toString(ips));
                }
            } catch (UnknownHostException e) {
                logger.error("DNS Resolution failed for host {}: {}", host, e.getMessage());
            }
        }
        
        /**
         * Adds standard HTTP headers to the request.
         *
         * @param request the request to add headers to
         */
        private void addStandardHeaders(ClientHttpRequest request) {
            request.getHeaders().add("Accept-Encoding", "gzip, deflate");
            request.getHeaders().add("Connection", "keep-alive");
        }
    }
}