package fr.becpg.repo.olap.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.util.StopWatch;

import fr.becpg.repo.olap.OlapService;
import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;
import fr.becpg.repo.olap.data.OlapChartMetadata;

public class OlapServiceImpl implements OlapService {

	private String olapUser;

	private String olapPassword;

	private String olapServerUrl;

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

	@Override
	public List<OlapChart> retrieveOlapCharts() throws JSONException, IOException  {
		
		
		List<OlapChart> olapCharts = new ArrayList<OlapChart>();
		JSONArray jsonArray = readJsonFromUrl(buildRepositoryUrl());
		
		if (jsonArray != null) {

			for (int row = 0; row < jsonArray.length(); row++) {
				String queryName = jsonArray.getJSONObject(row).getString("name");
				try {
					
					OlapChart chart = new OlapChart(queryName);
					String xml  = chart.load(buildQueryUrl(queryName));
					sendCreateQueryPostRequest(xml,chart.getQueryId());
					olapCharts.add(chart);
				} catch (Exception e) {
					logger.error("Cannot load query :"+queryName,e);
				}
			}
			
			
		}
		
		return olapCharts;
	}


	private String sendCreateQueryPostRequest(String xml, String olapQueryId) throws IOException {


		logger.debug("Send POST request:\n"+xml);
		
		String stringToReverse = URLEncoder.encode(xml, "UTF-8");

		URL url = new URL(buildCreateQueryUrl(olapQueryId));
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);

		OutputStreamWriter out = new OutputStreamWriter(
	                              connection.getOutputStream());
		out.write("xml=" + stringToReverse);
		out.close();
		
		
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
				connection.getInputStream()));
		try {
			String decodedString;
			
			while ((decodedString = in.readLine()) != null) {
				//keep it as it's important to read the stream
			  logger.debug("POST response :"+decodedString);
			}
		} finally {
			in.close();
		}
	
		
		return olapQueryId;
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

		OlapChartData ret = new OlapChartData();

		JSONArray jsonArray = readJsonFromUrl(buildDataUrl(olapQueryId));

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
		return ret;
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
				return Float.parseFloat(value.replace(",","."));
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

	public  JSONArray readJsonFromUrl(String url) throws IOException, JSONException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		
	   Authenticator.setDefault(new Authenticator() {
			    protected PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication (olapUser, olapPassword.toCharArray());
			    }
		});

		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONArray json = new JSONArray(jsonText.trim());

			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("Retrivied JSON Data from :" + url + " in " + watch.getTotalTimeSeconds() + " seconds");
				logger.debug("Value : " + json.toString());

			}

			return json;
		} finally {
			is.close();
		}
	}

	private  String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}
	
	
	
	

}
