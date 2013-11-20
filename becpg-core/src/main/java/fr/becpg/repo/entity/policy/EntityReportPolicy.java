/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.report.entity.EntityReportAsyncGenerator;

/**
 * Generate documents when product properties are updated.
 * 
 * @author querephi, matthieu
 */
@Service
public class EntityReportPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityReportPolicy.class);

	private EntityReportAsyncGenerator entityReportAsyncGenerator;

	public void setEntityReportAsyncGenerator(EntityReportAsyncGenerator entityReportAsyncGenerator) {
		this.entityReportAsyncGenerator = entityReportAsyncGenerator;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {

		logger.debug("Init EntityReportPolicy...");

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
				"onCreateAssociation"));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
				"onDeleteAssociation"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this, "onUpdateProperties"));
	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {

		onUpdateProduct(assocRef.getSourceRef());
	}

	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {

		onUpdateProduct(assocRef.getSourceRef());
	}

	
	@Override
	public void onUpdateProperties(NodeRef entityNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		for (Map.Entry<QName, Serializable> kv : after.entrySet()) {

			boolean hasChanged = false;

			if (kv.getValue() == null) {
				if (before.get(kv.getKey()) != null) {
					hasChanged = true;
				}
			} else if (!kv.getValue().equals(before.get(kv.getKey()))) {
				hasChanged = true;
			}

			// generate report depending of properties updated
			if (hasChanged && !ReportModel.PROP_REPORT_ENTITY_GENERATED.equals(kv.getKey()) && !ContentModel.PROP_MODIFIED.equals(kv.getKey())
					&& !ContentModel.PROP_MODIFIER.equals(kv.getKey()) && !ContentModel.PROP_VERSION_LABEL.equals(kv.getKey())
					&& !ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA.equals(kv.getKey())) {

				if (logger.isDebugEnabled()) {
					logger.debug("Generate report since prop has changed. Prop: " + kv.getKey() + " before: " + before.get(kv.getKey()) + " after: " + kv.getValue());
				}

				onUpdateProduct(entityNodeRef);
				return;
			}
		}
	}

	private void onUpdateProduct(NodeRef entityNodeRef) {
		if (nodeService.exists(entityNodeRef) && !nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL) && isNotLocked(entityNodeRef)
				&& !isVersionStoreNode(entityNodeRef)) {
			queueNode(entityNodeRef);
		}
	}

	
	@Override
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {

		entityReportAsyncGenerator.queueNodes(new ArrayList<>(pendingNodes));

	}

}
