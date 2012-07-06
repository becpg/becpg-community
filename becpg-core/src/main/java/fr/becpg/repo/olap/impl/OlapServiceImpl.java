package fr.becpg.repo.olap.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.olap.OlapService;
import fr.becpg.repo.olap.OlapUtils;
import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;
import fr.becpg.repo.olap.data.OlapChartMetadata;

public class OlapServiceImpl implements OlapService {

	private String olapUser;

	private String olapPassword;

	private String olapServerUrl;
	
	private BeCPGCacheService beCPGCacheService;
	

	private static String ROW_HEADER = "ROW_HEADER_HEADER";

	private static Log logger = LogFactory.getLog(OlapServiceImpl.class);

	public void setOlapUser(String olapUser) {
		this.olapUser = olapUser;
	}

	public void setOlapPassword(String olapPassword) {
		this.olapPassword = olapPassword;
	}

	public void setOlapServerUrl(String olapServerUrl) {
		this.olapServerUrl = olapServerUrl;
	}

	
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	@Override
	public List<OlapChart> retrieveOlapCharts()  {
		
		return beCPGCacheService.getFromCache(OlapService.class.getName(),"olapCharts" , new BeCPGCacheDataProviderCallBack<List<OlapChart>>() {

			@Override
			public List<OlapChart> getData() {
				HttpClient httpClient = getHttpClient();

				List<OlapChart> olapCharts = new ArrayList<OlapChart>();
				try {
					JSONArray jsonArray = OlapUtils.readJsonArrayFromUrl(buildRepositoryUrl(),httpClient);
				
					if (jsonArray != null) {
	
						for (int row = 0; row < jsonArray.length(); row++) {
							String queryName = jsonArray.getJSONObject(row).getString("name");
							try {
								
								OlapChart chart = new OlapChart(queryName);
								
								JSONObject json = OlapUtils.readJsonObjectFromUrl(buildQueryUrl(queryName),httpClient);
								
								chart.load(json.getString("xml"));
								olapCharts.add(chart);
							} catch (Exception e) {
								logger.error("Cannot load query :"+queryName);
								logger.debug(e,e);
							}
						}
	
					}
				} catch (Exception e) {
					logger.error(e,e);
				}
				
				return olapCharts;
			}
			
		});
		
		
	}

	
	
	
	

	private String sendCreateQueryPostRequest(HttpClient httpclient, String xml) throws IOException {
		String uuid = UUID.randomUUID().toString();
		
		String postUrl = buildCreateQueryUrl(uuid);

		if(logger.isDebugEnabled()){
			logger.debug("Send POST request:\n"+xml+"\n to "+postUrl);
		}
		
		HttpPost httpPost = new HttpPost(postUrl);

		
		HttpEntity entity = new StringEntity("xml=" + xml,"UTF-8");
		
		httpPost.setEntity(entity);
		HttpResponse response = httpclient.execute(httpPost);
		//keep that as we should read the response
		entity = response.getEntity();
		String ret = EntityUtils.toString(entity);
		logger.debug("Ret: "+ret);
		
		
		return uuid;
	}

	/**
	 * Parse [
	 * [{"value":"null","properties":{},"type":"COLUMN_HEADER"},{"value":
	 * "Oct","properties":{},"type":"COLUMN_HEADER"}]
	 * ,[{"value":"Type","properties":{"levelindex":"0"}
	 * ,"type":"ROW_HEADER_HEADER"}
	 * ,{"value":"Week41/2011","properties":{"levelindex"
	 * :"1","dimension":"Date de modification"
	 * },"type":"COLUMN_HEADER"}],[{"value"
	 * :"finishedProduct","properties":{"levelindex"
	 * :"0","dimension":"Type de produit"
	 * },"type":"ROW_HEADER"},{"value":"10","properties"
	 * :null,"type":"DATA_CELL"}
	 * ],[{"value":"rawMaterial","properties":{"levelindex"
	 * :"0","dimension":"Type de produit"
	 * },"type":"ROW_HEADER"},{"value":"14","properties"
	 * :null,"type":"DATA_CELL"}]]
	 * 
	 * @throws JSONException
	 * @throws IOException
	 */
	@Override
	public OlapChartData retrieveChartData(String olapQueryId) throws IOException, JSONException {

		
		HttpClient httpclient = getHttpClient();
		
		OlapChart chart = getOlapChart(olapQueryId);
		
		OlapChartData ret = new OlapChartData();
		if(chart!=null){
			String uuid = sendCreateQueryPostRequest(httpclient, chart.getXml());
			JSONArray jsonArray = OlapUtils.readJsonObjectFromUrl(buildDataUrl(uuid),httpclient).getJSONArray("cellset");
	
			if (jsonArray != null) {
	
				int lowest_level = 0;
				for (int row = 0; row < jsonArray.length(); row++) {	
					JSONArray cur =  jsonArray.getJSONArray(row);
					if (ROW_HEADER.equals(cur.getJSONObject(0).getString("type"))) {
						for (int field = 0; field < cur.length(); field++) {
							if (ROW_HEADER.equals(cur.getJSONObject(field).getString("type"))) {
								ret.shiftMetadata();
								lowest_level = field;
							}
							ret.addMetadata(new OlapChartMetadata(field,
									retrieveDataType(jsonArray.getJSONArray(row + 1).getJSONObject(field).getString("value")),
									cur.getJSONObject(field).getString("value")));
						}
					} else if (cur.getJSONObject(0).getString("value") != null) {
						List<Object> record = new ArrayList<Object>();
						for (int col = lowest_level; col < cur.length(); col++) {
							String value = cur.getJSONObject(col).getString("value");
							record.add(convert(value));
						}
						ret.getResultsets().add(record);
					}
				}
			}
		} 
		return ret;
	}

	private HttpClient getHttpClient() {
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(olapUser, olapPassword);
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
			    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), 
			    creds);
		
		DefaultHttpClient httpclient = new DefaultHttpClient();
		httpclient.setCredentialsProvider(credsProvider);
		
		
		return httpclient;
	}

	private OlapChart getOlapChart(String olapQueryId) throws JSONException, IOException {
		for(OlapChart chart : retrieveOlapCharts()){
			if(chart.getQueryId().equals(olapQueryId)){
				return chart;
			}
		}
		logger.warn("No chart found for id:"+olapQueryId);
		return null;
	}

	//TODO crappy !!!
	private Object convert(String value) {
		if(value==null || value.isEmpty()){
			return new Long(0);
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			try {
				return Double.parseDouble(value.replace(",","."));
			} catch (NumberFormatException e2) {
			}
		}
		return value;
	}

	private String retrieveDataType(String value) {
		return convert(value).getClass().getSimpleName();
	}

	private String buildDataUrl(String olapQueryId) {
		return  olapServerUrl + "/rest/saiku/"+olapUser+"/query/" + olapQueryId
				+ "/result/cheat";
	}
	
	private String buildCreateQueryUrl(String olapQueryId) {
		return  olapServerUrl + "/rest/saiku/"+olapUser+"/query/" + olapQueryId;
	}
	
	private String buildRepositoryUrl() {
		return olapServerUrl + "/rest/saiku/"+olapUser+"/repository";
	}
	

	private String buildQueryUrl(String queryName) throws  URIException {
		return URIUtil.encodePath(buildRepositoryUrl()+"/"+queryName,"UTF-8");
	}

	
	
	
	

}
