package fr.becpg.repo.web.scripts.product;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.CharactDetails;

/**
 * return List details
 * @author matthieu
 *
 */
public class FormulateCharactDetailsWebScript extends AbstractProductWebscript {
	
	private static Log logger = LogFactory.getLog(FormulateCharactDetailsWebScript.class);
	
	private static final String PARAM_DATA_LIST_NAME = "dataListName";
	
	private static final String PARAM_ITEM_TYPE = "itemType";
	
	private static final String PARAM_DATALISTITEMS = "dataListItems";
	
	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";
	
	private NodeService nodeService;
	
	private NamespaceService namespaceService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		
		String dataListName = req.getParameter(PARAM_DATA_LIST_NAME);
		String itemType = req.getParameter(PARAM_ITEM_TYPE);
		String entityNodeRef = req.getParameter(PARAM_ENTITY_NODEREF);
		QName dataType = QName.createQName(itemType, namespaceService);
		
		String dataListItems = req.getParameter(PARAM_DATALISTITEMS);
		List<NodeRef> elements = new ArrayList<NodeRef>();
		if(dataListItems!=null && dataListItems.length()>0){
			
			for(String nodeRef : dataListItems.split(",")){
				elements.add(new NodeRef(nodeRef));
			}
			
		}

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		
		NodeRef productNodeRef = new NodeRef(entityNodeRef); 
		
		try {
			
			CharactDetails ret =  productService.formulateDetails(productNodeRef, dataType, dataListName, elements);
			
			if("csv".equals(req.getFormat()) ){
				res.setContentType("application/vnd.ms-excel");
				res.setContentEncoding("ISO-8859-1");
				CharactDetailsHelper.writeCSV(ret,nodeService,res.getWriter());
			} else {
				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				res.getWriter().write(CharactDetailsHelper.toJSONObject(ret,nodeService).toString(3));

			}
	
		} catch (FormulateException e) {
			handleFormulationError(e);
			
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON",e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("FormulateCharactDetailsWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

		
	}

}
