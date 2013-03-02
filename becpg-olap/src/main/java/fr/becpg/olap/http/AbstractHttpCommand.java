package fr.becpg.olap.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

public abstract class AbstractHttpCommand {

	private String serverUrl;
	
	protected static Log logger = LogFactory.getLog(AbstractHttpCommand.class);
	
	public enum HttpCommandMethod {
		METHOD_GET,METHOD_PUT,METHOD_POST,METHOD_DELETE
	}
	
	private HttpCommandMethod httpMethod = HttpCommandMethod.METHOD_GET;
	
	public AbstractHttpCommand(String serverUrl)  {
		super();
		this.serverUrl = serverUrl;
	}



	public void setHttpMethod(HttpCommandMethod httpMethod) {
		this.httpMethod = httpMethod;
	}



	public InputStream runCommand(HttpClient client , Object... params) throws IOException{

		HttpEntity entity = getEntity(client, params);
		if(entity!=null){
			return entity.getContent();
		}
		return null;

	}

	
	protected HttpEntity getEntity(HttpClient client , Object... params) throws IOException{
		
	
		String url  = getHttpUrl( params);
		if(logger.isDebugEnabled()){
		 logger.debug("Run http command:"+url);
		}

		HttpResponse response = client.execute(getHttpMethod(url,params));
		
		return response.getEntity();

	}
	
	
	private HttpUriRequest getHttpMethod(String url, Object[] params) {

		switch (httpMethod) {
		case METHOD_DELETE:
			return new HttpDelete(url);
		case METHOD_POST :
			return buildHttpPost(url, params);
		default:
			 return new HttpGet(url);
		}
		
		
	}



	protected HttpUriRequest buildHttpPost(String url, Object[] params){
		throw new IllegalStateException("Not implemented");
	}



	public abstract String getHttpUrl(Object... params);

	public String getServerUrl() {
		return serverUrl;
	}

	
	protected Object[] encodeParams(Object[] params) {
		for(int i=0;i< params.length;i++){
			if(params[i] instanceof String){
				try {
					params[i] = URLEncoder.encode((String)params[i],"UTF-8");
				} catch (UnsupportedEncodingException e) {
					logger.warn(e,e);
				}
			}
		}
		
		return params;
		
	}
	


	
	
}
