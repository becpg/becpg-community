package fr.becpg.repo.report.engine;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.service.cmr.dictionary.InvalidTypeException;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NoTransformerException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
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
 *
 */
@SuppressWarnings("deprecation")
public class ReportContentServiceWrapper implements ContentService {

	private static Log logger = LogFactory.getLog(ReportContentServiceWrapper.class);

	private ContentService contentService;

	private EntityReportService entityReportService;

	private NodeService nodeService;

	private TransactionService transactionService;

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@Override
	public long getStoreTotalSpace() {
		return contentService.getStoreTotalSpace();
	}

	@Override
	public long getStoreFreeSpace() {
		return contentService.getStoreFreeSpace();
	}

	@Override
	public ContentReader getRawReader(String contentUrl) {
		return contentService.getRawReader(contentUrl);
	}

	@Override
	public ContentReader getReader(NodeRef nodeRef, QName propertyQName) throws InvalidNodeRefException, InvalidTypeException {

		if (ReportModel.TYPE_REPORT.equals(nodeService.getType(nodeRef))) {

			if ((nodeRef != null) && nodeService.hasAspect(nodeRef, VirtualContentModel.ASPECT_VIRTUAL_DOCUMENT)) {
				nodeRef = new NodeRef((String) nodeService.getProperty(nodeRef, VirtualContentModel.PROP_ACTUAL_NODE_REF));
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

	@Override
	public ContentWriter getWriter(NodeRef nodeRef, QName propertyQName, boolean update) throws InvalidNodeRefException, InvalidTypeException {
		return contentService.getWriter(nodeRef, propertyQName, update);
	}

	@Override
	public ContentWriter getTempWriter() {
		return contentService.getTempWriter();
	}

	@Override
	public void transform(ContentReader reader, ContentWriter writer) throws NoTransformerException, ContentIOException {
		contentService.transform(reader, writer);

	}

	@Override
	public void transform(ContentReader reader, ContentWriter writer, Map<String, Object> options) throws NoTransformerException, ContentIOException {
		contentService.transform(reader, writer, options);

	}

	@Override
	public void transform(ContentReader reader, ContentWriter writer, TransformationOptions options)
			throws NoTransformerException, ContentIOException {
		contentService.transform(reader, writer, options);

	}

	@Override
	public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype) {

		return contentService.getTransformer(sourceMimetype, targetMimetype);
	}

	@Override
	public List<ContentTransformer> getTransformers(String sourceUrl, String sourceMimetype, long sourceSize, String targetMimetype,
			TransformationOptions options) {
		return contentService.getTransformers(sourceUrl, sourceMimetype, sourceSize, targetMimetype, options);
	}

	@Override
	public ContentTransformer getTransformer(String sourceUrl, String sourceMimetype, long sourceSize, String targetMimetype,
			TransformationOptions options) {
		return contentService.getTransformer(sourceUrl, sourceMimetype, sourceSize, targetMimetype, options);
	}

	@Override
	public ContentTransformer getTransformer(String sourceMimetype, String targetMimetype, TransformationOptions options) {
		return contentService.getTransformer(sourceMimetype, targetMimetype, options);
	}

	@Override
	public long getMaxSourceSizeBytes(String sourceMimetype, String targetMimetype, TransformationOptions options) {
		return contentService.getMaxSourceSizeBytes(sourceMimetype, targetMimetype, options);
	}

	@Override
	public List<ContentTransformer> getActiveTransformers(String sourceMimetype, long sourceSize, String targetMimetype,
			TransformationOptions options) {
		return contentService.getActiveTransformers(sourceMimetype, sourceSize, targetMimetype, options);
	}

	@Override
	public List<ContentTransformer> getActiveTransformers(String sourceMimetype, String targetMimetype, TransformationOptions options) {
		return contentService.getActiveTransformers(sourceMimetype, targetMimetype, options);
	}

	@Override
	public ContentTransformer getImageTransformer() {
		return contentService.getImageTransformer();
	}

	@Override
	public boolean isTransformable(ContentReader reader, ContentWriter writer) {
		return contentService.isTransformable(reader, writer);
	}

	@Override
	public boolean isTransformable(ContentReader reader, ContentWriter writer, TransformationOptions options) {
		return contentService.isTransformable(reader, writer, options);
	}

}
