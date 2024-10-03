package fr.becpg.repo.helper;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * <p>RestTemplateHelper class.</p>
 *
 * @author matthieu
 */
public class RestTemplateHelper {
	
	// Static instance of RestTemplate to be reused across the application
    private static final RestTemplate restTemplate;

    static {
        HttpClientProvider httpClientProvider = new HttpClientProvider();
        CloseableHttpClient httpClient = httpClientProvider.createHttpClient();
        ClientHttpRequestFactory httpRequestFactory = new CustomClientHttpRequestFactory(httpClient);
        restTemplate = new RestTemplate(httpRequestFactory);
    }

    /**
     * <p>Getter for the field <code>restTemplate</code>.</p>
     *
     * @return a {@link org.springframework.web.client.RestTemplate} object
     */
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
			PoolingHttpClientConnectionManagerBuilder connectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
			connectionManagerBuilder.setMaxConnTotal(50); // Set the maximum number of total connections
			connectionManagerBuilder.setMaxConnPerRoute(20); // Set the maximum number of connections per route
			connectionManagerBuilder.setDefaultConnectionConfig(ConnectionConfig.custom().setTimeToLive(5, TimeUnit.SECONDS).build());
			builder.setConnectionManager(connectionManagerBuilder.build());
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
