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
 * <p>Thumbnail class.</p>
 *
 * @author matthieu,gaspard
 * @version $Id: $Id
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
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
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
		// Try to find a logo for the specific type

		NodeRef img = null;

		if (entityService.hasAssociatedImages(type)) {

			try {
				img = entityService.getEntityDefaultImage(sourceNode.getNodeRef());

			} catch (BeCPGException e) {
				logger.debug(e, e);
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
		// Try to find a logo for the specific type

		NodeRef img = null;
		try {
			if (entityService.hasAssociatedImages(type)) {
				img = entityService.getEntityDefaultImage(sourceNode.getNodeRef());
			}
		} catch (BeCPGException e) {
			logger.debug(e, e);
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

		NodeRef sourceNodeRef = sourceNode.getNodeRef();

		if ((sourceNodeRef != null) && nodeService.hasAspect(sourceNodeRef, VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT)) {
			sourceNodeRef = new NodeRef((String) nodeService.getProperty(sourceNodeRef, VirtualContentModel.PROP_ACTUAL_NODE_REF));
		}

		final NodeRef finalNodeRef = sourceNodeRef;

		if (entityReportService.shouldGenerateReport(sourceNodeRef, null)) {

			RetryingTransactionHelper txnHelper = serviceRegistry.getRetryingTransactionHelper();
			txnHelper.setForceWritable(true);
			boolean requiresNew = false;
			if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE) {
				// We can be in a read-only transaction, so force a new
				// transaction
				requiresNew = true;
			}
			txnHelper.doInTransaction(() -> AuthenticationUtil.runAs(() -> {

				logger.debug("getReportNode: Entity report is not up to date for " + finalNodeRef);
				NodeRef reportNodeRef = entityReportService.getSelectedReport(finalNodeRef);
				if (reportNodeRef != null) {
					cleanThumbnails(reportNodeRef);
				}
				entityReportService.generateReports(finalNodeRef);

				return reportNodeRef;

			}, AuthenticationUtil.getSystemUserName()), false, requiresNew);

		}

		NodeRef reportNodeRef = entityReportService.getSelectedReport(sourceNode.getNodeRef());

		return reportNodeRef != null ? new ScriptNode(reportNodeRef, serviceRegistry, getScope()) : sourceNode;

	}

	/**
	 * <p>refreshReport.</p>
	 *
	 * @param reportNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode refreshReport(ScriptNode reportNode) {
		NodeRef reportNodeRef = reportNode.getNodeRef();

		if ((reportNodeRef != null) && nodeService.hasAspect(reportNodeRef, VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT)) {
			reportNodeRef = new NodeRef((String) nodeService.getProperty(reportNodeRef, VirtualContentModel.PROP_ACTUAL_NODE_REF));
		}

		final NodeRef finalNodeRef = reportNodeRef;

		NodeRef entityNodeRef = entityReportService.getEntityNodeRef(reportNodeRef);
		if ((entityNodeRef != null) && entityReportService.shouldGenerateReport(entityNodeRef, reportNodeRef)) {

			RetryingTransactionHelper txnHelper = serviceRegistry.getRetryingTransactionHelper();
			txnHelper.setForceWritable(true);
			boolean requiresNew = false;
			if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE) {
				// We can be in a read-only transaction, so force a new
				// transaction
				requiresNew = true;
			}
			return new ScriptNode(txnHelper.doInTransaction(() -> AuthenticationUtil.runAs(() -> {

				logger.debug("refreshReport: Entity report is not up to date for " + entityNodeRef);

				entityReportService.generateReport(entityNodeRef, finalNodeRef);
				cleanThumbnails(finalNodeRef);

				return finalNodeRef;

			}, AuthenticationUtil.getSystemUserName()), false, requiresNew), serviceRegistry, getScope());

		}

		return reportNode;

	}

	private void cleanThumbnails(NodeRef reportNodeRef) {

		// Ensure thumbnail is regenerated before preview
		NodeRef thumbNodeRef = serviceRegistry.getThumbnailService().getThumbnailByName(reportNodeRef, ContentModel.PROP_CONTENT, "webpreview");
		if (thumbNodeRef != null) {
			nodeService.deleteNode(thumbNodeRef);
		}

		thumbNodeRef = serviceRegistry.getThumbnailService().getThumbnailByName(reportNodeRef, ContentModel.PROP_CONTENT, "pdf");
		if (thumbNodeRef != null) {
			nodeService.deleteNode(thumbNodeRef);
		}

	}

}
