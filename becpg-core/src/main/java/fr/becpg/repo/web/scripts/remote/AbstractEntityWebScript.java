package fr.becpg.repo.web.scripts.remote;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ExportFormat;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Abstract remote entity webscript
 * @author matthieu
 *
 */
public abstract class AbstractEntityWebScript extends AbstractWebScript{

	protected static Log logger = LogFactory.getLog(AbstractEntityWebScript.class);
	
	/** The Constant PARAM_QUERY. */
	protected static final String PARAM_QUERY = "query";
	
	/** The Constant PARAM_PATH. */
	protected static final String PARAM_PATH = "path";
	
	/** The Constant PARAM_PATH. */
	protected static final String PARAM_FORMAT = "format";
	
	
	/** The Constant PARAM_NODEREF. */
	protected static final String PARAM_NODEREF = "nodeRef";

	/** Services **/

	protected NodeService nodeService;
	
	protected EntityService entityService;
	
	protected BeCPGSearchService beCPGSearchService;
	
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}


	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}


	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	

	protected NodeRef findEntity(WebScriptRequest req) {
		String nodeRef = req.getParameter(PARAM_NODEREF);
		if(nodeRef!=null && nodeRef.length()>0){
			NodeRef node = new NodeRef(nodeRef);
			if(nodeService.exists(node)){
				return node;
			} else {
				throw new WebScriptException("Node "+nodeRef+" doesn't exist in repository");
			}
			
		}
		String path = req.getParameter(PARAM_PATH);
		if(path!=null && path.length()>0){
		
		}
		String query = req.getParameter(PARAM_QUERY);
		if(query !=null && query.length()>0){
			query+= " +TYPE:\"bcpg:entity\"";
			List<NodeRef> refs =   beCPGSearchService.luceneSearch(query, 1);
			if(refs.size()>0){
				return refs.get(0);
			}
			throw new WebScriptException("No entity node found for query "+query);
		}
		
		
		throw new WebScriptException("No entity node found");
	}
	
	protected void sendOKStatus(NodeRef entityNodeRef, WebScriptResponse resp) {
		// TODO Auto-generated method stub
		
	}
	
	

	protected ExportFormat getFormat(WebScriptRequest req) {
		String format = req.getParameter(PARAM_FORMAT);
		if(format!=null && ExportFormat.csv.toString().equals(format)){
			return ExportFormat.csv;
		}
		return ExportFormat.xml;
	}
}
