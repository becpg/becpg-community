package fr.becpg.repo.olap;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StopWatch;

import fr.becpg.repo.olap.impl.OlapServiceImpl;

public class OlapUtils {
	
	

	private static Log logger = LogFactory.getLog(OlapServiceImpl.class);

	

	public static JSONObject readJsonObjectFromUrl(String url,
			HttpClient httpclient) throws IOException, JSONException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		String ret="";
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			ret = EntityUtils.toString(entity);
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
			HttpClient httpclient) throws  IOException, JSONException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		String ret="";
		try {
			HttpGet httpget = new HttpGet(buildDataUrl);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			ret = EntityUtils.toString(entity);
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


	

}
