package fr.becpg.repo.search.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * <p>SearchConfig class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SearchConfig {

	private static final Log logger = LogFactory.getLog(SearchConfig.class);

	private List<DataListSearchFilter> dataListSearchFilters = new ArrayList<>();
	

	private static Set<String> keysToExclude = new HashSet<>();


	/**
	 * <p>Getter for the field <code>dataListSearchFilters</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<DataListSearchFilter> getDataListSearchFilters() {
		return dataListSearchFilters;
	}

	/**
	 * <p>Setter for the field <code>dataListSearchFilters</code>.</p>
	 *
	 * @param dataListSearchFilters a {@link java.util.List} object.
	 */
	public void setDataListSearchFilters(List<DataListSearchFilter> dataListSearchFilters) {
		this.dataListSearchFilters = dataListSearchFilters;
	}
	

	/**
	 * <p>Getter for the field <code>keysToExclude</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<String> getKeysToExclude() {
		return keysToExclude;
	}


	/**
	 * <p>parse.</p>
	 *
	 * @param content a {@link java.lang.String} object.
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void parse(String content, NamespaceService namespaceService, EntityDictionaryService entityDictionaryService) {
		try {

			JSONObject jsonObject = new JSONObject(content);

			JSONObject filters = jsonObject.getJSONObject("dataListSearchFilters");

			for (@SuppressWarnings("unchecked")
			Iterator<String> iterator = filters.keys(); iterator.hasNext();) {
				String filterName = iterator.next();

				JSONArray jsonArray = filters.getJSONArray(filterName);

				DataListSearchFilter filter = new DataListSearchFilter(filterName);

				for (int i = 0; i < jsonArray.length(); i++) {

					JSONObject conf = jsonArray.getJSONObject(i);

					DataListSearchFilterField field = new DataListSearchFilterField();

					field.setAttributeQname(QName.createQName(conf.getString("attribute"), namespaceService));

					if (conf.has("operator")) {
						field.setOperator(conf.getString("operator").toLowerCase());
					}

					if (conf.has("value")) {
						field.setValue(conf.getString("value"));
					}

					if (conf.has("htmlId")) {
						field.setHtmlId(conf.getString("htmlId"));
						keysToExclude.add(field.getHtmlId());
					}
					
					if (conf.has("listType")) {
						field.setSourceTypeQname(QName.createQName(conf.getString("listType"), namespaceService));
					}

					if (entityDictionaryService.isAssoc(field.getAttributeQname())) {
						filter.getAssocsFilters().add(field);
					} else {
						filter.getPropFilters().add(field);
					}

				}

				dataListSearchFilters.add(filter);
			}
		} catch (JSONException e) {
			logger.error("Unable to parse content to catalog, content: " + content, e);
		}

	}

}
