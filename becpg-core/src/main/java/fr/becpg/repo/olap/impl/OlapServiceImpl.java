/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
package fr.becpg.repo.olap.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.authentication.BeCPGTicketService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.olap.OlapService;
import fr.becpg.repo.olap.OlapUtils;
import fr.becpg.repo.olap.data.OlapChart;
import fr.becpg.repo.olap.data.OlapChartData;
import fr.becpg.repo.olap.data.OlapChartMetadata;
import fr.becpg.repo.olap.data.OlapContext;

/**
 * <p>OlapServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("olapService")
public class OlapServiceImpl implements OlapService {

	private static final String ROW_HEADER = "ROW_HEADER_HEADER";

	private static final Log logger = LogFactory.getLog(OlapServiceImpl.class);


	@Value("${becpg.olap.url.public}")
	private String olapPublicUrl;

	@Value("${becpg.olap.url.internal}")
	private String olapServerUrl;
	
	@Value("${becpg.olap.enabled}")
	private Boolean enabled;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private ContentService contentService;

	@Autowired
	private RepoService repoService;

	@Autowired
	private BeCPGTicketService beCPGTicketService;


	/** {@inheritDoc} */
	@Override
	public List<OlapChart> retrieveOlapCharts() {
		List<OlapChart> olapCharts = new ArrayList<>();

		for (FileInfo fileInfo : fileFolderService.list(getOlapQueriesFolder())) {

			if (fileInfo.getName().endsWith(".saiku")) {
				try {
					OlapChart chart = new OlapChart(fileInfo);

					ContentReader reader = contentService.getReader(fileInfo.getNodeRef(), ContentModel.PROP_CONTENT);

					chart.load(reader.getContentString());

					olapCharts.add(chart);
				} catch (Exception e) {
					logger.error(e, e);
				}
			}

		}

		return olapCharts;
	}

	/** {@inheritDoc} */
	@Override
	public NodeRef getOlapQueriesFolder() {
		return repoService.getFolderByPath("/" + RepoConsts.PATH_SYSTEM + "/" + RepoConsts.PATH_OLAP_QUERIES);
	}

	/** {@inheritDoc} */
	@Override
	public List<OlapChart> retrieveOlapChartsFromSaiku() throws IOException, JSONException {

		List<OlapChart> olapCharts = new ArrayList<>();
		try (OlapContext olapContext = new OlapContext(beCPGTicketService.getCurrentBeCPGUserName(), beCPGTicketService.getCurrentAuthToken())) {

			JSONArray jsonArray = new JSONArray(OlapUtils.readJsonFromUrl(buildRepositoryUrl(olapContext), olapContext));

			if (jsonArray != null) {

				for (int row = 0; row < jsonArray.length(); row++) {
					String queryName = jsonArray.getJSONObject(row).getString("name");
					try {

						OlapChart chart = new OlapChart(queryName);

						JSONObject json = new JSONObject(OlapUtils.readJsonFromUrl(buildQueryUrl(queryName, olapContext), olapContext));

						chart.load(json.getString("xml"));
						olapCharts.add(chart);
					} catch (Exception e) {
						logger.error("Cannot load query :" + queryName);
						logger.debug(e, e);
					}
				}

			}
		}

		return olapCharts;

	}

	/**
	 * {@inheritDoc}
	 *
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
	 */
	@Override
	public OlapChartData retrieveChartData(String olapQueryId) throws IOException {

		try (OlapContext olapContext = new OlapContext(beCPGTicketService.getCurrentBeCPGUserName(), beCPGTicketService.getCurrentAuthToken())) {

			OlapChart chart = getOlapChart(olapQueryId);

			OlapChartData ret = new OlapChartData();
			if (chart != null) {

				OlapUtils.sendCreateQueryPostRequest(olapContext, buildCreateQueryUrl(olapQueryId, olapContext), chart.getXml());

				String data = OlapUtils.readJsonFromUrl(buildDataUrl(olapQueryId, olapContext), olapContext);

				if (data != null && data.length() > 0) {

					try {
						JSONArray jsonArray = (new JSONObject(data)).getJSONArray("cellset");
	
						if (jsonArray != null) {
	
							int lowestLevel = 0;
							for (int row = 0; row < jsonArray.length(); row++) {
								JSONArray cur = jsonArray.getJSONArray(row);
								if (ROW_HEADER.equals(cur.getJSONObject(0).getString("type"))) {
									for (int field = 0; field < cur.length(); field++) {
										if (ROW_HEADER.equals(cur.getJSONObject(field).getString("type"))) {
											ret.shiftMetadata();
											lowestLevel = field;
										}
										ret.addMetadata(new OlapChartMetadata(field, retrieveDataType(jsonArray.getJSONArray(row + 1).getJSONObject(field).getString("value")), cur.getJSONObject(field)
												.getString("value")));
									}
								} else if (cur.getJSONObject(0).getString("value") != null) {
									List<Object> olapRecord = new ArrayList<>();
									for (int col = lowestLevel; col < cur.length(); col++) {
										String value = cur.getJSONObject(col).getString("value");
										olapRecord.add(OlapUtils.convert(value));
									}
									ret.getResultsets().add(olapRecord);
								}
							}
						}
					} catch (JSONException e) {
						logger.error("Incorrect data return by saiku for "+buildDataUrl(olapQueryId, olapContext));
						if(logger.isDebugEnabled()) {
							logger.debug("Data: "+data);
						}
					}
				} else {
					logger.error("No data return by saiku for "+buildDataUrl(olapQueryId, olapContext));
				}
			}
			return ret;
		}
	}


	/** {@inheritDoc} */
	@Override
	public String getSSOUrl() {
		if(Boolean.TRUE.equals(enabled)){
			return olapPublicUrl + "?ticket=" + beCPGTicketService.getCurrentAuthToken();
		} 
		return null;
	}

	private OlapChart getOlapChart(String olapQueryId)  {
		for (OlapChart chart : retrieveOlapCharts()) {
			if (chart.getQueryId().equals(olapQueryId)) {
				return chart;
			}
		}
		logger.warn("No chart found for id:" + olapQueryId);
		return null;
	}

	private String retrieveDataType(String value) {
		return OlapUtils.convert(value).getClass().getSimpleName();
	}

	private String buildDataUrl(String olapQueryId, OlapContext context) throws URIException {
		return olapServerUrl + "/rest/saiku/" + URIUtil.encodeWithinPath(context.getCurrentUser(), "UTF-8") + "/query/" + URIUtil.encodeWithinPath(olapQueryId, "UTF-8") + "/result/cheat";
	}

	private String buildCreateQueryUrl(String olapQueryId, OlapContext context) throws URIException {
		return olapServerUrl + "/rest/saiku/" + URIUtil.encodeWithinPath(context.getCurrentUser(), "UTF-8") + "/query/" + URIUtil.encodeWithinPath(olapQueryId, "UTF-8");
	}

	private String buildRepositoryUrl(OlapContext context) throws URIException {
		return olapServerUrl + "/rest/saiku/" + URIUtil.encodeWithinPath(context.getCurrentUser(), "UTF-8") + "/repository";
	}

	private String buildQueryUrl(String queryName, OlapContext context) throws URIException {
		return buildRepositoryUrl(context) + "/" + URIUtil.encodeWithinPath(queryName, "UTF-8");
	}

}
