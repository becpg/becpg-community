package fr.becpg.repo.helper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
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
 * Helper class for RestTemplate configuration with optimized connection, DNS cache management,
 * and DNS resolution logging.
 */
public final class RestTemplateHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateHelper.class);
    
    private static final int MAX_TOTAL_CONNECTIONS = 50;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 20;
    private static final int CONNECTION_TTL_SECONDS = 5;
    private static final Timeout CONNECTION_TIMEOUT = Timeout.of(5, TimeUnit.SECONDS);
    private static final Timeout SOCKET_TIMEOUT = Timeout.of(30, TimeUnit.SECONDS);
    
    private static final RestTemplate restTemplate;

    static {
        
        HttpClientProvider httpClientProvider = new HttpClientProvider();
        CloseableHttpClient httpClient = httpClientProvider.createHttpClient();
        ClientHttpRequestFactory httpRequestFactory = new CustomClientHttpRequestFactory(httpClient);
        restTemplate = new RestTemplate(httpRequestFactory);
    }

    public static RestTemplate getRestTemplate() {
        return restTemplate;
    }

    private RestTemplateHelper() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    private static class HttpClientProvider {
        private CloseableHttpClient createHttpClient() {
                HttpClientBuilder clientBuilder = HttpClients.custom();
                applyConfiguration(clientBuilder);
                return clientBuilder.build();
        }

        private void applyConfiguration(HttpClientBuilder builder) {
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(CONNECTION_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setTimeToLive(CONNECTION_TTL_SECONDS, TimeUnit.SECONDS)
                .build();

            PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(MAX_TOTAL_CONNECTIONS)
                .setMaxConnPerRoute(MAX_CONNECTIONS_PER_ROUTE)
                .setDefaultConnectionConfig(connectionConfig);

            builder.setConnectionManager(connectionManagerBuilder.build())
                   .setConnectionManagerShared(true)
                   .evictExpiredConnections()
                   .evictIdleConnections(Timeout.of(CONNECTION_TTL_SECONDS, TimeUnit.SECONDS));

            builder.setConnectionReuseStrategy((request, response, context) -> true);
        }
    }

    static class CustomClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {
        CustomClientHttpRequestFactory(CloseableHttpClient httpClient) {
            super(httpClient);
            setConnectTimeout((int) CONNECTION_TIMEOUT.toMilliseconds());
            setConnectionRequestTimeout((int) CONNECTION_TIMEOUT.toMilliseconds());
        }

        @Override
        public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
                // Log DNS resolution before making the request
            	if(LOGGER.isDebugEnabled()) {
            		logDnsResolution(uri);
            	}
                
                ClientHttpRequest request = super.createRequest(uri, httpMethod);
                request.getHeaders().add("Accept-Encoding", "gzip, deflate");
                request.getHeaders().add("Connection", "keep-alive");
                return request;
        }
        
        private void logDnsResolution(URI uri) {
            try {
                String host = uri.getHost();
                InetAddress[] addresses = InetAddress.getAllByName(host);
                
                LOGGER.info("DNS Resolution for host {}: {}", 
                    host, 
                    Arrays.stream(addresses)
                          .map(InetAddress::getHostAddress)
                          .toArray(String[]::new));
                
            } catch (UnknownHostException e) {
                LOGGER.error("DNS Resolution failed for host {}: {}", 
                    uri.getHost(), 
                    e.getMessage());
            }
        }
    }
    
    /**
     * Utility method to manually check DNS resolution for a hostname
     * @param hostname The hostname to resolve
     * @return Array of resolved IP addresses
     */
    public static String[] checkDnsResolution(String hostname) {
        try {
            InetAddress[] addresses = InetAddress.getAllByName(hostname);
            String[] ips = Arrays.stream(addresses)
                                .map(InetAddress::getHostAddress)
                                .toArray(String[]::new);
            
            if(LOGGER.isInfoEnabled()) {
            	LOGGER.info("Manual DNS check for {}: {}", hostname, Arrays.toString(ips));
            }
            return ips;
            
        } catch (UnknownHostException e) {
            LOGGER.error("Manual DNS check failed for {}: {}", hostname, e.getMessage());
            return new String[0];
        }
    }
}