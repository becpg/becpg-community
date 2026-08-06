/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.repo.score;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * Invalidates the cached score definitions when one, or one of its sub lists, changes.
 *
 * <p>Without it a definition created by an administrator, or imported as reference data,
 * would only be taken into account after a restart, and the score it defines would silently
 * never be published.</p>
 *
 * @author matthieu
 */
public class ScoreDefinitionPolicy extends AbstractBeCPGPolicy
		implements NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdateNodePolicy, NodeServicePolicies.OnDeleteNodePolicy,
		OnUpdatePropertiesPolicy {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(ScoreDefinitionPolicy.class);

	/** Constant <code>WATCHED_TYPES</code> */
	private static final List<QName> WATCHED_TYPES = List.of(PLMModel.TYPE_SCORE_DEFINITION, PLMModel.TYPE_SCORE_THRESHOLD_LIST,
			PLMModel.TYPE_SCORE_BADGE_LIST, PLMModel.TYPE_SCORE_DEF_COEFF_LIST);

	private ScoreDefinitionService scoreDefinitionService;

	/**
	 * <p>Setter for the field <code>scoreDefinitionService</code>.</p>
	 *
	 * @param scoreDefinitionService a {@link fr.becpg.repo.score.ScoreDefinitionService} object
	 */
	public void setScoreDefinitionService(ScoreDefinitionService scoreDefinitionService) {
		this.scoreDefinitionService = scoreDefinitionService;
	}

	/**
	 * <p>doInit.</p>
	 */
	public void doInit() {
		for (QName type : WATCHED_TYPES) {
			policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, type, new JavaBehaviour(this, "onCreateNode"));
			policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, type, new JavaBehaviour(this, "onUpdateNode"));
			policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, type, new JavaBehaviour(this, "onDeleteNode"));
			policyComponent.bindClassBehaviour(OnUpdatePropertiesPolicy.QNAME, type, new JavaBehaviour(this, "onUpdateProperties"));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		queueNode(childAssocRef.getChildRef());
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateNode(NodeRef nodeRef) {
		queueNode(nodeRef);
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		queueNode(childAssocRef.getChildRef());
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		queueNode(nodeRef);
	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		if (!pendingNodes.isEmpty()) {
			logger.debug("Clean score definition cache");
			scoreDefinitionService.clearCache();
		}
		return true;
	}

}
