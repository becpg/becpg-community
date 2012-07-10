package fr.becpg.repo.web.scripts.entity.datalist;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;

/**
 * 
 * @author matthieu
 * 
 */
public class SortDataListWebScript extends DeclarativeWebScript {

	private static Log logger = LogFactory.getLog(SortDataListWebScript.class);

	private static final String PARAM_STORE_TYPE = "store_type";

	private static final String PARAM_STORE_ID = "store_id";

	protected static final String PARAM_ID = "id";

	private static final String PARAM_SELECTED_NODEREFS = "selectedNodeRefs";

	private static final String PARAM_DIR = "dir";

	private DataListSortService dataListSortService;

	private NodeService nodeService;

	public void setDataListSortService(DataListSortService dataListSortService) {
		this.dataListSortService = dataListSortService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		logger.debug("call Sort webscript");

		Map<String, Object> model = new HashMap<String, Object>();

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		String selectedNodeRefsArgs = req.getParameter(PARAM_SELECTED_NODEREFS);
		String dir = req.getParameter(PARAM_DIR);

		if (storeType != null && storeId != null && nodeId != null && selectedNodeRefsArgs != null) {
			NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);

			String[] selectedNodeRefs = selectedNodeRefsArgs.split(",");


				model.put("origSort", nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT));

				if (dir != null || selectedNodeRefs.length > 1) {
					
					if (!dir.equals("up")) {
						ArrayUtils.reverse(selectedNodeRefs);
					}
					
					for (int i = 0; i < selectedNodeRefs.length; i++) {
						dataListSortService.move(new NodeRef(selectedNodeRefs[i]), dir.equals("up"));
					}

				} else if (selectedNodeRefs.length == 1) {
					dataListSortService.insertAfter(new NodeRef(selectedNodeRefs[0]), nodeRef);
				}

				model.put("destSort", nodeService.getProperty(nodeRef, BeCPGModel.PROP_SORT));
				return model;
			
		}
		throw new WebScriptException("Invalid argument ");
	}

}
