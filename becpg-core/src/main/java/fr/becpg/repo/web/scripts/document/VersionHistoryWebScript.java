/*
 * 
 */
package fr.becpg.repo.web.scripts.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.entity.version.VersionData;

// TODO: Auto-generated Javadoc
/**
 * The Class VersionHistoryWebScript.
 *
 * @author querephi
 */
public class VersionHistoryWebScript extends DeclarativeWebScript  {

	// request parameter names
	/** The Constant PARAM_STORE_TYPE. */
	private static final String PARAM_STORE_TYPE = "store_type";
	
	/** The Constant PARAM_STORE_ID. */
	private static final String PARAM_STORE_ID = "store_id";
	
	/** The Constant PARAM_ID. */
	private static final String PARAM_ID = "id";	
	// model key names
	/** The Constant MODEL_KEY_NAME_VERSIONS. */
	private static final String MODEL_KEY_NAME_VERSIONS = "versions";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(VersionHistoryWebScript.class);		
	
	/** The entity version service. */
	private EntityVersionService entityVersionService;
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The version service. */
	private VersionService versionService;
	
	/** The person service. */
	private PersonService personService;
	
	/**
	 * Sets the entity version service.
	 *
	 * @param entityVersionService the new entity version service
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}	
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the version service.
	 *
	 * @param versionService the new version service
	 */
	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}
	
	/**
	 * Sets the person service.
	 *
	 * @param personService the new person service
	 */
	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}
	
	/**
	 * Get entity version history.
	 *
	 * @param req the req
	 * @param status the status
	 * @param cache the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
				
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		
		logger.debug("VersionWebScript executeImpl()");			
		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);		
		List<VersionData> sortedVersionHistory = null;
		
		// entity
		if(nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_PRODUCT)){			
			
			List<VersionData> versionHistory = entityVersionService.getVersionHistoryWithProperties(nodeRef);
			int cnt = versionHistory.size();
			sortedVersionHistory = new ArrayList<VersionData>(cnt);
			
			while(0 < cnt){
				cnt--;
				sortedVersionHistory.add(versionHistory.get(cnt));			
			}
		}
		else{
			sortedVersionHistory = new ArrayList<VersionData>();
			VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
			if(versionHistory != null){				
				
				for(Version version : versionHistory.getAllVersions()){
					
					NodeRef personNodeRef = personService.getPerson(version.getFrozenModifier());
					Map<QName, Serializable> personProperties = nodeService.getProperties(personNodeRef);
					
					VersionData versionData = new VersionData(version.getVersionedNodeRef(), 
																						(String)nodeService.getProperty(version.getVersionedNodeRef(), ContentModel.PROP_NAME),
																						version.getVersionLabel(),
																						(String)version.getVersionProperties().get(Version2Model.PROP_QNAME_VERSION_DESCRIPTION),
																						version.getFrozenModifiedDate(),
																						(String)personProperties.get(ContentModel.PROP_USERNAME),
																						(String)personProperties.get(ContentModel.PROP_FIRSTNAME),
																						(String)personProperties.get(ContentModel.PROP_LASTNAME));
																						
					sortedVersionHistory.add(versionData);
				}			
			}			
		}					
				
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_VERSIONS, sortedVersionHistory);
		
		return model;
	}		
}
