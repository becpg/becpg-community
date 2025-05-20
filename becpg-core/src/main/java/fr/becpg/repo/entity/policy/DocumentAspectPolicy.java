package fr.becpg.repo.entity.policy;

import org.activiti.bpmn.model.Association;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

public class DocumentAspectPolicy extends AbstractBeCPGPolicy implements ContentServicePolicies.OnContentUpdatePolicy  {

	private static final Log logger = LogFactory.getLog(DocumentAspectPolicy.class);
	
	
    private AssociationService associationService;
    
    
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	public void doInit() {
	//TODO	policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, BeCPGModel.ASPECT_DOCUMENT_ASPECT, new JavaBehaviour(this, "onContentUpdate"));
		
	}

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		
		
		
		logger.debug("onContentUpdate");
		//associationService.getTargetAssoc(nodeRef, DOC_CHARACT)
		
		// TODO Auto-generated method stub
//		onContentUpdate
//	    Si Type de validité   none -> Date de début à aujourd'hui et pas de date de fin
//	    Si Type de validité   manuel -> On fait rien
//	    Si Type de validité   auto -> Date de début à aujourd'hui et fin à aujourd'hui + X jours
//	    documentState -> ToValidate
		//nodeService.setProperty(nodeRef, BeCPGModel.PROP_CM_TO, new date());
		
	}

}
