package fr.becpg.repo.quality.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.quality.NonConformityService;
import fr.becpg.repo.quality.data.NonConformityData;
import fr.becpg.repo.quality.data.dataList.WorkLogDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

public class NonConformityPolicies extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.BeforeDeleteNodePolicy {

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	List<NodeRef> currentDeletedNodes = new ArrayList<>(); 
	
	private NonConformityService nonConformityService;

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setNonConformityService(NonConformityService nonConformityService) {
		this.nonConformityService = nonConformityService;
	}

	@Override
	public void doInit() {

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, QualityModel.TYPE_NC, new JavaBehaviour(this, "onUpdateProperties"));
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, QualityModel.TYPE_NC, new JavaBehaviour(this, "beforeDeleteNode"));

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
		} else if (afterComment != null && !afterComment.isEmpty() && !afterComment.equals(beforeComment)) {
			addWorkLog = true;
		}

		if (addWorkLog) {

			NonConformityData ncData = (NonConformityData) alfrescoRepository.findOne(nodeRef);

			if (ncData.getWorkLog() == null) {
				ncData.setWorkLog(new ArrayList<WorkLogDataItem>(1));
			}

			// add a work log
			ncData.getWorkLog().add(new WorkLogDataItem(null, afterState, (String) after.get(QualityModel.PROP_NC_COMMENT), null, null));
			// reset comment
			ncData.setComment(null);

			alfrescoRepository.save(ncData);
		}

	}

	public void beforeDeleteNode(NodeRef ncNodeRef) {
		for(String instance : nonConformityService.getAssociatedWorkflow(ncNodeRef)){
			queueNode(new NodeRef(new StoreRef("tmp", "wfInstance"), instance));
		}
	}

	
	
	
	
	@Override
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {
		List<String> instanceIds = new ArrayList<>();
		for(NodeRef tmp : pendingNodes){
			instanceIds.add(tmp.getId());
		}
		
		nonConformityService.deleteWorkflows(instanceIds);
		
	}
	
}
