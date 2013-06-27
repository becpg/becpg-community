package fr.becpg.repo.quality.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.quality.NonConformityService;
import fr.becpg.repo.quality.data.NonConformityData;
import fr.becpg.repo.quality.data.dataList.WorkLogDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

public class NonConformityPolicies implements NodeServicePolicies.OnUpdatePropertiesPolicy,
		NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy {

	private static Log logger = LogFactory.getLog(NonConformityPolicies.class);

	private PolicyComponent policyComponent;
	
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	private NonConformityService nonConformityService;

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}


	public void setNonConformityService(NonConformityService nonConformityService) {
		this.nonConformityService = nonConformityService;
	}

	public void init() {

		logger.debug("Init NonConformityPolicies...");

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, QualityModel.TYPE_NC,
				new JavaBehaviour(this, "onUpdateProperties"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				QualityModel.TYPE_NC, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				QualityModel.TYPE_NC, new JavaBehaviour(this, "onDeleteAssociation"));
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		String beforeState = (String) before.get(QualityModel.PROP_NC_STATE);
		String afterState = (String) after.get(QualityModel.PROP_NC_STATE);
		
		String beforeComment = (String) before.get(QualityModel.PROP_NC_COMMENT);
		String afterComment = (String) after.get(QualityModel.PROP_NC_COMMENT);
		
		boolean addWorkLog = false;
		
		if (afterState != null && !afterState.isEmpty() && !afterState.equals(beforeState)) {
			addWorkLog = true;
		}
		else if (afterComment != null && !afterComment.isEmpty() && !afterComment.equals(beforeComment)) {
			addWorkLog = true;
		}
		
		if(addWorkLog){

			NonConformityData ncData = (NonConformityData) alfrescoRepository.findOne(nodeRef);

			if (ncData.getWorkLog() == null) {
				ncData.setWorkLog(new ArrayList<WorkLogDataItem>(1));
			}

			// add a work log
			ncData.getWorkLog().add(
					new WorkLogDataItem(null, afterState, (String) after.get(QualityModel.PROP_NC_COMMENT), null, null));
			// reset comment
			ncData.setComment(null);

			alfrescoRepository.save(ncData);
		}

	}

	@Deprecated
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {

		logger.debug("NC onCreateAssociation");
//		if (assocRef.getTypeQName().equals(QualityModel.ASSOC_PRODUCT)) {
//			nonConformityService.classifyNC(assocRef.getSourceRef(), assocRef.getTargetRef());
//		}
	}

	@Deprecated
	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {

		logger.debug("NC onDeleteAssociation");
//		if (assocRef.getTypeQName().equals(QualityModel.ASSOC_PRODUCT)) {
//			nonConformityService.classifyNC(assocRef.getSourceRef(), null);
//		}
	}
}
