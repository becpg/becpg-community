/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.olap;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StopWatch;

import fr.becpg.repo.olap.data.OlapContext;
import fr.becpg.repo.olap.impl.OlapServiceImpl;

public class OlapUtils {
	
	private static final String ALF_USER_HEADER = "ALF_USER";

	private static final Log logger = LogFactory.getLog(OlapServiceImpl.class);

	
	
	public static OlapContext createOlapContext(String userName){

		HttpClient httpClient = HttpClientBuilder.create().build();
		
		return new OlapContext(httpClient, userName);
	}
	

	

	public static JSONObject readJsonObjectFromUrl(String url,
			OlapContext olapContext) throws IOException, JSONException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		String ret="";
		try {
			HttpGet httpget = new HttpGet(url);
			httpget.addHeader(ALF_USER_HEADER, olapContext.getCurrentUser());
			HttpResponse response = olapContext.getSession().execute(httpget);
			HttpEntity entity = response.getEntity();
			ret = EntityUtils.toString(entity, "UTF-8");
			JSONObject json = new JSONObject(ret);

			return json;
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("Retrivied JSON Data from :" + url + " in " + watch.getTotalTimeSeconds() + " seconds");
				logger.debug("Value : " + ret);

			}
			
		}
	}
	
	public static JSONArray readJsonArrayFromUrl(String buildDataUrl,
			OlapContext olapContext) throws  IOException, JSONException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		String ret="";
		try {
			HttpGet httpget = new HttpGet(buildDataUrl);
			httpget.addHeader(ALF_USER_HEADER, olapContext.getCurrentUser());
			HttpResponse response = olapContext.getSession().execute(httpget);
			HttpEntity entity = response.getEntity();
			ret = EntityUtils.toString(entity, "UTF-8");
			JSONArray json = new JSONArray(ret);

			return json;
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("Retrivied JSON Data from :" + buildDataUrl + " in " + watch.getTotalTimeSeconds() + " seconds");
				logger.debug("Value : " + ret);

			}
			
		}
	}
	
	public static void sendCreateQueryPostRequest(OlapContext olapContext, String postUrl ,  String xml) throws IOException {
		

		if(logger.isDebugEnabled()){
			logger.debug("Send POST request:\n"+xml+"\n to "+postUrl);
		}
		
		HttpPost httpPost = new HttpPost(postUrl);

		httpPost.addHeader(ALF_USER_HEADER, olapContext.getCurrentUser());
		HttpEntity entity = new StringEntity("xml=" + xml,"UTF-8");
		
		httpPost.setEntity(entity);
		HttpResponse response = olapContext.getSession().execute(httpPost);
		//keep that as we should read the response
		entity = response.getEntity();

		String ret = EntityUtils.toString(entity);

		if(logger.isDebugEnabled()){
			logger.debug("Ret: "+ret);
		}
		
		
	}



	//TODO crappy !!!
	public static Object convert(String value) {
		if(value==null || value.isEmpty()){
			return new Long(0);
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			try {
				return Double.parseDouble(value.replace(",","."));
			} catch (NumberFormatException ignored) {
			}
		}
		return value;
	}


	

}
