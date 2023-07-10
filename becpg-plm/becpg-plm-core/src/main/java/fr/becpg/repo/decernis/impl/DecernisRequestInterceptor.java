package fr.becpg.repo.decernis.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * <p>DecernisRequestInterceptor class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DecernisRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(DecernisRequestInterceptor.class);

    /** {@inheritDoc} */
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        traceRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        traceResponse(response);
        return response;
    }

    private void traceRequest(HttpRequest request, byte[] body) throws IOException {
    	if (log.isTraceEnabled()) {
    		log.trace("===========================request begin================================================");
    		log.trace("URI         : {}", request.getURI());
    		log.trace("Method      : {}", request.getMethod());
    		log.trace("Headers     : {}", request.getHeaders() );
    		log.trace("Request body: {}", new String(body, "UTF-8"));
    	}
    }

    private void traceResponse(ClientHttpResponse response) throws IOException {
    	if (log.isTraceEnabled()) {
    		StringBuilder inputStringBuilder = new StringBuilder();
    		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), "UTF-8"));
    		String line = bufferedReader.readLine();
    		while (line != null) {
    			inputStringBuilder.append(line);
    			inputStringBuilder.append('\n');
    			line = bufferedReader.readLine();
    		}
    		log.trace("Status code  : {}", response.getStatusCode());
    		log.trace("Status text  : {}", response.getStatusText());
    		log.trace("Headers      : {}", response.getHeaders());
    		log.trace("Response body: {}", inputStringBuilder.toString());
    		log.trace("=======================response end======================================================");
    	}
    }

}
