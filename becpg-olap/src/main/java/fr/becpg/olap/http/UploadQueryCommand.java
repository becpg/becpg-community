package fr.becpg.olap.http;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;


public class UploadQueryCommand  extends AbstractHttpCommand {


	private static String COMMAND_URL_TEMPLATE = "/api/upload";

	
	public UploadQueryCommand(String serverUrl) {
		super(serverUrl);
		setHttpMethod(HttpCommandMethod.METHOD_POST);
	}

	
	@Override
	protected HttpUriRequest buildHttpPost(String url, Object[] params) {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
	
		
		HttpPost postRequest = new HttpPost(url);
		if(params.length>2){
			//Case create
			formparams.add(new BasicNameValuePair("uploaddirectory", (String)params[0]));
			formparams.add(new BasicNameValuePair("filename", (String)params[1]));
			formparams.add(new BasicNameValuePair("filedata", (String)params[2]));
		
		} else {
			//Case update
			formparams.add(new BasicNameValuePair("updatenoderef", (String)params[0]));
			formparams.add(new BasicNameValuePair("filedata", (String)params[1]));
		}
		
		UrlEncodedFormEntity entity;
		try {
			entity = new UrlEncodedFormEntity(formparams, "UTF-8");

			postRequest.setEntity(entity);
	 
		} catch (UnsupportedEncodingException e) {
			logger.error(e,e);
		}
		return postRequest;
	}
	
	
	@Override
	public String getHttpUrl(Object... params) {
	
		return getServerUrl() + COMMAND_URL_TEMPLATE;
	}
	
	
}
