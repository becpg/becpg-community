package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.activiti.bpmn.model.Association;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>DocumentAspectPolicy class.</p>
 *
 * @author maxime
 * @version $Id: $Id
 */


public class DocumentAspectPolicy extends AbstractBeCPGPolicy 
	implements NodeServicePolicies.OnAddAspectPolicy, ContentServicePolicies.OnContentUpdatePolicy  {

	private static final Log logger = LogFactory.getLog(DocumentAspectPolicy.class);
	
	
    private AssociationService associationService;
    
    
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_DOCUMENT_ASPECT,
				new JavaBehaviour(this, "onAddAspect"));
		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, BeCPGModel.ASPECT_DOCUMENT_ASPECT, 
				new JavaBehaviour(this, "onContentUpdate"));
		
	}
	
	/** {@inheritDoc} */
	@Override
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) {
		queueNode(nodeRef);
	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		
		logger.debug("onContentUpdate");
		
		if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef)) {
			NodeRef documentTypeRef = associationService.getTargetAssoc(nodeRef, BeCPGModel.ASSOC_DOCUMENT_TYPE_REF);
			if (documentTypeRef != null) {
				String effectivityType = (String) nodeService.getProperty(documentTypeRef, BeCPGModel.PROP_DOCUMENT_TYPE_EFFECTIVITY_TYPE);
				if ( "AUTO".equals(effectivityType) || "NONE".equals(effectivityType) ) {
					Date fromDate = (Date) nodeService.getProperty(nodeRef, BeCPGModel.PROP_CM_FROM);
					if (fromDate == null) {
						fromDate = new Date();
						nodeService.setProperty(nodeRef, BeCPGModel.PROP_CM_FROM, fromDate);
					}
					
					if ("AUTO".equals(effectivityType)) {
						Integer autoExpirationDelay = (Integer) nodeService.getProperty(documentTypeRef, BeCPGModel.PROP_DOCUMENT_TYPE_AUTO_EXPIRATION_DELAY);
						if (autoExpirationDelay != null) {
							Calendar toDateCal = Calendar.getInstance();
							toDateCal.setTime(fromDate);
							toDateCal.add(Calendar.DAY_OF_YEAR, autoExpirationDelay);
							nodeService.setProperty(nodeRef, BeCPGModel.PROP_CM_TO, toDateCal.getTime());
						}
					}
				}
			}
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_DOCUMENT_STATE, "ToValidate");
		}
	}
}
