/*
 *
 */
package fr.becpg.repo.entity.policy;

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * The Class EntityFolderPolicy.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class EntityTplRefAspectPolicy extends AbstractBeCPGPolicy
		implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnAddAspectPolicy, NodeServicePolicies.BeforeDeleteNodePolicy {

	private static final Log logger = LogFactory.getLog(EntityTplRefAspectPolicy.class);

	private AssociationService associationService;

	private EntityTplService entityTplService;
	
	private AttributeExtractorService attributeExtractorService;
	
	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>entityTplService</code>.</p>
	 *
	 * @param entityTplService a {@link fr.becpg.repo.entity.EntityTplService} object.
	 */
	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		logger.debug("Init EntityTplPolicy...");

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				BeCPGModel.ASSOC_ENTITY_TPL_REF, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_ENTITY_TPL_REF,
				new JavaBehaviour(this, "onAddAspect"));
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, BeCPGModel.ASPECT_ENTITY_TPL,
				new JavaBehaviour(this, "beforeDeleteNode"));

		super.disableOnCopyBehaviour(BeCPGModel.ASPECT_ENTITY_TPL_REF);

	}

	/** {@inheritDoc} */
	@Override
	public void onAddAspect(NodeRef entityNodeRef, QName aspectTypeQName) {

		if ((aspectTypeQName != null) && aspectTypeQName.equals(BeCPGModel.ASPECT_ENTITY_TPL_REF)) {
			queueNode(entityNodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		if (policyBehaviourFilter.isEnabled(BeCPGModel.ASPECT_ENTITY_TPL_REF)  && policyBehaviourFilter.isEnabled(assocRef.getSourceRef(), BeCPGModel.ASPECT_ENTITY_TPL_REF)) {

			NodeRef entityNodeRef = assocRef.getSourceRef();

			if (assocRef.getTypeQName().equals(BeCPGModel.ASSOC_ENTITY_TPL_REF)) {

				NodeRef entityTplNodeRef = assocRef.getTargetRef();
				if (logger.isDebugEnabled()) {
					logger.debug("Call synchronizeEntity with template '" + nodeService.getProperty(entityTplNodeRef, ContentModel.PROP_NAME)
							+ "' for entity " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
				}

				entityTplService.synchronizeEntity(entityNodeRef, entityTplNodeRef);

			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef entityNodeRef : pendingNodes) {
			if (nodeService.exists(entityNodeRef) && !isWorkingCopyOrVersion(entityNodeRef) ) {
				NodeRef entityTplNodeRef = associationService.getTargetAssoc(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);

				if (entityTplNodeRef == null) {
					QName entityType = nodeService.getType(entityNodeRef);
					entityTplNodeRef = entityTplService.getEntityTpl(entityType);
					if ((entityTplNodeRef != null) && nodeService.exists(entityTplNodeRef)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Found default entity template '" + nodeService.getProperty(entityTplNodeRef, ContentModel.PROP_NAME)
									+ "' to assoc to " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
						}
						associationService.update(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF, entityTplNodeRef);
					}
				}
			}
		}
		return true;
	}

	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		
		List<NodeRef> sourcesAssocs = associationService
				.getEntitySourceAssocs(List.of(nodeRef), BeCPGModel.ASSOC_ENTITY_TPL_REF, null, true, null, new PagingRequest(5)).stream()
				.map(e -> e.getEntityNodeRef()).toList();
		
		if (sourcesAssocs != null && !sourcesAssocs.isEmpty()) {
			
			StringBuilder sb = new StringBuilder();

			for (NodeRef sourceNodeRef : sourcesAssocs) {
				sb.append("\n").append(decorate(sourceNodeRef));
			}
			
			throw new IllegalStateException(I18NUtil.getMessage("integrity-checker.association-multiplicity-error", sb.toString()));
		}
		
	}
	
	private String decorate(NodeRef sourceNodeRef) {

		if (nodeService.exists(sourceNodeRef)) {
			String ret = attributeExtractorService.extractPropName(sourceNodeRef);
			String code = (String) nodeService.getProperty(sourceNodeRef, BeCPGModel.PROP_CODE);
			if (code != null && !code.isEmpty()) {
				ret = code + " - " + ret;
			} else {
				ret = ret + " (" + sourceNodeRef + ")";
			}
			return ret;
		}

		return "";
	}
	
}
