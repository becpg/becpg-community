package fr.becpg.repo.report.search.actions;

import java.io.InputStream;
import java.util.Locale;

import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.download.AbstractDownloadExporter;

/**
 * <p>Abstract AbstractSearchDownloadExporter class.</p>
 *
 * @author matthieu
 */
public abstract class AbstractSearchDownloadExporter extends AbstractDownloadExporter implements Exporter {

	/**
	 * The template node reference for the report template.
	 */
	protected NodeRef templateNodeRef;

	/**
	 * <p>Constructor for AbstractSearchDownloadExporter.</p>
	 *
	 * @param transactionHelper a {@link org.alfresco.repo.transaction.RetryingTransactionHelper} object
	 * @param updateService a {@link org.alfresco.repo.download.DownloadStatusUpdateService} object
	 * @param downloadStorage a {@link org.alfresco.repo.download.DownloadStorage} object
	 * @param downloadNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param nbOfLines a {@link java.lang.Long} object
	 */
	protected AbstractSearchDownloadExporter(RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService,
			DownloadStorage downloadStorage,
			NodeRef downloadNodeRef, NodeRef templateNodeRef, Long nbOfLines) {

		super(transactionHelper,updateService, downloadStorage, downloadNodeRef, nbOfLines );
		
		this.templateNodeRef = templateNodeRef;

	}

	/** {@inheritDoc} */
	@Override
	public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endACL(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endAspect(NodeRef nodeRef, QName aspect) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endAspects(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endAssoc(NodeRef nodeRef, QName assoc) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endAssocs(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endNamespace(String prefix) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endNode(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endProperties(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endProperty(NodeRef nodeRef, QName property) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endReference(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endValueCollection(NodeRef nodeRef, QName property) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void endValueMLText(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void permission(NodeRef nodeRef, AccessPermission permission) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startACL(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startAspect(NodeRef nodeRef, QName aspect) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startAspects(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startAssoc(NodeRef nodeRef, QName assoc) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startAssocs(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startNamespace(String prefix, String uri) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startProperties(NodeRef nodeRef) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startProperty(NodeRef nodeRef, QName property) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startReference(NodeRef nodeRef, QName childName) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startValueCollection(NodeRef nodeRef, QName property) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void value(NodeRef nodeRef, QName property, Object value, int index) {
		// Empty method

	}

	/** {@inheritDoc} */
	@Override
	public void warning(String warning) {
		// Empty method

	}

}
