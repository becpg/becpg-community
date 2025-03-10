/*
 *
 */
package fr.becpg.repo.web.scripts.search;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.search.SavedSearchService;
import fr.becpg.repo.search.data.SavedSearch;

/**
 * Webscript that send the list of saved search
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GetSavedSearchWebScript extends AbstractSearchWebScript {

	private static final String PARAM_SITEID = "site";

	private static final String PARAM_SEARCH_TYPE = "type";

	private static final String PARAM_NODEREF = "nodeRef";

	private SavedSearchService savedSearchService;

	/**
	 * <p>Setter for the field <code>savedSearchService</code>.</p>
	 *
	 * @param savedSearchService a {@link fr.becpg.repo.search.SavedSearchService} object
	 */
	public void setSavedSearchService(SavedSearchService savedSearchService) {
		this.savedSearchService = savedSearchService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		try {
			SavedSearch filter = new SavedSearch();

			if (req.getParameter(PARAM_NODEREF) != null) {
				filter.setNodeRef(new NodeRef(req.getParameter(PARAM_NODEREF)));
			} else {
				filter.setSearchType(req.getParameter(PARAM_SEARCH_TYPE));
				filter.setSiteId(req.getParameter(PARAM_SITEID));
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");

			JSONObject jsonResponse = new JSONObject();

			if (filter.getNodeRef() != null) {
				// Ajout de l'élément "items" lorsque NodeRef est présent
				String itemsContent = savedSearchService.getSavedSearchContent(filter);

				JSONArray items = new JSONArray();
				JSONObject ret = new JSONObject();
				ret.put("nodeRef", filter.getNodeRef());
				ret.put("data", itemsContent);
				items.put(ret);
				jsonResponse.put("items", items);

			} else {
				NodeRef destNodeRef = savedSearchService.getSaveSearchFolder(filter);
			
				jsonResponse.put("destNodeRef", destNodeRef!=null ? destNodeRef.toString(): null);
				JSONArray items = new JSONArray();

				List<SavedSearch> results = savedSearchService.findSavedSearch(filter);
				for (SavedSearch savedSearch : results) {
					JSONObject ret = new JSONObject();
					ret.put("nodeRef", savedSearch.getNodeRef());
					ret.put("name", savedSearch.getName());
					ret.put("isGlobal", savedSearch.getIsGlobal());
					items.put(ret);
				}
				
				filter.setGlobal(true);
				NodeRef globalDestNodeRef = savedSearchService.getSaveSearchFolder(filter);
				jsonResponse.put("globalDestNodeRef", globalDestNodeRef!=null ? globalDestNodeRef.toString():null);
				jsonResponse.put("items", items);
			}

			res.getWriter().write(jsonResponse.toString());

		} catch (IOException | JSONException e) {
			throw new WebScriptException("Erreur lors du traitement de la requête JSON", e);
		}

	}

}
