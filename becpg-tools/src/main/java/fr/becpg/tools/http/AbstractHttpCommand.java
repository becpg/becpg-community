/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.tools.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

public abstract class AbstractHttpCommand {

	private final String serverUrl;
	
	protected static final Log logger = LogFactory.getLog(AbstractHttpCommand.class);
	
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



	public CloseableHttpResponse runCommand(CloseableHttpClient client, HttpContext context, Object... params) throws IOException{

		String url  = getHttpUrl( params);
		if(logger.isDebugEnabled()){
		 logger.debug("Run http command:"+url+" method "+ httpMethod.toString());
		}
	
		return client.execute(getHttpMethod(url,params), context);
		
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
