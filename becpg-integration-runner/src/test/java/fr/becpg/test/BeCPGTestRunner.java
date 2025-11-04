/**
 * Copyright (C) 2017 Alfresco Software Limited.
 * <p/>
 * This file is part of the Alfresco SDK project.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.becpg.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.alfresco.rad.SpringContextHolder;
import org.alfresco.rad.test.Remote;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>
 * This is a JUnit test runner that is designed to work with an Alfresco
 * repository. It detects if it's executing a test inside of a running Alfresco
 * instance. If that is the case the tests are all run normally. If however the
 * test is being run from outside the repository, from the maven command line or
 * from an IDE such as IntelliJ or STS/Eclipse for example, then instead of
 * running the actual test an HTTP request is made to a Web Script in a running
 * Alfresco instance. This Web Script runs the test and returns enough
 * information to this class so we can emulate having run the test locally.
 * </p>
 * <p>
 * By doing this, we are able to create Integration Tests (IT) using standard
 * JUnit capabilities. These can then be run from our IDEs with the associated
 * visualizations, support for re-running failed tests, etc.
 * </p>
 * Integration testing framework donated by Zia Consulting
 *
 * @author Bindu Wavell (bindu@ziaconsulting.com)
 * @author martin.bergljung@alfresco.com (some editing)
 * @since 3.0
 */
public class BeCPGTestRunner extends SpringJUnit4ClassRunner {
	private static final String ACS_ENDPOINT_PROP = "acs.endpoint.path";
	private static final String ACS_DEFAULT_ENDPOINT = "http://localhost:8080/alfresco";
	private static final String ACS_USERNAME_PROP = "acs.test.username";
	private static final String ACS_PASSWORD_PROP = "acs.test.password";
	private static final String DEFAULT_USERNAME = "admin";
	private static final String DEFAULT_PASSWORD = "becpg";

	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";

	private static final int CONNECT_TIMEOUT_MS = 30000;
	private static final int SOCKET_TIMEOUT_MS = 600000;
	private static final int CONNECTION_REQUEST_TIMEOUT_MS = 10000;
	private static final int MAX_RETRY_ATTEMPTS = 3;
	private static final int MAX_TOTAL_CONNECTIONS = 10;
	private static final int MAX_CONNECTIONS_PER_ROUTE = 5;

	private static Log logger = LogFactory.getLog(BeCPGTestRunner.class);

	public BeCPGTestRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	public static String serializableToString(Serializable serializable) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
			 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(serializable);
			return Base64.encodeBase64URLSafeString(baos.toByteArray());
		}
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		if (areWeRunningInAlfresco()) {
			// Just run the test as normally
			super.runChild(method, notifier);
		} else {
			// We are not running in an Alfresco Server, we need to call one and
			// have it execute the test...
			Description desc = describeChild(method);
			if (method.getAnnotation(Ignore.class) != null) {
				notifier.fireTestIgnored(desc);
			} else {
				callProxiedChild(method, notifier, desc);
			}
		}
	}

	/**
	 * Call a remote Alfresco server and have the test run there.
	 *
	 * @param method the test method to run
	 * @param notifier given @{@link RunNotifier} to notify the result of the test
	 * @param desc given @{@link Description} of the test to run
	 */
	protected void callProxiedChild(FrameworkMethod method, RunNotifier notifier, Description desc) {
		notifier.fireTestStarted(desc);

		String className = this.getTestClass().getJavaClass().getCanonicalName();
		String methodName = method.getName();
		if (null != methodName) {
			className += "#" + methodName;
		}

		try (CloseableHttpClient httpclient = createHttpClient()) {
			String fullUrl = buildTestUrl(className, method);
			HttpGet get = new HttpGet(fullUrl);

			long startTime = System.currentTimeMillis();
			HttpResponse resp = httpclient.execute(get);
			long duration = System.currentTimeMillis() - startTime;

			if (logger.isDebugEnabled()) {
				logger.debug("Remote test completed in " + duration + "ms: " + className + " at " + fullUrl);
			}

			int statusCode = resp.getStatusLine().getStatusCode();
			if (statusCode != HttpStatus.SC_OK) {
				String errorMsg = String.format("HTTP request failed with status %d: %s", 
					statusCode, resp.getStatusLine().getReasonPhrase());
				notifier.fireTestFailure(new Failure(desc, new IOException(errorMsg)));
				return;
			}

			String responseBody = readResponseBody(resp);
			processTestResponse(responseBody, notifier, desc);

		} catch (IOException e) {
			logger.error("IOException while executing proxied test: " + className, e);
			notifier.fireTestFailure(new Failure(desc, e));
		} catch (Exception e) {
			logger.error("Unexpected error while executing proxied test: " + className, e);
			notifier.fireTestFailure(new Failure(desc, e));
		}
	}

	/**
	 * Create configured HTTP client with credentials, timeouts, retry logic, and connection pooling.
	 */
	private CloseableHttpClient createHttpClient() {
		String username = System.getProperty(ACS_USERNAME_PROP, DEFAULT_USERNAME);
		String password = System.getProperty(ACS_PASSWORD_PROP, DEFAULT_PASSWORD);

		CredentialsProvider provider = new BasicCredentialsProvider();
		UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
		provider.setCredentials(AuthScope.ANY, credentials);

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout(CONNECT_TIMEOUT_MS)
				.setSocketTimeout(SOCKET_TIMEOUT_MS)
				.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MS)
				.build();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(MAX_TOTAL_CONNECTIONS);
		connectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);

		HttpRequestRetryHandler retryHandler = (exception, executionCount, context) -> {
			if (executionCount > MAX_RETRY_ATTEMPTS) {
				return false;
			}
			if (exception instanceof java.net.SocketTimeoutException) {
				logger.warn("Socket timeout on attempt " + executionCount + ", retrying...");
				return true;
			}
			if (exception instanceof ConnectTimeoutException) {
				logger.warn("Connection timeout on attempt " + executionCount + ", retrying...");
				return true;
			}
			if (exception instanceof java.net.UnknownHostException) {
				logger.warn("Unknown host on attempt " + executionCount + ", retrying...");
				return true;
			}
			if (exception instanceof java.net.NoRouteToHostException) {
				logger.warn("No route to host on attempt " + executionCount + ", retrying...");
				return true;
			}
			return false;
		};

		return HttpClientBuilder.create()
				.setDefaultCredentialsProvider(provider)
				.setDefaultRequestConfig(requestConfig)
				.setConnectionManager(connectionManager)
				.setRetryHandler(retryHandler)
				.build();
	}

	/**
	 * Build the full URL for the test web script.
	 */
	private String buildTestUrl(String className, FrameworkMethod method) {
		String encodedClassName = URLEncoder.encode(className, StandardCharsets.UTF_8);
		String testWebScriptUrl = "/service/testing/test.xml?clazz=" + encodedClassName;
		return getContextRoot(method) + testWebScriptUrl;
	}

	/**
	 * Read the response body from HTTP response.
	 */
	private String readResponseBody(HttpResponse resp) throws IOException {
		StringBuilder body = new StringBuilder();
		try (InputStream is = resp.getEntity().getContent();
			 InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8);
			 BufferedReader br = new BufferedReader(ir)) {
			
			String line;
			while ((line = br.readLine()) != null) {
				body.append(line).append("\n");
			}
		}
		return body.toString();
	}

	/**
	 * Process the XML response from the test execution.
	 */
	private void processTestResponse(String responseBody, RunNotifier notifier, Description desc) {
		if (StringUtils.isBlank(responseBody)) {
			notifier.fireTestFailure(new Failure(desc, 
				new IOException("Attempt to proxy test into running Alfresco instance failed, no response received")));
			return;
		}

		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			// Disable external entity processing for security
			dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(new InputSource(new StringReader(responseBody)));

			Element root = doc.getDocumentElement();
			NodeList results = root.getElementsByTagName("result");
			
			if (results == null || results.getLength() == 0) {
				notifier.fireTestFailure(new Failure(desc, 
					new IOException("Unable to process response for proxied test request: " + responseBody)));
				return;
			}

			String result = results.item(0).getFirstChild().getNodeValue();
			if (SUCCESS.equals(result)) {
				notifier.fireTestFinished(desc);
			} else {
				handleTestFailure(root, notifier, desc);
			}
		} catch (ParserConfigurationException | SAXException e) {
			logger.error("Cannot parse response body: " + responseBody, e);
			notifier.fireTestFailure(new Failure(desc, e));
		} catch (IOException e) {
			logger.error("Error processing test response", e);
			notifier.fireTestFailure(new Failure(desc, e));
		}
	}

	/**
	 * Handle test failure by extracting and deserializing throwable information.
	 */
	private void handleTestFailure(Element root, RunNotifier notifier, Description desc) {
		boolean failureFired = false;
		NodeList throwableNodes = root.getElementsByTagName("throwable");
		
		for (int tid = 0; tid < throwableNodes.getLength(); tid++) {
			String throwableBody = throwableNodes.item(tid).getFirstChild().getNodeValue();
			if (StringUtils.isNotBlank(throwableBody)) {
				try {
					Object object = objectFromString(throwableBody);
					if (object instanceof Throwable) {
						notifier.fireTestFailure(new Failure(desc, (Throwable) object));
						failureFired = true;
					} else {
						logger.warn("Deserialized object is not a Throwable: " + object.getClass().getName());
					}
				} catch (IOException | ClassNotFoundException e) {
					logger.error("Failed to deserialize throwable from response", e);
					notifier.fireTestFailure(new Failure(desc, 
						new IOException("Unable to deserialize exception: " + throwableBody, e)));
					failureFired = true;
				}
			}
		}
		
		if (!failureFired) {
			notifier.fireTestFailure(new Failure(desc, 
				new AssertionError("Test failed but no throwable information was available")));
		}
	}

	/**
	 * Deserialize an object from a Base64 encoded string.
	 */
	protected static Object objectFromString(String string) throws IOException, ClassNotFoundException {
		byte[] buffer = Base64.decodeBase64(string);
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer))) {
			return ois.readObject();
		}
	}

	/**
	 * Check if we are running this test in an Alfresco server instance.
	 *
	 * @return true if we are running in an Alfresco server
	 */
	protected boolean areWeRunningInAlfresco() {
		Object contextHolder = SpringContextHolder.Instance();
		return (contextHolder != null);
	}

	/**
	 * Check the @Remote config on the test class to see where the Alfresco Repo
	 * is running. If it is not present, check the ACS_ENDPOINT_PROP system
	 * property as an alternative location. If none of them has a value, then
	 * return the default location.
	 *
	 * @param method given @{@link FrameworkMethod} to be executed
	 * @return the ACS endpoint
	 */
	protected String getContextRoot(FrameworkMethod method) {
		Class<?> declaringClass = method.getMethod().getDeclaringClass();
		boolean annotationPresent = declaringClass.isAnnotationPresent(Remote.class);
		if (annotationPresent) {
			Remote annotation = declaringClass.getAnnotation(Remote.class);
			return annotation.endpoint();
		}

		final String platformEndpoint = System.getProperty(ACS_ENDPOINT_PROP);
		return StringUtils.isNotBlank(platformEndpoint) ? platformEndpoint : ACS_DEFAULT_ENDPOINT;
	}
}