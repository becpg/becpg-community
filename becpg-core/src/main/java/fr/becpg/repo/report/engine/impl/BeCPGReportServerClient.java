package fr.becpg.repo.report.engine.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Element;

import fr.becpg.repo.report.engine.BeCPGReportEngine;


//TODO close all stream
public class BeCPGReportServerClient implements BeCPGReportEngine{

	private static Log logger = LogFactory.getLog(BeCPGReportServerClient.class);
	
	private String reportServerUrl;
	

	private NodeService nodeService;
	
	
	private ContentService contentService;

	public void setReportServerUrl(String reportServerUrl) {
		this.reportServerUrl = reportServerUrl;
	}



	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}


	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}



	@Override
	public void createReport(NodeRef tplNodeRef, Element nodeElt,
			OutputStream out, Map<String, Object> params) {
		HttpClient httpclient = getHttpClient();

		try {
			String pingTemplateUrl = reportServerUrl+"/template?nodeRef="+tplNodeRef.toString();
			
			logger.debug("Ping beCPG Report"+ pingTemplateUrl);
			
			HttpGet httpGet = new HttpGet(pingTemplateUrl);
			
			HttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			
			Date dateModified = (Date) nodeService.getProperty(tplNodeRef,ContentModel.PROP_MODIFIED);
			//Timestamp or -1
			Long timeStamp = Long.parseLong(EntityUtils.toString(entity));
			if(timeStamp<0 || timeStamp<dateModified.getTime()){
				sendTemplate(httpclient,tplNodeRef );
			}
	
			String reportUrl = getReportUrl(params);
			
			logger.debug("Send Report command"+ reportUrl);
			
			HttpPost httpPost = new HttpPost(reportUrl);
			
			entity =  new StringEntity("xml=" + nodeElt.asXML(),"UTF-8");
			
			httpPost.setEntity(entity);
			response = httpclient.execute(httpPost);
			//keep that as we should read the response
			entity = response.getEntity();
			if (entity != null) {
			    try {
			    	entity.writeTo(out);
			    } finally {
			       IOUtils.closeQuietly(entity.getContent());
			    }
			}
			
			
		
			
			
		} catch (HttpHostConnectException conEx){
			logger.warn("Report failed : Cannot connect to report server");
		} catch (Exception e) {
			logger.error(e,e);
		} finally {
			IOUtils.closeQuietly(out);
		}
		
		
	}
	
	

	private String getReportUrl(Map<String, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}



	private void sendTemplate(HttpClient httpclient, NodeRef tplNodeRef) throws ClientProtocolException, IOException {
		String sentTemplateUrl = reportServerUrl+"/template?nodeRef="+tplNodeRef.toString();
		
		logger.debug("Send Template command"+ sentTemplateUrl);

		HttpPost httpPost = new HttpPost(sentTemplateUrl);


		ContentReader reader = contentService.getReader(tplNodeRef, ContentModel.PROP_CONTENT);
	
		
		HttpEntity entity =  new InputStreamEntity(reader.getContentInputStream(), reader.getSize());
		
		httpPost.setEntity(entity);
		httpclient.execute(httpPost);
		
	}

	private HttpClient getHttpClient() {
//		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(olapUser, olapPassword);
//		CredentialsProvider credsProvider = new BasicCredentialsProvider();
//		credsProvider.setCredentials(
//			    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), 
//			    creds);
//		
		DefaultHttpClient httpclient = new DefaultHttpClient();
//		httpclient.setCredentialsProvider(credsProvider);
		
		
		return httpclient;
	}

}
