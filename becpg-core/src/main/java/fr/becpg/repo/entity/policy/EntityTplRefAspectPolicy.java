/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.rule.RuntimeRuleService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * The Class EntityFolderPolicy.
 * 
 * @author querephi
 */
public class EntityTplRefAspectPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnAddAspectPolicy {

	private static Log logger = LogFactory.getLog(EntityTplRefAspectPolicy.class);

	private EntityService entityService;

	private AssociationService associationService;

	private EntityTplService entityTplService;

	private EntityListDAO entityListDAO;

	private RuntimeRuleService ruleService;

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setRuleService(RuntimeRuleService ruleService) {
		this.ruleService = ruleService;
	}

	public void doInit() {
		logger.debug("Init EntityTplPolicy...");

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.ASPECT_ENTITY_TPL_REF,
				BeCPGModel.ASSOC_ENTITY_TPL_REF, new JavaBehaviour(this, "onCreateAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnAddAspectPolicy.QNAME, BeCPGModel.ASPECT_ENTITY_TPL_REF, new JavaBehaviour(this,
				"onAddAspect"));

		super.disableOnCopyBehaviour(BeCPGModel.ASPECT_ENTITY_TPL_REF);

	}

	@Override
	public void onAddAspect(NodeRef entityNodeRef, QName aspectTypeQName) {

		if (aspectTypeQName != null && aspectTypeQName.equals(BeCPGModel.ASPECT_ENTITY_TPL_REF)) {

			queueNode(entityNodeRef);
		}
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {

		NodeRef entityNodeRef = assocRef.getSourceRef();

		if (assocRef.getTypeQName().equals(BeCPGModel.ASSOC_ENTITY_TPL_REF)) {

			NodeRef entityTplNodeRef = assocRef.getTargetRef();

			try {
				((RuleService) ruleService).disableRules(entityNodeRef);

				// copy files
				entityService.copyFiles(entityTplNodeRef, entityNodeRef);

				// copy datalists
				entityListDAO.copyDataLists(entityTplNodeRef, entityNodeRef, false);

				// copy rules
				// Check whether the node already has rules or not
				if (nodeService.hasAspect(entityTplNodeRef, RuleModel.ASPECT_RULES) == true
						&& !((RuleService) ruleService).getRules(entityTplNodeRef, false).isEmpty()) {

					boolean error = false;
					if (nodeService.hasAspect(entityNodeRef, RuleModel.ASPECT_RULES) == true) {
						// Check for a linked to node
						NodeRef linkedToNode = ((RuleService) ruleService).getLinkedToRuleNode(entityNodeRef);
						if (linkedToNode == null) {
							// if the node has no rules we can delete the folder
							// ready to link
							List<Rule> rules = ((RuleService) ruleService).getRules(entityNodeRef, false);
							if (rules.isEmpty() == false) {
								// Can't link a node if it already has rules
								error = true;
							} else {
								// Delete the rules system folder
								NodeRef ruleFolder = ruleService.getSavedRuleFolderAssoc(entityNodeRef).getChildRef();
								nodeService.deleteNode(ruleFolder);
							}
						} else {
							// Just remove the aspect and have the associated
							// data automatically removed
							nodeService.removeAspect(entityNodeRef, RuleModel.ASPECT_RULES);
						}

					}
					if (!error) {

						// Create the destination folder as a secondary child of
						// the first
						NodeRef ruleSetNodeRef = ruleService.getSavedRuleFolderAssoc(entityTplNodeRef).getChildRef();
						// The required aspect will automatically be added to
						// the node
						nodeService.addChild(entityNodeRef, ruleSetNodeRef, RuleModel.ASSOC_RULE_FOLDER, RuleModel.ASSOC_RULE_FOLDER);

					} else {
						logger.error("The current folder has rules and can not be linked to another folder.");
					}

				}
				// copy missing aspects
				Set<QName> aspects = nodeService.getAspects(entityTplNodeRef);
				for (QName aspect : aspects) {
					if (!nodeService.hasAspect(entityNodeRef, aspect) && !BeCPGModel.ASPECT_ENTITY_TPL.isMatch(aspect)) {
						nodeService.addAspect(entityNodeRef, aspect, null);
					}
				}

			} finally {
				((RuleService) ruleService).enableRules(entityNodeRef);
			}
		}
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {

		for (NodeRef entityNodeRef : pendingNodes) {
			if (nodeService.exists(entityNodeRef)) {
				NodeRef entityTplNodeRef = associationService.getTargetAssoc(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF);

				if (entityTplNodeRef == null) {
					QName entityType = nodeService.getType(entityNodeRef);
					entityTplNodeRef = entityTplService.getEntityTpl(entityType);
					if (entityTplNodeRef != null && nodeService.exists(entityTplNodeRef)) {
						if (logger.isDebugEnabled()) {
							logger.debug("Found default entity template '" + nodeService.getProperty(entityTplNodeRef, ContentModel.PROP_NAME)
									+ "' to assoc.");
						}
						associationService.update(entityNodeRef, BeCPGModel.ASSOC_ENTITY_TPL_REF, entityTplNodeRef);
					}
				}
			}
		}
	}

}
