package fr.becpg.repo.toxicology;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>IngToxPolicy class.</p>
 *
 * @author matthieu
 */
public class IngToxPolicy extends AbstractBeCPGPolicy implements OnUpdatePropertiesPolicy {

	private Repository repository;
	
	private RepoService repoService;
	
	private ToxicologyService toxicologyService;
	
	/**
	 * <p>Setter for the field <code>toxicologyService</code>.</p>
	 *
	 * @param toxicologyService a {@link fr.becpg.repo.toxicology.ToxicologyService} object
	 */
	public void setToxicologyService(ToxicologyService toxicologyService) {
		this.toxicologyService = toxicologyService;
	}
	
	/**
	 * <p>Setter for the field <code>repository</code>.</p>
	 *
	 * @param repository a {@link org.alfresco.repo.model.Repository} object
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	/**
	 * <p>Setter for the field <code>repoService</code>.</p>
	 *
	 * @param repoService a {@link fr.becpg.repo.helper.RepoService} object
	 */
	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
	}
	
	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, PLMModel.TYPE_ING,
				new JavaBehaviour(this, "onUpdateProperties"));
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (before.containsKey(PLMModel.PROP_ING_TOX_DATA) && (boolean) before.get(PLMModel.PROP_ING_TOX_DATA)) {
			return;
		}
		if (after.containsKey(PLMModel.PROP_ING_TOX_DATA)	&& (boolean) after.get(PLMModel.PROP_ING_TOX_DATA)) {
			queueNode(nodeRef);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		NodeRef companyHomeNodeRef = repository.getCompanyHome();
		NodeRef systemNodeRef = repoService.getFolderByPath(companyHomeNodeRef, RepoConsts.PATH_SYSTEM);
		NodeRef charactsNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_CHARACTS);
		NodeRef listContainer = nodeService.getChildByName(charactsNodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
		NodeRef toxFolder = nodeService.getChildByName(listContainer, ContentModel.ASSOC_CONTAINS, PlmRepoConsts.PATH_TOXICITIES);
		List<NodeRef> toxList = nodeService.getChildAssocs(toxFolder).stream().map(c -> c.getChildRef()).toList();
		
		for (NodeRef ingNodeRef : pendingNodes) {
			for (NodeRef toxNodeRef : toxList) {
				toxicologyService.createOrUpdateToxIngNodeRef(ingNodeRef, toxNodeRef);
			}
			nodeService.setProperty(ingNodeRef, PLMModel.PROP_ING_TOX_DATA, false);
		}
		
		return true;
	}


}
