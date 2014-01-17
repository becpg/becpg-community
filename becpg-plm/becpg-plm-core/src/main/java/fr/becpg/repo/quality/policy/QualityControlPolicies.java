package fr.becpg.repo.quality.policy;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.quality.QualityControlService;

public class QualityControlPolicies implements NodeServicePolicies.OnCreateAssociationPolicy {

	private static Log logger = LogFactory.getLog(QualityControlPolicies.class);
	
	private PolicyComponent policyComponent;
	private QualityControlService qualityControlService;
		
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setQualityControlService(QualityControlService qualityControlService) {
		this.qualityControlService = qualityControlService;
	}

	public void init() {				
		
		logger.debug("Init QualityControlPolicies...");
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				QualityModel.TYPE_QUALITY_CONTROL, new JavaBehaviour(this,
						"onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT));		
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		
		logger.debug("QualityControlPolicies onCreateAssociation");
		
		if(assocRef.getTypeQName().equals(QualityModel.ASSOC_QC_CONTROL_PLANS)){
			qualityControlService.createSamplingList(assocRef.getSourceRef(), assocRef.getTargetRef());
		}		
	}

}
