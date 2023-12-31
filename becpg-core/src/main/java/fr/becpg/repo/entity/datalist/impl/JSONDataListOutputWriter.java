package fr.becpg.repo.entity.datalist.impl;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.DataListOutputWriter;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;

/**
 * <p>JSONDataListOutputWriter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class JSONDataListOutputWriter implements DataListOutputWriter {

	private static final String PARAM_QUERY_EXECUTION_ID = "queryExecutionId";

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private DataListSortService dataListSortService;

	/** {@inheritDoc} */
	@Override
	public void write(WebScriptRequest req, WebScriptResponse res, DataListFilter dataListFilter, PaginatedExtractedItems extractedItems) throws IOException {
		try {
			JSONObject ret = new JSONObject();
			if (!dataListFilter.isSimpleItem()) {
				ret.put("startIndex", dataListFilter.getPagination().getPage());
				ret.put("pageSize", dataListFilter.getPagination().getPageSize());
				ret.put("totalRecords", extractedItems.getFullListSize());
				if (dataListFilter.getPagination().getQueryExecutionId() != null) {
					ret.put(PARAM_QUERY_EXECUTION_ID, dataListFilter.getPagination().getQueryExecutionId());
				}

			}

			JSONObject metadata = new JSONObject();

			JSONObject parent = new JSONObject();

			parent.put("nodeRef", dataListFilter.getParentNodeRef());

			JSONObject permissions = new JSONObject();
			JSONObject userAccess = new JSONObject();

			userAccess.put("create",
					((((dataListFilter.getSiteId() != null) && !dataListFilter.getSiteId().isEmpty()) || (dataListFilter.getParentNodeRef() != null))
							&& dataListFilter.hasWriteAccess()
							&& (permissionService.hasPermission(dataListFilter.getParentNodeRef(), "CreateChildren") == AccessStatus.ALLOWED))
					        && !((dataListFilter.getDataListName() != null) && dataListFilter.getDataListName().startsWith(RepoConsts.WUSED_PREFIX)));

			permissions.put("userAccess", userAccess);

			parent.put("permissions", permissions);

			metadata.put("parent", parent);

			ret.put("metadata", metadata);
			if (dataListFilter.isSimpleItem() && !extractedItems.getPageItems().isEmpty()) {
				Map<String, Object> item = extractedItems.getPageItems().get(0);
				ret.put("item", new JSONObject(item));
				ret.put("lastSiblingNodeRef", dataListSortService.getLastChild(new NodeRef(item.get(AbstractDataListExtractor.PROP_NODE).toString())));
			} else {
				ret.put("items", processResults(extractedItems));
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());

		} catch (JSONException e) {
			throw new WebScriptException("Unable to parse JSON", e);
		}

	}

	private JSONArray processResults(PaginatedExtractedItems extractedItems) {

		JSONArray items = new JSONArray();

		for (Map<String, Object> item : extractedItems.getPageItems()) {
			items.put(new JSONObject(item));
		}

		return items;

	}

}
