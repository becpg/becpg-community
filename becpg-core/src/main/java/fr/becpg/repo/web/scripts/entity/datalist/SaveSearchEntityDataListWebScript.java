/*
 *
 */
package fr.becpg.repo.web.scripts.entity.datalist;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.repo.entity.datalist.data.DataListFilter;

/**
 * Webscript that send the result of a datalist
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SaveSearchEntityDataListWebScript extends AbstractEntityDataListWebScript {

	private static final Log logger = LogFactory.getLog(SaveSearchEntityDataListWebScript.class);

	private static final String PARAM_GLOBAL_SAVED_SEARCH = "globalSavedSearch";

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
			logger.debug("EntityDataListWebScript executeImpl()");
		}

		try {

			JSONObject jsonObject = new JSONObject();

			DataListFilter dataListFilter = createDataListFilter(req);
			NodeRef savedSearchNodeRef = null;

			String savedSearchNodeRefArg = req.getParameter(PARAM_SAVED_SEARCH_NODEREF);
			if (savedSearchNodeRefArg != null) {
				savedSearchNodeRef = new NodeRef(savedSearchNodeRefArg);
			}
			String isGlobalSavedSearchArg = req.getParameter(PARAM_GLOBAL_SAVED_SEARCH);
			boolean isGlobalSavedSearch = false;
			if ("true".equals(isGlobalSavedSearchArg)) {
				isGlobalSavedSearch = false;
			}

			savedSearchNodeRef = savedSearchService.createOrUpdate(savedSearchNodeRef, createSearchType(dataListFilter), dataListFilter.getSiteId(),
					dataListFilter.toJsonString(), isGlobalSavedSearch);

			jsonObject.put("savedSearchNodeRef", savedSearchNodeRef);
			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			jsonObject.write(res.getWriter());

		} catch (JSONException e) {
			throw new WebScriptException("Unable to parse JSON", e);
		}

	}

	private String createSearchType(DataListFilter dataListFilter) {
		return dataListFilter.getDataType().toPrefixString(namespaceService);
	}

}
