package fr.becpg.repo.product.policy;

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.EntityListState;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PLMWorkflowModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.policy.CodePolicy;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>EntityCopyPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityCopyPolicy extends AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyCompletePolicy {

	private static final Log logger = LogFactory.getLog(CodePolicy.class);

	private NamespaceService namespaceService;
	
	private String propertiesToReset;
	
	private EntityService entityService;
	
	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>propertiesToReset</code>.</p>
	 *
	 * @param propertiesToReset a {@link java.lang.String} object.
	 */
	public void setPropertiesToReset(String propertiesToReset) {
		this.propertiesToReset = propertiesToReset;
	}

	/**
	 * <p>Setter for the field <code>entityService</code>.</p>
	 *
	 * @param entityService a {@link fr.becpg.repo.entity.EntityService} object.
	 */
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init ProductPolicy...");
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "onCopyComplete"));

	}

	/** {@inheritDoc} */
	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {

		if (policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)
				&& policyBehaviourFilter.isEnabled(sourceNodeRef, ContentModel.ASPECT_AUDITABLE) && !isWorkingCopyOrVersion(destinationRef)
				&& !isWorkingCopyOrVersion(sourceNodeRef)

		) {
			
			if(nodeService.hasAspect(destinationRef, BeCPGModel.ASPECT_ERP_CODE)) {
				nodeService.setProperty(destinationRef, BeCPGModel.PROP_ERP_CODE, null);
			}
			
			entityService.changeEntityListStates(destinationRef, EntityListState.ToValidate);
			
			
			if(propertiesToReset!=null) {
		        for(String propertyToKeep : propertiesToReset.split(",")) {	        	
		        	QName propertyQname = QName.createQName(propertyToKeep,namespaceService );	
		        	nodeService.removeProperty(destinationRef, propertyQname);
		        }
	        }
			
			if(nodeService.hasAspect(destinationRef, PLMModel.ASPECT_PRODUCT)) {
				nodeService.setProperty(destinationRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation);
			}
			
			if (nodeService.hasAspect(destinationRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT)) {
				nodeService.removeAspect(destinationRef, PLMWorkflowModel.ASPECT_PRODUCT_VALIDATION_ASPECT);
			}
	
			if(nodeService.hasAspect(destinationRef,  ContentModel.ASPECT_VERSIONABLE)) {
				nodeService.removeAspect(destinationRef, ContentModel.ASPECT_VERSIONABLE);
			}
			
			if (nodeService.hasAspect(destinationRef, BeCPGModel.ASPECT_ENTITY_BRANCH)) {
				nodeService.removeAspect(destinationRef, BeCPGModel.ASPECT_ENTITY_BRANCH);
			}
		}

	}

}
