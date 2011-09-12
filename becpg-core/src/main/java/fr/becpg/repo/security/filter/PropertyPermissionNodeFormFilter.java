/*
 * 
 */
package fr.becpg.repo.security.filter;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The Class PropertyPermissionNodeFormFilter.
 *
 * @author querephi
 */
public class PropertyPermissionNodeFormFilter extends AbstractPropertyPermissionFormFilter<NodeRef> {

	/** The logger. */
	private static Log logger = LogFactory.getLog(PropertyPermissionNodeFormFilter.class);	
	
	/** The node service. */
	private NodeService nodeService;	
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#afterGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
	 */
	@Override
	public void afterGenerate(NodeRef item, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {
		logger.debug("afterGenerate");					
		filterFormFields(nodeService.getType(item), form);		

	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#afterPersist(java.lang.Object, org.alfresco.repo.forms.FormData, org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	public void afterPersist(NodeRef item, FormData data, NodeRef persistedObject) {
		// TODO Auto-generated method stub		
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#beforeGenerate(java.lang.Object, java.util.List, java.util.List, org.alfresco.repo.forms.Form, java.util.Map)
	 */
	@Override
	public void beforeGenerate(NodeRef item, List<String> fields,
			List<String> forcedFields, Form form, Map<String, Object> context) {			

	
		
	}

	/* (non-Javadoc)
	 * @see fr.becpg.repo.form.processor.PropertyPermissionFormFilter#beforePersist(java.lang.Object, org.alfresco.repo.forms.FormData)
	 */
	@Override
	public void beforePersist(NodeRef item, FormData data) {
		// TODO Auto-generated method stub
	}

}
