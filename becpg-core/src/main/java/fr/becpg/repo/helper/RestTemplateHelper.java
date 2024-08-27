package fr.becpg.repo.helper;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class RestTemplateHelper {
	
	// Static instance of RestTemplate to be reused across the application
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

	// Private constructor to prevent instantiation
	private RestTemplateHelper() {
		// Do Nothing
	}

	private static class HttpClientProvider {

		private CloseableHttpClient createHttpClient() {
			try {
				HttpClientBuilder clientBuilder = HttpClients.custom();
				applyConfiguration(clientBuilder);
				return clientBuilder.build();
			} catch (Exception e) {
				throw new IllegalStateException("Failed to create ClientHttpRequestFactory. " + e.getMessage(), e);
			}
		}

		private void applyConfiguration(HttpClientBuilder builder) {
			   PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
	            connectionManager.setMaxTotal(50); // Set the maximum number of total connections
	            connectionManager.setDefaultMaxPerRoute(20); // Set the maximum number of connections per route
	            
	            // Set the time after which an idle connection will be revalidated
	            connectionManager.setValidateAfterInactivity(5000); // 5 seconds
	            builder.setConnectionTimeToLive(5, TimeUnit.SECONDS);

	            // Set up connection configuration (like TTL) and socket configuration
	            builder.setConnectionManager(connectionManager);
	            builder.setDefaultConnectionConfig(ConnectionConfig.custom().setBufferSize(8192).build());
	            builder.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout((int) TimeUnit.SECONDS.toMillis(5)).build());
	  
		}
	}

	static class CustomClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {
		CustomClientHttpRequestFactory(CloseableHttpClient httpClient) {
			super(httpClient);
		}

		@Override
		public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
			// This is to avoid the Brotli content encoding that is not well-supported by the combination of
			// the Apache Http Client and the Spring RestTemplate
			ClientHttpRequest request = super.createRequest(uri, httpMethod);
			request.getHeaders().add("Accept-Encoding", "gzip, deflate");
			return request;
		}
	}
	
}
