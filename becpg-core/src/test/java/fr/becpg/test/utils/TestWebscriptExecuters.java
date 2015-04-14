package fr.becpg.test.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

public class TestWebscriptExecuters {

	private HttpClient httpClient = null;

	private static Log logger = LogFactory.getLog(TestWebscriptExecuters.class);

	
	private static TestWebscriptExecuters instance = new TestWebscriptExecuters();
	
	
	
	
	public static TestWebscriptExecuters getInstance() {
		return instance;
	}

	
	
	private  TestWebscriptExecuters()  {
		
		httpClient = new HttpClient();
		httpClient.getParams().setBooleanParameter(HttpClientParams.PREEMPTIVE_AUTHENTICATION, true);
		httpClient.getState()
				.setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), new UsernamePasswordCredentials("admin", "admin"));
	}

	
	public static Response sendRequest(Request req, int expectedStatus, String asUser) throws IOException {
		return getInstance().internalSendRequest(req, expectedStatus, asUser);
	}
	
	public static Response sendRequest(Request req, int expectedStatus) throws IOException {
		return getInstance().internalSendRequest(req, expectedStatus, null);
	}


	private Response internalSendRequest(Request req, int expectedStatus, String asUser) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("");
			logger.debug("* Request: " + req.getMethod() + " " + req.getFullUri()
					+ (req.getBody() == null ? "" : "\n" + new String(req.getBody(), "UTF-8")));
		}

		Response res = sendRemoteRequest(req, expectedStatus);

		if (logger.isDebugEnabled()) {
			logger.debug("");
			logger.debug("* Response: " + res.getStatus() + " " + res.getContentType() + " " + req.getMethod() + " " + req.getFullUri() + "\n");
			logger.debug(res.getContentAsString());
		}

		if (expectedStatus > 0 && expectedStatus != res.getStatus()) {
			Assert.fail("Status code " + res.getStatus() + " returned, but expected " + expectedStatus + " for " + req.getFullUri() + " (" + req.getMethod()
					+ ")\n" + res.getContentAsString());
		}

		return res;
	}

	
	protected Response sendRemoteRequest(Request req, int expectedStatus) throws IOException {
		String uri = req.getFullUri();
		if (!uri.startsWith("http")) {
			uri = "http://localhost:8080/alfresco/service" + uri;
		}

		// construct method
		HttpMethod httpMethod = null;
		String method = req.getMethod();
		if (method.equalsIgnoreCase("GET")) {
			GetMethod get = new GetMethod(uri);
			httpMethod = get;
		} else if (method.equalsIgnoreCase("POST")) {
			PostMethod post = new PostMethod(uri);
			post.setRequestEntity(new ByteArrayRequestEntity(req.getBody(), req.getType()));
			httpMethod = post;
		} else if (method.equalsIgnoreCase("PATCH")) {
			PatchMethod post = new PatchMethod(uri);
			post.setRequestEntity(new ByteArrayRequestEntity(req.getBody(), req.getType()));
			httpMethod = post;
		} else if (method.equalsIgnoreCase("PUT")) {
			PutMethod put = new PutMethod(uri);
			put.setRequestEntity(new ByteArrayRequestEntity(req.getBody(), req.getType()));
			httpMethod = put;
		} else if (method.equalsIgnoreCase("DELETE")) {
			DeleteMethod del = new DeleteMethod(uri);
			httpMethod = del;
		} else {
			throw new AlfrescoRuntimeException("Http Method " + method + " not supported");
		}
		if (req.getHeaders() != null) {
			for (Map.Entry<String, String> header : req.getHeaders().entrySet()) {
				httpMethod.setRequestHeader(header.getKey(), header.getValue());
			}
		}

		// execute method
		httpClient.executeMethod(httpMethod);
		
		return new Response(httpMethod);
	}

	/**
	 * PATCH method
	 */
	public static class PatchMethod extends EntityEnclosingMethod {
		public PatchMethod(String uri) {
			super(uri);
		}

		@Override
		public String getName() {
			return "PATCH";
		}
	}



	/**
	 * HttpMethod wrapped as Web Script Test Response
	 */
	public static class Response {
		private HttpMethod method;

		public Response(HttpMethod method) {
			this.method = method;
		}

		public byte[] getContentAsByteArray() {
			try {
				return method.getResponseBody();
			} catch (IOException e) {
				return null;
			}
		}

		public String getContentAsString() throws UnsupportedEncodingException {
			try {
				return method.getResponseBodyAsString();
			} catch (IOException e) {
				return null;
			}
		}

		public String getContentType() {
			return getHeader("Content-Type");
		}

		public int getContentLength() {
			try {
				return method.getResponseBody().length;
			} catch (IOException e) {
				return 0;
			}
		}

		public String getHeader(String name) {
			Header header = method.getResponseHeader(name);
			return (header != null) ? header.getValue() : null;
		}

		public int getStatus() {
			return method.getStatusCode();
		}
		
		public void release(){

			 method.releaseConnection();
		}

	}
	
	
	 /**
     * A Web Script Test Request
     */
    public static class Request
    {
        private String method;
        private String uri;
        private Map<String, String> args;
        private Map<String, String> headers;
        private byte[] body;
        private String encoding = "UTF-8";
        private String contentType;
        
        public Request(Request req)
        {
            this.method = req.method;
            this.uri= req.uri;
            this.args = req.args;
            this.headers = req.headers;
            this.body = req.body;
            this.encoding = req.encoding;
            this.contentType = req.contentType;
        }
        
        public Request(String method, String uri)
        {
            this.method = method;
            this.uri = uri;
        }
        
        public String getMethod()
        {
            return method;
        }
        
        public String getUri()
        {
            return uri;
        }
        
        public String getFullUri()
        {
            // calculate full uri
            String fullUri = uri == null ? "" : uri;
            if (args != null && args.size() > 0)
            {
                char prefix = (uri.indexOf('?') == -1) ? '?' : '&';
                for (Map.Entry<String, String> arg : args.entrySet())
                {
                    fullUri += prefix + arg.getKey() + "=" + (arg.getValue() == null ? "" : arg.getValue());
                    prefix = '&';
                }
            }
            
            return fullUri;
        }
        
        public Request setArgs(Map<String, String> args)
        {
            this.args = args;
            return this;
        }
        
        public Map<String, String> getArgs()
        {
            return args;
        }

        public Request setHeaders(Map<String, String> headers)
        {
            this.headers = headers;
            return this;
        }
        
        public Map<String, String> getHeaders()
        {
            return headers;
        }
        
        public Request setBody(byte[] body)
        {
        	this.body = body;
            return this;
        }
        
        public byte[] getBody()
        {
            return body;
        }
        
        public Request setEncoding(String encoding)
        {
            this.encoding = encoding;
            return this;
        }
        
        public String getEncoding()
        {
            return encoding;
        }

        public Request setType(String contentType)
        {
            this.contentType = contentType;
            return this;
        }
        
        public String getType()
        {
            return contentType;
        }
    }
    
    /**
     * Test GET Request
     */
    public static class GetRequest extends Request
    {
        public GetRequest(String uri)
        {
            super("get", uri);
        }
    }

    /**
     * Test POST Request
     */
    public static class PostRequest extends Request
    {
        public PostRequest(String uri, String post, String contentType)
            throws UnsupportedEncodingException 
        {
            super("post", uri);
            setBody(getEncoding() == null ? post.getBytes() : post.getBytes(getEncoding()));
            setType(contentType);
        }

        public PostRequest(String uri, byte[] post, String contentType)
        {
            super("post", uri);
            setBody(post);
            setType(contentType);
        }
    }

    /**
     * Test PUT Request
     */
    public static class PutRequest extends Request
    {
        public PutRequest(String uri, String put, String contentType)
            throws UnsupportedEncodingException
        {
            super("put", uri);
            setBody(getEncoding() == null ? put.getBytes() : put.getBytes(getEncoding()));
            setType(contentType);
        }
        
        public PutRequest(String uri, byte[] put, String contentType)
        {
            super("put", uri);
            setBody(put);
            setType(contentType);
        }
    }

    /**
     * Test DELETE Request
     */
    public static class DeleteRequest extends Request
    {
        public DeleteRequest(String uri)
        {
            super("delete", uri);
        }
    }

    /**
     * Test PATCH Request
     */
    public static class PatchRequest extends Request
    {
        public PatchRequest(String uri, String put, String contentType)
            throws UnsupportedEncodingException
        {
            super("patch", uri);
            setBody(getEncoding() == null ? put.getBytes() : put.getBytes(getEncoding()));
            setType(contentType);
        }
        
        public PatchRequest(String uri, byte[] put, String contentType)
        {
            super("patch", uri);
            setBody(put);
            setType(contentType);
        }
    }

	
    
}
