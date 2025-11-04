/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.jscript;

import java.util.Objects;
import java.util.function.Supplier;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 * Helper JScript extension that retrieves or creates thumbnails/reports for entities.
 *
 * Acts as a thin orchestration layer over {@link fr.becpg.repo.entity.EntityService} and {@link fr.becpg.repo.report.entity.EntityReportService}
 * with Alfresco transaction guard logic to avoid lock conflicts.
 *
 * @author matthieu,gaspard
 */
public final class Thumbnail extends BaseScopableProcessorExtension {

	private static final Log logger = LogFactory.getLog(Thumbnail.class);

	private NodeService nodeService;
	private EntityService entityService;
	private EntityReportService entityReportService;
	private ServiceRegistry serviceRegistry;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
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
	 * <p>Setter for the field <code>entityReportService</code>.</p>
	 *
	 * @param entityReportService a {@link fr.becpg.repo.report.entity.EntityReportService} object.
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/**
	 * <p>Setter for the field <code>serviceRegistry</code>.</p>
	 *
	 * @param serviceRegistry a {@link org.alfresco.service.ServiceRegistry} object.
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * <p>getThumbnailNode.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode getThumbnailNode(ScriptNode sourceNode) {

		QName type = nodeService.getType(sourceNode.getNodeRef());
		NodeRef img = null;
		if (entityService.hasAssociatedImages(type)) {
			try {
				img = entityService.getEntityDefaultImage(sourceNode.getNodeRef());
			} catch (BeCPGException e) {
				// debug only - absence of image is not fatal
				logger.debug("getThumbnailNode: error while retrieving default image", e);
			}

			if (img == null) {
				img = entityService.getEntityDefaultIcon(sourceNode.getNodeRef(), "thumb");
			}
		}

		return img != null ? new ScriptNode(img, serviceRegistry, getScope()) : sourceNode;
	}

	/**
	 * <p>getOrCreateImageNode.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode getOrCreateImageNode(ScriptNode sourceNode) {

		QName type = nodeService.getType(sourceNode.getNodeRef());
		NodeRef img = null;

		try {
			if (entityService.hasAssociatedImages(type)) {
				img = entityService.getEntityDefaultImage(sourceNode.getNodeRef());
			}
		} catch (BeCPGException e) {
			logger.debug("getOrCreateImageNode: error while getting default image", e);
		}

		if (img == null) {
			img = entityService.createDefaultImage(sourceNode.getNodeRef());

		}

		return img != null ? new ScriptNode(img, serviceRegistry, getScope()) : sourceNode;
	}

	/**
	 * <p>getReportNode.</p>
	 *
	 * @param sourceNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode getReportNode(ScriptNode sourceNode) {

		NodeRef sourceNodeRef = unwrapVirtualNodeIfNeeded(sourceNode.getNodeRef());

		NodeRef reportNodeRef = null;

		if (entityReportService.shouldGenerateReport(sourceNodeRef, null)) {
			// attempt to generate report in a writable context avoiding unnecessary nested writable transactions
			reportNodeRef = executeWithWritableTransaction(() -> {
				logger.debug("getReportNode: Entity report is not up to date for " + sourceNodeRef);
				NodeRef rnr = entityReportService.getSelectedReport(sourceNodeRef);
				if (rnr != null) {
					cleanThumbnails(rnr);
				}
				entityReportService.generateReports(sourceNodeRef);
				return entityReportService.getSelectedReport(sourceNodeRef);
			});
		}

		if (reportNodeRef == null) {
			reportNodeRef = entityReportService.getSelectedReport(sourceNode.getNodeRef());
		}

		return reportNodeRef != null ? new ScriptNode(reportNodeRef, serviceRegistry, getScope()) : sourceNode;
	}

	/**
	 * Force a refresh of an existing report node. Returns the refreshed report script node.
	 *
	 * @param reportNode a {@link org.alfresco.repo.jscript.ScriptNode} object
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object
	 */
	public ScriptNode refreshReport(ScriptNode reportNode) {

		NodeRef reportNodeRef = unwrapVirtualNodeIfNeeded(reportNode.getNodeRef());
		final NodeRef finalReportNodeRef = reportNodeRef;

		NodeRef entityNodeRef = entityReportService.getEntityNodeRef(reportNodeRef);
		if ((entityNodeRef != null) && entityReportService.shouldGenerateReport(entityNodeRef, reportNodeRef)) {
			executeWithWritableTransaction(() -> {
				logger.debug("refreshReport: Entity report is not up to date for " + entityNodeRef);
				entityReportService.generateReport(entityNodeRef, finalReportNodeRef);
				cleanThumbnails(finalReportNodeRef);
				return finalReportNodeRef;
			});
		}

		return new ScriptNode(reportNodeRef, serviceRegistry, getScope());
	}

	@SuppressWarnings("deprecation")
	private void cleanThumbnails(NodeRef reportNodeRef) {
		if (reportNodeRef == null) {
			return;
		}
		// Ensure thumbnail is regenerated before preview - handle multiple thumbnail types in one place
		String[] thumbnailNames = { "webpreview", "pdf" };
		for (String name : thumbnailNames) {
			NodeRef thumbNodeRef = serviceRegistry.getThumbnailService().getThumbnailByName(reportNodeRef, ContentModel.PROP_CONTENT, name);
			if (thumbNodeRef != null) {
				nodeService.deleteNode(thumbNodeRef);
				logger.debug("cleanThumbnails: deleted thumbnail " + name + " for " + reportNodeRef);
			}

		}
	}

	/**
	 * Helper to execute a supplier in a writable transaction and as system user. It avoids creating a new transaction if the current one is already writable.
	 *
	 * Returns the supplier result or null on failure.
	 */
	private <T> T executeWithWritableTransaction(Supplier<T> supplier) {
		Objects.requireNonNull(supplier, "supplier must not be null");
		// If already in a writable transaction we execute directly under system user
		TxnReadState currentTxnState = AlfrescoTransactionSupport.getTransactionReadState();
		if (currentTxnState == TxnReadState.TXN_READ_WRITE) {
			return AuthenticationUtil.runAs(() -> supplier.get(), AuthenticationUtil.getSystemUserName());
		} else {
			// create a new forced-writable transaction
			RetryingTransactionHelper txnHelper = serviceRegistry.getRetryingTransactionHelper();
			txnHelper.setForceWritable(true);
			return txnHelper.doInTransaction(() -> AuthenticationUtil.runAs(() -> supplier.get(), AuthenticationUtil.getSystemUserName()), false, // readOnly
					true // commit (force commit)
			);
		}
	}

	private NodeRef unwrapVirtualNodeIfNeeded(NodeRef nodeRef) {
		if (nodeRef == null) {
			return null;
		}
		if (nodeService.hasAspect(nodeRef, VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT)) {
			return new NodeRef((String) nodeService.getProperty(nodeRef, VirtualContentModel.PROP_ACTUAL_NODE_REF));
		}
		return nodeRef;
	}

}
