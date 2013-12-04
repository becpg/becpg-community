package fr.becpg.report.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


/**
 * Abstract Report server client proxy
 * @author matthieu
 *
 */
public abstract class AbstractBeCPGReportClient {

	protected static Log logger = LogFactory.getLog(AbstractBeCPGReportClient.class);
	

	protected String reportServerUrl;
	
	protected String userName;
	
	protected String password;
	

	public void setReportServerUrl(String reportServerUrl) {
		this.reportServerUrl = reportServerUrl;
	}

	
	public void setUserName(String userName) {
		this.userName = userName;
	}


	public void setPassword(String password) {
		this.password = password;
	}


	protected HttpClient getHttpClient() {
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		

		if(userName!=null && password!=null){
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(userName, password);
			CredentialsProvider credsProvider = new BasicCredentialsProvider();
			credsProvider.setCredentials(
				    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), 
				    creds);
			httpclient.setCredentialsProvider(credsProvider);
		}
		
		return httpclient;
	}


	protected Long getTemplateTimeStamp(ReportSession reportSession, String templateId) throws ClientProtocolException, IOException {

		reportSession.setTemplateId(templateId);
		
		String templateTimeStampUrl =  reportSession.getTemplateTimeStampUrl();;
		
		logger.debug("Get Template TimeStamp at: "+ templateTimeStampUrl);
		
		HttpGet httpGet = new HttpGet(templateTimeStampUrl);
		
		HttpResponse response = reportSession.getHttpClient().execute(httpGet);
		HttpEntity entity = response.getEntity();
		String ret = EntityUtils.toString(entity);
		
		try {
			return Long.parseLong(ret);
		} catch (NumberFormatException e) {
			logger.error("Unable to parse response",e);
			if(logger.isDebugEnabled()){
				logger.debug("Server response :"+ret);
			}
			return null;
		}
	}
	
	

	protected void saveTemplate(ReportSession reportSession, InputStream in) throws ClientProtocolException, IOException, ReportException {
			String saveTemplateUrl = reportSession.getSaveTemplateUrl(); 
		
		logger.debug("Save Template at: "+ saveTemplateUrl);
		
		HttpPost httpPost = new HttpPost(saveTemplateUrl);

		HttpEntity entity =  new InputStreamEntity(in,-1);
		
		httpPost.setEntity(entity);
		
		HttpResponse response = reportSession.getHttpClient().execute(httpPost);
	    entity = response.getEntity();
		
	    Long timeStamp = Long.parseLong(EntityUtils.toString(entity));
	    if(timeStamp == null || timeStamp<0){
	    	throw new ReportException("Error sending template");
	    }
	    
		
	}
	

	protected void sendImage(ReportSession reportSession, String imageName , InputStream in) throws ClientProtocolException, IOException, ReportException {
			
		String sendImageUrl = reportSession.getSendImageUrl(imageName); 
		
		logger.debug("Send image at: "+ sendImageUrl);
		
		HttpPost httpPost = new HttpPost(sendImageUrl);

		HttpEntity entity =  new InputStreamEntity(in,-1);
		
		httpPost.setEntity(entity);
		
		HttpResponse response = reportSession.getHttpClient().execute(httpPost);
	    entity = response.getEntity();
		
	    String ret = EntityUtils.toString(entity);
	    if(ret == null || !ret.equals(ReportParams.RESP_OK)){
	    	throw new ReportException("Error sending image");
	    }
	    
	    
	    
		
	}
	
	
	protected void generateReport(ReportSession reportSession, String format,
			InputStream in, OutputStream out) throws ClientProtocolException, IOException{

		
		reportSession.setFormat(format);
		
		String reportUrl = reportSession.getReportUrl();
		
		logger.debug("Send Report command: "+ reportUrl);
		
		HttpPost httpPost = new HttpPost(reportUrl);
		
		HttpEntity entity =  new InputStreamEntity(in,-1);
		
		httpPost.setEntity(entity);
		HttpResponse response = reportSession.getHttpClient().execute(httpPost);
		entity = response.getEntity();
		if (entity != null) {
			 in = entity.getContent();
	    	
		    try {
		    	int l;
		    	byte[] tmp = new byte[2048];
		    	while ((l = in.read(tmp)) != -1) {
		    	    out.write(tmp, 0, l);
		    	}
		    	
		    } finally {
		    	in.close();
		    	out.flush();
		    	out.close();
		    	
		    }
		}
		
	}
	
	/*
	 * Usefull pattern
	 * 
	 */
	
	
	protected void executeInSession(ReportSessionCallBack callBack) throws ReportException{
		HttpClient httpClient = getHttpClient();
		
		ReportSession reportSession = new ReportSession(httpClient);
		
		try {
			callBack.doInReportSession(reportSession);
		}  catch (HttpHostConnectException conEx){
			logger.warn("Report failed : Cannot connect to report server");
		} catch (ClientProtocolException e) {
			throw new ReportException(e);
		} catch (IOException e) {
			throw new ReportException(e);
		} 	
		
	}
	
	
	protected interface ReportSessionCallBack {

		void doInReportSession(ReportSession reportSession) throws ClientProtocolException, IOException, ReportException;

	}
	
	
	protected class ReportSession {

		String templateId;
		
		String format;
		
		HttpClient httpClient;

		public ReportSession(HttpClient httpClient) {
			super();
			this.httpClient = httpClient;
		}

		public String getSendImageUrl(String imageName) {
			return  reportServerUrl+"/report?"+ReportParams.TEMPLATE_ID_PARAM+"="+imageName+"&"+ReportParams.PARAM_FORMAT+"="+ReportParams.PARAM_IMAGES;
		}

		public String getReportUrl() {
			return  reportServerUrl+"/report?"+ReportParams.TEMPLATE_ID_PARAM+"="+templateId+"&"+ReportParams.PARAM_FORMAT+"="+format;
		}

		public String getSaveTemplateUrl() {
			return getTemplateTimeStampUrl();
		}

		public String getTemplateTimeStampUrl() {
			return   reportServerUrl+"/template?"+ReportParams.TEMPLATE_ID_PARAM+"="+templateId;
		}

		public String getTemplateId() {
			return templateId;
		}

		public void setTemplateId(String templateId) {
			this.templateId = templateId;
		}

		public HttpClient getHttpClient() {
			return httpClient;
		}

		public void setHttpClient(HttpClient httpClient) {
			this.httpClient = httpClient;
		}

		public void setFormat(String format) {
			this.format = format;
		}
		
		
		
		
	}
	
}
