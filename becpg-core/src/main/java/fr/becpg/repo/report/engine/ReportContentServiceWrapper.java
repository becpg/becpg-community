package fr.becpg.repo.report.engine;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.DirectAccessUrl;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 *
 * Override ou ContentReader ExporterComponent
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ReportContentServiceWrapper implements ContentService {

	private static Log logger = LogFactory.getLog(ReportContentServiceWrapper.class);

	private ContentService contentService;

	private EntityReportService entityReportService;

	private NodeService nodeService;

	private TransactionService transactionService;

	/**
	 * <p>
	 * Setter for the field <code>contentService</code>.
	 * </p>
	 *
	 * @param contentService a
	 *                       {@link org.alfresco.service.cmr.repository.ContentService}
	 *                       object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>
	 * Setter for the field <code>entityReportService</code>.
	 * </p>
	 *
	 * @param entityReportService a
	 *                            {@link fr.becpg.repo.report.entity.EntityReportService}
	 *                            object.
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/**
	 * <p>
	 * Setter for the field <code>nodeService</code>.
	 * </p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService}
	 *                    object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>
	 * Setter for the field <code>transactionService</code>.
	 * </p>
	 *
	 * @param transactionService a
	 *                           {@link org.alfresco.service.transaction.TransactionService}
	 *                           object.
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/** {@inheritDoc} */
	@Override
	public long getStoreTotalSpace() {
		return contentService.getStoreTotalSpace();
	}

	/** {@inheritDoc} */
	@Override
	public long getStoreFreeSpace() {
		return contentService.getStoreFreeSpace();
	}

	/** {@inheritDoc} */
	@Override
	public ContentReader getRawReader(String contentUrl) {
		return contentService.getRawReader(contentUrl);
	}

	/** {@inheritDoc} */
	@Override
	public ContentReader getReader(NodeRef nodeRef, QName propertyQName)
			throws InvalidNodeRefException, InvalidTypeException {

		if (ReportModel.TYPE_REPORT.equals(nodeService.getType(nodeRef))) {

			if ((nodeRef != null) && nodeService.hasAspect(nodeRef, VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT)) {
				nodeRef = new NodeRef(
						(String) nodeService.getProperty(nodeRef, VirtualContentModel.PROP_ACTUAL_NODE_REF));
			}

			final NodeRef finalNodeRef = nodeRef;

			NodeRef entityNodeRef = entityReportService.getEntityNodeRef(nodeRef);
			if ((entityNodeRef != null) && entityReportService.shouldGenerateReport(entityNodeRef, nodeRef)) {

				RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
				txnHelper.setForceWritable(true);
				boolean requiresNew = false;
				if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE) {
					// We can be in a read-only transaction, so force a new
					// transaction
					requiresNew = true;
				}
				txnHelper.doInTransaction(() -> AuthenticationUtil.runAs(() -> {

					logger.debug("refreshReport: Entity report is not up to date for " + entityNodeRef);

					entityReportService.generateReport(entityNodeRef, finalNodeRef);

					return finalNodeRef;

				}, AuthenticationUtil.getSystemUserName()), false, requiresNew);

			}
		}

		return contentService.getReader(nodeRef, propertyQName);
	}

	/** {@inheritDoc} */
	@Override
	public ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update)
			throws InvalidNodeRefException, InvalidTypeException {
		return contentService.getWriter(nodeRef, propertyQName, update);
	}

	/** {@inheritDoc} */
	@Override
	public ContentWriter getTempWriter() {
		return contentService.getTempWriter();
	}

	@Override
	public boolean isContentDirectUrlEnabled() {
		return contentService.isContentDirectUrlEnabled();
	}

	@Override
	public boolean isContentDirectUrlEnabled(NodeRef nodeRef, QName propertyQName) {
		return contentService.isContentDirectUrlEnabled(nodeRef, propertyQName);
	}

	@Override
	public DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, QName propertyQName, boolean attachment,
			Long validFor) {
		return contentService.requestContentDirectUrl(nodeRef, propertyQName, attachment, validFor);
	}

	@Override
	public DirectAccessUrl requestContentDirectUrl(NodeRef nodeRef, QName propertyQName, boolean attachment, Long validFor, String fileName) {
		return contentService.requestContentDirectUrl(nodeRef, propertyQName, attachment, validFor,fileName);
	}

}
