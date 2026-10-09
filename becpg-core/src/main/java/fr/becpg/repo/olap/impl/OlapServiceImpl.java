/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG. 
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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
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
import fr.becpg.repo.helper.TranslateHelper;
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

	/** Constant <code>ROW_HEADER="ROW_HEADER_HEADER"</code> */
	private static final String ROW_HEADER = "ROW_HEADER_HEADER";

	/** Separates the levels of a multi-level row axis inside the single label published. */
	private static final String LEVEL_SEPARATOR = " / ";

	/** Constant <code>logger</code> */
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
	private PersonService personService;

	@Autowired
	private NodeService nodeService;

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

		NodeRef sharedFolder = getOlapQueriesFolder();
		if (sharedFolder == null) {
			logger.warn("OLAP queries folder not found, returning empty chart list");
		} else {
			collectCharts(sharedFolder, olapCharts);
		}

		collectCharts(getPersonalOlapQueriesFolder(), olapCharts);

		return olapCharts;
	}

	/**
	 * Adds every {@code .saiku} document of a folder to the chart list.
	 *
	 * @param folder the folder to read, ignored when null
	 * @param olapCharts the list to fill
	 */
	private void collectCharts(NodeRef folder, List<OlapChart> olapCharts) {
		if (folder == null) {
			return;
		}

		for (FileInfo fileInfo : fileFolderService.list(folder)) {
			if (!fileInfo.getName().endsWith(OlapChart.SAIKU_EXTENSION)) {
				continue;
			}
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

	/**
	 * Resolves the current user's personal OLAP query folder.
	 *
	 * <p>beCPG OLAP saves a user's own queries under their home folder rather than in the shared
	 * system space, so the dashlet has to read both. The folder carries the localised name of
	 * {@code path.olapqueries}; every translation is accepted, because the folder may have been
	 * created by an OLAP session running in another language than this repository's default.
	 *
	 * @return the folder, or null when the user has none
	 */
	private NodeRef getPersonalOlapQueriesFolder() {
		String userName = AuthenticationUtil.getFullyAuthenticatedUser();
		if (userName == null) {
			return null;
		}

		NodeRef person = personService.getPersonOrNull(userName);
		if (person == null) {
			return null;
		}

		NodeRef homeFolder = (NodeRef) nodeService.getProperty(person, ContentModel.PROP_HOMEFOLDER);
		if (homeFolder == null) {
			return null;
		}

		for (String candidate : personalFolderNames()) {
			NodeRef folder = nodeService.getChildByName(homeFolder, ContentModel.ASSOC_CONTAINS, candidate);
			if (folder != null) {
				return folder;
			}
		}
		return null;
	}

	/**
	 * Names the personal query folder can carry: this repository's locale first, then every other
	 * translation of {@code path.olapqueries}.
	 *
	 * @return the candidate folder names, never null
	 */
	private static List<String> personalFolderNames() {
		List<String> names = new ArrayList<>();

		String preferred = TranslateHelper.getTranslatedPath(RepoConsts.PATH_OLAP_QUERIES);
		if (preferred != null) {
			names.add(preferred);
		}

		MLText translations = TranslateHelper.getTranslatedPathMLText(RepoConsts.PATH_OLAP_QUERIES);
		if (translations != null) {
			for (String translation : translations.values()) {
				if ((translation != null) && !names.contains(translation)) {
					names.add(translation);
				}
			}
		}

		return names;
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
	
							// A query putting several levels on rows - a year and a month, a family and a
							// product - used to lose every level but the innermost: the outer metadata
							// was shifted out and the records started at the last row header. "Products
							// created per year-month" came back as (month, count), the year silently
							// gone. The levels are joined into one label instead, so nothing is lost and
							// the consumers still receive one label column followed by the measures.
							int rowHeaders = 0;
							for (int row = 0; row < jsonArray.length(); row++) {
								JSONArray cur = jsonArray.getJSONArray(row);
								if (ROW_HEADER.equals(cur.getJSONObject(0).getString("type"))) {
									rowHeaders = countRowHeaders(cur);
									ret.addMetadata(new OlapChartMetadata(0,
											retrieveDataType(jsonArray.getJSONArray(row + 1).getJSONObject(0)),
											joinHeaderLabels(cur, rowHeaders)));
									for (int field = rowHeaders; field < cur.length(); field++) {
										ret.addMetadata(new OlapChartMetadata((field - rowHeaders) + 1,
												retrieveDataType(jsonArray.getJSONArray(row + 1).getJSONObject(field)),
												cur.getJSONObject(field).getString("value")));
									}
								} else if (cur.getJSONObject(0).getString("value") != null) {
									List<Object> olapRecord = new ArrayList<>();
									olapRecord.add(joinRowLabels(cur, rowHeaders));
									for (int col = rowHeaders; col < cur.length(); col++) {
										olapRecord.add(cellValue(cur.getJSONObject(col)));
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

	/**
	 * Counts the leading row-header columns of a cell set, which is how many levels the query put
	 * on rows.
	 *
	 * @param headerRow the first row of the cell set
	 * @return the number of row-header columns, at least one
	 */
	private static int countRowHeaders(JSONArray headerRow) throws JSONException {
		int count = 0;
		while ((count < headerRow.length()) && ROW_HEADER.equals(headerRow.getJSONObject(count).getString("type"))) {
			count++;
		}
		return Math.max(count, 1);
	}

	/**
	 * Joins the captions of the row levels into the single column name the consumers expect.
	 *
	 * @param headerRow the first row of the cell set
	 * @param rowHeaders how many of its columns are row headers
	 * @return the joined caption
	 */
	private static String joinHeaderLabels(JSONArray headerRow, int rowHeaders) throws JSONException {
		List<String> labels = new ArrayList<>();
		for (int col = 0; col < rowHeaders; col++) {
			labels.add(headerRow.getJSONObject(col).getString("value"));
		}
		return String.join(LEVEL_SEPARATOR, labels);
	}

	/**
	 * Joins the row-header values of one data row into a single label.
	 *
	 * @param row a data row of the cell set
	 * @param rowHeaders how many of its columns are row headers
	 * @return the joined label
	 */
	private String joinRowLabels(JSONArray row, int rowHeaders) throws JSONException {
		List<String> labels = new ArrayList<>();
		for (int col = 0; col < rowHeaders; col++) {
			labels.add(String.valueOf(cellValue(row.getJSONObject(col))));
		}
		return String.join(LEVEL_SEPARATOR, labels);
	}

	private String xmlEscape(String input) {
		if (input == null) return null;
		return input.replace("&", "&amp;")
					.replace("<", "&lt;")
					.replace(">", "&gt;")
					.replace("\"", "&quot;")
					.replace("'", "&apos;");
	}

	/** {@inheritDoc} */
	@Override
	public OlapChartData runMdxQuery(String cube, String mdxQuery) throws IOException, JSONException {
		try (OlapContext olapContext = new OlapContext(beCPGTicketService.getCurrentBeCPGUserName(), beCPGTicketService.getCurrentAuthToken())) {
			String olapQueryId = "dynamic_" + java.util.UUID.randomUUID().toString();
			OlapChartData ret = new OlapChartData();

			// Raw-MDX execution must declare type="MDX": with type="QM" (Query Model) Saiku tries to expand a
			// QueryModel that is not provided here (only <MDX> is), which fails with Mondrian
			// "Not enough variable values available to expand ...". Saved .saiku queries use QM because they
			// carry a full <QueryModel>; a dynamic raw-MDX query must not.
			String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
					"<Query name=\"" + olapQueryId + "\" type=\"MDX\" connection=\"beCPG\" cube=\"" + (cube.startsWith("[") ? cube : "[" + cube + "]") + "\" catalog=\"beCPG OLAP Schema\" schema=\"beCPG OLAP Schema\">\n" +
					"  <MDX>" + xmlEscape(mdxQuery) + "</MDX>\n" +
					"</Query>";

			OlapUtils.sendCreateQueryPostRequest(olapContext, buildCreateQueryUrl(olapQueryId, olapContext), xml);

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
									ret.addMetadata(new OlapChartMetadata(field, retrieveDataType(jsonArray.getJSONArray(row + 1).getJSONObject(field)), cur.getJSONObject(field).getString("value")));
								}
							} else if (cur.getJSONObject(0).getString("value") != null) {
								List<Object> olapRecord = new ArrayList<>();
								for (int col = lowestLevel; col < cur.length(); col++) {
									olapRecord.add(cellValue(cur.getJSONObject(col)));
								}
								ret.getResultsets().add(olapRecord);
							}
						}
					}
				} catch (JSONException e) {
					logger.error("Incorrect data return by saiku for dynamic query execution " + buildDataUrl(olapQueryId, olapContext));
					if (logger.isDebugEnabled()) {
						logger.debug("Data: " + data);
					}
					throw e;
				}
			} else {
				logger.error("No data return by saiku for dynamic query " + buildDataUrl(olapQueryId, olapContext));
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

	/**
	 * <p>getOlapChart.</p>
	 *
	 * @param olapQueryId a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.olap.data.OlapChart} object
	 */
	private OlapChart getOlapChart(String olapQueryId)  {
		for (OlapChart chart : retrieveOlapCharts()) {
			if (chart.getQueryId().equals(olapQueryId)) {
				return chart;
			}
		}
		logger.warn("No chart found for id:" + olapQueryId);
		return null;
	}

	/**
	 * The class a column's values will be published as, taken from the first data
	 * cell of that column.
	 *
	 * @param cell the first data cell under the header being described
	 * @return the simple class name of the converted value
	 */
	private String retrieveDataType(JSONObject cell) {
		return cellValue(cell).getClass().getSimpleName();
	}

	/**
	 * The value of one cellset cell.
	 *
	 * A Saiku data cell carries the measure twice: {@code value}, formatted for the
	 * connection locale, and {@code properties.raw}, the number itself. Only the
	 * second one can be parsed without guessing — the displayed {@code "1,148"} is
	 * 1 148, and the same shape elsewhere in the payload is a genuine fraction. See
	 * {@link fr.becpg.repo.olap.OlapUtils#convertCell(String, String)}.
	 *
	 * @param cell a cellset cell
	 * @return the converted value
	 */
	private Object cellValue(JSONObject cell) {
		String raw = null;
		JSONObject properties = cell.optJSONObject("properties");
		if (properties != null) {
			raw = properties.optString("raw", null);
		}
		return OlapUtils.convertCell(raw, cell.optString("value", null));
	}

	/**
	 * <p>buildDataUrl.</p>
	 *
	 * @param olapQueryId a {@link java.lang.String} object
	 * @param context a {@link fr.becpg.repo.olap.data.OlapContext} object
	 * @return a {@link java.lang.String} object
	 * @throws org.apache.commons.httpclient.URIException if any.
	 */
	private String buildDataUrl(String olapQueryId, OlapContext context) throws URIException {
		return olapServerUrl + "/rest/saiku/" + URIUtil.encodeWithinPath(context.getCurrentUser(), "UTF-8") + "/query/" + URIUtil.encodeWithinPath(olapQueryId, "UTF-8") + "/result/cheat";
	}

	/**
	 * <p>buildCreateQueryUrl.</p>
	 *
	 * @param olapQueryId a {@link java.lang.String} object
	 * @param context a {@link fr.becpg.repo.olap.data.OlapContext} object
	 * @return a {@link java.lang.String} object
	 * @throws org.apache.commons.httpclient.URIException if any.
	 */
	private String buildCreateQueryUrl(String olapQueryId, OlapContext context) throws URIException {
		return olapServerUrl + "/rest/saiku/" + URIUtil.encodeWithinPath(context.getCurrentUser(), "UTF-8") + "/query/" + URIUtil.encodeWithinPath(olapQueryId, "UTF-8");
	}

	/**
	 * <p>buildRepositoryUrl.</p>
	 *
	 * @param context a {@link fr.becpg.repo.olap.data.OlapContext} object
	 * @return a {@link java.lang.String} object
	 * @throws org.apache.commons.httpclient.URIException if any.
	 */
	private String buildRepositoryUrl(OlapContext context) throws URIException {
		return olapServerUrl + "/rest/saiku/" + URIUtil.encodeWithinPath(context.getCurrentUser(), "UTF-8") + "/repository";
	}

	/**
	 * <p>buildQueryUrl.</p>
	 *
	 * @param queryName a {@link java.lang.String} object
	 * @param context a {@link fr.becpg.repo.olap.data.OlapContext} object
	 * @return a {@link java.lang.String} object
	 * @throws org.apache.commons.httpclient.URIException if any.
	 */
	private String buildQueryUrl(String queryName, OlapContext context) throws URIException {
		return buildRepositoryUrl(context) + "/" + URIUtil.encodeWithinPath(queryName, "UTF-8");
	}

}
